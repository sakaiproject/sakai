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
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.SortType;

import java.util.List;

public class SortGradeItemsByGradeItemPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public SortGradeItemsByGradeItemPanel(final String id) {
		super(id);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final List<Assignment> assignments = this.businessService.getGradebookAssignments(SortType.SORT_BY_SORTING);

		add(new ListView<Assignment>("gradeItemList", assignments) {
			@Override
			protected void populateItem(final ListItem<Assignment> assignmentItem) {
				final Assignment assignment = assignmentItem.getModelObject();
				assignmentItem.add(new Label("name", assignment.getName()));
				assignmentItem.add(new HiddenField<Long>("id",
						Model.of(assignment.getId())).add(
								new AttributeModifier("name",
										String.format("id", assignment.getId()))));
				assignmentItem.add(new HiddenField<Integer>("order",
						Model.of(assignment.getSortOrder())).add(
								new AttributeModifier("name",
										String.format("item_%s[order]", assignment.getId()))));
				assignmentItem.add(new HiddenField<Integer>("current_order",
						Model.of(assignment.getSortOrder())).add(
								new AttributeModifier("name",
										String.format("item_%s[current_order]", assignment.getId()))));
			}
		});
	}
}
