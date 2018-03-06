/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

public class SettingsGradeReleasePanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	IModel<GbSettings> model;

	Label preview;
	Label minimumOptions;

	private boolean expanded;

	AjaxCheckBox points;
	AjaxCheckBox letterGrade;
	AjaxCheckBox percentage;

	WebMarkupContainer courseGradeType;

	public SettingsGradeReleasePanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final SettingsPage settingsPage = (SettingsPage) getPage();

		final WebMarkupContainer settingsGradeReleasePanel = new WebMarkupContainer("settingsGradeReleasePanel");
		// Preserve the expand/collapse state of the panel
		settingsGradeReleasePanel.add(new AjaxEventBehavior("shown.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradeReleasePanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
				SettingsGradeReleasePanel.this.expanded = true;
			}
		});
		settingsGradeReleasePanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradeReleasePanel.add(new AttributeModifier("class", "panel-collapse collapse"));
				SettingsGradeReleasePanel.this.expanded = false;
			}
		});
		if (this.expanded) {
			settingsGradeReleasePanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
		}
		add(settingsGradeReleasePanel);

		// display released items to students
		final AjaxCheckBox displayReleased = new AjaxCheckBox("displayReleased",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.displayReleasedGradeItemsToStudents")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		displayReleased.setOutputMarkupId(true);
		settingsGradeReleasePanel.add(displayReleased);

		// display course grade
		final AjaxCheckBox displayCourseGrade = new AjaxCheckBox("displayCourseGrade",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.courseGradeDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// update preview
				if (SettingsGradeReleasePanel.this.preview.isVisibleInHierarchy()) {
					target.add(SettingsGradeReleasePanel.this.preview);
				}

				// toggle courseGradeType panel
				target.add(SettingsGradeReleasePanel.this.courseGradeType);

				// if we have disabled this, enable categories and weighting
				// if its enabled then catgories and weighting may or may not be enabled depending on other rules
				final Radio<Integer> categoriesAndWeightingRadio = settingsPage.getSettingsCategoryPanel().getCategoriesAndWeightingRadio();
				settingsPage.getSettingsCategoryPanel().updateCategoriesAndWeightingRadioState();
				target.add(categoriesAndWeightingRadio);

				// disabling this should also uncheck all formatting types
				updatePointsCheckboxState();
				updateLetterGradeCheckboxState();
				updatePercentageCheckboxState();

				target.add(SettingsGradeReleasePanel.this.points);
				target.add(SettingsGradeReleasePanel.this.letterGrade);
				target.add(SettingsGradeReleasePanel.this.percentage);
			}
		};
		displayCourseGrade.setOutputMarkupPlaceholderTag(true);
		settingsGradeReleasePanel.add(displayCourseGrade);

		// course grade type container
		this.courseGradeType = new WebMarkupContainer("courseGradeType") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return displayCourseGrade.getModelObject();
			}

		};
		this.courseGradeType.setOutputMarkupPlaceholderTag(true);
		settingsGradeReleasePanel.add(this.courseGradeType);

		// letter grade
		this.letterGrade = new AjaxCheckBox("letterGrade",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.courseLetterGradeDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// update preview and validation
				target.add(SettingsGradeReleasePanel.this.preview);
				target.add(SettingsGradeReleasePanel.this.minimumOptions);
			}
		};
		this.letterGrade.setOutputMarkupPlaceholderTag(true);
		this.courseGradeType.add(this.letterGrade);

		// percentage
		this.percentage = new AjaxCheckBox("percentage",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.courseAverageDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// update preview and validation
				target.add(SettingsGradeReleasePanel.this.preview);
				target.add(SettingsGradeReleasePanel.this.minimumOptions);
			}
		};
		this.percentage.setOutputMarkupPlaceholderTag(true);
		this.courseGradeType.add(this.percentage);

		// points
		this.points = new AjaxCheckBox("points",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.coursePointsDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// update preview and validation
				target.add(SettingsGradeReleasePanel.this.preview);
				target.add(SettingsGradeReleasePanel.this.minimumOptions);

				// if points selected, disable categories and weighting
				final Radio<Integer> categoriesAndWeightingRadio = settingsPage.getSettingsCategoryPanel().getCategoriesAndWeightingRadio();
				settingsPage.getSettingsCategoryPanel().updateCategoriesAndWeightingRadioState();
				target.add(categoriesAndWeightingRadio);
			}
		};
		this.points.setOutputMarkupPlaceholderTag(true);
		this.courseGradeType.add(this.points);

		// minimum options label. only shows if we have too few selected
		this.minimumOptions = new Label("minimumOptions", new ResourceModel("settingspage.displaycoursegrade.notenough")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				final GradebookInformation settings = SettingsGradeReleasePanel.this.model.getObject().getGradebookInformation();

				// validation label
				if (settings.isCourseGradeDisplayed()) {
					int displayOptions = 0;
					if (settings.isCourseLetterGradeDisplayed()) {
						displayOptions++;
					}
					if (settings.isCourseAverageDisplayed()) {
						displayOptions++;
					}
					if (settings.isCoursePointsDisplayed()) {
						displayOptions++;
					}
					if (displayOptions == 0) {
						return true;
					}
				}
				return false;
			}

		};
		this.minimumOptions.setOutputMarkupPlaceholderTag(true);
		this.courseGradeType.add(this.minimumOptions);

		// preview model, uses settings to determine out what to display
		final Model<String> previewModel = new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {

				final List<String> parts = new ArrayList<>();

				final GradebookInformation settings = SettingsGradeReleasePanel.this.model.getObject().getGradebookInformation();

				if (settings.isCourseLetterGradeDisplayed()) {
					parts.add(getString("settingspage.displaycoursegrade.preview-letter"));
				}

				if (settings.isCourseAverageDisplayed()) {
					if (parts.isEmpty()) {
						parts.add(getString("settingspage.displaycoursegrade.preview-percentage-first"));
					} else {
						parts.add(getString("settingspage.displaycoursegrade.preview-percentage-second"));
					}
				}

				if (settings.isCoursePointsDisplayed()) {
					if (parts.isEmpty()) {
						parts.add(getString("settingspage.displaycoursegrade.preview-points-first"));
					} else {
						parts.add(getString("settingspage.displaycoursegrade.preview-points-second"));
					}
				}

				if (parts.isEmpty()) {
					parts.add(getString("settingspage.displaycoursegrade.preview-none"));
				}

				return StringUtils.join(parts, " ");
			}
		};

		// course grade type container
		final WebMarkupContainer courseGradePreview = new WebMarkupContainer("courseGradePreview");
		courseGradePreview.setVisible(displayCourseGrade.getModelObject());
		courseGradePreview.setOutputMarkupPlaceholderTag(true);
		settingsGradeReleasePanel.add(courseGradePreview);

		// preview
		this.preview = new Label("preview", previewModel);
		this.preview.setOutputMarkupId(true);
		courseGradePreview.add(this.preview);

		// behaviour for when the 'display course grade' checkbox is changed
		displayCourseGrade.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				final boolean checked = displayCourseGrade.getModelObject();
				SettingsGradeReleasePanel.this.courseGradeType.setVisible(checked);
				courseGradePreview.setVisible(checked);
				target.add(SettingsGradeReleasePanel.this.courseGradeType);
				target.add(courseGradePreview);

			}
		});

		// if weighted category, disable points
		updatePointsCheckboxState();
	}

	public boolean isExpanded() {
		return this.expanded;
	}

	// to enable inter panel comms
	AjaxCheckBox getPointsCheckBox() {
		return this.points;
	}

	// helper to apply the rules for whether the points checkbox should be enabled
	// runs via data from the model
	void updatePointsCheckboxState() {

		final GradebookInformation settings = this.model.getObject().getGradebookInformation();
		final GbCategoryType type = GbCategoryType.valueOf(settings.getCategoryType());

		// if categories and weighting, disable course grade points
		if (settings.isCourseGradeDisplayed()) {
			if (type == GbCategoryType.WEIGHTED_CATEGORY) {
				this.points.setEnabled(false);
			} else {
				this.points.setEnabled(true);
			}
		}

		// if course grade disabled, clear this field
		if (!settings.isCourseGradeDisplayed()) {
			this.points.setDefaultModelObject(Boolean.FALSE);
		}
	}

	void updateLetterGradeCheckboxState() {
		final GradebookInformation settings = this.model.getObject().getGradebookInformation();

		// if course grade disabled, clear this field
		if (!settings.isCourseGradeDisplayed()) {
			this.letterGrade.setDefaultModelObject(Boolean.FALSE);
		}
	}

	void updatePercentageCheckboxState() {
		final GradebookInformation settings = this.model.getObject().getGradebookInformation();

		// if course grade disabled, clear this field
		if (!settings.isCourseGradeDisplayed()) {
			this.percentage.setDefaultModelObject(Boolean.FALSE);
		}
	}

}
