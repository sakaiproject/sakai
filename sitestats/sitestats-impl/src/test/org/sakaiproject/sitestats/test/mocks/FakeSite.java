package org.sakaiproject.sitestats.test.mocks;

import java.util.Collection;
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

public class FakeSite implements Site {
	private String id;
	private String title;
	private String toolId;

	public FakeSite(String id) {
		this.id = id;
		this.title = id;
	}
	
	public FakeSite(String id, String toolId) {
		this.id = id;
		this.title = id;
		this.toolId = toolId;
	}
	
	public Group addGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	public SitePage addPage() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getCreatedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getCreatedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public Group getGroup(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getGroupsWithMember(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getGroupsWithMemberHasRole(String arg0, String arg1) {
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

	public String getJoinerRole() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getModifiedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getModifiedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public List getOrderedPages() {
		// TODO Auto-generated method stub
		return null;
	}

	public SitePage getPage(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getPages() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSkin() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		return title;
	}

	public ToolConfiguration getTool(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ToolConfiguration getToolForCommonId(String toolId) {
		if(toolId != null && this.toolId != null) {
			return new FakeToolConfiguration();
		}else{
			return null;
		}
	}

	public Collection getTools(String[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getTools(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasGroups() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCustomPageOrdered() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isJoinable() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPubView() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPublished() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isType(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void loadAll() {
		// TODO Auto-generated method stub

	}

	public void regenerateIds() {
		// TODO Auto-generated method stub

	}

	public void removeGroup(Group arg0) {
		// TODO Auto-generated method stub

	}

	public void removePage(SitePage arg0) {
		// TODO Auto-generated method stub

	}

	public void setCustomPageOrdered(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setDescription(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setIconUrl(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setInfoUrl(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setJoinable(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setJoinerRole(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setPubView(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setPublished(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setShortDescription(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setSkin(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setTitle(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setType(String arg0) {
		// TODO Auto-generated method stub

	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isActiveEdit() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getId() {
		return id;
	}

	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Element toXml(Document arg0, Stack arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addMember(String arg0, String arg1, boolean arg2, boolean arg3) {
		// TODO Auto-generated method stub

	}

	public Role addRole(String arg0) throws RoleAlreadyDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Role addRole(String arg0, Role arg1) throws RoleAlreadyDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMaintainRole() {
		// TODO Auto-generated method stub
		return null;
	}

	public Member getMember(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProviderGroupId() {
		// TODO Auto-generated method stub
		return null;
	}

	public Role getRole(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getRolesIsAllowed(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Role getUserRole(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getUsersHasRole(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getUsersIsAllowed(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasRole(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAllowed(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean keepIntersection(AuthzGroup arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeMember(String arg0) {
		// TODO Auto-generated method stub

	}

	public void removeMembers() {
		// TODO Auto-generated method stub

	}

	public void removeRole(String arg0) {
		// TODO Auto-generated method stub

	}

	public void removeRoles() {
		// TODO Auto-generated method stub

	}

	public void setMaintainRole(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setProviderGroupId(String arg0) {
		// TODO Auto-generated method stub

	}

}
