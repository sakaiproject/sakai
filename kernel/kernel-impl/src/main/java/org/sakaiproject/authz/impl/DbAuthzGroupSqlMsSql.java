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
 *       http://www.osedu.org/licenses/ECL-2.0
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
 * methods for accessing authz data in an ms sql server database.
 */
public class DbAuthzGroupSqlMsSql extends DbAuthzGroupSqlDefault
{
	/**
	 * returns the sql statement to write a row into the sakai_function_role table.
	 */
	@Override
	public String getInsertRealmFunctionSql()
	{
		return "insert into SAKAI_REALM_FUNCTION (FUNCTION_NAME) values (?)";
	}

	@Override
	public String getInsertRealmRoleDescription1Sql()
	{
		return "SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?";
	}

	@Override
	public String getInsertRealmRoleDescription2Sql()
	{
		return "SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = ?";
	}

	@Override
	public String getInsertRealmRoleDescriptionSql()
	{
		return "INSERT INTO SAKAI_REALM_ROLE_DESC (REALM_KEY, ROLE_KEY, DESCRIPTION, PROVIDER_ONLY) VALUES (?, ?, ?, ?)";
	}

	@Override
	public String getInsertRealmRoleFunction1Sql()
	{
		return "SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?";
	}

	@Override
	public String getInsertRealmRoleFunction2Sql()
	{
		return "SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = ?";
	}

	@Override
	public String getInsertRealmRoleFunction3Sql()
	{
		return "SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = ?";
	}

	@Override
	public String getInsertRealmRoleFunctionSql()
	{
		return "INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (?, ?, ?)";
	}

	@Override
	public String getInsertRealmRoleGroup1_1Sql()
	{
		return "SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?";
	}

	@Override
	public String getInsertRealmRoleGroup1_2Sql()
	{
		return "SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = ?";
	}

	@Override
	public String getInsertRealmRoleGroup1Sql()
	{
		return "INSERT INTO SAKAI_REALM_RL_GR (REALM_KEY, USER_ID, ROLE_KEY, ACTIVE, PROVIDED) VALUES (?, ?, ?, ?, ?)";
	}

	@Override
	public String getInsertRealmRoleGroup2_1Sql()
	{
		return "select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = ?";
	}

	@Override
	public String getInsertRealmRoleGroup2Sql()
	{
		return "insert into SAKAI_REALM_RL_GR (REALM_KEY, USER_ID, ROLE_KEY, ACTIVE, PROVIDED) values (?, ?, ?, '1', '1')";
	}

	@Override
	public String getInsertRealmRoleGroup3_1Sql()
	{
		return "select REALM_KEY from SAKAI_REALM where REALM_ID = ?";
	}

	@Override
	public String getInsertRealmRoleGroup3_2Sql()
	{
		return "select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = ?";
	}

	@Override
	public String getInsertRealmRoleGroup3Sql()
	{
		return "insert into SAKAI_REALM_RL_GR (REALM_KEY, USER_ID, ROLE_KEY, ACTIVE, PROVIDED) values (?, ?, ?, ?, ?)";
	}

	/**
	 * returns the sql statement to write a row into the sakai_realm_role table.
	 */
	@Override
	public String getInsertRealmRoleSql()
	{
		return "insert into SAKAI_REALM_ROLE (ROLE_NAME) values(?)";
	}
}
