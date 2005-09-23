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

package org.sakaiproject.api.section;

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;

/**
 * Provides methods for adding a top-level course object, to which CourseSections
 * can be associated, and for associating users with the course.
 * 
 * The membership-related methods are intended for use in testing the standalone
 * application only.  In sakai, we will can use this as an external interface
 * that allows for Course creation, but does not alter membership in the course.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface CourseManager {
	
	/**
	 * Creates a new Course object for this site.
	 * 
	 * @param siteContext The site context
	 * @param title The title of the course or site
	 * @param selfRegAllowed Whether to allow students to register for sections in this course.
	 * @param selfSwitchingAllowed Whether to allow students to switch sections in this course.
	 * @param externallyManaged Whether to flag this course as externally manager
	 * (read-only to the app).
	 * 
	 * @return The newly created Course object.
	 */
	public Course createCourse(String siteContext, String title,
			boolean selfRegAllowed, boolean selfSwitchingAllowed,
			boolean externallyManaged);
	
	/**
	 * Checks to see whether a course exists in this site.
	 * 
	 * @param siteContext The site context
	 * @return
	 */
	public boolean courseExists(String siteContext);
	
	/**
	 * When a user is removed from a site, the course manager must be notified
	 * so it can remove the user from all section memberships in the site.
	 * 
	 * @param userUid
	 */
	public void removeUserFromAllSections(String userUid, String siteContext);
	
	/**
	 * Adds a student to a course.  Useful for dataloading in standalone mode.
	 * 
	 * @param user
	 * @param course
	 * @return
	 */
	public ParticipationRecord addEnrollment(User user, Course course);

	/**
	 * Adds a TA to a course.  Useful for dataloading in standalone mode.
	 * 
	 * @param user
	 * @param course
	 * @return
	 */
	public ParticipationRecord addTA(User user, Course course);

	/**
	 * Adds an instructor to a course.  Useful for dataloading in standalone mode.
	 * 
	 * @param user
	 * @param course
	 * @return
	 */
	public ParticipationRecord addInstructor(User user, Course course);
	
	
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
