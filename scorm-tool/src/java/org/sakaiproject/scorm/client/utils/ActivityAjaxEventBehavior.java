package org.sakaiproject.scorm.client.utils;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.string.AppendingStringBuffer;

public abstract class ActivityAjaxEventBehavior extends AjaxEventBehavior {

	public ActivityAjaxEventBehavior(String event) {
		super(event);
	}

	private static final long serialVersionUID = 1L;

	
	protected CharSequence getCallbackScript(boolean onlyTargetActivePage)
	{
		return generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "'");
	}
	
	public CharSequence getCallbackUrl()
	{
		if (getComponent() == null)
		{
			throw new IllegalArgumentException(
					"Behavior must be bound to a component to create the URL");
		}
		
		final RequestListenerInterface rli;
		
		rli = IBehaviorListener.INTERFACE;
		
		WebRequest webRequest = (WebRequest)getComponent().getRequest();
		HttpServletRequest servletRequest = webRequest.getHttpServletRequest();

		String toolUrl = servletRequest.getContextPath();
		
		AppendingStringBuffer url = new AppendingStringBuffer();
		url.append(toolUrl).append("/");
		url.append(getComponent().urlFor(this, rli));

		return url;
	}

}
