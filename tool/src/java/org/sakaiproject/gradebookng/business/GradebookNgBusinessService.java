package org.sakaiproject.gradebookng.business;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.Assignment;


/**
 * Business service for GradebookNG
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

public class GradebookNgBusinessService {

	@Setter
	private SiteService siteService;
	
	@Setter
	private UserDirectoryService userDirectoryService;
	
	@Setter
	private ToolManager toolManager;
	
	@Setter
	private GradebookService gradebookService;
	
	/**
	 * Get a list of users in the current site that can have grades
	 * 
	 * @return a list of users or null if none
	 */
	public List<User> getGradeableUsers() {
		try {
			String siteId = toolManager.getCurrentPlacement().getContext();
			Set<String> userIds = siteService.getSite(siteId).getUsersIsAllowed("gradebook.viewOwnGrades");			
			return userDirectoryService.getUsers(userIds);
		} catch (IdUnusedException e) {
			return null;
		}
	}
	
	public void test() {
		
	//Gradebook g = gradebookService.getGradesForStudentsForItem(arg0, arg1, arg2)
	
	}
	
	/**
	 * Helper to get a reference to the gradebook for the current site
	 * 
	 * @return the gradebook for the site
	 */
	private Gradebook getGradebook() {
		String siteId = toolManager.getCurrentPlacement().getContext();
		try {
			Gradebook gradebook = (Gradebook)gradebookService.getGradebook(siteId);
			return gradebook;
		} catch (GradebookNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * Get a list of assignments in the gradebook in the current site
	 * 
	 * @return a list of assignments or null if no gradebook
	 */
	public List<Assignment> getGradebookAssignments() {
		Gradebook gradebook = getGradebook();
		if(gradebook != null) {
			List<Assignment> assignments = gradebookService.getAssignments(gradebook.getUid());
			return assignments;
		}
		return null;
	}
	
	/**
	 * Get a list of grades for the supplied gradebook assignment, for the supplied list of users
	 * 
	 * This has the potential to be paged into an infinite scroll by passing a different list of users each time
	 * 
	 * @param assignmentId
	 * @param userUuids
	 * @return
	 */
	public List<GradeDefinition> getGradesForStudentsForItem(final Long assignmentId, final List<String> userUuids){
		Gradebook gradebook = getGradebook();
		if(gradebook != null) {
			List<GradeDefinition> grades = gradebookService.getGradesForStudentsForItem(gradebook.getUid(), assignmentId, userUuids);
			return grades;
		}
		return null;
	}
	
	//possibly create a helper method here that gets all grades for all assignments for a given list of users, so we dont need to interate in the front end.
	
	/**
	 * Get a map of course grades for the users in the site, using a grade override preferentially over a calculated one
	 * key = uuid
	 * value = course grade
	 * 
	 * Note, could potentially change the Value to be an object to store a field that can show if its been overridden
	 * 
	 * @return the map of course grades for students, or an empty map
	 */
	public Map<String,String> getCourseGrades() {
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook != null) {
			
			//get course grades and use entered grades preferentially, if they exist
	        Map<String, String> courseGrades = gradebookService.getCalculatedCourseGrade(gradebook.getUid()); 
	        Map<String, String> enteredGrades = gradebookService.getEnteredCourseGrade(gradebook.getUid());
	          
	        Iterator<String> gradeOverrides = enteredGrades.keySet().iterator();
	        while(gradeOverrides.hasNext()) {
	        	String username = gradeOverrides.next();
	        	String override = enteredGrades.get(username);
	        	
	        	if(StringUtils.isNotBlank(override)) {
	        		courseGrades.put(username, override);
	        	}
	        }
	        
	        return courseGrades;
		}
		
		return Collections.emptyMap();
		
	}
}
