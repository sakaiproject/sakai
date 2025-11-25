/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
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

package org.sakaiproject.poll.api.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sakaiproject.poll.api.entity.PollEntity;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.model.VoteCollection;

/**
 * This is the interface for the Manager for our poll tool, 
 * it handles the data access functionality of the tool, we currently
 * have 2 implementations (memory and hibernate)
 * @author DH
 *
 */
public interface PollsService {

    /**
     * Delete an option from the database.
     * 
     * @param optionId - The option to delete
     */
    void deleteOption(Long optionId);

    /**
     * Delete a poll option, either "hard" or "soft".
     * 
     * @param optionId - The option to delete
     * @param soft - <b>true</b> if you want to "soft" delete (flag the 'deleted' field) or <b>false</b> to "hard" delete (remove from database) 
     */
    void deleteOption(Long optionId, boolean soft);
    
    /**
     * Delete a poll
     * @param id - the poll id to remove
     */
    void deletePoll(String id) throws SecurityException, IllegalArgumentException;
    
    /**
     * Gets all the Polls
     * @return - a collection of task objects (empty collection if none found)
     */
    List<Poll> findAllPolls();

    /**
     * Gets all the task objects for the site
     * @param siteId - the siteId of the site
     * @return - a collection of task objects (empty collection if none found)
     */
    List<Poll> findAllPolls(String siteId);

    /**
     * Get all the polls for a user in a set of sites (can be one) given the permission,
     * will return only polls that can be voted on if the permission is {@link org.sakaiproject.poll.api.PollConstants#PERMISSION_VOTE}
     * 
     * @param userId a sakai internal user id (not eid)
     * @param siteIds an array of site ids (can be null or empty to get the polls for all without security check)
     * @param permissionConstant either the {@link org.sakaiproject.poll.api.PollConstants#PERMISSION_VOTE} (for all polls a user can vote on) or
     * {@link org.sakaiproject.poll.api.PollConstants#PERMISSION_ADD} for all the polls the user can control
     * @return the list of all polls this user can access
     */
    List<Poll> findAllPollsForUserAndSitesAndPermission(String userId, String[] siteIds, String permissionConstant);

    /**
     *  Get a specific option by its id
     */
    Optional<Option> getOptionById(Long optionId);

    /**
     * Get options for the given poll that are not flagged as deleted.
     * @param id the id of the poll
     * @return
     * 	the options for the given poll that are not deleted OR empt if there are
     * 	no "visible" options for this poll
     * @throws IllegalArgumentException if the pollId is invalid
     */
    List<Option> getVisibleOptionsForPoll(String id);

    /**
     *  get a poll by its Entity  Reference  
     */
    Optional<Poll> getPoll(String ref);

    /**
     * Retrieve a specific poll
     * @param id
     * @return a single poll object
     */
    Optional<Poll> getPollById(String id) throws SecurityException;

    /**
     * Get a specific poll with all its votes
     * @param pollId
     * @return a poll object
     */
    Poll getPollWithVotes(String pollId);

    /**
     *  Can the this user view the results for this poll?
     * @param poll
     * @param userId
     * @return true if the user can view this poll
     */

    boolean isAllowedViewResults(Poll poll, String userId);

    /**
     * Save an individual option
     * @param t
     * @return
     */
    boolean saveOption(Option t);
    
    /**
     *  Save a poll
     * @param poll - the poll object to save
     * @return - the saved poll object
     */
    Poll savePoll(Poll poll) throws SecurityException, IllegalArgumentException;

    /**
     * Is this poll public?
     * @param poll
     * @return
     */
    boolean isPollPublic(Poll poll);

    /**
     * Is user allowed to delete the votes on a poll?
     *
     * @param poll - the poll object
     * @return true or false
     */
    boolean userCanDeletePoll(Poll poll);

    // Vote Management Methods

    /**
     * Get a vote by id
     * @param voteId the vote id
     * @return the vote OR null if not found
     */
    Vote getVoteById(Long voteId);

    /**
     * Save a vote
     * @param vote the vote to save
     * @return true if successful
     */
    boolean saveVote(Vote vote);

    /**
     * Save a vote collection - a users collection of votes for a specific poll
     * @param voteCollection the list of votes to save
     */
    void saveVoteList(List<Vote> voteCollection);

    /**
     * Create a new vote for the supplied poll option with the current user's context.
     *
     * @param poll the poll being voted on
     * @param option the poll option selected
     * @param submissionId identifier that groups the vote submission
     * @return a newly instantiated {@link Vote}
     */
    Vote createVote(Poll poll, Option option, String submissionId);

    /**
     * Get all votes for a poll
     * @param pollId the id of the poll
     * @return list of all votes for this poll
     */
    List<Vote> getAllVotesForPoll(String pollId);

