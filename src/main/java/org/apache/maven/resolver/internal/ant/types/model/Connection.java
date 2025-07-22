package org.apache.maven.resolver.internal.ant.types.model;

import org.apache.tools.ant.types.DataType;

/**
 * The Connection element is used in the Scm section.
 * An example is:
 * <pre>{@code
 * <connection>scm:git:https://github.com/Alipsa/matrix.git</connection>
 * }</pre>
 */
public class Connection extends DataType {
  String text;

  /**
   * Adds text to the Connection element, replacing any properties in the text.
   *
   * @param text the Connection text to add
   */
  public void addText(String text) {
    this.text = getProject().replaceProperties(text).trim();
  }

  /**
   * Returns the text of the Connection element.
   *
   * @return the Connection text
   */
  public String getText() {
    return text;
  }
}
