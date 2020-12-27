package com.github.codeteapot.tools.packer;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PackerTest {

  private static final String BUILD_COMMAND = "build";

  private static final String FORCE_ARG_NAME = "-force";
  private static final String ONLY_ARG_NAME = "-only";
  private static final String EXCEPT_ARG_NAME = "-except";
  private static final String VAR_ARG_NAME = "-var";
  private static final String VAR_FILE_ARG_NAME = "-var-file";

  private static final PackerExecution SOME_EXECUTION = new TestPackerExecution();

  private static final boolean DO_FORCE = true;
  private static final boolean DO_NOT_FORCE = false;

  private static final String ANY_TEMPLATE = "any-template.json";

  private static final Set<String> EMPTY_ONLY = emptySet();
  private static final Set<String> EMPTY_EXCEPT = emptySet();
  private static final Map<String, Object> EMPTY_VARS = emptyMap();
  private static final Set<String> EMPTY_VAR_FILES = emptySet();

  private static final String SOME_TEMPLATE = "some-template.json";
  private static final Set<String> SOME_ONLY = Stream.of("first-only", "second-only")
      .collect(toSet());
  private static final Set<String> SOME_EXCEPT = Stream.of("first-except", "second-except")
      .collect(toSet());
  private static final Map<String, Object> SOME_VARS = Stream.of(
      new SimpleEntry<>("first-var-name", "first-var-value"),
      new SimpleEntry<>("second-var-name", "second-var-value"))
      .collect(toMap(Entry::getKey, Entry::getValue));
  private static final Set<String> SOME_VAR_FILES = Stream.of("first-var-file", "second-var-file")
      .collect(toSet());

  private static final String SOME_ONLY_ARG_VALUE = "first-only,second-only";
  private static final String SOME_ONLY_ARG_REVERSED_VALUE = "second-only,first-only";
  private static final String SOME_EXCEPT_ARG_VALUE = "first-except,second-except";
  private static final String SOME_EXCEPT_ARG_REVERSED_VALUE = "second-except,first-except";
  private static final String SOME_FIRST_VAR_ARG_VALUE = "first-var-name=first-var-value";
  private static final String SOME_SECOND_VAR_ARG_VALUE = "second-var-name=second-var-value";
  private static final String SOME_FIRST_VAR_FILE_ARG_VALUE = "first-var-file";
  private static final String SOME_SECOND_VAR_FILE_ARG_VALUE = "second-var-file";

  @Mock
  private PackerExecutor executor;

  private Packer packer;

  @BeforeEach
  public void setUp() {
    packer = new Packer(executor);
  }

  @Test
  public void buildWithSomeTemplate() throws Exception {
    when(executor.execute(eq(BUILD_COMMAND), argThat(args -> args.size() == 1
        && args.get(0).equals(SOME_TEMPLATE))))
            .thenReturn(SOME_EXECUTION);

    PackerExecution execution = packer.build(
        SOME_TEMPLATE,
        DO_NOT_FORCE,
        EMPTY_ONLY,
        EMPTY_EXCEPT,
        EMPTY_VARS,
        EMPTY_VAR_FILES);

    assertThat(execution).isEqualTo(SOME_EXECUTION);
  }

  @Test
  public void buildWithForce() throws Exception {
    when(executor.execute(eq(BUILD_COMMAND), argThat(args -> args.size() == 2
        && args.get(0).equals(FORCE_ARG_NAME))))
            .thenReturn(SOME_EXECUTION);

    PackerExecution execution = packer.build(
        ANY_TEMPLATE,
        DO_FORCE,
        EMPTY_ONLY,
        EMPTY_EXCEPT,
        EMPTY_VARS,
        EMPTY_VAR_FILES);

    assertThat(execution).isEqualTo(SOME_EXECUTION);
  }

  @Test
  public void buildWithSomeOnly() throws Exception {
    when(executor.execute(eq(BUILD_COMMAND), argThat(args -> args.size() == 3
        && args.get(0).equals(ONLY_ARG_NAME)
        && Stream.of(SOME_ONLY_ARG_VALUE, SOME_ONLY_ARG_REVERSED_VALUE)
            .anyMatch(value -> value.equals(args.get(1))))))
                .thenReturn(SOME_EXECUTION);

    PackerExecution execution = packer.build(
        ANY_TEMPLATE,
        DO_NOT_FORCE,
        SOME_ONLY,
        EMPTY_EXCEPT,
        EMPTY_VARS,
        EMPTY_VAR_FILES);

    assertThat(execution).isEqualTo(SOME_EXECUTION);
  }

  @Test
  public void buildWithSomeExcept() throws Exception {
    when(executor.execute(eq(BUILD_COMMAND), argThat(args -> args.size() == 3
        && args.get(0).equals(EXCEPT_ARG_NAME)
        && Stream.of(SOME_EXCEPT_ARG_VALUE, SOME_EXCEPT_ARG_REVERSED_VALUE)
            .anyMatch(value -> value.equals(args.get(1))))))
                .thenReturn(SOME_EXECUTION);

    PackerExecution execution = packer.build(
        ANY_TEMPLATE,
        DO_NOT_FORCE,
        EMPTY_ONLY,
        SOME_EXCEPT,
        EMPTY_VARS,
        EMPTY_VAR_FILES);

    assertThat(execution).isEqualTo(SOME_EXECUTION);
  }

  @Test
  public void buildWithSomeVars() throws Exception {
    when(executor.execute(eq(BUILD_COMMAND), argThat(args -> args.size() == 5
        && args.get(0).equals(VAR_ARG_NAME)
        && args.get(2).equals(VAR_ARG_NAME)
        && ((args.get(1).equals(SOME_FIRST_VAR_ARG_VALUE)
            && args.get(3).equals(SOME_SECOND_VAR_ARG_VALUE))
            || (args.get(1).equals(SOME_SECOND_VAR_ARG_VALUE)
                && args.get(3).equals(SOME_FIRST_VAR_ARG_VALUE))))))
                    .thenReturn(SOME_EXECUTION);

    PackerExecution execution = packer.build(
        ANY_TEMPLATE,
        DO_NOT_FORCE,
        EMPTY_ONLY,
        EMPTY_EXCEPT,
        SOME_VARS,
        EMPTY_VAR_FILES);

    assertThat(execution).isEqualTo(SOME_EXECUTION);
  }

  @Test
  public void buildWithSomeVarFiles() throws Exception {
    when(executor.execute(eq(BUILD_COMMAND), argThat(args -> args.size() == 5
        && args.get(0).equals(VAR_FILE_ARG_NAME)
        && args.get(2).equals(VAR_FILE_ARG_NAME)
        && ((args.get(1).equals(SOME_FIRST_VAR_FILE_ARG_VALUE)
            && args.get(3).equals(SOME_SECOND_VAR_FILE_ARG_VALUE))
            || (args.get(1).equals(SOME_SECOND_VAR_FILE_ARG_VALUE)
                && args.get(3).equals(SOME_FIRST_VAR_FILE_ARG_VALUE))))))
                    .thenReturn(SOME_EXECUTION);

    PackerExecution execution = packer.build(
        ANY_TEMPLATE,
        DO_NOT_FORCE,
        EMPTY_ONLY,
        EMPTY_EXCEPT,
        EMPTY_VARS,
        SOME_VAR_FILES);

    assertThat(execution).isEqualTo(SOME_EXECUTION);
  }
}
