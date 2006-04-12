/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California and The Regents of the University of Michigan
*
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

package org.sakaiproject.component.section.sakai21;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.SectionEnrollments;

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
public class SectionEnrollmentsImpl implements SectionEnrollments, Serializable {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(SectionEnrollmentsImpl.class);
	
	protected Map studentToMap;

	public SectionEnrollmentsImpl(List enrollmentRecords) {
		studentToMap = new HashMap();
		for(Iterator iter = enrollmentRecords.iterator(); iter.hasNext();) {
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
