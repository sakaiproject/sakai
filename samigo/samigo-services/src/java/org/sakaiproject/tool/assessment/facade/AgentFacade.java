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

package org.sakaiproject.tool.assessment.facade;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.ifc.shared.AgentDataIfc;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;

/**
 * <p>Description: Facade for agent.
 * Uses helper to determine integration context implementation.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
@Slf4j
public class AgentFacade implements Serializable, AgentDataIfc
{

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

  private static final AgentHelper helper =
    IntegrationContextFactory.getInstance().getAgentHelper();
  private static final boolean integrated =
    IntegrationContextFactory.getInstance().isIntegrated();

  private AgentImpl agent;
  private String agentString;
  private String eid;
  private boolean accessViaUrl;
  private String displayId;
    private String displayIdString;

    /**
   * Create AgentFacade for agent Id
   * @param agentId the agent Id
   */
  public AgentFacade(String agentId)
  {
    agent = new AgentImpl(agentId, null, new IdImpl(agentId));
    agentString = agentId;
    eid = helper.getEid(agentId);
    displayId = helper.getDisplayId(agentId);
  }

  /**
   * Create AgentFacade and have it look up its own agentString (id).
   */
  public AgentFacade()
  {
    // If an agent string is available, UNASSIGNED_AGENT_STRING willbe replaced
    String agentId = helper.getAgentString(AgentHelper.UNASSIGNED_AGENT_STRING);
    agent = new AgentImpl(agentId, null, new IdImpl(agentId));
    agentString = agentId;
    eid = helper.getEid(AgentHelper.UNASSIGNED_AGENT_STRING);
  }

  /**
   * Get an osid Agent implementation class instance.
   *
   * @return an AgentImpl: osid Agent implementation class.
   */
  public static AgentImpl getAgent()
  {
    return helper.getAgent();
  }

  /**
   * Get the agent string.
   * Static convenience method.
   * @return the agent string.
   */
  public static String getAgentString()
  {
    AgentFacade facade =new AgentFacade();
    return facade.agentString;
  }


  /**
   * Get the current user's Eid.
   * Static convenience method.
   * @return the eid.
   */
  public static String getEid()
  {
    AgentFacade facade =new AgentFacade();
    return facade.eid;
  }

  /**
   * Get the agent string.
   * Preferred approach: instantiate and then call this.
   * @return the agent string.
   */
  public String getAgentInstanceString()
  {
    return this.agentString;
  }

//  /**
//   * Get the agent string.
//   * @param req the HttpServletRequest
//   * @param res the HttpServletResponse
//   * @return the agent string.
//   */
//  public static String getAgentString(HttpServletRequest req,
//                                      HttpServletResponse res)
//  {
//    return helper.getAgentString(agentString);
//  }

  /**
   * Get the Eid String of an Agent.
   * @return the Agent Eid.
   */
  public String getEidString()
  {
log.debug("agentfacade.getEid(agentS) agentString = " + agentString);
    return helper.getEidById(agentString);
  }


  /**
   * Get the Agent display name.
   * @param agentS the Agent string.
   * @return the Agent display name.
   */
  public static String getDisplayName(String agentS)
  {
    return helper.getDisplayName(agentS);
  }



  /**
   * Get the Agent display name for this agent.
   * @return the Agent display name.
   */
  public String getDisplayName()
  {
    return helper.getDisplayName(this.agentString);
  }

  /**
   * Get the Agent first name.
   * @return the Agent first name.
   */
  public String getFirstName()
  {
    return helper.getFirstName(agentString);
  }

  /**
   * Get the Agent last name.
   * @return the Agent last name.
   */
  public String getLastName()
  {
    return helper.getLastName(agentString);
  }

  /**
   * Get the Agent last name.
   * @return the Agent last name.
   */
  public String getEmail()
  {
    return helper.getEmail(agentString);
  }
  
