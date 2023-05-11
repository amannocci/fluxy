package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.techcode.fluxy.event.Event;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public class Merge extends AbstractVerticle implements Component, Handler<Void>, Consumer<Event> {

  protected Pipe in;
  protected Pipe out;
  protected Mailbox eventMailbox;
  protected Mailbox pipeAvailableMailbox;
  protected Mailbox pipeUnavailableMailbox;

  public Merge(int numberOfInputs) {
    in = new Pipe(Pipe.DEFAULT_CAPACITY * numberOfInputs, true);
  }

  @Override
  public void start() {
    Preconditions.checkNotNull(in, "Merge isn't connected");
    Preconditions.checkNotNull(out, "Merge isn't connected");
    Context ctx = vertx.getOrCreateContext();
    eventMailbox = new Mailbox(ctx, this);
    pipeAvailableMailbox = new Mailbox(ctx, this::onPipeAvailable);
    pipeUnavailableMailbox = new Mailbox(ctx, this::onPipeUnavailable);
    in.setEventHandler(eventMailbox);
    out.setAvailableHandler(pipeAvailableMailbox);
    out.setUnavailableHandler(pipeUnavailableMailbox);
  }

  public Pipe in() {
    return this.in;
  }

  public Pipe out() {
    return this.out;
  }

  public void connectTo(Pipe pipe) {
    out = pipe;
  }

  protected void onPipeAvailable(Void evt) {
    pipeAvailableMailbox.reset();
    in.pullMany(this, out.remainingCapacity());
  }

  protected void onPipeUnavailable(Void evt) {
    pipeUnavailableMailbox.reset();
  }

  @Override
  public void handle(Void evt) {
    eventMailbox.reset();
    in.pullMany(this, out.remainingCapacity());
  }

  @Override
  public void accept(Event evt) {
    out.pushOne(evt);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("in", in)
      .add("out", out)
      .toString();
  }

}
