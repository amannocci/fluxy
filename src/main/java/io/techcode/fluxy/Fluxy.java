package io.techcode.fluxy;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.techcode.fluxy.pipeline.Pipeline;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.Optional;

public class Fluxy extends AbstractVerticle {

  private Optional<String> mainPipelineId = Optional.empty();

  @Override
  public void start(Promise<Void> startPromise){
    vertx.exceptionHandler(Throwable::printStackTrace);

    Config conf = ConfigFactory.load();
    Pipeline pipeline = new Pipeline(conf.getConfig("pipeline"));
    vertx.deployVerticle(pipeline)
      .onSuccess(id -> {
        mainPipelineId = Optional.of(id);
        startPromise.complete();
      })
      .onFailure(err -> {
        err.printStackTrace();
        startPromise.fail(err);
        vertx.close();
      });
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    var undeploy = mainPipelineId.map(id -> vertx.undeploy(id)).orElse(Future.succeededFuture());
    undeploy
      .onSuccess(e -> stopPromise.complete())
      .onFailure(stopPromise::fail);
  }

}
