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
	
	private String userId;
	private String displayId;
	private String displayName;
	private String sortName;
	private String email;
	private String role;
	private String status;
	private String credits;
	
	private Map<String, String> groups = new HashMap<String, String>();
	
	public RosterMember() {
		
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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
