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

package org.sakaiproject.site.impl;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.api.Course;
import org.sakaiproject.site.api.CourseManagementProvider;
import org.sakaiproject.site.api.Term;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * Sample of course management provider.
 * </p>
 * <p>
 * Todo: %%% to be implemented; read course info from some config file.
 * </p>
 */
public class SampleCourseManagementProvider implements CourseManagementProvider
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("SampleCourseManagementProvider");

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SampleCourseManagementProvider.class);

	/** Sample coursed. */
	protected Course[] m_courses = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			M_log.info("init()");
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}

		// make sample courses
		Course c = new Course();
		c.setId("2005,FALL,SMPL,001,001");
		c.setTermId("FALL 2005");
		c.setTitle("Sample Course");
		m_courses = new Course[1];
		m_courses[0] = c;

	} // init

	/**
	 * Returns to uninitialized state. You can use this method to release resources thet your Service allocated when Turbine shuts down.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

	} // destroy

	/**********************************************************************************************************************************************************************************************************************************************************
	 * CourseManagementProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public List getCourseIdRequiredFields()
	{
		List rv = new Vector();
		rv.add(rb.getString("required_fields_subject"));
		rv.add(rb.getString("required_fields_course"));
		rv.add(rb.getString("required_fields_section"));
		return rv;

	} // getRequiredFieldsForCourseId

	/**
	 * Return a list of maximum field size for course id required fields
	 */
	public List getCourseIdRequiredFieldsSizes()
	{
		List rv = new Vector();
		rv.add(new Integer(8));
		rv.add(new Integer(3));
		rv.add(new Integer(3));
		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public String getCourseId(Term term, List requiredFields)
	{
		String rv = new String("");
		if (term != null)
		{
			rv = rv.concat(term.getYear() + "," + term.getTerm());
		}
		else
		{
			rv = rv.concat(",,");
		}
		for (int i = 0; i < requiredFields.size(); i++)
		{
			rv = rv.concat(",").concat((String) requiredFields.get(i));
		}
		return rv;

	} // getCourseId

	/**
	 * @inheritDoc
	 */
	public String getCourseName(String courseId)
	{
		StringBuffer tab = new StringBuffer();
		String[] fields;

		// A single course with a single section; tab is course and section
		fields = courseId.split(",");
		if (fields.length == 5)
		{
			tab.append(fields[2]);
			tab.append(" ");
			tab.append(fields[3]);
			tab.append(" ");
			tab.append(fields[4]);
		}

		return tab.toString();

	}

	/**
	 * @inheritDoc
	 */
	public Course getCourse(String courseId)
	{
		for (int i = 0; i < m_courses.length; i++)
		{
			if (m_courses[i].getId().equals(courseId))
			{
				return m_courses[i];
			}
		}
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public List getCourseMembers(String courseId)
	{
		for (int i = 0; i < m_courses.length; i++)
		{
			if (m_courses[i].getId().equals(courseId))
			{
				return m_courses[i].getMembers();
			}
		}
		return new Vector();
	}

	/**
	 * @inheritDoc
	 */
	public List getInstructorCourses(String instructorId, String termYear, String termTerm)
	{
		List rv = new Vector();
		for (int i = 0; i < m_courses.length; i++)
		{
			rv.add(m_courses[i]);
		}
		return rv;
	}
}
