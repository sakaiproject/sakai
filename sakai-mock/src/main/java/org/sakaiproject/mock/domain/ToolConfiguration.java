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

import java.util.Properties;

import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.tool.api.Tool;

public class ToolConfiguration implements org.sakaiproject.site.api.ToolConfiguration {
	
	private static final long serialVersionUID = 1L;

	String pageId;
	int pageOrder;
	String siteId;
	String context;
	String id;
	String title;
	String skin;
	
	Properties config;
	Properties placementConfig;
	
	Tool tool;
	
	
	Page page;
	
	public ToolConfiguration(Page page) {
		this.page = page;
	}

	public ToolConfiguration(String toolId, Page page) {
		this.id = toolId;
		this.page = page;
	}

	public SitePage getContainingPage() {
		return page;
	}


	public void moveDown() {
	}

	public void moveUp() {
	}

	public int[] parseLayoutHints() {
		return null;
	}

	public void setLayoutHints(String hints) {
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public int getPageOrder() {
		return pageOrder;
	}

	public void setPageOrder(int pageOrder) {
		this.pageOrder = pageOrder;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getSkin() {
		return skin;
	}

	public void setSkin(String skin) {
		this.skin = skin;
	}

	public String getLayoutHints() {
		return null;
	}

	public String getToolId() {
		return tool.getId();
	}

	public void save() {
	}

	public Properties getConfig() {
		return config;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Properties getPlacementConfig() {
		return placementConfig;
	}

	public void setPlacementConfig(Properties placementConfig) {
		this.placementConfig = placementConfig;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Tool getTool() {
		return tool;
	}

	public void setTool(Tool tool) {
		this.tool = tool;
	}

	public void setTool(String toolId, Tool tool) {
		this.tool = tool;
	}

}
