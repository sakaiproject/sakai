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
package org.sakaiproject.tool.assessment.test.integration.context;

import junit.framework.*;
import org.springframework.test.*;

import org.sakaiproject.tool.assessment.integration.context.*;
import org.sakaiproject.tool.assessment.integration.helper.ifc.*;
import org.sakaiproject.tool.assessment.test.integration.helper.ifc.TestAgentHelper;
import org.sakaiproject.tool.assessment.test.integration.helper.ifc.TestGradebookHelper;
import org.sakaiproject.tool.assessment.test.integration.helper.ifc.TestPublishingTargetHelper;
import org.sakaiproject.tool.assessment.test.integration.helper.ifc.TestAuthzHelper;
import org.sakaiproject.tool.assessment.test.integration.helper.ifc.TestGradebookServiceHelper;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class TestIntCtxtFactoryMethods extends TestCase {
  private IntegrationContextFactory integrationContextFactory = null;

  public TestIntCtxtFactoryMethods(IntegrationContextFactory factory) {
    this.integrationContextFactory = factory;
  }


  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testGetAgentHelper() {
  AgentHelper actualReturn = integrationContextFactory.getAgentHelper();
  System.out.println("testGetAgentHelper="+actualReturn);
  assertNotNull(actualReturn);
  System.out.println("    *** testing agent helper methods");
  TestAgentHelper testAgent =  new TestAgentHelper(actualReturn);
  System.out.println("testCreateAnonymous()");
  testAgent.testCreateAnonymous();
  System.out.println("testGetAgent()");
  testAgent.testGetAgent();
  System.out.println("testGetAgentString()");
  testAgent.testGetAgentString();
  System.out.println("testGetAgentString_RequestResponse()");
  testAgent.testGetAgentString_RequestResponse();
  System.out.println("testGetAnonymousId()");
  testAgent.testGetAnonymousId();
  System.out.println("testGetCurrentSiteId()");
  testAgent.testGetCurrentSiteId();
  System.out.println("testGetCurrentSiteIdFromExternalServlet()");
  testAgent.testGetCurrentSiteIdFromExternalServlet();
  System.out.println("testGetCurrentSiteName()");
  testAgent.testGetCurrentSiteName();
  System.out.println("testGetDisplayName()");
  testAgent.testGetDisplayName();
  System.out.println("testGetDisplayNameByAgentId()");
  testAgent.testGetDisplayNameByAgentId();
  System.out.println("testGetFirstName()");
  testAgent.testGetFirstName();
  System.out.println("testGetLastName()");
  testAgent.testGetLastName();
  System.out.println("testGetRole()");
  testAgent.testGetRole();
  System.out.println("testGetRoleForCurrentAgent()");
  testAgent.testGetRoleForCurrentAgent();
  System.out.println("testGetSiteName()");
  testAgent.testGetSiteName();
  System.out.println("testIsIntegratedEnvironment()");
  testAgent.testIsIntegratedEnvironment();
  System.out.println("testIsStandaloneEnvironment()");
  testAgent.testIsStandaloneEnvironment();

  System.out.println("    *** complete: testing agent helper methods");
}

// test this first, it is used below
public void testIsIntegrated() {
  boolean expectedReturn = false;
  boolean actualReturn = integrationContextFactory.isIntegrated();
  System.out.println("verified: testIsIntegrated="+actualReturn);
}

public void testGetAuthzHelper() {
  AuthzHelper actualReturn = integrationContextFactory.getAuthzHelper();
  assertNotNull(actualReturn);
  System.out.println("testGetAuthzHelper="+actualReturn);
  System.out.println("    *** testing gradebook helper methods");
  TestAuthzHelper  testAuthz = new TestAuthzHelper(actualReturn,
    integrationContextFactory.isIntegrated());
  System.out.println("testCheckAuthorization()");
  testAuthz.testCheckAuthorization();
  System.out.println("testCheckMembership()");
  testAuthz.testCheckMembership();
  System.out.println("testCreateAuthorization()");
  testAuthz.testCreateAuthorization();
  System.out.println("testGetAssessments()");
  testAuthz.testGetAssessments();
  System.out.println("testGetAssessmentsByAgentAndFunction()");
  testAuthz.testGetAssessmentsByAgentAndFunction();
  System.out.println("testGetAuthorizationByAgentAndFunction()");
  testAuthz.testGetAuthorizationByAgentAndFunction();
  System.out.println("testGetAuthorizationByFunctionAndQualifier()");
  testAuthz.testGetAuthorizationByFunctionAndQualifier();
  System.out.println("testGetAuthorizationToViewAssessments()");
  testAuthz.testGetAuthorizationToViewAssessments();
  System.out.println("testRemoveAuthorizationByQualifier()");
  testAuthz.testRemoveAuthorizationByQualifier();
  System.out.println("testIsAuthorized()");
  testAuthz.testIsAuthorized();

  System.out.println("    *** complete: testing gradebook helper methods");
}

public void testGetGradeBookServiceHelper() {
  GradebookServiceHelper actualReturn = integrationContextFactory.getGradebookServiceHelper();
  assertNotNull(actualReturn);
  System.out.println("testGetGradeBookServiceHelper="+actualReturn);
  System.out.println("    *** testing gradebook service helper methods");
  TestGradebookServiceHelper  testGBS = new TestGradebookServiceHelper(actualReturn,
                             integrationContextFactory.isIntegrated());
  System.out.println("    *** testing gradebook service helper methods");
  System.out.println("testAddToGradebook");
  testGBS.testAddToGradebook();
  System.out.println("testGradebookExists");
  testGBS.testGradebookExists();
  System.out.println("testRemoveExternalAssessment");
  testGBS.testRemoveExternalAssessment();
  System.out.println("testUpdateExternalAssessmentScore");
  testGBS.testUpdateExternalAssessmentScore();
  System.out.println("    *** complete: testing gradebook service helper methods");
}

public void testGetGradebookHelper() {
  GradebookHelper actualReturn = integrationContextFactory.getGradebookHelper();
  assertNotNull(actualReturn);
  System.out.println("testGetGradebookHelper="+actualReturn);
  System.out.println("    *** testing gradebook helper methods");
  TestGradebookHelper  testGB = new TestGradebookHelper(actualReturn,
                             integrationContextFactory.isIntegrated());
  testGB.testGetDefaultGradebookUId();
  System.out.println("testGetDefaultGradebookUId()");
  testGB.testGetGradebookUId();
  System.out.println("testGetGradebookUId()");

  System.out.println("    *** complete: testing gradebook helper methods");
}

public void testGetPublishingTargetHelper() {
  PublishingTargetHelper actualReturn = integrationContextFactory.getPublishingTargetHelper();
  assertNotNull(actualReturn);
  System.out.println("testGetPublishingTargetHelper="+actualReturn);
  System.out.println("integrationContextFactory.isIntegrated()="+integrationContextFactory.isIntegrated());
  System.out.println("    *** testing publish helper methods");
  TestPublishingTargetHelper test = new TestPublishingTargetHelper(actualReturn,
                             integrationContextFactory.isIntegrated());
  System.out.println("testGetLog");
  test.testGetLog();
  System.out.println("testGetSiteService");
  test.testGetSiteService();
  System.out.println("testGetTargets");
  test.testGetTargets();
  System.out.println("testSetLog");
  test.testSetLog();
  System.out.println("testSetSiteService");
  test.testSetSiteService();
  System.out.println("    *** complete: testing publish helper methods");
}




}
