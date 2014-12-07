package org.sakaiproject.gradebookng.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gradebookng.business.dto.GradebookUserPreferences;
import org.sakaiproject.gradebookng.tool.model.StudentGrades;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;


/**
 * Business service for GradebookNG
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

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
	
	//@Setter
	//private XmlMarshaller xmlMarshaller;
	
	/**
	 * Get a list of users in the current site that can have grades
	 * 
	 * @return a list of users or null if none
	 */
	public List<User> getGradeableUsers() {
		try {
			String siteId = this.getCurrentSiteId();
			Set<String> userIds = siteService.getSite(siteId).getUsersIsAllowed("gradebook.viewOwnGrades");			
			return userDirectoryService.getUsers(userIds);
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
		String siteId = this.getCurrentSiteId();
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
		Gradebook gradebook = getGradebook();
		if(gradebook != null) {
			List<Assignment> assignments = gradebookService.getAssignments(gradebook.getUid());
			assignments = sortAssignments(assignments);			
			return assignments;
		}
		return null;
	}
	
	/**
	 * Sort the assignment list according to some criteria
	 * 
	 * @param assignments
	 * @return sorted list of assignments
	 */
	private List<Assignment> sortAssignments(List<Assignment> assignments) {
		//this is a placeholder for when we eventually implement custom sorting of assignment columns
		return assignments;
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
	
	/**
	 * Save the grade and comment for a student's assignment
	 * 
	 * @param assignmentId	id of the gradebook assignment
	 * @param studentUuid	uuid of the user
	 * @param grade			grade for the user
	 * @param comment		optional comment for the grade
	 * 
	 * @return
	 */
	public boolean saveGrade(final Long assignmentId, final String studentUuid, final String grade, final String comment) {
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return false;
		}
		
		try {
			gradebookService.saveGradeAndCommentForStudent(gradebook.getUid(), assignmentId, studentUuid, grade, comment);
		} catch (InvalidGradeException | GradebookNotFoundException | AssessmentNotFoundException e) {
			log.error("An error occurred saving the grade. " + e.getClass() + ": " + e.getMessage());
			return false;
		}
		return true;
	}
	
	
	public List<StudentGrades> buildGradeMatrix() {
		List<User> students = this.getGradeableUsers();
		List<Assignment> assignments = this.getGradebookAssignments();
		List<StudentGrades> grades = new ArrayList<StudentGrades>();
		for(User u: students) {
			StudentGrades sg = this.getGradeListForStudent(u, assignments);
			grades.add(sg);
		}
		return grades;
	}

	/**
	 * Get the list of grades for the given assignments for each user
	 * 
	 * This may need to be reworked for performance and use the method to get the grades for all students per item so it can include GradeDenitiion items
	 * 
	 * @param studentUuid
	 * @param assignments
	 */
	public StudentGrades getGradeListForStudent(final User student, List<Assignment> assignments){
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return null;
		}
		
		List<String> scores = new ArrayList<String>();
		
		//the list of assignments will already be ordered so no need to maintain a map
		for(Assignment assignment: assignments) {
			scores.add(gradebookService.getAssignmentScoreString(gradebook.getUid(), assignment.getId(), student.getId()));
		}
		
		StudentGrades sg = new StudentGrades(student);
		sg.setAssignments(scores);
		
		//TODO get course grade
		
		//NOTES:
		//a reorder of columns can happen client side and be saved as then any refresh is going to refetch the data and it will have the new order applied
		
		return sg;
	}
	
	/**
	 * Get the user prefs for this gradebook instance
	 * @param userUuid
	 * @return
	 */
	public GradebookUserPreferences getUserPrefs() {
		String siteId = this.getCurrentSiteId();
		String userUuid = this.getCurrentUserUuid();
		
		try {
			Site site = siteService.getSite(siteId);
			
			ResourceProperties props = site.getProperties();
			String xml = (String) props.get(GradebookUserPreferences.getPropKey(userUuid));
			
			GradebookUserPreferences prefs = (GradebookUserPreferences) XmlMarshaller.unmarshall(xml);
			return prefs;
			
		} catch (IdUnusedException | JAXBException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	
	/**
	 * Helper to save user prefs
	 * 
	 * @param prefs
	 */
	public void saveUserPrefs (GradebookUserPreferences prefs) {
		String siteId = this.getCurrentSiteId();
		
		try {
			Site site = siteService.getSite(siteId);
			
			ResourcePropertiesEdit props = site.getPropertiesEdit();
			props.addProperty(GradebookUserPreferences.getPropKey(prefs.getUserUuid()), XmlMarshaller.marshal(prefs));
			siteService.save(site);
			
		} catch (IdUnusedException | JAXBException | PermissionException e) {
			e.printStackTrace();
		}
		 
	}
	
	
	/**
	 * Helper to get siteid
	 * @return
	 */
	private String getCurrentSiteId() {
		return this.toolManager.getCurrentPlacement().getContext();
	}
	
	/**
	 * Helper to get user uuid
	 * @return
	 */
	public String getCurrentUserUuid() {
		return this.userDirectoryService.getCurrentUser().getId();
	}
	
	
}
