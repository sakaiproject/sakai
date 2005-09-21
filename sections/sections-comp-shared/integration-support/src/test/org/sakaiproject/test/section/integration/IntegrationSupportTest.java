/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
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

package org.sakaiproject.test.section.integration;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.component.section.support.IntegrationSupport;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class IntegrationSupportTest extends AbstractTransactionalSpringContextTests {
	private static final Log log = LogFactory.getLog(IntegrationSupportTest.class);
	
    protected String[] getConfigLocations() {
        String[] configLocations = {
        							"org/sakaiproject/component/section/spring-beans.xml",
        							"org/sakaiproject/component/section/spring-db.xml",
        							"org/sakaiproject/component/section/spring-sectionAwareness.xml",
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
			"category.lecture", new Integer(100), "noplace special", null, null, false,
			false, false, false, false, false, false);
    	
    	log.info("Adding site membership");
    	ParticipationRecord siteMembership = integrationSupport.addSiteMembership(
    			user1.getUserUuid(), SITE_CONTEXT_1, Role.STUDENT);

    	log.info("Adding section membership");
    	ParticipationRecord sectionMembership = integrationSupport.addSectionMembership(
    			user1.getUserUuid(), section1.getUuid(), Role.STUDENT);
    	
    	// Ensure that we can find the set of sections for this user
    	Set foundSectionMemberhsips = integrationSupport.getAllSectionMemberships(user1.getUserUuid(), SITE_CONTEXT_1);
    	Assert.assertEquals(foundSectionMemberhsips.size(), 1);
    	Assert.assertTrue(foundSectionMemberhsips.contains(sectionMembership));
    	
    	// Remove the user from the section, and ensure that the query no longer returns the enrollment
    	log.info("Remove section membership");
    	integrationSupport.removeSectionMembership(user1.getUserUuid(), section1.getUuid());
    	foundSectionMemberhsips = integrationSupport.getAllSectionMemberships(user1.getUserUuid(), SITE_CONTEXT_1);
    	Assert.assertEquals(foundSectionMemberhsips.size(), 0);
    	
    	// Ensure that we know which site the user belongs to
    	List siteMemberships = integrationSupport.getAllSiteMemberships(user1.getUserUuid());
    	Assert.assertTrue(siteMemberships.contains(siteMembership));
    	
    }

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
