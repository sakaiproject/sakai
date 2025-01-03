package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;

public class StylableSelectOptionsGroup extends Border {
	private static final long	serialVersionUID	= 1L;
	private WebMarkupContainer optgroup;

	public StylableSelectOptionsGroup(String id, IModel model, IModel style, IModel<String> hclass) {
		super(id);
		// Add the body container first
		addToBorder(getBodyContainer());

		optgroup = new WebMarkupContainer("optgroup");
		optgroup.add(new AttributeModifier("label", model));
		if(style != null && !"null".equals((String) style.getObject())) {
			optgroup.add(new AttributeModifier("style", style));
		}
		if(hclass != null && !"null".equals(hclass.getObject())) {
			optgroup.add(new AttributeModifier("class", hclass));
		}
		addToBorder(optgroup);
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, org.apache.wicket.markup.ComponentTag openTag) {
		super.onComponentTagBody(markupStream, openTag);
	}

	@Override
	protected void onDetach() {
		super.onDetach();

		// Detach the optgroup
		if (optgroup != null) {
			optgroup.detach();
		}
		// Detach the body container
		detachBodyContainer();
	}

	private void detachBodyContainer() {
		if (getBodyContainer() != null) {
			getBodyContainer().detach();
			// after detachment remove the body container so we don't process it again
			//remove(getBodyContainer());
		}
	}
}