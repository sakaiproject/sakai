/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/shared/impl/grading/GradingSectionAwareServiceImpl.java $
 * $Id: GradingSectionAwareServiceImpl.java 9277 2006-05-10 23:10:33Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.shared.impl.grading;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.MediaIfc;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingSectionAwareServiceAPI;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.GradingServiceException;
import java.util.ArrayList;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
//import org.sakaiproject.tool.assessment.integration.helper.integrated.SectionAwareServiceHelperImpl;
import org.sakaiproject.tool.assessment.integration.helper.ifc.SectionAwareServiceHelper;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;

/**
 *
 * The GradingServiceAPI implements the shared interface to get grading information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */

public class GradingSectionAwareServiceImpl implements GradingSectionAwareServiceAPI
{
  //private static Log log = LogFactory.getLog(GradingServiceImpl.class);
  private static final SectionAwareServiceHelper helper =
    IntegrationContextFactory.getInstance().getSectionAwareServiceHelper();

//  private static final SectionAwareServiceHelper  helper= new SectionAwareServiceHelperImpl();


  public boolean isUserAbleToGrade(String siteId, String userUid){
    return helper.isUserAbleToGrade(siteId, userUid);
  }

  public boolean isUserAbleToGradeAll(String siteId, String userUid){
    return helper.isUserAbleToGradeAll(siteId, userUid);
  }

  public boolean isUserAbleToGradeSection(String sectionUid, String userUid){
    return helper.isUserAbleToGradeSection(sectionUid, userUid);
  }

  public boolean isUserAbleToEdit(String Uid, String userUid){
    return helper.isUserAbleToEdit(Uid, userUid);
  }

  public boolean isUserGradable(String Uid, String userUid){
    return helper.isUserGradable(Uid, userUid);
  }


  /**
  * @return
  *      an EnrollmentRecord list for each student that the current user
  *  is allowed to grade.
  */
  public List getAvailableEnrollments(String Uid, String userUid){
    return helper.getAvailableEnrollments(Uid, userUid);
  }


  /**
  * @return
  *      a CourseSection list for each group that the current user
  *  belongs to.
  */
  public List getAvailableSections(String Uid, String userUid){
    return helper.getAvailableSections(Uid, userUid);
  }

  /**
  * The section enrollment list will not be returned unless the user
  * has access to it.
  *
  * @return
  *  an EnrollmentRecord list for all the students in the given group.
  */
  public List getSectionEnrollments(String Uid, String sectionUid, String userUid){
    return helper.getSectionEnrollments(Uid, sectionUid, userUid);
  }


  /**
   * added by gopalrc - Jan 2008
   * @return
   *  an EnrollmentRecord list for each student that the current user
   *  is allowed to grade, who is in at least one of the release groups 
   *  for this published assessment. 
   */
   public List getGroupReleaseEnrollments(String Uid, String userUid, String publishedAssessmentId){
     return helper.getGroupReleaseEnrollments(Uid, userUid, publishedAssessmentId);
   }
  
  
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
  public List findMatchingEnrollments(String Uid, String searchString, String optionalSectionUid, String userUid){
    return helper.findMatchingEnrollments(Uid, searchString, optionalSectionUid, userUid);
  }

 /**
  * @param sectionId
  *
  * @param studentId
  *
  * @return
  *  whether a student belongs to a section 
  */
  public boolean isSectionMemberInRoleStudent(String sectionId, String studentId) {
    return helper.isSectionMemberInRoleStudent(sectionId, studentId);
  }


}
