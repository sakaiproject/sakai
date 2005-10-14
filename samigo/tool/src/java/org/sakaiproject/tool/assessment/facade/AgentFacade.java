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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.ifc.shared.AgentDataIfc;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.integration.context.
  IntegrationContextFactory;

/**
 * <p>Description: Facade for agent.
 * Uses helper to determine integration context implementation.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */

public class AgentFacade implements Serializable, AgentDataIfc
{

  private static Log log = LogFactory.getLog(AgentFacade.class);

  private static final AgentHelper helper =
    IntegrationContextFactory.getInstance().getAgentHelper();
  private static final boolean integrated =
    IntegrationContextFactory.getInstance().isIntegrated();

  AgentImpl agent;
  String agentString;

  /**
   * Create AgentFacade for agent Id
   * @param agentId the agent Id
   */
  public AgentFacade(String agentId)
  {
    agent = new AgentImpl(agentId, null, new IdImpl(agentId));
    agentString = agentId;
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
   * @return the agent string.
   */
  public static String getAgentString()
  {
    return helper.getAgentString();
  }

  /**
   * Get the agent string.
   * @param req the HttpServletRequest
   * @param res the HttpServletResponse
   * @return the agent string.
   */
  public static String getAgentString(HttpServletRequest req,
                                      HttpServletResponse res)
  {
    return helper.getAgentString(req, res);
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
    return helper.getCurrentSiteId();
  }

  /**
   * Get the current site name.
   * @return the site name.
   */
  public static String getCurrentSiteName()
  {
    return helper.getCurrentSiteName();
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
    return helper.createAnonymous();
  }


  /**
   * Is this a standlaone environment?
   * @return false, in this implementation
   */
  public static boolean isStandaloneEnvironment()
  {
    return!integrated;
  }

  /**
   * Is this an integrated environment?
   * @return true, in this implementation
   */
  public static boolean isIntegratedEnvironment()
  {
    return integrated;
  }

  /**
   * Get current site id from within an external servlet.
   * @param req the HttpServletRequest
   * @param res the HttpServletResponse
   * @return teh site id.
   */
  public static String getCurrentSiteIdFromExternalServlet(HttpServletRequest
    req, HttpServletResponse res)
  {
    return helper.getCurrentSiteIdFromExternalServlet(req, res);
  }

  /**
   * Get the anonymous user id.
   * @return the anonymous user id.
   */
  public static String getAnonymousId()
  {
    return helper.getAnonymousId();
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


}
