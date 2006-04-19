/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.site.api;

import java.util.List;

import org.sakaiproject.exception.IdUnusedException;

/**
 * <p>
 * CourseManagementProvider is the Interface for course management information providers. These are used by a course management service to access external course information.
 * </p>
 */
public interface CourseManagementProvider
{
	/**
	 * Return a list of field (labels) required for constructing course id
	 */
	public List getCourseIdRequiredFields();

	/**
	 * Return a list of maximum field size for course id required fields
	 */
	public List getCourseIdRequiredFieldsSizes();

	/**
	 * Construct course id based on provided information
	 */
	public String getCourseId(Term term, List requiredFields);

	/**
	 * Access a course object. Update the object with the information found.
	 * 
	 * @param String
	 *        the course id
	 * @return The course object found
	 */
	Course getCourse(String courseId) throws IdUnusedException;

	/**
	 * Access the course members.
	 * 
	 * @param String
	 *        the course id
	 * @return The list of CourseMember objects
	 */
	List getCourseMembers(String courseId) throws IdUnusedException;

	/**
	 * Get the course name by id
	 * 
	 * @param courseId
	 *        The course Id
	 * @return The course name
	 */
	String getCourseName(String courseId) throws IdUnusedException;

	/**
	 * Get all the course objects in specific term and with the user as the instructor
	 * 
	 * @param instructorId
	 *        The id for the instructor
	 * @param termYear
	 *        The term year
	 * @param termTerm
	 *        The term term
	 * @return The list of courses
	 */
	public List getInstructorCourses(String instructorId, String termYear, String termTerm);
}
