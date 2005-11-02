package org.sakaiproject.component.section.support;

import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.uuid.UuidManager;
import org.sakaiproject.api.section.CourseManager;
import org.sakaiproject.api.section.SectionManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.component.section.EnrollmentRecordImpl;
import org.sakaiproject.component.section.InstructorRecordImpl;
import org.sakaiproject.component.section.TeachingAssistantRecordImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of California and The Regents of the University of Michigan
*
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

/**
 * Provides integration support using the standalone hibernate implementation.
 */
public class IntegrationSupportImpl extends HibernateDaoSupport implements IntegrationSupport {
	private static final Log log = LogFactory.getLog(IntegrationSupportImpl.class);
	
	private CourseManager courseManager;
	private SectionManager sectionManager;
	private UserManager userManager;
	private UuidManager uuidManager;
	
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
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.getNamedQuery("getUsersSiteMemberships");
				q.setParameter("userUid", userUid);
				return q.list();
			}
		};
		return getHibernateTemplate().executeFind(hc);
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
			ir.setUuid(uuidManager.createUuid());
			getHibernateTemplate().save(ir);
			record = ir;
		} else if(role.isTeachingAssistant()) {
			TeachingAssistantRecordImpl tar = new TeachingAssistantRecordImpl(course, user);
			tar.setUuid(uuidManager.createUuid());
			getHibernateTemplate().save(tar);
			record = tar;
		} else if(role.isStudent()) {
			EnrollmentRecordImpl sr = new EnrollmentRecordImpl(course, null, user);
			sr.setUuid(uuidManager.createUuid());
			getHibernateTemplate().save(sr);
			record = sr;
		} else {
			throw new RuntimeException("You can not add a user without a role");
		}
		return record;
	}

	public void removeSiteMembership(final String userUid, final String siteContext) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				log.info("getting query object");
				Query q = session.getNamedQuery("loadSiteParticipation");
				log.info("query = " + q);
				q.setParameter("userUid", userUid);
				q.setParameter("siteContext", siteContext);
				return q.uniqueResult();
			}
		};
		ParticipationRecord record = (ParticipationRecord)getHibernateTemplate().execute(hc);
		if(record != null) {
			log.info("Preparing to delete record " + record);
			getHibernateTemplate().delete(record);
		}
	}

	public ParticipationRecord addSectionMembership(String userUid, String sectionUuid, Role role) {
		return sectionManager.addSectionMembership(userUid, role, sectionUuid);
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

	public void setUuidManager(UuidManager uuidManager) {
		this.uuidManager = uuidManager;
	}
}

