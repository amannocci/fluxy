package io.techcode.fluxy.module.std;

import com.typesafe.config.Config;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.event.Event;

public class StdoutSink extends Sink {

  public StdoutSink(Config options) {
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
