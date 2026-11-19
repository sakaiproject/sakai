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

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_DANGER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_INFO;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_SUCCESS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_WARNING;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_AUTHORING_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_AUTHORING_SITES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_AUTHORING_TOP_EVENT;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_LEARNER_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_LEARNER_SITES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_LEARNER_TOP_EVENT;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_PRESENCE_ACTIVE_30D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_PRESENCE_ACTIVE_SITES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_PRESENCE_LOW_ACTIVITY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_PRESENCE_NEVER_VISITED;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsChartDataset;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsInstitutionalEventDictionary;
import org.sakaiproject.sitestats.api.view.SiteStatsReportInfoItem;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;

@Slf4j
public class SiteStatsInstitutionalAnalytics {

	private static final long LOW_ACTIVITY_VISIT_THRESHOLD = 5L;
	private static final int TOP_SITES = 25;
	private static final int TOP_EVENTS = 10;
	/** Oracle/MySQL-friendly upper bound for a single IN list. */
	private static final int MAX_SITE_IN_CLAUSE = 500;

	@Setter private SiteStatsInstitutionalScope institutionalScope;
	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;
	@Setter private WidgetMetricSupport metricSupport;
	@Setter private SiteService siteService;
	@Setter private SqlService sqlService;
	@Setter private ServerConfigurationService serverConfigurationService;

	Map<String, WidgetMetricValue> presenceMetricValues(SiteStatsReportRequest request) {
		PresenceRollup rollup = presenceRollup(request, false);
		SiteStatsReportRequest last30 = SiteStatsReportRequest.normalized(request);
		last30.setDate(ReportManager.WHEN_LAST30DAYS);
		PresenceRollup rollup30 = presenceRollup(last30, false);
		Map<String, WidgetMetricValue> values = new LinkedHashMap<String, WidgetMetricValue>();
		values.put(METRIC_INST_PRESENCE_ACTIVE_SITES, presenceActiveSitesValue(rollup));
		values.put(METRIC_INST_PRESENCE_ACTIVE_30D, presenceActiveSitesValue(rollup30));
		values.put(METRIC_INST_PRESENCE_LOW_ACTIVITY, presenceLowActivityValue(rollup));
		values.put(METRIC_INST_PRESENCE_NEVER_VISITED, presenceNeverVisitedValue(rollup));
		return values;
	}

	Map<String, WidgetMetricValue> learnerMetricValues(SiteStatsReportRequest request) {
		return activityMetricValues(request, SiteStatsInstitutionalEventDictionary.LEARNER_EVENTS,
				METRIC_INST_LEARNER_EVENTS, METRIC_INST_LEARNER_SITES, METRIC_INST_LEARNER_TOP_EVENT);
	}

	Map<String, WidgetMetricValue> authoringMetricValues(SiteStatsReportRequest request) {
		return activityMetricValues(request, SiteStatsInstitutionalEventDictionary.AUTHORING_EVENTS,
				METRIC_INST_AUTHORING_EVENTS, METRIC_INST_AUTHORING_SITES, METRIC_INST_AUTHORING_TOP_EVENT);
	}

	WidgetMetricValue presenceActiveSitesValue(String siteId, String userId, SiteStatsReportRequest request) {
		return presenceActiveSitesValue(presenceRollup(request, false));
	}

	WidgetMetricValue presenceActive30dValue(String siteId, String userId, SiteStatsReportRequest request) {
		SiteStatsReportRequest last30 = SiteStatsReportRequest.normalized(request);
		last30.setDate(ReportManager.WHEN_LAST30DAYS);
		return presenceActiveSitesValue(presenceRollup(last30, false));
	}

	WidgetMetricValue presenceLowActivityValue(String siteId, String userId, SiteStatsReportRequest request) {
		return presenceLowActivityValue(presenceRollup(request, false));
	}

	WidgetMetricValue presenceNeverVisitedValue(String siteId, String userId, SiteStatsReportRequest request) {
		return presenceNeverVisitedValue(presenceRollup(request, false));
	}

	SiteStatsReportView presenceBySiteReport(String siteId, SiteStatsReportRequest request, String userId) {
		PresenceRollup rollup = presenceRollup(request, true);
		SiteStatsReportView view = reportShell(siteId, "overview_title_institutional_presence");
		if (rollup.failed) {
			markUnavailable(view);
			return view;
		}
		if (request.isIncludeTable()) {
			view.setTable(siteVisitsTable(rollup.rows, request));
		}
		if (request.isIncludeChart()) {
			view.setChart(siteBarChart(rollup.rows, message("overview_title_institutional_presence"), "visits"));
		}
		return view;
	}

