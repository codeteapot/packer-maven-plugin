package com.github.codeteapot.tools.checksum;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Checksum of a directory, based on the content of its files.
 */
public class Checksum {

  private final ChecksumEntryManager entryManager;
  private final Set<ChecksumEntry> entrySet;

  /**
   * Checksum for the given directory.
   *
   * @param digestCreator Creator of {@link MessageDigest} used for calculating the hash of the
   *        content of the directory files.
   * @param algorithm Algorithm used to calculate the hash of directory files content.
   * @param directory Directory where the checksum calculation is applied.
   * @param ignoreFile Predicate that determines the file with the given path relative to specified
   *        directory must be ignored.
   * 
   * @throws IOException If some IO error has been occurred.
   * @throws NoSuchAlgorithmException If the given algorithm is not known.
   */
  public Checksum(MessageDigestCreator digestCreator, String algorithm, File directory,
      Predicate<String> ignoreFile) throws IOException, NoSuchAlgorithmException {
    this(new ChecksumEntryManager(), digestCreator, algorithm, directory, ignoreFile);
  }

  Checksum(ChecksumEntryManager entryManager, MessageDigestCreator digestCreator, String algorithm,
      File directory, Predicate<String> ignoreFile) throws IOException, NoSuchAlgorithmException {
    this.entryManager = entryManager;
    this.entrySet = this.entryManager.generate(digestCreator, algorithm, directory, ignoreFile);
  }

  /**
   * Determines if this checksum matches with a checksum previously stored to the given file.
   * 
   * <p>If checksum file does not exist, it matches when this checksum was generated on a directory
   * that does not contain any non-ignored regular file.
   *
   * @param checksumFile The file where checksum to compare is in.
   * 
   * @return {@code true} if, and only if this checksum matches with a checksum stored on the given
   *         file.
   * 
   * @throws IOException If some IO error has been occurred.
   */
  public boolean match(File checksumFile) throws IOException {
    return entrySet.equals(entryManager.load(checksumFile));
  }

  /**
   * Store this checksum to the given file.
   * 
   * <p>The content of the file is overwritten, or it is created if it does not exist.
   *
   * @param checksumFile The file where this checksum will be stored.
   * 
   * @throws IOException If some IO error has been occurred.
   */
  public void store(File checksumFile) throws IOException {
    entryManager.store(checksumFile, entrySet);
  }
}
