/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.facade;

import java.util.HashMap;
import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;

public interface AuthzQueriesFacadeAPI
{

  public boolean hasAdminPriviledge(String agentId, String function, String qualifier);

  public boolean isAuthorized(String agentId, String function, String qualifier);

  public AuthorizationData createAuthorization(String agentId,
      String functionId, String qualifierId);

  public void removeAuthorizationByQualifier(String qualifierId);

  /** This returns a HashMap containing (String a.qualiferId, AuthorizationData a)
   * agentId is a site for now but can be a user
   */
  public HashMap getAuthorizationToViewAssessments(String agentId);

  public List getAuthorizationByAgentAndFunction(String agentId,
      String functionId);

  public List getAuthorizationByFunctionAndQualifier(String functionId,
      String qualifierId);

  public boolean checkMembership(String siteId);

}
