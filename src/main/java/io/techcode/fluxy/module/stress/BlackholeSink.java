package io.techcode.fluxy.module.stress;

import com.typesafe.config.Config;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public class BlackholeSink extends Sink implements Handler<Void>, Consumer<Event> {


  public BlackholeSink(Config options) {
  }

  @Override
  public void handle(Void evt) {
    super.handle(evt);
    in.pullMany(this);
  }

}
