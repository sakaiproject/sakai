package org.sakaiproject.cmprovider.data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
  @NotNull
  @Size(min=1)
  public String code;
  
  @NotNull
  public String description = "";

  public String getId() { return code; }
}
