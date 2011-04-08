/******************************************************************************
 * TestSite.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2006 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.logic.stubs;

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
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test class for the Sakai Site object<br/>
 * This has to be here since I cannot create a Site object in Sakai for some 
 * reason... sure would be nice if I could though -AZ
 * @author Sakai App Builder -AZ
 */
@SuppressWarnings("unchecked")
public class FakeSite implements Site {

	private static final long serialVersionUID = 4761288804996964705L;
	private String id;
	private String title = "Title";
	private String reference = "/site/default";

   public FakeSite() { }

	public FakeSite(String id, String title, String reference) {
		this.id = id;
		this.title = title;
		this.reference = reference;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#addGroup()
	 */
	public Group addGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#addPage()
	 */
	public SitePage addPage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getCreatedBy()
	 */
	public User getCreatedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getCreatedTime()
	 */
	public Time getCreatedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getDescription()
	 */
	public String getDescription() {
		return "Description";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getGroup(java.lang.String)
	 */
	public Group getGroup(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getGroups()
	 */
	public Collection getGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getGroupsWithMember(java.lang.String)
	 */
	public Collection getGroupsWithMember(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getGroupsWithMemberHasRole(java.lang.String, java.lang.String)
	 */
	public Collection getGroupsWithMemberHasRole(String userId, String role) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getIconUrl()
	 */
	public String getIconUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getIconUrlFull()
	 */
	public String getIconUrlFull() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getInfoUrl()
	 */
	public String getInfoUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getInfoUrlFull()
	 */
	public String getInfoUrlFull() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getJoinerRole()
	 */
	public String getJoinerRole() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getModifiedBy()
	 */
	public User getModifiedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getModifiedTime()
	 */
	public Time getModifiedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getOrderedPages()
	 */
	public List getOrderedPages() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getPage(java.lang.String)
	 */
	public SitePage getPage(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getPages()
	 */
	public List getPages() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getShortDescription()
	 */
	public String getShortDescription() {
		return "Short desc";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getSkin()
	 */
	public String getSkin() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getTool(java.lang.String)
	 */
	public ToolConfiguration getTool(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getToolForCommonId(java.lang.String)
	 */
	public ToolConfiguration getToolForCommonId(String commonToolId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getTools(java.lang.String[])
	 */
	public Collection getTools(String[] toolIds) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getTools(java.lang.String)
	 */
	public Collection getTools(String commonToolId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#getType()
	 */
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#hasGroups()
	 */
	public boolean hasGroups() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#isJoinable()
	 */
	public boolean isJoinable() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#isPubView()
	 */
	public boolean isPubView() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#isPublished()
	 */
	public boolean isPublished() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#isType(java.lang.Object)
	 */
	public boolean isType(Object type) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#loadAll()
	 */
	public void loadAll() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#regenerateIds()
	 */
	public void regenerateIds() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#removeGroup(org.sakaiproject.site.api.Group)
	 */
	public void removeGroup(Group group) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#removePage(org.sakaiproject.site.api.SitePage)
	 */
	public void removePage(SitePage page) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setIconUrl(java.lang.String)
	 */
	public void setIconUrl(String url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setInfoUrl(java.lang.String)
	 */
	public void setInfoUrl(String url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setJoinable(boolean)
	 */
	public void setJoinable(boolean joinable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setJoinerRole(java.lang.String)
	 */
	public void setJoinerRole(String role) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setPubView(boolean)
	 */
	public void setPubView(boolean pubView) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setPublished(boolean)
	 */
	public void setPublished(boolean published) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setShortDescription(java.lang.String)
	 */
	public void setShortDescription(String description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setSkin(java.lang.String)
	 */
	public void setSkin(String skin) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.Site#setType(java.lang.String)
	 */
	public void setType(String type) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Edit#getPropertiesEdit()
	 */
	public ResourcePropertiesEdit getPropertiesEdit() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Edit#isActiveEdit()
	 */
	public boolean isActiveEdit() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getProperties()
	 */
	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getReference()
	 */
	public String getReference() {
		return reference;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getReference(java.lang.String)
	 */
	public String getReference(String rootProperty) {
		return reference;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getUrl()
	 */
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getUrl(java.lang.String)
	 */
	public String getUrl(String rootProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#toXml(org.w3c.dom.Document, java.util.Stack)
	 */
	public Element toXml(Document doc, Stack stack) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#addMember(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public void addMember(String userId, String roleId, boolean active,
			boolean provided) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#addRole(java.lang.String)
	 */
	public Role addRole(String id) throws RoleAlreadyDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#addRole(java.lang.String, org.sakaiproject.authz.api.Role)
	 */
	public Role addRole(String id, Role other)
			throws RoleAlreadyDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getMaintainRole()
	 */
	public String getMaintainRole() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getMember(java.lang.String)
	 */
	public Member getMember(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getMembers()
	 */
	public Set getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getProviderGroupId()
	 */
	public String getProviderGroupId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getRole(java.lang.String)
	 */
	public Role getRole(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getRoles()
	 */
	public Set getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getRolesIsAllowed(java.lang.String)
	 */
	public Set getRolesIsAllowed(String function) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getUserRole(java.lang.String)
	 */
	public Role getUserRole(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getUsers()
	 */
	public Set getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getUsersHasRole(java.lang.String)
	 */
	public Set getUsersHasRole(String role) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#getUsersIsAllowed(java.lang.String)
	 */
	public Set getUsersIsAllowed(String function) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#hasRole(java.lang.String, java.lang.String)
	 */
	public boolean hasRole(String userId, String role) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#isAllowed(java.lang.String, java.lang.String)
	 */
	public boolean isAllowed(String userId, String function) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#isEmpty()
	 */
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#keepIntersection(org.sakaiproject.authz.api.AuthzGroup)
	 */
	public boolean keepIntersection(AuthzGroup other) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#removeMember(java.lang.String)
	 */
	public void removeMember(String userId) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#removeMembers()
	 */
	public void removeMembers() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#removeRole(java.lang.String)
	 */
	public void removeRole(String role) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#removeRoles()
	 */
	public void removeRoles() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#setMaintainRole(java.lang.String)
	 */
	public void setMaintainRole(String role) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.AuthzGroup#setProviderGroupId(java.lang.String)
	 */
	public void setProviderGroupId(String id) {
		// TODO Auto-generated method stub

	}

	public boolean isCustomPageOrdered() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCustomPageOrdered(boolean custom) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Date getCreatedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSoftlyDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Date getSoftlyDeletedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSoftlyDeleted(boolean flag) {
		// TODO Auto-generated method stub
		
	}

}
