package com.github.codeteapot.maven.plugins.packer.tools;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.codeteapot.maven.plugins.packer.tools.DefaultChecksumFactory;
import com.github.codeteapot.tools.checksum.Checksum;
import java.io.File;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultChecksumFactoryTest {

  private DefaultChecksumFactory checksumFactory;

  @BeforeEach
  public void setUp() throws Exception {
    checksumFactory = new DefaultChecksumFactory();
  }

  @Test
  public void getChecksum(
      @Mock Predicate<String> anyIgnoreFile,
      @TempDir File anyDirectory) throws Exception {
    Checksum checksum = checksumFactory.getChecksum(anyDirectory, anyIgnoreFile);

    assertThat(checksum).isNotNull();
  }
}
