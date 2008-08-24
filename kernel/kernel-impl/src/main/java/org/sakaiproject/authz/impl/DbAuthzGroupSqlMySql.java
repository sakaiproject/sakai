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

import java.util.Collection;

/**
 * methods for accessing authz data in a mysql database.
 */
public class DbAuthzGroupSqlMySql extends DbAuthzGroupSqlDefault
{
	/**
	 * returns the sql statement to delete the realm functions from the sakai_realm_rl_fn table for a given realm id..
	 */
	public String getDeleteRealmRoleFunction()
	{
		return "delete SAKAI_REALM_RL_FN from SAKAI_REALM_RL_FN inner join SAKAI_REALM on SAKAI_REALM_RL_FN.REALM_KEY = SAKAI_REALM.REALM_KEY where REALM_ID = ?";
	}

	/**
	 * returns the sql statement to delete the realm groups from the sakai_realm_rl_gr table for a given realm id.
	 */
	public String getDeleteRealmRoleGroup()
	{
		return "delete SAKAI_REALM_RL_GR from SAKAI_REALM_RL_GR inner join SAKAI_REALM on SAKAI_REALM_RL_GR.REALM_KEY = SAKAI_REALM.REALM_KEY where REALM_ID = ?";
	}

	/**
	 * returns the sql statement to delete the realm providers from the sakai_realm_provider table for a given realm id.
	 */
	public String getDeleteRealmProvider()
	{
		return "delete SAKAI_REALM_PROVIDER from SAKAI_REALM_PROVIDER inner join SAKAI_REALM on SAKAI_REALM_PROVIDER.REALM_KEY = SAKAI_REALM.REALM_KEY where REALM_ID = ?";
	}

	/**
	 * returns the sql statement to write a row into the sakai_function_role table.
	 */
	public String getInsertRealmFunctionSql()
	{
		return "insert into SAKAI_REALM_FUNCTION (FUNCTION_KEY, FUNCTION_NAME) values (DEFAULT, ?)";
	}

	/**
	 * returns the sql statement to write a row into the sakai_realm_role table.
	 */
	public String getInsertRealmRoleSql()
	{
		return "insert into SAKAI_REALM_ROLE (ROLE_KEY, ROLE_NAME) values (DEFAULT, ?)";
	}

	public String getDeleteRealmRoleFunction1Sql()
	{
		return "DELETE RRF FROM SAKAI_REALM_RL_FN RRF" + " INNER JOIN SAKAI_REALM R ON RRF.REALM_KEY = R.REALM_KEY AND R.REALM_ID = ?"
				+ " INNER JOIN SAKAI_REALM_ROLE RR ON RRF.ROLE_KEY = RR.ROLE_KEY AND RR.ROLE_NAME = ?"
				+ " INNER JOIN SAKAI_REALM_FUNCTION RF ON RRF.FUNCTION_KEY = RF.FUNCTION_KEY AND RF.FUNCTION_NAME = ?";
	}

	public String getDeleteRealmRoleGroup1Sql()
	{
		return "DELETE RRG FROM SAKAI_REALM_RL_GR RRG" + " INNER JOIN SAKAI_REALM R ON RRG.REALM_KEY = R.REALM_KEY AND R.REALM_ID = ?"
				+ " INNER JOIN SAKAI_REALM_ROLE RR ON RRG.ROLE_KEY = RR.ROLE_KEY AND RR.ROLE_NAME = ?"
				+ " WHERE RRG.USER_ID = ? AND RRG.ACTIVE = ? AND RRG.PROVIDED = ?";
	}

	public String getDeleteRoleDescriptionSql()
	{
		return "DELETE RRD FROM SAKAI_REALM_ROLE_DESC RRD" + " INNER JOIN SAKAI_REALM R ON RRD.REALM_KEY = R.REALM_KEY AND R.REALM_ID = ?"
				+ " INNER JOIN SAKAI_REALM_ROLE RR ON RRD.ROLE_KEY = RR.ROLE_KEY AND RR.ROLE_NAME = ?";
	}

