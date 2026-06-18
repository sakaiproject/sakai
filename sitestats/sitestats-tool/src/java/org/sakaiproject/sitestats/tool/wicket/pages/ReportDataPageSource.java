/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.wicket.pages;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.io.IClusterable;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.models.ReportDefModel;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
abstract class ReportDataPageSource implements IClusterable {

	private static final long serialVersionUID = 1L;

	@Getter protected final String siteId;
	protected final ReportDefModel reportDefModel;
	protected final Component component;

	static ReportDataPageSource resolve(String siteId, ReportDefModel reportDefModel, PageParameters pageParameters, Component component) {
		String widgetId = pageParameters == null ? null : pageParameters.get("widgetId").toOptionalString();
		String tabId = pageParameters == null ? null : pageParameters.get("tabId").toOptionalString();
		String metricId = pageParameters == null ? null : pageParameters.get("metricId").toOptionalString();
		if (StringUtils.isNotBlank(widgetId) && StringUtils.isNotBlank(tabId)) {
			return new WidgetTabSource(siteId, reportDefModel, component, widgetId, tabId, pageParameters);
		}
		if (StringUtils.isNotBlank(widgetId) && StringUtils.isNotBlank(metricId)) {
			return new WidgetMetricSource(siteId, reportDefModel, component, widgetId, metricId);
		}
		ReportDef reportDef = reportDef(reportDefModel);
		if (reportDef == null || reportDef.getReportParams() == null) {
			return null;
		}
		if (reportDef.getId() > 0) {
			return new PersistedSource(siteId, reportDefModel, component, reportDef.getId());
		}
		return new PreviewSource(siteId, reportDefModel, component, pageParameters == null ? null : pageParameters.get("previewId").toOptionalString());
	}

	abstract String title();

	abstract String endpoint(SiteStatsReportRequest request);

	boolean showPrint() {
		return true;
	}

	boolean canExport() {
		return false;
	}

	Report exportReport() {
		throw new IllegalStateException("Report source is not exportable");
	}

	protected ReportDef reportDef() {
		return reportDef(reportDefModel);
	}

	protected String defaultReportTitle() {
		return (String) new ResourceModel("reportres_title").getObject();
	}

	protected String widgetTitle(String title) {
		return StringUtils.defaultIfBlank(title, defaultReportTitle());
	}

	private static ReportDef reportDef(ReportDefModel reportDefModel) {
		return reportDefModel == null ? null : (ReportDef) reportDefModel.getObject();
	}

	private static class WidgetTabSource extends ReportDataPageSource {

		private static final long serialVersionUID = 1L;

		private final String widgetId;
		private final String tabId;
		private final String date;
		private final String role;
		private final String tool;
		private final String resourceAction;
		private final String lessonAction;

		private WidgetTabSource(String siteId, ReportDefModel reportDefModel, Component component, String widgetId, String tabId,
				PageParameters pageParameters) {
			super(siteId, reportDefModel, component);
			this.widgetId = widgetId;
			this.tabId = tabId;
			this.date = pageParameters.get("date").toOptionalString();
			this.role = pageParameters.get("role").toOptionalString();
			this.tool = pageParameters.get("tool").toOptionalString();
			this.resourceAction = pageParameters.get("resourceAction").toOptionalString();
			this.lessonAction = pageParameters.get("lessonAction").toOptionalString();
		}

		@Override
		String title() {
			try {
				SiteStatsWidgetTab tab = Locator.getFacade().getSiteStatsViewService().getWidgetTab(siteId, widgetId, tabId);
				return widgetTitle(tab.getWidgetTitle()) + " - " + StringUtils.defaultIfBlank(tab.getTitle(), tabId);
			} catch (RuntimeException e) {
				log.warn("Unable to load SiteStats tab metadata for {}/{} in site {}: {}", widgetId, tabId, siteId, e.getMessage());
				return widgetTitle(null) + " - " + tabId;
			}
		}

		@Override
		String endpoint(SiteStatsReportRequest request) {
			request.setDate(date);
			request.setRole(role);
			request.setTool(tool);
			request.setResourceAction(resourceAction);
			request.setLessonAction(lessonAction);
			return SiteStatsApiUrls.widgetReport(siteId, widgetId, tabId, request);
		}

		@Override
		boolean showPrint() {
			return false;
		}
	}

	private static class WidgetMetricSource extends ReportDataPageSource {

