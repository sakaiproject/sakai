/**
 * 
 */
package org.sakaiproject.roster.api;

import java.util.List;

/**
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterGroup {

	private String groupId;
	private String groupTitle;
	private List<String> userIds;
	
	public RosterGroup(String groupId, String groupTitle, List<String> userIds) {

		this.groupId = groupId;
		this.groupTitle = groupTitle;
		this.userIds = userIds;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupTitle() {
		return groupTitle;
	}

	public void setGroupTitle(String groupTitle) {
		this.groupTitle = groupTitle;
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}
	
	
}
