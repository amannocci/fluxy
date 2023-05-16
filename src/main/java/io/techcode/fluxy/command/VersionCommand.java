package io.techcode.fluxy.command;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "version")
public class VersionCommand implements Callable<Integer> {
  @Override
  public Integer call() {
    var pkg = getClass().getPackage();
    System.out.println(pkg.getImplementationVersion());
    return 0;
  }
}
