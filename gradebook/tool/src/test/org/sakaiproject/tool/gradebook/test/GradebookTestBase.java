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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;

/**
 * Base class for gradebook test classes that provides the spring application
 * context.  The database used is an in-memory hsqldb by default, but this can
 * be overridden to test specific database configurations by setting the "mem"
 * system property to "false".  In the "mem=false" case, the database configuration
 * is set in the hibernate.properties file in the "hibernate.properties.dir" directory.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public abstract class GradebookTestBase extends AbstractTransactionalSpringContextTests {
	private static Log log = LogFactory.getLog(GradebookTestBase.class);

    protected Authz authz;
    protected Authn authn;
    protected GradebookManager gradebookManager;
    protected GradeManager gradeManager;
    protected GradebookService gradebookService;
    protected CourseManagement courseManagement;
    protected UserDirectoryService userDirectoryService;

    protected void onSetUpInTransaction() throws Exception {
        authn = (Authn)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_Authn");
        authz = (Authz)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_Authz");
        gradebookService = (GradebookService)applicationContext.getBean("org_sakaiproject_service_gradebook_GradebookService");
        gradebookManager = (GradebookManager)applicationContext.getBean("org_sakaiproject_tool_gradebook_business_GradebookManager");
        gradeManager = (GradeManager)applicationContext.getBean("org_sakaiproject_tool_gradebook_business_GradeManager");
        courseManagement = (CourseManagement)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_CourseManagement");
        userDirectoryService = (UserDirectoryService)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_UserDirectoryService");
    }

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        String[] configLocations = {"spring-db.xml", "spring-beans.xml", "spring-facades.xml", "spring-service.xml", "spring-hib.xml",
        	"spring-beans-test.xml",
        	"spring-hib-test.xml",
        	/* SectionAwareness integration support. */
        	"classpath*:org/sakaiproject/component/section/support/spring-integrationSupport.xml",
			/*
				We could just go with
					"classpath*:org/sakaiproject/component/section/spring-*.xml"
				except that for now we need to strip away a transactionManager defined
				in section/spring-hib.xml.
			*/
			"classpath*:org/sakaiproject/component/section/spring-beans.xml",
			"classpath*:org/sakaiproject/component/section/spring-sectionAwareness.xml",
			"classpath*:org/sakaiproject/component/section/spring-services.xml",
        };
        return configLocations;
    }

}
