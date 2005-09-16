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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;

public class SakaiUtil {
	private static final Log log = LogFactory.getLog(SakaiUtil.class);

	/**
	 * Gets a User from Sakai's UserDirectory (legacy) service.
	 * 
	 * @param userUuid The user uuid
	 * @return
	 */
	public static final User getUserFromSakai(String userUuid) {
		final org.sakaiproject.service.legacy.user.User sakaiUser;
		try {
			sakaiUser = UserDirectoryService.getUser(userUuid);
		} catch (IdUnusedException e) {
			log.error("User not found: " + userUuid);
			e.printStackTrace();
			return null;
		}
		return convertUser(sakaiUser);
	}

	/**
	 * Converts a sakai user object into a user object suitable for use in the section
	 * manager tool and in section awareness.
	 * 
	 * @param sakaiUser The sakai user, as returned by Sakai's legacy SecurityService.
	 * 
	 * @return
	 */
	public static final User convertUser(final org.sakaiproject.service.legacy.user.User sakaiUser) {
		UserImpl user = new UserImpl(sakaiUser.getDisplayName(), sakaiUser.getId(),
				sakaiUser.getSortName(), sakaiUser.getId());
		return user;
	}
	
    /**
     * @return The current sakai authz reference
     */
    public static final String getSiteReference() {
        Placement placement = ToolManager.getCurrentPlacement();
        String context = placement.getContext();
        return SiteService.siteReference(context);
    }


}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
