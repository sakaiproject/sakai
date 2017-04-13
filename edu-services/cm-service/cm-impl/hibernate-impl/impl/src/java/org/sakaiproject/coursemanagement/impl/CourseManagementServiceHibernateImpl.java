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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.Query;
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
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides access to course and enrollment data stored in sakai's local hibernate tables.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class CourseManagementServiceHibernateImpl extends HibernateDaoSupport implements CourseManagementService {

	public void init() {
		log.info("Initializing " + getClass().getName());
	}

	public void destroy() {
		log.info("Destroying " + getClass().getName());
	}
	
	/**
	 * A generic approach to finding objects by their eid.  This is "coding by convention",
	 * since it expects the parameterized query to use "eid" as the single named parameter.
	 * 
	 * @param eid The eid of the object we're trying to load
	 * @param className The name of the class / interface we're looking for
	 * @return The object, if found
	 * @throws IdNotFoundException
	 */
	private Object getObjectByEid(final String eid, final String className) throws IdNotFoundException {
		HibernateCallback hc = session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(className).append(" as obj where obj.eid=:eid");
            Query q = session.createQuery(hql.toString());
            q.setParameter("eid", eid);
            Object result = q.uniqueResult();
            if(result == null) {
                throw new IdNotFoundException(eid, className);
            }
            return result;
        };
		return getHibernateTemplate().execute(hc);
	}
	
	public CourseSet getCourseSet(String eid) throws IdNotFoundException {
		return (CourseSet)getObjectByEid(eid, CourseSetCmImpl.class.getName());
	}

	public Set<CourseSet> getChildCourseSets(final String parentCourseSetEid) throws IdNotFoundException {
		// Ensure that the parent exists
		if(!isCourseSetDefined(parentCourseSetEid)) {
			throw new IdNotFoundException(parentCourseSetEid, CourseSetCmImpl.class.getName());
		}
		return new HashSet<CourseSet>((List<CourseSet>) getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findChildCourseSets", "parentEid", parentCourseSetEid));
	}

	public Set<CourseSet> getCourseSets() {
		return new HashSet<CourseSet>((List<CourseSet>) getHibernateTemplate().findByNamedQuery("findTopLevelCourseSets"));
	}

	public Set<Membership> getCourseSetMemberships(String courseSetEid) throws IdNotFoundException {
		return getMemberships((AbstractMembershipContainerCmImpl)getCourseSet(courseSetEid));
	}

	public CanonicalCourse getCanonicalCourse(String eid) throws IdNotFoundException {
		return (CanonicalCourse)getObjectByEid(eid, CanonicalCourseCmImpl.class.getName());
	}

	public Set<CanonicalCourse> getEquivalentCanonicalCourses(String canonicalCourseEid) {
		final CanonicalCourseCmImpl canonicalCourse = (CanonicalCourseCmImpl)getCanonicalCourse(canonicalCourseEid);
		HibernateCallback<List<CanonicalCourse>> hc = session -> {
            Query q = session.getNamedQuery("findEquivalentCanonicalCourses");
            q.setParameter("crossListing", canonicalCourse.getCrossListing());
            q.setParameter("canonicalCourse", canonicalCourse);
            return q.list();
        };
		return new HashSet<>(getHibernateTemplate().execute(hc));
	}

	public Set<CanonicalCourse> getCanonicalCourses(final String courseSetEid) throws IdNotFoundException {
		return ((CourseSetCmImpl)getCourseSet(courseSetEid)).getCanonicalCourses();
	}

	public List <AcademicSession> getAcademicSessions() {
	    return getHibernateTemplate().execute((HibernateCallback<List<AcademicSession>>) session -> {
            Query query = session.getNamedQuery("findAcademicSessions");
            query.setCacheable(true);
            return query.list();
        });

	}

	public List <AcademicSession> getCurrentAcademicSessions() {
	    return getHibernateTemplate().execute((HibernateCallback<List<AcademicSession>>) session -> {
            Query query = session.getNamedQuery("findCurrentAcademicSessions");
            query.setCacheable(true);
            return query.list();
        });
	}

	public AcademicSession getAcademicSession(final String eid) throws IdNotFoundException {
		return (AcademicSession)getObjectByEid(eid, AcademicSessionCmImpl.class.getName());
	}
	
	public CourseOffering getCourseOffering(String eid) throws IdNotFoundException {
		return (CourseOffering)getObjectByEid(eid, CourseOfferingCmImpl.class.getName());
	}

	public Set<CourseOffering> getCourseOfferingsInCourseSet(final String courseSetEid) throws IdNotFoundException {
		if( ! isCourseSetDefined(courseSetEid)) {
			throw new IdNotFoundException(courseSetEid, CourseOfferingCmImpl.class.getName());
		}
		return ((CourseSetCmImpl)getCourseSet(courseSetEid)).getCourseOfferings();
	}

	public Set<CourseOffering> getEquivalentCourseOfferings(String courseOfferingEid) throws IdNotFoundException {
		final CourseOfferingCmImpl courseOffering = (CourseOfferingCmImpl)getCourseOffering(courseOfferingEid);
		HibernateCallback<List<CourseOffering>> hc = session -> {
            Query q = session.getNamedQuery("findEquivalentCourseOfferings");
            q.setParameter("crossListing", courseOffering.getCrossListing());
            q.setParameter("courseOffering", courseOffering);
            return q.list();
        };
		return new HashSet<>(getHibernateTemplate().execute(hc));
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
		
		// This may be a dynamic proxy.  In that case, make sure we're using the class
		// that hibernate understands.
		final String className = Hibernate.getClass(container).getName();
		
		HibernateCallback<List<Membership>> hc = session -> {
            StringBuilder sb = new StringBuilder("select mbr from MembershipCmImpl as mbr, ");
                sb.append(className);
                sb.append(" as container where mbr.memberContainer=container ");
                sb.append("and container.eid=:eid");
            Query q = session.createQuery(sb.toString());
            q.setParameter("eid", container.getEid());
            return q.list();
        };
		return new HashSet<>(getHibernateTemplate().execute(hc));
	}

	public Section getSection(String eid) throws IdNotFoundException {
		return (Section)getObjectByEid(eid, SectionCmImpl.class.getName());
	}

	public Set<Section> getSections(String courseOfferingEid) throws IdNotFoundException {
		CourseOffering courseOffering = getCourseOffering(courseOfferingEid);
		return new HashSet<Section>((List<Section>) getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findTopLevelSectionsInCourseOffering", "courseOffering", courseOffering));
	}

	public Set<Section> getChildSections(final String parentSectionEid) throws IdNotFoundException {
		if( ! isSectionDefined(parentSectionEid)) {
			throw new IdNotFoundException(parentSectionEid, SectionCmImpl.class.getName());
		}
		return new HashSet<Section>((List<Section>) getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findChildSections", "parentEid", parentSectionEid));
	}

	public Set<Membership> getSectionMemberships(String sectionEid) throws IdNotFoundException {
		return getMemberships((AbstractMembershipContainerCmImpl)getSection(sectionEid));
	}

	public EnrollmentSet getEnrollmentSet(String eid) throws IdNotFoundException {
		return (EnrollmentSet)getObjectByEid(eid, EnrollmentSetCmImpl.class.getName());
	}

	public Set<EnrollmentSet> getEnrollmentSets(final String courseOfferingEid) throws IdNotFoundException {
		if(! isCourseOfferingDefined(courseOfferingEid)) {
			throw new IdNotFoundException(courseOfferingEid, CourseOfferingCmImpl.class.getName());
		}
		return new HashSet<EnrollmentSet>((List<EnrollmentSet>) getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findEnrollmentSetsByCourseOffering", "courseOfferingEid", courseOfferingEid));
	}

	public Set<Enrollment> getEnrollments(final String enrollmentSetEid) throws IdNotFoundException {
		if( ! isEnrollmentSetDefined(enrollmentSetEid)) {
			throw new IdNotFoundException(enrollmentSetEid, EnrollmentSetCmImpl.class.getName());
		}
		return new HashSet<Enrollment>((List<Enrollment>) getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findEnrollments", "enrollmentSetEid", enrollmentSetEid));
	}

	public boolean isEnrolled(final String userId, final Set<String> enrollmentSetEids) {
		HibernateCallback hc = session -> {
            Query q = session.getNamedQuery("countEnrollments");
            q.setParameter("userId", userId);
            q.setParameterList("enrollmentSetEids", enrollmentSetEids);
            return q.iterate().next();
        };
		int i = ((Number)getHibernateTemplate().execute(hc)).intValue();
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
		HibernateCallback hc = session -> {
            Query q = session.getNamedQuery("findEnrollment");
            q.setParameter("userId", userId);
            q.setParameter("enrollmentSetEid", enrollmentSetEid);
            return q.uniqueResult();
        };
		return (Enrollment)getHibernateTemplate().execute(hc);
	}
	
	public Set<String> getInstructorsOfRecordIds(String enrollmentSetEid) throws IdNotFoundException {
		EnrollmentSet es = getEnrollmentSet(enrollmentSetEid);
		return es.getOfficialInstructors();
	}


	public Set<EnrollmentSet> findCurrentlyEnrolledEnrollmentSets(final String userId) {
		return new HashSet<EnrollmentSet>((List<EnrollmentSet>) getHibernateTemplate().findByNamedQueryAndNamedParam("findCurrentlyEnrolledEnrollmentSets", "userId", userId));
	}


	public Set<EnrollmentSet> findCurrentlyInstructingEnrollmentSets(final String userId) {
		return new HashSet<EnrollmentSet>((List<EnrollmentSet>) getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findCurrentlyInstructingEnrollmentSets", "userId", userId));
	}

	public Set<Section> findInstructingSections(final String userId) {
		return new HashSet<Section>((List<Section>) getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findInstructingSections", "userId", userId));
	}

	public Set<Section> findEnrolledSections(final String userId) {
		return new HashSet<Section>((List<Section>) getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findEnrolledSections", "userId", userId));
	}

	
	
	public List<CourseOffering> findActiveCourseOfferingsInCanonicalCourse(
			String eid) {
		log.debug("findActiveCourseOfferingsInCanonicalCourse(eid");
		/**
		 * select * from CM_MEMBER_CONTAINER_T where start_date <= now() and end_date>=now() and class_discr='org.sakaiproject.coursemanagement.impl.CourseOfferingCmImpl' and canonical_course in (select MEMBER_CONTAINER_ID from CM_MEMBER_CONTAINER_T where enterprise_id= ? and CLASS_DISCR='org.sakaiproject.coursemanagement.impl.CanonicalCourseCmImpl');
		 */
		CanonicalCourse canonicalCourse = null;
		try {
			canonicalCourse = this.getCanonicalCourse(eid);
		}
		catch (IdNotFoundException e) {
			//its quite possible someone ask for a course that doesn't exits
			return new ArrayList<CourseOffering>();
		}
		
		List<CourseOffering> ret = new ArrayList<CourseOffering>((List<CourseOffering>) getHibernateTemplate().findByNamedQueryAndNamedParam("findActiveCourseOfferingsInCanonicalCourse", 
				"canonicalCourse", canonicalCourse));
		
		return ret;
	}
	
	
	public Set<Section> findInstructingSections(final String userId, final String academicSessionEid) {
		HibernateCallback<List<Section>> hc = session -> {
            Query q = session.getNamedQuery("findInstructingSectionsByAcademicSession");
            q.setParameter("userId", userId);
            q.setParameter("academicSessionEid", academicSessionEid);
            return q.list();
        };
		return new HashSet<>(getHibernateTemplate().execute(hc));
	}

	public Set<CourseOffering> findCourseOfferings(final String courseSetEid, final String academicSessionEid) throws IdNotFoundException {
		HibernateCallback<List<CourseOffering>> hc = session -> {
            Query q = session.getNamedQuery("findCourseOfferingsByCourseSetAndAcademicSession");
            q.setParameter("courseSetEid", courseSetEid);
            q.setParameter("academicSessionEid", academicSessionEid);
            return q.list();
        };
		return new HashSet<>(getHibernateTemplate().execute(hc));
	}

	public boolean isEmpty(final String courseSetEid) {
		HibernateCallback hc = session -> {
            Query q = session.getNamedQuery("findNonEmptyCourseSet");
            q.setParameter("eid", courseSetEid);
            return Boolean.valueOf( ! q.iterate().hasNext());
        };
		return ((Boolean)getHibernateTemplate().execute(hc)).booleanValue();
	}


	public List<CourseSet> findCourseSets(final String category) {
		return (List<CourseSet>) getHibernateTemplate().findByNamedQueryAndNamedParam("findCourseSetByCategory", "category", category);
	}


	public Map<String, String> findCourseOfferingRoles(final String userEid) {
		// Keep track of CourseOfferings that we've already queried
		Set<String> queriedCourseOfferingEids = new HashSet<String>();
		List results = getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findCourseOfferingRoles", "userEid", userEid);
		Map<String, String> courseOfferingRoleMap = new HashMap<String, String>();
		for(Iterator iter = results.iterator(); iter.hasNext();) {
			Object[] oa = (Object[])iter.next();
			courseOfferingRoleMap.put((String)oa[0], (String)oa[1]);
			queriedCourseOfferingEids.add((String)oa[0]);
		}
		return courseOfferingRoleMap;
	}

	public Map<String, String> findCourseSetRoles(final String userEid) {
		List results = getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findCourseSetRoles", "userEid", userEid);
		Map<String, String> courseSetRoleMap = new HashMap<String, String>();
		for(Iterator iter = results.iterator(); iter.hasNext();) {
			Object[] oa = (Object[])iter.next();
			courseSetRoleMap.put((String)oa[0], (String)oa[1]);
		}
		return courseSetRoleMap;
	}


	public Map<String, String> findSectionRoles(final String userEid) {
		List results = getHibernateTemplate().findByNamedQueryAndNamedParam(
				"findSectionRoles", "userEid", userEid);
		Map<String, String> sectionRoleMap = new HashMap<String, String>();
		for(Iterator iter = results.iterator(); iter.hasNext();) {
			Object[] oa = (Object[])iter.next();
			sectionRoleMap.put((String)oa[0], (String)oa[1]);
		}
		return sectionRoleMap;
	}


	public Set<CourseOffering> getCourseOfferingsInCanonicalCourse(final String canonicalCourseEid) throws IdNotFoundException {
		if(!isCanonicalCourseDefined(canonicalCourseEid)) {
			throw new IdNotFoundException(canonicalCourseEid, CanonicalCourseCmImpl.class.getName());
		}
		return new HashSet<CourseOffering>((List<CourseOffering>) getHibernateTemplate().findByNamedQueryAndNamedParam("findCourseOfferingsByCanonicalCourse", "canonicalCourseEid", canonicalCourseEid));
	}

	public boolean isAcademicSessionDefined(String eid) {
		return ((Number)getHibernateTemplate().findByNamedQueryAndNamedParam("isAcademicSessionDefined", "eid", eid).get(0)).intValue() == 1;
	}

	public boolean isCanonicalCourseDefined(String eid) {
		return ((Number)getHibernateTemplate().findByNamedQueryAndNamedParam("isCanonicalCourseDefined", "eid", eid).get(0)).intValue() == 1;
	}

	public boolean isCourseOfferingDefined(String eid) {
		return ((Number)getHibernateTemplate().findByNamedQueryAndNamedParam("isCourseOfferingDefined", "eid", eid).get(0)).intValue() == 1;
	}

	public boolean isCourseSetDefined(String eid) {
		return ((Number)getHibernateTemplate().findByNamedQueryAndNamedParam("isCourseSetDefined", "eid", eid).get(0)).intValue() == 1;
	}

	public boolean isEnrollmentSetDefined(String eid) {
		return ((Number)getHibernateTemplate().findByNamedQueryAndNamedParam("isEnrollmentSetDefined", "eid", eid).get(0)).intValue() == 1;
	}

	public boolean isSectionDefined(String eid) {
		return ((Number)getHibernateTemplate().findByNamedQueryAndNamedParam("isSectionDefined", "eid", eid).get(0)).intValue() == 1;
	}

	public List<String> getSectionCategories() {
		return (List<String>) getHibernateTemplate().findByNamedQuery("findSectionCategories");
	}

	public String getSectionCategoryDescription(String categoryCode) {
		if(categoryCode == null) {
			return null;
		}
		SectionCategory cat = (SectionCategory)getHibernateTemplate().get(SectionCategoryCmImpl.class, categoryCode);
		if(cat == null) {
			return null;
		} else {
			return cat.getCategoryDescription();
		}
	}

	public Map<String, String> getEnrollmentStatusDescriptions(Locale locale) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("enrolled", "Enrolled");
		map.put("wait", "Waitlisted");
		return map;
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
