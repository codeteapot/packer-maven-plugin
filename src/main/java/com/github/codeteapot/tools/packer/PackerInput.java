package com.github.codeteapot.tools.packer;

/**
 * Input of packer terminal.
 * 
 * <p>It allows to write anything to program input stream.
 *
 * @see PackerTerminal#send(PackerInput)
 */
public interface PackerInput {

  /**
   * Write something to program input stream.
   *
   * @param str String to be written.
   */
  void write(String str);
}
