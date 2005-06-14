/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/component/src/java/org/sakaiproject/tool/gradebook/business/FacadeUtils.java,v 1.3 2005/05/26 18:04:56 josh.media.berkeley.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.gradebook.business;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Enrollment;

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
	 * Get the UID identifying the current user.
	 */
	public static String getUserUid(Authn authnFacade) {
		return authnFacade.getUserUid(null);
	}

    /**
     * A comparator that sorts enrollments by student sortName
     */
    public static final Comparator ENROLLMENT_NAME_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
            return ((Enrollment)o1).getUser().getSortName().compareToIgnoreCase(((Enrollment)o2).getUser().getSortName());
		}
	};

    /**
     * A comparator that sorts enrollments by student display UID (for installations
     * where a student UID is not a number)
     */
    public static final Comparator ENROLLMENT_DISPLAY_UID_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            return ((Enrollment)o1).getUser().getDisplayUid().compareToIgnoreCase(((Enrollment)o2).getUser().getDisplayUid());
        }
    };

    /**
     * A comparator that sorts enrollments by student display UID (for installations
     * where a student UID is a number)
     */
    public static final Comparator ENROLLMENT_DISPLAY_UID_NUMERIC_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            long user1DisplayUid = Long.parseLong(((Enrollment)o1).getUser().getDisplayUid());
            long user2DisplayUid = Long.parseLong(((Enrollment)o2).getUser().getDisplayUid());
            return (int)(user1DisplayUid - user2DisplayUid);
        }
    };

    /**
     * Temporary relocation for paging/sorting refactoring.
     */
    public static Collection getStudentUids(Collection enrollments) {
		Collection studentUids = new HashSet();
		for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
			Enrollment enr = (Enrollment)iter.next();
			studentUids.add(enr.getUser().getUserUid());
		}
		return studentUids;
	}

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/component/src/java/org/sakaiproject/tool/gradebook/business/FacadeUtils.java,v 1.3 2005/05/26 18:04:56 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
