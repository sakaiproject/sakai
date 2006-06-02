/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/AuthzQueriesFacadeAPI.java $
 * $Id: AuthzQueriesFacadeAPI.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.facade;

import java.util.HashMap;
import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;

public interface AuthzQueriesFacadeAPI
{

  public boolean hasPrivilege(String functionName);

  public boolean isAuthorized(String agentId, String function, String qualifier);

  public AuthorizationData createAuthorization(String agentId,
      String functionId, String qualifierId);

  public void removeAuthorizationByQualifier(String qualifierId, boolean isPublishedAssessment);

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
