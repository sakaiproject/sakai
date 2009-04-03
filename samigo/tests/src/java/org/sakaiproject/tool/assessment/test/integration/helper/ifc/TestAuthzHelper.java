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
import java.util.*;
import org.sakaiproject.tool.assessment.data.dao.authz.*;

public class TestAuthzHelper extends TestCase {
  private AuthzHelper authzHelper = null;
  boolean integrated;

  public TestAuthzHelper( AuthzHelper helper,
                             boolean integrated) {
    this.authzHelper = helper;
    this.integrated = integrated;
    assertNotNull(authzHelper);
  }


  protected void setUp() throws Exception {
    super.setUp();
    /**@todo verify the constructors*/
    authzHelper = null;
  }

  protected void tearDown() throws Exception {
    authzHelper = null;
    super.tearDown();
  }

  // I don't like this, this method seems to want to return null on find fail
  public void testGetAuthorizationByAgentAndFunction() {
    String agentId = "bogus";//a bogus agent
    String functionId = "bogus";//a bogus function, change if used

    List actualReturn = null;
    try
    {
      actualReturn = authzHelper.getAuthorizationByAgentAndFunction(
        agentId, functionId);
    }
    catch (Exception ex)
    {
      log.warn(ex.getMessage());
    }
    assertNotNull(actualReturn);
    assertEquals(actualReturn.size(), 0);
  }

  public void testCheckAuthorization() {
    String agentId = "esmiley";
    String functionId = "update";
    String qualifierId = "wombats";
    boolean actualReturn = true;
    boolean unimplemented = false;
    try
    {
      actualReturn = authzHelper.checkAuthorization(agentId, functionId, qualifierId);    }
    catch (java.lang.UnsupportedOperationException ex)
    {
      unimplemented = true;
    }

    if (isStandalone())
    {
      assertTrue(unimplemented);
    }
    if (this.isIntegrated())
    {
      assertFalse(actualReturn);
    }
  }

  public void testGetAuthorizationByFunctionAndQualifier() {
    String qualifierId = "a bogus q string";
    String functionId = "a bogus function";

    List actualReturn = null;
    try
    {
      actualReturn =
        authzHelper.getAuthorizationByFunctionAndQualifier(functionId,
        qualifierId);
    }
    catch (Exception ex)
    {
      log.warn(ex.getMessage());
    }

    assertNotNull(actualReturn);
    assertEquals(actualReturn.size(), 0);
  }

  public void testCheckMembership() {
    String siteId = "a bogus nonexistent site";
    boolean actualReturn = authzHelper.checkMembership(siteId);

    if (isStandalone())
    {
      assertTrue(actualReturn);
    }
    if (this.isIntegrated())
    {
      this.assertFalse(actualReturn);
    }
  }

  public void testGetAssessmentsByAgentAndFunction() {
    String agentId = "esmiley";
    String functionId = "update";
    ArrayList actualReturn = null;
    boolean unimplemented = false;
    try
    {
      actualReturn = authzHelper.getAssessments(agentId, functionId);
    }
      catch (java.lang.UnsupportedOperationException ex)
    {
      unimplemented = true;
    }

    if (isStandalone())
    {
      assertTrue(unimplemented);
    }
    if (this.isIntegrated())
    {
      assertNotNull(actualReturn);
      assertEquals("return value", 0, actualReturn.size());
    }
  }

  public void testRemoveAuthorizationByQualifier() {
    boolean success = false;
    try
    {
      String qualifierId = "this is a non-existent bogus qualifier";
      authzHelper.removeAuthorizationByQualifier(qualifierId);
      success = true;
    }
    catch (Exception ex)
    {
      log.warn(ex.getMessage());
    }
    assertTrue(success);
  }

  public void testCreateAuthorization() {
    String agentId = null;
    String functionId = null;
    String qualifierId = null;
    AuthorizationData expectedReturn = null;
    AuthorizationData actualReturn = null;
    boolean createdWithNulls = false; // should NEVER happen, in stand. OR int.
    try
    {
      actualReturn = authzHelper.createAuthorization(agentId, functionId,
        qualifierId);
      createdWithNulls = true; //UH-OH
    }
    catch (Exception ex)
    {
      log.warn(ex.getMessage());
    }
    assertEquals("return value", expectedReturn, actualReturn);
    assertFalse(createdWithNulls);
  }

  public void testGetAssessments() {
    ArrayList expectedReturn = null;
    String agentId = "esmiley";
    String functionId = "update";
    ArrayList actualReturn = null;
    boolean unimplemented = false;
    try
    {
      actualReturn = authzHelper.getAssessments(agentId, functionId);
    }
      catch (java.lang.UnsupportedOperationException ex)
    {
      unimplemented = true;
    }

    if (isStandalone())
    {
      assertTrue(unimplemented);
    }
    if (this.isIntegrated())
    {
      assertNotNull(actualReturn);
      assertEquals("return value", 0, actualReturn.size());
    }
  }

  public void testIsAuthorized() {
    String agentId = "esmiley";
    String functionId = "update";
    String qualifierId = "wombats";
    boolean actualReturn = authzHelper.isAuthorized(agentId, functionId, qualifierId);
    if (isStandalone())
    {
      assertTrue(actualReturn);
    }
    if (this.isIntegrated())
    {
      assertFalse(actualReturn);
    }
  }

  public void testGetAuthorizationToViewAssessments() {
    HashMap expectedReturn = null;
    String agentId = "a bogus agent string";

    HashMap actualReturn = authzHelper.getAuthorizationToViewAssessments(agentId);

    assertNotNull(actualReturn);
    assertEquals("return value", 0, actualReturn.size());
//    assertNotNull(actualReturn);
//assertEquals(actualReturn.size(), 0);

  }

  public boolean isIntegrated()
  {
    return integrated;
  }
  public boolean isStandalone()
  {
    return !integrated;
  }

}
