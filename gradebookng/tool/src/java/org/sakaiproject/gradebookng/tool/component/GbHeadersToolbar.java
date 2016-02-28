package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GbHeadersToolbar extends HeadersToolbar {

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;


	public GbHeadersToolbar(final DataTable table, final ISortStateLocator stateLocator) {
		super(table, stateLocator);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final Map<String, Object> gradebookData = ((GbDataTable)getTable()).getGradebookData();
		final GradebookPage page = (GradebookPage) getPage();
		final GradebookUiSettings settings = page.getUiSettings();

		if (settings.isCategoriesEnabled()) {
			WebMarkupContainer categoriesRow = new WebMarkupContainer("categoriesRow");

			List<Assignment> assignments = (List<Assignment>) gradebookData.get("assignments");
			List<CategoryDefinition> categories = (List<CategoryDefinition>) gradebookData.get("categories");

			Collections.sort(categories, CategoryDefinition.orderComparator);

			Map<Long, Integer> categoryCounts = new HashMap<Long, Integer>();

			for (CategoryDefinition category : categories) {
				categoryCounts.put(category.getId(), 0);
			}
			// take into account assignments without a category
			categoryCounts.put(null, 0);

			for (Assignment assignment : assignments) {
				if (categoryCounts.containsKey(assignment.getCategoryId())) {
					Integer increment = categoryCounts.get(assignment.getCategoryId())+1;
					categoryCounts.put(assignment.getCategoryId(), increment);
				}
			}

			final GbCategoryType categoryType = GbHeadersToolbar.this.businessService.getGradebookCategoryType();

			categories = categories.stream().
					filter(c -> categoryCounts.get(c.getId()) > 0).collect(Collectors.toList());

			categoriesRow.add(new ListView<CategoryDefinition>("categories", categories) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(final ListItem<CategoryDefinition> categoryItem) {
					CategoryDefinition category = categoryItem.getModelObject();
					// add colspan attribute + 1 to accound for the category average column
					categoryItem.add(new AttributeModifier("colspan", categoryCounts.get(category.getId()) + 1));
					categoryItem.add(new AttributeModifier("data-category-id", category.getId()));
					String color = settings.getCategoryColor(category.getName());
					if (color == null) {
						color = page.generateRandomRGBColorString();
						settings.setCategoryColor(category.getName(), color);
						page.setUiSettings(settings);
					}
					categoryItem.add(new AttributeModifier("style",
							String.format("background-color: %s;", color)));
					categoryItem.add(new Label("name", category.getName()));
					categoryItem.add(page.buildFlagWithPopover("extraCreditCategoryFlag",
							getString("label.gradeitem.extracreditcategory")).
							setVisible(category.isExtraCredit()));

					if (GbCategoryType.WEIGHTED_CATEGORY.equals(categoryType) && category.getWeight() != null) {
						String weight = String.format("%s%%", Math.round(category.getWeight() * 100));
						categoryItem.add(new Label("weight", weight));
					} else {
						categoryItem.add(new WebMarkupContainer("weight").setVisible(false));
					}
				}
			});

			if (categoryCounts.get(null) > 0) {
				WebMarkupContainer uncategorizedHeader = new WebMarkupContainer("uncategorized");
				uncategorizedHeader.add(new AttributeModifier("colspan", categoryCounts.get(null)));
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
