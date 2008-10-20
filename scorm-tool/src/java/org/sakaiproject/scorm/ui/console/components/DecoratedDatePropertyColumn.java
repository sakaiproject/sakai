package org.sakaiproject.scorm.ui.console.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.model.IModel;
import org.sakaiproject.wicket.markup.html.repeater.data.table.DecoratedPropertyColumn;

public class DecoratedDatePropertyColumn extends DecoratedPropertyColumn {

	private static final long serialVersionUID = 1L;
	
	private SimpleDateFormat dateFormat;
	
	public DecoratedDatePropertyColumn(IModel displayModel, String sortProperty, String propertyExpression) {
		super(displayModel, sortProperty, propertyExpression);
		this.dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
	}

	@Override
	public Object convertObject(Object object) {
		
		if (object instanceof Date)
			return dateFormat.format(object);
		
		return object;
	}

}
