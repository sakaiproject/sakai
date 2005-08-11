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

package org.sakaiproject.tool.gradebook.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.facades.Enrollment;
import org.sakaiproject.tool.gradebook.facades.User;
import org.sakaiproject.tool.gradebook.facades.standalone.dataload.UserLoader;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class TestStandaloneCourseManagement extends GradebookDbTestBase {
	private static final Log log = LogFactory.getLog(TestStandaloneCourseManagement.class);

    protected String gradebookUid = "QA_8";

    public void testFindEnrollment() throws Exception {
        Set enrollments = courseManagement.findEnrollmentsByStudentNameOrDisplayUid(gradebookUid, "First Middle LastName211");
        Assert.assertTrue(enrollments.size() == 1);

        enrollments = courseManagement.findEnrollmentsByStudentNameOrDisplayUid(gradebookUid, "First Middle");
        Assert.assertTrue(enrollments.size() > 0);

        enrollments = courseManagement.findEnrollmentsByStudentNameOrDisplayUid(gradebookUid, "LastName, First");
        Assert.assertTrue(enrollments.size() == 0);

        enrollments = courseManagement.findEnrollmentsByStudentNameOrDisplayUid(gradebookUid, "LastName21, Firs");
        Assert.assertTrue(enrollments.size() == 1);

        enrollments = courseManagement.findEnrollmentsByStudentNameOrDisplayUid(gradebookUid, "uid_211");
        Assert.assertTrue(enrollments.size() == 1);
    }

	public void testGetEnrollmentsSize() throws Exception {
		Set enrollments = courseManagement.getEnrollments(gradebookUid);
		int enrollmentSize = courseManagement.getEnrollmentsSize(gradebookUid);
		Assert.assertTrue(enrollments.size() == enrollmentSize);
	}

	public void testFindEnrollmentsByUserUid() throws Exception {
		String[] userUids = {"stu_1", "stu_2"};
		Set found = courseManagement.findEnrollmentsByUserUids(gradebookUid, Arrays.asList(userUids));
		Map enrMap = new HashMap();
		for (Iterator iter = found.iterator(); iter.hasNext(); ) {
			Enrollment enr = (Enrollment)iter.next();
			enrMap.put(enr.getUser().getUserUid(), enr);
		}
		Assert.assertTrue(enrMap.size() == 2);
		for (int i = 0; i < userUids.length; i++) {
			Assert.assertTrue(enrMap.get(userUids[i]) != null);
		}
	}

	public void testSortNamePagedEnrollments() throws Exception {
		List enrollments = new ArrayList(courseManagement.getEnrollments(gradebookUid));
		Collections.sort(enrollments, FacadeUtils.ENROLLMENT_NAME_COMPARATOR);
		int enrollmentSize = enrollments.size();
		int pageSize = 10;

		// Test ascending.
		List pageList = courseManagement.findEnrollmentsPagedBySortName(gradebookUid, 0, pageSize, true);
		for (int i = 0; i < pageSize; i++) {
			Enrollment enrA = (Enrollment)enrollments.get(i);
			Enrollment enrB = (Enrollment)pageList.get(i);
			// Duplicate names are allowed.
			Assert.assertTrue(enrA.getUser().getUserUid().equals(enrB.getUser().getUserUid()) ||
				enrA.getUser().getSortName().equals(enrB.getUser().getSortName()));
		}

		// Test end of paging.
		int startRow = enrollmentSize - 3;
		pageList = courseManagement.findEnrollmentsPagedBySortName(gradebookUid, startRow, pageSize, true);
		Assert.assertTrue(pageList.size() == 3);
		for (int i = 0; i < pageList.size(); i++) {
			Enrollment enrA = (Enrollment)enrollments.get(startRow + i);
			Enrollment enrB = (Enrollment)pageList.get(i);
			Assert.assertTrue(enrA.getUser().getUserUid().equals(enrB.getUser().getUserUid()) ||
				enrA.getUser().getSortName().equals(enrB.getUser().getSortName()));
		}

		// Test descending.
		Collections.reverse(enrollments);
		pageList = courseManagement.findEnrollmentsPagedBySortName(gradebookUid, 0, pageSize, false);
		for (int i = 0; i < pageSize; i++) {
			Enrollment enrA = (Enrollment)enrollments.get(i);
			Enrollment enrB = (Enrollment)pageList.get(i);
			Assert.assertTrue(enrA.getUser().getUserUid().equals(enrB.getUser().getUserUid()) ||
				enrA.getUser().getSortName().equals(enrB.getUser().getSortName()));
		}
	}

	public void testDisplayUidPagedEnrollments() throws Exception {
		List enrollments = new ArrayList(courseManagement.getEnrollments(gradebookUid));
		Collections.sort(enrollments, FacadeUtils.ENROLLMENT_DISPLAY_UID_COMPARATOR);
		int enrollmentSize = enrollments.size();
		int pageSize = 10;

		// Test ascending.
		List pageList = courseManagement.findEnrollmentsPagedByDisplayUid(gradebookUid, 0, pageSize, true);
		for (int i = 0; i < pageSize; i++) {
			Enrollment enrA = (Enrollment)enrollments.get(i);
			Enrollment enrB = (Enrollment)pageList.get(i);
			Assert.assertTrue(enrA.getUser().getUserUid().equals(enrB.getUser().getUserUid()));
		}

		// Test end of paging.
		int startRow = enrollmentSize - 3;
		pageList = courseManagement.findEnrollmentsPagedByDisplayUid(gradebookUid, startRow, pageSize, true);
		Assert.assertTrue(pageList.size() == 3);
		for (int i = 0; i < pageList.size(); i++) {
			Enrollment enrA = (Enrollment)enrollments.get(startRow + i);
			Enrollment enrB = (Enrollment)pageList.get(i);
			Assert.assertTrue(enrA.getUser().getUserUid().equals(enrB.getUser().getUserUid()));
		}

		// Test descending.
		Collections.reverse(enrollments);
		pageList = courseManagement.findEnrollmentsPagedByDisplayUid(gradebookUid, 0, pageSize, false);
		for (int i = 0; i < pageSize; i++) {
			Enrollment enrA = (Enrollment)enrollments.get(i);
			Enrollment enrB = (Enrollment)pageList.get(i);
			Assert.assertTrue(enrA.getUser().getUserUid().equals(enrB.getUser().getUserUid()));
		}
	}

    public void testFindUser() throws Exception {
        // This throws an exception if this fails, so just make the call
        User user = courseManagement.getUser(UserLoader.AUTHID_TEACHER_ALL);
    }
}


