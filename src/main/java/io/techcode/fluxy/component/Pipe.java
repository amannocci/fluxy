package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import io.techcode.fluxy.event.Event;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jctools.queues.SpscUnboundedArrayQueue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Pipe {

  public static final int DEFAULT_CAPACITY = 128;
  private static final AtomicInteger idTracker = new AtomicInteger(0);
  public final int id;
  private final MessagePassingQueue<Event> queue;
  private final int lowWaterMark;
  private final int highWaterMark;
  private final AtomicBoolean isBackPressure;
  private volatile Mailbox onEventDispatcher;
  private volatile Mailbox onAvailableDispatcher;
  private volatile Mailbox onUnavailableDispatcher;

  public Pipe() {
    this(DEFAULT_CAPACITY);
  }

  public Pipe(int capacity) {
    this(capacity, false);
  }

  public Pipe(int capacity, boolean multiProducer) {
    id = idTracker.incrementAndGet();
    if (multiProducer) {
      queue = new MpscUnboundedArrayQueue<>(capacity);
    } else {
      queue = new SpscUnboundedArrayQueue<>(capacity);
    }
    lowWaterMark = (int) (capacity * 0.10F);
    highWaterMark = (int) (capacity * 0.90F);
    isBackPressure = new AtomicBoolean(true);
  }

  public void setEventHandler(Mailbox mailbox) {
    onEventDispatcher = mailbox;
  }

  public void setAvailableHandler(Mailbox mailbox) {
    onAvailableDispatcher = mailbox;
  }

  public void setUnavailableHandler(Mailbox mailbox) {
    onUnavailableDispatcher = mailbox;
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
    if (isBackPressure.get()) return 0;
    return Math.max(0, highWaterMark - queue.size());
  }

  private void handleEvent() {
    onEventDispatcher.dispatch();
  }

  private synchronized void handleLowPressure() {
    if (isBackPressure.get() && queue.size() < lowWaterMark) {
      isBackPressure.set(false);
      onAvailableDispatcher.dispatch();
    }
  }

  private synchronized void handleHighPressure() {
    if (!isBackPressure.get() && queue.size() > highWaterMark) {
      isBackPressure.set(true);
      onUnavailableDispatcher.dispatch();
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
      .add("onAvailableDispatcher", onAvailableDispatcher)
      .add("onUnavailableDispatcher", onUnavailableDispatcher)
      .toString();
  }
}
