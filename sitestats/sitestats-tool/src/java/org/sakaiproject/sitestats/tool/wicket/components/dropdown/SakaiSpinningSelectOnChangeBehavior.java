package org.sakaiproject.sitestats.tool.wicket.components.dropdown;

import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;

/**
 * Adds the Sakai overlay spinner to the DropDownChoice
 * @author plukasew
 */
public abstract class SakaiSpinningSelectOnChangeBehavior extends AjaxFormComponentUpdatingBehavior
{
	public SakaiSpinningSelectOnChangeBehavior()
	{
		super("onchange");
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		attributes.setChannel(new AjaxChannel("blocking", AjaxChannel.Type.ACTIVE));

		AjaxCallListener listener = new SakaiSpinningSelectAjaxCallListener(getComponent().getMarkupId(), false);
		attributes.getAjaxCallListeners().add(listener);
	}
}
