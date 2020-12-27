package com.github.codeteapot.tools.packer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestPackerExecution implements PackerExecution {

  @Override
  public void abort() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void success() throws PackerExecutionException, PackerAbortionException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void success(long timeout, TimeUnit unit)
      throws PackerExecutionException, PackerAbortionException, TimeoutException {
    throw new UnsupportedOperationException();
  }
}
