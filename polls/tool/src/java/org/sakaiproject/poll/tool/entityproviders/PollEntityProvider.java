/**
 * $Id$
 * $URL$
 * PollEntityProvider.java - polls - Aug 21, 2008 7:34:47 PM - azeckoski
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;

import lombok.extern.slf4j.Slf4j;


/**
 * Handles the poll entity
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class PollEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful, 
        RequestStorable, RedirectDefinable {

    private PollListManager pollListManager;
    public void setPollListManager(PollListManager pollListManager) {
        this.pollListManager = pollListManager;
    }

    private PollVoteManager pollVoteManager;
    public void setPollVoteManager(PollVoteManager pollVoteManager) {
        this.pollVoteManager = pollVoteManager;
    }

    public static final String PREFIX = "poll";
    public String getEntityPrefix() {
        return PREFIX;
    }

    public TemplateMap[] defineURLMappings() {
        return new TemplateMap[] {
                new TemplateMap("/{prefix}/{pollId}/vote", PollVoteEntityProvider.PREFIX + "{dot-extension}"), // all votes in a poll
                new TemplateMap("/{prefix}/{pollId}/option", PollOptionEntityProvider.PREFIX + "{dot-extension}"), // all options in a poll
                new TemplateMap("/{prefix}/site/{siteId}", "{prefix}{dot-extension}") // all polls in a site
        };
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
     */
    @Deprecated
    public boolean entityExists(String id) {
        if (id == null) {
            return false;
        }
        if ("".equals(id)) {
            return true;
        }
        Poll poll = getPollById(id);
        boolean exists = (poll != null);
        return exists;
    }

    /**
     * @param id
     * @return
     */
    @Deprecated
    private Poll getPollById(String id) {
        Long pollId;
        try {
            pollId = Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid poll id ("+id+"), the id must be a number");
        }
        Poll poll = pollListManager.getPollById(pollId, false);
        return poll;
    }

    /**
     * Note that details is the only optional field
     */
    @Deprecated
    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        Poll poll = (Poll) entity;
        poll.setCreationDate(new Date());
        if (poll.getId() == null) {
            poll.setId( UUID.randomUUID().toString() );
        }
        if (poll.getOwner() == null) {
            poll.setOwner( developerHelperService.getCurrentUserId() );
        }
        String siteId = developerHelperService.getCurrentLocationId();
        if (poll.getSiteId() == null) {
            poll.setSiteId( siteId );
        } else {
            siteId = poll.getSiteId();
        }
        String userReference = developerHelperService.getCurrentUserReference();
        String location = "/site/" + siteId;
        boolean allowed = developerHelperService.isUserAllowedInEntityReference(userReference, PollListManager.PERMISSION_ADD, location);
        if (!allowed) {
            throw new SecurityException("Current user ("+userReference+") cannot create polls in location ("+location+")");
        }
        pollListManager.savePoll(poll);
        return poll.getPollId()+"";
    }

    public Object getSampleEntity() {
        return new Poll();
    }

    @Deprecated
    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String id = ref.getId();
        if (id == null) {
            throw new IllegalArgumentException("The reference must include an id for updates (id is currently null)");
        }
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new SecurityException("anonymous user cannot update poll: " + ref);
        }
        Poll current = getPollById(id);
        if (current == null) {
            throw new IllegalArgumentException("No poll found to update for the given reference: " + ref);
        }
        Poll poll = (Poll) entity;
        String siteId = developerHelperService.getCurrentLocationId();
        if (poll.getSiteId() == null) {
            poll.setSiteId( siteId );
        } else {
            siteId = poll.getSiteId();
        }
        String location = "/site/" + siteId;
        // should this check a different permission?
        boolean allowed = developerHelperService.isUserAllowedInEntityReference(userReference, PollListManager.PERMISSION_ADD, location);
        if (!allowed) {
            throw new SecurityException("Current user ("+userReference+") cannot update polls in location ("+location+")");
        }
        developerHelperService.copyBean(poll, current, 0, new String[] {"id", "pollId", "owner","siteId","creationDate","reference","url","properties"}, true);
        pollListManager.savePoll(current);
    }

    @Deprecated
    public Object getEntity(EntityReference ref) {
        String id = ref.getId();
        if (id == null) {
            return new Poll();
        }
        Poll poll = getPollById(id);
        if (poll == null) {
            throw new IllegalArgumentException("No poll found for the given reference: " + ref);
        }
        Long pollId = poll.getPollId();
        String currentUserId = developerHelperService.getCurrentUserId();
        
        boolean allowedManage = false;
        if (! developerHelperService.isEntityRequestInternal(ref+"")) {
            if (!pollListManager.isPollPublic(poll)) {
                //this is not a public poll? (ie .anon role has poll.vote)
                String userReference = developerHelperService.getCurrentUserReference();
                if(userReference == null) {
                    throw new EntityException("User must be logged in in order to access poll data", ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
                }
                allowedManage = developerHelperService.isUserAllowedInEntityReference(userReference, PollListManager.PERMISSION_ADD, "/site/" + poll.getSiteId());
                boolean allowedVote = developerHelperService.isUserAllowedInEntityReference(userReference, PollListManager.PERMISSION_VOTE, "/site/" + poll.getSiteId());
                if (!allowedManage && !allowedVote) {
                    throw new SecurityException("User ("+userReference+") not allowed to access poll data: " + ref);
                }
           }
        }
	
        Boolean includeVotes = requestStorage.getStoredValueAsType(Boolean.class, "includeVotes");
        if (includeVotes == null) { includeVotes = false; }
        if (includeVotes) {
            List<Vote> votes = pollVoteManager.getAllVotesForPoll(poll);
            poll.setVotes(votes);
        }
        Boolean includeOptions = requestStorage.getStoredValueAsType(Boolean.class, "includeOptions");
        if (includeOptions == null) { includeOptions = false; }
        if (includeOptions) {
            List<Option> options = pollListManager.getOptionsForPoll(poll);
            poll.setOptions(options);
        }
        // add in the indicator that this user has replied
        if (currentUserId != null) {
            Map<Long, List<Vote>> voteMap = pollVoteManager.getVotesForUser(currentUserId, new Long[] {pollId});
            List<Vote> l = voteMap.get(pollId);
            if (l != null) {
                poll.setCurrentUserVoted(true);
                poll.setCurrentUserVotes(l);
            } else {
                poll.setCurrentUserVoted(false);
            }
        }
        return poll;
    }

    @Deprecated
    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        String id = ref.getId();
        if (id == null) {
            throw new IllegalArgumentException("The reference must include an id for deletes (id is currently null)");
        }
        Poll poll = getPollById(id);
        if (poll == null) {
            throw new IllegalArgumentException("No poll found for the given reference: " + ref);
        }
        try {
            pollListManager.deletePoll(poll);
        } catch (SecurityException e) {
            throw new SecurityException("The current user ("+developerHelperService.getCurrentUserReference()
                    +") is not allowed to delete this poll: " + ref);
        }
    }

    @Deprecated
    public List<?> getEntities(EntityReference ref, Search search) {
        log.info("get entities");
        // get the setting which indicates if we are getting polls we can admin or polls we can take
        boolean adminControl = false;
        Restriction adminRes = search.getRestrictionByProperty("admin");
        if (adminRes != null) {
            adminControl = developerHelperService.convert(adminRes.getSingleValue(), boolean.class);
        }
        // get the location (if set)
        Restriction locRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_LOCATION_REFERENCE); //requestStorage.getStoredValueAsType(String.class, "siteId");
        String[] siteIds = null;
        if (locRes != null) {
            String siteId = developerHelperService.getLocationIdFromRef(locRes.getStringValue());
            siteIds = new String[] {siteId};
        }
        // get the user (if set)
        Restriction userRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_USER_REFERENCE);
        String userId = null;
        if (userRes != null) {
            String currentUser = developerHelperService.getCurrentUserReference();
            String userReference = userRes.getStringValue(); 
            if (userReference == null) {
                throw new IllegalArgumentException("Invalid request: Cannot limit polls by user when the value is null");
            }
            if (userReference.equals(currentUser) || developerHelperService.isUserAdmin(currentUser)) {
                userId = developerHelperService.getUserIdFromRef(userReference); //requestStorage.getStoredValueAsType(String.class, "userId");
            } else {
                throw new SecurityException("Only the admin can get polls for other users, you requested polls for: " + userReference);
            }
        } else {
            userId = developerHelperService.getCurrentUserId();
            if (userId == null) {
                throw new EntityException("No user is currently logged in so no polls data can be retrieved", ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        String perm = PollListManager.PERMISSION_VOTE;
        if (adminControl) {
            perm = PollListManager.PERMISSION_ADD;
        }
        List<Poll> polls = pollListManager.findAllPollsForUserAndSitesAndPermission(userId, siteIds, perm);
        if (adminControl) {
            // add in options
            for (Poll p : polls) {
                List<Option> options = pollListManager.getOptionsForPoll(p.getPollId());
                p.setOptions(options);
            }
        } else {
            // add in the indicators that this user has replied
            Long[] pollIds = new Long[polls.size()];
            for (int i = 0; i < polls.size(); i++) {
                pollIds[i] = polls.get(i).getPollId();
            }
            Map<Long, List<Vote>> voteMap = pollVoteManager.getVotesForUser(userId, pollIds);
            for (Poll poll : polls) {
                Long pollId = poll.getPollId();
                List<Vote> l = voteMap.get(pollId);
                if (l != null) {
                    poll.setCurrentUserVoted(true);
                    poll.setCurrentUserVotes(l);
                } else {
                    poll.setCurrentUserVoted(false);
                }
            }
        }
        return polls;
    }

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }

    RequestStorage requestStorage = null;
    public void setRequestStorage(RequestStorage requestStorage) {
        this.requestStorage = requestStorage;
    }

}
