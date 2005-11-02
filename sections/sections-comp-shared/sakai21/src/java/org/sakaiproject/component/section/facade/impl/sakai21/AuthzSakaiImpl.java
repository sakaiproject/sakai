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

package org.sakaiproject.component.section.facade.impl.sakai21;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.facade.manager.Authz;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;

/**
 * Uses Sakai's SecurityService to determine the current user's site role, or
 * consults the CourseSection membership to determine section role.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class AuthzSakaiImpl implements Authz {
	private static final String SITE_UPDATE = "site.upd";
	private static final String SITE_UPDATE_GROUP_MEMBERSHIP = "site.upd.grp.mbrshp";

	private static final Log log = LogFactory.getLog(AuthzSakaiImpl.class);

	/**
	 * The user must have site.upd to update sections in the Section Info tool.
	 */
	public boolean isSectionManagementAllowed(String userUid, String siteContext) {
		User sakaiUser = UserDirectoryService.getCurrentUser();
		String siteRef = SiteService.siteReference(siteContext);
		boolean canUpdateSite = SecurityService.unlock(sakaiUser, AuthzSakaiImpl.SITE_UPDATE, siteRef);
		
		return canUpdateSite;
	}

	/**
	 * The user must have site.upd to update section options in the Section Info tool.
	 */
	public boolean isSectionOptionsManagementAllowed(String userUid, String siteContext) {
		return isSectionManagementAllowed(userUid, siteContext);
	}

	/**
	 * The user must have site.upd to update TA assignments in the Section Info
	 * tool, even though the framework doesn't require this (it would accept site.upd.grp.mbrshp).
	 */
	public boolean isSectionTaManagementAllowed(String userUid, String siteContext) {
		return isSectionManagementAllowed(userUid, siteContext);
	}

	/**
	 * The user must have either site.upd or site.upd.grp.mbrshp to update
	 * section enrollments in the Section Info tool.
	 */
	public boolean isSectionEnrollmentMangementAllowed(String userUid, String siteContext) {
		User sakaiUser = UserDirectoryService.getCurrentUser();
		String siteRef = SiteService.siteReference(siteContext);
		boolean canUpdateSite = SecurityService.unlock(sakaiUser, AuthzSakaiImpl.SITE_UPDATE, siteRef);
		boolean canUpdateGroups = SecurityService.unlock(sakaiUser, AuthzSakaiImpl.SITE_UPDATE_GROUP_MEMBERSHIP, siteRef);
		
		return canUpdateSite || canUpdateGroups;
	}

	/**
	 * The user must have access to the student marker function (section.role.student)
	 * to view their own section enrollments.
	 */
	public boolean isViewOwnSectionsAllowed(String userUid, String siteContext) {
		User sakaiUser = UserDirectoryService.getCurrentUser();
		String siteRef = SiteService.siteReference(siteContext);
		boolean isStudent = SecurityService.unlock(sakaiUser, SectionAwareness.STUDENT_MARKER, siteRef);
		
		return isStudent;
	}

	/**
	 * Even if a TA can't make changes to the sections or their enrollments,
	 * they can always view the sections and their enrollments.
	 */
	public boolean isViewAllSectionsAllowed(String userUid, String siteContext) {
		User sakaiUser = UserDirectoryService.getCurrentUser();
		String siteRef = SiteService.siteReference(siteContext);
		 return SecurityService.unlock(sakaiUser, AuthzSakaiImpl.SITE_UPDATE, siteRef) ||
		 		SecurityService.unlock(sakaiUser, AuthzSakaiImpl.SITE_UPDATE_GROUP_MEMBERSHIP, siteRef) ||
		 		SecurityService.unlock(sakaiUser, SectionAwareness.TA_MARKER, siteRef);
	}

}