	SiteStatsChart presenceShareHighlight(String siteId, String userId, SiteStatsReportRequest request) {
		PresenceRollup rollup = presenceRollup(request, false);
		if (rollup.failed) {
			return null;
		}
		long healthy = Math.max(0L, rollup.activeSites - rollup.lowActivitySites);
		String category = message("overview_title_inst_presence_share");
		SiteStatsChart chart = compactStackedBar(category);
		chart.getDatasets().add(shareDataset("active", message("overview_title_inst_presence_active"),
				CHART_COLOR_SUCCESS, category, healthy));
		chart.getDatasets().add(shareDataset("low", message("overview_title_inst_presence_low_activity"),
				CHART_COLOR_WARNING, category, rollup.lowActivitySites));
		chart.getDatasets().add(shareDataset("never", message("overview_title_inst_presence_never_visited"),
				CHART_COLOR_DANGER, category, rollup.neverVisitedSites));
		return chart;
	}

	WidgetMetricValue learnerEventsValue(String siteId, String userId, SiteStatsReportRequest request) {
		return activityEventsValue(activityRollup(request, SiteStatsInstitutionalEventDictionary.LEARNER_EVENTS));
	}

	WidgetMetricValue learnerSitesValue(String siteId, String userId, SiteStatsReportRequest request) {
		return activitySitesValue(activityRollup(request, SiteStatsInstitutionalEventDictionary.LEARNER_EVENTS));
	}

	WidgetMetricValue learnerTopEventValue(String siteId, String userId, SiteStatsReportRequest request) {
		return topEventMetric(activityRollup(request, SiteStatsInstitutionalEventDictionary.LEARNER_EVENTS));
	}

	WidgetMetricValue authoringEventsValue(String siteId, String userId, SiteStatsReportRequest request) {
		return activityEventsValue(activityRollup(request, SiteStatsInstitutionalEventDictionary.AUTHORING_EVENTS));
	}

	WidgetMetricValue authoringSitesValue(String siteId, String userId, SiteStatsReportRequest request) {
		return activitySitesValue(activityRollup(request, SiteStatsInstitutionalEventDictionary.AUTHORING_EVENTS));
	}

	WidgetMetricValue authoringTopEventValue(String siteId, String userId, SiteStatsReportRequest request) {
		return topEventMetric(activityRollup(request, SiteStatsInstitutionalEventDictionary.AUTHORING_EVENTS));
	}

	SiteStatsReportView learnerByEventReport(String siteId, SiteStatsReportRequest request, String userId) {
		return activityByEventReport(siteId, request, SiteStatsInstitutionalEventDictionary.LEARNER_EVENTS,
				"overview_title_institutional_learner_activity");
	}

	SiteStatsReportView authoringByEventReport(String siteId, SiteStatsReportRequest request, String userId) {
		return activityByEventReport(siteId, request, SiteStatsInstitutionalEventDictionary.AUTHORING_EVENTS,
				"overview_title_institutional_authoring_activity");
	}

	SiteStatsChart learnerEventShareHighlight(String siteId, String userId, SiteStatsReportRequest request) {
		ActivityRollup rollup = activityRollup(request, SiteStatsInstitutionalEventDictionary.LEARNER_EVENTS);
		if (rollup.failed) {
			return null;
		}
		return activityEventShareHighlight(rollup, message("overview_title_inst_learner_event_share"));
	}

	SiteStatsChart authoringEventShareHighlight(String siteId, String userId, SiteStatsReportRequest request) {
		ActivityRollup rollup = activityRollup(request, SiteStatsInstitutionalEventDictionary.AUTHORING_EVENTS);
		if (rollup.failed) {
			return null;
		}
		return activityEventShareHighlight(rollup, message("overview_title_inst_authoring_event_share"));
	}

	private Map<String, WidgetMetricValue> activityMetricValues(SiteStatsReportRequest request, List<String> events,
			String eventsMetricId, String sitesMetricId, String topEventMetricId) {
		ActivityRollup rollup = activityRollup(request, events);
		Map<String, WidgetMetricValue> values = new LinkedHashMap<String, WidgetMetricValue>();
		values.put(eventsMetricId, activityEventsValue(rollup));
		values.put(sitesMetricId, activitySitesValue(rollup));
		values.put(topEventMetricId, topEventMetric(rollup));
		return values;
	}

