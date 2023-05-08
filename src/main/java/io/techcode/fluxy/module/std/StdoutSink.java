package io.techcode.fluxy.module.std;

import com.google.common.base.MoreObjects;
import io.techcode.fluxy.component.Pipe;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public class StdoutSink extends Sink implements Handler<Void>, Consumer<Event> {

  public StdoutSink(Pipe in) {
    super(in);
  }

  @Override
  public boolean isBlocking() {
    return true;
  }

  @Override public void handle(Void evt) {
    super.handle(evt);
    in.pullMany(this);
  }

  @Override public void accept(Event evt) {
    System.out.println(evt.getPayload());
  }

}
