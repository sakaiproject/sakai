/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.SectionEnrollments;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.exception.MembershipException;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.section.api.facade.manager.Authn;
import org.sakaiproject.section.api.facade.manager.Context;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A standalone implementation of the Section Management API.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionManagerHibernateImpl extends HibernateDaoSupport implements
        SectionManager {

	private static final Log log = LogFactory.getLog(SectionManagerHibernateImpl.class);

	/** The resource bundle containing the category IDs and names */
	static final String CATEGORY_BUNDLE = "org.sakaiproject.component.section.CourseSectionCategories";

	// Fields configured via dep. injection
    protected Authn authn;
    protected Context context;

	/**
	 * {@inheritDoc}
	 */
	public List<CourseSection> getSections(final String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting sections for context " + siteContext);
    	return getHibernateTemplate().findByNamedQueryAndNamedParam("findSectionsBySiteContext", "context", siteContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CourseSection> getSectionsInCategory(final String siteContext, final String categoryId) {
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
	 * {@inheritDoc}
	 */
	public CourseSection getSection(final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
	        public Object doInHibernate(Session session) throws HibernateException {
	        	return getSection(sectionUuid, session);
	        }
        };
        return (CourseSection)getHibernateTemplate().execute(hc);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ParticipationRecord> getSiteInstructors(final String siteContext) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourseFromSiteContext(siteContext, session);
            	Query q = session.getNamedQuery("findSiteInstructors");
                q.setParameter("course", course);
                return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ParticipationRecord> getSiteTeachingAssistants(final String siteContext) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourseFromSiteContext(siteContext, session);
            	Query q = session.getNamedQuery("findSiteTAs");
                q.setParameter("course", course);
                return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<EnrollmentRecord> getSiteEnrollments(final String siteContext) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourseFromSiteContext(siteContext, session);
            	Query q = session.getNamedQuery("findSiteEnrollments");
                q.setParameter("course", course);
                return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map getEnrollmentCount(List sectionSet) {

		// TODO - not implemented
		
		return new HashMap();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String,List<ParticipationRecord>> getSectionTeachingAssistantsMap(List sectionSet)
	{
		ArrayList<String> siteGroupRefs = new ArrayList<String>(sectionSet.size());
		Map<String,List<ParticipationRecord>> sectionTaMap = new HashMap<String,List<ParticipationRecord>>();
	
		// TODO - not implemented
		
		return sectionTaMap;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<ParticipationRecord> getSectionTeachingAssistants(final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSection section = getSection(sectionUuid, session);
            	Query q = session.getNamedQuery("findSectionTAs");
                q.setParameter("section", section);
                return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<EnrollmentRecord> getSectionEnrollments(final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSection section = getSection(sectionUuid, session);
            	Query q = session.getNamedQuery("findSectionStudents");
                q.setParameter("section", section);
                return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<EnrollmentRecord> findSiteEnrollments(String siteContext, String pattern) {
		List<EnrollmentRecord> fullList = getSiteEnrollments(siteContext);
		List<EnrollmentRecord> filteredList = new ArrayList<EnrollmentRecord>();
		for(Iterator iter = fullList.iterator(); iter.hasNext();) {
			EnrollmentRecord record = (EnrollmentRecord)iter.next();
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
	 * {@inheritDoc}
	 */
	public String getCategoryName(String categoryId, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(CATEGORY_BUNDLE, locale);
		String name;
		try {
			name = bundle.getString(categoryId);
		} catch(MissingResourceException mre) {
			if(log.isDebugEnabled()) log.debug("Could not find the name for category id = " + categoryId + " in locale " + locale.getDisplayName());
			name = null;
		}
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getSectionCategories(String siteContext) {
		ResourceBundle bundle = ResourceBundle.getBundle(CATEGORY_BUNDLE);
		
		Enumeration keys = bundle.getKeys();
		List<String> categoryIds = new ArrayList<String>();
		while(keys.hasMoreElements()) {
			categoryIds.add(keys.nextElement().toString());
		}
		Collections.sort(categoryIds);
		return categoryIds;
	}

	private CourseSectionImpl getSection(final String sectionUuid, Session session) throws HibernateException {
        Query q = session.getNamedQuery("loadSectionByUuid");
        q.setParameter("uuid", sectionUuid);
        CourseSectionImpl section = (CourseSectionImpl)q.uniqueResult();
        if(section == null) {
        	throw new MembershipException("No section exists with uuid=" + sectionUuid);
        } else {
        	return section;
        }
	}

	private Course getCourseFromSiteContext(String siteContext, Session session) throws HibernateException {
        Query q = session.getNamedQuery("loadCourseBySiteContext");
        q.setParameter("siteContext", siteContext);
        Course course = (Course)q.uniqueResult();
        if(course == null) {
        	throw new MembershipException("No course exists for site = " + siteContext);
        } else {
        	return course;
        }
	}

	private CourseImpl getCourseFromUuid(String courseUuid, Session session) throws HibernateException {
        Query q = session.getNamedQuery("loadCourseByUuid");
        q.setParameter("uuid", courseUuid);
        CourseImpl course = (CourseImpl)q.uniqueResult();
        if(course == null) {
        	throw new MembershipException("No course exists with uuid = " + courseUuid);
        } else {
        	return course;
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public Course getCourse(final String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting course for context " + siteContext);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	return getCourseFromSiteContext(siteContext, session);
            }
        };
        return (Course)getHibernateTemplate().execute(hc);
	}

	/**
	 * {@inheritDoc}
	 */
	public SectionEnrollments getSectionEnrollmentsForStudents(final String siteContext, final Set studentUids) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourse(siteContext);
            	Query q = session.getNamedQuery("findSectionEnrollments");
            	q.setParameter("course", course);
            	q.setParameterList("studentUids", studentUids);
            	return q.list();
            }
        };
		if(studentUids == null || studentUids.isEmpty()) {
			if(log.isDebugEnabled()) log.debug("No student uids were passed to getSectionEnrollments.");
			return new SectionEnrollmentsImpl(new ArrayList());
		}
    	return new SectionEnrollmentsImpl(getHibernateTemplate().executeFind(hc));
	}

	/**
	 * {@inheritDoc}
	 */
    public EnrollmentRecord joinSection(final String sectionUuid, int maxSize) {
    	return joinSection(sectionUuid);
    }

	/**
	 * {@inheritDoc}
	 */
    public EnrollmentRecord joinSection(final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	try {
            		getSection(sectionUuid, session);
            	} catch (MembershipException me) {
            		log.error(me);
            		return null;
            	}
            	String userUid = authn.getUserUid(null);
            	String siteContext = context.getContext(null);
                Query q = session.getNamedQuery("findEnrollment");
                q.setParameter("userUid", userUid);
                q.setParameter("sectionUuid", sectionUuid);
                Object enrollment = q.uniqueResult();
                if(enrollment == null) {
                	CourseSection section = getSection(sectionUuid, session);
                	User user = getUserFromSiteParticipation(siteContext, userUid, session);
                	EnrollmentRecordImpl enr = new EnrollmentRecordImpl(section, null, user);
                	enr.setUuid(UUID.randomUUID().toString());
                	session.save(enr);
                	return enr;
                } else {
                	throw new MembershipException(userUid + " is already a student in this section");
                }
            }
        };
        return (EnrollmentRecord)getHibernateTemplate().execute(hc);
    }

	/**
	 * {@inheritDoc}
	 */
    public void switchSection(final String newSectionUuid, int maxSize) {
    	switchSection(newSectionUuid);
    }
    
	/**
	 * {@inheritDoc}
	 */
    public void switchSection(final String newSectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	try {
            		getSection(newSectionUuid, session);
            	} catch (MembershipException me) {
            		log.error(me);
            		return null;
            	}
            	String userUid = authn.getUserUid(null);
            	String siteContext = context.getContext(null);
            	CourseSection newSection = getSection(newSectionUuid, session);
            	Course course = newSection.getCourse();
            	String category = newSection.getCategory();
            	
            	// Find the existing section enrollment in this category, so we can drop it
                Query q = session.getNamedQuery("findEnrollmentInCategory");
                q.setParameter("userUid", userUid);
                q.setParameter("course", course);
                q.setParameter("category", category);
                Object result = q.uniqueResult();
                if(result == null) {
                	throw new MembershipException(userUid +
                			" is not enrolled in any " + category +
                			" section, so s/he can not switch sections");
                } else {
                	User user = getUserFromSiteParticipation(siteContext, userUid, session);
                	
                	// Add the new enrollment
                	EnrollmentRecordImpl enr = new EnrollmentRecordImpl(newSection, null, user);
                	enr.setUuid(UUID.randomUUID().toString());
                	session.save(enr);
                	
                	// Remove the old enrollment
                	session.delete(result);
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

	/**
	 * {@inheritDoc}
	 */
    public ParticipationRecord addSectionMembership(String userUid, Role role, String sectionUuid)
            throws MembershipException {
    	if(role.isInstructor()) {
    		throw new MembershipException("You can not add an instructor to a section... please add them to the course");
    	} else if(role.isStudent()) {
    		return addSectionEnrollment(userUid, sectionUuid);
    	} else if(role.isTeachingAssistant()) {
    		return addSectionTeachingAssistant(userUid, sectionUuid);
    	} else {
    		throw new MembershipException("You can not add a user to a section with a role of 'none'");
    	}
    }

	private EnrollmentRecordImpl addSectionEnrollment(final String userUid, final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSection section = getSection(sectionUuid, session);
            	String siteContext = ((CourseImpl)section.getCourse()).getSiteContext();
            	User user = getUserFromSiteParticipation(siteContext, userUid, session);

            	Set currentEnrollments = getSectionEnrollments(userUid, section.getCourse().getUuid());
            	for(Iterator iter = currentEnrollments.iterator(); iter.hasNext();) {
            		EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
                	
            		// Make sure they are not already enrolled in this learning context
            		if(enrollment.getLearningContext().equals(section)) {
            			if(log.isDebugEnabled()) log.debug("Not adding an enrollment for student "
            					+ userUid + " in section " + sectionUuid + "... already enrolled.");
            			return null;
            		}

            		// Make sure any enrollment in another section of the same category is removed
            		if(((CourseSection)enrollment.getLearningContext()).getCategory().equals(section.getCategory())) {
            			if(log.isDebugEnabled()) log.debug("Removing enrollment for student"
            					+ userUid + " in section " + enrollment.getLearningContext().getUuid()
            					+ "... enrolling in " + sectionUuid);
            			session.delete(enrollment);
            		}
            	}
            	
            	EnrollmentRecordImpl enrollment = new EnrollmentRecordImpl(section, null, user);
            	enrollment.setUuid(UUID.randomUUID().toString());
            	session.save(enrollment);
            	return enrollment;
            }
        };
        return (EnrollmentRecordImpl)getHibernateTemplate().execute(hc);
	}

	private TeachingAssistantRecordImpl addSectionTeachingAssistant(final String userUid, final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSection section = getSection(sectionUuid, session);
            	String siteContext = ((CourseImpl)section.getCourse()).getSiteContext();
            	User user = getUserFromSiteParticipation(siteContext, userUid, session);

            	// Make sure they are not already a TA in this section
            	List taRecords = getSectionTeachingAssistants(sectionUuid);
            	for(Iterator iter = taRecords.iterator(); iter.hasNext();) {
            		ParticipationRecord record = (ParticipationRecord)iter.next();
            		if(record.getUser().getUserUid().equals(userUid)) {
            			if(log.isDebugEnabled()) log.debug("Not adding a TA record for "
            					+ userUid + "... already a TA in section " + sectionUuid);
            		}
            	}
            	TeachingAssistantRecordImpl ta = new TeachingAssistantRecordImpl(section, user);
            	ta.setUuid(UUID.randomUUID().toString());
            	session.save(ta);
            	return ta;
            }
        };
        return (TeachingAssistantRecordImpl)getHibernateTemplate().execute(hc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSectionMemberships(final Set userUids, final Role role, final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	try {
            		getSection(sectionUuid, session);
            	} catch (MembershipException me) {
            		log.error(me);
            		return null;
            	}

            	String siteContext = context.getContext(null);
        		List currentMembers;
        		if(role.isTeachingAssistant()) {
        			currentMembers = getSectionTeachingAssistants(sectionUuid);
        		} else if(role.isStudent()) {
        			currentMembers = getSectionEnrollments(sectionUuid);
        		} else {
        			throw new RuntimeException("You can not setSectionMemberships with role " + role.getDescription());
        		}
        		Set currentUserUids = new HashSet();
        		for(Iterator iter = currentMembers.iterator(); iter.hasNext();) {
        			ParticipationRecord membership = (ParticipationRecord)iter.next();
        			// Keep a set of all current userUids
        			currentUserUids.add(membership.getUser().getUserUid());
        			// If this current member is not in the new set of users, drop them
        			if(! userUids.contains(membership.getUser().getUserUid())) {
        				session.delete(membership);
        			}
        		}

        		// Generate a set of new member userUuids
        		Set newMemberUserUuids = new HashSet(userUids);
        		newMemberUserUuids.removeAll(currentUserUids);
        		
        		// Get the section
        		CourseSection section = getSection(sectionUuid, session);
        		
        		// Add the new members to the section
        		for(Iterator iter = newMemberUserUuids.iterator(); iter.hasNext();) {
        			String newMemberUuid = (String)iter.next();
        			User user = getUserFromSiteParticipation(siteContext, newMemberUuid, session);
        			addMembership(user, section, role, session);
        		}

        		// Remove the new members from other sections in this category
        		if(!newMemberUserUuids.isEmpty()) {
            		removeSectionEnrollments(newMemberUserUuids, section, session);
        		}
        		
        		return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

    private void removeSectionEnrollments(Set newMemberUserUids, CourseSection section, Session session) throws HibernateException {
    	Query q = session.getNamedQuery("findOtherEnrollmentsInCategory");
    	q.setParameter("course", section.getCourse());
    	q.setParameter("category", section.getCategory());
    	q.setParameter("sectionUuid", section.getUuid());
    	q.setParameter("course", section.getCourse());
    	q.setParameterList("userUids", newMemberUserUids);
    	List enrollmentsToDelete = q.list();
    	for(Iterator iter = enrollmentsToDelete.iterator(); iter.hasNext();) {
    		EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
    		session.delete(enrollment);
    	}
    	
	}

	private void addMembership(User user, CourseSection section, Role role, Session session)
    	throws HibernateException {
    	if(role.isTeachingAssistant()) {
    		TeachingAssistantRecordImpl membership = new TeachingAssistantRecordImpl(section, user);
    		membership.setUuid(UUID.randomUUID().toString());
    		session.save(membership);
    	} else if(role.isStudent()) {
    		EnrollmentRecordImpl membership = new EnrollmentRecordImpl(section, null, user);
    		membership.setUuid(UUID.randomUUID().toString());
    		session.save(membership);
    	} else {
    		throw new MembershipException("You can not add an instructor as a section member");
    	}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dropSectionMembership(final String userUid, final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
                // Get the primary section
            	Query q = session.getNamedQuery("findParticipationRecord");
            	q.setParameter("sectionUuid", sectionUuid);
            	q.setParameter("userUid", userUid);
            	Object result = q.uniqueResult();
            	session.delete(result);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

	/**
	 * {@inheritDoc}
	 */
	public void dropEnrollmentFromCategory(final String studentUid,
			final String siteContext, final String category) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Query q = session.getNamedQuery("findCategoryEnrollment");
            	q.setParameter("studentUid", studentUid);
            	q.setParameter("category", category);
            	q.setParameter("siteContext", siteContext);
            	Object result = q.uniqueResult();
            	if(result != null) {
                	session.delete(result);
            	}
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTotalEnrollments(final String learningContextUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
                // Get the primary section
            	Query q = session.getNamedQuery("countEnrollments");
            	q.setParameter("learningContextUuid", learningContextUuid);
            	return q.iterate().next();
            }
        };
        return ((Integer)getHibernateTemplate().execute(hc)).intValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public Map getTotalEnrollmentsMap(String learningContextUuid) {

		// Not fully implemented, which is OK as this implementation does
		// not support transaction-backed enforcement of maximum section size anyway
		Map roleMap = new HashMap<Role, Integer>();
		roleMap.put(Role.STUDENT, getTotalEnrollments(learningContextUuid));
		roleMap.put(Role.TA, 0);
		roleMap.put(Role.INSTRUCTOR, 0);
		return roleMap;
	}
	
	/**
	 * {@inheritDoc}
	 */
    public CourseSection addSection(final String courseUuid, final String title,
    		final String category, final Integer maxEnrollments,
    		final String location, final Time startTime,
    		final Time endTime, final boolean monday,
    		final boolean tuesday, final boolean wednesday, final boolean thursday,
    		final boolean friday, final boolean saturday, final boolean sunday) {
    	final String uuid = UUID.randomUUID().toString();
        if(log.isDebugEnabled()) log.debug("Creating section with uuid = " + uuid);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourseFromUuid(courseUuid, session);
            	if(course == null) {
            		throw new MembershipException("Course uuid = " + courseUuid + "does not exist");
            	}
            	String uuid = UUID.randomUUID().toString();
            	CourseSectionImpl section = new CourseSectionImpl(course, title, uuid, category, maxEnrollments, location, startTime,
            			endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
            	session.save(section);
                return section;
            }
        };
            
        return (CourseSection)getHibernateTemplate().execute(hc);
    }

    private List getHibernateMeetings(List meetings) {
    	List meetingEntities = new ArrayList();
		if(meetings != null) {
			for(Iterator iter = meetings.iterator(); iter.hasNext();) {
				Meeting meeting = (Meeting)iter.next();
				meetingEntities.add(new MeetingImpl(meeting));
			}
		}
		return meetingEntities;
    }

    public CourseSection addSection(final String courseUuid, final String title, final String category, final Integer maxEnrollments, List meetings) {
		// We can't be sure that the meetings are in fact hibernate entities.  I guess this is true for other kinds of objects as well!
		final List meetingEntities = getHibernateMeetings(meetings);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourseFromUuid(courseUuid, session);
            	if(course == null) {
            		throw new MembershipException("Course uuid = " + courseUuid + "does not exist");
            	}
            	String uuid = UUID.randomUUID().toString();
            	CourseSectionImpl section = new CourseSectionImpl(course, title, uuid, category, maxEnrollments, meetingEntities);
            	session.save(section);
                return section;
            }
        };
            
        return (CourseSection)getHibernateTemplate().execute(hc);
	}

	/**
	 * {@inheritDoc}
	 */
    public void updateSection(final String sectionUuid, final String title,
    		final Integer maxEnrollments, final String location, final Time startTime,
    		final Time endTime, final boolean monday, final boolean tuesday,
    		final boolean wednesday, final boolean thursday, final boolean friday,
    		final boolean saturday, final boolean sunday) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSectionImpl section = getSection(sectionUuid, session);
            	section.setTitle(title);
            	section.setMaxEnrollments(maxEnrollments);
            	List meetingList = new ArrayList(1);
            	MeetingImpl meeting = new MeetingImpl(location, startTime, endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
            	meetingList.add(meeting);
            	section.setMeetings(meetingList);
            	
            	session.update(section);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

	public void updateSection(final String sectionUuid, final String title, final Integer maxEnrollments, final List meetings) {
		final List meetingEntities = getHibernateMeetings(meetings);
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSectionImpl section = getSection(sectionUuid, session);
            	section.setTitle(title);
            	section.setMaxEnrollments(maxEnrollments);
            	section.setMeetings(meetingEntities);

            	session.update(section);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

    /**
     * {@inheritDoc}
     */
    public void disbandSection(final String sectionUuid) {
        if(log.isDebugEnabled()) log.debug("Disbanding section " + sectionUuid);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSection section = getSection(sectionUuid, session);

            	// Delete all of the memberships in the section
            	Query q = session.getNamedQuery("findParticipantsBySectionUuid");
            	q.setParameter("sectionUuid", sectionUuid);
            	for(Iterator iter = q.iterate();iter.hasNext();) {
            		session.delete(iter.next());
            	}
            	
            	// Delete the section
            	session.delete(section);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    /**
     * {@inheritDoc}
     */
	public void disbandSections(Set<String> sectionUuids) {
		for(Iterator<String> iter = sectionUuids.iterator(); iter.hasNext();) {
			disbandSection(iter.next());
		}
	}

    /**
     * {@inheritDoc}
     */
	public boolean isExternallyManaged(final String courseUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseImpl course = getCourseFromUuid(courseUuid, session);
            	return Boolean.valueOf(course.isExternallyManaged());
            }
        };
        return ((Boolean)getHibernateTemplate().execute(hc)).booleanValue();
	}

	public void setExternallyManaged(final String courseUuid, final boolean externallyManaged) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseImpl course = (CourseImpl)getCourseFromUuid(courseUuid, session);
            	course.setExternallyManaged(externallyManaged);
            	if(externallyManaged) {
                	// Also set the self join/switch to false
                	course.setSelfRegistrationAllowed(false);
                	course.setSelfSwitchingAllowed(false);
            	}
            	session.update(course);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

    /**
     * {@inheritDoc}
     */
    public boolean isSelfRegistrationAllowed(final String courseUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseImpl course = getCourseFromUuid(courseUuid, session);
            	return Boolean.valueOf(course.isSelfRegistrationAllowed());
            }
        };
        return ((Boolean)getHibernateTemplate().execute(hc)).booleanValue();
    }

	/**
	 * {@inheritDoc}
	 */
    public boolean isSelfSwitchingAllowed(final String courseUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseImpl course = getCourseFromUuid(courseUuid, session);
            	return Boolean.valueOf(course.isSelfSwitchingAllowed());
            }
        };
        return ((Boolean)getHibernateTemplate().execute(hc)).booleanValue();
    }
    
	/**
	 * {@inheritDoc}
	 */
	public List<EnrollmentRecord> getUnsectionedEnrollments(final String courseUuid, final String category) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Query q = session.getNamedQuery("findUnsectionedEnrollmentsInCategory");
            	q.setParameter("courseUuid", courseUuid);
            	q.setParameter("category", category);
            	return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<EnrollmentRecord> getSectionEnrollments(final String userUid, final String courseUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Query q = session.getNamedQuery("findSingleStudentSectionEnrollmentsInCourse");
            	q.setParameter("userUid", userUid);
            	q.setParameter("courseUuid", courseUuid);
            	return q.list();
            }
        };
        return new HashSet(getHibernateTemplate().executeFind(hc));
	}

	/**
	 * {@inheritDoc}
	 */
	public User getSiteEnrollment(final String siteContext, final String studentUid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	return getUserFromSiteParticipation(siteContext, studentUid, session);
            }
        };
        return (User)getHibernateTemplate().execute(hc);
	}

	
	private User getUserFromSiteParticipation(String siteContext, String userUid, Session session) throws HibernateException {
		Query q = session.getNamedQuery("findUserFromSiteParticipation");
		q.setParameter("userUid", userUid);
		q.setParameter("siteContext", siteContext);
		User result = (User)q.uniqueResult();
		return result;
	}
	
	public ExternalIntegrationConfig getConfiguration(Object obj) {
		HttpServletRequest request = (HttpServletRequest)obj;
		ServletContext context = request.getSession(true).getServletContext();
		ExternalIntegrationConfig config = (ExternalIntegrationConfig)context.getAttribute(CONFIGURATION_KEY);
		
		// Set the default if the configuration string is missing
		if(config == null) {
			config = ExternalIntegrationConfig.MANUAL_DEFAULT;
			context.setAttribute(CONFIGURATION_KEY, config);
		}
		
		return config;
	}
	

    // Dependency injection

	public void setAuthn(Authn authn) {
        this.authn = authn;
    }

	public void setContext(Context context) {
		this.context = context;
	}

	public void setJoinOptions(final String courseUuid, final boolean joinAllowed, final boolean switchAllowed) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseImpl course = (CourseImpl)getCourseFromUuid(courseUuid, session);
            	course.setSelfRegistrationAllowed(joinAllowed);
            	course.setSelfSwitchingAllowed(switchAllowed);
            	session.update(course);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

	public Collection<CourseSection> addSections(String courseUuid, Collection<CourseSection> sections) {
		// We need to ensure that these sections are hibernate entities, not some other impl.
		sections = ensureHibernateSections(sections);
		
		Collection<CourseSection> addedSections = new ArrayList<CourseSection>();
		for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
			addedSections.add(
					addSection(courseUuid, section.getTitle(), section.getCategory(),
							section.getMaxEnrollments(), ensureHibernateMeetings(section.getMeetings())));
		}
		return addedSections;
	}
	
	private List<MeetingImpl> ensureHibernateMeetings(List<Meeting> meetings) {
		List<MeetingImpl> hibernateEntities = new ArrayList<MeetingImpl>();
		for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
			Meeting meeting = iter.next();
			hibernateEntities.add(new MeetingImpl(meeting));
		}
		return hibernateEntities;
	}
	
	private List<CourseSection> ensureHibernateSections(Collection<CourseSection> sections) {
		List<CourseSection> hibernateEntities = new ArrayList<CourseSection>();
		for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
			hibernateEntities.add(new CourseSectionImpl(section));
		}
		return hibernateEntities;
	}
}
