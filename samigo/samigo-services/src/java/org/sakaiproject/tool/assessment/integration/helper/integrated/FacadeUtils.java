/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/integration/helper/integrated/FacadeUtils.java $
 * $Id: FacadeUtils.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.integration.helper.integrated;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;

/**
 * borrowed gradebook's FacadeUtils class
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



