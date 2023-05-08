package io.techcode.fluxy.module.stress;

import io.techcode.fluxy.component.ComponentConfig;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public class BlackholeSink extends Sink implements Handler<Void>, Consumer<Event> {

  public BlackholeSink(ComponentConfig conf) {
    super(conf.in().orElseThrow());
  }

  @Override
  public void handle(Void evt) {
    super.handle(evt);
    in.pullMany(this);
  }

}
