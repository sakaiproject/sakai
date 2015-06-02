/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;

public class CourseOfferingCmImpl extends CrossListableCmImpl
	implements CourseOffering, Serializable {

	private static final long serialVersionUID = 1L;

	private String status;
	private CanonicalCourse canonicalCourse;
	private String canonicalCourseEid;
	private AcademicSession academicSession;
	private CrossListingCmImpl crossListingCmImpl;
	private Set courseSets;
	private Date startDate;
	private Date endDate;

	/** A cache of courseSetEids */
	private Set courseSetEids;

	public CourseOfferingCmImpl() {}
	
	public CourseOfferingCmImpl(String eid, String title, String description,String status, AcademicSession academicSession, CanonicalCourse canonicalCourse, Date startDate, Date endDate) {
		this.eid = eid;
		this.title = title;
		this.description = description;
		this.status = status;
		this.academicSession = academicSession;
		this.canonicalCourse = canonicalCourse;
		if(canonicalCourse == null) {
			this.canonicalCourseEid = null;
		} else {
			this.canonicalCourseEid = canonicalCourse.getEid();
		}
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public Set getCourseSets() {
		return courseSets;
	}
	public void setCourseSets(Set courseSets) {
		this.courseSets = courseSets;

		// Update our cache of courseSetEids
		if(courseSets == null) {
			courseSetEids = new HashSet();
		} else {
			courseSetEids = new HashSet(courseSets.size());
			for(Iterator iter = courseSets.iterator(); iter.hasNext();) {
				CourseSet courseSet = (CourseSet)iter.next();
				courseSetEids.add(courseSet.getEid());
			}
		}
	}

	public CrossListingCmImpl getCrossListing() {
		return crossListingCmImpl;
	}
	public void setCrossListing(CrossListingCmImpl crossListingCmImpl) {
		this.crossListingCmImpl = crossListingCmImpl;
	}

	public CanonicalCourse getCanonicalCourse() {
		return canonicalCourse;
	}
	public void setCanonicalCourse(CanonicalCourse canonicalCourse) {
		this.canonicalCourse = canonicalCourse;
		if(canonicalCourse == null) {
			this.canonicalCourseEid = null;
		} else {
			this.canonicalCourseEid = canonicalCourse.getEid();
		}
	}
	
	public AcademicSession getAcademicSession() {
		return academicSession;
	}
	public void setAcademicSession(AcademicSession academicSession) {
		this.academicSession = academicSession;
	}

	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getCanonicalCourseEid() {
		return canonicalCourseEid;
	}

	public Set getCourseSetEids() {
		return courseSetEids;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getTitle() {
		if (isTitleEmpty() && canonicalCourse != null) {
			return canonicalCourse.getTitle();
		}

		return title;
	}

	@Override
	public String getDescription() {
		if (isDescriptionEmpty() && canonicalCourse != null) {
			return canonicalCourse.getDescription();
		}

		return description;
	}
}
