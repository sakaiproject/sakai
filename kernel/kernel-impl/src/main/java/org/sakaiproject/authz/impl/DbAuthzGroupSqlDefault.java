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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * methods for accessing authz data in a database.
 */
public class DbAuthzGroupSqlDefault implements DbAuthzGroupSql
{
	public String getCountRealmFunctionSql()
	{
		return "select count(1) from SAKAI_REALM_FUNCTION where FUNCTION_NAME = ?";
	}

	public String getCountRealmRoleFunctionEndSql(Set<Integer> roleIds, String inClause)
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" and FUNCTION_KEY in (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = ?) ");
		sql.append(" and (ROLE_KEY in (select ROLE_KEY from SAKAI_REALM_RL_GR where ACTIVE = '1' and USER_ID = ? ");		
		sql.append(" and REALM_KEY in (select REALM_KEY from SAKAI_REALM where " + inClause + ")) ");
		Iterator<Integer> rolesIt = roleIds.iterator();
		if (rolesIt.hasNext())
		{
			sql.append(" or ROLE_KEY in (");
			sql.append("?");
			rolesIt.next();
			while(rolesIt.hasNext())
			{
				sql.append(", ?");
				rolesIt.next();
			}
			sql.append(")");
		}
		sql.append(" )");
		return sql.toString();
	}

	public String getCountRealmRoleFunctionSql(Set<Integer> roleIds)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select count(1) " + "from   SAKAI_REALM_RL_FN MAINTABLE ");
		sql.append("       LEFT JOIN SAKAI_REALM_RL_GR GRANTED_ROLES ON (MAINTABLE.REALM_KEY = GRANTED_ROLES.REALM_KEY AND ");
		sql.append("       MAINTABLE.ROLE_KEY = GRANTED_ROLES.ROLE_KEY), SAKAI_REALM REALMS, SAKAI_REALM_FUNCTION FUNCTIONS ");
		sql.append("where (");
				// our criteria
		Iterator<Integer> rolesIt = roleIds.iterator();
		if (rolesIt.hasNext())
		{
			sql.append("  MAINTABLE.ROLE_KEY in(");
			sql.append("?");
			rolesIt.next();
			while(rolesIt.hasNext())
			{
				sql.append(", ?");
				rolesIt.next();
			}
			sql.append(") or ");
		}
		sql.append("  (GRANTED_ROLES.USER_ID = ? AND GRANTED_ROLES.ACTIVE = 1)) AND FUNCTIONS.FUNCTION_NAME = ? AND REALMS.REALM_ID in (?) ");
		sql.append("  AND MAINTABLE.REALM_KEY = REALMS.REALM_KEY AND MAINTABLE.FUNCTION_KEY = FUNCTIONS.FUNCTION_KEY ");
		return sql.toString();
	}

	public String getCountRealmRoleFunctionSql(Set<Integer> roleIds, String inClause)
	{
		return "select count(1) from SAKAI_REALM_RL_FN " + "where  REALM_KEY in (select REALM_KEY from SAKAI_REALM where " + inClause + ")"
				+ getCountRealmRoleFunctionEndSql(roleIds, inClause);
	}

	public String getCountRealmRoleSql()
	{
		return "select count(1) from SAKAI_REALM_ROLE where ROLE_NAME = ?";
	}
	
	public String getCountRoleFunctionSql(String inClause, boolean isDelegated)
	{
		return "select count(1) from SAKAI_REALM_RL_FN MAINTABLE "
				+ "		JOIN SAKAI_REALM_ROLE ROLE ON ROLE.ROLE_KEY = MAINTABLE.ROLE_KEY "
				+ "		JOIN SAKAI_REALM_FUNCTION FUNCTIONS ON FUNCTIONS.FUNCTION_KEY = MAINTABLE.FUNCTION_KEY "
				+ "		JOIN SAKAI_REALM SAKAI_REALM ON SAKAI_REALM.REALM_KEY = MAINTABLE.REALM_KEY "
				+ (isDelegated ? "":"		JOIN SAKAI_REALM_RL_GR GRANTS ON GRANTS.REALM_KEY = MAINTABLE.REALM_KEY")
				+ "		where ROLE.ROLE_NAME = ? AND FUNCTIONS.FUNCTION_NAME = ?"
				+ "		and " + inClause
				+ (isDelegated ? "":"		and GRANTS.ACTIVE = '1' and GRANTS.USER_ID = ?");
	}

	public String getDeleteRealmProvider1Sql()
	{
		return "DELETE FROM SAKAI_REALM_PROVIDER WHERE REALM_KEY IN (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)";
	}

	public String getDeleteRealmProvider2Sql()
	{
		return "DELETE FROM SAKAI_REALM_PROVIDER WHERE REALM_KEY IN (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?) AND PROVIDER_ID = ?";
	}

	public String getDeleteRealmRoleDescription1Sql()
	{
		return "DELETE FROM SAKAI_REALM_ROLE_DESC" + " WHERE REALM_KEY IN (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)"
				+ " AND ROLE_KEY IN (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = ?)";
	}

	public String getDeleteRealmRoleDescription2Sql()
	{
		return "DELETE FROM SAKAI_REALM_ROLE_DESC WHERE REALM_KEY IN (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)";
	}

	public String getDeleteRealmRoleFunction1Sql()
	{
		return "DELETE FROM SAKAI_REALM_RL_FN" + " WHERE REALM_KEY IN (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)"
				+ " AND ROLE_KEY IN (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = ?)"
				+ " AND FUNCTION_KEY IN (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = ?)";
	}

	public String getDeleteRealmRoleFunction2Sql()
	{
		return "DELETE FROM SAKAI_REALM_RL_FN WHERE REALM_KEY IN (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)";
	}

	public String getDeleteRealmRoleGroup1Sql()
	{
		return "DELETE FROM SAKAI_REALM_RL_GR" + " WHERE REALM_KEY IN (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)"
				+ " AND ROLE_KEY IN (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = ?)" + " AND USER_ID = ? AND ACTIVE = ? AND PROVIDED = ?";
	}

	public String getDeleteRealmRoleGroup2Sql()
	{
		return "DELETE FROM SAKAI_REALM_RL_GR WHERE REALM_KEY IN (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)";
	}

	public String getDeleteRealmRoleGroup3Sql()
	{
		return "delete from SAKAI_REALM_RL_GR where REALM_KEY = ? and USER_ID = ?";
	}

	public String getDeleteRealmRoleGroup4Sql()
	{
		return "DELETE FROM SAKAI_REALM_RL_GR WHERE REALM_KEY IN (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?) AND USER_ID = ?";
	}

	public String getInsertRealmFunctionSql()
	{
		return "insert into SAKAI_REALM_FUNCTION (FUNCTION_KEY, FUNCTION_NAME) values (NEXT VALUE FOR SAKAI_REALM_FUNCTION_SEQ, ?)";
	}

	public String getInsertRealmProviderSql()
	{
		return "INSERT INTO SAKAI_REALM_PROVIDER (REALM_KEY, PROVIDER_ID) VALUES ( (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?), ?)";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleDescription1Sql()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleDescription2Sql()
	{
		return null;
	}

	public String getInsertRealmRoleDescriptionSql()
	{
		return "INSERT INTO SAKAI_REALM_ROLE_DESC (REALM_KEY, ROLE_KEY, DESCRIPTION, PROVIDER_ONLY) VALUES ("
				+ " (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)," + " (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = ?), ?, ?)";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleFunction1Sql()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleFunction2Sql()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleFunction3Sql()
	{
		return null;
	}

	public String getInsertRealmRoleFunctionSql()
	{
		return "INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES ("
				+ " (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)," + " (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = ?),"
				+ " (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = ?))";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleGroup1_1Sql()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleGroup1_2Sql()
	{
		return null;
	}

	public String getInsertRealmRoleGroup1Sql()
	{
		return "INSERT INTO SAKAI_REALM_RL_GR (REALM_KEY, USER_ID, ROLE_KEY, ACTIVE, PROVIDED) VALUES ("
				+ " (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?), ?, "
				+ " (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = ?), ?, ?)";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleGroup2_1Sql()
	{
		return null;
	}

	public String getInsertRealmRoleGroup2Sql()
	{
		return "insert into SAKAI_REALM_RL_GR (REALM_KEY, USER_ID, ROLE_KEY, ACTIVE, PROVIDED) values (?, ?, (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = ?), '1', '1')";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleGroup3_1Sql()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInsertRealmRoleGroup3_2Sql()
	{
		return null;
	}

	public String getInsertRealmRoleGroup3Sql()
	{
		return "insert into SAKAI_REALM_RL_GR (REALM_KEY, USER_ID, ROLE_KEY, ACTIVE, PROVIDED) values ((select REALM_KEY from SAKAI_REALM where REALM_ID = ?), ?, (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = ?), ?, ?)";
	}

	public String getInsertRealmRoleSql()
	{
		return "insert into SAKAI_REALM_ROLE (ROLE_KEY, ROLE_NAME) values (NEXT VALUE FOR SAKAI_REALM_ROLE_SEQ, ?)";
	}

	public String getSelectRealmFunction1Sql()
	{
		return "select FUNCTION_NAME from SAKAI_REALM_FUNCTION";
	}

	public String getSelectRealmFunction2Sql()
	{
		return "SELECT RR.ROLE_NAME, RF.FUNCTION_NAME FROM SAKAI_REALM_RL_FN RRF"
				+ " INNER JOIN SAKAI_REALM R ON RRF.REALM_KEY = R.REALM_KEY AND R.REALM_ID = ?"
				+ " INNER JOIN SAKAI_REALM_ROLE RR ON RRF.ROLE_KEY = RR.ROLE_KEY"
				+ " INNER JOIN SAKAI_REALM_FUNCTION RF ON RRF.FUNCTION_KEY = RF.FUNCTION_KEY";
	}

	public String getSelectRealmFunctionFunctionNameSql(String inClause)
	{
		StringBuilder sqlBuf = new StringBuilder();

		sqlBuf.append("select DISTINCT FUNCTION_NAME ");
		sqlBuf.append("from SAKAI_REALM_FUNCTION SRF ");
		sqlBuf.append("inner join SAKAI_REALM_RL_FN SRRF on SRF.FUNCTION_KEY = SRRF.FUNCTION_KEY ");
		sqlBuf.append("inner join SAKAI_REALM_ROLE SRR on SRRF.ROLE_KEY = SRR.ROLE_KEY ");
		sqlBuf.append("inner join SAKAI_REALM SR on SRRF.REALM_KEY = SR.REALM_KEY ");
		sqlBuf.append("where SRR.ROLE_NAME = ? ");
		sqlBuf.append("and " + inClause);
		return sqlBuf.toString();
	}

	public String getSelectRealmIdSql()
	{
		return "select sr.REALM_ID from SAKAI_REALM sr INNER JOIN SAKAI_REALM_PROVIDER srp on sr.REALM_KEY = srp.REALM_KEY where srp.PROVIDER_ID=?";
	}

	public String getSelectRealmIdSql(Collection azGroups)
	{
		StringBuilder sqlBuilder = new StringBuilder();
		String sql = "select     SR.REALM_ID " + "from       SAKAI_REALM_FUNCTION SRF "
				+ "inner join SAKAI_REALM_RL_FN SRRF on SRF.FUNCTION_KEY = SRRF.FUNCTION_KEY "
				+ "inner join SAKAI_REALM_RL_GR SRRG on SRRF.ROLE_KEY = SRRG.ROLE_KEY and SRRF.REALM_KEY = SRRG.REALM_KEY "
				+ "inner join SAKAI_REALM SR on SRRF.REALM_KEY = SR.REALM_KEY "
				+ "where      SRF.FUNCTION_NAME = ? and SRRG.USER_ID = ? and SRRG.ACTIVE = '1' ";

		sqlBuilder.append(sql);
		if (azGroups != null)
		{
			sqlBuilder.append("and SR.REALM_ID in (");
			for (int i = 0; i < azGroups.size() - 1; i++)
				sqlBuilder.append("?,");

			sqlBuilder.append("?) ");
		}
		return sqlBuilder.toString();
	}
	
	public String getSelectRealmIdRoleSwapSql(Collection azGroups)
	{
		StringBuilder sqlBuilder = new StringBuilder();
		String sql = "select     SR.REALM_ID " + "from       SAKAI_REALM_FUNCTION SRF "
				+ "inner join SAKAI_REALM_RL_FN SRRF on SRF.FUNCTION_KEY = SRRF.FUNCTION_KEY "
				+ "inner join SAKAI_REALM_RL_GR SRRG on SRRF.REALM_KEY = SRRG.REALM_KEY "
				+ "inner join SAKAI_REALM SR on SRRF.REALM_KEY = SR.REALM_KEY "
				+ "join SAKAI_REALM_ROLE ROLE on ROLE.ROLE_KEY = SRRF.ROLE_KEY "
				+ "where      SRF.FUNCTION_NAME = ? and SRRG.USER_ID = ? and SRRG.ACTIVE = '1' ";

		sqlBuilder.append(sql);
		if (azGroups != null)
		{
			sqlBuilder.append("and SR.REALM_ID in (");
			for (int i = 0; i < azGroups.size() - 1; i++)
				sqlBuilder.append("?,");
		
			sqlBuilder.append("?) ");
		}
		sqlBuilder.append("and ROLE.ROLE_NAME = ? "); 
		return sqlBuilder.toString();
	}

	public String getSelectRealmsProviderIDsSql(String inClause)
	{
		return "SELECT r.realm_id, r.provider_id FROM SAKAI_REALM r WHERE " + inClause;
	}

	public String getSelectRealmProvider2Sql()
	{
		return "SELECT RR.ROLE_NAME, RRD.DESCRIPTION, RRD.PROVIDER_ONLY FROM SAKAI_REALM_ROLE_DESC RRD"
				+ " INNER JOIN SAKAI_REALM R ON RRD.REALM_KEY = R.REALM_KEY AND R.REALM_ID = ?"
				+ " INNER JOIN SAKAI_REALM_ROLE RR ON RRD.ROLE_KEY = RR.ROLE_KEY";
	}

	public String getSelectRealmProviderId1Sql()
	{
		return "select srp.PROVIDER_ID from SAKAI_REALM sr INNER JOIN SAKAI_REALM_PROVIDER srp on sr.REALM_KEY = srp.REALM_KEY where sr.REALM_ID=?";
	}

	public String getSelectRealmProviderId2Sql()
	{
		return "SELECT RP.PROVIDER_ID FROM SAKAI_REALM_PROVIDER RP INNER JOIN SAKAI_REALM R ON RP.REALM_KEY = R.REALM_KEY AND R.REALM_ID = ?";
	}

	public String getSelectRealmProviderSql(String inClause)
	{
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("select distinct SRP.REALM_KEY, SR.PROVIDER_ID ");
		sqlBuf.append("from SAKAI_REALM_PROVIDER SRP ");
		sqlBuf.append("inner join SAKAI_REALM SR on SRP.REALM_KEY = SR.REALM_KEY ");
		sqlBuf.append("where " + inClause);
		return sqlBuf.toString();
	}

	public String getSelectRealmRoleDescriptionSql()
	{
		return "SELECT SAKAI_REALM_ROLE.ROLE_NAME, SAKAI_REALM_ROLE_DESC.DESCRIPTION, SAKAI_REALM_ROLE_DESC.PROVIDER_ONLY"
				+ " FROM SAKAI_REALM_ROLE_DESC"
				+ " INNER JOIN SAKAI_REALM ON SAKAI_REALM.REALM_KEY = SAKAI_REALM_ROLE_DESC.REALM_KEY AND SAKAI_REALM.REALM_ID = ?"
				+ " INNER JOIN SAKAI_REALM_ROLE ON SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_ROLE_DESC.ROLE_KEY";
	}

	public String getSelectRealmRoleFunctionSql()
	{
		return "SELECT SAKAI_REALM_ROLE.ROLE_NAME, SAKAI_REALM_FUNCTION.FUNCTION_NAME FROM SAKAI_REALM_RL_FN"
				+ " INNER JOIN SAKAI_REALM ON SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_FN.REALM_KEY AND SAKAI_REALM.REALM_ID = ?"
				+ " INNER JOIN SAKAI_REALM_ROLE ON SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_FN.ROLE_KEY"
				+ " INNER JOIN SAKAI_REALM_FUNCTION ON SAKAI_REALM_FUNCTION.FUNCTION_KEY = SAKAI_REALM_RL_FN.FUNCTION_KEY";
	}

	public String getSelectRealmRoleGroup1Sql()
	{
		return "SELECT SAKAI_REALM_ROLE.ROLE_NAME, SAKAI_REALM_RL_GR.USER_ID, SAKAI_REALM_RL_GR.ACTIVE, SAKAI_REALM_RL_GR.PROVIDED"
				+ " FROM SAKAI_REALM_RL_GR"
				+ " INNER JOIN SAKAI_REALM ON SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_GR.REALM_KEY AND SAKAI_REALM.REALM_ID = ?"
				+ " INNER JOIN SAKAI_REALM_ROLE ON SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_GR.ROLE_KEY";
	}

	public String getSelectRealmRoleGroup2Sql()
	{
		return "SELECT RRG.USER_ID, RR.ROLE_NAME, RRG.ACTIVE, RRG.PROVIDED FROM SAKAI_REALM_RL_GR RRG "
				+ " INNER JOIN SAKAI_REALM R ON RRG.REALM_KEY = R.REALM_KEY AND R.REALM_ID = ?"
				+ " INNER JOIN SAKAI_REALM_ROLE RR ON RRG.ROLE_KEY = RR.ROLE_KEY";
	}

	public String getSelectRealmRoleGroup3Sql()
	{
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("select SRRG.REALM_KEY, SRR.ROLE_NAME, SRRG.ACTIVE, SRRG.PROVIDED ");
		sqlBuf.append("from SAKAI_REALM_ROLE SRR ");
		sqlBuf.append("inner join SAKAI_REALM_RL_GR SRRG on SRR.ROLE_KEY = SRRG.ROLE_KEY ");
		sqlBuf.append("where SRRG.USER_ID = ?");
		return sqlBuf.toString();
	}

	public String getSelectRealmUserGroupSql( String inClause )
	{
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("select SAKAI_REALM.REALM_ID FROM SAKAI_REALM, SAKAI_REALM_RL_GR WHERE ");
		sqlBuf.append("SAKAI_REALM.REALM_KEY=SAKAI_REALM_RL_GR.REALM_KEY ");
		sqlBuf.append("and SAKAI_REALM_RL_GR.REALM_KEY=SAKAI_REALM.REALM_KEY ");
		sqlBuf.append("and " );
		sqlBuf.append( inClause );
		sqlBuf.append(" and SAKAI_REALM_RL_GR.USER_ID = ?");
		return sqlBuf.toString();
	}
	
	public String getSelectRealmRoleUserIdSql(String inClause)
	{
		StringBuilder sqlBuf = new StringBuilder();

		sqlBuf.append("SELECT USER_ID ");
		sqlBuf.append("FROM SAKAI_REALM SR INNER JOIN SAKAI_REALM_RL_GR SRRG ON SR.REALM_KEY = SRRG.REALM_KEY ");
		sqlBuf.append("INNER JOIN SAKAI_REALM_RL_FN SRRF ON SRRF.ROLE_KEY = SRRG.ROLE_KEY AND SRRF.REALM_KEY = SR.REALM_KEY ");
		sqlBuf.append("INNER JOIN SAKAI_REALM_FUNCTION SRF ON SRRF.FUNCTION_KEY = SRF.FUNCTION_KEY ");
		sqlBuf.append("WHERE FUNCTION_NAME = ? and SRRG.ACTIVE = '1' and " + inClause + " ");

		return sqlBuf.toString();
	}

	public String getSelectRealmRoleGroupUserIdSql(String inClause)
	{
		StringBuilder sqlBuf = new StringBuilder();

		sqlBuf.append("SELECT USER_ID, REALM_ID ");
		sqlBuf.append("FROM SAKAI_REALM SR INNER JOIN SAKAI_REALM_RL_GR SRRG ON SR.REALM_KEY = SRRG.REALM_KEY ");
		sqlBuf.append("INNER JOIN SAKAI_REALM_RL_FN SRRF ON SRRF.ROLE_KEY = SRRG.ROLE_KEY AND SRRF.REALM_KEY = SR.REALM_KEY ");
		sqlBuf.append("INNER JOIN SAKAI_REALM_FUNCTION SRF ON SRRF.FUNCTION_KEY = SRF.FUNCTION_KEY ");
		sqlBuf.append("WHERE FUNCTION_NAME = ? and SRRG.ACTIVE = '1' and " + inClause + " ");
	
		return sqlBuf.toString();
	}
	
	public String getSelectRealmRoleGroupUserCountSql(String inClause)
	{
		StringBuilder sqlBuf = new StringBuilder();
		
		sqlBuf.append("SELECT REALM_ID, COUNT(REALM_ID) ");
		sqlBuf.append("FROM SAKAI_REALM SR INNER JOIN SAKAI_REALM_RL_GR SRRG ON SR.REALM_KEY = SRRG.REALM_KEY ");
		sqlBuf.append("INNER JOIN SAKAI_REALM_RL_FN SRRF ON SRRF.ROLE_KEY = SRRG.ROLE_KEY AND SRRF.REALM_KEY = SR.REALM_KEY ");
		sqlBuf.append("INNER JOIN SAKAI_REALM_FUNCTION SRF ON SRRF.FUNCTION_KEY = SRF.FUNCTION_KEY ");	
		sqlBuf.append("WHERE FUNCTION_NAME = ? and SRRG.ACTIVE = '1' and " + inClause + " ");
		sqlBuf.append("GROUP BY REALM_ID");
	
		return sqlBuf.toString();		
	}
	
	public String getSelectRealmRoleNameSql()
	{
		return "select SRR.ROLE_NAME from SAKAI_REALM_RL_GR SRRG " + "inner join SAKAI_REALM SR on SRRG.REALM_KEY = SR.REALM_KEY "
				+ "inner join SAKAI_REALM_ROLE SRR on SRRG.ROLE_KEY = SRR.ROLE_KEY "
				+ "where SR.REALM_ID = ? and SRRG.USER_ID = ? and SRRG.ACTIVE = '1'";
	}
	
	public String getSelectRealmRolesSql(String inClause)
	{
		return "select SR.REALM_ID, SRR.ROLE_NAME from SAKAI_REALM_RL_GR SRRG " + "inner join SAKAI_REALM SR on SRRG.REALM_KEY = SR.REALM_KEY "
				+ "inner join SAKAI_REALM_ROLE SRR on SRRG.ROLE_KEY = SRR.ROLE_KEY "
				+ "where SRRG.USER_ID = ? and SRRG.ACTIVE = '1' and " + inClause + " ";
	}

	public String getSelectRealmRoleSql()
	{
		return "select ROLE_NAME, ROLE_KEY from SAKAI_REALM_ROLE";
	}

	public String getSelectRealmRoleKeySql()
	{
		return "select ROLE_NAME, ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = ?";
	}
	
	public String getSelectRealmSize()
	{
		return "select COUNT(REALM_KEY) from SAKAI_REALM_RL_GR where REALM_KEY = ?";
	}

	public String getSelectRealmUpdate()
	{
		return "select REALM_KEY from SAKAI_REALM where REALM_ID = ? FOR UPDATE";
	}

	public String getSelectRealmUserRoleSql(String inClause)
	{
		return "select SRRG.USER_ID, SRR.ROLE_NAME from SAKAI_REALM_RL_GR SRRG " + "inner join SAKAI_REALM SR on SRRG.REALM_KEY = SR.REALM_KEY "
				+ "inner join SAKAI_REALM_ROLE SRR on SRRG.ROLE_KEY = SRR.ROLE_KEY " + "where SR.REALM_ID = ? and " + inClause
				+ " and SRRG.ACTIVE = '1'";
	}

	public String getSelectRealmUsersInGroupsSql( String inClause)
	{
		return "select SRRG.USER_ID from SAKAI_REALM_RL_GR SRRG inner join SAKAI_REALM SR ON SRRG.REALM_KEY = SR.REALM_KEY where SRRG.ACTIVE = '1' and " + inClause;
	}

    public String getMaintainRolesSql() {
        return "SELECT ROLE_NAME FROM SAKAI_REALM_ROLE WHERE ROLE_KEY IN (SELECT DISTINCT MAINTAIN_ROLE FROM SAKAI_REALM WHERE MAINTAIN_ROLE IS NOT NULL)";
    }

	@Override
	public String getSelectRealmLocksSql() {
		return "SELECT REALM_KEY, REFERENCE, LOCK_MODE FROM SAKAI_REALM_LOCKS WHERE REALM_KEY = (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)";
	}

	@Override
	public String getInsertRealmLocksSql() {
		return "INSERT INTO SAKAI_REALM_LOCKS (REALM_KEY, REFERENCE, LOCK_MODE) VALUES ((SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?), ?, ?)";
	}

	public String getDeleteRealmLocksForRealmSql()
	{
		return "DELETE FROM SAKAI_REALM_LOCKS WHERE REALM_KEY = (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?)";
	}

	@Override
	public String getDeleteRealmLocksForRealmWithReferenceSql() {
		return "DELETE FROM SAKAI_REALM_LOCKS WHERE REALM_KEY = (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = ?) AND REFERENCE = ?";
	}
}
