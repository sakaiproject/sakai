/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

package org.sakaiproject.tool.gradebook.facades;

import org.sakaiproject.service.gradebook.shared.UnknownUserException;

/**
 * Facade to a user directory service provided by the framework.
 */
public interface UserDirectoryService {

    /**
     * Looks up a user's display name based on their uid.
     *
     * TODO See if there's some way to narrow this broad method. It's used only in two places:
     *
     * 1) When displaying a grade history log, it's used to obtain a grader's name based on their user UID.
     *    In this case, it couldn't be replaced by checking just people who play an explict part in the
     *    gradebook, since Authz may have let administrators change scores, or the grader may no longer play
     *    an active part. The only workaround is to pick up the currently authorized user's name and then
     *    store it as an additional field in the grade history log.
     * 2) In the student view, it's currently used to display the student's name. This case could be
     *    handled by supporting either a SectionAwareness getEnrollmentForUserUid() method or an Authn
     *    getCurrentUserDisplayName() method.
     */
    public String getUserDisplayName(String userUid) throws UnknownUserException;
    
    public String getUserEmailAddress(String userUid) throws UnknownUserException;
}

