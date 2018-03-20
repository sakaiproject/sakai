package org.sakaiproject.gradebookng.tool.panels;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.tool.gradebook.Gradebook;

public class CourseGradeColumnHeaderPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	IModel<Boolean> model;

	public CourseGradeColumnHeaderPanel(final String id, final IModel<Boolean> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final GradebookPage gradebookPage = (GradebookPage) getPage();

		getParentCellFor(this).setOutputMarkupId(true);

		final Link<String> title = new Link<String>("title") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {

				// toggle the sort direction on each click
				final GradebookUiSettings settings = gradebookPage.getUiSettings();

				// if null, set a default sort, otherwise toggle, save, refresh.
				if (settings.getCourseGradeSortOrder() == null) {
					settings.setCourseGradeSortOrder(SortDirection.getDefault());
				} else {
					final SortDirection sortOrder = settings.getCourseGradeSortOrder();
					settings.setCourseGradeSortOrder(sortOrder.toggle());
				}

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				setResponsePage(GradebookPage.class);
			}

		};

		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		title.add(new AttributeModifier("title", new ResourceModel("column.header.coursegrade")));
		title.add(new Label("label", new ResourceModel("column.header.coursegrade")));
		if (settings != null && settings.getCourseGradeSortOrder() != null) {
			title.add(
				new AttributeModifier("class", "gb-sort-" + settings.getCourseGradeSortOrder().toString().toLowerCase()));
		}
		add(title);

		final Gradebook gradebook = getGradebook();
		final GbRole role = getUserRole();

		final GbCategoryType categoryType = GbCategoryType.valueOf(gradebook.getCategory_type());

		// get setting
		final Boolean showPoints = this.model.getObject();

		// icons
		final Map<String, Object> popoverModel = new HashMap<>();
		popoverModel.put("role", role);
		popoverModel.put("flag", HeaderFlagPopoverPanel.Flag.COURSE_GRADE_RELEASED);
		add(gradebookPage.buildFlagWithPopover("isReleasedFlag",
				new HeaderFlagPopoverPanel("popover", Model.ofMap(popoverModel)).toPopoverString())
				.setVisible(gradebook.isCourseGradeDisplayed()));
		popoverModel.put("flag", HeaderFlagPopoverPanel.Flag.COURSE_GRADE_NOT_RELEASED);
		add(gradebookPage.buildFlagWithPopover("notReleasedFlag",
				new HeaderFlagPopoverPanel("popover", Model.ofMap(popoverModel)).toPopoverString())
				.setVisible(!gradebook.isCourseGradeDisplayed()));

		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return role == GbRole.INSTRUCTOR;
			}
		};
		menu.add(new GbAjaxLink("setUngraded") {
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

		final GbAjaxLink<Boolean> showHidePoints = new GbAjaxLink("showHidePoints", this.model) {
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
				setResponsePage(GradebookPage.class);
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