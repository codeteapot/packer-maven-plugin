package com.github.codeteapot.tools.packer;

import static java.util.Objects.requireNonNull;

import java.io.File;

/**
 * Exception thrown by {@link PackerExecution} success methods when execution has failed.
 */
public class PackerExecutionException extends Exception {

  private static final long serialVersionUID = 1L;

  private final File errorFile;

  /**
   * Execution exception with its error output file.
   *
   * @param errorFile File containing execution error output.
   */
  public PackerExecutionException(File errorFile) {
    this.errorFile = requireNonNull(errorFile);
  }

  /**
   * File containing execution error output.
   *
   * @return Error output file.
   */
  public File getErrorFile() {
    return errorFile;
  }
}
