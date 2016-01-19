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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

public class SettingsGradeReleasePanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<GbSettings> model;

	Label preview;

	public SettingsGradeReleasePanel(final String id, final IModel<GbSettings> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final WebMarkupContainer settingsGradeReleasePanel = new WebMarkupContainer("settingsGradeReleasePanel");
		// Preserve the expand/collapse state of the panel
		settingsGradeReleasePanel.add(new AjaxEventBehavior("shown.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradeReleasePanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
			}
		});
		settingsGradeReleasePanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradeReleasePanel.add(new AttributeModifier("class", "panel-collapse collapse"));
			}
		});
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
				// update preview
				target.add(SettingsGradeReleasePanel.this.preview);
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
				// update preview
				target.add(SettingsGradeReleasePanel.this.preview);
			}
		};
		percentage.setOutputMarkupId(true);
		courseGradeType.add(percentage);

		// points
		final AjaxCheckBox points = new AjaxCheckBox("points",
				new PropertyModel<Boolean>(this.model, "gradebookInformation.coursePointsDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// update preview
				target.add(SettingsGradeReleasePanel.this.preview);
			}
		};
		points.setOutputMarkupId(true);
		courseGradeType.add(points);

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

	}
}
