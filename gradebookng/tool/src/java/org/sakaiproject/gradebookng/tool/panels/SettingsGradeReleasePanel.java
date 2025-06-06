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
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
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
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradingConstants;

public class SettingsGradeReleasePanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	IModel<GbSettings> model;

	Label preview;
	Label minimumOptions;

	private boolean expanded;

	AjaxCheckBox points;
	AjaxCheckBox letterGrade;
	AjaxCheckBox percentage;

	WebMarkupContainer displayCourseGradeContainer;
	WebMarkupContainer allowStudentsToCompareGradesContainer;
	WebMarkupContainer courseGradeType;
	WebMarkupContainer allowStudentsToCompareGradesOptionsContainer;

	public SettingsGradeReleasePanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final SettingsPage settingsPage = (SettingsPage) getPage();

		final WebMarkupContainer settingsGradeAccordionButton = new WebMarkupContainer("settingsGradeAccordionButton");
		final WebMarkupContainer settingsGradeReleasePanel = new WebMarkupContainer("settingsGradeReleasePanel");

		// Set up accordion behavior
		setupAccordionBehavior(settingsGradeAccordionButton, settingsGradeReleasePanel, this.expanded, 
			new AccordionStateUpdater() {
				@Override
				public void updateState(boolean newState) {
					SettingsGradeReleasePanel.this.expanded = newState;
				}
				
				@Override
				public boolean getState() {
					return SettingsGradeReleasePanel.this.expanded;
				}
			});
		
		add(settingsGradeReleasePanel);
		add(settingsGradeAccordionButton);

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

		// display course grade to students container
		this.displayCourseGradeContainer = new WebMarkupContainer("displayCourseGradeContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return serverConfigService.getBoolean(SAK_PROP_SHOW_COURSE_GRADE_STUDENT, SAK_PROP_SHOW_COURSE_GRADE_STUDENT_DEFAULT);
			}

		};
		this.displayCourseGradeContainer.setOutputMarkupPlaceholderTag(true);
		settingsGradeReleasePanel.add(this.displayCourseGradeContainer);

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
		displayCourseGradeContainer.add(displayCourseGrade);

		// display allow students to compare grades container
		this.allowStudentsToCompareGradesContainer = new WebMarkupContainer("allowStudentsToCompareGradesContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return serverConfigService.getBoolean(SAK_PROP_ALLOW_COMPARE_GRADES, SAK_PROP_ALLOW_COMPARE_GRADES_DEFAULT);
			}

		};
		this.allowStudentsToCompareGradesContainer.setOutputMarkupPlaceholderTag(true);
		settingsGradeReleasePanel.add(this.allowStudentsToCompareGradesContainer);
		
		// display allow students to compare grades
		final AjaxCheckBox allowStudentsToCompareGrades = new AjaxCheckBox("allowStudentsToCompareGrades",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.allowStudentsToCompareGrades")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				target.add(SettingsGradeReleasePanel.this.allowStudentsToCompareGradesOptionsContainer);
			}
		};
		allowStudentsToCompareGrades.setOutputMarkupPlaceholderTag(true);
		allowStudentsToCompareGradesContainer.add(allowStudentsToCompareGrades);
		
		
		// allow students to compare grades container
		this.allowStudentsToCompareGradesOptionsContainer = new WebMarkupContainer("allowStudentsToCompareGradesOptionsContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return allowStudentsToCompareGrades.getModelObject();
			}

		};
		this.allowStudentsToCompareGradesOptionsContainer.setOutputMarkupPlaceholderTag(true);
		settingsGradeReleasePanel.add(this.allowStudentsToCompareGradesOptionsContainer);


		// display student names when comparing grades with classmates
		final AjaxCheckBox comparingDisplayStudentNames = new AjaxCheckBox("comparingDisplayStudentNames",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.comparingDisplayStudentNames")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		comparingDisplayStudentNames.setOutputMarkupId(true);
		allowStudentsToCompareGradesOptionsContainer.add(comparingDisplayStudentNames);

		// display student surnames when comparing grades with classmates
		final AjaxCheckBox comparingDisplayStudentSurnames = new AjaxCheckBox("comparingDisplayStudentSurnames",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.comparingDisplayStudentSurnames")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		comparingDisplayStudentSurnames.setOutputMarkupId(true);
		allowStudentsToCompareGradesOptionsContainer.add(comparingDisplayStudentSurnames);

		final AjaxCheckBox comparingDisplayTeacherComments = new AjaxCheckBox("comparingDisplayTeacherComments",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.comparingDisplayTeacherComments")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		comparingDisplayTeacherComments.setOutputMarkupId(true);
		allowStudentsToCompareGradesOptionsContainer.add(comparingDisplayTeacherComments);

		// include grades that doesn't count when comparing grades with classmates
		final AjaxCheckBox comparingIncludeAllGrades = new AjaxCheckBox("comparingIncludeAllGrades",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.comparingIncludeAllGrades")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		comparingIncludeAllGrades.setOutputMarkupId(true);
		allowStudentsToCompareGradesOptionsContainer.add(comparingIncludeAllGrades);

		// randomize diplayed data order when comparing grades with classmates
		final AjaxCheckBox comparingRandomizeDisplayedData = new AjaxCheckBox("comparingRandomizeDisplayedData",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.comparingRandomizeDisplayedData")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		comparingRandomizeDisplayedData.setOutputMarkupId(true);
		allowStudentsToCompareGradesOptionsContainer.add(comparingRandomizeDisplayedData);


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
				if (settings.getCourseGradeDisplayed()) {
					int displayOptions = 0;
					if (settings.getCourseLetterGradeDisplayed()) {
						displayOptions++;
					}
					if (settings.getCourseAverageDisplayed()) {
						displayOptions++;
					}
					if (settings.getCoursePointsDisplayed()) {
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

				if (settings.getCourseLetterGradeDisplayed()) {
					parts.add(getString("settingspage.displaycoursegrade.preview-letter"));
				}

				if (settings.getCourseAverageDisplayed()) {
					if (parts.isEmpty()) {
						parts.add(getString("settingspage.displaycoursegrade.preview-percentage-first"));
					} else {
						parts.add(getString("settingspage.displaycoursegrade.preview-percentage-second"));
					}
				}

				if (settings.getCoursePointsDisplayed()) {
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
		displayCourseGrade.add(new AjaxFormComponentUpdatingBehavior("change") {
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
		final Integer type = settings.getCategoryType();

		// if categories and weighting, disable course grade points
		if (settings.getCourseGradeDisplayed()) {
            this.points.setEnabled(!Objects.equals(type, GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY));
		}

		// if course grade disabled, clear this field
		if (!settings.getCourseGradeDisplayed()) {
			this.points.setDefaultModelObject(Boolean.FALSE);
		}
	}

	void updateLetterGradeCheckboxState() {
		final GradebookInformation settings = this.model.getObject().getGradebookInformation();

		// if course grade disabled, clear this field
		if (!settings.getCourseGradeDisplayed()) {
			this.letterGrade.setDefaultModelObject(Boolean.FALSE);
		}
	}

	void updatePercentageCheckboxState() {
		final GradebookInformation settings = this.model.getObject().getGradebookInformation();

		// if course grade disabled, clear this field
		if (!settings.getCourseGradeDisplayed()) {
			this.percentage.setDefaultModelObject(Boolean.FALSE);
		}
	}

}
