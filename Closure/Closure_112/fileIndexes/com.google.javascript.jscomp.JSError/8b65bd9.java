/*
 * Copyright 2004 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.rhino.Node;

/**
 * Compile error description
 *
*
*
*
 */
public class JSError {
  /** A type of the error */
  private final DiagnosticType type;

  /** Description of the error */
  public final String description;

  /** Name of the source */
  public final String sourceName;

  /** Line number of the source */
  public final int lineNumber;

  /** Level */
  public final CheckLevel level;

  // character number
  private final int charno;

  //
  // JSError.make - static factory methods for creating JSError objects
  //
  //  The general form of the arguments is
  //
  //    [source location] [level] DiagnosticType [argument ...]
  //
  //  This order echos a typical command line diagnostic.  Source location
  //  arguments are arranged to be sources of information in the order
  //  file-line-column.
  //
  //  If the level is not given, it is taken from the level of the
  //  DiagnosticType.


  /**
   * Creates a JSError with no source information
   *
   * @param type The DiagnosticType
   * @param arguments Arguments to be incorporated into the message
   */
  public static JSError make(DiagnosticType type, String... arguments) {
    return new JSError(null, -1, -1, type, null, arguments);
  }

  /**
   * Creates a JSError at a given source location
   *
   * @param sourceName The source file name
   * @param lineno Line number with source file, or -1 if unknown
   * @param charno Column number within line, or -1 for whole line.
   * @param type The DiagnosticType
   * @param arguments Arguments to be incorporated into the message
   */
  public static JSError make(String sourceName, int lineno, int charno,
                             DiagnosticType type, String... arguments) {
    return new JSError(sourceName, lineno, charno, type, null, arguments);
  }

  /**
   * Creates a JSError at a given source location
   *
   * @param sourceName The source file name
   * @param lineno Line number with source file, or -1 if unknown
   * @param charno Column number within line, or -1 for whole line.
   * @param type The DiagnosticType
   * @param arguments Arguments to be incorporated into the message
   */
  public static JSError make(String sourceName, int lineno, int charno,
      CheckLevel level, DiagnosticType type, String... arguments) {
    return new JSError(sourceName, lineno, charno, type, level, arguments);
  }

  /**
   * Creates a JSError from a file and Node position.
   *
   * @param sourceName The source file name
   * @param n Determines the line and char position within the source file name
   * @param type The DiagnosticType
   * @param arguments Arguments to be incorporated into the message
   */
  public static JSError make(String sourceName, Node n,
                             DiagnosticType type, String... arguments) {
    return new JSError(sourceName, n, type, arguments);
  }

  /**
   * Creates a JSError from a file and Node position.
   *
   * @param sourceName The source file name
   * @param n Determines the line and char position within the source file name
   * @param type The DiagnosticType
   * @param arguments Arguments to be incorporated into the message
   */
  public static JSError make(String sourceName, Node n, CheckLevel level,
      DiagnosticType type, String... arguments) {

    return new JSError(sourceName, n.getLineno(), n.getCharno(), type, level,
        arguments);
  }

  /**
   * Creates a JSError during NodeTraversal.
   *
   * @param t Determines source file name containing current script
   * @param n Determines the line and char position within the source file name
   * @param type The DiagnosticType
   * @param arguments Arguments to be incorporated into the message
   */
  public static JSError make(NodeTraversal t, Node n,
      CheckLevel level, DiagnosticType type, String... arguments) {
    return new JSError(t.getSourceName(), n.getLineno(), n.getCharno(), type,
        level, arguments);
  }

  /**
   * Creates a JSError during NodeTraversal.
   *
   * @param t Determines source file name containing current script
   * @param n Determines the line and char position within the source file name
   * @param type The DiagnosticType
   * @param arguments Arguments to be incorporated into the message
   */
  public static JSError make(NodeTraversal t, Node n,
                             DiagnosticType type, String... arguments) {
    return new JSError(t.getSourceName(), n, type, arguments);
  }

  //
  //  JSError constructors
  //

  /**
   * Creates a JSError at a CheckLevel for a source file location.  Package
   * private to avoid any entanglement with code outside of the compiler.
   *
   * This is a preferred internal constructor.
   */
  private JSError(String sourceName, int lineno, int charno,
      DiagnosticType type, CheckLevel level, String... arguments) {
    this.type = type;
    this.description = type.format.format(arguments);
    this.lineNumber = lineno;
    this.charno = charno;
    this.sourceName = sourceName;
    this.level = level == null ? type.level : level;
  }

  /**
   * Creates a JSError for a source file location.  Package private to avoid
   * any entanglement with code outside of the compiler.
   *
   * This is a preferred internal constructor.
   */
  private JSError(String sourceName, Node node,
                  DiagnosticType type, String... arguments) {
    this(sourceName,
         (node != null) ? node.getLineno() : -1,
         (node != null) ? node.getCharno() : -1,
         type, null, arguments);
  }

  public DiagnosticType getType() {
    return type;
  }

  /**
   * Format a message at the given level.
   *
   * @return the formatted message or {@code null}
   */
  public String format(CheckLevel level, MessageFormatter formatter) {
    switch (level) {
      case ERROR:
        return formatter.formatError(this);

      case WARNING:
        return formatter.formatWarning(this);

      default:
        return null;
    }
  }

  @Override
  public String toString() {
    // TODO(user): remove custom toString.
    return type.key + ". " + description + " at " +
      (sourceName != null && sourceName.length() > 0 ?
       sourceName : "(unknown source)") + " line " +
      (lineNumber != -1 ? String.valueOf(lineNumber) : "(unknown line)");
  }

  /**
   * Get the character number.
   */
  public int getCharno() {
    return charno;
  }

  @Override
  public boolean equals(Object o) {
    // Generated by Intellij IDEA
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    JSError jsError = (JSError) o;

    if (charno != jsError.charno) {
      return false;
    }
    if (lineNumber != jsError.lineNumber) {
      return false;
    }
    if (!description.equals(jsError.description)) {
      return false;
    }
    if (level != jsError.level) {
      return false;
    }
    if (sourceName != null ? !sourceName.equals(jsError.sourceName)
        : jsError.sourceName != null) {
      return false;
    }
    if (!type.equals(jsError.type)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    // Generated by Intellij IDEA
    int result = type.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + (sourceName != null ? sourceName.hashCode() : 0);
    result = 31 * result + lineNumber;
    result = 31 * result + level.hashCode();
    result = 31 * result + charno;
    return result;
  }
}
