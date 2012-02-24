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
package org.sakaiproject.coursemanagement.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;

/**
 * A read-only service that queries enterprise course, section, membership, and
 * enrollment data.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface CourseManagementService {
	
	/**
	 * Gets a CourseSet by its eid.
	 * 
	 * @param courseSetEid The CourseSet's unique eid
	 * @return The CourseSet
	 * @throws IdNotFoundException If the eid is not associated with any CourseSet
	 */
	public CourseSet getCourseSet(String courseSetEid) throws IdNotFoundException;
	
	/**
	 * Checks whether a CourseSet exists.
	 * 
	 * @param eid The enterprise id
	 * @return Whether the object exists
	 */
	public boolean isCourseSetDefined(String eid);

	/**
	 * Gets the child CourseSet from a parent CourseSet.
	 * 
	 * @param parentCourseSetEid The parent CourseSet eid
	 * @return The Set of child CourseSets
	 */
	public Set<CourseSet> getChildCourseSets(String parentCourseSetEid) throws IdNotFoundException;

	/**
	 * Gets all of the top level CourseSets
	 * 
	 * @return The Set of CourseSets that have no parent CourseSet
	 */
	public Set<CourseSet> getCourseSets();
		
	/**
	 * Gets the memberships directly contained by this CourseSet.
	 * 
	 * @param courseSetEid
	 * @return The set of memberships in this CourseSet.  This is not a transitive
	 * set.
	 * @throws IdNotFoundException If the eid is not associated with any CourseSet
	 */
	public Set<Membership> getCourseSetMemberships(String courseSetEid) throws IdNotFoundException;

	/**
	 * Gets a CanonicalCourse by its eid.
	 * 
	 * @param canonicalCourseEid
	 * @return The CanonicalCourse
	 * @throws IdNotFoundException If the eid is not associated with any CanonicalCourse
	 */
	public CanonicalCourse getCanonicalCourse(String canonicalCourseEid) throws IdNotFoundException;
	
	/**
	 * Checks whether a CanonicalCourse exists.
	 * 
	 * @param eid The enterprise id
	 * @return Whether the object exists
	 */
	public boolean isCanonicalCourseDefined(String eid);

	/**
	 * Gets the equivalent CanonicalCourses.
	 * 
	 * @param canonicalCourseEid The eid of the CanonicalCourse to use in finding equivalents
	 * @return The set of CanonicalCourses that are equivalent (in the Enterprise
	 * view, not in the Java view -- this is independent of CanonicalCourse.equals()).
	 */
	public Set<CanonicalCourse> getEquivalentCanonicalCourses(String canonicalCourseEid) throws IdNotFoundException;

	/**
	 * Gets the CanonicalCourses in a CourseSet.
	 * 
	 * @param courseSetEid The eid of the CourseSet
	 * @return The set of CanonicalCourses in the CourseSet
	 * @throws IdNotFoundException If the eid is not associated with any CourseSet
	 */
	public Set<CanonicalCourse> getCanonicalCourses(String courseSetEid) throws IdNotFoundException;

	/**
	 * Gets the list of all known AcademicSessions, sorted by start date.
	 * 
	 * @return
	 */
	public List<AcademicSession> getAcademicSessions();
	
	/**
	 * Gets the list of current AcademicSessions, sorted by start date.
	 * 
	 * @return
	 */
	public List<AcademicSession> getCurrentAcademicSessions();
	
	/**
	 * Gets a AcademicSession by its eid.
	 * @param eid
	 * @return The AcademicSession
	 * @throws IdNotFoundException If the eid is not associated with any AcademicSession
	 */
	public AcademicSession getAcademicSession(String eid) throws IdNotFoundException;
	
	/**
	 * Checks whether an AcademicSession exists.
	 * 
	 * @param eid The enterprise id
	 * @return Whether the object exists
	 */
	public boolean isAcademicSessionDefined(String eid);

	/**
	 * Gets a CourseOffering by its eid.
	 * 
	 * @param courseOfferingEid
	 * @return The CourseOffering
	 * @throws IdNotFoundException If the eid is not associated with any CourseOffering
	 */
	public CourseOffering getCourseOffering(String courseOfferingEid) throws IdNotFoundException;

	/**
	 * Checks whether a CourseOffering exists.
	 * 
	 * @param eid The enterprise id
	 * @return Whether the object exists
	 */
	public boolean isCourseOfferingDefined(String eid);

	/**
	 * Gets any equivalent CourseOfferings.
	 * 
	 * @param courseOfferingEid The eid of the CourseOffering to use in finding equivalents
	 * @return The set of CourseOfferings that are equivalent (in the Enterprise
	 * view, not in the Java view -- this is independent of CourseOffering.equals()).
	 * @throws IdNotFoundException If the eid is not associated with any CourseOffering
	 */
	public Set<CourseOffering> getEquivalentCourseOfferings(String courseOfferingEid) throws IdNotFoundException;

	/**
	 * Gets the memberships directly contained by this CourseOffering.
	 * 
	 * @param courseOfferingEid
	 * @return The set of memberships in this CourseOffering.  This is not a recursive
	 * set of Memberships.
	 * @throws IdNotFoundException If the eid is not associated with any CourseOffering
	 */
	public Set<Membership> getCourseOfferingMemberships(String courseOfferingEid) throws IdNotFoundException;

	/**
	 * Gets the CourseOfferings in a CourseSet.
	 * 
	 * @param courseSetEid The eid of the CourseSet
	 * @return The set of CourseOfferings in the CourseSet
	 * @throws IdNotFoundException If the eid is not associated with any CourseSet
	 */
	public Set<CourseOffering> getCourseOfferingsInCourseSet(String courseSetEid) throws IdNotFoundException;

	/**
	 * Finds all of the course offerings in a course set that are current for any given
	 * academic session (regardless of the courseOffering's start and end dates).
	 * 
	 * @param courseSetEid
	 * @param academicSessionEid
	 * @return The set of course offerings
	 * @throws IdNotFoundException
	 */
	public Set<CourseOffering> findCourseOfferings(String courseSetEid, String academicSessionEid) throws IdNotFoundException;
	
	/**
	 * Finds all course offerings belonging to a canonical course.
	 * 
	 * @param canonicalCourseEid The enterprise id of the canonical course
	 * @return The set of course offerings
	 * @throws IdNotFoundException
	 */
	public Set<CourseOffering> getCourseOfferingsInCanonicalCourse(String canonicalCourseEid) throws IdNotFoundException;
	
	/**
	 * Finds all course sets in a given category.  Useful for listing the departments
	 * @param category
	 * @return The list of course sets, sorted by title, ascending
	 */
	public List<CourseSet> findCourseSets(String category);
	
	/**
	 * Determines whether a CourseSet has any CanonicalCourses or CourseSets.
	 * 
	 * @param courseSetEid
	 * @return
	 */
	public boolean isEmpty(String courseSetEid);

	/**
	 * Gets a Section by its eid.
	 * 
	 * @param sectionEid
	 * @return The Section
	 * @throws IdNotFoundException If the eid is not associated with any Section
	 */
	public Section getSection(String sectionEid) throws IdNotFoundException;

	/**
	 * Checks whether a Section exists.
	 * 
	 * @param eid The enterprise id
	 * @return Whether the object exists
	 */
	public boolean isSectionDefined(String eid);

	/**
	 * Gets the top-level Sections associated with a CourseOffering
	 * 
	 * @param courseOfferingEid
	 * @return The Set of Sections
	 * @throws IdNotFoundException If the eid is not associated with any CourseOffering
	 */
	public Set<Section> getSections(String courseOfferingEid) throws IdNotFoundException;

	/**
	 * Gets the list of section categories defined by the institution.
	 * 
	 * @return
	 */
	public List<String> getSectionCategories();

	/**
	 * Gets the description for a category, identified by the category code, or null
	 * if the category code can not be found.
	 * 
	 * @param sectionCategoryCode
	 * @return
	 */
	public String getSectionCategoryDescription(String categoryCode);
	
	/**
	 * Gets the child Sections from a parent Section.
	 * 
	 * @param parentSectionEid The parent Section eid
	 * @return The Set of child Sections
	 * @throws IdNotFoundException If the eid is not associated with any parent Section
	 */
	public Set<Section> getChildSections(String parentSectionEid) throws IdNotFoundException;

	/**
	 * Gets the members directly contained by this Section.
	 * 
	 * @param sectionEid
	 * @return The set of members in this Section.  This is not a transitive
	 * set.
	 * @throws IdNotFoundException If the eid is not associated with any Section
	 */
	public Set<Membership> getSectionMemberships(String sectionEid) throws IdNotFoundException;
	
	/**
	 * Gets an EnrollmentSet by its eid.
	 * 
	 * @param enrollmentSetEid
	 * @return The EnrollmentSet
	 * @throws IdNotFoundException If the eid is not associated with any EnrollmentSet
	 */
	public EnrollmentSet getEnrollmentSet(String enrollmentSetEid) throws IdNotFoundException;

	/**
	 * Checks whether an EnrollmentSet exists.
	 * 
	 * @param eid The enterprise id
	 * @return Whether the object exists
	 */
	public boolean isEnrollmentSetDefined(String eid);

	/**
	 * Gets the EnrollmentSets associated with a CourseOffering
	 * 
	 * @param courseOfferingEid
	 * @return The Set of EnrollmentSets
	 * @throws IdNotFoundException If the eid is not associated with any CourseOffering
	 */
	public Set<EnrollmentSet> getEnrollmentSets(String courseOfferingEid) throws IdNotFoundException;

	/**
	 * Gets the Enrollments in an EnrollmentSet (including dropped enrollments)
	 * 
	 * @param enrollmentSetEid
	 * @return The Set of Enrollments
	 * @throws IdNotFoundException If the eid is not associated with any EnrollmentSet
	 */
	public Set<Enrollment> getEnrollments(String enrollmentSetEid) throws IdNotFoundException;

	/**
	 * Gets the known enrollment status codes and descriptions for Enrollments.
	 * 
	 * @return
	 */
	public Map<String, String> getEnrollmentStatusDescriptions(Locale locale);
	
	/**
	 * Gets the known grading scheme codes and descriptions for Enrollments.
	 * 
	 * @return
	 */
	public Map<String, String> getGradingSchemeDescriptions(Locale locale);

	/**
	 * Gets the known membership status codes and descriptions for Memberships.
	 * 
	 * @return
	 */
	public Map<String, String> getMembershipStatusDescriptions(Locale locale);
	
	/**
	 * Gets the set of user ids that are, according to the enterprise, responsible for
	 * the EnrollmentSet.  Responsibilities usually include submitting the final grades
	 * for students enrolled in the EnrollmentSet.
	 * 
	 * @param enrollmentSetEid
	 * @return The set of ids for users who are responsible for this EnrollmentSet
	 * @throws IdNotFoundException If the eid is not associated with any EnrollmentSet
	 */
	public Set<String> getInstructorsOfRecordIds(String enrollmentSetEid) throws IdNotFoundException;
	
	/**
	 * Determines whether a user is enrolled (and not dropped) in an EnrollmentSet.
	 * This method is needed to implement Sakai's GroupProvider.
	 * 
	 * @param userEid The student's userEid
	 * @param enrollmentSetEids The set of EnrollmentSetEids
	 * @return
	 */
	public boolean isEnrolled(String userEid, Set<String> enrollmentSetEids);

	/**
	 * Convenience method for checking whether a user is enrolled (and not dropped)
	 * in an EnrollmentSet.
	 * 
	 * @param userEid
	 * @param enrollmentSetEid
	 * @return
	 */
	public boolean isEnrolled(String userEid, String enrollmentSetEid);


	/**
	 * Finds the Enrollment for a user in an EnrollmentSet.  If the user isn't in the
	 * EnrollmentSet, or the EnrollmentSet doesn't exist, this returns null.  Note that
	 * this method will return enrollments flagged as "dropped".
	 * 
	 * TODO Should this throw more descriptive exceptions e.g. when the EnrollmentSet doesn't exist?
	 * 
	 * @param userEid
	 * @param enrollmentSetEid
	 * @return
	 */
	public Enrollment findEnrollment(String userEid, String enrollmentSetEid);

	/**
	 * Finds the set of current EnrollmentSets for which a user is enrolled but not dropped.
	 * An EnrollmentSet is considered current if its CourseOffering's start date
	 * (is null or prior to the current date/time) and its end date (is null or
	 * after the current date/time).
	 * 
	 * @param userEid
	 * @return
	 */
	public Set<EnrollmentSet> findCurrentlyEnrolledEnrollmentSets(String userEid);

	/**
	 * Finds the set of current EnrollmentSets for which a user is an instructor of
	 * record.  An EnrollmentSet is considered current if its CourseOffering's start
	 * date (is null or prior to the current date/time) and its end date (is null or
	 * after the current date/time).
	 * 
	 * @param userEid
	 * @return
	 */
	public Set<EnrollmentSet> findCurrentlyInstructingEnrollmentSets(String userEid);
	
	/**
	 * Finds all Sections that are linked to an EnrollmentSet for
	 * which a user is an instructor of record.
	 * 
	 * @param userEid
	 * @return
	 */
	public Set<Section> findInstructingSections(String userEid);

	/**
	 * Finds all Sections that are linked to an EnrollmentSet for
	 * which a user is enrolled (but not dropped).
	 * 
	 * @param userEid
	 * @return
	 */
	public Set<Section> findEnrolledSections(String userEid);

	/**
	 * Finds all Sections that are linked to an EnrollmentSet for which a user is an
	 * instructor of record and which are part of a CourseOffering in a given
	 * AcademicSession.
	 * 
	 * @param userEid
	 * @param academicSessionEid
	 * @return
	 */
	public Set<Section> findInstructingSections(String userEid, String academicSessionEid) throws IdNotFoundException;

	/**
	 * Finds the Sections (and roles) for which a user is a member.
	 * 
	 * @param userEid
	 * @return A Map of Section EIDs to roles for the user
	 */
	public Map<String, String> findSectionRoles(String userEid);

	/**
	 * Finds the CourseOfferings (and roles) for which a user is a member.
	 * 
	 * @param userEid
	 * @return A Map of CourseOffering EIDs to roles for the user
	 */
	public Map<String, String> findCourseOfferingRoles(String userEid);


	/**
	 * Finds the CourseSets (and roles) for which a user is a member.
	 * 
	 * @param userEid
	 * @return A Map of CourseSet EIDs to roles for the user
	 */
	public Map<String, String> findCourseSetRoles(String userEid);
	
	/**
	 * Find the currently offered course offerings in the cannonical course
	 * @param eid
	 * @return
	 */
	public List<CourseOffering> findActiveCourseOfferingsInCanonicalCourse(String eid); 

}
