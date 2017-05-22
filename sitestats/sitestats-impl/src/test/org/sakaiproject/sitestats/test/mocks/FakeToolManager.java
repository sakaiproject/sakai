/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import org.mockito.Mockito;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class FakeToolManager implements ToolManager {

	Set<Tool> tools = new HashSet<Tool>();
	Placement currentPlacement;
	FakeTool dropbox;
	FakeTool resources;
	FakeTool chat;
	FakeTool sitestats;

	public FakeToolManager() {
		dropbox = Mockito.spy(FakeTool.class);
		dropbox.setId(StatsManager.DROPBOX_TOOLID);
		dropbox.setTitle("DropBox");
		tools.add(dropbox);
		resources = Mockito.spy(FakeTool.class);
		resources.setId(StatsManager.RESOURCES_TOOLID);
		resources.setTitle("Resources");
		tools.add(resources);
		chat = Mockito.spy(FakeTool.class);
		chat.setId(FakeData.TOOL_CHAT);
		chat.setTitle("Chat");
		tools.add(chat);
		sitestats = Mockito.spy(FakeTool.class);
		sitestats.setId(StatsManager.SITESTATS_TOOLID);
		sitestats.setTitle("SiteStats");
		tools.add(sitestats);
		
		//setDefaultPlacementContext();
		currentPlacement = Mockito.spy(FakePlacement.class).set(resources, null);
	}
	
	public void setDefaultPlacementContext(String siteId) {
		currentPlacement = Mockito.mock(FakePlacement.class).set(resources, siteId);
	}
	
	public void setDefaultPlacementContext() {
		currentPlacement = Mockito.mock(FakePlacement.class).set(resources, FakeData.SITE_A_ID);
	}
	
	public Placement getDefaultPlacementContext() {
		return currentPlacement;
	}
	
	public Set<Tool> findTools(Set categories, Set keywords) {
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
    public boolean isHidden(Placement placement) {
        return false;
    }

    public boolean allowTool(Site site, Placement placement) {
        return true;
    }
}
