/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestStandaloneFramework.java,v 1.3 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
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

import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.standalone.dataload.UserLoader;
import org.sakaiproject.tool.gradebook.standalone.FrameworkManager;

public class TestStandaloneFramework extends SpringEnabledTestCase {
	private static final Log log = LogFactory.getLog(TestStandaloneFramework.class);
	FrameworkManager frameworkManager;

	protected void setUp() throws Exception {
		log.info("Attempting to obtain spring-managed services.");
		initialize("components.xml,components-test.xml");
        frameworkManager = (FrameworkManager)getBean("org_sakaiproject_tool_gradebook_standalone_FrameworkManager");
	}

	public void testGetAccessibleGradebooks() throws Exception {
		List gradebooks;

		gradebooks = frameworkManager.getAccessibleGradebooks(UserLoader.AUTHID_NO_GRADEBOOK);
		Assert.assertTrue(gradebooks.size() == 0);

		// Instructor in at least one gradebook.
		gradebooks = frameworkManager.getAccessibleGradebooks(UserLoader.AUTHID_TEACHER_ALL);
		Assert.assertTrue(gradebooks.size() >= 1);

		// Student in at least one gradebook.
		gradebooks = frameworkManager.getAccessibleGradebooks(UserLoader.AUTHID_STUDENT_ALL);
		Assert.assertTrue(gradebooks.size() >= 1);

		// Test a case where a user plays an instructor in one gradebook and
		// a student in another.
		gradebooks = frameworkManager.getAccessibleGradebooks(UserLoader.AUTHID_TEACHER_AND_STUDENT);
		Assert.assertTrue(gradebooks.size() == 2);

	}
}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestStandaloneFramework.java,v 1.3 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
