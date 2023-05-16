package io.techcode.fluxy.module.stress;

import com.typesafe.config.Config;
import io.techcode.fluxy.component.Sink;
import io.techcode.fluxy.pipeline.Pipeline;

public class BlackholeSink extends Sink {


  public BlackholeSink(Pipeline pipeline, Config options) {
    super(pipeline);
  }

  @Override
  protected void onPull() {
    in.pullMany(this::onPush);
  }

}
