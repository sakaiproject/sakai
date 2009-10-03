/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.impl;

import java.util.HashSet;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Integration tests for the CM services.  The CmGroupProvider must be registered
 * with the component manager for these tests to pass.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CmIntegrationTest extends SakaiTestBase {
	private static final Log log = LogFactory.getLog(CmIntegrationTest.class);

	private AuthzGroupService authzGroupService;
	private SiteService siteService;
	private CourseManagementAdministration cmAdmin;
	private UserDirectoryService uds;
	private SessionManager sessionManager;

	private Site site;

	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(CmIntegrationTest.class)) {
			protected void setUp() throws Exception {
				log.debug("starting setup");
				oneTimeSetup();
				log.debug("finished setup");
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}

	public void setUp() throws Exception {
		log.debug("Setting up a CmIntegrationTest test");

		// Connect to the required services
		authzGroupService = (AuthzGroupService)getService(AuthzGroupService.class.getName());
		siteService = (SiteService)getService(SiteService.class.getName());
		cmAdmin = (CourseManagementAdministration)getService(CourseManagementAdministration.class.getName());
		uds = (UserDirectoryService)getService(UserDirectoryService.class.getName());

		// Log in
		sessionManager = (SessionManager)getService(SessionManager.class.getName());
		Session session = sessionManager.getCurrentSession();
		session.setUserId("admin");

		// Create some Sakai users (who will be added to the site by the CMGroupProvider)
		uds.addUser("cm-student1", "cm-student1", "cm-student", "CM-one", "cm-student1@foo.bar", "cm-student1", null, null);
		uds.addUser("cm-student2", "cm-student2", "cm-student", "CM-two", "cm-student2@foo.bar", "cm-student2", null, null);
		uds.addUser("cm-instructor1", "cm-instructor1", "cm-instructor", "CM-one", "cm-instructor1@foo.bar", "cm-instructor1", null, null);
		uds.addUser("cm-instructor2", "cm-instructor2", "cm-instructor", "CM-two", "cm-instructor2@foo.bar", "cm-instructor2", null, null);

		// Create CM objects
		cmAdmin.createAcademicSession("CM-AS1","CM-AS1", "CM-AS1", null, null);
		cmAdmin.createCanonicalCourse("CM-CC1", "CM-CC1", "CM-CC1");
		cmAdmin.createCourseOffering("CM-CO1", "CM-CO1", "CM-CO1", "open", "CM-AS1", "CM-CC1", null, null);
		Set instructors = new HashSet();
		instructors.add("cm-instructor1");
		cmAdmin.createEnrollmentSet("CM-ES1", "CM-ES1", "CM-ES1", "lecture", "3", "CM-CO1", instructors);
		cmAdmin.createSection("CM-SEC1", "CM-SEC1", "CM-SEC1", "lecture", null, "CM-CO1", "CM-ES1");

		// Create some CM memberships, etc
		cmAdmin.addOrUpdateCourseOfferingMembership("cm-instructor2", "Just helping out with guest lectures", "CM-CO1","active");
		cmAdmin.addOrUpdateEnrollment("cm-student1", "CM-ES1", "wait", "4", "letter grade");

		// Create a site
		site = siteService.addSite("CM1", "course");
		site.setTitle("CM Integration Test Site");
		site.setProviderGroupId("CM-SEC1");
		siteService.save(site);

		// This (or something similar) is needed to trigger the authz provider update, I think
		String siteRef = site.getReference();
		AuthzGroup azg = authzGroupService.getAuthzGroup(siteRef);
		// Ensure that the azg has the correct provider id
		Assert.assertEquals("CM-SEC1", azg.getProviderGroupId());
		azg.addMember("cm-student2", "Student", true, false);
		authzGroupService.save(azg);

		// Now we have all of our users as members of the site: student1 is enrolled,
		// student2 was manually added, instructor1 is an official instructor, and
		// instructor2 has a membership in the course offering.
	}

	public void tearDown() throws Exception {
		log.debug("Tearing down an AuthzIntegrationTest test");

		// Remove the objects created for testing
		uds.removeUser(uds.editUser("cm-student1"));
		uds.removeUser(uds.editUser("cm-student2"));
		uds.removeUser(uds.editUser("cm-instructor1"));
		uds.removeUser(uds.editUser("cm-instructor2"));

		cmAdmin.removeSection("CM-SEC1");
		cmAdmin.removeEnrollmentSet("CM-ES1");
		cmAdmin.removeCourseOffering("CM-CO1");
		cmAdmin.removeCanonicalCourse("CM-CC1");
		cmAdmin.removeAcademicSession("CM-AS1");

		siteService.removeSite(site);
	}

	public void testCmGroupProvider() throws Exception {
		// Our official instructors, enrollments and CM memberships should be expressed as sakai members
		Site site = siteService.getSite("CM1");

		// Of course, our manually added member should be here
		Member manualMember = site.getMember("cm-student2");
		Assert.assertNotNull(manualMember);
		Assert.assertEquals("Student", manualMember.getRole().getId());

		// Our CM-defined student should be here, too
		Member student = site.getMember("cm-student1");
		Assert.assertNotNull(student);
		Assert.assertEquals("Student", student.getRole().getId());
	}

	public void testCmAdminAuthz() throws Exception {
		sessionManager.getCurrentSession().invalidate();
		sessionManager.getCurrentSession().setUserId("cm-student1");
		
		try {
			cmAdmin.createCanonicalCourse("CM-CC2", "CM-CC2", "CM-CC2");
			fail();
		} catch (Exception e) {
			log.debug("Trying to create a course as a student threw exception ", e);
		}

		sessionManager.getCurrentSession().invalidate();
		sessionManager.getCurrentSession().setUserId("admin");
	}
}
