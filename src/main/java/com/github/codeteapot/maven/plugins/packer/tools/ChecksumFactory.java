package com.github.codeteapot.maven.plugins.packer.tools;

import com.github.codeteapot.tools.checksum.Checksum;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Predicate;

/**
 * Factory for creating a {@link Checksum} instance for a given directory.
 */
public interface ChecksumFactory {

  /**
   * Role of Plexus component.
   */
  String ROLE = ChecksumFactory.class.getName();

  /**
   * Get the checksum of the given directory.
   *
   * @param directory Directory where checksum will be calculated.
   * @param ignoreFile Predicate that determines the file with the given path relative to specified
   *        directory will be ignored.
   * 
   * @return The checksum for the given directory.
   * 
   * @throws IOException If some IO error has been occurred.
   * @throws NoSuchAlgorithmException If the given algorithm is not known.
   */
  Checksum getChecksum(File directory, Predicate<String> ignoreFile)
      throws IOException, NoSuchAlgorithmException;
}
