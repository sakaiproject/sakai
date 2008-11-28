/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportManager;


public class ToolModel implements IModel {
	private static final long	serialVersionUID	= 1L;

	private String				toolId				= "";
	private String				toolName			= "";

	public ToolModel(String toolId, String toolName) {
		this.toolId = toolId;
		this.toolName = toolName;
	}
	
	public ToolModel(ToolInfo e) {
		this.toolId = e.getToolId();
		this.toolName = e.getToolName();
	}

	public Object getObject() {
		return getToolId() + " + " + getToolName();
	}

	public void setObject(Object object) {
		if(object instanceof String){
			String[] str = ((String) object).split(" \\+ ");
			toolId = str[0];
			toolName = str[1];
		}
	}

	public String getToolId() {
		return toolId;
	}

	public String getToolName() {
		if(ReportManager.WHAT_EVENTS_ALLTOOLS.equals(toolName)) {
			return (String) new ResourceModel("all").getObject();
		}else{
			return toolName;
		}
	}

	public void detach() {
		toolId = null;
		toolName = null;
	}

}