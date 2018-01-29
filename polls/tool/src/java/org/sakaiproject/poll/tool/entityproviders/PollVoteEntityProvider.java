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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

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
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Entity provider which represents poll votes
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
 @Slf4j
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

    private UsageSessionService usageSessionService;    
    public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}
    
    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
    	this.userDirectoryService = userDirectoryService;
    }

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
        Vote vote = getVoteById(id);
        boolean exists = (vote != null);
        return exists;
    }

    @Deprecated
    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String userId = userDirectoryService.getCurrentUser().getId();
        Vote vote = (Vote) entity;
        
        log.debug("got vote: " + vote.toString());
        
        Long pollId = null;
        try {
        	pollId = Long.valueOf((String)params.get("pollId"));
        }
        catch (Exception e) {
			log.warn(e.getMessage());
		}
        
        if (pollId == null) {
            throw new IllegalArgumentException("Poll Id must be set to create a vote");
        }
        
        vote.setPollId(pollId);
        
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
        if (! pollVoteManager.isUserAllowedVote(userId, pollId, false)) {
            throw new SecurityException("User ("+userId+") is not allowed to vote in this poll ("+pollId+")");
        }
        
        vote.setPollOption(optionId);
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
        UsageSession usageSession = usageSessionService.getSession();
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
        	new Vote();
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
        
        if (id == null) {
            return new Vote();
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
        Long pollId = null;
        boolean viewVoters = false;
        if (developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
        	viewVoters = true;
        }
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
        
        if (developerHelperService.isEntityRequestInternal(ref.toString())) {
            // ok for all internal requests
        } else if (!pollListManager.isAllowedViewResults(poll, currentUserId)) {
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
    
	/**
	 * Allows a user to create multiple Vote objects at once, taking one or more
	 * pollOption parameters.
	 */
    @Deprecated
	@EntityCustomAction(action = "vote", viewKey = EntityView.VIEW_NEW)
	public List<Vote> vote(EntityView view, EntityReference ref, String prefix, Search search, OutputStream out,
			Map<String, Object> params) {
		Long pollId = null;
		try {
			pollId = Long.valueOf((String) params.get("pollId"));
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("No pollId found.");
		}
		String userId = userDirectoryService.getCurrentUser().getId();
		Poll poll = pollListManager.getPollById(pollId, false);
		if (poll == null) {
			throw new IllegalArgumentException("No poll found to update for the given reference: " + ref);
		}
		if (!pollVoteManager.isUserAllowedVote(userId, poll.getPollId(), false)) {
			throw new SecurityException("User (" + userId + ") is not allowed to vote in this poll ("
					+ poll.getPollId() + ")");
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
		Map<Long, Option> options = new HashMap<Long, Option>();
		for (String optionId : optionIds) {
			try {
				Option option = pollListManager.getOptionById(Long.valueOf(optionId));
				if (!poll.getPollId().equals(option.getPollId()))
					throw new Exception();
				options.put(option.getOptionId(), option);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid pollOption: " + optionId);
			}
		}

		// Validate that the number of options voted for is within acceptable
		// bounds.
		if (options.size() < poll.getMinOptions())
			throw new IllegalArgumentException("You must provide at least " + poll.getMinOptions() + " options, not "
					+ options.size() + ".");
		if (options.size() > poll.getMaxOptions())
			throw new IllegalArgumentException("You may provide at most " + poll.getMaxOptions() + " options, not "
					+ options.size() + ".");

		// Create and save the Vote objects.
		UsageSession usageSession = usageSessionService.getSession();
		List<Vote> votes = new ArrayList<Vote>();
		for (Option option : options.values()) {
			Vote vote = new Vote();

			vote.setVoteDate(new Date());
			vote.setUserId(userId);
			vote.setPollId(poll.getPollId());
			vote.setPollOption(option.getOptionId());

			if (vote.getSubmissionId() == null) {
				String sid = userId + ":" + UUID.randomUUID();
				vote.setSubmissionId(sid);
			}

			if (usageSession != null)
				vote.setIp(usageSession.getIpAddress());

			boolean saved = pollVoteManager.saveVote(vote);
			if (!saved) {
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
    private Vote getVoteById(String id) {
        Long voteId;
        try {
            voteId = Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert id ("+id+") to long: " + e.getMessage(), e);
        }
        Vote vote = pollVoteManager.getVoteById(voteId);
        return vote;
    }

    @Deprecated
    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        throw new UnsupportedOperationException("Votes cannot currently be updated: " + ref);
    }

}
