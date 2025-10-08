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

package org.sakaiproject.poll.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.repository.PollRepository;
import org.sakaiproject.poll.repository.VoteRepository;

@Slf4j
@Setter
public class PollVoteManagerImpl implements PollVoteManager {

	private ExternalLogic externalLogic;    
	private VoteRepository voteRepository;
	private PollRepository pollRepository;
	private PollListManager pollListManager;

	public void saveVoteList(List<Vote> votes) {
		Long pollId = null;
		for (int i =0; i < votes.size(); i ++) {
			Vote vote = (Vote)votes.get(i);
			pollId = vote.getPollId();
			saveVote(vote);
            externalLogic.registerStatement(pollListManager.getPollById(pollId).getText(), vote);
		}
	}

	public boolean saveVote(Vote vote)  {
		voteRepository.save(vote);
		log.debug(" Vote  " + vote.getId() + " successfuly saved");
		return true;
	}

	public List<Vote> getAllVotesForPoll(Poll poll) {
		return voteRepository.findByPollId(poll.getPollId());
	}

	public List<Vote> getAllVotesForOption(Option option) {

		return voteRepository.findByPollIdAndPollOption(option.getPollId(), option.getOptionId());
	}

	public Map<Long, List<Vote>> getVotesForUser(String userId, Long[] pollIds) {
		if (userId == null) {
			throw new IllegalArgumentException("userId cannot be null");
		}

		List<Vote> votes;
		if (pollIds == null) {
			votes = voteRepository.findByUserId(userId);
		} else if (pollIds.length > 0) {
			votes = voteRepository.findByUserIdAndPollIds(userId, Arrays.asList(pollIds));
		} else {
			return new HashMap<>();
		}

		Map<Long, List<Vote>> map = new HashMap<>();
		for (Vote vote : votes) {
			Long pollId = vote.getPollId();
			map.computeIfAbsent(pollId, key -> new ArrayList<>()).add(vote);
		}
		return map;
	}

	public int getDisctinctVotersForPoll(Poll poll) {
		return voteRepository.countDistinctSubmissionIds(poll.getPollId());
	}

	public boolean userHasVoted(Long pollid, String userID) {
		return voteRepository.existsByPollIdAndUserId(pollid, userID);
	}

	public boolean userHasVoted(Long pollId) {

		return userHasVoted(pollId, externalLogic.getCurrentUserId());
	}

	public Vote getVoteById(Long voteId) {
		if (voteId == null) {
			throw new IllegalArgumentException("voteId cannot be null when getting vote");
		}
		return voteRepository.findById(voteId).orElse(null);
	}

	public boolean isUserAllowedVote(String userId, Long pollId, boolean ignoreVoted) {
		boolean allowed = false;
		//pollId
		Poll poll =  pollRepository.findById(pollId).orElse(null);
		if (poll == null) {
			throw new IllegalArgumentException("Invalid poll id ("+pollId+") when checking user can vote");
		}
		if (externalLogic.isUserAdmin(userId)) {
			allowed = true;
		} else {
			String siteRef = "/site/" + poll.getSiteId();
			if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_VOTE, siteRef, "/user/" +userId)) {
				if (ignoreVoted) {
					allowed = true;
				} else {
					Map<Long, List<Vote>> m = getVotesForUser(userId, new Long[] {pollId});
					if (m.isEmpty()) {
						allowed = true;
					}
				}
			}
		}
		return allowed;
	}


	public boolean pollIsVotable(Poll poll)
	{
		//POLL-148 this could be null
		if (poll == null) {
			return false;
		}
		
		//poll must have options to be votable

		List<Option> votableOptions = pollListManager.getVisibleOptionsForPoll(poll.getPollId());
		if (votableOptions == null || votableOptions.size() == 0) {
			log.debug("poll has no options");
			return false;
		}

		boolean pollAfterOpen = true;
		boolean pollBeforeClose = true;

		if (poll.getVoteClose()!=null) {
			if (poll.getVoteClose().before(new Date())) {
				log.debug("Poll is closed for voting");
				pollBeforeClose=false;
			}

		} 

		if (poll.getVoteOpen()!=null) {
			if(new Date().before(poll.getVoteOpen())) {
				log.debug("Poll is not open yet");
				pollAfterOpen=false;
			}
		} 

		if (pollAfterOpen && pollBeforeClose)
		{
			if (poll.getLimitVoting() && userHasVoted(poll.getPollId())) {
				return false;
			}
			//the user hasn't voted do they have permission to vote?'
			log.debug("about to check if this user can vote in " + poll.getSiteId());
			if (externalLogic.isAllowedInLocation("poll.vote", externalLogic.getSiteRefFromId(poll.getSiteId())) || externalLogic.isUserAdmin())
			{
				log.debug("this poll is votable because the user has permissions, " + poll.getText());
				return true;
			}
			
			//SAK-18855 individual public polls
			if(poll.getIsPublic()) {
				log.debug("this poll is votable because it is public, " + poll.getText());
				return true;
			}
		}

		return false;
	}

	public void deleteVote(Vote vote) {
		voteRepository.delete(vote);
	}

    public void deleteAll(List<Vote> votes) {
        voteRepository.deleteAll(votes);
    }
}
