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
package org.sakaiproject.component.section;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Query;
import org.sakaiproject.section.api.CourseManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * Hibernate implementation of CourseManager.  Useful for loading data in standalone mode.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseManagerHibernateImpl extends HibernateDaoSupport
	implements CourseManager {

	private static final Logger log = LoggerFactory.getLogger(CourseManagerHibernateImpl.class);

	public Course createCourse(final String siteContext, final String title,
			final boolean selfRegAllowed, final boolean selfSwitchingAllowed,
			final boolean externallyManaged) {
		
		if(log.isDebugEnabled()) log.debug("Creating a new course offering named " + title);

		HibernateCallback<Course> hc = session -> {
            CourseImpl course = new CourseImpl();
            course.setExternallyManaged(externallyManaged);
            course.setSelfRegistrationAllowed(selfRegAllowed);
            course.setSelfSwitchingAllowed(selfSwitchingAllowed);
            course.setSiteContext(siteContext);
            course.setTitle(title);
            course.setUuid(UUID.randomUUID().toString());
            session.save(course);
            return course;
        };

		return getHibernateTemplate().execute(hc);
	}

	public boolean courseExists(final String siteContext) {
		HibernateCallback hc = session -> {
            Query q = session.getNamedQuery("loadCourseBySiteContext");
            q.setParameter("siteContext", siteContext);
            return q.uniqueResult();
        };

		return getHibernateTemplate().execute(hc) != null;
	}

	public ParticipationRecord addInstructor(final User user, final Course course) {
		HibernateCallback<ParticipationRecord> hc = session -> {
            InstructorRecordImpl pr = new InstructorRecordImpl(course, user);
            pr.setUuid(UUID.randomUUID().toString());
            session.save(pr);
            return pr;
        };
		return getHibernateTemplate().execute(hc);
	}

	public ParticipationRecord addEnrollment(final User user, final Course course) {
		HibernateCallback<ParticipationRecord> hc = session -> {
            EnrollmentRecordImpl enr = new EnrollmentRecordImpl(course, "enrolled", user);
            enr.setUuid(UUID.randomUUID().toString());
            session.save(enr);
            return enr;
        };
		return getHibernateTemplate().execute(hc);
	}

	public ParticipationRecord addTA(final User user, final Course course) {
		HibernateCallback<ParticipationRecord> hc = session -> {
            TeachingAssistantRecordImpl ta = new TeachingAssistantRecordImpl(course, user);
            ta.setUuid(UUID.randomUUID().toString());
            session.save(ta);
            return ta;
        };
		return getHibernateTemplate().execute(hc);
	}
	
	public void removeCourseMembership(final String userUid, final Course course) {
		HibernateCallback hc = session -> {
            Query q = session.getNamedQuery("loadSiteParticipation");
            q.setParameter("siteContext", course.getSiteContext());
            q.setParameter("userUid", userUid);
            Object result = q.uniqueResult();
            if(result != null) {
                session.delete(result);
                if(log.isInfoEnabled()) log.info("Site membership for " + userUid + " in course " +
                        course + " has been deleted");
            } else {
                if(log.isInfoEnabled()) log.info("Could not find membership for " +
                        userUid + " in course " + course);
            }
            return null;
        };
		getHibernateTemplate().execute(hc);
		
		// Make sure we remove any orphaned section memberships
		removeOrphans(course.getSiteContext());
		
	}
	
	/**
	 * @inheritDoc
	 */
	public void removeOrphans(final String siteContext) {
		final Set userUids = getSiteMemberIds(siteContext);
		if(userUids == null || userUids.isEmpty()) {
			return;
		}
		HibernateCallback hc = session -> {
            Query q = session.getNamedQuery("findOrphanedSectionMemberships");
            q.setParameter("siteContext", siteContext);
            q.setParameterList("userUids", userUids);
            int deleted = 0;
            for(Iterator iter = q.iterate(); iter.hasNext(); deleted++) {
                session.delete(iter.next());
            }
            if(log.isInfoEnabled()) log.info(deleted + " section memberships deleted");
            return null;
        };
		getHibernateTemplate().execute(hc);
	}

	/**
	 * Gets only the user IDs of the site members
	 * 
	 * @param siteContext
	 * @return
	 */
	private Set<String> getSiteMemberIds(final String siteContext) {
		HibernateCallback<Set<String>> hc = session -> {
            Query q = session.getNamedQuery("findSiteMemberUserUids");
            q.setParameter("siteContext", siteContext);
            return new HashSet<>(q.list());
        };

		return getHibernateTemplate().execute(hc);
	}
}

