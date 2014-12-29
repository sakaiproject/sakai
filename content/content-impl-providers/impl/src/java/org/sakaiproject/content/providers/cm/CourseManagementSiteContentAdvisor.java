/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.content.providers.cm;

import java.util.List;

import org.sakaiproject.content.api.providers.SiteContentAdvisor;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;

/**
 * @author ieb
 */
public class CourseManagementSiteContentAdvisor implements SiteContentAdvisor
{
	private static final long ONE_WEEK = 1000L * 60L * 60L * 24L * 7L;

	private CourseManagementService courseManagementService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.api.SiteContentAdvisor#getDefaultRetractTime()
	 */
	public long getDefaultRetractTime()
	{

		long now = System.currentTimeMillis();
		long defaultRetractTime = now + ONE_WEEK;
		List<AcademicSession> terms = courseManagementService.getAcademicSessions();
		boolean found = false;
		long guess = -1;
		for (AcademicSession term : terms)
		{
			if (term.getEndDate() != null && term.getEndDate().getTime() > now)
			{
				if (guess == -1)
				{
					guess = term.getEndDate().getTime();
				}
				else if (term.getEndDate().getTime() < guess)
				{
					guess = term.getEndDate().getTime();
				}
			}
		}
		if (guess != -1)
		{
			defaultRetractTime = guess;
		}
		return guess;
	}

	/**
	 * @return the courseManagementService
	 */
	public CourseManagementService getCourseManagementService()
	{
		return courseManagementService;
	}

	/**
	 * @param courseManagementService the courseManagementService to set
	 */
	public void setCourseManagementService(CourseManagementService courseManagementService)
	{
		this.courseManagementService = courseManagementService;
	}

}
