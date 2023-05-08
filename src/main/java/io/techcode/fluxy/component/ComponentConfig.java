package io.techcode.fluxy.component;

import com.typesafe.config.Config;

import java.util.Optional;

public record ComponentConfig(
  Optional<Pipe> in,
  Optional<Pipe> out,
  Config options
) {}

