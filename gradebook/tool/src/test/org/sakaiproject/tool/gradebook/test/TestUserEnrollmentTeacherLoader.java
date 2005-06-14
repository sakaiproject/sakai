/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestUserEnrollmentTeacherLoader.java,v 1.3 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
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

package org.sakaiproject.tool.gradebook.test;

import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.standalone.dataload.EnrollmentLoader;
import org.sakaiproject.tool.gradebook.facades.standalone.dataload.TeachingAssignmentLoader;
import org.sakaiproject.tool.gradebook.facades.standalone.dataload.UserLoader;

/**
 * Loads Users and Enrollments into the database for standalone operation
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class TestUserEnrollmentTeacherLoader extends SpringEnabledTestCase {

    UserLoader userLoader;
    EnrollmentLoader enrollmentLoader;
    TeachingAssignmentLoader teachingAssignmentLoader;
    GradebookManager gradebookManager;

    protected void setUp() throws Exception {
        log.info("Attempting to obtain spring-managed services.");
        initialize("components.xml,components-test.xml");
        userLoader = (UserLoader)getBean("org_sakaiproject_tool_gradebook_facades_standalone_dataload_UserLoader");
        enrollmentLoader = (EnrollmentLoader)getBean("org_sakaiproject_tool_gradebook_facades_standalone_dataload_EnrollmentLoader");
        teachingAssignmentLoader = (TeachingAssignmentLoader)getBean("org_sakaiproject_tool_gradebook_facades_standalone_dataload_TeachingAssignmentLoader");
        gradebookManager = (GradebookManager)getBean("org_sakaiproject_tool_gradebook_business_GradebookManager");
    }

    public void testLoadUsers() throws Exception {
        log.warn("Loading users");
        userLoader.loadUsers();
    }

     public void testLoadEnrollments() throws Exception {
        log.warn("Loading enrollments");
        enrollmentLoader.loadEnrollments();
     }

     public void testLoadTeachingAssignments() throws Exception {
        log.warn("Loading teaching assignments");
        teachingAssignmentLoader.loadTeachingAssignments();
    }

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestUserEnrollmentTeacherLoader.java,v 1.3 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
