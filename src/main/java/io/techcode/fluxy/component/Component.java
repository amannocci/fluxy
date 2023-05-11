package io.techcode.fluxy.component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public abstract class Component extends AbstractVerticle {

  private Promise<Void> isStopping;

  @Override
  public void stop(Promise<Void> isStopping) {
    this.isStopping = isStopping;
  }

  public boolean isStopping() {
    return isStopping != null;
  }

  public void shutdown() {
    if (isStopping != null) isStopping.complete();
  }

  public boolean isBlocking() {
    return false;
  }

}
