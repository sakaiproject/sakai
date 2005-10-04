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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.sakaiproject.service.legacy.realm.Realm;
import org.sakaiproject.service.legacy.realm.cover.RealmService;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 *
 * <p>Description:
 * This is an integrated context implementation helper delegate class for
 * the AuthzQueriesFacade class. It overloads some standalone version methods.
 *
 * "Integrated" means that Samigo (Tests and Quizzes)
 * is running within the context of the Sakai portal and authentication
 * mechanisms, and therefore makes calls on Sakai for things it needs.</p>
 *
 * <p>Note: To customize behavior you can add your own helper class to the
 * Spring injection via the integrationContext.xml for your context.
 * The particular integrationContext.xml to be used is selected by the
 * build process.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p>@see org.sakaiproject.tool.assessment.integration.helper.standalone.AuthzHelperImpl
 * from which it inherits the following:
 * <ul>
 *  <li>
 *   public void removeAuthorizationByQualifier(String qualifierId) {
 *  </li>
 *   public HashMap getAuthorizationToViewAssessments(String agentId) {
 *  </li>
 *   public List getAuthorizationByAgentAndFunction(String agentId,
 *  <li>
 *   public List getAuthorizationByFunctionAndQualifier(String functionId,
 *  </li>
 * </ul>
</p>
 * @author Ed Smiley <esmiley@stanford.edu>
 * based on code originally in AuthzQueriesFacade
 */

public class AuthzHelperImpl
  extends org.sakaiproject.tool.assessment.integration.helper.standalone.AuthzHelperImpl
{
  static ResourceBundle res = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.integration.helper.integrated.AuthzResource");
  private static Log log = LogFactory.getLog(AuthzHelperImpl.class);

  // this method is added by daisyf on 02/22/05
  /**
   * Is the agent {agentId} authorized to do {function} to {qualifier}?
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @param qualifierId the target of the function.
   * @return true if authorized, false otherwise.
   */
  public boolean isAuthorized(final String agentId,
                              final String functionId, final String qualifierId)
  {
    String query =
      res.getString("select_authdata_f_id_q_id");
    log.info("query=" + query);
    List authorizationList = getHibernateTemplate().find(query,
      new Object[]
      {functionId, qualifierId}
      ,
      new net.sf.hibernate.type.Type[]
      {Hibernate.STRING, Hibernate.STRING});

    String currentSiteId = null;
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean(res.getString("delivery"));
    if (!delivery.getAccessViaUrl())
    {
      currentSiteId = org.sakaiproject.service.framework.portal.cover.
        PortalService.getCurrentSiteId();
    }
    if (currentSiteId == null)
    {
      return false; // user doesn't login via any site if they are using published url-daisyf
    }

    String currentAgentId = UserDirectoryService.getCurrentUser().getId();
    for (int i = 0; i < authorizationList.size(); i++)
    {
      AuthorizationData a = (AuthorizationData) authorizationList.get(i);
      String siteId = a.getAgentIdString();
      if ( (res.getString("AUTH")).equals(siteId) && (currentAgentId != null))
      {
        return true;
      }
      else if ( (res.getString("ANON")).equals(siteId))
      {
        return true;
      }
      else if (currentSiteId.equals(siteId))
      {
        return true;
      }
    }
    return false;
  }

  /**
  *  Create an authorization record for {agentId} authorized to do {function}
  * to {qualifier}
  * @param agentId the agent id.
  * @param functionId the function to be performed.
  * @param qualifierId the target of the function.
  * @return the authorization data
  */

  public AuthorizationData createAuthorization(
      String agentId, String functionId,
      String qualifierId)
  {
    if (agentId == null || functionId == null || qualifierId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {
      AuthorizationData ad = new AuthorizationData();

      Calendar cal = Calendar.getInstance();
      Date lastModifiedDate = cal.getTime();

      ad.setAgentIdString(agentId);
      ad.setFunctionId(functionId);
      ad.setQualifierId(qualifierId);
      ad.setLastModifiedBy(res.getString("someone"));
      ad.setLastModifiedDate(lastModifiedDate);
      getHibernateTemplate().save(ad);
      return ad;
    }
  }

  /**
   * Check if member of site.
   * @param siteId the site id
   * @return true if a member.
   */
  public boolean checkMembership(String siteId) {
    boolean isMember = false;
    try{
      String realmName = res.getString("_site_") + siteId;
      Realm siteRealm = RealmService.getRealm(realmName);
      if (siteRealm.getUserRole(AgentFacade.getAgentString()) != null)
        isMember = true;
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
    }
    return isMember;
  }

  /**
   * Check the agent {agentId} authorized to do {function} to {qualifier}?
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @param qualifierId the target of the function.
   * @return true if the agent checks out for function-> qualifier
   */

  public boolean checkAuthorization(final String agentId,
      final String functionId, final String qualifierId)
  {
    if (functionId == null || qualifierId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    final String queryAgentId = org.sakaiproject.service.framework.portal.cover.PortalService.getCurrentSiteId();

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query query = session.createQuery(res.getString("HQL_QUERY_CHECK_AUTHZ"));
        if (agentId == null)
        {
          query.setString(res.getString("a_id"), queryAgentId);
        }
        else
        {
          query.setString(res.getString("a_id"), agentId);
        }
        query.setString(res.getString("f_id"), functionId);
        query.setString(res.getString("q_id"), qualifierId);

        log.info("query=" + query);
        return query.uniqueResult();
      }
    };
    Object result = (AuthorizationData)getHibernateTemplate().execute(hcb);

    if(result != null)
      return true;
    else
      return false;
  }

  /**
   * Warning. Oddly named method.  Just following the convention.
   * Actually using select from ...AuthorizationData...
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @return list of authorization data for the assessments
   */

  public ArrayList getAssessments(final String agentId, final String functionId)
  {
    ArrayList returnList = new ArrayList();
    if (agentId == null || functionId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {
      HibernateCallback hcb = new HibernateCallback()
      {
        public Object doInHibernate(Session session) throws HibernateException,
            SQLException
        {
          Query query = session.createQuery(res.getString("HQL_QUERY_BY_AGENT_FUNC"));
          query.setString(res.getString("a_id"), agentId);
          query.setString(res.getString("f_id"), functionId);
          log.info("query="+ query);
          return query.list();
        }
      };
      List result = (List)getHibernateTemplate().execute(hcb);
      for (int i=0; i<result.size();i++){
        AuthorizationData ad = (AuthorizationData) result.get(i);
        returnList.add(ad);
      }
    }

    return returnList;
  }

  /**
   * @param agentId the agent id.
   * @param functionId the function to be performed.
   * @return list of base assessments
   */
  public ArrayList getAssessmentsByAgentAndFunction(final String agentId, final String functionId)
  {
    ArrayList returnList = new ArrayList();
    if (agentId == null || functionId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {
      HibernateCallback hcb = new HibernateCallback()
      {
        public Object doInHibernate(Session session) throws HibernateException,
        SQLException
        {
          Query query =
            session.createQuery(res.getString("HQL_QUERY_ASSESS_BY_AGENT_FUNC"));
          query.setString(res.getString("a_id"), agentId);
          query.setString(res.getString("f_id"), functionId);
          log.info("query=" + query);
          return query.list();
        }
      };
      List result = (List)getHibernateTemplate().execute(hcb);
      for(int i=0; i<result.size(); i++)
      {
        AssessmentBaseData ad = (AssessmentBaseData)result.get(i);
        returnList.add(ad);
      }
    }

    return returnList;
  }

//////////////////////////////////////////////////////////////////////////////
// The following are inherited from the standalone version.
// Remove when we have verified it is working.
//////////////////////////////////////////////////////////////////////////////
//  /**
//   * Remove authorization from qualifier (target).
//   * @param qualifierId the target.
//   */
//  public void removeAuthorizationByQualifier(String qualifierId) {
//    String query =
//      res.getString("select_authdata_q_id") + qualifierId;
//    log.info("query=" + query);
//
//    List l = getHibernateTemplate().find(query);
//    getHibernateTemplate().deleteAll(l);
//  }
//
//  /**
//   * This returns a HashMap containing authorization data.
//   * @param agentId is a site for now but can be a user
//   * @return HashMap containing (String a.qualiferId, AuthorizationData a)
//   */
//  public HashMap getAuthorizationToViewAssessments(String agentId) {
//    HashMap h = new HashMap();
//    List l = getAuthorizationByAgentAndFunction(agentId, res.getString("VIEW_PUB"));
//    for (int i=0; i<l.size();i++){
//      AuthorizationData a = (AuthorizationData) l.get(i);
//      h.put(a.getQualifierId(), a);
//    }
//    return h;
//  }
//
//  /**
//   * Get authorization list by agent and function.
//   * @param agentId the agent id.
//   * @param functionId the function to be performed.
//   * @return the list of authorizations.
//   */
//  public List getAuthorizationByAgentAndFunction(String agentId,
//                                                 String functionId)
//  {
//    String query = res.getString("select_authdata_a_id") + agentId +
//      res.getString("and_f_id") + functionId + "'";
//    System.out.println("query=" + query);
//    return getHibernateTemplate().find(query);
//  }
//  /**
//   * Get authorization list by qualifier and function.
//   * @param functionId the function to be performed.
//   * @param qualifierId the target of the function.
//   * @return the list of authorizations.
//   */
//  public List getAuthorizationByFunctionAndQualifier(String functionId,
//    String qualifierId)
//  {
//    String query =
//      res.getString("select_authdata_f_id") + functionId +
//      res.getString("and_a_id") + qualifierId + "'";
//    log.info("query=" + query);
//
//    return getHibernateTemplate().find(query);
//  }
//

}
