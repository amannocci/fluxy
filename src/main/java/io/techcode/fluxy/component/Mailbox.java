package io.techcode.fluxy.component;

import io.vertx.core.Context;
import io.vertx.core.Handler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Mailbox {

  private final Context ctx;
  private final Handler<Void> handler;
  private final AtomicBoolean isNotified;

  public Mailbox(Context ctx, Handler<Void> handler) {
    this.ctx = ctx;
    this.handler = handler;
    this.isNotified = new AtomicBoolean(false);
  }

  public void dispatch() {
    if (!isNotified.getAndSet(true)) {
      ctx.runOnContext(handler);
    }
  }

  public void reset() {
    isNotified.set(false);
  }

}
