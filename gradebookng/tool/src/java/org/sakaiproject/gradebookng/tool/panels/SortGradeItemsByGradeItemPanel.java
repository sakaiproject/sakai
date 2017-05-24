package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.SortType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
					Model.of(assignment.getId())).
					add(new AttributeModifier("name",
						String.format("id", assignment.getId()))));
				assignmentItem.add(new HiddenField<Integer>("order",
					Model.of(assignment.getSortOrder())).
						add(new AttributeModifier("name",
							String.format("item_%s[order]", assignment.getId()))));
				assignmentItem.add(new HiddenField<Integer>("current_order",
					Model.of(assignment.getSortOrder())).
					add(new AttributeModifier("name",
						String.format("item_%s[current_order]", assignment.getId()))));
			}
		});
	}
}
