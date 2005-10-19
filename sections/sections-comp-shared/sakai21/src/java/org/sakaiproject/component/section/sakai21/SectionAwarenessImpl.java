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
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.component.section.facade.impl.sakai21.SakaiUtil;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.site.Group;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SiteService;

/**
 * A sakai 2.1 based implementation of the Section Awareness API, using the
 * new grouping capability of the framework.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionAwarenessImpl implements SectionAwareness {

	private ResourceBundle sectionCategoryBundle = ResourceBundle.getBundle(
			"org.sakaiproject.api.section.bundle.CourseSectionCategories");

	private static final Log log = LogFactory.getLog(SectionAwarenessImpl.class);

	protected SiteService siteService;
	
	/**
	 * @inheritDoc
	 */
	public Set getSections(final String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting sections for context " + siteContext);
    	Set sectionSet = new HashSet();
    	Collection sections;
    	try {
    		sections = siteService.getSite(siteContext).getGroups();
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + siteContext);
    		return new HashSet();
    	}
    	for(Iterator iter = sections.iterator(); iter.hasNext();) {
    		Group section = (Group)iter.next();
    		sectionSet.add(new CourseSectionImpl(section));
    	}
    	return sectionSet;
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
	public CourseSection getSection(final String sectionUuid) {
		Group section;
		section = siteService.findGroup(sectionUuid);
		if(section == null) {
			log.error("Unable to find section " + sectionUuid);
			return null;
		}
		return new CourseSectionImpl(section);
	}

	/**
	 * @inheritDoc
	 */
	public List getSiteMembersInRole(final String siteContext, final Role role) {
		// TODO Replace with list of site members in the specified role
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

	private Course getCourse(final String siteContext) {
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
	public boolean isSiteMemberInRole(String siteContext, String userUid, Role role) {
		List members = getSiteMembersInRole(siteContext, role);
		for(Iterator iter = members.iterator(); iter.hasNext();) {
			ParticipationRecord record = (ParticipationRecord)iter.next();
			if(record.getUser().getUserUid().equals(userUid)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionMembers(final String sectionUuid) {
		log.error("FIXME!");
		return new ArrayList();
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionMembersInRole(final String sectionUuid, final Role role) {
		log.error("FIXME!");
		return new ArrayList();
	}


	/**
	 * @inheritDoc
	 */
	public boolean isSectionMemberInRole(final String sectionUuid, final String userUid, final Role role) {
		log.error("FIXME!");
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public String getSectionName(final String sectionUuid) {
		CourseSection section = getSection(sectionUuid);
		return section.getTitle();
	}

	/**
	 * @inheritDoc
	 */
	public String getSectionCategory(final String sectionUuid) {
		CourseSection section = getSection(sectionUuid);
		return section.getCategory();
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionsInCategory(final String siteContext, final String categoryId) {
    	if(log.isDebugEnabled()) log.debug("Getting " + categoryId + " sections for context " + siteContext);
    	List sectionList = new ArrayList();
    	Collection sections;
    	try {
    		sections = siteService.getSite(siteContext).getGroups();
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + siteContext);
    		return new ArrayList();
    	}
    	for(Iterator iter = sections.iterator(); iter.hasNext();) {
    		Group section = (Group)iter.next();
    		if(categoryId.equals(section.getProperties().getProperty(CourseSectionImpl.CATEGORY))) {
        		sectionList.add(new CourseSectionImpl(section));
    		}
    	}
    	return sectionList;
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
			if(log.isInfoEnabled()) log.info("Could not find the name for category id = " + categoryId + " in locale " + locale.getDisplayName());
			name = null;
		}
		return name;
	}

	/**
	 * @inheritDoc
	 */
	public List getUnassignedMembersInRole(final String siteContext, final Role role) {
		log.error("FIXME!");
		return new ArrayList();
	}
	
	// Dependency injection

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

}


/**********************************************************************************
 * $Id$
 *********************************************************************************/
