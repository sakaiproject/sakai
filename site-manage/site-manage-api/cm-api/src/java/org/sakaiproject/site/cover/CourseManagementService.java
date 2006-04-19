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

package org.sakaiproject.site.cover;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.Term;

/**
 * <p>
 * CourseManagementService is a static Cover for the {@link org.sakaiproject.site.api CourseManageService}; see that interface for usage details.
 * </p>
 */
public class CourseManagementService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.site.api.CourseManagementService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.site.api.CourseManagementService) ComponentManager
						.get(org.sakaiproject.site.api.CourseManagementService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.site.api.CourseManagementService) ComponentManager
					.get(org.sakaiproject.site.api.CourseManagementService.class);
		}
	}

	private static org.sakaiproject.site.api.CourseManagementService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.site.api.CourseManagementService.SERVICE_NAME;

	public static java.util.List getCourseIdRequiredFields()
	{
		org.sakaiproject.site.api.CourseManagementService service = getInstance();
		if (service == null) return null;

		return service.getCourseIdRequiredFields();
	}

	public static java.util.List getCourseIdRequiredFieldsSizes()
	{
		org.sakaiproject.site.api.CourseManagementService service = getInstance();
		if (service == null) return null;

		return service.getCourseIdRequiredFieldsSizes();
	}

	public static String getCourseId(Term term, List requiredFields)
	{
		org.sakaiproject.site.api.CourseManagementService service = getInstance();
		if (service == null) return null;

		return service.getCourseId(term, requiredFields);
	}

	public static org.sakaiproject.site.api.Course getCourse(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.site.api.CourseManagementService service = getInstance();
		if (service == null) return null;

		return service.getCourse(param0);
	}

	public static java.util.List getCourseMembers(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.site.api.CourseManagementService service = getInstance();
		if (service == null) return null;

		return service.getCourseMembers(param0);
	}

	public static java.lang.String getCourseName(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.site.api.CourseManagementService service = getInstance();
		if (service == null) return null;

		return service.getCourseName(param0);
	}

	public static java.util.List getInstructorCourses(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.site.api.CourseManagementService service = getInstance();
		if (service == null) return null;

		return service.getInstructorCourses(param0, param1, param2);
	}

	public static java.util.List getTerms()
	{
		org.sakaiproject.site.api.CourseManagementService service = getInstance();
		if (service == null) return null;

		return service.getTerms();
	}

	public static org.sakaiproject.site.api.Term getTerm(java.lang.String param0)
	{
		org.sakaiproject.site.api.CourseManagementService service = getInstance();
		if (service == null) return null;

		return service.getTerm(param0);
	}

}
