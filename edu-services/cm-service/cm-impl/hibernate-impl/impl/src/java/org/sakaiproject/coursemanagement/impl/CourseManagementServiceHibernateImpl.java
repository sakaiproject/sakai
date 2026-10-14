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
package org.sakaiproject.coursemanagement.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import org.hibernate.query.Query;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.SectionCategory;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides access to course and enrollment data stored in sakai's local hibernate tables.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
@Transactional(
		propagation = Propagation.REQUIRED,
		readOnly = true,
		noRollbackFor = IdNotFoundException.class
)
public class CourseManagementServiceHibernateImpl implements CourseManagementService {

	@Setter private SessionFactory sessionFactory;

	private static final ResourceLoader enrollmentsMessages = new ResourceLoader("enrollmentstatus");

	/**
	 * Finds a mapped object by its eid property.
	 * 
	 * @param eid The eid of the object we're trying to load
	 * @param type The mapped class we're looking for
	 * @return The object, if found
	 * @throws IdNotFoundException
	 */
	private <T> T getObjectByEid(final String eid, final Class<T> type) throws IdNotFoundException {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(type);
		Root<T> root = cq.from(type);
		cq.select(root).where(cb.equal(root.get("eid"), eid));
		T result = sessionFactory.getCurrentSession().createQuery(cq).uniqueResult();
		if (result == null) {
			throw new IdNotFoundException(eid, type.getName());
		}
		return result;
	}

	public CourseSet getCourseSet(String eid) throws IdNotFoundException {
		return (CourseSet)getObjectByEid(eid, CourseSetCmImpl.class);
	}

