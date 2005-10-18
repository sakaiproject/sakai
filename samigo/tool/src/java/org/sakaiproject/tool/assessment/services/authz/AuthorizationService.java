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
 * The QuestionPoolService calls the service locator to reach the
 * manager on the back end.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class AuthorizationService
{
  private static ContextUtil cu;
  private static Log log = LogFactory.getLog(AuthorizationService.class);
  private HashMap map = new HashMap();

  public boolean allowAdminAssessment(String siteId)
  {
    boolean hasAdminPrivilege = canCreateAssessment(siteId) 
                        || canEditAnyAssessment(siteId) || canEditOwnAssessment(siteId)
                        || canDeleteAnyAssessment(siteId) || canDeleteOwnAssessment(siteId)
                        || canPublishAnyAssessment(siteId) || canPublishOwnAssessment(siteId)
                        || canGradeAnyAssessment(siteId) || canGradeOwnAssessment(siteId)
                        || canCreateQuestionPool(siteId)
                        || canEditOwnQuestionPool(siteId)
                        || canDeleteOwnQuestionPool(siteId)
                        || canCopyOwnQuestionPool(siteId);

    return hasAdminPrivilege;
  }

  public boolean canCreateAssessment(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "create_assessment");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canEditAnyAssessment(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "edit_any_assessment");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);

    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canEditOwnAssessment(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "edit_own_assessment");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canDeleteAnyAssessment(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "delete_any_assessment");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canDeleteOwnAssessment(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "delete_own_assessment");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canPublishAnyAssessment(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "publish_any_assessment");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canPublishOwnAssessment(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "publish_own_assessment");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canGradeAnyAssessment(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "grade_any_assessment");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canGradeOwnAssessment(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "grade_own_assessment");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canCreateQuestionPool(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "create_questionpool");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }
  
  public boolean canEditOwnQuestionPool(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "edit_own_questionpool");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canDeleteOwnQuestionPool(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "delete_own_questionpool");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

  public boolean canCopyOwnQuestionPool(String siteId)
  {
    String functionName=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.facade.authz.resource.AuthzPermissions", "copy_own_questionpool");
    boolean privilege = PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(functionName);
    map.put(functionName+"_"+siteId, new Boolean(privilege)); 
    System.out.println(functionName+"_"+siteId+"="+privilege);
    return privilege;
  }

}
