package com.github.codeteapot.tools.checksum;

import static java.lang.String.format;
import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.util.Arrays;

class ChecksumEntry {

  final String path;
  final byte[] hash;

  ChecksumEntry(String path, byte[] hash) {
    this.path = path;
    this.hash = hash;
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    ChecksumEntry entry = (ChecksumEntry) obj;
    return Arrays.equals(hash, entry.hash);
  }

  @Override
  public String toString() {
    return format("%s %s", convert(hash), path);
  }

  String getPath() {
    return path;
  }

  static ChecksumEntry parse(String str) {
    String[] parts = str.split(" ");
    return new ChecksumEntry(parts[1], convert(parts[0]));
  }

  private static String convert(byte[] bytes) {
    return printHexBinary(bytes);
  }

  private static byte[] convert(String str) {
    return parseHexBinary(str);
  }
}
