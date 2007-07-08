/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.List;

/**
 * database methods.
 */
public interface DbAuthzGroupSql
{
	String getCountRealmFunctionSql();

	String getCountRealmRoleFunctionSql(String anonymousRole, String authorizationRole, boolean authorized);

	String getCountRealmRoleFunctionSql(String anonymousRole, String authorizationRole, boolean authorized, String inClause);

	String getCountRealmRoleFunctionEndSql(String anonymousRole, String authorizationRole, boolean authorized, String inClause);

	String getCountRealmRoleSql();

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

	String getInsertRealmRoleDescriptionSql();

	String getInsertRealmRoleFunctionSql();

	String getInsertRealmRoleGroup1Sql();

	String getInsertRealmRoleGroup2Sql();

	String getInsertRealmRoleGroup3Sql();

	String getInsertRealmRoleSql();

	String getSelectRealmFunction1Sql();

	String getSelectRealmFunction2Sql();

	String getSelectRealmFunctionFunctionNameSql(String inClause);

	String getSelectRealmIdSql();

	String getSelectRealmIdSql(Collection azGroups);

	String getSelectRealmProvider2Sql();

	String getSelectRealmProviderId1Sql();

	String getSelectRealmProviderId2Sql();

	String getSelectRealmProviderSql(String inClause);

	String getSelectRealmRoleDescriptionSql();

	String getSelectRealmRoleFunctionSql();

	String getSelectRealmRoleGroup1Sql();

	String getSelectRealmRoleGroup2Sql();

	String getSelectRealmRoleGroup3Sql();

	String getSelectRealmRoleGroup4Sql();

	String getSelectRealmRoleGroupUserIdSql(String inClause1, String inClause2);

	String getSelectRealmRoleNameSql();

	String getSelectRealmRoleSql();

	String getSelectRealmUserRoleSql(String inClause);
}
