/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.sitestats.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.SiteStatsDemoSeedService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Seeds SiteStats aggregates via {@link StatsUpdateManager} and sample session User-Agents for the sitestats tool demo.
 */
@Slf4j
public class SiteStatsDemoSeedServiceImpl implements SiteStatsDemoSeedService {

	public static final String SESSION_ID_PREFIX = "sitestats-demo-ua-";

	private static final String[] ACTIVITY_EVENTS = {
			"content.read",
			"content.new",
			"asn.submit.submission",
			"asn.read.assignment",
			"gradebook.studentView",
			"conversations.post.viewed",
			"conversations.topic.created"
	};

	private static final String[] DEMO_USER_AGENTS = {
			// Desktop
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 14_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15",
			"Mozilla/5.0 (X11; Linux x86_64; rv:121.0) Gecko/20100101 Firefox/121.0",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
			// Mobile
			"Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
			"Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
			// Tablet
			"Mozilla/5.0 (iPad; CPU OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
			"Mozilla/5.0 (Linux; Android 13; SM-X700) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
	};

	@Setter private ObjectFactory<StatsUpdateManager> statsUpdateManagerFactory;
	@Setter private SessionFactory sessionFactory;
	@Setter private TransactionTemplate transactionTemplate;
	@Setter private SqlService sqlService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SimplePageToolDao simplePageToolDao;
	@Setter private UserTimeService userTimeService;

	private final Random random = new Random(42);

	@Override
	public void seed(Collection<Site> sites, Collection<User> students, Collection<User> instructors) {
		if (sites == null || sites.isEmpty()) {
			log.info("SiteStats demo seed skipped: no sites");
			return;
		}

		List<Site> demoSites = sites.stream()
				.filter(site -> site != null && StringUtils.isNotBlank(site.getId()))
				.filter(site -> site.getProperties() != null
						&& "true".equalsIgnoreCase(site.getProperties().getProperty(DEMO_SITE_PROPERTY)))
				.toList();
		if (demoSites.isEmpty()) {
			log.info("SiteStats demo seed skipped: no marked demo sites");
			return;
		}

		int historyDays = serverConfigurationService.getInt("sitestats.demo.seed.days", 30);
		int visitsPerUser = serverConfigurationService.getInt("sitestats.demo.seed.visits.per.user", 8);
		int sessionCount = serverConfigurationService.getInt("sitestats.demo.seed.sessions", 24);

		List<User> studentList = students == null ? List.of() : new ArrayList<>(students);
		List<User> instructorList = instructors == null ? List.of() : new ArrayList<>(instructors);
		List<User> actors = new ArrayList<>(studentList);
		actors.addAll(instructorList);
		if (actors.isEmpty()) {
			log.warn("SiteStats demo seed: no users to attribute events to");
			return;
		}

		int seededSites = 0;
		int completedSites = 0;
		for (Site site : demoSites) {
			Set<String> memberIds = site.getUsers();
			List<User> siteActors = actors.stream().filter(user -> memberIds.contains(user.getId())).toList();
			List<User> siteStudents = studentList.stream().filter(user -> memberIds.contains(user.getId())).toList();
			if (siteActors.isEmpty()) {
				log.warn("SiteStats demo seed skipped: no enrolled users in site {}", site.getId());
				continue;
			}
			try {
				if (seedOnce("site:" + site.getId(), () -> seedSiteEvents(statsUpdateManagerFactory.getObject(),
						site, siteActors, siteStudents, historyDays, visitsPerUser))) {
					seededSites++;
				}
				completedSites++;
			} catch (Exception e) {
				log.warn("SiteStats demo seed failed for site {}", site.getId(), e);
			}
		}

		if (completedSites == 0) {
			log.warn("SiteStats demo seed skipped: no completed sites");
			return;
		}
		seedOnce("server", () -> seedServerEvents(statsUpdateManagerFactory.getObject(), actors, historyDays));
		seedSessionUserAgents(actors, sessionCount, historyDays);

		log.info("SiteStats demo seed completed: {} site(s), {} day history", seededSites, historyDays);
	}

	private boolean seedOnce(String seedKey, Runnable seedAction) {
		Boolean seeded = transactionTemplate.execute(status -> {
			Session session = sessionFactory.getCurrentSession();
			if (session.find(DemoSeed.class, seedKey) != null) {
				return false;
			}
			// The reservation and aggregates commit together; failed work can be retried.
			session.persist(new DemoSeed(seedKey));
			session.flush();
			seedAction.run();
			return true;
		});
		return Boolean.TRUE.equals(seeded);
	}

