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
package org.sakaiproject.component.app.messageforums;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.kernel.function.FunctionManager;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.api.app.messageforums.DefaultFunctionManager;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroupService;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 *
 */
public class DefaultFunctionManagerImpl 
    implements DefaultFunctionManager 
{
  private static final Log LOG = LogFactory.getLog(DefaultFunctionManagerImpl.class);
  //Dependency injected
  private FunctionManager functionManager;
  private AuthzGroupService authzGroupService;

  
  public void init()
  {
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_NEW_FORUM);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_NEW_TOPIC);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_NEW_RESPONSE);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_NEW_RESPONSE_TO_RESPONSE);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_MOVE_POSTINGS);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_CHANGE_SETTINGS);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_POST_TO_GRADEBOOK);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_READ);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_REVISE_ANY);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_REVISE_OWN);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_DELETE_ANY);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_DELETE_OWN);
    functionManager.registerFunction(DefaultFunctionManager.FUNCTION_MARK_AS_READ);
  }
  /**
   * @param functionManager The functionManager to set.
   */
  public void setFunctionManager(FunctionManager functionManager)
  {
    this.functionManager = functionManager;
  }
  
  /**
   * @param authzGroupService The authzGroupService to set.
   */
  public void setAuthzGroupService(AuthzGroupService authzGroupService)
  {
    this.authzGroupService = authzGroupService;
  }
  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isNewForum(java.lang.String)
   */
  public boolean isNewForum(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_NEW_FORUM);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isNewTopic(java.lang.String)
   */
  public boolean isNewTopic(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_NEW_TOPIC);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isNewResponse(java.lang.String)
   */
  public boolean isNewResponse(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_NEW_RESPONSE);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isResponseToResponse(java.lang.String)
   */
  public boolean isResponseToResponse(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_NEW_RESPONSE_TO_RESPONSE);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isMovePostings(java.lang.String)
   */
  public boolean isMovePostings(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_MOVE_POSTINGS);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isChangeSettings(java.lang.String)
   */
  public boolean isChangeSettings(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_CHANGE_SETTINGS);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isPostToGradeBook(java.lang.String)
   */
  public boolean isPostToGradebook(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_POST_TO_GRADEBOOK);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isRead(java.lang.String)
   */
  public boolean isRead(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_READ);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isReviseAny(java.lang.String)
   */
  public boolean isReviseAny(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_REVISE_ANY);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isReviseOwn(java.lang.String)
   */
  public boolean isReviseOwn(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_REVISE_OWN);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isDeleteAny(java.lang.String)
   */
  public boolean isDeleteAny(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_DELETE_ANY);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isDeleteOwn(java.lang.String)
   */
  public boolean isDeleteOwn(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_DELETE_OWN);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultFunctionManager#isMarkAsRead(java.lang.String)
   */
  public boolean isMarkAsRead(String role)
  {
    return hasPermission(role, DefaultFunctionManager.FUNCTION_MARK_AS_READ);
  }
  
  private boolean hasPermission(String role, String permission)
  {
    Collection realmList = new ArrayList();
    realmList.add(getContextSiteId());
    Set allowedFunctions = authzGroupService.getAllowedFunctions(role, realmList);
    return allowedFunctions.contains(permission);
  }
  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");
    return ("/site/" + ToolManager.getCurrentPlacement().getContext());
  }
}
