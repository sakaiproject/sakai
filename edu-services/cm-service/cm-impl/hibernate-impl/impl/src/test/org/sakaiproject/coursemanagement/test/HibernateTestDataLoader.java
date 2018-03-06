/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.impl.DataLoader;
import org.sakaiproject.coursemanagement.impl.AcademicSessionCmImpl;
import org.sakaiproject.coursemanagement.impl.CanonicalCourseCmImpl;
import org.sakaiproject.coursemanagement.impl.CourseOfferingCmImpl;
import org.sakaiproject.coursemanagement.impl.CourseSetCmImpl;
import org.sakaiproject.coursemanagement.impl.CrossListingCmImpl;
import org.sakaiproject.coursemanagement.impl.EnrollmentCmImpl;
import org.sakaiproject.coursemanagement.impl.EnrollmentSetCmImpl;
import org.sakaiproject.coursemanagement.impl.MembershipCmImpl;
import org.sakaiproject.coursemanagement.impl.SectionCategoryCmImpl;
import org.sakaiproject.coursemanagement.impl.SectionCmImpl;

/**
 * Loads data into the current transaction for use in a test case.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Slf4j
public class HibernateTestDataLoader extends HibernateDaoSupport implements DataLoader {
	private CourseManagementService cm;

	public void setCourseManagementService(CourseManagementService cm) {
		this.cm = cm;
	}

	public static void main(String[] args) {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(new String[] {"spring-test.xml", "spring-config-test.xml"});
		DataLoader loader = (DataLoader)ac.getBean(DataLoader.class.getName());
		try {
			loader.load();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void load() {
		loadAcademicSessions();
		loadCourseSetsAndMembers();
		loadCanonicalCourses();
		loadCourseOfferingsAndMembers();
		loadSectionsAndMembers();
		loadEnrollmentSets();
		loadEnrollments();
		
		getHibernateTemplate().flush();
		getHibernateTemplate().clear();
	}
	
	void loadAcademicSessions() {
		AcademicSessionCmImpl term = new AcademicSessionCmImpl();
		term.setEid("F2006");
		term.setTitle("Fall 2006");
		term.setDescription("Fall 2006, starts Sept 1, 2006");
		term.setCurrent(true);
		getHibernateTemplate().save(term);
	}
	
	void loadCourseSetsAndMembers() {
		CourseSetCmImpl cSet = new CourseSetCmImpl("BIO_DEPT", "Biology Department", "Department of Biology", "DEPT", null);
		getHibernateTemplate().save(cSet);

		MembershipCmImpl courseSetMember = new MembershipCmImpl();
		courseSetMember.setRole("departmentAdmin");
		courseSetMember.setUserId("user1");
		courseSetMember.setMemberContainer(cSet);
		courseSetMember.setStatus("active");
		getHibernateTemplate().save(courseSetMember);
		
		CourseSetCmImpl cSetChild = new CourseSetCmImpl("BIO_CHEM_GROUP", "Biochem Group", "Biochemistry group, Department of Biology", "DEPT_GROUP", cSet);
		getHibernateTemplate().save(cSetChild);
		
		CourseSetCmImpl cSetEmpty = new CourseSetCmImpl("EMPTY_COURSE_SET", "Empty CourseSet", "Empty CourseSet", null, null);
		getHibernateTemplate().save(cSetEmpty);
	}

	void loadCanonicalCourses() {
		// Cross-list bio and chem (but not English)
		CrossListingCmImpl cl = new CrossListingCmImpl();
		getHibernateTemplate().save(cl);

		// Build and save the CanonicalCourses
		CanonicalCourseCmImpl cc1 = new CanonicalCourseCmImpl();
		cc1.setEid("BIO101");
		cc1.setTitle("Biology 101");
		cc1.setDescription("An intro to biology");
		cc1.setCrossListing(cl);
		getHibernateTemplate().save(cc1);
				
		CanonicalCourseCmImpl cc2 = new CanonicalCourseCmImpl();
		cc2.setEid("CHEM101");
		cc2.setTitle("Chem 101");
		cc2.setDescription("An intro to chemistry");
		cc2.setCrossListing(cl);
		getHibernateTemplate().save(cc2);
				
		CanonicalCourseCmImpl cc3 = new CanonicalCourseCmImpl();
		cc3.setEid("ENG101");
		cc3.setTitle("English 101");
		cc3.setDescription("An intro to English");
		getHibernateTemplate().save(cc3);
				
		// Add these canonical courses to course sets
		CourseSetCmImpl bioCset = (CourseSetCmImpl)cm.getCourseSet("BIO_DEPT");
		CourseSetCmImpl bioChemCset = (CourseSetCmImpl)cm.getCourseSet("BIO_CHEM_GROUP");
		
		Set bioCourses = new HashSet();
		bioCourses.add(cc1);
		bioCset.setCanonicalCourses(bioCourses);
		
		getHibernateTemplate().update(bioCset);
		
		Set bioChemCourses = new HashSet();
		bioChemCourses.add(cc1);
		bioChemCourses.add(cc2);
		bioChemCset.setCanonicalCourses(bioChemCourses);
		getHibernateTemplate().update(bioChemCset);		
	}
	
	void loadCourseOfferingsAndMembers() {
		// Get the object dependencies
		AcademicSession term = cm.getAcademicSession("F2006");
		CanonicalCourseCmImpl cc1 = (CanonicalCourseCmImpl)cm.getCanonicalCourse("BIO101");
		CanonicalCourseCmImpl cc2 = (CanonicalCourseCmImpl)cm.getCanonicalCourse("CHEM101");
		CanonicalCourseCmImpl cc3 = (CanonicalCourseCmImpl)cm.getCanonicalCourse("ENG101");

		// Cross list bio and chem, but not English
		CrossListingCmImpl cl = new CrossListingCmImpl();
		getHibernateTemplate().save(cl);

		CourseOfferingCmImpl co1 = new CourseOfferingCmImpl();
		co1.setAcademicSession(term);
		co1.setCanonicalCourse(cc1);
		co1.setCrossListing(cl);
		co1.setEid("BIO101_F2006_01");
		co1.setTitle("Bio 101: It's all about the gene");
		co1.setDescription("Fall 2006 Bio 101 Offering");
		
		// Make this almost always a "current" course offering (until 2020, that is)
		co1.setStartDate(new Date(0));
		co1.setEndDate(new Date(1577836800000L));
		
		getHibernateTemplate().save(co1);

		CourseOfferingCmImpl co2 = new CourseOfferingCmImpl();
		co2.setAcademicSession(term);
		co2.setCanonicalCourse(cc2);
		co2.setCrossListing(cl);
		co2.setEid("CHEM101_F2006_01");
		co2.setTitle("Chem 101: It's all about the gene");
		co2.setDescription("Fall 2006 Chem 101 Offering");
		
		// Make this almost never a "current" course offering (until 2020, that is)
		co2.setStartDate(new Date(1577836800000L));
		co2.setEndDate(new Date(1577836800000L));

		getHibernateTemplate().save(co2);

		CourseOfferingCmImpl co3 = new CourseOfferingCmImpl();
		co3.setAcademicSession(term);
		co3.setCanonicalCourse(cc3);
		co3.setEid("ENG101_F2006_01");
		co3.setTitle("English 101: Intro to literature");
		co3.setDescription("Fall 2006 Eng 101 Offering");
		getHibernateTemplate().save(co3);
		
		CourseOfferingCmImpl co4 = new CourseOfferingCmImpl();
		co4.setAcademicSession(term);
		co4.setCanonicalCourse(cc3);
		co4.setEid("ENG101_F2006_02");
		co4.setTitle("English 101: Intro to literature");
		co4.setDescription("Fall 2006 Eng 101 Offering");
		//we need this one to be active
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.MONTH, -1);
		co4.setStartDate(cal.getTime());
		cal.add(Calendar.MONTH, +7);
		co4.setEndDate(cal.getTime());
		getHibernateTemplate().save(co4);

		// Add these course offerings to course sets
		CourseSetCmImpl bioCset = (CourseSetCmImpl)cm.getCourseSet("BIO_DEPT");
		CourseSetCmImpl bioChemCset = (CourseSetCmImpl)cm.getCourseSet("BIO_CHEM_GROUP");
		
		Set bioCourses = new HashSet();
		bioCourses.add(co1);
		bioCset.setCourseOfferings(bioCourses);
		
		getHibernateTemplate().update(bioCset);
		
		Set bioChemCourses = new HashSet();
		bioChemCourses.add(co1);
		bioChemCourses.add(co2);
		bioChemCset.setCourseOfferings(bioChemCourses);
		getHibernateTemplate().update(bioChemCset);
		
		// Add a member to some CourseOfferings
		MembershipCmImpl member1 = new MembershipCmImpl("coUser", "coRole1", co1, "active");
		getHibernateTemplate().save(member1);

		MembershipCmImpl member2 = new MembershipCmImpl("coUser", "coRole2", co2, "active");
		getHibernateTemplate().save(member2);
}
	
	void loadSectionsAndMembers() {
		CourseOffering co = cm.getCourseOffering("BIO101_F2006_01");

		// Add the section categories
		getHibernateTemplate().save(new SectionCategoryCmImpl("lct", "Lecture"));
		getHibernateTemplate().save(new SectionCategoryCmImpl("lab", "Lab"));
		getHibernateTemplate().save(new SectionCategoryCmImpl("dis", "Discussion"));
		
		// Add a section
		SectionCmImpl section = new SectionCmImpl();
		section.setCategory("lct");
		section.setCourseOffering(co);
		section.setDescription("The lecture");
		section.setEid("BIO101_F2006_01_SEC01");
		section.setTitle("Main lecture");
                section.setMaxSize(Integer.valueOf(100));
		getHibernateTemplate().save(section);

		// Add a membership to this section
		MembershipCmImpl member = new MembershipCmImpl();
		member.setRole("AN_ENTERPRISE_ROLE");
		member.setUserId("AN_ENTERPRISE_USER");
		member.setMemberContainer(section);
		member.setStatus("active");
		getHibernateTemplate().save(member);

		// Add a child section
		SectionCmImpl childSection = new SectionCmImpl();
		childSection.setCategory("lab");
		childSection.setCourseOffering(co);
		childSection.setDescription("Joe's monday morning lab");
		childSection.setEid("BIO101_F2006_01_SEC02");
		childSection.setTitle("Joe's Monday Morning Biology Lab");
		childSection.setParent(section);
                childSection.setMaxSize(Integer.valueOf(100));
		getHibernateTemplate().save(childSection);

		// Add a section for the future course offering
		CourseOffering futureCo = cm.getCourseOffering("CHEM101_F2006_01");
		
		SectionCmImpl futureSection = new SectionCmImpl();
		futureSection.setCategory("lab");
		futureSection.setCourseOffering(futureCo);
		futureSection.setDescription("Future lab");
		futureSection.setEid("CHEM101_F2006_01_SEC01");
		futureSection.setTitle("Future Lab");
                futureSection.setMaxSize(Integer.valueOf(100));
		getHibernateTemplate().save(futureSection);

		// Add a member to this future section
		MembershipCmImpl member2 = new MembershipCmImpl();
		member2.setRole("student");
		member2.setUserId("josh");
		member2.setMemberContainer(futureSection);
		member2.setStatus("active");
		getHibernateTemplate().save(member2);
	}
	
	void loadEnrollmentSets() {
		EnrollmentSetCmImpl enrollmentSet1 = new EnrollmentSetCmImpl();
		enrollmentSet1.setCategory("lab");
		enrollmentSet1.setCourseOffering(cm.getCourseOffering("BIO101_F2006_01"));
		enrollmentSet1.setDefaultEnrollmentCredits("3");
		enrollmentSet1.setDescription("An enrollment set description");
		enrollmentSet1.setEid("BIO101_F2006_01_ES01");
		enrollmentSet1.setTitle("The lab enrollment set");
		Set officialInstructors = new HashSet();
		officialInstructors.add("grader1");
		officialInstructors.add("grader2");
		enrollmentSet1.setOfficialInstructors(officialInstructors);
		getHibernateTemplate().save(enrollmentSet1);

		SectionCmImpl section1 = (SectionCmImpl)cm.getSection("BIO101_F2006_01_SEC01");
		section1.setEnrollmentSet(enrollmentSet1);
		getHibernateTemplate().update(section1);
		
		EnrollmentSetCmImpl enrollmentSet2 = new EnrollmentSetCmImpl();
		enrollmentSet2.setCategory("lab");
		enrollmentSet2.setCourseOffering(cm.getCourseOffering("CHEM101_F2006_01"));
		enrollmentSet2.setDefaultEnrollmentCredits("3");
		enrollmentSet2.setDescription("Another enrollment set description");
		enrollmentSet2.setEid("CHEM101_F2006_01_ES01");
		enrollmentSet2.setTitle("The lab enrollment set");

		getHibernateTemplate().save(enrollmentSet2);

		SectionCmImpl section2 = (SectionCmImpl)cm.getSection("CHEM101_F2006_01_SEC01");
		section2.setEnrollmentSet(enrollmentSet2);
		getHibernateTemplate().update(section2);
}
	
	void loadEnrollments() {
		EnrollmentSet enrollmentSet = cm.getEnrollmentSet("BIO101_F2006_01_ES01");
		EnrollmentCmImpl enrollment = new EnrollmentCmImpl();
		enrollment.setCredits("3");
		enrollment.setEnrollmentSet(enrollmentSet);
		enrollment.setEnrollmentStatus("waitlisted");
		enrollment.setGradingScheme("pass/fail");
		enrollment.setUserId("josh");
		getHibernateTemplate().save(enrollment);
		
		EnrollmentSet enrollmentSet2 = cm.getEnrollmentSet("CHEM101_F2006_01_ES01");
		EnrollmentCmImpl enrollment2 = new EnrollmentCmImpl();
		enrollment2.setCredits("3");
		enrollment2.setEnrollmentSet(enrollmentSet2);
		enrollment2.setEnrollmentStatus("officially enrolled");
		enrollment2.setGradingScheme("letter grade");
		enrollment2.setUserId("josh");
		enrollment2.setDropped(true);
		getHibernateTemplate().save(enrollment2);
		
	}
}
