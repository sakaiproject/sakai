/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section.support;

import java.sql.Time;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.Query;

import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.component.section.EnrollmentRecordImpl;
import org.sakaiproject.component.section.InstructorRecordImpl;
import org.sakaiproject.component.section.TeachingAssistantRecordImpl;
import org.sakaiproject.section.api.CourseManager;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.exception.RoleConfigurationException;
import org.sakaiproject.section.api.facade.Role;

/**
 * Provides integration support using the standalone hibernate implementation.
 */
@Slf4j
public class IntegrationSupportImpl extends HibernateDaoSupport implements IntegrationSupport {
	private CourseManager courseManager;
	private SectionManager sectionManager;
	private UserManager userManager;
	
	public Course createCourse(String siteContext, String title, boolean externallyManaged,
			boolean selfRegistrationAllowed, boolean selfSwitchingAllowed) {
		return courseManager.createCourse(siteContext, title, selfRegistrationAllowed,
				selfSwitchingAllowed, externallyManaged);
	}

	public CourseSection createSection(String courseUuid, String title, String category, Integer maxEnrollments,
			String location, Time startTime, Time endTime, boolean monday, boolean tuesday,
			boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
		return sectionManager.addSection(courseUuid, title, category, maxEnrollments, location,
				startTime, endTime, monday, tuesday, wednesday, thursday, friday,
				saturday, sunday);
	}

	public User createUser(String userUid, String displayName, String sortName, String displayId) {
		return userManager.createUser(userUid, displayName, sortName, displayId);
	}

	public User findUser(final String userUid) {
		return userManager.findUser(userUid);
	}

	public List getAllSiteMemberships(final String userUid) {
		HibernateCallback<List> hc = session -> {
            Query q = session.getNamedQuery("getUsersSiteMemberships");
            q.setParameter("userUid", userUid);
            return q.list();
        };
		return getHibernateTemplate().execute(hc);
	}

	public Set getAllSectionMemberships(String userUid, String siteContext) {
		Course course = sectionManager.getCourse(siteContext);
		return sectionManager.getSectionEnrollments(userUid, course.getUuid());
	}

	public ParticipationRecord addSiteMembership(String userUid, String siteContext, Role role) {
		User user = findUser(userUid);
		Course course = sectionManager.getCourse(siteContext);
		
		ParticipationRecord record = null;
		if(role.isInstructor()) {
			InstructorRecordImpl ir = new InstructorRecordImpl(course, user);
			ir.setUuid(UUID.randomUUID().toString());
			getHibernateTemplate().save(ir);
			record = ir;
		} else if(role.isTeachingAssistant()) {
			TeachingAssistantRecordImpl tar = new TeachingAssistantRecordImpl(course, user);
			tar.setUuid(UUID.randomUUID().toString());
			getHibernateTemplate().save(tar);
			record = tar;
		} else if(role.isStudent()) {
			EnrollmentRecordImpl sr = new EnrollmentRecordImpl(course, null, user);
			sr.setUuid(UUID.randomUUID().toString());
			getHibernateTemplate().save(sr);
			record = sr;
		} else {
			throw new RuntimeException("You can not add a user without a role");
		}
		return record;
	}

	public void removeSiteMembership(final String userUid, final String siteContext) {
		HibernateCallback hc = session -> {
            log.info("getting query object");
            Query q = session.getNamedQuery("loadSiteParticipation");
            log.info("query = " + q);
            q.setParameter("userUid", userUid);
            q.setParameter("siteContext", siteContext);
            return q.uniqueResult();
        };
		ParticipationRecord record = (ParticipationRecord)getHibernateTemplate().execute(hc);
		if(record != null) {
			log.info("Preparing to delete record " + record);
			getHibernateTemplate().delete(record);
		}
	}

	public ParticipationRecord addSectionMembership(String userUid, String sectionUuid, Role role) {
		ParticipationRecord record;
		try {
			record = sectionManager.addSectionMembership(userUid, role, sectionUuid);
			return record;
		} catch (RoleConfigurationException rce) {
			throw new RuntimeException(rce);
		}
	}

	public void removeSectionMembership(String userUid, String sectionUuid) {
		sectionManager.dropSectionMembership(userUid, sectionUuid);
	}

	// Dependency Injection
	
	public void setSectionManager(SectionManager sectionManager) {
		this.sectionManager = sectionManager;
	}

	public void setCourseManager(CourseManager courseManager) {
		this.courseManager = courseManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
}
