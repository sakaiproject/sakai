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
 * The membership-related methods are intended for use in standalone mode only.
 * In sakai, we will need an external interface that allows for Course creation,
 * but does not alter membership in the course, since Course (or site) membership
 * will be controlled by other services.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface CourseManager {
	public Course createCourse(String siteContext, String title,
			boolean selfRegAllowed, boolean selfSwitchingAllowed,
			boolean externallyManaged);
	
	public ParticipationRecord addEnrollment(User user, Course course);
	public ParticipationRecord addTA(User user, Course course);
	public ParticipationRecord addInstructor(User user, Course course);
	
	
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
