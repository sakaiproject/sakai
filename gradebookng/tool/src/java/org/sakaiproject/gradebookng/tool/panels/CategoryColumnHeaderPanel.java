package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbCategoryAverageSortOrder;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

/**
 *
 * Header panel for each category column in the UI
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class CategoryColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final IModel<CategoryDefinition> modelData;

	public CategoryColumnHeaderPanel(final String id, final IModel<CategoryDefinition> modelData) {
		super(id);
		this.modelData = modelData;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final CategoryDefinition category = this.modelData.getObject();

		final Link<String> title = new Link<String>("title", Model.of(category.getName())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {

				// toggle the sort direction on each click
				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GradebookUiSettings settings = gradebookPage.getUiSettings();

				// if null, set a default sort, otherwise toggle, save, refresh.
				if (settings.getCategorySortOrder() == null
						|| !category.getId().equals(settings.getCategorySortOrder().getCategoryId())) {
					settings.setCategorySortOrder(new GbCategoryAverageSortOrder(category.getId(), SortDirection.ASCENDING));
				} else {
					final GbCategoryAverageSortOrder sortOrder = settings.getCategorySortOrder();
					SortDirection direction = sortOrder.getDirection();
					direction = direction.toggle();
					sortOrder.setDirection(direction);
					settings.setCategorySortOrder(sortOrder);
				}

				// clear any assignment sort order to prevent conflicts
				settings.setAssignmentSortOrder(null);

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				setResponsePage(new GradebookPage());
			}

		};
		title.add(new AttributeModifier("title", category.getName()));
		title.add(new Label("label", category.getName()));

		// set the class based on the sortOrder. May not be set for this category so match it
		final GradebookPage gradebookPage = (GradebookPage) getPage();
		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		if (settings != null && settings.getCategorySortOrder() != null
				&& settings.getCategorySortOrder().getCategoryId() == category.getId()) {
			title.add(
					new AttributeModifier("class", "gb-sort-" + settings.getCategorySortOrder().getDirection().toString().toLowerCase()));
		}

		add(title);

		String categoryColor = settings.getCategoryColor(category.getName());

		final Component colorSwatch = gradebookPage.buildFlagWithPopover("categorySwatch",
				(new StringResourceModel("label.gradeitem.categoryaverage", this, null,
						new Object[] { category.getName() })).getString());
		colorSwatch.add(new AttributeAppender("style", String.format("background-color:%s;", categoryColor)));
		add(colorSwatch);
	}

}
