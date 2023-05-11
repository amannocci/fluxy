package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;

public abstract class Source extends AbstractVerticle implements Component {

  protected Mailbox pipeAvailableMailbox;
  protected Mailbox pipeUnavailableMailbox;
  protected Pipe out;

  @Override
  public void start() {
    Preconditions.checkNotNull(out, "Source isn't connected");
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

  public void connectTo(Pipe pipe) {
    out = pipe;
  }

  public Pipe out() {
    return out;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("out", out)
      .toString();
  }

}
