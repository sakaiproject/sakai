package org.sakaiproject.sitestats.api.parser;

import java.util.List;

import org.sakaiproject.sitestats.api.event.ToolInfo;



public interface ToolFactory {

	public ToolInfo createTool(String toolId);
	
	public ToolInfo createTool(String toolId, List<String> additionalToolIds);

}
