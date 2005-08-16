/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.section.decorator;

import java.io.Serializable;

import org.sakaiproject.api.section.coursemanagement.CourseSection;

public class StudentSectionDecorator extends CourseSectionDecorator
	implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;

	protected String instructorNames;
	protected int spotsAvailable;
	public StudentSectionDecorator(CourseSection courseSection, String categoryForDisplay, String instructorNames, int totalEnrollments) {
		super(courseSection, categoryForDisplay);
		this.instructorNames = instructorNames;
		int spots = courseSection.getMaxEnrollments() - totalEnrollments;
		this.spotsAvailable = spots > 0 ? spots : 0;
	}
	public String getInstructorNames() {
		return instructorNames;
	}
	public int getSpotsAvailable() {
		return spotsAvailable;
	}
	
	public int compareTo(Object o) {
		return this.getTitle().compareTo(((StudentSectionDecorator)o).getTitle());
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