	private void seedSiteEvents(StatsUpdateManager statsUpdateManager, Site site, List<User> actors,
			List<User> students, int historyDays, int visitsPerUser) {
		String siteId = site.getId();
		List<Event> events = new ArrayList<>();
		List<User> visitors = students.isEmpty() ? actors : students;
		int baseVisitorCount = Math.min(visitors.size(), Math.max(5, visitors.size() / 4));
		ZoneId zone = userTimeService.getLocalTimeZone().toZoneId();
		LocalDate today = LocalDate.now(zone);

		for (int dayOffset = 0; dayOffset < historyDays; dayOffset++) {
			LocalDate day = today.minusDays(dayOffset);
			double intensity = visitDayIntensity(day, dayOffset);
			if (intensity <= 0.05) {
				// Quiet day: skip visits almost entirely
				continue;
			}
			int visitorCount = Math.max(1, (int) Math.round(baseVisitorCount * intensity));
			visitorCount = Math.min(visitorCount, visitors.size());

			for (int v = 0; v < visitorCount; v++) {
				User user = visitors.get((dayOffset + v) % visitors.size());
				int maxVisits = Math.max(1, (int) Math.round((visitsPerUser / 4.0) * intensity));
				int visitsToday = 1 + random.nextInt(maxVisits);
				for (int i = 0; i < visitsToday; i++) {
					Instant begin = dayAtHour(day, random.nextInt(10), zone);
					Instant end = begin.plus(5 + random.nextInt(55), ChronoUnit.MINUTES);
					String presenceRef = "/presence/" + siteId + PresenceService.PRESENCE_SUFFIX;
					String sessionId = "demo-sess-" + siteId + "-" + user.getId() + "-" + dayOffset + "-" + i;
					events.add(statsUpdateManager.buildEvent(Date.from(begin), StatsManager.SITEVISIT_EVENTID,
							presenceRef, null, user.getId(), sessionId));
					events.add(statsUpdateManager.buildEvent(Date.from(end), StatsManager.SITEVISITEND_EVENTID,
							presenceRef, null, user.getId(), sessionId));
				}
			}

			// Activity + resources (also scales with the day)
			int activityCount = Math.max(1, (int) Math.round((3 + random.nextInt(4)) * intensity));
			for (int a = 0; a < activityCount; a++) {
				User user = actors.get(random.nextInt(actors.size()));
				String eventId = ACTIVITY_EVENTS[random.nextInt(ACTIVITY_EVENTS.length)];
				String ref;
				if (eventId.startsWith("content.")) {
					ref = "/content/group/" + siteId + "/searchdata/demo-file-" + (a + 1) + ".txt";
				} else if (eventId.startsWith("asn.")) {
					ref = "/assignment/a/" + siteId + "/demo-asn-" + (a + 1);
				} else if (eventId.startsWith("conversations.")) {
					ref = "/conversations/" + siteId + "/topic/demo-" + (a + 1);
				} else {
					ref = "/gradebook/" + siteId;
				}
				events.add(statsUpdateManager.buildEvent(Date.from(dayAtHour(day, random.nextInt(12), zone)),
						eventId, ref, siteId, user.getId(), "demo-act-" + dayOffset + "-" + a));
			}
		}

		// Lesson page reads against real pages when available
		List<SimplePage> pages = listLessonPages(siteId);
		if (!pages.isEmpty()) {
			for (int dayOffset = 0; dayOffset < Math.min(historyDays, 14); dayOffset++) {
				LocalDate day = today.minusDays(dayOffset);
				SimplePage page = pages.get(dayOffset % pages.size());
				User user = visitors.get(dayOffset % visitors.size());
				String pageRef = "/lessonbuilder/page/" + page.getPageId();
				events.add(statsUpdateManager.buildEvent(Date.from(dayAtHour(day, 2, zone)),
						LessonBuilderEvents.PAGE_READ, pageRef, siteId, user.getId(),
						"demo-lb-" + dayOffset));
			}
		}

		flushEvents(statsUpdateManager, events);
	}

	private void seedServerEvents(StatsUpdateManager statsUpdateManager, List<User> actors, int historyDays) {
		List<Event> events = new ArrayList<>();
		ZoneId zone = userTimeService.getLocalTimeZone().toZoneId();
		LocalDate today = LocalDate.now(zone);
		int days = Math.min(historyDays, 14);
		for (int dayOffset = 0; dayOffset < days; dayOffset++) {
			LocalDate day = today.minusDays(dayOffset);
			int logins = 5 + random.nextInt(15);
			for (int i = 0; i < logins; i++) {
				User user = actors.get(random.nextInt(actors.size()));
				events.add(statsUpdateManager.buildEvent(Date.from(dayAtHour(day, random.nextInt(20), zone)),
						StatsManager.LOGIN_EVENTID, "/user/" + user.getId(), null,
						user.getId(), "demo-login-" + dayOffset + "-" + i));
			}
			if (dayOffset % 3 == 0) {
				User user = actors.get(random.nextInt(actors.size()));
				Date dayDate = Date.from(dayAtHour(day, 10, zone));
				events.add(statsUpdateManager.buildEvent(dayDate, "user.add", "/user/" + user.getId(),
						null, user.getId(), "demo-useradd-" + dayOffset));
				events.add(statsUpdateManager.buildEvent(dayDate, "site.add", "/site/demo-site-" + dayOffset,
						null, user.getId(), "demo-siteadd-" + dayOffset));
			}
		}
		flushEvents(statsUpdateManager, events);
	}

