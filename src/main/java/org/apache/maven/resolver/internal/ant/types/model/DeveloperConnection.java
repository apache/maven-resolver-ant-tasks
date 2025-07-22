package org.apache.maven.resolver.internal.ant.types.model;

import org.apache.tools.ant.types.DataType;

public class DeveloperConnection extends DataType {
  String text;

  public void addText(String text) {
    this.text = getProject().replaceProperties(text).trim();
  }

  public String getText() {
    return text;
  }
}
