/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades.sectionsimpl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;

/**
 * A SectionAwareness implementation of CourseManagement.
 */
public class CourseManagementSectionsImpl extends AbstractSectionsImpl implements CourseManagement {
    private static final Log log = LogFactory.getLog(CourseManagementSectionsImpl.class);

    /**
     * Returns all enrollments in the specified gradebook
     */
	public Set getEnrollments(String gradebookUid) {
		List participationRecords = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);

		// TODO Use the SectionAwareness interfaces directly rather than deal with these expensive re-wrappings.
		return convertParticipationRecordListToEnrollmentSet(participationRecords);
	}

	private Set convertParticipationRecordListToEnrollmentSet(List participationRecords) {
		Set enrollments = new HashSet(participationRecords.size());
		for (Iterator iter = participationRecords.iterator(); iter.hasNext(); ) {
			enrollments.add(new EnrollmentSectionsImpl((EnrollmentRecord)iter.next()));
		}
		return enrollments;
	}

	public int getEnrollmentsSize(String gradebookUid) {
		List participationRecords = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
		return participationRecords.size();
	}

    public Set findEnrollmentsByStudentNameOrDisplayUid(String gradebookUid, String studentPattern) {
    	List participationRecords = getSectionAwareness().findSiteMembersInRole(gradebookUid, Role.STUDENT, studentPattern);
		return convertParticipationRecordListToEnrollmentSet(participationRecords);
	}
}
