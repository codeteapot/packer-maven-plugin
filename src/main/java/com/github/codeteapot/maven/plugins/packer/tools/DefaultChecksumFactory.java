package com.github.codeteapot.maven.plugins.packer.tools;

import com.github.codeteapot.tools.checksum.Checksum;
import com.github.codeteapot.tools.checksum.MessageDigestCreator;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Predicate;

/**
 * Default implementation.
 */
public class DefaultChecksumFactory implements ChecksumFactory {

  private static final MessageDigestCreator DIGEST_CREATOR = MessageDigest::getInstance;
  private static final String CHECKSUM_ALGORITHM = "SHA-256";

  /**
   * Default constructor.
   */
  public DefaultChecksumFactory() {}

  @Override
  public Checksum getChecksum(File directory, Predicate<String> ignoreFile)
      throws IOException, NoSuchAlgorithmException {
    return new Checksum(DIGEST_CREATOR, CHECKSUM_ALGORITHM, directory, ignoreFile);
  }
}
