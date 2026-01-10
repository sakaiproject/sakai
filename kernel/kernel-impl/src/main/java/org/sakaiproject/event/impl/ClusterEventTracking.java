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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SimpleEvent;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.scheduling.api.SchedulingService;

/**
 * <p>
 * ClusterEventTracking is the implmentation for the EventTracking service for use in a clustered multi-app server configuration.<br />
 * Events are backed in the cluster database, and this database is polled to read and process locally events posted by the other cluster members.
 * </p>
 */
@Slf4j
public abstract class ClusterEventTracking extends BaseEventTrackingService implements Runnable
{

	/** String used to identify this service in the logs */
	protected static final String m_logId = "EventTracking: ";
	// see http://jira.sakaiproject.org/browse/SAK-3793 for more info about these numbers
	private static final long WARNING_SAFE_EVENTS_TABLE_SIZE = 18000000l;
	private static final long MAX_SAFE_EVENTS_TABLE_SIZE = 20000000l;
	/** The db event checker thread. */
	protected Thread m_thread = null;

	/** The thread quit flag. */
	protected boolean m_threadStop = false;

	/** Last event code read from the db */
	protected long m_lastEventSeq = 0;

	protected long m_totalEventsCount = 0;

	/** Queue of events to write if we are batching. */
	protected Collection<Event> m_eventQueue = null;
	/** Unless false, check the db for events from the other cluster servers. */
	protected boolean m_checkDb = true;
	/** If true, batch events for bulk write. */
	protected boolean m_batchWrite = true;
	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	private String serverInstance;
	private String serverId;

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/
	/** How long to wait in seconds between checks for new events from the db. */
	protected int m_period = 5;
	/** contains a map of the database dependent handler. */
	protected Map<String, ClusterEventTrackingServiceSql> databaseBeans;
	/** contains database dependent code. */
	protected ClusterEventTrackingServiceSql clusterEventTrackingServiceSql;

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/
	/** The events caches (ONLY used if enabled) - KNL-1184 */
	private Cache eventCache;
	private Cache eventLastCache;
	/** is caching enabled? - KNL-1184 */
	private boolean cachingEnabled;

	protected abstract SqlService sqlService();

	protected abstract SchedulingService schedulingService();

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract MemoryService memoryService();

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

