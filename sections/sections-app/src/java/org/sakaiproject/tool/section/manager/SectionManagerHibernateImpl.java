/**********************************************************************************
*
* $Id: $
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
package org.sakaiproject.tool.section.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.uuid.UuidManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.SectionEnrollments;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.exception.MembershipException;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.api.section.facade.manager.UserDirectory;
import org.sakaiproject.tool.section.CourseImpl;
import org.sakaiproject.tool.section.CourseSectionImpl;
import org.sakaiproject.tool.section.EnrollmentRecordImpl;
import org.sakaiproject.tool.section.SectionEnrollmentsImpl;
import org.sakaiproject.tool.section.TeachingAssistantRecordImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * A standalone implementation of the Section Management API.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionManagerHibernateImpl extends HibernateDaoSupport implements
        SectionManager {

	private static final Log log = LogFactory.getLog(SectionManagerHibernateImpl.class);
	
	// Fields configured via dep. injection
	protected UuidManager uuidManager;
    protected Authn authn;
    protected Context context;
    protected UserDirectory userDirectory;

	private List sectionCategoryList;
    
	public List getSections(final String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting sections for context " + siteContext);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.getNamedQuery("findSectionsBySiteContext");
                q.setParameter("context", siteContext);
                return q.list();
            }
        };
        return getHibernateTemplate().executeFind(hc);
	}

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

	public CourseSection getSection(final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
	        public Object doInHibernate(Session session) throws HibernateException {
	        	return getSection(sectionUuid, session);
	        }
        };
        return (CourseSection)getHibernateTemplate().execute(hc);
	}

	public List getSiteInstructors(final String siteContext) {
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

	public List getSiteTeachingAssistants(final String siteContext) {
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
	
	public List getSiteEnrollments(final String siteContext) {
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

	public List getSectionTeachingAssistants(final String sectionUuid) {
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

	public List getSectionEnrollments(final String sectionUuid) {
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

	public List findSiteEnrollments(String siteContext, String pattern) {
		List fullList = getSiteEnrollments(siteContext);
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

	public String getCategoryName(String categoryId, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle("org.sakaiproject.api.section.bundle.Messages", locale);
		String name;
		try {
			name = bundle.getString(categoryId);
		} catch(MissingResourceException mre) {
			if(log.isInfoEnabled()) log.info("Could not find the name for category id = " + categoryId + " in locale " + locale.getDisplayName());
			name = null;
		}
		return name;
	}

	public List getSectionCategories() {
		return sectionCategoryList;
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

	private Course getCourseFromUuid(String courseUuid, Session session) throws HibernateException {
        Query q = session.getNamedQuery("loadCourseByUuid");
        q.setParameter("uuid", courseUuid);
        Course course = (Course)q.uniqueResult();
        if(course == null) {
        	throw new MembershipException("No course exists with uuid = " + courseUuid);
        } else {
        	return course;
        }
	}

	public Course getCourse(final String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting course for context " + siteContext);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	return getCourseFromSiteContext(siteContext, session);
            }
        };
        return (Course)getHibernateTemplate().execute(hc);
	}

	public SectionEnrollments getSectionEnrollmentsForStudents(final String siteContext, final Set studentUuids) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourse(siteContext);
            	Query q = session.getNamedQuery("findSectionEnrollments");
            	q.setParameter("course", course);
            	q.setParameterList("studentUuids", studentUuids);
            	return q.list();
            }
        };
		if(studentUuids == null || studentUuids.isEmpty()) {
			if(log.isDebugEnabled()) log.debug("No student uuids were passed to getSectionEnrollments.");
			return new SectionEnrollmentsImpl(new ArrayList());
		}
    	return new SectionEnrollmentsImpl(getHibernateTemplate().executeFind(hc));
	}

    public EnrollmentRecord joinSection(final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	String userUuid = authn.getUserUuid();
                Query q = session.getNamedQuery("findEnrollment");
                q.setParameter("userUuid", userUuid);
                q.setParameter("sectionUuid", sectionUuid);
                Object enrollment = q.uniqueResult();
                if(enrollment == null) {
                	CourseSection section = getSection(sectionUuid, session);
                	User user = userDirectory.getUser(userUuid);
                	EnrollmentRecordImpl enr = new EnrollmentRecordImpl(section, "enrolled", user);
                	enr.setUuid(uuidManager.createUuid());
                	session.save(enr);
                	return enr;
                } else {
                	throw new MembershipException(userUuid + " is already a student in this section");
                }
            }
        };
        return (EnrollmentRecord)getHibernateTemplate().execute(hc);
    }

    public void switchSection(final String newSectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	String userUuid = authn.getUserUuid();
            	CourseSection newSection = getSection(newSectionUuid, session);
            	Course course = newSection.getCourse();
            	String category = newSection.getCategory();
            	
            	// Find the existing section enrollment in this category, so we can drop it
                Query q = session.getNamedQuery("findEnrollmentInCategory");
                q.setParameter("userUuid", userUuid);
                q.setParameter("course", course);
                q.setParameter("category", category);
                Object result = q.uniqueResult();
                if(result == null) {
                	throw new MembershipException(userUuid +
                			" is not enrolled in any " + category +
                			" section, so s/he can not switch sections");
                } else {
                	User user = userDirectory.getUser(userUuid);
                	
                	// Add the new enrollment
                	EnrollmentRecordImpl enr = new EnrollmentRecordImpl(newSection, "enrolled", user);
                	enr.setUuid(uuidManager.createUuid());
                	session.save(enr);
                	
                	// Remove the old enrollment
                	session.delete(result);
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    public ParticipationRecord addSectionMembership(String userId, Role role, String sectionUuid)
            throws MembershipException {
    	if(role.isInstructor()) {
    		throw new MembershipException("You can not add an instructor to a section... please add them to the course");
    	} else if(role.isStudent()) {
    		return addSectionEnrollment(userId, sectionUuid);
    	} else if(role.isTeachingAssistant()) {
    		return addSectionTeachingAssistant(userId, sectionUuid);
    	} else {
    		throw new MembershipException("You can not add a user to a section with a role of 'none'");
    	}
    }

	private EnrollmentRecordImpl addSectionEnrollment(final String userUuid, final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSection section = getSection(sectionUuid, session);
            	User user = userDirectory.getUser(userUuid);

            	// TODO Make sure they are not already enrolled in this learning context

            	// TODO Make sure they are not enrolled in another section of the same category
            	
            	EnrollmentRecordImpl enrollment = new EnrollmentRecordImpl(section, "enrolled", user);
            	enrollment.setUuid(uuidManager.createUuid());
            	session.save(enrollment);
            	return enrollment;
            }
        };
        return (EnrollmentRecordImpl)getHibernateTemplate().execute(hc);
	}

	private TeachingAssistantRecordImpl addSectionTeachingAssistant(final String userId, final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSection section = getSection(sectionUuid, session);
            	User user = userDirectory.getUser(userId);

            	// TODO Make sure they are not already a TA in this section
            	
            	TeachingAssistantRecordImpl ta = new TeachingAssistantRecordImpl(section, user);
            	ta.setUuid(uuidManager.createUuid());
            	session.save(ta);
            	return ta;
            }
        };
        return (TeachingAssistantRecordImpl)getHibernateTemplate().execute(hc);
	}
	
	public void setSectionMemberships(final Set userUuids, final Role role, final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
        		List currentMembers;
        		if(role.isTeachingAssistant()) {
        			currentMembers = getSectionTeachingAssistants(sectionUuid);
        		} else if(role.isStudent()) {
        			currentMembers = getSectionEnrollments(sectionUuid);
        		} else {
        			throw new RuntimeException("You can not setSectionMemberships with role " + role.getDescription());
        		}
        		Set currentUserUuids = new HashSet();
        		for(Iterator iter = currentMembers.iterator(); iter.hasNext();) {
        			ParticipationRecord membership = (ParticipationRecord)iter.next();
        			// Keep a set of all current userUuids
        			currentUserUuids.add(membership.getUser().getUserUuid());
        			// If this current member is not in the new set of users, drop them
        			if(! userUuids.contains(membership.getUser().getUserUuid())) {
        				session.delete(membership);
        			}
        		}

        		// Generate a set of new member userUuids
        		Set newMemberUserUuids = new HashSet(userUuids);
        		newMemberUserUuids.removeAll(currentUserUuids);
        		
        		// Get the section
        		CourseSection section = getSection(sectionUuid, session);
        		
        		// Add the new members to the section
        		for(Iterator iter = newMemberUserUuids.iterator(); iter.hasNext();) {
        			String newMemberUuid = (String)iter.next();
        			User user = userDirectory.getUser(newMemberUuid);
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

    private void removeSectionEnrollments(Set newMemberUserUuids, CourseSection section, Session session) throws HibernateException {
    	Query q = session.getNamedQuery("findOtherEnrollmentsInCategory");
    	q.setParameter("course", section.getCourse());
    	q.setParameter("category", section.getCategory());
    	q.setParameter("sectionUuid", section.getUuid());
    	q.setParameter("course", section.getCourse());
    	q.setParameterList("userUuids", newMemberUserUuids);
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
    		membership.setUuid(uuidManager.createUuid());
    		session.save(membership);
    	} else if(role.isStudent()) {
    		EnrollmentRecordImpl membership = new EnrollmentRecordImpl(section, "enrolled", user);
    		membership.setUuid(uuidManager.createUuid());
    		session.save(membership);
    	} else {
    		throw new MembershipException("You can not add an instructor as a section member");
    	}
	}

	public void dropSectionMembership(final String userUuid, final String sectionUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
                // Get the primary section
            	Query q = session.getNamedQuery("findParticipationRecord");
            	q.setParameter("sectionUuid", sectionUuid);
            	q.setParameter("userUuid", userUuid);
            	Object result = q.uniqueResult();
            	session.delete(result);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

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

    public CourseSection addSection(final String courseUuid, final String title,
    		final String category, final int maxEnrollments,
    		final String location, final String startTime, final boolean startTimeAm,
    		final String endTime, final boolean endTimeAm, final boolean monday,
    		final boolean tuesday, final boolean wednesday, final boolean thursday,
    		final boolean friday, final boolean saturday, final boolean sunday) {
    	final String uuid = uuidManager.createUuid();
        if(log.isDebugEnabled()) log.debug("Creating section with uuid = " + uuid);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourseFromUuid(courseUuid, session);
            	if(course == null) {
            		throw new MembershipException("Course uuid = " + courseUuid + "does not exist");
            	}
            	String uuid = uuidManager.createUuid();
            	CourseSectionImpl section = new CourseSectionImpl(course, title, uuid, category, maxEnrollments, location, startTime, startTimeAm,
            			endTime, endTimeAm, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
            	session.save(section);
                return section;
            }
        };
            
        return (CourseSection)getHibernateTemplate().execute(hc);
    }

    public void updateSection(final String sectionUuid, final String title,
    		final String category, final int maxEnrollments,
    		final String location, final String startTime, final boolean startTimeAm,
    		final String endTime, final boolean endTimeAm, final boolean monday,
    		final boolean tuesday, final boolean wednesday, final boolean thursday,
    		final boolean friday, final boolean saturday, final boolean sunday) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseSectionImpl section = getSection(sectionUuid, session);
            	section.setTitle(title);
            	section.setCategory(category);
            	section.setMaxEnrollments(maxEnrollments);
            	section.setLocation(location);
            	section.setStartTime(startTime);
            	section.setStartTimeAm(startTimeAm);
            	section.setEndTime(endTime);
            	section.setEndTimeAm(endTimeAm);
            	section.setMonday(monday);
            	section.setTuesday(tuesday);
            	section.setWednesday(wednesday);
            	section.setThursday(thursday);
            	section.setFriday(friday);
            	section.setSaturday(saturday);
            	section.setSunday(sunday);
            	
            	session.update(section);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

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

    public boolean isSelfRegistrationAllowed(final String courseUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourseFromUuid(courseUuid, session);
            	return new Boolean(course.isSelfRegistrationAllowed());
            }
        };
        return ((Boolean)getHibernateTemplate().execute(hc)).booleanValue();
    }

    public void setSelfRegistrationAllowed(final String courseUuid, final boolean allowed) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseImpl course = (CourseImpl)getCourseFromUuid(courseUuid, session);
            	course.setSelfRegistrationAllowed(allowed);
            	session.update(course);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    public boolean isSelfSwitchingAllowed(final String courseUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Course course = getCourseFromUuid(courseUuid, session);
            	return new Boolean(course.isSelfSwitchingAllowed());
            }
        };
        return ((Boolean)getHibernateTemplate().execute(hc)).booleanValue();
    }

    public void setSelfSwitchingAllowed(final String courseUuid, final boolean allowed) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	CourseImpl course = (CourseImpl)getCourseFromUuid(courseUuid, session);
            	course.setSelfSwitchingAllowed(allowed);
            	session.update(course);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }
    
	public List getUnsectionedEnrollments(final String courseUuid, final String category) {
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

	public Set getSectionEnrollments(final String userUuid, final String courseUuid) {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	Query q = session.getNamedQuery("findSingleStudentSectionEnrollmentsInCourse");
            	q.setParameter("userUuid", userUuid);
            	q.setParameter("courseUuid", courseUuid);
            	return q.list();
            }
        };
        return new HashSet(getHibernateTemplate().executeFind(hc));
	}

    // Dependency injection

	public void setAuthn(Authn authn) {
        this.authn = authn;
    }

	public void setUuidManager(UuidManager uuidManager) {
		this.uuidManager = uuidManager;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}

	public void setUserDirectory(UserDirectory userDirectory) {
		this.userDirectory = userDirectory;
	}

	public void setSectionCategoryList(List sectionCategoryList) {
		this.sectionCategoryList = sectionCategoryList;
	}

}


/**********************************************************************************
 * $Id: $
 *********************************************************************************/
