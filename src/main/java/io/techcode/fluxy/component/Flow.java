package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import io.techcode.fluxy.event.Event;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public abstract class Flow extends AbstractVerticle implements Component, Handler<Void>, Consumer<Event> {

  protected final Pipe in;
  protected final Pipe out;
  protected Mailbox eventMailbox;
  protected Mailbox lowPressureMailbox;
  protected Mailbox highPressureMailbox;

  public Flow(Pipe in, Pipe out) {
    this.in = in;
    this.out = out;
  }

  @Override public void start() {
    lowPressureMailbox = new Mailbox(vertx.getOrCreateContext(), this::onLowPressure);
    highPressureMailbox = new Mailbox(vertx.getOrCreateContext(), this::onHighPressure);
    eventMailbox = new Mailbox(vertx.getOrCreateContext(), this);
    in.setEventHandler(eventMailbox);
    out.setLowPressureHandler(lowPressureMailbox);
    out.setHighPressureHandler(highPressureMailbox);
  }

  protected void onLowPressure(Void evt) {
    lowPressureMailbox.reset();
  }

  protected void onHighPressure(Void evt) {
    highPressureMailbox.reset();
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
