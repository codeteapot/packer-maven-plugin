package com.github.codeteapot.maven.plugins.packer;

import static com.github.codeteapot.tools.packer.PackerMessage.DATA_UI_ERROR;
import static com.github.codeteapot.tools.packer.PackerMessage.DATA_UI_MESSAGE;
import static com.github.codeteapot.tools.packer.PackerMessage.DATA_UI_SAY;
import static com.github.codeteapot.tools.packer.PackerMessage.TYPE_UI;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.github.codeteapot.maven.plugins.packer.tools.ChecksumFactory;
import com.github.codeteapot.tools.checksum.Checksum;
import com.github.codeteapot.tools.packer.Packer;
import com.github.codeteapot.tools.packer.PackerAbortionException;
import com.github.codeteapot.tools.packer.PackerExecution;
import com.github.codeteapot.tools.packer.PackerExecutionException;
import com.github.codeteapot.tools.packer.PackerMessage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Execute Packer {@code build} command.
 *
 * <p>It not is actually executed when the previous execution on the given input directory was
 * successfully terminated and there is not any change, unless changes are not needed. An execution
 * failure could be not taken into account since last successful through "invalidate on failure"
 * flag to false.
 */
@Mojo(name = "build")
public final class PackerBuildMojo extends AbstractPackerMojo {

  private static final String CHECKSUM_FILE_NAME = ".checksum";

  @Component
  private ChecksumFactory checksumFactory;

  /**
   * Input directory that Packer build command uses as its working directory.
   */
  @Parameter(required = false, defaultValue = "${project.build.directory}/packer/input")
  private File inputDirectory;

  /**
   * Indicates that changes are needed to actually execute this plug-in command.
   */
  @Parameter(defaultValue = "true")
  private boolean changesNeeded;

  /**
   * Determines when a failure must be taken into account since current Packer build command
   * execution in order to actually execute this command following times.
   */
  @Parameter(defaultValue = "true")
  private boolean invalidateOnFailure;

  /**
   * First parameter of Packer <a href="https://www.packer.io/docs/commands/build">build</a>
   * command, corresponding to the template file path relative to the input directory.
   */
  @Parameter(defaultValue = "template.json")
  private String template;

  /**
   * Determines if <a href="https://www.packer.io/docs/commands/build#force">"--force"</a> argument
   * of Packer build command is present.
   */
  @Parameter(defaultValue = "false")
  private boolean force;

  /**
   * Comma-separated list for
   * <a href="https://www.packer.io/docs/commands/build#only-foo-bar-baz">"--only"</a> argument of
   * Packer build command, that is only present when it is specified.
   */
  @Parameter
  private String only;

  /**
   * Comma-separated list for
   * <a href="https://www.packer.io/docs/commands/build#except-foo-bar-baz">"--except"</a> argument
   * of Packer build command, that is only present when it is specified.
   */
  @Parameter
  private String except;

  /**
   * Element-separated of property name-value pairs for appending as
   * <a href="https://www.packer.io/docs/commands/build#var-file">"--var"</a> argument on Packer
   * build command.
   */
  @Parameter
  private Properties vars;

  /**
   * Element-separated file paths for appending as
   * <a href="https://www.packer.io/docs/commands/build#var-file">"--var-file"</a> argument on
   * Packer build command.
   */
  @Parameter
  private Set<String> varFiles;

  /**
   * Default constructor.
   */
  public PackerBuildMojo() {
    checksumFactory = null;
    inputDirectory = null;
    changesNeeded = false;
    invalidateOnFailure = false;
    template = null;
    force = false;
    only = null;
    except = null;
    vars = null;
    varFiles = null;
  }

  /**
   * Inject a checksum factory.
   *
   * @param checksumFactory A checksum factory;
   */
  public void setChecksumFactory(ChecksumFactory checksumFactory) {
    this.checksumFactory = checksumFactory;
  }

  /**
   * Set value of {@code inputDirectory} property.
   *
   * @param inputDirectory New value;
   */
  public void setInputDirectory(File inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  /**
   * Set value of {@code changesNeeded} property.
   *
   * @param changesNeeded New value;
   */
  public void setChangesNeeded(boolean changesNeeded) {
    this.changesNeeded = changesNeeded;
  }

  /**
   * Set value of {@code template} property.
   *
   * @param template New value;
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  @Override
  protected Optional<File> getTerminalWorkingDir() {
    return Optional.of(inputDirectory);
  }

  @Override
  protected void terminalReceive(PackerMessage message) throws PackerAbortionException {
    if (TYPE_UI.equals(message.getType())) {
      String[] data = message.getData();
      switch (data[0]) {
        case DATA_UI_MESSAGE:
        case DATA_UI_SAY:
          forEachLine(data[1], this::logInfo);
          break;
        case DATA_UI_ERROR:
          forEachLine(data[1], this::logError);
          break;
        default:
          forEachLine(data[1], this::logDebug);
      }
    }
  }

  /*
   * TODO Use command arguments as cache key. Maybe a hash to suffix the checksum file
   */
  @Override
  protected boolean mustBeExecuted() throws MojoExecutionException {
    try {
      File checksumFile = getChecksumFile();
      Checksum checksum = checksumFactory.getChecksum(inputDirectory, this::isChecksumFile);
      if (!checksum.match(checksumFile)) {
        checksum.store(checksumFile);
        return true;
      }
      if (changesNeeded) {
        getLog().info("There is not any change. Ignoring...");
        return false;
      }
      return true;
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new MojoExecutionException("Checksum error", e);
    }
  }

  @Override
  protected PackerExecution executionGet(Packer packer) throws MojoExecutionException, IOException {
    return packer.build(
        Optional.of(template)
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .orElseThrow(() -> new MojoExecutionException("Template is empty")),
        force,
        ofNullable(only)
            .map(str -> str.split(","))
            .map(Stream::of)
            .orElseGet(Stream::empty)
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .collect(toSet()),
        ofNullable(except)
            .map(str -> str.split(","))
            .map(Stream::of)
            .orElseGet(Stream::empty)
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .collect(toSet()),
        ofNullable(vars)
            .map(Properties::entrySet)
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .collect(toMap(e -> e.getKey().toString().trim(), Entry::getValue)),
        ofNullable(varFiles)
            .map(Set::stream)
            .orElseGet(Stream::empty)
            .map(String::trim)
            .collect(toSet()));
  }

  @Override
  protected void executionAborted(PackerAbortionException e) {
    invalidate();
  }

  @Override
  protected void executionFailed(PackerExecutionException e) {
    if (invalidateOnFailure) {
      invalidate();
    }
  }

  @Override
  protected void executionFailed(IOException e) {
    if (invalidateOnFailure) {
      invalidate();
    }
  }

  @Override
  protected void executionInterrupted(InterruptedException e) {
    invalidate();
  }

  private File getChecksumFile() {
    return new File(inputDirectory, CHECKSUM_FILE_NAME);
  }

  private boolean isChecksumFile(String path) {
    return path.equals(CHECKSUM_FILE_NAME);
  }

  private void invalidate() {
    getChecksumFile().delete();
  }

  private void logInfo(String msg) {
    getLog().info(msg);
  }

  private void logError(String msg) {
    getLog().error(msg);
  }

  private void logDebug(String msg) {
    getLog().debug(msg);
  }

  private static void forEachLine(String str, Consumer<String> lineConsumer) {
    Stream.of(str.split("\n"))
        .forEach(lineConsumer::accept);
  }
}
