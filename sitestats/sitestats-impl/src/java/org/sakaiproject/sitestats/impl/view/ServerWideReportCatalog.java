/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.ServerWideStatsRecord;
import org.sakaiproject.sitestats.api.view.SiteStatsServerWideReportIds;

public class ServerWideReportCatalog {

	private static final String LABEL = "label";
	private static final String LOGINS = "logins";
	private static final String UNIQUE_LOGINS = "uniqueLogins";
	private static final String SITES_CREATED = "sitesCreated";
	private static final String SITES_DELETED = "sitesDeleted";
	private static final String NEW_USERS = "newUsers";
	private static final String FIVE_PLUS = "fivePlus";
	private static final String FOUR = "four";
	private static final String THREE = "three";
	private static final String TWO = "two";
	private static final String ONE = "one";
	private static final String AVERAGE_USERS = "averageUsers";
	private static final String LAST_7 = "last7";
	private static final String LAST_30 = "last30";
	private static final String LAST_365 = "last365";
	private static final String TOTAL = "total";

	private final Map<String, ServerWideReportSpec> specs;

	public ServerWideReportCatalog() {
		LinkedHashMap<String, ServerWideReportSpec> mutableSpecs = new LinkedHashMap<String, ServerWideReportSpec>();
		add(mutableSpecs, periodSpec(SiteStatsServerWideReportIds.MONTHLY_LOGIN, reportManager -> periodRows(
				reportManager.getMonthlyTotalLogins(), reportManager.getMonthlyUniqueLogins(),
				reportManager.getSiteCreatedDeletedStats("monthly"), reportManager.getNewUserStats("monthly"))));
		add(mutableSpecs, periodSpec(SiteStatsServerWideReportIds.WEEKLY_LOGIN, reportManager -> periodRows(
				reportManager.getWeeklyTotalLogins(), reportManager.getWeeklyUniqueLogins(),
				reportManager.getSiteCreatedDeletedStats("weekly"), reportManager.getNewUserStats("weekly"))));
		add(mutableSpecs, periodSpec(SiteStatsServerWideReportIds.DAILY_LOGIN, reportManager -> periodRows(
				reportManager.getDailyTotalLogins(), reportManager.getDailyUniqueLogins(),
				reportManager.getSiteCreatedDeletedStats("daily"), reportManager.getNewUserStats("daily"))));
		add(mutableSpecs, new ServerWideReportSpec(SiteStatsServerWideReportIds.REGULAR_USERS,
				titleKey(SiteStatsServerWideReportIds.REGULAR_USERS), "line", regularUserColumns(),
				reportManager -> regularUserRows(reportManager.getWeeklyRegularUsers())));
		add(mutableSpecs, new ServerWideReportSpec(SiteStatsServerWideReportIds.HOURLY_USAGE,
				titleKey(SiteStatsServerWideReportIds.HOURLY_USAGE), "bar", hourlyColumns(),
				reportManager -> hourlyAverageRows(reportManager.getHourlyUsagePattern())));
		add(mutableSpecs, new ServerWideReportSpec(SiteStatsServerWideReportIds.TOP_ACTIVITIES,
				titleKey(SiteStatsServerWideReportIds.TOP_ACTIVITIES), "bar", topActivityColumns(),
				reportManager -> topActivityRows(reportManager.getTop20Activities())));
		add(mutableSpecs, new ServerWideReportSpec(SiteStatsServerWideReportIds.TOOL,
				titleKey(SiteStatsServerWideReportIds.TOOL), "pie", totalColumns("th_tool"),
				reportManager -> totalRows(reportManager.getToolCount())));
		specs = Collections.unmodifiableMap(mutableSpecs);
	}

	public ServerWideReportSpec get(String reportType) {
		ServerWideReportSpec spec = specs.get(reportType);
		if (spec == null) {
			throw new IllegalArgumentException("Unknown server-wide SiteStats report: " + reportType);
		}
		return spec;
	}

	public List<ServerWideReportSpec> getReports() {
		return new ArrayList<ServerWideReportSpec>(specs.values());
	}

	private void add(Map<String, ServerWideReportSpec> specs, ServerWideReportSpec spec) {
		specs.put(spec.getId(), spec);
	}

