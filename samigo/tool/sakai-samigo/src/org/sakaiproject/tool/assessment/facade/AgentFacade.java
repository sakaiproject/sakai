package org.sakaiproject.tool.assessment.facade;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import java.io.Serializable;
import javax.faces.context.FacesContext;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
//import test.org.sakaiproject.tool.assessment.jsf.TestBackingBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.BackingBean;
import org.sakaiproject.service.legacy.realm.Realm;
import org.sakaiproject.service.legacy.realm.Role;
import org.sakaiproject.service.framework.portal.cover.PortalService;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.service.legacy.realm.cover.RealmService;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import javax.servlet.*;
import javax.servlet.http.*;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * SectionFacade implements SectionDataIfc that encapsulates our out of bound (OOB)
 * agreement.
 * @author not attributable
 * @version 1.0
 */
public class AgentFacade implements Serializable {

  AgentImpl agent;
  String agentString;
  private static Log log = LogFactory.getLog(AgentFacade.class);

  public AgentFacade(String agentId)
  {
    agent = new AgentImpl(agentId, null, new IdImpl(agentId));
    agentString = agentId;
  }

  public static AgentImpl getAgent(){
    AgentImpl agent = new AgentImpl("Administrator", null, new IdImpl("admin"));
    return agent;
  }

  public static String getAgentString(){
    String agentS="";
    // this is anonymous user sign 'cos sakai don't know about them
    if (UserDirectoryService.getCurrentUser().getId()==null || ("").equals(UserDirectoryService.getCurrentUser().getId())){
      agentS = getAnonymousId();
    }
    else {
      agentS = UserDirectoryService.getCurrentUser().getId();
    }
    log.debug("** getAgentString() ="+agentS);
    return agentS;
  }

  public static String getAgentString(HttpServletRequest req, HttpServletResponse res){
    String agentS="";
    // this is a sign that an unauthenticated person is trying to access the application
    // 'cos sakai don't know about them
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

  // should phrase out this one
  public static String getDisplayName(String agentS){
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


  public static String getRole(String agentIdString)
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

  public static String getCurrentSiteId(){
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

  public static String getCurrentSiteIdFromExternalServlet(HttpServletRequest req,  HttpServletResponse res){
    // access via url => users does not login via any sites
    String currentSiteId = null;
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet("delivery",req, res);
    if (delivery !=null){
      delivery = (DeliveryBean) delivery;
      currentSiteId = ToolManager.getCurrentPlacement().getContext();
    }
    return currentSiteId;
  }

  // this method should live somewhere else
  public static String createAnonymous(){
    BackingBean bean = (BackingBean) ContextUtil.lookupBean("backingbean");
    String agentS = "anonymous_"+(new java.util.Date()).getTime();
    log.debug("create anonymous ="+agentS);
    bean.setProp1(agentS);
    return agentS;
  }

  public static String getCurrentSiteName(){
    // access via url => users does not login via any sites
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

  public static String getSiteName(String siteId){
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

  // should phrase out this one
  public static String getDisplayNameByAgentId(String agentId){
    String name = null;
    try{
      name = UserDirectoryService.getUser(agentId).getDisplayName();
    }
    catch (Exception e){
      System.out.println(e.getMessage());
    }
    return name;
  }

  public static boolean isStandaloneEnvironment(){
    return false;
  }

  public static boolean isIntegratedEnvironment(){
    return !isStandaloneEnvironment();
  }

  public static String getAnonymousId(){
    String agentS="";
    BackingBean bean = (BackingBean) ContextUtil.lookupBean("backingbean");
    if (bean != null && !bean.getProp1().equals("prop1"))
        agentS = bean.getProp1();
    return agentS;
  }

}
