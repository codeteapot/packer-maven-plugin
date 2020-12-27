package com.github.codeteapot.tools.packer;

interface PackerAbortable {

  void abort(PackerAbortionException abortionException);
}
