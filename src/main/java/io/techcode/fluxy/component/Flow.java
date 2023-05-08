package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import io.techcode.fluxy.event.Event;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public abstract class Flow extends AbstractVerticle implements Component, Handler<Void>, Consumer<Event> {

  protected final Pipe in;
  protected final Pipe out;
  protected Mailbox eventMailbox;
  protected Mailbox pipeAvailableMailbox;
  protected Mailbox pipeUnavailableMailbox;

  public Flow(Pipe in, Pipe out) {
    this.in = in;
    this.out = out;
  }

  @Override public void start() {
    Context ctx = vertx.getOrCreateContext();
    eventMailbox = new Mailbox(ctx, this);
    pipeAvailableMailbox = new Mailbox(ctx, this::onPipeAvailable);
    pipeUnavailableMailbox = new Mailbox(ctx, this::onPipeUnavailable);
    in.setEventHandler(eventMailbox);
    out.setAvailableHandler(pipeAvailableMailbox);
    out.setUnavailableHandler(pipeUnavailableMailbox);
  }

  protected void onPipeAvailable(Void evt) {
    pipeAvailableMailbox.reset();
  }

  protected void onPipeUnavailable(Void evt) {
    pipeUnavailableMailbox.reset();
  }

  @Override public void handle(Void evt) {
    eventMailbox.reset();
  }

  @Override public void accept(Event evt) {
    // Do nothing
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("in", in)
      .add("out", out)
      .toString();
  }

}
