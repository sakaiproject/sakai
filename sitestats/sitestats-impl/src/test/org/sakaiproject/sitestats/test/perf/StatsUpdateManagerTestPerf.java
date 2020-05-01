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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.impl.CustomEventImpl;
import org.sakaiproject.sitestats.test.SiteStatsTestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

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
@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@Sql("/update-manager-perf.sql")
@Transactional(transactionManager = "org.sakaiproject.sitestats.SiteStatsTransactionManager")
public class StatsUpdateManagerTestPerf extends AbstractTransactionalJUnit4SpringContextTests {

	@Resource(name = "javax.sql.DataSource")
	private DataSource dataSource;
	@Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
	private SessionFactory sessionFactory;
	@Resource(name = "org.sakaiproject.sitestats.api.StatsUpdateManager")
	private StatsUpdateManager statsUpdateManager;

	@Before
	public void setUp() throws SQLException {

		String isolation;
		Connection connection = dataSource.getConnection();
		switch (connection.getTransactionIsolation()) {
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
		log.info("Transaction isolation is: "+ isolation);

		sessionFactory.openStatelessSession(connection);
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
	

	public void getEvents() throws InterruptedException {
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
				"SELECT e.event_date, e.event, e.ref, e.context, s.session_user, e.session_id, s.session_server server " +
				"FROM SAKAI_EVENT e " +
				"LEFT JOIN SAKAI_SESSION s ON e.session_id = s.session_id ORDER BY e.event_date ASC LIMIT 400000"
		);
		query.setReadOnly(true);
		query.setFetchSize(0);
		ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);

		// How long to wait until we post the events (can't initialize until first run through.)
		Date postBatchAt = null;
		int droppedEvents = 0, events = 0;
		Map<String, EventProcessor> processors = new HashMap<>();
		long reported = System.currentTimeMillis();
		long reportEvery = 4000;
		long reportedLast = 0;
		while (results.next()) {
			long now = System.currentTimeMillis();
			if (reported+reportEvery < now) {
				log.info("Current rate: {}/sec", String.format("%.2f", (float)(events-reportedLast)/reportEvery*1000));
				reportedLast = events;
				reported = now;
			}

			Object[] row = results.get();
			CustomEventImpl event = new CustomEventImpl(
					(Date) row[0],
					(String) row[1],
					(String) row[2],
					(String) row[3],
					(String) row[4],
					(String) row[5]);
			String serverId = (String) row[6];
			if (serverId != null) {
				serverId = extractServerId(serverId);
			} else {
				String sessionId = results.getString(5);
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
					postBatchAt = calcNextDate(event.getDate());
				}
				if (event.getDate().after(postBatchAt)) {
					// Send all the batches to the threads.
					for (EventProcessor processor: processors.values()) {
						processor.postToQueue();
					}
					postBatchAt = calcNextDate(event.getDate());
				}
			} else {
				droppedEvents++;
			}
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

	protected Date calcNextDate(Date date) {
		return new Date(date.getTime() + 8000);
	}
	
	public StatsUpdateManager updateInstance() {
		statsUpdateManager.setCollectEventsForSiteWithToolOnly(false);
		return statsUpdateManager;
	}
	
	class EventProcessor extends Thread {

		private List<Event> batch = new ArrayList<>();
		private BlockingQueue<List<Event>> queue = new ArrayBlockingQueue<>(1);
		private StatsUpdateManager updateManager;
		private boolean run = true;
		
		public EventProcessor(StatsUpdateManager updateManager) {
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
					log.debug("{} took {}ms for {}", Thread.currentThread().getName(), (end - start), events.size());
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
