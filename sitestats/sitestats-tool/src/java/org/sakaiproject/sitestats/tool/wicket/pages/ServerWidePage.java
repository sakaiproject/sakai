package org.sakaiproject.sitestats.tool.wicket.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.AdminMenu;
import org.sakaiproject.sitestats.tool.wicket.components.AjaxLazyLoadImage;
import org.sakaiproject.sitestats.tool.wicket.models.ServerWideModel;

/**
 * @author Nuno Fernandes
 */
public class ServerWidePage extends BasePage {
	private static final long			serialVersionUID		= 1L;

	@SpringBean
	private transient SakaiFacade		facade;

	// UI Components
	private Label						reportTitle				= null;
	private Label						reportDescription		= null;
	private AjaxLazyLoadImage			reportChart				= null;
	private Label						reportNotes				= null;
	private WebMarkupContainer			selectors				= null;

	private ServerWideModel				report					= null;

	private String						siteId					= null;
	private int							selectedWidth			= 0;
	private int							selectedHeight			= 0;
	private List<Component>				links					= new ArrayList<Component>();
	private Map<Component,Component>	labels					= new HashMap<Component,Component>();
	

	public ServerWidePage() {
		this(null);
	}

	public ServerWidePage(PageParameters params) {
		siteId = facade.getToolManager().getCurrentPlacement().getContext();
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStatsAdmin(siteId);
		if(allowed) {
			renderBody();
		}else{
			redirectToInterceptPage(new NotAuthorizedPage());
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.renderJavascriptReference("/library/js/jquery.js");
	}
	
	private void renderBody() {
		add(new AdminMenu("menu"));
		
		// model
		report = new ServerWideModel();
		setModel(new CompoundPropertyModel(this));
		
		Form form = new Form("serverWideReportForm");
		add(form);
		
		// title, description & notes
		reportTitle = new Label("report.reportTitle");
		reportTitle.setOutputMarkupId(true);
		form.add(reportTitle);
		reportDescription = new Label("report.reportDescription");
		reportDescription.setOutputMarkupId(true);
		form.add(reportDescription);
		reportNotes = new Label("report.reportNotes");
		reportNotes.setOutputMarkupId(true);
		form.add(reportNotes);

		// chart
		reportChart = new AjaxLazyLoadImage("reportChart", getPage()) {
			@Override
			public byte[] getImageData() {
				return getChartImage(selectedWidth, selectedHeight);
			}

			@Override
			public byte[] getImageData(int width, int height) {
				return getChartImage(width, height);
			}
		};
		reportChart.setOutputMarkupId(true);
		reportChart.setAutoDetermineChartSizeByAjax(".chartContainer");
		form.add(reportChart);
		
		// selectors
		selectors = new WebMarkupContainer("selectors");
		selectors.setOutputMarkupId(true);
		form.add(selectors);
		makeSelectorLink("reportMonthlyLogin", StatsManager.MONTHLY_LOGIN_REPORT);
		makeSelectorLink("reportWeeklyLogin", StatsManager.WEEKLY_LOGIN_REPORT);
		makeSelectorLink("reportDailyLogin", StatsManager.DAILY_LOGIN_REPORT);
		makeSelectorLink("reportRegularUsers", StatsManager.REGULAR_USERS_REPORT);
		makeSelectorLink("reportHourlyUsage", StatsManager.HOURLY_USAGE_REPORT);
		makeSelectorLink("reportTopActivities", StatsManager.TOP_ACTIVITIES_REPORT);
		makeSelectorLink("reportTool", StatsManager.TOOL_REPORT);
	}

	public void setReport(ServerWideModel report) {
		this.report = report;
	}

	public ServerWideModel getReport() {
		return report;
	}
	
	private byte[] getChartImage(int width, int height) {
		int _width = (width <= 0) ? 350 : width;
		int _height = (height <= 0) ? 200: height;
		return facade.getServerWideReportManager().generateReportChart(
			report.getSelectedView(), _width, _height
			);
	}
	
	@SuppressWarnings("serial")
	private void makeSelectorLink(final String id, final String view) {
		IndicatingAjaxLink link = new IndicatingAjaxLink(id) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				// select view
				report.setSelectedView(view);
				// make title, description & notes visible
				reportTitle.add(new AttributeModifier("style", true, new Model("display: block")));
				reportDescription.add(new AttributeModifier("style", true, new Model("display: block")));
				reportNotes.add(new AttributeModifier("style", true, new Model("display: block")));
				reportChart.renderImage(target, true);
				// toggle selectors link state
				for(Component lbl : labels.values()) {
					lbl.setVisible(false);
				}
				for(Component lnk : links) {
					lnk.setVisible(true);
				}
				this.setVisible(false);
				labels.get(this).setVisible(true);
				// mark component for rendering
				target.addComponent(selectors);
				target.addComponent(reportTitle);
				target.addComponent(reportDescription);
				target.addComponent(reportNotes);
				target.appendJavascript("setMainFrameHeightNoScroll( window.name, 650 )");
			}
		};
		link.setVisible(true);
		links.add(link);
		selectors.add(link);
		makeSelectorLabel(link, id + "Lbl");
	}
	
	private void makeSelectorLabel(final Component link, final String id) {
		WebMarkupContainer label = new WebMarkupContainer(id);
		label.setVisible(false);
		labels.put(link, label);
		selectors.add(label);
	}
}

