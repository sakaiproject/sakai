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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.bean.authz;

import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
//import org.sakaiproject.spring.SpringBeanLocator;
import java.io.Serializable;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthorizationBean implements Serializable {
  /**
	 * 
	 */
	private static final long serialVersionUID = -2782949557257727817L;

private static Log log = LogFactory.getLog(AuthorizationBean.class);

  private HashMap map = new HashMap();
  private boolean adminPrivilege = false;
  private boolean adminNewAssessmentPrivilege = false;
  private boolean adminCoreAssessmentPrivilege = false;
  private boolean adminPublishedAssessmentPrivilege = false;
  private boolean adminAssessmentPrivilege = false;
  private boolean adminTemplatePrivilege = false;
  private boolean adminQuestionPoolPrivilege = false;

  public AuthorizationBean(){}

  public HashMap getAuthzMap(){
    return map;
  }
  public boolean getAdminPrivilege(){
    return getPrivilege("admin_privilege");
  }

  public boolean getAdminNewAssessment(){
    return getPrivilege("admin_new_assessment");
  }

  public boolean getAdminCoreAssessment(){
    return getPrivilege("admin_core_assessment");
  }

  public boolean getAdminPublishedAssessment(){
    return getPrivilege("admin_published_assessment");
  }

  public boolean getAdminAssessment(){
    return getPrivilege("admin_assessment");
  }

  public boolean getAdminTemplate(){
    return getPrivilege("admin_template");
  }

  public boolean getAdminQuestionPool(){
    return getPrivilege("admin_questionpool");
  }


  // This method is called (via jsf/security/roleCheckStaticInclude.jsp)
  // when user first enter samigo in a site.
  // This is the time when we determine his permission in the site.
  // This info is stored in the AuthorizationService HashMap for the entire session.
  public void addAllPrivilege(String siteId){
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
    boolean p14 = canCreateTemplate(siteId);
    boolean p15 = canEditOwnTemplate(siteId);
    boolean p16 = canDeleteOwnTemplate(siteId);

    // non admin functions
    canTakeAssessment(siteId);
    canSubmitAssessmentForGrade(siteId);

    // set adminPrivilege
    adminNewAssessmentPrivilege = p1;
    addAdminPrivilege(adminNewAssessmentPrivilege, "admin_new_assessment", siteId);

    adminCoreAssessmentPrivilege = p2 || p3 || p4 || p5 || p6 || p7;
    addAdminPrivilege(adminCoreAssessmentPrivilege, "admin_core_assessment", siteId);

    adminPublishedAssessmentPrivilege = p8 || p9;
    addAdminPrivilege(adminPublishedAssessmentPrivilege, "admin_published_assessment", siteId);

    adminAssessmentPrivilege = p1 || p2 || p3 || p4 || p5 || p6 || p7 || p8 || p9;
    addAdminPrivilege(adminAssessmentPrivilege, "admin_assessment", siteId);

    adminQuestionPoolPrivilege = p10 || p11 || p12 || p13;
    addAdminPrivilege(adminQuestionPoolPrivilege, "admin_questionpool", siteId);

    adminTemplatePrivilege = p14 ||p15 || p16;
    addAdminPrivilege(adminTemplatePrivilege, "admin_template", siteId);

    adminPrivilege = adminAssessmentPrivilege || adminQuestionPoolPrivilege || adminTemplatePrivilege;
    addAdminPrivilege(adminPrivilege, "admin_privilege", siteId);
  }

  public boolean canTakeAssessment(String siteId)
  {
    return addPrivilege("take_assessment", siteId);
  }

  public boolean canSubmitAssessmentForGrade(String siteId)
  {
    return addPrivilege("submit_assessment_for_grade", siteId);
  }

  public boolean canCreateAssessment(String siteId)
  {
    return addPrivilege("create_assessment", siteId);
  }

  public boolean canEditAnyAssessment(String siteId)
  {
    return addPrivilege("edit_any_assessment", siteId);
  }

  public boolean canEditOwnAssessment(String siteId)
  {
    return addPrivilege("edit_own_assessment", siteId);
  }

  public boolean canDeleteAnyAssessment(String siteId)
  {
    return addPrivilege("delete_any_assessment", siteId);
  }

  public boolean canDeleteOwnAssessment(String siteId)
  {
    return addPrivilege("delete_own_assessment", siteId);
  }

  public boolean canPublishAnyAssessment(String siteId)
  {
    return addPrivilege("publish_any_assessment", siteId);
  }

  public boolean canPublishOwnAssessment(String siteId)
  {
    return addPrivilege("publish_own_assessment", siteId);
  }

  public boolean canGradeAnyAssessment(String siteId)
  {
    return addPrivilege("grade_any_assessment", siteId);
  }

  public boolean canGradeOwnAssessment(String siteId)
  {
    return addPrivilege("grade_own_assessment", siteId);
  }

  public boolean canCreateQuestionPool(String siteId)
  {
    return addPrivilege("create_questionpool", siteId);
  }

  public boolean canEditOwnQuestionPool(String siteId)
  {
    return addPrivilege("edit_own_questionpool", siteId);
  }

  public boolean canDeleteOwnQuestionPool(String siteId)
  {
    return addPrivilege("delete_own_questionpool", siteId);
  }

  public boolean canCopyOwnQuestionPool(String siteId)
  {
    return addPrivilege("copy_own_questionpool", siteId);
  }

  public boolean canCreateTemplate(String siteId)
  {
    return addPrivilege("create_template", siteId);
  }

  public boolean canEditOwnTemplate(String siteId)
  {
    return addPrivilege("edit_own_template", siteId);
  }

  public boolean canDeleteOwnTemplate(String siteId)
  {
    return addPrivilege("delete_own_template", siteId);
  }


  public boolean addPrivilege(String functionKey, String siteId){
     String functionName=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthzPermissions", functionKey);
     boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
     map.put(functionName+"_"+siteId, Boolean.valueOf(privilege));
     //log.debug(functionName+"_"+siteId+"="+privilege);
     return privilege;
  }

  public void addAdminPrivilege(boolean privilege, String functionKey, String siteId){
     String functionName=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthzPermissions", functionKey);
     map.put(functionName+"_"+siteId, Boolean.valueOf(privilege));
     //log.debug(functionName+"_"+siteId+"="+privilege);
  }

  public boolean getTakeAssessment(){
    return getPrivilege("take_assessment");
  } 

  public boolean getSubmitAssessmentForGrade(){
    return getPrivilege("submit_assessment_for_grade");
  } 
  public boolean getCreateAssessment(){
    return getPrivilege("create_assessment");
  } 

  public boolean getEditAnyAssessment() {
    return getPrivilege("edit_any_assessment");
  } 

  public boolean getEditOwnAssessment() {
    return getPrivilege("edit_own_assessment");
  } 

  public boolean getDeleteAnyAssessment() {
    return getPrivilege("delete_any_assessment");
  } 

  public boolean getDeleteOwnAssessment() {
    return getPrivilege("delete_own_assessment");
  } 

  public boolean getPublishAnyAssessment() {
    return getPrivilege("publish_any_assessment");
  } 

  public boolean getPublishOwnAssessment() {
    return getPrivilege("publish_own_assessment");
  } 

  public boolean getGradeAnyAssessment() {
	boolean priv = getPrivilege("grade_any_assessment");   
    return priv;
  } 

  public boolean getGradeOwnAssessment() {
	boolean priv = getPrivilege("grade_own_assessment");
    return priv;
  } 

  public boolean getCreateQuestionPool() {
    return getPrivilege("create_questionpool");
  } 

  public boolean getEditOwnQuestionPool() {
    return getPrivilege("edit_own_questionpool");
  } 

  public boolean getDeleteOwnQuestionPool() {
    return getPrivilege("delete_own_questionpool");
  } 

  public boolean getCopyOwnQuestionPool() {
    return getPrivilege("copy_own_questionpool");
  } 

  public boolean getCreateTemplate() {
    return getPrivilege("create_template");
  } 

  public boolean getEditOwnTemplate() {
    return getPrivilege("edit_own_template");
  } 

  public boolean getDeleteOwnTemplate() {
    return getPrivilege("delete_own_template");
  } 

  public boolean getPrivilege(String functionKey){
    String siteId  = AgentFacade.getCurrentSiteId();
    String functionName=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthzPermissions", functionKey);
    boolean privilege = false;
    Object o = map.get(functionName+"_"+siteId);
    if (o!=null)
      privilege = ((Boolean)o).booleanValue();
    //log.debug("**** authzBean:"+functionName+"_"+siteId+"="+privilege);
    return privilege;
  }


  // added the follwoing for ShowMediaServlet
  public boolean getGradeAnyAssessment(HttpServletRequest req, 
                                       String siteId) {
    return getPrivilege(req, "grade_any_assessment", siteId);
  } 

  public boolean getGradeOwnAssessment(HttpServletRequest req, 
                                       String siteId) {
    return getPrivilege(req, "grade_own_assessment", siteId);
  } 

  public boolean getPrivilege(HttpServletRequest req,
                              String functionKey, String siteId){
    String functionName=(String)ContextUtil.getLocalizedString(req,
                             "org.sakaiproject.tool.assessment.bundle.AuthzPermissions", 
                              functionKey);
    boolean privilege = false;
    Object o = map.get(functionName+"_"+siteId);
    if (o!=null)
      privilege = ((Boolean)o).booleanValue();
    return privilege;
  }

}
