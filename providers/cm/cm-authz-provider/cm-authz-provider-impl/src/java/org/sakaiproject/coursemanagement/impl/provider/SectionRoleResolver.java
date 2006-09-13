package org.sakaiproject.coursemanagement.impl.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;

public class SectionRoleResolver implements RoleResolver {
	private static final Log log = LogFactory.getLog(SectionRoleResolver.class);

	/** Map of CM section roles to Sakai roles */
	Map roleMap;

	/** The Sakai role to use for official instructors of EnrollmentSets */
	String officialInstructorRole;

	/** The Sakai role to use for official enrollments in EnrollmentSets */
	String enrollmentRole;

	/**
	 * {@inheritDoc}
	 */
	public Map getUserRoles(CourseManagementService cmService, Section section) {
		Map userRoleMap = new HashMap();

		EnrollmentSet enrSet = section.getEnrollmentSet();
		if(log.isDebugEnabled()) log.debug( "EnrollmentSet  " + enrSet + " is attached to section " + section.getEid());
		if(enrSet != null) {
			// Check for official instructors
			Set officialInstructors = cmService.getInstructorsOfRecordIds(enrSet.getEid());
			for(Iterator iter = officialInstructors.iterator(); iter.hasNext();) {
				userRoleMap.put(iter.next(), officialInstructorRole);
			}

			// Check for enrollments
			Set enrollments = cmService.getEnrollments(section.getEnrollmentSet().getEid());
			for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
				Enrollment enr = (Enrollment)iter.next();
				if( ! userRoleMap.containsKey(enr.getUserId())) {
					// If they are an official instructor and also enrolled (huh?), defer to the instructor status
					userRoleMap.put(enr.getUserId(), enrollmentRole);
				}
			}
		}
		
		// Check for memberships
		Set memberships = cmService.getSectionMemberships(section.getEid());
		for(Iterator iter = memberships.iterator(); iter.hasNext();) {
			Membership membership = (Membership)iter.next();
			// Only add the membership role if the user isn't enrolled or an official instructor(these take precedence)
			if( ! userRoleMap.containsKey(membership.getUserId())) {
				String convertedRole = convertRole(membership.getRole());
				if(convertedRole != null) {
					userRoleMap.put(membership.getUserId(), convertedRole);
				}
			}
		}
		return userRoleMap;
	}

	public Map getGroupRoles(CourseManagementService cmService, String userEid) {
		// Start with the sectionEid->role map
		Map groupRoleMap = cmService.findSectionRoles(userEid);
		
		// Convert these roles to Sakai roles
		Set iterSet = new HashSet(groupRoleMap.keySet());
		for(Iterator iter = iterSet.iterator(); iter.hasNext();) {
			String key = (String)iter.next();
			groupRoleMap.put(key, convertRole((String)groupRoleMap.get(key)));
		}

		// Next add all enrollments to the sectionEid->role map, overriding memberships
		Set enrolledSections = cmService.findEnrolledSections(userEid);
		if(log.isDebugEnabled()) log.debug("Found " + enrolledSections.size() + " currently enrolled sections for user " + userEid);
		for(Iterator secIter = enrolledSections.iterator(); secIter.hasNext();) {
			Section section = (Section)secIter.next();
			if(log.isDebugEnabled()) log.debug(userEid + " is enrolled in an enrollment set attached to section " + section.getEid());
			groupRoleMap.put(section.getEid(), enrollmentRole);
		}

		// Finally, add the official instructors, overriding any other roles if necessary
		Set instructingSections = cmService.findInstructingSections(userEid);
		for(Iterator iter = instructingSections.iterator(); iter.hasNext();) {
			Section instructingSection = (Section)iter.next();
			groupRoleMap.put(instructingSection.getEid(), officialInstructorRole);
		}
		
		if(log.isDebugEnabled()) {
			for(Iterator iter = groupRoleMap.keySet().iterator(); iter.hasNext();) {
				String sectionEid = (String)iter.next();
				log.debug("User " + userEid + " has role " + groupRoleMap.get(sectionEid) + " in " + sectionEid);
			}
		}
		return groupRoleMap;
	}
	
	public String convertRole(String cmRole) {
		if (cmRole == null) {
			log.warn("Can not convert CM role 'null' to a sakai role.");
			return null;
		}
		String sakaiRole = (String)roleMap.get(cmRole);
		if(sakaiRole== null) {
			log.warn("Unable to find sakai role for CM role " + cmRole);
			return null;
		} else {
			return sakaiRole;
		}
	}

	// Dependency injection
	
	public void setRoleMap(Map roleMap) {
		this.roleMap = roleMap;
	}
		
	public void setOfficialInstructorRole(String officialInstructorRole) {
		this.officialInstructorRole = officialInstructorRole;
	}

	public void setEnrollmentRole(String enrollmentRole) {
		this.enrollmentRole = enrollmentRole;
	}

}
