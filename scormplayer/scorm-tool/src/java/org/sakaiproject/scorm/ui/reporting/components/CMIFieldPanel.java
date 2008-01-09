package org.sakaiproject.scorm.ui.reporting.components;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.sakaiproject.scorm.model.api.CMIField;

public class CMIFieldPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final RepeatingView children;
	
	public CMIFieldPanel(String id, CMIField field, String value) {
		super(id);
		
		add(new Label("description", new Model(field.getDescription())));
		add(new Label("fieldValue", new Model(value)));
		
		add(children = new RepeatingView("children"));
		
		if (field.isParent()) {
			for (CMIField child : field.getChildren()) {
				for (String fieldValue : field.getFieldValues())
					addChild(child, fieldValue, children);			
			}
		}
	}
	
	
	/*
	 * Copied the basic organization of this method from an Apache Wicket class
	 * 	org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
	 * originally authored by Igor Vaynberg (ivaynberg)
	 */
	private void addChild(CMIField field, String value, RepeatingView container)
	{
		CMIFieldPanel fieldComponent = new CMIFieldPanel("child", field, value);
		fieldComponent.setRenderBodyOnly(true);

		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(fieldComponent);

		container.add(item);
	}

}
