package org.sakaiproject.tool.assessment.facade;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.ui.bean.shared.BackingBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

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

  private static Log log = LogFactory.getLog(AgentFacade.class);
  AgentImpl agent;
  String agentString;

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
    String agentS = "admin";
    BackingBean bean = (BackingBean) ContextUtil.lookupBean("backingbean");
    //System.out.println("Bean = " + bean.getProp1());
    if (bean != null && !bean.getProp1().equals("prop1"))
      agentS = bean.getProp1();
    return agentS;
  }

  public static String getAgentString(HttpServletRequest req, HttpServletResponse res){
    String agentS = "admin";
    BackingBean bean = (BackingBean) ContextUtil.lookupBeanFromExternalServlet(
        "backingbean", req, res);
    //System.out.println("Bean = " + bean.getProp1());
    if (bean != null && !bean.getProp1().equals("prop1"))
      agentS = bean.getProp1();
    return agentS;
  }

  public static String getDisplayName(String agentS){
    if ("admin".equals(agentS))
      return "Administrator";
    else if (agentS.equals("rachel"))
      return "Rachel Gollub";
    else if (agentS.equals("marith"))
      return "Margaret Petit";
    else
      return "Dr. Who";
  }

  public String getFirstName()
  {
    if ("admin".equals(agentString))
      return "Samigo";
    else if (agentString.equals("rachel"))
      return "Rachel";
    else if (agentString.equals("marith"))
      return "Margaret";
    else
      return "Dr.";
  }

  public String getLastName()
  {
    if ("admin".equals(agentString))
      return "Administrator";
    else if (agentString.equals("rachel"))
      return "Gollub";
    else if (agentString.equals("marith"))
      return "Petit";
    else
      return "Who";
  }

  public String getRole()
  {
    return "Student";
  }

  public static String getRole(String agentId)
  {
    return "Maintain";
  }

  public static String getCurrentSiteId(){
    return "Samigo Site";
  }

  public static String getCurrentSiteName(){
    return "Samigo Site";
  }

  public static String getSiteName(String siteId){
    return "Samigo Site";
  }

  public String getIdString()
  {
    return agentString;
  }

  public static String getDisplayNameByAgentId(String agentId){
    return "Samigo Administrator";
  }

  public static void createAnonymous(){
    BackingBean bean = (BackingBean) ContextUtil.lookupBean("backingbean");
    bean.setProp1("anonymous_"+(new java.util.Date()).getTime());
  }

  public static boolean isStandaloneEnvironment(){
    return true;
  }

  public static boolean isIntegratedEnvironment(){
    return !isStandaloneEnvironment();
  }

  public static String getCurrentSiteIdFromExternalServlet(HttpServletRequest req,  HttpServletResponse res){
      return "Samigo Site";
  }
 }