    /**
     * Check if the given user can vote in the supplied poll,
     * also checks if the user has already voted, if so this will return false
     *
     * @param userId an internal user id
     * @param pollId the id of a poll
     * @param ignoreVoted if true then ignores the user's vote when checking,
     * else will only return true if the user is allowed AND has not already voted
     * @return true if user can vote OR false if not
     */
    boolean isUserAllowedVote(String userId, String pollId, boolean ignoreVoted);

    /**
     * Check if a user has voted in a poll
     * @param pollid the poll id
     * @param userID the user id
     * @return true if the user has voted
     */
    boolean userHasVoted(String pollid, String userID);

    /**
     * Check if the current user has voted in a poll
     * @param pollid the poll id
     * @return true if the current user has voted
     */
    boolean userHasVoted(String pollid);

    /**
     * Get the count of distinct voters for a poll
     * @param poll the poll
     * @return count of distinct voters
     */
    int getDisctinctVotersForPoll(Poll poll);

    /**
     * Get all the votes for a specific user in a poll or polls (or all polls)
     * @param userId an internal user id (not username)
     * @param pollIds an array of all polls to get the votes for (null to get all)
     * @return the map of poll ID => list of votes for that poll for this user
     */
    Map<String, List<Vote>> getVotesForUser(String userId, String[] pollIds);

    /**
     * Get all votes for an option
     * @param option the option
     * @return list of all votes for this option
     */
    List<Vote> getAllVotesForOption(Option option);

    /**
     * Is the current user able to vote on this poll?
     *
     * @param poll the poll
     * @return true if votable
     */
    boolean pollIsVotable(Poll poll);

    /**
     * Delete the given vote
     * @param vote The vote to delete
     */
    void deleteVote(Vote vote);

    /**
     * Delete the given votes
     * @param votes The votes to delete
     */
    void deleteAll(List<Vote> votes);

    // EntityBroker Entity Adapter Methods

    /**
     * Create a PollEntity adapter from a Poll domain entity by poll ID.
     * Computes user-specific presentation data (currentUserVoted).
     * Constructs the EntityBroker reference internally.
     *
     * @param pollId The poll ID
     * @param includeVotes Include votes in response
     * @param includeOptions Include options in response (overrides default lazy loading)
     * @return PollEntity with requested data
     * @throws SecurityException if user doesn't have access
     */
    PollEntity createPollEntity(String pollId, boolean includeVotes, boolean includeOptions) throws SecurityException;

    /**
     * Create a PollEntity adapter from an existing Poll object.
     * Computes user-specific presentation data (currentUserVoted).
     * Constructs the EntityBroker reference internally.
     *
     * @param poll The Poll domain entity
     * @param includeVotes Include votes in response
     * @param includeOptions Include options in response (overrides default lazy loading)
     * @return PollEntity with requested data
     */
    PollEntity createPollEntity(Poll poll, boolean includeVotes, boolean includeOptions);

    /**
     * Update poll from PollEntity (from XHR/REST requests).
     * Extracts changes from PollEntity and updates the persisted Poll.
     *
     * @param pollId The poll ID to update
     * @param pollEntity PollEntity with updated values
     * @return Updated Poll entity
     * @throws SecurityException if user doesn't have permission to update
     * @throws IllegalArgumentException if poll doesn't exist
     */
    Poll updatePollFromEntity(String pollId, PollEntity pollEntity) throws SecurityException, IllegalArgumentException;

    /**
     * Create new poll from PollEntity (from XHR/REST requests).
     *
     * @param pollEntity PollEntity with poll data
     * @return Created Poll entity
     * @throws SecurityException if user doesn't have permission to create
     * @throws IllegalArgumentException if data is invalid
     */
    Poll createPollFromEntity(PollEntity pollEntity) throws SecurityException, IllegalArgumentException;

    // Bulk Operations

    /**
     * Delete multiple polls
     * @param pollIds collection of poll IDs to delete
     */
    void deletePolls(java.util.Collection<String> pollIds);

    /**
     * Reset votes for multiple polls
     * @param pollIds collection of poll IDs to reset votes for
     */
    void resetPollVotes(java.util.Collection<String> pollIds);

    /**
     * Delete an option with orphan vote handling
     * @param optionId the option ID to delete
     * @param orphanVoteHandling how to handle orphaned votes ("do-nothing" or "return-votes")
     * @return the poll that contained the deleted option
     */
    Poll deleteOptionWithVoteHandling(Long optionId, String orphanVoteHandling);

    /**
     * Save multiple options in batch
     * @param pollId the poll ID to add options to
     * @param optionTexts list of option text values to add
     */
    void saveOptionsBatch(String pollId, java.util.List<String> optionTexts);

    /**
     * Submit a vote for a poll
     * @param pollId the poll ID
     * @param selectedOptionIds list of selected option IDs
     * @return the vote collection
     * @throws IllegalArgumentException if the vote is invalid
     */
    VoteCollection submitVote(String pollId, java.util.List<Long> selectedOptionIds);
}
