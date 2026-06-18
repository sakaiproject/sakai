/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class WidgetMetricSupport {

	private final SiteStatsWidgetDefinitionSupport support;

	WidgetMetricSupport(SiteStatsWidgetDefinitionSupport support) {
		this.support = support;
	}

	double percent(long partial, long total) {
		return total == 0 ? 0 : Util.round(100 * partial / (double) total, 0);
	}

	String msToString(long ms) {
		StringJoiner time = new StringJoiner(" ");
		String hoursAbbr = support.message("hours_abbr");
		String minsAbbr = support.message("minutes_abbr");
		String secsAbbr = support.message("seconds_abbr");
		long totalSecs = ms / 1000;
		long hours = totalSecs / 3600;
		long mins = (totalSecs / 60) % 60;
		long secs = totalSecs % 60;
		String minsString = mins == 0 ? "0" : Long.toString(mins);
		String secsString = secs == 0 ? "0" : Long.toString(secs);
		if (hours > 0) {
			time.add(Long.toString(hours)).add(hoursAbbr).add(minsString).add(minsAbbr).add(secsString).add(secsAbbr);
		} else if (mins > 0) {
			time.add(Long.toString(mins)).add(minsAbbr).add(secsString).add(secsAbbr);
		} else {
			time.add(secsString).add(secsAbbr);
		}
		return time.toString();
	}

	String userDisplayId(String userId) {
		if (userId == null || "-".equals(userId) || EventTrackingService.UNKNOWN_USER.equals(userId)) {
			return "-";
		}
		try {
			return support.getUserDirectoryService().getUser(userId).getDisplayId();
		} catch (UserNotDefinedException e) {
			return userId;
		}
	}

	String userTooltip(String userId) {
		if (userId == null) {
			return null;
		}
		if ("-".equals(userId)) {
			return support.message("user_anonymous");
		}
		if (EventTrackingService.UNKNOWN_USER.equals(userId)) {
			return support.message("user_anonymous_access");
		}
		return support.getStatsManager().getUserNameForDisplay(userId);
	}

	long sitePresenceDuration(String siteId, List<String> userIds) {
		ReportDef reportDef = support.getReportFactory().baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWho(userIds == null ? ReportManager.WHO_ALL : ReportManager.WHO_CUSTOM);
		if (userIds != null) {
			params.setWhoUserIds(userIds);
		}
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_SITE));
		Report report = support.getReportManager().getReport(reportDef, true);
		if (report.getReportData().isEmpty()) {
			return 0;
		}
		return ((SitePresence) report.getReportData().get(0)).getDuration();
	}

	Date firstPresenceDate(String siteId) {
		ReportDef reportDef = support.getReportFactory().baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE));
		params.setHowSort(true);
		params.setHowSortAscending(true);
		params.setHowSortBy(StatsManager.T_DATE);
		PagingPosition paging = new PagingPosition();
		Report report = support.getReportManager().getReport(reportDef, true, paging, false);
		if (report.getReportData().isEmpty()) {
			return new Date();
		}
		return ((SitePresence) report.getReportData().get(0)).getDate();
	}

	int countExistingResources(Report report) {
		int total = 0;
		ContentHostingService contentHostingService = support.getContentHostingService();
		for (Stat stat : report.getReportData()) {
			try {
				String resourceId = ((ResourceStat) stat).getResourceRef();
				String prefix = "/content";
				if (resourceId.startsWith(prefix)) {
					resourceId = resourceId.substring(prefix.length());
				}
				if (!resourceId.endsWith("/")) {
					contentHostingService.checkResource(resourceId);
					total++;
				}
			} catch (PermissionException e) {
				total++;
			} catch (IdUnusedException | TypeException e) {
				log.debug("Skipping unavailable SiteStats resource metric row", e);
			} catch (Exception e) {
				log.debug("Skipping unreadable SiteStats resource metric row", e);
			}
		}
		return total;
	}
}
