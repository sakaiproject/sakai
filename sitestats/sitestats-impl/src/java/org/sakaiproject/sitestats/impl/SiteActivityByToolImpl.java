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
package org.sakaiproject.sitestats.impl;

import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.event.ToolInfo;

public class SiteActivityByToolImpl implements SiteActivityByTool {
	private String siteId	= null;
	private ToolInfo toolInfo 		= null;
	private long count		= 0;

	public long getCount() {
		return count;
	}

	public String getSiteId() {
		return siteId;
	}

	public ToolInfo getTool() {
		return toolInfo;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public void setTool(ToolInfo toolInfo) {
		this.toolInfo = toolInfo;
	}

	
	public String toString(){
		return siteId + " : " + toolInfo.getToolId() + " : " + count;
	}
}
