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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.SectionEnrollments;

/**
 * A data structure that keeps the UI layer from needing to know how the
 * SectionManager service packages up the section enrollment information for
 * a course.
 * 
 * This is an effort to aviod MOP (Map Oriented Programming).
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class SectionEnrollmentsImpl implements SectionEnrollments, Serializable {
	private static final long serialVersionUID = 1L;
	
	protected Map studentToMap;

	public SectionEnrollmentsImpl(List enrollmentRecords) {
		studentToMap = new HashMap<>();
		for(Iterator<EnrollmentRecord> iter = enrollmentRecords.iterator(); iter.hasNext();) {
			EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
			String userUid = enrollment.getUser().getUserUid();
			CourseSection section = (CourseSection)enrollment.getLearningContext();
			Map sectionMap;
			if(studentToMap.get(userUid) == null) {
				// Insert a new entry into the map
				sectionMap = new HashMap();
				studentToMap.put(userUid, sectionMap);
			} else {
				sectionMap = (Map)studentToMap.get(userUid);
			}
			sectionMap.put(section.getCategory(), section);
		}
	}
	
	public CourseSection getSection(String studentUid, String categoryId) {
		Map sectionMap = (Map)studentToMap.get(studentUid);
		if(sectionMap == null) {
			if(log.isDebugEnabled()) log.debug("Student " + studentUid + " is not represented in this SectionEnrollments data structure");
			return null;
		}
		CourseSection section = (CourseSection)sectionMap.get(categoryId);
		if(section == null) {
			if(log.isDebugEnabled()) log.debug("Student " + studentUid + " is not enrolled in a " + categoryId);
		}
		return section;
	}

	public Set getStudentUuids() {
		return studentToMap.keySet();
	}

	public String toString() {
		return new ToStringBuilder(this).append(studentToMap).toString();
	}
}
