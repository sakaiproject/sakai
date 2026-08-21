/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.sakaiproject.sitestats.api.StatsManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StatsSqlBuilderTest {

	@Test
	public void groupsMysqlUserReportsByTheAnonymizedUser() {
		StatsManagerImpl.StatsSqlBuilder builder = new StatsManagerImpl.StatsSqlBuilder(
				"mysql", StatsManager.Q_TYPE_EVENT, Arrays.asList(StatsManager.T_USER), "site-id",
				Arrays.asList("chat.new", "poll.vote"), Collections.singleton("poll.vote"), true,
				null, null, null, null, null, false, StatsManager.T_USER, false);

		String hql = builder.getHQL();
		String anonymizedUser = "(CASE WHEN s.eventId not in ('poll.vote') THEN s.userId ELSE '-' END)";
		assertTrue(hql.contains(anonymizedUser + " as user"));
		assertTrue(hql.contains("group by s.siteId, " + anonymizedUser));
		assertTrue(hql.contains("order by " + anonymizedUser + " DESC"));
		assertFalse(hql.contains("group by s.siteId, s.eventId, s.userId"));
	}

	@Test
	public void recognizesMariaDbAsMysqlCompatible() {
		assertEquals("mysql", StatsManagerImpl.identifyDbVendor("org.hibernate.dialect.MariaDBDialect"));
		assertEquals("mysql", StatsManagerImpl.identifyDbVendor("org.hibernate.dialect.MySQL8Dialect"));
		assertEquals("oracle", StatsManagerImpl.identifyDbVendor("org.hibernate.dialect.Oracle12cDialect"));
		assertEquals("hsql", StatsManagerImpl.identifyDbVendor("org.hibernate.dialect.HSQLDialect"));
	}

	@Test
	public void doesNotAnonymizeQueriesWithoutAUserColumn() {
		StatsManagerImpl.StatsSqlBuilder builder = new StatsManagerImpl.StatsSqlBuilder(
				"mysql", StatsManager.Q_TYPE_EVENT, Arrays.asList(StatsManager.T_TOOL), "site-id",
				Arrays.asList("chat.new", "poll.vote"), Collections.singleton("poll.vote"), true,
				null, null, null, null, null, false, StatsManager.T_TOTAL, false);

		assertFalse(builder.getHQL().contains("anonymousEvents"));
		assertFalse(builder.hasAnonymousUserParameter());
	}
}
