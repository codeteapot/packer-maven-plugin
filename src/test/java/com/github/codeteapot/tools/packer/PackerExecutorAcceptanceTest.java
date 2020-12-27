package com.github.codeteapot.tools.packer;

import static java.nio.file.Files.write;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PackerExecutorAcceptanceTest {

  private static final String SOME_COMMAND = "some-command";
  private static final List<String> SOME_ARGS = emptyList();

  private static final Instant SOME_TIMESTAMP = ofEpochMilli(1000L);
  private static final String SOME_TARGET = "some-target";
  private static final String SOME_TYPE = "some-type";
  private static final String SOME_DATA = "some-data";
  private static final String SOME_SCRIPT_CONTENT = "!/bin/sh\n" +
      "echo 1,some-target,some-type,some-data";

  private static final String SOME_ABORTION_MESSAGE = "some-abortion-message";
  private static final Throwable SOME_ABORTION_CAUSE = new Exception();

  @Mock
  private PackerTerminal terminal;

  @Mock
  private PackerProcessBuilderFactory processBuilderFactory;

  private PackerExecutor executor;

  @BeforeEach
  public void setUp() {
    executor = new PackerExecutor(terminal, processBuilderFactory, newFixedThreadPool(1));
  }

  @Test
  public void abortedWithMessageByTerminalReceiving(@TempDir File scriptDir) throws Exception {
    File script = new File(scriptDir, "packer.sh");
    write(script.toPath(), singletonList(SOME_SCRIPT_CONTENT));
    script.setReadable(true);
    script.setExecutable(true);
    when(terminal.getWorkingDir())
        .thenReturn(Optional.empty());
    doThrow(new PackerAbortionException(SOME_ABORTION_MESSAGE))
        .when(terminal).receive(argThat(message -> message.getTimestamp().equals(SOME_TIMESTAMP)
            && message.getTarget().map(SOME_TARGET::equals).orElse(false)
            && message.getType().equals(SOME_TYPE)
            && message.getData().length == 1
            && message.getData()[0].equals(SOME_DATA)));
    when(processBuilderFactory.getProcessBuilder(SOME_COMMAND, SOME_ARGS))
        .thenReturn(new ProcessBuilder(script.getAbsolutePath()));

    PackerExecution execution = executor.execute(SOME_COMMAND, SOME_ARGS);
    Throwable e = catchThrowable(() -> execution.success());

    assertThat(e)
        .isInstanceOf(PackerAbortionException.class)
        .hasMessage(SOME_ABORTION_MESSAGE);
  }

  @Test
  public void abortedWithCauseByTerminalReceiving(@TempDir File tempDir) throws Exception {
    File script = new File(tempDir, "packer.sh");
    write(script.toPath(), singletonList(SOME_SCRIPT_CONTENT));
    script.setReadable(true);
    script.setExecutable(true);
    when(terminal.getWorkingDir())
        .thenReturn(Optional.empty());
    doThrow(new PackerAbortionException(SOME_ABORTION_CAUSE))
        .when(terminal).receive(argThat(message -> message.getTimestamp().equals(SOME_TIMESTAMP)
            && message.getTarget().map(SOME_TARGET::equals).orElse(false)
            && message.getType().equals(SOME_TYPE)
            && message.getData().length == 1
            && message.getData()[0].equals(SOME_DATA)));
    when(processBuilderFactory.getProcessBuilder(SOME_COMMAND, SOME_ARGS))
        .thenReturn(new ProcessBuilder(script.getAbsolutePath()));

    PackerExecution execution = executor.execute(SOME_COMMAND, SOME_ARGS);
    Throwable e = catchThrowable(() -> execution.success());

    assertThat(e)
        .isInstanceOf(PackerAbortionException.class)
        .hasCause(SOME_ABORTION_CAUSE);
  }

  @Test
  public void abortedWithMessageAndCauseByTerminalReceiving(@TempDir File tempDir)
      throws Exception {
    File script = new File(tempDir, "packer.sh");
    write(script.toPath(), singletonList(SOME_SCRIPT_CONTENT));
    script.setReadable(true);
    script.setExecutable(true);
    when(terminal.getWorkingDir())
        .thenReturn(Optional.empty());
    doThrow(new PackerAbortionException(SOME_ABORTION_MESSAGE, SOME_ABORTION_CAUSE))
        .when(terminal).receive(argThat(message -> message.getTimestamp().equals(SOME_TIMESTAMP)
            && message.getTarget().map(SOME_TARGET::equals).orElse(false)
            && message.getType().equals(SOME_TYPE)
            && message.getData().length == 1
            && message.getData()[0].equals(SOME_DATA)));
    when(processBuilderFactory.getProcessBuilder(SOME_COMMAND, SOME_ARGS))
        .thenReturn(new ProcessBuilder(script.getAbsolutePath()));

    PackerExecution execution = executor.execute(SOME_COMMAND, SOME_ARGS);
    Throwable e = catchThrowable(() -> execution.success());

    assertThat(e)
        .isInstanceOf(PackerAbortionException.class)
        .hasMessage(SOME_ABORTION_MESSAGE)
        .hasCause(SOME_ABORTION_CAUSE);
  }
}
