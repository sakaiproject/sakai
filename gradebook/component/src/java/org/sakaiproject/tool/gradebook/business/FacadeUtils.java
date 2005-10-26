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

package org.sakaiproject.tool.gradebook.business;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;

/**
 * The facades to external services are defined as interfaces which could be
 * implemented outside the gradebook development team.
 * This noninstantiable utility class provides convenience methods to gradebook-specific logic.
 */
public class FacadeUtils {
	private static final Log log = LogFactory.getLog(FacadeUtils.class);

	// Enforce noninstantiability.
	private FacadeUtils() {
	}

    /**
     * A comparator that sorts enrollments by student sortName
     */
    public static final Comparator ENROLLMENT_NAME_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
            return ((EnrollmentRecord)o1).getUser().getSortName().compareToIgnoreCase(((EnrollmentRecord)o2).getUser().getSortName());
		}
	};

    /**
     * A comparator that sorts enrollments by student display UID (for installations
     * where a student UID is not a number)
     */
    public static final Comparator ENROLLMENT_DISPLAY_UID_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            return ((EnrollmentRecord)o1).getUser().getDisplayId().compareToIgnoreCase(((EnrollmentRecord)o2).getUser().getDisplayId());
        }
    };

    /**
     * A comparator that sorts enrollments by student display UID (for installations
     * where a student UID is a number)
     */
    public static final Comparator ENROLLMENT_DISPLAY_UID_NUMERIC_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            long user1DisplayId = Long.parseLong(((EnrollmentRecord)o1).getUser().getDisplayId());
            long user2DisplayId = Long.parseLong(((EnrollmentRecord)o2).getUser().getDisplayId());
            return (int)(user1DisplayId - user2DisplayId);
        }
    };

    /**
     * A convenience method for UID-based filtering.
     */
    public static Set getStudentUids(Collection enrollments) {
		Set studentUids = new HashSet();
		for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			studentUids.add(enr.getUser().getUserUid());
		}
		return studentUids;
	}

}



