package org.sakaiproject.scorm.ui.console.components;

import java.util.Date;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.scorm.model.api.ContentPackage;

public class StatusColumn extends AbstractColumn {

	private static final long serialVersionUID = 1L;

	public StatusColumn(IModel displayModel, String sortProperty) {
		super(displayModel, sortProperty);
	}

	public void populateItem(Item item, String componentId, IModel model) {
		item.add(new Label(componentId, createLabelModel(model)));
	}
	
	protected IModel createLabelModel(IModel embeddedModel)
	{
		String resourceId = "status.unknown";
		Object target = embeddedModel.getObject();
		
		if (target instanceof ContentPackage) {
			ContentPackage contentPackage = (ContentPackage)target;
			
			Date now = new Date();
			
			if (now.after(contentPackage.getReleaseOn())) {
				if (now.before(contentPackage.getDueOn())) 
					resourceId = "status.open";
				else if (now.before(contentPackage.getAcceptUntil()))
					resourceId = "status.overdue";
				else 
					resourceId = "status.closed";
			} else {
				resourceId = "status.notyetopen";
			}
		}
		
		
		return new ResourceModel(resourceId);
	}

	
	
	
}
