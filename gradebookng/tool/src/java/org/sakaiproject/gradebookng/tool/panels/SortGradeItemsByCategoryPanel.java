/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SortGradeItemsByCategoryPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public SortGradeItemsByCategoryPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final Map<String, Object> model = (Map<String, Object>) getDefaultModelObject();
		final GradebookUiSettings settings = (GradebookUiSettings) model.get("settings");

		// retrieve all categories, remove empty and ensure they're sorted
		final List<CategoryDefinition> categories = this.businessService.getGradebookCategories().stream()
				.filter(c -> !c.getAssignmentList().isEmpty()).collect(Collectors.toList());
		Collections.sort(categories, CategoryDefinition.orderComparator);

		add(new ListView<CategoryDefinition>("categoriesList", categories) {
			@Override
			protected void populateItem(final ListItem<CategoryDefinition> categoryItem) {
				final CategoryDefinition category = categoryItem.getModelObject();
				final List<Assignment> assignments = category.getAssignmentList();
				Collections.sort(assignments, new CategorizedAssignmentComparator());

				categoryItem.add(new AttributeModifier("style",
						String.format("border-left-color: %s", settings.getCategoryColor(category.getName(), category.getId()))));
				categoryItem.add(new Label("name", category.getName()));
				categoryItem.add(new ListView<Assignment>("gradeItemList", assignments) {
					@Override
					protected void populateItem(final ListItem<Assignment> assignmentItem) {
						final Assignment assignment = assignmentItem.getModelObject();
						assignmentItem.add(new Label("name", assignment.getName()));
						assignmentItem.add(new HiddenField<>("id",
								Model.of(assignment.getId())).add(
										new AttributeModifier("name",
												String.format("id", assignment.getId()))));
						assignmentItem.add(new HiddenField<>("order",
								Model.of(assignment.getCategorizedSortOrder())).add(
										new AttributeModifier("name",
												String.format("item_%s[order]", assignment.getId()))));
						assignmentItem.add(new HiddenField<>("current_order",
								Model.of(assignment.getCategorizedSortOrder())).add(
										new AttributeModifier("name",
												String.format("item_%s[current_order]", assignment.getId()))));
					}
				});
			}
		});
	}
}

class CategorizedAssignmentComparator implements Comparator<Assignment> {
	@Override
	public int compare(final Assignment a1, final Assignment a2) {
		// if in the same category, sort by their categorized sort order
		if (Objects.equals(a1.getCategoryId(), a2.getCategoryId())) {
			// handles null orders by putting them at the end of the list
			if (a1.getCategorizedSortOrder() == null) {
				return 1;
			} else if (a2.getCategorizedSortOrder() == null) {
				return -1;
			}
			return Integer.compare(a1.getCategorizedSortOrder(), a2.getCategorizedSortOrder());

			// otherwise, sort by their category order
		} else {
			if (a1.getCategoryOrder() == null && a2.getCategoryOrder() == null) {
				// both orders are null.. so order by A-Z
				if (a1.getCategoryName() == null && a2.getCategoryName() == null) {
					// both names are null so order by id
					return a1.getCategoryId().compareTo(a2.getCategoryId());
				} else if (a1.getCategoryName() == null) {
					return 1;
				} else if (a2.getCategoryName() == null) {
					return -1;
				} else {
					return a1.getCategoryName().compareTo(a2.getCategoryName());
				}
			} else if (a1.getCategoryOrder() == null) {
				return 1;
			} else if (a2.getCategoryOrder() == null) {
				return -1;
			} else {
				return a1.getCategoryOrder().compareTo(a2.getCategoryOrder());
			}
		}
	}
}
