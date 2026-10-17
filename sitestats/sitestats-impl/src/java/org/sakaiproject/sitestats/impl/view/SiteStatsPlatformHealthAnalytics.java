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

import java.util.ArrayList;
import java.util.List;

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

	@Setter private ServerWideReportManager serverWideReportManager;
	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetMetricSupport metricSupport;

	WidgetMetricValue loginsValue(String siteId, String userId, SiteStatsReportRequest request) {
		long total = sumLastDays(serverWideReportManager.getDailyTotalLogins(), 30, 1);
		return WidgetMetricValue.of(metricSupport.formatNumber(total));
	}

	WidgetMetricValue uniqueLoginsValue(String siteId, String userId, SiteStatsReportRequest request) {
		long total = sumLastDays(serverWideReportManager.getDailyUniqueLogins(), 30, 1);
		return WidgetMetricValue.of(metricSupport.formatNumber(total));
	}

	WidgetMetricValue sitesCreatedValue(String siteId, String userId, SiteStatsReportRequest request) {
		long total = sumPeriodField(serverWideReportManager.getSiteCreatedDeletedStats("daily"), 30, 1);
		return WidgetMetricValue.of(metricSupport.formatNumber(total));
	}

	WidgetMetricValue sitesDeletedValue(String siteId, String userId, SiteStatsReportRequest request) {
		long total = sumPeriodField(serverWideReportManager.getSiteCreatedDeletedStats("daily"), 30, 2);
		return WidgetMetricValue.of(metricSupport.formatNumber(total));
	}

	WidgetMetricValue newUsersValue(String siteId, String userId, SiteStatsReportRequest request) {
		long total = sumPeriodField(serverWideReportManager.getNewUserStats("daily"), 30, 1);
		return WidgetMetricValue.of(metricSupport.formatNumber(total));
	}

	WidgetMetricValue topToolValue(String siteId, String userId, SiteStatsReportRequest request) {
		List<ServerWideStatsRecord> tools = serverWideReportManager.getToolCount();
		if (tools == null || tools.isEmpty()) {
			return WidgetMetricValue.of("-");
		}
		ServerWideStatsRecord top = tools.get(0);
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
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(message("overview_title_platform_login_trend"));
		chart.setType("line");
		chart.setXKey("label");
		chart.setYKey("value");
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey("logins");
		dataset.setLabel(message("legend_logins"));
		dataset.setColor(CHART_COLOR_INFO);
		List<ServerWideStatsRecord> rows = serverWideReportManager.getDailyTotalLogins();
		int from = rows == null ? 0 : Math.max(0, rows.size() - 30);
		if (rows != null) {
			for (int i = from; i < rows.size(); i++) {
				ServerWideStatsRecord row = rows.get(i);
				SiteStatsChartPoint point = new SiteStatsChartPoint();
				Object label = row.get(0);
				point.setX(label == null ? "" : String.valueOf(label));
				point.setLabel(label == null ? "" : String.valueOf(label));
				Number value = numberAt(row, 1);
				point.setY(Double.valueOf(value == null ? 0d : value.doubleValue()));
				dataset.getPoints().add(point);
			}
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	SiteStatsReportView loginsByDateReport(String siteId, SiteStatsReportRequest request, String userId) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message("overview_title_platform_login_trend"));
		view.setGeneratedOn(java.time.Instant.now().toString());
		if (request.isIncludeTable()) {
			SiteStatsTable table = new SiteStatsTable();
			table.setCaption(view.getTitle());
			table.getColumns().add(column("date", "th_date", "text"));
			table.getColumns().add(column("logins", "legend_logins", "number"));
			List<ServerWideStatsRecord> rows = serverWideReportManager.getDailyTotalLogins();
			int from = rows == null ? 0 : Math.max(0, rows.size() - 30);
			if (rows != null) {
				for (int i = from; i < rows.size(); i++) {
					ServerWideStatsRecord row = rows.get(i);
					SiteStatsTableRow tableRow = new SiteStatsTableRow();
					Object label = row.get(0);
					tableRow.getCells().put("date", textCell(label == null ? "" : String.valueOf(label)));
					Number value = numberAt(row, 1);
					tableRow.getCells().put("logins", numberCell(value == null ? 0L : value.longValue()));
					table.getRows().add(tableRow);
				}
			}
			view.setTable(table);
		}
		if (request.isIncludeChart()) {
			view.setChart(loginTrendHighlight(siteId, userId, request));
		}
		return view;
	}

	SiteStatsReportView toolInstallsReport(String siteId, SiteStatsReportRequest request, String userId) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message("overview_title_platform_tool_installs"));
		view.setGeneratedOn(java.time.Instant.now().toString());
		if (request.isIncludeTable()) {
			SiteStatsTable table = new SiteStatsTable();
			table.setCaption(view.getTitle());
			table.getColumns().add(column("tool", "th_tool", "text"));
			table.getColumns().add(column("count", "th_installs", "number"));
			List<ServerWideStatsRecord> tools = serverWideReportManager.getToolCount();
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
			view.setChart(toolInstallsChart());
		}
		return view;
	}

	private SiteStatsChart toolInstallsChart() {
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
		List<ServerWideStatsRecord> tools = serverWideReportManager.getToolCount();
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

	private long sumLastDays(List<ServerWideStatsRecord> rows, int days, int valueIndex) {
		if (rows == null || rows.isEmpty()) {
			return 0L;
		}
		int from = Math.max(0, rows.size() - days);
		long total = 0L;
		for (int i = from; i < rows.size(); i++) {
			Number value = numberAt(rows.get(i), valueIndex);
			if (value != null) {
				total += value.longValue();
			}
		}
		return total;
	}

	private long sumPeriodField(List<ServerWideStatsRecord> rows, int days, int valueIndex) {
		return sumLastDays(rows, days, valueIndex);
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
}
