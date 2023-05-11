package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import io.techcode.fluxy.event.Event;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public abstract class Sink extends AbstractVerticle implements Component, Handler<Void>, Consumer<Event> {

  protected Pipe in;
  protected Mailbox eventMailbox;

  public Sink() {
    in = new Pipe();
  }

  @Override public void start() {
    eventMailbox = new Mailbox(vertx.getOrCreateContext(), this);
    in.setEventHandler(eventMailbox);
    in.pullOne(this);
  }

  @Override public void handle(Void event) {
    eventMailbox.reset();
  }

  @Override public void accept(Event evt) {
    // Do nothing
  }

  public Pipe in() {
    return in;
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("in", in)
      .toString();
  }

}
