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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
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

import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.LessonBuilderStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.AjaxLazyLoadImage;
import org.sakaiproject.sitestats.tool.wicket.components.ImageWithLink;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.components.SakaiDataTable;
import org.sakaiproject.sitestats.tool.wicket.models.ReportDefModel;
import org.sakaiproject.sitestats.tool.wicket.providers.ReportsDataProvider;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * @author Nuno Fernandes
 */
@Slf4j
public class ReportDataPage extends BasePage {
	private static final long			serialVersionUID	= 1L;

	private String						realSiteId;
	private String						siteId;
	private boolean						inPrintVersion;

	private ReportDefModel				reportDefModel;
	private Report						report;
	private PrefsData					prefsdata;
	private WebPage						returnPage;

	private AjaxLazyLoadImage			reportChart			= null;
	private byte[]						chartImage			= null;
	private int							selectedWidth		= 0;
	private int							selectedHeight		= 0;
	
	public ReportDataPage(final ReportDefModel reportDef) {
		this(reportDef, null, null);
	}

	public ReportDataPage(final ReportDefModel reportDef, final PageParameters pageParameters) {
		this(reportDef, pageParameters, null);
	}

	public ReportDataPage(final ReportDefModel reportDef, final PageParameters pageParameters, final WebPage returnPage) {
		this.reportDefModel = reportDef;
		realSiteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.get("siteId").toString();
			inPrintVersion = pageParameters.get("printVersion").toBoolean(false);
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
			if(reportDef != null && getReportDef() != null && getReportDef().getReportParams() != null) {
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
		// reportAction
		if(getReportDef().getTitle() != null && getReportDef().getTitle().trim().length() != 0) {
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
		toPrintVersion.setVisible(!inPrintVersion);
		toPrintVersion.add(new Link("printLink") {
			@Override
			public void onClick() {
				PageParameters params = new PageParameters();
				params.set("printVersion", "true");
				params.set("siteId", siteId);
				setResponsePage(new ReportDataPage(reportDefModel, params));
			}			
		});
		add(toPrintVersion);
		add(new WebMarkupContainer("inPrintVersion").setVisible(inPrintVersion));

		// Report data
		final ReportsDataProvider dataProvider = new ReportsDataProvider(getPrefsdata(), getReportDef());
		report = dataProvider.getReport();
		
		// Report: chart
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
		add(reportChart);
		if(ReportManager.HOW_PRESENTATION_CHART.equals(report.getReportDefinition().getReportParams().getHowPresentationMode())
				|| ReportManager.HOW_PRESENTATION_BOTH.equals(report.getReportDefinition().getReportParams().getHowPresentationMode()) ) {
			reportChart.setVisible(true);
			reportChart.setAutoDetermineChartSizeByAjax(".chartContainer");
		}else{
			reportChart.setVisible(false);
		}			
		
		// Report: table
		SakaiDataTable reportTable = new SakaiDataTable(
				"table", 
				getTableColumns(getReportParams(), true), 
				dataProvider, 
				!inPrintVersion);
		if(inPrintVersion) {
			reportTable.setItemsPerPage(Integer.MAX_VALUE);
		}
		reportTable.setVisible(
				ReportManager.HOW_PRESENTATION_TABLE.equals(report.getReportDefinition().getReportParams().getHowPresentationMode())
				|| ReportManager.HOW_PRESENTATION_BOTH.equals(report.getReportDefinition().getReportParams().getHowPresentationMode())
				);
		form.add(reportTable);
		
		
		// Report: header (report info)		
		WebMarkupContainer trDescription = new WebMarkupContainer("trDescription");
		trDescription.setVisible(getReportDescription() != null);
		trDescription.add(new Label("reportDescription"));
		add(trDescription);
		
		add(new Label("reportSite"));
		
		add(new Label("reportActivityBasedOn"));
		
		WebMarkupContainer trResourceAction = new WebMarkupContainer("trResourceAction");
		trResourceAction.setVisible(getReportResourceAction() != null);
		trResourceAction.add(new Label("reportResourceActionTitle"));
		trResourceAction.add(new Label("reportResourceAction"));
		add(trResourceAction);
		
		WebMarkupContainer trActivitySelection = new WebMarkupContainer("trActivitySelection");
		trActivitySelection.setVisible(getReportActivitySelection() != null);
		trActivitySelection.add(new Label("reportActivitySelectionTitle"));
		trActivitySelection.add(new Label("reportActivitySelection"));
		add(trActivitySelection);
		
		add(new Label("reportTimePeriod"));
		
		add(new Label("reportUserSelectionType"));
		
		WebMarkupContainer trReportUserSelection = new WebMarkupContainer("trReportUserSelection");
		trReportUserSelection.setVisible(getReportUserSelectionTitle() != null);
		trReportUserSelection.add(new Label("reportUserSelectionTitle"));
		trReportUserSelection.add(new Label("reportUserSelection"));
		add(trReportUserSelection);
		
		add(new Label("report.localizedReportGenerationDate"));
		
		
		// buttons
		form.add(new Button("back") {
			@Override
			public void onSubmit() {
				setResponsePage(returnPage);
				super.onSubmit();
			}
		}.setVisible(!inPrintVersion));
		form.add(new Button("export") {
			@Override
			public void onSubmit() {
				super.onSubmit();
			}
		}.setDefaultFormProcessing(false).setVisible(Locator.getFacade().getStatsManager().isEnableReportExport() && !inPrintVersion));
		form.add(new Button("exportXls") {
			@Override
			public void onSubmit() {
				exportXls();
				super.onSubmit();
			}
		});
		form.add(new Button("exportCsv") {
			@Override
			public void onSubmit() {
				exportCsv();
				super.onSubmit();
			}
		});
		form.add(new Button("exportPdf") {
			@Override
			public void onSubmit() {
				exportPdf();
				super.onSubmit();
			}
		});
	}
	
	@SuppressWarnings("serial")
	public static List<IColumn> getTableColumns(
			final ReportParams reportParams, final boolean columnsSortable
		) {
		List<IColumn> columns = new ArrayList<IColumn>();
		final Map<String,ToolInfo> eventIdToolMap = Locator.getFacade().getEventRegistryService().getEventIdToolMap();
		
		// site
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_SITE)) {
			columns.add(new PropertyColumn(new ResourceModel("th_site"), columnsSortable ? ReportsDataProvider.COL_SITE : null, ReportsDataProvider.COL_SITE) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String site = ((Stat) model.getObject()).getSiteId();
					String lbl = "", href = "";
					Site s = null;
					try{
						s = Locator.getFacade().getSiteService().getSite(site);
						lbl = s.getTitle();
						href = s.getUrl();
					}catch(IdUnusedException e){
						lbl = (String) new ResourceModel("site_unknown").getObject();
						href = null;
					}
					item.add(new ImageWithLink(componentId, null, href, lbl, "_parent"));
				}
			});
		}
		// user
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_USER)) {
			columns.add(new PropertyColumn(new ResourceModel("th_id"), columnsSortable ? ReportsDataProvider.COL_USERID : null, ReportsDataProvider.COL_USERID) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String userId = ((Stat) model.getObject()).getUserId();
					String name = null;
					if (userId != null) {
						if(("-").equals(userId) || EventTrackingService.UNKNOWN_USER.equals(userId)) {
							name = "-";
						}else{
							try{
								name = Locator.getFacade().getUserDirectoryService().getUser(userId).getDisplayId();
							}catch(UserNotDefinedException e1){
								name = userId;
							}
						}
					}else{
						name = (String) new ResourceModel("user_unknown").getObject();
					}
					item.add(new Label(componentId, name));
				}
			});
			columns.add(new PropertyColumn(new ResourceModel("th_user"), columnsSortable ? ReportsDataProvider.COL_USERNAME : null, ReportsDataProvider.COL_USERNAME) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String userId = ((Stat) model.getObject()).getUserId();
					String name = null;
					if (userId != null) {
						if(("-").equals(userId)) {
							name = (String) new ResourceModel("user_anonymous").getObject();
						}else if(EventTrackingService.UNKNOWN_USER.equals(userId)) {
							name = (String) new ResourceModel("user_anonymous_access").getObject();
						}else{
							name= Locator.getFacade().getStatsManager().getUserNameForDisplay(userId);
						}
					}else{
						name = (String) new ResourceModel("user_unknown").getObject();
					}
					item.add(new Label(componentId, name));
				}
			});
		}
		// tool
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_TOOL)) {
			columns.add(new PropertyColumn(new ResourceModel("th_tool"), columnsSortable ? ReportsDataProvider.COL_TOOL : null, ReportsDataProvider.COL_TOOL) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String toolId = ((EventStat) model.getObject()).getToolId();
					String toolName = "";
					if(!"".equals(toolId)){
						toolName = Locator.getFacade().getEventRegistryService().getToolName(toolId);
					}
					Label toolLabel = new Label(componentId, toolName);
					String toolIconClass = "toolIcon";
					String toolIconPath = "url(" + Locator.getFacade().getEventRegistryService().getToolIcon(toolId) + ")";
					toolLabel.add(new AttributeModifier("class", new Model(toolIconClass)));
					toolLabel.add(new AttributeModifier("style", new Model("background-image: "+toolIconPath)));
					toolLabel.add(new AttributeModifier("title", new Model(toolName)));
					item.add(toolLabel);
				}
			});
		}
		// event
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_EVENT)) {
			columns.add(new PropertyColumn(new ResourceModel("th_event"), columnsSortable ? ReportsDataProvider.COL_EVENT : null, ReportsDataProvider.COL_EVENT) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String eventId = ((EventStat) model.getObject()).getEventId();
					String eventName = "";
					if(!"".equals(eventId)){
						eventName = Locator.getFacade().getEventRegistryService().getEventName(eventId);
					}
					Label eventLabel = new Label(componentId, eventName);
					ToolInfo toolInfo = eventIdToolMap.get(eventId);
					if(toolInfo != null) {
						String toolId = toolInfo.getToolId();
						String toolName = Locator.getFacade().getEventRegistryService().getToolName(toolId);
						String toolIconClass = "toolIcon";
						String toolIconPath = "url(" + Locator.getFacade().getEventRegistryService().getToolIcon(toolId) + ")";
						eventLabel.add(new AttributeModifier("class", new Model(toolIconClass)));
						eventLabel.add(new AttributeModifier("style", new Model("background-image: "+toolIconPath)));
						eventLabel.add(new AttributeModifier("title", new Model(toolName)));
					}
					item.add(eventLabel);
				}
			});
		}
		// resource
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_RESOURCE)) {
			columns.add(new PropertyColumn(new ResourceModel("th_resource"), columnsSortable ? ReportsDataProvider.COL_RESOURCE : null, ReportsDataProvider.COL_RESOURCE) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String ref = ((ResourceStat) model.getObject()).getResourceRef();
					String imgUrl = null, lnkUrl = null, lnkLabel = null;
					Component resourceComp = null;
					if(ref != null && !"".equals(ref)){
						imgUrl = Locator.getFacade().getStatsManager().getResourceImage(ref);
						lnkUrl = Locator.getFacade().getStatsManager().getResourceURL(ref);
						lnkLabel = Locator.getFacade().getStatsManager().getResourceName(ref);
						if(lnkLabel == null) {
							lnkLabel = (String) new ResourceModel("resource_unknown").getObject();
						}					
					}
					resourceComp = new ImageWithLink(componentId, imgUrl, lnkUrl, lnkLabel, "_new");					
					item.add(resourceComp);
				}
			});
		}
		// resource action
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_RESOURCE_ACTION)) {
			columns.add(new PropertyColumn(new ResourceModel("th_action"), columnsSortable ? ReportsDataProvider.COL_ACTION : null, ReportsDataProvider.COL_ACTION) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String refAction = ((ResourceStat) model.getObject()).getResourceAction();
					String action = "";
					if(refAction == null){
						action = "";
					}else{
						if(!"".equals(refAction.trim()))
							action = (String) new ResourceModel("action_"+refAction).getObject();
					}
					item.add(new Label(componentId, action));
				}
			});
		}
        // lessonbuilder page
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_PAGE)) {
			columns.add(new PropertyColumn(new ResourceModel("th_page"), columnsSortable ? ReportsDataProvider.COL_PAGE : null, ReportsDataProvider.COL_PAGE) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					LessonBuilderStat stat = (LessonBuilderStat) model.getObject();
					final String ref = stat.getPageRef();
					String lnkLabel = stat.getPageTitle();
					String imgUrl = "", lnkUrl = "";
				    if (lnkLabel == null) {
					    lnkLabel = (String) new ResourceModel("resource_unknown").getObject();
					}
					Component resourceComp = new ImageWithLink(componentId, imgUrl, lnkUrl, lnkLabel, "_new");
					item.add(resourceComp);
				}
			});
		}
        // lessonbuilder page action
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_PAGE_ACTION)) {
			columns.add(new PropertyColumn(new ResourceModel("th_action"), columnsSortable ? ReportsDataProvider.COL_ACTION : null, ReportsDataProvider.COL_ACTION) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String pageAction = ((LessonBuilderStat) model.getObject()).getPageAction();
					String action = "";
					if (pageAction == null){
						action = "";
					} else {
						if (!"".equals(pageAction.trim()))
							action = (String) new ResourceModel("action_" + pageAction).getObject();
					}
					item.add(new Label(componentId, action));
				}
			});
		}
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_DATE)) {
			columns.add(new PropertyColumn(new ResourceModel("th_date"), columnsSortable ? ReportsDataProvider.COL_DATE : null, ReportsDataProvider.COL_DATE));
		}
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_DATEMONTH)) {
			columns.add(new PropertyColumn(new ResourceModel("th_date"), columnsSortable ? ReportsDataProvider.COL_DATE : null, ReportsDataProvider.COL_DATE) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final Date date = ((Stat) model.getObject()).getDate();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
					item.add(new Label(componentId, sdf.format(date)));
				}
			});
		}
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_DATEYEAR)) {
			columns.add(new PropertyColumn(new ResourceModel("th_date"), columnsSortable ? ReportsDataProvider.COL_DATE : null, ReportsDataProvider.COL_DATE) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final Date date = ((Stat) model.getObject()).getDate();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
					item.add(new Label(componentId, sdf.format(date)));
				}
			});
		}
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_LASTDATE)) {
			columns.add(new PropertyColumn(new ResourceModel("th_lastdate"), columnsSortable ? ReportsDataProvider.COL_DATE : null, ReportsDataProvider.COL_DATE));
		}
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_TOTAL)) {
			columns.add(new PropertyColumn(new ResourceModel("th_total"), columnsSortable ? ReportsDataProvider.COL_TOTAL : null, "count"));
		}
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_VISITS)) {
			columns.add(new PropertyColumn(new ResourceModel("th_visits"), columnsSortable ? ReportsDataProvider.COL_VISITS : null, "totalVisits"));
		}
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_UNIQUEVISITS)) {
			columns.add(new PropertyColumn(new ResourceModel("th_uniquevisitors"), columnsSortable ? ReportsDataProvider.COL_UNIQUEVISITS : null, "totalUnique"));
		}
		if(Locator.getFacade().getReportManager().isReportColumnAvailable(reportParams, StatsManager.T_DURATION)) {
			columns.add(new PropertyColumn(new ResourceModel("th_duration"), columnsSortable ? ReportsDataProvider.COL_DURATION : null, "duration") {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					double duration = (double) ((SitePresence) model.getObject()).getDuration();
					duration = Util.round(duration / 1000 / 60, 1); // in minutes
					StringBuilder b = new StringBuilder(String.valueOf(duration));
					b.append(' ');
					b.append(new ResourceModel("minutes_abbr").getObject());					
					item.add(new Label(componentId, b.toString()));
				}
			});
		}
		return columns;
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

