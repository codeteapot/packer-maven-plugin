package com.github.codeteapot.maven.plugins.packer;

import static com.github.codeteapot.maven.plugin.testing.MavenPluginContext.configuration;
import static com.github.codeteapot.maven.plugin.testing.MavenPluginContext.configurationNode;
import static com.github.codeteapot.maven.plugin.testing.MavenPluginContext.configurationValue;
import static com.github.codeteapot.maven.plugin.testing.MavenPluginLoggerMessageLevel.LOG_DEBUG;
import static com.github.codeteapot.maven.plugin.testing.MavenPluginLoggerMessageLevel.LOG_ERROR;
import static com.github.codeteapot.maven.plugin.testing.MavenPluginLoggerMessageLevel.LOG_INFO;
import static com.github.codeteapot.tools.packer.PackerMessage.DATA_UI_ERROR;
import static com.github.codeteapot.tools.packer.PackerMessage.DATA_UI_MESSAGE;
import static com.github.codeteapot.tools.packer.PackerMessage.DATA_UI_SAY;
import static com.github.codeteapot.tools.packer.PackerMessage.TYPE_ARTIFACT_COUNT;
import static com.github.codeteapot.tools.packer.PackerMessage.TYPE_UI;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.data.Index.atIndex;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.github.codeteapot.maven.plugin.testing.MavenPluginContext;
import com.github.codeteapot.maven.plugin.testing.junit.jupiter.MavenPluginExtension;
import com.github.codeteapot.maven.plugin.testing.AccumulatedMavenPluginLogger;
import com.github.codeteapot.maven.plugins.packer.tools.ChecksumFactory;
import com.github.codeteapot.maven.plugins.packer.tools.PackerFactory;
import com.github.codeteapot.tools.checksum.Checksum;
import com.github.codeteapot.tools.packer.Packer;
import com.github.codeteapot.tools.packer.PackerAbortionException;
import com.github.codeteapot.tools.packer.PackerExecution;
import com.github.codeteapot.tools.packer.PackerExecutionException;
import com.github.codeteapot.tools.packer.PackerInput;
import com.github.codeteapot.tools.packer.PackerMessage;
import com.github.codeteapot.tools.packer.PackerTerminal;
import com.github.codeteapot.tools.packer.TestPackerMessage;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("integration")
@ExtendWith(MockitoExtension.class)
@ExtendWith(MavenPluginExtension.class)
public class PackerBuildMojoTest {

  private static final boolean CHEKSUM_MATCH = true;
  private static final boolean CHEKSUM_MISMATCH = false;

  private static final boolean FORCE = true;
  private static final boolean DO_NOT_FORCE = false;

  private static final Set<String> EMPTY_ONLY = emptySet();
  private static final Set<String> EMPTY_EXCEPT = emptySet();
  private static final Map<String, Object> EMPTY_VARS = emptyMap();
  private static final Set<String> EMPTY_VAR_FILES = emptySet();

  private static final String DEFAULT_TEMPLATE = "template.json";
  private static final String DEFAULT_INPUT_DIRECTORY_PATH_FROM_BASEDIR = "/target/packer/input";

  private static final String SOME_ABSOLUTE_INPUT_DIRECTORY_PATH = "/some/absolute/dir";
  private static final String SOME_RELATIVE_INPUT_DIRECTORY_PATH_FROM_BASEDIR = "some/relative/dir";
  private static final String SOME_TEMPLATE = "some-template.json";
  private static final Set<String> SOME_ONLY = Stream.of("first-only", "second-only")
      .collect(toSet());
  private static final Set<String> SOME_EXCEPT = Stream.of("first-except", "second-except")
      .collect(toSet());
  private static final Map<String, Object> SOME_VARS = Stream.of(
      new SimpleEntry<>("first-var-name", "first-var-value"),
      new SimpleEntry<>("second-var-name", "second-var-value"))
      .collect(toMap(Entry::getKey, Entry::getValue));
  private static final Set<String> SOME_VAR_FILES = Stream.of("first-var-file", "second-var-file")
      .collect(toSet());

  private static final String FIRST_MESSAGE_LINE = "First message line";
  private static final String SECOND_MESSAGE_LINE = "Second message line";

