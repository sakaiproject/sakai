/**
 * Copyright (c) 2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
