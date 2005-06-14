package org.sakaiproject.tool.assessment.facade;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 *
 * An Authorization Facade
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @version 1.0
 */
public class AuthzQueriesFacade
          extends HibernateDaoSupport implements AuthzQueriesFacadeAPI
{
  private static Log log = LogFactory.getLog(AuthzQueriesFacade.class);

  public  boolean isAuthorized
    (String agentId, String function, String qualifier)
  {
    return true;
  }

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
    return true;
    /**
    boolean isMember = false;
    String realmName = "/site/" + siteId;
    Realm siteRealm = RealmService.getRealm(realmName);
    if (siteRealm.getUserRole(AgentFacade.getAgentString()) != null)
      isMember = true;
    return isMember;
        */
  }
}
