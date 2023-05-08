package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

public abstract class Source extends AbstractVerticle implements Component {

  protected final Pipe out;
  protected Mailbox pipeAvailableMailbox;
  protected Mailbox pipeUnavailableMailbox;

  public Source(Pipe out) {
    this.out = out;
  }

  @Override public void start() {
    Context ctx = vertx.getOrCreateContext();
    pipeAvailableMailbox = new Mailbox(ctx, this::onPipeAvailable);
    pipeUnavailableMailbox = new Mailbox(ctx, this::onPipeUnavailable);
    out.setAvailableHandler(pipeAvailableMailbox);
    out.setUnavailableHandler(pipeUnavailableMailbox);
  }

  protected void onPipeAvailable(Void event) {
    pipeAvailableMailbox.reset();
  }

  protected void onPipeUnavailable(Void event) {
    pipeUnavailableMailbox.reset();
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("out", out)
      .toString();
  }

}