	private void seedSessionUserAgents(List<User> actors, int sessionCount, int historyDays) {
		if (sessionCount <= 0 || actors.isEmpty()) {
			return;
		}
		Instant now = Instant.now();
		for (int i = 0; i < sessionCount; i++) {
			User user = actors.get(i % actors.size());
			String sessionId = SESSION_ID_PREFIX + String.format("%03d", i);
			if (demoSessionExists(sessionId)) {
				continue;
			}
			String ua = DEMO_USER_AGENTS[i % DEMO_USER_AGENTS.length];
			Instant end = now.minus(random.nextInt(Math.max(1, historyDays)), ChronoUnit.DAYS)
					.minus(random.nextInt(12), ChronoUnit.HOURS);
			Instant start = end.minus(30 + random.nextInt(120), ChronoUnit.MINUTES);
			insertSession(sessionId, user.getId(), ua, start, end);
		}
		log.info("Completed demo SAKAI_SESSION seeding for {} session IDs", sessionCount);
	}

	private void flushEvents(StatsUpdateManager statsUpdateManager, List<Event> events) {
		if (events.isEmpty()) {
			return;
		}
		// Process in chunks to bound memory
		int chunkSize = 200;
		for (int i = 0; i < events.size(); i += chunkSize) {
			List<Event> chunk = events.subList(i, Math.min(i + chunkSize, events.size()));
			if (!statsUpdateManager.collectEvents(new ArrayList<>(chunk))) {
				throw new IllegalStateException("Failed to persist SiteStats demo event chunk starting at index " + i);
			}
		}
	}

	private List<SimplePage> listLessonPages(String siteId) {
		if (simplePageToolDao == null) {
			return List.of();
		}
		try {
			List<SimplePage> pages = simplePageToolDao.getSitePages(siteId);
			return pages == null ? List.of() : pages;
		} catch (RuntimeException e) {
			log.debug("Unable to list lesson pages for {}: {}", siteId, e.toString());
			return List.of();
		}
	}

	private boolean demoSessionExists(String sessionId) {
		Connection connection = null;
		try {
			connection = sqlService.borrowConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"select count(*) from SAKAI_SESSION where SESSION_ID = ?")) {
				statement.setString(1, sessionId);
				try (ResultSet result = statement.executeQuery()) {
					return result.next() && result.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Could not check demo session " + sessionId, e);
		} finally {
			sqlService.returnConnection(connection);
		}
	}

	private void insertSession(String sessionId, String userId, String userAgent, Instant start, Instant end) {
		Connection connection = null;
		try {
			connection = sqlService.borrowConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"insert into SAKAI_SESSION (SESSION_ID, SESSION_SERVER, SESSION_USER, SESSION_IP, "
							+ "SESSION_HOSTNAME, SESSION_USER_AGENT, SESSION_START, SESSION_END, SESSION_ACTIVE) "
							+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				statement.setString(1, sessionId);
				statement.setString(2, "demo-server");
				statement.setString(3, userId);
				statement.setString(4, "127.0.0.1");
				statement.setString(5, "localhost");
				statement.setString(6, StringUtils.left(userAgent, 255));
				statement.setTimestamp(7, Timestamp.from(start));
				statement.setTimestamp(8, Timestamp.from(end));
				statement.setBoolean(9, false);
				statement.executeUpdate();
				connection.commit();
			}
		} catch (SQLException e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException rollbackFailure) {
					e.addSuppressed(rollbackFailure);
				}
			}
			throw new IllegalStateException("Could not insert demo session " + sessionId, e);
		} finally {
			sqlService.returnConnection(connection);
		}
	}

	/**
	 * Day intensity for visits: weekends quieter, midweek stronger, plus sparse peak/quiet spikes.
	 * @return multiplier typically in ~0.15–1.8; near-zero for occasional quiet days
	 */
	private double visitDayIntensity(LocalDate day, int dayOffset) {
		DayOfWeek dow = day.getDayOfWeek();
		double base;
		if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
			base = 0.25 + random.nextDouble() * 0.25;
		} else if (dow == DayOfWeek.TUESDAY || dow == DayOfWeek.WEDNESDAY || dow == DayOfWeek.THURSDAY) {
			base = 0.9 + random.nextDouble() * 0.4;
		} else {
			base = 0.55 + random.nextDouble() * 0.35;
		}
		// Occasional assignment-deadline peaks and holiday-like troughs
		if (dayOffset % 11 == 3) {
			base *= 1.7;
		} else if (dayOffset % 9 == 5) {
			base *= 0.2;
		} else if (dayOffset % 13 == 0) {
			base = 0.0;
		}
		return Math.min(2.0, base);
	}

	private static Instant dayAtHour(LocalDate day, int hour, ZoneId zone) {
		return ZonedDateTime.of(day, LocalTime.of(hour, 0), zone).toInstant();
	}

}
