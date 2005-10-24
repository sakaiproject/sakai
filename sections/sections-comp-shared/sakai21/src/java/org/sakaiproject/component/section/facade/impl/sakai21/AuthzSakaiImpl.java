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

package org.sakaiproject.component.section.facade.impl.sakai21;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.facade.Role;
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
	private static final Log log = LogFactory.getLog(AuthzSakaiImpl.class);
    
	/**
	 * Ignores the userUid parameter, since sakai can get the current user itself.
	 * 
	 * @inheritDoc
	 */
	public Role getSiteRole(String userUid, String siteContext) {
		User sakaiUser = UserDirectoryService.getCurrentUser();
		String siteAuthzRef = SiteService.siteReference(siteContext);
		return getRole(sakaiUser, siteAuthzRef);
	}

	/**
	 * Ignores the userUid parameter, since sakai can get the current user itself.
	 * 
	 * @inheritDoc
	 */
	public Role getSectionRole(final String userUid, final String sectionUuid) {
		User sakaiUser = UserDirectoryService.getCurrentUser();
		return getRole(sakaiUser, sectionUuid);
	}

	/**
	 * Determines the local role based on the sakai role markers in the permission matrix.
	 * 
	 * @param sakaiUser The user
	 * @param authzRef The security reference string
	 * 
	 * @return The internal role enumeration (not the sakai role)
	 */
	private Role getRole(User sakaiUser, String authzRef) {
		if(SecurityService.unlock(sakaiUser, SectionAwareness.INSTRUCTOR_MARKER, authzRef)) {
			return Role.INSTRUCTOR;
		}
		if(SecurityService.unlock(sakaiUser, SectionAwareness.TA_MARKER, authzRef)) {
			return Role.TA;
		}
		if(SecurityService.unlock(sakaiUser, SectionAwareness.STUDENT_MARKER, authzRef)) {
			return Role.STUDENT;
		}
		return Role.NONE;
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
