/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sakaiproject.section.api.coursemanagement.CourseGroup;
import org.sakaiproject.site.api.Group;

/**
 * A detachable CourseSection for persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseGroupImpl implements CourseGroup, Comparable<CourseGroupImpl>, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String uuid;
	protected String title;
    protected String description;

    public CourseGroupImpl() {
    	// Default constructor needed by hibernate
    }

    public CourseGroupImpl(Group group) {
    	this.uuid = group.getReference();
    	this.title = group.getTitle();
    	this.description = group.getDescription();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int compareTo(CourseGroupImpl other) {
		return title.compareTo(other.getTitle());
	}

	public boolean equals(Object o) {
		if(o instanceof CourseGroup) {
			CourseGroup other = (CourseGroup)o;
			return new EqualsBuilder().append(this.uuid, other.getUuid()).isEquals();
		}
		return false;
	}
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(uuid).toHashCode();
	}
}
