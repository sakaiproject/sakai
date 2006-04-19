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
 * CourseManagemeentService provides ways to access and manupilate Term and Course objects.
 * </p>
 */
public interface CourseManagementService
{
	/** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = CourseManagementService.class.getName();

	/** Security lock / event for updating any course. */
	public static final String SECURE_UPDATE_COURSE_ANY = "revise.course.any";

	/** Security lock / event for adding any term. */
	public static final String SECURE_ADD_TERM = "add.term";

	/** Security lock / event for removing any term. */
	public static final String SECURE_REMOVE_TERM = "remove.term";

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
	 * Get the course object with the course provider id specified
	 * 
	 * @param the
	 *        course provider id, the meaning of which is defined in the implementation
	 * @return The Course object
	 */
	public Course getCourse(String courseId) throws IdUnusedException;

	/**
	 * Get the list of CourseMember objects with the course provider id specified
	 * 
	 * @param the
	 *        course provider id, the meaning of which is defined in the implementation
	 * @return a list of CourseMember objects
	 */
	public List getCourseMembers(String courseId) throws IdUnusedException;

	/**
	 * Get the course name by id
	 * 
	 * @param courseId
	 *        The course Id
	 * @return The course name
	 */
	public String getCourseName(String courseId) throws IdUnusedException;

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

	/**
	 * Get the list of all know terms
	 * 
	 * @return The List of Term objects
	 */
	public List getTerms();

	/**
	 * Get the term with the id
	 * 
	 * @return The Term object
	 */
	public Term getTerm(String termId);

} // CourseManagementService

