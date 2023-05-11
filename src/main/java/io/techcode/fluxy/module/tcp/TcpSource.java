package io.techcode.fluxy.module.tcp;

import com.typesafe.config.Config;
import io.techcode.fluxy.component.Source;
import io.techcode.fluxy.event.Event;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import java.util.HashMap;
import java.util.Map;

public class TcpSource extends Source {

  private final Map<String, NetSocket> connections;
  private NetServer server;

  public TcpSource(Config options) {
    connections = new HashMap<>();
  }

  @Override
  public void start() {
    super.start();
    server = vertx.createNetServer();

    server.connectHandler(conn -> {
      conn.pause();
      connections.put(conn.writeHandlerID(), conn);
      conn.handler(data -> {
        out.pushOne(new Event());
        if (out.isUnavailable()) {
          onPipeUnavailable(null);
        }
      });
      conn.endHandler(e -> connections.remove(conn.writeHandlerID()));
      if (out.isAvailable()) {
        conn.resume();
      }
    });
    server.listen(8080, "0.0.0.0");
    System.out.println("Listening on: 8080");
  }

  @Override
  protected void onPipeAvailable(Void evt) {
    super.onPipeAvailable(evt);
    for (var conn : connections.values()) {
      conn.resume();
    }
  }

  @Override
  protected void onPipeUnavailable(Void evt) {
    super.onPipeUnavailable(evt);
    for (var conn : connections.values()) {
      conn.pause();
    }
  }

  @Override
  public void stop() {
    connections.clear();
    server.close();
  }

}