	private static ServerWideReportSpec periodSpec(String reportType, ServerWideReportSpec.ServerWideReportDataProvider dataProvider) {
		return new ServerWideReportSpec(reportType, titleKey(reportType), "line", periodColumns(), dataProvider);
	}

	private static String titleKey(String reportType) {
		return "title_" + SiteStatsServerWideReportIds.keySuffix(reportType);
	}

	private static List<ServerWideReportColumn> periodColumns() {
		List<ServerWideReportColumn> columns = new ArrayList<ServerWideReportColumn>();
		columns.add(ServerWideReportColumn.label(LABEL, "th_date"));
		columns.add(ServerWideReportColumn.number(LOGINS, "legend_logins"));
		columns.add(ServerWideReportColumn.number(UNIQUE_LOGINS, "legend_unique_logins"));
		columns.add(ServerWideReportColumn.number(SITES_CREATED, "legend_site_created"));
		columns.add(ServerWideReportColumn.number(SITES_DELETED, "legend_site_deleted"));
		columns.add(ServerWideReportColumn.number(NEW_USERS, "legend_new_user"));
		return Collections.unmodifiableList(columns);
	}

	private static List<ServerWideReportColumn> regularUserColumns() {
		List<ServerWideReportColumn> columns = new ArrayList<ServerWideReportColumn>();
		columns.add(ServerWideReportColumn.label(LABEL, "th_date"));
		columns.add(ServerWideReportColumn.literalNumber(FIVE_PLUS, "5+"));
		columns.add(ServerWideReportColumn.literalNumber(FOUR, "4"));
		columns.add(ServerWideReportColumn.literalNumber(THREE, "3"));
		columns.add(ServerWideReportColumn.literalNumber(TWO, "2"));
		columns.add(ServerWideReportColumn.literalNumber(ONE, "1"));
		return Collections.unmodifiableList(columns);
	}

	private static List<ServerWideReportColumn> hourlyColumns() {
		List<ServerWideReportColumn> columns = new ArrayList<ServerWideReportColumn>();
		columns.add(ServerWideReportColumn.label(LABEL, "th_date"));
		columns.add(ServerWideReportColumn.number(AVERAGE_USERS, "legend_unique_logins"));
		return Collections.unmodifiableList(columns);
	}

	private static List<ServerWideReportColumn> topActivityColumns() {
		List<ServerWideReportColumn> columns = new ArrayList<ServerWideReportColumn>();
		columns.add(ServerWideReportColumn.label(LABEL, "th_event"));
		columns.add(ServerWideReportColumn.literalNumber(LAST_7, "7"));
		columns.add(ServerWideReportColumn.literalNumber(LAST_30, "30"));
		columns.add(ServerWideReportColumn.literalNumber(LAST_365, "365"));
		return Collections.unmodifiableList(columns);
	}

	private static List<ServerWideReportColumn> totalColumns(String labelKey) {
		List<ServerWideReportColumn> columns = new ArrayList<ServerWideReportColumn>();
		columns.add(ServerWideReportColumn.label(LABEL, labelKey));
		columns.add(ServerWideReportColumn.number(TOTAL, "th_total"));
		return Collections.unmodifiableList(columns);
	}

	private static List<ServerWideReportRow> periodRows(List<ServerWideStatsRecord> totals, List<ServerWideStatsRecord> uniques,
			List<ServerWideStatsRecord> sites, List<ServerWideStatsRecord> users) {
		LinkedHashMap<Object, Map<String, Object>> combined = new LinkedHashMap<Object, Map<String, Object>>();
		mergePeriodValue(combined, totals, LOGINS, 1);
		mergePeriodValue(combined, uniques, UNIQUE_LOGINS, 1);
		mergePeriodValue(combined, sites, SITES_CREATED, 1);
		mergePeriodValue(combined, sites, SITES_DELETED, 2);
		mergePeriodValue(combined, users, NEW_USERS, 1);

		List<ServerWideReportRow> rows = new ArrayList<ServerWideReportRow>();
		for (Map.Entry<Object, Map<String, Object>> entry : combined.entrySet()) {
			Map<String, Object> values = entry.getValue();
			rows.add(ServerWideReportRow.of(entry.getKey(),
					LOGINS, numericValue(values, LOGINS),
					UNIQUE_LOGINS, numericValue(values, UNIQUE_LOGINS),
					SITES_CREATED, numericValue(values, SITES_CREATED),
					SITES_DELETED, numericValue(values, SITES_DELETED),
					NEW_USERS, numericValue(values, NEW_USERS)));
		}
		Collections.sort(rows, Comparator.comparing(row -> row.getValues().get(LABEL), ServerWideReportCatalog::compareLabels));
		return rows;
	}

