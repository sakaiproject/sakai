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

import org.sakaiproject.cmprovider.data.DateUtils;
import org.sakaiproject.coursemanagement.api.CourseOffering;

/**
 * Represents a course offering.
 * @see CourseOffering
 *
 * Example json:
 *
 * { "eid": "term1.CS101",
 *   "title": "CS101 Term 1",
 *   "description": "Introduction to computer science",
 *   "academicSession": "term1",
 *   "canonicalCourse": "CS101",
 *   "status": "nostatus",
 *   "endDate": "2015-05-30",
 *   "startDate": "2015-01-01" }
 *
 * @author Christopher Schauer
 */
public class CourseOfferingData implements CmEntityData {
  @NotNull
  @Size(min=1)
  public String eid;
  
  @NotNull
  public String title = "";
  
  @NotNull
  public String description = "";
  
  @NotNull
  @Size(min=1)
  public String academicSession;
  
  @NotNull
  @Size(min=1)
  public String canonicalCourse;
  
  public String status;
  public String startDate;
  public String endDate;

  public String getId() { return eid; }
  
  public CourseOfferingData() {}

  public CourseOfferingData(CourseOffering course) {
    eid = course.getEid();
    title = course.getTitle();
    description = course.getDescription();
    status = course.getStatus();
    academicSession = course.getAcademicSession().getEid();
    canonicalCourse = course.getCanonicalCourseEid();
    startDate = DateUtils.dateToString(course.getStartDate());
    endDate = DateUtils.dateToString(course.getEndDate());
  }
}
