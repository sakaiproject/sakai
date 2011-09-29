/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/UsageSessionServiceSqlDefault.java $
 * $Id: UsageSessionServiceSqlDefault.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
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
package org.sakaiproject.event.impl;

import java.util.List;


/**
 * methods for accessing session usage data in a database.
 */
public class UsageSessionServiceSqlDefault implements UsageSessionServiceSql
{
	protected static final String USAGE_SESSION_COLUMNS = "SESSION_ID,SESSION_SERVER,SESSION_USER,SESSION_IP,SESSION_HOSTNAME,SESSION_USER_AGENT,SESSION_START,SESSION_END,SESSION_ACTIVE";
	protected static final String MOST_RECENT_USAGE_SESSION_COLUMNS = "SESSION_ID,SESSION_SERVER,SESSION_USER,SESSION_IP,SESSION_HOSTNAME,SESSION_USER_AGENT,MAX(SESSION_START) as SESSION_START,SESSION_END,SESSION_ACTIVE";

   /**
	 * returns the sql statement which inserts a sakai session into the sakai_session table.
	 */
	public String getInsertSakaiSessionSql()
	{
		return "insert into SAKAI_SESSION (" + USAGE_SESSION_COLUMNS + ") values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}

	/**
	 * returns the sql statement which retrieves a sakai session from the sakai_session table for a given session id.
	 */
	public String getSakaiSessionSql1()
	{
		return "select " + USAGE_SESSION_COLUMNS + " from SAKAI_SESSION where SESSION_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves all the open sakai sessions from the sakai_session table.
	 */
	public String getSakaiSessionSql2()
	{
		return "select " + USAGE_SESSION_COLUMNS + " from SAKAI_SESSION where SESSION_ACTIVE=1 ORDER BY SESSION_SERVER ASC, SESSION_START ASC";
	}

	/**
	 * returns the sql statement which retrieves all the active sakai sessions from the sakai_session table based on a join column and criteria.
	 */
   public String getSakaiSessionSql3(String alias, String joinAlias, String joinTable, String joinColumn, String joinCriteria)
   {
      return "select " + alias + ".SESSION_ID," + alias + ".SESSION_SERVER," + alias + ".SESSION_USER," + alias + ".SESSION_IP," + alias + ".SESSION_HOSTNAME," + alias + ".SESSION_USER_AGENT," + alias + ".SESSION_START," + alias + ".SESSION_END," + alias + ".SESSION_ACTIVE " +
             "from   SAKAI_SESSION " + alias                                    + " " +
             "inner join " + joinTable + " " + joinAlias                        + " " +
             "ON "    + alias + ".SESSION_ID = " + joinAlias + "." + joinColumn + " " +
             "where " + alias + ".SESSION_ACTIVE=1 and " + joinCriteria;
   }

   /**
    * returns the sql statement which updates a sakai session in the sakai_session table for a given session id.
    */
	public String getUpdateSakaiSessionSql()
	{
		return "update SAKAI_SESSION set SESSION_END = ?, SESSION_ACTIVE = ? where SESSION_ID = ?";
	}

	public String getOpenSessionsOnInvalidServersSql(List<String> validServerIds)
	{
		StringBuilder sql = new StringBuilder("select "+ USAGE_SESSION_COLUMNS + " from SAKAI_SESSION where SESSION_ACTIVE=1 and SESSION_SERVER not in (");
		for (int i = 0; i < validServerIds.size(); i++)
		{
			String serverId = validServerIds.get(i);
			if (i > 0) sql.append(",");
			sql.append("'").append(serverId).append("'");
		}
		sql.append(")");
		
		return sql.toString();
	}

	/**
	 * @return the sql statement which updates the SESSION_SERVER column in the sakai_session table for a given session id.
	 */
	public String getUpdateServerSakaiSessionSql() {
		return "update SAKAI_SESSION set SESSION_SERVER = ? where SESSION_ID = ?";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.impl.ClusterEventTrackingServiceSql#getSessionsCountSql()
	 */
	public String getSessionsCountSql() {
		return "select COUNT(*) from SAKAI_SESSION";
	}


}
