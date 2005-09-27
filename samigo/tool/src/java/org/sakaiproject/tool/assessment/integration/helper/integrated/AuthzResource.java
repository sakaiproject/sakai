/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/tool/src/java/org/sakaiproject/tool/assessment/integration/helper/integrated/AuthzHelperImpl.java $
 * $Id: AuthzHelperImpl.java 2008 2005-09-23 20:01:57Z esmiley@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
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

import java.util.ListResourceBundle;

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
	{ "select_authdata_q_id", "select a from AuthorizationData a where a.qualifierId=" },
	{ "select_authdata_f_id", "select a from AuthorizationData a where a.functionId=\'" },
	{ "someone", "someone" }};
  public Object[][] getContents()
  {
    return contents;
  }
}