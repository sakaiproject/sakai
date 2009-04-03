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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AuthzGroup implements org.sakaiproject.authz.api.AuthzGroup {
	private static final long serialVersionUID = 1L;

	String id;
	String description;
	String providerGroupId;
	String maintainRole;

	User createdBy;
	User modifiedBy;
	
	Time createdTime;
	Time modifiedTime;
	
	Map<String, Member> members;
	Map<String, Role> roles;
	Map<String, Set<String>> rolesIsAllowed;

	String reference;
	ResourcePropertiesEdit propertiesEdit;
	ResourceProperties properties;
	
	public AuthzGroup() {
		members = new HashMap<String, Member>();
		roles = new HashMap<String, Role>();
		rolesIsAllowed = new HashMap<String, Set<String>>();
	}
	
	public void addMember(String userId, String roleId, boolean active,
			boolean provided) {
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

	public Member getMember(String userId) {
		return members.get(userId);
	}

	public Set getMembers() {
		Set mbrs = new HashSet();
		Set entries = members.entrySet();
		for(Iterator<Entry<String, Member>> iter = entries.iterator(); iter.hasNext();) {
			mbrs.add(iter.next().getValue());
		}
		return mbrs;
	}

	public Role getRole(String id) {
		return roles.get(id);
	}

	public Set getRoles() {
		Set rls = new HashSet();
		Set entries = roles.entrySet();
		for(Iterator<Entry<String, Role>> iter = entries.iterator(); iter.hasNext();) {
			rls.add(iter.next().getValue());
		}
		return rls;
	}

	public Set getRolesIsAllowed(String function) {
		return rolesIsAllowed.get(function);
	}

	public Role getUserRole(String userId) {
		Set entries = members.entrySet();
		for(Iterator<Entry<String, Member>> iter = entries.iterator(); iter.hasNext();) {
			Member mbr = iter.next().getValue();
			if(userId.equals(mbr.getUserId())) return mbr.getRole();
		}
		return null;
	}

	public Set getUsers() {
		Set entries = members.entrySet();
		Set<String> userIds = new HashSet<String>();
		for(Iterator<Entry<String, Member>> iter = entries.iterator(); iter.hasNext();) {
			userIds.add(iter.next().getValue().getUserId());
		}
		return userIds;
	}

	public Set getUsersHasRole(String role) {
		Set entries = members.entrySet();
		Set<String> userIds = new HashSet<String>();
		for(Iterator<Entry<String, Member>> iter = entries.iterator(); iter.hasNext();) {
			Member member = iter.next().getValue();
			if(member.getRole().toString().equals(role)) {
				userIds.add(member.getUserId());
			}
		}
		return userIds;
	}

	public Set getUsersIsAllowed(String function) {
		Set entries = members.entrySet();
		Set<String> userIds = new HashSet<String>();
		for(Iterator<Entry<String, Member>> iter = entries.iterator(); iter.hasNext();) {
			Member member = iter.next().getValue();
			if(rolesIsAllowed.containsKey(member.getRole().getId())) {
				userIds.add(member.getUserId());
			}
		}
		return userIds;
	}

	public boolean hasRole(String userId, String role) {
		Member member = members.get(userId);
		if(member != null) {
			return member.getRole().getId().equals(role);
		}
		return false;
	}

	public boolean isAllowed(String userId, String function) {
		Member member = members.get(userId);
		Set functions = rolesIsAllowed.get(member.getRole().getId());
		return functions != null && functions.contains(function);
	}

	public boolean isEmpty() {
		return members.isEmpty();
	}

	public boolean keepIntersection(org.sakaiproject.authz.api.AuthzGroup other) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeMember(String userId) {
		members.remove(userId);
	}

	public void removeMembers() {
		members.clear();
	}

	public void removeRole(String role) {
		roles.remove(role);
	}

	public void removeRoles() {
		roles.clear();
	}

	public boolean isActiveEdit() {
		return false;
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

	public int compareTo(Object o) {
		return 0;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public Time getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Time createdTime) {
		this.createdTime = createdTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMaintainRole() {
		return maintainRole;
	}

	public void setMaintainRole(String maintainRole) {
		this.maintainRole = maintainRole;
	}

	public User getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(User modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Time getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Time modifiedTime) {
		this.modifiedTime = modifiedTime;
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

	public String getProviderGroupId() {
		return providerGroupId;
	}

	public void setProviderGroupId(String providerGroupId) {
		this.providerGroupId = providerGroupId;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Map<String, Set<String>> getRolesIsAllowed() {
		return rolesIsAllowed;
	}

	public void setRolesIsAllowed(Map<String, Set<String>> rolesIsAllowed) {
		this.rolesIsAllowed = rolesIsAllowed;
	}

	public void setMembers(Map<String, Member> members) {
		this.members = members;
	}

	public void setRoles(Map<String, Role> roles) {
		this.roles = roles;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
