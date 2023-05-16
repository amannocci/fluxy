package io.techcode.fluxy.component;

public class Linker {

  public static Pipe connectTo(Source source, Flow flow) {
    source.out = flow.in;
    return source.out;
  }

  public static Pipe connectTo(Source source, Broadcast broadcast) {
    source.out = broadcast.in;
    return source.out;
  }

  public static Pipe connectTo(Source source, Merge merge) {
    source.out = merge.in;
    return source.out;
  }

  public static Pipe connectTo(Source source, Sink sink) {
    source.out = sink.in;
    return source.out;
  }

  public static Pipe connectTo(Flow sourceFlow, Flow targetFlow) {
    sourceFlow.out = targetFlow.in;
    return sourceFlow.out;
  }

  public static Pipe connectTo(Flow flow, Broadcast broadcast) {
    flow.out = broadcast.in;
    return flow.out;
  }

  public static Pipe connectTo(Flow flow, Merge merge) {
    flow.out = merge.in;
    return flow.out;
  }

  public static Pipe connectTo(Flow flow, Sink sink) {
    flow.out = sink.in;
    return flow.out;
  }

  public static Pipe connectTo(Broadcast broadcast, Flow flow) {
    broadcast.outs.add(flow.in);
    return flow.in;
  }

  public static Pipe connectTo(Broadcast broadcast, Merge merge) {
    broadcast.outs.add(merge.in);
    return merge.in;
  }

  public static Pipe connectTo(Broadcast broadcast, Sink sink) {
    broadcast.outs.add(sink.in);
    return sink.in;
  }

  public static Pipe connectTo(Merge merge, Flow flow) {
    merge.out = flow.in;
    return merge.out;
  }

  public static Pipe connectTo(Merge merge, Sink sink) {
    merge.out = sink.in;
    return merge.out;
  }

}
