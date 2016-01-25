package org.sakaiproject.cmprovider.data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.sakaiproject.coursemanagement.api.Enrollment;

/**
 * Represents an enrollment.
 * @see Enrollment
 *
 * Example json:
 *
 * { "userId": "crs142",
 *   "enrollmentSet": "term1.CS101.1",
 *   "status": "true",
 *   "credits": "03:00:00",
 *   "gradingScheme": "noscheme" }
 *
 * @author Christopher Schauer
 */
public class EnrollmentData implements CmEntityData {
  @NotNull
  @Size(min=1)
  public String userId;
  
  @NotNull
  @Size(min=1)
  public String enrollmentSet;
  
  @NotNull
  @Size(min=1)
  public String status;
  
  @NotNull
  @Size(min=1)
  public String credits;
  
  @NotNull
  @Size(min=1)
  public String gradingScheme;
  
  public String dropDate;
  public boolean dropped = false;

  public String getId() { return userId + "," + enrollmentSet; }

  public EnrollmentData() {}

  public EnrollmentData(Enrollment enrollment, String enrollmentSetEid) {
    userId = enrollment.getUserId();
    enrollmentSet = enrollmentSetEid;
    status = enrollment.getEnrollmentStatus();
    credits = enrollment.getCredits();
    gradingScheme = enrollment.getGradingScheme();
    dropDate = DateUtils.dateToString(enrollment.getDropDate());
    dropped = enrollment.isDropped();
  }
}
