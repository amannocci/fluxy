package io.techcode.fluxy.module.stress;

import com.typesafe.config.Config;
import io.techcode.fluxy.component.Sink;

public class BlackholeSink extends Sink {


  public BlackholeSink(Config options) {
  }

  @Override
  protected void onPull() {
    in.pullMany(this::onPush);
  }

}
