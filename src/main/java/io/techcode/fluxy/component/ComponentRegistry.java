package io.techcode.fluxy.component;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import io.techcode.fluxy.module.core.MutateFlow;
import io.techcode.fluxy.module.std.StdoutSink;
import io.techcode.fluxy.module.stress.BlackholeSink;
import io.techcode.fluxy.module.stress.GeneratorSource;
import io.techcode.fluxy.module.tcp.TcpSource;

import java.util.function.Function;
import java.util.function.Supplier;

public class ComponentRegistry {

  public final static ComponentRegistry INSTANCE = new ComponentRegistry();

  private final ImmutableMap<String, Function<Config, Source>> sourceRegistry;
  private final ImmutableMap<String, Function<Config, Flow>> flowRegistry;
  private final ImmutableMap<String, Function<Config, Sink>> sinkRegistry;

  private ComponentRegistry() {
    sourceRegistry = ImmutableMap.<String, Function<Config, Source>>builder()
      .put("tcp", TcpSource::new)
      .put("generator", GeneratorSource::new)
      .build();
    flowRegistry = ImmutableMap.<String, Function<Config, Flow>>builder()
      .put("mutate", MutateFlow::new)
      .build();
    sinkRegistry = ImmutableMap.<String, Function<Config, Sink>>builder()
      .put("blackhole", BlackholeSink::new)
      .put("stdout", StdoutSink::new)
      .build();
  }

  public Source createSource(String type, Config conf) {
    var factory = sourceRegistry.get(type);
    if (factory == null) throw new IllegalArgumentException("Invalid `" + type + "` type");
    return factory.apply(conf);
  }

  public Flow createFlow(String type, Config conf) {
    var factory = flowRegistry.get(type);
    if (factory == null) throw new IllegalArgumentException("Invalid `" + type + "` type");
    return factory.apply(conf);
  }

  public Sink createSink(String type, Config conf) {
    var factory = sinkRegistry.get(type);
    if (factory == null) throw new IllegalArgumentException("Invalid `" + type + "` type");
    return factory.apply(conf);
  }

}
