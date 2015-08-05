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

package org.sakaiproject.tool.assessment.integration.helper.ifc;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;

/**
 *
 * <p>Description:
 * This is a context implementation helper delegate interface for
 * the AgentFacade class.  Using Spring injection via the integrationContext.xml
 * selected by the build process to find the implementation.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 */

public interface AgentHelper extends Serializable
{

  public static final String UNASSIGNED_AGENT_STRING = "UNASSIGNED";

  public AgentImpl getAgent();

  public String getAgentString(String agentString);

  public String createAnonymous(AgentFacade agent);

  public String getAnonymousId(String agentString);

  public String getDisplayName(String agentString);

  public String getFirstName(String agentString);

  public String getLastName(String agentString);

  public String getEmail(String agentString);
  
  public String getRole(String agentString); // for static call

  public String getRoleForCurrentAgent(String agentString); // for instance call

  public String getCurrentSiteId(boolean accessViaUrl);

  public String getCurrentSiteName(boolean accessViaUrl);

  public String getSiteName(String siteId);

  public String getDisplayNameByAgentId(String agentId);

  public boolean isIntegratedEnvironment();

  public Map getUserRolesFromContextRealm(Collection inUsers);

  //cwen
  public String getRoleForAgentAndSite(String agentString, String siteId);

  public String getEid(String agentString);
  public String getEidById(String agentString);

  /**
   * This is a kludge to work around a JSF scriptlet dependency introduced by cwen
   * on org.sakaiproject.service.component.cover.ServerConfigurationService.
   * @todo for 2.2 remove method when done with refactor.
   * @deprecated
   *
   * @return true unless it is turned off
   */
  public boolean isFileUploadAvailable();

  public String getDisplayId(String agentId);
}
