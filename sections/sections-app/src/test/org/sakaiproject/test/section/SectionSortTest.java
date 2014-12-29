/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.test.section;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.component.section.CourseImpl;
import org.sakaiproject.component.section.CourseSectionImpl;
import org.sakaiproject.tool.section.decorator.SectionDecorator;

public class SectionSortTest extends TestCase {
	private CourseSection sectionA;
	private CourseSection sectionB;
	
	private CourseSection sectionC;
	private CourseSection sectionD;
	

	private CourseSection sectionE;
	private CourseSection sectionF;

	private List<String> instructorsA;
	private List<String> instructorsB;
	
	private List<String> categoryNames;
	private List<String> categoryIds;
	
	protected void setUp() throws Exception {
		categoryNames = new ArrayList<String>();
		categoryNames.add("Category A");
		categoryNames.add("Category B");
		
		categoryIds = new ArrayList<String>();
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
				"a section uuid", "a category", Integer.valueOf(10), "a section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				true, true, false, false, false, false, false);

		startCal.set(Calendar.HOUR_OF_DAY, 9);
		endCal.set(Calendar.HOUR_OF_DAY, 10);

		sectionB = new CourseSectionImpl(course, "B section",
				"b section uuid", "a category", Integer.valueOf(20), "b section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, true, false, false, false, false, false);

		startCal.set(Calendar.HOUR_OF_DAY, 10);
		endCal.set(Calendar.HOUR_OF_DAY, 11);

		sectionC = new CourseSectionImpl(course, "c section",
				"c section uuid", "b category", Integer.valueOf(5), "c section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, true, false, false, false, false, false);


		startCal.set(Calendar.HOUR_OF_DAY, 11);
		endCal.set(Calendar.HOUR_OF_DAY, 12);

		sectionD = new CourseSectionImpl(course, "D section",
				"d section uuid", "b category", Integer.valueOf(15), "d section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, true, true, false, false, false);
		
		startCal.set(Calendar.HOUR_OF_DAY, 12);
		endCal.set(Calendar.HOUR_OF_DAY, 13);

		sectionE = new CourseSectionImpl(course, "E section",
				"e section uuid", "b category", Integer.valueOf(15), "e section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, true, true, false, false);

		// Keep these times the same as sectionE
		
		sectionF = new CourseSectionImpl(course, "F section",
				"f section uuid", "b category", Integer.valueOf(15), "f section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, false, true, true, true);

		instructorsA = new ArrayList<String>();
		instructorsA.add("Schmoe, Joe");
		instructorsA.add("Adams, Sally");
	
		instructorsB = new ArrayList<String>();
		instructorsA.add("Schmoe, Joe");
	}

	
	public void testSectionDecoratorSorting() throws Exception {
		SectionDecorator secA = new SectionDecorator(sectionA, "Category A", instructorsA, 10, true);
		SectionDecorator secB = new SectionDecorator(sectionB, "Category A", instructorsB, 20, true);
		SectionDecorator secC = new SectionDecorator(sectionC, "Category B", new ArrayList<String>(), 10, true);
		SectionDecorator secD = new SectionDecorator(sectionD, "Category B", new ArrayList<String>(), 20, true);
		SectionDecorator secE = new SectionDecorator(sectionE, "Category B", new ArrayList<String>(), 20, true);
		SectionDecorator secF = new SectionDecorator(sectionF, "Category B", new ArrayList<String>(), 20, true);
		
		
		Comparator<SectionDecorator> mgrComp = SectionDecorator.getManagersComparator(true);

		// Compare managers in sections of the same category
		Assert.assertTrue(mgrComp.compare(secA, secB) > 0);
		Assert.assertTrue(mgrComp.compare(secC, secD) < 0); // Using the title, since managers are equal
		
		// Compare managers in sections in different categories.  The one with no managers sorts first
		Assert.assertTrue(mgrComp.compare(secC, secA) > 0);
		
		mgrComp = SectionDecorator.getEnrollmentsComparator(true, false);

		// Compare the max enrollments in sections of the same category
		Assert.assertTrue(mgrComp.compare(secB, secA) > 0);

		// Compare the max enrollments in different categories
		Assert.assertTrue(mgrComp.compare(secB, secC) < 0);
		
		// Compare the days in a meeting.
		Comparator<SectionDecorator> dayComp = SectionDecorator.getDayComparator(true);
		
		Assert.assertTrue(dayComp.compare(secA, secB) < 0);
		Assert.assertTrue(dayComp.compare(secC, secD) < 0);
		Assert.assertTrue(dayComp.compare(secD, secE) < 0);
		Assert.assertTrue(dayComp.compare(secE, secF) < 0);

		// Compare the times in meetings
		Comparator<SectionDecorator> timeComp = SectionDecorator.getTimeComparator(true);

		Assert.assertTrue(timeComp.compare(secA, secB) < 0);
		Assert.assertTrue(timeComp.compare(secA, secC) < 0);
		Assert.assertTrue(timeComp.compare(secA, secD) < 0);
		Assert.assertTrue(timeComp.compare(secA, secE) < 0);
		Assert.assertTrue(timeComp.compare(secA, secF) < 0);
		
		Assert.assertTrue(timeComp.compare(secB, secA) > 0);
		Assert.assertTrue(timeComp.compare(secB, secC) < 0);
		Assert.assertTrue(timeComp.compare(secB, secD) < 0);
		Assert.assertTrue(timeComp.compare(secB, secE) < 0);
		Assert.assertTrue(timeComp.compare(secB, secF) < 0);

		Assert.assertTrue(timeComp.compare(secC, secA) > 0);
		Assert.assertTrue(timeComp.compare(secC, secB) > 0);
		Assert.assertTrue(timeComp.compare(secC, secD) < 0);
		Assert.assertTrue(timeComp.compare(secC, secE) < 0);
		Assert.assertTrue(timeComp.compare(secC, secF) < 0);

		Assert.assertTrue(timeComp.compare(secD, secE) < 0);
		Assert.assertTrue(timeComp.compare(secE, secF) < 0); // Even though the times and categories are equal, the titles should sort e before f
		
		// Sort a list and ensure that it's in the correct order.
		List<SectionDecorator> list = new ArrayList<SectionDecorator>();
		list.add(secA);
		list.add(secB);
		list.add(secC);
		list.add(secD);
		list.add(secE);
		list.add(secF);
		Collections.sort(list, timeComp);
		Assert.assertEquals(0, list.indexOf(secA));
		Assert.assertEquals(1, list.indexOf(secB));
		Assert.assertEquals(2, list.indexOf(secC));
		Assert.assertEquals(3, list.indexOf(secD));
		Assert.assertEquals(4, list.indexOf(secE));
		Assert.assertEquals(5, list.indexOf(secF));
	}
}
