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

import org.apache.tools.ant.types.DataType;

/**
 * A valid time zone ID like America/New_York or Europe/Berlin,
 * or a numerical offset in hours (and fraction) from UTC where the developer lives,
 * e.g., -5 or +1.
 * Time zone IDs are highly preferred because they are not affected by DST and time zone shifts.
 * Refer to the IANA for the official time zone database and a listing in Wikipedia.
 */
public class Timezone extends DataType {
    String timezone;

    /**
     * Default constructor.
     */
    public Timezone() {}

    /**
     * Allow ant to add text to the <code>timezone</code> element, replacing any properties in the text.
     *
     * @param timezone the <code>timezone</code> value to add (zoneId or offset)
     */
    public void addText(String timezone) {
        this.timezone = getProject().replaceProperties(timezone);
    }

    /**
     * Returns the text of the <code>timezone</code> element.
     *
     * @return the <code>Timezone</code> text
     */
    public String getText() {
        return timezone;
    }
}
