/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SitePresenceTotal;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsChartDataset;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public class WidgetMetricSupport {

	private static final String ROLE_COLUMN = "role";
	private static final int SPARKLINE_DAYS = 30;

	static final long BOUNCE_THRESHOLD_MS = 5L * 60L * 1000L;

	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetReportDefFactory reportFactory;

	double percent(long partial, long total) {
		return total == 0 ? 0 : Util.round(100 * partial / (double) total, 0);
	}

	String msToString(long ms) {
		long safeMs = Math.max(0L, ms);
		long totalSecs = safeMs / 1000;
		long hours = totalSecs / 3600;
		long mins = (totalSecs / 60) % 60;
		long secs = totalSecs % 60;
		String daysAbbr = context.message("days_abbr");
		String hoursAbbr = context.message("hours_abbr");
		String minsAbbr = context.message("minutes_abbr");
		String secsAbbr = context.message("seconds_abbr");
		List<String> parts = new ArrayList<>();
		if (hours >= 48) {
			parts.add((hours / 24) + " " + daysAbbr);
			hours = hours % 24;
		}
		if (hours > 0) {
			parts.add(hours + " " + hoursAbbr);
		}
		if (mins > 0) {
			parts.add(mins + " " + minsAbbr);
		}
		if (secs > 0 || parts.isEmpty()) {
			parts.add(secs + " " + secsAbbr);
		}
		return String.join(" ", parts);
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

	Set<String> siteUsers(String siteId) {
		Set<String> users = context.getStatsManager().getSiteUsers(siteId);
		return users == null ? Collections.<String>emptySet() : users;
	}

	Set<String> usersWithVisits(String siteId) {
		Set<String> users = context.getStatsManager().getUsersWithVisits(siteId);
		return users == null ? new HashSet<String>() : users;
	}

	int membersWhoVisitedCount(String siteId) {
		Set<String> siteUsers = siteUsers(siteId);
		Set<String> visited = usersWithVisits(siteId);
		int count = 0;
		for (String siteUser : siteUsers) {
			if (visited.contains(siteUser)) {
				count++;
			}
		}
		return count;
	}

	WidgetMetricValue membersVisitedValue(String siteId) {
		Set<String> enrolled = siteUsers(siteId);
		int visited = membersWhoVisitedCount(siteId);
		return WidgetMetricValue.withPercentage(visited + " / " + enrolled.size(), (int) percent(visited, enrolled.size()));
	}

	WidgetMetricValue uniqueVisitsValue(String siteId) {
		return WidgetMetricValue.of(Long.toString(context.getStatsManager().getTotalSiteUniqueVisits(siteId)));
	}

	SiteStatsChart last30DaysVisitsChart(String siteId, String userId) {
		ZoneId zone = localZoneId();
		List<LocalDate> days = sparklineDays(zone);
		Map<LocalDate, Long> values = new HashMap<LocalDate, Long>();
		if (StringUtils.isNotBlank(userId)) {
			for (Stat stat : visitRows(siteId, Arrays.asList(userId), ReportManager.WHEN_LAST30DAYS)) {
				addDayValue(values, toLocalDate(stat.getDate(), zone), stat.getCount());
			}
		} else {
			Date from = startOfDay(days.get(0), zone);
			Date to = startOfDay(days.get(days.size() - 1), zone);
			List<SiteVisits> visits = context.getStatsManager().getSiteVisits(siteId, from, to);
			if (visits != null) {
				for (SiteVisits dayVisits : visits) {
					addDayValue(values, toLocalDate(dayVisits.getDate(), zone), dayVisits.getTotalVisits());
				}
			}
		}
		return compactDailyChart(context.message("overview_title_visits_last30days"), StatsManager.T_VISITS,
				context.message("overview_title_visits"), days, values, zone);
	}

	SiteStatsChart last30DaysPresenceChart(String siteId, List<String> userIds) {
		ZoneId zone = localZoneId();
		List<LocalDate> days = sparklineDays(zone);
		Map<LocalDate, Long> values = new HashMap<LocalDate, Long>();
		for (SitePresence row : presenceRows(siteId, userIds, ReportManager.WHEN_LAST30DAYS)) {
			addDayValue(values, toLocalDate(row.getDate(), zone), Math.max(0L, row.getDuration()));
		}
		for (Map.Entry<LocalDate, Long> entry : values.entrySet()) {
			entry.setValue(Long.valueOf(entry.getValue().longValue() / 60000L));
		}
		return compactDailyChart(context.message("overview_title_presence_last30days"), StatsManager.T_DURATION,
				context.message("overview_title_presence_time"), days, values, zone);
	}

	SiteStatsReportView visitsByRoleView(String siteId, SiteStatsReportRequest request) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequest.normalized(request);
		String title = context.message("overview_title_visits");
		List<RoleVisitCount> counts = visitsByRole(siteId, dateFilter(safeRequest));

		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(title);
		view.setPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		if (safeRequest.isIncludeChart()) {
			view.setChart(visitsByRoleChart(title, counts));
		}
		if (safeRequest.isIncludeTable()) {
			view.setTable(visitsByRoleTable(title, counts, safeRequest));
		}
		return view;
	}

	WidgetMetricValue trafficTrendValue(String siteId, String userId) {
		ZoneId zone = localZoneId();
		LocalDate today = LocalDate.now(zone);
		Date currentFrom = startOfDay(today.minusDays(6), zone);
		Date currentTo = startOfDay(today, zone);
		Date previousFrom = startOfDay(today.minusDays(13), zone);
		Date previousTo = startOfDay(today.minusDays(7), zone);
		long current = visitCount(siteId, userId, currentFrom, currentTo);
		long previous = visitCount(siteId, userId, previousFrom, previousTo);
		String primary = Long.toString(current);
		if (previous == 0) {
			return current == 0 ? WidgetMetricValue.withPercentage(primary, 0) : WidgetMetricValue.of(primary);
		}
		int change = (int) Math.round(100d * (current - previous) / previous);
		return WidgetMetricValue.withPercentage(primary, change);
	}

	long visitCountForRole(String siteId, String roleId, String when) {
		ReportDef reportDef = reportFactory.baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		params.setWho(ReportManager.WHO_ROLE);
		params.setWhoRoleId(roleId);
		params.setWhen(StringUtils.defaultIfBlank(when, ReportManager.WHEN_ALL));
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_SITE));
		return sumCounts(context.getReportManager().getReport(reportDef, true));
	}

	WidgetMetricValue bounceRateValue(String siteId, List<String> userIds) {
		if (!presencesEnabled()) {
			return WidgetMetricValue.of("-");
		}
		List<SitePresence> rows = presenceRows(siteId, userIds, ReportManager.WHEN_ALL);
		int bounce = 0;
		for (SitePresence row : rows) {
			if (row.getDuration() < BOUNCE_THRESHOLD_MS) {
				bounce++;
			}
		}
		return WidgetMetricValue.withPercentage(bounce + " / " + rows.size(), (int) percent(bounce, rows.size()));
	}

	List<SitePresence> presenceRows(String siteId, List<String> userIds, String when) {
		ReportDef reportDef = reportFactory.baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWhen(StringUtils.defaultIfBlank(when, ReportManager.WHEN_ALL));
		params.setWho(userIds == null ? ReportManager.WHO_ALL : ReportManager.WHO_CUSTOM);
		if (userIds != null) {
			params.setWhoUserIds(userIds);
		}
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_USER));
		Report report = context.getReportManager().getReport(reportDef, true);
		List<SitePresence> rows = new ArrayList<SitePresence>();
		for (Stat stat : report.getReportData()) {
			if (stat instanceof SitePresence) {
				rows.add((SitePresence) stat);
			}
		}
		return rows;
	}

	private long visitCount(String siteId, String userId, Date from, Date to) {
		if (StringUtils.isNotBlank(userId)) {
			return eventVisitCount(siteId, Arrays.asList(userId), from, to);
		}
		return context.getStatsManager().getTotalSiteVisits(siteId, from, to);
	}

	private long eventVisitCount(String siteId, List<String> userIds, Date from, Date to) {
		ReportDef reportDef = reportFactory.baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		params.setWho(ReportManager.WHO_CUSTOM);
		params.setWhoUserIds(userIds);
		params.setWhen(ReportManager.WHEN_CUSTOM);
		params.setWhenFrom(from);
		params.setWhenTo(to);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_SITE));
		return sumCounts(context.getReportManager().getReport(reportDef, true));
	}

	private long sumCounts(Report report) {
		long total = 0;
		if (report == null || report.getReportData() == null) {
			return total;
		}
		for (Stat stat : report.getReportData()) {
			total += stat.getCount();
		}
		return total;
	}

	private Date startOfDay(LocalDate date, ZoneId zone) {
		return Date.from(date.atStartOfDay(zone).toInstant());
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

	WidgetMetricValue medianPresencePerVisit(String siteId) {
		return medianPresenceValue(siteId, null);
	}

	WidgetMetricValue medianPresencePerVisitForUser(String siteId, String userId) {
		if (StringUtils.isBlank(userId)) {
			return WidgetMetricValue.of("-");
		}
		return medianPresenceValue(siteId, Arrays.asList(userId));
	}

	private WidgetMetricValue medianPresenceValue(String siteId, List<String> userIds) {
		if (!presencesEnabled()) {
			return WidgetMetricValue.of("-");
		}
		return WidgetMetricValue.of(msToString(medianDuration(presenceRows(siteId, userIds, ReportManager.WHEN_ALL))));
	}

	private long medianDuration(List<SitePresence> rows) {
		if (rows == null || rows.isEmpty()) {
			return 0;
		}
		long[] durations = new long[rows.size()];
		for (int i = 0; i < rows.size(); i++) {
			durations[i] = rows.get(i).getDuration();
		}
		Arrays.sort(durations);
		int middle = durations.length / 2;
		if (durations.length % 2 == 0) {
			return (durations[middle - 1] + durations[middle]) / 2;
		}
		return durations[middle];
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

	private List<Stat> visitRows(String siteId, List<String> userIds, String when) {
		ReportDef reportDef = reportFactory.baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		params.setWhen(StringUtils.defaultIfBlank(when, ReportManager.WHEN_ALL));
		params.setWho(userIds == null ? ReportManager.WHO_ALL : ReportManager.WHO_CUSTOM);
		if (userIds != null) {
			params.setWhoUserIds(userIds);
		}
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE));
		Report report = context.getReportManager().getReport(reportDef, true);
		return report.getReportData() == null ? Collections.<Stat>emptyList() : report.getReportData();
	}

	private List<RoleVisitCount> visitsByRole(String siteId, String when) {
		List<RoleVisitCount> counts = new ArrayList<RoleVisitCount>();
		try {
			Site site = context.getSiteService().getSite(siteId);
			Set<Role> roles = site.getRoles();
			if (roles == null) {
				return counts;
			}
			for (Role role : roles) {
				if (role == null || role.getId() == null) {
					continue;
				}
				counts.add(new RoleVisitCount(role.getId(), visitCountForRole(siteId, role.getId(), when)));
			}
		} catch (IdUnusedException e) {
			return counts;
		}
		Collections.sort(counts, Comparator.comparingLong(RoleVisitCount::getVisits).reversed());
		return counts;
	}

	private SiteStatsChart visitsByRoleChart(String title, List<RoleVisitCount> counts) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_PIE);
		chart.setXKey(ROLE_COLUMN);
		chart.setYKey(StatsManager.T_TOTAL);
		chart.setEmptyMessage(context.message("no_data"));
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey(ROLE_COLUMN);
		dataset.setLabel(context.message("overview_title_visits"));
		for (RoleVisitCount count : counts) {
			SiteStatsChartPoint point = new SiteStatsChartPoint();
			point.setX(count.getRoleId());
			point.setLabel(count.getRoleId());
			point.setY(Long.valueOf(count.getVisits()));
			dataset.getPoints().add(point);
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	private SiteStatsTable visitsByRoleTable(String title, List<RoleVisitCount> counts, SiteStatsReportRequest request) {
		NumberFormat numberFormat = NumberFormat.getNumberInstance(currentLocale());
		SiteStatsTable table = new SiteStatsTable();
		table.setCaption(title);
		table.setPage(request.getPage());
		table.setPageSize(request.getPageSize());
		table.setTotalRows(counts.size());
		table.getColumns().add(column(ROLE_COLUMN, context.message("report_who_role"), "text", "start"));
		table.getColumns().add(column(StatsManager.T_TOTAL, context.message("th_visits"), "number", "end"));
		int fromIndex = Math.min((request.getPage() - 1) * request.getPageSize(), counts.size());
		int toIndex = Math.min(fromIndex + request.getPageSize(), counts.size());
		for (RoleVisitCount count : counts.subList(fromIndex, toIndex)) {
			SiteStatsTableRow row = new SiteStatsTableRow();
			row.getCells().put(ROLE_COLUMN, cell(count.getRoleId(), count.getRoleId()));
			row.getCells().put(StatsManager.T_TOTAL, cell(Long.valueOf(count.getVisits()), numberFormat.format(count.getVisits())));
			table.getRows().add(row);
		}
		return table;
	}

	private SiteStatsChart compactDailyChart(String title, String yKey, String datasetLabel, List<LocalDate> days,
			Map<LocalDate, Long> values, ZoneId zone) {
		if (!hasSparklineData(values)) {
			return null;
		}
		DateTimeFormatter labels = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(currentLocale()).withZone(zone);
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey(StatsManager.T_DATE);
		chart.setYKey(yKey);
		chart.setEmptyMessage(context.message("no_data"));
		chart.setItemLabelsVisible(false);
		chart.setCompact(true);
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey(yKey);
		dataset.setLabel(datasetLabel);
		for (LocalDate day : days) {
			Long value = values.get(day);
			SiteStatsChartPoint point = new SiteStatsChartPoint();
			point.setX(day.toString());
			point.setLabel(labels.format(day.atStartOfDay(zone)));
			point.setY(value == null ? Long.valueOf(0L) : value);
			dataset.getPoints().add(point);
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	private boolean hasSparklineData(Map<LocalDate, Long> values) {
		for (Long value : values.values()) {
			if (value != null && value.longValue() > 0L) {
				return true;
			}
		}
		return false;
	}

	private List<LocalDate> sparklineDays(ZoneId zone) {
		List<LocalDate> days = new ArrayList<LocalDate>();
		LocalDate today = LocalDate.now(zone);
		for (int i = SPARKLINE_DAYS - 1; i >= 0; i--) {
			days.add(today.minusDays(i));
		}
		return days;
	}

	private void addDayValue(Map<LocalDate, Long> values, LocalDate day, long amount) {
		if (day == null) {
			return;
		}
		Long current = values.get(day);
		values.put(day, Long.valueOf((current == null ? 0L : current.longValue()) + amount));
	}

	private LocalDate toLocalDate(Date date, ZoneId zone) {
		if (date == null) {
			return null;
		}
		return Instant.ofEpochMilli(date.getTime()).atZone(zone).toLocalDate();
	}

	private String dateFilter(SiteStatsReportRequest request) {
		return StringUtils.defaultIfBlank(request.getDate(), ReportManager.WHEN_ALL);
	}

	private SiteStatsTableColumn column(String key, String label, String type, String align) {
		SiteStatsTableColumn column = new SiteStatsTableColumn();
		column.setKey(key);
		column.setLabel(label);
		column.setType(type);
		column.setAlign(align);
		return column;
	}

	private SiteStatsTableCell cell(Object raw, String display) {
		SiteStatsTableCell cell = new SiteStatsTableCell();
		cell.setRaw(raw);
		cell.setSort(raw);
		cell.setDisplay(display);
		return cell;
	}

	private Locale currentLocale() {
		if (context.getMessages() != null && context.getMessages().getLocale() != null) {
			return context.getMessages().getLocale();
		}
		return Locale.getDefault();
	}

	@Getter
	private static class RoleVisitCount {
		private final String roleId;
		private final long visits;

		RoleVisitCount(String roleId, long visits) {
			this.roleId = roleId;
			this.visits = visits;
		}
	}
}
