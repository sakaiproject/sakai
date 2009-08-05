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
package org.sakaiproject.mock.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Page implements SitePage {
	private static final long serialVersionUID = 1L;

	Site site;
	String id;
	String title;
	boolean titleCustom;
	String reference;
	boolean popUp;
	boolean activeEdit;
	int layout;
	int position;
	
	List<ToolConfiguration> tools;

	ResourcePropertiesEdit propertiesEdit;
	ResourceProperties properties;
	
	public Page(Site site) {
		this.site = site;
	}
	
	public ToolConfiguration addTool() {
		ToolConfiguration tool = new org.sakaiproject.mock.domain.ToolConfiguration(this);
		tools.add(tool);
		return tool;
	}

	public ToolConfiguration addTool(String toolId) {
		ToolConfiguration tool = new org.sakaiproject.mock.domain.ToolConfiguration(toolId, this);
		tools.add(tool);
		return tool;
	}

	public Site getContainingSite() {
		return site;
	}

	public String getLayoutTitle() {
		return "layout title";
	}

	public String getSiteId() {
		return site.getId();
	}

	public String getSkin() {
		return site.getSkin();
	}


	public ToolConfiguration getTool(String id) {
		for(Iterator<ToolConfiguration> iter = tools.iterator(); iter.hasNext();) {
			ToolConfiguration tc = iter.next();
			if(tc.getId().equals(id)) return tc;
		}
		return null;
	}

	public List getTools() {
		return tools;
	}

	public List getTools(int col) {
		return tools;
	}

	public Collection getTools(String[] toolIds) {
		List toolIdList = Arrays.asList(toolIds);
		Collection specificTools = new HashSet();
		for(Iterator<ToolConfiguration> iter = tools.iterator(); iter.hasNext();) {
			ToolConfiguration tc = iter.next();
			if(toolIdList.contains(tc.getId())) specificTools.add(tc); 
		}
		return specificTools;
	}

	public void moveDown() {
	}

	public void moveUp() {
	}

	public void removeTool(ToolConfiguration tool) {
		tools.remove(tool);
	}

	public String getReference(String rootProperty) {
		return reference;
	}

	public String getUrl() {
		return null;
	}

	public String getUrl(String rootProperty) {
		return null;
	}

	public Element toXml(Document doc, Stack stack) {
		return null;
	}

	public ToolConfiguration addTool(Tool reg) {
		ToolConfiguration tc = new org.sakaiproject.mock.domain.ToolConfiguration(this);
		tools.add(tc);
		return tc;
	}

	public boolean isPopUp() {
		return popUp;
	}

	public void setPopUp(boolean popUp) {
		this.popUp = popUp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLayout() {
		return layout;
	}

	public void setLayout(int layout) {
		this.layout = layout;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public ResourceProperties getProperties() {
		return properties;
	}

	public void setProperties(ResourceProperties properties) {
		this.properties = properties;
	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		return propertiesEdit;
	}

	public void setPropertiesEdit(ResourcePropertiesEdit propertiesEdit) {
		this.propertiesEdit = propertiesEdit;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public String getTitle() {
		return title;
	}

	public void setTitleCustom(boolean custom) {
		this.titleCustom = custom;
	}

	public boolean getTitleCustom() {
		return titleCustom;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void localizePage() {
	}
	
	public void setTools(List<ToolConfiguration> tools) {
		this.tools = tools;
	}

	public void setPopup(boolean popUp) {
		this.popUp = popUp;
	}

	public boolean isActiveEdit() {
		return activeEdit;
	}

	public void setActiveEdit(boolean activeEdit) {
		this.activeEdit = activeEdit;
	}

	public void setupPageCategory(String toolId)
	{
		// TODO Auto-generated method stub
		
	}

}
