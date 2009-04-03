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
import org.sakaiproject.section.api.coursemanagement.User;

/**
 * A detachable User for persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class UserImpl extends AbstractPersistentObject implements User, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String userUid;
	protected String sortName;
	protected String displayId;
	protected String displayName;

    /**
	 * No-arg constructor needed for hibernate
	 */
	public UserImpl() {		
	}
	
	public UserImpl(String displayName, String displayId, String sortName, String userUid) {
		this.displayName = displayName;
		this.displayId = displayId;
		this.sortName = sortName;
		this.userUid = userUid;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	public String getDisplayId() {
		return displayId;
	}
	public String getSortName() {
		return sortName;
	}
	public String getUserUid() {
		return userUid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof UserImpl) {
			UserImpl other = (UserImpl)o;
			return new EqualsBuilder()
				.append(userUid, other.userUid)
				.isEquals();
		}
		return false;
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(userUid)
			.toHashCode();
	}
	
	public String toString() {
		return new ToStringBuilder(this).append(displayName)
		.append(userUid).append(id).toString();
	}
}
