package io.techcode.fluxy.module.std;

import com.typesafe.config.Config;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.event.Event;
import io.techcode.fluxy.pipeline.Pipeline;

public class StdoutSink extends Sink {

  public StdoutSink(Pipeline pipeline, Config options) {
    super(pipeline);
  }

  @Override
  public boolean isBlocking() {
    return true;
  }

  @Override
  public void onPull() {
    in.pullMany(this::onPush);
  }

  @Override
  protected void onPush(Event evt) {
    System.out.println(evt.payload());
  }

}
