package com.github.codeteapot.tools.packer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PackerExecutionImplTest {

  private static final int ZERO_EXIT_CODE = 0;
  private static final int NON_ZERO_EXIT_CODE = -1;

  private static final boolean WAIT_FOR_TIMEDOUT = false;
  private static final boolean WAIT_FOR_NON_TIMEDOUT = true;

  private static final long SOME_TIMEOUT = 1L;
  private static final TimeUnit SOME_UNIT = MILLISECONDS;

  private static final PackerAbortionException SOME_ABORTION_EXCEPTION =
      new PackerAbortionException();

  @Mock
  private Process process;

  @Mock
  private PackerTerminalTask terminalTask;

  private File errorFile;

  private AtomicReference<PackerAbortionException> abortionException;

  private PackerExecutionImpl executionImpl;

  @BeforeEach
  public void setUp(@Mock Executor executor, @TempDir File tempDir) throws Exception {
    errorFile = new File(tempDir, "error-file.err");
    abortionException = new AtomicReference<>();
    executionImpl = new PackerExecutionImpl(
        process,
        (execution, input) -> terminalTask,
        errorFile,
        abortionException,
        executor);

    verify(executor).execute(terminalTask);
  }

  @Test
  public void terminateOnAbort() {
    executionImpl.abort();

    assertThat(abortionException.get()).isNotNull();
    verify(process).destroy();
  }

  @Test
  public void raiseAbortionOnAbortedSuccess() {
    abortionException.set(SOME_ABORTION_EXCEPTION);

    Throwable e = catchThrowable(() -> executionImpl.success());

    assertThat(e).isEqualTo(SOME_ABORTION_EXCEPTION);
  }

  @Test
  public void successWithoutFailure() throws Exception {
    when(process.waitFor())
        .thenReturn(ZERO_EXIT_CODE);

    Throwable e = catchThrowable(() -> executionImpl.success());

    InOrder order = inOrder(terminalTask, process);
    order.verify(terminalTask, times(1)).awaitRunning();
    order.verify(process, times(1)).waitFor();
    order.verify(terminalTask, times(1)).awaitTermination();
    assertThat(e).isNull();
  }

  @Test
  public void successWithFailure() throws Exception {
    when(process.waitFor())
        .thenReturn(NON_ZERO_EXIT_CODE);

    PackerExecutionException e = catchThrowableOfType(
        () -> executionImpl.success(),
        PackerExecutionException.class);

    InOrder order = inOrder(terminalTask, process);
    order.verify(terminalTask, times(1)).awaitRunning();
    order.verify(process, times(1)).waitFor();
    order.verify(terminalTask, times(1)).awaitTermination();
    assertThat(e.getErrorFile()).isEqualTo(errorFile);
  }

  @Test
  public void raiseAbortionOnAbortedTimeoutSuccess() throws Exception {
    when(process.waitFor(SOME_TIMEOUT, SOME_UNIT))
        .thenReturn(WAIT_FOR_NON_TIMEDOUT);
    abortionException.set(SOME_ABORTION_EXCEPTION);

    Throwable e = catchThrowable(() -> executionImpl.success(SOME_TIMEOUT, SOME_UNIT));

    InOrder order = inOrder(terminalTask, process);
    order.verify(terminalTask, times(1)).awaitRunning();
    order.verify(process, times(1)).waitFor(anyLong(), any());
    order.verify(terminalTask, times(1)).awaitTermination();
    assertThat(e).isEqualTo(SOME_ABORTION_EXCEPTION);
  }

  @Test
  public void raiseTimeoutOnTimeoutSuccess() throws Exception {
    when(process.waitFor(SOME_TIMEOUT, SOME_UNIT))
        .thenReturn(WAIT_FOR_TIMEDOUT);

    Throwable e = catchThrowable(() -> executionImpl.success(SOME_TIMEOUT, SOME_UNIT));

    InOrder order = inOrder(terminalTask, process);
    order.verify(terminalTask, times(1)).awaitRunning();
    order.verify(process, times(1)).waitFor(anyLong(), any());
    order.verify(terminalTask, times(1)).awaitTermination();
    assertThat(e).isInstanceOf(TimeoutException.class);
  }

  @Test
  public void timeoutSuccessWithoutFailure() throws Exception {
    when(process.waitFor(SOME_TIMEOUT, SOME_UNIT))
        .thenReturn(WAIT_FOR_NON_TIMEDOUT);
    when(process.exitValue())
        .thenReturn(ZERO_EXIT_CODE);

    Throwable e = catchThrowable(() -> executionImpl.success(SOME_TIMEOUT, SOME_UNIT));

    InOrder order = inOrder(terminalTask, process);
    order.verify(terminalTask, times(1)).awaitRunning();
    order.verify(process, times(1)).waitFor(anyLong(), any());
    order.verify(terminalTask, times(1)).awaitTermination();
    assertThat(e).isNull();
  }

  @Test
  public void timeoutSuccessWithFailure() throws Exception {
    when(process.waitFor(SOME_TIMEOUT, SOME_UNIT))
        .thenReturn(WAIT_FOR_NON_TIMEDOUT);
    when(process.exitValue())
        .thenReturn(NON_ZERO_EXIT_CODE);

    PackerExecutionException e = catchThrowableOfType(
        () -> executionImpl.success(SOME_TIMEOUT, SOME_UNIT),
        PackerExecutionException.class);

    InOrder order = inOrder(terminalTask, process);
    order.verify(terminalTask, times(1)).awaitRunning();
    order.verify(process, times(1)).waitFor(anyLong(), any());
    order.verify(terminalTask, times(1)).awaitTermination();
    assertThat(e.getErrorFile()).isEqualTo(errorFile);
  }
}
