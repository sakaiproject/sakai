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
package org.sakaiproject.sitestats.impl.parser;

import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.ToolFactory;
import org.xml.sax.Attributes;


public class ToolFactoryImpl implements ToolFactory, ObjectCreationFactory {

	public ToolInfo createTool(String toolId) {
		return new ToolInfo(toolId);
	}
	
	public ToolInfo createTool(String toolId, List<String> additionalToolIds) {
		return new ToolInfo(toolId, additionalToolIds);
	}
	
	public Object createObject(Attributes attributes) throws Exception {
		String toolId = attributes.getValue("toolId");
		String selected = attributes.getValue("selected");
		String additionalToolIds = attributes.getValue("additionalToolIds");

		if(toolId == null){ throw new Exception("Mandatory toolId attribute not present on tool tag."); }
		ToolInfo toolInfo = new ToolInfo(toolId.trim());
		toolInfo.setSelected(Boolean.parseBoolean(selected));
		if(additionalToolIds != null) {
			toolInfo.setAdditionalToolIdsStr(additionalToolIds);
		}
		return toolInfo;
	}

	public Digester getDigester() {
		return null;
	}

	public void setDigester(Digester digester) {
	}

}
