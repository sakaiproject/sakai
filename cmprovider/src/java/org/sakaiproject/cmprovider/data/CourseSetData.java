package org.sakaiproject.cmprovider.data;

import org.sakaiproject.cmprovider.data.validation.NotEmpty;
import org.sakaiproject.cmprovider.data.validation.NotNull;
import org.sakaiproject.coursemanagement.api.CourseSet;

/**
 * Represents a Course Set.
 * @see CourseSet
 *
 * Example json: 
 * { "eid": "cs",
 *   "title": "Computer Science",
 *   "description": "Computer Science",
 *   "category": "Department of Computer Science"
 * }
 *
 * @author Christopher Schauer
 */
public class CourseSetData implements CmEntityData {

  @NotEmpty public String eid;
  @NotNull public String title = "";
  @NotNull public String description = "";
  @NotNull public String category = "";
  public String parent;

  public String getId() { return eid; }
  
  public CourseSetData() {}

  public CourseSetData(CourseSet set) {
    eid = set.getEid();
    title = set.getTitle();
    description = set.getDescription();
    category = set.getCategory();

    if (set.getParent() != null) parent = set.getParent().getEid();
  }
}
