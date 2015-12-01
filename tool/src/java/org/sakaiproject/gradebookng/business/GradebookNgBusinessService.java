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
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;


/**
 * Business service for GradebookNG
 * 
 * This is not designed to be consumed outside of the application or supplied entityproviders. 
 * Use at your own risk.
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
		
		//max entries unbounded, no TTL eviction (TODO set this to 10 seconds?), TTI 10 seconds
		//TODO this should be configured in sakai.properties so we dont have redundant config code here
		cache = memoryService.getCache(NOTIFICATIONS_CACHE_NAME);
		if(cache == null) {
			cache = memoryService.createCache("org.sakaiproject.gradebookng.cache.notifications", null);
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
	 * @param groupFilter GbGroupType to filter on
	 * 
	 * @return a list of users as uuids or null if none
	 */
	private List<String> getGradeableUsers(GbGroup groupFilter) {
				
		try {
			String siteId = this.getCurrentSiteId();
			
			//note that this list MUST exclude TAs as it is checked in the GradebookService and will throw a SecurityException if invalid users are provided
			Set<String> userUuids = siteService.getSite(siteId).getUsersIsAllowed(GbRole.STUDENT.getValue());
			
			
			//filter the allowed list based on membership
			if(groupFilter != null && groupFilter.getType() != GbGroup.Type.ALL) {
			
				Set<String> groupMembers = new HashSet<>();
				
				/* groups handles both
				if(groupFilter.getType() == GbGroup.Type.SECTION) {
					Set<Membership> members = this.courseManagementService.getSectionMemberships(groupFilter.getId());
					for(Membership m: members) {
						if(userUuids.contains(m.getUserId())) {
							groupMembers.add(m.getUserId());
						}
					}
				}
				*/
				
				if(groupFilter.getType() == GbGroup.Type.GROUP) {
					Set<Member> members = this.siteService.getSite(siteId).getGroup(groupFilter.getId()).getMembers();
					for(Member m: members) {
						if(userUuids.contains(m.getUserId())) {
							groupMembers.add(m.getUserId());
						}
					}
				}
								
				//only keep the ones we identified in the group
				userUuids.retainAll(groupMembers);
			}
			
			//if TA, pass it through the gradebook permissions (only if there are permissions)		
			if(this.getUserRole(siteId) == GbRole.TA) {
				User user = this.getCurrentUser();
				
				//if there are permissions, pass it through them
				//don't need to test TA access if no permissions
				List<PermissionDefinition> perms = this.getPermissionsForUser(user.getId());
				if(!perms.isEmpty()) {
				
					Gradebook gradebook = this.getGradebook(siteId);
				
					//get list of sections and groups this TA has access to
					List courseSections = this.gradebookService.getViewableSections(gradebook.getUid());
													
					//get viewable students.
					List<String> viewableStudents = this.gradebookPermissionService.getViewableStudentsForUser(gradebook.getUid(), user.getId(), new ArrayList<>(userUuids), courseSections);
									
					if(viewableStudents != null) {
						userUuids.retainAll(viewableStudents); //retain only those that are visible to this TA
					} else {
						userUuids.clear(); //TA can't view anyone
					}
				}
			}
			
			return new ArrayList<>(userUuids);
						
		} catch (IdUnusedException e) {
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
	private List<User> getUsers(List<String> userUuids) throws GbException {
		try {
			List<User> users = userDirectoryService.getUsers(userUuids);
			Collections.sort(users, new LastNameComparator()); //default sort
			return users;
		} catch (RuntimeException e) {
			//an LDAP exception can sometimes be thrown here, catch and rethrow
			throw new GbException("An error occurred getting the list of users.", e);
		}
	}
	
	/**
	 * Helper to get a reference to the gradebook for the current site
	 * 
	 * @return the gradebook for the site
	 */
	public Gradebook getGradebook() {
		return getGradebook(this.getCurrentSiteId());
	}
	
	/**
	 * Helper to get a reference to the gradebook for the specified site
	 * 
	 * @param siteId the siteId
	 * @return the gradebook for the site
	 */
	private Gradebook getGradebook(String siteId) {
		try {
			Gradebook gradebook = (Gradebook)gradebookService.getGradebook(siteId);
			return gradebook;
		} catch (GradebookNotFoundException e) {
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
		return getGradebookAssignments(this.getCurrentSiteId());
	}
	
	/**
	 * Get a list of assignments in the gradebook in the specified site that the current user is allowed to access, sorted by sort order
	 * 
	 * @param siteId the siteId
	 * @return a list of assignments or null if no gradebook
	 */
	public List<Assignment> getGradebookAssignments(String siteId) {
		Gradebook gradebook = getGradebook(siteId);
		if(gradebook != null) {
			//applies permissions (both student and TA) and default sort is SORT_BY_SORTING
			return gradebookService.getViewableAssignmentsForCurrentUser(gradebook.getUid());
		}
		return null;
	}
	
	/**
	 * Get a list of categories in the gradebook in the current site
	 * 
	 * @return list of categories or null if no gradebook
	 */
	public List<CategoryDefinition> getGradebookCategories() {
		return getGradebookCategories(this.getCurrentSiteId());
	}
	
	/**
	 * Get a list of categories in the gradebook in the specified site
	 * 
	 * @param siteId the siteId
	 * @return a list of categories or empty if no gradebook
	 */
	public List<CategoryDefinition> getGradebookCategories(String siteId) {
		Gradebook gradebook = getGradebook(siteId);
		
		List<CategoryDefinition> rval = new ArrayList<>();
		
		if(gradebook != null) {
			rval = gradebookService.getCategoryDefinitions(gradebook.getUid());
		}
		
		//filter for TAs
		if(this.getUserRole(siteId) == GbRole.TA) {
			User user = this.getCurrentUser();
			
			//build a list of categoryIds
			List<Long> allCategoryIds = new ArrayList<>();
			for(CategoryDefinition cd: rval) {
				allCategoryIds.add(cd.getId());
			}
			
			if(allCategoryIds.isEmpty()) {
				return Collections.emptyList();
			}
						
			//get a list of category ids the user can actually view
			List<Long> viewableCategoryIds = this.gradebookPermissionService.getCategoriesForUser(gradebook.getId(), user.getId(), allCategoryIds);
			
			//remove the ones that the user can't view
			Iterator<CategoryDefinition> iter = rval.iterator();
			while (iter.hasNext()) {
				CategoryDefinition categoryDefinition = iter.next();
				if(!viewableCategoryIds.contains(categoryDefinition.getId())) {
					iter.remove();
				}
			}
						
		}
		
		return rval;
	}
		
	/**
	 * Get a map of course grades for all users in the site.
	 * key = student eid
	 * value = course grade
	 * 
	 * Note that this map is keyed on EID. Since the business service does not have a list of eids, to save an iteration, the calling service needs to do the filtering
	 * 
	 * @param userUuids
	 * @return the map of course grades for students, or an empty map
	 */
	public Map<String,String> getSiteCourseGrades() {
		
		Map<String,String> rval = new HashMap<>();
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook != null) {
			
			//get course grades. THis new method for Sakai 11 does the override automatically, so GB1 data is preserved
			rval = gradebookService.getImportCourseGrade(gradebook.getUid());
									
		}
		return rval;
	}
	
	/**
	 * Get the course grade for a student
	 * 
	 * @param studentUuid
	 * @return coursegrade. May have null fields if the coursegrade has not been released
	 */
	public CourseGrade getCourseGrade(String studentUuid) {
		
		Gradebook gradebook = this.getGradebook();
		
		CourseGrade rval = this.gradebookService.getCourseGradeForStudent(gradebook.getUid(), studentUuid);
		return rval;
	}
	
	
	
	
	
	/**
	 * Save the grade and comment for a student's assignment. Ignores the concurrency check.
	 * 
	 * @param assignmentId	id of the gradebook assignment
	 * @param studentUuid	uuid of the user
	 * @param grade 		grade for the assignment/user
	 * @param comment		optional comment for the grade. Can be null.
	 * 
	 * @return
	 */
	public GradeSaveResponse saveGrade(final Long assignmentId, final String studentUuid, final String grade, final String comment) {
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return GradeSaveResponse.ERROR;
		}
		
		return this.saveGrade(assignmentId, studentUuid, null, grade, comment);
	}
	
	/**
	 * Save the grade and comment for a student's assignment and do concurrency checking
	 * 
	 * @param assignmentId	id of the gradebook assignment
	 * @param studentUuid	uuid of the user
	 * @param oldGrade 		old grade, passed in for concurrency checking/ If null, concurrency checking is skipped.
	 * @param newGrade		new grade for the assignment/user
	 * @param comment		optional comment for the grade. Can be null.
	 * 
	 * @return
	 * 
	 * TODO make the concurrency check a boolean instead of the null oldGrade
	 */
	public GradeSaveResponse saveGrade(final Long assignmentId, final String studentUuid, String oldGrade, String newGrade, final String comment) {
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return GradeSaveResponse.ERROR;
		}
		
		//get current grade
		String storedGrade = gradebookService.getAssignmentScoreString(gradebook.getUid(), assignmentId, studentUuid);
		
		//trim the .0 from the grades if present. UI removes it so lets standardise.
		storedGrade = StringUtils.removeEnd(storedGrade, ".0");
		oldGrade = StringUtils.removeEnd(oldGrade, ".0");
		newGrade = StringUtils.removeEnd(newGrade, ".0");
		
		//trim to null so we can better compare against no previous grade being recorded (as it will be null)
		//note that we also trim newGrade so that don't add the grade if the new grade is blank and there was no grade previously
		storedGrade = StringUtils.trimToNull(storedGrade);
		oldGrade = StringUtils.trimToNull(oldGrade);	
		newGrade = StringUtils.trimToNull(newGrade);	
		
		if(log.isDebugEnabled()) {
			log.debug("storedGrade: " + storedGrade);
			log.debug("oldGrade: " + oldGrade);
			log.debug("newGrade: " + newGrade);
		}
		
		//if comment longer than 500 chars, error. 
		//the field is a CLOB, probably by mistake. Loading this field up may cause performance issues
		//see SAK-29595
		if(StringUtils.length(comment) > 500) {
			log.error("Comment too long. Maximum 500 characters.");
			return GradeSaveResponse.ERROR;
		}

		//over limit check, get max points for assignment and check if the newGrade is over limit
		//we still save it but we return the warning
		Assignment assignment = this.getAssignment(assignmentId);
		Double maxPoints = assignment.getPoints();

		//no change
		if(StringUtils.equals(storedGrade, newGrade)){
			Double storedGradePoints = NumberUtils.toDouble(storedGrade);
			if(storedGradePoints.compareTo(maxPoints) > 0) {
				return GradeSaveResponse.OVER_LIMIT;
			} else {
				return GradeSaveResponse.NO_CHANGE;
			}
		}

		//concurrency check, if stored grade != old grade that was passed in, someone else has edited.
		//if oldGrade == null, ignore concurrency check
		if(oldGrade != null && !StringUtils.equals(storedGrade, oldGrade)) {	
			return GradeSaveResponse.CONCURRENT_EDIT;
		}
		
		//about to edit so push a notification
		pushEditingNotification(gradebook.getUid(), this.getCurrentUser(), studentUuid, assignmentId);

		GradeSaveResponse rval = null;

		if (StringUtils.isNotBlank(newGrade)) {
			Double newGradePoints = NumberUtils.toDouble(newGrade);

			if(newGradePoints.compareTo(maxPoints) > 0) {
				log.debug("over limit. Max: " + maxPoints);
				rval = GradeSaveResponse.OVER_LIMIT;
			}
		}

		//save
		try {
			//note, you must pass in the comment or it wil lbe nulled out by the GB service
			gradebookService.saveGradeAndCommentForStudent(gradebook.getUid(), assignmentId, studentUuid, newGrade, comment);
			if(rval == null) {
				//if we don't have some other warning, it was all OK
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
	public List<GbStudentGradeInfo> buildGradeMatrix(List<Assignment> assignments) throws GbException {
		return this.buildGradeMatrix(assignments, this.getGradeableUsers());
	}
	
	/**
	 * Build the matrix of assignments and grades for the given users.
	 * In general this is just one, as we use it for the instructor view student summary but could be more for paging etc
	 * 
	 * @param assignments list of assignments
	 * @param list of uuids
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(List<Assignment> assignments, List<String> studentUuids) throws GbException {
		return this.buildGradeMatrix(assignments, studentUuids, null, null);
	}
	
	/**
	 * Build the matrix of assignments, students and grades for all students, with the specified sortOrder
	 * 
	 * @param assignments list of assignments
	 * @param assignmentSortOrder the assignment sort order
	 * @param nameSortOrder name sort order
	 * @param groupFilter if a specific group has been selected (null for all groups)
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(List<Assignment> assignments, GbAssignmentGradeSortOrder assignmentSortOrder, GbStudentNameSortOrder nameSortOrder, GbGroup groupFilter) throws GbException {
		return this.buildGradeMatrix(assignments, this.getGradeableUsers(groupFilter), assignmentSortOrder, nameSortOrder);
	}
	
	/**
	 * Build the matrix of assignments and grades for the given users with the specified sort order
	 * 
	 * @param assignments list of assignments
	 * @param list of uuids
	 * @Param assignmentSortOrder the assignment sort we want. Wraps assignmentId and direction.
	 * @param nameSortOrder name sort order
	 * @return
	 */
	public List<GbStudentGradeInfo> buildGradeMatrix(List<Assignment> assignments, List<String> studentUuids, GbAssignmentGradeSortOrder assignmentSortOrder, GbStudentNameSortOrder nameSortOrder) throws GbException {

		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		Temp.timeWithContext("buildGradeMatrix", "buildGradeMatrix start", stopwatch.getTime());
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return null;
		}
		Temp.timeWithContext("buildGradeMatrix", "getGradebook", stopwatch.getTime());
		
		//get role for current user
		GbRole role = this.getUserRole();
		
		//get uuids as list of Users.
		//this gives us our base list and will be sorted as per our desired sort method
		List<User> students = this.getUsers(studentUuids);
		if(nameSortOrder != null) {
						
			if(nameSortOrder == GbStudentNameSortOrder.LAST_NAME) {
				Collections.sort(students, new LastNameComparator());
			} else {
				Collections.sort(students, new FirstNameComparator());
			}
		}
		
		//because this map is based on eid not uuid, we do the filtering later so we can save an iteration
		Map<String,String> courseGrades = this.getSiteCourseGrades();
		
		Temp.timeWithContext("buildGradeMatrix", "getSiteCourseGrades", stopwatch.getTime());
		
		//setup a map because we progressively build this up by adding grades to a student's entry
		Map<String, GbStudentGradeInfo> matrix = new LinkedHashMap<>();
		
		//seed the map for all students so we can progresseively add grades to it
		//also add the course grade here, to save an iteration later
		//TA permissions already included in course grade visibility
		for(User student: students) {
			
			//create and add the user info
			GbStudentGradeInfo sg = new GbStudentGradeInfo(student);

			//add the course grade
			sg.setCourseGrade(courseGrades.get(student.getEid()));
			
			//add to map so we can build on it later
			matrix.put(student.getId(), sg);
		}
		Temp.timeWithContext("buildGradeMatrix", "matrix seeded", stopwatch.getTime());
		
		//get categories. This call is filtered for TAs as well.
		List<CategoryDefinition> categories = this.getGradebookCategories();
	
		//for TA's, build a lookup map of visible categoryIds so we can filter the assignment list to not fetch grades
		//for assignments we don't have category level access to.
		//for everyone else this will just be an empty list that is unused
		List<Long> categoryIds = new ArrayList<>();
		
		if(role == GbRole.TA) {
			for(CategoryDefinition category: categories) {
				categoryIds.add(category.getId());
			}
		}
		
		//this holds a map of categoryId and the list of assignment ids in each
		//we build this whilst iterating below to save further iterations when building the category list
		Map<Long,Set<Long>> categoryAssignments = new TreeMap<>();
		
		//iterate over assignments and get the grades for each
		//note, the returned list only includes entries where there is a grade for the user
		//we also build the category lookup map here
		for(Assignment assignment: assignments) {
			
			Long categoryId = assignment.getCategoryId();
			Long assignmentId = assignment.getId();
						
			//TA permission check. If there are categories and they don't have access to this one, skip it
			if(role == GbRole.TA) {
				if(!categoryIds.isEmpty() && !categoryIds.contains(categoryId)) {
					continue;
				}
			}

			//build the category map (if assignment is categorised)
			if(categoryId != null) {
				Set<Long> values;
				if(categoryAssignments.containsKey(categoryId)) {
					values = categoryAssignments.get(categoryId);
					values.add(assignmentId);
				} else {
					values = new HashSet<Long>();
					values.add(assignmentId);
				}
				categoryAssignments.put(categoryId, values);
			}
						
			//get grades
			List<GradeDefinition> defs = this.gradebookService.getGradesForStudentsForItem(gradebook.getUid(), assignment.getId(), studentUuids);
			Temp.timeWithContext("buildGradeMatrix", "getGradesForStudentsForItem: " + assignment.getId(), stopwatch.getTime());
	
			//iterate the definitions returned and update the record for each student with any grades
			for(GradeDefinition def: defs) {
				GbStudentGradeInfo sg = matrix.get(def.getStudentUid());
				
				if(sg == null) {
					log.warn("No matrix entry seeded for: " + def.getStudentUid() + ". This user may be been removed from the site");
				} else {
					sg.addGrade(assignment.getId(), new GbGradeInfo(def));
				}
			}
			Temp.timeWithContext("buildGradeMatrix", "updatedStudentGradeInfo: " + assignment.getId(), stopwatch.getTime());
		}
		Temp.timeWithContext("buildGradeMatrix", "matrix built", stopwatch.getTime());
		
		//build category columns
		for(CategoryDefinition category: categories) {
				
			//use the category mappings for faster lookup of the assignmentIds and grades in the category
			Set<Long> categoryAssignmentIds = categoryAssignments.get(category.getId());
			
			//if there are no assignments in the category (ie its a new category) this will be null, so skip
			if(categoryAssignmentIds != null) {
			
				for(User student: students) {
					
					GbStudentGradeInfo sg = matrix.get(student.getId());
					
					//get grades
					Map<Long,GbGradeInfo> grades = sg.getGrades();
					
					//build map of just the grades we want
					Map<Long,String> gradeMap = new HashMap<>();
					for(Long assignmentId: categoryAssignmentIds) {
						GbGradeInfo gradeInfo = grades.get(assignmentId);
						if(gradeInfo != null) {
							gradeMap.put(assignmentId,gradeInfo.getGrade());
						}
					}
					
					Double categoryScore = this.gradebookService.calculateCategoryScore(category, gradeMap);
					
					//add to GbStudentGradeInfo
					sg.addCategoryAverage(category.getId(), categoryScore);
					
					//TODO the TA permission check could reuse this iteration... check performance.
					
				}
			}
			
		}
		Temp.timeWithContext("buildGradeMatrix", "categories built", stopwatch.getTime());
		
		// course grade override. if no grades, course grade should be - instead of 'F'
		//TODO this iteration may not be necessary as we could instead
		// add a boolean to the GbStudentGradeInfo object for each student and when calling addGrade set it to true
		// then check the boolean on the front end, but then it needs to be checked everywhere so this may be better.
		for(User student: students) {
			GbStudentGradeInfo sg = matrix.get(student.getId());
			
			if(sg.getGrades().isEmpty()) {
				sg.setCourseGrade("-");
			}
		}
		Temp.timeWithContext("buildGradeMatrix", "course grade override done", stopwatch.getTime());
		
		//for a TA, apply the permissions to each grade item to see if we can render it
		//the list of students, assignments and grades is already filtered to those that can be viewed
		//so we are only concerned with the gradeable permission
		if(role == GbRole.TA) {
			
			//get permissions
			List<PermissionDefinition> permissions = this.getPermissionsForUser(this.getCurrentUser().getId());
				
			//only need to process this if some are defined
			//again only concerned with grade permission, so parse the list to remove those that aren't GRADE
			if(!permissions.isEmpty()) {
				
				Iterator<PermissionDefinition> iter = permissions.iterator();
				while (iter.hasNext()) {
					PermissionDefinition permission = iter.next();
					if(!StringUtils.equalsIgnoreCase(GraderPermission.GRADE.toString(), permission.getFunction())) {
						iter.remove();
					}
				}
			}
			
			//if we still have permissions, they will be of type grade, so we need to enrich the students grades
			if(!permissions.isEmpty()) {
				
				log.debug("Grade permissions exist, processing: " + permissions.size());

				//first need a lookup map of assignment id to category so we can link up permissions by category
				Map<Long, Long> assignmentCategoryMap = new HashMap<>();
				for(Assignment assignment: assignments) {
					assignmentCategoryMap.put(assignment.getId(), assignment.getCategoryId());
				}
				
				//get the group membership for the students
				Map<String, List<String>> groupMembershipsMap = this.getGroupMemberships();
				
				//for every student
				for(User student: students) {
					
					log.debug("Processing student: " + student.getEid());
					
					GbStudentGradeInfo sg = matrix.get(student.getId());
					
					//get their assignment/grade list
					Map<Long, GbGradeInfo> gradeMap = sg.getGrades();
									
					//for every assignment that has a grade
					for (Map.Entry<Long, GbGradeInfo> entry : gradeMap.entrySet()) {
						
						//categoryId
						Long gradeCategoryId = assignmentCategoryMap.get(entry.getKey());
						
						log.debug("Grade: " + entry.getValue());
						
						//iterate the permissions
						// if category, compare the category,
						// then check the group and find the user in the group
						//if all ok, mark it as GRADEABLE
						
						boolean categoryOk = false;
						boolean groupOk = false;
						boolean gradeable = false;
						
						for(PermissionDefinition permission: permissions) {
							//we know they are all GRADE so no need to check here
							
							Long permissionCategoryId = permission.getCategoryId();
							String permissionGroupReference = permission.getGroupReference();
							
							log.debug("permissionCategoryId: " + permissionCategoryId);
							log.debug("permissionGroupReference: " + permissionGroupReference);
								
							//if permissions category is null (can grade all categories) or they match (can grade this category)
							if(permissionCategoryId == null || permissionCategoryId.equals(gradeCategoryId)) {
								categoryOk = true;
								log.debug("Category check passed");
							}
								
							//if group reference is null (can grade all groups) or group membership contains student (can grade this group)
							if(StringUtils.isBlank(permissionGroupReference)) {
								groupOk = true;
								log.debug("Group check passed #1");
							} else {
								List<String> groupMembers = groupMembershipsMap.get(permissionGroupReference);
								log.debug("groupMembers: " + groupMembers);

								if(groupMembers != null && groupMembers.contains(student.getId())) {
									groupOk = true;
									log.debug("Group check passed #2");
								}
							}
							
							if(categoryOk && groupOk) {
								gradeable = true;
								continue;
							}
						}
						
						//set the gradeable flag on this grade instance
						GbGradeInfo gradeInfo = entry.getValue();
						gradeInfo.setGradeable(gradeable);						
					}
				}
			}
			Temp.timeWithContext("buildGradeMatrix", "TA permissions applied", stopwatch.getTime());
		}
		

		//get the matrix as a list of GbStudentGradeInfo
		List<GbStudentGradeInfo> items = new ArrayList<>(matrix.values());

		//sort the matrix based on the supplied assignment sort order (if any)
		if(assignmentSortOrder != null) {
			GradeComparator comparator = new GradeComparator();
			comparator.setAssignmentId(assignmentSortOrder.getAssignmentId());
			
			SortDirection direction = assignmentSortOrder.getDirection();
			
			//sort
			Collections.sort(items, comparator);
			
			//reverse if required
			if(direction == SortDirection.DESCENDING) {
				Collections.reverse(items);
			}
		}
		Temp.timeWithContext("buildGradeMatrix", "matrix sorted", stopwatch.getTime());
		
		return items;
	}
	
	/**
	 * Get a list of sections and groups in a site
	 * @return
	 */
	public List<GbGroup> getSiteSectionsAndGroups() {
		String siteId = this.getCurrentSiteId();
		
		List<GbGroup> rval = new ArrayList<>();		
		
		//get sections
		// groups handles both
		/*
		try {
			Set<Section> sections = courseManagementService.getSections(siteId);
			for(Section section: sections){				
				rval.add(new GbGroup(section.getEid(), section.getTitle(), GbGroup.Type.SECTION));
			}
		} catch (IdNotFoundException e) {
			//not a course site or no sections, ignore
		}
		*/
		
		//get groups
		try {			
			Site site = siteService.getSite(siteId);
			Collection<Group> groups = site.getGroups();

			for(Group group: groups) {
				rval.add(new GbGroup(group.getId(), group.getTitle(), group.getReference(), GbGroup.Type.GROUP));
			}
						
		} catch (IdUnusedException e) {
			//essentially ignore and use what we have
			log.error("Error retrieving groups", e);
		}
		
		//if user is a TA, get the groups they can see and filter the GbGroup list to keep just those
		if(this.getUserRole(siteId) == GbRole.TA) {
			Gradebook gradebook = this.getGradebook(siteId);
			User user = this.getCurrentUser();
			
			//need list of all groups as REFERENCES (not ids)
			List<String> allGroupIds = new ArrayList<>();
			for(GbGroup group: rval) {
				allGroupIds.add(group.getReference());
			}
									
			//get the ones the TA can actually view
			//note that if a group is empty, it will not be included.
			List<String> viewableGroupIds = this.gradebookPermissionService.getViewableGroupsForUser(gradebook.getId(), user.getId(), allGroupIds);
						
			//remove the ones that the user can't view
			Iterator<GbGroup> iter = rval.iterator();
			while (iter.hasNext()) {
				GbGroup group = iter.next();
				if(!viewableGroupIds.contains(group.getReference())) {
					iter.remove();
				}
			}
			
		}
		
		Collections.sort(rval);
		
		return rval;
	}

	
	/**
	 * Helper to get siteid.
	 * This will ONLY work in a portal site context, it will return null otherwise (ie via an entityprovider).
	 * @return
	 */
	public String getCurrentSiteId() {
		try {
    		return this.toolManager.getCurrentPlacement().getContext();
    	} catch (Exception e){
    		return null;
    	}
	}
	
	/**
     * Get the placement id of the gradebookNG tool in the site.
     * This will ONLY work in a portal site context, null otherwise
     * @return
     */
	private String getToolPlacementId() {
    	try {
    		return this.toolManager.getCurrentPlacement().getId();
    	} catch (Exception e){
    		return null;
    	}
    }
	
	/**
	 * Helper to get user
	 * @return
	 */
	public User getCurrentUser() {
		return this.userDirectoryService.getCurrentUser();
	}

    /**
     * Add a new assignment definition to the gradebook
     * @param assignment
	 * @return id of the newly created assignment or null if there were any errors
     */
    public Long addAssignment(Assignment assignment) {
		
    	Gradebook gradebook = getGradebook();
        
    	if(gradebook != null) {
            String gradebookId = gradebook.getUid();

            Long assignmentId = this.gradebookService.addAssignment(gradebookId, assignment);

            // Force the assignment to sit at the end of the list
            if (assignment.getSortOrder() == null) {
                List<Assignment> allAssignments = this.gradebookService.getAssignments(gradebookId);
                int nextSortOrder = allAssignments.size();
                for (Assignment anotherAssignment : allAssignments) {
                    if (anotherAssignment.getSortOrder() != null && anotherAssignment.getSortOrder() >= nextSortOrder) {
                        nextSortOrder = anotherAssignment.getSortOrder() + 1;
                    }
                }
                updateAssignmentOrder(assignmentId, nextSortOrder);
            }

            // also update the categorized order
            syncCatagorizedAssignmentOrder(getCurrentSiteId(), assignment);

            return assignmentId;
            
            //TODO wrap this so we can catch any runtime exceptions
        }
		return null;
    }
    
    /**
     * Update the order of an assignment for the current site.
	 *
     * @param assignmentId
     * @param order
     */
    public void updateAssignmentOrder(long assignmentId, int order) {
    	
    	String siteId = this.getCurrentSiteId();
		this.updateAssignmentOrder(siteId, assignmentId, order);
    }
    
    /**
     * Update the order of an assignment. If calling outside of GBNG, use this method as you can provide the site id.
     * 
     * @param siteId	the siteId
     * @param assignmentId the assignment we are reordering
     * @param order the new order
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public void updateAssignmentOrder(String siteId, long assignmentId, int order) {
    	
		Gradebook gradebook = this.getGradebook(siteId);
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
	public void updateCategorizedAssignmentOrder(long assignmentId, int order) throws JAXBException, IdUnusedException, PermissionException {
		String siteId = this.getCurrentSiteId();
		updateCategorizedAssignmentOrder(siteId, assignmentId, order);
	}


  /**
   * Update the categorized order of an assignment.
   *
   * @param siteId the site's id
   * @param assignmentId the assignment we are reordering
   * @param order the new order
   * @throws JAXBException
   * @throws IdUnusedException
   * @throws PermissionException
   */
  public void updateCategorizedAssignmentOrder(String siteId, long assignmentId, int order) throws JAXBException, IdUnusedException, PermissionException {
    Site site = null;
    try {
      site = this.siteService.getSite(siteId);
    } catch (IdUnusedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    }

    Gradebook gradebook = (Gradebook)gradebookService.getGradebook(siteId);

    if (gradebook == null) {
      log.error(String.format("Gradebook not in site %s", siteId));
      return;
    }

    Assignment assignmentToMove = gradebookService.getAssignment(gradebook.getUid(), assignmentId);

    if (assignmentToMove == null) {
      // TODO Handle assignment not in gradebook
      log.error(String.format("Assignment %d not in site %s", assignmentId, siteId));
      return;
    }

    String category = assignmentToMove.getCategoryName();

    Map<String, List<Long>> orderedAssignments = getCategorizedAssignmentsOrder(siteId);

    if (!orderedAssignments.containsKey(category)) {
      orderedAssignments = initializeCategorizedAssignmentOrder(siteId, category);
    }

    orderedAssignments.get(category).remove(assignmentToMove.getId());

    if (orderedAssignments.get(category).size() == order) {
      orderedAssignments.get(category).add(assignmentToMove.getId());
    } else {
      orderedAssignments.get(category).add(order, assignmentToMove.getId());
    }

    storeCategorizedAssignmentsOrder(siteId, orderedAssignments);
  }


  /**
   * Get the ordered categorized assignment ids for the current site
   */
  public Map<String, List<Long>> getCategorizedAssignmentsOrder() {
    try {
      return getCategorizedAssignmentsOrder(getCurrentSiteId());
    } catch (JAXBException e) {
      e.printStackTrace();
    } catch(IdUnusedException e) {
      e.printStackTrace();
    } catch(PermissionException e) {
      e.printStackTrace();
    }
    return null;
  }


  /**
   * Get the ordered categorized assignment ids for the siteId
   *
   * @param siteId	the siteId
   * @throws JAXBException
   * @throws IdUnusedException
   * @throws PermissionException
   */
  private Map<String, List<Long>> getCategorizedAssignmentsOrder(String siteId) throws JAXBException, IdUnusedException, PermissionException {
    Site site = null;
    try {
      site = this.siteService.getSite(siteId);
    } catch (IdUnusedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }

    Gradebook gradebook = (Gradebook)gradebookService.getGradebook(siteId);

    if (gradebook == null) {
      log.error(String.format("Gradebook not in site %s", siteId));
      return null;
    }

    ResourceProperties props = site.getProperties();
    String xml = props.getProperty(ASSIGNMENT_ORDER_PROP);

    if(StringUtils.isNotBlank(xml)) {
      try {
        //goes via the xml list wrapper as that is serialisable
        XmlList<AssignmentOrder> xmlList = (XmlList<AssignmentOrder>) XmlMarshaller.unmarshall(xml);
        Map<String, List<Long>> result = new HashMap<String, List<Long>>();
        List<AssignmentOrder> assignmentOrders = xmlList.getItems();

        // Sort the assignments by their category and then order
        Collections.sort(assignmentOrders, new AssignmentOrderComparator());

        for (AssignmentOrder ao : assignmentOrders) {
          // add the category if the XML doesn't have it already
          if (!result.containsKey(ao.getCategory())) {
            result.put(ao.getCategory(), new ArrayList<Long>());
          }

          result.get(ao.getCategory()).add(ao.getAssignmentId());
        }

        return result;
      } catch (JAXBException e) {
        e.printStackTrace();
      }
    } else {
      return initializeCategorizedAssignmentOrder(siteId);
    }

    return null;
  }


	/**
	 * Get the  categorized order for an assignment
	 *
	 * @param assignmentId	the assignment id
	 * @throws JAXBException
	 * @throws IdUnusedException
	 * @throws PermissionException
	 */
	public int getCategorizedSortOrder(Long assignmentId) throws JAXBException, IdUnusedException, PermissionException {
		String siteId = this.getCurrentSiteId();
		Gradebook gradebook = getGradebook(siteId);

		if(gradebook != null) {
			Assignment assignment = gradebookService.getAssignment(gradebook.getUid(), assignmentId);

			Map<String, List<Long>> categorizedOrder = getCategorizedAssignmentsOrder(siteId);
			return categorizedOrder.get(assignment.getCategoryName()).indexOf(assignmentId);
		}

		return -1;
	}


  /**
   * Set up initial Categorized Assignment Order
   */
  private Map<String, List<Long>> initializeCategorizedAssignmentOrder(String siteId) throws JAXBException, IdUnusedException, PermissionException {
    Gradebook gradebook = getGradebook(siteId);

    List<Assignment> assignments = getGradebookAssignments();

    Map<String, List<Long>> categoriesToAssignments = new HashMap<String, List<Long>>();
    for (Assignment assignment : assignments) {
      String category = assignment.getCategoryName();
      if (!categoriesToAssignments.containsKey(category)) {
        categoriesToAssignments.put(category, new ArrayList<Long>());
      }
      categoriesToAssignments.get(category).add(assignment.getId());
    }

    storeCategorizedAssignmentsOrder(siteId, categoriesToAssignments);

    return categoriesToAssignments;
  }
  

  /**
   * Set up Categorized Assignment Order for single category
   *   This is required if a category is added to the gradebook
   *   after the categorized assignment order has been initialized.
   */
  private Map<String, List<Long>> initializeCategorizedAssignmentOrder(String siteId, String category) throws JAXBException, IdUnusedException, PermissionException {
    List<Assignment> assignments = getGradebookAssignments();
    List<Long> assignmentIds = new ArrayList<Long>();
    for (Assignment assignment : assignments) {
      if (category.equals(assignment.getCategoryName())) {
        assignmentIds.add(assignment.getId());
    }
    }
    Map<String, List<Long>> orderData = getCategorizedAssignmentsOrder();
    orderData.put(category, assignmentIds);
    storeCategorizedAssignmentsOrder(siteId, orderData);

    return orderData;
  }

  /**
    * Store categorized assignment order as XML on a site property
    *
    * @param siteId the site's id
    * @param assignments a list of assignments in their new order
    * @throws JAXBException     * @throws IdUnusedException
    * @throws PermissionException
    */
  private void storeCategorizedAssignmentsOrder(String siteId, Map<String, List<Long>> categoriesToAssignments) throws JAXBException, IdUnusedException, PermissionException {
    Site site = null;
    try {
      site = this.siteService.getSite(siteId);
    } catch (IdUnusedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    }

    List<AssignmentOrder> assignmentOrders = new ArrayList<AssignmentOrder>();

    for (String category : categoriesToAssignments.keySet()) {
      List<Long> assignmentIds = categoriesToAssignments.get(category);
      for (int i = 0; i < assignmentIds.size(); i++) {
        assignmentOrders.add(new AssignmentOrder(assignmentIds.get(i), category, i));
      }
    }

    XmlList<AssignmentOrder> newXmlList = new XmlList<AssignmentOrder>(assignmentOrders);
    String newXml = XmlMarshaller.marshal(newXmlList);

    ResourcePropertiesEdit props = site.getPropertiesEdit();
    props.addProperty(ASSIGNMENT_ORDER_PROP, newXml);

    log.debug("Updated assignment order: " + newXml);
    this.siteService.save(site);
  }

  /**
   * Ensure the assignment is ordered within their category
   */
  private void syncCatagorizedAssignmentOrder(String siteId, Assignment assignment) {
    Map<String, List<Long>> orderData = getCategorizedAssignmentsOrder();
    // remove assignment from existing category
    if (orderData.containsValue(assignment.getId())) {
      for (String category : orderData.keySet()) {
        orderData.get(category).remove(assignment.getId());
      }
    }

    try {
      // ensure category order data exists
      if (!orderData.containsKey(assignment.getCategoryName())) {
        initializeCategorizedAssignmentOrder(siteId, assignment.getCategoryName());
      }

      // add assignment end of rightful category
      orderData.get(assignment.getCategoryName()).add(assignment.getId());

      // store in the database
      storeCategorizedAssignmentsOrder(siteId, orderData);
    } catch ( Exception e) {
      log.error("Failed to sync categorized assignment order for: " + assignment.getId());
      e.printStackTrace();
    }
  }


  /**
    * Comparator class for sorting a list of users by last name
    * Secondary sort is on first name to maintain consistent order for those with the same last name
    */
    class LastNameComparator implements Comparator<User> {
	    @Override
	    public int compare(User u1, User u2) {
	    	return new CompareToBuilder()
			.append(u1.getLastName(), u2.getLastName())
			.append(u1.getFirstName(), u2.getFirstName())
			.toComparison();
	    }
    }
    
    /**
     * Comparator class for sorting a list of users by first name
     * Secondary sort is on last name to maintain consistent order for those with the same first name
     */
     class FirstNameComparator implements Comparator<User> {
 	    @Override
 	    public int compare(User u1, User u2) {
 	    	return new CompareToBuilder()
			.append(u1.getFirstName(), u2.getFirstName())
			.append(u1.getLastName(), u2.getLastName())
			.toComparison();
 	    }
     }
    
     /**
      * Push a an notification into the cache that someone is editing this gradebook.
      * We store one entry in the cache per gradebook. This allows fast lookup for a given gradebookUid.
      * Within the cached object we store a map keyed on the user (eid) that performed the edit (ie could be several instructors editing at once)
      * The value of the map is a map wih a special key of assignmentid+studentUuid, again for fast lookup. We can then access the data object directly and update it. It holds the coords of a grade cell that has been edited.
      * So for a given user editing many cells there will be many GbGradeCells associated with that user.
      * These have a time associated with each so we can discard manually if desired, on lookup.
      * 
      * @param gradebookUid
      */
     private void pushEditingNotification(final String gradebookUid, final User currentUser, final String studentUuid, final long assignmentId) {
    	 
    	 //TODO Tie into the event system so other edits also participate in this
    	 
    	 //get the notifications for this gradebook
    	 Map<String,Map<String,GbGradeCell>> notifications = (Map<String,Map<String,GbGradeCell>>) cache.get(gradebookUid);
    	 
    	 Map<String,GbGradeCell> cells = null;
    	 
    	 //get or create cell map
    	 if(notifications != null) {
    		 cells = notifications.get(currentUser.getId());
    	 } else {
    		 notifications = new HashMap<>();
    	 }
    	 
    	 if(cells == null) {
    		 cells = new LinkedHashMap<>();
    	 }
    	 
    	 //push the edited cell into the map. It will add/update as required
		 cells.put(buildCellKey(studentUuid, assignmentId), new GbGradeCell(studentUuid, assignmentId, currentUser.getDisplayName()));
    	 
    	 //push the new/updated cell map into the main map
    	 notifications.put(currentUser.getEid(), cells);
    	 
    	 //update the map in the cache
    	 cache.put(gradebookUid, notifications);
    	 
     }
     
     /**
      * Get a list of editing notifications for this gradebook. Excludes any notifications for the current user
      * 
      * @param gradebookUid the gradebook that we are interested in
      * @return
      */
     public List<GbGradeCell> getEditingNotifications(String gradebookUid) {
		
    	 String currentUserId = this.getCurrentUser().getEid();
    	     	 
    	 //get the notifications for this gradebook
    	 Map<String,Map<String,GbGradeCell>> notifications = (Map<String,Map<String,GbGradeCell>>) cache.get(gradebookUid);
    	 
    	 List<GbGradeCell> rval = new ArrayList<>();
    	 
    	 if(notifications != null) {
    		// clone array
    		notifications = new HashMap(notifications);
    		notifications.remove(currentUserId);
    		
    		//join the rest of the maps to get a flat list of GbGradeCells
    		for(Map<String, GbGradeCell> cells : notifications.values()) {
    			rval.addAll(cells.values());
    		}
    		
    	 }
    	 
    	 //TODO accept a timestamp and filter the list. We are only itnerested in notifications after the given timestamp
    	 //this solves the problem where old editing notifications are returned even though the user has recently refreshed the list
    	 
    	 return rval;
     }

     

     /**
      * Get an Assignment in the current site given the assignment id
      * 
      * @param siteId
      * @param assignmentId
      * @return
      */
     public Assignment getAssignment(long assignmentId) {
    	 String siteId = this.getCurrentSiteId();
    	 return this.getAssignment(siteId, assignmentId);
     }
     
     /**
      * Get an Assignment in the specified site given the assignment id
      * 
      * @param siteId
      * @param assignmentId
      * @return
      */
     public Assignment getAssignment(String siteId, long assignmentId) {
    	 Gradebook gradebook = getGradebook(siteId);
    	 if(gradebook != null) {
    		 return gradebookService.getAssignment(gradebook.getUid(), assignmentId);
    	 }
    	 return null;
     }
     
     /**
      * Get the sort order of an assignment. If the assignment has a sort order, use that.
      * Otherwise we determine the order of the assignment in the list of assignments
      * 
      * This means that we can always determine the most current sort order for an assignment, even if the list has never been sorted.
      * 
      * 
      * @param assignmentId
      * @return sort order if set, or calculated, or -1 if cannot determine at all.
      */
     public int getAssignmentSortOrder(long assignmentId) {
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	     	 
    	 if(gradebook != null) {
    		 Assignment assignment = gradebookService.getAssignment(gradebook.getUid(), assignmentId);
    		 
    		 //if the assignment has a sort order, return that
    		 if(assignment.getSortOrder() != null) {
    			 return assignment.getSortOrder();
    		 }
    		 
    		 //otherwise we need to determine the assignment sort order within the list of assignments
    		 List<Assignment> assignments = this.getGradebookAssignments(siteId);
    		
    		 
    		 for(int i=0; i<assignments.size(); i++) {
    			 Assignment a = assignments.get(i);
    			 if(assignmentId == a.getId()) {
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
     public boolean updateAssignment(Assignment assignment) {
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	 
    	 //need the original name as the service needs that as the key...
    	 Assignment original = this.getAssignment(assignment.getId());
    	 
    	 try {
    		 gradebookService.updateAssignment(gradebook.getUid(), original.getId(), assignment);
			 if (original.getCategoryId() != assignment.getCategoryId()) {
			 	syncCatagorizedAssignmentOrder(siteId, assignment);
			 }
    		 return true;
    	 } catch (Exception e) {
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
     public boolean updateUngradedItems(long assignmentId, double grade) {
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	 
    	 //get students
    	 List<String> studentUuids = this.getGradeableUsers();
    	 
    	 //get grades (only returns those where there is a grade)
    	 List<GradeDefinition> defs = this.gradebookService.getGradesForStudentsForItem(gradebook.getUid(), assignmentId, studentUuids);

    	 //iterate and trim the studentUuids list down to those that don't have grades
    	 for(GradeDefinition def: defs) {
    		 
    		 //don't remove those where the grades are blank, they need to be updated too
    		 if(StringUtils.isNotBlank(def.getGrade())) {
    			 studentUuids.remove(def.getStudentUid());
    		 }
    	 }
    	 
    	 if(studentUuids.isEmpty()) {
    		 log.debug("Setting default grade. No students are ungraded.");
    	 }
    	
    	 try {
	    	 //for each student remaining, add the grade
	    	 for(String studentUuid : studentUuids) {
	    		 
	    		 log.debug("Setting default grade. Values of assignmentId: " + assignmentId + ", studentUuid: " + studentUuid + ", grade: " + grade);
	    		 
	    		 //TODO if this is slow doing it one by one, might be able to batch it
	    		 gradebookService.saveGradeAndCommentForStudent(gradebook.getUid(), assignmentId, studentUuid, String.valueOf(grade), null);
	    	 }
	    	 return true;
    	 } catch (Exception e) {
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
    	 List<GradingEvent> gradingEvents = this.gradebookService.getGradingEvents(studentUuid, assignmentId);
    	 
    	 List<GbGradeLog> rval = new ArrayList<>();
    	 for(GradingEvent ge: gradingEvents) {
    		 rval.add(new GbGradeLog(ge));
    	 }
    	 
    	 Collections.reverse(rval);
    	 
    	 return rval;
     }
     
     /**
      * Get the user given a uuid
      * @param userUuid
      * @return GbUser or null if cannot be found
      */
     public GbUser getUser(String userUuid) {
    	 try {
    		 User u = userDirectoryService.getUser(userUuid);
    		 return new GbUser(u);
    	 } catch (UserNotDefinedException e) {
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
     public String getAssignmentGradeComment(final long assignmentId, final String studentUuid){
    	 
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	 
    	 try {
        	 CommentDefinition def = this.gradebookService.getAssignmentScoreComment(gradebook.getUid(), assignmentId, studentUuid);
    		 if(def != null){
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
    	 
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	 
    	 try {
    		 //could do a check here to ensure we aren't overwriting someone else's comment that has been updated in the interim...
    		 this.gradebookService.setAssignmentScoreComment(gradebook.getUid(), assignmentId, studentUuid, comment);
    		 return true;
    	 } catch (GradebookNotFoundException | AssessmentNotFoundException | IllegalArgumentException e) {
 			log.error("An error occurred saving the comment. " + e.getClass() + ": " + e.getMessage());
    	 }
    	 
    	 return false;
     }
     
     /**
      * Get the role of the current user in the current site
      * @return Role
      */
     public GbRole getUserRole() {
    	 String siteId = this.getCurrentSiteId();
    	 return this.getUserRole(siteId);
     }
     
     /**
      * Get the role of the current user in the given site
      * @param siteId the siteId to check
      * @return Role
      */
     public GbRole getUserRole(String siteId) {
    	 
    	 String userId = this.getCurrentUser().getId();
    	 
    	 String siteRef;
    	 try {
    		 siteRef = this.siteService.getSite(siteId).getReference();
    	 } catch (IdUnusedException e) {
 			e.printStackTrace();
 			return null;
 		}
    	 
    	 GbRole rval;
    	 
    	 if(securityService.unlock(userId, GbRole.INSTRUCTOR.getValue(), siteRef)) {
        	 rval = GbRole.INSTRUCTOR;
         } else if(securityService.unlock(userId, GbRole.TA.getValue(), siteRef)) {
        	 rval = GbRole.TA;
         } else if(securityService.unlock(userId, GbRole.STUDENT.getValue(), siteRef)) {
        	 rval = GbRole.STUDENT;
         } else {
        	 throw new SecurityException("Current user does not have a valid section.role.x permission");
         }
    	 
    	 return rval;
     }
     
     /**
      * Get a map of grades for the given student. Safe to call when logged in as a student. 
      * @param studentUuid
      * @param assignments list of assignments the user can
      * @return map of assignment to GbGradeInfo
      */
     public Map<Assignment,GbGradeInfo> getGradesForStudent(String studentUuid) {
    	 
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	 
    	 //will apply permissions and only return those the student can view
    	 List<Assignment> assignments = this.getGradebookAssignments(siteId);
    	 
    	 Map<Assignment,GbGradeInfo> rval = new LinkedHashMap<>();
    	 for(Assignment assignment: assignments) {
    		 GradeDefinition def = this.gradebookService.getGradeDefinitionForStudentForItem(gradebook.getUid(), assignment.getId(), studentUuid);
    		 rval.put(assignment, new GbGradeInfo(def));
    	 }
    	 
    	 return rval;
     }
     
     /**
      * Get the category score for the given student. Safe to call when logged in as a student.
      * @param categoryId id of category	
      * @param studentUuid uuid of student
      * @param grades Map of grades obtained from getGradesForStudent.
      * @return
      */
     public Double getCategoryScoreForStudent(Long categoryId, String studentUuid, Map<Assignment,GbGradeInfo> grades) {
    	 
    	 String siteId = this.getCurrentSiteId();
    	     	 
    	 //get assignments (filtered to just the category ones later)
    	 List<Assignment> assignments = new ArrayList<Assignment>(grades.keySet());
    	 
    	 //build map of just the grades and assignments we want for the assignments in the given category
    	 Map<Long,String> gradeMap = new HashMap<>();
    	 
    	 Iterator<Assignment> iter = assignments.iterator();
    	 while (iter.hasNext()) {
    		 Assignment assignment = iter.next();
    		 if(categoryId == assignment.getCategoryId()) {
    			 GbGradeInfo gradeInfo = grades.get(assignment);
    			 if(gradeInfo != null) {
    				 gradeMap.put(assignment.getId(),gradeInfo.getGrade());
    			 }
    		 } else {
 				iter.remove();
    		 }
    	 }
    	 
    	 //get the score
    	 Double score = this.gradebookService.calculateCategoryScore(categoryId, assignments, gradeMap);
    	 
    	 log.info("Category score for category: " + categoryId + ", student: " + studentUuid + ":" + score);
    	 
    	 return score;
     }
     
     /**
      * Get the settings for this gradebook
      * 
      * @return
      */
     public GradebookInformation getGradebookSettings() {
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	 
    	 GradebookInformation settings = this.gradebookService.getGradebookInformation(gradebook.getUid());
    	 return settings;
     }
     
     /**
      * Update the settings for this gradebook
      * @param settings GradebookInformation settings
      */
     public void updateGradebookSettings(GradebookInformation settings) {
    	 
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	     	 
    	 this.gradebookService.updateGradebookSettings(gradebook.getUid(), settings);
     }
    

     /**
      * Remove an assignment from its gradebook
      * @param assignmentId the id of the assignment to remove
      */
     public void removeAssignment(Long assignmentId) {
        Assignment assignment = getAssignment(assignmentId);
        String category = assignment.getCategoryName();

        this.gradebookService.removeAssignment(assignmentId);

        // remove assignment from the categorized sort order XML
        Map<String, List<Long>> categorizedOrder = getCategorizedAssignmentsOrder();
        if (categorizedOrder.containsKey(category)) {
            boolean removed = categorizedOrder.get(category).remove(assignmentId);
            if (removed) {
                try {
                    storeCategorizedAssignmentsOrder(getCurrentSiteId(), categorizedOrder);
                } catch (JAXBException | IdUnusedException | PermissionException e) {
                    e.printStackTrace();
                    log.error("Unable to storeCategorizedAssignmentsOrder after removing assignmentId: " + assignmentId);
                }
            }
        }
     }
     
     /**
      * Get a list of teaching assistants in the current site
      * @return
      */
     public List<GbUser> getTeachingAssistants() {

    	 String siteId = this.getCurrentSiteId();
    	 List<GbUser> rval = new ArrayList<>();
    	 
    	 try {
    		 Set<String> userUuids = siteService.getSite(siteId).getUsersIsAllowed(GbRole.TA.getValue());
    		 for(String userUuid: userUuids) {
    			 rval.add(this.getUser(userUuid));
    		 }
    	 } catch (IdUnusedException e) {
    		 e.printStackTrace();
    	 }
    	 
    	 return rval;
     }
     
     /**
      * Get a list of permissions defined for the given user.
      * Note: These are currently only defined/used for a teaching assistant.
      * @param userUuid
      * @return list of permissions or empty list if none
      */
     public List<PermissionDefinition> getPermissionsForUser(String userUuid) {
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	 
    	 List<PermissionDefinition> permissions = this.gradebookPermissionService.getPermissionsForUser(gradebook.getUid(), userUuid);
    	 if(permissions == null) {
    		 return new ArrayList<>();
    	 }
    	 return permissions; 
     }
     
     /**
      * Update the permissions for the user.
      * Note: These are currently only defined/used for a teaching assistant.
      * @param userUuid
      * @param permissions
      */
     public void updatePermissionsForUser(String userUuid, List<PermissionDefinition> permissions) {
    	 String siteId = this.getCurrentSiteId();
    	 Gradebook gradebook = getGradebook(siteId);
    	 
    	 this.gradebookPermissionService.updatePermissionsForUser(gradebook.getUid(), userUuid, permissions);
     }
     
     /**
      * Check if the course grade is visible to the user
      * 
      * For TA's, the students are already filtered by permission so the TA won't see those they don't have access to anyway
      * However if there are permissions and the course grade checkbox is NOT checked, then they explicitly do not have access to the course grade.
      * So this method checks if the TA has any permissions assigned for the site, and if one of them is the course grade permission, then they have access.
      * 
      * @param userUuid user to check
      * @return boolean
      */
     public boolean isCourseGradeVisible(String userUuid) {
    	 
    	 String siteId = this.getCurrentSiteId();
    	 
    	 GbRole role = this.getUserRole(siteId);
    	 
    	 //if instructor, allowed
    	 if(role == GbRole.INSTRUCTOR) {
    		 return true;
    	 }
    	 
    	 if(role == GbRole.TA) {
    	 
	    	 //if no defs, implicitly allowed
	    	 List<PermissionDefinition> defs = getPermissionsForUser(userUuid);
	    	 if(defs.isEmpty()) {
	    		 return true;
	    	 }
    	 
	    	 //if defs and one is the view course grade, explicitly allowed
	    	 for(PermissionDefinition def: defs) {
	    		 if(StringUtils.equalsIgnoreCase(def.getFunction(), GraderPermission.VIEW_COURSE_GRADE.toString())){
	    			 return true;
	    		 }
	    	 }
	    	 return false;
    	 }
    	 
    	 //students not currently supported. Could leverage the settings later.
    	 return false;
     }
     
     /**
      * Build a list of group references to site membership (as uuids) for the groups that are viewable for the current user.
      * @return
      */
     public Map<String,List<String>> getGroupMemberships() {
    	 
    	 String siteId = this.getCurrentSiteId();
    	 
    	 Site site;
    	 try {
    		 site = this.siteService.getSite(siteId);
    	 } catch (IdUnusedException e) {
    		 log.error("Error looking up site: " + siteId, e);
    		 return null;
    	 }
    	 
    	 //filtered for the user
    	 List<GbGroup> viewableGroups = this.getSiteSectionsAndGroups();
    	 
    	 Map<String,List<String>> rval = new HashMap<>();
    	 
    	 for(GbGroup gbGroup: viewableGroups) {
    		 String groupReference = gbGroup.getReference();
    		 List<String> memberUuids = new ArrayList<>();
					
    		 
    		 Group group = site.getGroup(groupReference);
    		 if(group != null) {
    			 Set<Member> members = group.getMembers();
    			 
    			 for(Member m: members) {
    				 memberUuids.add(m.getUserId());
    			 }
    		 }
    		 
    		 rval.put(groupReference, memberUuids);
   
    	 }
    	 
    	 return rval;
     }


    /**
     * Have categories been enabled for the gradebook?
     * @return if the gradebook is setup for either "Categories Only" or "Categories & Weighting"
     */
    public boolean categoriesAreEnabled() {
        String siteId = this.getCurrentSiteId();
        Gradebook gradebook = getGradebook(siteId);

        return GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY == gradebook.getCategory_type() ||
                GradebookService.CATEGORY_TYPE_ONLY_CATEGORY == gradebook.getCategory_type();
    }
    

    /**
     * Comparator class for sorting a list of AssignmentOrders
     */
    class AssignmentOrderComparator implements Comparator<AssignmentOrder> {
      @Override
      public int compare(AssignmentOrder ao1, AssignmentOrder ao2) {
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
          return ((String) ao1.getCategory()).compareTo(ao2.getCategory());
        }
      }
    }
    
    /**
     * Build the key to identify the cell. Used in the notifications cache.
     * @param studentUuid
     * @param assignmentId
     * @return
     */
    private String buildCellKey(String studentUuid, long assignmentId) {
    	return studentUuid + "-" + assignmentId;
    }
    
    
    /**
     * Comparator class for sorting an assignment by the grades
     * Note that this must have the assignmentId set into it so we can extract the appropriate grade entry from the map that each student has
     * 
     */
    class GradeComparator implements Comparator<GbStudentGradeInfo> {
    
    	@Setter
    	private long assignmentId;
    	 
		@Override
		public int compare(GbStudentGradeInfo g1, GbStudentGradeInfo g2) {
						
			GbGradeInfo info1 = g1.getGrades().get(assignmentId);
			GbGradeInfo info2 = g2.getGrades().get(assignmentId);
			
			//for proper number ordering, these have to be numerical
			Double grade1 = (info1 != null) ? NumberUtils.toDouble(info1.getGrade()) : null; 
			Double grade2 = (info2 != null) ? NumberUtils.toDouble(info2.getGrade()) : null; 
			
			return new CompareToBuilder()
			.append(grade1, grade2)
			.toComparison();
			
		}
    }
}
