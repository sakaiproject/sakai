package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Panel that is rendered for each student's course grade
 */
public class CourseGradeItemCellPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	IModel<Map<String, Object>> model;

	public CourseGradeItemCellPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		getParentCellFor(this).setOutputMarkupId(true);

		// unpack model

		// Available options:
		// courseGradeDisplay
		// studentUuid
		// currentUserUuid
		// currentUserRole
		// gradebook
		// showPoints
		// showOverride
		// courseGradeVisible
		final Map<String, Object> modelData = this.model.getObject();
		final String courseGradeDisplay = (String) modelData.get("courseGradeDisplay");
		final String studentUuid = (String) modelData.get("studentUuid");

		final GbRole role = (GbRole) modelData.get("currentUserRole");
		final Gradebook gradebook = (Gradebook) modelData.get("gradebook");
		final boolean courseGradeVisible = (boolean) modelData.get("courseGradeVisible");
		final boolean showPoints = (boolean) modelData.get("showPoints");
		final boolean showOverride = (boolean) modelData.get("showOverride");

		final boolean hasCourseGradeOverride = (boolean) modelData.get("hasCourseGradeOverride");

		if (hasCourseGradeOverride) {
			getParentCellFor(this).add(new AttributeAppender("class", " gb-cg-override"));
		}

		// the model map contains a lot of additional info we need for the course grade label, this is passed through

		final GradebookPage gradebookPage = (GradebookPage) getPage();

		// course grade label
		final Label courseGradeLabel = new Label("courseGrade", Model.of(courseGradeDisplay)) {

			@Override
			public void onEvent(final IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof ScoreChangedEvent) {
					final ScoreChangedEvent scoreChangedEvent = (ScoreChangedEvent) event.getPayload();

					// ensure event is for this student (which may not be applicable if we have no student)
					// TODO is this check ever not satisfied now that this has been refactroed?
					if (StringUtils.equals(studentUuid, scoreChangedEvent.getStudentUuid())) {

						final String newCourseGradeDisplay = refreshCourseGrade(studentUuid, gradebook, role, courseGradeVisible,
								showPoints, showOverride);

						// if course grade has changed, then refresh it
						if (!newCourseGradeDisplay.equals(getDefaultModelObject())) {
							setDefaultModel(Model.of(newCourseGradeDisplay));

							scoreChangedEvent.getTarget().add(this);
							scoreChangedEvent.getTarget().appendJavaScript(
								String.format("$('#%s').closest('td').addClass('gb-score-dynamically-updated');", this.getMarkupId()));
						}
					}
				}
			}

		};
		courseGradeLabel.setOutputMarkupId(true);
		courseGradeLabel.setEscapeModelStrings(false);
		add(courseGradeLabel);

		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return role == GbRole.INSTRUCTOR;
			}
		};
		menu.add(new GbAjaxLink("courseGradeOverride", Model.of(studentUuid)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getUpdateCourseGradeDisplayWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new CourseGradeOverridePanel(window.getContentId(), getModel(), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		menu.add(new GbAjaxLink<String>("courseGradeOverrideLog", Model.of(studentUuid)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getUpdateCourseGradeDisplayWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new CourseGradeOverrideLogPanel(window.getContentId(), getModel(), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		add(menu);
	}

	/**
	 * Helper to get the parent cell for the given component TODO move this and all other instances of the same method to a common helper
	 * class
	 *
	 * @param component
	 * @return
	 */
	private Component getParentCellFor(final Component component) {
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), "cells")) {
			return component;
		} else {
			return getParentCellFor(component.getParent());
		}
	}

	/**
	 *
	 * Helper to refresh the course grade
	 *
	 * @param studentUuid
	 * @param gradebook
	 * @param role
	 * @param courseGradeVisible
	 * @param showPoints
	 * @param showOverride
	 * @return
	 */
	private String refreshCourseGrade(final String studentUuid, final Gradebook gradebook, final GbRole role,
			final boolean courseGradeVisible, final boolean showPoints, final boolean showOverride) {

		final CourseGradeFormatter courseGradeFormatter = new CourseGradeFormatter(
				gradebook,
				role,
				courseGradeVisible,
				showPoints,
				showOverride);

		final CourseGrade courseGrade = this.businessService.getCourseGrade(studentUuid);

		return courseGradeFormatter.format(courseGrade);
	}

}