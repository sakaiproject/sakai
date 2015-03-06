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
import org.sakaiproject.gradebookng.business.util.XmlList;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
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
import org.sakaiproject.tool.gradebook.Permission;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;


/**
 * Business service for GradebookNG
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

// TODO add permission checks! Remove from entityprovider if double up

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
	
	public static final String ASSIGNMENT_ORDER_PROP = "gbng_assignment_order";
	
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
			
			return userDirectoryService.getUsers(matchingUuids);
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
	 * Get a list of assignments in the gradebook in the specified site
	 * 
	 * @param siteId the siteId
	 * @return a list of assignments or null if no gradebook
	 */
	public List<Assignment> getGradebookAssignments(String siteId) {
		Gradebook gradebook = getGradebook(siteId);
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
	        Map<String, String> courseGrades = gradebookService.getImportCourseGrade(gradebook.getUid()); 
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
	 * Save the grade for a student's assignment
	 * 
	 * @param assignmentId	id of the gradebook assignment
	 * @param studentUuid	uuid of the user
	 * @param grade			grade for the user
	 * 
	 * @return
	 */
	public boolean saveGrade(final Long assignmentId, final String studentUuid, final String grade) {
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return false;
		}
		
		return this.saveGrade(assignmentId, studentUuid, grade, null);
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
	
	
	/**
	 * Build the matrix of assignments, students and grades
	 * 
	 * In the future this can be expanded to be given a list of students so we can do scrolling
	 * @return
	 */
	public List<StudentGradeInfo> buildGradeMatrix() {
		
		List<User> students = this.getGradeableUsers();
		List<Assignment> assignments = this.getGradebookAssignments();
		
		Map<String,String> courseGrades = this.getCourseGrades();
		
		//NOTES:
		//a reorder of columns can happen client side and be saved as then any refresh is going to refetch the data and it will have the new order applied
		
		List<StudentGradeInfo> rval = new ArrayList<StudentGradeInfo>();
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return null;
		}
		
		//TODO this could be optimised to iterate the assignments instead, and pass the list of users and use getGradesForStudentsForItem,
		//however the logic needs to be reworked so we can capture the user info
		//currently storing the full grade definition too, this may be unnecessary
		//NOT a high priority unless performance issue deems it to be
		
		for(User student: students) {
			
			StudentGradeInfo sg = new StudentGradeInfo(student);
			
			//add the assignment grades
			for(Assignment assignment: assignments) {
				GradeDefinition gradeDefinition = gradebookService.getGradeDefinitionForStudentForItem(gradebook.getUid(), assignment.getId(), student.getId());
				sg.addGrade(assignment.getId(), new GradeInfo(gradeDefinition));
			}
			
			//add the course grade
			sg.setCourseGrade(courseGrades.get(student.getId()));
			
			//add the section info
			//this.courseManagementService.getSe
			
			rval.add(sg);
			
		}
		return rval;
		
	}
	
	public List<StudentGradeInfo> buildGradeMatrix(List<String> userUuids) {
		
		List<User> students = this.getGradeableUsers();
		List<Assignment> assignments = this.getGradebookAssignments();
		
		Map<String,String> courseGrades = this.getCourseGrades();
		
		//NOTES:
		//a reorder of columns can happen client side and be saved as then any refresh is going to refetch the data and it will have the new order applied
		
		List<StudentGradeInfo> rval = new ArrayList<StudentGradeInfo>();
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return null;
		}
		
		//TODO this could be optimised to iterate the assignments instead, and pass the list of users and use getGradesForStudentsForItem,
		//however the logic needs to be reworked so we can capture the user info
		//currently storing the full grade definition too, this may be unnecessary
		//NOT a high priority unless performance issue deems it to be
		
		for(User student: students) {
			
			StudentGradeInfo sg = new StudentGradeInfo(student);
			
			//add the assignment grades
			for(Assignment assignment: assignments) {
				GradeDefinition gradeDefinition = gradebookService.getGradeDefinitionForStudentForItem(gradebook.getUid(), assignment.getId(), student.getId());
				sg.addGrade(assignment.getId(), new GradeInfo(gradeDefinition));
			}
			
			//add the course grade
			sg.setCourseGrade(courseGrades.get(student.getId()));
			
			//add the section info
			//this.courseManagementService.getSe
			
			rval.add(sg);
			
		}
		return rval;
		
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
	 * Get a list of sections
	 * 
	 * @return
	 */
	public List<Section> getSiteSections() {
		String siteId = this.getCurrentSiteId();
		
		try {
			Set<Section> sections = courseManagementService.getSections(siteId);
			return new ArrayList<Section>(sections);
		} catch (IdNotFoundException e) {
			//if not a course site or no sections
			return Collections.emptyList();
		}
		
		
	}
	
	/**
	 * Get a list of section memberships for the users in the site
	 * @return
	 */
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
	
	
	/**
	 * Helper to get siteid
	 * This will ONLY work in a portal site context, null otherwise.
	 * @return
	 */
	private String getCurrentSiteId() {
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
	 * Helper to get user uuid
	 * @return
	 */
	public String getCurrentUserUuid() {
		return this.userDirectoryService.getCurrentUser().getId();
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
     * Get the configured assignment order for the site
     * 
     * @param siteId
     * @return
     */
    public List<AssignmentOrder> getAssignmentOrder(String siteId) {
    	
    	Site site = null;
		try {
			site = this.siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		ResourceProperties props = site.getProperties();
    	String xml = props.getProperty(ASSIGNMENT_ORDER_PROP);
    	
    	if(StringUtils.isNotBlank(xml)) {
    		try {
    			//goes via the xml list wrapper as that is serialisable
    			XmlList<AssignmentOrder> xmlList = (XmlList<AssignmentOrder>) XmlMarshaller.unmarshall(xml);
    			return xmlList.getItems();
    		} catch (JAXBException e) {
    			e.printStackTrace();
    		}
    	}
    	    	
    	return null;
    }
    
    public void updateAssignmentOrder(String siteId, int assignmentId, int order) throws JAXBException, IdUnusedException, PermissionException {
    	//in the update order method, check for assignemntid element, update it or add it, resave
    	//the sort order needs to be used in the sortAssignments method too
    	
    	Site site = null;
		try {
			site = this.siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		ResourcePropertiesEdit props = site.getPropertiesEdit();
    	String xml = props.getProperty(ASSIGNMENT_ORDER_PROP);
    	
    	List<AssignmentOrder> assignmentOrder = new ArrayList<>();
    	if(StringUtils.isNotBlank(xml)) {
    		XmlList<AssignmentOrder> xmlList = (XmlList<AssignmentOrder>) XmlMarshaller.unmarshall(xml);
    		if(xmlList != null) {
    			assignmentOrder = xmlList.getItems();
    		}
    	}
    	
    	//try to update an existing order
    	boolean matched = false;
    	for(AssignmentOrder as: assignmentOrder) {
    		if(as.getAssignmentId() == assignmentId) {
    			matched = true;
    			as.setOrder(order);
    		}
    	}
    	
    	//otherwise add it
    	if(!matched) {
    		assignmentOrder.add(new AssignmentOrder(assignmentId, order));
    	}
    	
    	//serialise the XmlList back to xml
    	XmlList<AssignmentOrder> updatedXmlList = new XmlList<AssignmentOrder>(assignmentOrder);
    	String updatedXml = XmlMarshaller.marshal(updatedXmlList);
		
		//and save it
		props.addProperty(ASSIGNMENT_ORDER_PROP, updatedXml);
		this.siteService.save(site);
    	
    }
    
    /*
    public GradebookUiConfiguration getGradebookUiConfiguration() {
    	//the front end will set this into the DOM so the JS can pick it up. need to include siteid, toolid etc.
    	//alternatxively can pass in the sited to get the config
    	
    	return null;
    }
    */
    
    
}
