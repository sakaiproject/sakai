package org.sakaiproject.portal.render.portlet.servlet;

import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

public class SakaiServletRequest extends HttpServletRequestWrapper
{

	private PortletState state;

	public SakaiServletRequest(HttpServletRequest servletRequest,
			PortletState state)
	{
		super(servletRequest);
		this.state = state;
	}

	public String getParameter(String string)
	{
		return (String) state.getParameters().get(string);
	}

	public Map getParameterMap()
	{
		return new HashMap(state.getParameters());
	}

	public Enumeration getParameterNames()
	{
		return new Vector(state.getParameters().keySet()).elements();
	}

	public String[] getParameterValues(String string)
	{
		return (String[]) state.getParameters().get(string);
	}

	/**
	 * This causes the placement ID to be retrievabl from the request
	 */
	@Override
	public Object getAttribute(String attributeName)
	{
		if (PortalService.PLACEMENT_ATTRIBUTE.equals(attributeName))
		{
			return state.getId();
		}
		else
		{
			return super.getAttribute(attributeName);
		}
	}
}
