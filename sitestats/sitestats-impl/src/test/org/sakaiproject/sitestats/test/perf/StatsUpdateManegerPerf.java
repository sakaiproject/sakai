package org.sakaiproject.sitestats.test.perf;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.sql.DataSource;

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
import org.sakaiproject.sitestats.test.perf.mock.MockEventRegistryService;
import org.sakaiproject.sitestats.test.perf.mock.MockEventTrackingService;
import org.sakaiproject.sitestats.test.perf.mock.MockSiteService;
import org.sakaiproject.sitestats.test.perf.mock.MockStatsManager;
import org.sakaiproject.sitestats.test.perf.mock.MockUsageSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations = { "classpath:/hibernate-beans.xml", "classpath:/hbm-db.xml"})
public class StatsUpdateManegerPerf {
	
	
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
		siteService = new MockSiteService();
		eventRegistryService = new MockEventRegistryService();
		statsManager = new MockStatsManager();
		usageSessionService = new MockUsageSessionService();
		eventTrackingService = new MockEventTrackingService();
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
		System.out.println("Transation islation is: "+ isolation);
		// As we can't haven mutlple @RunWith annotations.

		// We're not going todo live lookups
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
		preparedStatement.setFetchSize(Integer.MIN_VALUE);
		ResultSet resultSet = preparedStatement.executeQuery();

		// How long to wait until we post the events (can't initialize until first run through.)
		Date postBatchAt = null;
		int droppedEvents = 0;
		Random rnd = new Random();
		Map<String, EventProcessor> processors = new HashMap<String, EventProcessor>();
		while (resultSet.next()) {
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
			resultSet.next();
		}
		for (EventProcessor processor: processors.values()) {
			processor.halt();
			processor.interrupt();
			processor.join();
			System.out.println(processor.getName()+ " Stopped");
		}
		System.out.println("Events dropped: "+ droppedEvents);
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
//					System.out.println(Thread.currentThread().getName()+ " took "+ (end - start)+ "ms for "+ events.size());
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
