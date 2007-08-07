/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades.sections;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;

import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.GradebookService;

import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * An implementation of Gradebook-specific authorization needs based
 * on the shared Section Awareness API.
 */
public class AuthzSectionsImpl implements Authz {
    private static final Log log = LogFactory.getLog(AuthzSectionsImpl.class);

    private Authn authn;
    private SectionAwareness sectionAwareness;
    private GradebookPermissionService gradebookPermissionService;
    private GradebookService gradebookService;

	public boolean isUserAbleToGrade(String gradebookUid) {
		String userUid = authn.getUserUid();
		return (getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR) || getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.TA));
	}

	public boolean isUserAbleToGradeAll(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR);
	}
	
	public boolean isUserHasGraderPermissions(String gradebookUid) {
		String userUid = authn.getUserUid();
		Long gradebookId = getGradebookId(gradebookUid);
		List permissions = gradebookPermissionService.getGraderPermissionsForUser(gradebookId, userUid);
		return permissions != null && permissions.size() > 0;
	}
	
	public boolean isUserHasGraderPermissions(Long gradebookId, String userUid) {
		List permissions = gradebookPermissionService.getGraderPermissionsForUser(gradebookId, userUid);
		return permissions != null && permissions.size() > 0;
	}
	
	/**
	 * 
	 * @param sectionUid
	 * @return whether user is Role.TA in given section
	 */
	private boolean isUserTAinSection(String sectionUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSectionMemberInRole(sectionUid, userUid, Role.TA);
	}

	public boolean isUserAbleToEditAssessments(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserAbleToViewOwnGrades(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.STUDENT);
	}
	
	private boolean isUserAbleToGradeOrViewItemForStudent(String gradebookUid, Long itemId, String studentUid, String function) throws IllegalArgumentException {
		if (itemId == null || studentUid == null || function == null) {
			throw new IllegalArgumentException("Null parameter(s) in AuthzSectionsServiceImpl.isUserAbleToGradeItemForStudent");
		}
		
		if (isUserAbleToGradeAll(gradebookUid)) {
			return true;
		}
		
		Long gradebookId = getGradebookId(gradebookUid);
		String userUid = authn.getUserUid();
		
		List viewableSections = getViewableSections(gradebookUid);
		List sectionIds = new ArrayList();
		if (viewableSections != null && !viewableSections.isEmpty()) {
			for (Iterator sectionIter = viewableSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection) sectionIter.next();
				sectionIds.add(section.getUuid());
			}
		}
		
		if (isUserHasGraderPermissions(gradebookId, userUid)) {

			// get the map of authorized item (assignment) ids to grade/view function
			Map itemIdFunctionMap = gradebookPermissionService.getAvailableItemsForStudent(gradebookId, userUid, studentUid, viewableSections);
			
			if (itemIdFunctionMap == null || itemIdFunctionMap.isEmpty()) {
				return false;  // not authorized to grade/view any items for this student
			}
			
			String functionValueForItem = (String)itemIdFunctionMap.get(itemId);
			String view = GradebookService.viewPermission;
			String grade = GradebookService.gradePermission;
			
			if (functionValueForItem != null) {
				if (function.equalsIgnoreCase(grade) && functionValueForItem.equalsIgnoreCase(grade))
					return true;
				
				if (function.equalsIgnoreCase(view) && (functionValueForItem.equalsIgnoreCase(grade) || functionValueForItem.equalsIgnoreCase(view)))
					return true;
			}
	
			return false;
			
		} else {
			// use OOTB permissions based upon TA section membership
			for (Iterator iter = sectionIds.iterator(); iter.hasNext(); ) {
				String sectionUuid = (String) iter.next();
				if (isUserTAinSection(sectionUuid) && getSectionAwareness().isSectionMemberInRole(sectionUuid, studentUid, Role.STUDENT)) {
					return true;
				}
			}
	
			return false;
		}
	}


	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid) throws IllegalArgumentException {	
		return isUserAbleToGradeOrViewItemForStudent(gradebookUid, itemId, studentUid, GradebookService.gradePermission);
	}
	
	public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid) throws IllegalArgumentException {
		return isUserAbleToGradeOrViewItemForStudent(gradebookUid, itemId, studentUid, GradebookService.viewPermission);
	}
	
	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, String itemName, String studentUid) throws IllegalArgumentException {
		if (itemName == null || studentUid == null) {
			throw new IllegalArgumentException("Null parameter(s) in AuthzSectionsServiceImpl.isUserAbleToGradeItemForStudent");
		}
		
		Assignment assignment = gradebookService.getAssignment(gradebookUid, itemName);
		if (assignment != null) {
			return isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid);
		}
		
		return false;
	}
	
	public List getViewableSections(String gradebookUid) {
		List viewableSections = new ArrayList();
		
		List allSections = getAllSections(gradebookUid);
		if (allSections == null || allSections.isEmpty()) {
			return viewableSections;
		}
		
		if (isUserAbleToGradeAll(gradebookUid)) {
			return allSections;
		}

		Map sectionIdCourseSectionMap = new HashMap();

		for (Iterator sectionIter = allSections.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection) sectionIter.next();
			sectionIdCourseSectionMap.put(section.getUuid(), section);
		}
		
		Long gradebookId = getGradebookId(gradebookUid);
		String userUid = authn.getUserUid();
		
		if (isUserHasGraderPermissions(gradebookId, userUid)) {	

			List viewableSectionIds =  gradebookPermissionService.getViewableGroupsForUser(gradebookId, userUid, new ArrayList(sectionIdCourseSectionMap.keySet()));
			if (viewableSectionIds != null && !viewableSectionIds.isEmpty()) {
				for (Iterator idIter = viewableSectionIds.iterator(); idIter.hasNext();) {
					String sectionUuid = (String) idIter.next();
					CourseSection viewableSection = (CourseSection)sectionIdCourseSectionMap.get(sectionUuid);
					if (viewableSection != null)
						viewableSections.add(viewableSection);
				}
			}
		} else {
			// return all sections that the current user is a TA for
			for (Iterator iter = sectionIdCourseSectionMap.keySet().iterator(); iter.hasNext(); ) {
				String sectionUuid = (String) iter.next();
				if (isUserTAinSection(sectionUuid)) {
					CourseSection viewableSection = (CourseSection)sectionIdCourseSectionMap.get(sectionUuid);
					if (viewableSection != null)
						viewableSections.add(viewableSection);
				}
			}
		}
		
		return viewableSections;

	}
	
	public List getAllSections(String gradebookUid) {
		SectionAwareness sectionAwareness = getSectionAwareness();
		List sections = sectionAwareness.getSections(gradebookUid);

		return sections;
	}
	
	private List getSectionEnrollmentsTrusted(String sectionUid) {
		return getSectionAwareness().getSectionMembersInRole(sectionUid, Role.STUDENT);
	}
	
	public Map findMatchingEnrollmentsForItem(String gradebookUid, Long categoryId, String optionalSearchString, String optionalSectionUid) {
		return this.findMatchingEnrollmentsForItemOrCourseGrade(gradebookUid, categoryId, optionalSearchString, optionalSectionUid, false);
	}
	
	public Map findMatchingEnrollmentsForViewableCourseGrade(String gradebookUid, String optionalSearchString, String optionalSectionUid) {
		return this.findMatchingEnrollmentsForItemOrCourseGrade(gradebookUid, null, optionalSearchString, optionalSectionUid, true);
	}
	
	public Map findMatchingEnrollmentsForViewableItems(String gradebookUid, String optionalSearchString, String optionalSectionUid) {
		Map enrollmentMap = new HashMap();
		List filteredEnrollments = new ArrayList();
		if (optionalSearchString != null)
			filteredEnrollments = getSectionAwareness().findSiteMembersInRole(gradebookUid, Role.STUDENT, optionalSearchString);
		else
			filteredEnrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
		
		if (filteredEnrollments.isEmpty()) 
			return enrollmentMap;
		
		// get all the students in the filtered section, if appropriate
		Map studentsInSectionMap = new HashMap();
		if (optionalSectionUid !=  null) {
			List sectionMembers = getSectionEnrollmentsTrusted(optionalSectionUid);
			if (!sectionMembers.isEmpty()) {
				for(Iterator memberIter = sectionMembers.iterator(); memberIter.hasNext();) {
					EnrollmentRecord member = (EnrollmentRecord) memberIter.next();
					studentsInSectionMap.put(member.getUser().getUserUid(), member);
				}
			}
		}
		
		Map studentIdEnrRecMap = new HashMap();
		for (Iterator enrIter = filteredEnrollments.iterator(); enrIter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
			String studentId = enr.getUser().getUserUid();
			if (optionalSectionUid != null) {
				if (studentsInSectionMap.containsKey(studentId)) {
					studentIdEnrRecMap.put(studentId, enr);
				}
			} else {
				studentIdEnrRecMap.put(studentId, enr);
			}
		}			
			
		if (isUserAbleToGradeAll(gradebookUid)) {
			List enrollments = new ArrayList(studentIdEnrRecMap.values());
			
			List allGbItems = gradebookService.getAssignments(gradebookUid);
			HashMap assignFunctionMap = new HashMap();
			if (!allGbItems.isEmpty()) {
				for (Iterator assignIter = allGbItems.iterator(); assignIter.hasNext();) {
					Assignment assign = (Assignment) assignIter.next();
					if (assign != null)
						assignFunctionMap.put(assign.getId(), GradebookService.gradePermission);
				}
			}
			
			for (Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
				EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
				enrollmentMap.put(enr, assignFunctionMap);
			}
			
		} else {
			String userId = authn.getUserUid();
			
			Map sectionIdCourseSectionMap = new HashMap();
			List viewableSections = getViewableSections(gradebookUid);
			for (Iterator sectionIter = viewableSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection) sectionIter.next();
				sectionIdCourseSectionMap.put(section.getUuid(), section);
			}
			
			if (isUserHasGraderPermissions(gradebookUid)) {
				// user has special grader permissions that override default perms
				Gradebook gradebook = (Gradebook)gradebookService.getGradebook(gradebookUid);
				Long gradebookId = gradebook.getId();
				
				List myStudentIds = new ArrayList(studentIdEnrRecMap.keySet());
				
				List selSections = new ArrayList();
				if (optionalSectionUid == null) {  
					// pass all sections
					selSections = new ArrayList(sectionIdCourseSectionMap.values());
				} else {
					// only pass the selected section
					CourseSection section = (CourseSection) sectionIdCourseSectionMap.get(optionalSectionUid);
					if (section != null)
						selSections.add(section);
				}
				
				// we need to get the viewable students, so first create section id --> student ids map
				myStudentIds = getGradebookPermissionService().getViewableStudentsForUser(gradebookId, userId, myStudentIds, selSections);
				Map viewableStudentIdItemsMap = new HashMap();
				if (gradebookService.getAssignments(gradebookUid).isEmpty()) {
					if (myStudentIds != null) {
						for (Iterator stIter = myStudentIds.iterator(); stIter.hasNext();) {
							String stId = (String) stIter.next();
							if (stId != null)
								viewableStudentIdItemsMap.put(stId, null);
						}
					}
				} else {
					viewableStudentIdItemsMap = gradebookPermissionService.getAvailableItemsForStudents(gradebookId, userId, myStudentIds, selSections);
				}
				
				if (!viewableStudentIdItemsMap.isEmpty()) {
					for (Iterator enrIter = viewableStudentIdItemsMap.keySet().iterator(); enrIter.hasNext();) {
						String studentId = (String) enrIter.next();
						EnrollmentRecord enrRec = (EnrollmentRecord)studentIdEnrRecMap.get(studentId);
						if (enrRec != null) {	
							Map itemIdFunctionMap = (Map)viewableStudentIdItemsMap.get(studentId);
							//if (!itemIdFunctionMap.isEmpty()) {
								enrollmentMap.put(enrRec, itemIdFunctionMap);
							//}
						}
					}
				}

			} else { 
				// use default section-based permissions
				
				// Determine the current user's section memberships
				List availableSections = new ArrayList();
				if (optionalSectionUid != null && isUserTAinSection(optionalSectionUid)) {
					if (sectionIdCourseSectionMap.containsKey(optionalSectionUid))
						availableSections.add(optionalSectionUid);
				} else {
					for (Iterator iter = sectionIdCourseSectionMap.keySet().iterator(); iter.hasNext(); ) {
						String sectionUuid = (String)iter.next();
						if (isUserTAinSection(sectionUuid)) {
							availableSections.add(sectionUuid);
						}
					}
				}
				
				// Determine which enrollees are in these sections
				Map uniqueEnrollees = new HashMap();
				for (Iterator iter = availableSections.iterator(); iter.hasNext(); ) {
					String sectionUuid = (String)iter.next();
					List sectionEnrollments = getSectionEnrollmentsTrusted(sectionUuid);
					for (Iterator eIter = sectionEnrollments.iterator(); eIter.hasNext(); ) {
						EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
						uniqueEnrollees.put(enr.getUser().getUserUid(), enr);
					}
				}
				
				// Filter out based upon the original filtered students
				List allGbItems = gradebookService.getAssignments(gradebookUid);
				for (Iterator iter = studentIdEnrRecMap.keySet().iterator(); iter.hasNext(); ) {
					String enrId = (String)iter.next();
					if (uniqueEnrollees.containsKey(enrId)) {
						// iterate through the assignments
						Map itemFunctionMap = new HashMap();
						if (!allGbItems.isEmpty()) {
							for (Iterator itemIter = allGbItems.iterator(); itemIter.hasNext();) {
								Assignment item = (Assignment)itemIter.next();
								if (item != null) {
									itemFunctionMap.put(item.getId(), GradebookService.gradePermission);
								}
							}
						}
						enrollmentMap.put(studentIdEnrRecMap.get(enrId), itemFunctionMap);
					}
				}
			}
		}

		return enrollmentMap;
	}
	
	/**
	 * 
	 * @param gradebookUid
	 * @param categoryId
	 * @param optionalSearchString
	 * @param optionalSectionUid
	 * @param itemIsCourseGrade
	 * @return Map of EnrollmentRecord --> View or Grade 
	 */
	private Map findMatchingEnrollmentsForItemOrCourseGrade(String gradebookUid, Long categoryId, String optionalSearchString, String optionalSectionUid, boolean itemIsCourseGrade) {
		Map enrollmentMap = new HashMap();
		List filteredEnrollments = new ArrayList();
		
		if (optionalSearchString != null)
			filteredEnrollments = getSectionAwareness().findSiteMembersInRole(gradebookUid, Role.STUDENT, optionalSearchString);
		else
			filteredEnrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
		
		if (filteredEnrollments.isEmpty()) 
			return enrollmentMap;
		
		// get all the students in the filtered section, if appropriate
		Map studentsInSectionMap = new HashMap();
		if (optionalSectionUid !=  null) {
			List sectionMembers = getSectionAwareness().getSectionMembersInRole(optionalSectionUid, Role.STUDENT);
			if (!sectionMembers.isEmpty()) {
				for(Iterator memberIter = sectionMembers.iterator(); memberIter.hasNext();) {
					EnrollmentRecord member = (EnrollmentRecord) memberIter.next();
					studentsInSectionMap.put(member.getUser().getUserUid(), member);
				}
			}
		}
		
		Map studentIdEnrRecMap = new HashMap();
		for (Iterator enrIter = filteredEnrollments.iterator(); enrIter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
			String studentId = enr.getUser().getUserUid();
			if (optionalSectionUid != null) {
				if (studentsInSectionMap.containsKey(studentId)) {
					studentIdEnrRecMap.put(studentId, enr);
				}
			} else {
				studentIdEnrRecMap.put(enr.getUser().getUserUid(), enr);
			}
		}
			
		if (isUserAbleToGradeAll(gradebookUid)) {
			List enrollments = new ArrayList(studentIdEnrRecMap.values());
			
			for (Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
				EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
				enrollmentMap.put(enr, GradebookService.gradePermission);
			}

		} else {
			String userId = authn.getUserUid();
			
			Map sectionIdCourseSectionMap = new HashMap();
			List allSections = getAllSections(gradebookUid);
			for (Iterator sectionIter = allSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection) sectionIter.next();
				sectionIdCourseSectionMap.put(section.getUuid(), section);
			}
			
			if (isUserHasGraderPermissions(gradebookUid)) {
				// user has special grader permissions that override default perms
				Gradebook gradebook = (Gradebook)gradebookService.getGradebook(gradebookUid);
				Long gradebookId = gradebook.getId();
				
				List myStudentIds = new ArrayList(studentIdEnrRecMap.keySet());
				
				List selSections = new ArrayList();
				if (optionalSectionUid == null) {  
					// pass all sections
					selSections = new ArrayList(sectionIdCourseSectionMap.values());
				} else {
					// only pass the selected section
					CourseSection section = (CourseSection) sectionIdCourseSectionMap.get(optionalSectionUid);
					if (section != null)
						selSections.add(section);
				}
				
				Map viewableEnrollees = new HashMap();
				if (itemIsCourseGrade) {
					viewableEnrollees = gradebookPermissionService.getCourseGradePermission(gradebookId, userId, myStudentIds, selSections);
				} else {
					int catType = gradebook.getCategory_type();
					viewableEnrollees = gradebookPermissionService.getStudentsForItem(gradebookId, userId, myStudentIds, catType, categoryId, selSections);
				}
				
				if (!viewableEnrollees.isEmpty()) {
					for (Iterator enrIter = viewableEnrollees.keySet().iterator(); enrIter.hasNext();) {
						String studentId = (String) enrIter.next();
						EnrollmentRecord enrRec = (EnrollmentRecord)studentIdEnrRecMap.get(studentId);
						if (enrRec != null) {
							enrollmentMap.put(enrRec, (String)viewableEnrollees.get(studentId));
						}
					}
				}

			} else { 
				// use default section-based permissions
				enrollmentMap = getEnrollmentMapUsingDefaultPermissions(userId, studentIdEnrRecMap, sectionIdCourseSectionMap, optionalSectionUid);
			}
		}

		return enrollmentMap;
	}
	
	/**
	 * 
	 * @param userUid
	 * @param studentIdEnrRecMap
	 * @param sectionIdCourseSectionMap
	 * @param optionalSectionUid
	 * @return Map of EnrollmentRecord to function view/grade using the default permissions (based on TA section membership)
	 */
	private Map getEnrollmentMapUsingDefaultPermissions(String userUid, Map studentIdEnrRecMap, Map sectionIdCourseSectionMap, String optionalSectionUid) {
		// Determine the current user's section memberships
		Map enrollmentMap = new HashMap();
		List availableSections = new ArrayList();
		if (optionalSectionUid != null && isUserTAinSection(optionalSectionUid)) {
			if (sectionIdCourseSectionMap.containsKey(optionalSectionUid))
				availableSections.add(optionalSectionUid);
		} else {
			for (Iterator iter = sectionIdCourseSectionMap.keySet().iterator(); iter.hasNext(); ) {
				String sectionUuid = (String)iter.next();
				if (isUserTAinSection(sectionUuid)) {
					availableSections.add(sectionUuid);
				}
			}
		}
		
		// Determine which enrollees are in these sections
		Map uniqueEnrollees = new HashMap();
		for (Iterator iter = availableSections.iterator(); iter.hasNext(); ) {
			String sectionUuid = (String)iter.next();
			List sectionEnrollments = getSectionEnrollmentsTrusted(sectionUuid);
			for (Iterator eIter = sectionEnrollments.iterator(); eIter.hasNext(); ) {
				EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
				uniqueEnrollees.put(enr.getUser().getUserUid(), enr);
			}
		}
		
		// Filter out based upon the original filtered students
		for (Iterator iter = studentIdEnrRecMap.keySet().iterator(); iter.hasNext(); ) {
			String enrId = (String)iter.next();
			if (uniqueEnrollees.containsKey(enrId)) {
				enrollmentMap.put(studentIdEnrRecMap.get(enrId), GradebookService.gradePermission);
			}
		}
		
		return enrollmentMap;
	}
	
	
	public List findStudentSectionMemberships(String gradebookUid, String studentUid) {
		List sectionMemberships = new ArrayList();
		try {
			sectionMemberships = (List)org.sakaiproject.site.cover.SiteService.getSite(gradebookUid).getGroupsWithMember(studentUid);
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + gradebookUid);
    	}
    	
    	return sectionMemberships;
	}
	
	public List getStudentSectionMembershipNames(String gradebookUid, String studentUid) {
		List sectionNames = new ArrayList();
		List sections = findStudentSectionMemberships(gradebookUid, studentUid);
		if (sections != null && !sections.isEmpty()) {
			Iterator sectionIter = sections.iterator();
			while (sectionIter.hasNext()) {
				Group myGroup = (Group) sectionIter.next();
				sectionNames.add(myGroup.getTitle());
			}
		}
		
		return sectionNames;
	}
	
	private Long getGradebookId(String gradebookUid) {
		return ((Gradebook)(gradebookService.getGradebook(gradebookUid))).getId();
	}

	public Authn getAuthn() {
		return authn;
	}
	public void setAuthn(Authn authn) {
		this.authn = authn;
	}
	public SectionAwareness getSectionAwareness() {
		return sectionAwareness;
	}
	public void setSectionAwareness(SectionAwareness sectionAwareness) {
		this.sectionAwareness = sectionAwareness;
	}
	public GradebookPermissionService getGradebookPermissionService() {
		return gradebookPermissionService;
	}
	public void setGradebookPermissionService(GradebookPermissionService gradebookPermissionService) {
		this.gradebookPermissionService = gradebookPermissionService;
	}
	public GradebookService getGradebookService() {
		return gradebookService;
	}
	public void setGradebookService(GradebookService gradebookService) {
		this.gradebookService = gradebookService;
	}

}
