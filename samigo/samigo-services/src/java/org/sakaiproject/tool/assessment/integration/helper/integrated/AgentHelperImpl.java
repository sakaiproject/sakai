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

package org.sakaiproject.tool.assessment.integration.helper.integrated;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

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
@Slf4j
 public class AgentHelperImpl implements AgentHelper
{
  AgentImpl agent;

  private AuthzGroupService authzGroupService;

  public AgentHelperImpl() {
    authzGroupService = ComponentManager.get(AuthzGroupService.class);
  }

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
      log.warn("getAgentString(): " + ex.getMessage());
    }
    return agentS;
  }

  public String getEid(String agentString){
    String eid="";
    // this is anonymous user sign 'cos sakai doesn't know about them-daisyf
    // this returns the currently logged in user's eid. 
    try
    {
      User user = UserDirectoryService.getCurrentUser();

      if (user ==  null || user.getId() == null ||
          ("").equals(user.getId()))
      {
        eid = getAnonymousId(agentString);
      }
      else
      {
        eid = user.getEid();
      }
    }
    catch (Exception ex)
    {
      log.warn("getEid: " + ex.getMessage());
    }
    return eid;
  }

  /**
   * Get the Agent Eid given an Id String.
   * @param agentS the Agent Id string.
   * @return the Agent Eid.
   */
  public String getEidById(String agentString){
log.debug("getEidById agentString = " + agentString);
    String s="";
    try{
      if (!agentString.startsWith("anonymous_"))
        s=UserDirectoryService.getUser(agentString).getEid();
log.debug("getEidById agentString s = " + s);
    }
    catch(Exception e){
      log.warn("getEidById: " + e.getMessage());
    }
    return s;
  }


  /**
   * Get the Agent display name.
   * @param agentS the Agent string.
   * @return the Agent display name.
   */
  public String getDisplayName(String agentString){
    String s="";
    try{
      if (!agentString.startsWith("anonymous_"))
        s=UserDirectoryService.getUser(agentString).getDisplayName();
    }
    catch(Exception e){
      log.warn("getDisplayName: " + e.getMessage());
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
      if (!agentString.startsWith("anonymous_"))
        s=UserDirectoryService.getUser(agentString).getFirstName();
    }
    catch(Exception e){
      log.warn("getFirstName:" + e.getMessage());
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
      if (!agentString.startsWith("anonymous_"))
        s=UserDirectoryService.getUser(agentString).getLastName();
    }
    catch(Exception e){
      // if agentString is anonymous, s=""
      log.warn("getLastName: " + e.getMessage());
    }
    return s;
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
      log.warn("getRole : " + ex.getMessage());
    }
    //cwen
    if ((thisSiteId == null) || (thisSiteId.equals("")))
      return role;

    String realmName = "/site/" + thisSiteId;
    Role userRole=null;

    try
    {
      AuthzGroup siteAuthzGroup = authzGroupService.getAuthzGroup(realmName);
      if (siteAuthzGroup!=null)
      userRole = siteAuthzGroup.getUserRole(agentString);
      if (userRole!=null)
        role = userRole.getId();
    }
    catch(Exception e)
    {
        log.error(e.getMessage(), e);
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
      log.warn("createAnonymous : " + ex.getMessage());
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
        log.warn("getCurrentSiteName : "  + e.getMessage());
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
    }
    catch (Exception ex){
      log.warn("getSiteName : " + ex.getMessage());
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
        log.error(e.getMessage(), e);
    }
    return name;
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
        log.warn("getUserRolesFromContextRealm : " + ex.getMessage());
      }
      //If none the returna blank map
      if (thisSiteId == null)
        return new HashMap();

    //create the realm from the site
      String realmName = "/site/" + thisSiteId;

      //get the roles from the realm and set of users
      return authzGroupService.getUsersRole(inUsers, realmName);
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
      AuthzGroup siteAuthzGroup = authzGroupService.getAuthzGroup(realmName);
      if (siteAuthzGroup!=null)
      userRole = siteAuthzGroup.getUserRole(agentString);
      if (userRole!=null)
        role = userRole.getId();
      log.debug(realmName + ":" + role);
    }
    catch(GroupNotDefinedException e)
    {
      log.error("error in:" + this + "-getRoleForAgnetAndSite");
    }
    return role;
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
    String commentOutFileUpload =
      ServerConfigurationService.getString("sam_file_upload_comment_out");

    if (commentOutFileUpload==null) return true;

    return !commentOutFileUpload.equalsIgnoreCase("true");
  }

    /**
     * Get the Agent DisplayId given an Id String.
     *
     * @param agentString the Agent Id string.
     * @return the Agent Eid.
     */
    public String getDisplayId(String agentString) {
        if (AgentHelper.UNASSIGNED_AGENT_STRING.equals(agentString)) {
            return "";
        }
        try {
            if (!agentString.startsWith("anonymous_"))  {
                return UserDirectoryService.getUser(agentString).getDisplayId();
            }

        } catch (Exception e) {
            log.warn("getDisplayId: " + e.getMessage());
        }
        return "";
    }


}
