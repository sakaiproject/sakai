/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/cluster/cluster-impl/impl/src/java/org/sakaiproject/cluster/impl/SakaiClusterServiceSqlDefault.java $
 * $Id: SakaiClusterServiceSqlDefault.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
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

package org.sakaiproject.cluster.impl;

/**
 * methods for accessing cluster data in a database.
 */
public class SakaiClusterServiceSqlDefault implements ClusterServiceSql
{
	/**
	 * returns the sql statement for deleting locks for a given session from the sakai_locks table.
	 */
	public String getDeleteLocksSql()
	{
		return "delete from SAKAI_LOCKS where USAGE_SESSION_ID = ?";
	}
	
	/**
	 * Because the most efficient form of this query requires knowing the internals of
	 * both the SAKAI_LOCKS and SAKAI_SESSION tables, it doesn't fit easily into either
	 * the Event or Db modules.
	 * 
	 * @return the SQL statement to find closed or deleted sessions referred to by lock records
	 */
	public String getOrphanedLockSessionsSql()
	{
		return "select distinct l.USAGE_SESSION_ID from SAKAI_LOCKS l left join SAKAI_SESSION s on l.USAGE_SESSION_ID = s.SESSION_ID where s.SESSION_ACTIVE is null";
	}

	/**
	 * returns the sql statement for deleting a server from the sakai_cluster table.
	 */
	public String getDeleteServerSql()
	{
		return "delete from SAKAI_CLUSTER where SERVER_ID_INSTANCE = ?";
	}

	/**
	 * returns the sql statement for inserting a server id into the sakai_cluster table.
	 */
	public String getInsertServerSql()
	{
		return "insert into SAKAI_CLUSTER (SERVER_ID_INSTANCE, UPDATE_TIME, STATUS, SERVER_ID) values (?, " + sqlTimestamp() + ", ?, ?)";
	}

	/**
    * returns the sql statement for obtaining a list of expired sakai servers from the sakai_cluster table.
    * <br/>br/>
    * @param timeout  how long (in seconds) we give an app server to respond before it is considered lost.
	 */
	public String getListExpiredServers(long timeout)
	{
		return "select SERVER_ID_INSTANCE from SAKAI_CLUSTER where SERVER_ID_INSTANCE != ? and DATEDIFF('ss', UPDATE_TIME, CURRENT_TIMESTAMP) >= " + timeout;
	}

	/**
	 * returns the sql statement for obtaining a list of sakai servers from the sakai_cluster table in server_id order.
	 */
	public String getListServersSql()
	{
		return "select SERVER_ID_INSTANCE from SAKAI_CLUSTER order by SERVER_ID_INSTANCE asc";
	}

	/**
	 * returns the sql statement for retrieving a particular server from the sakai_cluster table.
	 */
	public String getReadServerSql()
	{
		return "select SERVER_ID_INSTANCE, STATUS from SAKAI_CLUSTER where SERVER_ID_INSTANCE = ?";
	}

	/**
	 * returns the sql statement for updating a server in the sakai_cluster table.
	 */
	public String getUpdateServerSql()
	{
		return "update SAKAI_CLUSTER set UPDATE_TIME = " + sqlTimestamp() + ", STATUS = ?, SERVER_ID = ? where SERVER_ID_INSTANCE = ?";
	}

	@Override
	public String getListServerStatusSql()
	{
		return "SELECT SERVER_ID_INSTANCE, STATUS, SERVER_ID, UPDATE_TIME FROM SAKAI_CLUSTER ORDER BY SERVER_ID_INSTANCE ASC";
	}

	/**
	 * returns the current timestamp.
	 */
	public String sqlTimestamp()
	{
		return "CURRENT_TIMESTAMP";
	}

}
