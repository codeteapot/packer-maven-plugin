package com.github.codeteapot.maven.plugins.packer.tools;

import com.github.codeteapot.tools.packer.Packer;
import com.github.codeteapot.tools.packer.PackerTerminal;

/**
 * Default implementation.
 */
public class DefaultPackerFactory implements PackerFactory {

  /**
   * Default constructor.
   */
  public DefaultPackerFactory() {}

  @Override
  public Packer getPacker(PackerTerminal terminal) {
    return new Packer(terminal);
  }
}
