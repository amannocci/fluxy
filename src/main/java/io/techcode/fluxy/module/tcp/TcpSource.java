package io.techcode.fluxy.module.tcp;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.techcode.fluxy.component.Source;
import io.techcode.fluxy.event.Event;
import io.techcode.fluxy.pipeline.Pipeline;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class TcpSource extends Source {

  // Option constant keys
  private final String OPTION_KEY_HOST = "host";
  private final String OPTION_KEY_PORT = "port";

  private final Map<String, NetSocket> connections;
  private NetServer server;
  private boolean isPaused = false;

  public TcpSource(Pipeline pipeline, Config options) {
    super(pipeline, options);
    connections = new HashMap<>();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    super.start();
    server = vertx.createNetServer();
    server.connectHandler(this::onConnectionOpen);
    var host = options.getString(OPTION_KEY_HOST);
    var port = options.getInt(OPTION_KEY_PORT);
    server.listen(port, host)
      .onComplete(result -> {
        if (result.succeeded()) {
          System.out.println("Listening on " + host + ':' + port);
          startPromise.complete();
        } else {
          System.err.println("Failed to listen " + host + ':' + port);
          startPromise.fail(result.cause());
        }
      });
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    System.out.println("Closing server");
    connections.clear();
    server.close().onComplete(evt -> {
      if (evt.succeeded()) {
        stopPromise.complete();
      } else {
        stopPromise.fail(evt.cause());
      }
    });
  }

  @Override
  protected Config defaultOptions() {
    return ConfigFactory.parseMap(ImmutableMap.<String, Object>builder()
      .put(OPTION_KEY_HOST, "0.0.0.0")
      .build()
    );
  }

  @Override
  protected void onOptionsValidate(Config options) {
    checkArgument(
      options.getNumber(OPTION_KEY_PORT) != null,
      "The field `port` is required"
    );
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
