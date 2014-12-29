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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseSet;

@SuppressWarnings("unchecked")
public class CanonicalCourseCmImpl extends CrossListableCmImpl
	implements CanonicalCourse, Serializable {
	
	private static final long serialVersionUID = 1L;

	private CrossListingCmImpl crossListingCmImpl;
    private Set courseSets;
	
	/** A cache of courseSetEids */
	private Set courseSetEids;

	public CanonicalCourseCmImpl() {}
	public CanonicalCourseCmImpl(String eid, String title, String description) {
		this.eid = eid;
		this.title = title;
		this.description = description;
	}
	
	public Set getCourseSets() {
		return courseSets;
	}
	public void setCourseSets(Set courseSets) {
		this.courseSets = courseSets;
		
		// Update our cache of courseSetEids
		if(courseSets == null) {
			courseSetEids = null;
			return;
		}
		courseSetEids = new HashSet(courseSets.size());
		for(Iterator iter = courseSets.iterator(); iter.hasNext();) {
			CourseSet courseSet = (CourseSet)iter.next();
			courseSetEids.add(courseSet.getEid());
		}
	}
	
	public CrossListingCmImpl getCrossListing() {
		return crossListingCmImpl;
	}
	public void setCrossListing(CrossListingCmImpl crossListingCmImpl) {
		this.crossListingCmImpl = crossListingCmImpl;
	}
	
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((eid == null) ? 0 : eid.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CanonicalCourseCmImpl other = (CanonicalCourseCmImpl) obj;
        if (eid == null) {
            if (other.eid != null)
                return false;
        } else if (!eid.equals(other.eid))
            return false;
        return true;
	}

	// Replaced the use of builders with eclipse generated equals and hashcode -AZ
	//	public boolean equals(Object o) {
	//		CanonicalCourse other = (CanonicalCourse)o;
	//		return new EqualsBuilder().append(this.eid, other.getEid()).isEquals();
	//	}
	//	
	//	public int hashCode() {
	//		return new HashCodeBuilder().append(eid).toHashCode();
	//	}

	public Set getCourseSetEids() {
		return courseSetEids;
	}
}
