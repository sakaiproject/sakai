/**
 * $Id$
 * $URL$
 * VoteEntityProvider.java - polls - Aug 22, 2008 9:50:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.poll.tool.entityproviders;

import java.io.OutputStream;
import java.util.ArrayList;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
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
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Entity provider which represents poll votes
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
 @Slf4j
public class PollVoteEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, 
    Createable, CollectionResolvable, Outputable, Inputable, Describeable, ActionsExecutable, Redirectable {

    @Setter private PollsService pollsService;
    @Setter private UsageSessionService usageSessionService;
    @Setter private UserDirectoryService userDirectoryService;

    public static final String PREFIX = "poll-vote";
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Deprecated
    public boolean entityExists(String id) {
        if (id == null) {
            return false;
        }
        if ("".equals(id)) {
            return true;
        }
        Optional<Vote> vote = getVoteById(id);
        return vote.isPresent();
    }

    @Deprecated
    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String userId = userDirectoryService.getCurrentUser().getId();
        Vote vote = (Vote) entity;
        
        log.debug("got vote: " + vote.toString());
        
        String pollId = (String)params.get("pollId");

        if (pollId == null) {
            throw new IllegalArgumentException("Poll Id must be set to create a vote");
        }


        Long optionId = null;
        try {
        	optionId = Long.valueOf((String)params.get("pollOption"));
        }
        catch (Exception e) {
        	log.warn(e.getMessage());
		}

        if (optionId == null) {
            throw new IllegalArgumentException("Poll Option must be set to create a vote");
        }
        if (! pollsService.isUserAllowedVote(userId, pollId, false)) {
            throw new SecurityException("User ("+userId+") is not allowed to vote in this poll ("+pollId+")");
        }

        // validate option
        Optional<Option> option = pollsService.getOptionById(optionId);
        if (option.isEmpty()) {
            throw new IllegalArgumentException("Invalid poll option ("+optionId+") [cannot find option] in vote ("+vote+") for user ("+userId+")");
        } else {
            vote.setOption(option.get());
            Poll optionPoll = option.get().getPoll();
            if (optionPoll == null || !pollId.equals(optionPoll.getId())) {
                throw new IllegalArgumentException("Invalid poll option ("+optionId+") [not in poll ("+pollId+")] in vote ("+vote+") for user ("+userId+")");
            }
        }
        // set default vote values
        vote.setVoteDate(Instant.now());
        vote.setUserId(userId);
        if (vote.getSubmissionId() == null) {
            String sid = userId + ":" + UUID.randomUUID();
            vote.setSubmissionId(sid);
        }
        // set the IP address
        UsageSession usageSession = usageSessionService.getSession();
        if (usageSession != null) {
            vote.setIp( usageSession.getIpAddress() );
        }
        Vote saved = pollsService.saveVote(vote);
        if (saved == null) {
            throw new IllegalStateException("Unable to save vote (" + vote + ") for user (" + userId + "): " + ref);
        }
        return vote.getId().toString();
    }

    public Object getSampleEntity() {
        return new Vote();
    }

    @Deprecated
    public Object getEntity(EntityReference ref) {
    	String id = ref.getId();
        String currentUser = developerHelperService.getCurrentUserReference();
        log.debug("current user is: "  + currentUser);
        if (currentUser == null || currentUser.length() == 0) {
            throw new EntityException("Anonymous users cannot view specific votes", ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
        }

        //is this a new object?
        if (ref.getId() == null) {
        	return new Vote();
        }

        Optional<Vote> voteOpt = getVoteById(id);
        if (voteOpt.isEmpty()) {
            throw new EntityException("Vote not found: " + id, ref.getId(), HttpServletResponse.SC_NOT_FOUND);
        }

        Vote vote = voteOpt.get();
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

    @Deprecated
    public List<?> getEntities(EntityReference ref, Search search) {
        String currentUserId = userDirectoryService.getCurrentUser().getId();
        
        Restriction pollRes = search.getRestrictionByProperty("pollId");

        if (pollRes == null || pollRes.getSingleValue() == null) {
          //  throw new IllegalArgumentException("Must include a non-null pollId in order to retreive a list of votes");
        	return null;
        }
        String pollId = null;
        boolean viewVoters = false;
        if (developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
        	viewVoters = true;
        }
        try {
            pollId = developerHelperService.convert(pollRes.getSingleValue(), String.class);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("Invalid: pollId must be a string: " + e.getMessage(), e);
        }
        Optional<Poll> poll = pollsService.getPollById(pollId);
        if (poll.isEmpty()) {
            throw new IllegalArgumentException("pollId ("+pollId+") is invalid and does not match any known polls");
        }
        List<Vote> votes = pollsService.getAllVotesForPoll(poll.get().getId());
        
        if (developerHelperService.isEntityRequestInternal(ref.toString())) {
            // ok for all internal requests
        } else if (!pollsService.isAllowedViewResults(poll.get(), currentUserId)) {
            // TODO - check vote location and perm?
            // not allowed to view
            throw new SecurityException("User ("+currentUserId+") cannot view vote ("+ref+")");
        }
        if (viewVoters)
        	return votes;
        else
        	return anonymizeVotes(votes);
    }

    @Deprecated
    private List<?> anonymizeVotes(List<Vote> votes) {
    	List<Vote> ret = new ArrayList<Vote>();
    	String userId = userDirectoryService.getCurrentUser().getId();

    	for (int i = 0; i < votes.size(); i++) {
    		Vote vote = (Vote)votes.get(i);
    		if (!userId.equals(vote.getUserId())) {
    			Vote newVote = new Vote();
    			newVote.setOption(vote.getOption());
    			newVote.setSubmissionId(vote.getSubmissionId());
    			ret.add(newVote);
    		} else {
    			ret.add(vote);
    		}

    	}
    	return ret;
    }
    
	/**
	 * Allows a user to create multiple Vote objects at once, taking one or more
	 * pollOption parameters.
	 */
    @Deprecated
	@EntityCustomAction(action = "vote", viewKey = EntityView.VIEW_NEW)
	public List<Vote> vote(EntityView view, EntityReference ref, String prefix, Search search, OutputStream out,
			Map<String, Object> params) {
		String pollId = (String) params.get("pollId");
		if (pollId == null) {
			throw new IllegalArgumentException("No pollId found.");
		}
		String userId = userDirectoryService.getCurrentUser().getId();
		Optional<Poll> poll = pollsService.getPollById(pollId);
		if (poll.isEmpty()) {
			throw new IllegalArgumentException("No poll found to update for the given reference: " + ref);
		}
		if (!pollsService.isUserAllowedVote(userId, poll.get().getId(), false)) {
			throw new SecurityException("User (" + userId + ") is not allowed to vote in this poll ("
					+ poll.get().getId() + ")");
		}

		Set<String> optionIds = new HashSet<String>();
		Object param = params.get("pollOption");
		if (param == null) {
			throw new IllegalArgumentException("At least one pollOption parameter must be provided to vote.");
		} else if (param instanceof String) {
			optionIds.add((String) param);
		} else if (param instanceof Iterable<?>) {
			for (Object o : (Iterable<?>) param)
				if (o instanceof String)
					optionIds.add((String) o);
				else
					throw new IllegalArgumentException("Each pollOption must be a String, not "
							+ o.getClass().getName());
		} else if (param instanceof Object[]) {
			for (Object o : (Object[]) param)
				if (o instanceof String)
					optionIds.add((String) o);
				else
					throw new IllegalArgumentException("Each pollOption must be a String, not "
							+ o.getClass().getName());
		} else
			throw new IllegalArgumentException("pollOption must be String, String[] or List<String>, not "
					+ param.getClass().getName());

		// Turn each option String into an Option, making sure that each is a
		// valid choice for the poll. We use a Map to make sure one cannot vote
		// more than once for any option by specifying it using equivalent
		// representations
		Map<Long, Option> options = new HashMap<>();
		for (String optionId : optionIds) {
			try {
				Optional<Option> option = pollsService.getOptionById(Long.valueOf(optionId));
				Poll optionPoll = option.get().getPoll();
				if (optionPoll == null || !poll.get().getId().equals(optionPoll.getId()))
					throw new Exception();
				options.put(option.get().getId(), option.get());
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid pollOption: " + optionId);
			}
		}

		// Validate that the number of options voted for is within acceptable
		// bounds.
		if (options.size() < poll.get().getMinOptions())
			throw new IllegalArgumentException("You must provide at least " + poll.get().getMinOptions() + " options, not "
					+ options.size() + ".");
		if (options.size() > poll.get().getMaxOptions())
			throw new IllegalArgumentException("You may provide at most " + poll.get().getMaxOptions() + " options, not "
					+ options.size() + ".");

		// Create and save the Vote objects.
		UsageSession usageSession = usageSessionService.getSession();
		List<Vote> votes = new ArrayList<>();
		for (Option option : options.values()) {
			Vote vote = new Vote();

			vote.setVoteDate(Instant.now());
			vote.setUserId(userId);
			vote.setOption(option);

			if (vote.getSubmissionId() == null) {
				String sid = userId + ":" + UUID.randomUUID();
				vote.setSubmissionId(sid);
			}

			if (usageSession != null)
				vote.setIp(usageSession.getIpAddress());

            Vote saved = pollsService.saveVote(vote);
			if (saved == null) {
				throw new IllegalStateException("Unable to save vote (" + vote + ") for user (" + userId + "): " + ref);
			}
			votes.add(vote);
		}
		return votes;
	}

	public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.FORM};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }


    /**
     * @param id
     * @return
     */
    @Deprecated
    private Optional<Vote> getVoteById(String id) {
        Long voteId;
        try {
            voteId = Long.valueOf(id);
        } catch (NumberFormatException e) {
            log.warn("Attempting to load a vote with an invalid id ({}): {}", id, e.toString());
            throw new EntityException("Invalid identifier provided for poll-vote", "", HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
        return pollsService.getVoteById(voteId);
    }

    @Deprecated
    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        throw new UnsupportedOperationException("Votes cannot currently be updated: " + ref);
    }

}
