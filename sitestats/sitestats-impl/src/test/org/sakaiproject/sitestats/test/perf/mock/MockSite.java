package org.sakaiproject.sitestats.test.perf.mock;

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

public class MockSite implements Site {

	public MockSite(String id) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isActiveEdit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ResourcePropertiesEdit getPropertiesEdit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl(String rootProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReference(String rootProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element toXml(Document doc, Stack<Element> stack) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addMember(String userId, String roleId, boolean active,
			boolean provided) {
		// TODO Auto-generated method stub

	}

	@Override
	public Role addRole(String id) throws RoleAlreadyDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Role addRole(String id, Role other)
			throws RoleAlreadyDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getCreatedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMaintainRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Member getMember(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Member> getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProviderGroupId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Role getRole(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Role> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getRolesIsAllowed(String function) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Role getUserRole(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getUsersHasRole(String role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getUsersIsAllowed(String function) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasRole(String userId, String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAllowed(String userId, String function) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeMember(String userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeMembers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRole(String role) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRoles() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaintainRole(String role) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProviderGroupId(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean keepIntersection(AuthzGroup other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public User getCreatedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getModifiedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Time getCreatedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Time getModifiedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHtmlShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHtmlDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIconUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIconUrlFull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfoUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfoUrlFull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isJoinable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getJoinerRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSkin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SitePage> getPages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<SitePage> getOrderedPages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPublished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SitePage getPage(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ToolConfiguration getTool(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ToolConfiguration> getTools(String[] toolIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ToolConfiguration> getTools(String commonToolId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ToolConfiguration getToolForCommonId(String commonToolId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isType(Object type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPubView() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Group getGroup(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Group> getGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Group> getGroupsWithMember(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Group> getGroupsWithMembers(String [] userIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Group> getGroupsWithMemberHasRole(String userId,
			String role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getMembersInGroups(Set<String> groupIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasGroups() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIconUrl(String url) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInfoUrl(String url) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setJoinable(boolean joinable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setJoinerRole(String role) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setShortDescription(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPublished(boolean published) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSkin(String skin) {
		// TODO Auto-generated method stub

	}

	@Override
	public SitePage addPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removePage(SitePage page) {
		// TODO Auto-generated method stub

	}

	@Override
	public void regenerateIds() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setType(String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPubView(boolean pubView) {
		// TODO Auto-generated method stub

	}

	@Override
	public Group addGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeGroup(Group group) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCustomPageOrdered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCustomPageOrdered(boolean custom) {
		// TODO Auto-generated method stub

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
