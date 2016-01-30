package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.tool.gradebook.Gradebook;

public class CourseGradeColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public CourseGradeColumnHeaderPanel(final String id) {
		super(id);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new Label("title", new ResourceModel("column.header.coursegrade")));

		final Gradebook gradebook = this.businessService.getGradebook();
		final GradebookPage gradebookPage = (GradebookPage) getPage();

		add(gradebookPage.buildFlagWithPopover("isReleasedFlag", getString("label.coursegrade.released"))
				.setVisible(gradebook.isCourseGradeDisplayed()));
		add(gradebookPage.buildFlagWithPopover("notReleasedFlag", getString("label.coursegrade.notreleased"))
				.setVisible(!gradebook.isCourseGradeDisplayed()));

		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu");

		menu.add(new AjaxLink<Void>("courseGradeOverride") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getAddOrEditGradeItemWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new EmptyPanel(window.getContentId()));
				window.showUnloadConfirmation(false);
				window.show(target);
			}

		});

		menu.add(new AjaxLink<Void>("courseGradeOverrideLog") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getAddOrEditGradeItemWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new EmptyPanel(window.getContentId()));
				window.showUnloadConfirmation(false);
				window.show(target);
			}

		});

		menu.add(new AjaxLink<Void>("setUngraded") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getAddOrEditGradeItemWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new EmptyPanel(window.getContentId()));
				window.showUnloadConfirmation(false);
				window.show(target);
			}

		});

		add(menu);
	}

	/**
	 * Get the parent cell
	 *
	 * @param component
	 * @return
	 */
	private Component getParentCellFor(final Component component) {
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), "header")) {
			return component;
		} else {
			return getParentCellFor(component.getParent());
		}
	}
}