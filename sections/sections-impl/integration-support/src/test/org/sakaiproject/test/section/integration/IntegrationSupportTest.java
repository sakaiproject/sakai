/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.test.section.integration;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.component.section.support.IntegrationSupport;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class IntegrationSupportTest extends AbstractTransactionalSpringContextTests {
	private static final Log log = LogFactory.getLog(IntegrationSupportTest.class);
	
    protected String[] getConfigLocations() {
        String[] configLocations = {
        							"org/sakaiproject/component/section/spring-beans.xml",
        							"org/sakaiproject/component/section/spring-db.xml",
        							"org/sakaiproject/component/section/support/spring-hib-test.xml",
        							"org/sakaiproject/component/section/support/spring-services-test.xml",
        							"org/sakaiproject/component/section/support/spring-integrationSupport.xml"
        							};
        return configLocations;
    }

    private IntegrationSupport integrationSupport;

    protected void onSetUpInTransaction() throws Exception {
        integrationSupport = (IntegrationSupport)applicationContext.getBean("org.sakaiproject.component.section.support.IntegrationSupport");
    }

    public void testUserIntegration() throws Exception {
    	User user1 = integrationSupport.createUser("integrationUserId1", "Integration 1 User",
    			"User, Integration 1", "intUser1");
    	Assert.assertTrue(user1 != null);
    	User foundUser = integrationSupport.findUser("integrationUserId1");
    	Assert.assertEquals(user1, foundUser);
    }

    public void testSiteMembership() throws Exception {
    	String SITE_CONTEXT_1 = "site_context_1";
    	String INSTRUCTOR_UUID_1 = "integration_instructor1";
    	
    	log.info("Creating course...");
    	integrationSupport.createCourse(SITE_CONTEXT_1, "Site 1",
    			false, false, false);

    	log.info("Creating user...");
    	integrationSupport.createUser(INSTRUCTOR_UUID_1, "Instructor One",
    			"One, Instructor", "inst1");

    	log.info("Creating instructor membership...");
    	ParticipationRecord instMembership1 = integrationSupport.addSiteMembership(
    			INSTRUCTOR_UUID_1, SITE_CONTEXT_1, Role.INSTRUCTOR);
    	
    	// Ensure that we can find the set of sites for this user
    	List siteMemberships = integrationSupport.getAllSiteMemberships(INSTRUCTOR_UUID_1);
    	
    	Assert.assertEquals(siteMemberships.size(), 1);
    	Assert.assertTrue(siteMemberships.contains(instMembership1));
    	
    	// Ensure that we can find the correct set of sites for this user after removing them from a site
    	log.info("Removing instructor membership...");
    	integrationSupport.removeSiteMembership(INSTRUCTOR_UUID_1, SITE_CONTEXT_1);
    	siteMemberships = integrationSupport.getAllSiteMemberships(INSTRUCTOR_UUID_1);
    	
    	Assert.assertEquals(siteMemberships.size(), 0);
    	
    }

    public void testSectionMembership() throws Exception {
    	String SITE_CONTEXT_1 = "site_context_1";
    	log.info("Creating course...");
    	Course course = integrationSupport.createCourse(SITE_CONTEXT_1, "Site 1",
    			false, false, false);

    	log.info("Creating user...");
    	User user1 = integrationSupport.createUser("integrationUserId1", "Integration 1 User",
    			"User, Integration 1", "intUser1");

    	log.info("Creating section...");

    	CourseSection section1 = integrationSupport.createSection(course.getUuid(), "Lecture 1",
			"category.lecture", Integer.valueOf(100), "noplace special", null, null, false,
			false, false, false, false, false, false);
    	
    	log.info("Adding site membership");
    	ParticipationRecord siteMembership = integrationSupport.addSiteMembership(
    			user1.getUserUid(), SITE_CONTEXT_1, Role.STUDENT);

    	log.info("Adding section membership");
    	ParticipationRecord sectionMembership = integrationSupport.addSectionMembership(
    			user1.getUserUid(), section1.getUuid(), Role.STUDENT);
    	
    	// Ensure that we can find the set of sections for this user
    	Set foundSectionMemberhsips = integrationSupport.getAllSectionMemberships(user1.getUserUid(), SITE_CONTEXT_1);
    	Assert.assertEquals(foundSectionMemberhsips.size(), 1);
    	Assert.assertTrue(foundSectionMemberhsips.contains(sectionMembership));
    	
    	// Remove the user from the section, and ensure that the query no longer returns the enrollment
    	log.info("Remove section membership");
    	integrationSupport.removeSectionMembership(user1.getUserUid(), section1.getUuid());
    	foundSectionMemberhsips = integrationSupport.getAllSectionMemberships(user1.getUserUid(), SITE_CONTEXT_1);
    	Assert.assertEquals(foundSectionMemberhsips.size(), 0);
    	
    	// Ensure that we know which site the user belongs to
    	List siteMemberships = integrationSupport.getAllSiteMemberships(user1.getUserUid());
    	Assert.assertTrue(siteMemberships.contains(siteMembership));
    }

}
