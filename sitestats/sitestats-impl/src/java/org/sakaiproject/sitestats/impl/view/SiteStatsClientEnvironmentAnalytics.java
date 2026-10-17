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
import static org.sakaiproject.sitestats.impl.view.SiteStatsUserAgentClassifier.DEVICE_DESKTOP;
import static org.sakaiproject.sitestats.impl.view.SiteStatsUserAgentClassifier.DEVICE_MOBILE;
import static org.sakaiproject.sitestats.impl.view.SiteStatsUserAgentClassifier.DEVICE_TABLET;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsChartDataset;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;

@Slf4j
public class SiteStatsClientEnvironmentAnalytics {

	public static final String PROP_ENABLED = "sitestats.institutional.clientEnv.enabled";

	@Setter private SqlService sqlService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;
	@Setter private WidgetMetricSupport metricSupport;

	boolean isEnabled() {
		return serverConfigurationService == null
				|| serverConfigurationService.getBoolean(PROP_ENABLED, true);
	}

	WidgetMetricValue desktopValue(String siteId, String userId, SiteStatsReportRequest request) {
		ClientRollup rollup = rollup(request);
		return shareMetric(rollup.devices.getOrDefault(DEVICE_DESKTOP, 0L), rollup.total);
	}

	WidgetMetricValue mobileValue(String siteId, String userId, SiteStatsReportRequest request) {
		ClientRollup rollup = rollup(request);
		return shareMetric(rollup.devices.getOrDefault(DEVICE_MOBILE, 0L), rollup.total);
	}

	WidgetMetricValue tabletValue(String siteId, String userId, SiteStatsReportRequest request) {
		ClientRollup rollup = rollup(request);
		return shareMetric(rollup.devices.getOrDefault(DEVICE_TABLET, 0L), rollup.total);
	}

	WidgetMetricValue topBrowserValue(String siteId, String userId, SiteStatsReportRequest request) {
		ClientRollup rollup = rollup(request);
		Map.Entry<String, Long> top = topEntry(rollup.browsers);
		if (top == null) {
			return WidgetMetricValue.of("-");
		}
		return WidgetMetricValue.withDetail(top.getKey(),
				metricSupport.formatNumber(top.getValue().longValue()));
	}

	WidgetMetricValue topOsValue(String siteId, String userId, SiteStatsReportRequest request) {
		ClientRollup rollup = rollup(request);
		Map.Entry<String, Long> top = topEntry(rollup.oses);
		if (top == null) {
			return WidgetMetricValue.of("-");
		}
		return WidgetMetricValue.withDetail(top.getKey(),
				metricSupport.formatNumber(top.getValue().longValue()));
	}

	SiteStatsChart deviceShareHighlight(String siteId, String userId, SiteStatsReportRequest request) {
		ClientRollup rollup = rollup(request);
		if (rollup.total <= 0L) {
			return null;
		}
		String browserCategory = message("overview_label_client_browser");
		String osCategory = message("overview_label_client_os");
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(message("overview_title_client_device_share"));
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey("category");
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		chart.setItemLabelsVisible(false);
		chart.setCompact(true);
		chart.setStacked(true);
		chart.setHorizontal(true);
		for (Map.Entry<String, Long> entry : sorted(rollup.browsers)) {
			chart.getDatasets().add(shareAcrossCategories("browser-" + entry.getKey(), entry.getKey(),
					browserCategory, entry.getValue().longValue(), osCategory, 0L));
		}
		for (Map.Entry<String, Long> entry : sorted(rollup.oses)) {
			chart.getDatasets().add(shareAcrossCategories("os-" + entry.getKey(), entry.getKey(),
					browserCategory, 0L, osCategory, entry.getValue().longValue()));
		}
		return chart;
	}

	SiteStatsReportView byDeviceReport(String siteId, SiteStatsReportRequest request, String userId) {
		return bucketReport(siteId, request, rollup(request).devices, "overview_title_client_by_device", "device");
	}

	SiteStatsReportView byBrowserReport(String siteId, SiteStatsReportRequest request, String userId) {
		return bucketReport(siteId, request, rollup(request).browsers, "overview_title_client_by_browser", "browser");
	}

