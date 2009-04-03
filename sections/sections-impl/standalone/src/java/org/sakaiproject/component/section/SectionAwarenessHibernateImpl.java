/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate based implementation of SectionAwareness.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionAwarenessHibernateImpl extends HibernateDaoSupport
        implements SectionAwareness {

	private static final Log log = LogFactory.getLog(SectionAwarenessHibernateImpl.class);

	/**
	 * @inheritDoc
	 */
	public List getSections(final String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting sections for context " + siteContext);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	// Get the sections
                Query secQuery = session.getNamedQuery("findSectionsBySiteContext");
                secQuery.setParameter("context", siteContext);
                List list = secQuery.list();

                // Get the teams
                Query teamQuery = session.getNamedQuery("findTeamsBySiteContext");
                teamQuery.setParameter("context", siteContext);

                // Add the teams after the sections
                list.addAll(teamQuery.list());
                return list;
            }
        };
        return getHibernateTemplate().executeFind(hc);
    }

	/**
	 * @inheritDoc
	 */
	public List getSectionCategories(String siteContext) {
		ResourceBundle bundle = ResourceBundle.getBundle(SectionManagerHibernateImpl.CATEGORY_BUNDLE, Locale.US);

		Enumeration keys = bundle.getKeys();
		List categoryIds = new ArrayList();
		while(keys.hasMoreElements()) {
			categoryIds.add(keys.nextElement());
		}
		Collections.sort(categoryIds);
		return categoryIds;
	}
	
	/**
	 * @inheritDoc
	 */
	public CourseSection getSection(final String sectionUuid) {
    	if(log.isDebugEnabled()) log.debug("Getting section with uuid=" + sectionUuid);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	return getSection(sectionUuid, session);
            }
        };
        return (CourseSection)getHibernateTemplate().execute(hc);
	}

	private CourseSection getSection(final String sectionUuid, Session session) throws HibernateException {
        Query q = session.getNamedQuery("loadSectionByUuid");
        q.setParameter("uuid", sectionUuid);
        Object section = q.uniqueResult();
        if(section == null) {
        	throw new IllegalArgumentException("No section exists with uuid=" + sectionUuid);
        } else {
        	return (CourseSection)section;
        }
	}

	/**
	 * @inheritDoc
	 */
	public List getSiteMembersInRole(final String siteContext, final Role role) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourse(siteContext, session);
            	if(course == null) {
            		if(log.isInfoEnabled()) log.info("No course founf for siteContext " + siteContext);
            		return new ArrayList();
            	}
            	Query q;
                if(role.isInstructor()) {
        			q = session.getNamedQuery("findSiteInstructors");
        		} else if(role.isStudent()) {
        			q = session.getNamedQuery("findSiteEnrollments");
        		} else if(role.isTeachingAssistant()) {
        			q = session.getNamedQuery("findSiteTAs");
        		} else {
        			throw new IllegalArgumentException("There are no users without a role in a site.");
        		}
                q.setParameter("course", course);
                return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}

	private Course getCourse(String siteContext, Session session) throws HibernateException {
        Query q = session.getNamedQuery("loadCourseBySiteContext");
        q.setParameter("siteContext", siteContext);
        Object course = q.uniqueResult();
    	return (Course)course;
	}
	
	/**
	 * The sakai implementation will not use the database to do this kind of searching,
	 * so I'll skip doing optimizations here.
	 * 
	 * @inheritDoc
	 */
	public List findSiteMembersInRole(final String siteContext, final Role role, final String pattern) {
		List fullList = getSiteMembersInRole(siteContext, role);
		List filteredList = new ArrayList();
		for(Iterator iter = fullList.iterator(); iter.hasNext();) {
			ParticipationRecord record = (ParticipationRecord)iter.next();
			User user = record.getUser();
			if(user.getDisplayName().toLowerCase().startsWith(pattern.toLowerCase()) ||
			   user.getSortName().toLowerCase().startsWith(pattern.toLowerCase()) ||
			   user.getDisplayId().toLowerCase().startsWith(pattern.toLowerCase())) {
				filteredList.add(record);
			}
		}
		return filteredList;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isSiteMemberInRole(final String siteContext, final String userUid, final Role role) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourse(siteContext, session);
            	if(course == null) {
            		if(log.isInfoEnabled()) log.info("No course founf for siteContext " + siteContext);
            		return Boolean.valueOf(false);
            	}
                Query q = session.getNamedQuery("checkForSiteMembershipInRole");
                q.setParameter("course", course);
                q.setParameter("userUid", userUid);
                List list = q.list();
                return checkRole(role, list);
            }
        };
        return ((Boolean)getHibernateTemplate().execute(hc)).booleanValue();
	}

	/**
	 * This code pops up a few places...
	 * 
	 * @param role The role to check
	 * @param list A list of participant records returned by hibernate
	 * @return Whether the list of participation record includes one record of
	 * the specified role.
	 */
	private Boolean checkRole(final Role role, List list) {
		if(list.size() == 1) {
        	ParticipationRecord record = (ParticipationRecord)list.get(0);
        	if(record.getRole().equals(role)) {
        		if(log.isDebugEnabled()) log.debug("This user is in role " + role.getDescription());
            	return Boolean.valueOf(true);
        	} else {
        		if(log.isDebugEnabled()) log.debug("This user is not in role " + role.getDescription());
            	return Boolean.valueOf(false);
        	}
        } else if(list.size() == 0){
    		if(log.isDebugEnabled()) log.debug("This user has no role in this learning context.");
        	return Boolean.valueOf(false);
        } else {
        	throw new RuntimeException("There are multiple participation records for this user in this learning context.");
        }
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionMembers(final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.getNamedQuery("findSectionMembers");
                q.setParameter("sectionUuid", sectionUuid);
                return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionMembersInRole(final String sectionUuid, final Role role) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSection section = getSection(sectionUuid, session);
            	Query q;
                if(role.isInstructor()) {
                	q = session.getNamedQuery("findSectionInstructors");
        		} else if(role.isStudent()) {
                    q = session.getNamedQuery("findSectionStudents");
        		} else if(role.isTeachingAssistant()) {
                    q = session.getNamedQuery("findSectionTAs");
        		} else {
        			throw new IllegalArgumentException("There are no users without a role in a section.");
        		}
                q.setParameter("section", section);
                return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}

	/**
	 * @inheritDoc
	 */
	public boolean isSectionMemberInRole(final String sectionUuid, final String userUid, final Role role) {
        HibernateCallback hc = new HibernateCallback(){
	        public Object doInHibernate(Session session) throws HibernateException {
	        	CourseSection section = getSection(sectionUuid, session);
	            Query q = session.getNamedQuery("checkForSectionMembershipInRole");
	            q.setParameter("section", section);
	            q.setParameter("userUid", userUid);
	            List list = q.list();
	            return checkRole(role, list);
        	}
        };
        return ((Boolean)getHibernateTemplate().execute(hc)).booleanValue();
	}

	/**
	 * @inheritDoc
	 */
	public String getSectionName(final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
	        public Object doInHibernate(Session session) throws HibernateException {
	            Query q = session.getNamedQuery("loadSectionName");
	            q.setParameter("sectionUuid", sectionUuid);
	            Object name = q.uniqueResult();
	            if(name != null) {
	            	if(log.isDebugEnabled()) log.debug("Section " + sectionUuid + " does not exist.");
	            }
	            return name;
        	}
        };
        return (String)getHibernateTemplate().execute(hc);
	}

	/**
	 * @inheritDoc
	 */
	public String getSectionCategory(final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
	        public Object doInHibernate(Session session) throws HibernateException {
	            Query q = session.getNamedQuery("loadSectionCategory");
	            q.setParameter("sectionUuid", sectionUuid);
	            Object category = q.uniqueResult();
	            if(category == null) {
	            	if(log.isDebugEnabled()) log.debug("Section " + sectionUuid + " does not exist.");
	            }
            	return category;
	        }
        };
        return (String)getHibernateTemplate().execute(hc);
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionsInCategory(final String siteContext, final String categoryId) {
        HibernateCallback hc = new HibernateCallback(){
	        public Object doInHibernate(Session session) throws HibernateException {
	            Query q = session.getNamedQuery("findSectionsByCategory");
	            q.setParameter("categoryId", categoryId);
	            q.setParameter("siteContext", siteContext);
	            return q.list();
	        }
        };
        return getHibernateTemplate().executeFind(hc);
	}

	/**
	 * @inheritDoc
	 */
	public String getCategoryName(String categoryId, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(SectionManagerHibernateImpl.CATEGORY_BUNDLE, locale);
		String name;
		try {
			name = bundle.getString(categoryId);
		} catch(MissingResourceException mre) {
			if(log.isInfoEnabled()) log.info("Could not find the name for category id = " + categoryId + " in locale " + locale.getDisplayName());
			name = null;
		}
		return name;
	}

	/**
	 * @inheritDoc
	 */
	public List getUnassignedMembersInRole(final String siteContext, final Role role) {
        HibernateCallback hc = new HibernateCallback(){
	        public Object doInHibernate(Session session) throws HibernateException {
	        	Course course = getCourse(siteContext, session);
	            Query q;
	            if(role.isStudent()) {
	            	q = session.getNamedQuery("findUnsectionedStudents");
	            } else if (role.isTeachingAssistant()) {
	            	q = session.getNamedQuery("findUnsectionedTas");
	            } else {
	            	if(log.isInfoEnabled()) log.info(role + " is never assigned to sections, so unsectioned members is empty.");
	            	return new ArrayList();
	            }
	            q.setParameter("courseUuid", course.getUuid());
	            return q.list();
	        }
        };
        return getHibernateTemplate().executeFind(hc);
	}

}
