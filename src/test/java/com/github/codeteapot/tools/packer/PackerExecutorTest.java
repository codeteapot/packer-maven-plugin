package com.github.codeteapot.tools.packer;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PackerExecutorTest {

  private static final String DUMMY_COMMAND = "echo";

  private static final String SOME_COMMAND = "some-command";
  private static final List<String> SOME_ARGS = emptyList();

  @Mock
  private PackerTerminal terminal;

  @Mock
  private PackerProcessBuilderFactory processBuilderFactory;

  @Mock
  private Executor terminalTaskExecutor;

  private ProcessBuilder processBuilder;

  private PackerExecutor executor;

  @BeforeEach
  public void setUp() {
    executor = new PackerExecutor(terminal, processBuilderFactory, terminalTaskExecutor);
    processBuilder = new ProcessBuilder(DUMMY_COMMAND);

    when(processBuilderFactory.getProcessBuilder(SOME_COMMAND, SOME_ARGS))
        .thenReturn(processBuilder);
  }

  @Test
  public void executeOnDefaultDirectory() throws Exception {
    when(terminal.getWorkingDir())
        .thenReturn(Optional.empty());

    PackerExecution execution = executor.execute(SOME_COMMAND, SOME_ARGS);

    assertThat(execution).isInstanceOf(PackerExecutionImpl.class);
    assertThat(processBuilder.directory()).isNull();
  }

  @Test
  public void executeOnSomeDirectory(@TempDir File someDir) throws Exception {
    when(terminal.getWorkingDir())
        .thenReturn(Optional.of(someDir));

    PackerExecution execution = executor.execute(SOME_COMMAND, SOME_ARGS);

    assertThat(execution).isInstanceOf(PackerExecutionImpl.class);
    assertThat(processBuilder.directory()).isEqualTo(someDir);
  }
}
