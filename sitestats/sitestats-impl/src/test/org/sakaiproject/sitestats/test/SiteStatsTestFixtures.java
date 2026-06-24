/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;

final class SiteStatsTestFixtures {

	private SiteStatsTestFixtures() {
	}

	static Site site(String siteId, String title, String... roleIds) {
		Site site = mock(Site.class);
		when(site.getId()).thenReturn(siteId);
		when(site.getTitle()).thenReturn(title);
		if (roleIds.length > 0) {
			LinkedHashSet<Role> roles = new LinkedHashSet<Role>();
			for (String roleId : roleIds) {
				Role role = mock(Role.class);
				when(role.getId()).thenReturn(roleId);
				roles.add(role);
			}
			when(site.getRoles()).thenReturn(roles);
		}
		return site;
	}

	static ToolInfo tool(String toolId, String eventId) {
		ToolInfo toolInfo = new ToolInfo(toolId);
		toolInfo.setSelected(true);
		EventInfo eventInfo = event(eventId, true);
		toolInfo.setEvents(Arrays.asList(eventInfo));
		return toolInfo;
	}

	static EventInfo event(String eventId, boolean selected) {
		EventInfo eventInfo = new EventInfo(eventId);
		eventInfo.setSelected(selected);
		return eventInfo;
	}

	static SiteVisits visitStat(String siteId, Date date, long totalVisits, long totalUnique) {
		SiteVisits stat = new SiteVisitsImpl();
		stat.setSiteId(siteId);
		stat.setDate(date);
		stat.setTotalVisits(totalVisits);
		stat.setTotalUnique(totalUnique);
		stat.setCount(totalVisits);
		return stat;
	}

	static ReportDef visitReport(String siteId, String userId) {
		ReportDef reportDef = new ReportDef();
		reportDef.setSiteId(siteId);
		reportDef.setTitle("Visits");
		reportDef.setCreatedBy(userId);
		reportDef.setCreatedOn(new java.util.Date());
		reportDef.setModifiedBy(userId);
		reportDef.setModifiedOn(new java.util.Date());
		ReportParams params = new ReportParams(siteId);
		params.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_VISITS, StatsManager.T_UNIQUEVISITS));
		params.setHowSortBy(StatsManager.T_DATE);
		reportDef.setReportParams(params);
		return reportDef;
	}
}
