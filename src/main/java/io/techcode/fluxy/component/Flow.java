package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.techcode.fluxy.event.Event;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import org.jctools.queues.MessagePassingQueue.Consumer;

public abstract class Flow extends Component implements Handler<Void>, Consumer<Event> {

  protected Pipe in;
  protected Pipe out;
  protected Mailbox eventMailbox;
  protected Mailbox pipeAvailableMailbox;
  protected Mailbox pipeUnavailableMailbox;

  public Flow() {
    in = new Pipe();
  }

  @Override
  public void start() {
    Preconditions.checkNotNull(in, "Flow isn't connected");
    Preconditions.checkNotNull(out, "Flow isn't connected");
    Context ctx = vertx.getOrCreateContext();
    eventMailbox = new Mailbox(ctx, this);
    pipeAvailableMailbox = new Mailbox(ctx, this::onPipeAvailable);
    pipeUnavailableMailbox = new Mailbox(ctx, this::onPipeUnavailable);
    in.addEventHandler(eventMailbox);
    out.addAvailableHandler(pipeAvailableMailbox);
    out.addUnavailableHandler(pipeUnavailableMailbox);
  }

  public void connectTo(Pipe pipe) {
    out = pipe;
  }

  public Pipe in() {
    return in;
  }

  public Pipe out() {
    return out;
  }

  protected void onPipeAvailable(Void evt) {
    pipeAvailableMailbox.reset();
  }

  protected void onPipeUnavailable(Void evt) {
    pipeUnavailableMailbox.reset();
  }

  @Override
  public void handle(Void evt) {
    eventMailbox.reset();
  }

  @Override
  public void accept(Event evt) {
    // Do nothing
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("in", in)
      .add("out", out)
      .toString();
  }

}