	private WidgetMetricValue presenceActiveSitesValue(PresenceRollup rollup) {
		if (rollup.failed) {
			return unavailable();
		}
		return WidgetMetricValue.withPercentage(
				rollup.activeSites + " / " + rollup.totalSites,
				percent(rollup.activeSites, rollup.totalSites));
	}

	private WidgetMetricValue presenceLowActivityValue(PresenceRollup rollup) {
		if (rollup.failed) {
			return unavailable();
		}
		return WidgetMetricValue.withPercentage(
				String.valueOf(rollup.lowActivitySites),
				percent(rollup.lowActivitySites, rollup.totalSites));
	}

	private WidgetMetricValue presenceNeverVisitedValue(PresenceRollup rollup) {
		if (rollup.failed) {
			return unavailable();
		}
		return WidgetMetricValue.withPercentage(
				String.valueOf(rollup.neverVisitedSites),
				percent(rollup.neverVisitedSites, rollup.totalSites));
	}

	private WidgetMetricValue activityEventsValue(ActivityRollup rollup) {
		if (rollup.failed) {
			return unavailable();
		}
		return WidgetMetricValue.of(metricSupport.formatNumber(rollup.totalEvents));
	}

	private WidgetMetricValue activitySitesValue(ActivityRollup rollup) {
		if (rollup.failed) {
			return unavailable();
		}
		return WidgetMetricValue.withPercentage(
				rollup.activeSites + " / " + rollup.scopedSites,
				percent(rollup.activeSites, rollup.scopedSites));
	}

	private PresenceRollup presenceRollup(SiteStatsReportRequest request, boolean includeTopSites) {
		SiteStatsInstitutionalScope.ResolvedScope scope = institutionalScope.resolve(request);
		Date from = filterCatalog.periodFrom(request);
		Date to = filterCatalog.periodTo(request);
		QueryResult<Map<String, Long>> visitsResult = loadVisitsBySite(scope.getSiteIds(), from, to);

		PresenceRollup rollup = new PresenceRollup();
		rollup.totalSites = scope.size();
		if (visitsResult.failed) {
			rollup.failed = true;
			return rollup;
		}
		Map<String, Long> visitsBySite = visitsResult.value;
		List<Map.Entry<String, Long>> ranked = new ArrayList<Map.Entry<String, Long>>();
		for (String id : scope.getSiteIds()) {
			long visits = visitsBySite.getOrDefault(id, Long.valueOf(0L)).longValue();
			if (visits > 0L) {
				rollup.activeSites++;
				if (includeTopSites) {
					ranked.add(Map.entry(id, Long.valueOf(visits)));
				}
			} else {
				rollup.neverVisitedSites++;
			}
			if (visits > 0L && visits < LOW_ACTIVITY_VISIT_THRESHOLD) {
				rollup.lowActivitySites++;
			}
		}
		if (includeTopSites) {
			ranked.sort(Comparator.<Map.Entry<String, Long>>comparingLong(e -> e.getValue().longValue()).reversed()
					.thenComparing(Map.Entry::getKey));
			int limit = Math.min(TOP_SITES, ranked.size());
			for (int i = 0; i < limit; i++) {
				Map.Entry<String, Long> entry = ranked.get(i);
				String id = entry.getKey();
				rollup.rows.add(new SiteVisitRow(id, siteTitle(id), entry.getValue().longValue()));
			}
		}
		return rollup;
	}

	private ActivityRollup activityRollup(SiteStatsReportRequest request, List<String> events) {
		SiteStatsInstitutionalScope.ResolvedScope scope = institutionalScope.resolve(request);
		Date from = filterCatalog.periodFrom(request);
		Date to = filterCatalog.periodTo(request);
		ActivityRollup rollup = new ActivityRollup();
		rollup.scopedSites = scope.size();
		if (events.isEmpty() || scope.size() == 0) {
			return rollup;
		}
		QueryResult<List<Object[]>> rowsResult = loadSiteActivityRows(events, scope.getSiteIds(), from, to);
		if (rowsResult.failed) {
			rollup.failed = true;
			return rollup;
		}
		Map<String, Long> byEvent = new HashMap<String, Long>();
		Set<String> activeSites = new java.util.HashSet<String>();
		for (Object[] row : rowsResult.value) {
			String siteId = (String) row[0];
			String eventId = (String) row[1];
			long count = ((Number) row[2]).longValue();
			if (!scope.contains(siteId) || count <= 0L) {
				continue;
			}
			activeSites.add(siteId);
			rollup.totalEvents += count;
			byEvent.merge(eventId, Long.valueOf(count), (a, b) -> Long.valueOf(a.longValue() + b.longValue()));
		}
		rollup.activeSites = activeSites.size();
		byEvent.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.limit(TOP_EVENTS)
				.forEachOrdered(e -> rollup.topEvents.put(e.getKey(), e.getValue()));
		return rollup;
	}

