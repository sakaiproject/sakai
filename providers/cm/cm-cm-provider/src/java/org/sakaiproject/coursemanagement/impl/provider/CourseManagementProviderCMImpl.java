package org.sakaiproject.coursemanagement.impl.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.site.api.Course;
import org.sakaiproject.site.api.CourseManagementProvider;
import org.sakaiproject.site.api.CourseMember;
import org.sakaiproject.site.api.Term;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

public class CourseManagementProviderCMImpl implements CourseManagementProvider {

	private static final Log log = LogFactory.getLog(CourseManagementProviderCMImpl.class);
	
	private CourseManagementService cmService;
	private GroupProvider cmGroupProvider;
	private UserDirectoryService uds;

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("CourseManagementProviderCMImpl");

	public Course getCourse(String courseId) {
		return getLegacyCourseFromCmSection(cmService.getSection(courseId));
	}

	private Course getLegacyCourseFromCmSection(Section section) {
		// Construct the course object
		Course course = new Course();

		// Get the CM section with this eid, along with any other needed CM objects
		CourseOffering co = cmService.getCourseOffering(section.getCourseOfferingEid());
		AcademicSession as = co.getAcademicSession();
		
		// Get the members
		List members = getCourseMembers(section.getEid());

		// Populate the course object
		course.setCrossListed(null); // Crosslisting is handled elsewhere in CM
		course.setId(section.getEid());
		course.setMembers(members);
		course.setSubject(co.getEid()); // TODO: What does subject map to in CM?
		course.setTermId(as.getEid());
		course.setTitle(section.getTitle());
		
		return course;
	}

	public String getCourseId(Term term, List requiredFields) {
		StringBuffer sb = new StringBuffer();
		if (term != null) {
			sb.append(term.getYear());
			sb.append(",");
			sb.append(term.getTerm());
		} else {
			sb.append(",,");
		}
		
		for (int i = 0; i < requiredFields.size(); i++)
		{
			sb.append(",");
			sb.append((String) requiredFields.get(i));
		}
		return sb.toString();
	}

	public List getCourseIdRequiredFields() {
		List fields = new ArrayList();
		fields.add(rb.getString("required_fields_subject"));
		fields.add(rb.getString("required_fields_course"));
		fields.add(rb.getString("required_fields_section"));
		return fields;
	}

	public List getCourseIdRequiredFieldsSizes() {
		List fields = new ArrayList();
		fields.add(new Integer(8));
		fields.add(new Integer(3));
		fields.add(new Integer(3));
		return fields;
	}

	public List getCourseMembers(String courseId) {
		Section section = cmService.getSection(courseId);
		Map userRoles = cmGroupProvider.getUserRolesForGroup(courseId);
		List members = new ArrayList();
		for(Iterator iter = userRoles.keySet().iterator(); iter.hasNext();) {
			String userEid = (String)iter.next();
			String role = (String)userRoles.get(userEid);
			CourseMember courseMember = new CourseMember();
			courseMember.setCourse(courseId);
			courseMember.setCredits(null); // TODO: Get the credits from the CM Enrollment 
			String displayName = null;
			try {
				displayName = uds.getUserByEid(userEid).getDisplayName();
			} catch (UserNotDefinedException unde) {
				log.warn("UserDirService can't find user " + userEid + " even though this eid was provided by the CM Service.");
			}
			courseMember.setName(displayName);
			courseMember.setRole(role);
			courseMember.setProviderRole(role);
			courseMember.setSection(section.getTitle());
			courseMember.setUniqname(userEid);
			
			// Add the course member to the list
			members.add(courseMember);
		}
		return members;
	}

	public String getCourseName(String courseId) {
		Section sec = cmService.getSection(courseId);
		return sec.getTitle();
	}

	public List getInstructorCourses(String instructorId, String termYear, String termTerm) {
		Set sections = cmService.findInstructingSections(instructorId);
		List courses = new ArrayList();
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			Section section = (Section)iter.next();
			Course course = getLegacyCourseFromCmSection(section);
			AcademicSession as = cmService.getAcademicSession(course.getTermId());
			
			// TODO: How do we convert between these term strings and the CM AcademicSession?
			if(as.getTitle().indexOf(termYear) != -1 && as.getTitle().indexOf(termTerm) != -1) {
				courses.add(course);
			}
		}
		return courses;
	}

	public String getProviderId(List providerIdList) {
		StringBuffer sb = new StringBuffer();
		for(Iterator iter = providerIdList.iterator(); iter.hasNext();) {
			String id = (String)iter.next();
			sb.append(id);
			if(iter.hasNext()) {
				sb.append("+");
			}
		}
		return sb.toString();
	}

	// Dependency injection
	
	public void setCmGroupProvider(GroupProvider cmGroupProvider) {
		this.cmGroupProvider = cmGroupProvider;
	}

	public void setCmService(CourseManagementService cmService) {
		this.cmService = cmService;
	}

	public void setUds(UserDirectoryService uds) {
		this.uds = uds;
	}

}
