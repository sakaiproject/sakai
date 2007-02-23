package org.sakaiproject.portal.render.portlet;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletWindowID;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;

public class SakaiPortletWindow implements PortletWindow
{

	private String contextPath;

	private String portletName;

	private PortletState state;

	public SakaiPortletWindow(String windowId, String contextPath, String portletName)
	{
		this.contextPath = contextPath;
		this.portletName = portletName;
		this.state = new PortletState(windowId);
	}

	public PortletState getState()
	{
		return state;
	}

	public void setState(PortletState state)
	{
		this.state = state;
	}

	public PortletWindowID getId()
	{
		return new SakaiPortletWindowId();
	}

	public String getContextPath()
	{
		return contextPath;
	}

	public String getPortletName()
	{
		return portletName;
	}

	public WindowState getWindowState()
	{
		return state.getWindowState();
	}

	public PortletMode getPortletMode()
	{
		return state.getPortletMode();
	}

	class SakaiPortletWindowId implements PortletWindowID
	{
		public String getStringId()
		{
			return state.getId();
		}
	}
}
