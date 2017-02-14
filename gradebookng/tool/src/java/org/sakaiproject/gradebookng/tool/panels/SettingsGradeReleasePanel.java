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
				target.add(SettingsGradeReleasePanel.this.preview);
			}
		};
		displayCourseGrade.setOutputMarkupId(true);
		settingsGradeReleasePanel.add(displayCourseGrade);

		// course grade type container
		final WebMarkupContainer courseGradeType = new WebMarkupContainer("courseGradeType") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return displayCourseGrade.getModelObject();
			}

		};
		courseGradeType.setOutputMarkupPlaceholderTag(true);
		settingsGradeReleasePanel.add(courseGradeType);

		// letter grade
		final AjaxCheckBox letterGrade = new AjaxCheckBox("letterGrade",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.courseLetterGradeDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// update preview and validation
				target.add(SettingsGradeReleasePanel.this.preview);
				target.add(SettingsGradeReleasePanel.this.minimumOptions);
			}
		};
		letterGrade.setOutputMarkupId(true);
		courseGradeType.add(letterGrade);

		// percentage
		final AjaxCheckBox percentage = new AjaxCheckBox("percentage",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.courseAverageDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// update preview and validation
				target.add(SettingsGradeReleasePanel.this.preview);
				target.add(SettingsGradeReleasePanel.this.minimumOptions);
			}
		};
		percentage.setOutputMarkupId(true);
		courseGradeType.add(percentage);

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
				final GradebookInformation settings = SettingsGradeReleasePanel.this.model.getObject().getGradebookInformation();

				final Radio<Integer> categoriesAndWeightingRadio = settingsPage.getSettingsCategoryPanel().getCategoriesAndWeightingRadio();
				if (settings.isCoursePointsDisplayed()) {
					categoriesAndWeightingRadio.setEnabled(false);
				} else {
					categoriesAndWeightingRadio.setEnabled(true);
				}
				target.add(categoriesAndWeightingRadio);

			}
		};
		this.points.setOutputMarkupId(true);
		courseGradeType.add(this.points);

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
		courseGradeType.add(this.minimumOptions);

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
		final WebMarkupContainer courseGradePreview = new WebMarkupContainer("courseGradePreview") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return displayCourseGrade.getModelObject();
			}

		};
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
				courseGradeType.setVisible(checked);
				courseGradePreview.setVisible(checked);
				target.add(courseGradeType);
				target.add(courseGradePreview);

			}
		});

		// if weighted category, disable points
		final GbCategoryType type = GbCategoryType.valueOf(this.model.getObject().getGradebookInformation().getCategoryType());
		if (type == GbCategoryType.WEIGHTED_CATEGORY) {
			this.points.setEnabled(false);
		}

	}

	public boolean isExpanded() {
		return this.expanded;
	}

	// to enable inter panel comms
	AjaxCheckBox getPointsCheckBox() {
		return this.points;
	}

}
