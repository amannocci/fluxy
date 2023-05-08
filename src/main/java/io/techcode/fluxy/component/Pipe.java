package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import io.techcode.fluxy.event.Event;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.SpscArrayQueue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Pipe {

  private static final AtomicInteger idTracker = new AtomicInteger(0);
  public final int id;
  private final SpscArrayQueue<Event> queue;
  private final int lowWaterMark;
  private final int highWaterMark;
  private final AtomicBoolean isBackPressure;
  private volatile Mailbox onEventDispatcher;
  private volatile Mailbox onLowPressureDispatcher;
  private volatile Mailbox onHighPressureDispatcher;

  public Pipe() {
    this(256);
  }

  public Pipe(int capacity) {
    id = idTracker.incrementAndGet();
    queue = new SpscArrayQueue<>(capacity);
    lowWaterMark = (int) (capacity * 0.10F);
    highWaterMark = (int) (capacity * 0.90F);
    isBackPressure = new AtomicBoolean(true);
  }

  public void setEventHandler(Mailbox mailbox) {
    onEventDispatcher = mailbox;
  }

  public void setLowPressureHandler(Mailbox mailbox) {
    onLowPressureDispatcher = mailbox;
  }

  public void setHighPressureHandler(Mailbox mailbox) {
    onHighPressureDispatcher = mailbox;
  }

  public void pushOne(Event evt) {
    queue.offer(evt);
    handleEvent();
    handleHighPressure();
  }

  public void pullOne(MessagePassingQueue.Consumer<Event> consumer) {
    pullMany(consumer, 1);
  }

  public void pullMany(MessagePassingQueue.Consumer<Event> consumer) {
    queue.drain(consumer);
    handleLowPressure();
  }

  public void pullMany(MessagePassingQueue.Consumer<Event> consumer, int limit) {
    queue.drain(consumer, limit);
    handleLowPressure();
  }

  public boolean isAvailable() {
    return !isBackPressure.get();
  }

  public boolean isUnavailable() {
    return isBackPressure.get();
  }

  /**
   * Returns the remaining pipe capacity.
   * The capacity is expected to be at least this number.
   * However, it can be higher in reality since the consumer
   * can consume elements at the same time.
   *
   * @return remaining pipe capacity.
   */
  public int remainingCapacity() {
    return Math.max(0, highWaterMark - queue.size());
  }

  private void handleEvent() {
    onEventDispatcher.dispatch();
  }

  private synchronized void handleLowPressure() {
    if (isBackPressure.get() && queue.size() < lowWaterMark) {
      isBackPressure.set(false);
      onLowPressureDispatcher.dispatch();
    }
  }

  private synchronized void handleHighPressure() {
    if (!isBackPressure.get() && queue.size() > highWaterMark) {
      isBackPressure.set(true);
      onHighPressureDispatcher.dispatch();
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("id", id)
      .add("queue", queue.size())
      .add("lowWaterMark", lowWaterMark)
      .add("highWaterMark", highWaterMark)
      .add("isBackPressure", isBackPressure)
      .add("onEventDispatcher", onEventDispatcher)
      .add("onLowPressureDispatcher", onLowPressureDispatcher)
      .add("onHighPressureDispatcher", onHighPressureDispatcher)
      .toString();
  }
}
