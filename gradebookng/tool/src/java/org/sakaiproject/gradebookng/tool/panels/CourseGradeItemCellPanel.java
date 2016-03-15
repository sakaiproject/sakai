package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.component.GbCourseGradeLabel;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

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
		final String studentUuid = (String) modelData.get("studentUuid");

		// the model map contains a lot of additional info we need for the course grade label, this is passed through

		final GradebookPage gradebookPage = (GradebookPage) getPage();

		// course grade label
		add(new GbCourseGradeLabel("courseGrade", Model.ofMap(modelData)));

		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu");
		menu.add(new AjaxLink<String>("courseGradeOverride", Model.of(studentUuid)) {
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
		menu.add(new AjaxLink<String>("courseGradeOverrideLog", Model.of(studentUuid)) {
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

	private Component getParentCellFor(final Component component) {
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), "cells")) {
			return component;
		} else {
			return getParentCellFor(component.getParent());
		}
	}

}