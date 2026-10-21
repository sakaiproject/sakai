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
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_DESKTOP;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_MOBILE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_TABLET;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_TOP_BROWSER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_TOP_OS;
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
import java.util.concurrent.ConcurrentHashMap;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsChartDataset;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsReportInfoItem;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;

@Slf4j
public class SiteStatsClientEnvironmentAnalytics {

	public static final String PROP_ENABLED = "sitestats.institutional.clientEnv.enabled";

	private static final int MAX_SESSION_ROWS = 50000;
	private static final long ROLLUP_TTL_MS = 5L * 60L * 1000L;

	@Setter private SqlService sqlService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;
	@Setter private WidgetMetricSupport metricSupport;

	private final ConcurrentHashMap<String, CachedClientRollup> rollupCache = new ConcurrentHashMap<String, CachedClientRollup>();
	private final ConcurrentHashMap<String, Object> loadLocks = new ConcurrentHashMap<String, Object>();

	boolean isEnabled() {
		return serverConfigurationService == null
				|| serverConfigurationService.getBoolean(PROP_ENABLED, true);
	}

	Map<String, WidgetMetricValue> metricValues(SiteStatsReportRequest request) {
		ClientRollup rollup = rollup(request);
		Map<String, WidgetMetricValue> values = new LinkedHashMap<String, WidgetMetricValue>();
		values.put(METRIC_CLIENT_DESKTOP, shareMetric(rollup, DEVICE_DESKTOP));
		values.put(METRIC_CLIENT_MOBILE, shareMetric(rollup, DEVICE_MOBILE));
		values.put(METRIC_CLIENT_TABLET, shareMetric(rollup, DEVICE_TABLET));
		values.put(METRIC_CLIENT_TOP_BROWSER, topBrowserValue(rollup));
		values.put(METRIC_CLIENT_TOP_OS, topOsValue(rollup));
		return values;
	}

	WidgetMetricValue desktopValue(String siteId, String userId, SiteStatsReportRequest request) {
		return shareMetric(rollup(request), DEVICE_DESKTOP);
	}

	WidgetMetricValue mobileValue(String siteId, String userId, SiteStatsReportRequest request) {
		return shareMetric(rollup(request), DEVICE_MOBILE);
	}

	WidgetMetricValue tabletValue(String siteId, String userId, SiteStatsReportRequest request) {
		return shareMetric(rollup(request), DEVICE_TABLET);
	}

	WidgetMetricValue topBrowserValue(String siteId, String userId, SiteStatsReportRequest request) {
		return topBrowserValue(rollup(request));
	}

	WidgetMetricValue topOsValue(String siteId, String userId, SiteStatsReportRequest request) {
		return topOsValue(rollup(request));
	}

