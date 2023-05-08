package io.techcode.fluxy.module.core;

import io.techcode.fluxy.component.Flow;
import io.techcode.fluxy.component.Pipe;
import io.techcode.fluxy.event.Event;
import io.vertx.core.Handler;
import org.jctools.queues.MessagePassingQueue.Consumer;

public class MutateFlow extends Flow implements Handler<Void>, Consumer<Event> {
  public MutateFlow(Pipe in, Pipe out) {
    super(in, out);
  }

  @Override
  public void accept(Event evt) {
    evt.getPayload().put("mutate", "test");
    out.pushOne(evt.copy());
  }

  @Override protected void onPipeAvailable(Void evt) {
    super.onPipeAvailable(evt);
    in.pullMany(this, out.remainingCapacity());
  }

  @Override public void handle(Void evt) {
    super.handle(evt);
    in.pullMany(this, out.remainingCapacity());
  }

}
