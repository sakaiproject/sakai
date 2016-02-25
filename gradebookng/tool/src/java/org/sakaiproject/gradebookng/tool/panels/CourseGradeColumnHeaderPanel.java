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

		getParentCellFor(this).setOutputMarkupId(true);

		add(new Label("title", new ResourceModel("column.header.coursegrade")));

		final Gradebook gradebook = this.businessService.getGradebook();
		final GradebookPage gradebookPage = (GradebookPage) getPage();

		add(gradebookPage.buildFlagWithPopover("isReleasedFlag",
				new HeaderFlagPopoverPanel("popover", HeaderFlagPopoverPanel.Flag.COURSE_GRADE_RELEASED).toPopoverString())
			.setVisible(gradebook.isCourseGradeDisplayed()));
		add(gradebookPage.buildFlagWithPopover("notReleasedFlag",
				new HeaderFlagPopoverPanel("popover", HeaderFlagPopoverPanel.Flag.COURSE_GRADE_NOT_RELEASED).toPopoverString())
			.setVisible(!gradebook.isCourseGradeDisplayed()));

		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu");
		menu.add(new AjaxLink<Void>("setUngraded") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getUpdateUngradedItemsWindow();
				window.setTitle(getString("heading.zeroungradeditems"));
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new ZeroUngradedItemsPanel(window.getContentId(), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		menu.add(new AjaxLink<Void>("updateCourseGradeDisplay") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getUpdateCourseGradeDisplayWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new UpdateCourseGradeDisplayPanel(window.getContentId(), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		add(menu);
	}

	private Component getParentCellFor(final Component component) {
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), "header")) {
			return component;
		} else {
			return getParentCellFor(component.getParent());
		}
	}
}