package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

/**
 * A GradebookNG implementation of Wicket's AjaxLink that
 * disables the link on click to reduce the likelihood of
 * double-clicks resulting in double actions.
 */
abstract public class GbAjaxLink<T> extends AjaxLink {

	public GbAjaxLink(String id) {
		this(id, (IModel)null);
	}

	public GbAjaxLink(String id, IModel<T> model) {
		super(id, model);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setChannel(new AjaxChannel("blocking", AjaxChannel.Type.ACTIVE));
	}
}