	private static void mergePeriodValue(LinkedHashMap<Object, Map<String, Object>> combined, List<ServerWideStatsRecord> records,
			String key, int sourceIndex) {
		if (records == null) {
			return;
		}
		for (ServerWideStatsRecord source : records) {
			Object period = source.get(0);
			Map<String, Object> values = combined.computeIfAbsent(period, ignored -> new LinkedHashMap<String, Object>());
			values.put(key, source.get(sourceIndex));
		}
	}

	private static List<ServerWideReportRow> regularUserRows(List<ServerWideStatsRecord> records) {
		List<ServerWideReportRow> rows = new ArrayList<ServerWideReportRow>();
		if (records == null) {
			return rows;
		}
		for (ServerWideStatsRecord record : records) {
			rows.add(ServerWideReportRow.of(record.get(0),
					FIVE_PLUS, record.get(1),
					FOUR, record.get(2),
					THREE, record.get(3),
					TWO, record.get(4),
					ONE, record.get(5)));
		}
		Collections.sort(rows, Comparator.comparing(row -> row.getValues().get(LABEL), ServerWideReportCatalog::compareLabels));
		return rows;
	}

	private static List<ServerWideReportRow> hourlyAverageRows(List<ServerWideStatsRecord> records) {
		long[] totals = new long[24];
		Set<Object> days = new HashSet<Object>();
		if (records != null) {
			for (ServerWideStatsRecord record : records) {
				int hour = ((Number) record.get(1)).intValue();
				if (hour >= 0 && hour < 24) {
					days.add(record.get(0));
					totals[hour] += ((Number) record.get(2)).longValue();
				}
			}
		}

		List<ServerWideReportRow> rows = new ArrayList<ServerWideReportRow>();
		int dayCount = days.size();
		for (int hour = 0; hour < 24; hour++) {
			long average = dayCount == 0 ? 0L : Math.round((double) totals[hour] / dayCount);
			rows.add(ServerWideReportRow.of(Integer.valueOf(hour), AVERAGE_USERS, Long.valueOf(average)));
		}
		return rows;
	}

	private static Object numericValue(Map<String, Object> values, String key) {
		Object value = values.get(key);
		return value instanceof Number ? value : Long.valueOf(0L);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compareLabels(Object left, Object right) {
		if (left == right) {
			return 0;
		}
		if (left == null) {
			return -1;
		}
		if (right == null) {
			return 1;
		}
		if (left instanceof Comparable && left.getClass().isInstance(right)) {
			return ((Comparable) left).compareTo(right);
		}
		return String.valueOf(left).compareTo(String.valueOf(right));
	}

	private static List<ServerWideReportRow> topActivityRows(List<ServerWideStatsRecord> records) {
		List<ServerWideReportRow> rows = new ArrayList<ServerWideReportRow>();
		if (records == null) {
			return rows;
		}
		for (ServerWideStatsRecord record : records) {
			rows.add(ServerWideReportRow.of(record.get(0),
					LAST_7, record.get(1),
					LAST_30, record.get(2),
					LAST_365, record.get(3)));
		}
		return rows;
	}

	private static List<ServerWideReportRow> totalRows(List<ServerWideStatsRecord> records) {
		List<ServerWideReportRow> rows = new ArrayList<ServerWideReportRow>();
		if (records == null) {
			return rows;
		}
		for (ServerWideStatsRecord record : records) {
			rows.add(ServerWideReportRow.of(record.get(0), TOTAL, record.get(1)));
		}
		return rows;
	}
}
