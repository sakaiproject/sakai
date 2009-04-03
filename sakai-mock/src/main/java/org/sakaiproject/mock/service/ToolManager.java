/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mock.service;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.w3c.dom.Document;

public class ToolManager implements org.sakaiproject.tool.api.ToolManager {
	Set<Tool> tools;
	Placement currentPlacement;

	public ToolManager(org.sakaiproject.mock.domain.Placement placement) {
		org.sakaiproject.mock.domain.Tool someTool = new org.sakaiproject.mock.domain.Tool();
		someTool.setId("mock.tool");
		someTool.setTitle("Mock Tool");
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
