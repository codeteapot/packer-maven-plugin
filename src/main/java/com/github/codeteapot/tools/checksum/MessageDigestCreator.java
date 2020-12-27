package com.github.codeteapot.tools.checksum;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Functional interface for creating a {@link MessageDigest} with a given algorithm, allowing to
 * throw a {@link NoSuchAlgorithmException} instance.
 */
@FunctionalInterface
public interface MessageDigestCreator {

  /**
   * Create a message digest with the given algorithm.
   *
   * @param algorithm Algorithm of the message digest.
   * 
   * @return The created message digest.
   * 
   * @throws NoSuchAlgorithmException If the algorithm is not known.
   */
  MessageDigest create(String algorithm) throws NoSuchAlgorithmException;
}
