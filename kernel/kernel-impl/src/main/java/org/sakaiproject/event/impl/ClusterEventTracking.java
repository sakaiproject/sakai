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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SimpleEvent;
import org.sakaiproject.memory.api.Cache;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * ClusterEventTracking is the implementation for the EventTracking service for use in a clustered multi-app server configuration.<br />
 * Events are backed in the cluster database, and this database is polled to read and process local events posted by the other cluster members.
 */
@Slf4j
public class ClusterEventTracking extends BaseEventTrackingService implements Runnable, SmartInitializingSingleton {

	private static final long WARNING_SAFE_EVENTS_TABLE_SIZE = 18000000L; // see SAK-3793
	private static final long MAX_SAFE_EVENTS_TABLE_SIZE = 20000000L; // see SAK-3793

	private String serverInstance;
	private String serverId;

    private AtomicLong lastEventSeq = new AtomicLong(0); // Last event code read from the db

	private Collection<Event> eventQueue = null; // Queue of events to write if we are batching
	private boolean checkDb = true; // Unless false, check the db for events from the other cluster servers
	private boolean batchWrite = true; // If true, batch events for bulk write
	private boolean autoDdl = false; // to run the ddl on init or not

	private int period = 5; // How long to wait in seconds between checks for new events from the db
	private ClusterEventTrackingServiceSql clusterEventTrackingServiceSql;

	private Cache<String, SimpleEvent> eventCache; // The events cache (ONLY used if enabled) - KNL-1184
	private Cache<String, Long> eventLastCache; // The events cache (ONLY used if enabled) - KNL-1184
	private boolean cachingEnabled; // is caching enabled? - KNL-1184

	@Setter private Map<String, ClusterEventTrackingServiceSql> databaseBeans;

	public void init() {
		serverInstance = serverConfigurationService.getServerIdInstance();
		serverId = serverConfigurationService.getServerId();

		setClusterEventTrackingServiceSql(sqlService.getVendor());

		try {
			// if we are auto-creating our schema, check and create
			if (autoDdl) sqlService.ddl(this.getClass().getClassLoader(), "sakai_event");
			if (batchWrite) eventQueue = new Vector<>();

			// start the event checking
			if (checkDb) {
				initLastEvent();
			}

			boolean eventsSizeCheck = serverConfigurationService.getBoolean("events.size.check", true);
			if (eventsSizeCheck) {
                // do the check for event oversizing and output log warning if needed - SAK-3793
    			long totalEventsCount = getEventsCount();
                if (totalEventsCount > WARNING_SAFE_EVENTS_TABLE_SIZE && totalEventsCount <= MAX_SAFE_EVENTS_TABLE_SIZE) {
					log.info(""" 
							The SAKAI_EVENT table size ({}) is approaching the point at which performance will
							begin to degrade ({}), we recommend you archive older events over to another table,
							remove older rows, or truncate this table before it reaches a size of {}
							""", totalEventsCount, MAX_SAFE_EVENTS_TABLE_SIZE, MAX_SAFE_EVENTS_TABLE_SIZE);
				} else if (totalEventsCount > MAX_SAFE_EVENTS_TABLE_SIZE) {
					log.warn("""
							The SAKAI_EVENT table size ({}) has passed the point at which performance will begin
							to degrade ({}), we recommend you archive older events over to another table, remove
							older rows, or truncate this table to ensure that performance is not affected negatively
							""", totalEventsCount, MAX_SAFE_EVENTS_TABLE_SIZE);
				}
			}

			log.info("period: {}, batch: {}, checkDb: {}", period, batchWrite, checkDb);

            log.info("Server Start: serverId={}, serverInstance={}, serverIdInstance={}, version={}/{}",
					serverConfigurationService.getServerId(),
					serverConfigurationService.getServerInstance(),
					serverConfigurationService.getServerIdInstance(),
					serverConfigurationService.getString("version.sakai", "unknown"),
					serverConfigurationService.getString("version.service", "unknown"));

            // initialize the caching server, if enabled
            initCacheServer();
		} catch (Exception e) {
			log.warn("Initialization failure", e);
		}
	}

	@Override
	public void afterSingletonsInstantiated() {
		// schedule a task for every pollDelaySeconds
		schedulingService.scheduleWithFixedDelay(
				this,
				60, // minimally wait 60 seconds for sakai to start
				period, // run every
				TimeUnit.SECONDS
		);
	}

	/**
     * @return the current total number of events in the events table (data storage)
     */
    protected long getEventsCount() {
        final long[] count = {0}; // array used to hold the count value, lambda capture
        final String eventCountStmt = clusterEventTrackingServiceSql.getEventsCountSql();
        try {
            sqlService.dbRead(eventCountStmt, null, result -> {
                try {
                    count[0] = result.getLong(1);
                } catch (SQLException ignore) {
                    log.info("Could not get count of events table using SQL ({})", eventCountStmt);
                }
                return count[0];
            });
        } catch (Exception e) {
            log.warn("Could not get count of events", e);
        }
        return count[0];
    }

