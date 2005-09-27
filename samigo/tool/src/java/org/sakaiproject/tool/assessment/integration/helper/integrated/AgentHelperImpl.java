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
import org.sakaiproject.service.legacy.realm.Realm;
import org.sakaiproject.service.legacy.realm.Role;
import org.sakaiproject.service.legacy.realm.cover.RealmService;
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
  public String getAgentString(){
    String agentS="";
    // this is anonymous user sign 'cos sakai doesn't know about them-daisyf
    if (UserDirectoryService.getCurrentUser().getId()==null || ("").equals(UserDirectoryService.getCurrentUser().getId())){
      agentS = getAnonymousId();
    }
    else {
      agentS = UserDirectoryService.getCurrentUser().getId();
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
    if (UserDirectoryService.getCurrentUser().getId()==null || ("").equals(UserDirectoryService.getCurrentUser().getId())){
      BackingBean bean = (BackingBean) ContextUtil.lookupBeanFromExternalServlet(
        "backingbean", req, res);
      if (bean != null && !bean.getProp1().equals("prop1"))
        agentS = bean.getProp1();
    }
    else {
      agentS = UserDirectoryService.getCurrentUser().getId();
    }
    System.out.println("** getAgentString() ="+agentS);
    return agentS;
  }

  /**
   * Get the Agent display name.
   * @deprecated should phrase out this one -daisyf
   * @param agentS the Agent string.
   * @return the Agent display name.
   */
  public String getDisplayName(String agentS){
    return UserDirectoryService.getCurrentUser().getDisplayName();
  }

  public String getDisplayName(){
    String s="";
    try{
      s=UserDirectoryService.getUser(this.agentString).getDisplayName();
    }
    catch(Exception e){
      System.out.println(e.getMessage());
    }
    return s;
  }

  /**
   * Get the Agent first name.
   * @return the Agent first name.
   */
  public String getFirstName()
  {
    String s="";
    try{
      s=UserDirectoryService.getUser(this.agentString).getFirstName();
    }
    catch(Exception e){
      System.out.println(e.getMessage());
    }
    return s;
  }

  /**
   * Get the Agent last name.
   * @return the Agent last name.
   */
  public String getLastName()
  {
    String s="";
    try{
      s=UserDirectoryService.getUser(this.agentString).getLastName();
    }
    catch(Exception e){
      System.out.println(e.getMessage());
    }
    return s;
  }

  /**
   * Get the agent role.
   * @return the agent role.
   */
  public String getRole()
  {
    String role = "anonymous_access";
    String thisSiteId = ToolManager.getCurrentPlacement().getContext();
    String realmName = "/site/" + thisSiteId;
    if (thisSiteId == null)
      return role;

    try
    {
      Realm siteRealm = RealmService.getRealm(realmName);
      Role currentUserRole = siteRealm.getUserRole(agentString);
      if (currentUserRole != null)
        role = currentUserRole.getId();
      log.debug(realmName + ":" + role);
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
    }
    return role;
  }


  /**
   *
   * @param agentIdString
   * @return
   */
  public String getRole(String agentIdString)
  {
    String role = "anonymous_access";
    String thisSiteId = ToolManager.getCurrentPlacement().getContext();
    String realmName = "/site/" + thisSiteId;
    Role userRole=null;
    if (thisSiteId == null)
      return role;

    try
    {
      Realm siteRealm = RealmService.getRealm(realmName);
      if (siteRealm!=null)
      userRole = siteRealm.getUserRole(agentIdString);
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

  public String getIdString()
  {
    return agentString;
  }

  /**
   * Get the current site id.
   * @return the site id.
   */
  public String getCurrentSiteId(){
    // access via url => users does not login via any sites
    String currentSiteId = null;
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    if (delivery !=null){
      delivery = (DeliveryBean) delivery;
      if (!delivery.getAccessViaUrl()){
        currentSiteId = ToolManager.getCurrentPlacement().getContext();
      }
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
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet("delivery",req, res);
    if (delivery !=null){
      delivery = (DeliveryBean) delivery;
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
    BackingBean bean = (BackingBean) ContextUtil.lookupBean("backingbean");
    String agentS = "anonymous_"+(new java.util.Date()).getTime();
    log.debug("create anonymous ="+agentS);
    bean.setProp1(agentS);
    return agentS;
  }

  /**
   * Get the current site name.
   * @return the site name.
   */
  public String getCurrentSiteName(){
    // access via url => users does not login via any sites-daisyf
    String currentSiteName = null;
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    if (!delivery.getAccessViaUrl()){
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
   * Get the site name.
   * @param siteId  site id
   * @return the site name.
   */
  public String getSiteName(String siteId){
   String siteName=null;
   try{
      siteName = SiteService.getSite(siteId).getTitle();
      log.debug("**** siteName="+siteName);
    }
    catch (Exception e){
      System.out.println(e.getMessage());
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
  public void setIdString(String idString)
  {
    agentString = idString;
  }

  /**
   * Get the anonymous user id.
   * @return the anonymous user id.
   */
  public String getAnonymousId(){
    String agentS="";
    BackingBean bean = (BackingBean) ContextUtil.lookupBean("backingbean");
    if (bean != null && !bean.getProp1().equals("prop1"))
        agentS = bean.getProp1();
    return agentS;
  }
 }