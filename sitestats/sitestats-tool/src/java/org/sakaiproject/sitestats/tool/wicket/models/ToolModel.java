/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.IModel;
import org.sakaiproject.sitestats.api.event.ToolInfo;


public class ToolModel implements IModel {
	private static final long	serialVersionUID	= 1L;

	String						toolId				= "";
	String						toolName			= "";

	public ToolModel(ToolInfo e) {
		toolId = e.getToolId();
		toolName = e.getToolName();
	}

	public Object getObject() {
		return toolId + " + " + toolName;
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
		return toolName;
	}

	public void detach() {
		toolId = null;
		toolName = null;
	}

}