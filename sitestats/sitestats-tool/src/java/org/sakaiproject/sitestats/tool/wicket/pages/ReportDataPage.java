/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.pages;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.handler.EmptyRequestHandler;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.models.ReportDefModel;

/**
 * @author Nuno Fernandes
 */
@Slf4j
public class ReportDataPage extends BasePage {
	private static final long			serialVersionUID	= 1L;

	private String						realSiteId;
	private String						siteId;
	private String						widgetId;
	private String						tabId;
	private String						metricId;
	private String						date;
	private String						role;
	private String						tool;
	private String						resourceAction;
	private String						lessonAction;
	private String						previewId;

	private ReportDefModel				reportDefModel;
	private WebPage						returnPage;

	private WebMarkupContainer			printLink;
	private Button						exportButton;

	public ReportDataPage(final ReportDefModel reportDef, final PageParameters pageParameters, final WebPage returnPage) {
		this.reportDefModel = reportDef;
		realSiteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.get("siteId").toString();
			widgetId = pageParameters.get("widgetId").toOptionalString();
			tabId = pageParameters.get("tabId").toOptionalString();
			metricId = pageParameters.get("metricId").toOptionalString();
			date = pageParameters.get("date").toOptionalString();
			role = pageParameters.get("role").toOptionalString();
			tool = pageParameters.get("tool").toOptionalString();
			resourceAction = pageParameters.get("resourceAction").toOptionalString();
			lessonAction = pageParameters.get("lessonAction").toOptionalString();
		}
		if(siteId == null){
			siteId = realSiteId;
		}
		if(returnPage == null) {
			this.returnPage = new ReportsPage(pageParameters);
		}else{
			this.returnPage = returnPage;
		}
		boolean allowed = Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			if(isWidgetReport() || isWidgetMetricReport() || hasReportDefinition()) {
				renderBody();
			}else{
				setResponsePage(ReportsPage.class);
			}
		}else{
			setResponsePage(NotAuthorizedPage.class);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		StringBuilder script = new StringBuilder();
		if (printLink != null && printLink.isVisibleInHierarchy()) {
			script.append("document.getElementById('").append(printLink.getMarkupId()).append("')?.addEventListener('click', function(event) {")
					.append("event.preventDefault(); window.print();")
					.append("});");
		}
		if (exportButton != null && exportButton.isVisibleInHierarchy()) {
			script.append("document.getElementById('").append(exportButton.getMarkupId()).append("')?.addEventListener('click', function(event) {")
					.append("event.preventDefault();")
					.append("this.classList.add('d-none');")
					.append("document.querySelectorAll('.sitestats-export-option').forEach(function(button) { button.classList.remove('d-none'); });")
					.append("});");
		}
		if (script.length() > 0) {
			response.render(OnDomReadyHeaderItem.forScript(script.toString()));
		}
	}
	
	@SuppressWarnings("serial")
	private void renderBody() {
		setVersioned(false);
		boolean widgetReport = isWidgetReport();
		boolean widgetMetricReport = isWidgetMetricReport();
		
		// reportAction
		if(widgetReport) {
			add(new Label("reportAction", getWidgetReportTitle()));
		}else if(widgetMetricReport) {
			add(new Label("reportAction", getWidgetMetricReportTitle()));
		}else if(getReportDef().getTitle() != null && getReportDef().getTitle().trim().length() != 0) {
			String titleStr = null;
			if(getReportDef().isTitleLocalized()) {
				titleStr = (String) new ResourceModel("reportres_title_detailed").getObject();
				titleStr = titleStr.replaceAll("\\$\\{title\\}", (String) new ResourceModel(getReportDef().getTitleBundleKey()).getObject());
			}else{
				titleStr = new StringResourceModel("reportres_title_detailed", this, reportDefModel).getString();
			}
			add(new Label("reportAction", titleStr));
		}else{
			add(new Label("reportAction", new ResourceModel("reportres_title")));
		}
		
		// model
		setDefaultModel(new CompoundPropertyModel(this));
		
		// form
		Form form = new Form("form");
		add(form);
		
		// menu
		add(new Menus("menu", siteId));
		
		// last job run
		add(new LastJobRun("lastJobRun", siteId));
		
		WebMarkupContainer printContainer = new WebMarkupContainer("printContainer");
		printContainer.setVisible(!widgetReport);
		printContainer.setVersioned(false);
		printLink = new WebMarkupContainer("printLink");
		printLink.setOutputMarkupId(true);
		printLink.setVersioned(false);
		printContainer.add(printLink);
		add(printContainer);

		// Report data
		WebMarkupContainer reportPanel = new WebMarkupContainer("reportPanel");
		reportPanel.add(AttributeModifier.replace("endpoint", getReportEndpoint()));
		reportPanel.setOutputMarkupId(true);
		reportPanel.setVersioned(false);
		add(reportPanel);
		
		
		// buttons
		form.add(new Button("back") {
			@Override
			public void onSubmit() {
				setResponsePage(returnPage);
				super.onSubmit();
			}
		});
		boolean exportAllowed = isExportAllowed();
		exportButton = new Button("export") {
			@Override
			public void onSubmit() {
				super.onSubmit();
			}
		};
		exportButton.setDefaultFormProcessing(false);
		exportButton.setOutputMarkupId(true);
		exportButton.setVisible(exportAllowed);
		form.add(exportButton);
		form.add(new Button("exportXls") {
			@Override
			public void onSubmit() {
				exportXls();
				super.onSubmit();
			}
		}.setVisible(exportAllowed));
		form.add(new Button("exportCsv") {
			@Override
			public void onSubmit() {
				exportCsv();
				super.onSubmit();
			}
		}.setVisible(exportAllowed));
		form.add(new Button("exportPdf") {
			@Override
			public void onSubmit() {
				exportPdf();
				super.onSubmit();
			}
		}.setVisible(exportAllowed));
	}
	protected String getExportFileName(Report exportReport) {
		StringBuilder exportFileName = new StringBuilder();
		ReportDef reportDef = exportReport.getReportDefinition();
		ReportParams reportParams = reportDef.getReportParams();
		if(reportDef.getTitle() != null && reportDef.getTitle().trim().length() != 0) {
			String titleStr = null;
			if(reportDef.isTitleLocalized()) {
				titleStr = (String) new ResourceModel(reportDef.getTitleBundleKey()).getObject();
			}else{
				titleStr = reportDef.getTitle();
			}
			exportFileName.append((String) new ResourceModel("reportres_title").getObject());
			exportFileName.append(" (");
			exportFileName.append(titleStr);
			exportFileName.append(')');
		}else if(ReportManager.WHAT_VISITS.equals(reportParams.getWhat()) || ReportManager.WHAT_VISITS_TOTALS.equals(reportParams.getWhat())) {
			exportFileName.append((String) new ResourceModel("report_what_visits").getObject());
		}else if(ReportManager.WHAT_EVENTS.equals(reportParams.getWhat()) || ReportManager.WHAT_ACTIVITY_TOTALS.equals(reportParams.getWhat())) {
			exportFileName.append((String) new ResourceModel("report_what_events").getObject());
		}else if(ReportManager.WHAT_PRESENCES.equals(reportParams.getWhat())) {
			exportFileName.append((String) new ResourceModel("report_what_presences").getObject());
		}else{
			exportFileName.append((String) new ResourceModel("report_what_resources").getObject());
		}
		// append site
		exportFileName.append(" (");
		try{
			exportFileName.append((Locator.getFacade().getSiteService().getSite(siteId)).getTitle());
		}catch(IdUnusedException e){
			exportFileName.append(siteId);
		}
		exportFileName.append(')');
		return exportFileName.toString();
	}

	protected void exportXls() {
		Report exportReport = getExportReport();
		String fileName = getExportFileName(exportReport);
		byte[] hssfWorkbookBytes = Locator.getFacade().getReportManager().getReportAsExcel(exportReport, fileName);
		
		RequestCycle.get().scheduleRequestHandlerAfterCurrent(new EmptyRequestHandler());
		WebResponse response = (WebResponse) getResponse();
		response.setContentType("application/vnd.ms-excel");
		response.setAttachmentHeader(fileName + ".xls");
		response.setHeader("Cache-Control", "max-age=0");
		response.setContentLength(hssfWorkbookBytes.length);
		OutputStream out = null;
		try{
			out = response.getOutputStream();
			out.write(hssfWorkbookBytes);
			out.flush();
		}catch(IOException e){
			log.error(e.getMessage());
		}finally{
			try{
				if(out != null) out.close();
			}catch(IOException e){
				log.error(e.getMessage());
			}
		}
	}

	protected void exportCsv() {
		Report exportReport = getExportReport();
		String fileName = getExportFileName(exportReport);
		byte[] csvBytes = Locator.getFacade().getReportManager().getReportAsCsv(exportReport).getBytes(StandardCharsets.UTF_8);
		
		RequestCycle.get().scheduleRequestHandlerAfterCurrent(new EmptyRequestHandler());
		WebResponse response = (WebResponse) getResponse();
		response.setContentType("text/comma-separated-values");
		response.setAttachmentHeader(fileName + ".csv");
		response.setHeader("Cache-Control", "max-age=0");
		response.setContentLength(csvBytes.length);
		OutputStream out = null;
		try{
			out = response.getOutputStream();
			out.write(csvBytes);
			out.flush();
		}catch(IOException e){
			log.error(e.getMessage());
		}finally{
			try{
				if(out != null) out.close();
			}catch(IOException e){
				log.error(e.getMessage());
			}
		}
	}

	protected void exportPdf() {
		Report exportReport = getExportReport();
		String fileName = getExportFileName(exportReport);
		byte[] pdf = Locator.getFacade().getReportManager().getReportAsPDF(exportReport);

		RequestCycle.get().scheduleRequestHandlerAfterCurrent(new EmptyRequestHandler());
		WebResponse response = (WebResponse) getResponse();
		response.setContentType("application/pdf");
		response.setAttachmentHeader(fileName + ".pdf");
		response.setHeader("Cache-Control", "max-age=0");
		response.setContentLength(pdf.length);
		OutputStream out = null;
		try{
			out = response.getOutputStream();
			out.write(pdf);
			out.flush();
		}catch(IOException e){
			log.error(e.getMessage());
		}finally{
			try{
				if(out != null) out.close();
			}catch(IOException e){
				log.error(e.getMessage());
			}
		}
	}

	private boolean isWidgetReport() {
		return StringUtils.isNotBlank(widgetId) && StringUtils.isNotBlank(tabId);
	}

	private boolean isWidgetMetricReport() {
		return StringUtils.isNotBlank(widgetId) && StringUtils.isNotBlank(metricId);
	}

	private boolean hasReportDefinition() {
		return getReportDef() != null && getReportDef().getReportParams() != null;
	}

	private boolean isExportAllowed() {
		if (!Locator.getFacade().getStatsManager().isEnableReportExport() || isWidgetReport()) {
			return false;
		}
		try {
			if (isWidgetMetricReport()) {
				return Locator.getFacade().getSiteStatsReportExportService().canExportWidgetMetricReport(siteId, widgetId, metricId);
			}
			if (!hasReportDefinition()) {
				return false;
			}
			if (getReportDef().getId() > 0) {
				return Locator.getFacade().getSiteStatsReportExportService().canExportPersistedReport(siteId, getReportDef().getId());
			}
			return Locator.getFacade().getSiteStatsReportExportService().canExportPreviewReport(siteId, getPreviewId());
		} catch (RuntimeException e) {
			log.warn("Unable to determine SiteStats export availability for site {}: {}", siteId, e.getMessage());
			return false;
		}
	}

	private Report getExportReport() {
		if (isWidgetMetricReport()) {
			return Locator.getFacade().getSiteStatsReportExportService().getWidgetMetricReport(siteId, widgetId, metricId);
		}
		if (getReportDef().getId() > 0) {
			return Locator.getFacade().getSiteStatsReportExportService().getPersistedReport(siteId, getReportDef().getId());
		}
		return Locator.getFacade().getSiteStatsReportExportService().getPreviewReport(siteId, getPreviewId());
	}

	private String getWidgetReportTitle() {
		try {
			SiteStatsWidgetTab tab = Locator.getFacade().getSiteStatsViewService().getWidgetTab(siteId, widgetId, tabId);
			return widgetTitle(tab.getWidgetTitle()) + " - " + StringUtils.defaultIfBlank(tab.getTitle(), tabId);
		} catch (RuntimeException e) {
			log.warn("Unable to load SiteStats tab metadata for {}/{} in site {}: {}", widgetId, tabId, siteId, e.getMessage());
			return widgetTitle(null) + " - " + tabId;
		}
	}

	private String getWidgetMetricReportTitle() {
		String metricTitle = metricId;
		String widgetTitle = null;
		try {
			SiteStatsWidgetMetric metric = Locator.getFacade().getSiteStatsViewService().getWidgetMetric(siteId, widgetId, metricId);
			metricTitle = metric.getLabel();
			widgetTitle = metric.getWidgetTitle();
		} catch (RuntimeException e) {
			log.warn("Unable to load SiteStats metric metadata for {}/{} in site {}: {}", widgetId, metricId, siteId, e.getMessage());
		}
		return widgetTitle(widgetTitle) + " - " + metricTitle;
	}

	private String widgetTitle(String title) {
		return StringUtils.defaultIfBlank(title, (String) new ResourceModel("reportres_title").getObject());
	}

	private String getReportEndpoint() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setPageSize(50);
		if (StringUtils.isNotBlank(widgetId) && StringUtils.isNotBlank(tabId)) {
			request.setDate(date);
			request.setRole(role);
			request.setTool(tool);
			request.setResourceAction(resourceAction);
			request.setLessonAction(lessonAction);
			return SiteStatsApiUrls.widgetReport(siteId, widgetId, tabId, request);
		}
		if (isWidgetMetricReport()) {
			return SiteStatsApiUrls.widgetMetricReport(siteId, widgetId, metricId, request);
		}
		if (getReportDef().getId() > 0) {
			return SiteStatsApiUrls.persistedReport(siteId, getReportDef().getId(), request);
		}
		return SiteStatsApiUrls.previewReport(siteId, getPreviewId(), request);
	}

	private String getPreviewId() {
		if (StringUtils.isBlank(previewId)) {
			String userId = Locator.getFacade().getSessionManager().getCurrentSession().getUserId();
			previewId = Locator.getFacade().getSiteStatsReportPreviewService().register(siteId, userId, getReportDef());
		}
		return previewId;
	}

	public void setReportDef(ReportDef reportDef) {
		this.reportDefModel.setObject(reportDef);
	}

	public ReportDef getReportDef() {
		return this.reportDefModel != null ? (ReportDef) this.reportDefModel.getObject() : null;
	}

	public void setReportParams(ReportParams reportParams) {
		getReportDef().setReportParams(reportParams);
	}

	public ReportParams getReportParams() {
		return getReportDef().getReportParams();
	}

}
