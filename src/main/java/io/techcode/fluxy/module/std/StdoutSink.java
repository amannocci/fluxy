package io.techcode.fluxy.module.std;

import com.typesafe.config.Config;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public class StdoutSink extends Sink implements Handler<Void>, Consumer<Event> {

  public StdoutSink(Config options) {
  }

  @Override
  public boolean isBlocking() {
    return true;
  }

  @Override
  public void handle(Void evt) {
    super.handle(evt);
    in.pullMany(this);

    // Handle shutdown
    if (isStopping() && in.isEmpty()) {
      shutdown();
    }
  }

  @Override
  public void accept(Event evt) {
    System.out.println(evt.payload());
  }

}