	/**
	 * Set the # seconds to wait between db checks for new events.
	 *
	 * @param time
	 *        The # seconds to wait between db checks for new events.
	 */
	public void setPeriod(String time)
	{
		m_period = Integer.parseInt(time);
	}

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
	 * Init
	 ************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		serverInstance = serverConfigurationService().getServerIdInstance();
		serverId = serverConfigurationService().getServerId();

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
				m_eventQueue = new Vector<Event>();
			}

			// startup the event checking
			if (m_checkDb)
			{
				initLastEvent();

				// schedule task for every pollDelaySeconds
				schedulingService().scheduleWithFixedDelay(
						this,
						60, // minimally wait 60 seconds for sakai to start
						m_period, // run every
						TimeUnit.SECONDS
				);
			}

			boolean eventsSizeCheck = serverConfigurationService().getBoolean("events.size.check", true);
			if (eventsSizeCheck) {
                // do the check for event oversizing and output log warning if needed - SAK-3793
    			long totalEventsCount = getEventsCount();
                if (totalEventsCount > WARNING_SAFE_EVENTS_TABLE_SIZE) {
                    log.info("The SAKAI_EVENT table size ({}) is approaching the point at which performance will" +
							" begin to degrade ({}), we recommend you archive older events over to another table," +
							" remove older rows, or truncate this table before it reaches a size of {}",
							totalEventsCount, MAX_SAFE_EVENTS_TABLE_SIZE, MAX_SAFE_EVENTS_TABLE_SIZE);
                } else if (totalEventsCount > MAX_SAFE_EVENTS_TABLE_SIZE) {
                    log.warn("The SAKAI_EVENT table size ({}) has passed the point at which performance will begin" +
							" to degrade ({}), we recommend you archive older events over to another table, remove" +
							" older rows, or truncate this table to ensure that performance is not affected negatively",
							totalEventsCount, MAX_SAFE_EVENTS_TABLE_SIZE);
    			}
			}

			log.info("period: {}, batch: {}, checkDb: {}", m_period, m_batchWrite, m_checkDb);

            String sakaiVersion = serverConfigurationService().getString("version.sakai", "unknown") + "/" + serverConfigurationService().getString("version.service", "unknown");
            log.info("Server Start: serverId={}, serverInstance={}, serverIdInstance={}, version={}",
					serverConfigurationService().getServerId(),
					serverConfigurationService().getServerInstance(),
					serverConfigurationService().getServerIdInstance(),
					sakaiVersion);

            // initialize the caching server, if enabled
            initCacheServer();
		}
		catch (Exception e)
		{
			log.warn(e.getMessage(), e);
		}
	}

    /**
     * @return the current total number of events in the events table (data storage)
     */
    protected long getEventsCount() {
        /*
         * NOTE: this is a weird way to get the value out but it matches the existing code
         * Added for SAK-3793
         */
        m_totalEventsCount = 0;
        final String eventCountStmt = clusterEventTrackingServiceSql.getEventsCountSql();
        try {
            sqlService().dbRead(eventCountStmt, null, new SqlReader() {
                public Object readSqlResultRecord(ResultSet result) {
                    try {
                        m_totalEventsCount = result.getLong(1);
                    } catch (SQLException ignore) {
                        log.info("Could not get count of events table using SQL ({})", eventCountStmt);
                    }
                    return Long.valueOf(m_totalEventsCount);
                }
            });
        } catch (Exception e) {
            log.warn("Could not get count of events: " + e);
        }
        return m_totalEventsCount;
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
		((BaseEvent) event).time = new Date();

		// notify locally generated events immediately -
		// they will not be process again when read back from the database
		try
		{
			notifyObservers(event, true);
		}
		catch (Exception t)
		{
			log.warn("postEvent, notifyObservers(), event: {}", event.toString(), t);
		}

		if (!event.isTransient()) {
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
		}

		log.debug("{}{}", m_logId, event);
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
        if (cachingEnabled) {
            // if caching is enabled, get the last inserted id
            Long eventId = sqlService().dbInsert(conn, statement, fields, "EVENT_ID");
            if (eventId != null) {
                // write event to cache
                writeEventToCluster(event, eventId);
            }
        } else {
            boolean ok = sqlService().dbWrite(conn, statement, fields);
            if (!ok) {
                log.warn("dbWrite failed: session: {} event: {}", fields[3], event.toString());
            }
        }
    }

	/**
	 * Write a batch of events to the db
	 * 
	 * @param events
	 *        The collection of event to write.
	 */
	protected void writeBatchEvents(Collection<Event> events)
	{
		// any events to process
		if (events == null || events.isEmpty()) { return; }
		log.debug("writing {} batched events", events.size());

		// get a connection
		Connection conn = null;
		boolean wasCommit = true;
		try
		{
			conn = sqlService().borrowConnection();

			// common preparation for each insert
			String statement = insertStatement();

			// Setup a batch of events if not using a cluster
			List<Object[]> eventList = new ArrayList<>();

			// write all events
			for (Event event : events)
			{
				Object fields[] = new Object[6];
				bindValues(event, fields);
				eventList.add(fields);

				// For clustered setups with caching enabled, use legacy, individual inserts
				// TODO: it might be possible to write the entire batch to database and still get return values. But this will need testing on MySQL and Oracle.
				if (cachingEnabled) {
					Long eventId = sqlService().dbInsert(conn, statement, fields, "EVENT_ID");
					if (eventId != null) {
						// write event to cache
						writeEventToCluster(event, eventId);
					}
				}
			}

			// Write all of these events in a batch if not using clustering
			if (!cachingEnabled) {
				boolean ok = sqlService().dbWriteBatch(conn, statement, eventList);
				if (!ok) {
					log.warn("dbWriteBatch failed: event count: {}", eventList.size());
				}
			}

			// commit
			if (!conn.isClosed()) {
			    conn.commit();
			}
		}
		catch (Exception e)
		{
			if (conn != null)
			{
				try
				{
					conn.rollback();
				}
				catch (Exception ee)
				{
					log.warn("while rolling back: {}", ee.getMessage(), ee);
				}
			}
			log.warn("{}", e.getMessage(), e);
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					if (!conn.isClosed() && conn.getAutoCommit() != wasCommit)
					{
						conn.setAutoCommit(wasCommit);
					}
				}
				catch (Exception e)
				{
					log.warn("while setting auto commit: {}", e.getMessage(), e);
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

		fields[0] = ((BaseEvent) event).time;
		fields[1] = event.getEvent() != null && event.getEvent().length() > 32 ?
				event.getEvent().substring(0, 32) : event.getEvent();
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
	 * Run the event checking thread.
	 */
	public void run()
	{
		try
		{
			Thread.currentThread().setName(this.getClass().getName());

			// wait for sakai's ComponentManager to finish starting before processing events
			ComponentManager.waitTillConfigured();

			// write any events we have
			if (m_batchWrite)
			{
				Collection<Event> batchEvents;
				synchronized (m_eventQueue)
				{
					batchEvents = new ArrayList<>(m_eventQueue);
					m_eventQueue.clear();
				}
				writeBatchEvents(batchEvents);
			}

			log.debug("checking for events > {}", m_lastEventSeq);
			// check the db for new events
			// We do a left join which gets us records from non-sessions also (SESSION_SERVER may be null when non-session events are returned)
			String statement = clusterEventTrackingServiceSql.getEventSql();

			// send in the last seq number parameter
			Object[] fields = new Object[1];
			fields[0] = Long.valueOf(m_lastEventSeq);

			List<Event> events = new ArrayList<>();
			if (cachingEnabled) { // KNL-1184
				// set to last event id processed + 1 since we've already processed the last event id
				long beginEventId = m_lastEventSeq + 1;
				// set m_lastEventSeq to latest key value in event cache
				initLastEventIdInEventCache();
				// only process events if there are new ones
				if (m_lastEventSeq >= beginEventId) {
					for (long i = beginEventId; i <= m_lastEventSeq; i++) {
						SimpleEvent event = (SimpleEvent) eventCache.get( String.valueOf(i) );
						if (event != null) {
							boolean nonSessionEvent = (event.getServerId() == null || StringUtils.startsWith(event.getSessionId(), "~"));
							String userId = null;
							boolean skipIt = false;

							if (nonSessionEvent) {
								String[] parts = StringUtils.split(event.getSessionId(), "~");
								if (parts.length > 1) {
									userId = parts[1];
								}

								// we skip this event if it came from our server
								if (parts.length > 0) {
									skipIt = serverId.equals(parts[0]);
								}

								event.setUserId(userId);
							} else {
								skipIt = serverInstance.equals(event.getServerId());
								event.setSessionId(event.getSessionId());
							}

							// add event to list, only if it is not a local server event
							if (!skipIt) {
								events.add(event);
							}
						}
					}
				}
			} else {
				events = sqlService().dbRead(statement, fields, new SqlReader() {
					public Object readSqlResultRecord(ResultSet result) {
						try {
							Long id = result.getLong(1);
							Date date = new Date(result.getTimestamp(2).getTime());
							String function = result.getString(3);
							String ref = result.getString(4);
							String session = result.getString(5);
							String code = result.getString(6);
							String context = result.getString(7);
							String eventSessionServerId = result.getString(8); // may be null

							if (id > m_lastEventSeq) {
								m_lastEventSeq = id;
							}

							boolean nonSessionEvent = (eventSessionServerId == null || session.startsWith("~"));
							String userId = null;
							boolean skipIt = false;

							if (nonSessionEvent) {
								String[] parts = StringUtils.split(session, "~");
								if (parts.length > 1) {
									userId = parts[1];
								}

								// we skip this event if it came from our server
								if (parts.length > 0) {
									skipIt = serverId.equals(parts[0]);
								}
							} else {
								skipIt = serverInstance.equals(eventSessionServerId);
							}

							if (skipIt) {
								return null;
							}

							// Note: events from outside the server don't need notification info, since notification is processed only on internal
							// events -ggolden
							BaseEvent event = new BaseEvent(id, function, ref, context, "m".equals(code), NotificationService.NOTI_NONE, date);
							if (nonSessionEvent) {
								event.setUserId(userId);
							} else {
								event.setSessionId(session);
							}
							return event;
						} catch (Exception ignore) {
							return null;
						}
					}
				});
			}
			// for each new event found, notify observers
			for (Event event : events) {
				notifyObservers(event, false);
			}
		}
		catch (Throwable t)
		{
			log.error("{}error during execution {}", m_logId, t.getMessage(), t);
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

		log.debug("Starting (after) Event #: {}", m_lastEventSeq);
	}

	/**
	 * KNL-1184
	 * Initializes the events cache, if enabled
	 */
	private void initCacheServer() {
		// remove down to and including this line
		cachingEnabled = serverConfigurationService().getBoolean("memory.cluster.enabled", false);
		if (cachingEnabled) {
			boolean eventsCacheUsed = false;
			boolean eventLastCacheUsed = false;
			String[] caches = serverConfigurationService().getStrings("memory.cluster.names");
			if(ArrayUtils.isNotEmpty(caches)) {
				for(String cacheName : caches) {
					if("org.sakaiproject.event.impl.ClusterEventTracking.eventsCache".equals(cacheName)) {
						eventCache = memoryService().newCache("org.sakaiproject.event.impl.ClusterEventTracking.eventsCache");
						eventsCacheUsed = true;
					} else if("org.sakaiproject.event.impl.ClusterEventTracking.eventLastCache".equals(cacheName)) {
						/**
						 * This cache only needs to hold a single value, the last updated event id
						 */
						eventLastCache = memoryService().newCache("org.sakaiproject.event.impl.ClusterEventTracking.eventLastCache");
						eventLastCacheUsed = true;
					}
				}
				cachingEnabled = eventsCacheUsed && eventLastCacheUsed;
			}
		}
	}

    /**
     * Finds the last event ID inserted into the event cache
     * (tracked in another cache)
     */
    private void initLastEventIdInEventCache() {
        if (cachingEnabled) {
            if (eventLastCache != null) {
                Long last = (Long) eventLastCache.get("lastEventId");
                if (last != null) {
                    m_lastEventSeq = last;
                }
            }
        }
    }

    /**
     * Writes an event to cache, if enabled
     * 
     * @param event the event object
     * @param eventId the id of the event object
     */
    private void writeEventToCluster(Event event, Long eventId) {
        if (cachingEnabled) {
            if (eventCache != null) {
                // store event as an element
                BaseEvent baseEvent = ensureBaseEvent(event);
                SimpleEvent simpleEvent = new SimpleEvent((Event) baseEvent, serverConfigurationService().getServerIdInstance());
                // add item to cache store
                eventCache.put(String.valueOf(eventId), simpleEvent);
                // update the last event id each time
                eventLastCache.put("lastEventId", eventId);
            } else {
				log.debug("Cannot store event to cache, event store not initialized.");
            }
        } else {
			log.debug("Cluster caching not enabled.");
        }
    }

}