	private QueryResult<Map<String, Long>> loadVisitsBySite(Collection<String> siteIds, Date from, Date to) {
		Map<String, Long> visits = new HashMap<String, Long>();
		if (siteIds == null || siteIds.isEmpty()) {
			return QueryResult.ok(visits);
		}
		for (List<String> batch : siteIdBatches(siteIds)) {
			QueryResult<Map<String, Long>> batchResult = loadVisitsBySiteBatch(batch, from, to);
			if (batchResult.failed) {
				return QueryResult.failed();
			}
			visits.putAll(batchResult.value);
		}
		return QueryResult.ok(visits);
	}

	private QueryResult<Map<String, Long>> loadVisitsBySiteBatch(List<String> siteIds, Date from, Date to) {
		Map<String, Long> visits = new HashMap<String, Long>();
		String table = sstTable("SST_SITEVISITS");
		StringBuilder sql = new StringBuilder(
				"select SITE_ID, sum(TOTAL_VISITS) from ").append(table).append(" where 1=1");
		List<Object> params = new ArrayList<Object>();
		appendSiteIdInClause(sql, params, siteIds);
		appendDateBounds(sql, params, "VISITS_DATE", from, to);
		sql.append(" group by SITE_ID");
		try {
			List<?> rows = sqlService.dbRead(sql.toString(), params.toArray(), new SqlReader() {
				@Override
				public Object readSqlResultRecord(ResultSet result) {
					try {
						return new Object[] { result.getString(1), Long.valueOf(result.getLong(2)) };
					} catch (SQLException e) {
						log.warn("Unable to read SST_SITEVISITS aggregate", e);
						return null;
					}
				}
			});
			if (rows != null) {
				for (Object row : rows) {
					if (row instanceof Object[]) {
						Object[] values = (Object[]) row;
						visits.put(String.valueOf(values[0]), (Long) values[1]);
					}
				}
			}
			return QueryResult.ok(visits);
		} catch (RuntimeException e) {
			log.warn("Institutional presence visit query failed: {}", e.toString());
			return QueryResult.failed();
		}
	}

	private QueryResult<List<Object[]>> loadSiteActivityRows(List<String> events, Collection<String> siteIds,
			Date from, Date to) {
		List<Object[]> out = new ArrayList<Object[]>();
		if (siteIds == null || siteIds.isEmpty()) {
			return QueryResult.ok(out);
		}
		for (List<String> batch : siteIdBatches(siteIds)) {
			QueryResult<List<Object[]>> batchResult = loadSiteActivityRowsBatch(events, batch, from, to);
			if (batchResult.failed) {
				return QueryResult.failed();
			}
			out.addAll(batchResult.value);
		}
		return QueryResult.ok(out);
	}

	private QueryResult<List<Object[]>> loadSiteActivityRowsBatch(List<String> events, List<String> siteIds,
			Date from, Date to) {
		List<Object[]> out = new ArrayList<Object[]>();
		String table = sstTable("SST_SITEACTIVITY");
		StringBuilder sql = new StringBuilder(
				"select SITE_ID, EVENT_ID, sum(ACTIVITY_COUNT) from ").append(table)
				.append(" where EVENT_ID in (").append(placeholders(events.size())).append(")");
		List<Object> params = new ArrayList<Object>();
		params.addAll(events);
		appendSiteIdInClause(sql, params, siteIds);
		appendDateBounds(sql, params, "ACTIVITY_DATE", from, to);
		sql.append(" group by SITE_ID, EVENT_ID");
		try {
			List<?> rows = sqlService.dbRead(sql.toString(), params.toArray(), new SqlReader() {
				@Override
				public Object readSqlResultRecord(ResultSet result) {
					try {
						return new Object[] {
								result.getString(1),
								result.getString(2),
								Long.valueOf(result.getLong(3)) };
					} catch (SQLException e) {
						log.warn("Unable to read SST_SITEACTIVITY aggregate", e);
						return null;
					}
				}
			});
			if (rows != null) {
				for (Object row : rows) {
					if (row instanceof Object[]) {
						out.add((Object[]) row);
					}
				}
			}
			return QueryResult.ok(out);
		} catch (RuntimeException e) {
			log.warn("Institutional activity query failed: {}", e.toString());
			return QueryResult.failed();
		}
	}

