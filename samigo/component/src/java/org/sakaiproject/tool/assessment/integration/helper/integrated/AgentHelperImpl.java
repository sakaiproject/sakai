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
package org.sakaiproject.tool.assessment.integration.helper.integrated;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
//cwen
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.tool.assessment.facade.AgentFacade;

/**
 *
 * <p>Description:
 * This is an integrated context implementation helper delegate class for
 * the AgentFacade class.  "Integrated" means that Samigo (Tests and Quizzes)
 * is running within the context of the Sakai portal and authentication
 * mechanisms, and therefore makes calls on Sakai for things it needs.</p>
 * <p>Note: To customize behavior you can add your own helper class to the
 * Spring injection via the integrationContext.xml for your context.
 * The particular integrationContext.xml to be used is selected by the
 * build process.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 * based on code originally in AgentFacade
 *
 *
 */
public class AgentHelperImpl implements AgentHelper
{
  private static Log log = LogFactory.getLog(AgentHelperImpl.class);
  AgentImpl agent;

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
  public String getAgentString(String agentString){
    String agentS="";
    // this is anonymous user sign 'cos sakai doesn't know about them-daisyf
    try
    {
      User user = UserDirectoryService.getCurrentUser();

      if (user ==  null || user.getId() == null ||
          ("").equals(user.getId()))
      {
        agentS = getAnonymousId(agentString);
      }
      else
      {
        agentS = user.getId();
      }
    }
    catch (Exception ex)
    {
      log.warn(ex);
    }
    log.debug("** getAgentString() ="+agentS);
    return agentS;
  }

  /**
   * Get the Agent display name.
   * @param agentS the Agent string.
   * @return the Agent display name.
   */
  public String getDisplayName(String agentS){
    String s="";
    try{
      s=UserDirectoryService.getUser(agentS).getDisplayName();
    }
    catch(Exception e){
      System.out.println(e.getMessage());
    }
    return s;
  }

  /**
   * Get the Agent first name.
   * @param agentString teh agent string
   * @return the Agent first name.
   */
  public String getFirstName(String agentString)
  {
    String s="";
    try{
      s=UserDirectoryService.getUser(agentString).getFirstName();
    }
    catch(Exception e){
      System.out.println(e.getMessage());
    }
    return s;
  }

  /**
   * Get the Agent last name.
   * @param agentString teh agent string
   * @return the Agent last name.
   */
  public String getLastName(String agentString)
  {
    String s="";
    try{
      s=UserDirectoryService.getUser(agentString).getLastName();
    }
    catch(Exception e){
      System.out.println(e.getMessage());
    }
    return s;
  }


  /**
   * Can be called statically from AgentFacade from an instance
   * @param agentString the agent string for an agent
   * @return role string
   */
  public String getRole(String agentString)
  {
    String role = "anonymous_access";
    String thisSiteId = null;
    try
    {
      thisSiteId = ToolManager.getCurrentPlacement().getContext();
    }
    catch (Exception ex)
    {
      log.warn("Failure to get site id from ToolManager.  \n" +
               "Need to fix if not running in unit test.");
      log.warn(ex);
    }
    //cwen
    if ((thisSiteId == null) || (thisSiteId.equals("")))
      return role;

    String realmName = "/site/" + thisSiteId;
    Role userRole=null;

    try
    {
      AuthzGroup siteAuthzGroup = AuthzGroupService.getAuthzGroup(realmName);
      if (siteAuthzGroup!=null)
      userRole = siteAuthzGroup.getUserRole(agentString);
      if (userRole!=null)
        role = userRole.getId();
      log.debug(realmName + ":" + role);
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
    }
    return role;
  }

  /**
   * Called by AgentFacade from an instance.  In integrated just wrap the above.
   * @param agentString the agent string for current AgentFacade instance
   * @return role string
   */
  public String getRoleForCurrentAgent(String agentString)
  {
     return this.getRole(agentString);
  }

