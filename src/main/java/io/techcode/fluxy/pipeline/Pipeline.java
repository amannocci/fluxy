package io.techcode.fluxy.pipeline;

import com.google.common.collect.Iterators;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.typesafe.config.Config;
import io.techcode.fluxy.component.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;

import java.util.*;
import java.util.stream.Collectors;

public class Pipeline extends AbstractVerticle {

  private final Config conf;
  private final Map<String, Source> sources;
  private final Map<String, Flow> flows;
  private final Map<String, Sink> sinks;
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

  private Graph<String> generateGraphView() {
    // Attempt to build a graph
    var graphView = GraphBuilder.directed().allowsSelfLoops(false).<String>build();

    // Build initial graph
    var section = conf.getConfig("sources");
    for (var entry : section.root().entrySet()) {
      graphView.addNode(entry.getKey());
    }

    if (conf.hasPath("flows")) {
      section = conf.getConfig("flows");
      for (var entry : section.root().entrySet()) {
        var componentConf = section.getConfig(entry.getKey());
        for (var input : componentConf.getStringList("inputs")) {
          if (!uniqueIds.contains(input))
            throw new IllegalArgumentException("Unknown `" + input + "` for `" + entry.getKey() + "`");
          graphView.putEdge(input, entry.getKey());
        }
      }
    }

    section = conf.getConfig("sinks");
    for (var entry : section.root().entrySet()) {
      var componentConf = section.getConfig(entry.getKey());
      for (var input : componentConf.getStringList("inputs")) {
        if (!uniqueIds.contains(input))
          throw new IllegalArgumentException("Unknown `" + input + "` for `" + entry.getKey() + "`");
        graphView.putEdge(input, entry.getKey());
      }
    }
    return graphView;
  }

  private void generatePipeline(Graph<String> graphView) {
    // Build initial graph
    var section = conf.getConfig("sources");
    for (var entry : section.root().entrySet()) {
      var componentId = entry.getKey();
      var componentConf = section.getConfig(componentId);
      sources.put(
        componentId,
        ComponentRegistry.INSTANCE.createSource(
          componentConf.getString("type"),
          new ComponentConfig(Optional.empty(), Optional.of(new Pipe()), componentConf.getConfig("options"))
        )
      );
    }

    if (conf.hasPath("flows")) {
      section = conf.getConfig("flows");
      for (var entry : section.root().entrySet()) {
        var componentId = entry.getKey();
        var componentConf = section.getConfig(componentId);

        // TODO: Support for more than one input
        var inputs = graphView.predecessors(entry.getKey());
        if (inputs.size() > 1) throw new IllegalStateException("A flow can only be connected to one input");

        var input = Iterators.get(inputs.iterator(), 0);
        if (sources.containsKey(input)) {
          var source = sources.get(input);
          flows.put(
            componentId,
            ComponentRegistry.INSTANCE.createFlow(
              componentConf.getString("type"),
              new ComponentConfig(Optional.of(source.out), Optional.of(new Pipe()), componentConf.getConfig("options"))
            )
          );
        } else {
          var flow = flows.get(input);
          flows.put(
            componentId,
            ComponentRegistry.INSTANCE.createFlow(
              componentConf.getString("type"),
              new ComponentConfig(Optional.of(flow.out), Optional.of(new Pipe()), componentConf.getConfig("options"))
            )
          );
        }
      }
    }

    section = conf.getConfig("sinks");
    for (var entry : section.root().entrySet()) {
      var componentId = entry.getKey();
      var componentConf = section.getConfig(componentId);

      // TODO: Support for more than one input
      var inputs = graphView.predecessors(entry.getKey());
      if (inputs.size() > 1) throw new IllegalStateException("A sink can only be connected to one input");

      var input = Iterators.get(inputs.iterator(), 0);
      if (sources.containsKey(input)) {
        var source = sources.get(input);
        sinks.put(
          componentId,
          ComponentRegistry.INSTANCE.createSink(
            componentConf.getString("type"),
            new ComponentConfig(Optional.of(source.out), Optional.empty(), componentConf.getConfig("options"))
          )
        );
      } else {
        var flow = flows.get(input);
        sinks.put(
          componentId,
          ComponentRegistry.INSTANCE.createSink(
            componentConf.getString("type"),
            new ComponentConfig(Optional.of(flow.out), Optional.empty(), componentConf.getConfig("options"))
          )
        );
      }
    }
  }

  @Override
  public void start(Promise<Void> startPromise) {
    CompositeFuture.join(sources.values()
        .stream()
        .map(source -> vertx.deployVerticle(source))
        .collect(Collectors.toList())
      ).compose(e -> CompositeFuture.join(flows.values()
        .stream()
        .map(flow -> vertx.deployVerticle(flow))
        .collect(Collectors.toList()))
      ).compose(e -> CompositeFuture.join(sinks.values()
        .stream()
        .map(sink -> vertx.deployVerticle(sink))
        .collect(Collectors.toList()))
      )
      .onFailure(startPromise::fail)
      .onSuccess(e -> startPromise.complete());
  }

}
