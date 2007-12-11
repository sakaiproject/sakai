package org.sakaiproject.scorm.ui.console.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.model.IModel;

public class DecoratedDatePropertyColumn extends DecoratedPropertyColumn {

	private static final long serialVersionUID = 1L;
	
	private SimpleDateFormat dateFormat;
	
	public DecoratedDatePropertyColumn(IModel displayModel, String sortProperty, String propertyExpression) {
		super(displayModel, sortProperty, propertyExpression);
		this.dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
	}

	@Override
	public Object convertObject(Object object) {
		
		if (object instanceof Date)
			return dateFormat.format(object);
		
		return object;
	}

}
