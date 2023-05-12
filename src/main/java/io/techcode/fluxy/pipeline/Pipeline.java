package io.techcode.fluxy.pipeline;

import com.google.common.collect.Lists;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.typesafe.config.Config;
import io.techcode.fluxy.component.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Pipeline extends AbstractVerticle {

  private final Config conf;
  private final Map<String, SourceComponent> sources;
  private final Map<String, FlowComponent> flows;
  private final Map<String, SinkComponent> sinks;
  private Set<String> uniqueIds;

  public Pipeline(Config conf) {
    this.conf = conf;
    sources = new HashMap<>();
    flows = new HashMap<>();
    sinks = new HashMap<>();
    checkUniqueIds();
    var graphView = generateGraphView();
    generatePipeline(graphView);
  }

  private void checkUniqueIds() {
    var uniqueIds = new HashSet<String>();
    checkComponentUniqueIds(uniqueIds, "sources", true);
    checkComponentUniqueIds(uniqueIds, "flows", false);
    checkComponentUniqueIds(uniqueIds, "sinks", true);
    this.uniqueIds = uniqueIds;
    System.out.println(uniqueIds);
  }

  private void checkComponentUniqueIds(Set<String> uniqueIds, String sectionName, boolean shouldExists) {
    if (!conf.hasPath(sectionName) && shouldExists) throw new IllegalArgumentException("Invalid configuration");
    if (conf.hasPath(sectionName)) {
      var section = conf.getObject(sectionName);
      for (var entry : section.entrySet()) {
        if (uniqueIds.contains(entry.getKey()))
          throw new IllegalArgumentException("Duplicate component id `" + entry.getKey() + "`");
        uniqueIds.add(entry.getKey());
      }
    }
  }

  private MutableGraph<String> generateGraphView() {
    // Attempt to build a graph
    var graphView = GraphBuilder.directed().allowsSelfLoops(false).<String>build();

    // Build initial graph
    var section = conf.getConfig("sources");
    for (var entry : section.root().entrySet()) {
      var componentId = entry.getKey();
      var componentConf = section.getConfig(componentId);
      graphView.addNode(componentId);
      sources.put(
        componentId,
        new SourceComponent(
          ComponentRegistry.INSTANCE.createSource(
            componentConf.getString("type"),
            componentConf.getConfig("options")
          )
        )
      );
    }

    if (conf.hasPath("flows")) {
      section = conf.getConfig("flows");
      for (var entry : section.root().entrySet()) {
        var componentId = entry.getKey();
        var componentConf = section.getConfig(componentId);
        for (var input : componentConf.getStringList("inputs")) {
          if (!uniqueIds.contains(input))
            throw new IllegalArgumentException("Unknown `" + input + "` for `" + entry.getKey() + "`");
          graphView.putEdge(input, entry.getKey());
          flows.put(
            componentId,
            new FlowComponent(
              ComponentRegistry.INSTANCE.createFlow(
                componentConf.getString("type"),
                componentConf.getConfig("options")
              )
            )
          );
        }
      }
    }

    section = conf.getConfig("sinks");
    for (var entry : section.root().entrySet()) {
      var componentId = entry.getKey();
      var componentConf = section.getConfig(componentId);
      for (var input : componentConf.getStringList("inputs")) {
        if (!uniqueIds.contains(input))
          throw new IllegalArgumentException("Unknown `" + input + "` for `" + entry.getKey() + "`");
        graphView.putEdge(input, entry.getKey());
        sinks.put(
          componentId,
          new SinkComponent(
            ComponentRegistry.INSTANCE.createSink(
              componentConf.getString("type"),
              componentConf.getConfig("options")
            )
          )
        );
      }
    }
    return graphView;
  }

  private void generateInitialSourcePipeline(MutableGraph<String> graphView) {
    for (var entry : sources.entrySet()) {
      var componentId = entry.getKey();
      var component = entry.getValue();

      var outputs = graphView.successors(componentId);
      var numberOfOutputs = outputs.size();
      if (numberOfOutputs == 0) {
        throw new IllegalStateException("Invalid pipeline");
      } else if (numberOfOutputs > 1) {
        var broadcast = new Broadcast(numberOfOutputs);
        component.fanOut = Optional.of(broadcast);
        Linker.connectTo(component.source, broadcast);
      }
    }
  }

  private void generateInitialFlowPipeline(MutableGraph<String> graphView) {
    for (var entry : flows.entrySet()) {
      var componentId = entry.getKey();
      var component = entry.getValue();

      var inputs = graphView.predecessors(componentId);
      var outputs = graphView.successors(componentId);
      var numberOfInputs = inputs.size();
      var numberOfOutputs = outputs.size();

      if (numberOfInputs == 0 || numberOfOutputs == 0) {
        throw new IllegalStateException("Invalid pipeline");
      }
      if (numberOfInputs > 1) {
        var merge = new Merge();
        component.fanIn = Optional.of(merge);
        Linker.connectTo(merge, component.flow);
      }
      if (numberOfOutputs > 1) {
        var broadcast = new Broadcast(numberOfOutputs);
        component.fanOut = Optional.of(broadcast);
        Linker.connectTo(component.flow, broadcast);
      }
    }
  }

  private void generateInitialSinkPipeline(MutableGraph<String> graphView) {
    for (var entry : sinks.entrySet()) {
      var componentId = entry.getKey();
      var component = entry.getValue();

      var inputs = graphView.predecessors(componentId);
      var numberOfInputs = inputs.size();

      if (numberOfInputs == 0) {
        throw new IllegalStateException("Invalid pipeline");
      } else if (numberOfInputs > 1) {
        var merge = new Merge();
        component.fanIn = Optional.of(merge);
        Linker.connectTo(merge, component.sink);
      }
    }
  }

  private void generatePipeline(MutableGraph<String> graphView) {
    // Build initial pipeline components
    generateInitialSourcePipeline(graphView);
    generateInitialFlowPipeline(graphView);
    generateInitialSinkPipeline(graphView);

    // Connect components
    for (var entry : sources.entrySet()) {
      var componentId = entry.getKey();
      var component = entry.getValue();

      var outputs = graphView.successors(componentId);
      for (var output : outputs) {
        if (flows.containsKey(output)) {
          var flow = flows.get(output);
          component.connectTo(flow);
        } else {
          var sink = sinks.get(output);
          component.connectTo(sink);
        }
      }
    }

    for (var entry : flows.entrySet()) {
      var componentId = entry.getKey();
      var component = entry.getValue();

      var outputs = graphView.successors(componentId);
      for (var output : outputs) {
        if (flows.containsKey(output)) {
          var flow = flows.get(output);
          component.connectTo(flow);
        } else {
          var sink = sinks.get(output);
          component.connectTo(sink);
        }
      }
    }
  }

  @Override
  public void start(Promise<Void> startPromise) {
    CompositeFuture.join(sources.values()
        .stream()
        .flatMap(SourceComponent::verticles)
        .map(verticle -> vertx.deployVerticle(verticle))
        .collect(Collectors.toList())
      ).compose(e -> CompositeFuture.join(flows.values()
        .stream()
        .flatMap(FlowComponent::verticles)
        .map(verticle -> vertx.deployVerticle(verticle))
        .collect(Collectors.toList()))
      ).compose(e -> CompositeFuture.join(sinks.values()
        .stream()
        .flatMap(SinkComponent::verticles)
        .map(verticle -> vertx.deployVerticle(verticle))
        .collect(Collectors.toList()))
      )
      .onFailure(startPromise::fail)
      .onSuccess(e -> startPromise.complete());
  }

  private static class SourceComponent {
    Source source;
    Optional<Broadcast> fanOut;

    public SourceComponent(Source source) {
      this.source = source;
      fanOut = Optional.empty();
    }

    public void connectTo(FlowComponent component) {
      if (fanOut.isEmpty() && component.fanIn.isEmpty()) {
        Linker.connectTo(source, component.flow);
      } else if (fanOut.isPresent() && component.fanIn.isEmpty()) {
        Linker.connectTo(fanOut.get(), component.flow);
      } else if (fanOut.isEmpty()) {
        Linker.connectTo(source, component.fanIn.get());
      } else {
        Linker.connectTo(fanOut.get(), component.fanIn.get());
      }
    }

    public void connectTo(SinkComponent component) {
      if (fanOut.isEmpty() && component.fanIn.isEmpty()) {
        Linker.connectTo(source, component.sink);
      } else if (fanOut.isPresent() && component.fanIn.isEmpty()) {
        Linker.connectTo(fanOut.get(), component.sink);
      } else if (fanOut.isEmpty()) {
        Linker.connectTo(source, component.fanIn.get());
      } else {
        Linker.connectTo(fanOut.get(), component.fanIn.get());
      }
    }

    public Stream<Verticle> verticles() {
      var verticles = Lists.<Verticle>newArrayList(source);
      fanOut.ifPresent(verticles::add);
      return verticles.stream();
    }
  }

  private static class FlowComponent {
    Optional<Merge> fanIn;
    Flow flow;
    Optional<Broadcast> fanOut;

    public FlowComponent(Flow flow) {
      fanIn = Optional.empty();
      this.flow = flow;
      fanOut = Optional.empty();
    }

    public void connectTo(FlowComponent component) {
      if (fanOut.isEmpty() && component.fanIn.isEmpty()) {
        Linker.connectTo(flow, component.flow);
      } else if (fanOut.isPresent() && component.fanIn.isEmpty()) {
        Linker.connectTo(fanOut.get(), component.flow);
      } else if (fanOut.isEmpty()) {
        Linker.connectTo(flow, component.fanIn.get());
      } else {
        Linker.connectTo(fanOut.get(), component.fanIn.get());
      }
    }

    public void connectTo(SinkComponent component) {
      if (fanOut.isEmpty() && component.fanIn.isEmpty()) {
        Linker.connectTo(flow, component.sink);
      } else if (fanOut.isPresent() && component.fanIn.isEmpty()) {
        Linker.connectTo(fanOut.get(), component.sink);
      } else if (fanOut.isEmpty()) {
        Linker.connectTo(flow, component.fanIn.get());
      } else {
        Linker.connectTo(fanOut.get(), component.fanIn.get());
      }
    }

    public Stream<Verticle> verticles() {
      var verticles = Lists.<Verticle>newArrayList(flow);
      fanIn.ifPresent(verticles::add);
      fanOut.ifPresent(verticles::add);
      return verticles.stream();
    }
  }

  private static class SinkComponent {
    Optional<Merge> fanIn;
    Sink sink;

    public SinkComponent(Sink sink) {
      fanIn = Optional.empty();
      this.sink = sink;
    }

    public Stream<Verticle> verticles() {
      var verticles = Lists.<Verticle>newArrayList(sink);
      fanIn.ifPresent(verticles::add);
      return verticles.stream();
    }
  }

}
