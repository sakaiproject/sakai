/**
 * Copyright (c) 2006-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.pages;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.UserId;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.util.Tools;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.components.useractivity.UserTrackingResultsPanel;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableDisplayUserListModel;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableDisplayUserListModel.DisplayUser;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableToolIdListModel;
import org.sakaiproject.wicket.component.SakaiAjaxButton;
import org.sakaiproject.wicket.component.SakaiDateTimeField;

/**
 * Page for the User Activity feature
 * @author plukasew
 */
public class UserActivityPage extends BasePage
{
	private static final long serialVersionUID = 1L;
	private final String siteId;
	private String user = ReportManager.WHO_NONE;  // cannot be made final, though your IDE may tell you so; is bound to the user filter drop down choice
	private String tool = ReportManager.WHAT_EVENTS_ALLTOOLS;	// cannot be made final, though your IDE may tell you so; is bound to the tool filter drop down choice
	private ZonedDateTime startDate, endDate;
	private Button searchButton;
	private WebMarkupContainer lastJobRunContainer;
	private DisplayUser displayUser = DisplayUser.NONE; // cannot be made final, though your IDE may tell you so; is bound to the user filter drop down choice

	public UserActivityPage()
	{
		this(new PageParameters());
	}

	public UserActivityPage(final PageParameters params)
	{
		siteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		if (!Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStats(siteId))
		{
			setResponsePage(NotAuthorizedPage.class);
		}
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		Form form = new Form("form");
		add(form);
		add(new Menus("menu", siteId));

		lastJobRunContainer = new WebMarkupContainer("lastJobRunContainer");
		lastJobRunContainer.setOutputMarkupId(true);
		add(lastJobRunContainer);
		lastJobRunContainer.add(new LastJobRun("lastJobRun", siteId));

		IChoiceRenderer<DisplayUser> userChoiceRenderer = new ChoiceRenderer<DisplayUser>()
		{
			@Override
			public Object getDisplayValue(DisplayUser user)
			{
				// Short circuit if user is blank
				if (StringUtils.isBlank(user.userId))
				{
					return new ResourceModel("user_unknown").getObject();
				}

				// String representation of 'select user' option
				if (ReportManager.WHO_NONE.equals(user.userId))
				{
					return new ResourceModel("de_select_user").getObject();
				}

				return user.display;
			}

			@Override
			public String getIdValue(DisplayUser user, int index)
			{
				return user.userId;
			}
		};

		DropDownChoice<DisplayUser> userFilter = new DropDownChoice<>("userFilter", new PropertyModel<>(this, "displayUser"),
				new LoadableDisplayUserListModel(siteId), userChoiceRenderer);
		userFilter.add(new AjaxFormComponentUpdatingBehavior("change")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				if (ReportManager.WHO_NONE.equals(displayUser.userId))
				{
					searchButton.setEnabled(false);
					target.add(searchButton);
				}
				else
				{
					searchButton.setEnabled(true);
					target.add(searchButton);
				}
			}
		});
		userFilter.setLabel(new ResourceModel("de_userFilter"));
		form.add(new SimpleFormComponentLabel("userFilterLabel", userFilter));
		form.add(userFilter);

		IChoiceRenderer<String> toolChoiceRenderer = new ChoiceRenderer<>()
		{
			@Override
			public Object getDisplayValue(String toolId)
			{
				return Locator.getFacade().getEventRegistryService().getToolName(toolId);
			}

			@Override
			public String getIdValue(String toolId, int index)
			{
				return toolId;
			}
		};
		DropDownChoice<String> eventFilterByTool = new DropDownChoice<>("eventFilter",
				new PropertyModel<>(this, "tool"), new LoadableToolIdListModel(siteId), toolChoiceRenderer);
		eventFilterByTool.setLabel(new ResourceModel("de_eventFilter"));
		form.add(new SimpleFormComponentLabel("eventFilterLabel", eventFilterByTool));
		form.add(eventFilterByTool);

		ZoneId tz = Locator.getFacade().getUserTimeService().getLocalTimeZone().toZoneId();
		startDate = ZonedDateTime.now(tz).truncatedTo(ChronoUnit.DAYS);
		SakaiDateTimeField startDateField = new SakaiDateTimeField("startDate", new PropertyModel<>(this, "startDate"), tz);
		startDateField.setAllowEmptyDate(false).setLabel(new ResourceModel("de_dateRangeFrom"));
		form.add(new SimpleFormComponentLabel("startDateLabel", startDateField));
		form.add(startDateField);

		endDate = startDate.plusDays(1);
		SakaiDateTimeField endDateField = new SakaiDateTimeField("endDate", new PropertyModel<>(this, "endDate"), tz);
		endDateField.setAllowEmptyDate(false).setLabel(new ResourceModel("de_dateRangeTo"));
		form.add(new SimpleFormComponentLabel("endDateLabel", endDateField));
		form.add(endDateField);

		String zoneName = startDate.getZone().getDisplayName(TextStyle.FULL, getSession().getLocale());
		StringResourceModel legendModel = new StringResourceModel("de_dateRange").setParameters(zoneName);
		form.add(new Label("dateRangeLegend", legendModel));

		final UserTrackingResultsPanel resultsPanel = new UserTrackingResultsPanel("results", TrackingParams.EMPTY_PARAMS);
		resultsPanel.setOutputMarkupPlaceholderTag(true);
		resultsPanel.setVisible(false);
		add(resultsPanel);

		searchButton = new SakaiAjaxButton("search", form)
		{
			@Override
			public void onSubmit(AjaxRequestTarget target)
			{
				// run search
				PrefsData pd = Locator.getFacade().getStatsManager().getPreferences(siteId, false);
				TrackingParams params = new TrackingParams(siteId, Tools.getEventsForToolFilter(tool, siteId, pd, true),
						Collections.singletonList(displayUser.userId), startDate.toInstant(), endDate.toInstant());
				resultsPanel.setTrackingParams(params);
				resultsPanel.setVisible(true);
				target.add(resultsPanel);
				lastJobRunContainer.replace(new LastJobRun("lastJobRun", siteId));
				target.add(lastJobRunContainer);
				Locator.getFacade().getStatsManager().logEvent(new UserId(user), StatsManager.LOG_ACTION_TRACK, siteId, false);
			}
		};
		searchButton.setOutputMarkupId(true);
		searchButton.setEnabled(false);
		form.add(searchButton);
	}	// onInitialize()
}
