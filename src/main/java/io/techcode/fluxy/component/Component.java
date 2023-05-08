package io.techcode.fluxy.component;

public interface Component {

  default boolean isBlocking() {
    return false;
  }

}
