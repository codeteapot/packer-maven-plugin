package com.github.codeteapot.maven.plugins.packer;

import static java.lang.Runtime.getRuntime;

import com.github.codeteapot.maven.plugins.packer.tools.PackerFactory;
import com.github.codeteapot.tools.packer.Packer;
import com.github.codeteapot.tools.packer.PackerAbortionException;
import com.github.codeteapot.tools.packer.PackerExecution;
import com.github.codeteapot.tools.packer.PackerExecutionException;
import com.github.codeteapot.tools.packer.PackerInput;
import com.github.codeteapot.tools.packer.PackerMessage;
import com.github.codeteapot.tools.packer.PackerTerminal;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Abstraction for executing Packer commands through Maven plug-in.
 */
public abstract class AbstractPackerMojo extends AbstractMojo {

  @Component
  private PackerFactory packerFactory;

  /**
   * Used to prevent this plug-in to be executed.
   */
  @Parameter(defaultValue = "false")
  private boolean skip;

  /**
   * Project where this plug-in is executed.
   */
  @Parameter(required = true, readonly = true, defaultValue = "${project}")
  protected MavenProject project;

  /**
   * Default constructor.
   */
  protected AbstractPackerMojo() {
    packerFactory = null;
    skip = false;
    project = null;
  }

  /**
   * Inject a Packer factory.
   *
   * @param packerFactory A Packer factory;
   */
  public void setPackerFactory(PackerFactory packerFactory) {
    this.packerFactory = packerFactory;
  }

  /**
   * Get working directory of suitable Packer terminal.
   *
   * @return Working directory of suitable Packer terminal, or empty for default.
   * 
   * @see PackerTerminal#getWorkingDir()
   */
  protected abstract Optional<File> getTerminalWorkingDir();

  /**
   * Receive message from suitable Packer terminal.
   *
   * @param message Received message.
   * 
   * @throws PackerAbortionException When execution must be aborted.
   * 
   * @see PackerTerminal#receive(PackerMessage)
   */
  protected abstract void terminalReceive(PackerMessage message) throws PackerAbortionException;

  /**
   * Determines if the plug-in must be actually executed.
   *
   * @return The flag value determining this plug-in must be actually executed.
   * 
   * @throws MojoExecutionException When an execution error has been occurred.
   */
  protected abstract boolean mustBeExecuted() throws MojoExecutionException;

  /**
   * Creates an execution instance from the given Packer tool with a suitable packer terminal.
   *
   * @param packer Packer tool instance with a suitable terminal.
   * 
   * @return The execution that is created with the given Packer tool.
   * 
   * @throws MojoExecutionException If some error has been occurred while trying to create the
   *         execution.
   * @throws IOException If some I/O error has been occurred.
   */
  protected abstract PackerExecution executionGet(Packer packer)
      throws MojoExecutionException, IOException;

  /**
   * Handles an execution abortion.
   *
   * @param e The associated abortion exception.
   */
  protected abstract void executionAborted(PackerAbortionException e);

  /**
   * Handles an execution failure.
   *
   * @param e The associated failure exception.
   */
  protected abstract void executionFailed(PackerExecutionException e);

  /**
   * Handles an execution I/O failure.
   *
   * @param e The associated I/O failure exception.
   */
  protected abstract void executionFailed(IOException e);

  /**
   * Handles an execution interruption.
   *
   * @param e The associated interruption exception.
   */
  protected abstract void executionInterrupted(InterruptedException e);

  /**
   * Execute a command through a Packer tool instance with a suitable terminal.
   *
   * @throws MojoExecutionException When there is some execution error.
   * @throws MojoFailureException When there is some failure while executing Packer command.
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().info("Execution skipped...");
    } else if (mustBeExecuted()) {
      execute(packerFactory.getPacker(new PackerMojoTerminal()));
    }
  }

  private void execute(Packer packer) throws MojoExecutionException {
    try {
      success(executionGet(packer));
    } catch (IOException e) {
      executionFailed(e);
      throw new MojoExecutionException("Packer execution failure", e);
    }
  }

  private void success(PackerExecution execution) throws MojoExecutionException {
    try {
      getRuntime().addShutdownHook(new Thread(execution::abort));
      execution.success();
    } catch (PackerAbortionException e) {
      executionAborted(e);
    } catch (PackerExecutionException e) {
      executionFailed(e);
      throw new MojoExecutionException("Packer execution failure", e);
    } catch (InterruptedException e) {
      executionInterrupted(e);
      throw new MojoExecutionException("Packer execution interruption", e);
    }
  }

  private class PackerMojoTerminal implements PackerTerminal {

    @Override
    public Optional<File> getWorkingDir() {
      return getTerminalWorkingDir();
    }

    @Override
    public void receive(PackerMessage message) throws PackerAbortionException {
      terminalReceive(message);
    }

    @Override
    public void send(PackerInput input) throws PackerAbortionException {
      throw new PackerAbortionException("Interactive execution is not supported");
    }
  }
}
