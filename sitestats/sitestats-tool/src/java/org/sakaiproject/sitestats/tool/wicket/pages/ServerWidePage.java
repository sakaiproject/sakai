/**
 * $URL$
 * $Id$
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsServerWideReportIds;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.AdminMenu;
import org.sakaiproject.sitestats.tool.wicket.models.ServerWideModel;

/**
 * @author Nuno Fernandes
 */
public class ServerWidePage extends BasePage {
	private static final long			serialVersionUID		= 1L;
	private static final Map<String, String> SELECTOR_IDS;

	static {
		Map<String, String> selectorIds = new HashMap<String, String>();
		selectorIds.put(SiteStatsServerWideReportIds.MONTHLY_LOGIN, "reportMonthlyLogin");
		selectorIds.put(SiteStatsServerWideReportIds.WEEKLY_LOGIN, "reportWeeklyLogin");
		selectorIds.put(SiteStatsServerWideReportIds.DAILY_LOGIN, "reportDailyLogin");
		selectorIds.put(SiteStatsServerWideReportIds.REGULAR_USERS, "reportRegularUsers");
		selectorIds.put(SiteStatsServerWideReportIds.HOURLY_USAGE, "reportHourlyUsage");
		selectorIds.put(SiteStatsServerWideReportIds.TOP_ACTIVITIES, "reportTopActivities");
		selectorIds.put(SiteStatsServerWideReportIds.TOOL, "reportTool");
		SELECTOR_IDS = Collections.unmodifiableMap(selectorIds);
	}

	// UI Components
	private Label						reportTitle				= null;
	private Label						reportDescription		= null;
	private WebMarkupContainer			reportChart				= null;
	private Label						reportNotes				= null;
	private WebMarkupContainer			selectors				= null;

	private ServerWideModel				report					= null;

	private String						siteId					= null;
	private List<Component>				links					= new ArrayList<Component>();
	private Map<Component,Component>	labels					= new HashMap<Component,Component>();
	

	public ServerWidePage() {
		this(null);
	}

	public ServerWidePage(PageParameters params) {
		siteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		boolean allowed = Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStatsAdmin(siteId);
		if(allowed) {
			renderBody();
		}else{
			redirectToInterceptPage(new NotAuthorizedPage());
		}
	}

	private void renderBody() {
		add(new AdminMenu("menu"));
		
		// model
		report = new ServerWideModel();
		setDefaultModel(new CompoundPropertyModel(this));
		
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

		reportChart = new WebMarkupContainer("reportChart");
		reportChart.setOutputMarkupId(true);
		reportChart.add(AttributeModifier.replace("endpoint", Model.of("")));
		form.add(reportChart);
		
		// selectors
		selectors = new WebMarkupContainer("selectors");
		selectors.setOutputMarkupId(true);
		form.add(selectors);
		for (String reportType : SiteStatsServerWideReportIds.ORDERED_IDS) {
			makeSelectorLink(selectorId(reportType), reportType);
		}
	}

	public void setReport(ServerWideModel report) {
		this.report = report;
	}

	public ServerWideModel getReport() {
		return report;
	}
	
	@SuppressWarnings("serial")
	private void makeSelectorLink(final String id, final String view) {
		IndicatingAjaxLink link = new IndicatingAjaxLink(id) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				// select view
				report.setSelectedView(view);
				// make title, description & notes visible
				reportTitle.add(new AttributeModifier("style", new Model("display: block")));
				reportDescription.add(new AttributeModifier("style", new Model("display: block")));
				reportNotes.add(new AttributeModifier("style", new Model("display: block")));
				reportChart.add(AttributeModifier.replace("endpoint", Model.of(SiteStatsApiUrls.serverWideReport(siteId, view))));
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
				target.add(selectors);
				target.add(reportTitle);
				target.add(reportDescription);
				target.add(reportNotes);
				target.add(reportChart);
				target.appendJavaScript("setMainFrameHeightNoScroll( window.name, 650 )");
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

	private String selectorId(String reportType) {
		String selectorId = SELECTOR_IDS.get(reportType);
		if (selectorId == null) {
			throw new IllegalArgumentException("Unknown server-wide SiteStats report: " + reportType);
		}
		return selectorId;
	}
}
