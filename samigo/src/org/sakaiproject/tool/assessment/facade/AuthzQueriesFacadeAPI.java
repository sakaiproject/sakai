package org.sakaiproject.tool.assessment.facade;

import java.util.HashMap;
import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;

public interface AuthzQueriesFacadeAPI
{

  public boolean isAuthorized(String agentId, String function, String qualifier);

  public AuthorizationData createAuthorization(String agentId,
      String functionId, String qualifierId);

  public void removeAuthorizationByQualifier(String qualifierId);

  /** This returns a HashMap containing (String a.qualiferId, AuthorizationData a)
   * agentId is a site for now but can be a user
   */
  public HashMap getAuthorizationToViewAssessments(String agentId);

  public List getAuthorizationByAgentAndFunction(String agentId,
      String functionId);

  public List getAuthorizationByFunctionAndQualifier(String functionId,
      String qualifierId);

  public boolean checkMembership(String siteId);

}