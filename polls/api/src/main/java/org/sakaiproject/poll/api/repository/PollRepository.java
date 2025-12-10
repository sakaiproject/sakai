package org.sakaiproject.poll.api.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface PollRepository extends SpringCrudRepository<Poll, String> {

    /**
     * Finds all polls for a given site, ordered by creation date in descending order.
     * Returns an empty list if the site ID is null.
     *
     * @param siteId the site identifier
     * @return list of polls ordered by creation date (newest first), or empty list if site ID is null
     */
    List<Poll> findBySiteIdOrderByCreationDateDesc(String siteId);

    /**
     * Finds all polls for multiple sites, ordered by creation date in ascending order.
     * Returns an empty list if the site IDs list is null or empty.
     *
     * @param siteIds the list of site identifiers
     * @return list of polls ordered by creation date (oldest first), or empty list if site IDs is null or empty
     */
    List<Poll> findBySiteIdsOrderByCreationDate(List<String> siteIds);

    /**
     * Finds all currently open polls for multiple sites.
     * A poll is considered open if the current instant falls between its voteOpen and voteClose dates.
     * Returns an empty list if any parameter is null or if site IDs list is empty.
     *
     * @param siteIds the list of site identifiers
     * @param now the current instant to check against poll open/close dates
     * @return list of open polls ordered by creation date (oldest first), or empty list if parameters are invalid
     */
    List<Poll> findOpenPollsBySiteIds(List<String> siteIds, Instant now);

    /**
     * Finds Options by the poll ID.
     *
     * @param pollId the poll identifier
     * @return List containing the options if found, empty List otherwise
     */
    List<Option> findOptionsByPollId(String pollId);

    /**
     * Finds the poll that contains the option with the specified ID.
     * Useful for navigating from an option back to its parent poll.
     *
     * @param optionId the option identifier
     * @return Optional containing the poll if found, empty Optional if option ID is null or not found
     */
    Optional<Option> findOptionByOptionId(Long optionId);
}
