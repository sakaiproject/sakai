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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;

public class Site extends AuthzGroup implements org.sakaiproject.site.api.Site {
	private static final long serialVersionUID = 1L;

	List<org.sakaiproject.mock.domain.Group> groups;
	List<Page> pages;
	List<org.sakaiproject.mock.domain.ToolConfiguration> tools;

	String title;
	String type;
	String skin;
	String shortDescription;
	String infoUrl;
	String iconUrl;
	String joinerRole;
	
	boolean pubView;
	boolean joinable;
	boolean published;

	public Site() {}
	
	public Site(String id, String type) {
		this.id = id;
		this.type = type;
	}
	
	public Group addGroup() {
		org.sakaiproject.mock.domain.Group group = new org.sakaiproject.mock.domain.Group(this);
		groups.add(group);
		return group;
	}

	public Page addPage() {
		Page page = new Page(this);
		pages.add(page);
		return page;
	}

	public Group getGroup(String id) {
		for(Iterator<org.sakaiproject.mock.domain.Group> iter = groups.iterator(); iter.hasNext();) {
			Group group = iter.next();
			if(group.getId().equals(id)) return group;
		}
		return null;
	}

	public Collection getGroups() {
		return groups;
	}

	public Collection getGroupsWithMember(String userId) {
		Set groupsWithMember = new HashSet();
		for(Iterator<org.sakaiproject.mock.domain.Group> iter = groups.iterator(); iter.hasNext();) {
			org.sakaiproject.mock.domain.Group group = iter.next();
			if(group.members.get(userId) != null) {
				groupsWithMember.add(group);
			}
		}
		return groupsWithMember;
	}

	public Collection getGroupsWithMemberHasRole(String userId, String role) {
		Set groupsWithMember = new HashSet();
		for(Iterator<org.sakaiproject.mock.domain.Group> iter = groups.iterator(); iter.hasNext();) {
			org.sakaiproject.mock.domain.Group group = iter.next();
			org.sakaiproject.authz.api.Member member = group.members.get(userId);
			if(member != null && member.getRole().getId().equals(role)) {
				groupsWithMember.add(group);
			}
		}
		return groupsWithMember;
	}

	public String getIconUrlFull() {
		return iconUrl;
	}

	public String getInfoUrlFull() {
		return infoUrl;
	}

	public List getOrderedPages() {
		return pages;
	}

	public SitePage getPage(String id) {
		for(Iterator<Page> iter = pages.iterator(); iter.hasNext();) {
			Page page = iter.next();
			if(page.getId().equals(id)) return page;
		}
		return null;
	}

	public List getPages() {
		return pages;
	}

	public ToolConfiguration getTool(String id) {
		for(Iterator<org.sakaiproject.mock.domain.ToolConfiguration> iter = tools.iterator(); iter.hasNext();) {
			org.sakaiproject.mock.domain.ToolConfiguration tc = iter.next();
			if(tc.getId().equals(id)) return tc;
		}
		return null;
	}

	public ToolConfiguration getToolForCommonId(String commonToolId) {
		return getTool(commonToolId);
	}

	public Collection getTools(String[] toolIds) {
		Set<String> toolSet = new HashSet<String>();
		for(Iterator<org.sakaiproject.mock.domain.ToolConfiguration> iter = tools.iterator(); iter.hasNext();) {
			org.sakaiproject.mock.domain.ToolConfiguration tc = iter.next();
			if(tc.getId().equals(id)) {
				toolSet.add(id);
			}
		}
		return toolSet;
	}

	public Collection getTools(String commonToolId) {
		return tools;
	}

	public boolean hasGroups() {
		return groups != null && groups.size() > 0;
	}

	public boolean isCustomPageOrdered() {
		return false;
	}

	public void setCustomPageOrdered(boolean custom) {
	}

	public boolean isType(Object type) {
		return type.equals(this.type);
	}

	public void loadAll() {
		// lazy loading is not necessary
	}

	public void regenerateIds() {
	}

	public void removeGroup(Group group) {
		groups.remove(group);
	}

	public void removePage(SitePage page) {
		pages.remove(page);
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getInfoUrl() {
		return infoUrl;
	}

	public void setInfoUrl(String infoUrl) {
		this.infoUrl = infoUrl;
	}

	public boolean isJoinable() {
		return joinable;
	}

	public void setJoinable(boolean joinable) {
		this.joinable = joinable;
	}

	public String getJoinerRole() {
		return joinerRole;
	}

	public void setJoinerRole(String joinerRole) {
		this.joinerRole = joinerRole;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isPubView() {
		return pubView;
	}

	public void setPubView(boolean pubView) {
		this.pubView = pubView;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getSkin() {
		return skin;
	}

	public void setSkin(String skin) {
		this.skin = skin;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<org.sakaiproject.mock.domain.ToolConfiguration> getTools() {
		return tools;
	}

	public void setTools(List<org.sakaiproject.mock.domain.ToolConfiguration> tools) {
		this.tools = tools;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setGroups(List<org.sakaiproject.mock.domain.Group> groups) {
		this.groups = groups;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

}
