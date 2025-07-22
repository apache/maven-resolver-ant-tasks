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

  public void addText(String timezone) {
    this.timezone = getProject().replaceProperties(timezone);
  }

  public String getText() {
    return timezone;
  }
}
