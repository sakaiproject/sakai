/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.component.section;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.uuid.UuidManager;
import org.sakaiproject.api.section.CourseManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Hibernate implementation of CourseManager.  Useful for loading data in standalone mode.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseManagerHibernateImpl extends HibernateDaoSupport
	implements CourseManager {

	private static final Log log = LogFactory.getLog(CourseManagerHibernateImpl.class);

	protected UuidManager uuidManager;

	public Course createCourse(final String siteContext, final String title,
			final boolean selfRegAllowed, final boolean selfSwitchingAllowed,
			final boolean externallyManaged) {
		
		if(log.isDebugEnabled()) log.debug("Creating a new course offering named " + title);

		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				CourseImpl course = new CourseImpl();
        		course.setExternallyManaged(externallyManaged);
        		course.setSelfRegistrationAllowed(selfRegAllowed);
        		course.setSelfSwitchingAllowed(selfSwitchingAllowed);
        		course.setSiteContext(siteContext);
        		course.setTitle(title);
        		course.setUuid(uuidManager.createUuid());
        		session.save(course);
        		return course;
			};
		};

		return (Course)getHibernateTemplate().execute(hc);
	}

	public boolean courseExists(final String siteContext) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				Query q = session.getNamedQuery("loadCourseBySiteContext");
				q.setParameter("siteContext", siteContext);
				return q.uniqueResult();
			};
		};

		return getHibernateTemplate().execute(hc) != null;
	}

	public void removeUserFromAllSections(final String userUuid, final String siteContext) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				Query q = session.getNamedQuery("findUserSectionMembershipsInSite");
				q.setParameter("userUid", userUuid);
				q.setParameter("siteContext", siteContext);
				List results = q.list();
				for(Iterator iter = results.iterator(); iter.hasNext();) {
					ParticipationRecord record = (ParticipationRecord) iter.next();
					if(log.isInfoEnabled()) log.info("User " + userUuid +
							" removed from the site... removing user from section " +
							record.getLearningContext().getTitle());
					session.delete(record);
				}
				return null;
			};
		};
		getHibernateTemplate().execute(hc);
	}	

	public ParticipationRecord addInstructor(final User user, final Course course) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				InstructorRecordImpl pr = new InstructorRecordImpl(course, user);
				pr.setUuid(uuidManager.createUuid());
				session.save(pr);
				return pr;
			}
		};
		return (ParticipationRecord)getHibernateTemplate().execute(hc);
	}

	public ParticipationRecord addEnrollment(final User user, final Course course) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				EnrollmentRecordImpl enr = new EnrollmentRecordImpl(course, "enrolled", user);
				enr.setUuid(uuidManager.createUuid());
				session.save(enr);
				return enr;
			}
		};
		return (ParticipationRecord)getHibernateTemplate().execute(hc);
	}

	public ParticipationRecord addTA(final User user, final Course course) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				TeachingAssistantRecordImpl ta = new TeachingAssistantRecordImpl(course, user);
				ta.setUuid(uuidManager.createUuid());
				session.save(ta);
				return ta;
			}
		};
		return (ParticipationRecord)getHibernateTemplate().execute(hc);
	}
	
	// Dependency injection

	public void setUuidManager(UuidManager uuidManager) {
		this.uuidManager = uuidManager;
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
