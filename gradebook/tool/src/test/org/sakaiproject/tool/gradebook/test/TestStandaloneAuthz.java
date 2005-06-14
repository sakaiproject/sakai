/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestStandaloneAuthz.java,v 1.3 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
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

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.Role;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class TestStandaloneAuthz extends SpringEnabledTestCase {
	private static final Log log = LogFactory.getLog(TestStandaloneAuthz.class);

    static String AUTHZ_MANAGER = "org_sakaiproject_tool_gradebook_facades_Authz";
    static String AUTHN_MANAGER = "org_sakaiproject_tool_gradebook_facades_Authn";
    static String GB_MANAGER = "org_sakaiproject_tool_gradebook_business_GradebookManager";

    Authz authz;
    Authn authn;
    GradebookManager gradebookManager;
    String gradebookUid;

	protected void setUp() throws Exception {
		log.info("Attempting to obtain spring-managed services.");
		initialize("components.xml,components-test.xml");
        authn = (Authn)getBean(AUTHN_MANAGER);
        authz = (Authz)getBean(AUTHZ_MANAGER);
        gradebookManager = (GradebookManager)getBean(GB_MANAGER);
        Gradebook gradebook = gradebookManager.getGradebook(new Long(1));
        gradebookUid = gradebook.getUid();
	}

	public void testGradebookModifyAuthz() throws Exception {
        String userUid = authn.getUserUid(null);
        // TODO Run more rigorous authz tests.
        Assert.assertTrue(authz.getGradebookRole(gradebookUid, userUid) == Role.NONE);

	}

    public void testViewReleasedForSelfAuthorized() throws Exception {
        String userUid = authn.getUserUid(null);
        Assert.assertFalse(authz.getGradebookRole(gradebookUid, userUid) == Role.STUDENT);
    }
}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestStandaloneAuthz.java,v 1.3 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
