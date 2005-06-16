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

package org.sakaiproject.tool.gradebook.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.standalone.dataload.EnrollmentLoader;
import org.sakaiproject.tool.gradebook.facades.standalone.dataload.TeachingAssignmentLoader;
import org.sakaiproject.tool.gradebook.facades.standalone.dataload.UserLoader;

/**
 * Loads Users and Enrollments into the database for standalone operation
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class TestUserEnrollmentTeacherLoader extends GradebookLoaderTestBase {
    Log log = LogFactory.getLog(TestUserEnrollmentTeacherLoader.class);

    UserLoader userLoader;
    EnrollmentLoader enrollmentLoader;
    TeachingAssignmentLoader teachingAssignmentLoader;
    
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
        userLoader = (UserLoader)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_standalone_dataload_UserLoader");
        enrollmentLoader = (EnrollmentLoader)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_standalone_dataload_EnrollmentLoader");
        teachingAssignmentLoader = (TeachingAssignmentLoader)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_standalone_dataload_TeachingAssignmentLoader");
    }

    public void testLoadUsers() throws Exception {
        log.info("Loading users");
        userLoader.loadUsers();
        setComplete();
    }

     public void testLoadEnrollments() throws Exception {
        log.info("Loading enrollments");
        enrollmentLoader.loadEnrollments();
        setComplete();
     }

     public void testLoadTeachingAssignments() throws Exception {
        log.info("Loading teaching assignments");
        teachingAssignmentLoader.loadTeachingAssignments();
        setComplete();
    }

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
