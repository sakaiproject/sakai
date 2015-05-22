package org.sakaiproject.cmprovider.api.data;

import org.sakaiproject.cmprovider.api.data.annotations.NotEmpty;
import org.sakaiproject.cmprovider.api.data.util.DateUtils;
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
  @NotEmpty public String userId;
  @NotEmpty public String enrollmentSet;
  @NotEmpty public String status;
  @NotEmpty public String credits;
  @NotEmpty public String gradingScheme;
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
