package org.sakaiproject.cmprovider.data;

import org.sakaiproject.cmprovider.data.validation.NotEmpty;
import org.sakaiproject.cmprovider.data.validation.NotNull;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;

/**
 * Represents a canonical course.
 * @see CanonicalCourse
 *
 * Example json:
 *
 * { "eid": "CS101",
 *   "title": "CS101",
 *   "description": "Introduction to computer science" } 
 *
 * @author Christopher Schauer
 */
public class CanonicalCourseData implements CmEntityData {
  @NotEmpty public String eid;
  @NotNull public String title = "";
  @NotNull public String description = "";

  public String getId() { return eid; }

  public CanonicalCourseData() {}

  public CanonicalCourseData(CanonicalCourse course) {
    eid = course.getEid();
    title = course.getTitle();
    description = course.getDescription();
  }
}