	private SiteStatsReportView bucketReport(String siteId, SiteStatsReportRequest request, Map<String, Long> buckets,
			String titleKey, String key) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message(titleKey));
		view.setGeneratedOn(Instant.now().toString());
		if (request.isIncludeTable()) {
			SiteStatsTable table = new SiteStatsTable();
			table.setCaption(view.getTitle());
			table.getColumns().add(column(key, titleKey, "text"));
			table.getColumns().add(column("count", "th_total", "number"));
			for (Map.Entry<String, Long> entry : sorted(buckets)) {
				SiteStatsTableRow row = new SiteStatsTableRow();
				row.getCells().put(key, textCell(entry.getKey()));
				row.getCells().put("count", numberCell(entry.getValue().longValue()));
				table.getRows().add(row);
			}
			view.setTable(table);
		}
		if (request.isIncludeChart()) {
			view.setChart(barChart(buckets, view.getTitle(), key));
		}
		return view;
	}

	private SiteStatsChart barChart(Map<String, Long> buckets, String title, String xKey) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey(xKey);
		chart.setYKey("count");
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey("count");
		dataset.setLabel(title);
		dataset.setColor(CHART_COLOR_INFO);
		for (Map.Entry<String, Long> entry : sorted(buckets)) {
			dataset.getPoints().add(point(entry.getKey(), entry.getValue()));
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	private SiteStatsChartDataset shareAcrossCategories(String key, String label, String firstCategory,
			long firstCount, String secondCategory, long secondCount) {
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey(key);
		dataset.setLabel(label);
		dataset.getPoints().add(point(firstCategory, Long.valueOf(firstCount)));
		dataset.getPoints().add(point(secondCategory, Long.valueOf(secondCount)));
		return dataset;
	}

	private SiteStatsChartPoint point(String label, Number y) {
		SiteStatsChartPoint point = new SiteStatsChartPoint();
		point.setX(label);
		point.setLabel(label);
		point.setY(y);
		return point;
	}

	private ClientRollup rollup(SiteStatsReportRequest request) {
		ClientRollup rollup = new ClientRollup();
		if (!isEnabled()) {
			return rollup;
		}
		Date from = filterCatalog == null ? null : filterCatalog.periodFrom(request);
		Date to = filterCatalog == null ? null : filterCatalog.periodTo(request);
		StringBuilder sql = new StringBuilder(
				"select SESSION_USER_AGENT from SAKAI_SESSION where SESSION_USER_AGENT is not null");
		List<Object> params = new ArrayList<Object>();
		if (from != null) {
			sql.append(" and SESSION_START >= ?");
			params.add(from);
		}
		if (to != null) {
			sql.append(" and SESSION_START <= ?");
			params.add(to);
		}
		// Cap rows scanned for safety on large instances.
		sql.append(" order by SESSION_START desc");
		final int maxRows = 50000;
		try {
			List<?> rows = sqlService.dbRead(sql.toString(), params.toArray(), new SqlReader() {
				private int counted;

				@Override
				public Object readSqlResultRecord(ResultSet result) {
					if (counted >= maxRows) {
						return null;
					}
					counted++;
					try {
						return result.getString(1);
					} catch (SQLException e) {
						log.warn("Unable to read SESSION_USER_AGENT", e);
						return null;
					}
				}
			});
			if (rows != null) {
				for (Object row : rows) {
					if (row == null) {
						continue;
					}
					SiteStatsUserAgentClassifier.Classification c =
							SiteStatsUserAgentClassifier.classify(String.valueOf(row));
					increment(rollup.devices, c.getDevice());
					increment(rollup.oses, c.getOs());
					increment(rollup.browsers, c.getBrowser());
					rollup.total++;
				}
			}
		} catch (RuntimeException e) {
			log.warn("Unable to aggregate client environment sessions", e);
		}
		return rollup;
	}

	private static void increment(Map<String, Long> map, String key) {
		map.merge(key, 1L, Long::sum);
	}

	private WidgetMetricValue shareMetric(long part, long whole) {
		int pct = whole <= 0L ? 0 : (int) Math.round(100.0d * part / whole);
		return WidgetMetricValue.withPercentage(metricSupport.formatNumber(part), pct);
	}

	private Map.Entry<String, Long> topEntry(Map<String, Long> map) {
		return sorted(map).stream().findFirst().orElse(null);
	}

	private List<Map.Entry<String, Long>> sorted(Map<String, Long> map) {
		List<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(map.entrySet());
		entries.sort(Comparator.<Map.Entry<String, Long>>comparingLong(e -> e.getValue().longValue()).reversed()
				.thenComparing(Map.Entry::getKey));
		return entries;
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
		cell.setDisplay(metricSupport.formatNumber(value));
		cell.setRaw(Long.valueOf(value));
		cell.setSort(Long.valueOf(value));
		return cell;
	}

	private String message(String key) {
		return context == null ? key : context.message(key);
	}

	private static final class ClientRollup {
		long total;
		Map<String, Long> devices = new HashMap<String, Long>();
		Map<String, Long> oses = new HashMap<String, Long>();
		Map<String, Long> browsers = new LinkedHashMap<String, Long>();
	}
}
