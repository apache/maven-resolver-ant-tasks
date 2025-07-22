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
    private Url url;
    private Organization organization;
    private OrganizationUrl organizationUrl;
    private Roles roles;
    private Timezone timezone;

    public Developer() {}

    public void addId(Id id) {
        this.id = id;
    }

    public Id getId() {
        return id;
    }

    public String getIdText() {
        return getId() == null ? null : getId().getText();
    }

    public void addName(Name name) {
        this.name = name;
    }

    public Name getName() {
        return name;
    }

    public String getNameText() {
        return getName() == null ? null : getName().getText();
    }

    public void addEmail(Email email) {
        this.email = email;
    }

    public Email getEmail() {
        return email;
    }

    public String getEmailText() {
        return getEmail() == null ? null : getEmail().getText();
    }

    public void addUrl(Url url) {
        this.url = url;
    }

    public Url getUrl() {
        return url;
    }

    public String getUrlText() {
        return getUrl() == null ? null : getUrl().getText();
    }

    public void addOrganization(Organization organization) {
        this.organization = organization;
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getOrganizationText() {
        return getOrganization() == null ? null : getOrganization().getText();
    }

    public void addOrganizationUrl(OrganizationUrl organizationUrl) {
        this.organizationUrl = organizationUrl;
    }

    public OrganizationUrl getOrganizationUrl() {
        return organizationUrl;
    }

    public String getOrganizationUrlText() {
        return getOrganizationUrl() == null ? null : getOrganizationUrl().getText();
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

    public Timezone getTimezone() {
        return timezone;
    }

    public String getTimezoneText() {
        return getTimezone() == null ? null : getTimezone().getText();
    }
}
