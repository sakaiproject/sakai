package org.sakaiproject.cmprovider.data;

import org.sakaiproject.cmprovider.data.validation.NotEmpty;
import org.sakaiproject.cmprovider.data.validation.NotNull;
import org.sakaiproject.coursemanagement.api.SectionCategory;

/**
 * Represents a section category.
 * @see SectionCategory
 *
 * Example json:
 *
 * { "code": "lecture",
 *   "description": "a lecture" }
 *
 * @author Christopher Schauer
 */
public class SectionCategoryData implements CmEntityData {
  @NotEmpty public String code;
  @NotNull public String description = "";

  public String getId() { return code; }
}
