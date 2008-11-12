package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;

public class SelectOptionsGroup extends Border {

	public SelectOptionsGroup(String id, IModel model) {
		super(id);
		WebMarkupContainer optgroup = new WebMarkupContainer("optgroup");
		optgroup.add(new AttributeModifier("label", true, model));
		add(optgroup);
		optgroup.add(getBodyContainer());
	}

}
