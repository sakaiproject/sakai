/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Course;
import org.sakaiproject.site.api.CourseMember;
import org.sakaiproject.site.api.Term;

/**
 * An adapter that implements the new CourseManagementService by delegating
 * to the legacy CourseManagementProvider.  Use this implementation only if you
 * have significant investment in the legacy provider, since this adapter does not
 * provider the full capabilities of the CourseManagementService.  Also note that
 * this class is abstract, since the legacy service can not fully implement the new CM
 * methods.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
public abstract class CourseManagementLegacyAdapter implements CourseManagementService {

	private static final String CM_LEGACY_AUTHORITY = "Legacy CM Adapter";

	protected static final String[] CATEGORY_IDS = new String[] {"lect", "lab", "disc"};
	protected static final String[] CATEGORY_NAMES = new String[] {"Lecture", "Lab", "Discussion"};
	protected static final Map<String, String> CATEGORY_MAP;

	static {
		CATEGORY_MAP = new HashMap<String, String>();
		for(int i=0; i < CATEGORY_IDS.length; i++) {
			CATEGORY_MAP.put(CATEGORY_IDS[i], CATEGORY_NAMES[i]);
		}
	}

	private static final Log log = LogFactory.getLog(CourseManagementLegacyAdapter.class);

	// Inject the legacy CM service
	protected org.sakaiproject.site.api.CourseManagementService legacyCmService;
	public void setLegacyCmService(org.sakaiproject.site.api.CourseManagementService legacyCmService) {
		this.legacyCmService = legacyCmService;
	}

	
	public Map<String, String> findCourseOfferingRoles(String userEid) {
		return new HashMap<String, String>();
	}

	public Set<CourseOffering> findCourseOfferings(String courseSetEid, String academicSessionEid) throws IdNotFoundException {
		return new HashSet<CourseOffering>();
	}

	public Map<String, String> findCourseSetRoles(String userEid) {
		return new HashMap<String, String>();
	}

	public List<CourseSet> findCourseSets(String category) {
		return new ArrayList<CourseSet>();
	}

	public Set<EnrollmentSet> findCurrentlyEnrolledEnrollmentSets(String userEid) {
		return new HashSet<EnrollmentSet>();
	}

	public Set<EnrollmentSet> findCurrentlyInstructingEnrollmentSets(String userEid) {
		return new HashSet<EnrollmentSet>();
	}

	public Set<Section> findEnrolledSections(String userEid) {
		return new HashSet<Section>();
	}

	public Enrollment findEnrollment(String userEid, String enrollmentSetEid) {
		return null;
	}

	public Set<Section> findInstructingSections(String userEid) {
		// Find all of the instructing courses for all terms
		List<Course> courses = new ArrayList<Course>();
		for(Iterator termIter = legacyCmService.getTerms().iterator(); termIter.hasNext();) {
			Term term = (Term)termIter.next();
			courses.addAll(legacyCmService.getInstructorCourses(userEid, term.getYear(), term.getTerm()));
		}
		
		Set<Section> sections = new HashSet<Section>();
		for(Iterator courseIter = courses.iterator(); courseIter.hasNext();) {
			Course course = (Course)courseIter.next();
			sections.add(new SectionAdapter(course));
		}
		
		return sections;
	}

	public Set<Section> findInstructingSections(String userEid, String academicSessionEid) throws IdNotFoundException {
		
		List courses = legacyCmService.getInstructorCourses(userEid, getTermYear(academicSessionEid), getTermTerm(academicSessionEid));
		Set<Section> sections = new HashSet<Section>();
		for(Iterator courseIter = courses.iterator(); courseIter.hasNext();) {
			Course course = (Course)courseIter.next();
			sections.add(new SectionAdapter(course));
		}
		
		return sections;
	}

	/**
	 * This method can not be implemented using the legacy CM service, so you must
	 * implement it yourself.
	 */
	public abstract Map<String, String> findSectionRoles(String userEid);

	public AcademicSession getAcademicSession(String eid) throws IdNotFoundException {
		Term term = legacyCmService.getTerm(eid);
		if(term == null) {
			throw new IdNotFoundException(eid, AcademicSession.class.getName());
		}
		return new AcademicSessionAdapter(term);
	}

	public List<AcademicSession> getAcademicSessions() {
		List<AcademicSession> list = new ArrayList<AcademicSession>();
		for(Iterator iter = legacyCmService.getTerms().iterator(); iter.hasNext();) {
			Term term = (Term)iter.next();
			list.add(new AcademicSessionAdapter(term));
		}
		return list;
	}

	public CanonicalCourse getCanonicalCourse(String canonicalCourseEid) throws IdNotFoundException {
		return null;
	}

	public Set<CanonicalCourse> getCanonicalCourses(String courseSetEid) throws IdNotFoundException {
		return new HashSet<CanonicalCourse>();
	}

	public Set<CourseSet> getChildCourseSets(String parentCourseSetEid) throws IdNotFoundException {
		return new HashSet<CourseSet>();
	}

	public Set<Section> getChildSections(String parentSectionEid) throws IdNotFoundException {
		return new HashSet<Section>();
	}

	public CourseOffering getCourseOffering(String courseOfferingEid) throws IdNotFoundException {
		return null;
	}

	public Set<Membership> getCourseOfferingMemberships(String courseOfferingEid) throws IdNotFoundException {
		return new HashSet<Membership>();
	}

	public Set<CourseOffering> getCourseOfferingsInCanonicalCourse(String canonicalCourseEid) throws IdNotFoundException {
		return new HashSet<CourseOffering>();
	}

	public Set<CourseOffering> getCourseOfferingsInCourseSet(String courseSetEid) throws IdNotFoundException {
		return new HashSet<CourseOffering>();
	}

	public CourseSet getCourseSet(String courseSetEid) throws IdNotFoundException {
		return null;
	}

	public Set<Membership> getCourseSetMemberships(String courseSetEid) throws IdNotFoundException {
		return new HashSet<Membership>();
	}

	public Set<CourseSet> getCourseSets() {
		return new HashSet<CourseSet>();
	}

	public List<AcademicSession> getCurrentAcademicSessions() {
		List<AcademicSession> academicSessions = new ArrayList<AcademicSession>();

		for(Iterator iter = legacyCmService.getTerms().iterator(); iter.hasNext();) {
			Term term = (Term)iter.next();
			if(term.isCurrentTerm()) {
				academicSessions.add(new AcademicSessionAdapter(term));
			}
		}
		return academicSessions;
	}

	public EnrollmentSet getEnrollmentSet(String enrollmentSetEid) throws IdNotFoundException {
		return null;
	}

	public Set<EnrollmentSet> getEnrollmentSets(String courseOfferingEid) throws IdNotFoundException {
		return new HashSet<EnrollmentSet>();
	}

	public Set<Enrollment> getEnrollments(String enrollmentSetEid) throws IdNotFoundException {
		return new HashSet<Enrollment>();
	}

	public Set<CanonicalCourse> getEquivalentCanonicalCourses(String canonicalCourseEid) throws IdNotFoundException {
		return new HashSet<CanonicalCourse>();
	}

	public Set<CourseOffering> getEquivalentCourseOfferings(String courseOfferingEid) throws IdNotFoundException {
		return new HashSet<CourseOffering>();
	}

	public Set<String> getInstructorsOfRecordIds(String enrollmentSetEid) throws IdNotFoundException {
		return new HashSet<String>();
	}

	public Section getSection(String sectionEid) throws IdNotFoundException {
		Course course = null;
		try {
			course = legacyCmService.getCourse(sectionEid);
		} catch (IdUnusedException e) {
			throw new IdNotFoundException(sectionEid, Section.class.getName());
		}
		return new SectionAdapter(course);
	}

	public List<String> getSectionCategories() {
		return Arrays.asList(CATEGORY_IDS);
	}

	public String getSectionCategoryDescription(String categoryCode) {
		return CATEGORY_MAP.get(categoryCode);
	}

	public Set<Membership> getSectionMemberships(String sectionEid) throws IdNotFoundException {
		List legacyMembers = null;
		try {
			legacyMembers = legacyCmService.getCourseMembers(sectionEid);
		} catch (IdUnusedException e) {
			throw new IdNotFoundException(sectionEid, Section.class.getName());
		}

		Set<Membership> members = new HashSet<Membership>();
		for(Iterator iter = legacyMembers.iterator(); iter.hasNext();) {
			CourseMember member = (CourseMember)iter.next();
			members.add(new MembershipAdapter(member));
		}
		return members;
	}

	public Set<Section> getSections(String courseOfferingEid) throws IdNotFoundException {
		return new HashSet<Section>();
	}

	public boolean isAcademicSessionDefined(String eid) {
		try {
			getAcademicSession(eid);
			return true;
		} catch (IdNotFoundException idnfe) {
			return false;
		}
	}

	public boolean isCanonicalCourseDefined(String eid) {
		return false;
	}

	public boolean isCourseOfferingDefined(String eid) {
		return false;
	}

	public boolean isCourseSetDefined(String eid) {
		return false;
	}

	public boolean isEmpty(String courseSetEid) {
		return true;
	}

	public boolean isEnrolled(String userEid, Set<String> enrollmentSetEids) {
		return false;
	}

	public boolean isEnrolled(String userEid, String enrollmentSetEid) {
		return false;
	}

	public boolean isEnrollmentSetDefined(String eid) {
		return false;
	}

	public boolean isSectionDefined(String eid) {
		try {
			getSection(eid);
			return true;
		} catch (IdNotFoundException idnfe) {
			return false;
		}
	}

	/**
	 * Customize this method to match your term string format.  An example might
	 * look like this: return academicSessionEid.split("\\s")[0];
	 * 
	 * @param academicSessionEid
	 * @return
	 */
	protected abstract String getTermTerm(String academicSessionEid);
	
	/**
	 * Customize this method to match your term string format.  An example might
	 * look like this: return academicSessionEid.split("\\s")[1];
	 * 
	 * @param academicSessionEid
	 * @return
	 */
	protected abstract String getTermYear(String academicSessionEid);

	class SectionAdapter implements Section {
		protected Course course;
		protected Set<Meeting> meetings;
		
		public SectionAdapter(Course course) {
			this.meetings = new HashSet<Meeting>();
			this.course = course;
		}

		public String getAuthority() {
			return CourseManagementLegacyAdapter.CM_LEGACY_AUTHORITY;
		}

		public String getCategory() {
			return "";
		}

		public String getCourseOfferingEid() {
			return null;
		}

		public String getDescription() {
			return course.getTitle();
		}

		public String getEid() {
			return course.getId();
		}

		public EnrollmentSet getEnrollmentSet() {
			return null;
		}

		public Integer getMaxSize() {
			return null;
		}

		public Set<Meeting> getMeetings() {
			return meetings;
		}

		public Section getParent() {
			return null;
		}

		public String getTitle() {
			return course.getTitle();
		}

		public void setAuthority(String authority) {
			log.warn("Can not call setters on SectionAdapter");
		}

		public void setCategory(String category) {
			log.warn("Can not call setters on SectionAdapter");
		}

		public void setDescription(String description) {
			log.warn("Can not call setters on SectionAdapter");
		}

		public void setEid(String eid) {
			log.warn("Can not call setters on SectionAdapter");
		}

		public void setEnrollmentSet(EnrollmentSet enrollmentSet) {
			log.warn("Can not call setters on SectionAdapter");
		}

		public void setMaxSize(Integer maxSize) {
			log.warn("Can not call setters on SectionAdapter");
		}

		public void setMeetings(Set<Meeting> meetingTimes) {
			log.warn("Can not call setters on SectionAdapter");
		}

		public void setParent(Section parent) {
			log.warn("Can not call setters on SectionAdapter");
		}

		public void setTitle(String title) {
			log.warn("Can not call setters on SectionAdapter");
		}		
	}
	
	class AcademicSessionAdapter implements AcademicSession {

		protected Term term;

		public AcademicSessionAdapter(Term term) {
			this.term = term;
		}
		
		public String getAuthority() {
			return CourseManagementLegacyAdapter.CM_LEGACY_AUTHORITY;
		}

		public String getDescription() {
			return getTitle();
		}

		public String getEid() {
			return term.getId();
		}

		public Date getEndDate() {
			return new Date(term.getEndTime().getTime());
		}

		public Date getStartDate() {
			return new Date(term.getStartTime().getTime());
		}

		public String getTitle() {
			return term.getTerm() + " " + term.getYear();
		}

		public void setAuthority(String authority) {
			log.warn("Can not call setters on AcademicSessionAdapter");
		}

		public void setDescription(String description) {
			log.warn("Can not call setters on AcademicSessionAdapter");
		}

		public void setEid(String eid) {
			log.warn("Can not call setters on AcademicSessionAdapter");
		}

		public void setEndDate(Date endDate) {
			log.warn("Can not call setters on AcademicSessionAdapter");
		}

		public void setStartDate(Date startDate) {
			log.warn("Can not call setters on AcademicSessionAdapter");
		}

		public void setTitle(String title) {
			log.warn("Can not call setters on AcademicSessionAdapter");
		}
	}

	class MembershipAdapter implements Membership {

		protected CourseMember courseMember;
		
		public MembershipAdapter(CourseMember courseMember) {
			this.courseMember = courseMember;
		}
		
		public String getAuthority() {
			return CourseManagementLegacyAdapter.CM_LEGACY_AUTHORITY;
		}

		public String getRole() {
			return courseMember.getRole();
		}

		public String getStatus() {
			return "";
		}

		public String getUserId() {
			return courseMember.getUniqname();
		}

		public void setStatus(String status) {
			log.warn("Can not call setters on MembershipAdapter");
		}
		
	}
}
