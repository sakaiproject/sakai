/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TimeZone;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SitePresenceTotal;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public class WidgetMetricSupport {

	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetReportDefFactory reportFactory;

	double percent(long partial, long total) {
		return total == 0 ? 0 : Util.round(100 * partial / (double) total, 0);
	}

	String msToString(long ms) {
		StringJoiner time = new StringJoiner(" ");
		String hoursAbbr = context.message("hours_abbr");
		String minsAbbr = context.message("minutes_abbr");
		String secsAbbr = context.message("seconds_abbr");
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
			return context.getUserDirectoryService().getUser(userId).getDisplayId();
		} catch (UserNotDefinedException e) {
			return userId;
		}
	}

	String userTooltip(String userId) {
		if (userId == null) {
			return null;
		}
		if ("-".equals(userId)) {
			return context.message("user_anonymous");
		}
		if (EventTrackingService.UNKNOWN_USER.equals(userId)) {
			return context.message("user_anonymous_access");
		}
		return context.getStatsManager().getUserNameForDisplay(userId);
	}

	long sitePresenceDuration(String siteId, List<String> userIds) {
		return sitePresenceDuration(siteId, userIds, ReportManager.WHEN_ALL);
	}

	long sitePresenceDuration(String siteId, List<String> userIds, String when) {
		ReportDef reportDef = reportFactory.baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWhen(StringUtils.defaultIfBlank(when, ReportManager.WHEN_ALL));
		params.setWho(userIds == null ? ReportManager.WHO_ALL : ReportManager.WHO_CUSTOM);
		if (userIds != null) {
			params.setWhoUserIds(userIds);
		}
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_SITE));
		Report report = context.getReportManager().getReport(reportDef, true);
		if (report.getReportData().isEmpty()) {
			return 0;
		}
		return ((SitePresence) report.getReportData().get(0)).getDuration();
	}

	boolean presencesEnabled() {
		return Boolean.TRUE.equals(context.getStatsManager().getEnableSitePresences());
	}

	WidgetMetricValue lastVisitValue(String siteId, String userId, boolean includeUserDetail) {
		LastVisit visit = findLastVisit(siteId, userId);
		if (visit == null) {
			return WidgetMetricValue.of("-");
		}
		String primary = formatDate(visit.getDate());
		if (includeUserDetail) {
			return WidgetMetricValue.withDetail(primary, userTooltip(visit.getUserId()));
		}
		return WidgetMetricValue.of(primary);
	}

	WidgetMetricValue presenceDurationValue(String siteId, List<String> userIds, String when) {
		if (!presencesEnabled()) {
			return WidgetMetricValue.of("-");
		}
		return WidgetMetricValue.of(msToString(sitePresenceDuration(siteId, userIds, when)));
	}

	WidgetMetricValue averagePresencePerVisit(String siteId) {
		if (!presencesEnabled()) {
			return WidgetMetricValue.of("-");
		}
		long durationInMs = sitePresenceDuration(siteId, null);
		Date firstPresenceDate = firstPresenceDate(siteId);
		long totalVisits = context.getStatsManager().getTotalSiteVisits(siteId, firstPresenceDate, null);
		double durationInMin = durationInMs == 0 || totalVisits == 0 ? 0
				: Util.round((durationInMs / (double) totalVisits) / 1000 / 60, 1);
		return WidgetMetricValue.of(durationInMin + " " + context.message("minutes_abbr"));
	}

	WidgetMetricValue averagePresencePerVisitForUser(String siteId, String userId) {
		if (!presencesEnabled() || StringUtils.isBlank(userId)) {
			return WidgetMetricValue.of("-");
		}
		long visits = context.getStatsManager().getTotalSiteVisitsForUser(siteId, userId);
		if (visits == 0) {
			return WidgetMetricValue.of("0");
		}
		long duration = sitePresenceDuration(siteId, Arrays.asList(userId));
		return WidgetMetricValue.of(msToString(duration / visits));
	}

	String formatDate(Date date) {
		if (date == null) {
			return "-";
		}
		Locale locale = context.getMessages() == null ? Locale.getDefault() : context.getMessages().getLocale();
		if (locale == null) {
			locale = Locale.getDefault();
		}
		return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
				.format(Instant.ofEpochMilli(date.getTime()).atZone(localZoneId()).toLocalDate());
	}

	private ZoneId localZoneId() {
		UserTimeService userTimeService = context.getUserTimeService();
		if (userTimeService != null) {
			TimeZone timeZone = userTimeService.getLocalTimeZone();
			if (timeZone != null) {
				return timeZone.toZoneId();
			}
		}
		return ZoneId.systemDefault();
	}

	private LastVisit findLastVisit(String siteId, String userId) {
		LastVisit fromTotals = lastVisitFromPresenceTotals(siteId, userId);
		if (fromTotals != null) {
			return fromTotals;
		}
		return lastVisitFromEvents(siteId, userId);
	}

	private LastVisit lastVisitFromPresenceTotals(String siteId, String userId) {
		Map<String, SitePresenceTotal> totals = context.getStatsManager().getPresenceTotalsForSite(siteId);
		if (totals == null || totals.isEmpty()) {
			return null;
		}
		if (StringUtils.isNotBlank(userId)) {
			SitePresenceTotal total = totals.get(userId);
			if (total == null || total.getLastVisitTime() == null) {
				return null;
			}
			return new LastVisit(userId, total.getLastVisitTime());
		}
		LastVisit latest = null;
		for (SitePresenceTotal total : totals.values()) {
			if (total.getLastVisitTime() == null) {
				continue;
			}
			if (latest == null || total.getLastVisitTime().after(latest.getDate())) {
				latest = new LastVisit(total.getUserId(), total.getLastVisitTime());
			}
		}
		return latest;
	}

	private LastVisit lastVisitFromEvents(String siteId, String userId) {
		ReportDef reportDef = reportFactory.baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		if (StringUtils.isNotBlank(userId)) {
			params.setWho(ReportManager.WHO_CUSTOM);
			params.setWhoUserIds(Arrays.asList(userId));
		} else {
			params.setWho(ReportManager.WHO_ALL);
		}
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_USER, StatsManager.T_LASTDATE));
		params.setHowSort(true);
		params.setHowSortBy(StatsManager.T_LASTDATE);
		params.setHowSortAscending(false);
		Report report = context.getReportManager().getReport(reportDef, true);
		if (report.getReportData().isEmpty()) {
			return null;
		}
		EventStat stat = (EventStat) report.getReportData().get(0);
		if (stat.getDate() == null) {
			return null;
		}
		return new LastVisit(stat.getUserId(), stat.getDate());
	}

	@Getter
	private static class LastVisit {
		private final String userId;
		private final Date date;

		LastVisit(String userId, Date date) {
			this.userId = userId;
			this.date = date;
		}
	}

	Date firstPresenceDate(String siteId) {
		ReportDef reportDef = reportFactory.baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE));
		params.setHowSort(true);
		params.setHowSortAscending(true);
		params.setHowSortBy(StatsManager.T_DATE);
		PagingPosition paging = new PagingPosition();
		Report report = context.getReportManager().getReport(reportDef, true, paging, false);
		if (report.getReportData().isEmpty()) {
			return new Date();
		}
		return ((SitePresence) report.getReportData().get(0)).getDate();
	}

	int countExistingResources(Report report) {
		int total = 0;
		ContentHostingService contentHostingService = context.getContentHostingService();
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
