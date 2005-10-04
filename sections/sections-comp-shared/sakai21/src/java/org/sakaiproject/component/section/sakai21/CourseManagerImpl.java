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

package org.sakaiproject.component.section.sakai21;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.CourseManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;

/**
 * Sakai 2.1 implementation of CourseManager.  Uses Sakai Sites API to store
 * all course metadata needed by the Section Info tool.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseManagerImpl implements CourseManager {

	private static final Log log = LogFactory.getLog(CourseManagerImpl.class);

	public Course createCourse(final String siteContext, final String title,
			final boolean selfRegAllowed, final boolean selfSwitchingAllowed,
			final boolean externallyManaged) {

		Site site;
		try {
			site = SiteService.getSite(siteContext);
		} catch (IdUnusedException e) {
			log.error("Not site with id = " + siteContext);
			return null;
		}
		
		// TODO Set the site properties
		
		return new CourseImpl(site);
	}
	
	/**
	 * The Sakai 2.1 implementation uses the SiteService API, so if the site exists
	 * then the "Course" exists.
	 */
	public boolean courseExists(final String siteContext) {
		try {
			SiteService.getSite(siteContext);
		} catch (IdUnusedException e) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * This is handled by the Site and Realm Services in Sakai 2.1.  No need to
	 * do anything here.
	 */
	public void removeOrphans(final String siteContext) {
	}

	/**
	 * Not supported in sakai 2.1
	 */
	public ParticipationRecord addInstructor(final User user, final Course course) {
//		String roleString = getRoleString(INSTRUCTOR);
//		RealmEdit realm;
//		String realmId = course.getUuid();
//		try {
//			realm = RealmService.editRealm(realmId);
//		} catch (Exception e) {
//			log.error("Unable to find or open realm " + realmId);
//			return null;
//		}
//		realm.addUserRole(user.getUserUid(), roleString, true, false);
//		RealmService.commitEdit(realm);
//		return new InstructorRecordImpl(course, user);
		throw new RuntimeException("Operation not supported in sakai 2.1");
	}

	/**
	 * Not supported in sakai 2.1
	 */
	public ParticipationRecord addEnrollment(final User user, final Course course) {
//		String roleString = getRoleString(STUDENT);
//		RealmEdit realm;
//		String realmId = course.getUuid();
//		try {
//			realm = RealmService.editRealm(realmId);
//		} catch (Exception e) {
//			log.error("Unable to find or open realm " + realmId);
//			return null;
//		}
//		realm.addUserRole(user.getUserUid(), roleString, true, false);
//		RealmService.commitEdit(realm);
//		return new EnrollmentRecordImpl(course, null, user);
		throw new RuntimeException("Operation not supported in sakai 2.1");
	}

	/**
	 * Not supported in sakai 2.1
	 */
	public ParticipationRecord addTA(final User user, final Course course) {
//		String roleString = getRoleString(TA);
//		RealmEdit realm;
//		String realmId = course.getUuid();
//		try {
//			realm = RealmService.editRealm(realmId);
//		} catch (Exception e) {
//			log.error("Unable to find or open realm " + realmId);
//			return null;
//		}
//		realm.addUserRole(user.getUserUid(), roleString, true, false);
//		RealmService.commitEdit(realm);
//		return new TeachingAssistantRecordImpl(course, user);
		throw new RuntimeException("Operation not supported in sakai 2.1");
	}

	/**
	 * Should this be supported in the Sakai 2.1 impl?
	 */
	public void removeCourseMembership(String userUid, Course course) {
		log.error("FIXME!");
	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