	/**
	 * Cause this new event to get to wherever it has to go for persistence, etc.
	 * 
	 * @param event
	 *        The new event to post.
	 */
	protected void postEvent(Event event) {
		// mark the event time
		((BaseEvent) event).time = new Date();

		// notify locally generated events immediately -
		// they will not be processed again when read back from the database
		try {
			notifyObservers(event, true);
		} catch (Exception e) {
			log.warn("postEvent, notifyObservers(), event: {}", event, e);
		}

		if (!event.isTransient()) {
			// batch the event if we are batching
            if (batchWrite) eventQueue.add(event);
            // if not batching, write out the individual event
            else writeEvent(event);
		}
		log.debug("{}", event);
	}

	/**
	 * Write a single event to the db
	 *
	 * @param event The event to write.
	 */
	protected void writeEvent(Event event) {
		// get the SQL statement
		String statement = insertStatement();

		// collect the fields
		Object[] fields = new Object[6];
		bindValues(event, fields);

        // process the insert
        if (cachingEnabled) {
            // if caching is enabled, get the last inserted id
            Long eventId = sqlService.dbInsert(null, statement, fields, "EVENT_ID");
            if (eventId != null) {
                // write event to cache
                writeEventToCluster(event, eventId);
            }
        } else {
            boolean ok = sqlService.dbWrite(null, statement, fields);
            if (!ok) {
                log.warn("dbWrite failed: session: {} event: {}", fields[3], event);
            }
        }
    }

