/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.impl;

import java.sql.Time;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.SectionCategory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

@Slf4j
public class SampleDataLoader implements BeanFactoryAware {
	protected static final int ACADEMIC_SESSION_YEAR;
	protected static final String[] ACADEMIC_SESSION_EIDS = new String[4];
	protected static final Date[] ACADEMIC_SESSION_START_DATES = new Date[4];
	protected static final Date[] ACADEMIC_SESSION_END_DATES = new Date[4];

	protected static final String CS = "SMPL";

	protected static final String CC1 = "SMPL101";
	protected static final String CC2 = "SMPL202";

	protected static final String CO1_PREFIX = CC1 + " ";
	protected static final String CO2_PREFIX = CC2 + " ";

	protected static final String ENROLLMENT_SET_SUFFIX = "es";

	protected static final int ENROLLMENT_SETS_PER_ACADEMIC_SESSION = 2;
	protected static final int ENROLLMENTS_PER_SET = 180;

	protected static final String[] AMPM;

	protected static final SimpleDateFormat sdf()
	{
		return new SimpleDateFormat("hh:mma");
	}
	protected static DecimalFormat df;
	static {
		GregorianCalendar startCal = new GregorianCalendar();
		GregorianCalendar endCal = new GregorianCalendar();
		ACADEMIC_SESSION_YEAR = startCal.get(Calendar.YEAR);
		
		ACADEMIC_SESSION_EIDS[0] = "Winter " + ACADEMIC_SESSION_YEAR;
		ACADEMIC_SESSION_EIDS[1] = "Spring " + ACADEMIC_SESSION_YEAR;
		ACADEMIC_SESSION_EIDS[2] = "Summer " + ACADEMIC_SESSION_YEAR;
		ACADEMIC_SESSION_EIDS[3] = "Fall " + ACADEMIC_SESSION_YEAR;

		startCal.set(ACADEMIC_SESSION_YEAR, 0, 1);
		endCal.set(ACADEMIC_SESSION_YEAR, 3, 1);
		ACADEMIC_SESSION_START_DATES[0] = startCal.getTime();
		ACADEMIC_SESSION_END_DATES[0] = endCal.getTime();

		startCal.set(ACADEMIC_SESSION_YEAR, 3, 1);
		endCal.set(ACADEMIC_SESSION_YEAR, 5, 1);
		ACADEMIC_SESSION_START_DATES[1] = startCal.getTime();
		ACADEMIC_SESSION_END_DATES[1] = endCal.getTime();

		startCal.set(ACADEMIC_SESSION_YEAR, 5, 1);
		endCal.set(ACADEMIC_SESSION_YEAR, 8, 1);
		ACADEMIC_SESSION_START_DATES[2] = startCal.getTime();
		ACADEMIC_SESSION_END_DATES[2] = endCal.getTime();

		startCal.set(ACADEMIC_SESSION_YEAR, 8, 1);
		endCal.set(ACADEMIC_SESSION_YEAR + 1, 0, 1);
		ACADEMIC_SESSION_START_DATES[3] = startCal.getTime();
		ACADEMIC_SESSION_END_DATES[3] = endCal.getTime();

		AMPM = new DateFormatSymbols().getAmPmStrings();

		df = new DecimalFormat("0000");
	}

	protected int studentMemberCount;

	// Begin Dependency Injection //
	protected CourseManagementAdministration cmAdmin;
	public void setCmAdmin(CourseManagementAdministration cmAdmin) {
		this.cmAdmin = cmAdmin;
	}

	protected CourseManagementService cmService;
	public void setCmService(CourseManagementService cmService) {
		this.cmService = cmService;
	}

	protected BeanFactory beanFactory;
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	protected AuthzGroupService authzGroupService;

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	/** A flag for disabling the sample data load */
	protected boolean loadSampleData;
	public void setLoadSampleData(boolean loadSampleData) {
		this.loadSampleData = loadSampleData;
	}
	// End Dependency Injection //

