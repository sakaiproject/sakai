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
import java.util.Set;
/**
 * methods for accessing authz data in an oracle database.
 */
public class DbAuthzGroupSqlOracle extends DbAuthzGroupSqlDefault
{
	/* KNL-382 */
	private static final int ORA_1795_LIMIT = 999;
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
	
	@Override
	public String getCountRealmRoleFunctionSql(Set<Integer> roleIds, String inClause)
	{
		StringBuilder rolePlaceholders = new StringBuilder();

		for (Integer roleId : roleIds) {
			if (rolePlaceholders.length() > 0) {
				rolePlaceholders.append(", ");
			}

			rolePlaceholders.append("?");
		}

		return "SELECT 1 FROM SAKAI_REALM_RL_FN srrf, SAKAI_REALM_FUNCTION srf, (select realm_key, role_key from SAKAI_REALM_RL_GR where ACTIVE = '1' and USER_ID = ? union select -1 as realm_key, -1 as role_key from dual) srrg WHERE rownum = 1 AND srrf.realm_key in (select realm_key from SAKAI_REALM where " + inClause + ") AND srrf.function_key = srf.function_key AND srf.function_name = ? AND ((srrf.role_key = srrg.role_key AND srrg.realm_key in (select realm_key from SAKAI_REALM where " + inClause + ")) OR srrf.role_key in (" + rolePlaceholders + "))";
	}


	@Override
	public String getSelectRealmIdSql(Collection azGroups)
	{
		StringBuilder sqlBuilder = new StringBuilder();
		String sql = "select     SR.REALM_ID " + "from       SAKAI_REALM_FUNCTION SRF "
				+ "inner join SAKAI_REALM_RL_FN SRRF on SRF.FUNCTION_KEY = SRRF.FUNCTION_KEY "
				+ "inner join SAKAI_REALM_RL_GR SRRG on SRRF.ROLE_KEY = SRRG.ROLE_KEY and SRRF.REALM_KEY = SRRG.REALM_KEY "
				+ "inner join SAKAI_REALM SR on SRRF.REALM_KEY = SR.REALM_KEY "
				+ "where      SRF.FUNCTION_NAME = ? and SRRG.USER_ID = ? and SRRG.ACTIVE = '1' ";

		sqlBuilder.append(sql);
		if (azGroups != null && azGroups.size()>0)
		{
			sqlBuilder.append("and (SR.REALM_ID in (");
			for (int k = 0,size = 0;size<azGroups.size()-1 && k<ORA_1795_LIMIT-1;size++,k++) {
				sqlBuilder.append("?,");
				if (k==ORA_1795_LIMIT-2 && size<azGroups.size()-2) {
					k=-1; size++; sqlBuilder.append("?) or SR.REALM_ID in (");
				}
			}
			sqlBuilder.append("?))");
		}
		return sqlBuilder.toString();
	}
}
