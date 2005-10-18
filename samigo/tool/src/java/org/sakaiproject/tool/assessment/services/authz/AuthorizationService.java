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

package org.sakaiproject.tool.assessment.services.authz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import java.util.HashMap;

/**
 * The AuthorizationService
 */
public class AuthorizationService
{
  private static ContextUtil cu;
  private static Log log = LogFactory.getLog(AuthorizationService.class);
  private HashMap map = new HashMap();

  // This method is called (via jsf/security/roleCheckStaticInclude.jsp) 
  // when user first enter samigo in a site.
  // This is the time when we determine his permission in the site.
  // This info is stored in the AuthorizationService HashMap for the entire session.
  public boolean allowAdminAssessment(String siteId)
  {
    // admin functions
    boolean p1 = canCreateAssessment(siteId);
    boolean p2 = canEditAnyAssessment(siteId);
    boolean p3 = canEditOwnAssessment(siteId);
    boolean p4 = canDeleteAnyAssessment(siteId);
    boolean p5 = canDeleteOwnAssessment(siteId);
    boolean p6 = canPublishAnyAssessment(siteId);
    boolean p7 = canPublishOwnAssessment(siteId);
    boolean p8 = canGradeAnyAssessment(siteId);
    boolean p9 = canGradeOwnAssessment(siteId);
    boolean p10 = canCreateQuestionPool(siteId);
    boolean p11 = canEditOwnQuestionPool(siteId);
    boolean p12 = canDeleteOwnQuestionPool(siteId);
    boolean p13 = canCopyOwnQuestionPool(siteId);

    // non admin functions
    boolean p14 = canTakeAssessment(siteId);
    boolean p15 = canSubmitAssessmentForGrade(siteId);

    boolean hasAdminPrivilege = p1 || p2 || p3 || p4 || p5 || p6 || p7
	                        || p8 || p9 || p10 || p11 || p12 || p13;

    return hasAdminPrivilege;
  }

  public HashMap getAuthzMap(){
    return map;
  }


  public boolean canTakeAssessment(String siteId)
  {
    return getPrivilege("take_assessment", siteId);
  }

  public boolean canSubmitAssessmentForGrade(String siteId)
  {
    return getPrivilege("submit_assessment_for_grade", siteId);
  }

  public boolean canCreateAssessment(String siteId)
  {
    return getPrivilege("create_assessment", siteId);
  }

  public boolean canEditAnyAssessment(String siteId)
  {
    return getPrivilege("edit_any_assessment", siteId);
  }

  public boolean canEditOwnAssessment(String siteId)
  {
    return getPrivilege("edit_own_assessment", siteId);
  }

  public boolean canDeleteAnyAssessment(String siteId)
  {
    return getPrivilege("delete_any_assessment", siteId);
  }

  public boolean canDeleteOwnAssessment(String siteId)
  {
    return getPrivilege("delete_own_assessment", siteId);
  }

  public boolean canPublishAnyAssessment(String siteId)
  {
    return getPrivilege("publish_any_assessment", siteId);
  }

  public boolean canPublishOwnAssessment(String siteId)
  {
    return getPrivilege("publish_own_assessment", siteId);
  }

  public boolean canGradeAnyAssessment(String siteId)
  {
    return getPrivilege("grade_any_assessment", siteId);
  }

  public boolean canGradeOwnAssessment(String siteId)
  {
    return getPrivilege("grade_own_assessment", siteId);
  }

  public boolean canCreateQuestionPool(String siteId)
  {
    return getPrivilege("create_questionpool", siteId);
  }
  
  public boolean canEditOwnQuestionPool(String siteId)
  {
    return getPrivilege("edit_own_questionpool", siteId);
  }

  public boolean canDeleteOwnQuestionPool(String siteId)
  {
    return getPrivilege("delete_own_questionpool", siteId);
  }

  public boolean canCopyOwnQuestionPool(String siteId)
  {
    return getPrivilege("copy_own_questionpool", siteId);
  }

  public boolean getPrivilege(String functionKey, String siteId){
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", functionKey);
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }
}
