/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import java.util.Properties;

import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;

public class FakeToolConfiguration implements ToolConfiguration {
	private String commonToolId;
	
	public FakeToolConfiguration(String commonToolId) {
		this.commonToolId = commonToolId;		
	}

	public SitePage getContainingPage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLayoutHints() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPageId() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPageOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getSiteId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSkin() {
		// TODO Auto-generated method stub
		return null;
	}

	public void moveDown() {
		// TODO Auto-generated method stub

	}

	public void moveUp() {
		// TODO Auto-generated method stub

	}

	public int[] parseLayoutHints() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLayoutHints(String arg0) {
		// TODO Auto-generated method stub

	}

	public Properties getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getPlacementConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	public Tool getTool() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolId() {
		return commonToolId;
	}

	public void save() {
		// TODO Auto-generated method stub

	}

	public void setTitle(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setTool(String arg0, Tool arg1) {
		// TODO Auto-generated method stub

	}

}
