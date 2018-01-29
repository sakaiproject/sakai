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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;

/**
 * <p>
 * Federates multiple CourseManagementService implementations.  Each individual implementation
 * must follow the following pattern to participate in federation:
 * </p>
 * 
 * <p>
 * If the implementation doesn't have any information about a particular method and would
 * like to defer to other impls in the chain, it should:
 * 
 * <ul>
 * 	<li>Throw an IdNotFoundException if the return type is an object and the method throws this exception</li>
 * 	<li>Return null if the return type is an object and the method does not throw IdNotFoundException</li>
 * 	<li>Throw UnsupportedOperationException if the return type is a primitive</li>
 * </ul>
 * 
 * Please ensure that your implementation is internally consistent.  If you implement
 * getEnrollments(String enrollmentSetEid), for instance, you should also implement
 * the isEnrolled() methods.  If you implement one but not the other, the data
 * provided by the CourseManagementService will be dependent on <i>how</i>
 * the client calls the API, rather than getting a consistent picture no matter which
 * methods are called.
 * </p>
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class CourseManagementServiceFederatedImpl implements
		CourseManagementService {

	private List<CourseManagementService> implList;
	
	/**
	 * Sets the list of implementations to consult.  Implementations earlier in the list will override later ones.
	 * @param implList
	 */
	public void setImplList(List<CourseManagementService> implList) {
		this.implList = implList;
	}
	
	public Set<CourseOffering> findCourseOfferings(String courseSetEid,
			String academicSessionEid) throws IdNotFoundException {
		Set<CourseOffering> resultSet = new HashSet<CourseOffering>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<CourseOffering> set = null;
			try {
				set = cm.findCourseOfferings(courseSetEid, academicSessionEid);
				if(set != null) {
					resultSet.addAll(set);
				}
			} catch (IdNotFoundException ide) {
				if(log.isDebugEnabled()) log.debug(cm + " could not find course set " + courseSetEid);
				exceptions++;
				continue;
			}
		}
		if(exceptions == implList.size()) {
			throw new IdNotFoundException("Could not find a CM impl with knowledge of academic session " +
					academicSessionEid + " and course set " + courseSetEid);
		}
		return resultSet;
	}

	public List<CourseSet> findCourseSets(String category) {
		List<CourseSet> resultSet = new ArrayList<CourseSet>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			List<CourseSet> list = cm.findCourseSets(category);
			if(list != null) {
				resultSet.addAll(list);
			}
		}
		
		// The federated list should be sorted by title.
		Collections.sort(resultSet, new Comparator<CourseSet>() {
			public int compare(CourseSet cs1, CourseSet cs2) {
				return cs1.getTitle().compareTo(cs2.getTitle());
			}
		});
		return resultSet;
	}

	public Set<EnrollmentSet> findCurrentlyEnrolledEnrollmentSets(String userId) {
		Set<EnrollmentSet> resultSet = new HashSet<EnrollmentSet>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<EnrollmentSet> set = cm.findCurrentlyEnrolledEnrollmentSets(userId);
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		return resultSet;
	}

	public Set<EnrollmentSet> findCurrentlyInstructingEnrollmentSets(String userId) {
		Set<EnrollmentSet> resultSet = new HashSet<EnrollmentSet>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<EnrollmentSet> set = cm.findCurrentlyInstructingEnrollmentSets(userId);
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		return resultSet;
	}

	public Enrollment findEnrollment(String userId, String enrollmentSetEid) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Enrollment enr = cm.findEnrollment(userId, enrollmentSetEid);
			if(enr != null) {
				return enr;
			}
		}
		return null;
	}

	public Set<Section> findInstructingSections(String userId) {
		Set<Section> resultSet = new HashSet<Section>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<Section> set = cm.findInstructingSections(userId);
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		return resultSet;
	}

	public Set<Section> findInstructingSections(String userId, String academicSessionEid) throws IdNotFoundException {
		Set<Section> resultSet = new HashSet<Section>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<Section> set = null;
			try {
				set = cm.findInstructingSections(userId, academicSessionEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not find academic session " + academicSessionEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the academic session, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(academicSessionEid, AcademicSession.class.getName());
		}
		return resultSet;
	}

	public AcademicSession getAcademicSession(String eid) throws IdNotFoundException {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				return cm.getAcademicSession(eid);
			} catch (IdNotFoundException ide) {
				if(log.isDebugEnabled()) log.debug(cm + " could not locate academic session " + eid);
			}
		}
		throw new IdNotFoundException(eid, AcademicSession.class.getName());
	}

	public List<AcademicSession> getAcademicSessions() {
		List<AcademicSession> resultSet = new ArrayList<AcademicSession>();
		
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			List<AcademicSession> list = cm.getAcademicSessions();
			if(list != null) {
				resultSet.addAll(list);
			}
		}
		// The federated list uses the sort provided by the db
		return resultSet;
	}

	public CanonicalCourse getCanonicalCourse(String canonicalCourseEid) throws IdNotFoundException {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				return cm.getCanonicalCourse(canonicalCourseEid);
			} catch (IdNotFoundException ide) {
				if(log.isDebugEnabled()) log.debug(cm + " could not locate canonical course " + canonicalCourseEid);
			}
		}
		throw new IdNotFoundException(canonicalCourseEid, CanonicalCourse.class.getName());
	}

	public Set<CanonicalCourse> getCanonicalCourses(String courseSetEid) throws IdNotFoundException {
		Set<CanonicalCourse> resultSet = new HashSet<CanonicalCourse>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<CanonicalCourse> set = null;
			try {
				set = cm.getCanonicalCourses(courseSetEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not find course set " + courseSetEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the course set, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(courseSetEid, CourseSet.class.getName());
		}
		return resultSet;
	}

	public Set<CourseSet> getChildCourseSets(String parentCourseSetEid) throws IdNotFoundException {
		Set<CourseSet> resultSet = new HashSet<CourseSet>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<CourseSet> set = null;
			try {
				set = cm.getChildCourseSets(parentCourseSetEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate parent course set " + parentCourseSetEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the course set, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(parentCourseSetEid, CourseSet.class.getName());
		}
		return resultSet;
	}

	public Set<Section> getChildSections(String parentSectionEid) throws IdNotFoundException {
		Set<Section> resultSet = new HashSet<Section>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<Section> set = null;
			try {
				set = cm.getChildSections(parentSectionEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate parent section " + parentSectionEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the section, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(parentSectionEid, Section.class.getName());
		}
		return resultSet;
	}

	public CourseOffering getCourseOffering(String courseOfferingEid) throws IdNotFoundException {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				return cm.getCourseOffering(courseOfferingEid);
			} catch (IdNotFoundException ide) {
				if(log.isDebugEnabled()) log.debug(cm + " could not locate course offering " + courseOfferingEid);
			}
		}
	throw new IdNotFoundException(courseOfferingEid, CanonicalCourse.class.getName());
	}

	public Set<Membership> getCourseOfferingMemberships(String courseOfferingEid) throws IdNotFoundException {
		Set<Membership> resultSet = new HashSet<Membership>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<Membership> set = null;
			try {
				set = cm.getCourseOfferingMemberships(courseOfferingEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate course offering " + courseOfferingEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the course offering, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(courseOfferingEid, CourseOffering.class.getName());
		}
		return resultSet;
	}

	public Set<CourseOffering> getCourseOfferingsInCourseSet(String courseSetEid) throws IdNotFoundException {
		Set<CourseOffering> resultSet = new HashSet<CourseOffering>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<CourseOffering> set = null;
			try {
				set = cm.getCourseOfferingsInCourseSet(courseSetEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate course set " + courseSetEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		if(exceptions == implList.size()) {
			// all of the impls threw an IdNotFoundException, so the course set doesn't exist anywhere
			throw new IdNotFoundException(courseSetEid, CourseSet.class.getName());
		}
		return resultSet;
	}

	public CourseSet getCourseSet(String eid) throws IdNotFoundException {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				return cm.getCourseSet(eid);
			} catch (IdNotFoundException ide) {
				if(log.isDebugEnabled()) log.debug(cm + " could not locate course set " + eid);
			}
		}
	throw new IdNotFoundException(eid, CourseSet.class.getName());
	}

	public Set<Membership> getCourseSetMemberships(String courseSetEid) throws IdNotFoundException {
		Set<Membership> resultSet = new HashSet<Membership>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<Membership> set = null;
			try {
				set = cm.getCourseSetMemberships(courseSetEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate course set " + courseSetEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the course set, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(courseSetEid, CourseSet.class.getName());
		}
		return resultSet;
	}

	public Set<CourseSet> getCourseSets() {
		Set<CourseSet> resultSet = new HashSet<CourseSet>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<CourseSet> set = cm.getCourseSets();
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		return resultSet;
	}

	public List<AcademicSession> getCurrentAcademicSessions() {
		List<AcademicSession> resultSet = new ArrayList<AcademicSession>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			List<AcademicSession> list = cm.getCurrentAcademicSessions();
			if(list != null) {
				resultSet.addAll(list);
			}
		}
		// The federated list uses the sort provided by the db
		return resultSet;
	}

	public EnrollmentSet getEnrollmentSet(String enrollmentSetEid) throws IdNotFoundException {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				return cm.getEnrollmentSet(enrollmentSetEid);
			} catch (IdNotFoundException ide) {
				if(log.isDebugEnabled()) log.debug(cm + " could not locate enrollmentSet " + enrollmentSetEid);
			}
		}
	throw new IdNotFoundException(enrollmentSetEid, EnrollmentSet.class.getName());
	}

	public Set<EnrollmentSet> getEnrollmentSets(String courseOfferingEid) throws IdNotFoundException {
		Set<EnrollmentSet> resultSet = new HashSet<EnrollmentSet>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<EnrollmentSet> set = null;
			try {
				set = cm.getEnrollmentSets(courseOfferingEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate course offering " + courseOfferingEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the course offering, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(courseOfferingEid, CourseOffering.class.getName());
		}
		return resultSet;
	}

	public Set<Enrollment> getEnrollments(String enrollmentSetEid) throws IdNotFoundException {
		Set<Enrollment> resultSet = new HashSet<Enrollment>();
		int exceptions =0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<Enrollment> set = null;
			try {
				set = cm.getEnrollments(enrollmentSetEid);
			} catch (IdNotFoundException ide) {
				if(log.isDebugEnabled()) log.debug(cm + " could not locate enrollment set " + enrollmentSetEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the enrollment set, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(enrollmentSetEid, EnrollmentSet.class.getName());
		}
		return resultSet;
	}

	public Set<CanonicalCourse> getEquivalentCanonicalCourses(String canonicalCourseEid)  throws IdNotFoundException {
		Set<CanonicalCourse> resultSet = new HashSet<CanonicalCourse>();
		int exceptions =0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<CanonicalCourse> set = null;
			try {
				set = cm.getEquivalentCanonicalCourses(canonicalCourseEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate canonical course " + canonicalCourseEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the canonical course, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(canonicalCourseEid, CanonicalCourse.class.getName());
		}
		return resultSet;
	}

	public Set<CourseOffering> getEquivalentCourseOfferings(String courseOfferingEid) throws IdNotFoundException {
		Set<CourseOffering> resultSet = new HashSet<CourseOffering>();
		int exceptions =0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<CourseOffering> set = null;
			try {
				set = cm.getEquivalentCourseOfferings(courseOfferingEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate course offering " + courseOfferingEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the course offering, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(courseOfferingEid, CourseOffering.class.getName());
		}
		return resultSet;
	}

	public Set<String> getInstructorsOfRecordIds(String enrollmentSetEid) throws IdNotFoundException {
		Set<String> resultSet = new HashSet<String>();
		int exceptions =0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<String> set = null;
			try {
				set = cm.getInstructorsOfRecordIds(enrollmentSetEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate enrollment set " + enrollmentSetEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the enrollment set, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(enrollmentSetEid, EnrollmentSet.class.getName());
		}
		return resultSet;
	}

	public Section getSection(String sectionEid) throws IdNotFoundException {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				return cm.getSection(sectionEid);
			} catch (IdNotFoundException ide) {
				if(log.isDebugEnabled()) log.debug(cm + " could not locate section " + sectionEid);
			}
		}
		throw new IdNotFoundException(sectionEid, Section.class.getName());
	}

	public Set<Membership> getSectionMemberships(String sectionEid) throws IdNotFoundException {
		Set<Membership> resultSet = new HashSet<Membership>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<Membership> set = null;
			try {
				set = cm.getSectionMemberships(sectionEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate section " + sectionEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the section, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(sectionEid, Section.class.getName());
		}
		return resultSet;
	}

	public Set<Section> getSections(String courseOfferingEid) throws IdNotFoundException {
		Set<Section> resultSet = new HashSet<Section>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<Section> set = null;
			try {
				set = cm.getSections(courseOfferingEid);
			} catch (IdNotFoundException ide) {
				exceptions++;
				if(log.isDebugEnabled()) log.debug(cm + " could not locate course offering " + courseOfferingEid);
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		// If none of the impls could find the course offering, throw an IdNotFoundException.
		if(exceptions == implList.size()) {
			throw new IdNotFoundException(courseOfferingEid, CourseOffering.class.getName());
		}
		return resultSet;
	}

	public boolean isEmpty(String courseSetEid) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				// If any implementation says that the course set is not empty, it's not empty!
				if(!cm.isEmpty(courseSetEid)) {
					return false;
				}
			} catch (UnsupportedOperationException uso) {
				if(log.isDebugEnabled()) log.debug(cm + " doesn't know whether " + courseSetEid + " is empty");
			}
		}
		return true;
	}

	public boolean isEnrolled(String userId, Set<String> enrollmentSetEids) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				// If any implementation says that this user is enrolled, they are enrolled!
				if(cm.isEnrolled(userId, enrollmentSetEids)) {
					return true;
				}
			} catch (UnsupportedOperationException uso) {
				if(log.isDebugEnabled()) log.debug(cm + " doesn't know whether " + userId + " is enrolled in any of these enrollment sets: " + enrollmentSetEids);
			}
		}
		return false;
	}

	public boolean isEnrolled(String userId, String enrollmentSetEid) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				// If any implementation says that this user is enrolled, they are enrolled!
				if(cm.isEnrolled(userId, enrollmentSetEid)) {
					return true;
				}
			} catch (UnsupportedOperationException uso) {
				if(log.isDebugEnabled()) log.debug(cm + " doesn't know whether " + userId + " is enrolled in enrollment sets: " + enrollmentSetEid);
			}
		}
		return false;
	}

	public Set<Section> findEnrolledSections(String userId) {
		Set<Section> resultSet = new HashSet<Section>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<Section> set = cm.findEnrolledSections(userId);
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		return resultSet;
	}

	public Map<String, String> findCourseOfferingRoles(String userEid) {
		Map<String, String> courseOfferingRoleMap = new HashMap<String, String>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Map<String, String> map = cm.findCourseOfferingRoles(userEid);
			if(map == null) {
				continue;
			}
			for(Iterator<Entry<String, String>> mapIter = map.entrySet().iterator(); mapIter.hasNext();) {
				Entry<String, String> entry = mapIter.next();
				String courseSetEid = entry.getKey();
				String role = entry.getValue();
				// Earlier impls take precedence, so don't overwrite what's in the map
				if( ! courseOfferingRoleMap.containsKey(courseSetEid)) {
					courseOfferingRoleMap.put(courseSetEid, role);
				}
			}
		}
		return courseOfferingRoleMap;
	}

	public Map<String, String> findCourseSetRoles(String userEid) {
		Map<String, String> courseSetRoleMap = new HashMap<String, String>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Map<String, String> map = cm.findCourseSetRoles(userEid);
			if(map == null) {
				continue;
			}
			for(Iterator<Entry<String, String>> mapIter = map.entrySet().iterator(); mapIter.hasNext();) {
				Entry<String, String> entry = mapIter.next();
				String courseSetEid = entry.getKey();
				String role = entry.getValue();
				// Earlier impls take precedence, so don't overwrite what's in the map
				if( ! courseSetRoleMap.containsKey(courseSetEid)) {
					courseSetRoleMap.put(courseSetEid, role);
				}
			}
		}
		return courseSetRoleMap;
	}

	public Map<String, String> findSectionRoles(String userEid) {
		Map<String, String> sectionRoleMap = new HashMap<String, String>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Map<String, String> map = cm.findSectionRoles(userEid);
			if(map == null) {
				continue;
			}
			for(Iterator<Entry<String, String>> mapIter = map.entrySet().iterator(); mapIter.hasNext();) {
				Entry<String, String> entry = mapIter.next();
				String sectionEid = entry.getKey();
				String role = entry.getValue();
				// Earlier impls take precedence, so don't overwrite what's in the map
				if( ! sectionRoleMap.containsKey(sectionEid)) {
					sectionRoleMap.put(sectionEid, role);
				}
			}
		}
		return sectionRoleMap;
	}

	public Set<CourseOffering> getCourseOfferingsInCanonicalCourse(String canonicalCourseEid) throws IdNotFoundException {
		Set<CourseOffering> resultSet = new HashSet<CourseOffering>();
		int exceptions = 0;
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			Set<CourseOffering> set = null;
			try {
				set = cm.getCourseOfferingsInCanonicalCourse(canonicalCourseEid);
			} catch (IdNotFoundException ide) {
				if(log.isDebugEnabled()) log.debug(cm + " could not find canonical course " + canonicalCourseEid);
				exceptions++;
			}
			if(set != null) {
				resultSet.addAll(set);
			}
		}
		if(exceptions == implList.size()) {
			// all of the impls threw an IdNotFoundException, so the canonical course doesn't exist anywhere
			throw new IdNotFoundException(canonicalCourseEid, CanonicalCourse.class.getName());
		}
		return resultSet;
	}

	public boolean isAcademicSessionDefined(String eid) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				// If any implementation says that the object exists, it exists!
				if(cm.isAcademicSessionDefined(eid)) {
					return true;
				}
			} catch (UnsupportedOperationException uso) {
				if(log.isDebugEnabled()) log.debug(cm + " doesn't know whether academic session "+ eid + " exists");
			}
		}
		return false;
	}

	public boolean isCanonicalCourseDefined(String eid) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				// If any implementation says that the object exists, it exists!
				if(cm.isCanonicalCourseDefined(eid)) {
					return true;
				}
			} catch (UnsupportedOperationException uso) {
				if(log.isDebugEnabled()) log.debug(cm + " doesn't know whether canonical course "+ eid + " exists");
			}
		}
		return false;
	}

	public boolean isCourseOfferingDefined(String eid) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				// If any implementation says that the object exists, it exists!
				if(cm.isCourseOfferingDefined(eid)) {
					return true;
				}
			} catch (UnsupportedOperationException uso) {
				if(log.isDebugEnabled()) log.debug(cm + " doesn't know whether course offering "+ eid + " exists");
			}
		}
		return false;
	}

	public boolean isCourseSetDefined(String eid) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				// If any implementation says that the object exists, it exists!
				if(cm.isCourseSetDefined(eid)) {
					return true;
				}
			} catch (UnsupportedOperationException uso) {
				if(log.isDebugEnabled()) log.debug(cm + " doesn't know whether course set "+ eid + " exists");
			}
		}
		return false;
	}

	public boolean isEnrollmentSetDefined(String eid) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				// If any implementation says that the object exists, it exists!
				if(cm.isEnrollmentSetDefined(eid)) {
					return true;
				}
			} catch (UnsupportedOperationException uso) {
				if(log.isDebugEnabled()) log.debug(cm + " doesn't know whether enrollment set "+ eid + " exists");
			}
		}
		return false;
	}

	public boolean isSectionDefined(String eid) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			try {
				// If any implementation says that the object exists, it exists!
				if(cm.isSectionDefined(eid)) {
					return true;
				}
			} catch (UnsupportedOperationException uso) {
				if(log.isDebugEnabled()) log.debug(cm + " doesn't know whether section "+ eid + " exists");
			}
		}
		return false;
	}

	public List<String> getSectionCategories() {
		List<String> resultSet = new ArrayList<String>();
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			List<String> list = cm.getSectionCategories();
			if(list != null) {
				resultSet.addAll(list);
			}
		}
		
		// The federated list should be sorted as a single collection.
		Collections.sort(resultSet, new Comparator<String>() {
			public int compare(String cat1,String cat2) {
				return cat1.compareTo(cat2);
			}
		});
		return resultSet;
	}

	public String getSectionCategoryDescription(String categoryCode) {
		for(Iterator implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = (CourseManagementService)implIter.next();
			String descr = cm.getSectionCategoryDescription(categoryCode);
			if(descr != null) {
				return descr;
			}
		}
		return null;
	}

	public Map<String, String> getEnrollmentStatusDescriptions(Locale locale) {
		Map<String, String> statusMap = new HashMap<String, String>();
		for(Iterator<CourseManagementService> implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = implIter.next();
			Map<String, String> map = cm.getEnrollmentStatusDescriptions(locale);
			if(map == null) {
				continue;
			}
			for(Iterator<Entry<String, String>> mapIter = map.entrySet().iterator(); mapIter.hasNext();) {
				Entry<String, String> entry = mapIter.next();
				// Earlier impls take precedence, so don't overwrite what's in the map
				if( ! statusMap.containsKey(entry.getKey())) {
					statusMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return statusMap;
	}

	public Map<String, String> getGradingSchemeDescriptions(Locale locale) {
		Map<String, String> gradingSchemeMap = new HashMap<String, String>();
		for(Iterator<CourseManagementService> implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = implIter.next();
			Map<String, String> map = cm.getGradingSchemeDescriptions(locale);
			if(map == null) {
				continue;
			}
			for(Iterator<Entry<String, String>> mapIter = map.entrySet().iterator(); mapIter.hasNext();) {
				Entry<String, String> entry = mapIter.next();
				// Earlier impls take precedence, so don't overwrite what's in the map
				if( ! gradingSchemeMap.containsKey(entry.getKey())) {
					gradingSchemeMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return gradingSchemeMap;
	}

	public Map<String, String> getMembershipStatusDescriptions(Locale locale) {
		Map<String, String> statusMap = new HashMap<String, String>();
		for(Iterator<CourseManagementService> implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = implIter.next();
			Map<String, String> map = cm.getMembershipStatusDescriptions(locale);
			if(map == null) {
				continue;
			}
			for(Iterator<Entry<String, String>> mapIter = map.entrySet().iterator(); mapIter.hasNext();) {
				Entry<String, String> entry = mapIter.next();
				// Earlier impls take precedence, so don't overwrite what's in the map
				if( ! statusMap.containsKey(entry.getKey())) {
					statusMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return statusMap;
	}

	public List<CourseOffering> findActiveCourseOfferingsInCanonicalCourse(	String eid) {
		log.debug("findActiveCourseOfferingsInCanonicalCourse(" + eid +")");
		List<CourseOffering> ret = new ArrayList<CourseOffering>();
		for(Iterator<CourseManagementService> implIter = implList.iterator(); implIter.hasNext();) {
			CourseManagementService cm = implIter.next();
			List<CourseOffering> col = cm.findActiveCourseOfferingsInCanonicalCourse(eid);
			if (col != null) {
				ret.addAll(col);
			}
		}
		return ret;
	}
}
