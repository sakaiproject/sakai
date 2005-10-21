/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceException;
import java.util.ArrayList;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.integration.helper.integrated.SectionAwareServiceHelperImpl;
import org.sakaiproject.tool.assessment.integration.helper.ifc.SectionAwareServiceHelper;

/**
 *
 * The GradingServiceAPI implements the shared interface to get grading information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */

public class GradingSectionAwareServiceImpl implements GradingSectionAwareServiceAPI
{
  private static Log log = LogFactory.getLog(GradingServiceImpl.class);
/*  private static final AgentHelper helper =
    IntegrationContextFactory.getInstance().getAgentHelper();
  private static final boolean integrated =
    IntegrationContextFactory.getInstance().isIntegrated();
*/

  private static final SectionAwareServiceHelper  helper= new SectionAwareServiceHelperImpl();


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
