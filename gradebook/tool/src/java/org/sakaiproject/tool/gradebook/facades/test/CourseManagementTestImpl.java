/**********************************************************************************
*
* $Id$
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

package org.sakaiproject.tool.gradebook.facades.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.sakaiproject.tool.gradebook.facades.CourseManagement;
import org.sakaiproject.tool.gradebook.facades.standalone.EnrollmentStandalone;
import org.sakaiproject.tool.gradebook.facades.standalone.UserStandalone;

/**
 * An in-memory stub implementation of CourseManagement, used for testing.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public class CourseManagementTestImpl implements CourseManagement {

    /**
	 */
	public Set getEnrollments(String gradebookUid) {
        Set enr = new HashSet();
        for(int i=0; i<100; i++) {
        	String userUid = getRndStr(i);
            enr.add(new EnrollmentStandalone(new UserStandalone(userUid, userUid, userUid, userUid + "@campus.edu"), null));
        }
        return enr;
	}

    private String getRndStr(int seed) {
        StringBuffer randomString = new StringBuffer();
        Random rnd = new Random(seed);
        for (int i = 0; i < 12; i++) {
            int digit = rnd.nextInt(62);
            char alphaNum;
            if (digit < 26) {
                alphaNum = (char) (digit + 'A');
            } else if (digit < 36) {
                alphaNum = (char) (digit - 26 + '0');
            } else {
                alphaNum = (char) (digit - 36 + 'a');
            }

            randomString.append(alphaNum);
        }
        return randomString.toString();
    }

	public Set findEnrollmentsByUserUids(String gradebookUid, Collection userUids) {
		Set users = new HashSet();
		for (Iterator iter = userUids.iterator(); iter.hasNext(); ) {
			String userUid = (String)iter.next();
			users.add(new UserStandalone(userUid, userUid, userUid, userUid + "@campus.edu"));
		}
		return users;
	}

    public int getEnrollmentsSize(String gradebookUid) {
    	return getEnrollments(gradebookUid).size();
    }

	/**
	 */
	public Set findEnrollmentsByStudentNameOrDisplayUid(String gradebookUid, String studentNameQuery) {
        // TODO Auto-generated method stub
		return new HashSet();
	}
	/**
	 */
	public Set findEnrollmentsByStudentDisplayUid(String gradebookUid,
			String studentDisplayUid) {
		// TODO Auto-generated method stub
		return new HashSet();
	}

	public List findEnrollmentsPagedBySortName(String gradebookUid, int startRange, int rangeMaximum, boolean isAscending) {
		// TODO Implement this.
		return new ArrayList();
	}

	public List findEnrollmentsPagedByDisplayUid(String gradebookUid, int startRange, int rangeMaximum, boolean isAscending) {
		// TODO Implement this.
		return new ArrayList();
	}
}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
