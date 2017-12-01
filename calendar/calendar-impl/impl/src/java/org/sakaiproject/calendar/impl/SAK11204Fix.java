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

package org.sakaiproject.calendar.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.impl.BaseCalendarService.BaseCalendarEventEdit;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;

/**
 * @author ieb
 */
@Slf4j
public class SAK11204Fix
{
	private static final int MIGRATE = 1;

	private static final int UPGRADE_SCHEMA = 2;

	private static final int OK = 0;

	private SqlService sqlService;

	private ServerConfigurationService serverConfigurationService;

	private BaseCalendarService.Storage storage;

	/**
	 * @param service
	 */
	public SAK11204Fix(DbCalendarService service)
	{
		this.sqlService = service.m_sqlService;
		this.serverConfigurationService = service.m_serverConfigurationService;
		this.storage = service.m_storage;
	}

	/**
	 * 
	 */
	public void apply(boolean autoDDL)
	{
 		// Check for sak11204.disable -- should only be set after successful upgrade completion
		boolean disableUpgrade = serverConfigurationService.getBoolean("sak11204.disable", false);
		if ( disableUpgrade )
			return;

		int upgrade = checkSAK11204ForUpgrade();

		if (upgrade == UPGRADE_SCHEMA)
		{
			if (autoDDL)
			{
				log.info("SAK-11204: Updating Schema ");
				sqlService.ddl(this.getClass().getClassLoader(), "SAK-11204");
				if (checkSAK11204ForUpgrade() == UPGRADE_SCHEMA)
				{
					log.error("SAK-11204: =============================================================================");
					log.error("SAK-11204: Database Upgrade for SAK-11204 Failed, you must investigate and fix before");
					log.error("SAK-11204: continuuing. I attempted to upgrade the schema but this appears to hav failed. You must");
					log.error("SAK-11204: ensure that the columns RANGE_START(BIGINT) and RANGE_END(BIGINT) are present in CALENDAR_EVENT");
					log.error("SAK-11204: and there are indexes on both of the columns.");
					log.error("SAK-11204: Thank you ");
					log.error("SAK-11204: =============================================================================");
					System.exit(-10);
				}
			}
			else
			{
				log.error("SAK-11204: =============================================================================");
				log.error("SAK-11204: Database Upgrade for SAK-11204 Failed, you must investigate and fix before");
				log.error("SAK-11204: continuuing. AutoDDL was OFF, so I could not change the database schema. You must");
				log.error("SAK-11204: ensure that the columns RANGE_START(BIGINT) and RANGE_END(BIGINT) are present in CALENDAR_EVENT");
				log.error("SAK-11204: and there are indexes on both of the columns.");
				log.error("SAK-11204: Thank you ");
				log.error("SAK-11204: =============================================================================");
				System.exit(-10);

			}
			log.info("SAK-11204: Schema Update Sucessfull ");
		}
		boolean forceUpgrade = serverConfigurationService.getBoolean("sak11204.forceupgrade", false);
		if (upgrade == MIGRATE || forceUpgrade)
		{
			// get a list of channels
			// for each channel get a list of events
			// for each event save
			// do this all at the storage layer so that we dont change the
			// calendars
			List<Calendar> calendars = storage.getCalendars();
			int i = 1;
			for (Iterator<Calendar> icalendars = calendars.iterator(); icalendars
					.hasNext();)
			{
				log.info("SAK-11204: Converting Calendar {} of {}", i, calendars.size());
				i++;
				Calendar calendar = icalendars.next();
				List<BaseCalendarEventEdit> levents = storage.getEvents(calendar);
				for (Iterator<BaseCalendarEventEdit> ievents = levents.iterator(); ievents
						.hasNext();)
				{
					BaseCalendarEventEdit event = ievents.next();
					event.activate();
					storage.commitEvent(calendar, event);
				}
			}
		}
		log.info("SAK-11204: Calendar Conversion Complete");
		if (forceUpgrade)
		{
			log.warn("SAK-11204: =========================================================================================================  ");
			log.warn("SAK-11204: This Conversion was forced, please ensure that you remove sak11204.forceupgrade from sakai.properties ");
			log.warn("SAK-11204: If you do not remove sak11204.forceupgrade from sakai.properties this conversion will be performed ");
			log.warn("SAK-11204: every time you start this instance of sakai, and it will take the same ammount of time ");
			log.warn("SAK-11204: =========================================================================================================  ");
		}

	}

	/**
	 * Runs a simple SQL statement to check if the databse has been patched
	 * 
	 * @return
	 */
	private int checkSAK11204ForUpgrade()
	{
			String test = "select count(*) from CALENDAR_EVENT where (RANGE_START is null) or (RANGE_END is null)";
			Connection connection = null;
			Statement s = null;
			ResultSet rs = null;

			try
			{
				connection = sqlService.borrowConnection();
				s = connection.createStatement();
				rs = s.executeQuery(test);
				if (rs.next())
				{
					long ntodo = rs.getLong(1);
					if ( ntodo == 0 ) {
						log.debug("SAK-11204: Database has been migrated");
						return OK;
					} else {
						log.info("SAK-11204: Migration check, there are null range fields");
						return MIGRATE;
					}
				} else {
					log.warn("SAK-11204: Could not count null range fields, assuming migrate");
					return MIGRATE;
				}
			}
			catch (SQLException ex)
			{
				log.info("SAK-11204: Migration check, CALENDAR_EVENT schema not uptodate, test query said: {}",
						ex.getMessage());
				return UPGRADE_SCHEMA;
			}
			finally
			{
				try
				{
					rs.close();
				}
				catch (Exception ex)
				{
				}
				try
				{
					s.close();
				}
				catch (Exception ex)
				{
				}
				try
				{
					if (connection != null)
					{
						sqlService.returnConnection(connection);
					}
				}
				catch (Exception ex)
				{
				}
			}
	}

}