	/**
	 * Write a batch of events to the db
	 * 
	 * @param events
	 *        The collection of events to write.
	 */
	protected void writeBatchEvents(Collection<Event> events) {
		// any events to process
		if (events == null || events.isEmpty()) { return; }
		log.debug("writing {} batched events", events.size());

		// get a connection
		Connection conn = null;
		boolean wasCommit = true;
		try
		{
			conn = sqlService.borrowConnection();

			// common preparation for each insert
			String statement = insertStatement();

			// set up a batch of events if not using a cluster
			List<Object[]> eventList = new ArrayList<>();

			// write all events
			for (Event event : events) {
				Object[] fields = new Object[6];
				bindValues(event, fields);
				eventList.add(fields);

				// For clustered setups with caching enabled, use legacy, individual inserts
				// TODO: it might be possible to write the entire batch to database and still get return values. But this will need testing on MySQL and Oracle.
				if (cachingEnabled) {
					Long eventId = sqlService.dbInsert(conn, statement, fields, "EVENT_ID");
                    // write event to cache
                    if (eventId != null) writeEventToCluster(event, eventId);
				}
			}

			// Write all of these events in a batch if not using clustering
			if (!cachingEnabled) {
				if (!sqlService.dbWriteBatch(conn, statement, eventList)) {
					log.warn("dbWriteBatch failed: event count: {}", eventList.size());
				}
			}

			// commit
			if (!conn.isClosed()) {
			    conn.commit();
			}
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (Exception ee) {
					log.warn("while rolling back: {}", ee.getMessage(), ee);
				}
			}
			log.warn("{}", e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					if (!conn.isClosed() && conn.getAutoCommit() != wasCommit) {
						conn.setAutoCommit(wasCommit);
					}
				} catch (Exception e) {
					log.warn("while setting auto commit: {}", e.getMessage(), e);
				}
				sqlService.returnConnection(conn);
			}
		}
	}

	/**
	 * Form the proper event insert a statement for the database technology.
	 * 
	 * @return The SQL insert statement for writing an event.
	 */
	protected String insertStatement() {
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
	protected void bindValues(Event event, Object[] fields) {
		// session or user?
		String reportId;
		if (event.getSessionId() != null) {
			reportId = event.getSessionId();
		} else {
			// form an id based on the cluster server's id and the event user id
			reportId = "~" + serverConfigurationService.getServerId() + "~" + event.getUserId();
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

	public void run() {
		try {
			Thread.currentThread().setName(this.getClass().getName());

			// wait for sakai's ComponentManager to finish starting before processing events
			ComponentManager.waitTillConfigured();

			// write any events we have
			if (batchWrite) {
				Collection<Event> batchEvents = new ArrayList<>(eventQueue);
				eventQueue.clear();
				writeBatchEvents(batchEvents);
			}

			log.debug("checking for events > {}", lastEventSeq.get());
			// check the db for new events
			// We do a left join which gets us records from non-sessions also (SESSION_SERVER may be null when non-session events are returned)
			String statement = clusterEventTrackingServiceSql.getEventSql();

			// send in the last seq number parameter
			Object[] fields = new Object[1];
			fields[0] = lastEventSeq.get();

			List<Event> events = new ArrayList<>();
			if (cachingEnabled) { // KNL-1184
				// set to the last event id processed + 1 since we've already processed the last event id
				long beginEventId = lastEventSeq.get() + 1;
				// set m_lastEventSeq to the latest key value in the event cache
				initLastEventIdInEventCache();
				// only process events if there are new ones
				long endEventId = lastEventSeq.get();
				if (endEventId >= beginEventId) {
					for (long i = beginEventId; i <= endEventId; i++) {
						SimpleEvent event = eventCache.get( String.valueOf(i) );
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

							// add an event to the list only if it is not a local server event
							if (!skipIt) {
								events.add(event);
							}
						}
					}
				}
			} else {
				events = sqlService.dbRead(statement, fields,result -> {
                    try {
                        long id = result.getLong(1);
                        Date date = new Date(result.getTimestamp(2).getTime());
                        String function = result.getString(3);
                        String ref = result.getString(4);
                        String session = result.getString(5);
                        String code = result.getString(6);
                        String context = result.getString(7);
                        String eventSessionServerId = result.getString(8); // maybe null

                        lastEventSeq.updateAndGet(current -> Math.max(current, id));

                        boolean nonSessionEvent = (eventSessionServerId == null || session.startsWith("~"));
                        String userId = null;
                        boolean skipIt = false;

                        if (nonSessionEvent) {
                            String[] parts = StringUtils.split(session, "~");
                            if (parts.length > 1) userId = parts[1];

                            // we skip this event if it came from our server
                            if (parts.length > 0) skipIt = serverId.equals(parts[0]);
                        } else {
							skipIt = serverInstance.equals(eventSessionServerId);
						}

                        if (skipIt) return null;

                        // Note: events from outside the server don't need notification info, since notification is processed only on internal
                        // events -ggolden
                        BaseEvent event = new BaseEvent(id, function, ref, context, "m".equals(code), NotificationService.NOTI_NONE, date);

                        if (nonSessionEvent) event.setUserId(userId);
                        else event.setSessionId(session);

                        return event;
                    } catch (Exception e) {
                        log.warn("Reading an event from database: {}", e.toString());
						return null;
                    }
                });
			}
			// for each new event found, notify observers
			for (Event event : events) {
				notifyObservers(event, false);
			}
		} catch (Throwable t) {
			log.error("Reading events from database", t);
		}
	}

	/**
	 * Check the db for the largest event seq number and set this as the one after which we will next get the event.
	 */
	protected void initLastEvent() {
		String statement = clusterEventTrackingServiceSql.getMaxEventIdSql();

		sqlService.dbRead(statement, null, result -> {
			try {
				// read the one-long value into our last event seq number
				lastEventSeq.set(result.getLong(1));
			} catch (SQLException se) {
				log.warn("Reading last event id from database: {}", se.toString());
			}
			return null;
		});

		log.debug("Starting (after) Event #: {}", lastEventSeq.get());
	}

	/**
	 * KNL-1184
	 * Initializes the events cache, if enabled
	 */
	private void initCacheServer() {
		// remove down to and including this line
		cachingEnabled = serverConfigurationService.getBoolean("memory.cluster.enabled", false);
		if (cachingEnabled) {
			boolean eventsCacheUsed = false;
			boolean eventLastCacheUsed = false;
			String[] caches = serverConfigurationService.getStrings("memory.cluster.names");
			if(ArrayUtils.isNotEmpty(caches)) {
				for(String cacheName : caches) {
					if("org.sakaiproject.event.impl.ClusterEventTracking.eventsCache".equals(cacheName)) {
						eventCache = memoryService.getCache("org.sakaiproject.event.impl.ClusterEventTracking.eventsCache");
						eventsCacheUsed = true;
					} else if("org.sakaiproject.event.impl.ClusterEventTracking.eventLastCache".equals(cacheName)) {
						// This cache only needs to hold a single value, the last updated event id
						eventLastCache = memoryService.getCache("org.sakaiproject.event.impl.ClusterEventTracking.eventLastCache");
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
                Long last = eventLastCache.get("lastEventId");
                if (last != null) lastEventSeq.set(last);
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
                SimpleEvent simpleEvent = new SimpleEvent(baseEvent, serverConfigurationService.getServerIdInstance());
                // add item to the cache store
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

	public void setCheckDb(String value) {
		checkDb = Boolean.parseBoolean(value);
	}

	public void setBatchWrite(String value) {
		batchWrite = Boolean.parseBoolean(value);
	}

	public void setAutoDdl(String value) {
		autoDdl = Boolean.parseBoolean(value);
	}

	public void setPeriod(String time) {
		period = Integer.parseInt(time);
	}

	public void setClusterEventTrackingServiceSql(String vendor) {
		this.clusterEventTrackingServiceSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

}
