package io.techcode.fluxy.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.techcode.fluxy.pipeline.Pipeline;
import io.vertx.core.Context;

public abstract class Source extends Component {

  protected Mailbox pipeAvailableMailbox;
  protected Mailbox pipeUnavailableMailbox;
  protected Pipe out;

  public Source(Pipeline pipeline) {
    super(pipeline);
  }

  @Override
  public void start() {
    Preconditions.checkNotNull(out, "Source isn't connected");
    Context ctx = vertx.getOrCreateContext();
    pipeAvailableMailbox = new Mailbox(ctx, this::onPipeAvailable);
    pipeUnavailableMailbox = new Mailbox(ctx, this::onPipeUnavailable);
    out.addAvailableHandler(pipeAvailableMailbox);
    out.addUnavailableHandler(pipeUnavailableMailbox);
  }

  protected void onPipeAvailable(Void event) {
    pipeAvailableMailbox.reset();
  }

  protected void onPipeUnavailable(Void event) {
    pipeUnavailableMailbox.reset();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("out", out)
      .toString();
  }

}
