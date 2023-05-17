package io.techcode.fluxy.component;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.techcode.fluxy.pipeline.Pipeline;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public abstract class Component extends AbstractVerticle {

  protected final Pipeline pipeline;
  protected final Config options;
  protected int retry;

  public Component(Pipeline pipeline) {
    this(pipeline, null);
  }

  public Component(Pipeline pipeline, Config options) {
    this.pipeline = pipeline;
    if (options != null) {
      this.options = options.withFallback(defaultOptions());
      onOptionsValidate(options);
    } else {
      this.options = null;
    }
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

  protected Config defaultOptions() {
    return ConfigFactory.empty();
  }

  protected void onOptionsValidate(Config options) {
    // No default validation
  }

  public boolean isBlocking() {
    return false;
  }

}
