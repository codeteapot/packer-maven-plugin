package com.github.codeteapot.tools.checksum;

import static java.nio.file.Files.write;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("integration")
@ExtendWith(MockitoExtension.class)
public class ChecksumEntryManagerTest {

  private static final MessageDigestCreator DEFAULT_DIGEST_CREATOR = MessageDigest::getInstance;
  private static final String KNOWN_ALGORITHM = "SHA-256";
  private static final String UNKNOWN_ALGORITHM = "UNKNOWN";

  private static final Predicate<String> DO_NOT_IGNORE_ANY_FILE = path -> false;
  private static final Predicate<String> IGNORE_THIRD_FILE = path -> "third.tmp".equals(path);

  private static final IOException LOAD_IO_EXCEPTION = new IOException();

  private static final byte[] ARBITRARY_FILE_CONTENT = {0x01, 0x02, 0x03};

  private static final String FIRST_DIRECTORY_NAME = "file";
  private static final String FIRST_FILE_NAME = "first.txt";
  private static final byte[] FIRST_FILE_CONTENT = {0x40, 0x41, 0x42};

  private static final String SECOND_FILE_NAME = "second.doc";

  private static final String THIRD_FILE_NAME = "third.tmp";

  private static final String FIRST_FILE_PATH = "file/first.txt";
  private static final byte[] FIRST_FILE_HASH = {
      -96, 85, -123, 34, -101, 29, 41, -112, -99, -92, 52, 115, -115, 34, 62, -91, -104, 55, 116,
      86, 35, -17, 7, -25, -15, -85, 124, -67, 106, 27, -109, 125
  };

  private static final String SECOND_FILE_PATH = "second.doc";
  private static final byte[] SECOND_FILE_HASH = {
      -29, -80, -60, 66, -104, -4, 28, 20, -102, -5, -12, -56, -103, 111, -71, 36, 39, -82, 65, -28,
      100, -101, -109, 76, -92, -107, -103, 27, 120, 82, -72, 85
  };

  private static final String SOME_CHECKSUM_FILE_NAME = "check.sum";

  private static final String FIRST_ENTRY_FILE_CONTENT =
      "A05585229B1D29909DA434738D223EA59837745623EF07E7F1AB7CBD6A1B937D file/first.txt";

  private static final String SECOND_ENTRY_FILE_CONTENT =
      "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855 second.doc";

  private static final String FIRST_AND_SECOND_ENTRIES_FILE_CONTENT = ""
      + "A05585229B1D29909DA434738D223EA59837745623EF07E7F1AB7CBD6A1B937D file/first.txt\n"
      + "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855 second.doc";

  private ChecksumEntryManager entryManager;

  @BeforeEach
  public void setUp() {
    entryManager = new ChecksumEntryManager();
  }

  @Test
  public void generateEntrySet(@TempDir File someDirectory) throws Exception {
    File firstDirectory = new File(someDirectory, FIRST_DIRECTORY_NAME);
    firstDirectory.mkdir();
    File firstFile = new File(firstDirectory, FIRST_FILE_NAME);
    firstFile.createNewFile();
    write(firstFile.toPath(), FIRST_FILE_CONTENT);
    File secondFile = new File(someDirectory, SECOND_FILE_NAME);
    secondFile.createNewFile();
    File thirdFile = new File(someDirectory, THIRD_FILE_NAME);
    thirdFile.createNewFile();

    Set<ChecksumEntry> entrySet = entryManager.generate(
        DEFAULT_DIGEST_CREATOR,
        KNOWN_ALGORITHM,
        someDirectory,
        IGNORE_THIRD_FILE);

    assertThat(entrySet)
        .hasSize(2)
        .anySatisfy(entry -> {
          assertThat(entry.path).isEqualTo(FIRST_FILE_PATH);
          assertThat(entry.hash).isEqualTo(FIRST_FILE_HASH);
        })
        .anySatisfy(entry -> {
          assertThat(entry.path).isEqualTo(SECOND_FILE_PATH);
          assertThat(entry.hash).isEqualTo(SECOND_FILE_HASH);
        });
  }