  private static final String SKIP_VALUE = "tRUe";
  private static final String CHANGES_NEEDED_VALUE = "trUE";
  private static final String CHANGES_NOT_NEEDED_VALUE = "faLSE";
  private static final String DOES_NOT_INVALIDATE_ON_FAILURE_VALUE = "FaLsE";
  private static final String SOME_ABSOLUTE_INPUT_DIRECTORY_VALUE = "/some/absolute/dir";
  private static final String SOME_RELATIVE_INPUT_DIRECTORY_VALUE = "some/relative/dir";
  private static final String SOME_TEMPLATE_VALUE = "\t some-template.json \n";
  private static final String BLANK_TEMPLATE_VALUE = "\t \n";
  private static final String FORCE_VALUE = "tRUE";
  private static final String BLANK_ONLY_VALUE = "\t , \n";
  private static final String SOME_ONLY_VALUE = "\t second-only ,first-only \n";
  private static final String BLANK_EXCEPT_VALUE = "\t , \n";
  private static final String SOME_EXCEPT_VALUE = "\t second-except ,first-except \n";
  private static final String FIRST_VAR_NAME_VALUE = " first-var-name ";
  private static final String FIRST_VAR_VALUE_VALUE = "first-var-value";
  private static final String SECOND_VAR_NAME_VALUE = " second-var-name ";
  private static final String SECOND_VAR_VALUE_VALUE = "second-var-value";
  private static final String FIRST_VAR_FILE_VALUE = "\t first-var-file \n";
  private static final String SECOND_VAR_FILE_VALUE = "\t second-var-file \n";

  private static final PackerMessage SOME_NON_UI_MESSAGE = new TestPackerMessage(
      TYPE_ARTIFACT_COUNT);
  private static final PackerMessage SOME_UI_MESSAGE_MESSAGE = new TestPackerMessage(
      TYPE_UI,
      DATA_UI_MESSAGE,
      "First message line\nSecond message line");
  private static final PackerMessage SOME_UI_MESSAGE_SAY = new TestPackerMessage(
      TYPE_UI,
      DATA_UI_SAY,
      "First message line\nSecond message line");
  private static final PackerMessage SOME_UI_MESSAGE_ERROR = new TestPackerMessage(
      TYPE_UI,
      DATA_UI_ERROR,
      "First message line\nSecond message line");
  private static final PackerMessage SOME_UI_MESSAGE_OTHER = new TestPackerMessage(
      TYPE_UI,
      "other",
      "First message line\nSecond message line");

  @Mock
  private ChecksumFactory checksumFactory;

  @Mock
  private PackerFactory packerFactory;

  @Captor
  private ArgumentCaptor<File> directory;

  @Captor
  private ArgumentCaptor<File> oldChecksumFile;

  @Captor
  private ArgumentCaptor<File> newChecksumFile;

  @Captor
  private ArgumentCaptor<PackerTerminal> packerTerminal;

  @TempDir
  protected File baseDir;

  @BeforeEach
  public void setUp(MavenPluginContext context) throws Exception {
    context.inject(checksumFactory, ChecksumFactory.ROLE);
    context.inject(packerFactory, PackerFactory.ROLE);
    context.setBaseDir(baseDir);
  }

