package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

/**
 * Panel that is rendered for each student's course grade
 */
public class CourseGradeItemCellPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

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
		final Map<String, Object> modelData = this.model.getObject();
		final String courseGrade = (String) modelData.get("courseGrade");
		final String studentUuid = (String) modelData.get("studentUuid");

		final GradebookPage gradebookPage = (GradebookPage) getPage();

		// label
		final Label courseGradeLabel = new Label("courseGrade", Model.of(courseGrade)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(final IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof ScoreChangedEvent) {
					final ScoreChangedEvent scoreChangedEvent = (ScoreChangedEvent) event.getPayload();
					if (studentUuid.equals(scoreChangedEvent.getStudentUuid())) {
						final CourseGrade updatedCourseGrade = CourseGradeItemCellPanel.this.businessService
								.getCourseGrade(scoreChangedEvent.getStudentUuid());
						((Model<String>) getDefaultModel()).setObject(updatedCourseGrade.getMappedGrade());

						scoreChangedEvent.getTarget().add(this);
						scoreChangedEvent.getTarget().appendJavaScript(
								String.format("$('#%s').closest('td').addClass('gb-score-dynamically-updated');",
										this.getMarkupId()));
					}
				}
			}
		};
		courseGradeLabel.setOutputMarkupId(true);
		add(courseGradeLabel);

		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu");
		menu.add(new AjaxLink<String>("courseGradeOverride", Model.of(studentUuid)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getAddOrEditGradeItemWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));

				// pass in getModel() here
				window.setContent(new EmptyPanel(window.getContentId()));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		menu.add(new AjaxLink<String>("courseGradeOverrideLog", Model.of(studentUuid)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getAddOrEditGradeItemWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));

				// pass in getModel() here
				window.setContent(new EmptyPanel(window.getContentId()));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		add(menu);
	}

	private Component getParentCellFor(final Component component) {
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), "cells")) {
			return component;
		} else {
			return getParentCellFor(component.getParent());
		}
	}
}