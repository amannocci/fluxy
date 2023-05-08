package io.techcode.fluxy.event;

import io.netty.util.AttributeMap;
import io.netty.util.DefaultAttributeMap;
import io.vertx.core.json.JsonObject;

public record Event(
  AttributeMap headers,
  JsonObject payload
) {

  public Event() {
    this(new JsonObject());
  }

  public Event(JsonObject payload) {
    this(new DefaultAttributeMap(), payload);
  }

  public Event copy() {
    return new Event(headers, payload.copy());
  }

}
