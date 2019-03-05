/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.gradebookng.tool.model.GbSettings;

public class SettingsStatisticsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final IModel<GbSettings> model;

	private boolean expanded;

	public SettingsStatisticsPanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final WebMarkupContainer settingsStatisticsPanel = new WebMarkupContainer("settingsStatisticsPanel");
		// Preserve the expand/collapse state of the panel
		settingsStatisticsPanel.add(new AjaxEventBehavior("shown.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsStatisticsPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
				SettingsStatisticsPanel.this.expanded = true;
			}
		});
		settingsStatisticsPanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsStatisticsPanel.add(new AttributeModifier("class", "panel-collapse collapse"));
				SettingsStatisticsPanel.this.expanded = false;
			}
		});
		if (this.expanded) {
			settingsStatisticsPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
		}
		add(settingsStatisticsPanel);

		// display assignment stats to students
		final AjaxCheckBox displayAssignmentStats = new AjaxCheckBox("displayAssignmentStats",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.assignmentStatsDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		displayAssignmentStats.setOutputMarkupId(true);
		settingsStatisticsPanel.add(displayAssignmentStats);

		// display course grade stats to students
		final AjaxCheckBox displayCourseGradeStats = new AjaxCheckBox("displayCourseGradeStats",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.courseGradeStatsDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		displayCourseGradeStats.setOutputMarkupId(true);
		settingsStatisticsPanel.add(displayCourseGradeStats);

	}

	public boolean isExpanded() {
		return this.expanded;
	}


}
