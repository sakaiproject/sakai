package org.sakaiproject.gradebookng.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.StopWatch;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gradebookng.business.dto.AssignmentOrder;
import org.sakaiproject.gradebookng.business.dto.GradebookUserPreferences;
import org.sakaiproject.gradebookng.business.model.GbEditingNotification;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbGroupType;
import org.sakaiproject.gradebookng.business.util.Temp;
import org.sakaiproject.gradebookng.business.util.XmlList;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.SimpleConfiguration;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.Permission;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;


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
	private CourseManagementService courseManagementService;
	
	@Setter
	private MemoryService memoryService;
	
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
	 * @return a list of users or null if none
	 */
	public List<User> getGradeableUsers() {
		return this.getGradeableUsers(Collections.<String> emptyList());
	}
	
	/**
	 * Get a list of users in the current site that can have grades, based on the passed in userUuids
	 * If the passed in list is empty, it returns all users that can be graded in the site
	 * 
	 * @return a list of users or null if none
	 */
	public List<User> getGradeableUsers(List<String> userUuids) {
		try {
			String siteId = this.getCurrentSiteId();
			Set<String> gradeableUserUuids = siteService.getSite(siteId).getUsersIsAllowed(Permissions.VIEW_OWN_GRADES.getValue());
			
			List<String> matchingUuids = new ArrayList<String>();
			if(userUuids.isEmpty()){
				matchingUuids.addAll(gradeableUserUuids);
			}
			
			for(String uuid : userUuids) {
				if(gradeableUserUuids.contains(uuid)){
					matchingUuids.add(uuid);
				}
			}
			
			List<User> users = userDirectoryService.getUsers(matchingUuids);
			
			Collections.sort(users, new LastNameComparator()); //this needs to take into account the GbStudentSortType
			return users;
			
		} catch (IdUnusedException e) {
			return null;
		}
	}
	
	
	/**
	 * Helper to get a reference to the gradebook for the current site
	 * 
	 * @return the gradebook for the site
	 */
	private Gradebook getGradebook() {
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
	 * Get a list of assignments in the gradebook in the current site
	 * 
	 * @return a list of assignments or null if no gradebook
	 */
	public List<Assignment> getGradebookAssignments() {
		return getGradebookAssignments(this.getCurrentSiteId());
	}
	
	/**
	 * Get a list of assignments in the gradebook in the specified site, sorted by sort order
	 * 
	 * @param siteId the siteId
	 * @return a list of assignments or null if no gradebook
	 */
	public List<Assignment> getGradebookAssignments(String siteId) {
		Gradebook gradebook = getGradebook(siteId);
		if(gradebook != null) {
			return gradebookService.getAssignments(gradebook.getUid(), SortType.SORT_BY_SORTING);
		}
		return null;
	}
	
	
	
		
	/**
	 * Get a map of course grades for all users in the site, using a grade override preferentially over a calculated one
	 * key = student eid
	 * value = course grade
	 * 
	 * Note that his mpa is keyed on EID. Since the business service does not have a list of eids, to save an iteration, the calling service needs to do the filtering
	 * 
	 * @param userUuids
	 * @return the map of course grades for students, or an empty map
	 */
	@SuppressWarnings("unchecked")
	public Map<String,String> getSiteCourseGrades() {
		
		Map<String,String> courseGrades = new HashMap<>();
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook != null) {
			
			//get course grades. THis new method for Sakai 11 does the override automatically, so GB1 data is preserved
			//note that this DOES not have the course grade points earned because that is in GradebookManagerHibernateImpl
			courseGrades = gradebookService.getImportCourseGrade(gradebook.getUid());
						
		}
		
		return courseGrades;
	}
	
	
	
	/**
	 * Save the grade for a student's assignment
	 * 
	 * @param assignmentId	id of the gradebook assignment
	 * @param studentUuid	uuid of the user
	 * @param oldGrade 		old grade, passed in for concurrency checking
	 * @param newGrade		new grade for the assignment/user
	 * 
	 * @return
	 */
	public GradeSaveResponse saveGrade(final Long assignmentId, final String studentUuid, final String oldGrade, final String newGrade) {
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return GradeSaveResponse.ERROR;
		}
		
		return this.saveGrade(assignmentId, studentUuid, oldGrade, newGrade, null);
	}
	
	/**
	 * Save the grade and comment for a student's assignment
	 * 
	 * @param assignmentId	id of the gradebook assignment
	 * @param studentUuid	uuid of the user
	 * @param oldGrade 		old grade, passed in for concurrency checking
	 * @param newGrade		new grade for the assignment/user
	 * @param comment		optional comment for the grade
	 * 
	 * @return
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

		//no change
		if(StringUtils.equals(storedGrade, newGrade)){
			return GradeSaveResponse.NO_CHANGE;
		}

		//concurrency check, if stored grade != old grade that was passed in, someone else has edited.
		if(!StringUtils.equals(storedGrade, oldGrade)) {	
			return GradeSaveResponse.CONCURRENT_EDIT;
		}
		
		//about to edit so push a notification
		pushEditingNotification(gradebook.getUid());
		
		//over limit check, get max points for assignment and check if the newGrade is over limit
		//we still save it but we return the warning
		Assignment assignment = this.gradebookService.getAssignment(gradebook.getUid(), assignmentId);
		Double maxPoints = assignment.getPoints();
		
		Double newGradePoints = NumberUtils.toDouble(newGrade);
		
		GradeSaveResponse rval = null;
		
		if(newGradePoints.compareTo(maxPoints) > 0) {
			log.debug("over limit. Max: " + maxPoints);
			rval = GradeSaveResponse.OVER_LIMIT;
		}
		
		//save
		try {
			gradebookService.saveGradeAndCommentForStudent(gradebook.getUid(), assignmentId, studentUuid, newGrade, comment);
			if(rval == null) {
				//if we don't have some other warning, it was all OK
				rval = GradeSaveResponse.OK;
				
				//push an event into the cache
				
			}
		} catch (InvalidGradeException | GradebookNotFoundException | AssessmentNotFoundException e) {
			log.error("An error occurred saving the grade. " + e.getClass() + ": " + e.getMessage());
			rval = GradeSaveResponse.ERROR;
		}
		return rval;
	}
	
	
	/**
	 * Build the matrix of assignments, students and grades
	 * 
	 * In the future this can be expanded to be given a list of students so we can do scrolling
	 * 
	 * @param assignments list of assignments
	 * @return
	 */
	public List<StudentGradeInfo> buildGradeMatrix(List<Assignment> assignments) {
		return this.buildGradeMatrix(assignments, Collections.<String> emptyList());
	}
	
	/**
	 * Build the matrix of assignments and grades for the given student uuids.
	 * 
	 * If the passed in list is empty, it returns all users that can be graded in the site
	 * 
	 * @param assignments
	 * @param userUuids student uuids to get the data for
	 * @return
	 */
	public List<StudentGradeInfo> buildGradeMatrix(List<Assignment> assignments, List<String> userUuids) {
		
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		Temp.timeWithContext("buildGradeMatrix", "buildGradeMatrix start", stopwatch.getTime());
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return null;
		}
		Temp.timeWithContext("buildGradeMatrix", "getGradebook", stopwatch.getTime());
		
		List<User> students = this.getGradeableUsers(userUuids);
		Temp.timeWithContext("buildGradeMatrix", "getGradeableUsers", stopwatch.getTime());

		//because this map is based on eid not uuid, we do the filtering later so we can save an iteration
		Map<String,String> courseGrades = this.getSiteCourseGrades();
		Temp.timeWithContext("buildGradeMatrix", "getSiteCourseGrades", stopwatch.getTime());
		
		List<StudentGradeInfo> rval = new ArrayList<StudentGradeInfo>();
		
		//TODO this could be optimised to iterate the assignments instead, and pass the list of users and use getGradesForStudentsForItem,
		//however the logic needs to be reworked so we can capture the user info
		//NOT a high priority unless performance issue deems it to be
		
		for(User student: students) {
			
			StudentGradeInfo sg = new StudentGradeInfo(student);
			
			//add the assignment grades
			for(Assignment assignment: assignments) {
				GradeDefinition gradeDefinition = gradebookService.getGradeDefinitionForStudentForItem(gradebook.getUid(), assignment.getId(), student.getId());
				Temp.timeWithContext("buildGradeMatrix", "getGradeDefinitionForStudentForItem", stopwatch.getTime());
				
				sg.addGrade(assignment.getId(), new GradeInfo(gradeDefinition));
			}
			Temp.timeWithContext("buildGradeMatrix", "all grades for student", stopwatch.getTime());

			//add the course grade
			sg.setCourseGrade(courseGrades.get(student.getEid()));
			
			//add the section info
			//this.courseManagementService.getSe
			
			rval.add(sg);
			
		}
		
		Temp.timeWithContext("buildGradeMatrix", "buildGradeMatrix done", stopwatch.getTime());

		return rval;
		
	}
	
	/**
	 * Get a list of sections and groups in a site
	 * @return
	 */
	public List<GbGroup> getSiteSectionsAndGroups() {
		String siteId = this.getCurrentSiteId();
		
		List<GbGroup> rval = new ArrayList<>();
		
		//get sections
		try {
			Set<Section> sections = courseManagementService.getSections(siteId);
			for(Section section: sections){
				rval.add(new GbGroup(section.getEid(), section.getTitle(), GbGroupType.SECTION));
			}
		} catch (IdNotFoundException e) {
			//not a course site or no sections, ignore
		}
		
		//get groups
		try {			
			Site site = siteService.getSite(siteId);
			Collection<Group> groups = site.getGroups();

			for(Group group: groups) {
				rval.add(new GbGroup(group.getId(), group.getTitle(), GbGroupType.GROUP));
			}
		} catch (IdUnusedException e) {
			//essentially ignore and use what we have
			log.error("Error retrieving groups", e);
		}
		
		Collections.sort(rval);
		
		//add the default ALL (this is a UI thing, it might not be appropriate here)
		//TODO also need to internationalse ths string
		rval.add(0, new GbGroup(null, "All Sections/Groups", GbGroupType.ALL));
		
		return rval;
		
	}
	
	/**
	 * Get a list of section memberships for the users in the site
	 * @return
	 */
	/*
	public List<String> getSectionMemberships() {
		
		List<Section> sections = getSiteSections();
		for(Section s: sections) {
			EnrollmentSet enrollmentSet = s.getEnrollmentSet();
			
			Set<Enrollment> enrollments = courseManagementService.getEnrollments(enrollmentSet.getEid());
			for(Enrollment e: enrollments) {
				
				//need to create a DTO for this
				
				//a user can be in multiple sections, need a list of sections per user
				
				//s.getTitle(); section title
				//e.getUserId(); user uuid
			}
		}
		
		return null;
		
	}
	*/
	
	
	/**
	 * Helper to get siteid
	 * This will ONLY work in a portal site context, null otherwise.
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
     */
    public void addAssignmentToGradebook(Assignment assignment) {
        Gradebook gradebook = getGradebook();
        if(gradebook != null) {
            String gradebookId = gradebook.getUid();
            this.gradebookService.addAssignment(gradebookId, assignment);
        }
    }
    
    /**
     * Update the order of an assignment.
     * 
     * @param siteId	the siteId
     * @param assignmentId the assignment we are reordering
     * @param order the new order
     * @throws JAXBException
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public void updateAssignmentOrder(String siteId, long assignmentId, int order) throws JAXBException, IdUnusedException, PermissionException {
    	
		Gradebook gradebook = this.getGradebook(siteId); //this is called from an entity provider so must provide the siteId
		this.gradebookService.updateAssignmentOrder(gradebook.getUid(), assignmentId, order);
    }
    
    /**
    * Comparator class for sorting a list of users by last name
    */
    class LastNameComparator implements Comparator<User> {
	    @Override
	    public int compare(User u1, User u2) {
	    	return u1.getLastName().compareTo(u2.getLastName());
	    }
    }
    
    /**
     * Comparator class for sorting a list of users by first name
     */
     class FirstNameComparator implements Comparator<User> {
 	    @Override
 	    public int compare(User u1, User u2) {
 	    	return u1.getFirstName().compareTo(u2.getFirstName());
 	    }
     }
    
     /**
      * Push a an notification into the cache that someone is editing this gradebook.
      * there could be multiple people editing this gradebook so we store a map of events keyed on the user performing the action
      * 
      * @param gradebookUid
      */
     private void pushEditingNotification(String gradebookUid) {
    	 
    	 //TODO do we need to tie into the event system so other edits also affect this?
    	 
    	 User currentUser = this.getCurrentUser();
    	 
    	 Map<String,GbEditingNotification> notifications = (Map<String,GbEditingNotification>) cache.get(gradebookUid);
    	 GbEditingNotification n;
    	 
    	 if(notifications == null) {
    		 notifications = new LinkedHashMap<>();  
    		 
    		 //create a new notification for the current user
    		 n = new GbEditingNotification(this.getCurrentUser(), gradebookUid);
    		 
    	 } else {
    		 
    		 //check if we have one for this user
    		 n = notifications.get(currentUser.getEid());

    		 //if not, create, otherwise update timestamp
    		 if(n == null) {
        		 n = new GbEditingNotification(this.getCurrentUser(), gradebookUid);
    		 } else {
    			 n.setLastUpdated(new Date());
    		 }
    		 
    	 }
    	 
    	 //push the new/updated notification into the map
    	 notifications.put(currentUser.getEid(), n);
    	 
    	 //update the map in the cache
    	 cache.put(gradebookUid, notifications);
    	 
     }
     
     /**
      * Get a list of editing notifications for this gradebook. Excludes any notifications for the current user
      * 
      * @param gradebookUid the gradebook that we are interested in
      * @return
      */
     public Map<String,GbEditingNotification> getEditingNotifications(String gradebookUid) {
		
    	 String currentUserId = this.getCurrentUser().getEid();
    	 
    	 Map<String,GbEditingNotification> notifications = (Map<String,GbEditingNotification>) cache.get(gradebookUid);
    	 if(notifications != null) {
    		notifications.remove(currentUserId);
    	 }
    	 
    	 return notifications;
     }
    
}
