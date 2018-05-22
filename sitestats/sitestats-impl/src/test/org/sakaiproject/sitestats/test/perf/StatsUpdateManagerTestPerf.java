/**
 * Copyright (c) 2006-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.perf;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.impl.CustomEventImpl;
import org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl;
import org.sakaiproject.sitestats.test.perf.mock.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <p>
 * This aims to test concurrent updates of the stats tables. In a production environment you may well have
 * multiple nodes updates the stats from their local events. When the database server is heavily loaded this can start
 * taking a reasonable amount of time and deadlocks could occur. This test aims to check that the code doesn't
 * deadlock.
 * </p><p>
 * Get the SQL to re-create the tables:
 * <pre>
 *     SHOW CREATE TABLE SAKAI_SESSION;
 *     SHOW CREATE TABLE SAKAI_EVENT;
 * </pre>
 * To get some test data export some example data from your production server with:
 * <pre>
 *     SELECT * INTO OUTFILE 'sessions-2014-01-21.sql' FROM SAKAI_SESSION WHERE date(SESSION_START) = '2014-01-21';
 *     SELECT * INTO OUTFILE 'events-2014-01-21.sql' FROM SAKAI_EVENTS WHERE date(EVENT_DATE) = '2014-01-21';
 * </pre>
 * and then this can be loaded into MySQL with something like:
 * <pre>
 *     LOAD DATA INFILE '/Users/buckett/dumps/events-2014-01-21.sql' INTO TABLE SAKAI_EVENT;
 *     LOAD DATA INFILE '/Users/buckett/dumps/sessions-2014-01-21.sql' INTO TABLE SAKAI_SESSION;
 * </pre>
 * and then create an index on the event date so that the select is reasonably fast and it doesn't have to do sorting:
 * <pre>
 *     CREATE INDEX SAKAI_EVENT_DATE_IDX ON SAKAI_EVENT(EVENT_DATE);
 * </pre>
 * </p>
 * <p>
 * Then load this data in a local database and then configure your database connection in
 * <code>hibernate.properties</code> and run this test. And comment out the @Sql annotation, this is there so that
 * the test can be run in normal performance tests so that we check it still works/compiles.
 * </p>
 */
@Sql("/update-manager-perf.sql")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations = { "/hibernate-beans.xml"})
@Slf4j
public class StatsUpdateManagerTestPerf {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private DataSource dataSource;
	
	private SiteService siteService;
	private EventRegistryService eventRegistryService;
	private StatsManager statsManager;
	private UsageSessionService usageSessionService;
	private EventTrackingService eventTrackingService;

	
	
	@Before
	public void setUp() throws SQLException {

		siteService = StubUtils.stubClass(MockSiteService.class);
		eventRegistryService = StubUtils.stubClass(MockEventRegistryService.class);
		statsManager = StubUtils.stubClass(MockStatsManager.class);
		usageSessionService = StubUtils.stubClass(MockUsageSessionService.class);
		eventTrackingService = StubUtils.stubClass(EventTrackingService.class);
		String isolation;
		switch (dataSource.getConnection().getTransactionIsolation()) {
			case Connection.TRANSACTION_NONE:
				isolation = "None";
				break;
			case Connection.TRANSACTION_READ_UNCOMMITTED:
				isolation = "Read uncomitted";
				break;
			case Connection.TRANSACTION_READ_COMMITTED:
				isolation = "Read committed";
				break;
			case Connection.TRANSACTION_REPEATABLE_READ:
				isolation = "Repeatable read";
				break;
			case Connection.TRANSACTION_SERIALIZABLE:
				isolation = "Serializable";
				break;
			default:
				isolation = "Unknown";
		}
		log.debug("Transation isolation is: "+ isolation);
		// As we can't haven mutlple @RunWith annotations.

		// We're not going to do live look ups
		//when(usageSessionService.getSession(anyString())).thenReturn(null);
	}

	@Test
	public void testLargePerformance() throws IdUnusedException, SQLException, InterruptedException {
		// Connect to DB. (props, JDBC driver)
		// Mock dependenant services.
		// Start multiple threads processing the events from DB based on the session worker.
		// want to tie each thread to an instance of StatsUpdateManager so we don't hit
		// locks in that code.
		// We watch to have a queue for the next piece of work, but we don't want the queue to 
		// be very big so we don't fill up heap and end up doing lots of GC and dying.
		// Is hibernate still doing caching?
		getEvents();
	}
	


	protected String extractServerId(String server) {
		int lastDash = server.lastIndexOf("-");
		if (lastDash > 0) {
			server = server.substring(0, lastDash);
		}
		return server;
	}
	
