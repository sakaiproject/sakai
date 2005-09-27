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

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.component.section.support.IntegrationSupport;
import org.sakaiproject.component.section.support.UserManager;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;
import org.springframework.test.AbstractTransactionalSpringContextTests;

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
	protected SectionAwareness sectionAwareness;
    protected UserDirectoryService userDirectoryService;
	protected IntegrationSupport integrationSupport;
	protected UserManager userManager;

    protected void onSetUpInTransaction() throws Exception {
        authn = (Authn)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_Authn");
        authz = (Authz)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_Authz");
        gradebookService = (GradebookService)applicationContext.getBean("org_sakaiproject_service_gradebook_GradebookService");
        gradebookManager = (GradebookManager)applicationContext.getBean("org_sakaiproject_tool_gradebook_business_GradebookManager");
        gradeManager = (GradeManager)applicationContext.getBean("org_sakaiproject_tool_gradebook_business_GradeManager");
        sectionAwareness = (SectionAwareness)applicationContext.getBean("org.sakaiproject.api.section.SectionAwareness");
        userDirectoryService = (UserDirectoryService)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_UserDirectoryService");
        integrationSupport = (IntegrationSupport)applicationContext.getBean("org.sakaiproject.component.section.support.IntegrationSupport");
        userManager = (UserManager)applicationContext.getBean("org.sakaiproject.component.section.support.UserManager");
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

	protected List addUsersEnrollments(Gradebook gradebook, Collection studentUids) {
		List enrollments = new ArrayList();
		for (Iterator iter = studentUids.iterator(); iter.hasNext(); ) {
			String studentUid = (String)iter.next();
			userManager.createUser(studentUid, null, null, null);
			EnrollmentRecord sectionEnrollment = (EnrollmentRecord)integrationSupport.addSiteMembership(studentUid, gradebook.getUid(), Role.STUDENT);
			enrollments.add(sectionEnrollment);
		}
		return enrollments;
	}

	public IntegrationSupport getIntegrationSupport() {
		return integrationSupport;
	}
	public void setIntegrationSupport(IntegrationSupport integrationSupport) {
		this.integrationSupport = integrationSupport;
	}
	public UserManager getUserManager() {
		return userManager;
	}
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
}
