/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/tool/src/java/org/sakaiproject/tool/assessment/facade/AuthzQueriesFacade.java $
* $Id: AuthzQueriesFacade.java 2422 2005-10-06 21:23:02Z daisyf@stanford.edu $
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
package org.sakaiproject.tool.assessment.facade.authz.standalone;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;

/**
 * <p>Description: Facade for AuthZ queries, standalone version.
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class AuthzQueriesFacade
          extends HibernateDaoSupport implements AuthzQueriesFacadeAPI
{
  private static Log log = LogFactory.getLog(AuthzQueriesFacade.class);

  // stores sql strings
  private static ResourceBundle res = ResourceBundle.getBundle(
    "org.sakaiproject.tool.assessment.facade.authz.resource.AuthzResource");

  public boolean hasPrivilege(String functionName)
  {
    return true;
  }

  /**
   * Is the agent {agentId} authorized to do {function} to {qualifier}?
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @param qualifierId the target of the function.
   * @return true if authorized (always in standalone)
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
  public void removeAuthorizationByQualifier(String qualifierId) {
    String query =
      res.getString("select_authdata_q_id") + qualifierId + "'";
    log.info("query=" + query);

    List l = getHibernateTemplate().find(query);
    getHibernateTemplate().deleteAll(l);
  }

  /** This returns a HashMap containing
   * (String a.qualiferId, AuthorizationData a)
   * agentId is a site for now but can be a user
   *
   * @param agentId the agent id
   * @return HashMap containing qualiferId, AuthorizationData
   */
  public HashMap getAuthorizationToViewAssessments(String agentId) {
    HashMap h = new HashMap();
    List l =
      getAuthorizationByAgentAndFunction(agentId, res.getString("VIEW_PUB"));
    for (int i = 0; i < l.size(); i++)
    {
      AuthorizationData a = (AuthorizationData) l.get(i);
      h.put(a.getQualifierId(), a);
    }
    return h;
  }

  /**
   * This returns a HashMap containing authorization data.
   * @param agentId is a site for now but can be a user
   * @param functionId the function to be performed.
   * @return HashMap containing (String a.qualiferId, AuthorizationData a)
   */
  public List getAuthorizationByAgentAndFunction(String agentId, String functionId) {
    try
    {
      String query = res.getString("select_authdata_a_id") +
        agentId + res.getString("and_f_id") + functionId + "'";
      log.info("query=" + query);

      List list = getHibernateTemplate().find(query);
      log.info("list="+list);
      return list;
    }
    catch (DataAccessException ex)
    {
      log.warn("getAuthorizationByAgentAndFunction "+ ex);
      return new ArrayList();//empty
    }
  }

  /**
   * Get authorization list by qualifier and function.
   * @param functionId the function to be performed.
   * @param qualifierId the target of the function.
   * @return the list of authorizations.
   */
  public List getAuthorizationByFunctionAndQualifier(String functionId, String qualifierId) {
    try
    {
      String query =
        res.getString("select_authdata_f_id") + functionId +
        res.getString("and_q_id") + qualifierId + "'";
      log.info("query=" + query);

      List list = getHibernateTemplate().find(query);
      System.out.println("list="+list);
      return list;
    }
    catch (DataAccessException ex)
    {
      log.warn("getAuthorizationByAgentAndFunction "+ ex);
      return new ArrayList();//empty
    }
  }

  /**
   * Check if member of site.
   * @param siteId the site id
   * @return true if a member.
   */
  public boolean checkMembership(String siteId)
  {
    return true;
  }

///////////////////////////////////////////////////////////////////////////////
// The following methods are not implemented in the standalone version of
// AuthzQueriesFacade.  Currently they are not used in the standalone context,
// so they are left throwing UnsupportedOperationExceptions.
//
// If required for standalone context at some point in the future, you'll
// need to implement these, and also should add them to AuthzQueriesFacadeAPI.
//
///////////////////////////////////////////////////////////////////////////////

 /**
  * UNIMPLEMENTED.
  * Check the agent {agentId} authorized to do {function} to {qualifier}?
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
   * Warning. Oddly named method.  Just following the convention.
   * Actually using select from ...AuthorizationData...
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
   * UNIMPLEMENTED.
   *
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