	SiteStatsChart deviceShareHighlight(String siteId, String userId, SiteStatsReportRequest request) {
		ClientRollup rollup = rollup(request);
		if (rollup.failed || rollup.total <= 0L) {
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
		return bucketReport(siteId, request, rollup(request), "overview_title_client_by_device", "device", true);
	}

	SiteStatsReportView byBrowserReport(String siteId, SiteStatsReportRequest request, String userId) {
		return bucketReport(siteId, request, rollup(request), "overview_title_client_by_browser", "browser", false);
	}

	private SiteStatsReportView bucketReport(String siteId, SiteStatsReportRequest request, ClientRollup rollup,
			String titleKey, String key, boolean devices) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message(titleKey));
		view.setGeneratedOn(Instant.now().toString());
		if (rollup.failed) {
			SiteStatsReportInfoItem item = new SiteStatsReportInfoItem();
			item.setLabel(message("overview_metric_unavailable"));
			item.setValue(message("overview_metric_unavailable"));
			view.getSummary().add(item);
			return view;
		}
		Map<String, Long> buckets = devices ? rollup.devices : rollup.browsers;
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
		if (!isEnabled()) {
			return new ClientRollup();
		}
		Date from = filterCatalog == null ? null : filterCatalog.periodFrom(request);
		Date to = filterCatalog == null ? null : filterCatalog.periodTo(request);
		String cacheKey = periodCacheKey(from, to);
		ClientRollup cached = cachedRollup(cacheKey);
		if (cached != null) {
			return cached;
		}
		Object lock = loadLocks.computeIfAbsent(cacheKey, key -> new Object());
		synchronized (lock) {
			try {
				cached = cachedRollup(cacheKey);
				if (cached != null) {
					return cached;
				}
				ClientRollup loaded = loadRollup(from, to);
				if (!loaded.failed) {
					rollupCache.put(cacheKey, new CachedClientRollup(loaded));
				}
				return loaded;
			} finally {
				loadLocks.remove(cacheKey, lock);
			}
		}
	}

	private ClientRollup cachedRollup(String cacheKey) {
		CachedClientRollup cached = rollupCache.get(cacheKey);
		if (cached == null) {
			return null;
		}
		if (cached.expired()) {
			rollupCache.remove(cacheKey, cached);
			return null;
		}
		return cached.value;
	}

	private static String periodCacheKey(Date from, Date to) {
		return (from == null ? "" : Long.toString(from.getTime())) + "|"
				+ (to == null ? "" : Long.toString(to.getTime()));
	}

	private ClientRollup loadRollup(Date from, Date to) {
		ClientRollup rollup = new ClientRollup();
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
		sql.append(" order by SESSION_START desc");
		appendVendorRowLimit(sql, params, MAX_SESSION_ROWS);
		try {
			List<?> rows = sqlService.dbRead(sql.toString(), params.toArray(), new SqlReader() {
				private int counted;

				@Override
				public Object readSqlResultRecord(ResultSet result) throws SqlReaderFinishedException {
					if (counted >= MAX_SESSION_ROWS) {
						throw new SqlReaderFinishedException();
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
			rollup.failed = true;
		}
		return rollup;
	}

	private void appendVendorRowLimit(StringBuilder sql, List<Object> params, int maxRows) {
		String vendor = sqlService == null ? null : sqlService.getVendor();
		if ("oracle".equalsIgnoreCase(vendor)) {
			sql.append(" FETCH FIRST ? ROWS ONLY");
		} else {
			sql.append(" LIMIT ?");
		}
		params.add(Integer.valueOf(maxRows));
	}

	private static void increment(Map<String, Long> map, String key) {
		map.merge(key, 1L, Long::sum);
	}

	private WidgetMetricValue shareMetric(ClientRollup rollup, String device) {
		if (rollup.failed) {
			return WidgetMetricValue.unavailable(message("overview_metric_unavailable"));
		}
		return shareMetric(rollup.devices.getOrDefault(device, 0L), rollup.total);
	}

	private WidgetMetricValue shareMetric(long part, long whole) {
		int pct = whole <= 0L ? 0 : (int) Math.round(100.0d * part / whole);
		return WidgetMetricValue.withPercentage(metricSupport.formatNumber(part), pct);
	}

	private WidgetMetricValue topBrowserValue(ClientRollup rollup) {
		if (rollup.failed) {
			return WidgetMetricValue.unavailable(message("overview_metric_unavailable"));
		}
		Map.Entry<String, Long> top = topEntry(rollup.browsers);
		if (top == null) {
			return WidgetMetricValue.of("-");
		}
		return WidgetMetricValue.withDetail(top.getKey(),
				metricSupport.formatNumber(top.getValue().longValue()));
	}

	private WidgetMetricValue topOsValue(ClientRollup rollup) {
		if (rollup.failed) {
			return WidgetMetricValue.unavailable(message("overview_metric_unavailable"));
		}
		Map.Entry<String, Long> top = topEntry(rollup.oses);
		if (top == null) {
			return WidgetMetricValue.of("-");
		}
		return WidgetMetricValue.withDetail(top.getKey(),
				metricSupport.formatNumber(top.getValue().longValue()));
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

	private static final class CachedClientRollup {
		final ClientRollup value;
		final long expiresAt;

		CachedClientRollup(ClientRollup value) {
			this.value = value;
			this.expiresAt = System.currentTimeMillis() + ROLLUP_TTL_MS;
		}

		boolean expired() {
			return System.currentTimeMillis() >= expiresAt;
		}
	}

	private static final class ClientRollup {
		boolean failed;
		long total;
		Map<String, Long> devices = new HashMap<String, Long>();
		Map<String, Long> oses = new HashMap<String, Long>();
		Map<String, Long> browsers = new LinkedHashMap<String, Long>();
	}
}
