package org.sakaiproject.portal.charon;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.tool.api.Placement;

public class StoredState {
	private SessionRequestHolder requestHolder = null;
	private Placement  placement = null;
	private String toolContextPath = null;
	private String toolPathInfo = null;
	private String marker;
	private String replacement;
	private String skin;
	
	public StoredState(String marker, String replacement) {
		this.marker = marker;
		this.replacement = replacement;
	}
	
	public Placement getPlacement()
	{
		return placement;
	}
	public void setPlacement(Placement placement)
	{
		this.placement = placement;
	}
	public HttpServletRequest getRequest(HttpServletRequest currentRequest)
	{
		return new RecoveredServletRequest(currentRequest,requestHolder,marker,replacement);
	}
	public void setRequest(HttpServletRequest request)
	{
		this.requestHolder = new SessionRequestHolder(request,marker,replacement);
	}
	public String getToolContextPath()
	{
		return toolContextPath;
	}
	public void setToolContextPath(String toolContextPath)
	{
		if ( toolContextPath != null ) {
			this.toolContextPath = toolContextPath.replace(marker,replacement);
		} else {
			this.toolContextPath = toolContextPath;
		}
	}
	public String getToolPathInfo()
	{
		return toolPathInfo;
	}
	public void setToolPathInfo(String toolPathInfo)
	{
		if ( toolPathInfo != null ) {
			this.toolPathInfo = toolPathInfo.replace(marker,replacement);
		} else {
			this.toolPathInfo = toolPathInfo;
		}
	}

	public void setSkin(String skin)
	{
		this.skin = skin;
		
	}

	public String getSkin()
	{
		return skin;
	}
	
	
	
}