package org.sakaiproject.cmprovider.data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.sakaiproject.coursemanagement.api.AcademicSession;

/**
 * Represents an academic session.
 * @see AcademicSession
 *
 * Example json:
 *
 * { "eid": "term1",
 *   "title": "Test Term",
 *   "description": "Test term created with cmprovider",
 *   "endDate": "2015-05-30",
 *   "startDate": "2015-01-01" }
 *
 * @author Christopher Schauer
 */
public class AcademicSessionData implements CmEntityData {
  @NotNull
  @Size(min=1)
  public String eid;

  @NotNull
  public String title = "";
  
  @NotNull
  public String description = "";
  
  public String startDate;
  public String endDate;

  public String getId() { return eid; }
  
  public AcademicSessionData() {}

  public AcademicSessionData(AcademicSession session) {
    eid = session.getEid();
    title = session.getTitle();
    description = session.getDescription();
    startDate = DateUtils.dateToString(session.getStartDate());
    endDate = DateUtils.dateToString(session.getEndDate());
  }
}