/**
 * Get role for this agent.
 * @return role string
 */
  public String getRole()
  {
    return helper.getRoleForCurrentAgent(agentString);
  }

  /**
   * Get role for  agent.
   * @param agentString the agent string.
   * @return role string
   */
  public static String getRole(String agentString)
  {
    return helper.getRole(agentString);
  }

  /**
   * Get the current site id.
   * @return the site id.
   */
  public static String getCurrentSiteId()
  {
    AgentFacade facade = new AgentFacade();
    boolean accessViaUrl = facade.isAccessViaUrl();

    return helper.getCurrentSiteId(accessViaUrl);
  }

  /**
   * Get the current site name.
   * @return the site name.
   */
  public static String getCurrentSiteName()
  {
    AgentFacade facade = new AgentFacade();
    boolean accessViaUrl = facade.isAccessViaUrl();

    return helper.getCurrentSiteName(accessViaUrl);
  }


  /**
   * Get the site name.
   * @param siteId  site id
   * @return the site name.
   */
  public static String getSiteName(String siteId)
  {
    return helper.getSiteName(siteId);
  }

  public String getIdString()
  {
    return agentString;
  }

  // should phrase out this one -daisyf
  /**
   * Get the display name for a specific agent id string.
   * @param agentId the agent id string.
   * @return the display name.
   */
  public static String getDisplayNameByAgentId(String agentId)
  {
    return helper.getDisplayNameByAgentId(agentId);
  }

  /**
   * Create anonymous user and return the anonymous user id.
   * @return the anonymous user id.
   */
  public static String createAnonymous()
  {
    AgentFacade facade = new AgentFacade();
    return helper.createAnonymous(facade);
  }

  /**
   * Is this an integrated environment?
   * @return true, in this implementation
   */
  public static boolean isIntegratedEnvironment()
  {
    return integrated;
  }

//  /**
//   * Get current site id from within an external servlet.
//   * @param req the HttpServletRequest
//   * @param res the HttpServletResponse
//   * @return teh site id.
//   */
//  public static String getCurrentSiteIdFromExternalServlet(HttpServletRequest
//    req, HttpServletResponse res)
//  {
//    return helper.getCurrentSiteIdFromExternalServlet(req, res);
//  }

  /**
   * Get the anonymous user id.
   * @return the anonymous user id.
   */
  public static String getAnonymousId()
  {
    AgentFacade facade = new AgentFacade();
    String agentString = facade.getAgentInstanceString();
    return helper.getAnonymousId(agentString);
  }


  /**
   * Change the agent string
   * @param idString the id string.
   */
  public void setIdString(String idString)
  {
    agentString = idString;
  }

  /**
   * Get the anonymous user id.
   * @return the anonymous user id.
   */
  public static Map getUserRolesFromContextRealm(Collection inUsers)
  {
    return helper.getUserRolesFromContextRealm(inUsers);
  }

  //cwen
  public static String getRoleForAgentAndSite(String agentString, String siteId)
  {
    return helper.getRoleForAgentAndSite(agentString, siteId);
  }
  public boolean isAccessViaUrl()
  {
    return (agentString.startsWith("anonymous_"));
  }
  public void setAccessViaUrl(boolean accessViaUrl)
  {
    this.accessViaUrl = accessViaUrl;
  }
  
  public void setAgentInstanceString(String agentInstanceString)
  {
    this.agentString = agentInstanceString;
  }

  /**
   * This is a kludge to work around a JSF scriptlet dependency introduced by cwen
   * on org.sakaiproject.service.component.cover.ServerConfigurationService.
   * @todo for 2.2 remove method when done with refactor.
   * @deprecated
   *
   * @return true unless it is turned off
   */

  public static boolean isFileUploadAvailable()
  {
    return helper.isFileUploadAvailable();
  }

  @Override
  public int hashCode() {
	  final int prime = 31;
	  int result = 1;
	  result = prime * result
			  + ((agentString == null) ? 0 : agentString.hashCode());
	  return result;
  }

  @Override
  public boolean equals(Object obj) {
	  if (this == obj)
		  return true;
	  if (obj == null)
		  return false;
	  if (getClass() != obj.getClass())
		  return false;
	  AgentFacade other = (AgentFacade) obj;
	  if (agentString == null) {
		  if (other.agentString != null)
			  return false;
	  } else if (!agentString.equals(other.agentString))
		  return false;
	  return true;
  }

    public static String getDisplayId() {
        AgentFacade facade =new AgentFacade();
        return facade.displayId;
    }

    public String getDisplayIdString() {
        return helper.getDisplayId(agentString);
    }
}
