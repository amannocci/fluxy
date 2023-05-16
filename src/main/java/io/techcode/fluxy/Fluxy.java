package io.techcode.fluxy;

import io.techcode.fluxy.command.RunCommand;
import io.techcode.fluxy.command.VersionCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(
  description = "Experimental high-performance events processor",
  version = "0.0.1-dev",
  subcommands = {
    VersionCommand.class,
    RunCommand.class
  }
)
public class Fluxy implements Runnable {

  @Spec
  CommandSpec spec;

  public static void main(String... args) {
    int rc = new CommandLine(new Fluxy()).execute(args);
    System.exit(rc);
  }

  public void run() {
    throw new ParameterException(spec.commandLine(), "Missing required subcommand");
  }

}
