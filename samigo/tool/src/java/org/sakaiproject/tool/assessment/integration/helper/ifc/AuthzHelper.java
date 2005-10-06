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
package org.sakaiproject.tool.assessment.integration.helper.ifc;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.hibernate.SessionFactory;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;

/**
 *
 * <p>Description:
 * This is a context implementation helper delegate interface for
 * the AuthzQueriesFacade class.  Using Spring injection via the
 * integrationContext.xml selected by the build process for the implementation.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 */

public interface AuthzHelper extends Serializable
{
  public boolean hasAdminPriviledge
    (String agentId, String function, String qualifier);

  public boolean isAuthorized
    (String agentId, String function, String qualifier);

  public AuthorizationData createAuthorization
    (String agentId, String functionId, String qualifierId);

  public void removeAuthorizationByQualifier(String qualifierId);

  public HashMap getAuthorizationToViewAssessments(String agentId);

  public List getAuthorizationByAgentAndFunction(String agentId,
                                                 String functionId);

  public List getAuthorizationByFunctionAndQualifier(String functionId,
    String qualifierId);

  public boolean checkMembership(String siteId);

  public boolean checkAuthorization(final String agentId,
                                    final String functionId,
                                    final String qualifierId);

  public ArrayList getAssessments(final String agentId, final String functionId);

  public ArrayList getAssessmentsByAgentAndFunction(final String agentId,
    final String functionId);

  public SessionFactory getSessionFactory();

  public void setSessionFactory(SessionFactory sessionFactory);

}
