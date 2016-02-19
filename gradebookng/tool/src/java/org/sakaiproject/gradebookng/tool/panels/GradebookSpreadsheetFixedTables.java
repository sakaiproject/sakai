package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GradebookSpreadsheetFixedTables extends Panel {

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	IModel<Map<String, Object>> model;

	// TODO: Ideally this panel would not be required! 
	// Move this to the main table as a second header row
	// so the JavaScript does not need to insert it manually.
	public GradebookSpreadsheetFixedTables(final String id, final IModel<Map<String, Object>> model) {
		super(id);

		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		Map<String, Object> modelData = model.getObject();

		List<Assignment> assignments = (List<Assignment>) modelData.get("assignments");
		List<CategoryDefinition> categories = (List<CategoryDefinition>) modelData.get("categories");

		Collections.sort(categories, CategoryDefinition.orderComparator);

		Map<Long, Integer> categoryCounts = new HashMap<Long, Integer>();

		for (CategoryDefinition category : categories) {
			categoryCounts.put(category.getId(), 0);
		}

		for (Assignment assignment : assignments) {
			if (assignment.getCategoryId() != null) {
				Integer increment = categoryCounts.get(assignment.getCategoryId())+1;
				categoryCounts.put(assignment.getCategoryId(), increment);
			}
		}

		final GradebookPage page = (GradebookPage) getPage();
		final GradebookUiSettings settings = page.getUiSettings();

		final GbCategoryType categoryType = this.businessService.getGradebookCategoryType();

		categories = categories.stream().
				filter(c -> categoryCounts.get(c.getId()) > 0).collect(Collectors.toList());

		add(new ListView<CategoryDefinition>("categories", categories) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<CategoryDefinition> categoryItem) {
				CategoryDefinition category = categoryItem.getModelObject();
				categoryItem.add(new AttributeModifier("colspan", categoryCounts.get(category.getId())));
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
	}
}