package io.techcode.fluxy.component;

public class Linker {

  public static void connectTo(Source source, Flow flow) {
    source.out = flow.in;
  }

  public static void connectTo(Source source, Broadcast broadcast) {
    source.out = broadcast.in;
  }

  public static void connectTo(Source source, Merge merge) {
    source.out = merge.in;
  }

  public static void connectTo(Source source, Sink sink) {
    source.out = sink.in;
  }

  public static void connectTo(Flow sourceFlow, Flow targetFlow) {
    sourceFlow.out = targetFlow.in;
  }

  public static void connectTo(Flow flow, Broadcast broadcast) {
    flow.out = broadcast.in;
  }

  public static void connectTo(Flow flow, Merge merge) {
    flow.out = merge.in;
  }

  public static void connectTo(Flow flow, Sink sink) {
    flow.out = sink.in;
  }

  public static void connectTo(Broadcast broadcast, Flow flow) {
    broadcast.outs.add(flow.in);
  }

  public static void connectTo(Broadcast broadcast, Merge merge) {
    broadcast.outs.add(merge.in);
  }

  public static void connectTo(Broadcast broadcast, Sink sink) {
    broadcast.outs.add(sink.in);
  }

  public static void connectTo(Merge merge, Flow flow) {
    merge.out = flow.in;
  }

  public static void connectTo(Merge merge, Sink sink) {
    merge.out = sink.in;
  }

}
