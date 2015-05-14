package org.sakaiproject.gradebookng.tool.pages;

import org.apache.wicket.model.IModel;
import org.sakaiproject.service.gradebook.shared.Assignment;


/**
 * Page for editing a gradebook item
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class EditGradebookItemPage extends BasePage {
	
	private static final long serialVersionUID = 1L;
	
	IModel<Long> model;

	public EditGradebookItemPage(IModel<Long> model) {
		this.model = model;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Long assignmentId = this.model.getObject();
		
		//get assignment
		Assignment assignment = this.businessService.getAssignment(assignmentId);
		
	}
}