  /**
   * Get the current site id.
   * @return the site id.
   */
  public String getCurrentSiteId(boolean accessViaUrl){
    // access via url => users does not login via any sites
    String currentSiteId = null;
    if (!accessViaUrl)
    {
//    cwen
      Placement thisPlacement = ToolManager.getCurrentPlacement();
      if(thisPlacement != null)
        currentSiteId = thisPlacement.getContext();
    }
    return currentSiteId;
  }

  // this method should live somewhere else-daisyf
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
      // leave... ...mostly for unit testing
    }
    return anonymousId;
  }

  /**
   * Get the current site name.
   * @return the site name.
   */
  public String getCurrentSiteName(boolean accessViaUrl){
    // access via url => users does not login via any sites-daisyf
    String currentSiteName = null;
    if (!accessViaUrl)
    {
      try{
        currentSiteName =
          SiteService.getSite(getCurrentSiteId(accessViaUrl)).getTitle();
      }
      catch (Exception e){
        System.out.println(e.getMessage());
      }
    }
    return currentSiteName;
  }


  /**
   * Get the site name.
   * @param siteId  site id
   * @return the site name.
   */
  public String getSiteName(String siteId){
   String siteName=null;
   try{
      siteName = SiteService.getSite(siteId).getTitle();
      log.info("**** siteName="+siteName);
    }
    catch (Exception ex){
      log.warn(ex);
      log.warn("SiteService not available.  " +
               "This needs to be fixed if you are not running a unit test.");
    }
    return siteName;
  }

  // should phrase out this one -daisyf
  /**
   * Get the display name for a specific agent id string.
   * @param agentId the agent id string.
   * @return the display name.
   */
  public String getDisplayNameByAgentId(String agentId){
    String name = null;
    try{
      name = UserDirectoryService.getUser(agentId).getDisplayName();
    }
    catch (Exception e){
      System.out.println(e.getMessage());
    }
    return name;
  }

  /**
   * Is this a standlaone environment?
   * @return false, in this implementation
   */
  public boolean isStandaloneEnvironment(){
    return false;
  }

  /**
   * Is this an integrated environment?
   * @return true, in this implementation
   */
  public boolean isIntegratedEnvironment(){
    return true;
  }

  /**
   * Set the agent id string.
   * @param idString the isd string.
   */

  /**
   * Get the anonymous user id.
   * @return the anonymous user id.
   */
  public String getAnonymousId(String agentString){
    String agentS="";
    if (!UNASSIGNED_AGENT_STRING.equals(agentString))
    {
      agentS = agentString;
    }
    return agentS;
  }


  /**
   * This gets the current site id and transforms it into the realm.
   *  From there it asks the AuthzGroupService for the roles of the given users
   *
   * @param inUsers the Collection of users who have their roles looked up.
   *                This is a Collection of userId Strings
   * @return Returns the map of users as keys and their roles as values.
   *			If the user is not in the realm then they will have a null role.
   */
  public Map getUserRolesFromContextRealm(Collection inUsers)
  {
        //Get the SiteId
      String thisSiteId = null;
      try
      {
        thisSiteId = ToolManager.getCurrentPlacement().getContext();
      }
      catch (Exception ex)
      {
        log.warn("Failure to get site id from ToolManager.  \n" +
                 "Need to fix if not running in unit test.");
        log.warn(ex);
      }
      //If none the returna blank map
      if (thisSiteId == null)
        return new HashMap();

    //create the realm from the site
      String realmName = "/site/" + thisSiteId;

      //get the roles from the realm and set of users
      return AuthzGroupService.getUsersRole(inUsers, realmName);
  }

  //cwen
  public String getRoleForAgentAndSite(String agentString, String siteId)
  {
    String role = "anonymous_access";

    if (siteId == null)
      return role;

    String realmName = "/site/" + siteId;
    Role userRole=null;

    try
    {
      AuthzGroup siteAuthzGroup = AuthzGroupService.getAuthzGroup(realmName);
      if (siteAuthzGroup!=null)
      userRole = siteAuthzGroup.getUserRole(agentString);
      if (userRole!=null)
        role = userRole.getId();
      log.debug(realmName + ":" + role);
    }
    catch(Exception e)
    {
      log.error("error in:" + this + "-getRoleForAgnetAndSite");
    }
    return role;
  }

}