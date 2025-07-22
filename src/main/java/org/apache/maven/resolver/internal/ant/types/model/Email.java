package org.apache.maven.resolver.internal.ant.types.model;

import org.apache.tools.ant.types.DataType;

public class Email extends DataType {
    private String email;

    public void addText(String email) {
        this.email = getProject().replaceProperties(email);
    }

    public String getText() {
        return email;
    }
}
