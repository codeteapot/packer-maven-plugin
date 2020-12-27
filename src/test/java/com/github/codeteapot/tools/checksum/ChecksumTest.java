package com.github.codeteapot.tools.checksum;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChecksumTest {

  private static final String SOME_ALGORITHM = "SOME-ALGORITHM";
  private static final Set<ChecksumEntry> SOME_CHECKSUM_ENTRY_SET = Stream.of(
      new ChecksumEntry("file/first.txt", new byte[] {0x01, 0x02, 0x03}),
      new ChecksumEntry("second.doc", new byte[] {0x04, 0x05, 0x06}))
      .collect(toSet());
  private static final Set<ChecksumEntry> SOME_LOADED_CHECKSUM_ENTRY_SET = Stream.of(
      new ChecksumEntry("second.doc", new byte[] {0x04, 0x05, 0x06}),
      new ChecksumEntry("file/first.txt", new byte[] {0x01, 0x02, 0x03}))
      .collect(toSet());
  private static final Set<ChecksumEntry> ANOTHER_LOADED_CHECKSUM_ENTRY_SET = Stream.of(
      new ChecksumEntry("second.doc", new byte[] {0x04, 0x05, 0x06}))
      .collect(toSet());
  private static final Set<ChecksumEntry> ANOTHER_PATH_LOADED_CHECKSUM_ENTRY_SET = Stream.of(
      new ChecksumEntry("another.txt", new byte[] {0x04, 0x05, 0x06}),
      new ChecksumEntry("file/first.txt", new byte[] {0x01, 0x02, 0x03}))
      .collect(toSet());
  private static final Set<ChecksumEntry> ANOTHER_HASH_LOADED_CHECKSUM_ENTRY_SET = Stream.of(
      new ChecksumEntry("second.doc", new byte[] {0x10, 0x11, 0x12}),
      new ChecksumEntry("file/first.txt", new byte[] {0x01, 0x02, 0x03}))
      .collect(toSet());


  @Mock
  private ChecksumEntryManager entryManager;

  private Checksum checksum;

  @BeforeEach
  public void setUp(
      @Mock MessageDigestCreator someMessageDigestCreator,
      @Mock Predicate<String> someIgnoreFile,
      @TempDir File someDirectory) throws Exception {
    when(entryManager.generate(
        someMessageDigestCreator,
        SOME_ALGORITHM,
        someDirectory,
        someIgnoreFile))
            .thenReturn(SOME_CHECKSUM_ENTRY_SET);

    checksum = new Checksum(
        entryManager,
        someMessageDigestCreator,
        SOME_ALGORITHM,
        someDirectory,
        someIgnoreFile);
  }

  @Test
  public void matchWhenHasSameEntries(@TempDir File someChecksumFile) throws Exception {
    when(entryManager.load(someChecksumFile))
        .thenReturn(SOME_LOADED_CHECKSUM_ENTRY_SET);

    boolean result = checksum.match(someChecksumFile);

    assertThat(result).isTrue();
  }

  @Test
  public void notMatchWhenHasDifferentEntries(@TempDir File anotherChecksumFile) throws Exception {
    when(entryManager.load(anotherChecksumFile))
        .thenReturn(ANOTHER_LOADED_CHECKSUM_ENTRY_SET);

    boolean result = checksum.match(anotherChecksumFile);

    assertThat(result).isFalse();
  }

  @Test
  public void notMatchWhenHasDifferentEntriesByPath(@TempDir File anotherChecksumFile)
      throws Exception {
    when(entryManager.load(anotherChecksumFile))
        .thenReturn(ANOTHER_PATH_LOADED_CHECKSUM_ENTRY_SET);

    boolean result = checksum.match(anotherChecksumFile);

    assertThat(result).isFalse();
  }

  @Test
  public void notMatchWhenHasDifferentEntriesByHash(@TempDir File anotherChecksumFile)
      throws Exception {
    when(entryManager.load(anotherChecksumFile))
        .thenReturn(ANOTHER_HASH_LOADED_CHECKSUM_ENTRY_SET);

    boolean result = checksum.match(anotherChecksumFile);

    assertThat(result).isFalse();
  }

  @Test
  public void storeEntries(@TempDir File someChecksumFile) throws Exception {
    checksum.store(someChecksumFile);

    verify(entryManager)
        .store(someChecksumFile, SOME_CHECKSUM_ENTRY_SET);
  }
}
