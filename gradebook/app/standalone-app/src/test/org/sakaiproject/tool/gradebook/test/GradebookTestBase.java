/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007 Sakai Foundation, the MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.tool.gradebook.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.component.section.support.IntegrationSupport;
import org.sakaiproject.component.section.support.UserManager;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;
import org.sakaiproject.tool.gradebook.facades.EventTrackingService;
import org.sakaiproject.tool.gradebook.facades.test.AuthnTestImpl;
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
	protected Authz authz;
    protected Authn authn;
    protected GradebookManager gradebookManager;
    protected GradebookService gradebookService;
    protected GradebookFrameworkService gradebookFrameworkService;
    protected GradebookExternalAssessmentService gradebookExternalAssessmentService;
	protected SectionAwareness sectionAwareness;
    protected UserDirectoryService userDirectoryService;
	protected IntegrationSupport integrationSupport;
	protected UserManager userManager;
    protected EventTrackingService eventTrackingService;
    protected GradebookPermissionService gradebookPermissionService;

    protected void onSetUpInTransaction() throws Exception {
        authn = (Authn)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_Authn");
        authz = (Authz)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_Authz");
        gradebookService = (GradebookService)applicationContext.getBean("org_sakaiproject_service_gradebook_GradebookService");
        gradebookFrameworkService = (GradebookFrameworkService)applicationContext.getBean("org_sakaiproject_service_gradebook_GradebookFrameworkService");
        gradebookExternalAssessmentService = (GradebookExternalAssessmentService)applicationContext.getBean("org_sakaiproject_service_gradebook_GradebookExternalAssessmentService");
        gradebookManager = (GradebookManager)applicationContext.getBean("org_sakaiproject_tool_gradebook_business_GradebookManager");
        sectionAwareness = (SectionAwareness)applicationContext.getBean("org.sakaiproject.section.api.SectionAwareness");
        userDirectoryService = (UserDirectoryService)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_UserDirectoryService");
        integrationSupport = (IntegrationSupport)applicationContext.getBean("org.sakaiproject.component.section.support.IntegrationSupport");
        userManager = (UserManager)applicationContext.getBean("org.sakaiproject.component.section.support.UserManager");
        eventTrackingService = (EventTrackingService) applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_EventTrackingService");
        gradebookPermissionService = (GradebookPermissionService) applicationContext.getBean("org_sakaiproject_service_gradebook_GradebookPermissionService");
    }

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        String[] configLocations = {"spring-db.xml", "spring-beans.xml", "spring-facades.xml",

			// To avoid warning messages every time the Gradebook code
			// finds out that the fresh DB doesn't have any grading scales
			// defined, use the configuration which explicitly defines
			// them.
			"spring-service-with-grade-mappings-test.xml",

			"spring-hib.xml",
        	"spring-beans-test.xml",
        	"spring-hib-test.xml",

        	// SectionAwareness integration support.
        	"classpath*:org/sakaiproject/component/section/support/spring-integrationSupport.xml",
			"classpath*:org/sakaiproject/component/section/spring-beans.xml",
			"classpath*:org/sakaiproject/component/section/spring-services.xml",
        };
        return configLocations;
    }

	protected List addUsersEnrollments(Gradebook gradebook, Collection studentUids) {
		List enrollments = new ArrayList();
		for (Iterator iter = studentUids.iterator(); iter.hasNext(); ) {
			String studentUid = (String)iter.next();
			
			// Some recently GradebookService methods assume unique user display IDs,
			// so make sure to set that as well.
			userManager.createUser(studentUid, null, null, studentUid);
			
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

	protected void setAuthnId(String newUserUid) {
		if (authn instanceof AuthnTestImpl) {
			((AuthnTestImpl)authn).setAuthnContext(newUserUid);
		} else {
			throw new UnsupportedOperationException();
		}
	}

    /**
     * Utility method for legacy tests after the "getCourseGradeWithStats" method stopped being used
     * by the application.
     */
	protected CourseGrade getCourseGradeWithStats(Long gradebookId) {
    	List gradableObjects = gradebookManager.getAssignmentsAndCourseGradeWithStats(gradebookId, Assignment.DEFAULT_SORT, true);
    	for (Iterator iter = gradableObjects.iterator(); iter.hasNext(); ) {
    		GradableObject gradableObject = (GradableObject)iter.next();
    		if (gradableObject instanceof CourseGrade) {
    			return (CourseGrade)gradableObject;
    		}
    	}
    	return null;
    }

		public GradebookPermissionService getGradebookPermissionService()
		{
			return gradebookPermissionService;
		}

		public void setGradebookPermissionService(
				GradebookPermissionService gradebookPermissionService)
		{
			this.gradebookPermissionService = gradebookPermissionService;
		}

}
