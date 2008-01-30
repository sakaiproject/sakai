package org.sakaiproject.scorm.ui.console.components;

import java.util.Date;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.LearnerExperience;
import org.sakaiproject.wicket.markup.html.repeater.data.table.DecoratedPropertyColumn;

public class AccessStatusColumn extends AbstractColumn implements ScormConstants {

	private static final long serialVersionUID = 1L;
	
	public AccessStatusColumn(IModel displayModel, String sortProperty) {
		super(displayModel, sortProperty);
	}
	
	public void populateItem(Item item, String componentId, IModel model) {
		item.add(new Label(componentId, createLabelModel(model)));
	}
	
	protected IModel createLabelModel(IModel embeddedModel)
	{
		String resourceId = "access.status.not.accessed";
		Object target = embeddedModel.getObject();
		
		if (target instanceof LearnerExperience) {
			LearnerExperience experience = (LearnerExperience)target;
			
			switch (experience.getStatus()) {
			case NOT_ACCESSED:
				resourceId = "access.status.not.accessed";
				break;
			case INCOMPLETE:
				resourceId = "access.status.incomplete";
				break;
			case COMPLETED:
				resourceId = "access.status.completed";
				break;
			case GRADED:
				resourceId = "access.status.graded";
				break;
			};
		}
		
		
		return new ResourceModel(resourceId);
	}
}