package org.sakaiproject.cmprovider.data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
  @NotNull
  @Size(min=1)
  public String eid;
  
  @NotNull
  public String title = "";
  
  @NotNull
  public String description = "";

  public String getId() { return eid; }

  public CanonicalCourseData() {}

  public CanonicalCourseData(CanonicalCourse course) {
    eid = course.getEid();
    title = course.getTitle();
    description = course.getDescription();
  }
}
