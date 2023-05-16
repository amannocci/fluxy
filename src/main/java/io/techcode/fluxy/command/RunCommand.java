package io.techcode.fluxy.command;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.techcode.fluxy.pipeline.Pipeline;
import io.vertx.core.Vertx;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

@Command(name = "run")
public class RunCommand implements Callable<Integer> {

  @Override
  public Integer call() throws InterruptedException {
    Vertx vertx = Vertx.vertx();
    vertx.exceptionHandler(Throwable::printStackTrace);

    Config conf = ConfigFactory.load();
    Pipeline pipeline = new Pipeline(conf.getConfig("pipeline"));
    vertx.deployVerticle(pipeline).onFailure(Throwable::printStackTrace);

    // Register signal handler
    final var runningPhase = new CountDownLatch(1);
    final var shuttingDownPhase = new CountDownLatch(1);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        runningPhase.countDown();
        shuttingDownPhase.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }));

    // Block until signal
    runningPhase.await();

    // Shutdown everything
    System.out.println("Shutdown everything");
    vertx.close().onComplete(result -> {
      if (result.succeeded()) {
        System.out.println("Gracefully shutdown");
      } else {
        System.err.println("Failed to flush pipelines");
      }
      shuttingDownPhase.countDown();
    });
    return 0;
  }
}
