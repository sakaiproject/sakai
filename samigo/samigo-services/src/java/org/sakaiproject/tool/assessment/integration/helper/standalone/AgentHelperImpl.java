/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/integration/helper/standalone/AgentHelperImpl.java $
 * $Id: AgentHelperImpl.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
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

package org.sakaiproject.tool.assessment.integration.helper.standalone;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 *
 * <p>Description:
 * This is a stub standalone context implementation helper delegate class for
 * the AgentFacade class.  "Standalone" means that Samigo (Tests and Quizzes)
 * is running without the context of the Sakai portal and authentication
 * mechanisms, and therefore we "make up" some of the values returned.</p>
 * <p>Note: To customize behavior you can add your own helper class to the
 * Spring injection via the integrationContext.xml for your context.
 * The particular integrationContext.xml to be used is selected by the
 * build process.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 * based on code originally in AgentFacade
 */
public class AgentHelperImpl implements AgentHelper
{
  private static Log log = LogFactory.getLog(AgentHelperImpl.class);
  String agentString;


  /**
   * Get an osid Agent implementation class instance.
   *
   * @return an AgentImpl: osid Agent implementation class.
   */
  public AgentImpl getAgent(){
    AgentImpl agent = new AgentImpl("Administrator", null, new IdImpl("admin"));
    return agent;
  }

  /**
   * Get the agent string.
   * @return the agent string.
   */

  public String getEid(String agentString){
    return getAgentString(agentString);
  }
  public String getEidById(String agentString){
    return getAgentString(agentString);
  }

  public String getAgentString(String agentString){
    String agentS = "admin";
    try
    {
      if (!UNASSIGNED_AGENT_STRING.equals(agentString))
      {
        agentS = agentString;
      }
    }
    catch (Exception ex)
    {
      log.warn(ex.getMessage());
      // default
    }
    return agentS;
  }

  /**
   * Get the agent string.
   * @param req the HttpServletRequest
   * @param res the HttpServletResponse
   * @return the agent string.
   */
  public String getAgentString(HttpServletRequest req, HttpServletResponse res){
    String agentS = "admin";
    try
    {
      if (!UNASSIGNED_AGENT_STRING.equals(agentString))
      {
        agentS = agentString;
      }
    }
    catch (Exception ex)
    {
      log.warn(ex.getMessage());
      // default
    }
    return agentS;
  }

  /**
   * Get the Agent display name.
   * @param agentS the Agent string.
   * @return the Agent display name.
   */
  public String getDisplayName(String agentString){
    if ("admin".equals(agentString))
      return "Administrator";
    else if ("rachel".equals(agentString))
      return "Rachel Gollub";
    else if ("marith".equals(agentString))
      return "Margaret Petit";
    else
      return "Dr. Who";
  }

  /**
   * Get the Agent first name.
   * @param agentString teh agent string
   * @return the Agent first name.
   */
  public String getFirstName(String agentString)
  {
    if ("admin".equals(agentString))
      return "Samigo";
    else if ("rachel".equals(agentString))
      return "Rachel";
    else if ("marith".equals(agentString))
      return "Margaret";
    else
      return "Dr.";
  }

  /**
   * Get the Agent last name.
   * @param agentString the agent string
   * @return the Agent last name.
   */
  public String getLastName(String agentString)
  {
    if ("admin".equals(agentString))
      return "Administrator";
    else if ("rachel".equals(agentString))
      return "Gollub";
    else if ("marith".equals(agentString))
      return "Petit";
    else
      return "Who";
  }
  
  /**
   * Get the Agent email.
   * @param agentString teh agent string
   * @return the Agent email.
   */
  public String getEmail(String agentString)
  {
    String s="";
    try{
      if (!agentString.startsWith("anonymous_"))
        s=UserDirectoryService.getUser(agentString).getEmail();
    }
    catch(Exception e){
      log.warn(e.getMessage());
    }
    return s;
  }

  /**
   * Called by AgentFacade from an instance.
   * @param agentString the agent string for current AgentFacade instance
   * @return role string
   */

  public String getRoleForCurrentAgent(String agentString)
  {
    return "Student";
  }

  /**
   * For a specific agent id, get the agent role.
   * @param agentString the agent string
   * @return the agent role.
   */
  public String getRole(String agentString)
  {
    return "Maintain";
  }

  /**
   * Get the current site id.
   * @return the site id.
   */
  public String getCurrentSiteId(boolean accessViaUrl){
    return "Samigo Site";
  }

  /**
   * Get the current site name.
   * @return the site name.
   */
  public String getCurrentSiteName(boolean accessViaUrl){
    return "Samigo Site";
  }

  /**
   * Get the site name.
   * @param siteId  site id
   * @return the site name.
   */
  public String getSiteName(String siteId){
    return "Samigo Site";
  }

  /**
   * Get the id string.
   * @return the id string.
   */

  /**
   * Get the display name fo ra specific agent id string.
   * @param agentId the agent id string.
   * @return the display name.
   */
  public String getDisplayNameByAgentId(String agentId){
    return "Samigo Administrator";
  }

  /**
   * Create anonymous user and return the anonymous user id.
   * @return the anonymous user id.
   */
  public String createAnonymous(AgentFacade agent){
    String anonymousId = "anonymous_";
    try
    {
      anonymousId += (new java.util.Date()).getTime();
      agent.setAgentInstanceString(anonymousId);
    }
    catch (Exception ex)
    {
      log.warn(ex.getMessage());
      // leave... ...mostly for unit testing
    }
    return anonymousId;
  }

  /**
   * Is this a standalone environment?
   * @return true, always, in this implementation
   */
  public boolean isStandaloneEnvironment(){
    return true;
  }

  /**
   * Is this an integrated environment?
   * @return false, in this implementation
   */
  public boolean isIntegratedEnvironment(){
    return false;
  }

  /**
   * Get current site id from within an external servlet.
   * @param req the HttpServletRequest
   * @param res the HttpServletResponse
   * @return teh site id.
   */
  public String getCurrentSiteIdFromExternalServlet(HttpServletRequest req,  HttpServletResponse res){
      return "Samigo Site";
  }

  /**
   * Get the anonymous user id.
   * @return the anonymous user id.
   */
  public String getAnonymousId(String agentString){
    String agentS = "";
    try
    {
      if (!UNASSIGNED_AGENT_STRING.equals(agentString))
      {
        agentS = agentString;
      }
    }
    catch (Exception ex)
    {
      log.warn(ex.getMessage());
      // leave... ...mostly for unit testing
    }
    return agentS;
  }

  /**
   * Set the agent id string.
   * @param idString the isd string.
   */

  /**
   * Set the agent string.
   * @param agentString the agent string.
   */
  public void setAgentString(String agentString)
  {
    this.agentString = agentString;
  }


  /**
   * This gets the current site id and transforms it into the realm.
   *
   * @param inUsers the Collection of users who have their roles looked up.
   *                This is a Collection of userId Strings
   * @return Returns the map of users as keys and their roles as values.
   *			If the user is not in the realm then they will have a null role.
   */
  public Map getUserRolesFromContextRealm(Collection inUsers)
  {
  return new HashMap();
  }

  //cwen
  public String getRoleForAgentAndSite(String agentString, String siteId)
  {
    return null;
  }
  /**
   * This is a kludge to work around a JSF scriptlet dependency introduced by cwen
   * on org.sakaiproject.component.cover.ServerConfigurationService.
   * @todo for 2.2 remove method when done with refactor.
   * @deprecated
   *
   * @return true unless it is turned off
   */

  public boolean isFileUploadAvailable()
  {
    return true;//always, in standalone
  }
}
