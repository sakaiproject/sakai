/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009, 2010 Sakai Foundation
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

package org.sakaiproject.user.api;

import org.sakaiproject.user.api.UserDirectoryService.PasswordRating;

/**
 * This interface provides the method stubs needed for any password policy object. 
 * All password policy implementations need to implement this interface.
 * 
 * https://jira.sakaiproject.org/browse/KNL-1123
 */
public interface PasswordPolicyProvider {

    /** sakai.property for minimum password entropy */
    public static final String SAK_PROP_PROVIDER_NAME = "user.password.policy.provider.name";

    /**
     * This function returns a boolean value of true/false, depending on if the given password meets the validation criteria.
     * 
     * @param password the password to be validated
     * @param user the user who this password belongs to (may be null if no user associated)
     * @return the password rating enum
     */
    public PasswordRating validatePassword(String password, User user);

}
