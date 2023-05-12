package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Context;
import io.vertx.core.Handler;

public class Merge extends Component implements Handler<Void> {

  protected Pipe in;
  protected Pipe out;
  protected Mailbox eventMailbox;
  protected Mailbox pipeAvailableMailbox;
  protected Mailbox pipeUnavailableMailbox;

  public Merge() {
    in = new Pipe(Pipe.DEFAULT_CAPACITY * 2, true);
  }

  @Override
  public void start() {
    Preconditions.checkNotNull(in, "Merge isn't connected");
    Preconditions.checkNotNull(out, "Merge isn't connected");
    Context ctx = vertx.getOrCreateContext();
    eventMailbox = new Mailbox(ctx, this);
    pipeAvailableMailbox = new Mailbox(ctx, this::onPipeAvailable);
    pipeUnavailableMailbox = new Mailbox(ctx, this::onPipeUnavailable);
    in.addEventHandler(eventMailbox);
    out.addAvailableHandler(pipeAvailableMailbox);
    out.addUnavailableHandler(pipeUnavailableMailbox);
  }

  protected void onPipeAvailable(Void evt) {
    pipeAvailableMailbox.reset();
    onPull();
  }

  protected void onPipeUnavailable(Void evt) {
    pipeUnavailableMailbox.reset();
  }

  @Override
  public void handle(Void evt) {
    eventMailbox.reset();
    onPull();

    // Handle the case where the mailbox is notified by more than one thread.
    // In that case, we can miss the event and wait indefinitely.
    // To avoid this issue, we trigger another dispatch.
    if (in.nonEmpty()) {
      eventMailbox.dispatch();
    }
  }

  public void onPull() {
    in.pullMany(this::onPush, out.remainingCapacity());
  }

  // onPush
  public void onPush(Event evt) {
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
