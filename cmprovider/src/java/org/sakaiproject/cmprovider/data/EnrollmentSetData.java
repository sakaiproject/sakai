package org.sakaiproject.cmprovider.data;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.sakaiproject.coursemanagement.impl.EnrollmentSetCmImpl;

/**
 * Represents an enrollment set.
 * @see EnrollmentSet
 *
 * Example json:
 *
 * { "eid": "term1.CS101.1",
 *   "courseOffering": "term1.CS101",
 *   "title": "CS101 Term 1 Section 1",
 *   "description": "Introduction to computer science",
 *   "category": "lecture",
 *   "defaultCredits": "03:00:00",
 *   "officialInstructors": [ "admin", "crs142" ] }
 *
 * @author Christopher Schauer
 */
public class EnrollmentSetData implements CmEntityData {
  @NotNull
  @Size(min=1)
  public String eid;

  @NotNull
  public String title = "";
  
  @NotNull
  public String description = "";
  
  @NotNull
  @Size(min=1)
  public String category;
  
  @NotNull
  @Size(min=1)
  public String defaultCredits;
  
  @NotNull
  @Size(min=1)
  public String courseOffering;
  
  public List<String> officialInstructors;

  public String getId() { return eid; }
  
  public EnrollmentSetData() {}

  /**
   * Unfortunately have to reference the EnrollmentSetCmImpl class since the
   * interface doesn't have methods to access the course offering.
   */
  public EnrollmentSetData(EnrollmentSetCmImpl set) {
    eid = set.getEid();
    title = set.getTitle();
    description = set.getDescription();
    category = set.getCategory();
    defaultCredits = set.getDefaultEnrollmentCredits();
    if (set.getOfficialInstructors() != null) {
      officialInstructors = new ArrayList<String>(set.getOfficialInstructors());
    }
    courseOffering = set.getCourseOffering().getEid();
  }
}
