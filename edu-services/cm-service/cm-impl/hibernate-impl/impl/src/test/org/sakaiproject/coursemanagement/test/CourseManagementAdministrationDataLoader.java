/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.test;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.SectionCategory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.sakaiproject.coursemanagement.impl.DataLoader;

import junit.framework.TestCase;

/**
 * Loads data into persistence.  This is not a junit test per se.  It extends TestCase
 * so it's easy to execute via maven.
 * 
 * If you want to load data into a database, just modify this class, set your db connection
 * information in hibernate.dataload.properties, and run 'maven load-data'.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class CourseManagementAdministrationDataLoader extends TestCase implements DataLoader {
	private CourseManagementAdministration cmAdmin;
	public void testLoadData() throws Exception {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(new String[] {"spring-test.xml", "spring-config-dataload.xml"});
		cmAdmin = (CourseManagementAdministration)ac.getBean(CourseManagementAdministration.class.getName());
		load();
	}

	public void load() throws Exception {
		// Academic Sessions
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		
		startCal.set(2006, 8, 1);
		endCal.set(2006, Calendar.DECEMBER, 1);
		cmAdmin.createAcademicSession("f2006", "Fall 2006", "The fall term, 2006", startCal.getTime(), endCal.getTime());
		
		startCal.set(2007, 3, 1);
		endCal.set(2007, 6, 1);
		cmAdmin.createAcademicSession("sp2007", "Spring 2007", "The spring term, 2007", startCal.getTime(), endCal.getTime());
		
		cmAdmin.createAcademicSession("IND", "Ongoing Courses", "Ongoing session for independent study", null, null);

		// Section Categories
		SectionCategory lectureCategory = cmAdmin.addSectionCategory("lct", "Lecture");
		SectionCategory labCategory = cmAdmin.addSectionCategory("lab", "Lab");
		SectionCategory discussionCategory = cmAdmin.addSectionCategory("dsc", "Discussion");
		SectionCategory recitationCategory = cmAdmin.addSectionCategory("rec", "Recitation");
		SectionCategory studioCategory = cmAdmin.addSectionCategory("sto", "Studio");
		
		// Course Sets
		cmAdmin.createCourseSet("bio", "Biology Department", "We study wet things in the Bio Dept", "DEPT", null);
		cmAdmin.addOrUpdateCourseSetMembership("da1","DeptAdmin", "bio", "active");
		
		// Canonical Courses
		cmAdmin.createCanonicalCourse("bio101", "Intro to Biology", "An introduction to biology");
		cmAdmin.createCanonicalCourse("indep_study_bio", "Independent Study in Biology", "A self paced independent study in biology.  Must be " +
				"approved and sponsored by biology department faculty.");
		
		// Course Offerings
		startCal.set(2006, 8, 1);
		endCal.set(2006, Calendar.DECEMBER, 1);
		cmAdmin.createCourseOffering("bio101_f2006", "Bio 101, Fall 2006", "Intro to Biology, Fall 06", "open", "f2006", "bio101", startCal.getTime(), endCal.getTime());
		cmAdmin.addCourseOfferingToCourseSet("bio","bio101_f2006");

		startCal.set(2007, 3, 1);
		endCal.set(2007, 6, 1);
		cmAdmin.createCourseOffering("bio101_sp2007", "Bio 101, Spring 2007", "Intro to Biology, Spring 07", "open", "sp2007", "bio101", startCal.getTime(), endCal.getTime());
		
		cmAdmin.createCourseOffering("indep_study_bio_molecular_research", "Independent study in molecular research", "Details to be determined by student and sponsor",
				"open", "IND", "indep_study_bio", null, null);
		
		// Enrollment sets
		Set instructors = new HashSet();
		instructors.add("admin");
		cmAdmin.createEnrollmentSet("bio101_f2006_lec1", "Bio 101 Lecture", "Bio 101 Lecture.  Required.", "lecture", "3", "bio101_f2006", instructors);
		
		instructors.clear();
		instructors.add("ta1");
		cmAdmin.createEnrollmentSet("bio101_f2006_lab1", "Lab 1", "Lab 1", "lab", "1", "bio101_f2006", instructors);

		instructors.clear();
		instructors.add("ta2");
		cmAdmin.createEnrollmentSet("bio101_f2006_lab2", "Lab 2", "Lab 2", "lab", "1", "bio101_f2006", instructors);

		// Enrollments
		cmAdmin.addOrUpdateEnrollment("student1", "bio101_f2006_lec1", "enrolled", "3", "standard");
		cmAdmin.addOrUpdateEnrollment("student2", "bio101_f2006_lec1", "enrolled", "3", "pass/fail");
		cmAdmin.addOrUpdateEnrollment("student3", "bio101_f2006_lec1", "waitlisted", "3", "standard");
		
		cmAdmin.addOrUpdateEnrollment("student1", "bio101_f2006_lab1", "enrolled", "1", "standard");
		cmAdmin.addOrUpdateEnrollment("student2", "bio101_f2006_lab1", "enrolled", "1", "pass/fail");

		cmAdmin.addOrUpdateEnrollment("student3", "bio101_f2006_lab2", "waitlisted", "1", "standard");

		// Sections
		Section lec1 = cmAdmin.createSection("bio101_f2006_lec1", "Bio 101, Lecture", "Intro to Biology, Fall 06, Lecture", lectureCategory.getCategoryCode(), null, "bio101_f2006", "bio101_f2006_lec1");
		cmAdmin.createSection("bio101_f2006_lab1", "Lab 1", "Intro to Biology, Fall 06, Lab 1", labCategory.getCategoryCode(), null, "bio101_f2006", "bio101_f2006_lab1");
		cmAdmin.createSection("bio101_f2006_lab2", "Lab 2", "Intro to Biology, Fall 06, Lab 2", labCategory.getCategoryCode(), null, "bio101_f2006", "bio101_f2006_lab2");
		
		// Meetings
		Set lecMeetings = new HashSet();
		lecMeetings.add(cmAdmin.newSectionMeeting("bio101_f2006_lec1", "a location", null, null, "somenotes"));
		lecMeetings.add(cmAdmin.newSectionMeeting("bio101_f2006_lec1", "another location", null, null, "some other notes"));
		lec1.setMeetings(lecMeetings);
		cmAdmin.updateSection(lec1);

	}

}
