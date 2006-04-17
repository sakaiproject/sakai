/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.presence.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;

/**
 * <p>
 * ClusterPresenceService extends the BasePresenceService with a Storage model that keeps track of presence for a cluster of Sakai app servers, backed by a shared DB table.
 * </p>
 */
public class ClusterPresenceService extends BasePresenceService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(ClusterPresenceService.class);

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
		m_autoDdl = new Boolean(value).booleanValue();
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
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
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
				M_log.warn("setPresence(): dbWrite failed");
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
				M_log.warn("removePresence(): dbWrite failed");
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public List getSessions(String locationId)
		{
			// TODO: Note: this assumes
			// 1) the UsageSessionService has a db component selected.
			// 2) the presence table and the session table are in the same db.

			// form a SQL query to select session ids for this location
			String statement = "select SESSION_ID from SAKAI_PRESENCE where LOCATION_ID = ?";

			// send in the locationId
			Object[] fields = new Object[1];
			fields[0] = locationId;

			// get these from usage session
			List sessions = m_usageSessionService.getSessions(statement, fields);

			return sessions;
		}

		/**
		 * {@inheritDoc}
		 */
		public List getLocations()
		{
			// form the SQL query
			String statement = "select DISTINCT LOCATION_ID from SAKAI_PRESENCE";

			List locs = m_sqlService.dbRead(statement);

			return locs;
		}
	}
}
