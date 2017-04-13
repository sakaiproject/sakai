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
}
