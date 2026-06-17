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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.handler.EmptyRequestHandler;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableMapper;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.AjaxLazyLoadImage;
import org.sakaiproject.sitestats.tool.wicket.components.ResourceLinkWithIcon;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.components.SakaiDataTable;
import org.sakaiproject.sitestats.tool.wicket.models.ReportDefModel;
import org.sakaiproject.sitestats.tool.wicket.providers.ReportsDataProvider;

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
	private String						date;
	private String						role;
	private String						tool;
	private String						resourceAction;
	private String						lessonAction;
	private String						previewId;
	private boolean						inPrintVersion;

	private ReportDefModel				reportDefModel;
	private Report						report;
	private PrefsData					prefsdata;
	private WebPage						returnPage;

	private AjaxLazyLoadImage			reportChart			= null;
	private byte[]						chartImage			= null;
	private int							selectedWidth		= 0;
	private int							selectedHeight		= 0;

	public ReportDataPage(final ReportDefModel reportDef, final PageParameters pageParameters, final WebPage returnPage) {
		this.reportDefModel = reportDef;
		realSiteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.get("siteId").toString();
			inPrintVersion = pageParameters.get("printVersion").toBoolean(false);
			widgetId = pageParameters.get("widgetId").toOptionalString();
			tabId = pageParameters.get("tabId").toOptionalString();
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
			if(isWidgetReport() || (reportDef != null && getReportDef() != null && getReportDef().getReportParams() != null)) {
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
		response.render(JavaScriptHeaderItem.forUrl(JQUERYSCRIPT));
	}
	
	@SuppressWarnings("serial")
	private void renderBody() {
		// Set versioned to false to prevent StalePageException when using printable version
		setVersioned(false);
		boolean widgetReport = isWidgetReport();
		
		// reportAction
		if(widgetReport) {
			add(new Label("reportAction", getWidgetReportTitle()));
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
		add(new Menus("menu", siteId).setVisible(!inPrintVersion));
		
		// last job run
		add(new LastJobRun("lastJobRun", siteId));
		
		// print link/info
		WebMarkupContainer toPrintVersion = new WebMarkupContainer("toPrintVersion");
		toPrintVersion.setVisible(!inPrintVersion && !widgetReport);
		toPrintVersion.setVersioned(false);
		Link<Void> printLink = new Link<Void>("printLink") {
			@Override
			public void onClick() {
				PageParameters params = new PageParameters();
				params.set("printVersion", "true");
				params.set("siteId", siteId);
				setResponsePage(new ReportDataPage(reportDefModel, params, getWebPage()));
			}
		};
		printLink.setVersioned(false);
		toPrintVersion.add(printLink);
		add(toPrintVersion);
		WebMarkupContainer inPrintContainer = new WebMarkupContainer("inPrintVersion");
		inPrintContainer.setVisible(inPrintVersion && !widgetReport);
		inPrintContainer.setVersioned(false);
		add(inPrintContainer);

		// Report data
		final ReportsDataProvider dataProvider = widgetReport ? null : new ReportsDataProvider(getPrefsdata(), getReportDef());
		if (!widgetReport) {
			report = dataProvider.getReport();
		}
		boolean renderJsonReport = renderJsonReport();

		WebMarkupContainer reportPanel = new WebMarkupContainer("reportPanel");
		if (renderJsonReport) {
			reportPanel.add(AttributeModifier.replace("endpoint", getReportEndpoint()));
		}
		reportPanel.setOutputMarkupId(true);
		reportPanel.setVersioned(false);
		reportPanel.setVisible(renderJsonReport);
		add(reportPanel);

		// Report: chart. Legacy rendering remains for printable reports only.
		reportChart = new AjaxLazyLoadImage("reportChart", getPage()) {
			@Override
			public byte[] getImageData() {
				return getChartImage();
			}

			@Override
			public byte[] getImageData(int width, int height) {
				return getChartImage(width, height);
			}
		};
		reportChart.setOutputMarkupId(true);
		reportChart.setVersioned(false);
		add(reportChart);
		boolean showLegacyChart = !widgetReport && inPrintVersion && (
				ReportManager.HOW_PRESENTATION_CHART.equals(report.getReportDefinition().getReportParams().getHowPresentationMode())
				|| ReportManager.HOW_PRESENTATION_BOTH.equals(report.getReportDefinition().getReportParams().getHowPresentationMode()) );
		reportChart.setVisible(showLegacyChart);
		if(showLegacyChart) {
			reportChart.setAutoDetermineChartSizeByAjax(".chartContainer");
		}

		// Report: table
		if (widgetReport) {
			form.add(new WebMarkupContainer("table").setVisible(false));
		} else {
			SakaiDataTable reportTable = new SakaiDataTable(
					"table",
					getTableColumns(getReportParams(), true),
					dataProvider,
					!inPrintVersion);
			if(inPrintVersion) {
				reportTable.setItemsPerPage(Integer.MAX_VALUE);
			}
			reportTable.setVisible(inPrintVersion && (
					ReportManager.HOW_PRESENTATION_TABLE.equals(report.getReportDefinition().getReportParams().getHowPresentationMode())
					|| ReportManager.HOW_PRESENTATION_BOTH.equals(report.getReportDefinition().getReportParams().getHowPresentationMode())
					));
			form.add(reportTable);
		}
		
		
		// Report: header (report info)		
		WebMarkupContainer summaryTable = new WebMarkupContainer("summaryTable");
		summaryTable.setVisible(!widgetReport);
		add(summaryTable);

		WebMarkupContainer trDescription = new WebMarkupContainer("trDescription");
		trDescription.setVisible(!widgetReport && getReportDescription() != null);
		trDescription.add(new Label("reportDescription", widgetReport ? "" : getReportDescription()));
		summaryTable.add(trDescription);
		
		summaryTable.add(new Label("reportSite", widgetReport ? "" : getReportSite()));
		
		summaryTable.add(new Label("reportActivityBasedOn", widgetReport ? "" : getReportActivityBasedOn()));
		
		WebMarkupContainer trResourceAction = new WebMarkupContainer("trResourceAction");
		trResourceAction.setVisible(!widgetReport && getReportResourceAction() != null);
		trResourceAction.add(new Label("reportResourceActionTitle", widgetReport ? "" : getReportResourceActionTitle()));
		trResourceAction.add(new Label("reportResourceAction", widgetReport ? "" : getReportResourceAction()));
		summaryTable.add(trResourceAction);
		
		WebMarkupContainer trActivitySelection = new WebMarkupContainer("trActivitySelection");
		trActivitySelection.setVisible(!widgetReport && getReportActivitySelection() != null);
		trActivitySelection.add(new Label("reportActivitySelectionTitle", widgetReport ? "" : getReportActivitySelectionTitle()));
		trActivitySelection.add(new Label("reportActivitySelection", widgetReport ? "" : getReportActivitySelection()));
		summaryTable.add(trActivitySelection);
		
		summaryTable.add(new Label("reportTimePeriod", widgetReport ? "" : getReportTimePeriod()));
		
		summaryTable.add(new Label("reportUserSelectionType", widgetReport ? "" : getReportUserSelectionType()));
		
		WebMarkupContainer trReportUserSelection = new WebMarkupContainer("trReportUserSelection");
		trReportUserSelection.setVisible(!widgetReport && getReportUserSelectionTitle() != null);
		trReportUserSelection.add(new Label("reportUserSelectionTitle", widgetReport ? "" : getReportUserSelectionTitle()));
		trReportUserSelection.add(new Label("reportUserSelection", widgetReport ? "" : getReportUserSelection()));
		summaryTable.add(trReportUserSelection);
		
		summaryTable.add(new Label("reportGenerationDate", widgetReport ? "" : getReportGenerationDate()));
		
		
		// buttons
		form.add(new Button("back") {
			@Override
			public void onSubmit() {
				setResponsePage(returnPage);
				super.onSubmit();
			}
		});
		form.add(new Button("export") {
			@Override
			public void onSubmit() {
				super.onSubmit();
			}
		}.setDefaultFormProcessing(false).setVisible(!widgetReport && Locator.getFacade().getStatsManager().isEnableReportExport() && !inPrintVersion));
		form.add(new Button("exportXls") {
			@Override
			public void onSubmit() {
				exportXls();
				super.onSubmit();
			}
		}.setVisible(!widgetReport));
		form.add(new Button("exportCsv") {
			@Override
			public void onSubmit() {
				exportCsv();
				super.onSubmit();
			}
		}.setVisible(!widgetReport));
		form.add(new Button("exportPdf") {
			@Override
			public void onSubmit() {
				exportPdf();
				super.onSubmit();
			}
		}.setVisible(!widgetReport));
	}
	
	@SuppressWarnings("serial")
	public static List<IColumn> getTableColumns(
			final ReportParams reportParams, final boolean columnsSortable
		) {
		List<IColumn> columns = new ArrayList<IColumn>();
		final SiteStatsTableMapper tableMapper = Locator.getFacade().getSiteStatsTableMapper();
		for (final SiteStatsTableColumn column : tableMapper.getColumns(reportParams, columnsSortable)) {
			columns.add(new PropertyColumn(new Model(column.getLabel()), column.isSortable() ? column.getSortKey() : null, column.getKey()) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					SiteStatsTableCell cell = tableMapper.getCell((Stat) model.getObject(), column.getKey());
					item.add(componentForCell(componentId, column, cell));
				}
			});
		}
		return columns;
	}

	private static Component componentForCell(String componentId, SiteStatsTableColumn column, SiteStatsTableCell cell) {
		if (StatsManager.T_SITE.equals(column.getKey()) || StatsManager.T_RESOURCE.equals(column.getKey())) {
			String target = StatsManager.T_SITE.equals(column.getKey()) ? "_parent" : "_new";
			return new ResourceLinkWithIcon(componentId, cell.getIcon(), cell.getHref(), cell.getDisplay(), target);
		}
		if (StatsManager.T_PAGE.equals(column.getKey())) {
			return new ResourceLinkWithIcon(componentId, "", "", cell.getDisplay(), "_new");
		}

		String display = StringUtils.defaultString(cell.getDisplay());
		Label label = new Label(componentId, needsLeadingIconSpace(column) && StringUtils.isNotBlank(display) ? " " + display : display);
		if (needsLeadingIconSpace(column) && StringUtils.isNotBlank(cell.getIcon())) {
			label.add(new AttributeModifier("class", new Model(cell.getIcon())));
			label.add(new AttributeModifier("title", new Model(display)));
		}
		return label;
	}

	private static boolean needsLeadingIconSpace(SiteStatsTableColumn column) {
		return StatsManager.T_TOOL.equals(column.getKey()) || StatsManager.T_EVENT.equals(column.getKey());
	}
	
	private byte[] getChartImage() {
		if(chartImage == null) {
			chartImage = getChartImage(selectedWidth, selectedHeight);
		}
		return chartImage;
	}
	
	private byte[] getChartImage(int width, int height) {
		PrefsData prefsData = Locator.getFacade().getStatsManager().getPreferences(siteId, false);
		int _width = (width <= 0) ? 350 : width;
		int _height = (height <= 0) ? 200: height;
		return Locator.getFacade().getChartService().generateChart(
					report, _width, _height,
					prefsData.isChartIn3D(), prefsData.getChartTransparency(),
					prefsData.isItemLabelsVisible()
			);
	}
	
	protected String getExportFileName() {
		StringBuilder exportFileName = new StringBuilder();
		if(getReportDef().getTitle() != null && getReportDef().getTitle().trim().length() != 0) {
			String titleStr = null;
			if(getReportDef().isTitleLocalized()) {
				titleStr = (String) new ResourceModel(getReportDef().getTitleBundleKey()).getObject();
			}else{
				titleStr = getReportDef().getTitle();
			}
			exportFileName.append((String) new ResourceModel("reportres_title").getObject());
			exportFileName.append(" (");
			exportFileName.append(titleStr);
			exportFileName.append(')');
		}else if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_VISITS)) {
			exportFileName.append((String) new ResourceModel("report_what_visits").getObject());
		}else if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS)) {
			exportFileName.append((String) new ResourceModel("report_what_events").getObject());
		}else if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_PRESENCES)) {
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
		String fileName = getExportFileName();
		byte[] hssfWorkbookBytes = Locator.getFacade().getReportManager().getReportAsExcel(report, fileName);
		
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
		String fileName = getExportFileName();
		String csvString = Locator.getFacade().getReportManager().getReportAsCsv(report);
		
		RequestCycle.get().scheduleRequestHandlerAfterCurrent(new EmptyRequestHandler());
		WebResponse response = (WebResponse) getResponse();
		response.setContentType("text/comma-separated-values");
		response.setAttachmentHeader(fileName + ".csv");
		response.setHeader("Cache-Control", "max-age=0");
		response.setContentLength(csvString.length());
		OutputStream out = null;
		try{
			out = response.getOutputStream();
			out.write(csvString.getBytes());
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
		String fileName = getExportFileName();
		byte[] pdf = Locator.getFacade().getReportManager().getReportAsPDF(report);

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

	private boolean renderJsonReport() {
		return !inPrintVersion;
	}

	private String getWidgetReportTitle() {
		String widgetTitleKey = "reportres_title";
		if ("visits".equals(widgetId) || "student-visits".equals(widgetId)) {
			widgetTitleKey = "overview_title_visits";
		} else if ("activity".equals(widgetId)) {
			widgetTitleKey = "overview_title_activity";
		} else if ("resources".equals(widgetId)) {
			widgetTitleKey = "overview_title_resources";
		} else if ("lessons".equals(widgetId)) {
			widgetTitleKey = "overview_title_lessonpages";
		}

		String tabTitleKey = "reportres_title";
		if ("bydate".equals(tabId)) {
			tabTitleKey = "overview_tab_bydate";
		} else if ("byuser".equals(tabId)) {
			tabTitleKey = "overview_tab_byuser";
		} else if ("bytool".equals(tabId)) {
			tabTitleKey = "overview_tab_bytool";
		} else if ("byresource".equals(tabId)) {
			tabTitleKey = "overview_tab_byresource";
		} else if ("bypage".equals(tabId)) {
			tabTitleKey = "overview_tab_bypage";
		}

		return new ResourceModel(widgetTitleKey).getObject() + " - " + new ResourceModel(tabTitleKey).getObject();
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
		if (getReportDef().getId() > 0) {
			return SiteStatsApiUrls.persistedReport(siteId, getReportDef().getId(), request);
		}
		return SiteStatsApiUrls.previewReport(siteId, getPreviewId(), request);
	}

	private String getPreviewId() {
		if (StringUtils.isBlank(previewId)) {
			previewId = Locator.getFacade().getSiteStatsReportPreviewService().register(siteId, getReportDef());
		}
		return previewId;
	}

	private PrefsData getPrefsdata() {
		if(prefsdata == null) {
			prefsdata = Locator.getFacade().getStatsManager().getPreferences(siteId, true);
		}
		return prefsdata;
	}

	public void setReportDef(ReportDef reportDef) {
		this.reportDefModel.setObject(reportDef);
	}

	public ReportDef getReportDef() {
		return (ReportDef) this.reportDefModel.getObject();
	}

	public void setReportParams(ReportParams reportParams) {
		getReportDef().setReportParams(reportParams);
	}

	public ReportParams getReportParams() {
		return getReportDef().getReportParams();
	}
	
	// ######################################################################################
	// Report results: SUMMARY 
	// ######################################################################################	
	public String getReportDescription() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportDescription(report);
	}
	
	public String getReportSite() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportSite(report);
	}
	
	public String getReportGenerationDate() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportGenerationDate(report);
	}
	
	public String getReportActivityBasedOn() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportActivityBasedOn(report);
	}
	
	public String getReportActivitySelectionTitle() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportActivitySelectionTitle(report);
	}
	
	public String getReportActivitySelection() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportActivitySelection(report);
	}
	
	public String getReportResourceActionTitle() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportResourceActionTitle(report);
	}
	
	public String getReportResourceAction() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportResourceAction(report);
	}
	
	public String getReportTimePeriod() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportTimePeriod(report);
	}
	
	public String getReportUserSelectionType() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportUserSelectionType(report);
	}
	
	public String getReportUserSelectionTitle() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportUserSelectionTitle(report);
	}
	
	public String getReportUserSelection() {
		return Locator.getFacade().getReportManager().getReportFormattedParams().getReportUserSelection(report);
	}

}
