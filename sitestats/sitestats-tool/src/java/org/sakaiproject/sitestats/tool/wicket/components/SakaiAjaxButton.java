package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;

/**
 * Disables the button on click, sets the standard Sakai spinner on it, and removes it/re-enables the button after the Ajax call completes.
 *
 * @author plukasew
 */
public class SakaiAjaxButton extends AjaxButton
{
	protected boolean willRenderOnClick = false;

	public SakaiAjaxButton(String id) {
		super(id);
	}

	public SakaiAjaxButton(String id, Form<?> form) {
		super(id, form);
	}

	/**
	 * Whether or not the button itself will be re-rendered as part of the ajax update
	 * @param value true if button will be re-rendered
	 * @return the button, for method chaining
	 */
	public SakaiAjaxButton setWillRenderOnClick(boolean value)
	{
		willRenderOnClick = value;
		return this;
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		attributes.setChannel(new AjaxChannel("blocking", AjaxChannel.Type.ACTIVE));

		AjaxCallListener listener = new SakaiSpinnerAjaxCallListener(getMarkupId(), willRenderOnClick);
		attributes.getAjaxCallListeners().add(listener);
	}
}
