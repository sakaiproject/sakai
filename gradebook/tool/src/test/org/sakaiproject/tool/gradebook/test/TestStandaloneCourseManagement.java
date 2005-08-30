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

    public void testFindUser() throws Exception {
        // This throws an exception if this fails, so just make the call
        User user = courseManagement.getUser(UserLoader.AUTHID_TEACHER_ALL);
    }
}


