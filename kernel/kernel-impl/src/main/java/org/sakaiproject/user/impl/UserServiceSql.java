/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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

package org.sakaiproject.user.impl;

/**
 * database methods.
 */
public interface UserServiceSql
{
	/**
	 * return the sql statement which deletes an external user id for a given user from the sakai_user_id_map table.
	 */
	String getDeleteUserIdSql();

	/**
	 * return the sql statement which inserts a user id and an external user id into the sakai_user_id_map table.
	 */
	String getInsertUserIdSql();

	/**
	 * return the sql statement which updates an external user id for a given user in the sakai_user_id_map table.
	 */
	String getUpdateUserIdSql();

	/**
	 * return the sql statement which retrieves an external user id for a given user from the sakai_user_id_map table.
	 */
	String getUserEidSql();

	/**
	 * return the sql statement which retrieves the user id for a given user from the sakai_user_id_map table.
	 */
	String getUserIdSql();

	/**
	 * return the sql statement which retrieves the where clause from the sakai_user_id_map table.
	 */
	String getUserWhereSql();
}
