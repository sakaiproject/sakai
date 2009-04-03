/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.test;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;

public class StandaloneSectionsDataLoader extends GradebookTestBase {
	private static Log log = LogFactory.getLog(StandaloneSectionsDataLoader.class);

    public static final String[] SITE_UIDS = {
		"QA_1",
		"QA_2",
		"QA_3",
		"QA_4",
		"QA_5",
		"QA_6",
		"QA_7",
		"QA_8",
    };
    public static final String[] SITE_NAMES = {
		"QA Site #1 [no students, 1 instructor, no self reg, no self switch, external]",
		"QA Site #2 [10 students, 1 instructor, no self reg, no self switch, internal]",
		"QA Site #3 [10 students, 2 instructors, no self reg, no self switch, internal]",
		"QA Site #4 [10 students, 1 instructor, no self reg, no self switch, internal]",
		"QA Site #5 [50 students, 1 instructor, no self reg, no self switch, internal]",
		"QA Site #6 [50 students, 1 instructor, no self reg, no self switch, internal]",
		"QA Site #7 [150 students, 1 instructor, no self reg, no self switch, internal]",
		"QA Site #8 [400 students, 1 instructor, no self reg, no self switch, internal]",
    };
    public static boolean[] SITE_SELF_REG = {
    	false, false, false, false,
    	false, false, false, false,
    };
    public static boolean[] SITE_SELF_SWITCH = {
    	false, false, false, false,
    	false, false, false, false,
    };
    public static boolean[] SITE_EXTERNALLY_MANAGED = {
    	true, false, false, false,
    	false, false, false, false,
    };

    /** Special users */
    public final static String AUTHID_TEACHER_ALL = "authid_teacher";
    public final static String AUTHID_TEACHER_AND_STUDENT = "authid_teacher_student";
    public final static String AUTHID_STUDENT_ALL = "stu_0";
    public final static String AUTHID_NO_SITE = "authid_nowhere";
    public final static String AUTHID_STUDENT_PREFIX = "stu_";
    public static final String AUTHID_WITHOUT_GRADES_1 = "stu_16";
    public static final String AUTHID_WITHOUT_GRADES_2 = "stu_17";
    public static final String AUTHID_TA = "authid_ta";

	/** Special sites */
	public static int SITE_AMBIGUOUS_TEACHER = 2;
	public static int SITE_AMBIGUOUS_STUDENT = 5;
	public static int SITE_LOADED_UP = 5;

	public StandaloneSectionsDataLoader() {
    	// Don't roll these tests back, since they are intended to load data
		setDefaultRollback(false);
	}

