package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;

public class StylableSelectOptionsGroup extends Border {
	private static final long	serialVersionUID	= 1L;

	public StylableSelectOptionsGroup(String id, IModel model, IModel style) {
		super(id);
		WebMarkupContainer optgroup = new WebMarkupContainer("optgroup");
		optgroup.add(new AttributeModifier("label", true, model));
		if(style != null && !"null".equals((String) style.getObject())) {
			optgroup.add(new AttributeModifier("style", true, style));
		}
		add(optgroup);
		optgroup.add(getBodyContainer());
	}

}
