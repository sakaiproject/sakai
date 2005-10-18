/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.assessment.ui.bean.authz;

import org.sakaiproject.tool.assessment.services.authz.AuthorizationService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import java.io.Serializable;

public class AuthorizationBean implements Serializable {
  private static ContextUtil cu;
  private AuthorizationService authzService = (AuthorizationService) SpringBeanLocator.getInstance().getBean("AuthorizationService");

  public AuthorizationBean(){}

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
    return getPrivilege("grade_any_assessment");
  } 

  public boolean getGradeOwnAssessment() {
    return getPrivilege("grade_own_assessment");
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

  public boolean getPrivilege(String functionKey){
    String siteId  = AgentFacade.getCurrentSiteId();
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", functionKey);
    boolean privilege = ((Boolean)authzService.getAuthzMap().get(functionName+"_"+siteId)).booleanValue();
    System.out.println("**** authzBean: authzService ="+authzService);
    System.out.println("**** authzBean:"+functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

}
