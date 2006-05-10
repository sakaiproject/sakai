/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section.sakai21;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.CourseManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

/**
 * Sakai 2.1 implementation of CourseManager.  Uses Sakai Sites API to store
 * all course metadata needed by the Section Info tool.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseManagerImpl implements CourseManager {

	private static final Log log = LogFactory.getLog(CourseManagerImpl.class);

	/**
	 * @inheritDoc
	 */
	public Course createCourse(final String siteContext, final String title,
			final boolean selfRegAllowed, final boolean selfSwitchingAllowed,
			final boolean externallyManaged) {

		log.warn("There should be no need to call " +
				"org.sakaiproject.api.section.CourseManager.createCourse() in " +
				"sakai 2.1.  This should only be called by a customized section " +
				"data loader.");
		
		Site site;
		try {
			site = SiteService.getSite(siteContext);
		} catch (IdUnusedException e) {
			log.error("Not site with id = " + siteContext);
			return null;
		}
		CourseImpl course = new CourseImpl(site);
		
		// Update the course with the new booleans passed to this method
		course.setSelfRegistrationAllowed(selfRegAllowed);
		course.setSelfSwitchingAllowed(selfSwitchingAllowed);
		course.setExternallyManaged(externallyManaged);
		course.decorateSite(site);
		
		// Save the modified site
		try {
			SiteService.save(site);
		} catch (IdUnusedException e) {
			log.error("Not site with id = " + siteContext);
			return null;
		} catch (PermissionException e) {
			log.error("Not allowed to save site with id = " + siteContext);
			return null;
		}
		return course;
	}
	
	/**
	 * The Sakai 2.1 implementation uses the SiteService API, so if the site exists
	 * then the "Course" exists.
	 */
	public boolean courseExists(final String siteContext) {
		try {
			SiteService.getSite(siteContext);
			return true;
		} catch (IdUnusedException e) {
			return false;
		}
	}
	
	
	/**
	 * This is handled by the Site and AuthzGroup Services in Sakai 2.1.  No need to
	 * do anything here.
	 */
	public void removeOrphans(final String siteContext) {
	}

	/**
	 * Not supported in sakai 2.1
	 */
	public ParticipationRecord addInstructor(final User user, final Course course) {
		throw new RuntimeException("Operation not supported in sakai 2.1");
	}

	/**
	 * Not supported in sakai 2.1
	 */
	public ParticipationRecord addEnrollment(final User user, final Course course) {
		throw new RuntimeException("Operation not supported in sakai 2.1");
	}

	/**
	 * Not supported in sakai 2.1
	 */
	public ParticipationRecord addTA(final User user, final Course course) {
		throw new RuntimeException("Operation not supported in sakai 2.1");
	}

	/**
	 * Not supported in sakai 2.1
	 */
	public void removeCourseMembership(String userUid, Course course) {
		throw new RuntimeException("Operation not supported in sakai 2.1");
	}

}

