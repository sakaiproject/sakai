/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportFormattedParams;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportInfoItem;
import org.sakaiproject.util.ResourceLoader;

public class SiteStatsReportSummaryMapper {

	@Setter private ReportManager reportManager;

	private ResourceLoader messages = new ResourceLoader("Messages");

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

	private void addSummaryItem(List<SiteStatsReportInfoItem> summary, String id, String label, String value) {
		if (StringUtils.isBlank(label) || StringUtils.isBlank(value)) {
			return;
		}
		summary.add(new SiteStatsReportInfoItem(id, label, value));
	}

	private String message(String key) {
		try {
			return messages.getString(key);
		} catch (Exception e) {
			return key;
		}
	}
}
