package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Handler;

public abstract class Sink extends Component implements Handler<Void> {

  protected Pipe in;
  protected Mailbox eventMailbox;

  public Sink() {
    in = new Pipe();
  }

  @Override
  public void start() {
    eventMailbox = new Mailbox(vertx.getOrCreateContext(), this);
    in.addEventHandler(eventMailbox);
    in.pullOne(this::onPush);
  }

  @Override
  public void handle(Void event) {
    eventMailbox.reset();
    onPull();
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
      .toString();
  }

}
