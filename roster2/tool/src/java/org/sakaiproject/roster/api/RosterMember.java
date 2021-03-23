/**
 * Copyright (c) 2010-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.api;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;

/**
 * <code>RosterMember</code> wraps together fields from <code>User</code>,
 * <code>Member</code>, and <code>CourseManagementService</code> for each
 * site member.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@Data
public class RosterMember {

	private final String userId;
	private String eid;
	private String displayId;
	private String displayName;
	private String sortName;
	private String email;
	private String role;
	private String enrollmentStatusId;
	private String enrollmentStatusText;
	private String credits;
	private String pronouns;
	private String pronunciation;
	private Map<String, String> groups = new HashMap<String, String>();
	private Map<String, String> userProperties = new HashMap<>();
	private int connectionStatus; // connection status to the current user
	private int totalSiteVisits;
	private long lastVisitTime;

	public void addGroup(String groupId, String groupTitle) {

		if (null == groupId) {
			throw new IllegalArgumentException("groupId cannot be null");
		}

		groups.put(groupId, groupTitle);
	}

	public String getGroupsToString() {
		return groups.values().stream().collect(Collectors.joining(","));
	}

	public String getEid() {
		return eid == null ? userId : eid;
	}
}
