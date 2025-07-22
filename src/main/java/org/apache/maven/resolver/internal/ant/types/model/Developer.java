/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.resolver.internal.ant.types.model;

import java.util.List;
import org.apache.tools.ant.types.DataType;

/**
 * Represents a developer in a project model.
 *
 *
 */
public class Developer extends DataType {

    private Id id;
    private Name name;
    private Email email;
    private Organization organization;
    private OrganizationUrl organizationUrl;
    private Roles roles;
    private Timezone timezone;

    public Developer() {}

    public void addId(Id id) {
        this.id = id;
    }

    public String getId() {
        return id.getText();
    }

    public void addName(Name name) {
        this.name = name;
    }

    public String getName() {
        return name == null ? null : name.getText();
    }

    public void addEmail(Email email) {
        this.email = email;
    }

    public String getEmail() {
        return email == null ? null : email.getText();
    }

    public void addOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getOrganization() {
        return organization == null ? null : organization.getText();
    }

    public void addOrganizationUrl(OrganizationUrl organizationUrl) {
        this.organizationUrl = organizationUrl;
    }

    public String getOrganizationUrl() {
        return organizationUrl == null ? null : organizationUrl.getText();
    }

    public void addRoles(Roles roles) {
        this.roles = roles;
    }

    public List<Role> getRoles() {
        return roles == null ? null : roles.getRoles();
    }

    public void addTimezone(Timezone timezone) {
        this.timezone = timezone;
    }

    public String getTimezone() {
        return timezone == null ? null : timezone.getText();
    }
}
