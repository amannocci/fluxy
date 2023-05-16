package io.techcode.fluxy.component;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import io.techcode.fluxy.module.core.MutateFlow;
import io.techcode.fluxy.module.std.StdoutSink;
import io.techcode.fluxy.module.stress.BlackholeSink;
import io.techcode.fluxy.module.stress.GeneratorSource;
import io.techcode.fluxy.module.tcp.TcpSource;
import io.techcode.fluxy.pipeline.Pipeline;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ComponentRegistry {

  public final static ComponentRegistry INSTANCE = new ComponentRegistry();

  private final ImmutableMap<String, BiFunction<Pipeline, Config, Source>> sourceRegistry;
  private final ImmutableMap<String, BiFunction<Pipeline, Config, Flow>> flowRegistry;
  private final ImmutableMap<String, BiFunction<Pipeline, Config, Sink>> sinkRegistry;

  private ComponentRegistry() {
    sourceRegistry = ImmutableMap.<String, BiFunction<Pipeline, Config, Source>>builder()
      .put("tcp", TcpSource::new)
      .put("generator", GeneratorSource::new)
      .build();
    flowRegistry = ImmutableMap.<String, BiFunction<Pipeline, Config, Flow>>builder()
      .put("mutate", MutateFlow::new)
      .build();
    sinkRegistry = ImmutableMap.<String, BiFunction<Pipeline, Config, Sink>>builder()
      .put("blackhole", BlackholeSink::new)
      .put("stdout", StdoutSink::new)
      .build();
  }

  public Source createSource(String type, Pipeline pipeline, Config conf) {
    var factory = sourceRegistry.get(type);
    if (factory == null) throw new IllegalArgumentException("Invalid `" + type + "` type");
    return factory.apply(pipeline, conf);
  }

  public Flow createFlow(String type, Pipeline pipeline, Config conf) {
    var factory = flowRegistry.get(type);
    if (factory == null) throw new IllegalArgumentException("Invalid `" + type + "` type");
    return factory.apply(pipeline, conf);
  }

  public Sink createSink(String type, Pipeline pipeline, Config conf) {
    var factory = sinkRegistry.get(type);
    if (factory == null) throw new IllegalArgumentException("Invalid `" + type + "` type");
    return factory.apply(pipeline, conf);
  }

}
