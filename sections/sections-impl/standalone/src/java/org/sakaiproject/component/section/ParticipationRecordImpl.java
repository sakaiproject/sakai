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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.section.api.coursemanagement.LearningContext;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;

/**
 * A base class of ParticipationRecords for detachable persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public abstract class ParticipationRecordImpl extends AbstractPersistentObject
	implements ParticipationRecord, Serializable {
	
	protected User user;
	protected LearningContext learningContext;

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
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
			ParticipationRecord other = (ParticipationRecord)o;
			return new EqualsBuilder()
				.append(user, other.getUser())
				.append(learningContext, other.getLearningContext())
				.isEquals();
		}
		return false;
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(user)
			.append(learningContext)
			.toHashCode();
	}
	
	public String toString() {
		return new ToStringBuilder(this).append(user)
		.append(learningContext).toString();
	}
}
