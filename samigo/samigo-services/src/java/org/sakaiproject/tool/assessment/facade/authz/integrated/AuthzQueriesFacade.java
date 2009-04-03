/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.tool.assessment.facade.authz.integrated;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;

/**
 * <p>Description: Facade for AuthZ queries, standalone version.
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p>@todo use resources in AuthzResource.</p>
 * @author cwen
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @author Ed Smiley <esmiley@stanford.edu> split integrated, standlaone.
 */
public class AuthzQueriesFacade
  extends HibernateDaoSupport implements AuthzQueriesFacadeAPI
{
  private final static org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(AuthzQueriesFacade.class);

  // stores sql strings
  private static ResourceBundle res = ResourceBundle.getBundle(
    "org.sakaiproject.tool.assessment.facade.authz.resource.AuthzResource");
  // can convert these to use the resource bundle....
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

  public boolean hasPrivilege(String functionName)
  {
      String context = ToolManager.getCurrentPlacement().getContext();
      boolean privilege = SecurityService.unlock(functionName, "/site/"+context);
      return privilege;
  }

    // this method is added by daisyf on 02/22/05
  public boolean isAuthorized(final String agentId,
      final String functionId, final String qualifierId)
  {
    final String query = "select a from AuthorizationData a where a.functionId=? and a.qualifierId=?";
    
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setString(0, functionId);
    		q.setString(1, qualifierId);
    		return q.list();
    	};
    };
    List authorizationList = getHibernateTemplate().executeFind(hcb);

//    List authorizationList = getHibernateTemplate().find(query,
//                             new Object[] { functionId, qualifierId },
//                             new org.hibernate.type.Type[] { Hibernate.STRING, Hibernate.STRING });

    String currentSiteId = null;
    if (ToolManager.getCurrentPlacement() != null)
      currentSiteId =ToolManager.getCurrentPlacement().getContext();
    if (currentSiteId == null)
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

  // this appears to be unused, it is also dangerous, as it is not in the API
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
    final String queryAgentId = ToolManager.getCurrentPlacement().getContext();

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
      ad.setLastModifiedBy(UserDirectoryService.getCurrentUser().getId());
      ad.setLastModifiedDate(lastModifiedDate);
      getHibernateTemplate().save(ad);
      return ad;
    }
  }

  // this appears to be unused, it is also dangerous, as it is not in the API
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

  // this appears to be unused, it is also dangerous, as it is not in the API
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

  public void removeAuthorizationByQualifier(String qualifierId, boolean isPublishedAssessment) {
    String query="select a from AuthorizationData a where a.qualifierId="+qualifierId;
    String clause="";
    if (isPublishedAssessment){
      clause = " and (a.functionId='OWN_PUBLISHED_ASSESSMENT'"+
               " or a.functionId='TAKE_PUBLISHED_ASSESSMENT'"+
               " or a.functionId='VIEW_PUBLISHED_ASSESSMENT_FEEDBACK'"+
               " or a.functionId='GRADE_PUBLISHED_ASSESSMENT'"+
               " or a.functionId='VIEW_PUBLISHED_ASSESSMENT')";
    }
    else{
	clause = " and a.functionId='EDIT_ASSESSMENT'";
    }
    List l = getHibernateTemplate().find(query+clause);
    getHibernateTemplate().deleteAll(l);
  }

  /**
   * Removes an authorization for a specified qualifier and function
   * added by gopalrc - Nov 2007 
   * @param qualifierId
   * @param functionId
   */
  public void removeAuthorizationByQualifierAndFunction(String qualifierId, String functionId) {
	    String query="select a from AuthorizationData a where a.qualifierId=? and a.functionId=?";
	    List l = getHibernateTemplate().find(query, new String[]{qualifierId, functionId});
	    getHibernateTemplate().deleteAll(l);
  }
  
  /**
   * Removes an authorization for a specified agent, qualifier and function
   * TODO: This should be optimized into a single SQL call for a set of agents (groups)
   * added by gopalrc - Nov 2007 
   * @param agentId
   * @param qualifierId
   */
  public void removeAuthorizationByAgentQualifierAndFunction(String agentId, String qualifierId, String functionId) {
	    String query="select a from AuthorizationData a where a.qualifierId=? and a.agentIdString=? and a.functionId=?";
	    List l = getHibernateTemplate().find(query, new String[]{qualifierId, agentId, functionId,});
	    if (l != null && l.size() > 0) {
	    	getHibernateTemplate().deleteAll(l);
	    }
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
    String query = "select a from AuthorizationData a where a.agentIdString=? and a.functionId=?";
    //System.out.println("query="+query);
    return getHibernateTemplate().find(query, new String[]{agentId, functionId});
  }

  public List getAuthorizationByFunctionAndQualifier(String functionId, String qualifierId) {
  return getHibernateTemplate().find(
		"select a from AuthorizationData a where a.functionId=?"+
		" and a.qualifierId=?",new String[]{functionId,qualifierId});
  }

  public boolean checkMembership(String siteId) {
    boolean isMember = false;
    try{
      String realmName = "/site/" + siteId;
      AuthzGroup siteAuthzGroup = AuthzGroupService.getAuthzGroup(realmName);
      if (siteAuthzGroup.getUserRole(AgentFacade.getAgentString()) != null)
        isMember = true;
    }
    catch(Exception e)
    {
       e.printStackTrace();
    }
    return isMember;
  }

  
}
