package io.techcode.fluxy.module.stress;

import io.techcode.fluxy.component.Pipe;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public class BlackholeSink extends Sink implements Handler<Void>, Consumer<Event> {

  public BlackholeSink(Pipe in) {
    super(in);
  }

  @Override
  public void handle(Void evt) {
    super.handle(evt);
    in.pullMany(this);
  }

}
