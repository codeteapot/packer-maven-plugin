package com.github.codeteapot.tools.packer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handler for Packer command execution.
 */
public interface PackerExecution {

  /**
   * Abort this execution, causing success methods to throw an abortion exception.
   */
  void abort();

  /**
   * Wait this execution to be terminated.
   * 
   * <p>It terminates successfully unless any exception is thrown.
   *
   * @throws PackerExecutionException When there was some failure on this execution.
   * @throws PackerAbortionException When this execution was aborted.
   * @throws InterruptedException When execution was interrupted.
   */
  void success() throws PackerExecutionException, PackerAbortionException, InterruptedException;

  /**
   * Wait this execution to be terminated before timed-out.
   * 
   * <p>It terminates successfully unless any exception is thrown.
   *
   * @param timeout Timeout amount.
   * @param unit Timeout unit.
   * 
   * @throws PackerExecutionException When there was some failure on this execution.
   * @throws PackerAbortionException When this execution was aborted.
   * @throws TimeoutException When operation was timed-out.
   * @throws InterruptedException When execution was interrupted.
   */
  void success(long timeout, TimeUnit unit)
      throws PackerExecutionException, PackerAbortionException, InterruptedException,
      TimeoutException;
}
