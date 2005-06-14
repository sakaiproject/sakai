/**********************************************************************************
 *
 * $Header: /cvs/sakai2/providers/sample/src/java/org/sakaiproject/component/legacy/coursemanagement/SampleCourseManagementProvider.java,v 1.3 2005/05/24 00:43:01 ggolden.umich.edu Exp $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

// package
package org.sakaiproject.component.legacy.coursemanagement;

// imports
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.legacy.coursemanagement.Course;
import org.sakaiproject.service.legacy.coursemanagement.CourseManagementProvider;

/**
 * <p>
 * Sample of course management provider.
 * </p>
 * <p>
 * Todo: %%% to be implemented; read course info from some config file.
 * </p>
 * 
 * @author University of Michigan, CHEF Software Development Team
 * @version $Revision: 1.3 $
 */
public class SampleCourseManagementProvider implements CourseManagementProvider
{
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
		c.setId("2005,FALL,,SMPL,001,001");
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
	public String getCourseName(String courseId)
	{
		for (int i = 0; i < m_courses.length; i++)
		{
			if (m_courses[i].getId().equals(courseId))
			{
				return m_courses[i].getTitle();
			}
		}
		return null;
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

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/providers/sample/src/java/org/sakaiproject/component/legacy/coursemanagement/SampleCourseManagementProvider.java,v 1.3 2005/05/24 00:43:01 ggolden.umich.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