		private static final long serialVersionUID = 1L;

		private final String widgetId;
		private final String metricId;

		private WidgetMetricSource(String siteId, ReportDefModel reportDefModel, Component component, String widgetId, String metricId) {
			super(siteId, reportDefModel, component);
			this.widgetId = widgetId;
			this.metricId = metricId;
		}

		@Override
		String title() {
			String metricTitle = metricId;
			String title = null;
			try {
				SiteStatsWidgetMetric metric = Locator.getFacade().getSiteStatsViewService().getWidgetMetric(siteId, widgetId, metricId);
				metricTitle = metric.getLabel();
				title = metric.getWidgetTitle();
			} catch (RuntimeException e) {
				log.warn("Unable to load SiteStats metric metadata for {}/{} in site {}: {}", widgetId, metricId, siteId, e.getMessage());
			}
			return widgetTitle(title) + " - " + metricTitle;
		}

		@Override
		String endpoint(SiteStatsReportRequest request) {
			return SiteStatsApiUrls.widgetMetricReport(siteId, widgetId, metricId, request);
		}

		@Override
		boolean canExport() {
			return Locator.getFacade().getSiteStatsReportExportService().canExportWidgetMetricReport(siteId, widgetId, metricId);
		}

		@Override
		Report exportReport() {
			return Locator.getFacade().getSiteStatsReportExportService().getWidgetMetricReport(siteId, widgetId, metricId);
		}
	}

	private static class PersistedSource extends ReportDataPageSource {

		private static final long serialVersionUID = 1L;

		private final long reportId;

		private PersistedSource(String siteId, ReportDefModel reportDefModel, Component component, long reportId) {
			super(siteId, reportDefModel, component);
			this.reportId = reportId;
		}

		@Override
		String title() {
			ReportDef reportDef = reportDef();
			if (reportDef.getTitle() == null || reportDef.getTitle().trim().length() == 0) {
				return defaultReportTitle();
			}
			if (reportDef.isTitleLocalized()) {
				String titleStr = (String) new ResourceModel("reportres_title_detailed").getObject();
				return titleStr.replaceAll("\\$\\{title\\}", (String) new ResourceModel(reportDef.getTitleBundleKey()).getObject());
			}
			return new StringResourceModel("reportres_title_detailed", component, reportDefModel).getString();
		}

		@Override
		String endpoint(SiteStatsReportRequest request) {
			return SiteStatsApiUrls.persistedReport(siteId, reportId, request);
		}

		@Override
		boolean canExport() {
			return Locator.getFacade().getSiteStatsReportExportService().canExportPersistedReport(siteId, reportId);
		}

		@Override
		Report exportReport() {
			return Locator.getFacade().getSiteStatsReportExportService().getPersistedReport(siteId, reportId);
		}
	}

	private static class PreviewSource extends ReportDataPageSource {

		private static final long serialVersionUID = 1L;

		private String previewId;

		private PreviewSource(String siteId, ReportDefModel reportDefModel, Component component, String previewId) {
			super(siteId, reportDefModel, component);
			this.previewId = previewId;
		}

		@Override
		String title() {
			ReportDef reportDef = reportDef();
			if (reportDef.getTitle() == null || reportDef.getTitle().trim().length() == 0) {
				return defaultReportTitle();
			}
			if (reportDef.isTitleLocalized()) {
				String titleStr = (String) new ResourceModel("reportres_title_detailed").getObject();
				return titleStr.replaceAll("\\$\\{title\\}", (String) new ResourceModel(reportDef.getTitleBundleKey()).getObject());
			}
			return new StringResourceModel("reportres_title_detailed", component, reportDefModel).getString();
		}

		@Override
		String endpoint(SiteStatsReportRequest request) {
			return SiteStatsApiUrls.previewReport(siteId, previewId(), request);
		}

		@Override
		boolean canExport() {
			return Locator.getFacade().getSiteStatsReportExportService().canExportPreviewReport(siteId, previewId());
		}

		@Override
		Report exportReport() {
			return Locator.getFacade().getSiteStatsReportExportService().getPreviewReport(siteId, previewId());
		}

		private String previewId() {
			if (StringUtils.isBlank(previewId)) {
				String userId = Locator.getFacade().getSessionManager().getCurrentSession().getUserId();
				previewId = Locator.getFacade().getSiteStatsReportPreviewService().register(siteId, userId, reportDef());
			}
			return previewId;
		}
	}
}
