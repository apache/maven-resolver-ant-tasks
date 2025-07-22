package org.apache.maven.resolver.internal.ant.types.model;

import org.apache.tools.ant.types.DataType;

public class Role extends DataType {
  private String role;

  public void addText(String role) {
    this.role = getProject().replaceProperties(role);
  }

  public String getText() {
    return role;
  }
}
