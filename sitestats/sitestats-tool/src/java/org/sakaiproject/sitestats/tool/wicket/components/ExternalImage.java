package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.Model;

public class ExternalImage extends WebComponent {
	private static final long	serialVersionUID	= 1L;

	public ExternalImage(String id, String imageUrl) {
	    super(id);
	    add(new AttributeModifier("src", true, new Model(imageUrl)));
	    setVisible(!(imageUrl==null || imageUrl.equals("")));
	}

	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "img");
		tag.addBehavior(new AttributeModifier("id", true, new Model(getMarkupId())));
	}

}
