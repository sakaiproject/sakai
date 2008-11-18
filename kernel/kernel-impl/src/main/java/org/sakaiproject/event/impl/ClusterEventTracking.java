/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * ClusterEventTracking is the implmentation for the EventTracking service for use in a clustered multi-app server configuration.<br />
 * Events are backed in the cluster database, and this database is polled to read and process locally events posted by the other cluster members.
 * </p>
 */
public abstract class ClusterEventTracking extends BaseEventTrackingService implements Runnable
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ClusterEventTracking.class);

	/** String used to identify this service in the logs */
	protected static String m_logId = "EventTracking: ";

	/** The db event checker thread. */
	protected Thread m_thread = null;

	/** The thread quit flag. */
	protected boolean m_threadStop = false;

	/** Last event code read from the db */
	protected long m_lastEventSeq = 0;

	/** Queue of events to write if we are batching. */
	protected Collection m_eventQueue = null;

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract SqlService sqlService();

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/

	/** Unless false, check the db for events from the other cluster servers. */
	protected boolean m_checkDb = true;

	/**
	 * Configuration: set the check-db.
	 * 
	 * @param value
	 *        The check-db value.
	 */
	public void setCheckDb(String value)
	{
		try
		{
			m_checkDb = Boolean.valueOf(value).booleanValue();
		}
		catch (Exception any)
		{
		}
	}

	/** If true, batch events for bulk write. */
	protected boolean m_batchWrite = true;

	/**
	 * Configuration: set the batch writing flag.
	 * 
	 * @param value
	 *        The batch writing value.
	 */
	public void setBatchWrite(String value)
	{
		try
		{
			m_batchWrite = Boolean.valueOf(value).booleanValue();
		}
		catch (Exception any)
		{
		}
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
		m_autoDdl = Boolean.valueOf(value).booleanValue();
	}

	/** How long to wait between checks for new events from the db. */
	protected long m_period = 1000L * 5L;

	/**
	 * Set the # seconds to wait between db checks for new events.
	 * 
	 * @param time
	 *        The # seconds to wait between db checks for new events.
	 */
	public void setPeriod(String time)
	{
		m_period = Integer.parseInt(time) * 1000L;
	}

	/** contains a map of the database dependent handler. */
	protected Map<String, ClusterEventTrackingServiceSql> databaseBeans;

	/** contains database dependent code. */
	protected ClusterEventTrackingServiceSql clusterEventTrackingServiceSql;

	public void setDatabaseBeans(Map databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	public ClusterEventTrackingServiceSql getClusterEventTrackingServiceSql()
	{
		return clusterEventTrackingServiceSql;
	}

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setClusterEventTrackingServiceSql(String vendor)
	{
		this.clusterEventTrackingServiceSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

	/*************************************************************************************************************************************************
	 * Init and Destroy
	 ************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		setClusterEventTrackingServiceSql(sqlService().getVendor());
		try
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_event");
			}

			super.init();

			if (m_batchWrite)
			{
				m_eventQueue = new Vector();
			}

			// startup the event checking
			if (m_checkDb)
			{
				start();
			}

			M_log.info(this + ".init() - period: " + m_period / 1000 + " batch: " + m_batchWrite + " checkDb: " + m_checkDb);

			this.post(this.newEvent("server.start", serverConfigurationService().getString("version.sakai", "unknown") + "/" + serverConfigurationService().getString("version.service", "unknown"), false));

		}
		catch (Throwable t)
		{
			M_log.warn(this + ".init(): ", t);
		}
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		// stop our thread
		stop();

		super.destroy();
	}

	/*************************************************************************************************************************************************
	 * Event post / flow
	 ************************************************************************************************************************************************/

	/**
	 * Cause this new event to get to wherever it has to go for persistence, etc.
	 * 
	 * @param event
	 *        The new event to post.
	 */
	protected void postEvent(Event event)
	{
		// mark the event time
		((BaseEvent) event).m_time = timeService().newTime();

		// notify locally generated events immediately -
		// they will not be process again when read back from the database
		try
		{
			notifyObservers(event, true);
		}
		catch (Throwable t)
		{
			M_log.warn("postEvent, notifyObservers(), event: " + event.toString(), t);
		}

		// batch the event if we are batching
		if (m_batchWrite)
		{
			synchronized (m_eventQueue)
			{
				m_eventQueue.add(event);
			}
		}

		// if not batching, write out the individual event
		else
		{
			writeEvent(event, null);
		}

		if (M_log.isDebugEnabled()) M_log.debug(m_logId + event);
	}

	/**
	 * Write a single event to the db
	 * 
	 * @param event
	 *        The event to write.
	 */
	protected void writeEvent(Event event, Connection conn)
	{
		// get the SQL statement
		String statement = insertStatement();

		// collect the fields
		Object fields[] = new Object[6];
		bindValues(event, fields);

		// process the insert
		boolean ok = sqlService().dbWrite(conn, statement, fields);
		if (!ok)
		{
			M_log.warn(this + ".writeEvent(): dbWrite failed: session: " + fields[3] + " event: " + event.toString());
		}
	}

	/**
	 * Write a batch of events to the db
	 * 
	 * @param events
	 *        The collection of event to write.
	 */
	protected void writeBatchEvents(Collection events)
	{
		// get a connection
		Connection conn = null;
		boolean wasCommit = true;
		try
		{
			conn = sqlService().borrowConnection();
			wasCommit = conn.getAutoCommit();
			if (wasCommit)
			{
				conn.setAutoCommit(false);
			}

			// Note: investigate batch writing via the jdbc driver: make sure we can still use prepared statements (check out host arrays, too)
			// -ggolden

			// common preparation for each insert
			String statement = insertStatement();
			Object fields[] = new Object[6];

			// write all events
			for (Iterator i = events.iterator(); i.hasNext();)
			{
				Event event = (Event) i.next();
				bindValues(event, fields);

				// process the insert
				boolean ok = sqlService().dbWrite(conn, statement, fields);
				if (!ok)
				{
					M_log.warn(this + ".writeBatchEvents(): dbWrite failed: session: " + fields[3] + " event: " + event.toString());
				}
			}

			// commit
			conn.commit();
		}
		catch (Throwable e)
		{
			if (conn != null)
			{
				try
				{
					conn.rollback();
				}
				catch (Exception ee)
				{
					M_log.warn(this + ".writeBatchEvents, while rolling back: " + ee);
				}
			}
			M_log.warn(this + ".writeBatchEvents: " + e);
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					if (conn.getAutoCommit() != wasCommit)
					{
						conn.setAutoCommit(wasCommit);
					}
				}
				catch (Exception e)
				{
					M_log.warn(this + ".writeBatchEvents, while setting auto commit: " + e);
				}
				sqlService().returnConnection(conn);
			}
		}
	}

	/**
	 * Form the proper event insert statement for the database technology.
	 * 
	 * @return The SQL insert statement for writing an event.
	 */
	protected String insertStatement()
	{
		return clusterEventTrackingServiceSql.getInsertEventSql();
	}

	/**
	 * Bind the event values into an array of fields for inserting.
	 * 
	 * @param event
	 *        The event to write.
	 * @param fields
	 *        The object[] to hold bind variables.
	 */
	protected void bindValues(Event event, Object[] fields)
	{
		// session or user?
		String reportId = null;
		if (event.getSessionId() != null)
		{
			reportId = event.getSessionId();
		}
		else
		{
			// form an id based on the cluster server's id and the event user id
			reportId = "~" + serverConfigurationService().getServerId() + "~" + event.getUserId();
		}

		fields[0] = ((BaseEvent) event).m_time;
		fields[1] = event.getEvent();
		fields[2] = event.getResource() != null && event.getResource().length() > 255 ? 
				event.getResource().substring(0, 255) : event.getResource();
		fields[3] = reportId;
		fields[4] = (event.getModify() ? "m" : "a");
		fields[5] = event.getContext() != null && event.getContext().length() > 255 ? 
				event.getContext().substring(0, 255) : event.getContext();
	}

	/*************************************************************************************************************************************************
	 * Runnable
	 ************************************************************************************************************************************************/

	/**
	 * Start the clean and report thread.
	 */
	protected void start()
	{
		m_threadStop = false;

		m_thread = new Thread(this, getClass().getName());
		m_thread.setDaemon(true);
		m_thread.start();
	}

	/**
	 * Stop the clean and report thread.
	 */
	protected void stop()
	{
		if (m_thread == null) return;

		// signal the thread to stop
		m_threadStop = true;

		// wake up the thread
		m_thread.interrupt();

		m_thread = null;
	}

	/**
	 * Run the event checking thread.
	 */
	public void run()
	{
		// since we might be running while the component manager is still being created and populated, such as at server startup, wait here for a
		// complete component manager
		ComponentManager.waitTillConfigured();

		// find the latest event in the db
		initLastEvent();

		// loop till told to stop
		while ((!m_threadStop) && (!Thread.currentThread().isInterrupted()))
		{
			final String serverInstance = serverConfigurationService().getServerIdInstance();
			final String serverId = serverConfigurationService().getServerId();

			try
			{
				// write any batched events
				Collection myEvents = new Vector();
				if (m_batchWrite)
				{
					synchronized (m_eventQueue)
					{
						if (m_eventQueue.size() > 0)
						{
							myEvents.addAll(m_eventQueue);
							m_eventQueue.clear();
						}
					}

					if (myEvents.size() > 0)
					{
						if (M_log.isDebugEnabled()) M_log.debug("writing " + myEvents.size() + " batched events");
						writeBatchEvents(myEvents);
					}
				}

				if (M_log.isDebugEnabled()) M_log.debug("checking for events > " + m_lastEventSeq);
				// check the db for new events
				// Note: the events may not all have sessions, so to get them we need an outer join.
				// TODO: switch to a "view" read once that's established, for now, a join -ggolden
				String statement = clusterEventTrackingServiceSql.getEventSql();

				// we might want a left join, which would get us records from non-sessions, which the above mysql code does NOT give -ggolden
				// select e.EVENT_ID,e.EVENT_DATE,e.EVENT,e.REF,e.SESSION_ID,e.EVENT_CODE,s.SESSION_SERVER
				// from SAKAI_EVENT e
				// left join SAKAI_SESSION s on (e.SESSION_ID = s.SESSION_ID)
				// where EVENT_ID > 0

				// send in the last seq number parameter
				Object[] fields = new Object[1];
				fields[0] = Long.valueOf(m_lastEventSeq);

				List events = sqlService().dbRead(statement, fields, new SqlReader()
				{
					public Object readSqlResultRecord(ResultSet result)
					{
						try
						{
							// read the Event
							long id = result.getLong(1);
							Time date = timeService().newTime(result.getTimestamp(2, sqlService().getCal()).getTime());
							String function = result.getString(3);
							String ref = result.getString(4);
							String session = result.getString(5);
							String code = result.getString(6);
							String context = result.getString(7);
							String eventSessionServerId = result.getString(8);

							// for each one (really, for the last one), update the last event seen seq number
							if (id > m_lastEventSeq)
							{
								m_lastEventSeq = id;
							}

							boolean nonSessionEvent = session.startsWith("~");
							String userId = null;
							boolean skipIt = false;

							if (nonSessionEvent)
							{
								String[] parts = StringUtil.split(session, "~");
								userId = parts[2];

								// we skip this event if it came from our server
								skipIt = serverId.equals(parts[1]);
							}

							// for session events, if the event is from this server instance,
							// we have already processed it and can skip it here.
							else
							{
								skipIt = serverInstance.equals(eventSessionServerId);
							}

							if (skipIt)
							{
								return null;
							}

							// Note: events from outside the server don't need notification info, since notification is processed only on internal
							// events -ggolden
							BaseEvent event = new BaseEvent(id, function, ref, context, "m".equals(code), NotificationService.NOTI_NONE);
							if (nonSessionEvent)
							{
								event.setUserId(userId);
							}
							else
							{
								event.setSessionId(session);
							}

							return event;
						}
						catch (SQLException ignore)
						{
							return null;
						}
					}
				});

				// for each new event found, notify observers
				for (int i = 0; i < events.size(); i++)
				{
					Event event = (Event) events.get(i);
					notifyObservers(event, false);
				}
			}
			catch (Throwable e)
			{
				M_log.warn("run: will continue: ", e);
			}

			// take a small nap
			try
			{
				Thread.sleep(m_period);
			}
			catch (Exception ignore)
			{
			}
		}
	}

	/**
	 * Check the db for the largest event seq number, and set this as the one after which we will next get event.
	 */
	protected void initLastEvent()
	{
		String statement = clusterEventTrackingServiceSql.getMaxEventIdSql();

		sqlService().dbRead(statement, null, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					// read the one long value into our last event seq number
					m_lastEventSeq = result.getLong(1);
				}
				catch (SQLException ignore)
				{
				}
				return null;
			}
		});

		if (M_log.isDebugEnabled()) M_log.debug(this + " Starting (after) Event #: " + m_lastEventSeq);
	}
}