	private List<List<String>> siteIdBatches(Collection<String> siteIds) {
		List<String> ids = new ArrayList<String>(siteIds);
		List<List<String>> batches = new ArrayList<List<String>>();
		for (int i = 0; i < ids.size(); i += MAX_SITE_IN_CLAUSE) {
			batches.add(ids.subList(i, Math.min(i + MAX_SITE_IN_CLAUSE, ids.size())));
		}
		return batches;
	}

	private void appendSiteIdInClause(StringBuilder sql, List<Object> params, Collection<String> siteIds) {
		sql.append(" and SITE_ID in (").append(placeholders(siteIds.size())).append(")");
		params.addAll(siteIds);
	}

	private void appendDateBounds(StringBuilder sql, List<Object> params, String column, Date from, Date to) {
		if (from != null) {
			sql.append(" and ").append(column).append(" >= ?");
			params.add(from);
		}
		if (to != null) {
			sql.append(" and ").append(column).append(" <= ?");
			params.add(to);
		}
	}

	private String placeholders(int count) {
		StringJoiner joiner = new StringJoiner(",");
		for (int i = 0; i < count; i++) {
			joiner.add("?");
		}
		return joiner.toString();
	}

	private String sstTable(String table) {
		String external = serverConfigurationService == null
				? null
				: StringUtils.trimToNull(serverConfigurationService.getString("sitestats.externalDb.name", null));
		return external == null ? table : external + "." + table;
	}

	private WidgetMetricValue topEventMetric(ActivityRollup rollup) {
		if (rollup.failed) {
			return unavailable();
		}
		if (rollup.topEvents.isEmpty()) {
			return WidgetMetricValue.of("-");
		}
		Map.Entry<String, Long> top = rollup.topEvents.entrySet().iterator().next();
		return WidgetMetricValue.withDetail(eventLabel(top.getKey()),
				metricSupport.formatNumber(top.getValue().longValue()));
	}

	private SiteStatsReportView activityByEventReport(String siteId, SiteStatsReportRequest request,
			List<String> events, String titleKey) {
		ActivityRollup rollup = activityRollup(request, events);
		SiteStatsReportView view = reportShell(siteId, titleKey);
		if (rollup.failed) {
			markUnavailable(view);
			return view;
		}
		if (request.isIncludeTable()) {
			SiteStatsTable table = new SiteStatsTable();
			table.setCaption(view.getTitle());
			table.getColumns().add(column("event", "th_event", "text"));
			table.getColumns().add(column("count", "th_total", "number"));
			for (Map.Entry<String, Long> entry : rollup.topEvents.entrySet()) {
				SiteStatsTableRow row = new SiteStatsTableRow();
				row.getCells().put("event", textCell(eventLabel(entry.getKey())));
				row.getCells().put("count", numberCell(entry.getValue().longValue()));
				table.getRows().add(row);
			}
			view.setTable(table);
		}
		if (request.isIncludeChart()) {
			view.setChart(activityEventShareHighlight(rollup, view.getTitle()));
		}
		return view;
	}

