/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsChartDataset;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;
import org.sakaiproject.util.ResourceLoader;

public class ServerWideReportViewMapper {

	private static final String LABEL = "label";

	private static final ResourceLoader MSGS = new ResourceLoader("Messages");

	public SiteStatsReportView map(ServerWideReportSpec spec, List<ServerWideReportRow> rows) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setTitle(message(spec.getTitleKey()));
		view.setPresentationMode("how-presentation-both");
		view.setChart(chart(spec, rows));
		view.setTable(table(spec, rows));
		return view;
	}

	private SiteStatsChart chart(ServerWideReportSpec spec, List<ServerWideReportRow> rows) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(message(spec.getTitleKey()));
		chart.setType(spec.getChartType());
		chart.setXKey(label(spec.getColumns().get(0)));
		chart.setYKey(message("th_total"));
		chart.setEmptyMessage(message("no_data"));

		for (ServerWideReportColumn column : spec.getColumns()) {
			if (LABEL.equals(column.getKey())) {
				continue;
			}
			SiteStatsChartDataset dataset = new SiteStatsChartDataset();
			dataset.setKey(column.getKey());
			dataset.setLabel(label(column));
			for (ServerWideReportRow row : rows) {
				Object rawValue = row.getValues().get(column.getKey());
				if (rawValue instanceof Number) {
					Object x = row.getValues().get(LABEL);
					SiteStatsChartPoint point = new SiteStatsChartPoint();
					point.setX(x);
					point.setLabel(displayValue(x));
					point.setY((Number) rawValue);
					dataset.getPoints().add(point);
				}
			}
			chart.getDatasets().add(dataset);
		}
		return chart;
	}

	private SiteStatsTable table(ServerWideReportSpec spec, List<ServerWideReportRow> rows) {
		SiteStatsTable table = new SiteStatsTable();
		table.setCaption(message(spec.getTitleKey()));
		table.setPage(1);
		table.setPageSize(rows.size());
		table.setTotalRows(rows.size());
		for (ServerWideReportColumn column : spec.getColumns()) {
			SiteStatsTableColumn tableColumn = new SiteStatsTableColumn();
			tableColumn.setKey(column.getKey());
			tableColumn.setLabel(label(column));
			tableColumn.setType(column.getType());
			tableColumn.setAlign("number".equals(column.getType()) ? "end" : "start");
			table.getColumns().add(tableColumn);
		}
		for (ServerWideReportRow sourceRow : rows) {
			SiteStatsTableRow row = new SiteStatsTableRow();
			for (ServerWideReportColumn column : spec.getColumns()) {
				Object raw = sourceRow.getValues().get(column.getKey());
				SiteStatsTableCell cell = new SiteStatsTableCell();
				cell.setRaw(raw);
				cell.setSort(raw);
				cell.setDisplay(displayValue(raw));
				row.getCells().put(column.getKey(), cell);
			}
			table.getRows().add(row);
		}
		return table;
	}

	private String label(ServerWideReportColumn column) {
		if (column.getLabel() != null) {
			return column.getLabel();
		}
		return message(column.getLabelKey());
	}

	private String displayValue(Object raw) {
		if (raw == null) {
			return "";
		}
		if (raw instanceof Date) {
			return DateFormat.getDateInstance(DateFormat.SHORT, MSGS.getLocale()).format((Date) raw);
		}
		if (raw instanceof Number) {
			return NumberFormat.getNumberInstance(MSGS.getLocale()).format(raw);
		}
		return String.valueOf(raw);
	}

	private String message(String key) {
		try {
			return MSGS.getString(key);
		} catch (Exception e) {
			return key;
		}
	}
}
