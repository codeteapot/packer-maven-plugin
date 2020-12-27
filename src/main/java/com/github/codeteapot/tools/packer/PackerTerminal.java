package com.github.codeteapot.tools.packer;

import java.io.File;
import java.util.Optional;

/**
 * Interface for satisfying communication between Packer commands execution and application.
 */
public interface PackerTerminal {

  /**
   * Working directory on where command will be executed, or empty for default.
   *
   * @return Working directory, or empty for default.
   */
  Optional<File> getWorkingDir();

  /**
   * Method called when a message is sent by Packer command execution, having this message as
   * parameter.
   *
   * <p>This method can abort the underlying command execution by throwing
   * {@code PackerAbortionException}.
   *
   * @param message Message sent by command execution.
   * 
   * @throws PackerAbortionException If underlying execution must be aborted.
   */
  void receive(PackerMessage message) throws PackerAbortionException;

  /**
   * Method called when Packer command execution wants an user interaction.
   * 
   * <p>Text written to {@code PackerInput} is committed after this method is called.
   * 
   * <p>This method can abort the underlying command execution by throwing
   * {@code PackerAbortionException}.
   *
   * @param input Input where user interaction text is written.
   * 
   * @throws PackerAbortionException If underlying execution must be aborted.
   */
  void send(PackerInput input) throws PackerAbortionException;
}
