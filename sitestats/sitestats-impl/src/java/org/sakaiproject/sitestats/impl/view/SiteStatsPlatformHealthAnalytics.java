/*
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
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_INFO;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_LOGINS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_NEW_USERS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_SITES_CREATED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_SITES_DELETED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_TOP_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_UNIQUE_LOGINS;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.ServerWideStatsRecord;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsChartDataset;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;

public class SiteStatsPlatformHealthAnalytics {

	private static final int LAST_DAYS = 30;

	@Setter private ServerWideReportManager serverWideReportManager;
	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetMetricSupport metricSupport;

	Map<String, WidgetMetricValue> metricValues(SiteStatsReportRequest request) {
		PlatformSnapshot snapshot = snapshot();
		Map<String, WidgetMetricValue> values = new LinkedHashMap<String, WidgetMetricValue>();
		values.put(METRIC_PLATFORM_LOGINS, loginsValue(snapshot));
		values.put(METRIC_PLATFORM_UNIQUE_LOGINS, uniqueLoginsValue(snapshot));
		values.put(METRIC_PLATFORM_SITES_CREATED, sitesCreatedValue(snapshot));
		values.put(METRIC_PLATFORM_SITES_DELETED, sitesDeletedValue(snapshot));
		values.put(METRIC_PLATFORM_NEW_USERS, newUsersValue(snapshot));
		values.put(METRIC_PLATFORM_TOP_TOOL, topToolValue(snapshot));
		return values;
	}

	WidgetMetricValue loginsValue(String siteId, String userId, SiteStatsReportRequest request) {
		return loginsValue(withDailyLogins(nullToEmpty(serverWideReportManager.getDailyTotalLogins())));
	}

	WidgetMetricValue uniqueLoginsValue(String siteId, String userId, SiteStatsReportRequest request) {
		PlatformSnapshot snapshot = new PlatformSnapshot();
		snapshot.uniqueLogins = serverWideReportManager.getUniqueLoginsInLastDays(LAST_DAYS);
		return uniqueLoginsValue(snapshot);
	}

	WidgetMetricValue sitesCreatedValue(String siteId, String userId, SiteStatsReportRequest request) {
		return sitesCreatedValue(withSiteCreatedDeleted(nullToEmpty(serverWideReportManager.getSiteCreatedDeletedStats("daily"))));
	}

	WidgetMetricValue sitesDeletedValue(String siteId, String userId, SiteStatsReportRequest request) {
		return sitesDeletedValue(withSiteCreatedDeleted(nullToEmpty(serverWideReportManager.getSiteCreatedDeletedStats("daily"))));
	}

	WidgetMetricValue newUsersValue(String siteId, String userId, SiteStatsReportRequest request) {
		return newUsersValue(withNewUsers(nullToEmpty(serverWideReportManager.getNewUserStats("daily"))));
	}

	WidgetMetricValue topToolValue(String siteId, String userId, SiteStatsReportRequest request) {
		return topToolValue(withTools(nullToEmpty(serverWideReportManager.getToolCount())));
	}

	private WidgetMetricValue loginsValue(PlatformSnapshot snapshot) {
		return WidgetMetricValue.of(metricSupport.formatNumber(sumLastDays(snapshot.dailyLogins, LAST_DAYS, 1)));
	}

	private WidgetMetricValue uniqueLoginsValue(PlatformSnapshot snapshot) {
		return WidgetMetricValue.of(metricSupport.formatNumber(snapshot.uniqueLogins));
	}

	private WidgetMetricValue sitesCreatedValue(PlatformSnapshot snapshot) {
		return WidgetMetricValue.of(metricSupport.formatNumber(sumLastDays(snapshot.siteCreatedDeleted, LAST_DAYS, 1)));
	}

	private WidgetMetricValue sitesDeletedValue(PlatformSnapshot snapshot) {
		return WidgetMetricValue.of(metricSupport.formatNumber(sumLastDays(snapshot.siteCreatedDeleted, LAST_DAYS, 2)));
	}

	private WidgetMetricValue newUsersValue(PlatformSnapshot snapshot) {
		return WidgetMetricValue.of(metricSupport.formatNumber(sumLastDays(snapshot.newUsers, LAST_DAYS, 1)));
	}

	private WidgetMetricValue topToolValue(PlatformSnapshot snapshot) {
		if (snapshot.tools == null || snapshot.tools.isEmpty()) {
			return WidgetMetricValue.of("-");
		}
		ServerWideStatsRecord top = snapshot.tools.get(0);
		String toolId = stringAt(top, 0);
		String label = toolLabel(toolId);
		Number count = numberAt(top, 1);
		return WidgetMetricValue.withDetail(label, count == null ? null : metricSupport.formatNumber(count.longValue()));
	}

	private String toolLabel(String toolId) {
		if (context == null || context.getEventRegistryService() == null) {
			return toolId;
		}
		try {
			String name = context.getEventRegistryService().getToolName(toolId);
			return org.apache.commons.lang3.StringUtils.defaultIfBlank(name, toolId);
		} catch (RuntimeException e) {
			return toolId;
		}
	}

	SiteStatsChart loginTrendHighlight(String siteId, String userId, SiteStatsReportRequest request) {
		return loginTrendChart(nullToEmpty(serverWideReportManager.getDailyTotalLogins()));
	}

	SiteStatsReportView loginsByDateReport(String siteId, SiteStatsReportRequest request, String userId) {
		List<ServerWideStatsRecord> dailyLogins = nullToEmpty(serverWideReportManager.getDailyTotalLogins());
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message("overview_title_platform_login_trend"));
		view.setGeneratedOn(Instant.now().toString());
		if (request.isIncludeTable()) {
			SiteStatsTable table = new SiteStatsTable();
			table.setCaption(view.getTitle());
			table.getColumns().add(column("date", "th_date", "text"));
			table.getColumns().add(column("logins", "legend_logins", "number"));
			for (ServerWideStatsRecord row : rowsInLastDays(dailyLogins, LAST_DAYS)) {
				SiteStatsTableRow tableRow = new SiteStatsTableRow();
				LocalDate date = recordDate(row);
				tableRow.getCells().put("date", textCell(date == null ? stringAt(row, 0) : date.toString()));
				Number value = numberAt(row, 1);
				tableRow.getCells().put("logins", numberCell(value == null ? 0L : value.longValue()));
				table.getRows().add(tableRow);
			}
			view.setTable(table);
		}
		if (request.isIncludeChart()) {
			view.setChart(loginTrendChart(dailyLogins));
		}
		return view;
	}

	SiteStatsReportView toolInstallsReport(String siteId, SiteStatsReportRequest request, String userId) {
		List<ServerWideStatsRecord> tools = nullToEmpty(serverWideReportManager.getToolCount());
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message("overview_title_platform_tool_installs"));
		view.setGeneratedOn(Instant.now().toString());
		if (request.isIncludeTable()) {
			SiteStatsTable table = new SiteStatsTable();
			table.setCaption(view.getTitle());
			table.getColumns().add(column("tool", "th_tool", "text"));
			table.getColumns().add(column("count", "th_installs", "number"));
			if (tools != null) {
				int limit = Math.min(20, tools.size());
				for (int i = 0; i < limit; i++) {
					ServerWideStatsRecord row = tools.get(i);
					SiteStatsTableRow tableRow = new SiteStatsTableRow();
					String toolId = stringAt(row, 0);
					tableRow.getCells().put("tool", textCell(toolLabel(toolId)));
					Number count = numberAt(row, 1);
					tableRow.getCells().put("count", numberCell(count == null ? 0L : count.longValue()));
					table.getRows().add(tableRow);
				}
			}
			view.setTable(table);
		}
		if (request.isIncludeChart()) {
			view.setChart(toolInstallsChart(tools));
		}
		return view;
	}

	private SiteStatsChart loginTrendChart(List<ServerWideStatsRecord> dailyLogins) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(message("overview_title_platform_login_trend"));
		chart.setType("line");
		chart.setXKey("label");
		chart.setYKey("value");
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey("logins");
		dataset.setLabel(message("legend_logins"));
		dataset.setColor(CHART_COLOR_INFO);
		for (ServerWideStatsRecord row : rowsInLastDays(dailyLogins, LAST_DAYS)) {
			SiteStatsChartPoint point = new SiteStatsChartPoint();
			LocalDate date = recordDate(row);
			String label = date == null ? stringAt(row, 0) : date.toString();
			point.setX(label);
			point.setLabel(label);
			Number value = numberAt(row, 1);
			point.setY(Double.valueOf(value == null ? 0d : value.doubleValue()));
			dataset.getPoints().add(point);
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	private SiteStatsChart toolInstallsChart(List<ServerWideStatsRecord> tools) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(message("overview_title_platform_tool_installs"));
		chart.setType("bar");
		chart.setXKey("tool");
		chart.setYKey("installs");
		chart.setHorizontal(true);
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey("installs");
		dataset.setLabel(message("th_installs"));
		dataset.setColor(CHART_COLOR_INFO);
		int limit = tools == null ? 0 : Math.min(15, tools.size());
		for (int i = 0; i < limit; i++) {
			ServerWideStatsRecord row = tools.get(i);
			SiteStatsChartPoint point = new SiteStatsChartPoint();
			String label = toolLabel(stringAt(row, 0));
			point.setX(label);
			point.setLabel(label);
			Number count = numberAt(row, 1);
			point.setY(Double.valueOf(count == null ? 0d : count.doubleValue()));
			dataset.getPoints().add(point);
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	private PlatformSnapshot snapshot() {
		PlatformSnapshot snapshot = new PlatformSnapshot();
		snapshot.dailyLogins = nullToEmpty(serverWideReportManager.getDailyTotalLogins());
		snapshot.uniqueLogins = serverWideReportManager.getUniqueLoginsInLastDays(LAST_DAYS);
		snapshot.siteCreatedDeleted = nullToEmpty(serverWideReportManager.getSiteCreatedDeletedStats("daily"));
		snapshot.newUsers = nullToEmpty(serverWideReportManager.getNewUserStats("daily"));
		snapshot.tools = nullToEmpty(serverWideReportManager.getToolCount());
		return snapshot;
	}

	private static PlatformSnapshot withDailyLogins(List<ServerWideStatsRecord> dailyLogins) {
		PlatformSnapshot snapshot = new PlatformSnapshot();
		snapshot.dailyLogins = dailyLogins;
		return snapshot;
	}

	private static PlatformSnapshot withSiteCreatedDeleted(List<ServerWideStatsRecord> siteCreatedDeleted) {
		PlatformSnapshot snapshot = new PlatformSnapshot();
		snapshot.siteCreatedDeleted = siteCreatedDeleted;
		return snapshot;
	}

	private static PlatformSnapshot withNewUsers(List<ServerWideStatsRecord> newUsers) {
		PlatformSnapshot snapshot = new PlatformSnapshot();
		snapshot.newUsers = newUsers;
		return snapshot;
	}

	private static PlatformSnapshot withTools(List<ServerWideStatsRecord> tools) {
		PlatformSnapshot snapshot = new PlatformSnapshot();
		snapshot.tools = tools;
		return snapshot;
	}

	private static List<ServerWideStatsRecord> nullToEmpty(List<ServerWideStatsRecord> rows) {
		return rows == null ? Collections.emptyList() : rows;
	}

	private long sumLastDays(List<ServerWideStatsRecord> rows, int days, int valueIndex) {
		long total = 0L;
		for (ServerWideStatsRecord row : rowsInLastDays(rows, days)) {
			Number value = numberAt(row, valueIndex);
			if (value != null) {
				total += value.longValue();
			}
		}
		return total;
	}

	private List<ServerWideStatsRecord> rowsInLastDays(List<ServerWideStatsRecord> rows, int days) {
		if (rows == null || rows.isEmpty() || days <= 0) {
			return Collections.emptyList();
		}
		LocalDate end = LocalDate.now();
		LocalDate start = end.minusDays(days - 1L);
		List<ServerWideStatsRecord> filtered = new ArrayList<ServerWideStatsRecord>();
		for (ServerWideStatsRecord row : rows) {
			LocalDate date = recordDate(row);
			if (date != null && !date.isBefore(start) && !date.isAfter(end)) {
				filtered.add(row);
			}
		}
		filtered.sort(Comparator.comparing(SiteStatsPlatformHealthAnalytics::recordDate,
				Comparator.nullsLast(Comparator.naturalOrder())));
		return filtered;
	}

	private static LocalDate recordDate(ServerWideStatsRecord row) {
		if (row == null) {
			return null;
		}
		Object value = row.get(0);
		if (value instanceof LocalDate) {
			return (LocalDate) value;
		}
		if (value instanceof java.sql.Date) {
			return ((java.sql.Date) value).toLocalDate();
		}
		if (value instanceof Date) {
			return Instant.ofEpochMilli(((Date) value).getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
		}
		return null;
	}

	private static String stringAt(ServerWideStatsRecord row, int index) {
		Object value = row.get(index);
		return value == null ? "" : String.valueOf(value);
	}

	private static Number numberAt(ServerWideStatsRecord row, int index) {
		Object value = row.get(index);
		return value instanceof Number ? (Number) value : null;
	}

	private SiteStatsTableColumn column(String key, String labelKey, String type) {
		SiteStatsTableColumn column = new SiteStatsTableColumn();
		column.setKey(key);
		column.setLabel(message(labelKey));
		column.setType(type);
		return column;
	}

	private SiteStatsTableCell textCell(String value) {
		SiteStatsTableCell cell = new SiteStatsTableCell();
		cell.setDisplay(value);
		cell.setRaw(value);
		cell.setSort(value);
		return cell;
	}

	private SiteStatsTableCell numberCell(long value) {
		SiteStatsTableCell cell = new SiteStatsTableCell();
		String display = metricSupport.formatNumber(value);
		cell.setDisplay(display);
		cell.setRaw(Long.valueOf(value));
		cell.setSort(Long.valueOf(value));
		return cell;
	}

	private String message(String key) {
		return context == null ? key : context.message(key);
	}

	private static final class PlatformSnapshot {
		List<ServerWideStatsRecord> dailyLogins = Collections.emptyList();
		long uniqueLogins;
		List<ServerWideStatsRecord> siteCreatedDeleted = Collections.emptyList();
		List<ServerWideStatsRecord> newUsers = Collections.emptyList();
		List<ServerWideStatsRecord> tools = Collections.emptyList();
	}
}
