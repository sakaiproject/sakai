/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.presence.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.UsageSession;

/**
 * <p>
 * ClusterPresenceService extends the BasePresenceService with a Storage model that keeps track of presence for a cluster of Sakai app servers, backed by a shared DB table.
 * </p>
 */
@Slf4j
public class ClusterPresenceService extends BasePresenceService
{

	/**
	 * Allocate a new storage object.
	 * 
	 * @return A new storage object.
	 */
	protected Storage newStorage()
	{
		return new ClusterStorage();
	}

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = Boolean.valueOf(value);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_presence");
			}

			super.init();
		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class ClusterStorage implements Storage
	{
		/**
		 * {@inheritDoc}
		 */
		public void setPresence(String sessionId, String locationId)
		{
			// send this to the database
			String statement = "insert into SAKAI_PRESENCE (SESSION_ID,LOCATION_ID) values ( ?, ?)";

			// collect the fields
			Object fields[] = new Object[2];
			fields[0] = sessionId;
			fields[1] = locationId;

			// process the insert
			boolean ok = m_sqlService.dbWrite(statement, fields);
			if (!ok)
			{
				log.warn("setPresence(): dbWrite failed");
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void removePresence(String sessionId, String locationId)
		{
			// form the SQL delete statement
			String statement = "delete from SAKAI_PRESENCE" + " where ( SESSION_ID = ? and LOCATION_ID = ?)";

			// setup the fields
			Object[] fields = new Object[1];
			fields[0] = sessionId;

			// process the remove
			boolean ok = m_sqlService.dbWrite(statement, fields, locationId);
			if (!ok)
			{
				log.warn("removePresence(): dbWrite failed");
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public List<String> removeSessionPresence(String sessionId)
		{
			Object[] fields = new Object[1];
			
			// get all the presence for this session
			String statement = "select LOCATION_ID from SAKAI_PRESENCE where SESSION_ID = ?";
			fields[0] = sessionId;
			List<String> presence = m_sqlService.dbRead(statement, fields, null);
	
			// remove all the presence for this session
			statement = "delete from SAKAI_PRESENCE where SESSION_ID = ?";
			boolean ok = m_sqlService.dbWrite(statement, fields);
			if (!ok)
			{
				log.warn("run(): dbWrite failed: " + statement);
			}
	
			return presence;
		}
		
		/**
		 * {@inheritDoc}
		 */
		
		public List<UsageSession> getSessions(String locationId)
		{
			// Note: this assumes
			// 1) the UsageSessionService has a db component selected.
			// 2) the presence table and the session table are in the same db.

			// to join the presence table to the session
			String joinTable = "SAKAI_PRESENCE";
			String joinAlias = "A";
			String joinColumn = "SESSION_ID";
			String joinCriteria = "A.LOCATION_ID = ?";

			// send in the locationId
			Object[] fields = new Object[1];
			fields[0] = locationId;

			// get these from usage session
			List<UsageSession> sessions = m_usageSessionService.getSessions(joinTable, joinAlias, joinColumn, joinCriteria, fields);

			return sessions;
		}

		/**
		 * {@inheritDoc}
		 */
		public List<String> getLocations()
		{
			// form the SQL query
			String statement = "select DISTINCT LOCATION_ID from SAKAI_PRESENCE";

			List<String> locs = m_sqlService.dbRead(statement);

			return locs;
		}
	}
}
