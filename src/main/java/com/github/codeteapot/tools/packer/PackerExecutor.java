package com.github.codeteapot.tools.packer;

import static java.io.File.createTempFile;
import static java.lang.ProcessBuilder.Redirect.PIPE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;

class PackerExecutor {

  private final PackerTerminal terminal;
  private final PackerProcessBuilderFactory processBuilderFactory;
  private final Executor terminalTaskExecutor;

  PackerExecutor(
      PackerTerminal terminal,
      PackerProcessBuilderFactory processBuilderFactory,
      Executor terminalTaskExecutor) {
    this.terminal = terminal;
    this.processBuilderFactory = processBuilderFactory;
    this.terminalTaskExecutor = terminalTaskExecutor;
  }

  PackerExecution execute(String command, List<String> args) throws IOException {
    File errorFile = createTempFile("packer-", ".err");
    errorFile.deleteOnExit();
    ProcessBuilder processBuilder = processBuilderFactory.getProcessBuilder(command, args)
        .redirectOutput(PIPE)
        .redirectInput(PIPE)
        .redirectError(errorFile);
    terminal.getWorkingDir()
        .ifPresent(processBuilder::directory);
    return new PackerExecutionImpl(
        processBuilder.start(),
        this::terminalTaskFactory,
        errorFile,
        terminalTaskExecutor);
  }

  private PackerTerminalTask terminalTaskFactory(PackerAbortable execution, InputStream input) {
    return new PackerTerminalTask(terminal, execution, input);
  }
}
