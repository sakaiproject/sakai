package org.sakaiproject.sitestats.test.mocks;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.w3c.dom.Document;

public class FakeToolManager implements ToolManager {

	Set<Tool> tools = new HashSet<Tool>();
	Placement currentPlacement;

	public FakeToolManager() {
		FakeTool dropbox = new FakeTool();
		dropbox.setId(StatsManager.DROPBOX_TOOLID);
		dropbox.setTitle("DropBox");
		tools.add(dropbox);
		FakeTool resources = new FakeTool();
		resources.setId(StatsManager.RESOURCES_TOOLID);
		resources.setTitle("Resources");
		tools.add(resources);
		FakeTool sitestats = new FakeTool();
		sitestats.setId(StatsManager.SITESTATS_TOOLID);
		sitestats.setTitle("SiteStats");
		tools.add(sitestats);
		
		FakePlacement placement = new FakePlacement(dropbox, "site-id");
		currentPlacement = placement;
	}
	
	public Set findTools(Set categories, Set keywords) {
		return tools;
	}

	public Set<Tool> getTools() {
		return tools;
	}

	public void setTools(Set<Tool> tools) {
		this.tools = tools;
	}

	public Placement getCurrentPlacement() {
		return currentPlacement;
	}

	public void setCurrentPlacement(Placement currentPlacement) {
		this.currentPlacement = currentPlacement;
	}

	public Tool getCurrentTool() {
		return currentPlacement.getTool();
	}

	public Tool getTool(String id) {
		for(Iterator<Tool> iter = tools.iterator(); iter.hasNext();) {
			Tool tool = iter.next();
			if(tool.getId().equals(id)) {
				return tool;
			}
		}
		return null;
	}

	public void register(Tool tool) {
	}

	public void register(Document toolXml) {
	}

	public void register(File toolXmlFile) {
	}

	public void register(InputStream toolXmlStream) {
	}

}
