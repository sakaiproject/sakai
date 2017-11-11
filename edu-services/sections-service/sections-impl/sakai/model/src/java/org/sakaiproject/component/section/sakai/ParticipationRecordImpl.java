/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.component.section.sakai;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sakaiproject.section.api.coursemanagement.LearningContext;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;

/**
 * A base class of ParticipationRecords for detachable persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public abstract class ParticipationRecordImpl implements ParticipationRecord, Serializable {

	private static final long serialVersionUID = 1L;
	protected User user;
	protected String userUid;
	protected LearningContext learningContext;

	public User getUser() {
		return user;
	}
	
	/**
	 * Returns null, since this is an unknown type of participant
	 */
	public Role getRole() {
		return null;
	}
	
	public String getUserUid() {
		return userUid;
	}

	public void setUserUid(String userUid) {
		this.userUid = userUid;
	}

	public LearningContext getLearningContext() {
		return learningContext;
	}
	public void setLearningContext(LearningContext learningContext) {
		this.learningContext = learningContext;
	}

	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof ParticipationRecord) {
			ParticipationRecordImpl other = (ParticipationRecordImpl)o;
			return new EqualsBuilder()
				.append(userUid, other.getUserUid())
				.append(learningContext, other.getLearningContext())
				.isEquals();
		}
		return false;
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(userUid)
			.append(learningContext)
			.toHashCode();
	}
	
	public String toString() {
		return new ToStringBuilder(this).append(userUid)
		.append(learningContext).toString();
	}
}
