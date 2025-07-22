package org.apache.maven.resolver.internal.ant.types.model;

import org.apache.tools.ant.types.DataType;

public class Organization extends DataType {
    private String orgName;

    public void addText(String name) {
        this.orgName = getProject().replaceProperties(name);
    }

    public String getText() {
        return orgName;
    }

}