	public Set<CourseSet> getChildCourseSets(final String parentCourseSetEid) throws IdNotFoundException {
		// Ensure that the parent exists
		if(!isCourseSetDefined(parentCourseSetEid)) {
			throw new IdNotFoundException(parentCourseSetEid, CourseSetCmImpl.class.getName());
		}
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CourseSet> cq = cb.createQuery(CourseSet.class);
		Root<CourseSetCmImpl> root = cq.from(CourseSetCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("parent").get("eid"), parentCourseSetEid));
		Query<CourseSet> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<CourseSet>((List<CourseSet>) q.list());
	}

	public Set<CourseSet> getCourseSets() {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CourseSet> cq = cb.createQuery(CourseSet.class);
		Root<CourseSetCmImpl> root = cq.from(CourseSetCmImpl.class);
		cq.select(root);
		cq.where(root.get("parent").isNull());
		Query<CourseSet> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<CourseSet>((List<CourseSet>) q.list());
	}

	public Set<Membership> getCourseSetMemberships(String courseSetEid) throws IdNotFoundException {
		return getMemberships((AbstractMembershipContainerCmImpl)getCourseSet(courseSetEid));
	}

	public CanonicalCourse getCanonicalCourse(String eid) throws IdNotFoundException {
		return (CanonicalCourse)getObjectByEid(eid, CanonicalCourseCmImpl.class);
	}

	public Set<CanonicalCourse> getEquivalentCanonicalCourses(String canonicalCourseEid) {
		final CanonicalCourseCmImpl canonicalCourse = (CanonicalCourseCmImpl)getCanonicalCourse(canonicalCourseEid);
		if (canonicalCourse.getCrossListing() == null) {
			return new HashSet<>();
		}
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CanonicalCourse> cq = cb.createQuery(CanonicalCourse.class);
		Root<CanonicalCourseCmImpl> root = cq.from(CanonicalCourseCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("crossListing"), canonicalCourse.getCrossListing()), cb.notEqual(root, canonicalCourse));
		Query<CanonicalCourse> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<>(q.list());
	}

	public Set<CanonicalCourse> getCanonicalCourses(final String courseSetEid) throws IdNotFoundException {
		return ((CourseSetCmImpl)getCourseSet(courseSetEid)).getCanonicalCourses();
	}

	public List <AcademicSession> getAcademicSessions() {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<AcademicSession> cq = cb.createQuery(AcademicSession.class);
		Root<AcademicSessionCmImpl> root = cq.from(AcademicSessionCmImpl.class);
		cq.select(root);
		cq.orderBy(cb.desc(root.get("startDate")));
		Query<AcademicSession> q = sessionFactory.getCurrentSession().createQuery(cq);
		return q.setCacheable(true).list();

	}

	public List <AcademicSession> getCurrentAcademicSessions() {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<AcademicSession> cq = cb.createQuery(AcademicSession.class);
		Root<AcademicSessionCmImpl> root = cq.from(AcademicSessionCmImpl.class);
		cq.select(root);
		cq.where(cb.isTrue(root.get("current")));
		cq.orderBy(cb.desc(root.get("startDate")));
		Query<AcademicSession> q = sessionFactory.getCurrentSession().createQuery(cq);
		return q.setCacheable(true).list();
	}

	public AcademicSession getAcademicSession(final String eid) throws IdNotFoundException {
		return (AcademicSession)getObjectByEid(eid, AcademicSessionCmImpl.class);
	}
	
	public CourseOffering getCourseOffering(String eid) throws IdNotFoundException {
		return (CourseOffering)getObjectByEid(eid, CourseOfferingCmImpl.class);
	}

	public Set<CourseOffering> getCourseOfferingsInCourseSet(final String courseSetEid) throws IdNotFoundException {
		if( ! isCourseSetDefined(courseSetEid)) {
			throw new IdNotFoundException(courseSetEid, CourseOfferingCmImpl.class.getName());
		}
		return ((CourseSetCmImpl)getCourseSet(courseSetEid)).getCourseOfferings();
	}

	public Set<CourseOffering> getEquivalentCourseOfferings(String courseOfferingEid) throws IdNotFoundException {
		final CourseOfferingCmImpl courseOffering = (CourseOfferingCmImpl)getCourseOffering(courseOfferingEid);
		if (courseOffering.getCrossListing() == null) {
			return new HashSet<>();
		}
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CourseOffering> cq = cb.createQuery(CourseOffering.class);
		Root<CourseOfferingCmImpl> root = cq.from(CourseOfferingCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("crossListing"), courseOffering.getCrossListing()), cb.notEqual(root, courseOffering));
		Query<CourseOffering> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<>(q.list());
	}

	public Set<Membership> getCourseOfferingMemberships(String courseOfferingEid) throws IdNotFoundException {
		return getMemberships((AbstractMembershipContainerCmImpl)getCourseOffering(courseOfferingEid));
	}

	/**
	 * Gets the memberships for a membership container.  This query can not be
	 * performed using just the container's eid, since it may conflict with other kinds
	 * of objects with the same eid.
	 * 
	 * @param container
	 * @return
	 */
	private Set<Membership> getMemberships(final AbstractMembershipContainerCmImpl container) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Membership> cq = cb.createQuery(Membership.class);
		Root<MembershipCmImpl> root = cq.from(MembershipCmImpl.class);
		cq.select(root).where(cb.equal(root.get("memberContainer"), container));
		return new HashSet<>(sessionFactory.getCurrentSession().createQuery(cq).getResultList());
	}

	public Section getSection(String eid) throws IdNotFoundException {
		return (Section)getObjectByEid(eid, SectionCmImpl.class);
	}

	public Set<Section> getSections(String courseOfferingEid) throws IdNotFoundException {
		CourseOffering courseOffering = getCourseOffering(courseOfferingEid);
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Section> cq = cb.createQuery(Section.class);
		Root<SectionCmImpl> root = cq.from(SectionCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("courseOffering"), courseOffering), root.get("parent").isNull());
		Query<Section> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<Section>((List<Section>) q.list());
	}

	public Set<Section> getChildSections(final String parentSectionEid) throws IdNotFoundException {
		if( ! isSectionDefined(parentSectionEid)) {
			throw new IdNotFoundException(parentSectionEid, SectionCmImpl.class.getName());
		}
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Section> cq = cb.createQuery(Section.class);
		Root<SectionCmImpl> root = cq.from(SectionCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("parent").get("eid"), parentSectionEid));
		Query<Section> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<Section>((List<Section>) q.list());
	}

	public Set<Membership> getSectionMemberships(String sectionEid) throws IdNotFoundException {
		return getMemberships((AbstractMembershipContainerCmImpl)getSection(sectionEid));
	}

	public EnrollmentSet getEnrollmentSet(String eid) throws IdNotFoundException {
		return (EnrollmentSet)getObjectByEid(eid, EnrollmentSetCmImpl.class);
	}

	public Set<EnrollmentSet> getEnrollmentSets(final String courseOfferingEid) throws IdNotFoundException {
		if(! isCourseOfferingDefined(courseOfferingEid)) {
			throw new IdNotFoundException(courseOfferingEid, CourseOfferingCmImpl.class.getName());
		}
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<EnrollmentSet> cq = cb.createQuery(EnrollmentSet.class);
		Root<EnrollmentSetCmImpl> root = cq.from(EnrollmentSetCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("courseOffering").get("eid"), courseOfferingEid));
		Query<EnrollmentSet> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<EnrollmentSet>((List<EnrollmentSet>) q.list());
	}

	public Set<Enrollment> getEnrollments(final String enrollmentSetEid) throws IdNotFoundException {
		if( ! isEnrollmentSetDefined(enrollmentSetEid)) {
			throw new IdNotFoundException(enrollmentSetEid, EnrollmentSetCmImpl.class.getName());
		}
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
		Root<EnrollmentCmImpl> root = cq.from(EnrollmentCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("enrollmentSet").get("eid"), enrollmentSetEid));
		Query<Enrollment> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<Enrollment>((List<Enrollment>) q.list());
	}

	public boolean isEnrolled(final String userId, final Set<String> enrollmentSetEids) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<EnrollmentCmImpl> root = cq.from(EnrollmentCmImpl.class);
		cq.select(cb.count(root));
		cq.where(
			cb.equal(root.get("userId"), userId),
			root.get("enrollmentSet").get("eid").in(enrollmentSetEids),
			cb.isFalse(root.get("dropped")));
		Query<Long> q = sessionFactory.getCurrentSession().createQuery(cq);
		int i = ((Number)q.uniqueResult()).intValue();
		if(log.isDebugEnabled()) log.debug(userId + " is enrolled in " + i + " of these " + enrollmentSetEids.size() + " EnrollmentSets" );
		return i > 0;
	}

	public boolean isEnrolled(String userId, String enrollmentSetEid) {
		HashSet<String> enrollmentSetEids = new HashSet<String>();
		enrollmentSetEids.add(enrollmentSetEid);
		return isEnrolled(userId, enrollmentSetEids);
	}
	
	public Enrollment findEnrollment(final String userId, final String enrollmentSetEid) {
		if( ! isEnrollmentSetDefined(enrollmentSetEid)) {
			log.warn("Could not find an enrollment set with eid=" + enrollmentSetEid);
			return null;
		}
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
		Root<EnrollmentCmImpl> root = cq.from(EnrollmentCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("enrollmentSet").get("eid"), enrollmentSetEid), cb.equal(root.get("userId"), userId));
		Query<Enrollment> q = sessionFactory.getCurrentSession().createQuery(cq);
		return (Enrollment)q.uniqueResult();
	}
	
	public Set<String> getInstructorsOfRecordIds(String enrollmentSetEid) throws IdNotFoundException {
		EnrollmentSet es = getEnrollmentSet(enrollmentSetEid);
		return es.getOfficialInstructors();
	}


	public Set<EnrollmentSet> findCurrentlyEnrolledEnrollmentSets(final String userId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<EnrollmentSet> cq = cb.createQuery(EnrollmentSet.class);
		Root<EnrollmentCmImpl> root = cq.from(EnrollmentCmImpl.class);
		cq.select(root.get("enrollmentSet"));
		cq.where(
			cb.equal(root.get("userId"), userId),
			cb.isFalse(root.get("dropped")),
			cb.or(root.get("enrollmentSet").get("courseOffering").get("startDate").isNull(), cb.lessThanOrEqualTo(root.get("enrollmentSet").get("courseOffering").get("startDate"), cb.currentDate())),
			cb.or(root.get("enrollmentSet").get("courseOffering").get("endDate").isNull(), cb.greaterThanOrEqualTo(root.get("enrollmentSet").get("courseOffering").get("endDate"), cb.currentDate())));
		Query<EnrollmentSet> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<EnrollmentSet>((List<EnrollmentSet>) q.list());
	}


	public Set<EnrollmentSet> findCurrentlyInstructingEnrollmentSets(final String userId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<EnrollmentSet> cq = cb.createQuery(EnrollmentSet.class);
		Root<EnrollmentSetCmImpl> root = cq.from(EnrollmentSetCmImpl.class);
		cq.select(root);
		cq.where(
			cb.isMember(userId, root.get("officialInstructors")),
			cb.or(root.get("courseOffering").get("startDate").isNull(), cb.lessThanOrEqualTo(root.get("courseOffering").get("startDate"), cb.currentDate())),
			cb.or(root.get("courseOffering").get("endDate").isNull(), cb.greaterThanOrEqualTo(root.get("courseOffering").get("endDate"), cb.currentDate())));
		Query<EnrollmentSet> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<EnrollmentSet>((List<EnrollmentSet>) q.list());
	}

	public Set<Section> findInstructingSections(final String userId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Section> cq = cb.createQuery(Section.class);
		Root<SectionCmImpl> root = cq.from(SectionCmImpl.class);
		cq.select(root);
		cq.where(cb.isMember(userId, root.get("enrollmentSet").get("officialInstructors")));
		Query<Section> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<Section>((List<Section>) q.list());
	}

	public Set<Section> findEnrolledSections(final String userId) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Section> cq = cb.createQuery(Section.class);
		Root<SectionCmImpl> root = cq.from(SectionCmImpl.class);
		Root<EnrollmentCmImpl> enrollment = cq.from(EnrollmentCmImpl.class);
		cq.select(root);
		cq.where(
			cb.equal(enrollment.get("userId"), userId),
			cb.equal(root.get("enrollmentSet"), enrollment.get("enrollmentSet")),
			cb.isFalse(enrollment.get("dropped")));
		Query<Section> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<Section>((List<Section>) q.list());
	}

	
	
	public List<CourseOffering> findActiveCourseOfferingsInCanonicalCourse(
			String eid) {
		log.debug("findActiveCourseOfferingsInCanonicalCourse(eid");
		/**
		 * select * from CM_MEMBER_CONTAINER_T where start_date <= now() and end_date>=now() and class_discr='org.sakaiproject.coursemanagement.impl.CourseOfferingCmImpl' and canonical_course in (select MEMBER_CONTAINER_ID from CM_MEMBER_CONTAINER_T where enterprise_id= ? and CLASS_DISCR='org.sakaiproject.coursemanagement.impl.CanonicalCourseCmImpl');
		 */
		final CanonicalCourse canonicalCourse;
		try {
			canonicalCourse = this.getCanonicalCourse(eid);
		}
		catch (IdNotFoundException e) {
			//its quite possible someone ask for a course that doesn't exits
			return new ArrayList<CourseOffering>();
		}

		Date now = new Date();
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CourseOffering> cq = cb.createQuery(CourseOffering.class);
		Root<CourseOfferingCmImpl> root = cq.from(CourseOfferingCmImpl.class);
		cq.select(root);
		cq.where(
			cb.lessThanOrEqualTo(root.get("startDate"), now),
			cb.greaterThanOrEqualTo(root.get("endDate"), now),
			cb.equal(root.get("canonicalCourse"), canonicalCourse));
		Query<CourseOffering> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new ArrayList<>(q.list());
	}
	
	
	public Set<Section> findInstructingSections(final String userId, final String academicSessionEid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Section> cq = cb.createQuery(Section.class);
		Root<SectionCmImpl> root = cq.from(SectionCmImpl.class);
		cq.select(root);
		cq.where(
			cb.isMember(userId, root.get("enrollmentSet").get("officialInstructors")),
			cb.equal(root.get("courseOffering").get("academicSession").get("eid"), academicSessionEid));
		Query<Section> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<>(q.list());
	}

	public Set<CourseOffering> findCourseOfferings(final String courseSetEid, final String academicSessionEid) throws IdNotFoundException {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CourseOffering> cq = cb.createQuery(CourseOffering.class);
		Root<CourseOfferingCmImpl> root = cq.from(CourseOfferingCmImpl.class);
		cq.select(root);
		cq.where(
			cb.equal(root.get("academicSession").get("eid"), academicSessionEid),
			cb.equal(root.join("courseSets").get("eid"), courseSetEid));
		Query<CourseOffering> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<>(q.list());
	}

	public boolean isEmpty(final String courseSetEid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CourseSet> cq = cb.createQuery(CourseSet.class);
		Root<CourseSetCmImpl> root = cq.from(CourseSetCmImpl.class);
		cq.select(root);
		cq.where(
			cb.equal(root.get("eid"), courseSetEid),
			cb.or(cb.isNotEmpty(root.get("canonicalCourses")), cb.isNotEmpty(root.get("courseOfferings"))));
		Query<CourseSet> q = sessionFactory.getCurrentSession().createQuery(cq);
		return ((Boolean)q.list().isEmpty()).booleanValue();
	}


	public List<CourseSet> findCourseSets(final String category) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CourseSet> cq = cb.createQuery(CourseSet.class);
		Root<CourseSetCmImpl> root = cq.from(CourseSetCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("category"), category));
		cq.orderBy(cb.asc(root.get("title")));
		Query<CourseSet> q = sessionFactory.getCurrentSession().createQuery(cq);
		return (List<CourseSet>) q.list();
	}


	public Map<String, String> findCourseOfferingRoles(final String userEid) {
		// Keep track of CourseOfferings that we've already queried
		Set<String> queriedCourseOfferingEids = new HashSet<String>();
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<CourseOfferingCmImpl> container = cq.from(CourseOfferingCmImpl.class);
		Root<MembershipCmImpl> membership = cq.from(MembershipCmImpl.class);
		cq.multiselect(container.get("eid"), membership.get("role"));
		cq.where(cb.equal(membership.get("userId"), userEid), cb.equal(membership.get("memberContainer"), container));
		Query<Object[]> q = sessionFactory.getCurrentSession().createQuery(cq);
		List results = q.list();
		Map<String, String> courseOfferingRoleMap = new HashMap<String, String>();
		for(Iterator iter = results.iterator(); iter.hasNext();) {
			Object[] oa = (Object[])iter.next();
			courseOfferingRoleMap.put((String)oa[0], (String)oa[1]);
			queriedCourseOfferingEids.add((String)oa[0]);
		}
		return courseOfferingRoleMap;
	}

	public Map<String, String> findCourseSetRoles(final String userEid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<CourseSetCmImpl> container = cq.from(CourseSetCmImpl.class);
		Root<MembershipCmImpl> membership = cq.from(MembershipCmImpl.class);
		cq.multiselect(container.get("eid"), membership.get("role"));
		cq.where(cb.equal(membership.get("userId"), userEid), cb.equal(membership.get("memberContainer"), container));
		Query<Object[]> q = sessionFactory.getCurrentSession().createQuery(cq);
		List results = q.list();
		Map<String, String> courseSetRoleMap = new HashMap<String, String>();
		for(Iterator iter = results.iterator(); iter.hasNext();) {
			Object[] oa = (Object[])iter.next();
			courseSetRoleMap.put((String)oa[0], (String)oa[1]);
		}
		return courseSetRoleMap;
	}


	public Map<String, String> findSectionRoles(final String userEid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<SectionCmImpl> container = cq.from(SectionCmImpl.class);
		Root<MembershipCmImpl> membership = cq.from(MembershipCmImpl.class);
		cq.multiselect(container.get("eid"), membership.get("role"));
		cq.where(cb.equal(membership.get("userId"), userEid), cb.equal(membership.get("memberContainer"), container));
		Query<Object[]> q = sessionFactory.getCurrentSession().createQuery(cq);
		List results = q.list();
		Map<String, String> sectionRoleMap = new HashMap<String, String>();
		for(Iterator iter = results.iterator(); iter.hasNext();) {
			Object[] oa = (Object[])iter.next();
			sectionRoleMap.put((String)oa[0], (String)oa[1]);
		}
		return sectionRoleMap;
	}

	public Map<String, String> findSectionRoles(final String userEid, final String academicSessionEid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<SectionCmImpl> container = cq.from(SectionCmImpl.class);
		Root<MembershipCmImpl> membership = cq.from(MembershipCmImpl.class);
		cq.multiselect(container.get("eid"), membership.get("role"));
		cq.where(
			cb.equal(membership.get("userId"), userEid),
			cb.equal(membership.get("memberContainer"), container),
			cb.equal(container.get("courseOffering").get("academicSession").get("eid"), academicSessionEid));
		Query<Object[]> q = sessionFactory.getCurrentSession().createQuery(cq);
		List<Object[]> results = new ArrayList<>((List<Object[]>) q.list());
		Map<String, String> sectionRoleMap = new HashMap<>();
		for(Object[] oa : results) {
			sectionRoleMap.put((String) oa[0], (String) oa[1]);
		}

		return sectionRoleMap;
	}

	public Set<CourseOffering> getCourseOfferingsInCanonicalCourse(final String canonicalCourseEid) throws IdNotFoundException {
		if(!isCanonicalCourseDefined(canonicalCourseEid)) {
			throw new IdNotFoundException(canonicalCourseEid, CanonicalCourseCmImpl.class.getName());
		}
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CourseOffering> cq = cb.createQuery(CourseOffering.class);
		Root<CourseOfferingCmImpl> root = cq.from(CourseOfferingCmImpl.class);
		cq.select(root);
		cq.where(cb.equal(root.get("canonicalCourse").get("eid"), canonicalCourseEid));
		Query<CourseOffering> q = sessionFactory.getCurrentSession().createQuery(cq);
		return new HashSet<CourseOffering>((List<CourseOffering>) q.list());
	}

	public boolean isAcademicSessionDefined(String eid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<AcademicSessionCmImpl> root = cq.from(AcademicSessionCmImpl.class);
		cq.select(cb.count(root));
		cq.where(cb.equal(root.get("eid"), eid));
		Query<Long> q = sessionFactory.getCurrentSession().createQuery(cq);
		return ((Number)q.list().get(0)).intValue() == 1;
	}

	public boolean isCanonicalCourseDefined(String eid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<CanonicalCourseCmImpl> root = cq.from(CanonicalCourseCmImpl.class);
		cq.select(cb.count(root));
		cq.where(cb.equal(root.get("eid"), eid));
		Query<Long> q = sessionFactory.getCurrentSession().createQuery(cq);
		return ((Number)q.list().get(0)).intValue() == 1;
	}

	public boolean isCourseOfferingDefined(String eid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<CourseOfferingCmImpl> root = cq.from(CourseOfferingCmImpl.class);
		cq.select(cb.count(root));
		cq.where(cb.equal(root.get("eid"), eid));
		Query<Long> q = sessionFactory.getCurrentSession().createQuery(cq);
		return ((Number)q.list().get(0)).intValue() == 1;
	}

	public boolean isCourseSetDefined(String eid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<CourseSetCmImpl> root = cq.from(CourseSetCmImpl.class);
		cq.select(cb.count(root));
		cq.where(cb.equal(root.get("eid"), eid));
		Query<Long> q = sessionFactory.getCurrentSession().createQuery(cq);
		return ((Number)q.list().get(0)).intValue() == 1;
	}

	public boolean isEnrollmentSetDefined(String eid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<EnrollmentSetCmImpl> root = cq.from(EnrollmentSetCmImpl.class);
		cq.select(cb.count(root));
		cq.where(cb.equal(root.get("eid"), eid));
		Query<Long> q = sessionFactory.getCurrentSession().createQuery(cq);
		return ((Number)q.list().get(0)).intValue() == 1;
	}

	public boolean isSectionDefined(String eid) {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<SectionCmImpl> root = cq.from(SectionCmImpl.class);
		cq.select(cb.count(root));
		cq.where(cb.equal(root.get("eid"), eid));
		Query<Long> q = sessionFactory.getCurrentSession().createQuery(cq);
		return ((Number)q.list().get(0)).intValue() == 1;
	}

	public List<String> getSectionCategories() {
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<SectionCategoryCmImpl> root = cq.from(SectionCategoryCmImpl.class);
		cq.select(root.get("categoryCode"));
		cq.orderBy(cb.asc(root.get("categoryCode")));
		Query<String> q = sessionFactory.getCurrentSession().createQuery(cq);
		return (List<String>) q.list();
	}

	public String getSectionCategoryDescription(String categoryCode) {
		if(categoryCode == null) {
			return null;
		}
		SectionCategory cat = (SectionCategory)sessionFactory.getCurrentSession().get(SectionCategoryCmImpl.class, categoryCode);
		if(cat == null) {
			return null;
		} else {
			return cat.getCategoryDescription();
		}
	}

	public String getEnrollmentStatusDescription(String statusId) {
		return enrollmentsMessages.getString(statusId, statusId);
	}

	public Map<String, String> getEnrollmentStatusDescriptions(Locale locale) {
		enrollmentsMessages.setContextLocale(locale);
		return ((Set<Map.Entry>) enrollmentsMessages.entrySet()).stream()
				.collect(Collectors.toMap(
						entry -> String.valueOf(entry.getKey()),
						entry -> String.valueOf(entry.getValue()),
						(a, b) -> b));
	}

	public Map<String, String> getGradingSchemeDescriptions(Locale locale) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("standard", "Letter Grades");
		map.put("pnp", "Pass / Not Pass");
		return map;
	}

	public Map<String, String> getMembershipStatusDescriptions(Locale locale) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("member", "Member");
		map.put("guest", "Guest");
		return map;
	}



}
