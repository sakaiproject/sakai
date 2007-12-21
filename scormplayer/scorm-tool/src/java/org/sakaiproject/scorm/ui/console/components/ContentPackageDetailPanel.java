package org.sakaiproject.scorm.ui.console.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.wicket.model.DecoratedPropertyModel;
import org.sakaiproject.wicket.model.SimpleDateFormatPropertyModel;

public class ContentPackageDetailPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public ContentPackageDetailPanel(String id, ContentPackage contentPackage) {
		super(id);

		add(newPropertyLabel(contentPackage, "title"));
		add(newDatePropertyLabel(contentPackage, "releaseOn"));
		add(newDatePropertyLabel(contentPackage, "dueOn"));
		add(newDatePropertyLabel(contentPackage, "acceptUntil"));
		add(newTriesPropertyLabel(contentPackage, "numberOfTries"));
		add(newPropertyLabel(contentPackage, "createdBy"));
		add(newDatePropertyLabel(contentPackage, "createdOn"));
		add(newPropertyLabel(contentPackage, "modifiedBy"));
		add(newDatePropertyLabel(contentPackage, "modifiedOn"));
	}

	private Label newPropertyLabel(ContentPackage contentPackage, String expression) {
		return new Label(expression, new PropertyModel(contentPackage, expression));
	}
	
	private Label newDatePropertyLabel(ContentPackage contentPackage, String expression) {
		return new Label(expression, new SimpleDateFormatPropertyModel(contentPackage, expression));
	}
	
	private Label newTriesPropertyLabel(ContentPackage contentPackage, String expression) {
		return new Label(expression, new TriesDecoratedPropertyModel(contentPackage, expression));
	}
	
	public class TriesDecoratedPropertyModel extends DecoratedPropertyModel {

		private static final long serialVersionUID = 1L;

		public TriesDecoratedPropertyModel(Object modelObject, String expression) {
			super(modelObject, expression);
		}

		@Override
		public Object convertObject(Object object) {
			String str = String.valueOf(object);
			
			if (str.equals("-1"))
				return "Unlimited";
			
			return str;
		}
		
	}
	
}
