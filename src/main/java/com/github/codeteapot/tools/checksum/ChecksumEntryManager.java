package com.github.codeteapot.tools.checksum;

import static java.nio.file.Files.walk;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.function.Predicate;

class ChecksumEntryManager {

  private static final int DIGEST_BUFFER_SIZE = 4 * 1024;

  ChecksumEntryManager() {}

  public Set<ChecksumEntry> generate(MessageDigestCreator digestCreator, String algorithm,
      File directory, Predicate<String> ignoreFile) throws IOException, NoSuchAlgorithmException {
    try {
      return walk(directory.toPath())
          .filter(ChecksumEntryManager::isRegularFile)
          .map(path -> new ChecksumPath(directory, path))
          .filter(path -> path.isNotIgnored(ignoreFile))
          .map(path -> path.toChecksumEntry(digestCreator, algorithm))
          .collect(toSet());
    } catch (UncheckedIOException e) {
      throw e.getCause();
    } catch (UncheckedNoSuchAlgorithmException e) {
      throw e.getCause();
    }
  }

  public Set<ChecksumEntry> load(File checksumFile) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(checksumFile))) {
      return reader.lines()
          .map(ChecksumEntry::parse)
          .collect(toSet());
    } catch (FileNotFoundException e) {
      return emptySet();
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  public void store(File checksumFile, Set<ChecksumEntry> entrySet) throws IOException {
    try (PrintWriter writer = new PrintWriter(checksumFile)) {
      entrySet.stream()
          .sorted(comparing(ChecksumEntry::getPath))
          .forEach(writer::println);
    }
  }

  private static boolean isRegularFile(Path path) {
    return path.toFile().isFile();
  }

  private static class ChecksumPath {

    private final File file;
    private final String relativePath;

    private ChecksumPath(File directory, Path path) {
      file = path.toFile();
      relativePath = directory.toPath().relativize(path).toString();
    }

    private boolean isNotIgnored(Predicate<String> ignoreFile) {
      return !ignoreFile.test(relativePath);
    }

    private ChecksumEntry toChecksumEntry(MessageDigestCreator digestCreator, String algorithm) {
      try (InputStream input = new FileInputStream(file)) {
        MessageDigest digest = digestCreator.create(algorithm);
        byte[] buf = new byte[DIGEST_BUFFER_SIZE];
        int len = input.read(buf, 0, DIGEST_BUFFER_SIZE);
        while (len > 0) {
          digest.update(buf, 0, len);
          len = input.read(buf, 0, DIGEST_BUFFER_SIZE);
        }
        return new ChecksumEntry(relativePath, digest.digest());
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      } catch (NoSuchAlgorithmException exception) {
        throw new UncheckedNoSuchAlgorithmException(exception);
      }
    }
  }

  private static class UncheckedNoSuchAlgorithmException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private UncheckedNoSuchAlgorithmException(NoSuchAlgorithmException cause) {
      super(cause);
    }

    @Override
    public NoSuchAlgorithmException getCause() {
      return (NoSuchAlgorithmException) super.getCause();
    }
  }
}
