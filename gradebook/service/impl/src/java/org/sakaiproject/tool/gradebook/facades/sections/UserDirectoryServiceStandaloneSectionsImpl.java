/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades.sections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.component.section.support.UserManager;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class UserDirectoryServiceStandaloneSectionsImpl implements UserDirectoryService {
    private static final Log log = LogFactory.getLog(UserDirectoryServiceStandaloneSectionsImpl.class);
	private UserManager userManager;

    public String getUserDisplayName(final String userUid) throws UnknownUserException {
    	User user = userManager.findUser(userUid);
    	if (user == null) {
    		throw new UnknownUserException("Unknown uid: " + userUid);
    	}
    	return user.getDisplayName();
    }
    
    public String getUserEmailAddress(final String userUid) throws UnknownUserException {
    	// the CourseManagement User does not include email address, so retrieve Sakai user
    	try {
            org.sakaiproject.user.api.User sakaiUser =
            	org.sakaiproject.user.cover.UserDirectoryService.getUser(userUid);
            return sakaiUser.getEmail();
        } catch (UserNotDefinedException e) {
            throw new UnknownUserException("Unknown uid: " + userUid);
        }
    }

	public UserManager getUserManager() {
		return userManager;
	}
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
}
