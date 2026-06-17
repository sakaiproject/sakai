/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportFormattedParams;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
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
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class SiteStatsReportViewMapper {

	@Setter private ReportManager reportManager;
	@Setter private SiteStatsTableMapper siteStatsTableMapper;

	private ResourceLoader messages = new ResourceLoader("Messages");

	public SiteStatsReportView mapReportView(String siteId, Report report, SiteStatsReportRequest request, PrefsData prefsData) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequests.orDefault(request);
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setPresentationMode(report.getReportDefinition().getReportParams().getHowPresentationMode());
		if (report.getReportGenerationDate() != null) {
			view.setGeneratedOn(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, currentLocale()).format(report.getReportGenerationDate()));
		}
		if (safeRequest.isIncludeTable()) {
			view.setTable(mapTable(report, safeRequest));
		}
		if (safeRequest.isIncludeChart()) {
			view.setChart(mapChart(report, prefsData));
		}
		return view;
	}

	public List<SiteStatsReportInfoItem> mapSummary(Report report) {
		ReportFormattedParams formatter = reportManager.getReportFormattedParams();
		if (formatter == null) {
			return Collections.emptyList();
		}

		List<SiteStatsReportInfoItem> summary = new ArrayList<SiteStatsReportInfoItem>();
		addSummaryItem(summary, "description", message("reportres_summ_description"), formatter.getReportDescription(report));
		addSummaryItem(summary, "site", message("reportres_summ_site"), formatter.getReportSite(report));
		addSummaryItem(summary, "activity-based-on", message("reportres_summ_act_basedon"), formatter.getReportActivityBasedOn(report));
		addSummaryItem(summary, "resource-action", formatter.getReportResourceActionTitle(report), formatter.getReportResourceAction(report));
		addSummaryItem(summary, "activity-selection", formatter.getReportActivitySelectionTitle(report), formatter.getReportActivitySelection(report));
		addSummaryItem(summary, "time-period", message("reportres_summ_timeperiod"), formatter.getReportTimePeriod(report));
		addSummaryItem(summary, "user-selection-type", message("reportres_summ_usr_selectiontype"), formatter.getReportUserSelectionType(report));
		addSummaryItem(summary, "user-selection", formatter.getReportUserSelectionTitle(report), formatter.getReportUserSelection(report));
		addSummaryItem(summary, "generated-on", message("reportres_summ_generatedon"), formatter.getReportGenerationDate(report));
		return summary;
	}

	public SiteStatsTable mapTable(Report report, SiteStatsReportRequest request) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequests.orDefault(request);
		ReportParams params = report.getReportDefinition().getReportParams();
		List<SiteStatsTableColumn> columns = siteStatsTableMapper.getColumns(params, false);
		List<Stat> reportData = report.getReportData() == null ? Collections.<Stat>emptyList() : report.getReportData();

		SiteStatsTable table = new SiteStatsTable();
		table.setCaption(localizedReportTitle(report.getReportDefinition()));
		table.setPage(safeRequest.getPage());
		table.setPageSize(safeRequest.getPageSize());
		table.setTotalRows(reportData.size());
		table.setColumns(columns);

		int first = (safeRequest.getPage() - 1) * safeRequest.getPageSize();
		int last = Math.min(first + safeRequest.getPageSize(), reportData.size());
		if (first > last) {
			first = last;
		}

		for (Stat stat : reportData.subList(first, last)) {
			SiteStatsTableRow row = new SiteStatsTableRow();
			Map<String, SiteStatsTableCell> cells = new LinkedHashMap<String, SiteStatsTableCell>();
			for (SiteStatsTableColumn column : columns) {
				cells.put(column.getKey(), siteStatsTableMapper.getCell(stat, column.getKey()));
			}
			row.setCells(cells);
			table.getRows().add(row);
		}
		return table;
	}

	public SiteStatsChart mapChart(Report report, PrefsData prefsData) {
		ReportParams params = report.getReportDefinition().getReportParams();
		PrefsData safePrefsData = prefsData == null ? new PrefsData() : prefsData;
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(localizedReportTitle(report.getReportDefinition()));
		chart.setType(params.getHowChartType());
		String chartSource = chartSourceKey(params);
		chart.setXKey(chartSource);
		chart.setYKey(StatsManager.T_TOTAL);
		chart.setEmptyMessage(message("no_data"));
		chart.setThreeDimensional(safePrefsData.isChartIn3D());
		chart.setTransparency(safePrefsData.getChartTransparency());
		chart.setItemLabelsVisible(safePrefsData.isItemLabelsVisible());

		List<Stat> reportData = report.getReportData() == null ? Collections.<Stat>emptyList() : report.getReportData();
		if (reportData.isEmpty()) {
			return chart;
		}

		try {
			mapChartDatasets(chart, report, reportData, chartSource);
		} catch (IllegalArgumentException e) {
			chart.getDatasets().clear();
			chart.setEmptyMessage(message("sitestats_chart_unsupported",
					"This chart configuration is not yet supported by the SiteStats JSON renderer."));
			log.warn("Unsupported SiteStats JSON chart configuration for report {}: {}", report.getReportDefinition().getId(), e.getMessage());
		}
		return chart;
	}

	public String localizedReportTitle(ReportDef reportDef) {
		if (reportDef.getTitle() == null) {
			return message("reportres_title");
		}
		if (reportDef.isTitleLocalized()) {
			return message(reportDef.getTitleBundleKey());
		}
		return reportDef.getTitle();
	}

	public String localizedReportDescription(ReportDef reportDef) {
		if (reportDef.getDescription() == null) {
			return null;
		}
		if (reportDef.isDescriptionLocalized()) {
			return message(reportDef.getDescriptionBundleKey());
		}
		return reportDef.getDescription();
	}

	private void addSummaryItem(List<SiteStatsReportInfoItem> summary, String id, String label, String value) {
		if (StringUtils.isBlank(label) || StringUtils.isBlank(value)) {
			return;
		}
		summary.add(new SiteStatsReportInfoItem(id, label, value));
	}

	private void mapChartDatasets(SiteStatsChart chart, Report report, List<Stat> reportData, String chartSource) {
		String chartType = chart.getType();
		if (StatsManager.CHARTTYPE_PIE.equals(chartType)) {
			mapPieChartDatasets(chart, report, reportData, chartSource);
			return;
		}
		if (StatsManager.CHARTTYPE_TIMESERIES.equals(chartType) || StatsManager.CHARTTYPE_TIMESERIESBAR.equals(chartType)) {
			mapTimeSeriesChartDatasets(chart, report, reportData, chartSource);
			return;
		}
		if (StatsManager.CHARTTYPE_BAR.equals(chartType) || StatsManager.CHARTTYPE_LINE.equals(chartType)) {
			mapCategoryChartDatasets(chart, report, reportData, chartSource);
			return;
		}
		throw new IllegalArgumentException("Unsupported chart type: " + chartType);
	}

	private void mapPieChartDatasets(SiteStatsChart chart, Report report, List<Stat> reportData, String chartSource) {
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey(chartSource);
		dataset.setLabel(siteStatsTableMapper.getColumn(chartSource, false).getLabel());
		ChartDatasetAccumulator accumulator = new ChartDatasetAccumulator(dataset.getKey(), dataset.getLabel());
		String valueKey = chartSingleValueKey(report);
		for (Stat stat : reportData) {
			addPoint(accumulator, siteStatsTableMapper.getCell(stat, chartSource), siteStatsTableMapper.getNumericValue(stat, valueKey));
		}
		chart.getDatasets().add(toDataset(accumulator, null, null));
	}

	private void mapCategoryChartDatasets(SiteStatsChart chart, Report report, List<Stat> reportData, String chartSource) {
		ReportParams params = report.getReportDefinition().getReportParams();
		String categorySource = chartCategorySource(params);
		if (categorySource == null) {
			mapSimpleValueDatasets(chart, report, reportData, chartSource);
			return;
		}

		List<String> valueKeys = chartValueKeys(report);
		if (valueKeys.size() != 1) {
			throw new IllegalArgumentException("Category charts with multiple value series are not supported");
		}
		String valueKey = valueKeys.get(0);
		Map<String, ChartDatasetAccumulator> datasets = new LinkedHashMap<String, ChartDatasetAccumulator>();
		Map<String, SiteStatsTableCell> xCells = new LinkedHashMap<String, SiteStatsTableCell>();
		for (Stat stat : reportData) {
			SiteStatsTableCell seriesCell = siteStatsTableMapper.getCell(stat, chartSource);
			SiteStatsTableCell categoryCell = siteStatsTableMapper.getCell(stat, categorySource);
			String seriesKey = pointKey(seriesCell);
			ChartDatasetAccumulator dataset = datasets.get(seriesKey);
			if (dataset == null) {
				dataset = new ChartDatasetAccumulator(seriesKey, seriesCell.getDisplay());
				datasets.put(seriesKey, dataset);
			}
			xCells.putIfAbsent(pointKey(categoryCell), categoryCell);
			addPoint(dataset, categoryCell, siteStatsTableMapper.getNumericValue(stat, valueKey));
		}
		addDatasets(chart, datasets, new ArrayList<String>(xCells.keySet()), xCells);
	}

	private void mapTimeSeriesChartDatasets(SiteStatsChart chart, Report report, List<Stat> reportData, String chartSource) {
		if (!isDateChartSource(chartSource)) {
			throw new IllegalArgumentException("Time-series charts require a date chart source: " + chartSource);
		}

		ReportParams params = report.getReportDefinition().getReportParams();
		String seriesSource = chartSeriesSource(params);
		if (seriesSource == null) {
			mapSimpleValueDatasets(chart, report, reportData, chartSource);
			return;
		}

		List<String> valueKeys = chartValueKeys(report);
		if (valueKeys.size() != 1) {
			throw new IllegalArgumentException("Time-series charts with both multiple value series and an extra series source are not supported");
		}
		String valueKey = valueKeys.get(0);
		Map<String, ChartDatasetAccumulator> datasets = new LinkedHashMap<String, ChartDatasetAccumulator>();
		Map<String, SiteStatsTableCell> xCells = new LinkedHashMap<String, SiteStatsTableCell>();
		for (Stat stat : reportData) {
			SiteStatsTableCell xCell = siteStatsTableMapper.getCell(stat, chartSource);
			SiteStatsTableCell seriesCell = siteStatsTableMapper.getCell(stat, seriesSource);
			String seriesKey = pointKey(seriesCell);
			ChartDatasetAccumulator dataset = datasets.get(seriesKey);
			if (dataset == null) {
				dataset = new ChartDatasetAccumulator(seriesKey, seriesCell.getDisplay());
				datasets.put(seriesKey, dataset);
			}
			xCells.putIfAbsent(pointKey(xCell), xCell);
			addPoint(dataset, xCell, siteStatsTableMapper.getNumericValue(stat, valueKey));
		}
		addDatasets(chart, datasets, new ArrayList<String>(xCells.keySet()), xCells);
	}

	private void mapSimpleValueDatasets(SiteStatsChart chart, Report report, List<Stat> reportData, String chartSource) {
		List<String> valueKeys = chartValueKeys(report);
		Map<String, ChartDatasetAccumulator> datasets = new LinkedHashMap<String, ChartDatasetAccumulator>();
		Map<String, SiteStatsTableCell> xCells = new LinkedHashMap<String, SiteStatsTableCell>();
		for (String valueKey : valueKeys) {
			SiteStatsTableColumn valueColumn = siteStatsTableMapper.getColumn(valueKey, false);
			datasets.put(valueKey, new ChartDatasetAccumulator(valueKey, valueColumn.getLabel()));
		}
		for (Stat stat : reportData) {
			SiteStatsTableCell xCell = siteStatsTableMapper.getCell(stat, chartSource);
			xCells.putIfAbsent(pointKey(xCell), xCell);
			for (String valueKey : valueKeys) {
				addPoint(datasets.get(valueKey), xCell, siteStatsTableMapper.getNumericValue(stat, valueKey));
			}
		}
		addDatasets(chart, datasets, new ArrayList<String>(xCells.keySet()), xCells);
	}

	private void addDatasets(SiteStatsChart chart, Map<String, ChartDatasetAccumulator> datasets, List<String> xOrder,
			Map<String, SiteStatsTableCell> xCells) {
		for (ChartDatasetAccumulator dataset : datasets.values()) {
			chart.getDatasets().add(toDataset(dataset, xOrder, xCells));
		}
	}

	private SiteStatsChartDataset toDataset(ChartDatasetAccumulator accumulator, List<String> xOrder, Map<String, SiteStatsTableCell> xCells) {
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey(accumulator.key);
		dataset.setLabel(accumulator.label);
		if (xOrder == null) {
			for (ChartPointAccumulator point : accumulator.points.values()) {
				dataset.getPoints().add(point.toPoint());
			}
			return dataset;
		}
		for (String xKey : xOrder) {
			ChartPointAccumulator point = accumulator.points.get(xKey);
			if (point == null) {
				SiteStatsTableCell xCell = xCells.get(xKey);
				point = new ChartPointAccumulator(xCell.getRaw(), xCell.getDisplay(), Long.valueOf(0));
			}
			dataset.getPoints().add(point.toPoint());
		}
		return dataset;
	}

	private void addPoint(ChartDatasetAccumulator dataset, SiteStatsTableCell xCell, Number y) {
		String xKey = pointKey(xCell);
		ChartPointAccumulator point = dataset.points.get(xKey);
		if (point == null) {
			point = new ChartPointAccumulator(xCell.getRaw(), xCell.getDisplay(), y);
			dataset.points.put(xKey, point);
		} else {
			point.y = addNumbers(point.y, y);
		}
	}

	private Number addNumbers(Number current, Number next) {
		if (current instanceof Float || current instanceof Double || next instanceof Float || next instanceof Double) {
			return Double.valueOf(current.doubleValue() + next.doubleValue());
		}
		return Long.valueOf(current.longValue() + next.longValue());
	}

	private String pointKey(SiteStatsTableCell cell) {
		return StringUtils.defaultString(cell.getRaw() == null ? cell.getDisplay() : String.valueOf(cell.getRaw()));
	}

	private String chartSingleValueKey(Report report) {
		List<String> valueKeys = chartValueKeys(report);
		if (valueKeys.isEmpty()) {
			throw new IllegalArgumentException("Chart has no value key");
		}
		return valueKeys.get(0);
	}

	private String chartCategorySource(ReportParams params) {
		String categorySource = StringUtils.trimToNull(params.getHowChartCategorySource());
		if (StatsManager.T_NONE.equals(categorySource)) {
			return null;
		}
		return categorySource;
	}

	private String chartSeriesSource(ReportParams params) {
		String seriesSource = StringUtils.trimToNull(params.getHowChartSeriesSource());
		if (StatsManager.T_NONE.equals(seriesSource) || StatsManager.T_TOTAL.equals(seriesSource)) {
			return null;
		}
		return seriesSource;
	}

	private boolean isDateChartSource(String chartSource) {
		return StatsManager.T_DATE.equals(chartSource)
				|| StatsManager.T_DATEMONTH.equals(chartSource)
				|| StatsManager.T_DATEYEAR.equals(chartSource)
				|| StatsManager.T_LASTDATE.equals(chartSource);
	}

	private String chartSourceKey(ReportParams params) {
		if (StatsManager.T_DATE.equals(params.getHowChartSource())) {
			if (params.getHowTotalsBy().contains(StatsManager.T_DATEMONTH)) {
				return StatsManager.T_DATEMONTH;
			}
			if (params.getHowTotalsBy().contains(StatsManager.T_DATEYEAR)) {
				return StatsManager.T_DATEYEAR;
			}
		}
		return params.getHowChartSource();
	}

	private List<String> chartValueKeys(Report report) {
		ReportParams params = report.getReportDefinition().getReportParams();
		if (ReportManager.WHAT_VISITS_TOTALS.equals(params.getWhat())
				|| params.getHowTotalsBy().contains(StatsManager.T_VISITS)
				|| params.getHowTotalsBy().contains(StatsManager.T_UNIQUEVISITS)) {
			List<String> values = new ArrayList<String>();
			values.add(StatsManager.T_VISITS);
			values.add(StatsManager.T_UNIQUEVISITS);
			return values;
		}
		if (ReportManager.WHAT_PRESENCES.equals(params.getWhat()) || params.getHowTotalsBy().contains(StatsManager.T_DURATION)) {
			return Arrays.asList(StatsManager.T_DURATION);
		}
		return Arrays.asList(StatsManager.T_TOTAL);
	}

	private Locale currentLocale() {
		return messages.getLocale();
	}

	private String message(String key) {
		try {
			return messages.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	private String message(String key, String defaultValue) {
		String value = message(key);
		if (key.equals(value) || StringUtils.startsWith(value, "[missing key")) {
			return defaultValue;
		}
		return value;
	}

	private static class ChartDatasetAccumulator {
		private final String key;
		private final String label;
		private final Map<String, ChartPointAccumulator> points = new LinkedHashMap<String, ChartPointAccumulator>();

		private ChartDatasetAccumulator(String key, String label) {
			this.key = key;
			this.label = label;
		}
	}

	private static class ChartPointAccumulator {
		private final Object x;
		private final String label;
		private Number y;

		private ChartPointAccumulator(Object x, String label, Number y) {
			this.x = x;
			this.label = label;
			this.y = y;
		}

		private SiteStatsChartPoint toPoint() {
			SiteStatsChartPoint point = new SiteStatsChartPoint();
			point.setX(x);
			point.setLabel(label);
			point.setY(y);
			return point;
		}
	}
}
