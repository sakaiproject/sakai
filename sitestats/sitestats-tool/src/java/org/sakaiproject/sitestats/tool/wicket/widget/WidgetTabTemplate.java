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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.util.Tools;
import org.sakaiproject.sitestats.tool.wicket.components.IndicatingAjaxDropDownChoice;
import org.sakaiproject.sitestats.tool.wicket.pages.ReportDataPage;

import lombok.extern.slf4j.Slf4j;

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

	private WebMarkupContainer 		reportPanel				= null;
	private Link 					tableLink				= null;
	
	private PrefsData				prefsdata				= null;
	private String					siteId					= null;
	private String					widgetId				= null;
	private String					tabId					= null;
	
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
		this(id, siteId, null, null);
	}

	public WidgetTabTemplate(String id, String siteId, String widgetId, String tabId) {
		super(id);
		this.siteId = siteId;
		this.widgetId = widgetId;
		this.tabId = tabId;
	}

	public abstract List<Integer> getFilters();

	/**
	 * Gets an optional message that will be displayed at the bottom of the tab.
	 * @return a model containing the message string
	 */
	protected Optional<IModel<String>> getFooterMsg()
	{
		String localSakaiName = Locator.getFacade().getStatsManager().getLocalSakaiName();
		StringResourceModel model = new StringResourceModel("widget_server_time_msg").setParameters(localSakaiName);
		return Optional.of(model);
	}

	@Override
	protected void onBeforeRender() {
		// update data
		setDefaultModel(new CompoundPropertyModel(this));
			
		removeAll();
			
		// render data
		renderFilters();
		renderReportPanel();
		
		Optional<IModel<String>> footerModel = getFooterMsg();
		add(new Label("widgetFooterMsg", footerModel.orElseGet(() -> Model.of(""))).setVisible(footerModel.isPresent()));

		super.onBeforeRender();
	}

	private void renderReportPanel() {
		reportPanel = new WebMarkupContainer("reportPanel");
		reportPanel.setOutputMarkupId(true);
		reportPanel.setVisible(StringUtils.isNotBlank(widgetId) && StringUtils.isNotBlank(tabId));
		reportPanel.add(AttributeModifier.replace("endpoint", getWidgetEndpoint()));
		add(reportPanel);

		tableLink = new StatelessLink("link") {
			private static final long	serialVersionUID	= 1L;
			@Override
			public void onClick() {
				PageParameters params = new PageParameters().set("siteId", siteId);
				if (widgetId != null && tabId != null) {
					params.set("widgetId", widgetId);
					params.set("tabId", tabId);
					params.set("date", getDateFilter());
					params.set("role", getRoleFilter());
					params.set("tool", getToolFilter());
					if (getResactionFilter() != null) {
						params.set("resourceAction", getResactionFilter());
					}
					if (getLessonActionFilter() != null) {
						params.set("lessonAction", getLessonActionFilter());
					}
				}
				setResponsePage(new ReportDataPage(null, params, getWebPage()));
			}					
		};
		tableLink.setOutputMarkupId(true);
		add(tableLink);
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

			@Override
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

			@Override
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		IndicatingAjaxDropDownChoice dateFilter = new IndicatingAjaxDropDownChoice("dateFilter", dateFilterOptions, dateFilterRenderer);
		dateFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
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
			Set<Role> roles = site.getRoles();
			for (Role r : roles) {
				roleFilterOptions.add(r.getId());
			}
		}catch(IdUnusedException e){
			log.warn("Site does not exist: " + siteId);
		}
		IChoiceRenderer roleFilterRenderer = new IChoiceRenderer() {
			private static final long	serialVersionUID	= 1L;

			@Override
			public Object getDisplayValue(Object object) {
				if(ReportManager.WHO_ALL.equals(object)) {
					return new ResourceModel("overview_filter_role_all").getObject();
				}else{
					return (String) object;
				}
			}

			@Override
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		IndicatingAjaxDropDownChoice roleFilter = new IndicatingAjaxDropDownChoice("roleFilter", roleFilterOptions, roleFilterRenderer);
		roleFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
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
		toolFilterOptions.addAll(Tools.getToolIds(siteId, getPrefsdata()));
		IChoiceRenderer toolFilterRenderer = new IChoiceRenderer() {
			private static final long	serialVersionUID	= 1L;

			@Override
			public Object getDisplayValue(Object object) {
				if(ReportManager.WHAT_EVENTS_ALLTOOLS.equals(object)) {
					return new ResourceModel("overview_filter_tool_all").getObject();
				}else{
					return Locator.getFacade().getEventRegistryService().getToolName((String) object);
				}
			}

			@Override
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		IndicatingAjaxDropDownChoice toolFilter = new IndicatingAjaxDropDownChoice("toolFilter", toolFilterOptions, toolFilterRenderer);
		toolFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
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
				ReportManager.WHAT_RESOURCES_ACTION_REVS, ReportManager.WHAT_RESOURCES_ACTION_DEL,
				ReportManager.WHAT_RESOURCES_ACTION_DOW
		);
		IChoiceRenderer resactionFilterRenderer = new IChoiceRenderer() {
			private static final long	serialVersionUID	= 1L;

			@Override
			public Object getDisplayValue(Object object) {
				if(object == null || "".equals(object)) {
					return new ResourceModel("overview_filter_resaction_all").getObject();
				}else{
					return (String) new ResourceModel("action_" + ((String) object)).getObject();
				}
			}

			@Override
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
		resactionFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
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

			@Override
			public Object getDisplayValue(String object) {

				if (object == null || object.isEmpty()) {
					return new ResourceModel("overview_filter_resaction_all").getObject();
				} else {
					return new ResourceModel("action_" + object).getObject();
				}
			}

			@Override
			public String getIdValue(String object, int index) {

				if (object == null || object.isEmpty()) {
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
		lessonActionFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
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
		if (reportPanel != null) {
			reportPanel.add(AttributeModifier.replace("endpoint", getWidgetEndpoint()));
			target.add(reportPanel);
		}
		target.appendJavaScript("setMainFrameHeightNoScroll(window.name, 0, 300);");
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

	private String getWidgetEndpoint() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setPageSize(MAX_TABLE_ROWS);
		request.setDate(getDateFilter());
		request.setRole(getRoleFilter());
		request.setTool(getToolFilter());
		request.setResourceAction(getResactionFilter());
		request.setLessonAction(getLessonActionFilter());
		return SiteStatsApiUrls.widgetReport(siteId, widgetId, tabId, request);
	}
	
}
