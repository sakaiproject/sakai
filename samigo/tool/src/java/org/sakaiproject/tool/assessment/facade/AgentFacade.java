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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.ifc.shared.AgentDataIfc;
import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.ui.bean.shared.BackingBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
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

  public AgentFacade(String agentId)
  {
    agent = new AgentImpl(agentId, null, new IdImpl(agentId));
    agentString = agentId;
  }

  public static AgentImpl getAgent()
  {
    return helper.getAgent();
  }

  public static String getAgentString()
  {
    return helper.getAgentString();
  }

  public static String getAgentString(HttpServletRequest req,
                                      HttpServletResponse res)
  {
    return helper.getAgentString(req, res);
  }

  public static String getDisplayName(String agentS)
  {
    return helper.getDisplayName(agentS);
  }

  public String getDisplayName()
  {
    return helper.getDisplayName(this.agentString);
  }

  public String getFirstName()
  {
    return helper.getFirstName(agentString);
  }

  public String getLastName()
  {
    return helper.getLastName(agentString);
  }

  /**
 * Called by AgentFacade from an instance.
 * @param agentString the agent string for current AgentFacade instance
 * @return role string
 */
  public String getRole()
  {
    return helper.getRoleForCurrentAgent(agentString);
  }

  public static String getRole(String agentString)
  {
    return helper.getRole(agentString);
  }

  public static String getCurrentSiteId()
  {
    return helper.getCurrentSiteId();
  }

  public static String getCurrentSiteName()
  {
    return helper.getCurrentSiteName();
  }

  public static String getSiteName(String siteId)
  {
    return helper.getSiteName(siteId);
  }

  public String getIdString()
  {
    return agentString;
  }


  public static String getDisplayNameByAgentId(String agentId)
  {
    return helper.getDisplayNameByAgentId(agentId);
  }

  public static String createAnonymous()
  {
    return helper.createAnonymous();
  }

  public static boolean isStandaloneEnvironment()
  {
    return!integrated;
  }

  public static boolean isIntegratedEnvironment()
  {
    return integrated;
  }

  public static String getCurrentSiteIdFromExternalServlet(HttpServletRequest
    req, HttpServletResponse res)
  {
    return helper.getCurrentSiteIdFromExternalServlet(req, res);
  }

  public static String getAnonymousId()
  {
    return helper.getAnonymousId();
  }

  public void setIdString(String idString)
  {
    agentString = idString;
  }

  /**
   * Call unit test.
   * @param args ignored
   */
  public static void main(String[] args)
  {
    unitTest();
  }

  /**
   * Unit test can be run from command line.
   * Needs integrationContext.xml in classpath at org.sakaiproject.spring
   * Some tests for integrated version require web context and are bypassed.
   */
  public static void unitTest()
  {
    String bypassing = "running integrated version standalone, bypassing: ";
    String testing = "testing: ";
    System.out.println("AgentFacade.isIntegratedEnvironment()=" +
                       AgentFacade.isIntegratedEnvironment());
    System.out.println("AgentFacade.isStandaloneEnvironment()=" +
                       AgentFacade.isStandaloneEnvironment());
    System.out.println("AgentFacade.getAgent()=" + AgentFacade.getAgent());
    System.out.println(testing + "AgentFacade.createAnonymous()="+AgentFacade.createAnonymous());

    if (integrated) // require web context for integrated
    {
      System.out.println(bypassing + "AgentFacade.getAgentString()");
      System.out.println(bypassing + "AgentFacade.getAgentString(null, null);");
      System.out.println(bypassing + "AgentFacade.getAnonymousId()");
      System.out.println(bypassing + "AgentFacade.getCurrentSiteId()");
      System.out.println(bypassing +
        "AgentFacade.getCurrentSiteIdFromExternalServlet(null, null)");
      System.out.println(bypassing + "AgentFacade.getCurrentSiteName()");
      System.out.println(bypassing + "AgentFacade.getDisplayName(\"rgollub\"");
      System.out.println(bypassing +
                         "AgentFacade.getDisplayNameByAgentId(\"rgollub\"");
      System.out.println(bypassing + "AgentFacade.getSiteName(\"rgollub\")");
      System.out.println(bypassing + "AgentFacade.getRole(\"rgollub\"");
    }
    else
    {
      System.out.println(testing + "AgentFacade.getAgentString()=" +
                         AgentFacade.getAgentString());
      System.out.println(testing + "AgentFacade.getAgentString(null, null)=" +
                         AgentFacade.getAgentString(null, null));
      System.out.println(testing + "AgentFacade.getAnonymousId()="+AgentFacade.getAnonymousId());
      System.out.println(testing + "AgentFacade.getCurrentSiteId()=" +
                         AgentFacade.getCurrentSiteId());
      System.out.println(testing +
        "AgentFacade.getCurrentSiteIdFromExternalServlet(null, null)=" +
                         AgentFacade.getCurrentSiteIdFromExternalServlet(null, null));
      System.out.println(testing + "AgentFacade.getCurrentSiteName()=" +
                         AgentFacade.getCurrentSiteName());
      System.out.println(testing + "AgentFacade.getDisplayName(\"rachel\")=" +
                         AgentFacade.getDisplayName("rachel"));
      System.out.println(testing +
                         "AgentFacade.getDisplayNameByAgentId(\"rachel\")=" +
                         AgentFacade.getDisplayNameByAgentId("rachel"));
      System.out.println(testing + "AgentFacade.getSiteName(\"rachel\")=" +
                         AgentFacade.getSiteName("rachel"));
      System.out.println("AgentFacade.getRole(\"rachel\")="+AgentFacade.getRole("rachel"));
    }

    // require instance
    AgentFacade agent = new AgentFacade("rachel");
    System.out.println(testing + "created: agent=" + agent);
    System.out.println(testing + "agent.getIdString()=" + agent.getIdString());

    if (integrated) // require web context for integrated
    {
      System.out.println(bypassing + "agent.getFirstName()");
      System.out.println(bypassing + "agent.getLastName()");
      System.out.println(bypassing + "agent.getRole()");
    }
    else
    {
      System.out.println(testing + "agent.getFirstName()=" + agent.getFirstName());
      System.out.println(testing + "agent.getLastName()=" + agent.getLastName());
      System.out.println(testing + "agent.getRole()=" + agent.getRole());
    }
  }

}
