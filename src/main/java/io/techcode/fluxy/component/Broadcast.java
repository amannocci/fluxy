package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.techcode.fluxy.event.Event;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

import java.util.List;

public class Broadcast extends AbstractVerticle implements Component, Handler<Void>, Consumer<Event> {

  private final Pipe in;
  private final List<Pipe> outs;
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
    in.setEventHandler(eventMailbox);
    for (var out : outs) {
      out.setAvailableHandler(pipeAvailableMailbox);
      out.setUnavailableHandler(pipeUnavailableMailbox);
    }
  }

  public Pipe in() {
    return in;
  }

  public void connectTo(Pipe pipe) {
    outs.add(pipe);
  }

  protected void onPipeAvailable(Void evt) {
    pipeAvailableMailbox.reset();
    var maxEventsToPull = outs.stream().mapToInt(Pipe::remainingCapacity).min().orElse(0);
    if (maxEventsToPull > 0) in.pullMany(this, maxEventsToPull);
  }

  protected void onPipeUnavailable(Void evt) {
    pipeUnavailableMailbox.reset();
  }

  @Override
  public void handle(Void evt) {
    eventMailbox.reset();
    var maxEventsToPull = outs.stream().mapToInt(Pipe::remainingCapacity).min().orElse(0);
    if (maxEventsToPull > 0) in.pullMany(this, maxEventsToPull);
  }

  @Override
  public void accept(Event evt) {
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
