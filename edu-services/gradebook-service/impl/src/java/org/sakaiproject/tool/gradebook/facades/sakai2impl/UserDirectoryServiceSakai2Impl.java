/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sakaiproject.user.api.UserNotDefinedException;

import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;

/**
 * Sakai2 implementation of the gradebook UserDirectoryService API.
 */
public class UserDirectoryServiceSakai2Impl implements UserDirectoryService {
    private static final Logger log = LoggerFactory.getLogger(UserDirectoryServiceSakai2Impl.class);

    public String getUserDisplayName(String userUid) throws UnknownUserException {
        try {
            org.sakaiproject.user.api.User sakaiUser =
            	org.sakaiproject.user.cover.UserDirectoryService.getUser(userUid);
            return sakaiUser.getDisplayName();
        } catch (UserNotDefinedException e) {
            throw new UnknownUserException("Unknown uid: " + userUid);
        }
    }
    
    public String getUserEmailAddress(String userUid) throws UnknownUserException {
    	try {
            org.sakaiproject.user.api.User sakaiUser =
            	org.sakaiproject.user.cover.UserDirectoryService.getUser(userUid);
            return sakaiUser.getEmail();
        } catch (UserNotDefinedException e) {
            throw new UnknownUserException("Unknown uid: " + userUid);
        }
    }
}
