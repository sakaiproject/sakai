package org.sakaiproject.cmprovider.api.data;

import org.sakaiproject.cmprovider.api.data.annotations.NotEmpty;
import org.sakaiproject.cmprovider.api.data.annotations.NotNull;
import org.sakaiproject.coursemanagement.api.Section;

/**
 * Represents a section.
 * @see Section
 *
 * Example json:
 *
 * { "eid": "term1.CS101.1",
 *   "courseOffering": "term1.CS101",
 *   "enrollmentSet": "term1.CS101.1",
 *   "title": "CS 101 Term 1 Section 1",
 *   "description": "Introduction to computer science",
 *   "category": "lecture",
 *   "maxSize": 100 }
 *
 * @author Christopher Schauer
 */
public class SectionData implements CmEntityData {
  @NotEmpty public String eid;
  @NotEmpty public String title;
  @NotNull public String description = "";
  @NotNull public String category = "";
  @NotEmpty public String courseOffering;
  @NotEmpty public String enrollmentSet;
  public Integer maxSize;
  public String parent;

  public String getId() { return eid; }
  
  public SectionData() {}

  public SectionData(Section section) {
    eid = section.getEid();
    title = section.getTitle();
    description = section.getDescription();
    category = section.getCategory();
    courseOffering = section.getCourseOfferingEid();
    enrollmentSet = section.getEnrollmentSet().getEid();
    maxSize = section.getMaxSize();

    if (section.getParent() != null) parent = section.getParent().getEid();
  }
}
