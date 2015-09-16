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

package org.sakaiproject.cluster.impl;

/**
 * database methods.
 */
public interface ClusterServiceSql
{
	/**
	 * returns the sql statement for deleting locks for a given session from the sakai_locks table.
	 */
	String getDeleteLocksSql();

	/**
	 * @return the SQL statement to find lock records belonging to closed or deleted sessions
	 */
	String getOrphanedLockSessionsSql();

	/**
	 * returns the sql statement for deleting a server from the sakai_cluster table.
	 */
	String getDeleteServerSql();

	/**
	 * returns the sql statement for inserting a server id into the sakai_cluster table.
	 */
	String getInsertServerSql();

	/**
	 * returns the sql statement for obtaining a list of expired sakai servers from the sakai_cluster table. <br/>br/>
	 * 
	 * @param timeout
	 *        how long (in seconds) we give an app server to respond before it is considered lost.
	 */
	String getListExpiredServers(long timeout);

	/**
	 * returns the sql statement for obtaining a list of sakai servers from the sakai_cluster table in server_id order.
	 */
	String getListServersSql();

	/**
	 * returns the sql statement for retrieving a particular server from the sakai_cluster table.
	 */
	String getReadServerSql();

	/**
	 * returns the sql statement for updating a server in the sakai_cluster table.
	 */
	String getUpdateServerSql();

	/**
	 * returns the sql statement for listing the servers and their status from the sakai_cluster table.
	 */
	String getListServerStatusSql();

	/**
	 * returns the current timestamp.
	 */
	String sqlTimestamp();


}
