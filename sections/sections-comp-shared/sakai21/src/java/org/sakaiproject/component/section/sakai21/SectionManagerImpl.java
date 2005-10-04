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
package org.sakaiproject.component.section.sakai21;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionManager;
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
import org.sakaiproject.component.section.facade.impl.sakai21.AuthzSakaiImpl;
import org.sakaiproject.component.section.facade.impl.sakai21.SakaiUtil;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.realm.Realm;
import org.sakaiproject.service.legacy.realm.cover.RealmService;
import org.sakaiproject.service.legacy.resource.Reference;
import org.sakaiproject.service.legacy.resource.ResourceProperties;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.Section;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SiteService;

/**
 * A sakai 2.1 based implementation of the Section Management API, using the
 * new grouping capability of the framework.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionManagerImpl implements SectionManager {

	private static final Log log = LogFactory.getLog(SectionManagerImpl.class);
	
	private ResourceBundle sectionCategoryBundle = ResourceBundle.getBundle(
			"org.sakaiproject.api.section.bundle.CourseSectionCategories");

    protected Authn authn;
    protected Context context;
    protected SiteService siteService;
    
    /**
     * The role name that corresponds to a TA.  Configured via dependency injection.
     */
    protected String taRole;

    /**
     * The role name that corresponds to a Student.  Configured via dependency injection.
     */
    protected String studentRole;
    
	/**
	 * @inheritDoc
	 */
	public List getSections(String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting sections for context " + siteContext);
    	List sectionList = new ArrayList();
    	Collection sections;
    	try {
    		sections = siteService.getSite(siteContext).getSections();
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + siteContext);
    		return new ArrayList();
    	}
    	for(Iterator iter = sections.iterator(); iter.hasNext();) {
    		Section section = (Section)iter.next();
    		sectionList.add(new CourseSectionImpl(section));
    	}
    	return sectionList;
	}
	
	/**
	 * @inheritDoc
	 */
	public List getSectionsInCategory(String siteContext, String categoryId) {
    	if(log.isDebugEnabled()) log.debug("Getting " + categoryId + " sections for context " + siteContext);
    	List sectionList = new ArrayList();
    	Collection sections;
    	try {
    		sections = siteService.getSite(siteContext).getSections();
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + siteContext);
    		return new ArrayList();
    	}
    	for(Iterator iter = sections.iterator(); iter.hasNext();) {
    		Section section = (Section)iter.next();
    		if(categoryId.equals(section.getProperties().getProperty(CourseSectionImpl.CATEGORY))) {
        		sectionList.add(new CourseSectionImpl(section));
    		}
    	}
    	return sectionList;
	}

	/**
	 * @inheritDoc
	 */
	public CourseSection getSection(String sectionUuid) {
		Section section;
		section = siteService.findSection(sectionUuid);
		if(section == null) {
			log.error("Unable to find section " + sectionUuid);
			return null;
		}
		return new CourseSectionImpl(section);
	}

	/**
	 * @inheritDoc
	 */
	public List getSiteInstructors(final String siteContext) {
        List sakaiMembers = SecurityService.unlockUsers(AuthzSakaiImpl.INSTRUCTOR_PERMISSION, SakaiUtil.getSiteReference());
        List membersList = new ArrayList();

        Course course = getCourse(siteContext);
        
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.service.legacy.user.User sakaiUser = (org.sakaiproject.service.legacy.user.User)iter.next();
        	User user = SakaiUtil.convertUser(sakaiUser);
    		InstructorRecordImpl record = new InstructorRecordImpl(course, user);
    		membersList.add(record);
        }
        return membersList;
	}

	/**
	 * @inheritDoc
	 */
	public List getSiteTeachingAssistants(final String siteContext) {
		String siteRef = SakaiUtil.getSiteReference();
        List sakaiMembers = SecurityService.unlockUsers(AuthzSakaiImpl.TA_PERMISSION, siteRef);
        sakaiMembers.removeAll(SecurityService.unlockUsers(AuthzSakaiImpl.INSTRUCTOR_PERMISSION, siteRef));

        List membersList = new ArrayList();
        Course course = getCourse(siteContext);
        
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.service.legacy.user.User sakaiUser = (org.sakaiproject.service.legacy.user.User)iter.next();
        	User user = SakaiUtil.convertUser(sakaiUser);
    		TeachingAssistantRecordImpl record = new TeachingAssistantRecordImpl(course, user);
    		membersList.add(record);
        }
        return membersList;
	}
	
	/**
	 * @inheritDoc
	 */
	public List getSiteEnrollments(final String siteContext) {
		// TODO Replace with list of site members in student role
        List sakaiMembers = new ArrayList();
        List membersList = new ArrayList();

        Course course = getCourse(siteContext);
        
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.service.legacy.user.User sakaiUser = (org.sakaiproject.service.legacy.user.User)iter.next();
        	User user = SakaiUtil.convertUser(sakaiUser);
    		// TODO Where do we get the enrollment status?
    		EnrollmentRecordImpl record = new EnrollmentRecordImpl(course, null, user);
    		membersList.add(record);
        }
        return membersList;
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionTeachingAssistants(final String sectionUuid) {
		log.error("FIXME!");
		return new ArrayList();
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionEnrollments(final String sectionUuid) {
		log.error("FIXME!");
		return new ArrayList();
	}

	/**
	 * @inheritDoc
	 */
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

	/**
	 * @inheritDoc
	 */
	public String getCategoryName(String categoryId, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle("org.sakaiproject.api.section.bundle.CourseSectionCategories", locale);
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
	 * @inheritDoc
	 */
	public List getSectionCategories(String siteContext) {
		Enumeration keys = sectionCategoryBundle.getKeys();
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
	public Course getCourse(final String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting course for context " + siteContext);
    	Site site;
    	try {
    		site = siteService.getSite(siteContext);
    	} catch (IdUnusedException e) {
    		log.error("Could not find site with id = " + siteContext);
    		return null;
    	}
    	return new CourseImpl(site);
	}

	/**
	 * @inheritDoc
	 */
	public SectionEnrollments getSectionEnrollmentsForStudents(final String siteContext, final Set studentUids) {
		// TODO Get the list of all section enrollment records
		log.error("FIXME!");
		return new SectionEnrollmentsImpl(new ArrayList());
	}

	/**
	 * @inheritDoc
	 */
    public EnrollmentRecord joinSection(final String sectionUuid) {
		log.error("FIXME!");
		return null;
    }

	/**
	 * @inheritDoc
	 */
    public void switchSection(final String newSectionUuid) {
		log.error("FIXME!");
    }

	/**
	 * @inheritDoc
	 */
    public ParticipationRecord addSectionMembership(String userUid, Role role, String sectionUuid)
            throws MembershipException {
		log.error("FIXME!");
    	return null;
    }
	
	/**
	 * @inheritDoc
	 */
	public void setSectionMemberships(final Set userUids, final Role role, final String sectionUuid) {
		log.error("FIXME!");
	}


	/**
	 * @inheritDoc
	 */
	public void dropSectionMembership(final String userUid, final String sectionUuid) {
		log.error("FIXME!");
    }

	/**
	 * @inheritDoc
	 */
	public void dropEnrollmentFromCategory(final String studentUid,
			final String siteContext, final String category) {
		log.error("FIXME!");
	}

	/**
	 * @inheritDoc
	 */
	public int getTotalEnrollments(final String learningContextUuid) {
		Reference ref = EntityManager.newReference(learningContextUuid);
		Realm realm;
		try {
			realm = RealmService.getRealm(ref.getId());
		} catch (IdUnusedException e) {
			log.error("learning context " + learningContextUuid + " is neither a site nor a section");
			return 0;
		}
		Set users = realm.getUsersWithRole(studentRole);
		return users.size();
	}

	/**
	 * @inheritDoc
	 */
    public CourseSection addSection(final String courseUuid, final String title,
    		final String category, final Integer maxEnrollments,
    		final String location, final Time startTime,
    		final Time endTime, final boolean monday,
    		final boolean tuesday, final boolean wednesday, final boolean thursday,
    		final boolean friday, final boolean saturday, final boolean sunday) {
    	Reference ref = EntityManager.newReference(courseUuid);
    	Site site;
    	try {
    		site = siteService.getSite(ref.getId());
    	} catch (IdUnusedException e) {
    		log.error("Unable to find site " + courseUuid);
    		return null;
    	}
    	Section section = site.addSection();
    	setSectionProperties(title, category, maxEnrollments, location, startTime, endTime, section);
    	    	
    	return new CourseSectionImpl(section);
    }

	/**
	 * Sets the properties of a section.  Since updates do not change category,
	 * you can call this with a null category and it will not change the section's
	 * current category.
	 * 
	 * @param title
	 * @param category
	 * @param maxEnrollments
	 * @param location
	 * @param startTime
	 * @param endTime
	 * @param section
	 */
    private void setSectionProperties(String title, String category, Integer maxEnrollments,
			String location, Time startTime, Time endTime, Section section) {
    	section.setTitle(title);
		ResourceProperties props = section.getProperties();
		if(category != null) {
	    	props.addProperty(CourseSectionImpl.CATEGORY, category);
		}
    	props.addProperty(CourseSectionImpl.LOCATION, location);
    	props.addProperty(CourseSectionImpl.START_TIME, CourseSectionImpl.convertTimeToString(startTime));
    	props.addProperty(CourseSectionImpl.END_TIME, CourseSectionImpl.convertTimeToString(endTime));
    	props.addProperty(CourseSectionImpl.LOCATION, location);

    	if(maxEnrollments != null) {
    		props.addProperty(CourseSectionImpl.MAX_ENROLLMENTS, maxEnrollments.toString());
    	}
    	
    	section.setDescription(null);

    	// TODO Update the properties in persistence
	}
    
	/**
	 * @inheritDoc
	 */
    public void updateSection(final String sectionUuid, final String title,
    		final Integer maxEnrollments, final String location, final Time startTime,
    		final Time endTime, final boolean monday, final boolean tuesday,
    		final boolean wednesday, final boolean thursday, final boolean friday,
    		final boolean saturday, final boolean sunday) {
    	Section section = siteService.findSection(sectionUuid);
    	if(section == null) {
    		throw new RuntimeException("Unable to find section " + sectionUuid);
    	}
    	setSectionProperties(title, null, maxEnrollments, location, startTime, endTime, section);
	}

	/**
	 * @inheritDoc
	 */
    public void disbandSection(final String sectionUuid) {
        if(log.isDebugEnabled()) log.debug("Disbanding section " + sectionUuid);
        Section section = siteService.findSection(sectionUuid);
        Site site = section.getContainingSite();
        site.removeSection(section);
    }

	/**
	 * @inheritDoc
	 */
    public boolean isSelfRegistrationAllowed(final String courseUuid) {
    	Reference ref = EntityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
    	return Boolean.toString(true).equals(props.getProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED));
    }

	/**
	 * @inheritDoc
	 */
    public void setSelfRegistrationAllowed(final String courseUuid, final boolean allowed) {
    	Reference ref = EntityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
		props.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, new Boolean(allowed).toString());
		
		// TODO Save the properties
		log.error("FIXME!");
    }

	/**
	 * @inheritDoc
	 */
    public boolean isSelfSwitchingAllowed(final String courseUuid) {
    	Reference ref = EntityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
    	return Boolean.toString(true).equals(props.getProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED));
    }

	/**
	 * @inheritDoc
	 */
    public void setSelfSwitchingAllowed(final String courseUuid, final boolean allowed) {
    	Reference ref = EntityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
		props.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, new Boolean(allowed).toString());
		
		// TODO Save the properties
		log.error("FIXME!");
    }
    
	/**
	 * @inheritDoc
	 */
	public List getUnsectionedEnrollments(final String courseUuid, final String category) {
		Reference siteRef = EntityManager.newReference(courseUuid);
		String siteId = siteRef.getId();
		List allEnrollments = getSiteEnrollments(siteId);
		List unsectionedEnrollments = new ArrayList();

		// TODO Filter the enrollments
		log.error("FIXME!");
		
		return unsectionedEnrollments;
	}

	/**
	 * @inheritDoc
	 */
	public Set getSectionEnrollments(final String userUid, final String courseUuid) {
		log.error("FIXME!");
    	return new HashSet();
	}

	/**
	 * @inheritDoc
	 */
	public User getSiteEnrollment(final String siteContext, final String studentUid) {
		return SakaiUtil.getUserFromSakai(studentUid);
	}

    // Dependency injection

	public void setAuthn(Authn authn) {
        this.authn = authn;
    }
	
	public void setContext(Context context) {
		this.context = context;
	}

	public void setStudentRole(String studentRole) {
		this.studentRole = studentRole;
	}

	public void setTaRole(String taRole) {
		this.taRole = taRole;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

}


/**********************************************************************************
 * $Id$
 *********************************************************************************/
