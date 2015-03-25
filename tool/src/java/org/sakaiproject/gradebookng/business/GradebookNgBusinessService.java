package org.sakaiproject.gradebookng.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbGroupType;
import org.sakaiproject.gradebookng.business.util.XmlList;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
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
	
	public static final String ASSIGNMENT_ORDER_PROP = "gbng_assignment_order";
	public static final String PREFS_PROP_PREFIX = "gbng_prefs_";

	
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
	 * Get a list of assignments in the gradebook in the specified site
	 * 
	 * @param siteId the siteId
	 * @return a list of assignments or null if no gradebook
	 */
	@SuppressWarnings("unchecked")
	public List<Assignment> getGradebookAssignments(String siteId) {
		Gradebook gradebook = getGradebook(siteId);
		if(gradebook != null) {
			List<Assignment> assignments = gradebookService.getAssignments(gradebook.getUid());
			assignments = sortAssignments(siteId,assignments);			
			return assignments;
		}
		return null;
	}
	
	/**
	 * Sort the assignment list according to the criteria stored in the site property
	 * 
	 * @param assignments
	 * @return sorted list of assignments
	 */
	private List<Assignment> sortAssignments(String siteId, List<Assignment> assignments) {
		
		// The performance of this could be better. However we are talking about a tiny list here (<50 items)
		//What would be ideal is an order on the assignments in the db table
		
		//get the stored order
		List<AssignmentOrder> assignmentOrder = this.getAssignmentOrder(siteId);
		if(assignmentOrder == null) {
			//no order set, use natural order
			return assignments;
		}
		
		//create a copy of the list we can modify
		List<Assignment> rval = new ArrayList<>();
		rval.addAll(assignments);
		int size = rval.size();

		//iterate over assignments, then check the order list for matches.
		//if matched, insert null to ensure list size, remove assignment and add at the new position in the rval list
		//if not matched, continue.
		//add the end, remove nulls
		
		//TODO should this iteration be on the order list first? the order list is potentially less iterations...
		for(Assignment a : assignments) {
			
			for(AssignmentOrder o: assignmentOrder) {
				long assignmentId = o.getAssignmentId();

				if(assignmentId == a.getId().longValue()){
					
					int order = o.getOrder();
					
					//trim order to bounds of assignment list
					if(order < 0){
						order = 0;
					} else if(order >= size) {
						order = size-1; //0 based index
					}
					
					rval.add(null); //ensure size remains the same for the remove
					rval.remove(a); //remove item
					rval.add(order, a); //add at ordered position
					
				}
			}
		}
		
		//retain only the assignment objects
		rval.retainAll(assignments);
	
		return rval;
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
		
		Gradebook gradebook = this.getGradebook();
		if(gradebook == null) {
			return null;
		}
		
		List<User> students = this.getGradeableUsers(userUuids);
		
		//because this map is based on eid not uuid, we do the filtering later so we can save an iteration
		Map<String,String> courseGrades = this.getSiteCourseGrades();
				
		List<StudentGradeInfo> rval = new ArrayList<StudentGradeInfo>();
		
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
			sg.setCourseGrade(courseGrades.get(student.getEid()));
			
			//add the section info
			//this.courseManagementService.getSe
			
			rval.add(sg);
			
		}
		return rval;
		
	}

	
	
	/**
	 * Get the user prefs for this gradebook instance
	 * 
	 * CURRENTLY UNUSED
	 * 
	 * @param userUuid
	 * @return
	 */
	public GradebookUserPreferences getUserPrefs() {
		String siteId = this.getCurrentSiteId();
		String userUuid = this.getCurrentUserUuid();
		
		try {
			Site site = siteService.getSite(siteId);
			
			ResourceProperties props = site.getProperties();
			String xml = (String) props.get(PREFS_PROP_PREFIX + userUuid);
			
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
	 * CURRENTLY UNUSED
	 * 
	 * @param prefs
	 */
	public void saveUserPrefs (GradebookUserPreferences prefs) {
		String siteId = this.getCurrentSiteId();
		
		try {
			Site site = siteService.getSite(siteId);
			
			ResourcePropertiesEdit props = site.getPropertiesEdit();
			props.addProperty(PREFS_PROP_PREFIX + prefs.getUserUuid(), XmlMarshaller.marshal(prefs));
			siteService.save(site);
			
		} catch (IdUnusedException | JAXBException | PermissionException e) {
			e.printStackTrace();
		}
		 
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
		
		log.debug("Updated assignment order: " + updatedXml);
		
		this.siteService.save(site);
    	
    }
    
    /*
    public GradebookUiConfiguration getGradebookUiConfiguration() {
    	//the front end will set this into the DOM so the JS can pick it up. need to include siteid, toolid etc.
    	//alternatxively can pass in the sited to get the config
    	
    	return null;
    }
    */
    
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
    
    
}
