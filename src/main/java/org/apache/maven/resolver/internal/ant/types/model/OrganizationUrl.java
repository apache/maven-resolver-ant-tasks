package org.apache.maven.resolver.internal.ant.types.model;

import org.apache.tools.ant.types.DataType;

public class OrganizationUrl extends DataType {
  String orgUrl;

  public void addText(String orgUrl) {
    this.orgUrl = getProject().replaceProperties(orgUrl);
  }

  public String getText() {
    return orgUrl;
  }
}
