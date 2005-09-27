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
package org.sakaiproject.tool.assessment.integration.helper.standalone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import java.util.ResourceBundle;

/**
 *
 * <p>Description:
 * This is a stub standalone context implementation helper delegate class for
 * the AuthzQueriesFacade class.  Three methods are unimplemented.
 * "Standalone" means that Samigo (Tests and Quizzes)
 * is running without the context of the Sakai portal and authentication
 * mechanisms, and therefore we leave some unused methods unimplemented.</p>
 * <p>Note: To customize behavior you can add your own helper class to the
 * Spring injection via the integrationContext.xml for your context.
 * The particular integrationContext.xml to be used is selected by the
 * build process.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */

public class AuthzHelperImpl extends HibernateDaoSupport implements AuthzHelper
{
  static ResourceBundle res = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.integration.helper.standalone.AuthzResource");
  private static Log log = LogFactory.getLog(AuthzHelperImpl.class);

  /**
   * Is the agent {agentId} authorized to do {function} to {qualifier}?
   * @param agentId the agent id.
   * @param function the function to be performed.
   * @param qualifier the target of the function.
   * @return true if authorized, false otherwise.
   */
  public boolean isAuthorized
    (String agentId, String function, String qualifier)
  {
    return true;
  }

  /**
   * Create an authorization record for {agentId} authorized to do {function}
   * to {qualifier}
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @param qualifierId the target of the function.
   * @return the authorization data
   */
  public AuthorizationData createAuthorization
    (String agentId, String functionId, String qualifierId)
  {
    AuthorizationData a = new AuthorizationData(
      agentId, functionId, qualifierId,
      new Date(), new Date(),
      AgentFacade.getAgentString(), new Date(), Boolean.TRUE);
    getHibernateTemplate().save(a);
    return a;
  }

  /**
   * Remove authorization from qualifier (target).
   * @param qualifierId the target.
   */
  public void removeAuthorizationByQualifier(String qualifierId)
  {
    List l = getHibernateTemplate().find(
      res.getString("select_authdata_w_qual") + qualifierId);
    getHibernateTemplate().deleteAll(l);
  }

  /**
   * This returns a HashMap containing authorization data.
   * @param agentId is a site for now but can be a user
   * @return HashMap containing (String a.qualiferId, AuthorizationData a)
   */
  public HashMap getAuthorizationToViewAssessments(String agentId)
  {
    HashMap h = new HashMap();
    List l =
      getAuthorizationByAgentAndFunction(agentId, res.getString("view_pub"));
    for (int i = 0; i < l.size(); i++)
    {
      AuthorizationData a = (AuthorizationData) l.get(i);
      h.put(a.getQualifierId(), a);
    }
    return h;
  }

  /**
   * Get authorization list by agent and function.
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @return the list of authorizations.
   */
  public List getAuthorizationByAgentAndFunction(String agentId,
                                                 String functionId)
  {
    String query = res.getString("select_authdata_w_agent") +
      agentId + res.getString("and_funid") + functionId + "'";
    log.debug("query=" + query);
    return getHibernateTemplate().find(query);
  }

  /**
   * Get authorization list by qualifier and function.
   * @param functionId the function to be performed.
   * @param qualifierId the target of the function.
   * @return the list of authorizations.
   */
  public List getAuthorizationByFunctionAndQualifier(String functionId,
    String qualifierId)
  {
    return getHibernateTemplate().find(
      res.getString("select_authdata_w_fun") + functionId +
      res.getString("and_qid") + qualifierId + "'");
  }

  /**
   * Check if member of site.
   * @param siteId
   * @return true--always
   */
  public boolean checkMembership(String siteId)
  {
    return true;
  }

///////////////////////////////////////////////////////////////////////////////
// The following methods are not implemented in the standalone version of
// AuthZ.  Currently they are not used in the standalone context, so they
// are left throwing UnsupportedOperationExceptions.
///////////////////////////////////////////////////////////////////////////////

  /**
   * UNIMPLEMENTED.
   * @todo If required for standalone context at some point in the future,
   * you'll need to implement this
   * org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @param qualifierId the target of the function.
   * @return --throw UnsupportedOperationException
   */
  public boolean checkAuthorization(String agentId, String functionId,
                                    String qualifierId)
  {
    throw new java.lang.UnsupportedOperationException(
      "Method checkAuthorization() not yet implemented for standalone context.");
  }

  /**
   * UNIMPLEMENTED.
   *
   * @todo If required for standalone context at some point in the future,
   * you'll need to implement this
   * org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @return --throw UnsupportedOperationException
   */
  public ArrayList getAssessments(String agentId, String functionId)
  {
    throw new java.lang.UnsupportedOperationException(
      "Method getAssessments() not yet implemented for standalone context.");
  }

  /**
   * @todo If required for standalone context at some point in the future,
   * you'll need to implement this org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper method
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @return --throw UnsupportedOperationException
   */
  public ArrayList getAssessmentsByAgentAndFunction(String agentId,
    String functionId)
  {
    throw new java.lang.UnsupportedOperationException(
      "Method getAssessmentsByAgentAndFunction() not yet implemented for standalone context.");
  }
}
