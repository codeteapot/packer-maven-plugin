package com.github.codeteapot.tools.packer;

import java.io.InputStream;

interface PackerTerminalTaskFactory {

  PackerTerminalTask getTerminalTask(PackerAbortable execution, InputStream input);
}