  @Test
  public void skipExecution(MavenPluginContext context) throws Exception {
    AccumulatedMavenPluginLogger logger = new AccumulatedMavenPluginLogger();
    context.setLogger(logger);
    context.goal("build")
        .set(configuration()
            .set(configurationValue("skip", SKIP_VALUE)))
        .execute();

    assertThat(logger.getAccumulated())
        .hasSize(1)
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_INFO);
          assertThat(message.getContent())
              .hasValue("Execution skipped...");
        }, atIndex(0));
    verify(checksumFactory, never())
        .getChecksum(any(), any());
  }

  @Test
  public void ignoreBuildBecauseDefaultChangesNeeded(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MATCH)
        .when(checksum).match(oldChecksumFile.capture());

    AccumulatedMavenPluginLogger logger = new AccumulatedMavenPluginLogger();
    context.setLogger(logger);
    context.goal("build")
        .execute();

    assertThat(logger.getAccumulated())
        .hasSize(1)
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_INFO);
          assertThat(message.getContent())
              .hasValue("There is not any change. Ignoring...");
        }, atIndex(0));
    verify(checksum, never())
        .store(newChecksumFile.capture());
    verify(packerFactory, never())
        .getPacker(packerTerminal.capture());
  }

  @Test
  public void ignoreBuildBecauseChangesNeeded(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MATCH)
        .when(checksum).match(any());

    AccumulatedMavenPluginLogger logger = new AccumulatedMavenPluginLogger();
    context.setLogger(logger);
    context.goal("build")
        .set(configuration()
            .set(configurationValue("changesNeeded", CHANGES_NEEDED_VALUE)))
        .execute();

    assertThat(logger.getAccumulated())
        .hasSize(1)
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_INFO);
          assertThat(message.getContent())
              .hasValue("There is not any change. Ignoring...");
        }, atIndex(0));
    verify(checksum, never())
        .store(any());
    verify(packerFactory, never())
        .getPacker(any());
  }

  @Test
  public void buildBecauseChangesNotNeeded(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationValue("changesNeeded", CHANGES_NOT_NEEDED_VALUE)))
        .execute();

    verify(checksum, never())
        .store(any());
  }

  @Test
  public void invalidateOnFailureDefaultWhenFailed(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution,
      @TempDir File tempDir)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doAnswer(invocation -> {
      File checksumFile = invocation.getArgument(0, File.class);
      checksumFile.getParentFile().mkdirs();
      checksumFile.createNewFile();
      return null;
    })
        .when(checksum).store(newChecksumFile.capture());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doThrow(new PackerExecutionException(new File(tempDir, "error-file.err")))
        .when(execution).success();

    Throwable e = catchThrowable(() -> context.goal("build")
        .execute());

    assertThat(e)
        .hasCauseInstanceOf(MojoExecutionException.class);
    assertThat(newChecksumFile.getValue())
        .doesNotExist();
  }

  @Test
  public void invalidateOnFailureWhenFailed(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution,
      @TempDir File tempDir)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doAnswer(invocation -> {
      File checksumFile = invocation.getArgument(0, File.class);
      checksumFile.getParentFile().mkdirs();
      checksumFile.createNewFile();
      return null;
    })
        .when(checksum).store(newChecksumFile.capture());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doThrow(new PackerExecutionException(new File(tempDir, "error-file.err")))
        .when(execution).success();

    Throwable e = catchThrowable(() -> context.goal("build")
        .set(configuration()
            .set(configurationValue("invalidateOnFailure", DOES_NOT_INVALIDATE_ON_FAILURE_VALUE)))
        .execute());

    assertThat(e)
        .hasCauseInstanceOf(MojoExecutionException.class);
    assertThat(newChecksumFile.getValue())
        .exists();
  }

  @Test
  public void invalidateOnFailureDefaultWhenIOFailed(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doAnswer(invocation -> {
      File checksumFile = invocation.getArgument(0, File.class);
      checksumFile.getParentFile().mkdirs();
      checksumFile.createNewFile();
      return null;
    })
        .when(checksum).store(newChecksumFile.capture());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doThrow(new IOException())
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());

    Throwable e = catchThrowable(() -> context.goal("build")
        .execute());

    assertThat(e)
        .hasCauseInstanceOf(MojoExecutionException.class);
    assertThat(newChecksumFile.getValue())
        .doesNotExist();
  }

  @Test
  public void invalidateOnFailureWhenIOFailed(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doAnswer(invocation -> {
      File checksumFile = invocation.getArgument(0, File.class);
      checksumFile.getParentFile().mkdirs();
      checksumFile.createNewFile();
      return null;
    })
        .when(checksum).store(newChecksumFile.capture());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doThrow(new IOException())
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());

    Throwable e = catchThrowable(() -> context.goal("build")
        .set(configuration()
            .set(configurationValue("invalidateOnFailure", DOES_NOT_INVALIDATE_ON_FAILURE_VALUE)))
        .execute());

    assertThat(e)
        .hasCauseInstanceOf(MojoExecutionException.class);
    assertThat(newChecksumFile.getValue())
        .exists();
  }

  @Test
  public void invalidateOnFailureWhenAborted(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doAnswer(invocation -> {
      File checksumFile = invocation.getArgument(0, File.class);
      checksumFile.getParentFile().mkdirs();
      checksumFile.createNewFile();
      return null;
    })
        .when(checksum).store(newChecksumFile.capture());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doThrow(new PackerAbortionException())
        .when(execution).success();

    Throwable e = catchThrowable(() -> context.goal("build")
        .set(configuration()
            .set(configurationValue("invalidateOnFailure", DOES_NOT_INVALIDATE_ON_FAILURE_VALUE)))
        .execute());

    assertThat(e).isNull();
    assertThat(newChecksumFile.getValue())
        .doesNotExist();
  }

  @Test
  public void invalidateOnFailureWhenInterrupted(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doAnswer(invocation -> {
      File checksumFile = invocation.getArgument(0, File.class);
      checksumFile.getParentFile().mkdirs();
      checksumFile.createNewFile();
      return null;
    })
        .when(checksum).store(newChecksumFile.capture());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doThrow(new InterruptedException())
        .when(execution).success();

    Throwable e = catchThrowable(() -> context.goal("build")
        .set(configuration()
            .set(configurationValue("invalidateOnFailure", DOES_NOT_INVALIDATE_ON_FAILURE_VALUE)))
        .execute());

    assertThat(e)
        .hasCauseInstanceOf(MojoExecutionException.class);
    assertThat(newChecksumFile.getValue())
        .doesNotExist();
  }

  @Test
  public void buildWithDefaultInputDirectory(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(directory.capture(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(oldChecksumFile.capture());
    doNothing()
        .when(checksum).store(newChecksumFile.capture());
    doReturn(packer)
        .when(packerFactory).getPacker(packerTerminal.capture());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .execute();
    Optional<File> workingDir = packerTerminal.getValue().getWorkingDir();

    assertThat(directory.getValue())
        .isEqualTo(new File(baseDir, DEFAULT_INPUT_DIRECTORY_PATH_FROM_BASEDIR));
    assertThat(workingDir)
        .hasValue(new File(baseDir, DEFAULT_INPUT_DIRECTORY_PATH_FROM_BASEDIR));
    assertThat(oldChecksumFile.getValue().getParentFile())
        .isEqualTo(new File(baseDir, DEFAULT_INPUT_DIRECTORY_PATH_FROM_BASEDIR));
    assertThat(newChecksumFile.getValue().getParentFile())
        .isEqualTo(new File(baseDir, DEFAULT_INPUT_DIRECTORY_PATH_FROM_BASEDIR));
  }

  @Test
  public void buildWithSomeAbsoluteInputDirectory(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(directory.capture(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(oldChecksumFile.capture());
    doNothing()
        .when(checksum).store(newChecksumFile.capture());
    doReturn(packer)
        .when(packerFactory).getPacker(packerTerminal.capture());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationValue("inputDirectory", SOME_ABSOLUTE_INPUT_DIRECTORY_VALUE)))
        .execute();
    Optional<File> workingDir = packerTerminal.getValue().getWorkingDir();

    assertThat(directory.getValue())
        .isEqualTo(new File(SOME_ABSOLUTE_INPUT_DIRECTORY_PATH));
    assertThat(workingDir)
        .hasValue(new File(SOME_ABSOLUTE_INPUT_DIRECTORY_PATH));
    assertThat(newChecksumFile.getValue().getParentFile())
        .isEqualTo(new File(SOME_ABSOLUTE_INPUT_DIRECTORY_PATH));
    assertThat(oldChecksumFile.getValue().getParentFile())
        .isEqualTo(new File(SOME_ABSOLUTE_INPUT_DIRECTORY_PATH));
  }

  @Test
  public void buildWithSomeRelativeInputDirectory(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(directory.capture(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(oldChecksumFile.capture());
    doNothing()
        .when(checksum).store(newChecksumFile.capture());
    doReturn(packer)
        .when(packerFactory).getPacker(packerTerminal.capture());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationValue("inputDirectory", SOME_RELATIVE_INPUT_DIRECTORY_VALUE)))
        .execute();
    Optional<File> workingDir = packerTerminal.getValue().getWorkingDir();

    assertThat(directory.getValue())
        .isEqualTo(new File(baseDir, SOME_RELATIVE_INPUT_DIRECTORY_PATH_FROM_BASEDIR));
    assertThat(workingDir)
        .hasValue(new File(baseDir, SOME_RELATIVE_INPUT_DIRECTORY_PATH_FROM_BASEDIR));
    assertThat(oldChecksumFile.getValue().getParentFile())
        .isEqualTo(new File(baseDir, SOME_RELATIVE_INPUT_DIRECTORY_PATH_FROM_BASEDIR));
    assertThat(newChecksumFile.getValue().getParentFile())
        .isEqualTo(new File(baseDir, SOME_RELATIVE_INPUT_DIRECTORY_PATH_FROM_BASEDIR));
  }

  @Test
  public void buildWithDefaultTemplate(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            eq(DEFAULT_TEMPLATE),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .execute();
  }

  @Test
  public void buildWithBlankTemplate(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());

    Throwable e = catchThrowable(() -> context.goal("build")
        .set(configuration()
            .set(configurationValue("template", BLANK_TEMPLATE_VALUE)))
        .execute());

    assertThat(e)
        .hasCauseInstanceOf(MojoExecutionException.class);
  }

  @Test
  public void buildWithSomeTemplate(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            eq(SOME_TEMPLATE),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationValue("template", SOME_TEMPLATE_VALUE)))
        .execute();
  }

  @Test
  public void buildWithDefaultForce(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            eq(DO_NOT_FORCE),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .execute();
  }

  @Test
  public void buildWithForce(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            eq(FORCE),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationValue("force", FORCE_VALUE)))
        .execute();
  }

  @Test
  public void buildWithDefaultOnly(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            eq(EMPTY_ONLY),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .execute();
  }

  @Test
  public void buildWithBlankOnly(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            eq(EMPTY_ONLY),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationValue("only", BLANK_ONLY_VALUE)))
        .execute();
  }

  @Test
  public void buildWithSomeOnly(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            eq(SOME_ONLY),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationValue("only", SOME_ONLY_VALUE)))
        .execute();
  }

  @Test
  public void buildWithDefaultExcept(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            eq(EMPTY_EXCEPT),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .execute();
  }

  @Test
  public void buildWithBlankExcept(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            eq(EMPTY_EXCEPT),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationValue("except", BLANK_EXCEPT_VALUE)))
        .execute();
  }

  @Test
  public void buildWithSomeExcept(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            eq(SOME_EXCEPT),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationValue("except", SOME_EXCEPT_VALUE)))
        .execute();
  }

  @Test
  public void buildWithDefaultVars(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            eq(EMPTY_VARS),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .execute();
  }

  @Test
  public void buildWithEmptyVars(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            eq(EMPTY_VARS),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationNode("vars")))
        .execute();
  }

  @Test
  public void buildWithSomeVars(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            eq(SOME_VARS),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationNode("vars")
                .set(configurationNode("property")
                    .set(configurationValue("name", FIRST_VAR_NAME_VALUE))
                    .set(configurationValue("value", FIRST_VAR_VALUE_VALUE)))
                .set(configurationNode("property")
                    .set(configurationValue("name", SECOND_VAR_NAME_VALUE))
                    .set(configurationValue("value", SECOND_VAR_VALUE_VALUE)))))
        .execute();
  }

  @Test
  public void buildWithDefaultVarFiles(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            eq(EMPTY_VAR_FILES));
    doNothing()
        .when(execution).success();

    context.goal("build")
        .execute();
  }

  @Test
  public void buildWithEmptyVarFiles(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            eq(EMPTY_VAR_FILES));
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationNode("varFiles")))
        .execute();
  }

  @Test
  public void buildWithSomeVarFiles(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(any());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            eq(SOME_VAR_FILES));
    doNothing()
        .when(execution).success();

    context.goal("build")
        .set(configuration()
            .set(configurationNode("varFiles")
                .set(configurationValue("varFile", FIRST_VAR_FILE_VALUE))
                .set(configurationValue("varFile", SECOND_VAR_FILE_VALUE))))
        .execute();
  }

  @Test
  public void buildChecksumFailure(MavenPluginContext context) throws Exception {
    doThrow(new IOException())
        .when(checksumFactory).getChecksum(any(), any());

    Throwable e = catchThrowable(() -> context.goal("build")
        .execute());

    assertThat(e)
        .hasCauseInstanceOf(MojoExecutionException.class);
    verify(packerFactory, never())
        .getPacker(any());
  }

  @Test
  public void teminalReceiveUIMessageMessage(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(packerTerminal.capture());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    AccumulatedMavenPluginLogger logger = new AccumulatedMavenPluginLogger();
    context.setLogger(logger);
    context.goal("build")
        .execute();
    packerTerminal.getValue().receive(SOME_UI_MESSAGE_MESSAGE);

    assertThat(logger.getAccumulated())
        .hasSize(2)
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_INFO);
          assertThat(message.getContent())
              .hasValue(FIRST_MESSAGE_LINE);
          assertThat(message.getError())
              .isEmpty();
        }, atIndex(0))
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_INFO);
          assertThat(message.getContent())
              .hasValue(SECOND_MESSAGE_LINE);
          assertThat(message.getError())
              .isEmpty();
        }, atIndex(1));
  }

  @Test
  public void teminalReceiveUIMessageSay(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(packerTerminal.capture());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    AccumulatedMavenPluginLogger logger = new AccumulatedMavenPluginLogger();
    context.setLogger(logger);
    context.goal("build")
        .execute();
    packerTerminal.getValue().receive(SOME_UI_MESSAGE_SAY);

    assertThat(logger.getAccumulated())
        .hasSize(2)
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_INFO);
          assertThat(message.getContent())
              .hasValue(FIRST_MESSAGE_LINE);
          assertThat(message.getError())
              .isEmpty();
        }, atIndex(0))
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_INFO);
          assertThat(message.getContent())
              .hasValue(SECOND_MESSAGE_LINE);
          assertThat(message.getError())
              .isEmpty();
        }, atIndex(1));
  }

  @Test
  public void teminalReceiveUIMessageError(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(packerTerminal.capture());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    AccumulatedMavenPluginLogger logger = new AccumulatedMavenPluginLogger();
    context.setLogger(logger);
    context.goal("build")
        .execute();
    packerTerminal.getValue().receive(SOME_UI_MESSAGE_ERROR);

    assertThat(logger.getAccumulated())
        .hasSize(2)
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_ERROR);
          assertThat(message.getContent())
              .hasValue(FIRST_MESSAGE_LINE);
          assertThat(message.getError())
              .isEmpty();
        }, atIndex(0))
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_ERROR);
          assertThat(message.getContent())
              .hasValue(SECOND_MESSAGE_LINE);
          assertThat(message.getError())
              .isEmpty();
        }, atIndex(1));
  }

  @Test
  public void teminalReceiveUIMessageOther(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(packerTerminal.capture());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    AccumulatedMavenPluginLogger logger = new AccumulatedMavenPluginLogger();
    context.setLogger(logger);
    context.goal("build")
        .execute();
    packerTerminal.getValue().receive(SOME_UI_MESSAGE_OTHER);

    assertThat(logger.getAccumulated())
        .hasSize(2)
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_DEBUG);
          assertThat(message.getContent())
              .hasValue(FIRST_MESSAGE_LINE);
          assertThat(message.getError())
              .isEmpty();
        }, atIndex(0))
        .satisfies(message -> {
          assertThat(message.getLevel())
              .isEqualTo(LOG_DEBUG);
          assertThat(message.getContent())
              .hasValue(SECOND_MESSAGE_LINE);
          assertThat(message.getError())
              .isEmpty();
        }, atIndex(1));
  }

  @Test
  public void teminalReceiveIgnoreNonUIMessage(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(packerTerminal.capture());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    AccumulatedMavenPluginLogger logger = new AccumulatedMavenPluginLogger();
    context.setLogger(logger);
    context.goal("build")
        .execute();
    packerTerminal.getValue().receive(SOME_NON_UI_MESSAGE);

    assertThat(logger.getAccumulated())
        .isEmpty();
  }

  @Test
  public void terminalSendNotSupported(
      MavenPluginContext context,
      @Mock Checksum checksum,
      @Mock Packer packer,
      @Mock PackerExecution execution,
      @Mock PackerInput input)
      throws Exception {
    doReturn(checksum)
        .when(checksumFactory).getChecksum(any(), any());
    doReturn(CHEKSUM_MISMATCH)
        .when(checksum).match(any());
    doReturn(packer)
        .when(packerFactory).getPacker(packerTerminal.capture());
    doReturn(execution)
        .when(packer).build(
            anyString(),
            anyBoolean(),
            anySet(),
            anySet(),
            anyMap(),
            anySet());
    doNothing()
        .when(execution).success();

    context.goal("build")
        .execute();
    Throwable e = catchThrowable(() -> packerTerminal.getValue().send(input));

    assertThat(e)
        .isInstanceOf(PackerAbortionException.class);
    verify(input, never())
        .write(anyString());
  }
}
