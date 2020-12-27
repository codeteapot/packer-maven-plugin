package com.github.codeteapot.maven.plugins.packer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.codeteapot.maven.plugins.packer.PackerBuildMojo;
import com.github.codeteapot.maven.plugins.packer.tools.DefaultChecksumFactory;
import com.github.codeteapot.maven.plugins.packer.tools.PackerFactory;
import com.github.codeteapot.tools.packer.Packer;
import com.github.codeteapot.tools.packer.PackerExecution;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PackerBuildMojoAcceptanceTest {

  private static final boolean CHANGES_NEEDED = true;

  private static final String ANY_CHANGE_FILE_NAME = "any-change.txt";
  private static final String ANY_TEMPLATE = "any-template.json";

  @Test
  public void avoidUnnecessaryWorkWhenThereIsNotAnyChange(
      @Mock PackerFactory packerFactory,
      @Mock Packer packer,
      @Mock PackerExecution execution,
      @TempDir File anyInputDirectory)
      throws Exception {
    when(packerFactory.getPacker(any()))
        .thenReturn(packer);
    when(packer.build(anyString(), anyBoolean(), anySet(), anySet(), anyMap(), anySet()))
        .thenReturn(execution);
    File anyChangeFile = new File(anyInputDirectory, ANY_CHANGE_FILE_NAME);
    anyChangeFile.createNewFile();
    PackerBuildMojo mojo = new PackerBuildMojo();
    mojo.setPackerFactory(packerFactory);
    mojo.setChecksumFactory(new DefaultChecksumFactory());
    mojo.setInputDirectory(anyInputDirectory);
    mojo.setChangesNeeded(CHANGES_NEEDED);
    mojo.setTemplate(ANY_TEMPLATE);

    mojo.execute();
    mojo.execute();

    verify(packerFactory, times(1)).getPacker(any());
  }
}
