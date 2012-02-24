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

package org.sakaiproject.event.impl;

import java.util.List;

/**
 * database methods.
 */
public interface UsageSessionServiceSql
{
	/**
	 * returns the sql statement which inserts a sakai session into the sakai_session table.
	 */
	String getInsertSakaiSessionSql();

	/**
	 * returns the sql statement which retrieves a sakai session from the sakai_session table for a given session id.
	 */
	String getSakaiSessionSql1();

	/**
	 * returns the sql statement which retrieves all the open sakai sessions from the sakai_session table.
	 */
	String getSakaiSessionSql2();

	/**
	 * returns the sql statement which retrieves all the active sakai sessions from the sakai_session table based on a join column and criteria.
	 */
	String getSakaiSessionSql3(String alias, String joinAlias, String joinTable, String joinColumn, String joinCriteria);

	/**
	 * returns the sql statement which updates a sakai session in the sakai_session table for a given session id.
	 */
	String getUpdateSakaiSessionSql();

	/**
	 * returns the sql statement which updates the SESSION_SERVER column in the sakai_session table for a given session id.
	 */
	String getUpdateServerSakaiSessionSql();
	
	/**
	 * @return the SQL statement which retrieves all supposedly active sessions associated with inactive servers
	 */
	String getOpenSessionsOnInvalidServersSql(List<String> validServerIds);

	/**
	 * returns the sql statement which counts the number of sessions in the sessions table
	 */
	String getSessionsCountSql();
}
