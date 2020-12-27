package com.github.codeteapot.tools.packer;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

class PackerExecutionImpl implements PackerExecution, PackerAbortable {

  private final Process process;
  private final PackerTerminalTask terminalTask;
  private final File errorFile;
  private final AtomicReference<PackerAbortionException> abortionException;

  PackerExecutionImpl(
      Process process,
      PackerTerminalTaskFactory terminalTaskFactory,
      File errorFile,
      Executor terminalTaskExecutor) {
    this(process, terminalTaskFactory, errorFile, new AtomicReference<>(), terminalTaskExecutor);
  }

  PackerExecutionImpl(
      Process process,
      PackerTerminalTaskFactory terminalTaskFactory,
      File errorFile,
      AtomicReference<PackerAbortionException> abortionException,
      Executor terminalTaskExecutor) {
    this.process = process;
    terminalTask = terminalTaskFactory.getTerminalTask(this, this.process.getInputStream());
    this.errorFile = errorFile;
    this.abortionException = abortionException;
    terminalTaskExecutor.execute(terminalTask);
  }

  @Override
  public void abort() {
    abort(new PackerAbortionException());
  }

  @Override
  public void abort(PackerAbortionException abortionException) {
    this.abortionException.set(abortionException);
    process.destroy();
  }

  @Override
  public void success()
      throws PackerExecutionException, PackerAbortionException, InterruptedException {
    terminalTask.awaitRunning();
    int exitValue = process.waitFor();
    terminalTask.awaitTermination();
    if (abortionException.get() != null) {
      throw abortionException.get();
    }
    if (exitValue != 0) {
      throw new PackerExecutionException(errorFile);
    }
  }

  @Override
  public void success(long timeout, TimeUnit unit)
      throws PackerExecutionException, PackerAbortionException, InterruptedException,
      TimeoutException {
    terminalTask.awaitRunning();
    if (!process.waitFor(timeout, unit)) {
      process.destroy();
      terminalTask.awaitTermination();
      throw new TimeoutException();
    }
    int exitValue = process.exitValue();
    terminalTask.awaitTermination();
    if (abortionException.get() != null) {
      throw abortionException.get();
    }
    if (exitValue != 0) {
      throw new PackerExecutionException(errorFile);
    }
  }
}
