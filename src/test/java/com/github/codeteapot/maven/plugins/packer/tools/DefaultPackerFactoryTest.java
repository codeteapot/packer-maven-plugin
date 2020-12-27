package com.github.codeteapot.maven.plugins.packer.tools;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.github.codeteapot.maven.plugins.packer.tools.DefaultPackerFactory;
import com.github.codeteapot.tools.packer.Packer;
import com.github.codeteapot.tools.packer.PackerTerminal;

@ExtendWith(MockitoExtension.class)
public class DefaultPackerFactoryTest {

  private DefaultPackerFactory packerFactory;

  @BeforeEach
  public void setUp() throws Exception {
    packerFactory = new DefaultPackerFactory();
  }

  @Test
  public void getPackerIsNotSupported(@Mock PackerTerminal terminal) throws Exception {
    Packer packer = packerFactory.getPacker(terminal);

    assertThat(packer).isNotNull();
  }
}
