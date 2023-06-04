package io.techcode.fluxy.module.core;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import io.techcode.fluxy.component.Flow;
import io.techcode.fluxy.event.Event;
import io.techcode.fluxy.pipeline.Pipeline;
import io.vertx.core.json.pointer.JsonPointer;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class MutateFlow extends Flow {

  private final List<Consumer<Event>> tasks;

  public MutateFlow(Pipeline pipeline, Config options) {
    super(pipeline, options);
    tasks = Lists.newArrayList();
    var tasksConf = options.getConfig("tasks");
    if (tasksConf.hasPath("set")) {
      tasks.add(new SetTask(tasksConf.getConfig("set")));
    }
  }

  @Override
  protected void onPull() {
    in.pullMany(this::onPush, out.remainingCapacity());
  }

  @Override
  protected void onPush(Event evt) {
    for (var task : tasks) {
      task.accept(evt);
    }
    out.pushOne(evt);
  }

  private static class SetTask implements Consumer<Event> {

    private final StringSubstitutor interpolator;
    private final JsonPointer targetPointer;
    private final String targetValue;
    private Event currentEvt;

    public SetTask(Config options) {
      var lookups = StringLookupFactory.INSTANCE.interpolatorStringLookup(new HashMap<>() {{
        put(StringLookupFactory.KEY_ENV, StringLookupFactory.INSTANCE.environmentVariableStringLookup());
        put(StringLookupFactory.KEY_JAVA, StringLookupFactory.INSTANCE.javaPlatformStringLookup());
        put(StringLookupFactory.KEY_SYS, StringLookupFactory.INSTANCE.systemPropertyStringLookup());
        put("eventPayload", StringLookupFactory.INSTANCE.functionStringLookup(new Function<String, String>() {
          @Override
          public String apply(String key) {
            var pointer = JsonPointer.from(key);
            return String.valueOf(pointer.queryJson(currentEvt.payload()));
          }
        }));
      }}, StringLookupFactory.INSTANCE.nullStringLookup(), false);
      interpolator = new StringSubstitutor(lookups).setEnableSubstitutionInVariables(true);
      targetPointer = JsonPointer.from(options.getString("target"));
      targetValue = options.getString("value");
    }

    @Override
    public void accept(Event evt) {
      try {
        currentEvt = evt;
        var value = interpolator.replace(targetValue);
        targetPointer.writeJson(evt.payload(), value, true);
      } finally {
        currentEvt = null;
      }
    }
  }

}
