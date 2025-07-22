package org.apache.maven.resolver.internal.ant.types.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.types.DataType;

public class Roles extends DataType {
  private final List<Role> roles = new ArrayList<>();

  public void addRole(Role role) {
    roles.add(role);
  }

  public List<Role> getRoles() {
    return roles;
  }
}
