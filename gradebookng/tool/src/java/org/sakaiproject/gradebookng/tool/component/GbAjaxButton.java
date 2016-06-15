package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;

/**
 * A GradebookNG implementation of Wicket's AjaxButton that
 * disables the button on click to reduce the likelihood of
 * double-clicks resulting in double actions.
 */
public class GbAjaxButton extends AjaxButton {

	public GbAjaxButton(String id) {
		super(id);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.getAjaxCallListeners().add(new AjaxCallListener()
			.onBefore("$('#" + getMarkupId() + "').prop('disabled',true);")
			.onComplete("$('#" + getMarkupId() + "').prop('disabled',false);"));
	}
}
