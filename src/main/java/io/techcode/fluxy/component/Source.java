package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import io.vertx.core.AbstractVerticle;

public abstract class Source extends AbstractVerticle implements Component {

  protected final Pipe out;
  protected Mailbox lowPressureMailbox;
  protected Mailbox highPressureMailbox;

  public Source(Pipe out) {
    this.out = out;
  }

  @Override public void start() {
    lowPressureMailbox = new Mailbox(vertx.getOrCreateContext(), this::onLowPressure);
    highPressureMailbox = new Mailbox(vertx.getOrCreateContext(), this::onHighPressure);
    out.setLowPressureHandler(lowPressureMailbox);
    out.setHighPressureHandler(highPressureMailbox);
  }

  protected void onLowPressure(Void event) {
    lowPressureMailbox.reset();
  }

  protected void onHighPressure(Void event) {
    highPressureMailbox.reset();
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("out", out)
      .toString();
  }

}
