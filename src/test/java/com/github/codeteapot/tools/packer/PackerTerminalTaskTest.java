package com.github.codeteapot.tools.packer;

import static java.nio.file.Files.write;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.util.concurrent.Semaphore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PackerTerminalTaskTest {

  private static final InterruptedException ANY_INTERRUPTED_EXCEPTION = new InterruptedException();

  private static final Instant SOME_TIMESTAMP = ofEpochMilli(1000L);
  private static final String SOME_TARGET = "some-target";
  private static final String SOME_TYPE = "some-type";
  private static final String SOME_DATA_FIRST_PART = "some-data";
  private static final String SOME_DATA_SECOND_PART = "Hello, World\n\r";

  private static final String SOME_INPUT_LINE = "1,some-target,some-type," +
      "some-data,Hello%!(PACKER_COMMA) World\\n\\r";

  private static final PackerAbortionException SOME_ABORTION_EXCEPTION =
      new PackerAbortionException();
  private static final RuntimeException SOME_RUNTIME_EXCEPTION = new RuntimeException();

  @Mock
  private PackerTerminal terminal;

  @Mock
  private PackerAbortable execution;

  private File inputFile;

  @Mock
  private Semaphore runningSemaphore;

  @Mock
  private Semaphore terminationSemaphore;

  private PackerTerminalTask terminalTask;

  @BeforeEach
  public void setUp(@TempDir File tempDir) throws Exception {
    inputFile = new File(tempDir, "packer.in");
    inputFile.createNewFile();
    terminalTask = new PackerTerminalTask(
        terminal,
        execution,
        new FileInputStream(inputFile),
        runningSemaphore,
        terminationSemaphore);
  }

  @Test
  public void receiveMessage() throws Exception {
    write(inputFile.toPath(), singletonList(SOME_INPUT_LINE));

    terminalTask.run();

    InOrder order = inOrder(terminal, runningSemaphore, terminationSemaphore);
    order.verify(terminationSemaphore, times(1)).acquire();
    order.verify(runningSemaphore, times(1)).release();
    order.verify(terminal, times(1))
        .receive(argThat(message -> message.getTimestamp().equals(SOME_TIMESTAMP)
            && message.getTarget().map(SOME_TARGET::equals).orElse(false)
            && message.getType().equals(SOME_TYPE)
            && message.getData().length == 2
            && message.getData()[0].equals(SOME_DATA_FIRST_PART)
            && message.getData()[1].equals(SOME_DATA_SECOND_PART)));
    order.verify(terminationSemaphore).release();
  }

  @Test
  public void receiveMessageAbort() throws Exception {
    write(inputFile.toPath(), singletonList(SOME_INPUT_LINE));
    doThrow(SOME_ABORTION_EXCEPTION)
        .when(terminal).receive(argThat(message -> message.getTimestamp().equals(SOME_TIMESTAMP)
            && message.getTarget().map(SOME_TARGET::equals).orElse(false)
            && message.getType().equals(SOME_TYPE)
            && message.getData().length == 2
            && message.getData()[0].equals(SOME_DATA_FIRST_PART)
            && message.getData()[1].equals(SOME_DATA_SECOND_PART)));

    terminalTask.run();

    InOrder order = inOrder(execution, runningSemaphore, terminationSemaphore);
    order.verify(terminationSemaphore, times(1)).acquire();
    order.verify(runningSemaphore, times(1)).release();
    order.verify(execution, times(1)).abort(SOME_ABORTION_EXCEPTION);
    order.verify(terminationSemaphore, times(1)).release();
  }

  @Test
  public void receiveMessageFailure() throws Exception {
    write(inputFile.toPath(), singletonList(SOME_INPUT_LINE));
    doThrow(SOME_RUNTIME_EXCEPTION)
        .when(terminal).receive(argThat(message -> message.getTimestamp().equals(SOME_TIMESTAMP)
            && message.getTarget().map(SOME_TARGET::equals).orElse(false)
            && message.getType().equals(SOME_TYPE)
            && message.getData().length == 2
            && message.getData()[0].equals(SOME_DATA_FIRST_PART)
            && message.getData()[1].equals(SOME_DATA_SECOND_PART)));

    terminalTask.run();

    InOrder order = inOrder(runningSemaphore, terminationSemaphore);
    order.verify(terminationSemaphore, times(1)).acquire();
    order.verify(runningSemaphore, times(1)).release();
    order.verify(terminationSemaphore, times(1)).release();
  }

  @Test
  public void acquireAndReleaseOnAwaitRunning() throws Exception {
    terminalTask.awaitRunning();

    InOrder order = inOrder(runningSemaphore);
    order.verify(runningSemaphore, times(1)).acquire(2);
    order.verify(runningSemaphore, times(1)).release();
  }

  @Test
  public void acquireAndReleaseOnAwaitTermination() throws Exception {
    terminalTask.awaitTermination();

    InOrder order = inOrder(terminationSemaphore);
    order.verify(terminationSemaphore, times(1)).acquire();
    order.verify(terminationSemaphore, times(1)).release();
  }

  @Test
  public void terminationAcquisitionInterrupted() throws Exception {
    doThrow(ANY_INTERRUPTED_EXCEPTION)
        .when(terminationSemaphore).acquire();

    terminalTask.run();

    verify(runningSemaphore, times(1)).release();
  }
}
