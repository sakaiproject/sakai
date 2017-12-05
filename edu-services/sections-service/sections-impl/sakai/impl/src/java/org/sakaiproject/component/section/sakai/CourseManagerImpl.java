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
package org.sakaiproject.component.section.sakai;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.section.api.CourseManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

/**
 * Sakai  implementation of CourseManager.  Uses Sakai Sites API to store
 * all course metadata needed by the Section Info tool.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class CourseManagerImpl implements CourseManager {

	/**
	 * @inheritDoc
	 */
	public Course createCourse(final String siteContext, final String title,
			final boolean selfRegAllowed, final boolean selfSwitchingAllowed,
			final boolean externallyManaged) {

		log.warn("There should be no need to call " +
				"org.sakaiproject.section.api.CourseManager.createCourse() in " +
				"sakai .  This should only be called by a customized section " +
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
	 * The Sakai implementation uses the SiteService API, so if the site exists
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
	 * This is handled by the Site and AuthzGroup Services in Sakai.  No need to
	 * do anything here.
	 */
	public void removeOrphans(final String siteContext) {
	}

	/**
	 * Not supported in sakai 
	 */
	public ParticipationRecord addInstructor(final User user, final Course course) {
		throw new RuntimeException("Operation not supported in sakai");
	}

	/**
	 * Not supported in sakai 
	 */
	public ParticipationRecord addEnrollment(final User user, final Course course) {
		throw new RuntimeException("Operation not supported in sakai");
	}

	/**
	 * Not supported in sakai 
	 */
	public ParticipationRecord addTA(final User user, final Course course) {
		throw new RuntimeException("Operation not supported in sakai");
	}

	/**
	 * Not supported in sakai 
	 */
	public void removeCourseMembership(String userUid, Course course) {
		throw new RuntimeException("Operation not supported in sakai");
	}

}

