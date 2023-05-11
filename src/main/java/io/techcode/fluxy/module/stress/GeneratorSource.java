package io.techcode.fluxy.module.stress;

import com.google.common.collect.Iterators;
import com.typesafe.config.Config;
import io.techcode.fluxy.component.Source;
import io.techcode.fluxy.event.Event;
import io.vertx.core.json.JsonObject;

import java.util.Iterator;

public class GeneratorSource extends Source {

  private final Iterator<String> lines;
  private boolean isClosing = false;

  public GeneratorSource(Config options) {
    this.lines = Iterators.cycle(options.getStringList("lines"));
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    this.isClosing = false;
  }

  @Override
  public boolean isBlocking() {
    return true;
  }

  @Override
  protected void onPipeAvailable(Void evt) {
    super.onPipeAvailable(evt);
    generateEvents();
  }

  private void generateEvents() {
    if (isClosing) return;
    vertx.runOnContext(a -> {
      int remainingCapacity = out.remainingCapacity();
      for (int i = 0; i < remainingCapacity; i++) {
        out.pushOne(new Event(new JsonObject().put("message", lines.next())));
      }
      generateEvents();
    });
  }

}
