/*
 * Created on 2005-1-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.sakaiproject.tool.assessment.facade;

import java.sql.SQLException;
import java.util.*;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;
import net.sf.hibernate.Hibernate;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;

import org.sakaiproject.service.legacy.realm.Realm;
import org.sakaiproject.service.legacy.realm.Role;
import org.sakaiproject.service.framework.portal.cover.PortalService;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.service.legacy.realm.cover.RealmService;

import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;

/**
 * @author cwen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AuthzQueriesFacade
	extends HibernateDaoSupport implements AuthzQueriesFacadeAPI
{
  private final static org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(AuthzQueriesFacade.class);
  private final static String HQL_QUERY_CHECK_AUTHZ =
    "select from " +
		"org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData as data" +
		" where data.agentIdString = :agentId and data.functionId = :functionId" + 
		" and data.qualifierId = :qualifierId";
  private final static String HQL_QUERY_BY_AGENT_FUNC = 
    "select from org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData " +
    "as item where item.agentIdString = :agentId and item.functionId = :functionId";
  private final static String HQL_QUERY_ASSESS_BY_AGENT_FUNC = "select asset from " +
		"org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData as asset, " +
		"org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData as authz " +
		"where asset.assessmentBaseId=authz.qualifierId and " +
		"authz.agentIdString = :agentId and authz.functionId = :functionId";
  
    // this method is added by daisyf on 02/22/05
  public boolean isAuthorized(final String agentId, 
      final String functionId, final String qualifierId)
  {
    String query = "select a from AuthorizationData a where a.functionId=? and a.qualifierId=?";
    List authorizationList = getHibernateTemplate().find(query,
                             new Object[] { functionId, qualifierId },
                             new net.sf.hibernate.type.Type[] { Hibernate.STRING, Hibernate.STRING });    

    String currentSiteId = null;
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    if (!delivery.getAccessViaUrl())
        currentSiteId = org.sakaiproject.service.framework.portal.cover.PortalService.getCurrentSiteId();
    if(currentSiteId == null)
	return false; // user don't login via any site if they are using published url

    //System.out.println("**** currentSiteId"+currentSiteId);
    String currentAgentId = UserDirectoryService.getCurrentUser().getId();
    for (int i=0; i<authorizationList.size(); i++){
      AuthorizationData a = (AuthorizationData) authorizationList.get(i);
      String siteId = a.getAgentIdString(); 
      if (("AUTHENTICATED_USERS").equals(siteId) && (currentAgentId!=null)){
        return true;
      }
      else if (("ANONYMOUS_USERS").equals(siteId)){
        return true;
      }
      else if(currentSiteId.equals(siteId))
      {
        return true;
      }
    }
    return false;
  }

  public boolean checkAuthorization(final String agentId, 
      final String functionId, final String qualifierId)
  {
/*    if (agentId == null || functionId == null || qualifierId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }*/
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
        Query query = session.createQuery(HQL_QUERY_CHECK_AUTHZ);
        //query.setString("agentId", agentId);
        if(agentId == null)
          query.setString("agentId", queryAgentId);
        else
          query.setString("agentId", agentId);
        query.setString("functionId", functionId);
        query.setString("qualifierId", qualifierId);
        return query.uniqueResult();
        //return query.list();
      }
    };
    Object result = (AuthorizationData)getHibernateTemplate().execute(hcb);
    
    if(result != null)
      return true;
    else
      return false;
  }

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
      ad.setLastModifiedBy("someone");
      ad.setLastModifiedDate(lastModifiedDate);
      getHibernateTemplate().save(ad);
      return ad;
    }
  }
  
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
          Query query = session.createQuery(HQL_QUERY_BY_AGENT_FUNC);
          query.setString("agentId", agentId);
          query.setString("functionId", functionId);
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
          Query query = session.createQuery(HQL_QUERY_ASSESS_BY_AGENT_FUNC);
          query.setString("agentId", agentId);
          query.setString("functionId", functionId);
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

  public void removeAuthorizationByQualifier(String qualifierId) {
    List l = getHibernateTemplate().find(
      "select a from AuthorizationData a where a.qualifierId="+qualifierId);
    getHibernateTemplate().deleteAll(l);    
  }

  /** This returns a HashMap containing (String a.qualiferId, AuthorizationData a)
    * agentId is a site for now but can be a user
    */
  public HashMap getAuthorizationToViewAssessments(String agentId) {
    HashMap h = new HashMap();
    List l = getAuthorizationByAgentAndFunction(agentId, "VIEW_PUBLISHED_ASSESSMENT");
    for (int i=0; i<l.size();i++){
      AuthorizationData a = (AuthorizationData) l.get(i);
      h.put(a.getQualifierId(), a);
    }
    return h;
  }

  public List getAuthorizationByAgentAndFunction(String agentId, String functionId) {
    String query = "select a from AuthorizationData a where a.agentIdString='"+ agentId +
        "' and a.functionId='"+functionId+"'";
    System.out.println("query="+query);
    return getHibernateTemplate().find(query);
  }

  public List getAuthorizationByFunctionAndQualifier(String functionId, String qualifierId) {
	return getHibernateTemplate().find(
        "select a from AuthorizationData a where a.functionId='"+ functionId +
        "' and a.qualifierId='"+qualifierId+"'");
  }

  public boolean checkMembership(String siteId) {
    boolean isMember = false;
    try{
      String realmName = "/site/" + siteId;
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

}
