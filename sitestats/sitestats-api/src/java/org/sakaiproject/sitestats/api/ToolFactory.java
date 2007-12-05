package org.sakaiproject.sitestats.api;

import java.util.List;



public interface ToolFactory {

	public ToolInfo createTool(String toolId);
	
	public ToolInfo createTool(String toolId, List<String> additionalToolIds);

}
