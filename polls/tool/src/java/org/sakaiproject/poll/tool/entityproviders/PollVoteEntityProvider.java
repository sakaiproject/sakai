/**
 * $Id$
 * $URL$
 * VoteEntityProvider.java - polls - Aug 22, 2008 9:50:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
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
 */

package org.sakaiproject.poll.tool.entityproviders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;


/**
 * Entity provider which represents poll votes
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class PollVoteEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, 
    Createable, CollectionResolvable, Outputable, Inputable, Describeable, ActionsExecutable, Redirectable {

    private PollListManager pollListManager;
    public void setPollListManager(final PollListManager pollListManager) {
        this.pollListManager = pollListManager;
    }

    private PollVoteManager pollVoteManager;
    public void setPollVoteManager(final PollVoteManager pollVoteManager) {
        this.pollVoteManager = pollVoteManager;
    }

    public static final String PREFIX = "poll-vote";
    public String getEntityPrefix() {
        return PREFIX;
    }

    public boolean entityExists(String id) {
        if (id == null) {
            return false;
        }
        if ("".equals(id)) {
            return true;
        }
        Vote vote = getVoteById(id);
        boolean exists = (vote != null);
        return exists;
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String userId = developerHelperService.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("user must be logged in to create new votes");
        }
        Vote vote = (Vote) entity;
        if (vote.getPollId() == null) {
            throw new IllegalArgumentException("Poll Id must be set to create a vote");
        }
        Long pollId = vote.getPollId();
        if (vote.getPollOption() == null) {
            throw new IllegalArgumentException("Poll Option must be set to create a vote");
        }
        if (! pollVoteManager.isUserAllowedVote(userId, pollId, false)) {
            throw new SecurityException("User ("+userId+") is not allowed to vote in this poll ("+pollId+")");
        }
        // validate option
        Option option = pollListManager.getOptionById(vote.getPollOption());
        if (option == null) {
            throw new IllegalArgumentException("Invalid poll option ("+vote.getPollOption()+") [cannot find option] in vote ("+vote+") for user ("+userId+")");
        } else {
            if (! pollId.equals(option.getPollId())) {
                throw new IllegalArgumentException("Invalid poll option ("+vote.getPollOption()+") [not in poll ("+pollId+")] in vote ("+vote+") for user ("+userId+")");
            }
        }
        // set default vote values
        vote.setVoteDate( new Date() );
        vote.setUserId(userId);
        if (vote.getSubmissionId() == null) {
            String sid = userId + ":" + UUID.randomUUID();
            vote.setSubmissionId(sid);
        }
        // set the IP address
        UsageSession usageSession = UsageSessionService.getSession();
        if (usageSession != null) {
            vote.setIp( usageSession.getIpAddress() );
        }
        boolean saved = pollVoteManager.saveVote(vote);
        if (!saved) {
            throw new IllegalStateException("Unable to save vote ("+vote+") for user ("+userId+"): " + ref);
        }
        return vote.getId()+"";
    }

    public Object getSampleEntity() {
        return new Vote();
    }

    public Object getEntity(EntityReference ref) {
        String id = ref.getId();
        if (id == null) {
            return new Vote();
        }
        String currentUser = developerHelperService.getCurrentUserReference();
        if (currentUser == null) {
            throw new SecurityException("Anonymous users cannot view specific votes: " + ref);
        }
        Vote vote = getVoteById(id);
        String userId = developerHelperService.getUserIdFromRef(currentUser);
        if (developerHelperService.isUserAdmin(currentUser)) {
            // ok to view this vote
        } else if (userId.equals(vote.getUserId())) {
            // ok to view own
        } else if (developerHelperService.isEntityRequestInternal(ref.toString())) {
            // ok for all internal requests
        } else {
            // TODO - check vote location and perm?
            // not allowed to view
            throw new SecurityException("User ("+currentUser+") cannot view vote ("+ref+")");
        }
        return vote;
    }

    public List<?> getEntities(EntityReference ref, Search search) {
        String currentUser = developerHelperService.getCurrentUserReference();
        if (currentUser == null) {
            throw new SecurityException("Anonymous users cannot view votes: " + ref);
        }
        Restriction pollRes = search.getRestrictionByProperty("pollId");
        if (pollRes == null || pollRes.getSingleValue() == null) {
            throw new IllegalArgumentException("Must include a non-null pollId in order to retreive a list of votes");
        }
        Long pollId = null;
        boolean viewVoters = false;
        try {
            pollId = developerHelperService.convert(pollRes.getSingleValue(), Long.class);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("Invalid: pollId must be a long number: " + e.getMessage(), e);
        }
        Poll poll = pollListManager.getPollById(pollId);
        if (poll == null) {
            throw new IllegalArgumentException("pollId ("+pollId+") is invalid and does not match any known polls");
        }
        List<Vote> votes = pollVoteManager.getAllVotesForPoll(poll);
        
        //check permissions
        String userId = developerHelperService.getUserIdFromRef(currentUser);
        if (developerHelperService.isUserAdmin(currentUser)) {
            // ok to view this vote
        	viewVoters = true;
        } else if (developerHelperService.isEntityRequestInternal(ref.toString())) {
            // ok for all internal requests
        } else if (!pollListManager.isAllowedViewResults(poll, userId)) {
            // TODO - check vote location and perm?
            // not allowed to view
            throw new SecurityException("User ("+currentUser+") cannot view vote ("+ref+")");
        }
        
        if (viewVoters)
        	return votes;
        else 
        	return anonymizeVotes(votes);
    }

    private List<?> anonymizeVotes(List<Vote> votes) {
    	List<Vote> ret = new ArrayList<Vote>();
    	String userId = developerHelperService.getCurrentUserId();

    	for (int i = 0; i < votes.size(); i++) {
    		Vote vote = (Vote)votes.get(i);
    		if (!userId.equals(vote.getUserId())) {
    			Vote newVote = new Vote();
    			newVote.setPollId(vote.getPollId());
    			newVote.setPollOption(vote.getPollOption());
    			newVote.setSubmissionId(vote.getSubmissionId());
    			ret.add(newVote);
    		} else {
    			ret.add(vote);
    		}
    		
    	}
    	return ret;
    }

	public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }


    /**
     * @param id
     * @return
     */
    private Vote getVoteById(String id) {
        Long voteId;
        try {
            voteId = new Long(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert id ("+id+") to long: " + e.getMessage(), e);
        }
        Vote vote = pollVoteManager.getVoteById(voteId);
        return vote;
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        throw new UnsupportedOperationException("Votes cannot currently be updated: " + ref);
    }

}
