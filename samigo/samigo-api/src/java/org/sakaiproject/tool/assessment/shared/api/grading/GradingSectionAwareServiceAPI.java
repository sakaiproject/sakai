/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/




package org.sakaiproject.tool.assessment.shared.api.grading;

import java.util.List;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;

/**
 *
 * The GradingServiceAPI implements the shared interface to get grading information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */

public interface GradingSectionAwareServiceAPI
{

  public boolean isUserAbleToGrade(String siteId, String userUid);

  public boolean isUserAbleToGradeAll(String siteId, String userUid);

  public boolean isUserAbleToGradeSection(String siteId, String userUid);

  public boolean isUserAbleToEdit(String siteId, String userUid);

  public boolean isUserGradable(String siteId, String userUid);


  /**
  * @return
  *      an EnrollmentRecord list for each student that the current user
  *  is allowed to grade.
  */
  public List<EnrollmentRecord> getAvailableEnrollments(String siteId, String userUid);

  /**
  * @return
  *      a CourseSection list for each group that the current user
  *  belongs to.
  */
  public List getAvailableSections(String siteId, String userUid);

  /**
  * The section enrollment list will not be returned unless the user
  * has access to it.
  *
  * @return
  *  an EnrollmentRecord list for all the students in the given group.
  */
  public List getSectionEnrollments(String siteId, String sectionUid, String userUid);

  public List getSectionEnrollmentsTrusted(String sectionUid);

  /**
  * @param searchString
  *  a substring search for student name or display UID; the exact rules are
  *  up to the implementation
  *
  * @param optionalSectionUid
  *  null if the search should be made across all sections
  *
  * @return
  *  an EnrollmentRecord list for all matching available students.
  */
  public List findMatchingEnrollments(String siteId, String searchString, String optionalSectionUid, String userUid);

 /**
  * @param sectionId
  *
  * @param studentId
  *
  * @return
  *  whether a student belongs to a section
  */
  public boolean isSectionMemberInRoleStudent(String sectionId, String studentId);

  
  /**
   * @return
   *  an EnrollmentRecord list for each student that the current user
   *  is allowed to grade, who is in at least one of the release groups 
   *  for this published assessment. 
   */
   public List getGroupReleaseEnrollments(String Uid, String userUid, String publishedAssessmentId);
  
}
