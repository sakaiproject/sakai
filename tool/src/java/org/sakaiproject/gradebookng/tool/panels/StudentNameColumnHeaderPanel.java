package org.sakaiproject.gradebookng.tool.panels;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.enums.EnumUtils;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.gradebookng.business.StudentSortOrder;

/**
 * 
 * Header panel for the student name/eid
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public StudentNameColumnHeaderPanel(String id, StudentSortOrder currentSortOrder) {
		super(id);
		
		//title
		add(new Label("title", new ResourceModel("column.header.students")));
		
		//get list of sort orders
		List<StudentSortOrder> sortOrders = Arrays.asList(StudentSortOrder.values());
		
		//dropdown
		DropDownChoice<StudentSortOrder> sortOrderList = new DropDownChoice<StudentSortOrder>("sortOrderList", sortOrders, new ChoiceRenderer<StudentSortOrder>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(StudentSortOrder o) {
				return o.name();
			}
			
			@Override
			public String getIdValue(StudentSortOrder o, int index) {
				return String.valueOf(o.getValue());
			}
			
		});
		
		//TODO set the default one to the currently selected one
		
		add(sortOrderList);
				
		//add the filter		
		add(new TextField<String>("filter"));
		
		
		
	}
	
	
	
}
