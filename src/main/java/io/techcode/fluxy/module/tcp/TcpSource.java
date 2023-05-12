package io.techcode.fluxy.module.tcp;

import com.typesafe.config.Config;
import io.techcode.fluxy.component.Source;
import io.techcode.fluxy.event.Event;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import java.util.HashMap;
import java.util.Map;

public class TcpSource extends Source {

  private final Map<String, NetSocket> connections;
  private NetServer server;

  private boolean isPaused = false;

  public TcpSource(Config options) {
    connections = new HashMap<>();
  }

  @Override
  public void start() {
    super.start();
    server = vertx.createNetServer();
    server.connectHandler(this::onConnectionOpen);
    server.listen(8080, "0.0.0.0");
    System.out.println("Listening on: 8080");
  }

  @Override
  public void stop() {
    connections.clear();
    server.close();
  }

  @Override
  protected void onPipeAvailable(Void evt) {
    super.onPipeAvailable(evt);
    if (isPaused) {
      for (var conn : connections.values()) {
        conn.resume();
      }
      isPaused = false;
    }
  }

  @Override
  protected void onPipeUnavailable(Void evt) {
    super.onPipeUnavailable(evt);
    if (!isPaused) {
      for (var conn : connections.values()) {
        conn.pause();
      }
      isPaused = true;
    }
  }

  protected void onConnectionOpen(NetSocket conn) {
    conn.pause();
    connections.put(conn.writeHandlerID(), conn);
    conn.handler(this::onPush);
    conn.endHandler(e -> onConnectionEnd(conn));
    if (out.isAvailable()) {
      conn.resume();
    }
  }

  protected void onConnectionEnd(NetSocket conn) {
    connections.remove(conn.writeHandlerID());
  }

  protected void onPush(Buffer event) {
    out.pushOne(new Event());
    if (out.isUnavailable()) {
      onPipeUnavailable(null);
    }
  }

}
