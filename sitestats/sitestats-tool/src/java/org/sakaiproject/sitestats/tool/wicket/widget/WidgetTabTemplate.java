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
package org.sakaiproject.sitestats.tool.wicket.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.AjaxLazyLoadImage;
import org.sakaiproject.sitestats.tool.wicket.components.IndicatingAjaxDropDownChoice;
import org.sakaiproject.sitestats.tool.wicket.components.SakaiDataTable;
import org.sakaiproject.sitestats.tool.wicket.models.ReportDefModel;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage;
import org.sakaiproject.sitestats.tool.wicket.pages.ReportDataPage;
import org.sakaiproject.sitestats.tool.wicket.providers.ReportsDataProvider;

@Slf4j
public abstract class WidgetTabTemplate extends Panel {
	private static final long		serialVersionUID			= 1L;
	public final static int			MAX_TABLE_ROWS				= 5;
	public final static Integer		FILTER_DATE					= Integer.valueOf(0);
	public final static Integer		FILTER_ROLE					= Integer.valueOf(1);
	public final static Integer		FILTER_USER					= Integer.valueOf(2);
	public final static Integer		FILTER_TOOL					= Integer.valueOf(3);
	public final static Integer		FILTER_RESOURCE_ACTION		= Integer.valueOf(4);
	public final static Integer		FILTER_LESSON_ACTION		= Integer.valueOf(5);

	private AjaxLazyLoadImage 		chart					= null;
	private WebMarkupContainer 		tableTd					= null;
	private SakaiDataTable 			table					= null;
	private Link 					tableLink				= null;
	private WebMarkupContainer 		tableJs					= null;
	
	private ReportsDataProvider		chartDataProvider		= null;
	private ReportsDataProvider		tableDataProvider		= null;
	private PrefsData				prefsdata				= null;
	private int						chartWidth				= 0;
	private String					siteId					= null;
	private boolean					renderChart				= false;
	private boolean					renderTable				= false;
	private boolean					tabTemplateRendered		= false;
	private transient Set<Role> 	roles					= null;
	
	private String					dateFilter				= ReportManager.WHEN_LAST7DAYS;
	private String					roleFilter				= ReportManager.WHO_ALL;
	private String					toolFilter				= ReportManager.WHAT_EVENTS_ALLTOOLS;
	private String					resactionFilter			= null;
	private String					lessonActionFilter		= null;
	public void setLessonActionFilter(String lessonActionFilter) {
        this.lessonActionFilter = StringUtils.trimToNull(lessonActionFilter);
	}
	public String getLessonActionFilter() {
		return lessonActionFilter;
	}
	
	public WidgetTabTemplate(String id, String siteId) {
		super(id);	
		this.siteId = siteId;
	}

	public abstract ReportDef getChartReportDefinition();

	public abstract ReportDef getTableReportDefinition();

	public abstract boolean useChartReportDefinitionForTable();
	
	public abstract List<Integer> getFilters();
	
	@Override
	protected void onBeforeRender() {
		// update data
		setDefaultModel(new CompoundPropertyModel(this));
			
		removeAll();
			
		// get report data
		ReportDef chartRD = getChartReportDefinition();
		ReportDef tableRD = getTableReportDefinition();
		if(chartRD != null) {
			renderChart = true;
			chartDataProvider = new ReportsDataProvider(getPrefsdata(), chartRD, false);
		}
		if(tableRD != null) {
			renderTable = true;
			if(!useChartReportDefinitionForTable()) {
				tableDataProvider = new ReportsDataProvider(getPrefsdata(), tableRD, false);
			}
		}
		
		// render data
		renderFilters();
		renderChart();
		renderTable();
		tabTemplateRendered = true;
		
		super.onBeforeRender();
	}

