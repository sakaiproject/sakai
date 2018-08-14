package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.ajax.attributes.AjaxCallListener;

/**
 * Abstract base class for AjaxCallListeners used to apply a spinner overlay to the component while Ajax call in progress
 * @author plukasew
 */
public abstract class AbstractSakaiSpinnerAjaxCallListener extends AjaxCallListener
{
	protected static final String SPINNER_CLASS = "spinButton";
	protected static final String DISABLED = "$('#%s').prop('disabled', true);";
	protected static final String ENABLED = "$('#%s').prop('disabled', false);";

	protected boolean willRender = false;
	protected String id = "";

	/**
	 * Call listener to overlay a spinner and disable the a clicked component
	 * @param componentMarkupId the markup id for the component
	 * @param componentWillRender whether or not the component will be re-rendered as a result of the ajax update
	 */
	public AbstractSakaiSpinnerAjaxCallListener(String componentMarkupId, boolean componentWillRender)
	{
		id = componentMarkupId;
		willRender = componentWillRender;
	}
}
