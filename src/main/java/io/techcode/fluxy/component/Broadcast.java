package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Context;
import io.vertx.core.Handler;

import java.util.List;

public class Broadcast extends Component implements Handler<Void> {

  protected final Pipe in;
  protected final List<Pipe> outs;
  protected Mailbox eventMailbox;
  protected Mailbox pipeAvailableMailbox;
  protected Mailbox pipeUnavailableMailbox;

  public Broadcast(int numberOfOutputs) {
    in = new Pipe();
    outs = Lists.newArrayListWithExpectedSize(numberOfOutputs);
  }

  @Override
  public void start() {
    Preconditions.checkNotNull(in, "Broadcast isn't connected");
    Preconditions.checkNotNull(outs, "Broadcast isn't connected");
    Context ctx = vertx.getOrCreateContext();
    eventMailbox = new Mailbox(ctx, this);
    pipeAvailableMailbox = new Mailbox(ctx, this::onPipeAvailable);
    pipeUnavailableMailbox = new Mailbox(ctx, this::onPipeUnavailable);
    in.addEventHandler(eventMailbox);
    for (var out : outs) {
      out.addAvailableHandler(pipeAvailableMailbox);
      out.addUnavailableHandler(pipeUnavailableMailbox);
    }
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
  }

  protected void onPull() {
    var maxEventsToPull = outs.stream().mapToInt(Pipe::remainingCapacity).min().orElse(0);
    if (maxEventsToPull > 0) in.pullMany(this::onPush, maxEventsToPull);
  }

  protected void onPush(Event evt) {
    for (var out : outs) {
      out.pushOne(evt.copy());
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("in", in)
      .add("outs", outs)
      .toString();
  }

}
