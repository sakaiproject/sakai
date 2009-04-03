/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade.authz.resource;

import java.util.ListResourceBundle;

/**
 *
 * <p> </p>
 * <p>Description: Resource strings for Authz Queries.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class AuthzResource extends java.util.ListResourceBundle
{
  private static final Object[][] contents = new String[][]{
  {"HQL_QUERY_CHECK_AUTHZ",
  "select from " +
    "org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData as data" +
    " where data.agentIdString = :agentId and data.functionId = :functionId" +
    " and data.qualifierId = :qualifierId"
   },
  {"HQL_QUERY_BY_AGENT_FUNC",
  "select from org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData " +
    "as item where item.agentIdString = :agentId and item.functionId = :functionId"
  },
  {"HQL_QUERY_ASSESS_BY_AGENT_FUNC",
  "select asset from " +
    "org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData as asset, " +
    "org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData as authz " +
    "where asset.assessmentBaseId=authz.qualifierId and " +
    "authz.agentIdString = :agentId and authz.functionId = :functionId"
  },
  { "AUTH", "AUTHENTICATED_USERS" },
  { "VIEW_PUB", "VIEW_PUBLISHED_ASSESSMENT" },
  { "ANON", "ANONYMOUS_USERS" },

  { "_site_", "/site/" },
  { "a_id", "agentId" },
  { "f_id", "functionId" },
  { "q_id", "qualifierId" },
  { "and_q_id", "\' and a.qualifierId=\'" },
  { "and_f_id", "\' and a.functionId=\'" },
  { "delivery", "delivery" },
  { "select_authdata_f_id_q_id", "select a from AuthorizationData a where a.functionId=? and a.qualifierId=?" },
  { "select_authdata_a_id", "select a from AuthorizationData a where a.agentIdString=\'" },
  { "select_authdata_q_id", "select a from AuthorizationData a where a.qualifierId=\'" },
  { "select_authdata_f_id", "select a from AuthorizationData a where a.functionId=\'" },
  { "someone", "someone" }};
  public Object[][] getContents()
  {
    return contents;
  }
}
