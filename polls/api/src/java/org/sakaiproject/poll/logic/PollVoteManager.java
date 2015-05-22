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


package org.sakaiproject.poll.logic;

import java.util.List;
import java.util.Map;

import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;

public interface PollVoteManager {

    /**
     * Get a vote by id
     * @param voteId
     * @return the vote OR null if not found
     */
    public Vote getVoteById(Long voteId);

    public boolean saveVote(Vote vote);

    /**
     * Save a vote collection - a users collection of votes for a specific poll
     * @param voteCollection
     */
    public void saveVoteList(List<Vote> voteCollection);

    public List<Vote> getAllVotesForPoll(Poll poll);

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
    public boolean isUserAllowedVote(String userId, Long pollId, boolean ignoreVoted);

    public boolean userHasVoted(Long pollid, String userID);

    /**
     * Assumes current user
     * @param pollid
     * @return
     */
    public boolean userHasVoted(Long pollid);

    public int getDisctinctVotersForPoll(Poll poll);

    /**
     * Get all the votes for a specific user in a poll or polls (or all polls)
     * @param userId an internal user id (not username)
     * @param pollIds an array of all polls to get the votes for (null to get all)
     * @return the map of poll ID => list of votes for that poll for this user
     */
    public Map<Long, List<Vote>> getVotesForUser(String userId, Long[] pollIds);
    
    /**
     * Get all votes for Option
     * @param option
     * @return
     */
    public List<Vote> getAllVotesForOption(Option option);
    
    /**
     * Is the current user able to vote on this poll?
     *
     * @param poll
     * @return
     */
    public boolean pollIsVotable(Poll poll);
    
    /**
     * Delete the given vote
     * @param vote The vote to delete
     */
    public void deleteVote(Vote vote);

    /**
     * Delete the given votes
     * @param votes The votes to delete
     */
    public void deleteAll(List<Vote> votes);
}
