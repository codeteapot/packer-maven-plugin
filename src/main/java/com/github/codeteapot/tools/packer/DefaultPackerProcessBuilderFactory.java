package com.github.codeteapot.tools.packer;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.List;
import java.util.stream.Stream;

class DefaultPackerProcessBuilderFactory implements PackerProcessBuilderFactory {

  private final String program;

  public DefaultPackerProcessBuilderFactory(String program) {
    this.program = requireNonNull(program);
  }

  @Override
  public ProcessBuilder getProcessBuilder(String command, List<String> args) {
    return new ProcessBuilder(
        concat(
            Stream.of(
                program,
                "-machine-readable",
                command),
            args.stream())
                .collect(toList()));
  }
}
