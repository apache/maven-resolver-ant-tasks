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
package org.apache.maven.resolver.internal.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

/**
 * An abstract base class for Ant tasks that support referencing another task instance via {@code refid}.
 * <p>
 * This allows build scripts to reuse task definitions by referencing a previously defined instance.
 * Subclasses that extend {@code RefTask} gain support for reference checking, attribute and child validation,
 * and safe type casting of the referenced object.
 * </p>
 *
 * <p>
 * Tasks inheriting from {@code RefTask} should use {@link #checkAttributesAllowed()} and {@link #checkChildrenAllowed()}
 * to enforce correct usage when a reference is set.
 * </p>
 *
 */
public abstract class RefTask extends Task {

    private Reference ref;

    /**
     * Default constructor for {@code RefTask}.
     */
    public RefTask() {
        // Default constructor
    }

    /**
     * Checks whether this task is configured as a reference to another task.
     *
     * @return {@code true} if a {@code refid} was set, {@code false} otherwise
     */
    public boolean isReference() {
        return ref != null;
    }

    /**
     * Sets a reference to another task instance defined elsewhere in the build file.
     * <p>
     * Once set, no other attributes or nested elements should be specified on this task.
     * </p>
     *
     * @param ref the reference to another task
     */
    public void setRefid(final Reference ref) {
        this.ref = ref;
    }

    /**
     * Verifies that attributes may still be set on this task.
     * <p>
     * If this task is configured as a reference, this method throws an exception.
     * </p>
     *
     * @throws BuildException if {@code refid} is set
     */
    protected void checkAttributesAllowed() {
        if (isReference()) {
            throw tooManyAttributes();
        }
    }

    /**
     * Verifies that nested child elements may be added to this task.
     * <p>
     * If this task is configured as a reference, this method throws an exception.
     * </p>
     *
     * @throws BuildException if {@code refid} is set
     */
    protected void checkChildrenAllowed() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
    }

    /**
     * Constructs a {@link BuildException} to indicate that additional attributes are not allowed
     * when {@code refid} is used.
     *
     * @return the build exception
     */
    protected BuildException tooManyAttributes() {
        return new BuildException("You must not specify more than one " + "attribute when using refid");
    }

    /**
     * Constructs a {@link BuildException} to indicate that nested elements are not allowed
     * when {@code refid} is used.
     *
     * @return the build exception
     */
    protected BuildException noChildrenAllowed() {
        return new BuildException("You must not specify nested elements " + "when using refid");
    }

    /**
     * Returns the Ant data type or task name associated with this task.
     * This is primarily used for error reporting and validation messages.
     *
     * @return the element name of this task
     */
    protected String getDataTypeName() {
        return ComponentHelper.getElementName(getProject(), this, true);
    }

    /**
     * Resolves the reference set on this task and verifies it is of the same type as this class.
     *
     * @return the referenced object
     * @throws BuildException if the reference is not set or is of an unexpected type
     */
    protected Object getCheckedRef() {
        return getCheckedRef(getClass(), getDataTypeName(), getProject());
    }

    /**
     * Resolves and validates the reference set on this task.
     * Ensures the referenced object is assignable to the specified class.
     *
     * @param requiredClass the expected class of the referenced object
     * @param dataTypeName the Ant data type name used for error messages
     * @param project the current Ant project
     * @return the referenced object
     *
     * @throws BuildException if the reference is missing, the project is {@code null},
     *         or the referenced object is of an incompatible type
     */
    protected Object getCheckedRef(final Class<?> requiredClass, final String dataTypeName, final Project project) {
        if (project == null) {
            throw new BuildException("No Project specified");
        }
        Object o = ref.getReferencedObject(project);
        if (!(requiredClass.isAssignableFrom(o.getClass()))) {
            log("Class " + o.getClass() + " is not a subclass of " + requiredClass, Project.MSG_VERBOSE);
            String msg = ref.getRefId() + " doesn't denote a " + dataTypeName;
            throw new BuildException(msg);
        }
        return o;
    }
}
