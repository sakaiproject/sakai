/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/authz/trunk/authz-api/api/src/java/org/sakaiproject/authz/api/AuthzGroup.java $
 * $Id: AuthzGroup.java 7063 2006-03-27 17:46:13Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.authz.impl;

/**
 * methods for accessing authz data in an oracle database.
 */
public class DbAuthzGroupSqlOracle extends DbAuthzGroupSqlDefault
{
	/**
	 * returns the sql statement to write a row into the sakai_function_role table.
	 */
	@Override
	public String getInsertRealmFunctionSql()
	{
		return "insert into SAKAI_REALM_FUNCTION (FUNCTION_KEY, FUNCTION_NAME) values (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, ?)";
	}

	/**
	 * returns the sql statement to write a row into the sakai_realm_role table.
	 */
	@Override
	public String getInsertRealmRoleSql()
	{
		return "insert into SAKAI_REALM_ROLE (ROLE_KEY, ROLE_NAME) values (SAKAI_REALM_ROLE_SEQ.NEXTVAL, ?)";
	}
	
	public String getCountRealmRoleFunctionSql(String anonymousRoleKey, String authorizationRoleKey, boolean authorized, String inClause)
	{
		String roleKeys = authorized? authorizationRoleKey + "," + anonymousRoleKey : anonymousRoleKey;
		return "SELECT 1 FROM SAKAI_REALM_RL_FN srrf, SAKAI_REALM_FUNCTION srf, (select realm_key, role_key from SAKAI_REALM_RL_GR where ACTIVE = '1' and USER_ID = ? union select -1 as realm_key, -1 as role_key from dual) srrg WHERE rownum = 1 AND srrf.realm_key in (select realm_key from SAKAI_REALM where " + inClause + ") AND srrf.function_key = srf.function_key AND srf.function_name = ? AND ((srrf.role_key = srrg.role_key AND srrg.realm_key in (select realm_key from SAKAI_REALM where " + inClause + ")) OR srrf.role_key in (" + roleKeys + "))";
	}

}
