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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper;

public class AuthzHelperImpl implements AuthzHelper
{
  private static Log log = LogFactory.getLog(AuthzHelperImpl.class);

  public boolean isAuthorized(String agentId, String function, String qualifier)
  {

    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method isAuthorized() not yet implemented.");
  }

  public AuthorizationData createAuthorization(String agentId,
                                               String functionId,
                                               String qualifierId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method createAuthorization() not yet implemented.");
  }

  public void removeAuthorizationByQualifier(String qualifierId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method removeAuthorizationByQualifier() not yet implemented.");
  }

  public HashMap getAuthorizationToViewAssessments(String agentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method getAuthorizationToViewAssessments() not yet implemented.");
  }

  public List getAuthorizationByAgentAndFunction(String agentId,
                                                 String functionId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method getAuthorizationByAgentAndFunction() not yet implemented.");
  }

  public List getAuthorizationByFunctionAndQualifier(String functionId,
    String qualifierId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method getAuthorizationByFunctionAndQualifier() not yet implemented.");
  }

  public boolean checkMembership(String siteId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method checkMembership() not yet implemented.");
  }

  public boolean checkAuthorization(String agentId, String functionId,
                                    String qualifierId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method checkAuthorization() not yet implemented.");
  }

  public ArrayList getAssessments(String agentId, String functionId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method getAssessments() not yet implemented.");
  }

  public ArrayList getAssessmentsByAgentAndFunction(String agentId,
    String functionId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method getAssessmentsByAgentAndFunction() not yet implemented.");
  }
}