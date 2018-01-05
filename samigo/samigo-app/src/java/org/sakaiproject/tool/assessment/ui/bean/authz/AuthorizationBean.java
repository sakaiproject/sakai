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

package org.sakaiproject.tool.assessment.ui.bean.authz;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener;
import org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.cover.ToolManager;

@Slf4j
public class AuthorizationBean implements Serializable {
  /**
	 * 
	 */
	private static final long serialVersionUID = -2782949557257727817L;

  private Map<String, Boolean> map = new ConcurrentHashMap<String, Boolean>();
  private boolean adminPrivilege = false;
  private boolean adminNewAssessmentPrivilege = false;
  private boolean adminCoreAssessmentPrivilege = false;
  private boolean adminPublishedAssessmentPrivilege = false;
  private boolean adminAssessmentPrivilege = false;
  private boolean adminTemplatePrivilege = false;
  private boolean adminQuestionPoolPrivilege = false;
  private Boolean superUser = null;

  public AuthorizationBean(){
  }

  public Map<String, Boolean> getAuthzMap(){
    return map;
  }
  public boolean getAdminPrivilege(){
	  if(!this.initializedPerm){
		  initializePermission();
		  this.initializedPerm=true;
	  }
    return getPrivilege("admin.privilege");
  }

  public boolean getAdminNewAssessment(){
    return getPrivilege("admin.new.assessment");
  }

  public boolean getAdminCoreAssessment(){
    return getPrivilege("admin.core.assessment");
  }

  public boolean getAdminPublishedAssessment(){
    return getPrivilege("admin.published.assessment");
  }

  public boolean getAdminAssessment(){
    return getPrivilege("admin.assessment");
  }

  public boolean getAdminTemplate(){
    return getPrivilege("admin.template");
  }

  public boolean getAdminQuestionPool(){
    return getPrivilege("admin.questionpool");
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
    addAdminPrivilege(adminNewAssessmentPrivilege, "admin.new.assessment", siteId);

    adminCoreAssessmentPrivilege = p2 || p3 || p4 || p5 || p6 || p7;
    addAdminPrivilege(adminCoreAssessmentPrivilege, "admin.core.assessment", siteId);

    adminPublishedAssessmentPrivilege = p8 || p9;
    addAdminPrivilege(adminPublishedAssessmentPrivilege, "admin.published.assessment", siteId);

    adminAssessmentPrivilege = p1 || p2 || p3 || p4 || p5 || p6 || p7 || p8 || p9;
    addAdminPrivilege(adminAssessmentPrivilege, "admin.assessment", siteId);

    adminQuestionPoolPrivilege = p10 || p11 || p12 || p13;
    addAdminPrivilege(adminQuestionPoolPrivilege, "admin.questionpool", siteId);

    adminTemplatePrivilege = p14 ||p15 || p16;
    addAdminPrivilege(adminTemplatePrivilege, "admin.template", siteId);

    adminPrivilege = adminAssessmentPrivilege || adminQuestionPoolPrivilege || adminTemplatePrivilege;
    addAdminPrivilege(adminPrivilege, "admin.privilege", siteId);
  }

  public boolean canTakeAssessment(String siteId)
  {
    return addPrivilege("assessment.takeAssessment", siteId);
  }

  public boolean canSubmitAssessmentForGrade(String siteId)
  {
    return addPrivilege("assessment.submitAssessmentForGrade", siteId);
  }

  public boolean canCreateAssessment(String siteId)
  {
    return addPrivilege("assessment.createAssessment", siteId);
  }

  public boolean canEditAnyAssessment(String siteId)
  {
    return addPrivilege("assessment.editAssessment.any", siteId);
  }

  public boolean canEditOwnAssessment(String siteId)
  {
    return addPrivilege("assessment.editAssessment.own", siteId);
  }

  public boolean canDeleteAnyAssessment(String siteId)
  {
    return addPrivilege("assessment.deleteAssessment.any", siteId);
  }

  public boolean canDeleteOwnAssessment(String siteId)
  {
    return addPrivilege("assessment.deleteAssessment.own", siteId);
  }

  public boolean canPublishAnyAssessment(String siteId)
  {
    return addPrivilege("assessment.publishAssessment.any", siteId);
  }

  public boolean canPublishOwnAssessment(String siteId)
  {
    return addPrivilege("assessment.publishAssessment.own", siteId);
  }

