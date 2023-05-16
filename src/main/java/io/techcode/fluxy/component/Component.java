package io.techcode.fluxy.component;

import io.techcode.fluxy.pipeline.Pipeline;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public abstract class Component extends AbstractVerticle {

  protected final Pipeline pipeline;
  protected int retry;

  public Component(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    vertx.setPeriodic(250, evt -> {
      if (pipeline.isFlushed()) {
        stopPromise.complete();
      }
      if (retry++ > 120) {
        stopPromise.fail("Failed to flush pipeline");
      }
    });
  }

  public boolean isBlocking() {
    return false;
  }

}
