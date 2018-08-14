package org.sakaiproject.sitestats.tool.wicket.components.dropdown;

import org.sakaiproject.sitestats.tool.wicket.components.AbstractSakaiSpinnerAjaxCallListener;

/**
 * Adds the Sakai overlay spinner to the component
 * @author plukasew
 */
public class SakaiSpinningSelectAjaxCallListener extends AbstractSakaiSpinnerAjaxCallListener
{
	private static final String SPIN = "$('#%s').parent().addClass('" + SPINNER_CLASS + "');";
	private static final String STOP = "$('#%s').parent().removeClass('" + SPINNER_CLASS + "');";
	private static final String DISABLE_AND_SPIN = DISABLED + SPIN;
	private static final String ENABLE_AND_STOP = ENABLED + STOP;

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the ajax call
	 */
	public SakaiSpinningSelectAjaxCallListener(String componentMarkupId)
	{
		this(componentMarkupId, false);
	}

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the ajax call
	 * @param componentWillRender whether the ajax call will result in the component being re-rendered
	 */
	public SakaiSpinningSelectAjaxCallListener(String componentMarkupId, boolean componentWillRender)
	{
		super(componentMarkupId, componentWillRender);

		// on the client side, disable the control and show the spinner after the ajax request is initiated
		// so that the value of the select can be sent as part of the request
		onAfter(String.format(DISABLE_AND_SPIN, id, id));

		// if the control is re-rendered the disabled property will be set by wicket and the spinner
		// class will not be on the control as wicket doesn't know about it
		if (!willRender)
		{
			onComplete(String.format(ENABLE_AND_STOP, id, id));
		}
	}
}
