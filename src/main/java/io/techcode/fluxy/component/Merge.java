package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public class Merge extends Component implements Handler<Void>, Consumer<Event> {

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
    in.pullMany(this, out.remainingCapacity());
  }

  protected void onPipeUnavailable(Void evt) {
    pipeUnavailableMailbox.reset();
  }

  @Override
  public void handle(Void evt) {
    eventMailbox.reset();
    in.pullMany(this, out.remainingCapacity());

    // Handle the case where the mailbox is notified by more than one thread.
    // In that case, we can miss the event and wait indefinitely.
    // To avoid this issue, we trigger another dispatch.
    if (in.nonEmpty()) {
      eventMailbox.dispatch();
    } else {
      // Handle shutdown
      if (isStopping()) shutdown();
    }
  }

  // onPush
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
