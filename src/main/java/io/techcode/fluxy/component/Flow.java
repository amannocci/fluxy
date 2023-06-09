package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import io.techcode.fluxy.event.Event;
import io.techcode.fluxy.pipeline.Pipeline;
import io.vertx.core.Context;
import io.vertx.core.Handler;

public abstract class Flow extends Component implements Handler<Void> {

  protected Pipe in;
  protected Pipe out;
  protected Mailbox eventMailbox;
  protected Mailbox pipeAvailableMailbox;
  protected Mailbox pipeUnavailableMailbox;

  public Flow(Pipeline pipeline, Config options) {
    super(pipeline, options);
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

    // Handle the case where the mailbox was notified.
    // We can miss the event and wait indefinitely.
    // To avoid this issue, we trigger another dispatch.
    if (in.nonEmpty()) {
      eventMailbox.dispatch();
    }
  }

  protected void onPull() {
    // Do nothing
  }

  protected void onPush(Event evt) {
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
