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
 * Represents a developer in a project model. This element is used to provide
 * metadata about an individual involved in the development of the project.
 * <p>
 * The developer element supports multiple sub-elements such as <code>id</code>,
 * <code>name</code>, <code>email</code>, <code>organization</code>, and
 * <code>roles</code>. This information is typically included in the generated
 * POM file when publishing to Maven repositories.
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

    /**
     * No-arg constructor.
     */
    public Developer() {}

    /**
     * Allows Ant to add an <code>&lt;id&gt;</code> nested element to this developer.
     *
     * @param id the developer ID element
     */
    public void addId(Id id) {
        this.id = id;
    }

    /**
     * Gets the developer ID element.
     *
     * @return the ID object, or {@code null} if not set
     */
    public Id getId() {
        return id;
    }

    /**
     * Gets the text content of the developer ID.
     *
     * @return the developer ID as a string, or {@code null} if not set
     */
    public String getIdText() {
        return getId() == null ? null : getId().getText();
    }

    /**
     * Allows Ant to add a <code>&lt;name&gt;</code> nested element to this developer.
     *
     * @param name the developer name element
     */
    public void addName(Name name) {
        this.name = name;
    }

    /**
     * Gets the developer name element.
     *
     * @return the Name object, or {@code null} if not set
     */
    public Name getName() {
        return name;
    }

    /**
     * Gets the text content of the developer name.
     *
     * @return the developer name as a string, or {@code null} if not set
     */
    public String getNameText() {
        return getName() == null ? null : getName().getText();
    }

    /**
     * Allows Ant to add an <code>&lt;email&gt;</code> nested element to this developer.
     *
     * @param email the developer email element
     */
    public void addEmail(Email email) {
        this.email = email;
    }

    /**
     * Gets the developer email element.
     *
     * @return the Email object, or {@code null} if not set
     */
    public Email getEmail() {
        return email;
    }

    /**
     * Gets the text content of the developer email.
     *
     * @return the email as a string, or {@code null} if not set
     */
    public String getEmailText() {
        return getEmail() == null ? null : getEmail().getText();
    }

    /**
     * Allows Ant to add a <code>&lt;url&gt;</code> nested element to this developer.
     *
     * @param url the developer URL element
     */
    public void addUrl(Url url) {
        this.url = url;
    }

    /**
     * Gets the developer URL element.
     *
     * @return the Url object, or {@code null} if not set
     */
    public Url getUrl() {
        return url;
    }

    /**
     * Gets the text content of the developer URL.
     *
     * @return the URL as a string, or {@code null} if not set
     */
    public String getUrlText() {
        return getUrl() == null ? null : getUrl().getText();
    }

    /**
     * Allows Ant to add an <code>&lt;organization&gt;</code> nested element to this developer.
     *
     * @param organization the developer's organization element
     */
    public void addOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * Gets the developer's organization element.
     *
     * @return the Organization object, or {@code null} if not set
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * Gets the text content of the developer's organization.
     *
     * @return the organization name as a string, or {@code null} if not set
     */
    public String getOrganizationText() {
        return getOrganization() == null ? null : getOrganization().getText();
    }

    /**
     * Allows Ant to add an <code>&lt;organizationUrl&gt;</code> nested element to this developer.
     *
     * @param organizationUrl the developer's organization URL element
     */
    public void addOrganizationUrl(OrganizationUrl organizationUrl) {
        this.organizationUrl = organizationUrl;
    }

    /**
     * Gets the developer's organization URL element.
     *
     * @return the OrganizationUrl object, or {@code null} if not set
     */
    public OrganizationUrl getOrganizationUrl() {
        return organizationUrl;
    }

    /**
     * Gets the text content of the developer's organization URL.
     *
     * @return the organization URL as a string, or {@code null} if not set
     */
    public String getOrganizationUrlText() {
        return getOrganizationUrl() == null ? null : getOrganizationUrl().getText();
    }

    /**
     * Allows Ant to add a <code>&lt;roles&gt;</code> nested element to this developer.
     *
     * @param roles the roles element containing one or more role entries
     */
    public void addRoles(Roles roles) {
        this.roles = roles;
    }

    /**
     * Gets the list of roles assigned to the developer.
     *
     * @return a list of Role objects, or {@code null} if not set
     */
    public List<Role> getRoles() {
        return roles == null ? null : roles.getRoles();
    }

    /**
     * Allows Ant to add a <code>&lt;timezone&gt;</code> nested element to this developer.
     *
     * @param timezone the developer's timezone element
     */
    public void addTimezone(Timezone timezone) {
        this.timezone = timezone;
    }

    /**
     * Gets the timezone element for the developer.
     *
     * @return the Timezone object, or {@code null} if not set
     */
    public Timezone getTimezone() {
        return timezone;
    }

    /**
     * Gets the text content of the timezone element.
     *
     * @return the timezone as a string, or {@code null} if not set
     */
    public String getTimezoneText() {
        return getTimezone() == null ? null : getTimezone().getText();
    }
}