	protected String extractServerIdFromSession(String sessionId) {
		String serverId = null;
		if (sessionId.startsWith("~")) {
			int lastTilda = sessionId.lastIndexOf("~");
			if (lastTilda > 0 ) {
				serverId = sessionId.substring(1, lastTilda);
			}
		}
		return serverId;
	}
	

	public void getEvents() throws SQLException, InterruptedException {
		Connection connection = dataSource.getConnection();
		// Must sort by date so that time never goes backwards.
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT e.event_date date, e.event, e.ref, e.context, s.session_user user, e.session_id, s.session_server server "+
				"FROM SAKAI_event e "+
				"LEFT JOIN SAKAI_SESSION s ON e.session_id = s.session_id ORDER BY e.event_date ASC LIMIT 400000",
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if ("mysql".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())) {
			// So that we dont' store the whole result set in memory
			preparedStatement.setFetchSize(Integer.MIN_VALUE);
		}
		ResultSet resultSet = preparedStatement.executeQuery();

		// How long to wait until we post the events (can't initialize until first run through.)
		Date postBatchAt = null;
		int droppedEvents = 0, events = 0;
		Random rnd = new Random();
		Map<String, EventProcessor> processors = new HashMap<String, EventProcessor>();
		long reported = System.currentTimeMillis();
		long reportEvery = 4000;
		long reportedLast = 0;
		while (resultSet.next()) {
			long now = System.currentTimeMillis();
			if (reported+reportEvery < now) {
				System.out.printf("Current rate: %.2f/sec\n", ((float)(events-reportedLast))/reportEvery*1000);
				reportedLast = events;
				reported = now;
			}
			Timestamp date = resultSet.getTimestamp("date");
					
			CustomEventImpl event = new CustomEventImpl(date,
					resultSet.getString("event"),
					resultSet.getString("ref"),
					resultSet.getString("context"),
					resultSet.getString("user"),
					resultSet.getString("session_id"));
			String serverId = resultSet.getString("server");
			if (serverId != null) {
				serverId = extractServerId(serverId);
			} else {
				String sessionId = resultSet.getString("session_id");
				serverId = extractServerIdFromSession(sessionId);
			}
			// We can't put null in map.
			if (serverId != null) {
				//serverId = serverId+ rnd.nextInt(1);
				//serverId = "single";
				if(!processors.containsKey(serverId)) { 
					EventProcessor processor = new EventProcessor(updateInstance());
					processor.start();
					processors.put(serverId, processor);
				}
				processors.get(serverId).addEvent(event);
				// Setup if needed.
				if (postBatchAt == null) {
					postBatchAt = calcNextDate(date);
				}
				if (date.after(postBatchAt)) {
					// Send all the batches to the threads.
					for (EventProcessor processor: processors.values()) {
						processor.postToQueue();
					}
					postBatchAt = calcNextDate(date);
				}
			} else {
				droppedEvents++;
			}
			//resultSet.next();
			events++;
		}
		for (EventProcessor processor: processors.values()) {
			processor.halt();
			processor.interrupt();
			processor.join();
			log.debug(processor.getName()+ " Stopped");
		}
		log.debug("Events dropped: "+ droppedEvents);
	}

	protected Date calcNextDate(Timestamp date) {
		return new Date(date.getTime() + 8000);
	}
	
	public StatsUpdateManagerImpl updateInstance() {
		StatsUpdateManagerImpl updateManager = new StatsUpdateManagerImpl();
		updateManager.setSessionFactory(sessionFactory);
		updateManager.setCollectEventsForSiteWithToolOnly(false);
		updateManager.setSiteService(siteService);
		updateManager.setEventRegistryService(eventRegistryService);
		updateManager.setStatsManager(statsManager);
		updateManager.setUsageSessionService(usageSessionService);
		updateManager.setEventTrackingService(eventTrackingService);
		return updateManager;
	}
	
	class EventProcessor extends Thread {

		List<Event> batch = new ArrayList<Event>();
		BlockingQueue<List<Event>> queue = new ArrayBlockingQueue<List<Event>>(1);
		StatsUpdateManagerImpl updateManager;
		boolean run = true;
		
		public EventProcessor(StatsUpdateManagerImpl updateManager) {
			this.updateManager = updateManager;
		}

		@Override
		public void run() {

			try {
				do {
					
					List<Event> events = queue.take();
					long start = System.currentTimeMillis();
					updateManager.collectEvents(events);
					long end = System.currentTimeMillis();
//					log.debug(Thread.currentThread().getName()+ " took "+ (end - start)+ "ms for "+ events.size());
				} while (run);
			} catch (InterruptedException e) {
				
			}

		}
		
		public void postToQueue() throws InterruptedException {
			queue.put(batch);
			batch = new ArrayList<Event>();
		}

		public void addEvent(Event event) {
			batch.add(event);
		}
		
		public void halt() {
			run = false;
		}
	}
}
