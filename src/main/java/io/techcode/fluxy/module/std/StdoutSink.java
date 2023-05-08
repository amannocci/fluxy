package io.techcode.fluxy.module.std;

import io.techcode.fluxy.component.ComponentConfig;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public class StdoutSink extends Sink implements Handler<Void>, Consumer<Event> {

  public StdoutSink(ComponentConfig conf) {
    super(conf.in().orElseThrow());
  }

  @Override
  public boolean isBlocking() {
    return true;
  }

  @Override
  public void handle(Void evt) {
    super.handle(evt);
    in.pullMany(this);
  }

  @Override
  public void accept(Event evt) {
    System.out.println(evt.payload());
  }

}
