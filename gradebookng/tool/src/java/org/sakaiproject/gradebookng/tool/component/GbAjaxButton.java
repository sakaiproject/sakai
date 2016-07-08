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
			// disable the button right away after clicking it
			// and mark on it if the window is unloading
			.onBefore(
				String.format(
					"$('#%s').prop('disabled', true);" +
						"$(window).on('beforeunload', function() {" +
						"$('#%s').data('unloading', true).prop('disabled', true)});",
					getMarkupId(), getMarkupId()))
			// if the page is unloading, keep it disabled, otherwise
			// add a slight delay in re-enabling it, just in case it succeeded
			// and there's a delay in closing a parent modal
			.onComplete(
				String.format("setTimeout(function() {" +
					"if (!$('#%s').data('unloading')) $('#%s').prop('disabled',false);" +
					"}, 1000)",
					getMarkupId(), getMarkupId())));
	}
}
