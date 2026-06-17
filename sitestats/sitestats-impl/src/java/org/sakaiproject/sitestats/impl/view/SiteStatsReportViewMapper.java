/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.text.DateFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Setter;

import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportInfoItem;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;
import org.sakaiproject.util.ResourceLoader;

public class SiteStatsReportViewMapper {

	@Setter private SiteStatsTableMapper siteStatsTableMapper;
	@Setter private SiteStatsChartMapper siteStatsChartMapper;
	@Setter private SiteStatsReportSummaryMapper siteStatsReportSummaryMapper;

	private ResourceLoader messages = new ResourceLoader("Messages");

	public SiteStatsReportView mapReportView(String siteId, Report report, SiteStatsReportRequest request, PrefsData prefsData) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequests.orDefault(request);
		SiteStatsReportView view = mapReportShell(siteId, report);
		if (safeRequest.isIncludeTable()) {
			view.setTable(mapTable(report, safeRequest));
		}
		if (safeRequest.isIncludeChart()) {
			view.setChart(mapChart(report, prefsData));
		}
		return view;
	}

	public SiteStatsReportView mapReportShell(String siteId, Report report) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setPresentationMode(report.getReportDefinition().getReportParams().getHowPresentationMode());
		if (report.getReportGenerationDate() != null) {
			view.setGeneratedOn(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, currentLocale()).format(report.getReportGenerationDate()));
		}
		return view;
	}

	public List<SiteStatsReportInfoItem> mapSummary(Report report) {
		return siteStatsReportSummaryMapper.mapSummary(report);
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
		return siteStatsChartMapper.mapChart(report, prefsData, localizedReportTitle(report.getReportDefinition()));
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
}
