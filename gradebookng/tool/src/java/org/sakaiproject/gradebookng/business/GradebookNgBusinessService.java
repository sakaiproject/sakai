/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Application;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.gradebookng.business.model.*;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gradebookng.business.exception.GbAccessDeniedException;
import org.sakaiproject.gradebookng.business.exception.GbException;
import org.sakaiproject.gradebookng.business.importExport.CommentValidator;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.business.util.EventHelper;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CategoryScoreData;
import org.sakaiproject.grading.api.CommentDefinition;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GraderPermission;
import org.sakaiproject.grading.api.GradingCategoryType;
import org.sakaiproject.grading.api.GradingPermissionService;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.InvalidGradeException;
import org.sakaiproject.grading.api.PermissionDefinition;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.UserSortNameComparator;

/**
 * Business service for GradebookNG
 *
 * This is not designed to be consumed outside of the application or supplied entityproviders. Use at your own risk.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

// TODO add permission checks! Remove logic from entityprovider if there is a
// double up
// TODO some of these methods pass in empty lists and its confusing. If we
// aren't doing paging, remove this.

@Slf4j
public class GradebookNgBusinessService {

	@Setter
	private AssignmentService assignmentService;

	@Setter
	private SiteService siteService;

	@Setter
	private UserDirectoryService userDirectoryService;

	@Setter @Getter
	private ServerConfigurationService serverConfigService;

	@Setter
	private ToolManager toolManager;

	@Setter
	private GradingService gradingService;

	@Setter
	private GradingPermissionService gradingPermissionService;

	@Setter
	private PreferencesService preferencesService;

	@Setter
	private SectionManager sectionManager;

	@Setter
	private CourseManagementService courseManagementService;

	@Setter
	private GroupProvider groupProvider;

	@Setter
	private SecurityService securityService;

	@Setter
	private RubricsService rubricsService;
	
	@Setter
	private FormattedText formattedText;

	@Setter
	private UserTimeService userTimeService;
	
	@Setter
	private TaskService taskService;
	

	public static final String GB_PREF_KEY = "GBNG-";
	public static final String ASSIGNMENT_ORDER_PROP = "gbng_assignment_order";
	public static final String ICON_SAKAI = "si si-";
	public static final String ALL = "all";

	private static final String SAK_PROP_ALLOW_STUDENTS_TO_COMPARE_GRADES = "gradebookng.allowStudentsToCompareGradesWithClassmates";
	private static final Boolean SAK_PROP_ALLOW_STUDENTS_TO_COMPARE_GRADES_DEFAULT = false;

	/**
	 * Get a list of all users in the current site that can have grades
	 *
	 * @return a list of users as uuids or null if none
	 */
	public List<String> getGradeableUsers() {
		return this.getGradeableUsers(getCurrentSiteId());
	}

	/**
	 * Get a list of all users in the given site that can have grades
	 *
	 * @param siteId
	 * @return a list of users as uuids or null if none
	 */
	public List<String> getGradeableUsers(final String siteId) {
		return this.getGradeableUsers(siteId, null);
	}

	/**
	 * Get the list of gradeable users
	 * @param groupFilter
	 * @return
	 *
	 */
	public List<String> getGradeableUsers(final GbGroup groupFilter) {
		return this.getGradeableUsers(null, groupFilter);
	}

	/**
	 * Get a list of all users in the given site, filtered by the given group, that can have grades
	 *
	 * @param siteId the id of the site to lookup
	 * @param groupFilter GbGroupType to filter on
	 *
	 * @return a list of users as uuids or null if none
	 */
	public List<String> getGradeableUsers(final String siteId, final GbGroup groupFilter) {

		try {

			String givenSiteId = siteId;
			if (StringUtils.isBlank(givenSiteId)) {
				givenSiteId = getCurrentSiteId();
			}

			// note that this list MUST exclude TAs as it is checked in the
			// GradingService and will throw a SecurityException if invalid
			// users are provided
			Site site = siteService.getSite(givenSiteId);
			final Set<String> userUuids = site.getUsersIsAllowed(GbRole.STUDENT.getValue());

			// filter the allowed list based on membership
			if (groupFilter != null && groupFilter.getType() != GbGroup.Type.ALL) {

				final Set<String> groupMembers = new HashSet<>();

				if (groupFilter.getType() == GbGroup.Type.GROUP) {
					final Set<Member> members = this.siteService.getSite(givenSiteId).getGroup(groupFilter.getId())
							.getMembers();
					for (final Member m : members) {
						if (userUuids.contains(m.getUserId())) {
							groupMembers.add(m.getUserId());
						}
					}
				}

				// only keep the ones we identified in the group
				userUuids.retainAll(groupMembers);
			}

			final GbRole role = this.getUserRole(givenSiteId);

			// if TA, pass it through the gradebook permissions (only if there
			// are permissions)
			if (role == GbRole.TA) {
				final User user = getCurrentUser();

				// if there are permissions, pass it through them
				// don't need to test TA access if no permissions
				final List<PermissionDefinition> perms = getPermissionsForUser(user.getId(),siteId);
				if (!perms.isEmpty()) {

					final Gradebook gradebook = this.getGradebook(givenSiteId);

					// get list of sections and groups this TA has access to
					final List<CourseSection> courseSections = this.gradingService.getViewableSections(gradebook.getUid());

					//for each section TA has access to, grab student Id's
					List<String> viewableStudents = new ArrayList();

					Map<String, Set<Member>> groupMembers = getGroupMembers(givenSiteId);
					
					//iterate through sections available to the TA and build a list of the student members of each section
					if(courseSections != null && !courseSections.isEmpty() && groupMembers!=null){
						for(CourseSection section:courseSections){
							if(groupMembers.containsKey(section.getUuid())) {
								Set<Member> members = groupMembers.get(section.getUuid());
								for(Member member:members){
									if(givenSiteId!=null && member.getUserId()!=null && securityService.unlock(member.getUserId(), GbPortalPermission.VIEW_OWN_GRADES.getValue(), siteService.siteReference(givenSiteId))/*member.getRole().equals("S")*/){
											viewableStudents.add(member.getUserId());
									}
								}
							}
						}
					}

					// If all group IDs in perms are null, this means TA has permission to view/grade All Sections/Groups.
					// In this situation, we should add non-provided site members to their viewable list
					List<String> nonProvidedMembers = site.getMembers().stream().filter(m -> !m.isProvided()).map(Member::getUserId).collect(Collectors.toList());
					if (perms.stream().allMatch(p -> p.getGroupReference() == null)) {
						viewableStudents.addAll(nonProvidedMembers);
					}

					if (!viewableStudents.isEmpty()) {
						userUuids.retainAll(viewableStudents); // retain only those that are visible to this TA
					} else {
						userUuids.removeAll(sectionManager.getSectionEnrollmentsForStudents(givenSiteId, userUuids).getStudentUuids()); // TA can view/grade students without section
						userUuids.removeAll(nonProvidedMembers); // Filter out non-provided users
					}
				}
			}

			return new ArrayList<>(userUuids);

		} catch (final IdUnusedException e) {
			log.warn("IdUnusedException trying to getGradeableUsers", e);
			return null;
		} catch (final GbAccessDeniedException e) {
			log.warn("GbAccessDeniedException trying to getGradeableUsers", e);
			return null;
		}
	}

	/**
	 * Given a list of uuids, get a list of Users
	 *
	 * @param userUuids list of user uuids
	 * @return
	 */
	public List<User> getUsers(final List<String> userUuids) throws GbException {
		try {
			final List<User> users = this.userDirectoryService.getUsers(userUuids);
			Collections.sort(users, new UserSortNameComparator()); // TODO: remove this sort, it causes double sorting in various scenarios
			return users;
		} catch (final RuntimeException e) {
			// an LDAP exception can sometimes be thrown here, catch and rethrow
			throw new GbException("An error occurred getting the list of users.", e);
		}
	}

	/**
	* Create a map so that we can use the user's EID (from the imported file) to lookup their UUID (used to store the grade by the backend service).
	*
	* @return Map where the user's EID is the key and the {@link GbUser} object is the value
	*/
	public Map<String, GbUser> getUserEidMap() {
		final List<GbUser> users = getGbUsers(getGradeableUsers());
		final Map<String, GbUser> userEidMap = new HashMap<>();
		for (final GbUser user : users) {
			final String eid = user.getDisplayId();
			if (StringUtils.isNotBlank(eid)) {
				userEidMap.put(eid, user);
			}
		}

		return userEidMap;
	}

	/**
	 * Gets a List of GbUsers for the specified userUuids without any filtering.
	 * Appropriate only for back end business like grade exports, statistics, etc.
	 * @param userUuids
	 * @return
	 */
	public List<GbUser> getGbUsers(final List<String> userUuids)
	{
		final List<GbUser> gbUsers = new ArrayList<>(userUuids.size());
		final List<User> users = getUsers(userUuids);
		final Site site = getCurrentSite().orElse(null);

		Map<String, List<String>> userSections
			= (site != null) ? getUserSections(site.getId()) : Collections.emptyMap();

		for (final User u : users) {
			gbUsers.add(new GbUser(u, getStudentNumber(u, site))
							.setSections(userSections.getOrDefault(u.getId(), Collections.emptyList())));
		}

		return gbUsers;
	}

	/**
	 * Helper to get a reference to the gradebook for the current site
	 *
	 * @return the gradebook for the site
	 */
	public Gradebook getGradebook() {
		return getGradebook(getCurrentSiteId());
	}

	/**
	 * Helper to get a reference to the gradebook for the specified site
	 *
	 * @param siteId the siteId
	 * @return the gradebook for the site
	 */
	private Gradebook getGradebook(String siteId) {

		return gradingService.getGradebook(siteId);
	}

	/**
	 * Get a list of assignments in the gradebook in the current site that the current user is allowed to access
	 *
	 * @return a list of assignments or empty list if none/no gradebook
	 */
	public List<Assignment> getGradebookAssignments() {
		return getGradebookAssignments(getCurrentSiteId(), SortType.SORT_BY_SORTING);
	}

	/**
	 * Get a list of assignments in the gradebook in the current site that the current user is allowed to access
	 *
	 * @param siteId
	 * @return a list of assignments or empty list if none/no gradebook
	 */
	public List<Assignment> getGradebookAssignments(String siteId) {
		return getGradebookAssignments(siteId, SortType.SORT_BY_SORTING);
	}

	/**
	 * Get a list of assignments in the gradebook in the current site that the current user is allowed to access sorted by the provided
	 * SortType
	 *
	 * @param sortBy
	 * @return a list of assignments or empty list if none/no gradebook
	 */
	public List<Assignment> getGradebookAssignments(final SortType sortBy) {
		return getGradebookAssignments(getCurrentSiteId(), sortBy);
	}

	/**
	 * Special operation to get a list of assignments in the gradebook that the specified student has access to. This taked into account
	 * externally defined assessments that may have grouping permissions applied.
	 *
	 * This should only be called if you are wanting to view the assignments that a student would see (ie if you ARE a student, or if you
	 * are an instructor using the student review mode)
	 *
	 * @param studentUuid
	 * @return a list of assignments or empty list if none/no gradebook
	 */
	public List<Assignment> getGradebookAssignmentsForStudent(final String studentUuid) {
		return getGradebookAssignmentsForStudent(studentUuid, SortType.SORT_BY_SORTING);
	}

	/**
	 * Special operation to get a list of assignments in the gradebook that the specified student has access to. This taked into account
	 * externally defined assessments that may have grouping permissions applied.
	 *
	 * This should only be called if you are wanting to view the assignments that a student would see (ie if you ARE a student, or if you
	 * are an instructor using the student review mode)
	 *
	 * Define the sortedBy to return these assignments back in the desired order.
	 *
	 * @return a list of assignments or empty list if none/no gradebook
	 */
	public List<Assignment> getGradebookAssignmentsForStudent(final String studentUuid, final SortType sortedBy) {

		final Gradebook gradebook = getGradebook(getCurrentSiteId());
		final List<Assignment> assignments = getGradebookAssignments(sortedBy);

		// NOTE: cannot do a role check here as it assumes the current user but this could have been called by an instructor (unless we add
		// a new method to handle this)
		// in any case the role check would just be a confirmation that the user passed in was a student.

		// for each assignment we need to check if it is grouped externally and if the user has access to the group
		final Iterator<Assignment> iter = assignments.iterator();
		while (iter.hasNext()) {
			final Assignment a = iter.next();
			if (a.getExternallyMaintained()) {
				if (this.gradingService.isExternalAssignmentGrouped(gradebook.getUid(), a.getExternalId()) &&
					!this.gradingService.isExternalAssignmentVisible(gradebook.getUid(), a.getExternalId(),
						studentUuid)) {
					iter.remove();
				}
			}
		}
		return assignments;
	}

	/**
	 * Get a list of assignments in the gradebook in the specified site that the current user is allowed to access, sorted by sort order
	 *
	 * @param siteId the siteId
	 * @param sortBy
	 * @return a list of assignments or empty list if none/no gradebook
	 */
	public List<Assignment> getGradebookAssignments(final String siteId, final SortType sortBy) {

		final List<Assignment> assignments = new ArrayList<>();
		final Gradebook gradebook = getGradebook(siteId);
		if (gradebook != null) {
			// applies permissions (both student and TA) and default sort is
			// SORT_BY_SORTING
			assignments.addAll(this.gradingService.getViewableAssignmentsForCurrentUser(gradebook.getUid(), sortBy));
		}
        log.debug("Retrieved {} assignments", assignments.size());
		return assignments;
	}

	public List<Assignment> getGradebookAssignmentsForCategory(final Long categoryId, final SortType sortBy) {
		return getGradebookAssignmentsForCategory(this.getCurrentSiteId(), categoryId, sortBy);
	}

	public List<Assignment> getGradebookAssignmentsForCategory(final String siteId, final Long categoryId, final SortType sortBy) {
		final List<Assignment> returnList = new ArrayList<>();
		final Gradebook gradebook = getGradebook(siteId);
		if (gradebook != null) {	// applies permissions (both student and TA) and default sort is SORT_BY_SORTING
			final List<Assignment> assignments = this.gradingService.getViewableAssignmentsForCurrentUser(gradebook.getUid(), sortBy);
			for (Assignment assignment : assignments) {
				if (Objects.equals(assignment.getCategoryId(), categoryId)) {
					returnList.add(assignment);
				}
			}
		}
		return returnList;
	}

	/**
	 * Get a list of categories in the gradebook in the current site
	 *
	 * @return list of categories or null if no gradebook
	 */
	public List<CategoryDefinition> getGradebookCategories() {
		return getGradebookCategories(getCurrentSiteId());
	}

	/**
	 * Get a list of categories in the gradebook in the specified site
	 *
	 * @param siteId the siteId
	 * @return a list of categories or empty if no gradebook
	 */
	public List<CategoryDefinition> getGradebookCategories(final String siteId) {
		final Gradebook gradebook = getGradebook(siteId);

		List<CategoryDefinition> rval = new ArrayList<>();

		if (gradebook == null) {
			return rval;
		}

		if (categoriesAreEnabled()) {
			rval = this.gradingService.getCategoryDefinitions(gradebook.getUid());
		}

		GbRole role;
		try {
			role = this.getUserRole(siteId);
		} catch (final GbAccessDeniedException e) {
			log.warn("GbAccessDeniedException trying to getGradebookCategories", e);
			return rval;
		}

		// filter for TAs
		if (role == GbRole.TA) {
			final User user = getCurrentUser();

			// build a list of categoryIds
			final List<Long> allCategoryIds = new ArrayList<>();
			for (final CategoryDefinition cd : rval) {
				allCategoryIds.add(cd.getId());
			}

			if (allCategoryIds.isEmpty()) {
				return Collections.emptyList();
			}

			// get a list of category ids the user can actually view
			List<Long> viewableCategoryIds = this.gradingPermissionService
					.getCategoriesForUser(gradebook.getId(), user.getId(), allCategoryIds);

			//FIXME: this is a hack to implement the old style realms checks. The above method only checks the gb_permission_t table and not realms
			//if categories is empty (no fine grain permissions enabled), Check permissions, if they are not empty then realms perms exist 
			//and they don't filter to category level so allow all.
			//This should still allow the gb_permission_t perms to override if the TA is restricted to certain categories
			if(viewableCategoryIds.isEmpty() && !this.getPermissionsForUser(user.getId()).isEmpty()){
				viewableCategoryIds = allCategoryIds;
			}

			// remove the ones that the user can't view
			final Iterator<CategoryDefinition> iter = rval.iterator();
			while (iter.hasNext()) {
				final CategoryDefinition categoryDefinition = iter.next();
				if (!viewableCategoryIds.contains(categoryDefinition.getId())) {
					iter.remove();
				}
			}

		}

		// Sort by categoryOrder
		Collections.sort(rval, CategoryDefinition.orderComparator);

		return rval;
	}

	/**
	* Retrieve the categories visible to the given student.
	*
	* This should only be called if you are wanting to view the categories that a student would see (ie if you ARE a student, or if you
	* are an instructor using the student review mode)
	*
	* @param studentUuid
	* @return list of visible categories
	*/
	public List<CategoryDefinition> getGradebookCategoriesForStudent(String studentUuid) {
		// find the categories that this student's visible assignments belong to
		List<Assignment> viewableAssignments = getGradebookAssignmentsForStudent(studentUuid);
		final List<Long> catIds = new ArrayList<>();
		for (Assignment a : viewableAssignments) {
			Long catId = a.getCategoryId();
			if (catId != null && !catIds.contains(catId)) {
				catIds.add(a.getCategoryId());
			}
		}

		// get all the categories in the gradebook, use a security advisor in case the current user is the student
		SecurityAdvisor gbAdvisor = (String userId, String function, String reference)
						-> "gradebook.gradeAll".equals(function) ? SecurityAdvice.ALLOWED : SecurityAdvice.PASS;
		securityService.pushAdvisor(gbAdvisor);
		List<CategoryDefinition> catDefs = gradingService.getCategoryDefinitions(getGradebook().getUid());
		securityService.popAdvisor(gbAdvisor);

		// filter out the categories that don't match the categories of the viewable assignments
		return catDefs.stream().filter(def -> catIds.contains(def.getId())).collect(Collectors.toList());

	}

	/**
	 * Get a map of course grades for all students in the given site.
	 *
	 * @param siteId siteId to get course grades for
	 * @return the map of course grades for students, key = studentUuid, value = course grade, or an empty map
	 */
	public Map<String, CourseGradeTransferBean> getCourseGrades(final String siteId) {
		final Gradebook gradebook = this.getGradebook(siteId);
		final List<String> studentUuids = this.getGradeableUsers(siteId);
		return this.getCourseGrades(gradebook, studentUuids, null);
	}

	/**
	 * Get a map of course grades for all students in the given site using the specified grading schema mapping.
	 *
	 * @param siteId siteId to get course grades for
	 * @param schema grading schema mapping
	 * @return the map of course grades for students, key = studentUuid, value = course grade, or an empty map
	 */
	public Map<String, CourseGradeTransferBean> getCourseGrades(final String siteId, final Map<String,Double> schema) {
		final Gradebook gradebook = this.getGradebook(siteId);
		final List<String> studentUuids = this.getGradeableUsers(siteId);
		return this.getCourseGrades(gradebook, studentUuids, schema);
	}


	/**
	 * Get a map of course grades for the given users.
	 *
	 * @param studentUuids uuids for the students
	 * @return the map of course grades for students, key = studentUuid, value = course grade, or an empty map
	 */
	public Map<String, CourseGradeTransferBean> getCourseGrades(final List<String> studentUuids) {
		final Gradebook gradebook = this.getGradebook();
		return this.getCourseGrades(gradebook, studentUuids, null);
	}

	/**
	 * Get a map of course grades for the given gradebook, users and optionally the grademap you want to use.
	 *
	 * @param gradebook the gradebook
	 * @param studentUuids uuids for the students
	 * @param gradeMap the grade mapping to use. This should be left blank if you are displaying grades to students so that the currently persisted value is used.
	 * @return the map of course grades for students, key = studentUuid, value = course grade, or an empty map
	 */
	private Map<String, CourseGradeTransferBean> getCourseGrades(final Gradebook gradebook, final List<String> studentUuids, final Map<String, Double> gradeMap) {
		Map<String, CourseGradeTransferBean> rval = new HashMap<>();
		if (gradebook != null) {
			if(gradeMap != null) {
				rval = this.gradingService.getCourseGradeForStudents(gradebook.getUid(), studentUuids, gradeMap);
			} else {
				rval = this.gradingService.getCourseGradeForStudents(gradebook.getUid(), studentUuids);
			}
		}
		return rval;
	}

	/**
	 * Get the course grade for a student. Safe to call when logged in as a student.
	 *
	 * @param studentUuid
	 * @return coursegrade. May have null fields if the coursegrade has not been released
	 */
	public CourseGradeTransferBean getCourseGrade(final String studentUuid) {

		final Gradebook gradebook = this.getGradebook();
		final CourseGradeTransferBean courseGrade = this.gradingService.getCourseGradeForStudent(gradebook.getUid(), studentUuid);

		// handle the special case in the gradebook service where totalPointsPossible = -1
		if (courseGrade != null && (courseGrade.getTotalPointsPossible() == null || courseGrade.getTotalPointsPossible() == -1)) {
			courseGrade.setTotalPointsPossible(null);
			courseGrade.setPointsEarned(null);
		}

		return courseGrade;
	}

	/**
	 * Get the student's course grade's GradableObject ID.
	 * @return coursegrade's GradableObject ID.
	 */
	public Long getCourseGradeId(Long gradebookId){
		return this.gradingService.getCourseGradeId(gradebookId);
	}

	/**
	 * Save the grade and comment for a student's assignment and do concurrency checking
	 *
	 * @param assignmentId id of the gradebook assignment
	 * @param studentUuid uuid of the user
	 * @param oldGrade old grade, passed in for concurrency checking/ If null, concurrency checking is skipped.
	 * @param newGrade new grade for the assignment/user
	 * @param comment optional comment for the grade. Can be null.
	 *
	 * @return
	 *
	 * 		TODO make the concurrency check a boolean instead of the null oldGrade
	 */
	public GradeSaveResponse saveGrade(final Long assignmentId, final String studentUuid, final String oldGrade,
			final String newGrade, final String comment) {

		final Gradebook gradebook = this.getGradebook();
		if (gradebook == null) {
			return GradeSaveResponse.ERROR;
		}

		// if newGrade is null, no change
		if (newGrade == null) {
			return GradeSaveResponse.NO_CHANGE;
		}

		// get current grade
		final String storedGrade = this.gradingService.getAssignmentScoreString(gradebook.getUid(), assignmentId,
				studentUuid);

		// get assignment config
		final Assignment assignment = this.getAssignment(assignmentId);
		final Double maxPoints = assignment.getPoints();

		// check what grading mode we are in
		final GradeType gradingType = gradebook.getGradeType();

		// if percentage entry type, reformat the grades, otherwise use points as is
		String newGradeAdjusted = newGrade;
		String oldGradeAdjusted = oldGrade;
		String storedGradeAdjusted = storedGrade;

		// Fix a problem when the grades comes from the old Gradebook API with locale separator, always compare the values using the same
		// separator
		if (StringUtils.isNotBlank(oldGradeAdjusted)) {
			oldGradeAdjusted = oldGradeAdjusted.replace(",".equals(formattedText.getDecimalSeparator()) ? "." : ",",
					",".equals(formattedText.getDecimalSeparator()) ? "," : ".");
		}
		if (StringUtils.isNotBlank(storedGradeAdjusted)) {
			storedGradeAdjusted = storedGradeAdjusted.replace(",".equals(formattedText.getDecimalSeparator()) ? "." : ",",
					",".equals(formattedText.getDecimalSeparator()) ? "," : ".");
		}

		if (gradingType == GradeType.PERCENTAGE) {
			// the passed in grades represents a percentage so the number needs to be adjusted back to points
			Double newGradePercentage = new Double("0.0");

			if(StringUtils.isNotBlank(newGrade)){
				newGradePercentage = FormatHelper.validateDouble(newGrade);
			}

			final Double newGradePointsFromPercentage = (newGradePercentage / 100) * maxPoints;
			newGradeAdjusted = FormatHelper.formatDoubleToDecimal(newGradePointsFromPercentage);

			// only convert if we had a previous value otherwise it will be out of sync
			if (StringUtils.isNotBlank(oldGradeAdjusted)) {
				// To check if our data is out of date, we first compare what we think
				// is the latest saved score against score stored in the database. As the score
				// is stored as points, we must convert this to a percentage. To be sure we're
				// comparing apples with apples, we first determine the number of decimal places
				// on the score, so the converted points-as-percentage is in the expected format.

				final Double oldGradePercentage = FormatHelper.validateDouble(oldGradeAdjusted);
				final Double oldGradePointsFromPercentage = (oldGradePercentage / 100) * maxPoints;

				oldGradeAdjusted = FormatHelper.formatDoubleToMatch(oldGradePointsFromPercentage, storedGradeAdjusted);

				oldGradeAdjusted = oldGradeAdjusted.replace(",".equals(formattedText.getDecimalSeparator()) ? "." : ",",
					",".equals(formattedText.getDecimalSeparator()) ? "," : ".");
			}

			// we dont need processing of the stored grade as the service does that when persisting.
		}

		// trim the .0 from the grades if present. UI removes it so lets standardise
		// trim to null so we can better compare against no previous grade being recorded (as it will be null)
		// Note that we also trim newGrade so that don't add the grade if the new grade is blank and there was no grade previously
		storedGradeAdjusted = StringUtils.trimToNull(StringUtils.removeEnd(storedGradeAdjusted, ".0"));
		oldGradeAdjusted = StringUtils.trimToNull(StringUtils.removeEnd(oldGradeAdjusted, ".0"));
		newGradeAdjusted = StringUtils.trimToNull(StringUtils.removeEnd(newGradeAdjusted, ".0"));

		storedGradeAdjusted = StringUtils.trimToNull(StringUtils.removeEnd(storedGradeAdjusted, ",0"));
		oldGradeAdjusted = StringUtils.trimToNull(StringUtils.removeEnd(oldGradeAdjusted, ",0"));
		newGradeAdjusted = StringUtils.trimToNull(StringUtils.removeEnd(newGradeAdjusted, ",0"));

		if (log.isDebugEnabled()) {
			log.debug("storedGradeAdjusted: " + storedGradeAdjusted);
			log.debug("oldGradeAdjusted: " + oldGradeAdjusted);
			log.debug("newGradeAdjusted: " + newGradeAdjusted);
		}

		// if comment longer than MAX_COMMENT_LENGTH chars, error.
		// SAK-33836 - MAX_COMMENT_LENGTH controlled by sakai.property 'gradebookng.maxCommentLength'; defaults to 20,000
		if (CommentValidator.isCommentInvalid(comment, serverConfigService)) {
			log.error("Comment too long. Maximum {} characters.", CommentValidator.getMaxCommentLength(serverConfigService));
			return GradeSaveResponse.ERROR;
		}

		// no change
		if (StringUtils.equals(storedGradeAdjusted, newGradeAdjusted)) {
			final Double storedGradePoints = FormatHelper.validateDouble(storedGradeAdjusted);
			if (storedGradePoints != null && storedGradePoints.compareTo(maxPoints) > 0) {
				return GradeSaveResponse.OVER_LIMIT;
			} else {
				return GradeSaveResponse.NO_CHANGE;
			}
		}

		// concurrency check, if stored grade != old grade that was passed in,
		// someone else has edited.
		// if oldGrade == null, ignore concurrency check
		if (oldGrade != null) {
			try {
				NumberFormat format = NumberFormat.getNumberInstance();
				// SAK-42001 A stored value in database of 69.225 needs to match the 69.22 coming back from UI AJAX call
				final BigDecimal storedBig = storedGradeAdjusted == null ? BigDecimal.ZERO : new BigDecimal(format.parse(storedGradeAdjusted).doubleValue()).setScale(2, RoundingMode.HALF_DOWN);
				final BigDecimal oldBig = oldGradeAdjusted == null ? BigDecimal.ZERO : new BigDecimal(format.parse(oldGradeAdjusted).doubleValue()).setScale(2, RoundingMode.HALF_DOWN);
				if (storedBig.compareTo(oldBig) != 0) {
					log.warn("Rejected new grade because of concurrent edit: {} vs {}", storedBig, oldBig);
					return GradeSaveResponse.CONCURRENT_EDIT;
				}
			} catch (ParseException pe) {
				log.warn("Failed to parse adjusted grades in current locale");
			}
		}

		GradeSaveResponse rval = null;

		if (StringUtils.isNotBlank(newGradeAdjusted)) {
			final Double newGradePoints = FormatHelper.validateDouble(newGradeAdjusted);

			// if over limit, still save but return the warning
			if (newGradePoints != null && newGradePoints.compareTo(maxPoints) > 0) {
				log.debug("over limit. Max: {}", maxPoints);
				rval = GradeSaveResponse.OVER_LIMIT;
			}
		}

		// save
		try {
			// note, you must pass in the comment or it will be nulled out by the GB service
			// also, must pass in the raw grade as the service does conversions between percentage etc
			this.gradingService.saveGradeAndCommentForStudent(gradebook.getUid(), assignmentId, studentUuid,
					newGrade, comment);
			if (rval == null) {
				// if we don't have some other warning, it was all OK
				rval = GradeSaveResponse.OK;
			}
		} catch (InvalidGradeException | AssessmentNotFoundException e) {
			log.error("An error occurred saving the grade. {}: {}", e.getClass(), e.getMessage());
			rval = GradeSaveResponse.ERROR;
		}

		EventHelper.postUpdateGradeEvent(gradebook, assignmentId, studentUuid, newGrade, rval, getUserRoleOrNone());

		return rval;
	}

	public GradeSaveResponse saveGradesAndCommentsForImport(final Gradebook gradebook, final Assignment assignment, final List<GradeDefinition> gradeDefList) {
		if (gradebook == null) {
			return GradeSaveResponse.ERROR;
		}

		try {
			gradingService.saveGradesAndComments(gradebook.getUid(), assignment.getId(), gradeDefList);
			return GradeSaveResponse.OK;
		} catch (InvalidGradeException | AssessmentNotFoundException e) {
			log.error("An error occurred saving the grade. {}: {}", e.getClass(), e.getMessage());
			return GradeSaveResponse.ERROR;
		}
	}

	/**
	 *
	 * @param assignmentId
	 * @param studentUuid
	 * @param excuse
	 * @return
	 */
	public GradeSaveResponse saveExcuse(final Long assignmentId, final String studentUuid, final boolean excuse){
		final Gradebook gradebook = this.getGradebook();
		if (gradebook == null) {
			return GradeSaveResponse.ERROR;
		}

		// get current grade
		final String storedGrade = this.gradingService.getAssignmentScoreString(gradebook.getUid(), assignmentId,
				studentUuid);

		GradeSaveResponse rval = null;

		// save
		try {
			//must pass in the raw grade as the service does conversions between percentage etc
			this.gradingService.saveGradeAndExcuseForStudent(gradebook.getUid(), assignmentId, studentUuid,
					storedGrade, excuse);

			if (rval == null) {
				// if we don't have some other warning, it was all OK
				rval = GradeSaveResponse.OK;
			}
		} catch (InvalidGradeException | AssessmentNotFoundException e) {
			log.error("An error occurred saving the excuse. " + e.getClass() + ": " + e.getMessage());
			rval = GradeSaveResponse.ERROR;
		}
		return rval;
	}
	/**
	 * Build the matrix of assignments, students and grades for all students
	 *
	 * @param assignments list of assignments
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(final List<Assignment> assignments) throws GbException {
		return this.buildGradeMatrix(assignments, this.getGradeableUsers());
	}

	/**
	 * Build the matrix of assignments and grades for the given users. In general this is just one, as we use it for the instructor view
	 * student summary but could be more for paging etc
	 *
	 * @param assignments list of assignments
	 * @param studentUuids of uuids
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(final List<Assignment> assignments, final List<String> studentUuids) throws GbException {
		return this.buildGradeMatrix(assignments, studentUuids, null);
	}

	/**
	 * Build the matrix of assignments, students and grades for all students, with the specified sortOrder
	 *
	 * @param assignments list of assignments
	 * @param uiSettings the UI settings. Wraps sort order and group filter (sort = null for no sort, filter = null for all groups)
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(final List<Assignment> assignments,
			final GradebookUiSettings uiSettings) throws GbException {
		return this.buildGradeMatrix(assignments, this.getGradeableUsers(uiSettings.getGroupFilter()), uiSettings);
	}

	public HashMap<String, Boolean> buildHasAssociatedRubricMap(final List<Assignment> assignments) {

		HashMap<String, Boolean> map = new HashMap<>();
		for (Assignment assignment : assignments) {
			String externalAppName = assignment.getExternalAppName();
			if (assignment.getExternallyMaintained()) {
				String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignment.getExternalId()).reckon().getId();
				boolean hasAssociatedRubric = StringUtils.equals(externalAppName, AssignmentConstants.TOOL_ID) ? rubricsService.hasAssociatedRubric(externalAppName, assignmentId) : false;
				map.put(assignmentId, hasAssociatedRubric);
			} else {
				Long assignmentId = assignment.getId();
				boolean hasAssociatedRubric = rubricsService.hasAssociatedRubric(RubricsConstants.RBCS_TOOL_GRADEBOOKNG, assignmentId.toString());
				map.put(assignmentId.toString(), hasAssociatedRubric);
			}
		}
		return map;
	}

	/**
	 * Build the matrix of assignments and grades for the given users with the specified sort order
	 *
	 * @param assignments list of assignments
	 * @param studentUuids student uuids
	 * @param uiSettings the settings from the UI that wraps up preferences
	 * @return
	 *
	 * 		TODO refactor this into a hierarchical method structure
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(final List<Assignment> assignments,
			final List<String> studentUuids, final GradebookUiSettings uiSettings) throws GbException {

		// TODO move GradebookUISettings to business

		// settings could be null depending on constructor so it needs to be corrected
		final GradebookUiSettings settings = (uiSettings != null) ? uiSettings : new GradebookUiSettings();

		final GbStopWatch stopwatch = new GbStopWatch();
		stopwatch.start();
		stopwatch.timeWithContext("buildGradeMatrix", "buildGradeMatrix start", stopwatch.getTime());

		final Gradebook gradebook = this.getGradebook();
		if (gradebook == null) {
			return null;
		}
		stopwatch.timeWithContext("buildGradeMatrix", "getGradebook", stopwatch.getTime());

		// get current user
		final String currentUserUuid = getCurrentUser().getId();

		// get role for current user
		GbRole role;
		try {
			role = this.getUserRole();
		} catch (final GbAccessDeniedException e) {
			throw new GbException("Error getting role for current user", e);
		}

		final Site site = getCurrentSite().orElse(null);

		// get users
		final List<GbUser> gbStudents = getGbUsersForUiSettings(studentUuids, settings, site);
		stopwatch.timeWithContext("buildGradeMatrix", "sortUsers", stopwatch.getTime());

		// setup a map because we progressively build this up by adding grades to a student's entry
		final Map<String, GbStudentGradeInfo> matrix = new LinkedHashMap<>();

		// get course grades
		putCourseGradesInMatrix(matrix, gbStudents, studentUuids, gradebook, role, isCourseGradeVisible(currentUserUuid), settings);
		stopwatch.timeWithContext("buildGradeMatrix", "putCourseGradesInMatrix", stopwatch.getTime());

		// get assignments and categories
		putAssignmentsAndCategoryItemsInMatrix(matrix, gbStudents, studentUuids, assignments, gradebook, currentUserUuid, role, settings);
		stopwatch.timeWithContext("buildGradeMatrix", "putAssignmentsAndCategoryItemsInMatrix", stopwatch.getTime());

		// sorting
		List<GbStudentGradeInfo> items = sortGradeMatrix(matrix, settings);
		stopwatch.timeWithContext("buildGradeMatrix", "sortGradeMatrix", stopwatch.getTime());

		return items;
	}

	/**
	 * Build the matrix of assignments and grades for the Export process
	 *
	 * @param assignments list of assignments
	 * @param groupFilter
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrixForImportExport(final List<Assignment> assignments, GbGroup groupFilter) throws GbException {
		// ------------- Initialization -------------
		final GbStopWatch stopwatch = new GbStopWatch();
		stopwatch.start();
		stopwatch.timeWithContext("buildGradeMatrixForImportExport", "buildGradeMatrix start", stopwatch.getTime());

		final Gradebook gradebook = this.getGradebook();
		if (gradebook == null) {
			return Collections.EMPTY_LIST;
		}
		stopwatch.timeWithContext("buildGradeMatrixForImportExport", "getGradebook", stopwatch.getTime());

		// get current user
		final String currentUserUuid = getCurrentUser().getId();

		// get role for current user
		GbRole role;
		try {
			role = this.getUserRole();
		} catch (final GbAccessDeniedException e) {
			throw new GbException("Error getting role for current user", e);
		}

		final GradebookUiSettings settings = new GradebookUiSettings();

		// ------------- Get Users -------------
		final List<String> studentUUIDs = getGradeableUsers(groupFilter);
		final List<GbUser> gbStudents = getGbUsers(studentUUIDs);
		stopwatch.timeWithContext("buildGradeMatrixForImportExport", "getGbUsersForUiSettings", stopwatch.getTime());

		// ------------- Course Grades -------------
		final Map<String, GbStudentGradeInfo> matrix = new LinkedHashMap<>();
		putCourseGradesInMatrix(matrix, gbStudents, studentUUIDs, gradebook, role, isCourseGradeVisible(currentUserUuid), settings);
		stopwatch.timeWithContext("buildGradeMatrixForImportExport", "putCourseGradesInMatrix", stopwatch.getTime());

		// ------------- Assignments -------------
		putAssignmentsAndCategoryItemsInMatrix(matrix, gbStudents, studentUUIDs, assignments, gradebook, currentUserUuid, role, settings);
		stopwatch.timeWithContext("buildGradeMatrixForImportExport", "putAssignmentsAndCategoryItemsInMatrix", stopwatch.getTime());

		// ------------- Sorting -------------
		List<GbStudentGradeInfo> items = sortGradeMatrix(matrix, settings);
		stopwatch.timeWithContext("buildGradeMatrixForImportExport", "sortGradeMatrix", stopwatch.getTime());

		return items;
	}

	public List<GbGradeComparisonItem> buildMatrixForGradeComparison(Assignment assignment, GradeType gradingType, GradebookInformation settings){
		// Only return the list if the feature is activated
		boolean serverPropertyOn = serverConfigService.getConfig(
				SAK_PROP_ALLOW_STUDENTS_TO_COMPARE_GRADES,
				SAK_PROP_ALLOW_STUDENTS_TO_COMPARE_GRADES_DEFAULT
		);
		if (!serverPropertyOn) {
			return new ArrayList<>();
		}
		
		List<GbGradeComparisonItem> data;
		
		String userEid = getCurrentUser().getEid();
		
		boolean isComparingAndDisplayingFullName = settings
						.getComparingDisplayStudentNames() &&
				settings
						.getComparingDisplayStudentSurnames();

		boolean isComparingOrDisplayingFullName = settings
								.getComparingDisplayStudentNames() ||
						settings
								.getComparingDisplayStudentSurnames();

		// Add advisor to retrieve the grades as student
		SecurityAdvisor advisor = null;
		try {
			advisor = addSecurityAdvisor();
			data = buildGradeMatrix(Collections.singletonList(assignment))
					.stream().map(GbGradeComparisonItem::new)
					.map(el -> {
						if(isComparingOrDisplayingFullName){
							String studentDisplayName = String.format(
								"%s%s%s",
								settings.getComparingDisplayStudentNames() ? el.getStudentFirstName() : "",
								isComparingAndDisplayingFullName ? " " : "",
								settings.getComparingDisplayStudentSurnames()? el.getStudentLastName() : ""
							);
							el.setStudentDisplayName(studentDisplayName);
						}
						el.setIsCurrentUser(userEid.equals(el.getEid()));
						
						el.setGrade(FormatHelper.formatGrade(el.getGrade()) + (
							GradeType.PERCENTAGE == gradingType ? "%" : ""
						));
						return el;
					})
					.collect(Collectors.toList());
			
			if(settings.getComparingRandomizeDisplayedData()){
				Collections.shuffle(data);
			}
			return data;
		} finally {
			removeSecurityAdvisor(advisor);
		}
	}

	private Map<String, List<String>> getUserSections(String siteId) {

		final Map<String, List<String>> userSections = new HashMap<>();

		// First off, add the locally authored sections, ie: the sections internal to Sakai.

		for (CourseSection cs : sectionManager.getSections(siteId)) {
			for (EnrollmentRecord er : sectionManager.getSectionEnrollments(cs.getUuid())) {
				String userId = er.getUser().getUserUid();
				List<String> sections = userSections.get(userId);
				if (sections == null) {
					userSections.put(userId, new ArrayList<>(Arrays.asList(cs.getTitle())));
				} else {
					sections.add(cs.getTitle());
				}
			}
		}

		// Now add the sections coming in from external providers, ie: course management

		String[] sectionIds = null;

		try {
			sectionIds = groupProvider.unpackId(siteService.getSite(siteId).getProviderGroupId());
		} catch (IdUnusedException idue) {}

		if (sectionIds == null || sectionIds.length == 0) {
			log.debug("No section ids found for {}. Returning an empty map ...", siteId);
			return userSections;
		}

		for (String sectionId : sectionIds) {
			Section section = null;
			try {
				section = courseManagementService.getSection(sectionId);
			} catch (IdNotFoundException idNotFoundException) {}

			if (section == null) {
				log.debug("Section '{}'  not found, skipping ...", sectionId);
				continue;
			}

			EnrollmentSet enrollmentSet = section.getEnrollmentSet();

			Set<Membership> memberships = courseManagementService.getSectionMemberships(sectionId);

			if ((memberships == null || memberships.size() == 0) && enrollmentSet == null) {
				log.debug("Section '{}' does not have any direct memberships or enrollments. Skipping ...", sectionId);
				continue;
			}

			final Section finalSection = section;
			Consumer<String> collect = (userId) -> {
				List<String> sections = userSections.get(userId);
				if (sections == null) {
					userSections.put(userId, new ArrayList<>(Arrays.asList(finalSection.getTitle())));
				} else {
					sections.add(finalSection.getTitle());
				}
			};

			if (enrollmentSet != null) {
				Set<Enrollment> enrollments = courseManagementService.getEnrollments(enrollmentSet.getEid());
				enrollments.forEach(e -> collect.accept(e.getUserId()));
			}
			
			if (memberships != null) {
				memberships.forEach(m -> collect.accept(m.getUserId()));
			}
		}

		return userSections;
	}

	/**
	 * Gets a {@link List} of {@link GbUser} objects for the specified userUuids, sorting and filtering in accordance with any UI settings.
	 * @param userUuids
	 * @param settings
	 * @param site
	 * @return
	 */
	public List<GbUser> getGbUsersForUiSettings(List<String> userUuids, GradebookUiSettings settings, Site site) {

		Map<String, List<String>> userSections = getUserSections(site.getId());

		List<User> users = getUsers(userUuids);
		List<GbUser> gbUsers = new ArrayList<>(users.size());
		if (settings.getStudentSortOrder() != null) {
			Comparator<User> comp = GbStudentNameSortOrder.FIRST_NAME == settings.getNameSortOrder() ? new FirstNameComparator() : new UserSortNameComparator();
			if (SortDirection.DESCENDING == settings.getStudentSortOrder()) {
				comp = Collections.reverseOrder(comp);
			}

			Collections.sort(users, comp);
		} else if (getCandidateDetailProvider() != null && settings.getStudentNumberSortOrder() != null) {
			if (site != null) {
				Comparator<User> comp = new StudentNumberComparator(getCandidateDetailProvider(), site);
				if (SortDirection.DESCENDING.equals(settings.getStudentNumberSortOrder())) {
					comp = Collections.reverseOrder(comp);
				}

				Collections.sort(users, comp);
			}
		}

		for (User u : users) {
			gbUsers.add(new GbUser(u, getStudentNumber(u, site))
							.setSections(userSections.getOrDefault(u.getId(), Collections.emptyList())));
		}

		return gbUsers;
	}

	/**
	 * Adds course grade info into the matrix specified in the first param
	 * @param matrix mapping of student uids to GbStudentGradeInfo in which to store course grades
	 * @param gbStudents list of student for whom to retrieve course grades
	 * @param studentUuids list of student UUIDs so we don't have to parse the list of GbUsers to extract the values if we already have them
	 * @param gradebook current site's gradebook
	 * @param role current user's GbRole in the site
	 * @param isCourseGradeVisible whether the current user can see course grades in this site
	 * @param settings GradebookUiSettings instance
	 */
	public void putCourseGradesInMatrix(Map<String, GbStudentGradeInfo> matrix, List<GbUser> gbStudents, List<String> studentUuids, Gradebook gradebook, GbRole role,
											boolean isCourseGradeVisible, GradebookUiSettings settings) {
		// Get the course grades
		final Map<String, CourseGradeTransferBean> courseGrades = getCourseGrades(studentUuids);

		// Setup the course grade formatter
		// TODO we want the override except in certain cases. Can we hard code this?
		final CourseGradeFormatter courseGradeFormatter = Application.exists() ?
			new CourseGradeFormatter(gradebook, role, isCourseGradeVisible, settings.getShowPoints(), true, this.getShowCalculatedGrade()) :
			null;

		for (final GbUser student : gbStudents) {
			// Create and add the user info
			final GbStudentGradeInfo sg = new GbStudentGradeInfo(student);

			// Add the course grade, including the display
			String uid = student.getUserUuid();
			final CourseGradeTransferBean courseGrade = courseGrades.get(uid);
			final CourseGradeTransferBean gbCourseGrade = courseGrades.get(uid);
			if (courseGradeFormatter != null) {
				gbCourseGrade.setDisplayString(courseGradeFormatter.format(courseGrade));
			}
			sg.setCourseGrade(gbCourseGrade);
			sg.setHasCourseGradeComment(StringUtils.isNotBlank(getAssignmentGradeComment(getCurrentSiteId(),courseGrade.getId(),uid)));
			// Add to map so we can build on it later
			matrix.put(uid, sg);
		}
	}

	/**
	 * Builds up the matrix (a map<userUid, GbStudentGradeInfo>) for the specified students / assignments.a
	 * @param matrix output parameter; a map of studentUuids to GbStudentGradeInfo objects which will contain grade data for the specified assignments
	 * @param gbStudents list of GbUsers for whom to retrieve grading data
	 * @param studentUuids list of student UUIDs, so we don't have to extract out of gbStudents
	 * @param assignments the list of assignments for which to retrieve grading data. Computes category scores associated with these assignments as appropriate
	 * @param gradebook the gradebook containing the assignments, etc.
	 * @param currentUserUuid
	 * @param role the current user's role
	 * @param settings the GradebookUiSettings instance associated with the user's session; used to determine whether the context is anonymous. If null, all grading data will be retrieved without any anonymous aware filtering
	 */
	public void putAssignmentsAndCategoryItemsInMatrix(Map<String, GbStudentGradeInfo> matrix, List<GbUser> gbStudents, List<String> studentUuids, List<Assignment> assignments,
														Gradebook gradebook, String currentUserUuid, GbRole role, GradebookUiSettings settings) {

		// Ensure the matrix is populated with GbStudentGradeInfo instances for each student
		gbStudents.stream().forEach(gbStudent -> {
			String userUuid = gbStudent.getUserUuid();
			GbStudentGradeInfo info = matrix.get(userUuid);
			if (info == null) {
				matrix.put(userUuid, new GbStudentGradeInfo(gbStudent));
			}
		});

		// get categories. This call is filtered for TAs as well.
		final List<CategoryDefinition> categories = this.getGradebookCategories();

		// for TA's, build a lookup map of visible categoryIds so we can filter
		// the assignment list to not fetch grades
		// for assignments we don't have category level access to.
		// for everyone else this will just be an empty list that is unused
		final List<Long> categoryIds = new ArrayList<>();
		if (role == GbRole.TA) {
			for (final CategoryDefinition category : categories) {
				categoryIds.add(category.getId());
			}
		}

		// this holds a map of categoryId and the list of assignment ids in each
		// we build this whilst iterating below to save further iterations when
		// building the category list
		final Map<Long, Set<Long>> categoryAssignments = new TreeMap<>();

		// iterate over assignments and get the grades for each
		// note, the returned list only includes entries where there is a grade
		// for the user
		// we also build the category lookup map here
		for (final Assignment assignment : assignments) {

			final Long categoryId = assignment.getCategoryId();
			final Long assignmentId = assignment.getId();

			// TA permission check. If there are categories and they don't have
			// access to this one, skip it
			if (role == GbRole.TA) {
				log.debug("TA processing category: {}", categoryId);

				if (!categoryIds.isEmpty() && categoryId != null && !categoryIds.contains(categoryId)) {
					continue;
				}

				// TA stub out. So that we can support 'per grade' permissions for a
				// TA, we need a stub record for every student
				// This is because getGradesForStudentsForItem only returns records
				// where there is a grade (even if blank)
				// So this iteration for TAs allows the matrix to be fully
				// populated.
				// This is later updated to be a real grade enry if there is one.
				for (final GbUser student : gbStudents) {
					final GbStudentGradeInfo sg = matrix.get(student.getUserUuid());
					sg.addGrade(assignment.getId(), new GbGradeInfo(null));
				}
			}

			// build the category map (if assignment is categorised)
			if (categoryId != null) {
				Set<Long> values;
				if (categoryAssignments.containsKey(categoryId)) {
					values = categoryAssignments.get(categoryId);
					values.add(assignmentId);
				} else {
					values = new HashSet<>();
					values.add(assignmentId);
				}
				categoryAssignments.put(categoryId, values);
			}

			// get grades
			final List<GradeDefinition> defs = this.gradingService.getGradesForStudentsForItem(gradebook.getUid(), assignment.getId(), studentUuids);

			// iterate the definitions returned and update the record for each
			// student with the grades
			for (final GradeDefinition def : defs) {
				final GbStudentGradeInfo sg = matrix.get(def.getStudentUid());

				if (sg == null) {
					log.warn("No matrix entry seeded for: {}. This user may have been removed from the site", def.getStudentUid());
				} else {
					// this will overwrite the stub entry for the TA matrix if
					// need be
					sg.addGrade(assignment.getId(), new GbGradeInfo(def));
				}
			}
		}

		// build category columns
		for (final CategoryDefinition category : categories) {

			Long categoryId = category.getId();

			// use the category mappings for faster lookup of the assignmentIds
			// and grades in the category
			final Set<Long> categoryAssignmentIds = categoryAssignments.get(categoryId);

			// if there are no assignments in the category (ie its a new category) this will be null, so skip
			if (categoryAssignmentIds != null) {
				for (final GbUser student : gbStudents) {
					final GbStudentGradeInfo sg = matrix.get(student.getUserUuid());

					// get grades
					final Map<Long, GbGradeInfo> grades = sg.getGrades();

					// build map of just the grades we want
					final Map<Long, String> gradeMap = new HashMap<>();
					for (final Long assignmentId : categoryAssignmentIds) {
						final GbGradeInfo gradeInfo = grades.get(assignmentId);
						if (gradeInfo != null && !gradeInfo.isExcused()) {
								gradeMap.put(assignmentId, gradeInfo.getGrade());
						}
					}

					final Optional<CategoryScoreData> categoryScore = gradingService.calculateCategoryScore(gradebook,
							student.getUserUuid(), category, category.getAssignmentList(), gradeMap, (role == GbRole.TA || role == GbRole.INSTRUCTOR));
					categoryScore.ifPresent(data -> {
						for (Long item : gradeMap.keySet())	{
							if (data.droppedItems.contains(item)) {
								grades.get(item).setDroppedFromCategoryScore(true);
							}
						}
						// add to GbStudentGradeInfo
						sg.addCategoryAverage(category.getId(), data.score);
					});

					// TODO the TA permission check could reuse this iteration... check performance.
				}
			}
		}

		// for a TA, apply the permissions to each grade item to see if we can render it
		// the list of students, assignments and grades is already filtered to those that can be viewed
		// so we are only concerned with the gradeable permission
		if (role == GbRole.TA) {
			// get permissions
			final List<PermissionDefinition> permissions = getPermissionsForUser(currentUserUuid);

			log.debug("All permissions: {}", permissions.size());

			// only need to process this if some are defined
			// again only concerned with grade permission, so parse the list to
			// remove those that aren't GRADE
			permissions.removeIf(permission -> !StringUtils.equalsIgnoreCase(GraderPermission.GRADE.toString(), permission.getFunctionName()));

			log.debug("Filtered permissions: {}", permissions.size());

			// if we still have permissions, they will be of type grade, so we
			// need to enrich the students grades
			boolean allPermissions = false;
			if (!permissions.isEmpty()) {
				// first need a lookup map of assignment id to category, so we
				// can link up permissions by category
				final Map<Long, Long> assignmentCategoryMap = new HashMap<>();
				for (final Assignment assignment : assignments) {
					assignmentCategoryMap.put(assignment.getId(), assignment.getCategoryId());
				}

				// get the group membership for the students
				final Map<String, List<String>> groupMembershipsMap = getGroupMemberships();

				//Pair group <-> category
				Map <Long, List<String>> permByCat = new HashMap<>();

				for (final PermissionDefinition permission : permissions) {
					final Long permissionCategoryId = permission.getCategoryId() != null ? permission.getCategoryId() : -1L;
					final String permissionGroupReference = permission.getGroupReference() != null ? permission.getGroupReference() : ALL;

					//permissions over all categories and grades
					if (Long.valueOf(-1L).equals(permissionCategoryId) && ALL.equals(permissionGroupReference)) {
						allPermissions = true;
						break;
					}

					//By categories
					List<String> arr = permByCat.getOrDefault(permissionCategoryId, new ArrayList<String>());
					arr.add(permissionGroupReference);
					permByCat.put(permissionCategoryId, arr);
				}

				// for every student
				for (final GbUser student : gbStudents) {
					log.debug("Processing student: {}", student.getDisplayId());

					final GbStudentGradeInfo sg = matrix.get(student.getUserUuid());

					// get their assignment/grade list
					final Map<Long, GbGradeInfo> gradeMap = sg.getGrades();

					// for every assignment that has a grade
					for (final Map.Entry<Long, GbGradeInfo> entry : gradeMap.entrySet()) {
						// categoryId
						final Long gradeCategoryId = assignmentCategoryMap.get(entry.getKey());

						log.debug("Grade: {}", entry.getValue());

						// iterate the permissions
						// if category, compare the category,
						// then check the group and find the user in the group
						// if all ok, mark it as GRADEABLE

						boolean gradeable = false;

						//If TA does not have all the permissions over categories and groups
						if (!allPermissions) {

							//Check category and group permissions
							List<String> arr = permByCat.getOrDefault(gradeCategoryId, new ArrayList<String>());

							//add groups with permission in all categories
							Optional.ofNullable(permByCat.get(Long.valueOf(-1L))).ifPresent(arr::addAll);

							for (String group: arr) {
								List<String> members = groupMembershipsMap.get(group);

								//permissions over this category in all groups
								if ((group != null && ALL.equals(group)) ||
										(members != null && members.contains(student.getUserUuid()))) {
									gradeable = true;
									break;
								}
							}
						}

						// set the gradeable flag on this grade instance
						final GbGradeInfo gradeInfo = entry.getValue();
						gradeInfo.setGradeable(gradeable || allPermissions);
					}
				}
			}
		}
	}

	/**
	 * Builds up the matrix (a map<userUid, GbStudentGradeInfo>) for the specified students / assignments.a
	 * @param matrix output parameter; a map of studentUuids to GbStudentGradeInfo objects which will contain grade data for the specified assignments
	 * @param gbStudents list of GbUsers for whom to retrieve grading data
	 * @param studentUuids list of student UUIDs so we don't have to extract from GbUsers
	 * @param assignments the list of assignments for which to retrieve grading data. Computes category scores associated with these assignments as appropriate
	 * @param gradebook the gradebook containing the assignments, etc.
	 * @param currentUserUuid
	 * @param role the current user's role
	 */
	public void putAssignmentsInMatrixForExport(Map<String, GbStudentGradeInfo> matrix, List<GbUser> gbStudents, List<String> studentUuids, List<Assignment> assignments,
													Gradebook gradebook, String currentUserUuid, GbRole role) {
		// Collect list of studentUuids, and ensure the matrix is populated with GbStudentGradeInfo instances for each student
		gbStudents.stream().forEach(gbStudent -> {
			String userUuid = gbStudent.getUserUuid();
			GbStudentGradeInfo info = matrix.get(userUuid);
			if (info == null)
			{
				matrix.put(userUuid, new GbStudentGradeInfo(gbStudent));
			}
		});

		// iterate over assignments and get the grades for each
		// note, the returned list only includes entries where there is a grade
		// for the user
		// we also build the category lookup map here
		for (final Assignment assignment : assignments) {

			// get grades
			final List<GradeDefinition> defs = this.gradingService.getGradesForStudentsForItem(gradebook.getUid(), assignment.getId(), studentUuids);

			// iterate the definitions returned and update the record for each
			// student with the grades
			for (final GradeDefinition def : defs) {
				final GbStudentGradeInfo sg = matrix.get(def.getStudentUid());

				if (sg == null) {
					log.warn("No matrix entry seeded for: {}. This user may have been removed from the site", def.getStudentUid());
				} else {
					// this will overwrite the stub entry for the TA matrix if
					// need be
					sg.addGrade(assignment.getId(), new GbGradeInfo(def));
				}
			}
		}

		// for a TA, apply the permissions to each grade item to see if we can export it
		// the list of students, assignments and grades is already filtered to those that can be viewed
		// so we are only concerned with the gradeable permission
		if (role == GbRole.TA) {

			// get permissions
			final List<PermissionDefinition> permissions = getPermissionsForUser(currentUserUuid);

			log.debug("All permissions: {}", permissions.size());

			// only need to process this if some are defined
			// again only concerned with grade permission, so parse the list to
			// remove those that aren't GRADE
			permissions.removeIf(permission -> !StringUtils.equalsIgnoreCase(GraderPermission.GRADE.toString(), permission.getFunctionName()));

			log.debug("Filtered permissions: {}", permissions.size());

			// if we still have permissions, they will be of type grade, so we
			// need to enrich the students grades
			if (!permissions.isEmpty()) {

				// first need a lookup map of assignment id to category, so we
				// can link up permissions by category
				final Map<Long, Long> assignmentCategoryMap = new HashMap<>();
				for (final Assignment assignment : assignments) {
					assignmentCategoryMap.put(assignment.getId(), assignment.getCategoryId());
				}

				// get the group membership for the students
				final Map<String, List<String>> groupMembershipsMap = getGroupMemberships();

				// for every student
				for (final GbUser student : gbStudents) {
					log.debug("Processing student: {}", student.getDisplayId());

					final GbStudentGradeInfo sg = matrix.get(student.getUserUuid());

					// get their assignment/grade list
					final Map<Long, GbGradeInfo> gradeMap = sg.getGrades();

					// for every assignment that has a grade
					for (final Map.Entry<Long, GbGradeInfo> entry : gradeMap.entrySet()) {
						// categoryId
						final Long gradeCategoryId = assignmentCategoryMap.get(entry.getKey());

						log.debug("Grade: {}", entry.getValue());

						// iterate the permissions
						// if category, compare the category,
						// then check the group and find the user in the group
						// if all ok, mark it as GRADEABLE

						boolean gradeable = false;

						for (final PermissionDefinition permission : permissions) {
							// we know they are all GRADE so no need to check here

							boolean categoryOk = false;
							boolean groupOk = false;

							final Long permissionCategoryId = permission.getCategoryId();
							final String permissionGroupReference = permission.getGroupReference();

							log.debug("permissionCategoryId: {}", permissionCategoryId);
							log.debug("permissionGroupReference: {}", permissionGroupReference);

							// if permissions category is null (can grade all categories) or they match (can grade this category)
							if (permissionCategoryId == null || permissionCategoryId.equals(gradeCategoryId)) {
								categoryOk = true;
								log.debug("Category check passed");
							}

							// if group reference is null (can grade all groups) or group membership contains student (can grade this group)
							if (StringUtils.isBlank(permissionGroupReference)) {
								groupOk = true;
								log.debug("Group check passed #1");
							} else {
								final List<String> groupMembers = groupMembershipsMap.get(permissionGroupReference);
								log.debug("groupMembers: {}", groupMembers);

								if (groupMembers != null && groupMembers.contains(student.getUserUuid())) {
									groupOk = true;
									log.debug("Group check passed #2");
								}
							}

							if (categoryOk && groupOk) {
								gradeable = true;
								break;
							}
						}

						// set the gradeable flag on this grade instance
						final GbGradeInfo gradeInfo = entry.getValue();
						gradeInfo.setGradeable(gradeable);
					}
				}
			}
		}
	}

	/**
	 * Takes the value set of the matrix (a map<studentUuid, GbStudentGradeInfo>), and sorts the value set appropriately wrt the GradebookUiSettings
	 * @param matrix
	 * @param settings
	 * @return the valueSet of the matrix as an appropriately sorted List
	 */
	public List<GbStudentGradeInfo> sortGradeMatrix(Map<String, GbStudentGradeInfo> matrix, GradebookUiSettings settings) {

		// get the matrix as a list of GbStudentGradeInfo
		final List<GbStudentGradeInfo> items = new ArrayList<>(matrix.values());

		// sort the matrix based on the supplied assignment sort order (if any)
		if (settings.getAssignmentSortOrder() != null) {
			Comparator<GbStudentGradeInfo> comparator = new AssignmentGradeComparator(settings.getAssignmentSortOrder().getAssignmentId());
			final SortDirection direction = settings.getAssignmentSortOrder().getDirection();

			// reverse if required
			if (direction == SortDirection.DESCENDING) {
				comparator = Collections.reverseOrder(comparator);
			}

			// sort
			Collections.sort(items, comparator);
		}

		// sort the matrix based on the supplied category sort order (if any)
		if (settings.getCategorySortOrder() != null) {
			Comparator comparator = new CategorySubtotalComparator(settings.getCategorySortOrder().getCategoryId());
			final SortDirection direction = settings.getCategorySortOrder().getDirection();

			// reverse if required
			if (direction == SortDirection.DESCENDING) {
				comparator = Collections.reverseOrder(comparator);
			}

			// sort
			Collections.sort(items, comparator);
		}

		if (settings.getCourseGradeSortOrder() != null) {
			Comparator<GbStudentGradeInfo> comp = new CourseGradeComparator(getGradebookSettings());

			// reverse if required
			if (settings.getCourseGradeSortOrder() == SortDirection.DESCENDING) {
				comp = Collections.reverseOrder(comp);
			}

			// sort
			Collections.sort(items, comp);
		}

		return items;
	}

	/**
	 * Get a list of sections and groups in a site
	 *
	 * @return
	 */
	public List<GbGroup> getSiteSectionsAndGroups() {
		final String siteId = getCurrentSiteId();

		final List<GbGroup> rval = new ArrayList<>();

		GbRole role;
		try {
			role = this.getUserRole(siteId);
		} catch (final GbAccessDeniedException e) {
			log.warn("GbAccessDeniedException trying to getGradebookCategories", e);
			return rval;
		}

		// get groups (handles both groups and sections)
		try {
			final Site site = this.siteService.getSite(siteId);
			final Collection<Group> groups = isSuperUser() || role == GbRole.INSTRUCTOR ? site.getGroups() : site.getGroupsWithMember(userDirectoryService.getCurrentUser().getId());

			for (final Group group : groups) {
				rval.add(new GbGroup(group.getId(), group.getTitle(), group.getReference(), GbGroup.Type.GROUP));
			}

		} catch (final IdUnusedException e) {
			// essentially ignore and use what we have
			log.error("Error retrieving groups", e);
		}


		// if user is a TA, get the groups they can see and filter the GbGroup
		// list to keep just those
		if (role == GbRole.TA) {
			final Gradebook gradebook = this.getGradebook(siteId);
			final User user = getCurrentUser();
			boolean canGradeAll = false;

			// need list of all groups as REFERENCES (not ids)
			final List<String> allGroupIds = new ArrayList<>();
			for (final GbGroup group : rval) {
				allGroupIds.add(group.getReference());
			}

			// get the ones the TA can actually view
			// note that if a group is empty, it will not be included.
			List<String> viewableGroupIds = this.gradingPermissionService
					.getViewableGroupsForUser(gradebook.getId(), user.getId(), allGroupIds);

			if (viewableGroupIds == null) {
				viewableGroupIds = new ArrayList<>();
			}

			//FIXME: Another realms hack. The above method only returns groups from gb_permission_t. If this list is empty,
			//need to check realms to see if user has privilege to grade any groups. This is already done in 
			if (CollectionUtils.isEmpty(viewableGroupIds)) {
				List<PermissionDefinition> realmsPerms = this.getPermissionsForUser(user.getId());
				if (CollectionUtils.isNotEmpty(realmsPerms)) {
					for (PermissionDefinition permDef : realmsPerms) {
						if (permDef.getGroupReference() != null) {
							viewableGroupIds.add(permDef.getGroupReference());
						} else {
							canGradeAll = true;
						}
					}
				}
			}

			if (!canGradeAll) {
				// remove the ones that the user can't view
				final Iterator<GbGroup> iter = rval.iterator();
				while (iter.hasNext()) {
					final GbGroup group = iter.next();
					if (!viewableGroupIds.contains(group.getReference())) {
						iter.remove();
					}
				}
			}

		}

		Collections.sort(rval);

		return rval;
	}

	private List<GbGroup> getSiteSectionsAndGroups(final String siteId) {

		final List<GbGroup> rval = new ArrayList<>();

		// get groups (handles both groups and sections)
		try {
			final Site site = this.siteService.getSite(siteId);
			final Collection<Group> groups = site.getGroups();

			for (final Group group : groups) {
				rval.add(new GbGroup(group.getId(), group.getTitle(), group.getReference(), GbGroup.Type.GROUP));
			}

		} catch (final IdUnusedException e) {
			// essentially ignore and use what we have
			log.error("Error retrieving groups", e);
		}

		GbRole role;
		try {
			role = this.getUserRole(siteId);
		} catch (final GbAccessDeniedException e) {
			log.warn("GbAccessDeniedException trying to getGradebookCategories", e);
			return rval;
		}

		// if user is a TA, get the groups they can see and filter the GbGroup
		// list to keep just those
		if (role == GbRole.TA) {
			final Gradebook gradebook = this.getGradebook(siteId);
			final User user = getCurrentUser();

			// need list of all groups as REFERENCES (not ids)
			final List<String> allGroupIds = new ArrayList<>();
			for (final GbGroup group : rval) {
				allGroupIds.add(group.getReference());
			}

			// get the ones the TA can actually view
			// note that if a group is empty, it will not be included.
			List<String> viewableGroupIds = this.gradingPermissionService
					.getViewableGroupsForUser(gradebook.getId(), user.getId(), allGroupIds);

			//FIXME: Another realms hack. The above method only returns groups from gb_permission_t. If this list is empty,
			//need to check realms to see if user has privilege to grade any groups. This is already done in 
			if(CollectionUtils.isEmpty(viewableGroupIds)){
				List<PermissionDefinition> realmsPerms = this.getPermissionsForUser(user.getId(),siteId);
				if(CollectionUtils.isNotEmpty(realmsPerms)){
					for(PermissionDefinition permDef : realmsPerms){
						if(permDef.getGroupReference()!=null){
							viewableGroupIds.add(permDef.getGroupReference());
						}
					}
				}
			}

			// remove the ones that the user can't view
			final Iterator<GbGroup> iter = rval.iterator();
			while (iter.hasNext()) {
				final GbGroup group = iter.next();
				if (!viewableGroupIds.contains(group.getReference())) {
					iter.remove();
				}
			}

		}

		Collections.sort(rval);

		return rval;
	}

	/**
	 * Helper to get siteid. This will ONLY work in a portal site context, it will return null otherwise (ie via an entityprovider).
	 *
	 * @return
	 */
	public String getCurrentSiteId() {
		try {
			return this.toolManager.getCurrentPlacement().getContext();
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Helper to get site. This will ONLY work in a portal site context, it will return empty otherwise (ie via an entityprovider).
	 *
	 * @return
	 */
	public Optional<Site> getCurrentSite()
	{
		final String siteId = getCurrentSiteId();
		if (siteId != null)
		{
			try
			{
				return Optional.of(this.siteService.getSite(siteId));
			}
			catch (final IdUnusedException e)
			{
				// do nothing
			}
		}

		return Optional.empty();
	}

	/**
	 * Helper to get user
	 *
	 * @return
	 */
	public User getCurrentUser() {
		return this.userDirectoryService.getCurrentUser();
	}

	/**
	 * Determine if the current user is an admin user.
	 *
	 * @return true if the current user is admin, false otherwise.
	 */
	public boolean isSuperUser() {
		return this.securityService.isSuperUser();
	}

	/**
	 * Add a new assignment definition to the gradebook
	 *
	 * @param assignment
	 * @return id of the newly created assignment or null if there were any errors
	 */
	public Long addAssignment(final Assignment assignment) {

		final Gradebook gradebook = getGradebook();

		if (gradebook != null) {
			final String gradebookId = gradebook.getUid();

			final Long assignmentId = this.gradingService.addAssignment(gradebookId, assignment);

			// Force the assignment to sit at the end of the list
			if (assignment.getSortOrder() == null) {
				final List<Assignment> allAssignments = this.gradingService.getAssignments(gradebookId);
				int nextSortOrder = allAssignments.size();
				for (final Assignment anotherAssignment : allAssignments) {
					if (anotherAssignment.getSortOrder() != null && anotherAssignment.getSortOrder() >= nextSortOrder) {
						nextSortOrder = anotherAssignment.getSortOrder() + 1;
					}
				}
				updateAssignmentOrder(assignmentId, nextSortOrder);
			}

			// also update the categorized order
			updateAssignmentCategorizedOrder(gradebook.getUid(), assignment.getCategoryId(), assignmentId,
					Integer.MAX_VALUE);

			EventHelper.postAddAssignmentEvent(gradebook, assignmentId, assignment, getUserRoleOrNone());
			
            if (assignment.getReleased()) {
                String reference =  GradingConstants.REFERENCE_ROOT + Entity.SEPARATOR + "a" + Entity.SEPARATOR + getCurrentSiteId() + Entity.SEPARATOR + assignmentId;
                Task task = new Task();
                task.setSiteId(getCurrentSiteId());
                task.setReference(reference);
                task.setSystem(true);
                task.setDescription(assignment.getName());
                task.setDue((assignment.getDueDate() == null) ? null : assignment.getDueDate().toInstant());
                Set<String> users = new HashSet<>(this.getGradeableUsers());
                taskService.createTask(task, users, Priorities.HIGH);
            }
                        
			return assignmentId;

			// TODO wrap this so we can catch any runtime exceptions
		}
		return null;
	}

	/**
	 * Update the order of an assignment for the current site.
	 *
	 * @param assignmentId
	 * @param order
	 */
	public void updateAssignmentOrder(final long assignmentId, final int order) {

		final String siteId = getCurrentSiteId();
		this.updateAssignmentOrder(siteId, assignmentId, order);
	}

	/**
	 * Update the order of an assignment. If calling outside of GBNG, use this method as you can provide the site id.
	 *
	 * @param siteId the siteId
	 * @param assignmentId the assignment we are reordering
	 * @param order the new order
	 */
	public void updateAssignmentOrder(final String siteId, final long assignmentId, final int order) {

		final Gradebook gradebook = this.getGradebook(siteId);
		this.gradingService.updateAssignmentOrder(gradebook.getUid(), assignmentId, order);
	}

	/**
	 * Update the categorized order of an assignment.
	 *
	 * @param assignmentId the assignment we are reordering
	 * @param order the new order
	 * @throws IdUnusedException
	 * @throws PermissionException
	 */
	public void updateAssignmentCategorizedOrder(final long assignmentId, final int order)
			throws IdUnusedException, PermissionException {
		final String siteId = getCurrentSiteId();
		updateAssignmentCategorizedOrder(siteId, assignmentId, order);
	}

	/**
	 * Update the categorized order of an assignment.
	 *
	 * @param siteId the site's id
	 * @param assignmentId the assignment we are reordering
	 * @param order the new order
	 * @throws IdUnusedException
	 * @throws PermissionException
	 */
	public void updateAssignmentCategorizedOrder(final String siteId, final long assignmentId, final int order)
			throws IdUnusedException, PermissionException {

		// validate site
		try {
			this.siteService.getSite(siteId);
		} catch (final IdUnusedException e) {
			log.warn("IdUnusedException trying to updateAssignmentCategorizedOrder", e);
			return;
		}

		final Gradebook gradebook = (Gradebook) this.gradingService.getGradebook(siteId);

		if (gradebook == null) {
			log.error(String.format("Gradebook not in site %s", siteId));
			return;
		}

		final Assignment assignmentToMove = this.gradingService.getAssignment(gradebook.getUid(), assignmentId);

		if (assignmentToMove == null) {
			// TODO Handle assignment not in gradebook
			log.error(String.format("GradebookAssignment %d not in site %s", assignmentId, siteId));
			return;
		}

		updateAssignmentCategorizedOrder(gradebook.getUid(), assignmentToMove.getCategoryId(), assignmentToMove.getId(),
				order);
	}

	/**
	 * Update the categorized order of an assignment via the gradebook service.
	 *
	 * @param gradebookId the gradebook's id
	 * @param categoryId the id for the cataegory in which we are reordering
	 * @param assignmentId the assignment we are reordering
	 * @param order the new order
	 */
	private void updateAssignmentCategorizedOrder(final String gradebookId, final Long categoryId,
			final Long assignmentId, final int order) {
		this.gradingService.updateAssignmentCategorizedOrder(gradebookId, categoryId, assignmentId, order);
	}

	/**
	 * Get a list of edit events for this gradebook. Excludes any events for the current user
	 *
	 * @param gradebookUid the gradebook that we are interested in
	 * @param since the time to check for changes from
	 * @return
	 */
	public List<GbGradeCell> getEditingNotifications(final String gradebookUid, final Date since) {

		final User currentUser = getCurrentUser();

		final List<GbGradeCell> rval = new ArrayList<>();

		final List<Assignment> assignments = this.gradingService.getViewableAssignmentsForCurrentUser(gradebookUid,
				SortType.SORT_BY_SORTING);
        log.debug("Retrieved {} assignments", assignments.size());
		final List<Long> assignmentIds = assignments.stream().map(a -> a.getId()).collect(Collectors.toList());
		final List<GradingEvent> events = this.gradingService.getGradingEvents(assignmentIds, since);

		// keep a hash of all users so we don't have to hit the service each time
		final Map<String, GbUser> users = new HashMap<>();

		// filter out any events made by the current user
		for (final GradingEvent event : events) {
			if (!event.getGraderId().equals(currentUser.getId())) {
				// update cache (if required)
				users.putIfAbsent(event.getGraderId(), getUser(event.getGraderId()));

				// pull user from the cache
				final GbUser updatedBy = users.get(event.getGraderId());
				rval.add(
						new GbGradeCell(
								event.getStudentId(),
								event.getGradableObject().getId(),
								updatedBy.getDisplayName()));
			}
		}

		return rval;
	}

	/**
	 * Get an GradebookAssignment in the current site given the assignment id
	 *
	 * @param assignmentId
	 * @return
	 */
	public Assignment getAssignment(final long assignmentId) {
		return this.getAssignment(getCurrentSiteId(), assignmentId);
	}

	/**
	 * Get an GradebookAssignment in the specified site given the assignment id
	 *
	 * @param siteId
	 * @param assignmentId
	 * @return
	 */
	public Assignment getAssignment(final String siteId, final long assignmentId) {
		final Gradebook gradebook = getGradebook(siteId);
		if (gradebook != null) {
			return this.gradingService.getAssignment(gradebook.getUid(), assignmentId);
		}
		return null;
	}

	/**
	 * Get an GradebookAssignment in the current site given the assignment name This should be avoided where possible but is required for the import
	 * process to allow modification of assignment point values
	 *
	 * @param assignmentName
	 * @return
	 */
	public Assignment getAssignment(final String assignmentName) {
		return this.getAssignment(getCurrentSiteId(), assignmentName);
	}

	/**
	 * Get an GradebookAssignment in the specified site given the assignment name This should be avoided where possible but is required for the
	 * import process to allow modification of assignment point values
	 *
	 * @param siteId
	 * @param assignmentName
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public Assignment getAssignment(final String siteId, final String assignmentName) {
		final Gradebook gradebook = getGradebook(siteId);
		if (gradebook != null) {
			return this.gradingService.getAssignment(gradebook.getUid(), assignmentName);
		}
		return null;
	}

	/**
	 * Get the sort order of an assignment. If the assignment has a sort order, use that. Otherwise we determine the order of the assignment
	 * in the list of assignments
	 *
	 * This means that we can always determine the most current sort order for an assignment, even if the list has never been sorted.
	 *
	 *
	 * @param assignmentId
	 * @return sort order if set, or calculated, or -1 if cannot determine at all.
	 */
	public int getAssignmentSortOrder(final long assignmentId) {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		if (gradebook != null) {
			final Assignment assignment = this.gradingService.getAssignment(gradebook.getUid(), assignmentId);

			// if the assignment has a sort order, return that
			if (assignment.getSortOrder() != null) {
				return assignment.getSortOrder();
			}

			// otherwise we need to determine the assignment sort order within
			// the list of assignments
			final List<Assignment> assignments = this.getGradebookAssignments(siteId);

			for (int i = 0; i < assignments.size(); i++) {
				final Assignment a = assignments.get(i);
				if (assignmentId == a.getId() && a.getSortOrder() != null) {
					return a.getSortOrder();
				}
			}
		}

		return -1;
	}

	/**
	 * Update the details of an assignment
	 *
	 * @param assignment
	 * @return
	 */
	public void updateAssignment(final Assignment assignment) {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		// need the original name as the service needs that as the key...
		final Assignment original = this.getAssignment(assignment.getId());

		gradingService.updateAssignment(gradebook.getUid(), original.getId(), assignment);
		
		// Update task
		String reference =  GradingConstants.REFERENCE_ROOT + Entity.SEPARATOR + "a" + Entity.SEPARATOR + getCurrentSiteId() + Entity.SEPARATOR + original.getId();
		Optional<Task> optTask = taskService.getTask(reference);
		if (optTask.isPresent()) {
			Task task = optTask.get();
			task.setDescription(assignment.getName());
			task.setDue((assignment.getDueDate() == null) ? null : assignment.getDueDate().toInstant());
			taskService.saveTask(task);
		} else if(assignment.getReleased()) {
			// Create the task
			Task task = new Task();
			task.setSiteId(getCurrentSiteId());
			task.setReference(reference);
			task.setSystem(true);
			task.setDescription(assignment.getName());
			task.setDue((assignment.getDueDate() == null) ? null : assignment.getDueDate().toInstant());
			Set<String> users = new HashSet<>(this.getGradeableUsers());
			taskService.createTask(task, users, Priorities.HIGH);
		}
        
		EventHelper.postUpdateAssignmentEvent(gradebook, assignment, getUserRoleOrNone());

		if (original.getCategoryId() != null && assignment.getCategoryId() != null
				&& original.getCategoryId().longValue() != assignment.getCategoryId().longValue()) {
			updateAssignmentCategorizedOrder(gradebook.getUid(), assignment.getCategoryId(), assignment.getId(),
					Integer.MAX_VALUE);
		}
	}

	/**
	 * Updates ungraded items in the given assignment with the given grade
	 *
	 * @param assignmentId
	 * @param grade
	 * @return
	 */
	public boolean updateUngradedItems(final long assignmentId, final String grade) {
		return updateUngradedItems(assignmentId, grade, null);
	}

	/**
	 * Updates ungraded items in the given assignment for students within a particular group and with the given grade
	 *
	 * @param assignmentId
	 * @param grade
	 * @param group
	 * @return
	 */
	public boolean updateUngradedItems(final long assignmentId, final String grade, final GbGroup group) {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);
		final Assignment assignment = getAssignment(assignmentId);

		// get students
		final List<String> studentUuids = (group == null) ? this.getGradeableUsers() : this.getGradeableUsers(group);

		// get grades (only returns those where there is a grade, or comment; does not return those where there is no grade AND no comment)
		final List<GradeDefinition> defs = this.gradingService.getGradesForStudentsForItem(gradebook.getUid(), assignmentId, studentUuids);

		// Remove students who already have a grade
		studentUuids.removeIf(studentUUID -> defs.stream().anyMatch(def -> studentUUID.equals(def.getStudentUid()) && StringUtils.isNotBlank(def.getGrade())));
		defs.removeIf(def -> StringUtils.isNotBlank(def.getGrade()));

		// Create new GradeDefinition objects for those students who do not have one
		for (String studentUUID : studentUuids) {
			if (defs.stream().noneMatch(def -> studentUUID.equals(def.getStudentUid()))) {
				GradeDefinition def = new GradeDefinition();
				def.setStudentUid(studentUUID);
				def.setGradeEntryType(gradebook.getGradeType());
				def.setGradeReleased(gradebook.getAssignmentsDisplayed() && assignment.getReleased());
				defs.add(def);
			}
		}

		// Short circuit
		if (defs.isEmpty()) {
			log.debug("Setting default grade. No students are ungraded.");
		}

		// Apply the new grade to the GradeDefinitions to be updated
		for (GradeDefinition def : defs) {
			def.setGrade(grade);
			log.debug("Setting default grade. Values of assignmentId: {}, studentUuid: {}, grade: {}", assignmentId, def.getStudentUid(), grade);
		}

		// Batch update the GradeDefinitions, and post an event on completion
		try {
			gradingService.saveGradesAndComments(gradebook.getUid(), assignmentId, defs);
			EventHelper.postUpdateUngradedEvent(gradebook, assignmentId, String.valueOf(grade), getUserRoleOrNone());
			return true;
		} catch (final Exception e) {
			log.error("An error occurred updating the assignment", e);
		}

		return false;
	}

	/**
	 * Get the grade log for the given student and assignment
	 *
	 * @param studentUuid
	 * @param assignmentId
	 * @return
	 */
	public List<GbGradeLog> getGradeLog(final String studentUuid, final long assignmentId) {
		final List<GradingEvent> gradingEvents = this.gradingService.getGradingEvents(studentUuid, assignmentId);

		final List<GbGradeLog> rval = new ArrayList<>();
		for (final GradingEvent ge : gradingEvents) {
			rval.add(new GbGradeLog(ge));
		}

		return rval;
	}

	/**
	 * Get the user given a uuid
	 *
	 * @param userUuid
	 * @return GbUser or null if cannot be found
	 */
	public GbUser getUser(final String userUuid) {
		try {
			final User u = this.userDirectoryService.getUser(userUuid);
			return new GbUser(u);
		} catch (final UserNotDefinedException e) {
			return null;
		}
	}

	/**
	 * Get the comment for a given student assignment grade
	 *
	 * @param assignmentId id of assignment
	 * @param studentUuid uuid of student
	 * @return the comment or null if none
	 */
	public String getAssignmentGradeComment(final long assignmentId, final String studentUuid) {
		return getAssignmentGradeComment(getCurrentSiteId(), assignmentId, studentUuid);
	}

	/**
	 * Get the comment for a given student assignment grade
	 *
	 * @param siteId site id
	 * @param assignmentId id of assignment
	 * @param studentUuid uuid of student
	 * @return the comment or null if none
	 */
	public String getAssignmentGradeComment(final String siteId, final long assignmentId, final String studentUuid) {
		final Gradebook gradebook = getGradebook(siteId);

		try {
			final CommentDefinition def = this.gradingService.getAssignmentScoreComment(gradebook.getUid(),
					assignmentId, studentUuid);
			if (def != null) {
				return def.getCommentText();
			}
		} catch (AssessmentNotFoundException e) {
			log.error("An error occurred retrieving the comment. {}: {}", e.getClass(), e.getMessage());
		}
		return null;
	}

	public String getAssignmentExcuse(final long assignmentId, final String studentUuid){
		return getAssignmentExcuse(getCurrentSiteId(), assignmentId, studentUuid);
	}

	public String getAssignmentExcuse(final String siteId, final long assignmentId, final String studentUuid){
		final Gradebook gradebook = getGradebook(siteId);

		try{
			final boolean excuse = this.gradingService.getIsAssignmentExcused(gradebook.getUid(), assignmentId, studentUuid);
			if(excuse){
				return "1";
			}else{
				return "0";
			}
		} catch (AssessmentNotFoundException e) {
			log.error("An error occurred retrieving the excuse. " + e.getClass() + ": " + e.getMessage());
		}
		return null;
	}

	/**
	 * Update (or set) the comment for a student's assignment
	 *
	 * @param assignmentId id of assignment
	 * @param studentUuid uuid of student
	 * @param comment the comment
	 * @return true/false
	 */
	public boolean updateAssignmentGradeComment(final long assignmentId, final String studentUuid,
			final String comment) {

		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		try {
			// could do a check here to ensure we aren't overwriting someone
			// else's comment that has been updated in the interim...
			this.gradingService.setAssignmentScoreComment(gradebook.getUid(), assignmentId, studentUuid, comment);

			EventHelper.postUpdateCommentEvent(getGradebook(), assignmentId, studentUuid, comment, getUserRoleOrNone());

			return true;
		} catch (AssessmentNotFoundException | IllegalArgumentException e) {
			log.error("An error occurred saving the comment. {}: {}", e.getClass(), e.getMessage());
		}

		return false;
	}

	/**
	 * Get the role of the current user in the current site
	 *
	 * @return GbRole
	 * @throws GbAccessDeniedException if something goes wrong checking the site or user permissions
	 */
	public GbRole getUserRole() throws GbAccessDeniedException {
		final String siteId = getCurrentSiteId();
		return this.getUserRole(siteId);
	}

	/**
	 * Get the role of the current user in the given site
	 *
	 * @param siteId the siteId to check
	 * @return GbRole for the current user
	 * @throws GbAccessDeniedException if something goes wrong checking the site or user permissions
	 */
	public GbRole getUserRole(final String siteId) throws GbAccessDeniedException {

		final String userId = getCurrentUser().getId();

		String siteRef;
		try {
			siteRef = this.siteService.getSite(siteId).getReference();
		} catch (final IdUnusedException e) {
			throw new GbAccessDeniedException(e);
		}

		GbRole rval;

		if (this.securityService.unlock(userId, GbRole.INSTRUCTOR.getValue(), siteRef)) {
			rval = GbRole.INSTRUCTOR;
		} else if (this.securityService.unlock(userId, GbRole.TA.getValue(), siteRef)) {
			rval = GbRole.TA;
		} else if (this.securityService.unlock(userId, GbRole.STUDENT.getValue(), siteRef)) {
			rval = GbRole.STUDENT;
		} else {
			throw new GbAccessDeniedException("Current user does not have a valid section.role.x permission");
		}

		return rval;
	}

	/**
	 * Get the role of the current user in the given site or GbRole.NONE if the user does not have access
	 *
	 * @return GbRole for the current user
	 */
	public GbRole getUserRoleOrNone() {
		try {
			return getUserRole();
		} catch (GbAccessDeniedException e) {
			return GbRole.NONE;
		}
	}

	/**
	 * Get a map of grades for the given student. Safe to call when logged in as a student.
	 *
	 * @param studentUuid
	 * @return map of assignment to GbGradeInfo
	 */
	public Map<Long, GbGradeInfo> getGradesForStudent(final String studentUuid) {

		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		// will apply permissions and only return those the student can view
		final List<Assignment> assignments = getGradebookAssignmentsForStudent(studentUuid);

		final Map<Long, GbGradeInfo> rval = new LinkedHashMap<>();

		// iterate all assignments and get the grades
		// if student, only proceed if grades are released for the site
		// if instructor or TA, skip this check
		// permission checks are still applied at the assignment level in the
		// GradingService
		GbRole role;
		try {
			role = this.getUserRole(siteId);
		} catch (final GbAccessDeniedException e) {
			log.warn("GbAccessDeniedException trying to getGradesForStudent", e);
			return rval;
		}

		if (role == GbRole.STUDENT) {
			final boolean released = gradebook.getAssignmentsDisplayed();
			if (!released) {
				return rval;
			}
		}

		for (final Assignment assignment : assignments) {
			final GradeDefinition def = this.gradingService.getGradeDefinitionForStudentForItem(gradebook.getUid(),
					assignment.getId(), studentUuid);
			rval.put(assignment.getId(), new GbGradeInfo(def));
		}

		return rval;
	}

	public GradeDefinition getGradeForStudentForItem(String studentId, Long assignmentId) {
		return this.gradingService.getGradeDefinitionForStudentForItem(getCurrentSiteId(), assignmentId, studentId);
	}

	/**
	 * Get the category score for the given student.
	 *
	 * @param categoryId id of category
	 * @param studentUuid uuid of student
	 * @param isInstructor will calculate the category score with non-released items for instructors but not for students
	 * @return
	 */
	public Optional<CategoryScoreData> getCategoryScoreForStudent(final Long categoryId, final String studentUuid, final boolean isInstructor) {

		final Gradebook gradebook = getGradebook();

		final Optional<CategoryScoreData> result = gradingService.calculateCategoryScore(gradebook.getId(), studentUuid, categoryId, isInstructor, gradebook.getCategoryType(), null);
		log.debug("Category score for category: {}, student: {}:{}", categoryId, studentUuid, result.map(r -> r.score).orElse(null));

		return result;
	}

	/**
	 * Get the settings for this gradebook. Note that this CANNOT be called by an
	 * entityprovider
	 *
	 * @return
	 */
	public GradebookInformation getGradebookSettings() {
		return getGradebookSettings(getCurrentSiteId());
	}

	/**
	 * Get the settings for this gradebook. Safe to use from an entityprovider.
	 *
	 * @return
	 */
	public GradebookInformation getGradebookSettings(final String siteId) {
		final Gradebook gradebook = getGradebook(siteId);

		SecurityAdvisor advisor = null;
		try {
			advisor = addSecurityAdvisor();
			final GradebookInformation settings = this.gradingService.getGradebookInformation(gradebook.getUid());
			Collections.sort(settings.getCategories(), CategoryDefinition.orderComparator);
			return settings;
		} finally {
			removeSecurityAdvisor(advisor);
		}
	}

	/**
	 * Update the settings for this gradebook. Note that this CANNOT be called by a
	 * student.
	 *
	 * @param settings GradebookInformation settings
	 */
	public void updateGradebookSettings(final GradebookInformation settings) {

		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		this.gradingService.updateGradebookSettings(gradebook.getUid(), settings);

		EventHelper.postUpdateSettingsEvent(gradebook);
	}

	/**
	 * Remove an assignment from its gradebook
	 *
	 * @param assignmentId the id of the assignment to remove
	 */
	public void removeAssignment(final Long assignmentId) {

		// Delete task
		String reference =  GradingConstants.REFERENCE_ROOT + Entity.SEPARATOR + "a" + Entity.SEPARATOR + getCurrentSiteId() + Entity.SEPARATOR + assignmentId; 
		taskService.removeTaskByReference(reference);

		rubricsService.deleteRubricAssociationsByItemIdPrefix(assignmentId.toString(), RubricsConstants.RBCS_TOOL_GRADEBOOKNG);
		this.gradingService.removeAssignment(assignmentId);

		EventHelper.postDeleteAssignmentEvent(getGradebook(), assignmentId, getUserRoleOrNone());
	}

	/**
	 * Get a list of teaching assistants in the current site
	 *
	 * @return
	 */
	public List<GbUser> getTeachingAssistants() {

		final String siteId = getCurrentSiteId();
		final List<GbUser> rval = new ArrayList<>();

		try {
			final Set<String> userUuids = this.siteService.getSite(siteId).getUsersIsAllowed(GbRole.TA.getValue());
			for (final String userUuid : userUuids) {
				GbUser user = getUser(userUuid);
				if (user != null) {
					rval.add(getUser(userUuid));
				}
			}
		} catch (final IdUnusedException e) {
			log.warn("IdUnusedException trying to getTeachingAssistants", e);
		}

		return rval;
	}

	/**
	 * Get a list of permissions defined for the given user. Note: These are currently only defined/used for a teaching assistant.
	 *
	 * @param userUuid
	 * @return list of permissions or empty list if none
	 */
	public List<PermissionDefinition> getPermissionsForUser(final String userUuid) {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		List<PermissionDefinition> permissions = this.gradingPermissionService
				.getPermissionsForUser(gradebook.getUid(), userUuid);

		//if db permissions are null, check realms permissions.
		if (permissions == null || permissions.isEmpty()) {
			//This method should return empty arraylist if they have no realms perms
			permissions = this.gradingPermissionService.getRealmsPermissionsForUser(userUuid, siteId, Role.TA);
		}
		return permissions;
	}

	private List<PermissionDefinition> getPermissionsForUser(final String userUuid, String siteId) {

		if(siteId==null) {
			siteId = getCurrentSiteId();
		}
		
		final Gradebook gradebook = getGradebook(siteId);

		List<PermissionDefinition> permissions = this.gradingPermissionService
				.getPermissionsForUser(gradebook.getUid(), userUuid);

		//if db permissions are null, check realms permissions.
		if (permissions == null || permissions.isEmpty()) {
			//This method should return empty arraylist if they have no realms perms
			permissions = this.gradingPermissionService.getRealmsPermissionsForUser(userUuid, siteId, Role.TA);
		}
		return permissions;
	}

	/**
	 * Update the permissions for the user. Note: These are currently only defined/used for a teaching assistant.
	 *
	 * @param userUuid
	 * @param permissions
	 */
	public void updatePermissionsForUser(final String userUuid, final List<PermissionDefinition> permissions) {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		this.gradingPermissionService.updatePermissionsForUser(gradebook.getUid(), userUuid, permissions);
	}

	/**
	 * Remove all permissions for the user. Note: These are currently only defined/used for users with the Teaching Assistant role.
	 *
	 * @param userUuid
	 */
	public void clearPermissionsForUser(final String userUuid) {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);
		this.gradingPermissionService.clearPermissionsForUser(gradebook.getUid(), userUuid);
	}

	/**
	 * Check if the course grade is visible to the user
	 *
	 * For TA's, the students are already filtered by permission so the TA won't see those they don't have access to anyway However if there
	 * are permissions and the course grade checkbox is NOT checked, then they explicitly do not have access to the course grade. So this
	 * method checks if the TA has any permissions assigned for the site, and if one of them is the course grade permission, then they have
	 * access.
	 *
	 * @param userUuid user to check
	 * @return boolean
	 */
	public boolean isCourseGradeVisible(final String userUuid) {

		final String siteId = getCurrentSiteId();

		GbRole role;
		try {
			role = this.getUserRole(siteId);
		} catch (final GbAccessDeniedException e) {
			log.warn("GbAccessDeniedException trying to check isCourseGradeVisible", e);
			return false;
		}

		// if instructor, allowed
		if (role == GbRole.INSTRUCTOR) {
			return true;
		}

		// if TA, permission checks
		if (role == GbRole.TA) {

			// if no defs, implicitly allowed
			final List<PermissionDefinition> defs = getPermissionsForUser(userUuid);
			if (defs.isEmpty()) {
				return true;
			}

			// if defs and one is the view course grade, explicitly allowed
			for (final PermissionDefinition def : defs) {
				if (StringUtils.equalsIgnoreCase(def.getFunctionName(), GraderPermission.VIEW_COURSE_GRADE.toString())) {
					return true;
				}
			}
			return false;
		}

		// if student, check the settings
		// this could actually get the settings but it would be more processing
		if (role == GbRole.STUDENT) {
			final Gradebook gradebook = this.getGradebook(siteId);

			if (gradebook.getCourseGradeDisplayed()) {
				return true;
			}
		}

		// other roles not yet catered for, catch all.
		log.warn("User: {} does not have a valid Gradebook related role in site: {}", userUuid, siteId);
		return false;
	}

	/**
	 * Are student numbers visible to the current user in the current site?
	 *
	 * @return true if student numbers are visible
	 */
	public boolean isStudentNumberVisible()
	{
		if (getCandidateDetailProvider() == null) {
			return false;
		}

		final User user = getCurrentUser();
		final Optional<Site> site = getCurrentSite();
		return user != null && site.isPresent() && getCandidateDetailProvider().isInstitutionalNumericIdEnabled(site.get())
				&& this.gradingService.currentUserHasViewStudentNumbersPerm(getGradebook().getUid());
	}

	public String getStudentNumber(final User u, final Site site)
	{
		if (site == null || getCandidateDetailProvider() == null)
		{
			return "";
		}

		return getCandidateDetailProvider().getInstitutionalNumericId(u, site).orElse("");
	}

	/**
	 * Are there any sections in the current site?
	 */
	public boolean isSectionsVisible() {

		final Optional<Site> site = getCurrentSite();
		return site.isPresent() && !sectionManager.getSections(site.get().getId()).isEmpty();
	}

	/**
	 * Build a list of group references to site membership (as uuids) for the groups that are viewable for the current user.
	 *
	 * @return
	 */
	public Map<String, List<String>> getGroupMemberships() {

		final String siteId = getCurrentSiteId();

		Site site;
		try {
			site = this.siteService.getSite(siteId);
		} catch (final IdUnusedException e) {
			log.error("Error looking up site: {}", siteId, e);
			return null;
		}

		// filtered for the user
		final List<GbGroup> viewableGroups = getSiteSectionsAndGroups();

		final Map<String, List<String>> rval = new HashMap<>();

		for (final GbGroup gbGroup : viewableGroups) {
			final String groupReference = gbGroup.getReference();
			final List<String> memberUuids = new ArrayList<>();

			final Group group = site.getGroup(groupReference);
			if (group != null) {
				final Set<Member> members = group.getMembers();

				for (final Member m : members) {
					memberUuids.add(m.getUserId());
				}
			}

			rval.put(groupReference, memberUuids);

		}

		return rval;
	}

	/**
	 * Build a list of group references to site membership (as Member) for the groups that are viewable for the current user.
	 *
	 * @return
	 */
	private Map<String, Set<Member>> getGroupMembers(final String siteId) {

		Site site;
		try {
			site = this.siteService.getSite(siteId);
		} catch (final IdUnusedException e) {
			log.error("Error looking up site: {}", siteId, e);
			return null;
		}

		// filtered for the user
		final List<GbGroup> viewableGroups = getSiteSectionsAndGroups(siteId);

		final Map<String, Set<Member>> rval = new HashMap<>();

				
		for (final GbGroup gbGroup : viewableGroups) {
			final String groupReference = gbGroup.getReference();
			final Group group = site.getGroup(groupReference);
			if (group != null) {
				rval.put(groupReference, group.getMembers());
			}
		}

		return rval;
	}
	
	/**
	 * Have categories been enabled for the gradebook?
	 *
	 * @return if the gradebook is setup for either "Categories Only" or "Categories & Weighting"
	 */
	public boolean categoriesAreEnabled() {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		return GradingCategoryType.ONLY_CATEGORY == gradebook.getCategoryType()
				|| GradingCategoryType.WEIGHTED_CATEGORY == gradebook.getCategoryType();
	}

	/**
	 * Get the currently configured gradebook category type
	 *
	 * @return GradingCategoryType wrapper around the int value
	 */
	public GradingCategoryType getGradebookCategoryType() {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		return gradebook.getCategoryType();
	}

	/**
	 * Update the course grade (override) for this student
	 *
	 * @param studentUuid uuid of the student
	 * @param grade the new grade
	 * @return
	 */
	public boolean updateCourseGrade(final String studentUuid, final String grade, final String gradeScale) {

		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		try {
			gradingService.updateCourseGradeForStudent(gradebook.getUid(), studentUuid, grade, gradeScale);
			EventHelper.postOverrideCourseGradeEvent(gradebook, studentUuid, grade, grade != null);
			return true;
		} catch (final Exception e) {
			log.error("An error occurred saving the course grade. {}: {}", e.getClass(), e.getMessage());
		}

		return false;
	}

	/**
	 * Get the user's preferred locale from the Sakai resource loader
	 *
	 * @return
	 */
	public Locale getUserPreferredLocale() {
		final ResourceLoader rl = new ResourceLoader();
		return rl.getLocale();
	}

	/**
	 * Get the user's custom GbUiSettings from PreferencesService
	 *
	 * @return String
	 */
	public String getUserGbPreference(final String prefName) {
		final String siteId = getCurrentSiteId();
		final String currentUserId = getCurrentUser().getId();
		Preferences userPrefs = preferencesService.getPreferences(currentUserId);
		ResourceProperties rp = userPrefs.getProperties(GB_PREF_KEY + siteId);
		return rp.getProperty(prefName);
	}

	/**
	 * Set the user's custom GbUiSettings in PreferencesService
	 *
	 * @return
	 */
	public void setUserGbPreference(final String prefName, final String prefValue) {
		final String siteId = getCurrentSiteId();
		final String currentUserId = getCurrentUser().getId();
		PreferencesEdit prefsEdit = null;
		try {
			prefsEdit = preferencesService.edit(currentUserId);
		}
		catch (IdUnusedException e) {
			try {
				prefsEdit = preferencesService.add(currentUserId);
			} catch (PermissionException e1) {
				log.warn("setUserGbPreference PermissionException attempting to add prefs for user {}, prefName={}", currentUserId, prefName);
			} catch (IdUsedException e1) {
				log.warn("setUserGbPreference IdUsedException attempting to add prefs for user {}, prefName={}", currentUserId, prefName);
			}
		} catch (PermissionException e) {
			log.warn("setUserGbPreference PermissionException attempting to edit prefs for user {}, prefName={}", currentUserId, prefName);
		} catch (InUseException e) {
			log.warn("setUserGbPreference InUseException attempting to edit prefs for user {}, prefName={}", currentUserId, prefName);
		}
		ResourcePropertiesEdit props = prefsEdit.getPropertiesEdit(GB_PREF_KEY + siteId);
		props.addProperty(prefName, prefValue);
		preferencesService.commit(prefsEdit);
	}

	/**
	 * Helper to check if a user is roleswapped
	 *
	 * @return true if ja, false if nay.
	 */
	public boolean isUserRoleSwapped() {
		try {
			return this.securityService.isUserRoleSwapped();
		} catch (final IdUnusedException e) {
			// something has happened between getting the siteId and getting the site.
			throw new GbException("An error occurred checking some bits and pieces, please try again.", e);
		}
	}

	/**
	 * Check if current user has "gradebook.editAssignments" permission 
	 *
	 * @return true if yes, false if no.
	 */
	public boolean isUserAbleToEditAssessments(){
		String siteRef;
		
		try {
			siteRef = this.siteService.getSite(getCurrentSiteId()).getReference();
		} catch (final IdUnusedException e) {
			throw new GbException(e);
		}
		
		return this.securityService.unlock("gradebook.editAssignments", siteRef);
	}

	/**
	 * Returns true if the given grade is numeric and meets the gradebook requirements (10 digits/2 decimal places max)
	 * @param grade the grade to be validated, expected to be numeric
	 * @return true if the grade is numeric and meets the gradebook requirements
	 */
	public boolean isValidNumericGrade(String grade)
	{
		return gradingService.isValidNumericGrade(grade);
	}

	/**
	 * Helper to determine the icon class to use depending on the assignment external source
	 *
	 * @param assignment
	 * @return
	 */
	public String getIconClass(final Assignment assignment) {
		final String externalAppName = assignment.getExternalAppName();
		String iconClass;
		switch (externalAppName) {
			case AssignmentConstants.TOOL_ID:
				iconClass = getAssignmentsIconClass();
				break;
			case "sakai.samigo":
				iconClass = getSamigoIconClass();
				break;
			case "sakai.lessonbuildertool":
				iconClass = getLessonBuilderIconClass();
				break;
			case "sakai.attendance":
				iconClass = getAttendanceIconClass();
				break;
			default:
				iconClass = getDefaultIconClass();
				break;
		}
		return iconClass;
	}

	/**
	 * Helper to determine the icon class for possible external app names
	 *
	 * @return
	 */
	public Map<String, String> getIconClassMap() {
		final Map<String, String> mapping = new HashMap<>();

		mapping.put(AssignmentConstants.TOOL_ID, getAssignmentsIconClass());
		mapping.put("sakai.samigo", getSamigoIconClass());
		mapping.put("sakai.lessonbuildertool", getLessonBuilderIconClass());
		mapping.put("sakai.attendance", getAttendanceIconClass());

		return mapping;
	}

	public String getDefaultIconClass() {
		return ICON_SAKAI + "default-tool fa fa-globe";
	}

	private String getAssignmentsIconClass() {
		return ICON_SAKAI + "sakai-assignment-grades";
	}

	private String getSamigoIconClass() {
		return ICON_SAKAI + "sakai-samigo";
	}

	private String getLessonBuilderIconClass() {
		return ICON_SAKAI + "sakai-lessonbuildertool";
	}

	private String getAttendanceIconClass() {
		return ICON_SAKAI + "sakai-attendance";
	}

	/**
	 * Gets a list of assignment averages for a category.
	 * @param category category
	 * @param group group of students
	 * @return allAssignmentGrades list of assignment averages for a specific group
	 */
	public List<Double> getCategoryAssignmentTotals(CategoryDefinition category, GbGroup group){
		final List<Double> allAssignmentGrades = new ArrayList<>();
		final List<String> groupUsers = getGradeableUsers(group);
		final List<String> studentUUIDs = new ArrayList<>();
		studentUUIDs.addAll(groupUsers);
		final List<Assignment> assignments = category.getAssignmentList();
		final List<GbStudentGradeInfo> grades = buildGradeMatrix(assignments, studentUUIDs);
		for (final Assignment assignment : assignments) {
			if (assignment != null) {
				final List<Double> allGrades = new ArrayList<>();
				for (int j = 0; j < grades.size(); j++) {
					final GbStudentGradeInfo studentGradeInfo = grades.get(j);
					final Map<Long, GbGradeInfo> studentGrades = studentGradeInfo.getGrades();
					final GbGradeInfo grade = studentGrades.get(assignment.getId());
					if (grade != null && grade.getGrade() != null) {
						allGrades.add(Double.valueOf(grade.getGrade()));
					}
				}
				if (grades.size() > 0) {
					if (!assignment.getExtraCredit()) {
						if (allGrades.size() > 0) {
							allAssignmentGrades.add((calculateAverage(allGrades) / assignment.getPoints()) * 100);
						}
					}
				}
			}
		}
		return allAssignmentGrades;
	}

	/**
	 * Calculates the average grade for an assignment
	 * @param allGrades list of grades
	 * @return the average of the grades
	 */
	public double calculateAverage(final List<Double> allGrades) {
		return allGrades.stream().reduce(0D, (sub, el) -> sub + el.doubleValue()) / allGrades.size();
	}

	// Return a CandidateDetailProvider or null if it's not enabled
	private CandidateDetailProvider getCandidateDetailProvider() {
		return (CandidateDetailProvider)ComponentManager.get("org.sakaiproject.user.api.CandidateDetailProvider");
	}

	/**
	 * Add advisor as allowed.
	 *
	 * @return
	 */
	public SecurityAdvisor addSecurityAdvisor() {
		final SecurityAdvisor advisor = (final String userId, final String function, final String reference) -> SecurityAdvice.ALLOWED;
		this.securityService.pushAdvisor(advisor);
		return advisor;
	}

	/**
	 * Remove advisor
	 *
	 * @param advisor
	 */
	public void removeSecurityAdvisor(final SecurityAdvisor advisor) {
		this.securityService.popAdvisor(advisor);
	}

	public boolean getShowCalculatedGrade() {
		return  this.serverConfigService.getBoolean("gradebook.coursegrade.showCalculatedGrade", true) ;
	}

	/**
	 * Get the date and time formatted via the UserTimeService
	 * @param dateGraded
	 * @return
	 */
	public String formatDateTime(Date dateTime) {
		return userTimeService.dateTimeFormat(dateTime, getUserPreferredLocale(), DateFormat.SHORT);
	}


	/**
	 * Get the date formatted by the UserTimeService
	 * @param date
	 * @param ifNull string to return if date is null
	 * @return
	 */
	public String formatDate(Date date, final String ifNull) {
		if (date == null) {
			return ifNull;
		}

		return userTimeService.dateFormat(date, getUserPreferredLocale(), DateFormat.SHORT);
	}

	/**
	 * Get the tool title in the current user language
	 * @param externalAppId tool id
	 * @return a tool title in user's current language
	 */
	public String getExternalAppName(String externalAppId) {
		Tool externalTool = toolManager.getTool(externalAppId);
		return externalTool != null ? externalTool.getTitle() : externalAppId;
	}

	public String getExternalSubmissionId(String externalId, String userId) {

		String assignmentId = AssignmentReferenceReckoner.reckoner().reference(externalId).reckon().getId();

		try {
			AssignmentSubmission as = assignmentService.getSubmission(assignmentId, userId);

			if (as == null) {
				throw new IllegalArgumentException("No submission for external id " + externalId + " and user " + userId);
			}

			return as.getId();
		} catch (Exception e) {
			log.error("Exception while getting external submission: {}", e.toString());
			return "";
		}
	}
}
