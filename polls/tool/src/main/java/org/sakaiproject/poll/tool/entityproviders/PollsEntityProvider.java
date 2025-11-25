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

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.poll.api.entity.PollEntity;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.user.api.UserDirectoryService;

import static org.sakaiproject.poll.api.PollConstants.*;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles the polls tool.
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 * @author Denny (denny.denny @ gmail.com)
 */
@Slf4j
public class PollsEntityProvider extends AbstractEntityProvider implements
		EntityProvider, AutoRegisterEntityProvider, RequestStorable,
		ActionsExecutable, Outputable, Describeable {

    public static final String ENTITY_PREFIX = "polls";

	@Setter private PollsService pollsService;
	@Setter private UsageSessionService usageSessionService;
	@Setter private UserDirectoryService userDirectoryService;
	@Setter private RequestStorage requestStorage = null;

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.XML, Formats.JSON, Formats.HTML };
	}

	/**
	 * site/siteId
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<PollEntity> getPollsForSite(EntityView view) {
		// get siteId
		String siteId = view.getPathSegment(2);
		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the polls for a site, via the URL /polls/site/siteId");
		}

		String[] siteIds = new String[] { siteId };

		if (log.isDebugEnabled()) {
			log.debug("poll for site {}", siteId);
		}


		String userId = developerHelperService.getCurrentUserId();
		if (userId == null) {
			throw new EntityException(
					"No user is currently logged in so no polls data can be retrieved",
					siteId, HttpServletResponse.SC_UNAUTHORIZED);
		}

		boolean adminControl = false;

		String perm = PERMISSION_VOTE;
		if (adminControl) {
			perm = PERMISSION_ADD;
		}
		List<Poll> polls = pollsService
				.findAllPollsForUserAndSitesAndPermission(userId, siteIds, perm);

		// Convert each Poll to PollEntity using service
		// Service constructs Reference internally
		boolean includeVotes = false;
		boolean includeOptions = adminControl;

		List<PollEntity> pollEntities = new ArrayList<>();
		for (Poll poll : polls) {
			// Use service to create PollEntity with computed fields
			PollEntity pollEntity = pollsService.createPollEntity(poll, includeVotes, includeOptions);
			pollEntities.add(pollEntity);
		}
		return pollEntities;
	}

	@EntityCustomAction(action = "my", viewKey = EntityView.VIEW_LIST)
	public List<?> getEntities(EntityReference ref, Search search) {
		log.info("get entities");
		// get the setting which indicates if we are getting polls we can admin
		// or polls we can take
		boolean adminControl = false;
		Restriction adminRes = search.getRestrictionByProperty("admin");
		if (adminRes != null) {
			adminControl = developerHelperService.convert(
					adminRes.getSingleValue(), boolean.class);
		}
		// get the location (if set)
		Restriction locRes = search
				.getRestrictionByProperty(CollectionResolvable.SEARCH_LOCATION_REFERENCE); // requestStorage.getStoredValueAsType(String.class,
																							// "siteId");
		String[] siteIds = null;
		if (locRes != null) {
			String siteId = developerHelperService.getLocationIdFromRef(locRes
					.getStringValue());
			siteIds = new String[] { siteId };
		}
		// get the user (if set)
		Restriction userRes = search
				.getRestrictionByProperty(CollectionResolvable.SEARCH_USER_REFERENCE);
		String userId = null;
		if (userRes != null) {
			String currentUser = developerHelperService
					.getCurrentUserReference();
			String userReference = userRes.getStringValue();
			if (userReference == null) {
				throw new IllegalArgumentException(
						"Invalid request: Cannot limit polls by user when the value is null");
			}
			if (userReference.equals(currentUser)
					|| developerHelperService.isUserAdmin(currentUser)) {
				userId = developerHelperService.getUserIdFromRef(userReference); // requestStorage.getStoredValueAsType(String.class,
																					// "userId");
			} else {
				throw new SecurityException(
						"Only the admin can get polls for other users, you requested polls for: "
								+ userReference);
			}
		} else {
			userId = developerHelperService.getCurrentUserId();
			if (userId == null) {
				throw new EntityException(
						"No user is currently logged in so no polls data can be retrieved",
						ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
			}
		}
		String perm = PERMISSION_VOTE;
		if (adminControl) {
			perm = PERMISSION_ADD;
		}
		List<Poll> polls = pollsService
				.findAllPollsForUserAndSitesAndPermission(userId, siteIds, perm);

		// Convert each Poll to PollEntity using service
		// Service constructs Reference internally
		boolean includeVotes = false;
		boolean includeOptions = adminControl;

		List<PollEntity> pollEntities = new ArrayList<>();
		for (Poll poll : polls) {
			// Use service to create PollEntity with computed fields
			PollEntity pollEntity = pollsService.createPollEntity(poll, includeVotes, includeOptions);
			pollEntities.add(pollEntity);
		}
		return pollEntities;
	}

	/**
	 * @param id
	 * @return
	 */
	private Poll getPollById(String id) {
		return pollsService.getPollById(id).orElse(null);
	}

	@EntityCustomAction(action = "poll-view", viewKey = EntityView.VIEW_SHOW)
	public PollEntity getPollEntity(EntityView view, EntityReference ref) {
		String id = ref.getId();
		log.debug("poll id: {}", id);

		if (StringUtils.isBlank(id)) {
			log.warn("Poll id is not exist. Returning an empty poll object.");
			// Return a PollEntity with an empty Poll
			Poll emptyPoll = new Poll();
			// Service will construct Reference internally
			return pollsService.createPollEntity(emptyPoll, false, false);
		}
		Poll poll = getPollById(id);
		if (poll == null) {
			throw new IllegalArgumentException(
					"No poll found for the given reference: " + id);
		}

		// Security checks
		if (!developerHelperService.isEntityRequestInternal(id + "")) {
			if (!pollsService.isPollPublic(poll)) {
				// this is not a public poll? (ie .anon role has poll.vote)
				String userReference = developerHelperService
						.getCurrentUserReference();
				if (userReference == null) {
					throw new EntityException(
							"User must be logged in in order to access poll data",
							id, HttpServletResponse.SC_UNAUTHORIZED);
				}
				boolean allowedManage = developerHelperService
						.isUserAllowedInEntityReference(userReference,
								PERMISSION_ADD,
								"/site/" + poll.getSiteId());
				boolean allowedVote = developerHelperService
						.isUserAllowedInEntityReference(userReference,
								PERMISSION_VOTE, "/site/"
										+ poll.getSiteId());
				if (!allowedManage && !allowedVote) {
					throw new SecurityException("User (" + userReference
							+ ") not allowed to access poll data: " + id);
				}
			}
		}

		// Get hydration flags
		log.debug("requestStorage: {}", requestStorage);
		Boolean includeVotes = requestStorage.getStoredValueAsType(
				Boolean.class, "includeVotes");
		if (includeVotes == null) {
			includeVotes = false;
		}
		Boolean includeOptions = requestStorage.getStoredValueAsType(
				Boolean.class, "includeOptions");
		if (includeOptions == null) {
			includeOptions = false;
		}

		// Use service to create PollEntity with computed presentation fields
		// Service constructs Reference internally
		PollEntity pollEntity = pollsService.createPollEntity(poll, includeVotes, includeOptions);

		return pollEntity;
	}

	/**
	 * Note that details is the only optional field
	 */
	@EntityCustomAction(action = "poll-create", viewKey = EntityView.VIEW_NEW)
	public String createPollEntity(EntityReference ref, Map<String, Object> params) {
		Poll poll = new Poll();
		// copy from params to Poll
		copyParamsToObject(params, poll);

		poll.setCreationDate(Instant.now());
		if (poll.getOwner() == null) {
			poll.setOwner(developerHelperService.getCurrentUserId());
		}
		String siteId = developerHelperService.getCurrentLocationId();
		if (poll.getSiteId() == null) {
			poll.setSiteId(siteId);
		} else {
			siteId = poll.getSiteId();
		}
		String userReference = developerHelperService.getCurrentUserReference();
		String location = "/site/" + siteId;
		boolean allowed = developerHelperService
				.isUserAllowedInEntityReference(userReference,
						PERMISSION_ADD, location);
		if (!allowed) {
			throw new SecurityException("Current user (" + userReference
					+ ") cannot create polls in location (" + location + ")");
		}
		pollsService.savePoll(poll);
		return poll.getId();
	}

	@EntityCustomAction(action = "poll-update", viewKey = EntityView.VIEW_EDIT)
	public void updatePollEntity(EntityReference ref, Map<String, Object> params) {
		String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException(
					"The reference must include an id for updates (id is currently null)");
		}
		String userReference = developerHelperService.getCurrentUserReference();
		if (userReference == null) {
			throw new SecurityException("anonymous user cannot update poll: "
					+ ref);
		}
		Poll current = getPollById(id);
		if (current == null) {
			throw new IllegalArgumentException(
					"No poll found to update for the given reference: " + ref);
		}
		Poll poll = new Poll();
		copyParamsToObject(params, poll);
		String siteId = developerHelperService.getCurrentLocationId();
		if (poll.getSiteId() == null) {
			poll.setSiteId(siteId);
		} else {
			siteId = poll.getSiteId();
		}
		String location = "/site/" + siteId;
		// should this check a different permission?
		boolean allowed = developerHelperService
				.isUserAllowedInEntityReference(userReference,
						PERMISSION_ADD, location);
		if (!allowed) {
			throw new SecurityException("Current user (" + userReference
					+ ") cannot update polls in location (" + location + ")");
		}
		developerHelperService.copyBean(poll, current, 0, new String[] { "uuid",
				"pollId", "owner", "siteId", "creationDate", "reference",
				"url", "properties" }, true);
		pollsService.savePoll(current);
	}

	@EntityCustomAction(action = "poll-delete", viewKey = EntityView.VIEW_SHOW)
	public Object deletePollEntity(EntityReference ref) {
		String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException(
					"The reference must include an id for deletes (id is currently null)");
		}
		Poll poll = getPollById(id);
		if (poll == null) {
			throw new IllegalArgumentException(
					"No poll found for the given reference: " + ref);
		}
		try {
			pollsService.deletePoll(poll.getId());
			return String.format("Poll id %s removed", id);
		} catch (SecurityException e) {
			throw new SecurityException("The current user ("
					+ developerHelperService.getCurrentUserReference()
					+ ") is not allowed to delete this poll: " + ref);
		}
	}

	/**
	 * /{pollId}/poll-options
	 */
	@EntityCustomAction(action = "poll-option-list", viewKey = EntityView.VIEW_SHOW)
	public List<?> getPollOptionList(EntityView view, EntityReference ref) {
		// get the pollId
		String id = ref.getId();
		log.debug(id);
		// check siteId supplied
		if (StringUtils.isBlank(id)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the polls for a site, via the URL /polls/site/siteId");
		}

		String pollId = id;
		// get the poll
		Optional<Poll> poll = pollsService.getPollById(pollId);
		if (poll.isEmpty()) {
			throw new IllegalArgumentException("pollId (" + pollId
					+ ") is invalid and does not match any known polls");
		} else {
			boolean allowedPublic = pollsService.isPollPublic(poll.get());
			if (!allowedPublic) {
				String userReference = developerHelperService
						.getCurrentUserReference();
				if (userReference == null) {
					throw new EntityException(
							"User must be logged in in order to access poll data",
							id, HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					boolean allowedManage = false;
					boolean allowedVote = false;
					allowedManage = developerHelperService
							.isUserAllowedInEntityReference(userReference,
									PERMISSION_ADD, "/site/"
											+ poll.get().getSiteId());
					allowedVote = developerHelperService
							.isUserAllowedInEntityReference(userReference,
									PERMISSION_VOTE, "/site/"
											+ poll.get().getSiteId());
					if (!(allowedManage || allowedVote)) {
						throw new SecurityException("User (" + userReference
								+ ") not allowed to access poll data: " + id);
					}
				}
			}
		}
		// get the options
		List<Option> options = poll.get().getOptions();
		return options;
	}

	@EntityCustomAction(action = "option-view", viewKey = EntityView.VIEW_SHOW)
	public Object getOptionEntity(EntityReference ref) {
		String id = ref.getId();
		if (id == null) {
			return new Option();
		}
		String currentUser = developerHelperService.getCurrentUserReference();
		if (currentUser == null) {
			throw new EntityException(
					"Anonymous users cannot view specific options",
					ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
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
				throw new SecurityException("User (" + currentUser
						+ ") cannot view option (" + ref + ")");
			}
		}
		return option;
	}

	@EntityCustomAction(action = "option-create", viewKey = EntityView.VIEW_NEW)
	public String createOptionEntity(EntityReference ref,
			Map<String, Object> params) {
		String userReference = developerHelperService.getCurrentUserReference();
		if (userReference == null) {
			throw new EntityException(
					"User must be logged in to create new options",
					ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
		}
		Option option = new Option();
		// copy from params to Option
		copyParamsToObject(params, option);

		// check minimum settings - pollId needs to be extracted from params
		String pollId = (String) params.get("pollId");
		if (pollId == null) {
			throw new IllegalArgumentException(
					"Poll ID must be set to create an option");
		}
		Optional<Poll> poll = pollsService.getPollById(pollId);
		if (poll.isEmpty()) {
			throw new IllegalArgumentException("Poll not found with ID: " + pollId);
		}
		// check minimum settings
		if (option.getText() == null) {
			throw new IllegalArgumentException(
					"Poll Option text must be set to create an option");
		}
		checkOptionPermission(userReference, option);

		Poll saved = pollsService.saveNewOption(poll.get(), option);
		if (saved == null) {
			throw new IllegalStateException("Unable to save option (" + option
					+ ") for user (" + userReference + "): " + ref);
		}
		return option.getId().toString();
	}

	/**
	 * Helper to copy from map of parameters to object.
	 * 
	 * @param params
	 *            source
	 * @param object
	 *            destination
	 */
	private void copyParamsToObject(Map<String, Object> params, Object object) {
		Class<?> c = object.getClass();
		Method[] methods = c.getDeclaredMethods();
		for (Method m : methods) {
			String name = m.getName();
			Class<?>[] types = m.getParameterTypes();
			if (name.startsWith("set") && (types.length == 1)) {
				String key = Character.toLowerCase(name.charAt(3))
						+ name.substring(4);
				Object value = params.get(key);
				if (value != null) {
					if (types[0].equals(Instant.class)) {
						Instant instantValue = Instant.ofEpochMilli(
								Long.valueOf(value.toString()));
						try {
							m.invoke(object, new Object[] { instantValue });
						} catch (IllegalAccessException e) {
							log.debug(e.getMessage(), e);
						} catch (IllegalArgumentException e) {
							log.debug(e.getMessage(), e);
						} catch (InvocationTargetException e) {
							log.debug(e.getMessage(), e);
						}
					} else {
						// use generic converter from BeanUtils
						try {
							BeanUtils.copyProperty(object, key, value);
						} catch (IllegalAccessException e) {
							log.debug(e.getMessage(), e);
						} catch (InvocationTargetException e) {
							log.debug(e.getMessage(), e);
						}
					}
				}
			}
		}
	}

	@EntityCustomAction(action = "option-update", viewKey = EntityView.VIEW_EDIT)
	public void updateOptionEntity(EntityReference ref,
			Map<String, Object> params) {
		String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException(
					"The reference must include an id for updates (id is currently null)");
		}
		String userReference = developerHelperService.getCurrentUserReference();
		if (userReference == null) {
			throw new EntityException("Anonymous user cannot update option",
					ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
		}
		Option current = getOptionById(id);
		if (current == null) {
			throw new IllegalArgumentException(
					"No option found to update for the given reference: " + ref);
		}
		Option option = new Option();
		// copy from params to Option
		copyParamsToObject(params, option);

		checkOptionPermission(userReference, current);
		developerHelperService.copyBean(option, current, 0, new String[] {"id", "poll" }, true);
		Poll saved = pollsService.savePoll(current.getPoll());
		if (saved == null) {
			throw new IllegalStateException("Unable to update option (" + option + ") for user (" + userReference + "): " + ref);
		}
	}

	@EntityCustomAction(action = "option-delete", viewKey = EntityView.VIEW_SHOW)
	public void deleteOptionEntity(EntityReference ref,
			Map<String, Object> params) {
		String id = ref.getId();
		String userReference = developerHelperService.getCurrentUserReference();
		if (userReference == null) {
			throw new EntityException("Anonymous user cannot delete option",
					ref.getId(), HttpServletResponse.SC_UNAUTHORIZED);
		}
		Option option = getOptionById(id);
		if (option == null) {
			throw new IllegalArgumentException(
					"No option found to delete for the given reference: " + ref);
		}
		checkOptionPermission(userReference, option);
		pollsService.deleteOption(option.getId());
		// return String.format("Poll option id %d removed", id);
	}

	/**
	 * Checks if the given user can create/update/delete options
	 * 
	 * @param userRef
	 * @param option
	 */
	private void checkOptionPermission(String userRef, Option option) {
		Poll optionPoll = option.getPoll();
		if (optionPoll == null || optionPoll.getId() == null) {
			throw new IllegalArgumentException(
					"Poll must be set in the option to check permissions: "
							+ option);
		}
		String pollId = optionPoll.getId();
		// validate poll exists
		Optional<Poll> poll = pollsService.getPollById(pollId);
		if (poll.isEmpty()) {
			throw new IllegalArgumentException("Invalid poll id (" + pollId
					+ "), could not find poll from option: " + option);
		}
		// check permissions
		String siteRef = "/site/" + poll.get().getSiteId();
		if (!developerHelperService.isUserAllowedInEntityReference(userRef,
				PERMISSION_ADD, siteRef)) {
			throw new SecurityException(
					"User ("
							+ userRef
							+ ") is not allowed to create/update/delete options in this poll ("
							+ pollId + ")");
		}
	}

	/**
	 * @param id
	 * @return
	 */
	private Option getOptionById(String id) {
		Long optionId;
		try {
			optionId = Long.valueOf(id);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Cannot convert id (" + id
					+ ") to long: " + e.getMessage(), e);
		}
        return pollsService.getOptionById(optionId).orElse(null);
	}

	@EntityCustomAction(action = "vote-create", viewKey = EntityView.VIEW_NEW)
	public String createVoteEntity(EntityReference ref, Map<String, Object> params) {
		String userId = userDirectoryService.getCurrentUser().getId();
		Vote vote = new Vote();
		copyParamsToObject(params, vote);

		log.debug("got vote: {}", vote);

		String pollId = (String) params.get("pollId");
		if (pollId == null) {
			throw new IllegalArgumentException(
					"Poll Id must be set to create a vote");
		}

		Long optionId = null;
		try {
			optionId = Long.valueOf((String) params.get("pollOption"));
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}

		if (optionId == null) {
			throw new IllegalArgumentException(
					"Poll Option must be set to create a vote");
		}
		if (!pollsService.isUserAllowedVote(userId, pollId, false)) {
			throw new SecurityException("User (" + userId
					+ ") is not allowed to vote in this poll (" + pollId + ")");
		}

		// validate option
		Optional<Option> option = pollsService.getOptionById(optionId);

        if (option.isEmpty()) {
			throw new IllegalArgumentException("Invalid poll option ("
					+ optionId + ") [cannot find option] in vote ("
					+ vote + ") for user (" + userId + ")");
		} else {
            vote.setOption(option.get());
			Poll optionPoll = option.get().getPoll();
			if (optionPoll == null || !pollId.equals(optionPoll.getId())) {
				throw new IllegalArgumentException("Invalid poll option ("
						+ optionId + ") [not in poll (" + pollId
						+ ")] in vote (" + vote + ") for user (" + userId + ")");
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
			vote.setIp(usageSession.getIpAddress());
		}
		Vote saved = pollsService.saveVote(vote);
		if (saved == null) {
			throw new IllegalStateException("Unable to save vote (" + vote + ") for user (" + userId + "): " + ref);
		}
		return vote.getId().toString();
	}

	@EntityCustomAction(action = "vote-view", viewKey = EntityView.VIEW_SHOW)
	public Object getVoteEntity(EntityReference ref) {
		String id = ref.getId();
		String currentUser = developerHelperService.getCurrentUserReference();
		log.debug("current user is: {}", currentUser);
		if (currentUser == null || currentUser.length() == 0) {
			throw new EntityException(
					"Anonymous users cannot view specific votes", ref.getId(),
					HttpServletResponse.SC_UNAUTHORIZED);
		}

		// is this a new object?
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
		} else if (developerHelperService.isEntityRequestInternal(ref
				.toString())) {
			// ok for all internal requests
		} else {
			// TODO - check vote location and perm?
			// not allowed to view
			throw new SecurityException("User (" + currentUser
					+ ") cannot view vote (" + ref + ")");
		}

		if (id == null) {
			return new Vote();
		}

		return vote;
	}

	@EntityCustomAction(action = "vote-list", viewKey = EntityView.VIEW_LIST)
	public List<?> getVoteEntities(EntityReference ref, Search search) {
		String currentUserId = userDirectoryService.getCurrentUser().getId();

		Restriction pollRes = search.getRestrictionByProperty("pollId");

		if (pollRes == null || pollRes.getSingleValue() == null) {
			// throw new
			// IllegalArgumentException("Must include a non-null pollId in order to retreive a list of votes");
			return null;
		}
		String pollId = null;
		boolean viewVoters = false;
		if (developerHelperService.isUserAdmin(developerHelperService
				.getCurrentUserReference())) {
			viewVoters = true;
		}
		try {
			pollId = developerHelperService.convert(pollRes.getSingleValue(),
					String.class);
		} catch (UnsupportedOperationException e) {
			throw new IllegalArgumentException(
					"Invalid: pollId must be a string: " + e.getMessage(),
					e);
		}
		Optional<Poll> poll = pollsService.getPollById(pollId);
		if (poll.isEmpty()) {
			throw new IllegalArgumentException("pollId (" + pollId
					+ ") is invalid and does not match any known polls");
		}
		List<Vote> votes = pollsService.getAllVotesForPoll(poll.get().getId());

		if (developerHelperService.isEntityRequestInternal(ref.toString())) {
			// ok for all internal requests
		} else if (!pollsService.isAllowedViewResults(poll.get(), currentUserId)) {
			// TODO - check vote location and perm?
			// not allowed to view
			throw new SecurityException("User (" + currentUserId
					+ ") cannot view vote (" + ref + ")");
		}
		if (viewVoters) {
			return votes;
		} else {
			return anonymizeVotes(votes);
		}
	}

    // TODO needs redoing
	private List<?> anonymizeVotes(List<Vote> votes) {
		List<Vote> ret = new ArrayList<>();
		String userId = userDirectoryService.getCurrentUser().getId();

		for (int i = 0; i < votes.size(); i++) {
			Vote vote = votes.get(i);
			if (!userId.equals(vote.getUserId())) {
				Vote newVote = new Vote();
//				newVote.setPollId(vote.getPollId());
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
	@EntityCustomAction(action = "vote", viewKey = EntityView.VIEW_NEW)
	public void vote(EntityView view, EntityReference ref, String prefix,
			Search search, OutputStream out, Map<String, Object> params) {
		String pollId = (String) params.get("pollId");
		if (pollId == null) {
			throw new IllegalArgumentException("No pollId found.");
		}
		String userId = userDirectoryService.getCurrentUser().getId();
		Optional<Poll> poll = pollsService.getPollById(pollId);
		if (poll.isEmpty()) {
			throw new IllegalArgumentException(
					"No poll found to update for the given reference: " + ref);
		}
		if (!pollsService.isUserAllowedVote(userId, poll.get().getId(), false)) {
			throw new SecurityException("User (" + userId
					+ ") is not allowed to vote in this poll ("
					+ poll.get().getId() + ")");
		}

		Set<String> optionIds = new HashSet<>();
		Object param = params.get("pollOption");
		if (param == null) {
			throw new IllegalArgumentException(
					"At least one pollOption parameter must be provided to vote.");
		} else if (param instanceof String) {
			optionIds.add((String) param);
		} else if (param instanceof Iterable<?>) {
			for (Object o : (Iterable<?>) param)
				if (o instanceof String)
					optionIds.add((String) o);
				else
					throw new IllegalArgumentException(
							"Each pollOption must be a String, not "
									+ o.getClass().getName());
		} else if (param instanceof Object[]) {
			for (Object o : (Object[]) param)
				if (o instanceof String)
					optionIds.add((String) o);
				else
					throw new IllegalArgumentException(
							"Each pollOption must be a String, not "
									+ o.getClass().getName());
		} else
			throw new IllegalArgumentException(
					"pollOption must be String, String[] or List<String>, not "
							+ param.getClass().getName());

		// Turn each option String into an Option, making sure that each is a
		// valid choice for the poll. We use a Map to make sure one cannot vote
		// more than once for any option by specifying it using equivalent
		// representations
		Map<Long, Option> options = new HashMap<>();
		for (String optionId : optionIds) {
			try {
				Optional<Option> option = pollsService.getOptionById(Long.parseLong(optionId));
				Poll optionPoll = option.get().getPoll();
				if (optionPoll == null || !poll.get().getId().equals(optionPoll.getId()))
					throw new Exception();
				options.put(option.get().getId(), option.get());
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid pollOption: "
						+ optionId);
			}
		}

		// Validate that the number of options voted for is within acceptable
		// bounds.
		if (options.size() < poll.get().getMinOptions())
			throw new IllegalArgumentException("You must provide at least "
					+ poll.get().getMinOptions() + " options, not " + options.size()
					+ ".");
		if (options.size() > poll.get().getMaxOptions())
			throw new IllegalArgumentException("You may provide at most "
					+ poll.get().getMaxOptions() + " options, not " + options.size()
					+ ".");

		// Create and save the Vote objects.
		UsageSession usageSession = usageSessionService.getSession();
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
				throw new IllegalStateException("Unable to save vote (" + vote
						+ ") for user (" + userId + "): " + ref);
			}
		}
	}

	/**
	 * @param id
	 * @return
	 */
	private Optional<Vote> getVoteById(String id) {
		Long voteId;
		try {
			voteId = Long.valueOf(id);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Cannot convert id (" + id
					+ ") to long: " + e.getMessage(), e);
		}
		return pollsService.getVoteById(voteId);
	}

}
