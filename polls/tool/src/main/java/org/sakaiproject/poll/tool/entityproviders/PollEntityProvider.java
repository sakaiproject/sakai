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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

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
import org.sakaiproject.poll.api.entity.PollEntity;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Poll;

import static org.sakaiproject.poll.api.PollConstants.*;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * Handles the poll entity
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class PollEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful,
        RequestStorable, RedirectDefinable {

    @Setter private PollsService pollsService;

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
        return pollsService.getPollByIdForReadOrEdit(id).isPresent();
    }

    /**
     * @param id
     * @return
     */
    @Deprecated
    private Optional<Poll> getPollById(String id) {
        return pollsService.getPollById(id);
    }

    /**
     * Note that details is the only optional field
     */
    @Deprecated
    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        Poll poll = (Poll) entity;
        poll.setId(null);
        poll.setCreationDate(Instant.now());
        poll.setOwner(developerHelperService.getCurrentUserId());
        if (poll.getSiteId() == null) {
            poll.setSiteId(developerHelperService.getCurrentLocationId());
        }
        pollsService.savePoll(poll);
        return poll.getId();
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
        Optional<Poll> current = pollsService.getPollByIdForReadOrEdit(id);
        if (current.isEmpty()) {
            throw new IllegalArgumentException("No poll found to update for the given reference: " + ref);
        }
        Poll poll = entity instanceof PollEntity pollEntity ? pollEntity.getPoll() : (Poll) entity;
        developerHelperService.copyBean(poll, current.get(), 0,
                new String[] { "id", "owner", "siteId", "creationDate", "options" }, true);
        pollsService.savePoll(current.get());
    }

    @Deprecated
    public Object getEntity(EntityReference ref) {
        String id = ref.getId();
        if (id == null) {
            return new Poll();
        }
        Poll poll = pollsService.getPollByIdForReadOrEdit(id)
                .orElseThrow(() -> new IllegalArgumentException("No poll found for the given reference: " + ref));

        boolean allowedRead = developerHelperService.isEntityRequestInternal(ref.toString()) || pollsService.isPollPublic(poll);
        if (!allowedRead) {
            String userReference = developerHelperService.getCurrentUserReference();
            if (userReference == null) {
                throw new EntityException("User must be logged in in order to access poll data", id, HttpServletResponse.SC_UNAUTHORIZED);
            }
            allowedRead = pollsService.isAllowedPollAdd(poll.getSiteId())
                    || developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_VOTE, "/site/" + poll.getSiteId());
            if (!allowedRead && !pollsService.isAllowedPollEdit(poll)) {
                throw new SecurityException("User (" + userReference + ") not allowed to access poll data: " + ref);
            }
        }

        // Get hydration flags
        boolean includeVotes = Boolean.TRUE.equals(requestStorage.getStoredValueAsType(Boolean.class, "includeVotes"))
                && allowedRead && pollsService.userCanViewPoll(poll, developerHelperService.getCurrentUserId());
        Boolean includeOptions = requestStorage.getStoredValueAsType(Boolean.class, "includeOptions");
        if (includeOptions == null) { includeOptions = false; }

        // Use service to create PollEntity with computed presentation fields
        // Service constructs Reference internally
        PollEntity pollEntity = pollsService.createPollEntity(poll, includeVotes, includeOptions);

        return pollEntity;
    }

    @Deprecated
    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        String id = ref.getId();
        if (id == null) {
            throw new IllegalArgumentException("The reference must include an id for deletes (id is currently null)");
        }
        Optional<Poll> poll = getPollById(id);
        if (poll.isEmpty()) {
            throw new IllegalArgumentException("No poll found for the given reference: " + ref);
        }
        try {
            pollsService.deletePoll(poll.get().getId());
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
        String perm = PERMISSION_VOTE;
        if (adminControl) {
            perm = PERMISSION_ADD;
        }
        List<Poll> polls = pollsService.findAllPollsForUserAndSitesAndPermission(userId, siteIds, perm);

        // Convert each Poll to PollEntity using service
        // Service constructs Reference internally
        boolean includeVotes = false;
        boolean includeOptions = adminControl; // Include options for admin view

        List<PollEntity> pollEntities = new java.util.ArrayList<>();
        for (Poll poll : polls) {
            // Use service to create PollEntity with computed fields
            PollEntity pollEntity = pollsService.createPollEntity(poll, includeVotes, includeOptions);
            pollEntities.add(pollEntity);
        }

        return pollEntities;
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