	public void init() {
		log.info("Initializing " + getClass().getName());
		if(cmAdmin == null) {
			return;
		}
		if(loadSampleData) {
			loginToSakai();
			PlatformTransactionManager tm = (PlatformTransactionManager)beanFactory.getBean("org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager");
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			TransactionStatus status = tm.getTransaction(def);
			try {
				load();
			} catch (Exception e) {
				log.error("Unable to load CM data: " + e);
				tm.rollback(status);
			} finally {
				if(!status.isCompleted()) {
					tm.commit(status);
				}
			}
			logoutFromSakai();
		} else {
			if(log.isInfoEnabled()) log.info("Skipped CM data load");
		}
	}

	public void destroy() {
		log.info("Destroying " + getClass().getName());
	}

	private void loginToSakai() {
	    Session sakaiSession = SessionManager.getCurrentSession();
		sakaiSession.setUserId("admin");
		sakaiSession.setUserEid("admin");

		// establish the user's session
		UsageSessionService.startSession("admin", "127.0.0.1", "CMSync");

		// update the user's externally provided realm definitions
		authzGroupService.refreshUser("admin");

		// post the login event
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, null, true));
	}

	private void logoutFromSakai() {
	    Session sakaiSession = SessionManager.getCurrentSession();
		sakaiSession.invalidate();

		// post the logout event
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
	}

	public void load() throws Exception {
		// Don't do anything if we've got data already.  The existence of an
		// AcademicSession for the first legacy term will be our indicator for existing
		// data.
		List<AcademicSession> existingAcademicSessions = cmService.getAcademicSessions();
		if(existingAcademicSessions != null && ! existingAcademicSessions.isEmpty()) {
			if(log.isInfoEnabled()) log.info("CM data exists, skipping data load.");
			return;
		}

		// Academic Sessions
		List<AcademicSession> academicSessions = new ArrayList<AcademicSession>();
		for(int i = 0; i < ACADEMIC_SESSION_EIDS.length; i++) {
			String academicSessionEid = ACADEMIC_SESSION_EIDS[i];
			academicSessions.add(cmAdmin.createAcademicSession(academicSessionEid,academicSessionEid,
					academicSessionEid, ACADEMIC_SESSION_START_DATES[i], ACADEMIC_SESSION_END_DATES[i]));
		}
		
		// Current Academic Sessions
		// 4 sample academic sessions have been created. Make the middle 2 "current".
		cmAdmin.setCurrentAcademicSessions(Arrays.asList(new String[] {ACADEMIC_SESSION_EIDS[1], ACADEMIC_SESSION_EIDS[2]}));

		// Course Sets
		cmAdmin.createCourseSet(CS, "Sample Department",
				"We study wet things in the Sample Dept", "DEPT", null);
		cmAdmin.addOrUpdateCourseSetMembership("da1","DeptAdmin", CS, "active");

		// Cross-listed Canonical Courses
		Set<CanonicalCourse> cc = new HashSet<CanonicalCourse>();
		cc.add(cmAdmin.createCanonicalCourse(CC1, "Sample 101", "A survey of samples"));
		cc.add(cmAdmin.createCanonicalCourse(CC2, "Sample 202", "An in depth study of samples"));
		cmAdmin.setEquivalentCanonicalCourses(cc);

		// Keep an ordered list of COs for use in building enrollment sets & adding enrollments
		List<CourseOffering> courseOfferingsList = new ArrayList<CourseOffering>();

		for(Iterator<AcademicSession> iter = academicSessions.iterator(); iter.hasNext();) {
			AcademicSession as = iter.next();
			CourseOffering co1 = cmAdmin.createCourseOffering(CO1_PREFIX + as.getEid(),
					CC1, "Sample course offering #1, " + as.getEid(), "open", as.getEid(),
					CC1, as.getStartDate(), as.getEndDate());
			CourseOffering co2 = cmAdmin.createCourseOffering(CO2_PREFIX + as.getEid(),
					CC2, "Sample course offering #2, " + as.getEid(), "open", as.getEid(),
					CC2, as.getStartDate(), as.getEndDate());

			courseOfferingsList.add(co1);
			courseOfferingsList.add(co2);

			Set<CourseOffering> courseOfferingSet = new HashSet<CourseOffering>();
			courseOfferingSet.add(co1);
			courseOfferingSet.add(co2);

			// Cross list these course offerings
			cmAdmin.setEquivalentCourseOfferings(courseOfferingSet);

			cmAdmin.addCourseOfferingToCourseSet(CS, co1.getEid());
			cmAdmin.addCourseOfferingToCourseSet(CS, co2.getEid());

			// And add some other instructors at the offering level (this should help with testing cross listings)
			cmAdmin.addOrUpdateCourseOfferingMembership("instructor1","I", co1.getEid(), null);
			cmAdmin.addOrUpdateCourseOfferingMembership("instructor2","I", co2.getEid(), null);
		}

		Map<String, String> enrollmentStatuses = cmService.getEnrollmentStatusDescriptions(Locale.US);
		Map<String, String> gradingSchemes = cmService.getGradingSchemeDescriptions(Locale.US);

		List<String> enrollmentEntries = new ArrayList<String>(enrollmentStatuses.keySet());
		List<String> gradingEntries = new ArrayList<String>(gradingSchemes.keySet());
		int enrollmentIndex = 0;
		int gradingIndex = 0;

		// Enrollment sets and sections
		Set<String> instructors = new HashSet<String>();
		instructors.add("admin");
		instructors.add("instructor");


		int enrollmentOffset = 1;
		for(Iterator<CourseOffering> iter = courseOfferingsList.iterator(); iter.hasNext();) {
			if(enrollmentOffset > (ENROLLMENT_SETS_PER_ACADEMIC_SESSION * ENROLLMENTS_PER_SET )) {
				enrollmentOffset = 1;
			}

			CourseOffering co = iter.next();
			EnrollmentSet es = cmAdmin.createEnrollmentSet(co.getEid() + ENROLLMENT_SET_SUFFIX,
					co.getTitle() + " Enrollment Set", co.getDescription() + " Enrollment Set",
					"lecture", "3", co.getEid(), instructors);

			// Enrollments
			for(int enrollmentCounter = enrollmentOffset; enrollmentCounter < (enrollmentOffset + ENROLLMENTS_PER_SET ); enrollmentCounter++) {
				if(++gradingIndex == gradingEntries.size()) {
					gradingIndex = 0;
				}
				String gradingScheme = gradingEntries.get(gradingIndex);

				if(++enrollmentIndex == enrollmentEntries.size()) {
					enrollmentIndex = 0;
				}
				String enrollmentStatus = enrollmentEntries.get(enrollmentIndex);

				cmAdmin.addOrUpdateEnrollment("student" + df.format(enrollmentCounter), es.getEid(), enrollmentStatus, "3", gradingScheme);
			}
			enrollmentOffset += ENROLLMENTS_PER_SET;
		}

		// Don't load the sections in a loop, since we need to define specific data for each
		// Section Categories (these are returned in alpha order, so we can control the order here)
		SectionCategory lectureCategory = cmAdmin.addSectionCategory("01.lct", "Lecture");
		SectionCategory discussionCategory = cmAdmin.addSectionCategory("03.dsc", "Discussion");
		cmAdmin.addSectionCategory("02.lab", "Lab");
		cmAdmin.addSectionCategory("04.rec", "Recitation");
		cmAdmin.addSectionCategory("05.sto", "Studio");

		for(Iterator<AcademicSession> iter = cmService.getAcademicSessions().iterator(); iter.hasNext();) {
			AcademicSession as = iter.next();

			// Clear the student count for this academic session
			resetStudentMemberCount();

			// Lecture Sections
			String co1Eid = CO1_PREFIX + as.getEid();
			String lec1Eid = co1Eid;
			Section lec1 = cmAdmin.createSection(lec1Eid, lec1Eid, lec1Eid + " Lecture",
				lectureCategory.getCategoryCode(), null, co1Eid, co1Eid + ENROLLMENT_SET_SUFFIX);
			Set<Meeting> lec1Meetings = new HashSet<Meeting>();
			Meeting mtg1 = cmAdmin.newSectionMeeting(lec1.getEid(), "A Building 11", getTime("10:30" + AMPM[0]), getTime("11:00" + AMPM[0]), null);
			mtg1.setMonday(true);
			mtg1.setWednesday(true);
			mtg1.setFriday(true);
			lec1Meetings.add(mtg1);
			lec1.setMeetings(lec1Meetings);
			cmAdmin.updateSection(lec1);
			if(log.isDebugEnabled()) log.debug("Created section " + lec1Eid);

			String co2Eid = CO2_PREFIX + as.getEid();
			String lec2Eid = co2Eid;
			Section lec2 = cmAdmin.createSection(lec2Eid, lec2Eid, lec2Eid + " Lecture",
				lectureCategory.getCategoryCode(), null, co2Eid, co2Eid + ENROLLMENT_SET_SUFFIX);
			Set<Meeting> lec2Meetings = new HashSet<Meeting>();
			Meeting mtg2 = cmAdmin.newSectionMeeting(lec2.getEid(), "A Building 11", getTime("10:30" + AMPM[0]), getTime("11:00" + AMPM[0]), null);
			mtg2.setMonday(true);
			mtg2.setWednesday(true);
			mtg2.setFriday(true);
			lec2Meetings.add(mtg2);
			lec2.setMeetings(lec2Meetings);
			cmAdmin.updateSection(lec2);
			if(log.isDebugEnabled()) log.debug("Created section " + lec2Eid);

			// Discussion sections, first Course Offering

			loadDiscussionSection("Discussion 1 " + CC1, as.getEid(), co1Eid,
					discussionCategory.getCategoryCode(), null, null, null,
					new boolean[]{false, false, false, false, false, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 2 " + CC1, as.getEid(), co1Eid,
					discussionCategory.getCategoryCode(), "B Building 202",
					getTime("10:00" + AMPM[0]), getTime("11:30" + AMPM[0]),
					new boolean[]{false, true, false, true, false, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 3 " + CC1, as.getEid(), co1Eid,
					discussionCategory.getCategoryCode(), "B Hall 11",
					getTime("9:00" + AMPM[0]), getTime("10:30" + AMPM[0]),
					new boolean[]{false, true, false, true, false, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 4 " + CC1, as.getEid(), co1Eid,
					discussionCategory.getCategoryCode(), "C Building 100",
					getTime("1:30" + AMPM[1]), getTime("3:00" + AMPM[1]),
					new boolean[]{false, true, false, true, false, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 5 " + CC1, as.getEid(), co1Eid,
					discussionCategory.getCategoryCode(), "Building 10",
					getTime("9:00" + AMPM[0]), getTime("10:00" + AMPM[0]),
					new boolean[]{true, false, true, false, true, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 6 " + CC1, as.getEid(), co1Eid,
					discussionCategory.getCategoryCode(), "Hall 200",
					getTime("4:00" + AMPM[1]), getTime("5:00" + AMPM[1]),
					new boolean[]{true, false, true, false, true, false, false}, studentMemberCount, incrementStudentCount());

			// Discussion sections, second Course Offering

			loadDiscussionSection("Discussion 1 " + CC2, as.getEid(), co2Eid,
					discussionCategory.getCategoryCode(), null, null, null,
					new boolean[]{false, false, false, false, false, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 2 " + CC2, as.getEid(), co2Eid,
					discussionCategory.getCategoryCode(), "2 Building A",
					getTime("11:30" + AMPM[0]), getTime("1:00" + AMPM[1]),
					new boolean[]{false, true, false, true, false, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 3 " + CC2, as.getEid(), co2Eid,
					discussionCategory.getCategoryCode(), "101 Hall A",
					getTime("10:00" + AMPM[0]), getTime("11:00" + AMPM[0]),
					new boolean[]{true, false, true, false, true, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 4 " + CC2, as.getEid(), co2Eid,
					discussionCategory.getCategoryCode(), "202 Building",
					getTime("8:00" + AMPM[0]), getTime("9:00" + AMPM[0]),
					new boolean[]{true, false, true, false, true, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 5 " + CC2, as.getEid(), co2Eid,
					discussionCategory.getCategoryCode(), "11 Hall B",
					getTime("2:00" + AMPM[1]), getTime("3:30" + AMPM[1]),
					new boolean[]{false, true, false, true, false, false, false}, studentMemberCount, incrementStudentCount());

			loadDiscussionSection("Discussion 6 " + CC2, as.getEid(), co2Eid,
					discussionCategory.getCategoryCode(), "100 Building C",
					getTime("3:00" + AMPM[1]), getTime("4:00" + AMPM[1]),
					new boolean[]{true, false, true, false, true, false, false}, studentMemberCount, incrementStudentCount());
		}

		if(log.isInfoEnabled()) log.info("Finished loading sample CM data");
	}

	protected Time getTime(String timeString) {
		Date date = null;
		try {
			date = sdf().parse(timeString);
		} catch (ParseException pe) {
			log.error("Can not parse time " + timeString);
			date = new Date();
		}
		return new Time(date.getTime());
	}

	protected void loadDiscussionSection(String secEidPrefix, String asEid, String coEid, String categoryCode,
			String location, Time startTime, Time endTime, boolean[] days, int studentStart, int studentEnd) {
		String secEid = secEidPrefix + " " + asEid;
		Section sec = cmAdmin.createSection(secEid, secEidPrefix, secEid,
				categoryCode, null, coEid, null);
		for(int studentCounter = studentStart; studentCounter < studentEnd ; studentCounter++) {
			String zeroPaddedId = df.format(studentCounter);
			cmAdmin.addOrUpdateSectionMembership("student" + zeroPaddedId, "S", secEid, "member");
		}
		cmAdmin.addOrUpdateSectionMembership("instructor", "I", secEid, "section_leader");
		cmAdmin.addOrUpdateSectionMembership("admin", "I", secEid, "section_leader");

		//SAK-25394 add ta's for testing purposes
		int sectionNum = Integer.parseInt(secEidPrefix.substring("Discussion ".length(),"Discussion ".length()+1));
		switch (sectionNum) {
			case 1: cmAdmin.addOrUpdateSectionMembership("ta1", "GSI", secEid, "section_leader"); break;
			case 2: cmAdmin.addOrUpdateSectionMembership("ta2", "GSI", secEid, "section_leader"); break;
			case 3: cmAdmin.addOrUpdateSectionMembership("ta3", "GSI", secEid, "section_leader"); break;
			case 4: cmAdmin.addOrUpdateSectionMembership("ta", "GSI", secEid, "section_leader");
					cmAdmin.addOrUpdateSectionMembership("ta1", "GSI", secEid, "section_leader");
					break;
			case 5: cmAdmin.addOrUpdateSectionMembership("ta", "GSI", secEid, "section_leader");
					cmAdmin.addOrUpdateSectionMembership("ta2", "GSI", secEid, "section_leader");
					break;
			default: cmAdmin.addOrUpdateSectionMembership("ta", "GSI", secEid, "section_leader"); break;
		}

		Set<Meeting> meetings = new HashSet<Meeting>();
		Meeting mtg = cmAdmin.newSectionMeeting(secEid, location, startTime, endTime, null);
		mtg.setMonday(days[0]);
		mtg.setTuesday(days[1]);
		mtg.setWednesday(days[2]);
		mtg.setThursday(days[3]);
		mtg.setFriday(days[4]);
		mtg.setSaturday(days[5]);
		mtg.setSunday(days[6]);
		meetings.add(mtg);
		sec.setMeetings(meetings);
		cmAdmin.updateSection(sec);

		if(log.isDebugEnabled()) log.debug("Created section " + secEid);
	}

	protected int incrementStudentCount() {
		studentMemberCount += 30;
		return studentMemberCount;
	}

	protected void resetStudentMemberCount() {
		studentMemberCount = 1;
	}
}
