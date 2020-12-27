package com.github.codeteapot.tools.packer;

import java.time.Instant;
import java.util.Optional;

/**
 * Message of Packer output.
 *
 * @see PackerTerminal#receive(PackerMessage)
 */
public interface PackerMessage {

  /**
   * Means that the information being provided is a human-readable string.
   * 
   * <p>There are three {@code data} subtypes:
   * <ul>
   * <li>{@link PackerMessage#DATA_UI_SAY}</li>
   * <li>{@link PackerMessage#DATA_UI_MESSAGE}</li>
   * <li>{@link PackerMessage#DATA_UI_ERROR}</li>
   * </ul>
   */
  public static final String TYPE_UI = "ui";
  
  /**
   * Tells how many artifacts a particular build produced.
   */
  public static final String TYPE_ARTIFACT_COUNT = "artifact-count";
  
  /**
   * Tells information about Packer created during this build.
   * 
   * <p>There are three data entry fields for each message.
   * <dl>
   * <dt>0</dt>
   * <dd>artifact_number</dd>
   * </dl>
   * <dl>
   * <dt>1</dt>
   * <dd>key</dd>
   * </dl>
   * <dl>
   * <dt>2</dt>
   * <dd>value</dd>
   * </dl>
   * 
   * <p>There are five available field keys for each artifact.
   * <ul>
   * <li>{@link PackerMessage#DATA_ARTIFACT_BUILDER_ID}</li>
   * <li>{@link PackerMessage#DATA_ARTIFACT_ID}</li>
   * <li>{@link PackerMessage#DATA_ARTIFACT_STRING}</li>
   * <li>{@link PackerMessage#DATA_ARTIFACT_FILES_COUNT}</li>
   * <li>{@link PackerMessage#DATA_ARTIFACT_END}</li>
   * </ul>
   */
  public static final String TYPE_ARTIFACT = "artifact";
  
  /**
   * What version of Packer is running.
   */
  public static final String TYPE_VERSION = "version";
  
  /**
   * Data will contain {@code dev} if version is prerelease, and otherwise will be blank.
   */
  public static final String TYPE_VERSION_PRERELEASE = "version-prerelease";
  
  /**
   * The <i>git</i> hash for the commit that the branch of Packer is currently on; most useful for
   * Packer developers.
   */
  public static final String TYPE_VERSION_COMMIT = "version-commit";

  /**
   * Normally used for announcements about beginning new steps in the build process.
   */
  public static final String DATA_UI_SAY = "say";
  
  /**
   * Used for basic updates during the build process.
   */
  public static final String DATA_UI_MESSAGE = "message";
  
  /**
   * Reserved for errors.
   */
  public static final String DATA_UI_ERROR = "error";
  
  /**
   * Artifact {@code builder-id} key.
   */
  public static final String DATA_ARTIFACT_BUILDER_ID = "builder-id";
  
  /**
   * Artifact {@code id} key.
   */
  public static final String DATA_ARTIFACT_ID = "id";
  
  /**
   * Artifact {@code string} key.
   */
  public static final String DATA_ARTIFACT_STRING = "string";
  
  /**
   * Artifact {@code files-count} key.
   */
  public static final String DATA_ARTIFACT_FILES_COUNT = "files-count";
  
  /**
   * Artifact {@code end} key.
   */
  public static final String DATA_ARTIFACT_END = "end";

  /**
   * The timestamp of when message was printed.
   *
   * @return The message {@code timestamp} value.
   */
  Instant getTimestamp();

  /**
   * When {@code build} command is executed this can be either empty or individual build names.
   * 
   * <p>It is normally empty when builds are in progress, and the build name when artifacts of
   * particular builds are being referred to.
   *
   * @return The target on {@code build} command, that can be empty.
   */
  Optional<String> getTarget();

  /**
   * The type of message being outputted.
   *
   * @return The type of the message.
   */
  String getType();

  /**
   * Values associated with the type.
   * 
   * <p>The exact amount and meaning of this data is type-dependent.
   *
   * @return Those values associated with the type.
   */
  String[] getData();
}
