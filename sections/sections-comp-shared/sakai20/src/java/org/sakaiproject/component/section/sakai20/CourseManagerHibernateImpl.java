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

package org.sakaiproject.component.section.sakai20;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.CourseManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.component.section.facade.impl.sakai20.AuthzSakaiImpl;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Hibernate implementation of CourseManager.  Supports adding a Course, but not
 * adding members.  Also observes changes to realms and removes students and TAs
 * from section memberships when they are removed from a site.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseManagerHibernateImpl extends HibernateDaoSupport
	implements CourseManager, Observer {

	private static final Log log = LogFactory.getLog(CourseManagerHibernateImpl.class);

//	private EventTrackingService eventTrackingService;

	public Course createCourse(final String siteContext, final String title,
			final boolean selfRegAllowed, final boolean selfSwitchingAllowed,
			final boolean externallyManaged) {
		
		if(log.isDebugEnabled()) log.debug("Creating a new section container named " + title);

		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				CourseImpl course = new CourseImpl();
        		course.setExternallyManaged(externallyManaged);
        		course.setSelfRegistrationAllowed(selfRegAllowed);
        		course.setSelfSwitchingAllowed(selfSwitchingAllowed);
        		course.setSiteContext(siteContext);
        		course.setTitle(title);
        		course.setUuid(siteContext);
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
				Object result = q.uniqueResult();
				return result;
			};
		};

		return getHibernateTemplate().execute(hc) != null;
	}

	/**
	 * Initializes this bean by registering as an observer on the realm service.
	 * This allows this bean to respond to changes in realms that affect sections
	 * and section memberships.
	 * 
	 * Since Sakai won't call the observer when the provider's memberships change,
	 * this should not register as an observer.  Culling orphaned section memberships
	 * will occur elsewhere.
	 */
	public void init() {
//		eventTrackingService.addObserver(this);
	}
	
	/**
	 * Update is called by the observable object to indicate a change has occurred.
	 * 
	 * Since Sakai does not call this observer each time site membership is
	 * changed, the implementation is now handled at runtime in SectionManager
	 * and in section awareness.
	 * 
	 */
	public void update(Observable o, Object arg) {
//		if (!(arg instanceof Event)) {
//			log.error(arg + " is not an event!");
//			return;
//		}
//
//		Event event = (Event)arg;
//		String function = event.getEvent();
//		if(function.equals(RealmService.SECURE_UPDATE_REALM)) {
//			// Get the site reference from the event
//			Reference realmRef = new Reference(event.getResource());
//			Reference entityRef = new Reference(realmRef.getId());
//
//			// If this isn't a site, we're done
//			if("org.sakaiproject.service.legacy.site.SiteService".equals(entityRef.getType())) {
//				if(log.isDebugEnabled()) log.debug("A realm update is triggering the " +
//					"removal of orphaned users from a site's sections.");
//
//				String siteRef = entityRef.getReference();
//				String siteContext = entityRef.getId();
//				
//				// TODO The logic of who is a TA/Student is now in two places... refactor!
//				// Remove all section memberships (student and ta) of anyone who is no longer a site member
//		        List list = SecurityService.unlockUsers(AuthzSakaiImpl.STUDENT_PERMISSION, siteRef);
//		        list.addAll(SecurityService.unlockUsers(AuthzSakaiImpl.TA_PERMISSION, siteRef));
//				Set userUids = new HashSet();
//				for(Iterator iter = list.iterator(); iter.hasNext();) {
//					org.sakaiproject.service.legacy.user.User user = (org.sakaiproject.service.legacy.user.User)iter.next();
//					userUids.add(user.getId());
//				}
//
//				log.info(userUids.size() + " users in site");
//				removeOrphans(siteContext, userUids);
//			}
//		}
	}
	
	/**
	 * @inheritDoc
	 */
	public void removeOrphans(final String siteContext) {
		final Set userUids = getSiteMemberIds();
		if(userUids == null || userUids.isEmpty()) {
			return;
		}
		
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.getNamedQuery("findOrphanedSectionMemberships");
				q.setParameter("siteContext", siteContext);
				q.setParameterList("userUids", userUids);
				int deleted = 0;
				for(Iterator iter = q.list().iterator(); iter.hasNext(); deleted++) {
					session.delete(iter.next());
				}
				if(deleted > 0 && log.isInfoEnabled()) log.info(deleted + " orphaned section memberships deleted");
				return null;
			}
		};
		getHibernateTemplate().execute(hc);
	}
	
	/**
	 * Gets only the user IDs of the members of the current site.
	 * 
	 * @return
	 */
	private Set getSiteMemberIds() {
		String siteRef = SakaiUtil.getSiteReference();
        List sakaiMembers = SecurityService.unlockUsers(AuthzSakaiImpl.STUDENT_PERMISSION, siteRef);

        Set members = new HashSet();
        
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.service.legacy.user.User sakaiUser = (org.sakaiproject.service.legacy.user.User)iter.next();
    		members.add(sakaiUser.getId());
        }
        return members;
	}

	public ParticipationRecord addInstructor(final User user, final Course course) {
		throw new RuntimeException("This method is not available in the sakai 2.0 service implementation");
	}

	public ParticipationRecord addEnrollment(final User user, final Course course) {
		throw new RuntimeException("This method is not available in the sakai 2.0 service implementation");
	}

	public ParticipationRecord addTA(final User user, final Course course) {
		throw new RuntimeException("This method is not available in the sakai 2.0 service implementation");
	}

	public void removeCourseMembership(String userUid, Course course) {
		throw new RuntimeException("This method is not available in the sakai 2.0 service implementation");
	}

	// Dependency injection
	
//	public void setEventTrackingService(EventTrackingService eventTrackingService) {
//		this.eventTrackingService = eventTrackingService;
//	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
