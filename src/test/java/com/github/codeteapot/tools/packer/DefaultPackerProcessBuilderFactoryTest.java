package com.github.codeteapot.tools.packer;

import static java.nio.file.Files.write;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("integration")
public class DefaultPackerProcessBuilderFactoryTest {

  private static final int ZERO_EXIT_VALUE = 0;

  private static final String SOME_COMMAND = "some-command";
  private static final List<String> SOME_ARGS = Stream.of("some-arg-1", "some-arg-2")
      .collect(toList());
  private static final String SOME_SCRIPT_CONTENT = "!/bin/sh\n" +
      "test \"$#\" -eq 4 &&\n" +
      "test \"$1\" = \"-machine-readable\" &&\n" +
      "test \"$2\" = \"some-command\" &&\n" +
      "test \"$3\" = \"some-arg-1\" &&\n" +
      "test \"$4\" = \"some-arg-2\"";

  private DefaultPackerProcessBuilderFactory defaultProcessBuilderFactory;

  @BeforeEach
  public void setUp(@TempDir File scriptDir) throws Exception {
    File script = new File(scriptDir, "packer.sh");
    write(script.toPath(), singletonList(SOME_SCRIPT_CONTENT));
    script.setReadable(true);
    script.setExecutable(true);
    defaultProcessBuilderFactory = new DefaultPackerProcessBuilderFactory(
        script.getAbsolutePath());
  }

  @Test
  public void createProgramProcessBuilder() throws Exception {
    ProcessBuilder processBuilder = defaultProcessBuilderFactory.getProcessBuilder(
        SOME_COMMAND,
        SOME_ARGS)
        .redirectOutput(Redirect.INHERIT);
    int exitValue = processBuilder.start().waitFor();

    assertThat(exitValue).isEqualTo(ZERO_EXIT_VALUE);
  }
}