	public String getDeleteRealmRoleFunction2Sql()
	{
		return "DELETE SAKAI_REALM_RL_FN FROM SAKAI_REALM_RL_FN INNER JOIN SAKAI_REALM ON SAKAI_REALM_RL_FN.REALM_KEY = SAKAI_REALM.REALM_KEY AND SAKAI_REALM.REALM_ID = ?";
	}

	public String getDeleteRealmRoleGroup2Sql()
	{
		return "DELETE SAKAI_REALM_RL_GR FROM SAKAI_REALM_RL_GR INNER JOIN SAKAI_REALM ON SAKAI_REALM_RL_GR.REALM_KEY = SAKAI_REALM.REALM_KEY AND SAKAI_REALM.REALM_ID = ?";
	}

	public String getDeleteRealmProvider1Sql()
	{
		return "DELETE SAKAI_REALM_PROVIDER FROM SAKAI_REALM_PROVIDER INNER JOIN SAKAI_REALM ON SAKAI_REALM_PROVIDER.REALM_KEY = SAKAI_REALM.REALM_KEY AND SAKAI_REALM.REALM_ID = ?";
	}

	public String getDeleteRealmRoleDescription2Sql()
	{
		return "DELETE SAKAI_REALM_ROLE_DESC FROM SAKAI_REALM_ROLE_DESC INNER JOIN SAKAI_REALM ON SAKAI_REALM_ROLE_DESC.REALM_KEY = SAKAI_REALM.REALM_KEY AND SAKAI_REALM.REALM_ID = ?";
	}

	public String getCountRealmRoleFunctionSql(String anonymousRole, String authorizationRole, boolean authorized, String inClause)
	{
		return "select count(1) from SAKAI_REALM_RL_FN,SAKAI_REALM force index "
				+ "(AK_SAKAI_REALM_ID) where SAKAI_REALM_RL_FN.REALM_KEY = SAKAI_REALM.REALM_KEY " + "and " + inClause
				+ getCountRealmRoleFunctionEndSql(anonymousRole, authorizationRole, authorized, inClause);
	}

	public String getSelectRealmRoleGroupUserIdSql(String inClause1, String inClause2)
	{
		StringBuilder sqlBuf = new StringBuilder();

		sqlBuf.append("select SRRG.USER_ID ");
		sqlBuf.append("from SAKAI_REALM_RL_GR SRRG ");
		sqlBuf.append("inner join SAKAI_REALM SR force index (AK_SAKAI_REALM_ID) ON SRRG.REALM_KEY = SR.REALM_KEY ");
		sqlBuf.append("where " + inClause1 + " ");
		sqlBuf.append("and SRRG.ACTIVE = '1' ");
		sqlBuf.append("and SRRG.ROLE_KEY in ");
		sqlBuf.append("(select SRRF.ROLE_KEY ");
		sqlBuf.append("from SAKAI_REALM_RL_FN SRRF ");
		sqlBuf.append("inner join SAKAI_REALM_FUNCTION SRF ON SRRF.FUNCTION_KEY = SRF.FUNCTION_KEY ");
		sqlBuf.append("inner join SAKAI_REALM SR1 force index (AK_SAKAI_REALM_ID) ON SRRF.REALM_KEY = SR1.REALM_KEY ");
		sqlBuf.append("where SRF.FUNCTION_NAME = ? ");
		sqlBuf.append("and " + inClause2 + ")");
		return sqlBuf.toString();
	}

	public String getDeleteRealmRoleGroup4Sql()
	{
		return "DELETE SAKAI_REALM_RL_GR FROM SAKAI_REALM_RL_GR INNER JOIN SAKAI_REALM ON SAKAI_REALM_RL_GR.REALM_KEY = SAKAI_REALM.REALM_KEY AND SAKAI_REALM.REALM_ID = ? WHERE SAKAI_REALM_RL_GR.USER_ID = ?";
	}
}
