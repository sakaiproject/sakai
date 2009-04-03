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

import org.sakaiproject.authz.api.Role;

public class Member implements org.sakaiproject.authz.api.Member {
	private static final long serialVersionUID = 1L;

	Role role;
	String userDisplayId;
	String userEid;
	String userId;
	boolean active;
	boolean provided;

	public int compareTo(Object o) {
		return userId.compareTo(((Member)o).userId);
	}


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}


	public boolean isProvided() {
		return provided;
	}


	public void setProvided(boolean provided) {
		this.provided = provided;
	}


	public Role getRole() {
		return role;
	}


	public void setRole(Role role) {
		this.role = role;
	}


	public String getUserDisplayId() {
		return userDisplayId;
	}


	public void setUserDisplayId(String userDisplayId) {
		this.userDisplayId = userDisplayId;
	}


	public String getUserEid() {
		return userEid;
	}


	public void setUserEid(String userEid) {
		this.userEid = userEid;
	}


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}

}
