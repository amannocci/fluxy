package io.techcode.fluxy.module.stress;

import com.google.common.collect.Iterators;
import com.typesafe.config.Config;
import io.techcode.fluxy.component.Source;
import io.techcode.fluxy.event.Event;
import io.techcode.fluxy.pipeline.Pipeline;
import io.vertx.core.json.JsonObject;

import java.util.Iterator;

public class GeneratorSource extends Source {

  private final Iterator<String> lines;

  public GeneratorSource(Pipeline pipeline, Config options) {
    super(pipeline);
    this.lines = Iterators.cycle(options.getStringList("lines"));
  }

  @Override
  public boolean isBlocking() {
    return true;
  }

  @Override
  protected void onPipeAvailable(Void evt) {
    super.onPipeAvailable(evt);
    onPush();
  }

  protected void onPush() {
    vertx.runOnContext(a -> {
      int remainingCapacity = out.remainingCapacity();
      for (int i = 0; i < remainingCapacity; i++) {
        out.pushOne(new Event(new JsonObject().put("message", lines.next())));
      }
      onPush();
    });
  }

}
