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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.facade.authz.integrated;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * <p>Description: Facade for AuthZ queries, standalone version.
 * <p>Sakai Project Copyright (c) 2005</p>
 * @author cwen
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @author Ed Smiley <esmiley@stanford.edu> split integrated, standlaone.
 */
@Slf4j
public class AuthzQueriesFacade extends HibernateDaoSupport implements AuthzQueriesFacadeAPI
{
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

  private AuthzGroupService authzGroupService;

  public void setAuthzGroupService(AuthzGroupService authzGroupService) {
    this.authzGroupService = authzGroupService;
  }

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
    final HibernateCallback<List<AuthorizationData>> hcb = session -> session
            .createQuery("select a from AuthorizationData a where a.functionId = :fid and a.qualifierId = :id")
            .setString("fid", functionId)
            .setString("id", qualifierId)
            .list();
    List<AuthorizationData> authorizationList = getHibernateTemplate().execute(hcb);

    String currentSiteId = null;
    if (ToolManager.getCurrentPlacement() != null)
      currentSiteId =ToolManager.getCurrentPlacement().getContext();
    if (currentSiteId == null)
      return false; // user don't login via any site if they are using published url

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

    HibernateCallback hcb = session -> {
      Query query = session.createQuery(HQL_QUERY_CHECK_AUTHZ);
      if(agentId == null) {
        query.setString("agentId", queryAgentId);
      } else {
        query.setString("agentId", agentId);
      }
      query.setString("functionId", functionId);
      query.setString("qualifierId", qualifierId);
      return query.uniqueResult();
    };
    Object result = getHibernateTemplate().execute(hcb);

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
  public List<AuthorizationData> getAssessments(final String agentId, final String functionId)
  {
    if (agentId == null || functionId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    HibernateCallback<List<AuthorizationData>> hcb = session -> {
      Query query = session.createQuery(HQL_QUERY_BY_AGENT_FUNC);
      query.setString("agentId", agentId);
      query.setString("functionId", functionId);
      return query.list();
    };
    List<AuthorizationData> returnList = getHibernateTemplate().execute(hcb);

    if (returnList == null) {
      returnList = new ArrayList<>();
    }

    return returnList;
  }

  // this appears to be unused, it is also dangerous, as it is not in the API
  public List<AssessmentBaseData> getAssessmentsByAgentAndFunction(final String agentId, final String functionId)
  {
    if (agentId == null || functionId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    HibernateCallback<List<AssessmentBaseData>> hcb = session -> {
      Query query = session.createQuery(HQL_QUERY_ASSESS_BY_AGENT_FUNC);
      query.setString("agentId", agentId);
      query.setString("functionId", functionId);
      return query.list();
    };
    return getHibernateTemplate().execute(hcb);
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
   * @param qualifierId
   * @param functionId
   */
  public void removeAuthorizationByQualifierAndFunction(String qualifierId, String functionId) {
	    String query="select a from AuthorizationData a where a.qualifierId = :id and a.functionId = :fid";
	    List l = getHibernateTemplate().findByNamedParam(query, new String[] {"id", "fid"}, new String[] {qualifierId, functionId});
	    getHibernateTemplate().deleteAll(l);
  }
  
  /**
   * Removes an authorization for a specified agent, qualifier and function
   * TODO: This should be optimized into a single SQL call for a set of agents (groups)
   * @param agentId
   * @param qualifierId
   */
  public void removeAuthorizationByAgentQualifierAndFunction(String agentId, String qualifierId, String functionId) {
	    String query="select a from AuthorizationData a where a.qualifierId = :id and a.agentIdString = :agent and a.functionId = :fid";
	    List l = getHibernateTemplate().findByNamedParam(query, new String[] {"id", "agent", "fid"},new String[] {qualifierId, agentId, functionId});
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
    String query = "select a from AuthorizationData a where a.agentIdString = :agent and a.functionId = :fid";
    return getHibernateTemplate().findByNamedParam(query, new String[] {"agent", "fid"}, new String[] {agentId, functionId});
  }

  public List<AuthorizationData> getAuthorizationByFunctionAndQualifier(String functionId, String qualifierId) {
    HibernateCallback<List<AuthorizationData>> hcb = session -> session
            .createQuery("select a from AuthorizationData a where a.functionId = :fid and a.qualifierId = :id")
            .setString("fid", functionId)
            .setString("id", qualifierId)
            .list();
    return getHibernateTemplate().execute(hcb);
  }

  public boolean checkMembership(String siteId) {
    boolean isMember = false;
    try{
      String realmName = "/site/" + siteId;
      AuthzGroup siteAuthzGroup = authzGroupService.getAuthzGroup(realmName);
      if (siteAuthzGroup.getUserRole(AgentFacade.getAgentString()) != null)
        isMember = true;
    }
    catch(Exception e)
    {
        log.error(e.getMessage(), e);
    }
    return isMember;
  }

}
