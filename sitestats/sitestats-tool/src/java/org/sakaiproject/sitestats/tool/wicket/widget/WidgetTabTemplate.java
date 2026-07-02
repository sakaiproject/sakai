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

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_LESSON_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_RESOURCE_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_TOOL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsFilterOption;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.IndicatingAjaxDropDownChoice;
import org.sakaiproject.sitestats.tool.wicket.pages.ReportDataPage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WidgetTabTemplate extends Panel {
	private static final long serialVersionUID = 1L;
	public final static int MAX_TABLE_ROWS = 5;

	private static final String FILTER_DATE_ID = FILTER_DATE;
	private static final String FILTER_ROLE_ID = FILTER_ROLE;
	private static final String FILTER_TOOL_ID = FILTER_TOOL;
	private static final String FILTER_RESOURCE_ACTION_ID = FILTER_RESOURCE_ACTION;
	private static final String FILTER_LESSON_ACTION_ID = FILTER_LESSON_ACTION;

	private WebMarkupContainer reportPanel = null;
	private Link tableLink = null;

	private String siteId = null;
	private String widgetId = null;
	private String tabId = null;

	private String dateFilter = ReportManager.WHEN_LAST7DAYS;
	private String roleFilter = ReportManager.WHO_ALL;
	private String toolFilter = ReportManager.WHAT_EVENTS_ALLTOOLS;
	private String resactionFilter = null;
	private String lessonActionFilter = null;

	public WidgetTabTemplate(String id, String siteId, String widgetId, String tabId) {
		super(id);
		this.siteId = siteId;
		this.widgetId = widgetId;
		this.tabId = tabId;
	}

	public void setLessonActionFilter(String lessonActionFilter) {
		this.lessonActionFilter = StringUtils.trimToNull(lessonActionFilter);
	}

	public String getLessonActionFilter() {
		return lessonActionFilter;
	}

	protected Optional<IModel<String>> getFooterMsg() {
		String localSakaiName = Locator.getFacade().getStatsManager().getLocalSakaiName();
		StringResourceModel model = new StringResourceModel("widget_server_time_msg").setParameters(localSakaiName);
		return Optional.of(model);
	}

	@Override
	protected void onBeforeRender() {
		setDefaultModel(new CompoundPropertyModel(this));
		removeAll();
		renderFilters();
		renderReportPanel();

		Optional<IModel<String>> footerModel = getFooterMsg();
		add(new Label("widgetFooterMsg", footerModel.orElseGet(() -> Model.of(""))).setVisible(footerModel.isPresent()));

		super.onBeforeRender();
	}

	private void renderReportPanel() {
		reportPanel = new WebMarkupContainer("reportPanel");
		reportPanel.setOutputMarkupId(true);
		reportPanel.add(AttributeModifier.replace("endpoint", getWidgetEndpoint()));
		add(reportPanel);

		tableLink = new StatelessLink("link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				PageParameters params = new PageParameters().set("siteId", siteId);
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
				setResponsePage(new ReportDataPage(null, params, getWebPage()));
			}
		};
		tableLink.setOutputMarkupId(true);
		add(tableLink);
	}

	private void renderFilters() {
		SiteStatsWidgetTab tabMetadata = loadWidgetTabMetadata();
		List<SiteStatsFilter> filters = tabMetadata.getFilters() == null ? Collections.<SiteStatsFilter>emptyList() : tabMetadata.getFilters();

		addFilter("dateFilter", FILTER_DATE_ID, filters);
		addFilter("roleFilter", FILTER_ROLE_ID, filters);
		addFilter("toolFilter", FILTER_TOOL_ID, filters);
		addFilter("resactionFilter", FILTER_RESOURCE_ACTION_ID, filters);
		addFilter("lessonActionFilter", FILTER_LESSON_ACTION_ID, filters);
	}

	private void addFilter(String wicketId, String filterId, List<SiteStatsFilter> filters) {
		Optional<SiteStatsFilter> filter = findFilter(filters, filterId);
		IndicatingAjaxDropDownChoice<String> dropdown = new IndicatingAjaxDropDownChoice<String>(wicketId,
				filterOptions(filter), filterRenderer(filter)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected CharSequence getDefaultChoice(String selected) {
				if (FILTER_RESOURCE_ACTION_ID.equals(filterId) || FILTER_LESSON_ACTION_ID.equals(filterId)) {
					return "";
				}
				return super.getDefaultChoice(selected);
			}
		};
		dropdown.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				updateData(target);
			}
		});
		dropdown.setOutputMarkupId(true);
		dropdown.setVisible(filter.isPresent());
		add(dropdown);
	}

	private SiteStatsWidgetTab loadWidgetTabMetadata() {
		try {
			return Locator.getFacade().getSiteStatsViewService().getWidgetTab(siteId, widgetId, tabId);
		} catch (RuntimeException e) {
			log.warn("Unable to load SiteStats widget metadata for {}/{} in {}: {}", widgetId, tabId, siteId, e.getMessage());
			throw e;
		}
	}

	private Optional<SiteStatsFilter> findFilter(List<SiteStatsFilter> filters, String filterId) {
		for (SiteStatsFilter filter : filters) {
			if (filterId.equals(filter.getId())) {
				return Optional.of(filter);
			}
		}
		return Optional.empty();
	}

	private List<String> filterOptions(Optional<SiteStatsFilter> filter) {
		if (!filter.isPresent() || filter.get().getOptions() == null) {
			return Collections.emptyList();
		}
		List<String> options = new ArrayList<String>();
		for (SiteStatsFilterOption option : filter.get().getOptions()) {
			options.add(StringUtils.defaultString(option.getValue()));
		}
		return options;
	}

	private IChoiceRenderer<String> filterRenderer(Optional<SiteStatsFilter> filter) {
		Map<String, String> labels = filterOptionLabels(filter);
		return new IChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String object) {
				String value = StringUtils.defaultString(object);
				return StringUtils.defaultIfBlank(labels.get(value), value);
			}

			@Override
			public String getIdValue(String object, int index) {
				return StringUtils.defaultString(object);
			}
		};
	}

	private Map<String, String> filterOptionLabels(Optional<SiteStatsFilter> filter) {
		if (!filter.isPresent() || filter.get().getOptions() == null) {
			return Collections.emptyMap();
		}
		Map<String, String> labels = new LinkedHashMap<String, String>();
		for (SiteStatsFilterOption option : filter.get().getOptions()) {
			labels.put(StringUtils.defaultString(option.getValue()), option.getLabel());
		}
		return labels;
	}

	private void updateData(AjaxRequestTarget target) {
		if (reportPanel != null) {
			reportPanel.add(AttributeModifier.replace("endpoint", getWidgetEndpoint()));
			target.add(reportPanel);
		}
		target.appendJavaScript("setMainFrameHeightNoScroll(window.name, 0, 300);");
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
		if ("".equals(resactionFilter)) {
			this.resactionFilter = null;
		} else {
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