  public boolean canGradeAnyAssessment(String siteId)
  {
    return addPrivilege("assessment.gradeAssessment.any", siteId);
  }

  public boolean canGradeOwnAssessment(String siteId)
  {
    return addPrivilege("assessment.gradeAssessment.own", siteId);
  }

  public boolean canCreateQuestionPool(String siteId)
  {
    return addPrivilege("assessment.questionpool.create", siteId);
  }

  public boolean canEditOwnQuestionPool(String siteId)
  {
    return addPrivilege("assessment.questionpool.edit.own", siteId);
  }

  public boolean canDeleteOwnQuestionPool(String siteId)
  {
    return addPrivilege("assessment.questionpool.delete.own", siteId);
  }

  public boolean canCopyOwnQuestionPool(String siteId)
  {
    return addPrivilege("assessment.questionpool.copy.own", siteId);
  }

  public boolean canCreateTemplate(String siteId)
  {
    return addPrivilege("assessment.template.create", siteId);
  }

  public boolean canEditOwnTemplate(String siteId)
  {
    return addPrivilege("assessment.template.edit.own", siteId);
  }

  public boolean canDeleteOwnTemplate(String siteId)
  {
    return addPrivilege("assessment.template.delete.own", siteId);
  }


  public boolean addPrivilege(String functionName, String siteId){
     boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
     map.put(functionName+"_"+siteId, Boolean.valueOf(privilege));
     return privilege;
  }
  
  private boolean initializedPerm =  false;
  /**
   * This will initialize the user permissions and related assessment's list 
   */
  private void initializePermission(){
	  //this is moved from original roleCheckStaticInclude.jsp page, which is not working for JSF 1.2 up.
	  addAllPrivilege(ToolManager.getCurrentPlacement().getContext());
	  if (!adminPrivilege)
	  {
	          SelectActionListener listener = new SelectActionListener();
	          listener.processAction(null);
	  }    
	  else
	  {
	     AuthorActionListener authorlistener = new AuthorActionListener();
	     authorlistener.processAction(null);
	  }
  }

  public void addAdminPrivilege(boolean privilege, String functionName, String siteId){
     map.put(functionName+"_"+siteId, Boolean.valueOf(privilege));
  }

  public boolean getTakeAssessment(){
    return getPrivilege("assessment.takeAssessment");
  } 

  public boolean getSubmitAssessmentForGrade(){
    return getPrivilege("assessment.submitAssessmentForGrade");
  } 
  public boolean getCreateAssessment(){
    return getPrivilege("assessment.createAssessment");
  } 

  public boolean getEditAnyAssessment() {
    return getPrivilege("assessment.editAssessment.any");
  } 

  public boolean getEditOwnAssessment() {
    return getPrivilege("assessment.editAssessment.own");
  } 

  public boolean getDeleteAnyAssessment() {
    return getPrivilege("assessment.deleteAssessment.any");
  } 

  public boolean getDeleteOwnAssessment() {
    return getPrivilege("assessment.deleteAssessment.own");
  } 

  public boolean getPublishAnyAssessment() {
    return getPrivilege("assessment.publishAssessment.any");
  } 

  public boolean getPublishOwnAssessment() {
    return getPrivilege("assessment.publishAssessment.own");
  } 

  public boolean getGradeAnyAssessment() {
    boolean priv = getPrivilege("assessment.gradeAssessment.any");
    return priv;
  } 

  public boolean getGradeOwnAssessment() {
    boolean priv = getPrivilege("assessment.gradeAssessment.own");
    return priv;
  } 

  public boolean getCreateQuestionPool() {
    return getPrivilege("assessment.questionpool.create");
  } 

  public boolean getEditOwnQuestionPool() {
    return getPrivilege("assessment.questionpool.edit.own");
  } 

  public boolean getDeleteOwnQuestionPool() {
    return getPrivilege("assessment.questionpool.delete.own");
  } 

  public boolean getCopyOwnQuestionPool() {
    return getPrivilege("assessment.questionpool.copy.own");
  } 

  public boolean getCreateTemplate() {
    return getPrivilege("assessment.template.create");
  } 

  public boolean getEditOwnTemplate() {
    return getPrivilege("assessment.template.edit.own");
  } 

  public boolean getDeleteOwnTemplate() {
    return getPrivilege("assessment.template.delete.own");
  } 

  public boolean getPrivilege(String functionName){
    return getPrivilege(functionName,null);
  }
 
