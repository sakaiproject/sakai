/**********************************************************************************
 * $URL: $
 * $Id: $
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
 *
 **********************************************************************************/
package org.sakaiproject.tool.assessment.test.integration.helper.ifc;

import junit.framework.*;
import org.sakaiproject.tool.assessment.integration.helper.ifc.*;
import org.sakaiproject.tool.assessment.osid.shared.impl.*;
import javax.servlet.http.*;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class TestAgentHelper extends TestCase
{
  private AgentHelper agentHelper = null;

  public TestAgentHelper(AgentHelper helper)
  {
    agentHelper = helper;
  }

  protected void setUp() throws Exception
  {
    super.setUp();
    agentHelper = null;
  }

  protected void tearDown() throws Exception
  {
    agentHelper = null;
    super.tearDown();
  }

  public void testCreateAnonymous()
  {
    String expectedReturnStartsWith = "anonymous_";
    String actualReturn = agentHelper.createAnonymous();
    this.assertNotNull(actualReturn);
    assertTrue(actualReturn.startsWith(expectedReturnStartsWith));
  }

  public void testGetAgent()
  {
    AgentImpl actualReturn = agentHelper.getAgent();
    assertNotNull(actualReturn);
  }

  public void testGetAgentString()
  {
    String expectedReturn = "admin";
    String actualReturn = null;
    try
    {
      actualReturn = agentHelper.getAgentString();
    }
    catch (NoSuchBeanDefinitionException ex)
    {
      if (agentHelper.isIntegratedEnvironment())
      {
        return;// this means that it is going looking for Component Manager, OK
      }
    }
    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetAgentString_RequestResponse()
  {
    HttpServletRequest req = null;
    HttpServletResponse res = null;
    String expectedReturn = "admin";
    String actualReturn = agentHelper.getAgentString(req, res);
    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetAnonymousId()
  {
    String expectedReturn = "";
    String actualReturn = agentHelper.getAnonymousId();
    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetCurrentSiteId()
  {
    String expectedReturn = "Samigo Site";
    String actualReturn = agentHelper.getCurrentSiteId();
    if (agentHelper.isStandaloneEnvironment())
    {
      assertNotNull(actualReturn);
      assertEquals("return value", expectedReturn, actualReturn);
    }
    if (agentHelper.isStandaloneEnvironment())
    {
      assertNull(actualReturn);// because Deliverybean not avail in unit test
    }
  }

  public void testGetCurrentSiteIdFromExternalServlet()
  {
    HttpServletRequest req = null;
    HttpServletResponse res = null;
    String expectedReturn = "Samigo Site";
    String actualReturn = agentHelper.getCurrentSiteIdFromExternalServlet(req,
      res);
    if (agentHelper.isStandaloneEnvironment())
    {
      assertNotNull(actualReturn);
      assertEquals("return value", expectedReturn, actualReturn);
    }
    if (agentHelper.isStandaloneEnvironment())
    {
      assertNull(actualReturn);// because Deliverybean not avail in unit test
    }
  }

  public void testGetCurrentSiteName()
  {
    String expectedReturn = "Samigo Site";
    String actualReturn = agentHelper.getCurrentSiteName();
    if (agentHelper.isStandaloneEnvironment())
    {
      assertEquals("return value", expectedReturn, actualReturn);
    }
    if (agentHelper.isIntegratedEnvironment())
    {
      assertNull(actualReturn);//delivery bean is not available in unit test
    }
  }

  public void testGetDisplayName()
  {
    String agentS = null;
    String actualReturn = agentHelper.getDisplayName(agentS);
    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      String expectedReturn = "Dr. Who";
      assertEquals("return value", expectedReturn, actualReturn);
      agentS = "rachel";
      expectedReturn = "Rachel Gollub";
      actualReturn = agentHelper.getDisplayName(agentS);
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetDisplayNameByAgentId()
  {
    String agentId = null;
    String expectedReturn = null;
    String actualReturn = agentHelper.getDisplayNameByAgentId(agentId);
    if (agentHelper.isStandaloneEnvironment())
    {
      agentId = "rachel";
      expectedReturn = "Samigo Administrator";
      actualReturn = agentHelper.getDisplayNameByAgentId(agentId);
      assertNotNull(actualReturn);
      assertEquals("return value", expectedReturn, actualReturn);
    }
    if (agentHelper.isIntegratedEnvironment())
    {
      assertNull(actualReturn);// component manager not avial in unit test
    }
  }

  public void testGetFirstName()
  {
    String agentString = null;
    String expectedReturn = null;
    String actualReturn = agentHelper.getFirstName(agentString);

    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      agentString = "rachel";
      expectedReturn = "Rachel";
      actualReturn = agentHelper.getFirstName(agentString);
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetLastName()
  {
    String agentString = null;
    String expectedReturn = null;
    String actualReturn = agentHelper.getLastName(agentString);
    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      agentString = "rachel";
      expectedReturn = "Gollub";
      actualReturn = agentHelper.getLastName(agentString);
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetRole()
  {
    String agentString = null;
    String expectedReturn = null;
    String actualReturn = null;
    String expectedReturnIntegrated = "anonymous_access";
    if (agentHelper.isStandaloneEnvironment())
    {
      agentString = "rachel";
      expectedReturn = "Maintain";
      actualReturn = agentHelper.getRole(agentString);
      assertNotNull(actualReturn);
      assertEquals("return value", expectedReturn, actualReturn);
    }

    //tool manager unavailable in unit test, so returns default
    if (agentHelper.isIntegratedEnvironment())
    {
      agentString = "rachel";
      expectedReturn = "anonymous_access";
      actualReturn = agentHelper.getRole(agentString);
      assertNotNull(actualReturn);
      assertEquals("return value", expectedReturn, actualReturn);
    }

  }

  public void testGetRoleForCurrentAgent()
  {
    String agentString = null;
    String expectedReturn = null;
    String actualReturn = agentHelper.getRoleForCurrentAgent(agentString);
    if (agentHelper.isStandaloneEnvironment())
    {
      agentString = "rachel";
      expectedReturn = "Student";
      actualReturn = agentHelper.getRoleForCurrentAgent(agentString);
      assertNotNull(actualReturn);
      assertEquals("return value", expectedReturn, actualReturn);
    }
    //tool manager unavailable in unit test, so returns default
    if (agentHelper.isIntegratedEnvironment())
    {
      agentString = "rachel";
      expectedReturn = "anonymous_access";
      actualReturn = agentHelper.getRole(agentString);
      assertNotNull(actualReturn);
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetSiteName()
  {
    String siteId = null;
    String expectedReturn = null;
    String actualReturn = agentHelper.getSiteName(siteId);

    if (agentHelper.isStandaloneEnvironment())
    {
      assertNotNull(actualReturn);
    }
    if (agentHelper.isIntegratedEnvironment())
    {
      assertNull(actualReturn);//SiteService not avail in unit test
    }

  }

  public void testIsIntegratedEnvironment()
  {
    boolean expectedReturn = !agentHelper.isStandaloneEnvironment();
    boolean actualReturn = agentHelper.isIntegratedEnvironment();
    assertEquals("return value", expectedReturn, actualReturn);
  }

  public void testIsStandaloneEnvironment()
  {
    boolean expectedReturn = !agentHelper.isIntegratedEnvironment();
    boolean actualReturn = agentHelper.isStandaloneEnvironment();
    assertEquals("return value", expectedReturn, actualReturn);
  }

}
