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
    assertNotNull(actualReturn);
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
    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      assertEquals("return value", expectedReturn, actualReturn);
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
    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      agentId = "rachel";
      expectedReturn = "Samigo Administrator";
      actualReturn = agentHelper.getDisplayNameByAgentId(agentId);
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetFirstName()
  {
    String agentString = null;
    String expectedReturn = null;
    String actualReturn = agentHelper.getFirstName(agentString);
//    assertEquals("return value", expectedReturn, actualReturn);
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
    String actualReturn = agentHelper.getRole(agentString);
    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      agentString = "rachel";
      expectedReturn = "Maintain";
      actualReturn = agentHelper.getRole(agentString);
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetRoleForCurrentAgent()
  {
    String agentString = null;
    String expectedReturn = null;
    String actualReturn = agentHelper.getRoleForCurrentAgent(agentString);
    assertNotNull(actualReturn);
    if (agentHelper.isStandaloneEnvironment())
    {
      agentString = "rachel";
      expectedReturn = "Student";
      actualReturn = agentHelper.getRoleForCurrentAgent(agentString);
      assertEquals("return value", expectedReturn, actualReturn);
    }
  }

  public void testGetSiteName()
  {
    String siteId = null;
    String expectedReturn = null;
    String actualReturn = agentHelper.getSiteName(siteId);
//    assertEquals("return value", expectedReturn, actualReturn);
    assertNotNull(actualReturn);
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