  public boolean getPrivilege(final String functionName, String siteId){
	if (siteId == null) {
		siteId  = AgentFacade.getCurrentSiteId();
	}
	
    boolean privilege = false;
    Object o = map.get(functionName+"_"+siteId);
    if (o != null) privilege = ((Boolean)o).booleanValue();
    return privilege;
  }

  // added the follwoing for ShowMediaServlet
  public boolean getGradeAnyAssessment(String siteId) {
    return getPrivilege("assessment.gradeAssessment.any", siteId);
  } 

  public boolean getGradeOwnAssessment(String siteId) {
    return getPrivilege("assessment.gradeAssessment.own", siteId);
  }

  public boolean isUserAllowedToPublishAssessment(final String assessmentId, final String assessmentOwnerId, final boolean published) {
    if (!isAssessmentInSite(assessmentId, published)) {
      return false;
    }

    if (getPublishAnyAssessment()) {
      return true;
    }
    else if (getPublishOwnAssessment()) {
      final String loggedInUser = AgentFacade.getAgentString();
      return StringUtils.equals(loggedInUser, assessmentOwnerId);
    }
    return false;
  }

  public boolean isUserAllowedToGradeAssessment(final String assessmentId, final String assessmentOwnerId, final boolean published) {
	 return isUserAllowedToGradeAssessment(assessmentId,assessmentOwnerId,published,null); 
  }

  public boolean isUserAllowedToGradeAssessment(final String assessmentId, final String assessmentOwnerId, final boolean published, String currentSiteId) {
    if (!isAssessmentInSite(assessmentId,currentSiteId,published)) {
      return false;
    }

    // Second check on the realm permissions
    if (getGradeAnyAssessment(currentSiteId)) {
      return true;
    }
    else if (getGradeOwnAssessment(currentSiteId)) {
      final String loggedInUser = AgentFacade.getAgentString();
      return StringUtils.equals(loggedInUser, assessmentOwnerId);
    }
    return false;
  }

  public boolean isUserAllowedToEditAssessment(final String assessmentId, final String assessmentOwnerId, final boolean published) {
    if (!isAssessmentInSite(assessmentId, published)) {
      return false;
    }

    // Second check on the realm permissions
    if (getEditAnyAssessment()) {
      return true;
    }
    else if (getEditOwnAssessment()) {
      final String loggedInUser = AgentFacade.getAgentString();
      return StringUtils.equals(loggedInUser, assessmentOwnerId);
    }
    return false;
  }

  public boolean isUserAllowedToDeleteAssessment(final String assessmentId, final String assessmentOwnerId, final boolean published) {
    if (!isAssessmentInSite(assessmentId, published)) {
      return false;
    }

    // Second check on the realm permissions
    if (getDeleteAnyAssessment()) {
      return true;
    }
    else if (getDeleteOwnAssessment()) {
      final String loggedInUser = AgentFacade.getAgentString();
      return StringUtils.equals(loggedInUser, assessmentOwnerId);
    }
    return false;
  }

  public boolean isUserAllowedToTakeAssessment(final String assessmentId) {
    if (!isAssessmentInSite(assessmentId, true)) { 
      return false;
    }

    // Second check on the realm permissions
    return getTakeAssessment();
  }

  public boolean isUserAllowedToCreateAssessment() {
    return getCreateAssessment();
  }

  // Check whether the assessment belongs to the given site
  public static boolean isAssessmentInSite(final String assessmentId, String siteId, final boolean published) {
	//Try to get the site Id
	if (siteId == null) {
		siteId = AgentFacade.getCurrentSiteId();
	}
    // get list of site that this published assessment has been released to
    List<AuthorizationData> l = PersistenceService.getInstance().getAuthzQueriesFacade().getAuthorizationByFunctionAndQualifier(published ? "OWN_PUBLISHED_ASSESSMENT" : "EDIT_ASSESSMENT", assessmentId);

    for (int i=0; i < l.size(); i++) {
      String assessmentSiteId = (l.get(i)).getAgentIdString();
      if (siteId.equals(assessmentSiteId)) {
        return true;
      }
    }

    return false;
  }

  public static boolean isAssessmentInSite(final String assessmentId, final boolean published) {
    return isAssessmentInSite(assessmentId, AgentFacade.getCurrentSiteId(), published);
  }
  
  public boolean isSuperUser() {
	  if (superUser == null) {
		  superUser = SecurityService.isSuperUser();
	  }
	  return superUser;
  }

}
