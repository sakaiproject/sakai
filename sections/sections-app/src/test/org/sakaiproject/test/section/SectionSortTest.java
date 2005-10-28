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

package org.sakaiproject.test.section;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.component.section.CourseImpl;
import org.sakaiproject.component.section.CourseSectionImpl;
import org.sakaiproject.tool.section.decorator.InstructorSectionDecorator;
import org.sakaiproject.tool.section.decorator.StudentSectionDecorator;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SectionSortTest extends TestCase {
	private CourseSection sectionA;
	private CourseSection sectionB;
	
	private CourseSection sectionC;
	private CourseSection sectionD;
	
	private List instructorsA;
	private List instructorsB;
	
	private List categoryNames;
	private List categoryIds;
	
	protected void setUp() throws Exception {
		categoryNames = new ArrayList();
		categoryNames.add("Category A");
		categoryNames.add("Category B");
		
		categoryIds = new ArrayList();
		categoryIds.add("a category");
		categoryIds.add("b category");

		CourseImpl course = new CourseImpl();
		course.setUuid("course 1 uuid");
		course.setTitle("course 1 title");
		
		Calendar startCal = new GregorianCalendar();
		startCal.set(Calendar.HOUR_OF_DAY, 8);

		Calendar endCal = new GregorianCalendar();
		endCal.set(Calendar.HOUR_OF_DAY, 9);
		
		sectionA = new CourseSectionImpl(course, "a section",
				"a section uuid", "a category", new Integer(10), "a section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, false, false, false, false);

		sectionB = new CourseSectionImpl(course, "b section",
				"b section uuid", "a category", new Integer(20), "b section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, false, false, false, false);

		sectionC = new CourseSectionImpl(course, "c section",
				"c section uuid", "b category", new Integer(5), "c section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, false, false, false, false);
		
		startCal.set(Calendar.HOUR_OF_DAY, 9);
		endCal.set(Calendar.HOUR_OF_DAY, 10);

		sectionD = new CourseSectionImpl(course, "d section",
				"d section uuid", "b category", new Integer(15), "d section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, false, false, false, false);
		
		instructorsA = new ArrayList();
		instructorsA.add("Schmoe, Joe");
		instructorsA.add("Adams, Sally");
	
		instructorsB = new ArrayList();
		instructorsA.add("Schmoe, Joe");
	}

	
	public void testInstructorSectionDecoratorSorting() throws Exception {
		InstructorSectionDecorator secA = new InstructorSectionDecorator(sectionA, "Category A", instructorsA, 10);
		InstructorSectionDecorator secB = new InstructorSectionDecorator(sectionB, "Category A", instructorsB, 20);
		InstructorSectionDecorator secC = new InstructorSectionDecorator(sectionC, "Category B", new ArrayList(), 10);
		InstructorSectionDecorator secD = new InstructorSectionDecorator(sectionD, "Category B", new ArrayList(), 20);
		
		Comparator comp = InstructorSectionDecorator.getManagersComparator(true);

		// Compare managers in sections of the same category
		Assert.assertTrue(comp.compare(secA, secB) > 0);
		Assert.assertTrue(comp.compare(secC, secD) == 0);
		
		// Compare managers in sections in different categories.  The one with no managers sorts first
		Assert.assertTrue(comp.compare(secC, secA) > 0);
		
		comp = InstructorSectionDecorator.getEnrollmentsComparator(true, false);

		// Compare the max enrollments in sections of the same category
		Assert.assertTrue(comp.compare(secB, secA) > 0);

		// Compare the max enrollments in different categories
		Assert.assertTrue(comp.compare(secB, secC) < 0);
		
		comp = InstructorSectionDecorator.getFieldComparator("title", true);
		Assert.assertTrue(comp.compare(secA, secB) < 0);
		
	}

	public void testStudentSectionDecoratorSorting() throws Exception {
		// secA will say "Member"
		StudentSectionDecorator secA = new StudentSectionDecorator(sectionA, "Category A", instructorsA, 10, true, false);
		
		// secB will say "Switch"
		StudentSectionDecorator secB = new StudentSectionDecorator(sectionB, "Category A", instructorsB, 10, false, true);

		Comparator comp = StudentSectionDecorator.getChangeComparator(true, true, true);
	
		// Compare the change status between two sections in the same category
		Assert.assertTrue(comp.compare(secA, secB) < 0);
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
