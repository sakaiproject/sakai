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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;

import static org.sakaiproject.poll.api.PollConstants.PERMISSION_ADD;
import static org.sakaiproject.poll.api.PollConstants.PERMISSION_VOTE;


/**
 * Entity provider which represents poll votes
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class PollOptionEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful {

    private PollsService pollsService;
    public void setPollListManager(PollsService pollsService) {
        this.pollsService = pollsService;
    }

    public static final String PREFIX = "poll-option";
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
        Option option = getOptionById(id);
        boolean exists = (option != null);
        return exists;
    }

    @Deprecated
    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new EntityException("User must be logged in to create new options", ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
        }
        Option option = (Option) entity;
        // check minimum settings
        Poll poll = option.getPoll();
        if (poll == null || poll.getId() == null) {
            throw new IllegalArgumentException("Poll must be set to create an option");
        }
        // check minimum settings
        if (option.getText() == null) {
            throw new IllegalArgumentException("Poll Option text must be set to create an option");
        }
        checkOptionPermission(userReference, option);
        boolean saved = pollsService.saveOption(option);
        if (!saved) {
            throw new IllegalStateException("Unable to save option ("+option+") for user ("+userReference+"): " + ref);
        }
        return option.getId()+"";
    }

    @Deprecated
    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String id = ref.getId();
        if (id == null) {
            throw new IllegalArgumentException("The reference must include an id for updates (id is currently null)");
        }
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new EntityException("Anonymous user cannot update option", ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
        }
        Option current = getOptionById(id);
        if (current == null) {
            throw new IllegalArgumentException("No option found to update for the given reference: " + ref);
        }
        Option option = (Option) entity;
        checkOptionPermission(userReference, current);
        developerHelperService.copyBean(option, current, 0, new String[] {"id", "pollId", "UUId"}, true);
        boolean saved = pollsService.saveOption(current);
        if (!saved) {
            throw new IllegalStateException("Unable to update option ("+option+") for user ("+userReference+"): " + ref);
        }
    }

    @Deprecated
    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        String id = ref.getId();
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new EntityException("Anonymous user cannot delete option", ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
        }
        Option option = getOptionById(id);
        if (option == null) {
            throw new IllegalArgumentException("No option found to delete for the given reference: " + ref);
        }
        checkOptionPermission(userReference, option);
        pollsService.deleteOption(option.getId());
    }

    public Object getSampleEntity() {
        return new Option();
    }

    @Deprecated
    public Object getEntity(EntityReference ref) {
        String id = ref.getId();
        if (id == null) {
            return new Option();
        }
        String currentUser = developerHelperService.getCurrentUserReference();
        if (currentUser == null) {
            throw new EntityException("Anonymous users cannot view specific options", ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
        }
        Option option = getOptionById(id);
        if (developerHelperService.isEntityRequestInternal(ref.toString())) {
            // ok to retrieve internally
        } else {
            // need to security check
            if (developerHelperService.isUserAdmin(currentUser)) {
                // ok to view this vote
            } else {
                // not allowed to view
                throw new SecurityException("User ("+currentUser+") cannot view option ("+ref+")");
            }
        }
        return option;
    }

    @Deprecated
    public List<?> getEntities(EntityReference ref, Search search) {
        // get the pollId
        Restriction pollRes = search.getRestrictionByProperty("pollId");
        if (pollRes == null || pollRes.getSingleValue() == null) {
            throw new IllegalArgumentException("Must include a non-null pollId in order to retreive a list of votes");
        }
        String pollId = developerHelperService.convert(pollRes.getSingleValue(), String.class);
        // get the poll
        Optional<Poll> poll = pollsService.getPollById(pollId);
        if (poll.isEmpty()) {
            throw new IllegalArgumentException("pollId ("+pollId+") is invalid and does not match any known polls");
        } else {
            boolean allowedPublic = pollsService.isPollPublic(poll.get());
            if (!allowedPublic) {
                String userReference = developerHelperService.getCurrentUserReference();
                if (userReference == null) {
                    throw new EntityException("User must be logged in in order to access poll data", ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    boolean allowedManage = false;
                    boolean allowedVote = false;
                    allowedManage = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_ADD, "/site/" + poll.get().getSiteId());
                    allowedVote = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_VOTE, "/site/" + poll.get().getSiteId());
                    if ( !(allowedManage || allowedVote)) {
                        throw new SecurityException("User ("+userReference+") not allowed to access poll data: " + ref);
                    }
                }
            }
        }
        // get the options
        List<Option> options = poll.get().getOptions();
        return options;
    }

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }

    /**
     * Checks if the given user can create/update/delete options
     * @param userRef
     * @param option
     */
    @Deprecated
    private void checkOptionPermission(String userRef, Option option) {
        Poll optionPoll = option.getPoll();
        if (optionPoll == null || optionPoll.getId() == null) {
            throw new IllegalArgumentException("Poll must be set in the option to check permissions: " + option);
        }
        String pollId = optionPoll.getId();
        // validate poll exists
        Optional<Poll> poll = pollsService.getPollById(pollId);
        if (poll.isEmpty()) {
            throw new IllegalArgumentException("Invalid poll id ("+pollId+"), could not find poll from option: " + option);
        }
        // check permissions
        String siteRef = "/site/" + poll.get().getSiteId();
        if (! developerHelperService.isUserAllowedInEntityReference(userRef, PERMISSION_ADD, siteRef)) {
            throw new SecurityException("User ("+userRef+") is not allowed to create/update/delete options in this poll ("+pollId+")");
        }
    }

    /**
     * @param id
     * @return
     */
    @Deprecated
    private Option getOptionById(String id) {
        Long optionId;
        try {
            optionId = Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert id ("+id+") to long: " + e.getMessage(), e);
        }
        Optional<Option> option = pollsService.getOptionById(optionId);
        return option.orElse(null);
    }

}
