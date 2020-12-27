package com.github.codeteapot.tools.packer;

/**
 * Exception thrown by {@link PackerTerminal} methods when execution must be aborted, and thrown by
 * {@link PackerExecution} success methods when execution is already aborted.
 */
public class PackerAbortionException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Empty abortion exception.
   */
  public PackerAbortionException() {}

  /**
   * Abortion exception with the given message.
   *
   * @param message Message of the abortion exception.
   */
  public PackerAbortionException(String message) {
    super(message);
  }

  /**
   * Abortion exception with the given cause.
   *
   * @param cause Cause of the abortion exception.
   */
  public PackerAbortionException(Throwable cause) {
    super(cause);
  }

  /**
   * Abortion exception with the given message and cause.
   *
   * @param message Message of the abortion exception.
   * @param cause Cause of the abortion exception.
   */
  public PackerAbortionException(String message, Throwable cause) {
    super(message, cause);
  }
}
