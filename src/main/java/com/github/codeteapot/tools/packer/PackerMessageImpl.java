package com.github.codeteapot.tools.packer;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.util.Optional;

class PackerMessageImpl implements PackerMessage {

  private final Instant timestamp;
  private final String target;
  private final String type;
  private final String[] data;

  PackerMessageImpl(Instant timestamp, String target, String type, String[] data) {
    this.timestamp = requireNonNull(timestamp);
    this.target = target;
    this.type = requireNonNull(type);
    this.data = requireNonNull(data);
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public Optional<String> getTarget() {
    return ofNullable(target);
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
