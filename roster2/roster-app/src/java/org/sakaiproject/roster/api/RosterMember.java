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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <code>RosterMember</code> wraps together fields from <code>User</code>,
 * <code>Member</code>, and <code>CourseManagementService</code> for each
 * site member.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@RequiredArgsConstructor
public class RosterMember {
	
	@Getter
	private final String userId;
	@Setter
	private String eid;
	@Getter @Setter
	private String displayId;
	@Getter @Setter
	private String displayName;
	@Getter @Setter
	private String sortName;
	@Getter @Setter
	private String email;
	@Getter @Setter
	private String role;
	@Getter @Setter
	private String status;
	@Getter @Setter
	private String credits;
	
	@Getter
	private Map<String, String> groups = new HashMap<String, String>();
		
	public void addGroup(String groupId, String groupTitle) {
		
		if (null == groupId) {
			throw new IllegalArgumentException("groupId cannot be null");
		}

		groups.put(groupId, groupTitle);
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
		if (null == eid) {
			return userId;
		}
		
		return eid;
	}
	
}
