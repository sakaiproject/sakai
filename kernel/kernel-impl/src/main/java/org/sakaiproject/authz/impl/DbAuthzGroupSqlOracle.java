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
 * methods for accessing authz data in an oracle database.
 */
public class DbAuthzGroupSqlOracle extends DbAuthzGroupSqlDefault
{
	/**
	 * returns the sql statement to write a row into the sakai_function_role table.
	 */
	public String getInsertRealmFunctionSql()
	{
		return "insert into SAKAI_REALM_FUNCTION (FUNCTION_KEY, FUNCTION_NAME) values (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, ?)";
	}

	/**
	 * returns the sql statement to write a row into the sakai_realm_role table.
	 */
	public String getInsertRealmRoleSql()
	{
		return "insert into SAKAI_REALM_ROLE (ROLE_KEY, ROLE_NAME) values (SAKAI_REALM_ROLE_SEQ.NEXTVAL, ?)";
	}
}
