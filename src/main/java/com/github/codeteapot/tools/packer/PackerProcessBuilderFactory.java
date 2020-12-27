package com.github.codeteapot.tools.packer;

import java.util.List;

/**
 * Packer {@link ProcessBuilder} factory.
 */
public interface PackerProcessBuilderFactory {

  /**
   * Retrieve a Packer process builder for running the given command with the given arguments.
   *
   * @param command Command to be executed.
   * @param args Command arguments.
   * 
   * @return The Packer command process builder.
   */
  ProcessBuilder getProcessBuilder(String command, List<String> args);
}
