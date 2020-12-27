package com.github.codeteapot.maven.plugins.packer.tools;

import com.github.codeteapot.tools.packer.Packer;
import com.github.codeteapot.tools.packer.PackerTerminal;

/**
 * Factory for creating a {@link Packer} instance with the given terminal.
 *
 * @see PackerTerminal
 */
public interface PackerFactory {

  /**
   * Role of Plexus component.
   */
  String ROLE = PackerFactory.class.getName();

  /**
   * Get access to Packer with the given terminal.
   *
   * @param terminal Terminal that will be used by Packer.
   * 
   * @return The Packer instance with the given terminal.
   */
  Packer getPacker(PackerTerminal terminal);
}