	public void testLoadData() {
		// Load courses. (No sections yet!)
		List sites = new ArrayList(SITE_UIDS.length);
		for (int i = 0; i < SITE_UIDS.length; i++) {
			Course courseSite = integrationSupport.createCourse(SITE_UIDS[i], SITE_NAMES[i], SITE_EXTERNALLY_MANAGED[i], SITE_SELF_REG[i], SITE_SELF_SWITCH[i]);
			sites.add(courseSite);
		}

		// Load users.
        User teacherAll = userManager.createUser(AUTHID_TEACHER_ALL, "Bizzy Teacher", "Teacher, Bizzy", "uTeacher");
        User teacherStudent = userManager.createUser(AUTHID_TEACHER_AND_STUDENT, "Teaching Student", "Student, Teaching", "uTeSt");
        List students = new ArrayList(400);

		for(int i=0; i < 400; i++) {
			String firstName;
			String lastName;
			switch(i) {
				case 0:
					firstName = "Abby Lynn";
					lastName = "Astudent";
					break;
				case 1:
					firstName = "Mary";
					lastName = "LongLastNameThatExceedsTheMaximumInTheGradebook";
					break;
				case 3:
					firstName = "Susan";
					lastName = "Smith-Morris";
					break;
				case 4:
					firstName = "Nathan Q., Jr.";
					lastName = "Brewster";
					break;
				case 5:
					firstName = "Carol Lee";
					lastName = "Williams";
					break;
				case 6:
					firstName = "Kim";
					lastName = "Jones Parker";
					break;
				case 7:
					firstName = "Joe";
					lastName = "Brown";
					break;
				case 8:
					firstName = "Joe";
					lastName = "Brown";
					break;
				case 9:
					firstName = "Sarah Jane";
					lastName = "Miller";
					break;
				case 10:
					firstName = "Rachel";
					lastName = "Wilson";
					break;
				case 11:
					firstName = "Ali";
					lastName = "Moore";
					break;
				case 12:
					firstName = "Chen-Wai";
					lastName = "Taylor";
					break;
				case 13:
					firstName = "Samuel Taylor Coleridge";
					lastName = "Ascot";
					break;
				case 14:
					firstName = "Jane Quincy";
					lastName = "Brandenburg";
					break;
				case 15:
					firstName = "Thor";
					lastName = "Mj\u00F8lner";
					break;
				case 16:
					firstName = "Lazy";
					lastName = "Etudient1";
					break;
				case 17:
					firstName = "Lazy";
					lastName = "Etudient2";
					break;
				default:
					firstName = "First Middle";
					lastName = "LastName" + i;
			}
			String uidPrefix = (i != 3) ? "uid_" : "uID_";
			students.add(userManager.createUser(AUTHID_STUDENT_PREFIX + i, firstName + " " + lastName, lastName + ", " + firstName, uidPrefix + i));
		}
		userManager.createUser(AUTHID_NO_SITE, "Johnny Nobody", "Nobody, Johnny", AUTHID_NO_SITE);

		// Load enrollments into the courses.
		for (int i = 0; i < students.size(); i++) {
			// Everyone is added to Site 8.
			integrationSupport.addSiteMembership(((User)students.get(i)).getUserUid(), SITE_UIDS[7], Role.STUDENT);

			// The first 150 students are added to Site 7.
			if (i < 150) {
				integrationSupport.addSiteMembership(((User)students.get(i)).getUserUid(), SITE_UIDS[6], Role.STUDENT);

				// The first 50 students are added to Site 5 and 6, but 6 contains a special student....
				if (i < 50) {
					integrationSupport.addSiteMembership(((User)students.get(i)).getUserUid(), SITE_UIDS[4], Role.STUDENT);
					if (i < 49) {
						integrationSupport.addSiteMembership(((User)students.get(i)).getUserUid(), SITE_UIDS[5], Role.STUDENT);

						// The first 10 students are added to Site 2, 3, and 4.
						if (i < 10) {
							integrationSupport.addSiteMembership(((User)students.get(i)).getUserUid(), SITE_UIDS[3], Role.STUDENT);
							integrationSupport.addSiteMembership(((User)students.get(i)).getUserUid(), SITE_UIDS[2], Role.STUDENT);
							integrationSupport.addSiteMembership(((User)students.get(i)).getUserUid(), SITE_UIDS[1], Role.STUDENT);
						}
					}
				}
			}
		}

		// Load instructors into the courses.
		for (int i = 0; i < SITE_UIDS.length; i++) {
			integrationSupport.addSiteMembership(teacherAll.getUserUid(), SITE_UIDS[i], Role.INSTRUCTOR);
		}

		// Load the ambiguous one.
		integrationSupport.addSiteMembership(teacherStudent.getUserUid(), SITE_UIDS[SITE_AMBIGUOUS_TEACHER], Role.INSTRUCTOR);
		integrationSupport.addSiteMembership(teacherStudent.getUserUid(), SITE_UIDS[SITE_AMBIGUOUS_STUDENT], Role.STUDENT);

		// Define and load sections for the assignment-loaded site.
		String loadedSiteUid = ((Course)sites.get(SITE_LOADED_UP)).getUuid();
		List sectionCategories = sectionAwareness.getSectionCategories(loadedSiteUid);

		// We'd better have more than one category for this to work....
		String catId = (String)sectionCategories.get(1);
		String catName = sectionAwareness.getCategoryName(catId, Locale.US);
		List catASectionUuids = new ArrayList();
		for (int i = 0; i < 4; i++) {
			String sectionName;
			if (i != 2) {
				sectionName = catName + " " + (i + 1);
			} else {
				sectionName = "Abe's " + catName;
			}
			CourseSection section = integrationSupport.createSection(loadedSiteUid, sectionName, catId, new Integer(40), "Room 2" + i, null, null, true, false, true,  false, false, false, false);
			catASectionUuids.add(section.getUuid());
		}
		catId = (String)sectionCategories.get(0);
		catName = sectionAwareness.getCategoryName(catId, Locale.US);
		List catBSectionUuids = new ArrayList();
		for (int i = 0; i < 2; i++) {
			String sectionName = catName + " " + (i + 1);
			CourseSection section = integrationSupport.createSection(loadedSiteUid, sectionName, catId, new Integer(40), "Room 3" + i, null, null, true, false, true,  false, false, false, false);
			catBSectionUuids.add(section.getUuid());
		}

		// Populate the sections. Not all students will end up in a Category A section.
		List enrollments = sectionAwareness.getSiteMembersInRole(SITE_UIDS[SITE_LOADED_UP], Role.STUDENT);
		for (int i = 0; i < enrollments.size(); i++) {
			String userUid = ((EnrollmentRecord)enrollments.get(i)).getUser().getUserUid();
			String sectionUuid = (String)catBSectionUuids.get(i % catBSectionUuids.size());
			integrationSupport.addSectionMembership(userUid, sectionUuid, Role.STUDENT);
			if (i < (enrollments.size() - 5)) {
				sectionUuid = (String)catASectionUuids.get(i % catASectionUuids.size());
				integrationSupport.addSectionMembership(userUid, sectionUuid, Role.STUDENT);
			}
		}

		// Add a TA to the site and two sections.
		userManager.createUser(AUTHID_TA, "Teech N. Assist", "Assist, Teech N.", "uTA");
		integrationSupport.addSiteMembership(AUTHID_TA, SITE_UIDS[SITE_LOADED_UP], Role.TA);
		integrationSupport.addSectionMembership(AUTHID_TA, (String)catASectionUuids.get(2), Role.TA);
		integrationSupport.addSectionMembership(AUTHID_TA, (String)catBSectionUuids.get(1), Role.TA);
	}
}

/**********************************************************************************
 * $Id$
 *********************************************************************************/
