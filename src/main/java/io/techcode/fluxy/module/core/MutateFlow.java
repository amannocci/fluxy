package io.techcode.fluxy.module.core;

import com.typesafe.config.Config;
import io.techcode.fluxy.component.Flow;
import io.techcode.fluxy.event.Event;
import io.techcode.fluxy.pipeline.Pipeline;

public class MutateFlow extends Flow {

  public MutateFlow(Pipeline pipeline, Config options) {
    super(pipeline, options);
  }

  @Override
  protected void onPull() {
    in.pullMany(this::onPush, out.remainingCapacity());
  }

  @Override
  protected void onPush(Event evt) {
    evt.payload().put("mutate", "test");
    out.pushOne(evt);
  }

}
