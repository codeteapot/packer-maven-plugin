package com.github.codeteapot.tools.packer;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Tool class for running local Packer commands.
 */
public class Packer {

  private static final String PACKER_PROGRAM = "packer";

  private final PackerExecutor executor;

  /**
   * Creates a Packer tool instance that will use the given terminal and the default process builder
   * factory.
   *
   * @param terminal Terminal used by Packer tool instance.
   */
  public Packer(PackerTerminal terminal) {
    this(terminal, new DefaultPackerProcessBuilderFactory(PACKER_PROGRAM));
  }

  /**
   * Creates a Packer tool instance that will use the given terminal and the given process builder
   * factory.
   *
   * @param terminal Terminal used by Packer tool instance.
   * @param processBuilderFactory Factory of a Packer process builder.
   */
  public Packer(PackerTerminal terminal, PackerProcessBuilderFactory processBuilderFactory) {
    this(new PackerExecutor(terminal, processBuilderFactory, newCachedThreadPool()));
  }

  Packer(PackerExecutor executor) {
    this.executor = executor;
  }

  /**
   * Executes a {@code build} command and returns its handler.
   *
   * @param template Template file path relative to the working directory.
   * @param force Determines if "force" argument is present.
   * @param only Set of values that will be used on "only" comma-separated argument, if not empty.
   * @param except Set of values that will be used on "except" comma-separated argument, if not
   *        empty.
   * @param vars Set of entries that will appear as "var" arguments.
   * @param varFiles Set of entries that will appear as "var-file" arguments.
   * 
   * @return An already running {@code build} command execution.
   * 
   * @throws IOException If some I/O error has been occurred.
   */
  public PackerExecution build(String template, boolean force, Set<String> only,
      Set<String> except, Map<String, Object> vars, Set<String> varFiles) throws IOException {
    return executor.execute("build", Stream.of(
        force
            ? Stream.of("-force")
            : Stream.<String>empty(),
        only.isEmpty()
            ? Stream.<String>empty()
            : Stream.of(
                "-only",
                only.stream()
                    .collect(joining(","))),
        except.isEmpty()
            ? Stream.<String>empty()
            : Stream.of(
                "-except",
                except.stream()
                    .collect(joining(","))),
        vars.entrySet().stream()
            .flatMap(var -> Stream.of(
                "-var",
                format(
                    "%s=%s",
                    var.getKey(),
                    var.getValue().toString()))),
        varFiles.stream()
            .flatMap(varFile -> Stream.of(
                "-var-file",
                varFile)),
        Stream.of(requireNonNull(template)))
        .flatMap(identity())
        .collect(toList()));
  }
}