  @Test
  public void generateWithIOError(@TempDir File someDirectory) throws Exception {
    File firstDirectory = new File(someDirectory, FIRST_DIRECTORY_NAME);
    firstDirectory.mkdir();
    File firstFile = new File(firstDirectory, FIRST_FILE_NAME);
    firstFile.createNewFile();
    firstFile.setReadable(false);

    Throwable e = catchThrowable(() -> entryManager.generate(
        DEFAULT_DIGEST_CREATOR,
        KNOWN_ALGORITHM,
        someDirectory,
        DO_NOT_IGNORE_ANY_FILE));

    assertThat(e).isInstanceOf(IOException.class);
  }

  @Test
  public void generateWithNoSuchAlgorithmError(@TempDir File someDirectory) throws Exception {
    File firstDirectory = new File(someDirectory, FIRST_DIRECTORY_NAME);
    firstDirectory.mkdir();
    File firstFile = new File(firstDirectory, FIRST_FILE_NAME);
    firstFile.createNewFile();

    Throwable e = catchThrowable(() -> entryManager.generate(
        DEFAULT_DIGEST_CREATOR,
        UNKNOWN_ALGORITHM,
        someDirectory,
        DO_NOT_IGNORE_ANY_FILE));

    assertThat(e).isInstanceOf(NoSuchAlgorithmException.class);
  }

  @Test
  public void loadExistingFile(@TempDir File someChecksumDir) throws Exception {
    File checksumFile = new File(someChecksumDir, SOME_CHECKSUM_FILE_NAME);
    checksumFile.createNewFile();
    write(checksumFile.toPath(), Stream.of(
        FIRST_ENTRY_FILE_CONTENT,
        SECOND_ENTRY_FILE_CONTENT)
        .collect(toSet()));

    Set<ChecksumEntry> entrySet = entryManager.load(checksumFile);

    assertThat(entrySet)
        .hasSize(2)
        .anySatisfy(entry -> {
          assertThat(entry.path).isEqualTo(FIRST_FILE_PATH);
          assertThat(entry.hash).isEqualTo(FIRST_FILE_HASH);
        })
        .anySatisfy(entry -> {
          assertThat(entry.path).isEqualTo(SECOND_FILE_PATH);
          assertThat(entry.hash).isEqualTo(SECOND_FILE_HASH);
        });
  }

  @Test
  public void loadNonExistingFile(@TempDir File someChecksumDir) throws Exception {
    File checksumFile = new File(someChecksumDir, SOME_CHECKSUM_FILE_NAME);

    Set<ChecksumEntry> entrySet = entryManager.load(checksumFile);

    assertThat(entrySet).isEmpty();
  }

  @Test
  public void loadWithIOError(@Mock File invalidChecksumFile) throws Exception {
    when(invalidChecksumFile.getPath())
        .thenThrow(new UncheckedIOException(LOAD_IO_EXCEPTION));

    Throwable e = catchThrowable(() -> entryManager.load(invalidChecksumFile));

    assertThat(e).isEqualTo(LOAD_IO_EXCEPTION);
  }

  @Test
  public void storeExistingFile(@TempDir File someChecksumDir) throws Exception {
    File checksumFile = new File(someChecksumDir, SOME_CHECKSUM_FILE_NAME);
    checksumFile.createNewFile();
    write(checksumFile.toPath(), ARBITRARY_FILE_CONTENT);

    entryManager.store(checksumFile, Stream.of(
        new ChecksumEntry(FIRST_FILE_PATH, FIRST_FILE_HASH),
        new ChecksumEntry(SECOND_FILE_PATH, SECOND_FILE_HASH))
        .collect(toSet()));

    assertThat(checksumFile)
        .hasContent(FIRST_AND_SECOND_ENTRIES_FILE_CONTENT);
  }

  @Test
  public void storeNonExistingFile(@TempDir File someChecksumDir) throws Exception {
    File checksumFile = new File(someChecksumDir, SOME_CHECKSUM_FILE_NAME);

    entryManager.store(checksumFile, Stream.of(
        new ChecksumEntry(FIRST_FILE_PATH, FIRST_FILE_HASH),
        new ChecksumEntry(SECOND_FILE_PATH, SECOND_FILE_HASH))
        .collect(toSet()));

    assertThat(checksumFile)
        .hasContent(FIRST_AND_SECOND_ENTRIES_FILE_CONTENT);
  }
}
