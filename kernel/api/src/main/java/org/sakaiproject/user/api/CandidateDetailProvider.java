/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.user.api;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

import java.util.List;
import java.util.Optional;

/**
 * This is a provider interface that allows Assignments to provide addition details about candidates
 * to the interface.
 */
public interface CandidateDetailProvider {
    /**
     * This gets an candidate ID for a user, this can be used to make candidate IDs anonymous.
     *
     * @param user The user for who an ID is wanted. Cannot be <code>null</code>
     * @param site The site in which the lookup is happening. If site is null, it will try to get the current site
     * @return An option containing the candidate ID.
     */
    Optional<String> getCandidateID(User user, Site site);

    /**
     * Should the candidate id (institutional anonymous id) be used for this site.
     * @param site The site in which the lookup is happening.
     * @return If <code>true</code> then use the candidateid for this site.
     */
    boolean useInstitutionalAnonymousId(Site site);

    /**
     * This gets additional notes for a user.
     * @param user The user for who addition notes are wanted. Cannot be <code>null</code>
     * @param site The site in which the lookup is happening. If site is null, it will try to get the current site
     * @return An option containing the additional user notes.
     */
    Optional<List<String>> getAdditionalNotes(User user, Site site);

    /**
     * Is the additional notes enabled for this site.
     * @param site The site in which the lookup is happening.
     * @return If <code>true</code> then show the additional details for this site.
     */
     boolean isAdditionalNotesEnabled(Site site);

    /**
     * Gets the student number (institutional numeric id) for the given user.
     * 
     * @param user the user to retrieve the student number for
     * @param site the site in which the lookup is happening
     * @return the user's student number, or empty if cannot be found
     */
    public Optional<String> getInstitutionalNumericId(User user, Site site);

    /**
     * Gets the student number (institutional numeric id) for the given user, ignoring their student
     * number visibility permission. This method is intended for use only when there is a business case
     * requiring full access to student numbers. It is not intended for use when displaying student numbers
     * to end users.
     * @param candidate the user to retrieve the student number for
     * @param site the site in which the lookup is happening
     * @return the user's student number, or empty if cannot be found
     */
    public Optional<String> getInstitutionalNumericIdIgnoringCandidatePermissions(User candidate, Site site);

    /**
     * Should the student number (institutional numeric id) be used for this site.
     * @param site The site in which the lookup is happening
     * @return true if the property is enabled at the site or system level
     */
    boolean isInstitutionalNumericIdEnabled(Site site);
}
