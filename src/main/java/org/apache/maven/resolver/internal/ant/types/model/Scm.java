package org.apache.maven.resolver.internal.ant.types.model;

import org.apache.tools.ant.types.DataType;

public class Scm extends DataType {
  private Connection connection;
  private DeveloperConnection developerConnection;
  private Url url;

  public void addConnection(Connection connection) {
    this.connection = connection;
  }

  public Connection getConnection() {
    return connection;
  }

  public void addDeveloperConnection(DeveloperConnection developerConnection) {
    this.developerConnection = developerConnection;
  }

  public DeveloperConnection getDeveloperConnection() {
    return developerConnection;
  }

  public void addUrl(Url url) {
    this.url = url;
  }

  public Url getUrl() {
    return url;
  }
}
