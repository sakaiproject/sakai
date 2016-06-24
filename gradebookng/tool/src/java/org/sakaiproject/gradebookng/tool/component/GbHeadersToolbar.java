package org.sakaiproject.gradebookng.tool.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

public class GbHeadersToolbar extends HeadersToolbar {

	private final IModel<Map<String, Object>> model;

	public GbHeadersToolbar(final DataTable table, final ISortStateLocator stateLocator, final IModel<Map<String, Object>> model) {
		super(table, stateLocator);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final GradebookPage page = (GradebookPage) getPage();
		final GradebookUiSettings settings = page.getUiSettings();

		final Map<String, Object> modelData = this.model.getObject();
		final boolean categoriesEnabled = (boolean) modelData.get("categoriesEnabled");

		if (categoriesEnabled && settings.isCategoriesEnabled()) {
			final WebMarkupContainer categoriesRow = new WebMarkupContainer("categoriesRow");

			final List<Assignment> assignments = (List<Assignment>) modelData.get("assignments");
			List<CategoryDefinition> categories = (List<CategoryDefinition>) modelData.get("categories");
			final GbCategoryType categoryType = (GbCategoryType) modelData.get("categoryType");

			Collections.sort(categories, CategoryDefinition.orderComparator);

			final Map<Long, Integer> categoryCounts = new HashMap<Long, Integer>();

			for (final CategoryDefinition category : categories) {
				categoryCounts.put(category.getId(), 0);
			}
			// take into account assignments without a category
			categoryCounts.put(null, 0);

			for (final Assignment assignment : assignments) {
				if (categoryCounts.containsKey(assignment.getCategoryId())) {
					final Integer increment = categoryCounts.get(assignment.getCategoryId()) + 1;
					categoryCounts.put(assignment.getCategoryId(), increment);
				}
			}

			categories = categories.stream().filter(c -> categoryCounts.get(c.getId()) > 0).collect(Collectors.toList());

			categoriesRow.add(new ListView<CategoryDefinition>("categories", categories) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(final ListItem<CategoryDefinition> categoryItem) {
					final CategoryDefinition category = categoryItem.getModelObject();
					// add colspan attribute + 1 to accound for the category average column
					categoryItem.add(new AttributeModifier("colspan", categoryCounts.get(category.getId()) + 1));
					categoryItem.add(new AttributeModifier("data-category-id", category.getId()));
					final String color = settings.getCategoryColor(category.getName());
					categoryItem.add(new AttributeModifier("style",
							String.format("background-color: %s;", color)));
					categoryItem.add(new Label("name", category.getName()));
					categoryItem.add(page.buildFlagWithPopover("extraCreditCategoryFlag",
							getString("label.gradeitem.extracreditcategory")).setVisible(category.isExtraCredit()));
					categoryItem.add(new AttributeModifier("title",
						new StringResourceModel("label.gradeitem.categoryheadertooltip", null,
							new Object[] {category.getName()})));

					if (GbCategoryType.WEIGHTED_CATEGORY.equals(categoryType) && category.getWeight() != null) {
						final String weight = FormatHelper.formatDoubleAsPercentage(category.getWeight() * 100);
						categoryItem.add(new Label("weight", weight));
					} else {
						categoryItem.add(new WebMarkupContainer("weight").setVisible(false));
					}
				}
			});

			if (categoryCounts.get(null) > 0) {
				final WebMarkupContainer uncategorizedHeader = new WebMarkupContainer("uncategorized");
				uncategorizedHeader.add(new AttributeModifier("colspan", categoryCounts.get(null)));
				uncategorizedHeader.add(new AttributeModifier("title",
					getString("gradebookpage.uncategorised")));
				categoriesRow.add(uncategorizedHeader);
			} else {
				categoriesRow.add(new WebMarkupContainer("uncategorized").setVisible(false));
			}

			add(categoriesRow);
		} else {
			add(new WebMarkupContainer("categoriesRow").setVisible(false));
		}
	}
}
