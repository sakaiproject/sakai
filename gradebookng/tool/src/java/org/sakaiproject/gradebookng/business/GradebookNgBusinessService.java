package org.sakaiproject.gradebookng.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.StopWatch;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gradebookng.business.dto.AssignmentOrder;
import org.sakaiproject.gradebookng.business.exception.GbException;
import org.sakaiproject.gradebookng.business.model.GbAssignmentGradeSortOrder;
import org.sakaiproject.gradebookng.business.model.GbCategoryAverageSortOrder;
import org.sakaiproject.gradebookng.business.model.GbGradeCell;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbGradeLog;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.util.Temp;
import org.sakaiproject.gradebookng.business.util.XmlList;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GraderPermission;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

/**
 * Business service for GradebookNG
 *
 * This is not designed to be consumed outside of the application or supplied entityproviders. Use at your own risk.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

// TODO add permission checks! Remove logic from entityprovider if there is a double up
// TODO some of these methods pass in empty lists and its confusing. If we aren't doing paging, remove this.

@CommonsLog
public class GradebookNgBusinessService {

	@Setter
	private SiteService siteService;

	@Setter
	private UserDirectoryService userDirectoryService;

	@Setter
	private ToolManager toolManager;

	@Setter
	private GradebookService gradebookService;

	@Setter
	private GradebookPermissionService gradebookPermissionService;

	@Setter
	private GradebookFrameworkService gradebookFrameworkService;

	@Setter
	private CourseManagementService courseManagementService;

	@Setter
	private MemoryService memoryService;

	@Setter
	private SecurityService securityService;

	public static final String ASSIGNMENT_ORDER_PROP = "gbng_assignment_order";

	private Cache cache;
	private static final String NOTIFICATIONS_CACHE_NAME = "org.sakaiproject.gradebookng.cache.notifications";

	@SuppressWarnings("unchecked")
	public void init() {

		// max entries unbounded, no TTL eviction (TODO set this to 10 seconds?), TTI 10 seconds
		// TODO this should be configured in sakai.properties so we dont have redundant config code here
		this.cache = this.memoryService.getCache(NOTIFICATIONS_CACHE_NAME);
		if (this.cache == null) {
			this.cache = this.memoryService.createCache("org.sakaiproject.gradebookng.cache.notifications", null);
		}
	}

	/**
	 * Get a list of all users in the current site that can have grades
	 *
	 * @return a list of users as uuids or null if none
	 */
	private List<String> getGradeableUsers() {
		return this.getGradeableUsers(null);
	}

	/**
	 * Get a list of all users in the current site, filtered by the given group, that can have grades
	 *
	 * @param groupFilter GbGroupType to filter on
	 *
	 * @return a list of users as uuids or null if none
	 */
	private List<String> getGradeableUsers(final GbGroup groupFilter) {

		try {
			final String siteId = getCurrentSiteId();

			// note that this list MUST exclude TAs as it is checked in the GradebookService and will throw a SecurityException if invalid
			// users are provided
			final Set<String> userUuids = this.siteService.getSite(siteId).getUsersIsAllowed(GbRole.STUDENT.getValue());

			// filter the allowed list based on membership
			if (groupFilter != null && groupFilter.getType() != GbGroup.Type.ALL) {

				final Set<String> groupMembers = new HashSet<>();

				/*
				 * groups handles both if(groupFilter.getType() == GbGroup.Type.SECTION) { Set<Membership> members =
				 * this.courseManagementService.getSectionMemberships(groupFilter.getId()); for(Membership m: members) {
				 * if(userUuids.contains(m.getUserId())) { groupMembers.add(m.getUserId()); } } }
				 */

				if (groupFilter.getType() == GbGroup.Type.GROUP) {
					final Set<Member> members = this.siteService.getSite(siteId).getGroup(groupFilter.getId()).getMembers();
					for (final Member m : members) {
						if (userUuids.contains(m.getUserId())) {
							groupMembers.add(m.getUserId());
						}
					}
				}

				// only keep the ones we identified in the group
				userUuids.retainAll(groupMembers);
			}

			// if TA, pass it through the gradebook permissions (only if there are permissions)
			if (this.getUserRole(siteId) == GbRole.TA) {
				final User user = getCurrentUser();

				// if there are permissions, pass it through them
				// don't need to test TA access if no permissions
				final List<PermissionDefinition> perms = getPermissionsForUser(user.getId());
				if (!perms.isEmpty()) {

					final Gradebook gradebook = this.getGradebook(siteId);

					// get list of sections and groups this TA has access to
					final List courseSections = this.gradebookService.getViewableSections(gradebook.getUid());

					// get viewable students.
					final List<String> viewableStudents = this.gradebookPermissionService.getViewableStudentsForUser(gradebook.getUid(),
							user.getId(), new ArrayList<>(userUuids), courseSections);

					if (viewableStudents != null) {
						userUuids.retainAll(viewableStudents); // retain only those that are visible to this TA
					} else {
						userUuids.clear(); // TA can't view anyone
					}
				}
			}

			return new ArrayList<>(userUuids);

		} catch (final IdUnusedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Given a list of uuids, get a list of Users
	 *
	 * @param userUuids list of user uuids
	 * @return
	 */
	private List<User> getUsers(final List<String> userUuids) throws GbException {
		try {
			final List<User> users = this.userDirectoryService.getUsers(userUuids);
			Collections.sort(users, new LastNameComparator()); // default sort
			return users;
		} catch (final RuntimeException e) {
			// an LDAP exception can sometimes be thrown here, catch and rethrow
			throw new GbException("An error occurred getting the list of users.", e);
		}
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
	private Gradebook getGradebook(final String siteId) {
		try {
			final Gradebook gradebook = (Gradebook) this.gradebookService.getGradebook(siteId);
			return gradebook;
		} catch (final GradebookNotFoundException e) {
			log.error("No gradebook in site: " + siteId);
			return null;
		}
	}


	/**
	 * Get a list of assignments in the gradebook in the current site that the current user is allowed to access
	 *
	 * @return a list of assignments or null if no gradebook
	 */
	public List<Assignment> getGradebookAssignments() {
		return getGradebookAssignments(getCurrentSiteId(), SortType.SORT_BY_SORTING);
	}


	/**
	 * Get a list of assignments in the gradebook in the current site that the current user is allowed to access
	 *
	 * @return a list of assignments or null if no gradebook
	 */
	public List<Assignment> getGradebookAssignments(final String siteId) {
		return getGradebookAssignments(siteId, SortType.SORT_BY_SORTING);
	}


	/**
	 * Get a list of assignments in the gradebook in the current site that the current user is allowed to access
	 * sorted by the provided SortType
	 *
	 * @return a list of assignments or null if no gradebook
	 */
	public List<Assignment> getGradebookAssignments(final SortType sortBy) {
		return getGradebookAssignments(getCurrentSiteId(), sortBy);
	}


	/**
	 * Get a list of assignments in the gradebook in the specified site that the current user is allowed to access, sorted by sort order
	 *
	 * @param siteId the siteId
	 * @return a list of assignments or null if no gradebook
	 */
	public List<Assignment> getGradebookAssignments(final String siteId, final SortType sortBy) {
		final Gradebook gradebook = getGradebook(siteId);
		if (gradebook != null) {
			// applies permissions (both student and TA) and default sort is SORT_BY_SORTING
			return this.gradebookService.getViewableAssignmentsForCurrentUser(gradebook.getUid(), sortBy);
		}
		return null;
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

		if (gradebook != null) {
			rval = this.gradebookService.getCategoryDefinitions(gradebook.getUid());
		}

		// filter for TAs
		if (this.getUserRole(siteId) == GbRole.TA) {
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
			final List<Long> viewableCategoryIds = this.gradebookPermissionService.getCategoriesForUser(gradebook.getId(), user.getId(),
					allCategoryIds);

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
	 * Get a map of course grades for all users in the site. key = student eid value = course grade
	 *
	 * Note that this map is keyed on EID. Since the business service does not have a list of eids, to save an iteration, the calling
	 * service needs to do the filtering
	 *
	 * @param userUuids
	 * @return the map of course grades for students, or an empty map
	 */
	public Map<String, String> getSiteCourseGrades() {

		Map<String, String> rval = new HashMap<>();

		final Gradebook gradebook = this.getGradebook();
		if (gradebook != null) {

			// get course grades. THis new method for Sakai 11 does the override automatically, so GB1 data is preserved
			rval = this.gradebookService.getImportCourseGrade(gradebook.getUid());

		}
		return rval;
	}

	/**
	 * Get the course grade for a student
	 *
	 * @param studentUuid
	 * @return coursegrade. May have null fields if the coursegrade has not been released
	 */
	public CourseGrade getCourseGrade(final String studentUuid) {

		final Gradebook gradebook = this.getGradebook();
		final CourseGrade rval = this.gradebookService.getCourseGradeForStudent(gradebook.getUid(), studentUuid);
		return rval;
	}

	/**
	 * Save the grade and comment for a student's assignment. Ignores the concurrency check.
	 *
	 * @param assignmentId id of the gradebook assignment
	 * @param studentUuid uuid of the user
	 * @param grade grade for the assignment/user
	 * @param comment optional comment for the grade. Can be null.
	 *
	 * @return
	 */
	public GradeSaveResponse saveGrade(final Long assignmentId, final String studentUuid, final String grade, final String comment) {

		final Gradebook gradebook = this.getGradebook();
		if (gradebook == null) {
			return GradeSaveResponse.ERROR;
		}

		return this.saveGrade(assignmentId, studentUuid, null, grade, comment);
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
	public GradeSaveResponse saveGrade(final Long assignmentId, final String studentUuid, final String oldGrade, final String newGrade,
			final String comment) {

		final Gradebook gradebook = this.getGradebook();
		if (gradebook == null) {
			return GradeSaveResponse.ERROR;
		}

		// get current grade
		final String storedGrade = this.gradebookService.getAssignmentScoreString(gradebook.getUid(), assignmentId, studentUuid);

		// trim the .0 from the grades if present. UI removes it so lets standardise.
		String processedStoredGrade = StringUtils.removeEnd(storedGrade, ".0");
		String processedOldGrade = StringUtils.removeEnd(oldGrade, ".0");
		String processedNewGrade = StringUtils.removeEnd(newGrade, ".0");

		// trim to null so we can better compare against no previous grade being recorded (as it will be null)
		// note that we also trim newGrade so that don't add the grade if the new grade is blank and there was no grade previously
		processedStoredGrade = StringUtils.trimToNull(processedStoredGrade);
		processedOldGrade = StringUtils.trimToNull(processedOldGrade);
		processedNewGrade = StringUtils.trimToNull(processedNewGrade);

		if (log.isDebugEnabled()) {
			log.debug("storedGrade: " + processedStoredGrade);
			log.debug("oldGrade: " + processedOldGrade);
			log.debug("newGrade: " + processedNewGrade);
		}

		// if comment longer than 500 chars, error.
		// the field is a CLOB, probably by mistake. Loading this field up may cause performance issues
		// see SAK-29595
		if (StringUtils.length(comment) > 500) {
			log.error("Comment too long. Maximum 500 characters.");
			return GradeSaveResponse.ERROR;
		}

		// over limit check, get max points for assignment and check if the newGrade is over limit
		// we still save it but we return the warning
		final Assignment assignment = this.getAssignment(assignmentId);
		final Double maxPoints = assignment.getPoints();

		// no change
		if (StringUtils.equals(processedStoredGrade, processedNewGrade)) {
			final Double storedGradePoints = NumberUtils.toDouble(processedStoredGrade);
			if (storedGradePoints.compareTo(maxPoints) > 0) {
				return GradeSaveResponse.OVER_LIMIT;
			} else {
				return GradeSaveResponse.NO_CHANGE;
			}
		}

		// concurrency check, if stored grade != old grade that was passed in, someone else has edited.
		// if oldGrade == null, ignore concurrency check
		if (oldGrade != null && !StringUtils.equals(processedStoredGrade, processedOldGrade)) {
			return GradeSaveResponse.CONCURRENT_EDIT;
		}

		// about to edit so push a notification
		pushEditingNotification(gradebook.getUid(), getCurrentUser(), studentUuid, assignmentId);

		GradeSaveResponse rval = null;

		if (StringUtils.isNotBlank(newGrade)) {
			final Double newGradePoints = NumberUtils.toDouble(processedNewGrade);

			if (newGradePoints.compareTo(maxPoints) > 0) {
				log.debug("over limit. Max: " + maxPoints);
				rval = GradeSaveResponse.OVER_LIMIT;
			}
		}

		// save
		try {
			// note, you must pass in the comment or it wil lbe nulled out by the GB service
			this.gradebookService.saveGradeAndCommentForStudent(gradebook.getUid(), assignmentId, studentUuid, processedNewGrade, comment);
			if (rval == null) {
				// if we don't have some other warning, it was all OK
				rval = GradeSaveResponse.OK;
			}
		} catch (InvalidGradeException | GradebookNotFoundException | AssessmentNotFoundException e) {
			log.error("An error occurred saving the grade. " + e.getClass() + ": " + e.getMessage());
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
	 * @param list of uuids
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(final List<Assignment> assignments, final List<String> studentUuids)
			throws GbException {
		return this.buildGradeMatrix(assignments, studentUuids, null, null, null);
	}

	/**
	 * Build the matrix of assignments, students and grades for all students, with the specified sortOrder
	 *
	 * @param assignments list of assignments
	 * @param assignmentSortOrder the assignment sort order
	 * @param nameSortOrder name sort order
	 * @param categorySortOrder the category subtotal sort order
	 * @param groupFilter if a specific group has been selected (null for all groups)
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(final List<Assignment> assignments,
			final GbAssignmentGradeSortOrder assignmentSortOrder,
			final GbStudentNameSortOrder nameSortOrder, final GbCategoryAverageSortOrder categorySortOrder,
			final GbGroup groupFilter)
					throws GbException {
		return this.buildGradeMatrix(assignments, this.getGradeableUsers(groupFilter), assignmentSortOrder, nameSortOrder,
				categorySortOrder);
	}

	/**
	 * Build the matrix of assignments and grades for the given users with the specified sort order
	 *
	 * @param assignments list of assignments
	 * @param list of uuids
	 * @Param assignmentSortOrder the assignment sort we want. Wraps assignmentId and direction.
	 * @param nameSortOrder name sort order
	 * @param categorySortOrder the category sort we want. Wraps categoryId and direction.
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(final List<Assignment> assignments, final List<String> studentUuids,
			final GbAssignmentGradeSortOrder assignmentSortOrder, final GbStudentNameSortOrder nameSortOrder,
			final GbCategoryAverageSortOrder categorySortOrder) throws GbException {

		final StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		Temp.timeWithContext("buildGradeMatrix", "buildGradeMatrix start", stopwatch.getTime());

		final Gradebook gradebook = this.getGradebook();
		if (gradebook == null) {
			return null;
		}
		Temp.timeWithContext("buildGradeMatrix", "getGradebook", stopwatch.getTime());

		// get role for current user
		final GbRole role = this.getUserRole();

		// get uuids as list of Users.
		// this gives us our base list and will be sorted as per our desired sort method
		final List<User> students = getUsers(studentUuids);
		if (nameSortOrder != null) {

			if (nameSortOrder == GbStudentNameSortOrder.LAST_NAME) {
				Collections.sort(students, new LastNameComparator());
			} else {
				Collections.sort(students, new FirstNameComparator());
			}
		}

		// because this map is based on eid not uuid, we do the filtering later so we can save an iteration
		final Map<String, String> courseGrades = getSiteCourseGrades();

		Temp.timeWithContext("buildGradeMatrix", "getSiteCourseGrades", stopwatch.getTime());

		// setup a map because we progressively build this up by adding grades to a student's entry
		final Map<String, GbStudentGradeInfo> matrix = new LinkedHashMap<>();

		// seed the map for all students so we can progresseively add grades to it
		// also add the course grade here, to save an iteration later
		// TA permissions already included in course grade visibility
		for (final User student : students) {

			// create and add the user info
			final GbStudentGradeInfo sg = new GbStudentGradeInfo(student);

			// add the course grade
			sg.setCourseGrade(courseGrades.get(student.getEid()));

			// add to map so we can build on it later
			matrix.put(student.getId(), sg);
		}
		Temp.timeWithContext("buildGradeMatrix", "matrix seeded", stopwatch.getTime());

		// get categories. This call is filtered for TAs as well.
		final List<CategoryDefinition> categories = this.getGradebookCategories();

		// for TA's, build a lookup map of visible categoryIds so we can filter the assignment list to not fetch grades
		// for assignments we don't have category level access to.
		// for everyone else this will just be an empty list that is unused
		final List<Long> categoryIds = new ArrayList<>();

		if (role == GbRole.TA) {
			for (final CategoryDefinition category : categories) {
				categoryIds.add(category.getId());
			}
		}

		// this holds a map of categoryId and the list of assignment ids in each
		// we build this whilst iterating below to save further iterations when building the category list
		final Map<Long, Set<Long>> categoryAssignments = new TreeMap<>();

		// iterate over assignments and get the grades for each
		// note, the returned list only includes entries where there is a grade for the user
		// we also build the category lookup map here
		for (final Assignment assignment : assignments) {

			final Long categoryId = assignment.getCategoryId();
			final Long assignmentId = assignment.getId();

			// TA permission check. If there are categories and they don't have access to this one, skip it
			if (role == GbRole.TA) {
				if (!categoryIds.isEmpty() && !categoryIds.contains(categoryId)) {
					continue;
				}
			}

			// TA stub out. So that we can support 'per grade' permissions for a TA, we need a stub record for every student
			// This is because getGradesForStudentsForItem only returns records where there is a grade (even if blank)
			// So this iteration for TAs allows the matrix to be fully populated.
			// This is later updated to be a real grade entry if there is one.
			if (role == GbRole.TA) {
				for (final User student : students) {
					final GbStudentGradeInfo sg = matrix.get(student.getId());
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
					values = new HashSet<Long>();
					values.add(assignmentId);
				}
				categoryAssignments.put(categoryId, values);
			}

			// get grades
			final List<GradeDefinition> defs = this.gradebookService.getGradesForStudentsForItem(gradebook.getUid(), assignment.getId(),
					studentUuids);
			Temp.timeWithContext("buildGradeMatrix", "getGradesForStudentsForItem: " + assignment.getId(), stopwatch.getTime());

			// iterate the definitions returned and update the record for each student with the grades
			for (final GradeDefinition def : defs) {
				final GbStudentGradeInfo sg = matrix.get(def.getStudentUid());

				if (sg == null) {
					log.warn("No matrix entry seeded for: " + def.getStudentUid() + ". This user may be been removed from the site");
				} else {
					// this will overwrite the stub entry for the TA matrix if need be
					sg.addGrade(assignment.getId(), new GbGradeInfo(def));
				}
			}
			Temp.timeWithContext("buildGradeMatrix", "updatedStudentGradeInfo: " + assignment.getId(), stopwatch.getTime());
		}
		Temp.timeWithContext("buildGradeMatrix", "matrix built", stopwatch.getTime());

		// build category columns
		for (final CategoryDefinition category : categories) {

			// use the category mappings for faster lookup of the assignmentIds and grades in the category
			final Set<Long> categoryAssignmentIds = categoryAssignments.get(category.getId());

			// if there are no assignments in the category (ie its a new category) this will be null, so skip
			if (categoryAssignmentIds != null) {

				for (final User student : students) {

					final GbStudentGradeInfo sg = matrix.get(student.getId());

					// get grades
					final Map<Long, GbGradeInfo> grades = sg.getGrades();

					// build map of just the grades we want
					final Map<Long, String> gradeMap = new HashMap<>();
					for (final Long assignmentId : categoryAssignmentIds) {
						final GbGradeInfo gradeInfo = grades.get(assignmentId);
						if (gradeInfo != null) {
							gradeMap.put(assignmentId, gradeInfo.getGrade());
						}
					}

					final Double categoryScore = this.gradebookService.calculateCategoryScore(gradebook, student.getId(),
							category, assignments, gradeMap);

					// add to GbStudentGradeInfo
					sg.addCategoryAverage(category.getId(), categoryScore);

					// TODO the TA permission check could reuse this iteration... check performance.

				}
			}

		}
		Temp.timeWithContext("buildGradeMatrix", "categories built", stopwatch.getTime());

		// course grade override. if no grades, course grade should be - instead of 'F'
		// TODO this iteration may not be necessary as we could instead
		// add a boolean to the GbStudentGradeInfo object for each student and when calling addGrade set it to true
		// then check the boolean on the front end, but then it needs to be checked everywhere so this may be better.
		for (final User student : students) {
			final GbStudentGradeInfo sg = matrix.get(student.getId());

			if (sg.getGrades().isEmpty()) {
				sg.setCourseGrade("-");
			}
		}
		Temp.timeWithContext("buildGradeMatrix", "course grade override done", stopwatch.getTime());

		// for a TA, apply the permissions to each grade item to see if we can render it
		// the list of students, assignments and grades is already filtered to those that can be viewed
		// so we are only concerned with the gradeable permission
		if (role == GbRole.TA) {

			// get permissions
			final List<PermissionDefinition> permissions = getPermissionsForUser(getCurrentUser().getId());

			// only need to process this if some are defined
			// again only concerned with grade permission, so parse the list to remove those that aren't GRADE
			if (!permissions.isEmpty()) {

				final Iterator<PermissionDefinition> iter = permissions.iterator();
				while (iter.hasNext()) {
					final PermissionDefinition permission = iter.next();
					if (!StringUtils.equalsIgnoreCase(GraderPermission.GRADE.toString(), permission.getFunction())) {
						iter.remove();
					}
				}
			}

			// if we still have permissions, they will be of type grade, so we need to enrich the students grades
			if (!permissions.isEmpty()) {

				log.debug("Grade permissions exist, processing: " + permissions.size());

				// first need a lookup map of assignment id to category so we can link up permissions by category
				final Map<Long, Long> assignmentCategoryMap = new HashMap<>();
				for (final Assignment assignment : assignments) {
					assignmentCategoryMap.put(assignment.getId(), assignment.getCategoryId());
				}

				// get the group membership for the students
				final Map<String, List<String>> groupMembershipsMap = getGroupMemberships();

				// for every student
				for (final User student : students) {

					log.debug("Processing student: " + student.getEid());

					final GbStudentGradeInfo sg = matrix.get(student.getId());

					// get their assignment/grade list
					final Map<Long, GbGradeInfo> gradeMap = sg.getGrades();

					// for every assignment that has a grade
					for (final Map.Entry<Long, GbGradeInfo> entry : gradeMap.entrySet()) {

						// categoryId
						final Long gradeCategoryId = assignmentCategoryMap.get(entry.getKey());

						log.debug("Grade: " + entry.getValue());

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

							log.debug("permissionCategoryId: " + permissionCategoryId);
							log.debug("permissionGroupReference: " + permissionGroupReference);

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
								log.debug("groupMembers: " + groupMembers);

								if (groupMembers != null && groupMembers.contains(student.getId())) {
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
			Temp.timeWithContext("buildGradeMatrix", "TA permissions applied", stopwatch.getTime());
		}

		// get the matrix as a list of GbStudentGradeInfo
		final List<GbStudentGradeInfo> items = new ArrayList<>(matrix.values());

		// sort the matrix based on the supplied assignment sort order (if any)
		if (assignmentSortOrder != null) {
			final AssignmentGradeComparator comparator = new AssignmentGradeComparator();
			comparator.setAssignmentId(assignmentSortOrder.getAssignmentId());

			final SortDirection direction = assignmentSortOrder.getDirection();

			// sort
			Collections.sort(items, comparator);

			// reverse if required
			if (direction == SortDirection.DESCENDING) {
				Collections.reverse(items);
			}
		}
		Temp.timeWithContext("buildGradeMatrix", "matrix sorted by assignment", stopwatch.getTime());

		// sort the matrix based on the supplied category sort order (if any)
		if (categorySortOrder != null) {
			final CategorySubtotalComparator comparator = new CategorySubtotalComparator();
			comparator.setCategoryId(categorySortOrder.getCategoryId());

			final SortDirection direction = categorySortOrder.getDirection();

			// sort
			Collections.sort(items, comparator);

			// reverse if required
			if (direction == SortDirection.DESCENDING) {
				Collections.reverse(items);
			}
		}
		Temp.timeWithContext("buildGradeMatrix", "matrix sorted by category", stopwatch.getTime());

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

		// get sections
		// groups handles both
		/*
		 * try { Set<Section> sections = courseManagementService.getSections(siteId); for(Section section: sections){ rval.add(new
		 * GbGroup(section.getEid(), section.getTitle(), GbGroup.Type.SECTION)); } } catch (IdNotFoundException e) { //not a course site or
		 * no sections, ignore }
		 */

		// get groups
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

		// if user is a TA, get the groups they can see and filter the GbGroup list to keep just those
		if (this.getUserRole(siteId) == GbRole.TA) {
			final Gradebook gradebook = this.getGradebook(siteId);
			final User user = getCurrentUser();

			// need list of all groups as REFERENCES (not ids)
			final List<String> allGroupIds = new ArrayList<>();
			for (final GbGroup group : rval) {
				allGroupIds.add(group.getReference());
			}

			// get the ones the TA can actually view
			// note that if a group is empty, it will not be included.
			final List<String> viewableGroupIds = this.gradebookPermissionService.getViewableGroupsForUser(gradebook.getId(), user.getId(),
					allGroupIds);

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
	 * Helper to get user
	 *
	 * @return
	 */
	public User getCurrentUser() {
		return this.userDirectoryService.getCurrentUser();
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

			final Long assignmentId = this.gradebookService.addAssignment(gradebookId, assignment);

			// Force the assignment to sit at the end of the list
			if (assignment.getSortOrder() == null) {
				final List<Assignment> allAssignments = this.gradebookService.getAssignments(gradebookId);
				int nextSortOrder = allAssignments.size();
				for (final Assignment anotherAssignment : allAssignments) {
					if (anotherAssignment.getSortOrder() != null && anotherAssignment.getSortOrder() >= nextSortOrder) {
						nextSortOrder = anotherAssignment.getSortOrder() + 1;
					}
				}
				updateAssignmentOrder(assignmentId, nextSortOrder);
			}

			// also update the categorized order
			updateAssignmentCategorizedOrder(gradebook.getUid(), assignment.getCategoryId(), assignmentId, Integer.MAX_VALUE);

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
	 * @throws IdUnusedException
	 * @throws PermissionException
	 */
	public void updateAssignmentOrder(final String siteId, final long assignmentId, final int order) {

		final Gradebook gradebook = this.getGradebook(siteId);
		this.gradebookService.updateAssignmentOrder(gradebook.getUid(), assignmentId, order);
	}

	/**
	 * Update the categorized order of an assignment.
	 *
	 * @param assignmentId the assignment we are reordering
	 * @param order the new order
	 * @throws JAXBException
	 * @throws IdUnusedException
	 * @throws PermissionException
	 */
	public void updateAssignmentCategorizedOrder(final long assignmentId, final int order)
			throws JAXBException, IdUnusedException, PermissionException {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		final Gradebook gradebook = (Gradebook) this.gradebookService.getGradebook(siteId);

		if (gradebook == null) {
			log.error(String.format("Gradebook not in site %s", siteId));
			return;
		}

		final Assignment assignmentToMove = this.gradebookService.getAssignment(gradebook.getUid(), assignmentId);

		if (assignmentToMove == null) {
			// TODO Handle assignment not in gradebook
			log.error(String.format("Assignment %d not in site %s", assignmentId, siteId));
			return;
		}

		updateAssignmentCategorizedOrder(gradebook.getUid(), assignmentToMove.getCategoryId(), assignmentToMove.getId(), order);
	}


	/**
	 * Update the categorized order of an assignment via the gradebook service.
	 *
	 * @param gradebookId the gradebook's id
	 * @param categoryId the id for the cataegory in which we are reordering
	 * @param assignmentId the assignment we are reordering
	 * @param order the new order
	 */
	private void updateAssignmentCategorizedOrder(final String gradebookId, final Long categoryId, final Long assignmentId, final int order) {
		this.gradebookService.updateAssignmentCategorizedOrder(gradebookId, categoryId, assignmentId, new Integer(order));
	}


	/**
	 * Comparator class for sorting a list of users by last name Secondary sort is on first name to maintain consistent order for those with
	 * the same last name
	 */
	class LastNameComparator implements Comparator<User> {
		@Override
		public int compare(final User u1, final User u2) {
			return new CompareToBuilder()
					.append(u1.getLastName(), u2.getLastName())
					.append(u1.getFirstName(), u2.getFirstName())
					.toComparison();
		}
	}

	/**
	 * Comparator class for sorting a list of users by first name Secondary sort is on last name to maintain consistent order for those with
	 * the same first name
	 */
	class FirstNameComparator implements Comparator<User> {
		@Override
		public int compare(final User u1, final User u2) {
			return new CompareToBuilder()
					.append(u1.getFirstName(), u2.getFirstName())
					.append(u1.getLastName(), u2.getLastName())
					.toComparison();
		}
	}

	/**
	 * Push a an notification into the cache that someone is editing this gradebook. We store one entry in the cache per gradebook. This
	 * allows fast lookup for a given gradebookUid. Within the cached object we store a map keyed on the user (eid) that performed the edit
	 * (ie could be several instructors editing at once) The value of the map is a map wih a special key of assignmentid+studentUuid, again
	 * for fast lookup. We can then access the data object directly and update it. It holds the coords of a grade cell that has been edited.
	 * So for a given user editing many cells there will be many GbGradeCells associated with that user. These have a time associated with
	 * each so we can discard manually if desired, on lookup.
	 *
	 * @param gradebookUid
	 */
	private void pushEditingNotification(final String gradebookUid, final User currentUser, final String studentUuid,
			final long assignmentId) {

		// TODO Tie into the event system so other edits also participate in this

		// get the notifications for this gradebook
		Map<String, Map<String, GbGradeCell>> notifications = (Map<String, Map<String, GbGradeCell>>) this.cache.get(gradebookUid);

		Map<String, GbGradeCell> cells = null;

		// get or create cell map
		if (notifications != null) {
			cells = notifications.get(currentUser.getId());
		} else {
			notifications = new HashMap<>();
		}

		if (cells == null) {
			cells = new LinkedHashMap<>();
		}

		// push the edited cell into the map. It will add/update as required
		cells.put(buildCellKey(studentUuid, assignmentId), new GbGradeCell(studentUuid, assignmentId, currentUser.getDisplayName()));

		// push the new/updated cell map into the main map
		notifications.put(currentUser.getEid(), cells);

		// update the map in the cache
		this.cache.put(gradebookUid, notifications);

	}

	/**
	 * Get a list of editing notifications for this gradebook. Excludes any notifications for the current user
	 *
	 * @param gradebookUid the gradebook that we are interested in
	 * @return
	 */
	public List<GbGradeCell> getEditingNotifications(final String gradebookUid) {

		final String currentUserId = getCurrentUser().getEid();

		// get the notifications for this gradebook
		Map<String, Map<String, GbGradeCell>> notifications = (Map<String, Map<String, GbGradeCell>>) this.cache.get(gradebookUid);

		final List<GbGradeCell> rval = new ArrayList<>();

		if (notifications != null) {
			// clone array
			notifications = new HashMap(notifications);
			notifications.remove(currentUserId);

			// join the rest of the maps to get a flat list of GbGradeCells
			for (final Map<String, GbGradeCell> cells : notifications.values()) {
				rval.addAll(cells.values());
			}

		}

		// TODO accept a timestamp and filter the list. We are only itnerested in notifications after the given timestamp
		// this solves the problem where old editing notifications are returned even though the user has recently refreshed the list

		return rval;
	}

	/**
	 * Get an Assignment in the current site given the assignment id
	 *
	 * @param siteId
	 * @param assignmentId
	 * @return
	 */
	public Assignment getAssignment(final long assignmentId) {
		final String siteId = getCurrentSiteId();
		return this.getAssignment(siteId, assignmentId);
	}

	/**
	 * Get an Assignment in the specified site given the assignment id
	 *
	 * @param siteId
	 * @param assignmentId
	 * @return
	 */
	public Assignment getAssignment(final String siteId, final long assignmentId) {
		final Gradebook gradebook = getGradebook(siteId);
		if (gradebook != null) {
			return this.gradebookService.getAssignment(gradebook.getUid(), assignmentId);
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
			final Assignment assignment = this.gradebookService.getAssignment(gradebook.getUid(), assignmentId);

			// if the assignment has a sort order, return that
			if (assignment.getSortOrder() != null) {
				return assignment.getSortOrder();
			}

			// otherwise we need to determine the assignment sort order within the list of assignments
			final List<Assignment> assignments = this.getGradebookAssignments(siteId);

			for (int i = 0; i < assignments.size(); i++) {
				final Assignment a = assignments.get(i);
				if (assignmentId == a.getId()) {
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
	public boolean updateAssignment(final Assignment assignment) {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		// need the original name as the service needs that as the key...
		final Assignment original = this.getAssignment(assignment.getId());

		try {
			this.gradebookService.updateAssignment(gradebook.getUid(), original.getId(), assignment);
			if (original.getCategoryId() != null && assignment.getCategoryId() != null
					&& original.getCategoryId().longValue() != assignment.getCategoryId().longValue()) {
				updateAssignmentCategorizedOrder(gradebook.getUid(), assignment.getCategoryId(), assignment.getId(), Integer.MAX_VALUE);
			}
			return true;
		} catch (final Exception e) {
			log.error("An error occurred updating the assignment", e);
		}

		return false;
	}

	/**
	 * Updates ungraded items in the given assignment with the given grade
	 *
	 * @param assignmentId
	 * @param grade
	 * @return
	 */
	public boolean updateUngradedItems(final long assignmentId, final double grade) {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		// get students
		final List<String> studentUuids = this.getGradeableUsers();

		// get grades (only returns those where there is a grade)
		final List<GradeDefinition> defs = this.gradebookService.getGradesForStudentsForItem(gradebook.getUid(), assignmentId,
				studentUuids);

		// iterate and trim the studentUuids list down to those that don't have grades
		for (final GradeDefinition def : defs) {

			// don't remove those where the grades are blank, they need to be updated too
			if (StringUtils.isNotBlank(def.getGrade())) {
				studentUuids.remove(def.getStudentUid());
			}
		}

		if (studentUuids.isEmpty()) {
			log.debug("Setting default grade. No students are ungraded.");
		}

		try {
			// for each student remaining, add the grade
			for (final String studentUuid : studentUuids) {

				log.debug("Setting default grade. Values of assignmentId: " + assignmentId + ", studentUuid: " + studentUuid + ", grade: "
						+ grade);

				// TODO if this is slow doing it one by one, might be able to batch it
				this.gradebookService.saveGradeAndCommentForStudent(gradebook.getUid(), assignmentId, studentUuid, String.valueOf(grade),
						null);
			}
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
		final List<GradingEvent> gradingEvents = this.gradebookService.getGradingEvents(studentUuid, assignmentId);

		final List<GbGradeLog> rval = new ArrayList<>();
		for (final GradingEvent ge : gradingEvents) {
			rval.add(new GbGradeLog(ge));
		}

		Collections.reverse(rval);

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

		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		try {
			final CommentDefinition def = this.gradebookService.getAssignmentScoreComment(gradebook.getUid(), assignmentId, studentUuid);
			if (def != null) {
				return def.getCommentText();
			}
		} catch (GradebookNotFoundException | AssessmentNotFoundException e) {
			log.error("An error occurred retrieving the comment. " + e.getClass() + ": " + e.getMessage());
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
	public boolean updateAssignmentGradeComment(final long assignmentId, final String studentUuid, final String comment) {

		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		try {
			// could do a check here to ensure we aren't overwriting someone else's comment that has been updated in the interim...
			this.gradebookService.setAssignmentScoreComment(gradebook.getUid(), assignmentId, studentUuid, comment);
			return true;
		} catch (GradebookNotFoundException | AssessmentNotFoundException | IllegalArgumentException e) {
			log.error("An error occurred saving the comment. " + e.getClass() + ": " + e.getMessage());
		}

		return false;
	}

	/**
	 * Get the role of the current user in the current site
	 *
	 * @return Role
	 */
	public GbRole getUserRole() {
		final String siteId = getCurrentSiteId();
		return this.getUserRole(siteId);
	}

	/**
	 * Get the role of the current user in the given site
	 *
	 * @param siteId the siteId to check
	 * @return Role
	 */
	public GbRole getUserRole(final String siteId) {

		final String userId = getCurrentUser().getId();

		String siteRef;
		try {
			siteRef = this.siteService.getSite(siteId).getReference();
		} catch (final IdUnusedException e) {
			e.printStackTrace();
			return null;
		}

		GbRole rval;

		if (this.securityService.unlock(userId, GbRole.INSTRUCTOR.getValue(), siteRef)) {
			rval = GbRole.INSTRUCTOR;
		} else if (this.securityService.unlock(userId, GbRole.TA.getValue(), siteRef)) {
			rval = GbRole.TA;
		} else if (this.securityService.unlock(userId, GbRole.STUDENT.getValue(), siteRef)) {
			rval = GbRole.STUDENT;
		} else {
			throw new SecurityException("Current user does not have a valid section.role.x permission");
		}

		return rval;
	}

	/**
	 * Get a map of grades for the given student. Safe to call when logged in as a student.
	 *
	 * @param studentUuid
	 * @param assignments list of assignments the user can
	 * @return map of assignment to GbGradeInfo
	 */
	public Map<Assignment, GbGradeInfo> getGradesForStudent(final String studentUuid) {

		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		// will apply permissions and only return those the student can view
		final List<Assignment> assignments = this.getGradebookAssignments(siteId);

		final Map<Assignment, GbGradeInfo> rval = new LinkedHashMap<>();

		// iterate all assignments and get the grades
		// if student, only proceed if grades are released for the site
		// if instructor or TA, skip this check
		// permission checks are still applied at the assignment level in the GradebookService
		final GbRole role = this.getUserRole(siteId);

		if (role == GbRole.STUDENT) {
			final boolean released = gradebook.isAssignmentsDisplayed();
			if (!released) {
				return rval;
			}
		}

		for (final Assignment assignment : assignments) {
			final GradeDefinition def = this.gradebookService.getGradeDefinitionForStudentForItem(gradebook.getUid(),
					assignment.getId(), studentUuid);
			rval.put(assignment, new GbGradeInfo(def));
		}

		return rval;
	}

	/**
	 * Get the category score for the given student. Safe to call when logged in as a student.
	 *
	 * @param categoryId id of category
	 * @param studentUuid uuid of student
	 * @return
	 */
	public Double getCategoryScoreForStudent(final Long categoryId, final String studentUuid) {

		final Gradebook gradebook = getGradebook();

		final Double score = this.gradebookService.calculateCategoryScore(gradebook.getId(), studentUuid, categoryId);
		log.info("Category score for category: " + categoryId + ", student: " + studentUuid + ":" + score);

		return score;
	}

	/**
	 * Get the settings for this gradebook. Note that this CANNOT be called by a student.
	 *
	 * @return
	 */
	public GradebookInformation getGradebookSettings() {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		final GradebookInformation settings = this.gradebookService.getGradebookInformation(gradebook.getUid());

		Collections.sort(settings.getCategories(), CategoryDefinition.orderComparator);

		return settings;
	}

	/**
	 * Update the settings for this gradebook
	 *
	 * @param settings GradebookInformation settings
	 */
	public void updateGradebookSettings(final GradebookInformation settings) {

		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		this.gradebookService.updateGradebookSettings(gradebook.getUid(), settings);
	}

	/**
	 * Remove an assignment from its gradebook
	 *
	 * @param assignmentId the id of the assignment to remove
	 */
	public void removeAssignment(final Long assignmentId) {
		final Assignment assignment = getAssignment(assignmentId);
		final String category = assignment.getCategoryName();

		this.gradebookService.removeAssignment(assignmentId);
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
				rval.add(getUser(userUuid));
			}
		} catch (final IdUnusedException e) {
			e.printStackTrace();
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

		final List<PermissionDefinition> permissions = this.gradebookPermissionService.getPermissionsForUser(gradebook.getUid(), userUuid);
		if (permissions == null) {
			return new ArrayList<>();
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

		this.gradebookPermissionService.updatePermissionsForUser(gradebook.getUid(), userUuid, permissions);
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

		final GbRole role = this.getUserRole(siteId);

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
				if (StringUtils.equalsIgnoreCase(def.getFunction(), GraderPermission.VIEW_COURSE_GRADE.toString())) {
					return true;
				}
			}
			return false;
		}

		// if student, check the settings
		// this could actually get the settings but it would be more processing
		if (role == GbRole.STUDENT) {
			final Gradebook gradebook = this.getGradebook(siteId);

			if (gradebook.isCourseGradeDisplayed()) {
				return true;
			}
		}

		// other roles not yet catered for, catch all.
		return false;
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
			log.error("Error looking up site: " + siteId, e);
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
	 * Have categories been enabled for the gradebook?
	 *
	 * @return if the gradebook is setup for either "Categories Only" or "Categories & Weighting"
	 */
	public boolean categoriesAreEnabled() {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		return GbCategoryType.ONLY_CATEGORY.getValue() == gradebook.getCategory_type() ||
				GbCategoryType.WEIGHTED_CATEGORY.getValue() == gradebook.getCategory_type();
	}

	/**
	 * Get the currently configured gradebook category type
	 *
	 * @return GbCategoryType wrapper around the int value
	 */
	public GbCategoryType getGradebookCategoryType() {
		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		final int configuredType = gradebook.getCategory_type();

		return GbCategoryType.valueOf(configuredType);
	}

	/**
	 * Update the course grade (override) for this student
	 * 
	 * @param studentUuid uuid of the student
	 * @param grade the new grade
	 * @return
	 */
	public boolean updateCourseGrade(final String studentUuid, final String grade) {

		final String siteId = getCurrentSiteId();
		final Gradebook gradebook = getGradebook(siteId);

		try {
			this.gradebookService.updateCourseGradeForStudent(gradebook.getUid(), studentUuid, grade);
			return true;
		} catch (final Exception e) {
			log.error("An error occurred saving the course grade. " + e.getClass() + ": " + e.getMessage());
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
	 * Comparator class for sorting a list of AssignmentOrders
	 */
	class AssignmentOrderComparator implements Comparator<AssignmentOrder> {
		@Override
		public int compare(final AssignmentOrder ao1, final AssignmentOrder ao2) {
			// Deal with uncategorized assignments (nulls!)
			if (ao1.getCategory() == null && ao2.getCategory() == null) {
				return ((Integer) ao1.getOrder()).compareTo(ao2.getOrder());
			} else if (ao1.getCategory() == null) {
				return 1;
			} else if (ao2.getCategory() == null) {
				return -1;
			}
			// Deal with friendly categorized assignments
			if (ao1.getCategory().equals(ao2.getCategory())) {
				return ((Integer) ao1.getOrder()).compareTo(ao2.getOrder());
			} else {
				return ao1.getCategory().compareTo(ao2.getCategory());
			}
		}
	}

	/**
	 * Build the key to identify the cell. Used in the notifications cache.
	 *
	 * @param studentUuid
	 * @param assignmentId
	 * @return
	 */
	private String buildCellKey(final String studentUuid, final long assignmentId) {
		return studentUuid + "-" + assignmentId;
	}

	/**
	 * Comparator class for sorting an assignment by the grades.
	 *
	 * Note that this must have the assignmentId set into it so we can extract the appropriate grade entry from the map that each student
	 * has.
	 *
	 */
	class AssignmentGradeComparator implements Comparator<GbStudentGradeInfo> {

		@Setter
		private long assignmentId;

		@Override
		public int compare(final GbStudentGradeInfo g1, final GbStudentGradeInfo g2) {

			final GbGradeInfo info1 = g1.getGrades().get(this.assignmentId);
			final GbGradeInfo info2 = g2.getGrades().get(this.assignmentId);

			// for proper number ordering, these have to be numerical
			final Double grade1 = (info1 != null) ? NumberUtils.toDouble(info1.getGrade()) : null;
			final Double grade2 = (info2 != null) ? NumberUtils.toDouble(info2.getGrade()) : null;

			return new CompareToBuilder()
					.append(grade1, grade2)
					.toComparison();

		}
	}

	/**
	 * Comparator class for sorting a category by the subtotals
	 *
	 * Note that this must have the categoryId set into it so we can extract the appropriate grade entry from the map that each student has.
	 *
	 */
	class CategorySubtotalComparator implements Comparator<GbStudentGradeInfo> {

		@Setter
		private long categoryId;

		@Override
		public int compare(final GbStudentGradeInfo g1, final GbStudentGradeInfo g2) {

			final Double subtotal1 = g1.getCategoryAverages().get(this.categoryId);
			final Double subtotal2 = g2.getCategoryAverages().get(this.categoryId);

			return new CompareToBuilder()
					.append(subtotal1, subtotal2)
					.toComparison();

		}
	}
}