	private SiteStatsChart activityEventShareHighlight(ActivityRollup rollup, String title) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey("event");
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		chart.setItemLabelsVisible(false);
		chart.setCompact(true);
		chart.setHorizontal(true);
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey("events");
		dataset.setLabel(title);
		dataset.setColor(CHART_COLOR_INFO);
		for (Map.Entry<String, Long> entry : rollup.topEvents.entrySet()) {
			SiteStatsChartPoint point = new SiteStatsChartPoint();
			String label = eventLabel(entry.getKey());
			point.setX(label);
			point.setLabel(label);
			point.setY(Double.valueOf(entry.getValue().doubleValue()));
			dataset.getPoints().add(point);
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	private String eventLabel(String eventId) {
		if (context != null && context.getEventRegistryService() != null) {
			try {
				String name = context.getEventRegistryService().getEventName(eventId);
				if (StringUtils.isNotBlank(name)) {
					return name;
				}
			} catch (RuntimeException e) {
				log.debug("No event label for {}: {}", eventId, e.toString());
			}
		}
		return eventId;
	}

	private SiteStatsTable siteVisitsTable(List<SiteVisitRow> rows, SiteStatsReportRequest request) {
		SiteStatsTable table = new SiteStatsTable();
		table.setCaption(message("overview_title_institutional_presence"));
		table.getColumns().add(column("site", "th_title", "text"));
		table.getColumns().add(column("visits", "overview_title_visits_sum", "number"));
		int page = request.getPage();
		int pageSize = request.getPageSize();
		int from = Math.max(0, (page - 1) * pageSize);
		int to = Math.min(rows.size(), from + pageSize);
		table.setPage(page);
		table.setPageSize(pageSize);
		table.setTotalRows(rows.size());
		for (int i = from; i < to; i++) {
			SiteVisitRow row = rows.get(i);
			SiteStatsTableRow tableRow = new SiteStatsTableRow();
			tableRow.getCells().put("site", textCell(row.title));
			tableRow.getCells().put("visits", numberCell(row.visits));
			table.getRows().add(tableRow);
		}
		return table;
	}

	private SiteStatsChart siteBarChart(List<SiteVisitRow> rows, String title, String yKey) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey("site");
		chart.setYKey(yKey);
		chart.setHorizontal(true);
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey(yKey);
		dataset.setLabel(title);
		dataset.setColor(CHART_COLOR_INFO);
		for (SiteVisitRow row : rows) {
			SiteStatsChartPoint point = new SiteStatsChartPoint();
			point.setX(row.title);
			point.setLabel(row.title);
			point.setY(Long.valueOf(row.visits));
			dataset.getPoints().add(point);
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	private SiteStatsChart compactStackedBar(String title) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey("status");
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		chart.setItemLabelsVisible(false);
		chart.setCompact(true);
		chart.setStacked(true);
		chart.setHorizontal(true);
		return chart;
	}

	private SiteStatsChartDataset shareDataset(String key, String label, String color, String category, long count) {
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey(key);
		dataset.setLabel(label);
		dataset.setColor(color);
		SiteStatsChartPoint point = new SiteStatsChartPoint();
		point.setX(category);
		point.setLabel(category);
		point.setY(Long.valueOf(count));
		dataset.getPoints().add(point);
		return dataset;
	}

	private SiteStatsReportView reportShell(String siteId, String titleKey) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message(titleKey));
		view.setGeneratedOn(Instant.now().toString());
		return view;
	}

	private void markUnavailable(SiteStatsReportView view) {
		SiteStatsReportInfoItem item = new SiteStatsReportInfoItem();
		item.setLabel(message("overview_metric_unavailable"));
		item.setValue(message("overview_metric_unavailable"));
		view.getSummary().add(item);
	}

	private String siteTitle(String siteId) {
		try {
			Site site = siteService.getSite(siteId);
			return StringUtils.defaultIfBlank(site.getTitle(), siteId);
		} catch (IdUnusedException e) {
			return siteId;
		}
	}

	private int percent(long part, long whole) {
		return whole <= 0L ? 0 : (int) Math.round(100.0d * part / whole);
	}

	private WidgetMetricValue unavailable() {
		return WidgetMetricValue.unavailable(message("overview_metric_unavailable"));
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

	private static final class PresenceRollup {
		boolean failed;
		int totalSites;
		int activeSites;
		int lowActivitySites;
		int neverVisitedSites;
		List<SiteVisitRow> rows = new ArrayList<SiteVisitRow>();
	}

	private static final class SiteVisitRow {
		final String siteId;
		final String title;
		final long visits;

		SiteVisitRow(String siteId, String title, long visits) {
			this.siteId = siteId;
			this.title = title;
			this.visits = visits;
		}
	}

	private static final class ActivityRollup {
		boolean failed;
		int scopedSites;
		int activeSites;
		long totalEvents;
		Map<String, Long> topEvents = new LinkedHashMap<String, Long>();
	}

	private static final class QueryResult<T> {
		final boolean failed;
		final T value;

		private QueryResult(boolean failed, T value) {
			this.failed = failed;
			this.value = value;
		}

		static <T> QueryResult<T> ok(T value) {
			return new QueryResult<T>(false, value);
		}

		static <T> QueryResult<T> failed() {
			return new QueryResult<T>(true, null);
		}
	}
}
