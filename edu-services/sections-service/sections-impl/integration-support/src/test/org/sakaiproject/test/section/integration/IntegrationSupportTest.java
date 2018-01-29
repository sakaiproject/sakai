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
 *       http://www.opensource.org/licenses/ECL-2.0
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

import org.junit.Assert;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import org.sakaiproject.component.section.support.IntegrationSupport;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;

@ContextConfiguration(locations = { 
		"classpath:org/sakaiproject/component/section/spring-beans.xml",
		"classpath:org/sakaiproject/component/section/spring-db.xml",
		"classpath:org/sakaiproject/component/section/support/spring-hib-test.xml",
		"classpath:org/sakaiproject/component/section/support/spring-services-test.xml",
		"classpath:org/sakaiproject/component/section/support/spring-integrationSupport.xml"
	})
public class IntegrationSupportTest extends AbstractTransactionalJUnit4SpringContextTests {
	private static String SITE_1 = "site_1";
	private static String USER_1 = "integration_user_1";
	private static String USER_2 = "integration_user_2";

	@Autowired
	private IntegrationSupport integrationSupport;
	
	@Test
	public void testIntegrationSupport() throws Exception {
	
		User user1 = integrationSupport.createUser(USER_1, "User One", "One, User", "user1");
		Assert.assertNotNull(user1);
		User user2 = integrationSupport.createUser(USER_2, "User Two", "Two, User", "user2");
		Assert.assertNotNull(user2);

		Course course1 = integrationSupport.createCourse(SITE_1, "Site 1", false, false, false);
		Assert.assertNotNull(course1);

		CourseSection section1 = integrationSupport.createSection(course1.getUuid(), "Lecture 1",
				"category.lecture", Integer.valueOf(100), "noplace special", null, null, false,
				false, false, false, false, false, false);
		
		ParticipationRecord membershipUser1 = integrationSupport.addSiteMembership(USER_1, SITE_1, Role.INSTRUCTOR);
		Assert.assertNotNull(membershipUser1);
		
		ParticipationRecord membershipUser2 = integrationSupport.addSiteMembership(USER_2, SITE_1, Role.STUDENT);
		Assert.assertNotNull(membershipUser2);

		ParticipationRecord sectionMembership1 = integrationSupport.addSectionMembership(USER_2, section1.getUuid(), Role.STUDENT);
		
		// user
		User foundUser1 = integrationSupport.findUser(USER_1);
		Assert.assertEquals(user1, foundUser1);

		User foundUser2 = integrationSupport.findUser(USER_2);
		Assert.assertEquals(user2, foundUser2);
    	
		// membership
		List siteMembershipUser1 = integrationSupport.getAllSiteMemberships(USER_1);
		Assert.assertTrue(siteMembershipUser1.contains(membershipUser1));
    	
		List siteMembershipUser2 = integrationSupport.getAllSiteMemberships(USER_2);
		Assert.assertTrue(siteMembershipUser2.contains(membershipUser2));
    	
		Set sectionMembershipUser2 = integrationSupport.getAllSectionMemberships(USER_2, SITE_1);
		Assert.assertTrue(sectionMembershipUser2.contains(sectionMembership1));
    	
		// Remove the user from the section, and ensure that the query no longer returns the enrollment
		integrationSupport.removeSectionMembership(USER_2, section1.getUuid());
		sectionMembershipUser2 = integrationSupport.getAllSectionMemberships(USER_2, SITE_1);
		Assert.assertEquals(sectionMembershipUser2.size(), 0);
    	
		integrationSupport.removeSiteMembership(USER_2, SITE_1);
		siteMembershipUser2 = integrationSupport.getAllSiteMemberships(USER_2);
		Assert.assertEquals(siteMembershipUser2.size(), 0);
    }
}
