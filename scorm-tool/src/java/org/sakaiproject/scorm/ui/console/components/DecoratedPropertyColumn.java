package org.sakaiproject.scorm.ui.console.components;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public abstract class DecoratedPropertyColumn extends PropertyColumn {

	private static final long serialVersionUID = 1L;

	public DecoratedPropertyColumn(IModel displayModel, String sortProperty, String propertyExpression) {
		super(displayModel, sortProperty, propertyExpression);
	}
	
	protected IModel createLabelModel(IModel embeddedModel)
	{
		return new PropertyModel(embeddedModel, getPropertyExpression()) {
	
			private static final long serialVersionUID = 1L;

			public Object getObject()
			{
				Object object = super.getObject();
				
				return convertObject(object);
			}
			
		};
	}

	public abstract Object convertObject(Object object);

}
