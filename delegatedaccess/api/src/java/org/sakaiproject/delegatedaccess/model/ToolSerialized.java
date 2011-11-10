package org.sakaiproject.delegatedaccess.model;

import java.io.Serializable;

public class ToolSerialized implements Serializable {
	private String toolId;
	private String toolName;
	private boolean selected = false;
	
	public ToolSerialized(String toolId, String toolName, boolean selected){
		this.toolId = toolId;
		this.toolName = toolName;
		this.selected = selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	public String getToolId() {
		return toolId;
	}
}