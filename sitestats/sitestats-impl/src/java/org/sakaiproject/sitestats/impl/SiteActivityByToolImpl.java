package org.sakaiproject.sitestats.impl;

import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.event.ToolInfo;

public class SiteActivityByToolImpl implements SiteActivityByTool {
	private String siteId	= null;
	private ToolInfo toolInfo 		= null;
	private long count		= 0;

	public long getCount() {
		return count;
	}

	public String getSiteId() {
		return siteId;
	}

	public ToolInfo getTool() {
		return toolInfo;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public void setTool(ToolInfo toolInfo) {
		this.toolInfo = toolInfo;
	}

	
	public String toString(){
		return siteId + " : " + toolInfo.getToolId() + " : " + count;
	}
}
