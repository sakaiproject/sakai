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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.Serializable;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.service.framework.portal.cover.PortalService;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.RealmService;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.tool.assessment.data.ifc.shared.AgentDataIfc;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.BackingBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.service.legacy.user.User;

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
  public String getAgentString(){
    String agentS="";
    // this is anonymous user sign 'cos sakai doesn't know about them-daisyf
    try
    {
      User user = UserDirectoryService.getCurrentUser();

      if (user ==  null || user.getId() == null ||
          ("").equals(user.getId()))
      {
        agentS = getAnonymousId();
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
   * Get the agent string.
   * @param req the HttpServletRequest
   * @param res the HttpServletResponse
   * @return the agent string.
   */
  public String getAgentString(HttpServletRequest req, HttpServletResponse res){
    String agentS="";
    // this is a sign that an unauthenticated person is trying to access the application
    // 'cos sakai doesn't know about them-daisyf
    try
    {
      User user = UserDirectoryService.getCurrentUser();

      if (user == null || user.getId() == null ||
          ("").equals(user.getId()))
      {
        BackingBean bean = lookupBackingBean(req, res);
        if (bean != null && !bean.getProp1().equals("prop1"))
        {
          agentS = bean.getProp1();
        }
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
    System.out.println("** getAgentString() ="+agentS);
    return agentS;
  }

  private BackingBean lookupBackingBean(HttpServletRequest req,
                                        HttpServletResponse res)
  {
    BackingBean bean =
      (BackingBean) ContextUtil.lookupBeanFromExternalServlet(
      "backingbean", req, res);
    return bean;
  }

  private BackingBean lookupBackingBean()
  {
    BackingBean bean =  null;
    try
    {
      bean = (BackingBean) ContextUtil.lookupBean("backingbean");
    }
    catch (Exception ex)
    {
      log.warn("Backing been not available.  " +
               "This needs to be fixed if you are not running a unit test.");
    }

    return bean;
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
    if (thisSiteId == null)
      return role;

    String realmName = "/site/" + thisSiteId;
    Role userRole=null;

    try
    {
      AuthzGroup siteAuthzGroup = RealmService.getAuthzGroup(realmName);
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
  public String getCurrentSiteId(){
    // access via url => users does not login via any sites
    String currentSiteId = null;
    DeliveryBean delivery =  lookupDeliveryBean();

    if (delivery!=null && !delivery.getAccessViaUrl())
    {
        currentSiteId = ToolManager.getCurrentPlacement().getContext();
    }
    return currentSiteId;
  }

  /**
   * Get current site id from within an external servlet.
   * @param req the HttpServletRequest
   * @param res the HttpServletResponse
   * @return teh site id.
   */
  public String getCurrentSiteIdFromExternalServlet(HttpServletRequest req,  HttpServletResponse res){
    // access via url => users does not login via any sites-daisyf
    String currentSiteId = null;
    DeliveryBean delivery =  lookupDeliveryBean();
    if (delivery!=null && !delivery.getAccessViaUrl())
    {
        currentSiteId = ToolManager.getCurrentPlacement().getContext();
    }
    return currentSiteId;
  }

  // this method should live somewhere else-daisyf
  /**
   * Create anonymous user and return the anonymous user id.
   * @return the anonymous user id.
   */

  public String createAnonymous(){
    String anonymousId = "anonymous_";
    try
    {
      BackingBean bean = lookupBackingBean();
      anonymousId += (new java.util.Date()).getTime();
      bean.setProp1(anonymousId);
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
  public String getCurrentSiteName(){
    // access via url => users does not login via any sites-daisyf
    String currentSiteName = null;
    DeliveryBean delivery =  lookupDeliveryBean();
    if (delivery!=null && !delivery.getAccessViaUrl()){
      try{
        currentSiteName = SiteService.getSite(getCurrentSiteId()).getTitle();
      }
      catch (Exception e){
        System.out.println(e.getMessage());
      }
    }
    return currentSiteName;
  }

  /**
   * Right now gets from faces context.
   * @return
   */
  private DeliveryBean lookupDeliveryBean()
  {
    DeliveryBean delivery = null;
    try
    {
      delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    }
    catch (Exception ex)
    {
      log.warn("Delivery bean not available.  " +
               "This needs to be fixed if you are not running a unit test.");
    }
    return delivery;
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
  public String getAnonymousId(){
    String agentS="";
    BackingBean bean = lookupBackingBean();
    if (bean != null && !bean.getProp1().equals("prop1"))
        agentS = bean.getProp1();
    return agentS;
  }

}