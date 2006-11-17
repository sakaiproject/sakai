/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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
package org.sakaiproject.coursemanagement.impl.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

/**
 * An adapter that allows the use of the legacy CourseManagementService (and
 * hence an unmodified site info / worksite setup) while using the new CourseManagementService
 * to provide enterprise data to Sakai.  This provider implementation may only be
 * used when Sakai is configured to use the CourseManagementGroupProvider implementation
 * of GroupProvider.  See /providers/component/src/webapp/WEB-INF/components.xml
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseManagementProviderCMImpl implements CourseManagementProvider {

	private static final Log log = LogFactory.getLog(CourseManagementProviderCMImpl.class);
	
	/** The new cm service **/
	private CourseManagementService cmService;
	
	/** Our CM group provider keep the logic for searching for users' roles in the CM hierarchy **/
	private GroupProvider cmGroupProvider;
	
	/** The Sakai UserDirectoryService */
	private UserDirectoryService uds;
	
	/** These roles are allowed to associate rosters (sections) with a site **/
	private List sectionMappingRoles;

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
		if(log.isDebugEnabled()) log.debug("Constructing getCourseId");

		sb.append(requiredFields.get(0)); // bio
		sb.append(requiredFields.get(1)); // 101
		sb.append("_");
		sb.append(term.getTerm());	// f
		sb.append(term.getYear()); // 2006
		sb.append("_");
		sb.append(requiredFields.get(2)); // lab1

		String id = sb.toString();
		if(log.isDebugEnabled()) log.debug("courseId constructed as: " + id);
		return id;
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
		fields.add(new Integer(3));
		fields.add(new Integer(3));
		fields.add(new Integer(4));
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
		if(log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer("Found the following course members for ");
			sb.append(courseId);
			sb.append(": ");
			for(Iterator iter = members.iterator(); iter.hasNext();) {
				CourseMember member = (CourseMember)iter.next();
				sb.append(member.getUniqname());
				if(iter.hasNext()) {
					sb.append(", ");
				}
			}
			log.debug(sb.toString());
		}
		return members;
	}

	public String getCourseName(String courseId) {
		Section sec = cmService.getSection(courseId);
		return sec.getTitle();
	}

	public List getInstructorCourses(String instructorId, String termYear, String termTerm) {
		Map groupRoleMap = cmGroupProvider.getGroupRolesForUser(instructorId);
		
		if(log.isDebugEnabled()) log.debug("Found the following section EIDs for instructor " + instructorId + ": " + groupRoleMap.keySet());
		List courses = new ArrayList();
		for(Iterator iter = groupRoleMap.keySet().iterator(); iter.hasNext();) {
			String sectionEid = (String)iter.next();
			String role = (String)groupRoleMap.get(sectionEid);
			
			// Does this role qualify the user to associate a section with a site?
			if( ! sectionMappingRoles.contains(role)) {
				continue;
			}
			
			Section section = cmService.getSection(sectionEid);
			Course course = getLegacyCourseFromCmSection(section);
			AcademicSession as = cmService.getAcademicSession(course.getTermId());
			
			// TODO: How do we convert between these term strings and the CM AcademicSession?
			if(as.getTitle().toLowerCase().indexOf(termYear.toLowerCase()) != -1 && as.getTitle().toLowerCase().indexOf(termTerm.toLowerCase()) != -1) {
				if(log.isDebugEnabled()) log.debug("Section " + section.getEid() + " matches the term " + termTerm + " " + termYear);
				courses.add(course);
			} else {
				if(log.isDebugEnabled()) log.debug("Section " + section.getEid() + " does not match the term " + termTerm + " " + termYear);
			}
		}
		return courses;
	}

	public String getProviderId(List providerIdList) {
		if(log.isDebugEnabled()) log.debug("Generating a provider id for " + providerIdList);
		StringBuffer sb = new StringBuffer();
		for(Iterator iter = providerIdList.iterator(); iter.hasNext();) {
			String id = (String)iter.next();
			sb.append(id);
			if(iter.hasNext()) {
				sb.append("+");
			}
		}
		if(log.isDebugEnabled()) log.debug("Provider id = " + sb.toString());
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

	public void setSectionMappingRoles(List sectionMappingRoles) {
		this.sectionMappingRoles = sectionMappingRoles;
	}

}
