package org.sakaiproject.sitestats.tool.wicket.pages;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderResponse;
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
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.EmptyRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
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
public class ReportDataPage extends BasePage {
	private static final long			serialVersionUID	= 1L;
	private static Log					LOG					= LogFactory.getLog(ReportDataPage.class);

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade		facade;

	private String						realSiteId;
	private String						siteId;
	private boolean						inPrintVersion;

	private ReportDefModel				reportDefModel;
	private Report						report;
	private PrefsData					prefsdata;
	private WebPage						returnPage;

	private AbstractDefaultAjaxBehavior	chartSizeBehavior	= null;
	private AjaxLazyLoadImage			reportChart			= null;
	private int							selectedWidth		= 0;
	private int							selectedHeight		= 0;
	private int							maximizedWidth		= 0;
	private int							maximizedHeight		= 0;
	
	public ReportDataPage(final ReportDefModel reportDef) {
		this(reportDef, null, null);
	}

	public ReportDataPage(final ReportDefModel reportDef, final PageParameters pageParameters) {
		this(reportDef, pageParameters, null);
	}

	public ReportDataPage(final ReportDefModel reportDef, final PageParameters pageParameters, final WebPage returnPage) {
		this.reportDefModel = reportDef;
		realSiteId = facade.getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.getString("siteId");
			inPrintVersion = pageParameters.getBoolean("printVersion");
		}
		if(siteId == null){
			siteId = realSiteId;
		}
		if(returnPage == null) {
			this.returnPage = new ReportsPage(pageParameters);			
		}else{
			this.returnPage = returnPage;
		}
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStats(siteId);
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
		response.renderJavascriptReference("/library/js/jquery.js");
		response.renderJavascriptReference("/sakai-sitestats-tool/script/common.js");
		super.renderHead(response);
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
		setModel(new CompoundPropertyModel(this));
		
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
				setResponsePage(new ReportDataPage(reportDefModel, new PageParameters("printVersion=true,siteId="+siteId)));
			}			
		});
		add(toPrintVersion);
		add(new WebMarkupContainer("inPrintVersion").setVisible(inPrintVersion));

		// Report data
		final ReportsDataProvider dataProvider = new ReportsDataProvider(getPrefsdata(), getReportDef());
		report = dataProvider.getReport();
		
		// Report: chart
		reportChart = new AjaxLazyLoadImage("reportChart") {
			@Override
			public BufferedImage getBufferedImage() {
				return getChartImage();
			}

			@Override
			public BufferedImage getBufferedMaximizedImage() {
				return getChartImage(maximizedWidth, maximizedHeight);
			}		
		};
		reportChart.setOutputMarkupId(true);
		add(reportChart);
		if(ReportManager.HOW_PRESENTATION_CHART.equals(report.getReportDefinition().getReportParams().getHowPresentationMode())
				|| ReportManager.HOW_PRESENTATION_BOTH.equals(report.getReportDefinition().getReportParams().getHowPresentationMode()) ) {
			reportChart.setVisible(true);
			// Report: chart ajax behavior now that we already have report data
			renderAjaxBehavior();
		}else{
			reportChart.setVisible(false);
		}
			
		
		// Report: table
		SakaiDataTable reportTable = new SakaiDataTable("table", getTableColumns(), dataProvider);
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
		}.setDefaultFormProcessing(false).setVisible(!inPrintVersion));
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
	private List<IColumn> getTableColumns() {
		List<IColumn> columns = new ArrayList<IColumn>();
		final Map<String,ToolInfo> eventIdToolMap = facade.getEventRegistryService().getEventIdToolMap();
		
		// site
		if(facade.getReportManager().isReportColumnAvailable(getReportDef().getReportParams(), StatsManager.T_SITE)) {
			columns.add(new PropertyColumn(new ResourceModel("th_site"), ReportsDataProvider.COL_SITE, ReportsDataProvider.COL_SITE) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String site = ((Stat) model.getObject()).getSiteId();
					String lbl = "", href = "";
					Site s = null;
					try{
						s = facade.getSiteService().getSite(site);
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
		if(facade.getReportManager().isReportColumnAvailable(getReportDef().getReportParams(), StatsManager.T_USER)) {
			columns.add(new PropertyColumn(new ResourceModel("th_id"), ReportsDataProvider.COL_USERID, ReportsDataProvider.COL_USERID) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String userId = ((Stat) model.getObject()).getUserId();
					String name = null;
					if (userId != null) {
						if(("-").equals(userId) || ("?").equals(userId)) {
							name = "-";
						}else{
							try{
								name = facade.getUserDirectoryService().getUser(userId).getDisplayId();
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
			columns.add(new PropertyColumn(new ResourceModel("th_user"), ReportsDataProvider.COL_USERNAME, ReportsDataProvider.COL_USERNAME) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String userId = ((Stat) model.getObject()).getUserId();
					String name = null;
					if (userId != null) {
						if(("-").equals(userId)) {
							name = (String) new ResourceModel("user_anonymous").getObject();
						}else if(("?").equals(userId)) {
							name = (String) new ResourceModel("user_anonymous_access").getObject();
						}else{
							try{
								name = facade.getUserDirectoryService().getUser(userId).getDisplayName();
							}catch(UserNotDefinedException e1){
								name = (String) new ResourceModel("user_unknown").getObject();
							}
						}
					}else{
						name = (String) new ResourceModel("user_unknown").getObject();
					}
					item.add(new Label(componentId, name));
				}
			});
		}
		// event
		if(facade.getReportManager().isReportColumnAvailable(getReportDef().getReportParams(), StatsManager.T_EVENT)) {
			columns.add(new PropertyColumn(new ResourceModel("th_event"), ReportsDataProvider.COL_EVENT, ReportsDataProvider.COL_EVENT) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String eventId = ((EventStat) model.getObject()).getEventId();
					String eventName = "";
					if(!"".equals(eventId)){
						eventName = facade.getEventRegistryService().getEventName(eventId);
					}
					Label eventLabel = new Label(componentId, eventName);
					ToolInfo toolInfo = eventIdToolMap.get(eventId);
					if(toolInfo != null) {
						String toolId = toolInfo.getToolId();
						String toolName = facade.getEventRegistryService().getToolName(toolId);
						String toolIconClass = "toolIcon";
						String toolIconPath = "url(" + facade.getEventRegistryService().getToolIcon(toolId) + ")";
						eventLabel.add(new AttributeModifier("class", true, new Model(toolIconClass)));
						eventLabel.add(new AttributeModifier("style", true, new Model("background-image: "+toolIconPath)));
						eventLabel.add(new AttributeModifier("title", true, new Model(toolName)));
					}
					item.add(eventLabel);
				}
			});
		}
		// resource
		if(facade.getReportManager().isReportColumnAvailable(getReportDef().getReportParams(), StatsManager.T_RESOURCE)) {
			columns.add(new PropertyColumn(new ResourceModel("th_resource"), ReportsDataProvider.COL_RESOURCE, ReportsDataProvider.COL_RESOURCE) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String ref = ((ResourceStat) model.getObject()).getResourceRef();
					String imgUrl = null, lnkUrl = null, lnkLabel = null;
					Component resourceComp = null;
					if(ref != null && !"".equals(ref)){
						imgUrl = facade.getStatsManager().getResourceImage(ref);
						lnkUrl = facade.getStatsManager().getResourceURL(ref);
						lnkLabel = facade.getStatsManager().getResourceName(ref);
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
		if(facade.getReportManager().isReportColumnAvailable(getReportDef().getReportParams(), StatsManager.T_RESOURCE_ACTION)) {
			columns.add(new PropertyColumn(new ResourceModel("th_action"), ReportsDataProvider.COL_ACTION, ReportsDataProvider.COL_ACTION) {
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
		if(facade.getReportManager().isReportColumnAvailable(getReportDef().getReportParams(), StatsManager.T_DATE)) {
			columns.add(new PropertyColumn(new ResourceModel("th_date"), ReportsDataProvider.COL_DATE, ReportsDataProvider.COL_DATE));
		}
		if(facade.getReportManager().isReportColumnAvailable(getReportDef().getReportParams(), StatsManager.T_LASTDATE)) {
			columns.add(new PropertyColumn(new ResourceModel("th_lastdate"), ReportsDataProvider.COL_DATE, ReportsDataProvider.COL_DATE));
		}
		if(facade.getReportManager().isReportColumnAvailable(getReportDef().getReportParams(), StatsManager.T_TOTAL)) {
			columns.add(new PropertyColumn(new ResourceModel("th_total"), ReportsDataProvider.COL_TOTAL, "count"));
		}
		return columns;
	}
	
	private BufferedImage getChartImage() {
		return getChartImage(selectedWidth, selectedHeight);
	}
	
	private BufferedImage getChartImage(int width, int height) {
		PrefsData prefsData = facade.getStatsManager().getPreferences(siteId, false);
		int _width = (width <= 0) ? 350 : width;
		int _height = (height <= 0) ? 200: height;
		BufferedImage img = facade.getChartService().generateChart(
				report, _width, _height,
				prefsData.isChartIn3D(), prefsData.getChartTransparency(),
				prefsData.isItemLabelsVisible()
		);
		return img;
	}
	
	@SuppressWarnings("serial")
	private void renderAjaxBehavior() {	
		final boolean renderChart = ReportManager.HOW_PRESENTATION_CHART.equals(report.getReportDefinition().getReportParams().getHowPresentationMode())
								|| ReportManager.HOW_PRESENTATION_BOTH.equals(report.getReportDefinition().getReportParams().getHowPresentationMode());
		
		chartSizeBehavior = new AbstractDefaultAjaxBehavior() {
			@Override
			protected void respond(AjaxRequestTarget target) {
				// get chart size
		    	Request req = RequestCycle.get().getRequest();
				try{
					selectedWidth = (int) Float.parseFloat(req.getParameter("width"));					
				}catch(NumberFormatException e){
					e.printStackTrace();
					selectedWidth = 400;
				}
				try{
					selectedHeight = (int) Float.parseFloat(req.getParameter("height"));
				}catch(NumberFormatException e){
					e.printStackTrace();
					selectedHeight = 200;
				}
				try{
					maximizedWidth = (int) Float.parseFloat(req.getParameter("maxwidth"));
				}catch(NumberFormatException e){
					e.printStackTrace();
					maximizedWidth = 640;
				}
				try{
					maximizedHeight = (int) Float.parseFloat(req.getParameter("maxheight"));
				}catch(NumberFormatException e){
					e.printStackTrace();
					maximizedHeight = 300;
				}
				target.appendJavascript(buildCallbackScript(reportChart.getCallbackUrl(), null));
			}		
			
			private String buildCallbackScript(CharSequence callbackUrl, CharSequence onSuccessCallbackScript) {
				StringBuilder script = new StringBuilder();
				script.append("wicketAjaxGet('");
				script.append(callbackUrl);
				script.append("', function() {");
				if(onSuccessCallbackScript != null) {
					script.append("setTimeout(\"");
					script.append(onSuccessCallbackScript);
					script.append("\",500)");
				}
				script.append("}, function() {});");
				return script.toString();
			}   
		};
		if(renderChart) {				
			add(chartSizeBehavior);
		}
		
		WebMarkupContainer js = new WebMarkupContainer("jsWicketChartSize");
		js.setOutputMarkupId(true);
		add(js);
		WebMarkupContainer jsCall = new WebMarkupContainer("jsWicketChartSizeCall") {
			@Override
			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
				if(renderChart) {
					StringBuilder buff = new StringBuilder();
					buff.append("jQuery(document).ready(function() {");
					buff.append("  var chartSizeCallback = '" + chartSizeBehavior.getCallbackUrl() + "'; ");
					buff.append("  setWicketChartSize(chartSizeCallback);");
					buff.append("});");
					replaceComponentTagBody(markupStream, openTag, buff.toString());
				}else{
					super.onComponentTagBody(markupStream, openTag);					
				}
			}	
		};
		jsCall.setOutputMarkupId(true);
		add(jsCall);
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
		}else{
			exportFileName.append((String) new ResourceModel("report_what_resources").getObject());
		}
		// append site
		exportFileName.append(" (");
		try{
			exportFileName.append((facade.getSiteService().getSite(siteId)).getTitle());
		}catch(IdUnusedException e){
			exportFileName.append(siteId);
		}
		exportFileName.append(')');
		return exportFileName.toString();
	}

	protected void exportXls() {
		String fileName = getExportFileName();
		byte[] hssfWorkbookBytes = facade.getReportManager().getReportAsExcel(report, fileName);
		
		RequestCycle.get().setRequestTarget(EmptyRequestTarget.getInstance());
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
			LOG.error(e);
			e.printStackTrace();
		}finally{
			try{
				if(out != null) out.close();
			}catch(IOException e){
				LOG.error(e);
				e.printStackTrace();
			}
		}
	}

	protected void exportCsv() {
		String fileName = getExportFileName();
		String csvString = facade.getReportManager().getReportAsCsv(report);
		
		RequestCycle.get().setRequestTarget(EmptyRequestTarget.getInstance());
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
			LOG.error(e);
			e.printStackTrace();
		}finally{
			try{
				if(out != null) out.close();
			}catch(IOException e){
				LOG.error(e);
				e.printStackTrace();
			}
		}
	}

	protected void exportPdf() {
		String fileName = getExportFileName();
		byte[] pdf = facade.getReportManager().getReportAsPDF(report);

		RequestCycle.get().setRequestTarget(EmptyRequestTarget.getInstance());
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
			LOG.error(e);
			e.printStackTrace();
		}finally{
			try{
				if(out != null) out.close();
			}catch(IOException e){
				LOG.error(e);
				e.printStackTrace();
			}
		}
	}

	private PrefsData getPrefsdata() {
		if(prefsdata == null) {
			prefsdata = facade.getStatsManager().getPreferences(siteId, true);
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
		return facade.getReportManager().getReportFormattedParams().getReportDescription(report);
	}
	
	public String getReportSite() {
		return facade.getReportManager().getReportFormattedParams().getReportSite(report);
	}
	
	public String getReportGenerationDate() {
		return facade.getReportManager().getReportFormattedParams().getReportGenerationDate(report);
	}
	
	public String getReportActivityBasedOn() {
		return facade.getReportManager().getReportFormattedParams().getReportActivityBasedOn(report);
	}
	
	public String getReportActivitySelectionTitle() {
		return facade.getReportManager().getReportFormattedParams().getReportActivitySelectionTitle(report);
	}
	
	public String getReportActivitySelection() {
		return facade.getReportManager().getReportFormattedParams().getReportActivitySelection(report);
	}
	
	public String getReportResourceActionTitle() {
		return facade.getReportManager().getReportFormattedParams().getReportResourceActionTitle(report);
	}
	
	public String getReportResourceAction() {
		return facade.getReportManager().getReportFormattedParams().getReportResourceAction(report);
	}
	
	public String getReportTimePeriod() {
		return facade.getReportManager().getReportFormattedParams().getReportTimePeriod(report);
	}
	
	public String getReportUserSelectionType() {
		return facade.getReportManager().getReportFormattedParams().getReportUserSelectionType(report);
	}
	
	public String getReportUserSelectionTitle() {
		return facade.getReportManager().getReportFormattedParams().getReportUserSelectionTitle(report);
	}
	
	public String getReportUserSelection() {
		return facade.getReportManager().getReportFormattedParams().getReportUserSelection(report);
	}
	
}

