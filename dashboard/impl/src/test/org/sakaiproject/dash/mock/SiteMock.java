/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.mock;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author jimeng
 *
 */
public class SiteMock implements Site {
	
	protected String siteId;
	protected String siteTitle;
	protected String siteUrl;

	/**
	 * @param siteTitle TODO
	 * @param siteUrl TODO
	 * 
	 */
	public SiteMock(String siteId, String siteTitle, String siteUrl) {
		this.siteId = siteId;
		this.siteTitle = siteTitle;
		this.siteUrl = siteUrl;
	}

	public boolean isActiveEdit() {
		// TODO Auto-generated method stub
		return false;
	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl() {
		return this.siteUrl;
	}

	public String getReference() {
		// TODO Auto-generated method stub
		return SiteService.REFERENCE_ROOT + Site.SEPARATOR + siteId;
	}

	public String getUrl(String rootProperty) {
		return this.siteUrl;
	}

	public String getReference(String rootProperty) {
		return this.getReference();
	}

	public String getId() {
		return siteId;
	}

	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public Element toXml(Document doc, Stack<Element> stack) {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addMember(String userId, String roleId, boolean active,
			boolean provided) {
		// TODO Auto-generated method stub
		
	}

	public Role addRole(String id) throws RoleAlreadyDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Role addRole(String id, Role other)
			throws RoleAlreadyDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getCreatedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMaintainRole() {
		// TODO Auto-generated method stub
		return null;
	}

	public Member getMember(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Member> getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProviderGroupId() {
		// TODO Auto-generated method stub
		return null;
	}

	public Role getRole(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Role> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getRolesIsAllowed(String function) {
		// TODO Auto-generated method stub
		return null;
	}

	public Role getUserRole(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getUsersHasRole(String role) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getUsersIsAllowed(String function) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasRole(String userId, String role) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAllowed(String userId, String function) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeMember(String userId) {
		// TODO Auto-generated method stub
		
	}

	public void removeMembers() {
		// TODO Auto-generated method stub
		
	}

	public void removeRole(String role) {
		// TODO Auto-generated method stub
		
	}

	public void removeRoles() {
		// TODO Auto-generated method stub
		
	}

	public void setMaintainRole(String role) {
		// TODO Auto-generated method stub
		
	}

	public void setProviderGroupId(String id) {
		// TODO Auto-generated method stub
		
	}

	public boolean keepIntersection(AuthzGroup other) {
		// TODO Auto-generated method stub
		return false;
	}

	public User getCreatedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getModifiedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getCreatedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getModifiedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		return this.siteTitle;
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getIconUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getIconUrlFull() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getInfoUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getInfoUrlFull() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isJoinable() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getJoinerRole() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSkin() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SitePage> getPages() {
		// TODO Auto-generated method stub
		return null;
	}

	public void loadAll() {
		// TODO Auto-generated method stub
		
	}

	public List<SitePage> getOrderedPages() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isPublished() {
		// TODO Auto-generated method stub
		return false;
	}

	public SitePage getPage(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public ToolConfiguration getTool(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<ToolConfiguration> getTools(String[] toolIds) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<ToolConfiguration> getTools(String commonToolId) {
		// TODO Auto-generated method stub
		return null;
	}

	public ToolConfiguration getToolForCommonId(String commonToolId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isType(Object type) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPubView() {
		// TODO Auto-generated method stub
		return false;
	}

	public Group getGroup(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Group> getGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Group> getGroupsWithMember(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Group> getGroupsWithMembers(String [] userIds) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Group> getGroupsWithMemberHasRole(String userId,
			String role) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getMembersInGroups(Set<String> groupIds) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHtmlDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getHtmlShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean hasGroups() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	public void setIconUrl(String url) {
		// TODO Auto-generated method stub
		
	}

	public void setInfoUrl(String url) {
		// TODO Auto-generated method stub
		
	}

	public void setJoinable(boolean joinable) {
		// TODO Auto-generated method stub
		
	}

	public void setJoinerRole(String role) {
		// TODO Auto-generated method stub
		
	}

	public void setShortDescription(String description) {
		// TODO Auto-generated method stub
		
	}

	public void setDescription(String description) {
		// TODO Auto-generated method stub
		
	}

	public void setPublished(boolean published) {
		// TODO Auto-generated method stub
		
	}

	public void setSkin(String skin) {
		// TODO Auto-generated method stub
		
	}

	public SitePage addPage() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removePage(SitePage page) {
		// TODO Auto-generated method stub
		
	}

	public void regenerateIds() {
		// TODO Auto-generated method stub
		
	}

	public void setType(String type) {
		// TODO Auto-generated method stub
		
	}

	public void setPubView(boolean pubView) {
		// TODO Auto-generated method stub
		
	}

	public Group addGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeGroup(Group group) {
		// TODO Auto-generated method stub
		
	}

	public boolean isCustomPageOrdered() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCustomPageOrdered(boolean custom) {
		// TODO Auto-generated method stub
		
	}

	public boolean isSoftlyDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	public Date getSoftlyDeletedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSoftlyDeleted(boolean flag) {
		// TODO Auto-generated method stub
		
	}
	

}
