/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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
package org.sakaiproject.roster.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>RosterMember</code> wraps together fields from <code>User</code>,
 * <code>Member</code>, and <code>CourseManagementService</code> for each
 * site member.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterMember {
	
	private String eId;
	private final String userId;
	private String displayId;
	private String displayName;
	private String sortName;
	private String email;
	private String role;
	private String status;
	private String credits;
	
	private Map<String, String> groups = new HashMap<String, String>();
	
	public RosterMember(String userId) {
		
		if (null == userId) {
			throw new IllegalArgumentException("must supply userId");
		}
		
		this.userId = userId;
	}
	
	public void addGroup(String groupId, String groupTitle) {
		
		if (null == groupId) {
			throw new IllegalArgumentException("groupId cannot be null");
		}

		groups.put(groupId, groupTitle);
	}
	
	public Map<String, String> getGroups() {
		return groups;
	}
	
	public String getGroupsToString() {
		
		StringBuilder groupsString = new StringBuilder();
		
		Iterator<String> iterator = groups.values().iterator();
		while (iterator.hasNext()) {
			groupsString.append(iterator.next());
			
			if (iterator.hasNext()) {
				groupsString.append(", ");
			}
		}
		return groupsString.toString();
	}

	public String getEid() {
		if (null == eId) {
			return userId;
		}
		
		return eId;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setEid(String eId) {
		this.eId = eId;
	}

	public String getDisplayId() {
		return displayId;
	}

	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCredits() {
		return credits;
	}

	public void setCredits(String credits) {
		this.credits = credits;
	}
	
}
