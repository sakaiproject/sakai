package org.sakaiproject.sitestats.tool.wicket.components;

/**
 * Adds the Sakai overlay spinner to the component making the Ajax call
 * @author plukasew
 */
public class SakaiSpinnerAjaxCallListener extends AbstractSakaiSpinnerAjaxCallListener
{
	private static final String SPIN = "$('#%s').addClass('" + SPINNER_CLASS + "');";
	private static final String STOP = "$('#%s').removeClass('" + SPINNER_CLASS + "');";
	private static final String DISABLE_AND_SPIN = DISABLED + SPIN;
	private static final String ENABLE_AND_STOP = ENABLED + STOP;

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the ajax call
	 */
	public SakaiSpinnerAjaxCallListener(String componentMarkupId)
	{
		this(componentMarkupId, false);
	}

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the ajax call
	 * @param componentWillRender whether the ajax call will result in the component being re-rendered
	 */
	public SakaiSpinnerAjaxCallListener(String componentMarkupId, boolean componentWillRender)
	{
		super(componentMarkupId, componentWillRender);

		// on the client side, disable the control and show the spinner after click
		onBefore(String.format(DISABLE_AND_SPIN, id, id));

		// if the control is re-rendered the disabled property will be set by wicket and the spinner
		// class will not be on the component as wicket doesn't know about it
		if (!willRender)
		{
			onComplete(String.format(ENABLE_AND_STOP, id, id));
		}
	}
}
