package com.github.codeteapot.tools.packer;

import static java.lang.Long.parseLong;
import static java.time.Instant.ofEpochMilli;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.concurrent.Semaphore;

class PackerTerminalTask implements Runnable {

  private final PackerTerminal terminal;
  private final PackerAbortable execution;
  private final BufferedReader reader;
  private final Semaphore runningSemaphore;
  private final Semaphore terminationSemaphore;

  PackerTerminalTask(
      PackerTerminal terminal,
      PackerAbortable execution,
      InputStream input) {
    this(terminal, execution, input, new Semaphore(1), new Semaphore(1));
  }

  PackerTerminalTask(
      PackerTerminal terminal,
      PackerAbortable execution,
      InputStream input,
      Semaphore runningSemaphore,
      Semaphore terminationSemaphore) {
    this.terminal = terminal;
    this.execution = execution;
    reader = new BufferedReader(new InputStreamReader(input));
    this.runningSemaphore = runningSemaphore;
    this.terminationSemaphore = terminationSemaphore;
  }

  /*
   * TODO Implement sending to terminal input
   */
  @Override
  public void run() {
    try {
      terminationSemaphore.acquire();
      runningSemaphore.release();
      reader.lines()
          .map(PackerTerminalTask::messageParts)
          .forEach(this::receiveMessage);
    } catch (InterruptedException e) {
      runningSemaphore.release();
    } catch (UncheckedIOException e) {
      // Stream closed...
    } finally {
      terminationSemaphore.release();
    }
  }

  void awaitRunning() throws InterruptedException {
    try {
      runningSemaphore.acquire(2);
    } finally {
      runningSemaphore.release();
    }
  }

  void awaitTermination() throws InterruptedException {
    try {
      terminationSemaphore.acquire();
    } finally {
      terminationSemaphore.release();
    }
  }

  private void receiveMessage(String[] parts) {
    try {
      terminal.receive(new PackerMessageImpl(
          messageTimestamp(parts, 0),
          parts[1],
          parts[2],
          messageData(parts, 3)));
    } catch (PackerAbortionException e) {
      execution.abort(e);
    } catch (RuntimeException e) {
      // Ignore malformed output...
    }
  }

  private static String[] messageParts(String message) {
    return message.split(",", -1);
  }

  private static Instant messageTimestamp(String[] parts, int index) {
    return ofEpochMilli(parseLong(parts[index]) * 1000L);
  }

  private static String[] messageData(String[] parts, int index) {
    String[] data = new String[parts.length - index];
    for (int i = index; i < parts.length; ++i) {
      data[i - index] = parts[i].replace("%!(PACKER_COMMA)", ",")
          .replace("\\n", "\n")
          .replace("\\r", "\r");
    }
    return data;
  }
}
