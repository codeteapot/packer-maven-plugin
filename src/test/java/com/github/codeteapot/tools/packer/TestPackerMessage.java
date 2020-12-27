package com.github.codeteapot.tools.packer;

import static java.time.Instant.now;
import java.time.Instant;
import java.util.Optional;

public class TestPackerMessage implements PackerMessage {

  private final String type;
  private final String[] data;

  TestPackerMessage(String type) {
    this(type, new String[0]);
  }

  public TestPackerMessage(String type, String... data) {
    this.type = type;
    this.data = data;
  }

  @Override
  public Instant getTimestamp() {
    return now();
  }

  @Override
  public Optional<String> getTarget() {
    return Optional.empty();
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String[] getData() {
    return data;
  }
}
