package org.sakaiproject.sitestats.tool.wicket.pages;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
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
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.EmptyRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.ImageWithLink;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menu;
import org.sakaiproject.sitestats.tool.wicket.components.SakaiDataTable;
import org.sakaiproject.sitestats.tool.wicket.models.ReportParamsModel;
import org.sakaiproject.sitestats.tool.wicket.providers.ReportsDataProvider;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * @author Nuno Fernandes
 */
public class ReportDataPage extends BasePage {
	private static Log				LOG				= LogFactory.getLog(ReportDataPage.class);

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	private String					realSiteId;
	private String					siteId;
	private String					siteTitle;
	private boolean					inPrintVersion;
	
	private ReportParams			reportParams;
	private Report					report;
	private PrefsData				prefsdata;
	private WebPage					returnPage;
	
	public ReportDataPage(final ReportParams reportParams) {
		this(null, reportParams, null);
	}

	public ReportDataPage(final PageParameters pageParameters, final ReportParams reportParams) {
		this(pageParameters, reportParams, null);
	}

	public ReportDataPage(final PageParameters pageParameters, final ReportParams reportParams, final ReportsPage reportsPage) {
		this.reportParams = reportParams;
		realSiteId = facade.getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.getString("siteId");
			inPrintVersion = pageParameters.getBoolean("printVersion");
		}
		if(siteId == null){
			siteId = realSiteId;
		}
		if(reportsPage == null) {
			returnPage = new ReportsPage(reportParams);			
		}
		this.returnPage = reportsPage;
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			if(reportParams != null) {
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
		// display site title/id?
		WebMarkupContainer site = new WebMarkupContainer("site");
		site.setVisible(siteId != null && !realSiteId.equals(siteId));
		add(site);
		siteTitle = facade.getSiteService().getSiteDisplay(siteId);
		site.add(new Label("siteTitle", siteTitle));
		
		// model
		setModel(new CompoundPropertyModel(this));
		
		// form
		Form form = new Form("form");
		add(form);
		
		// menu
		add(new Menu("menu", siteId).setVisible(!inPrintVersion));
		
		// last job run
		add(new LastJobRun("lastJobRun", siteId));
		
		// print link/info
		WebMarkupContainer toPrintVersion = new WebMarkupContainer("toPrintVersion");
		toPrintVersion.setVisible(!inPrintVersion);
		toPrintVersion.add(new Link("printLink") {
			@Override
			public void onClick() {
				setResponsePage(new ReportDataPage(new PageParameters("printVersion=true,siteId="+siteId), reportParams));
			}			
		});
		add(toPrintVersion);
		add(new WebMarkupContainer("inPrintVersion").setVisible(inPrintVersion));

		
		// Report: table		
		final ReportsDataProvider dataProvider = new ReportsDataProvider(siteId, getPrefsdata(), reportParams);
		SakaiDataTable reportTable = new SakaiDataTable("table", getTableColumns(), dataProvider);
		form.add(reportTable);
		report = dataProvider.getReport();
		
		// Report: header (report info)
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
		columns.add(new PropertyColumn(new ResourceModel("th_id"), ReportsDataProvider.COL_USERID, ReportsDataProvider.COL_USERID) {
			@Override
			public void populateItem(Item item, String componentId, IModel model) {
				final String userId = ((CommonStatGrpByDate) model.getObject()).getUserId();
				String name = "-";
				if (userId != null) {
					try{
						name = facade.getUserDirectoryService().getUser(userId).getDisplayId();
					}catch(UserNotDefinedException e1){
						name = "-";
					}
				}
				item.add(new Label(componentId, name));
			}
		});
		columns.add(new PropertyColumn(new ResourceModel("th_user"), ReportsDataProvider.COL_USERNAME, ReportsDataProvider.COL_USERNAME) {
			@Override
			public void populateItem(Item item, String componentId, IModel model) {
				final String userId = ((CommonStatGrpByDate) model.getObject()).getUserId();
				String name = "-";
				if (userId != null) {
					try{
						name = facade.getUserDirectoryService().getUser(userId).getDisplayName();
					}catch(UserNotDefinedException e1){
						name = "-";
					}
				}
				item.add(new Label(componentId, name));
			}
		});
		if(!ReportManager.WHAT_RESOURCES.equals(reportParams.getWhat())
				&& !ReportManager.WHO_NONE.equals(reportParams.getWho()) ) {
			columns.add(new PropertyColumn(new ResourceModel("th_event"), ReportsDataProvider.COL_EVENT, ReportsDataProvider.COL_EVENT) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String ref = ((CommonStatGrpByDate) model.getObject()).getRef();
					String eventName = "";
					if(!"".equals(ref)){
						eventName = facade.getEventRegistryService().getEventName(ref);
					}
					item.add(new Label(componentId, eventName));
				}
			});
		}
		if(ReportManager.WHAT_RESOURCES.equals(reportParams.getWhat())
				&& !ReportManager.WHO_NONE.equals(reportParams.getWho()) ) {
			columns.add(new PropertyColumn(new ResourceModel("th_resource"), ReportsDataProvider.COL_RESOURCE, ReportsDataProvider.COL_RESOURCE) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String ref = ((CommonStatGrpByDate) model.getObject()).getRef();
					String imgUrl = "", lnkUrl = "", lnkLabel = "";
					if(!"".equals(ref)){
						imgUrl = facade.getStatsManager().getResourceImage(ref);
						lnkUrl = facade.getStatsManager().getResourceURL(ref);
						lnkLabel = facade.getStatsManager().getResourceName(ref);
					}
					item.add(new ImageWithLink(componentId, imgUrl, lnkUrl, lnkLabel, "_new"));
				}
			});
			columns.add(new PropertyColumn(new ResourceModel("th_action"), ReportsDataProvider.COL_ACTION, ReportsDataProvider.COL_ACTION) {
				@Override
				public void populateItem(Item item, String componentId, IModel model) {
					final String refAction = ((CommonStatGrpByDate) model.getObject()).getRefAction();
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
		if(!ReportManager.WHO_NONE.equals(reportParams.getWho()) ) {
			columns.add(new PropertyColumn(new ResourceModel("th_date"), ReportsDataProvider.COL_DATE, ReportsDataProvider.COL_DATE));
			columns.add(new PropertyColumn(new ResourceModel("th_total"), ReportsDataProvider.COL_TOTAL, ReportsDataProvider.COL_TOTAL));
		}
		return columns;
	}
	
	protected String getExportFileName() {
		String exportFileName = "";
		if(report.getReportParams().getWhat().equals(ReportManager.WHAT_VISITS))
			exportFileName = (String) new ResourceModel("report_what_visits").getObject();
		else if(report.getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS))
			exportFileName = (String) new ResourceModel("report_what_events").getObject();
		else 
			exportFileName = (String) new ResourceModel("report_what_resources").getObject();
		return exportFileName;
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
	
	// ######################################################################################
	// Report results: SUMMARY 
	// ######################################################################################	
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

