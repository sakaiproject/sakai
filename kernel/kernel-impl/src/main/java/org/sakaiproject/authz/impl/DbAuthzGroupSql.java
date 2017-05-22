/**********************************************************************************
 * $URL$
 * $Id$
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
 * database methods.
 */
public interface DbAuthzGroupSql
{
	String getCountRealmFunctionSql();
	
	String getCountRealmRoleFunctionEndSql(Set<Integer> roleIds, String inClause);

	String getCountRealmRoleFunctionSql(Set<Integer> roleIds);
 
	String getCountRealmRoleFunctionSql(Set<Integer> roleIds, String inClause);

	String getCountRealmRoleSql();
	
	String getCountRoleFunctionSql(String inClause, boolean isDelegated);

	String getDeleteRealmProvider1Sql();

	String getDeleteRealmProvider2Sql();

	String getDeleteRealmRoleDescription1Sql();

	String getDeleteRealmRoleDescription2Sql();

	String getDeleteRealmRoleFunction1Sql();

	String getDeleteRealmRoleFunction2Sql();

	String getDeleteRealmRoleGroup1Sql();

	String getDeleteRealmRoleGroup2Sql();

	String getDeleteRealmRoleGroup3Sql();

	String getDeleteRealmRoleGroup4Sql();

	String getInsertRealmFunctionSql();

	String getInsertRealmProviderSql();

	String getInsertRealmRoleDescription1Sql();

	String getInsertRealmRoleDescription2Sql();

	String getInsertRealmRoleDescriptionSql();

	String getInsertRealmRoleFunction1Sql();

	String getInsertRealmRoleFunction2Sql();

	String getInsertRealmRoleFunction3Sql();

	String getInsertRealmRoleFunctionSql();

	String getInsertRealmRoleGroup1_1Sql();

	String getInsertRealmRoleGroup1_2Sql();

	String getInsertRealmRoleGroup1Sql();

	String getInsertRealmRoleGroup2_1Sql();

	String getInsertRealmRoleGroup2Sql();

	String getInsertRealmRoleGroup3_1Sql();

	String getInsertRealmRoleGroup3_2Sql();

	String getInsertRealmRoleGroup3Sql();

	String getInsertRealmRoleSql();

	String getSelectRealmFunction1Sql();

	String getSelectRealmFunction2Sql();

	String getSelectRealmFunctionFunctionNameSql(String inClause);

	String getSelectRealmIdSql();

	String getSelectRealmIdSql(Collection azGroups);
	
	String getSelectRealmIdRoleSwapSql(Collection azGroups);

	String getSelectRealmProvider2Sql();

	String getSelectRealmProviderId1Sql();

	String getSelectRealmProviderId2Sql();

	String getSelectRealmsProviderIDsSql(String inClause);

	String getSelectRealmProviderSql(String inClause);

	String getSelectRealmRoleDescriptionSql();

	String getSelectRealmRoleFunctionSql();

	String getSelectRealmRoleGroup1Sql();

	String getSelectRealmRoleGroup2Sql();

	String getSelectRealmRoleGroup3Sql();

	String getSelectRealmUserGroupSql( String inClause );

	String getSelectRealmRoleUserIdSql(String inClause);

	String getSelectRealmRoleGroupUserIdSql(String inClause);
	
	String getSelectRealmRoleGroupUserCountSql(String inClause);
	
	String getSelectRealmRoleNameSql();

	String getSelectRealmRoleSql();
	
	String getSelectRealmRoleKeySql();

	String getSelectRealmRolesSql(String inClause);
	
	String getSelectRealmSize();

	String getSelectRealmUpdate();

	String getSelectRealmUserRoleSql(String inClause);

	String getSelectRealmUsersInGroupsSql( String inClause);

    String getMaintainRolesSql();
}
