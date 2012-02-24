/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.coursemanagement.impl;

import java.io.Serializable;

import org.sakaiproject.coursemanagement.api.Membership;

public class MembershipCmImpl extends AbstractPersistentCourseManagementObjectCmImpl
	implements Membership, Serializable {

	private static final long serialVersionUID = 1L;

	private String userId;
	private String role;
	private AbstractMembershipContainerCmImpl memberContainer;
	private String status;
	
	public MembershipCmImpl() {}
	
    public MembershipCmImpl(String userId, String role, AbstractMembershipContainerCmImpl memberContainer,
                            String status) {
		this.userId = userId;
		this.role = role;
		this.memberContainer = memberContainer;
                this.status = status;
	}
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public AbstractMembershipContainerCmImpl getMemberContainer() {
		return memberContainer;
	}

	public void setMemberContainer(AbstractMembershipContainerCmImpl memberContainer) {
		this.memberContainer = memberContainer;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
