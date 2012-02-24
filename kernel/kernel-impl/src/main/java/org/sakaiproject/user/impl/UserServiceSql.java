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
	
	/**
	 * Return a "SELECT... WHERE... IN" statement to find multiple user records by EID in a single query.
	 * The EID value count is used to generate the correct "(?, ?, ?)" string. 
	 */
	String getUsersWhereEidsInSql(int numberOfSearchValues);
	
	/**
	 * Return a "SELECT... WHERE... IN" statement to find multiple user records (with their EIDs) by ID
	 * in a single query. The ID value count is used to generate the correct "(?, ?, ?)" string. 
	 */
	String getUsersWhereIdsInSql(int numberOfSearchValues);
	
	/**
	 * The maximum size of a "SELECT... WHERE... IN" query varies by database, but when it's reached, the
	 * error can be difficult to interpret. This should be set to a reasonably safe value and used by
	 * clients to break very long queries into a set of somewhat shorter ones. 
	 */
	int getMaxInputsForSelectWhereInQueries();
}
