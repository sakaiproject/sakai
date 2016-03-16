package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.tool.gradebook.Gradebook;

public class CourseGradeColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<Boolean> model;

	public CourseGradeColumnHeaderPanel(final String id, final IModel<Boolean> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		getParentCellFor(this).setOutputMarkupId(true);

		add(new Label("title", new ResourceModel("column.header.coursegrade")));

		final Gradebook gradebook = this.businessService.getGradebook();
		final GradebookPage gradebookPage = (GradebookPage) getPage();

		final GbCategoryType categoryType = GbCategoryType.valueOf(gradebook.getCategory_type());

		// get setting
		final Boolean showPoints = this.model.getObject();

		// icons
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

		final AjaxLink<Boolean> showHidePoints = new AjaxLink<Boolean>("showHidePoints", this.model) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {

				// get current setting
				final Boolean currentSetting = CourseGradeColumnHeaderPanel.this.model.getObject();

				// toggle it
				final Boolean nextSetting = !currentSetting;

				// set it
				final GradebookUiSettings settings = gradebookPage.getUiSettings();
				settings.setShowPoints(nextSetting);

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				setResponsePage(new GradebookPage());
			}

			@Override
			public boolean isVisible() {
				return categoryType != GbCategoryType.WEIGHTED_CATEGORY;
			}
		};

		// the label changes depending on the state so we wrap it in a model
		final IModel<String> showHidePointsModel = new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {

				// toggles the label to the opposite one
				if (showPoints) {
					return getString("coursegrade.option.hidepoints");
				} else {
					return getString("coursegrade.option.showpoints");
				}
			}
		};
		showHidePoints.add(new Label("showHidePointsLabel", showHidePointsModel));
		menu.add(showHidePoints);

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