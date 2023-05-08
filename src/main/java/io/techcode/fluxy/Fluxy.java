package io.techcode.fluxy;

import com.google.common.collect.Lists;
import io.techcode.fluxy.component.Flow;
import io.techcode.fluxy.component.Pipe;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.component.Source;
import io.techcode.fluxy.module.core.MutateFlow;
import io.techcode.fluxy.module.stress.BlackholeSink;
import io.techcode.fluxy.module.stress.GeneratorSource;
import io.techcode.fluxy.module.tcp.TcpSource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

public class Fluxy extends AbstractVerticle {

  @Override
  public void start() {
    vertx.exceptionHandler(Throwable::printStackTrace);

    Pipe pipe1 = new Pipe();
    Pipe pipe2 = new Pipe();
    //Source source = new GeneratorSource(pipe1, Lists.newArrayList("test"));
    Source source = new TcpSource(pipe1);
    Flow flow = new MutateFlow(pipe1, pipe2);
    Sink sink = new BlackholeSink(pipe2);
    Future<String> sourcesDeployed = vertx.deployVerticle(source, new DeploymentOptions().setWorker(source.isBlocking()));
    sourcesDeployed
      .flatMap(e -> vertx.deployVerticle(flow, new DeploymentOptions().setWorker(flow.isBlocking())))
      .onSuccess(e -> vertx.deployVerticle(sink, new DeploymentOptions().setWorker(sink.isBlocking())));
  }

}
