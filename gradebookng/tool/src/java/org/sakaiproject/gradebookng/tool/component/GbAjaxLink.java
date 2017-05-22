package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

/**
 * A GradebookNG implementation of Wicket's AjaxLink that
 * disables the link on click to reduce the likelihood of
 * double-clicks resulting in double actions.
 */
abstract public class GbAjaxLink<T> extends AjaxLink<T> {

	private static final long serialVersionUID = 1L;

	public GbAjaxLink(final String id) {
		this(id, (IModel<T>)null);
	}

	public GbAjaxLink(final String id, final IModel<T> model) {
		super(id, model);
	}

	@Override
	protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setChannel(new AjaxChannel("blocking", AjaxChannel.Type.ACTIVE));
	}
}
