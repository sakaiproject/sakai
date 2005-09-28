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

package org.sakaiproject.tool.section.testservice;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.api.section.CourseManager;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.facade.Role;

/**
 * Tests whether the services exposed by the Section Info tool can be utilized
 * in another web application.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class TestServiceBean {
	private SectionAwareness sectionAwareness;
	private CourseManager courseManager;
	
	public boolean isCourseExists() {
		return courseManager.courseExists(ToolManager.getCurrentPlacement().getContext());
	}
	
	public boolean isCourseExistsFromCover() {
		String siteContext = ToolManager.getCurrentPlacement().getContext();;
		return org.sakaiproject.component.section.cover.CourseManager.courseExists(siteContext);
	}

	public List getSections() {
		return new ArrayList(sectionAwareness.getSections(ToolManager.getCurrentPlacement().getContext()));
	}
	
	public List getSectionsFromCover() {
		String siteContext = ToolManager.getCurrentPlacement().getContext();;
		return new ArrayList(org.sakaiproject.component.section.cover.SectionAwareness.getSections(siteContext));
	}
	
	public List getUnassignedStudents() {
		String siteContext = ToolManager.getCurrentPlacement().getContext();;
		return sectionAwareness.getUnassignedMembersInRole(siteContext, Role.STUDENT);
	}
	
	public List getUnassignedTas() {
		String siteContext = ToolManager.getCurrentPlacement().getContext();;
		return sectionAwareness.getUnassignedMembersInRole(siteContext, Role.TA);
	}

	// Dependency Injection
	
	public void setCourseManager(CourseManager courseManager) {
		this.courseManager = courseManager;
	}
	public void setSectionAwareness(SectionAwareness sectionAwareness) {
		this.sectionAwareness = sectionAwareness;
	}
}

/**********************************************************************************
 * $Id$
 *********************************************************************************/
