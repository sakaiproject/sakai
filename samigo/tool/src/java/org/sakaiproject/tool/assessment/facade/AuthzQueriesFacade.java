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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper;

/**
 * <p>Description: Facade for AuthZ queries.
 * Uses helper to determine integration context implementation.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @author Ed Smiley <esmiley@stanford.edu> added helper delegate.
 */
public class AuthzQueriesFacade
          extends HibernateDaoSupport implements AuthzQueriesFacadeAPI
{
  private static Log log = LogFactory.getLog(AuthzQueriesFacade.class);
  private static final AuthzHelper helper =
    IntegrationContextFactory.getInstance().getAuthzHelper();
  private static final boolean integrated =
    IntegrationContextFactory.getInstance().isIntegrated();

  /**
   * Is the agent {agentId} authorized to do {function} to {qualifier}?
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @param qualifierId the target of the function.
   * @return true if authorized, false otherwise.
   */
  public  boolean isAuthorized
    (String agentId, String functionId, String qualifierId)
  {
  //    standalone version: return true;
  //    integrated version:
//      String query = "select a from AuthorizationData a where a.functionId=? and a.qualifierId=?";
//      List authorizationList = getHibernateTemplate().find(query,
//                               new Object[] { functionId, qualifierId },
//                               new net.sf.hibernate.type.Type[] { Hibernate.STRING, Hibernate.STRING });
//
//      String currentSiteId = null;
//      DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
//      if (!delivery.getAccessViaUrl())
//          currentSiteId = org.sakaiproject.service.framework.portal.cover.PortalService.getCurrentSiteId();
//      if(currentSiteId == null)
//    return false; // user don't login via any site if they are using published url
//
//      System.out.println("**** currentSiteId"+currentSiteId);
//      String currentAgentId = UserDirectoryService.getCurrentUser().getId();
//      for (int i=0; i<authorizationList.size(); i++){
//        AuthorizationData a = (AuthorizationData) authorizationList.get(i);
//        String siteId = a.getAgentIdString();
//        if (("AUTHENTICATED_USERS").equals(siteId) && (currentAgentId!=null)){
//          return true;
//        }
//        else if (("ANONYMOUS_USERS").equals(siteId)){
//          return true;
//        }
//        else if(currentSiteId.equals(siteId))
//        {
//          return true;
//        }
//      }
//      return false;

  return helper.isAuthorized(agentId, functionId, qualifierId);

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
  // original standalone code:
//    AuthorizationData a = new AuthorizationData(
//      agentId, functionId, qualifierId,
//      new Date(), new Date(),
//      AgentFacade.getAgentString(), new Date(), Boolean.TRUE);
//      getHibernateTemplate().save(a);
//      return a;
  // original integrated code:
//    if (agentId == null || functionId == null || qualifierId == null)
//{
//  throw new IllegalArgumentException("Null Argument");
//}
//else
//{
//  AuthorizationData ad = new AuthorizationData();
//
//  Calendar cal = Calendar.getInstance();
//  Date lastModifiedDate = cal.getTime();
//
//  ad.setAgentIdString(agentId);
//  ad.setFunctionId(functionId);
//  ad.setQualifierId(qualifierId);
//  ad.setLastModifiedBy("someone");
//  ad.setLastModifiedDate(lastModifiedDate);
//  getHibernateTemplate().save(ad);
//  return ad;
//}

      return helper.createAuthorization(agentId, functionId, qualifierId);
  }

  /**
   * Remove authorization from qualifier (target).
   * @param qualifierId the target.
   */
  public void removeAuthorizationByQualifier(String qualifierId) {
    String debugQuery =
      "select a from AuthorizationData a where a.qualifierId="+qualifierId;
    log.info("Should be removeing: " + debugQuery);
      helper.removeAuthorizationByQualifier(qualifierId);
  }

  /** This returns a HashMap containing (String a.qualiferId, AuthorizationData a)
   * agentId is a site for now but can be a user
   *
   * @param agentId the agent id
   * @return HashMap containing qualiferId, AuthorizationData
   */
  public HashMap getAuthorizationToViewAssessments(String agentId) {
    //original version:
//    HashMap h = new HashMap();
//    List l = getAuthorizationByAgentAndFunction(agentId, "VIEW_PUBLISHED_ASSESSMENT");
//    for (int i=0; i<l.size();i++){
//      AuthorizationData a = (AuthorizationData) l.get(i);
//      h.put(a.getQualifierId(), a);
//    }
//    return h;
    return helper.getAuthorizationToViewAssessments(agentId);
  }

  /**
   * This returns a HashMap containing authorization data.
   * @param agentId is a site for now but can be a user
   * @param functionId the function to be performed.
   * @return HashMap containing (String a.qualiferId, AuthorizationData a)
   */
  public List getAuthorizationByAgentAndFunction(String agentId, String functionId) {
    String debugQuery =
      "select a from AuthorizationData a where a.agentIdString='"+ agentId +
            "' and a.functionId='"+functionId+"'";
    log.info("Should be finding: " + debugQuery);

    return helper.getAuthorizationByAgentAndFunction(agentId, functionId);
  }

  /**
   * Get authorization list by qualifier and function.
   * @param functionId the function to be performed.
   * @param qualifierId the target of the function.
   * @return the list of authorizations.
   */
  public List getAuthorizationByFunctionAndQualifier(String functionId, String qualifierId) {
    String debugQuery =
        "select a from AuthorizationData a where a.functionId='"+ functionId +
        "' and a.qualifierId='"+qualifierId+"'";
    log.info("Should be finding: " + debugQuery);

    return helper.getAuthorizationByFunctionAndQualifier(functionId, qualifierId);
  }

  /**
   * Check if member of site.
   * @param siteId the site id
   * @return true if a member.
   */
  public boolean checkMembership(String siteId) {
    // standalone code: return true
    // integrated code:
//    boolean isMember = false;
//    try{
//      String realmName = "/site/" + siteId;
//      Realm siteRealm = RealmService.getRealm(realmName);
//      if (siteRealm.getUserRole(AgentFacade.getAgentString()) != null)
//        isMember = true;
//    }
//    catch(Exception e)
//    {
//      System.out.println(e.getMessage());
//    }
//    return isMember;

    return helper.checkMembership(siteId);
  }

  /**
 * Check the agent {agentId} authorized to do {function} to {qualifier}?
 * @param agentId the agent id.
 * @param functionId the function to be performed.
 * @param qualifierId the target of the function.
 * @return true if the agent checks out for function-> qualifier
 */
  public boolean checkAuthorization(String agentId, String functionId,
                                    String qualifierId)
  {
    // standalone code: doesn't exist
    // integrated code:
//    /*    if (agentId == null || functionId == null || qualifierId == null)
//    {
//      throw new IllegalArgumentException("Null Argument");
//    }*/
//    if (functionId == null || qualifierId == null)
//    {
//      throw new IllegalArgumentException("Null Argument");
//    }
//    final String queryAgentId = org.sakaiproject.service.framework.portal.cover.PortalService.getCurrentSiteId();
//
//    HibernateCallback hcb = new HibernateCallback()
//    {
//      public Object doInHibernate(Session session) throws HibernateException,
//          SQLException
//      {
//        Query query = session.createQuery(HQL_QUERY_CHECK_AUTHZ);
//        //query.setString("agentId", agentId);
//        if(agentId == null)
//          query.setString("agentId", queryAgentId);
//        else
//          query.setString("agentId", agentId);
//        query.setString("functionId", functionId);
//        query.setString("qualifierId", qualifierId);
//        return query.uniqueResult();
//        //return query.list();
//      }
//    };
//    Object result = (AuthorizationData)getHibernateTemplate().execute(hcb);
//
//    if(result != null)
//      return true;
//    else
//      return false;
//

    return helper.checkAuthorization(agentId, functionId, qualifierId);
  }

  /**
   * Warning. Oddly named method.  Just following the convention.
   * Actually using select from ...AuthorizationData...
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @return list of authorization data for the assessments
   */
  public ArrayList getAssessments(String agentId, String functionId)
  {
    // standalone code: doesn't exist
    // integrated code:
//    ArrayList returnList = new ArrayList();
//    if (agentId == null || functionId == null)
//    {
//      throw new IllegalArgumentException("Null Argument");
//    }
//    else
//    {
//      HibernateCallback hcb = new HibernateCallback()
//      {
//        public Object doInHibernate(Session session) throws HibernateException,
//            SQLException
//        {
//          Query query = session.createQuery(HQL_QUERY_BY_AGENT_FUNC);
//          query.setString("agentId", agentId);
//          query.setString("functionId", functionId);
//          return query.list();
//        }
//      };
//      List result = (List)getHibernateTemplate().execute(hcb);
//      for (int i=0; i<result.size();i++){
//        AuthorizationData ad = (AuthorizationData) result.get(i);
//        returnList.add(ad);
//      }
//  }
//
//  return returnList;


    return helper.getAssessments(agentId, functionId);
  }

  /**
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @return list of base assessments
   */
  public ArrayList getAssessmentsByAgentAndFunction(String agentId,
    String functionId)
  {
    // standalone code: doesn't exist
    // integrated code:
//    ArrayList returnList = new ArrayList();
//    if (agentId == null || functionId == null)
//    {
//      throw new IllegalArgumentException("Null Argument");
//    }
//    else
//    {
//      HibernateCallback hcb = new HibernateCallback()
//      {
//        public Object doInHibernate(Session session) throws HibernateException,
//        SQLException
//        {
//          Query query = session.createQuery(HQL_QUERY_ASSESS_BY_AGENT_FUNC);
//          query.setString("agentId", agentId);
//          query.setString("functionId", functionId);
//          return query.list();
//        }
//      };
//      List result = (List)getHibernateTemplate().execute(hcb);
//      for(int i=0; i<result.size(); i++)
//      {
//        AssessmentBaseData ad = (AssessmentBaseData)result.get(i);
//        returnList.add(ad);
//      }
//    }
//
//    return returnList;

    return helper.getAssessmentsByAgentAndFunction(agentId, functionId);
  }

}
