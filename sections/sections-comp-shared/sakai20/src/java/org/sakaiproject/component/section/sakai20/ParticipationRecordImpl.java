/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.section.sakai20;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.LearningContext;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;

/**
 * A base class of ParticipationRecords for detachable persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public abstract class ParticipationRecordImpl extends AbstractPersistentObject
	implements ParticipationRecord, Serializable {

	protected User user;
	protected String userUuid;
	protected LearningContext learningContext;

	public User getUser() {
		if(user == null) {
			user = SakaiUtil.getUserFromSakai(userUuid);
		}
		return user;
	}
	
	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
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
				.append(userUuid, other.getUserUuid())
				.append(learningContext, other.getLearningContext())
				.isEquals();
		}
		return false;
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(userUuid)
			.append(learningContext)
			.toHashCode();
	}
	
	public String toString() {
		return new ToStringBuilder(this).append(userUuid)
		.append(learningContext).toString();
	}
}

/**********************************************************************************
 * $Id$
 *********************************************************************************/
