package org.sakaiproject.portal.api;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.tool.api.Placement;

public interface StoredState
{

	String getToolContextPath();

	HttpServletRequest getRequest(HttpServletRequest req);

	Placement getPlacement();

	String getToolPathInfo();

	String getSkin();

	void setRequest(HttpServletRequest req);

	void setPlacement(Placement siteTool);

	void setToolContextPath(String toolContextPath);

	void setToolPathInfo(String toolPathInfo);

	void setSkin(String skin);

}
