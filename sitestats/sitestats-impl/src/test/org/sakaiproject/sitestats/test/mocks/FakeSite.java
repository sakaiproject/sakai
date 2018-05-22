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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;

public abstract class FakeSite implements Site {
	private String id;
	private String title;
	private List<String> toolIds = new ArrayList<String>();
	private Map<String,SitePage> pages = new HashMap<String,SitePage>();
	
	private Time createdTime;
	private Set<String> users;
	private Set members;

	public FakeSite set(String id) {
		set(id, new ArrayList<String>());
		return this;
	}
	
	public FakeSite set(String id, String toolId) {
		set(id, Arrays.asList(toolId));
		return this;
	}
	
	public FakeSite set(String id, List<String> toolIds) {
		this.id = id;
		this.title = id;
		this.toolIds = toolIds;
		for(String tId : toolIds) {
			addPage(tId);
		}
		return this;
	}

	public SitePage addPage() {
		return Mockito.spy(FakeSitePage.class).set(id, null);
	}
	
	public void addPage(String toolId) {
		pages.put(toolId, Mockito.spy(FakeSitePage.class).set(id, toolId));
	}

	public Time getCreatedTime() {
		return createdTime;
	}
	
	public Date getCreatedDate() {
		return new Date(createdTime.getTime());
	}
	
	public void setCreatedTime(Time time) {
		createdTime = time;
	}

	public SitePage getPage(String id) {
		return pages.get(id);
	}

	public List getPages() {
		return new ArrayList(pages.values());
	}

	public String getTitle() {
		return title;
	}

	public ToolConfiguration getTool(String id) {
		return null;
	}

	public ToolConfiguration getToolForCommonId(String toolId) {
		if(toolId != null && toolIds.contains(toolId)) {
			return Mockito.spy(FakeToolConfiguration.class).set(toolId);
		}else{
			return null;
		}
	}

	public String getId() {
		return id;
	}

	public Set getMembers() {
		return members;
	}
	public void setMembers(Set members) {
		this.members = members;
	}

	public Set<String> getUsers() {
		return users;
	}
	public void setUsers(Set<String> users) {
		this.users = users;
	}

}