	private void renderChart() {
		WebMarkupContainer chartTd = new WebMarkupContainer("chartTd");
		chartTd.setOutputMarkupId(true);
		chart = new AjaxLazyLoadImage("chart", OverviewPage.class) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public byte[] getImageData() {
				return getChartImage(chartWidth, 200);
			}

			@Override
			public byte[] getImageData(int width, int height) {
				return getChartImage(width, height);
			}
			
			private byte[] getChartImage(int width, int height) {
				PrefsData prefsData = Locator.getFacade().getStatsManager().getPreferences(siteId, false);
				int _width = (width <= 0) ? 350 : width;
				int _height = (height <= 0) ? 200: height;
				return Locator.getFacade().getChartService().generateChart(
							chartDataProvider.getReport(), _width, _height,
							prefsData.isChartIn3D(), prefsData.getChartTransparency(),
							prefsData.isItemLabelsVisible()
				);
			}
		};
		chart.setAutoDetermineChartSizeByAjax("#"+chartTd.getMarkupId());
		chart.setOutputMarkupId(true);
		chartTd.add(chart);
		if(!renderChart) {
			chartTd.setVisible(false);
		}else if(renderChart && !renderTable) {
			chartTd.add(AttributeModifier.replace("colspan", "2"));
		}
		add(chartTd);
	}
	
	private void renderTable() {
		tableTd = new WebMarkupContainer("tableTd");
		createTable();
		if(!renderTable) {
			tableTd.setVisible(false);
		}else if(renderTable && !renderChart) {
			tableTd.add(AttributeModifier.replace("colspan", "2"));
		}
		tableLink = new StatelessLink("link") {
			private static final long	serialVersionUID	= 1L;
			@Override
			public void onClick() {
				ReportDef rd = null;
				if(useChartReportDefinitionForTable()) {
					rd = getChartReportDefinition();
				}else{
					rd = getTableReportDefinition();
				}
				String siteId = rd.getSiteId();
				ReportDefModel reportDefModel = new ReportDefModel(rd);
				setResponsePage(new ReportDataPage(reportDefModel, new PageParameters().set("siteId", siteId), getWebPage()));
			}					
		};
		tableLink.setOutputMarkupId(true);
		tableTd.add(tableLink);
		
		tableJs = new WebMarkupContainer("tableJs") {
			private static final long	serialVersionUID	= 1L;
			@Override
			public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
				StringBuilder js = new StringBuilder();
				js.append("jQuery('#");
				js.append(table.getMarkupId());
				js.append("').fadeIn();");
				js.append("jQuery('#");
				js.append(tableLink.getMarkupId());
				js.append("').fadeIn();");
				replaceComponentTagBody(markupStream, openTag, js.toString());
			}
		};
		tableJs.setOutputMarkupId(true);
		tableTd.add(tableJs);
		
		tableTd.setOutputMarkupId(true);
		add(tableTd);
	}
	
	private void createTable() {
		if(useChartReportDefinitionForTable()) {
			table = new SakaiDataTable(
					"table", 
					ReportDataPage.getTableColumns(getChartReportDefinition().getReportParams(), false), 
					chartDataProvider, false
					);
			
		}else{
			table = new SakaiDataTable(
					"table", 
					ReportDataPage.getTableColumns(getTableReportDefinition().getReportParams(), false), 
					tableDataProvider, false
					);
		}
		table.setItemsPerPage(MAX_TABLE_ROWS);
		table.setOutputMarkupId(true);
		tableTd.add(table);
	}

	private void renderFilters() {
		List<Integer> filters = getFilters();
		
		// DATE Filter
		List<String> dateFilterOptions = Arrays.asList(
				ReportManager.WHEN_ALL, ReportManager.WHEN_LAST365DAYS,
				ReportManager.WHEN_LAST30DAYS, ReportManager.WHEN_LAST7DAYS
				);
		IChoiceRenderer dateFilterRenderer = new IChoiceRenderer() {
			private static final long	serialVersionUID	= 1L;
			public Object getDisplayValue(Object object) {
				if(ReportManager.WHEN_ALL.equals(object)) {
					return new ResourceModel("overview_filter_date_all").getObject();
				}
				if(ReportManager.WHEN_LAST365DAYS.equals(object)) {
					return new ResourceModel("report_when_last365days").getObject();
				}
				if(ReportManager.WHEN_LAST30DAYS.equals(object)) {
					return new ResourceModel("report_when_last30days").getObject();
				}
				if(ReportManager.WHEN_LAST7DAYS.equals(object)) {
					return new ResourceModel("report_when_last7days").getObject();
				}
				return object;
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		IndicatingAjaxDropDownChoice dateFilter = new IndicatingAjaxDropDownChoice("dateFilter", dateFilterOptions, dateFilterRenderer);
		dateFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				updateData(target);
			}
		});
		dateFilter.setOutputMarkupId(true);
		add(dateFilter);
		dateFilter.setVisible(filters.contains(FILTER_DATE));
		
		
		// ROLE Filter
		List<String> roleFilterOptions = new ArrayList<String>();
		roleFilterOptions.add(ReportManager.WHO_ALL);
		try{
			Site site = Locator.getFacade().getSiteService().getSite(siteId);
			roles = site.getRoles();
			Iterator<Role> i = roles.iterator();
			while(i.hasNext()){
				Role r = i.next();
				roleFilterOptions.add(r.getId());
			}
		}catch(IdUnusedException e){
			log.warn("Site does not exist: " + siteId);
		}
		IChoiceRenderer roleFilterRenderer = new IChoiceRenderer() {
			private static final long	serialVersionUID	= 1L;
			public Object getDisplayValue(Object object) {
				if(ReportManager.WHO_ALL.equals(object)) {
					return new ResourceModel("overview_filter_role_all").getObject();
				}else{
					return (String) object;
				}
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		IndicatingAjaxDropDownChoice roleFilter = new IndicatingAjaxDropDownChoice("roleFilter", roleFilterOptions, roleFilterRenderer);
		roleFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				updateData(target);
			}
		});
		roleFilter.setOutputMarkupId(true);
		add(roleFilter);
		roleFilter.setVisible(filters.contains(FILTER_ROLE));
		
		
		// TOOL Filter
		List<String> toolFilterOptions = new ArrayList<String>();
		toolFilterOptions.add(ReportManager.WHAT_EVENTS_ALLTOOLS);
		toolFilterOptions.addAll(getToolIds());
		IChoiceRenderer toolFilterRenderer = new IChoiceRenderer() {
			private static final long	serialVersionUID	= 1L;
			public Object getDisplayValue(Object object) {
				if(ReportManager.WHAT_EVENTS_ALLTOOLS.equals(object)) {
					return new ResourceModel("overview_filter_tool_all").getObject();
				}else{
					return Locator.getFacade().getEventRegistryService().getToolName((String) object);
				}
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		IndicatingAjaxDropDownChoice toolFilter = new IndicatingAjaxDropDownChoice("toolFilter", toolFilterOptions, toolFilterRenderer);
		toolFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				updateData(target);
			}
		});
		toolFilter.setOutputMarkupId(true);
		add(toolFilter);
		toolFilter.setVisible(filters.contains(FILTER_TOOL));
		
		
		// RESOURCE_ACTION Filter
		List<String> resactionFilterOptions = Arrays.asList(
				null,
				ReportManager.WHAT_RESOURCES_ACTION_NEW, ReportManager.WHAT_RESOURCES_ACTION_READ,
				ReportManager.WHAT_RESOURCES_ACTION_REVS, ReportManager.WHAT_RESOURCES_ACTION_DEL
		);
		IChoiceRenderer resactionFilterRenderer = new IChoiceRenderer() {
			private static final long	serialVersionUID	= 1L;
			public Object getDisplayValue(Object object) {
				if(object == null || "".equals(object)) {
					return new ResourceModel("overview_filter_resaction_all").getObject();
				}else{
					return (String) new ResourceModel("action_" + ((String) object)).getObject();
				}
			}
			public String getIdValue(Object object, int index) {
				if(object == null || "".equals(object)) {
					return "";
				}else{
					return (String) object;
				}
			}
		};
		IndicatingAjaxDropDownChoice resactionFilter = new IndicatingAjaxDropDownChoice("resactionFilter", resactionFilterOptions, resactionFilterRenderer) {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected CharSequence getDefaultChoice(String selected) {
				return "";
			}
		};
		resactionFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				updateData(target);
			}
		});
		resactionFilter.setOutputMarkupId(true);
		add(resactionFilter);
		resactionFilter.setVisible(filters.contains(FILTER_RESOURCE_ACTION));

		// LESSON_ACTION Filter
		List<String> lessonActionFilterOptions = Arrays.asList(
				null,
				ReportManager.WHAT_LESSONS_ACTION_CREATE, ReportManager.WHAT_LESSONS_ACTION_READ,
				ReportManager.WHAT_LESSONS_ACTION_DELETE, ReportManager.WHAT_LESSONS_ACTION_UPDATE
		);

		IChoiceRenderer<String> lessonActionFilterRenderer = new IChoiceRenderer<String>() {
			private static final long	serialVersionUID	= 1L;
			public Object getDisplayValue(String object) {

				if (object == null || "".equals(object)) {
					return new ResourceModel("overview_filter_resaction_all").getObject();
				} else {
					return new ResourceModel("action_" + object).getObject();
				}
			}
			public String getIdValue(String object, int index) {

				if (object == null || "".equals(object)) {
					return "";
				}else {
					return object;
				}
			}
		};

		IndicatingAjaxDropDownChoice lessonActionFilter = new IndicatingAjaxDropDownChoice("lessonActionFilter", lessonActionFilterOptions, lessonActionFilterRenderer) {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected CharSequence getDefaultChoice(String selected) {
				return "";
			}
		};
		lessonActionFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				updateData(target);
			}
		});
		lessonActionFilter.setOutputMarkupId(true);
		add(lessonActionFilter);
		lessonActionFilter.setVisible(filters.contains(FILTER_LESSON_ACTION));
	}
	
	private void updateData(AjaxRequestTarget target) {
		if(renderChart) {
			chartDataProvider.setReportDef(getChartReportDefinition());
			target.add(chart);
		}
		if(renderTable) {
			if(useChartReportDefinitionForTable()) {
				chartDataProvider.setReportDef(getChartReportDefinition());
			}else{
				tableDataProvider.setReportDef(getTableReportDefinition());
			}
			tableTd.remove(table);
			createTable();
			target.add(tableTd);
		}
		target.appendJavaScript("setMainFrameHeightNoScroll(window.name, 0, 300);");
	}

	private List<String> getToolIds() {
		List<String> toolIds = new ArrayList<String>();
		for(ToolInfo ti : getPrefsdata().getToolEventsDef()) {
			if(isToolSuported(ti) && ti.isSelected()) {
				toolIds.add(ti.getToolId());
			}
		}
		return toolIds;
	}
	
	private boolean isToolSuported(final ToolInfo toolInfo) {
		if(Locator.getFacade().getStatsManager().isEventContextSupported()){
			return true;
		}else{
			List<ToolInfo> siteTools = Locator.getFacade().getEventRegistryService().getEventRegistry(siteId, getPrefsdata().isListToolEventsOnlyAvailableInSite());
			Iterator<ToolInfo> i = siteTools.iterator();
			while (i.hasNext()){
				ToolInfo t = i.next();
				if(t.getToolId().equals(toolInfo.getToolId())){
					EventParserTip parserTip = t.getEventParserTip();
					if(parserTip != null && parserTip.getFor().equals(StatsManager.PARSERTIP_FOR_CONTEXTID)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean isRole(String role) {
		for(Role r : roles) {
			if(r.getId().equals(role)) {
				return true;
			}
		}
		return false;
	}
	
	private PrefsData getPrefsdata() {
		if(prefsdata == null) {
			prefsdata = Locator.getFacade().getStatsManager().getPreferences(siteId, false);
		}
		return prefsdata;
	}

	public void setDateFilter(String dateFilter) {
		this.dateFilter = dateFilter;
	}

	public String getDateFilter() {
		return dateFilter;
	}

	public void setRoleFilter(String roleFilter) {
		this.roleFilter = roleFilter;
	}

	public String getRoleFilter() {
		return roleFilter;
	}

	public void setToolFilter(String toolFilter) {
		this.toolFilter = toolFilter;
	}

	public String getToolFilter() {
		return toolFilter;
	}

	public List<String> getToolEventsFilter() {
		if(ReportManager.WHAT_EVENTS_ALLTOOLS.equals(toolFilter)) {
			return getPrefsdata().getToolEventsStringList();
		}else{
			List<String> eventIds = new ArrayList<String>();
			for(ToolInfo ti : getPrefsdata().getToolEventsDef()) {
				if(isToolSuported(ti) && ti.isSelected() && ti.getToolId().equals(toolFilter)) {
					for(EventInfo ei : ti.getEvents()) {
						if(ei.isSelected()) {
							eventIds.add(ei.getEventId());
						}
					}
					break;
				}
			}
			return eventIds;
		}
	}

	public void setResactionFilter(String resactionFilter) {
		if("".equals(resactionFilter)) {
			this.resactionFilter = null;
		}else{
			this.resactionFilter = resactionFilter;
		}
	}

	public String getResactionFilter() {
		return resactionFilter;
	}
	
}
