package io.techcode.fluxy.event;

import io.netty.util.AttributeMap;
import io.netty.util.DefaultAttributeMap;
import io.vertx.core.json.JsonObject;

public class Event {

  private AttributeMap headers;
  private JsonObject payload;

  public Event() {
    this(new JsonObject());
  }

  public Event(JsonObject payload) {
    this(new DefaultAttributeMap(), payload);
  }

  private Event(AttributeMap headers, JsonObject payload) {
    this.headers = headers;
    this.payload = payload;
  }

  public JsonObject getPayload() {
    return payload;
  }

  public Event copy() {
    return new Event(headers, payload.copy());
  }

}
