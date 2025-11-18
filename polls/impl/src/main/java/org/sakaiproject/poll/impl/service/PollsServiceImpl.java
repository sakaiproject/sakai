/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.poll.impl.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.poll.api.entity.PollEntity;
import org.sakaiproject.poll.api.logic.ExternalLogic;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.repository.PollRepository;
import org.sakaiproject.poll.api.repository.VoteRepository;
import org.sakaiproject.poll.api.util.PollUtil;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.MergeConfig;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.sakaiproject.poll.api.PollConstants.*;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class PollsServiceImpl implements PollsService, EntityProducer, EntityTransferrer {

    private static final String DEFAULT_IP_ADDRESS = "Nothing";

    @Setter private EntityManager entityManager;
    @Setter private PollRepository pollRepository;
    @Setter private VoteRepository voteRepository;
    @Setter private ExternalLogic externalLogic;
    @Setter private LTIService ltiService;
    @Setter private LinkMigrationHelper linkMigrationHelper;
    @Setter private UsageSessionService usageSessionService;

    public void init() {
        entityManager.registerEntityProducer(this, REFERENCE_ROOT);
        externalLogic.registerFunction(PERMISSION_VOTE, true);
        externalLogic.registerFunction(PERMISSION_ADD, true);
        externalLogic.registerFunction(PERMISSION_DELETE_OWN, true);
        externalLogic.registerFunction(PERMISSION_DELETE_ANY, true);
        externalLogic.registerFunction(PERMISSION_EDIT_ANY, true);
        externalLogic.registerFunction(PERMISSION_EDIT_OWN, true);
    }

    public List<Poll> findAllPollsForUserAndSitesAndPermission(String userId, String[] siteIds, String permissionConstant) {
        if (userId == null || permissionConstant == null) {
            throw new IllegalArgumentException("userId and permissionConstant must be set");
        }
        List<Poll> polls;
        // get all allowed sites for this user
        List<String> allowedSites = externalLogic.getSitesForUser(userId, permissionConstant);
        if (allowedSites.isEmpty()) {
                // no sites to search so EXIT here
            return new ArrayList<>();
        } else {
            if (siteIds != null && siteIds.length > 0) {
                List<String> requestedSiteIds = Arrays.asList(siteIds);
                // filter down to just the requested ones
                allowedSites.retainAll(requestedSiteIds);
            }
            Date now = new Date();
            if (PERMISSION_VOTE.equals(permissionConstant)) {
                polls = pollRepository.findOpenPollsBySiteIds(allowedSites, now);
            } else {
                polls = pollRepository.findBySiteIdsOrderByCreationDate(allowedSites);
            }
        }
        if (polls == null) {
            polls = new ArrayList<>();
        }
        return polls;
    }

    @Override
    public Poll savePoll(final Poll poll) throws SecurityException, IllegalArgumentException {
        if (poll == null
                || StringUtils.isAnyBlank(poll.getText(), poll.getSiteId(), poll.getVoteOpen().toString(), poll.getVoteClose().toString())) {
            throw new IllegalArgumentException("you must supply a question, siteId & open and close dates");
        }
        String userRef = externalLogic.getCurrentuserReference();
        String siteRef = externalLogic.getSiteRefFromId(poll.getSiteId());
        if (!externalLogic.isAllowedInLocation(PERMISSION_ADD, siteRef, userRef)) {
            throw new SecurityException("user:" + userRef + " can't add poll to site: " + siteRef);
        }

        if (poll.getCreationDate() == null) poll.setCreationDate(new Date());
        if (StringUtils.isBlank(poll.getOwner())) poll.setOwner(externalLogic.getCurrentUserId());

        boolean isNew = poll.getId() == null;
        Poll savedPoll = pollRepository.save(poll);

        log.debug("Poll {} successfully saved", poll);
        externalLogic.registerStatement(savedPoll.getText(), isNew, savedPoll.getId());

        return savedPoll;
    }

    @Override
    public void deletePoll(final Poll poll) throws SecurityException, IllegalArgumentException {
        if (poll == null || poll.getId() == null) {
            throw new IllegalArgumentException("The poll or its id can't be null");
        }

        if (!pollCanDelete(poll)) {
            throw new SecurityException("user: " + externalLogic.getCurrentuserReference() + " can't delete poll: " + poll.getId());
        }

        // Delete poll - cascade will automatically delete options and votes
        voteRepository.findByPollId(poll.getId()).forEach(voteRepository::delete);
        pollRepository.delete(poll);

        log.debug("Poll id {} deleted", poll.getId());
        externalLogic.postEvent("poll.delete", "poll/site/" + poll.getSiteId() + "/poll/" + poll.getId(), true);
    }

    @Override
    public boolean userCanDeletePoll(final Poll poll) {
        String locationRef = externalLogic.getCurrentLocationReference();
        return externalLogic.isAllowedInLocation(PERMISSION_DELETE_ANY, locationRef)
            || (externalLogic.isAllowedInLocation(PERMISSION_DELETE_OWN, locationRef)
                && poll.getOwner().equals(externalLogic.getCurrentUserId()));
    }

    public List<Poll> findAllPolls() {
        return new ArrayList<>(pollRepository.findAll());
    }

    public List<Poll> findAllPolls(final String siteId) {
        return new ArrayList<>(pollRepository.findBySiteIdOrderByCreationDateDesc(siteId));
    }

    public Optional<Poll> getPollById(final String pollId) throws SecurityException {
        // User needs at least site visit to read a poll
        Optional<Poll> poll = pollRepository.findById(pollId);

        if (poll.isPresent()) {
            if (!externalLogic.isAllowedInLocation("site.visit", externalLogic.getSiteRefFromId(poll.get().getSiteId()), externalLogic.getCurrentuserReference())) {
                throw new SecurityException("user:" + externalLogic.getCurrentuserReference() + " can't read poll " + pollId);
            }
        }
        return poll;
    }

    @Transactional(readOnly = true)
    public List<Option> getOptionsForPoll(final Poll poll) {
        if (poll == null) {
            throw new IllegalArgumentException("Poll cannot be null when retrieving options");
        }
        return getOptionsForPoll(poll.getId());
    }

    @Transactional(readOnly = true)
    public List<Option> getOptionsForPoll(final String pollId) {
        if (pollId == null) {
            throw new IllegalArgumentException("Poll id cannot be null when retrieving options");
        }
        return pollRepository.findOptionsByPollId(pollId);
    }

    public List<Option> getVisibleOptionsForPoll(final String pollId) {
        return getOptionsForPoll(pollId).stream().filter(Predicate.not(Option::getDeleted)).toList();
    }

    @Transactional(readOnly = true)
    public Poll getPollWithVotes(final String pollId) {
        return pollRepository.findById(pollId).orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Option> getOptionById(final Long optionId) {
        if (optionId == null) return Optional.empty();
        return pollRepository.findOptionByOptionId(optionId);
    }

    @Override
    public void deleteOption(final Long optionId) {
        deleteOption(optionId, false);
    }

    @Transactional
    public void deleteOption(final Long optionId, boolean soft) {
        if (optionId == null) {
            throw new IllegalArgumentException("Option ID cannot be null");
        }

        Option option = getOptionById(optionId)
            .orElseThrow(() -> new IllegalArgumentException("Option with id " + optionId + " not found"));

        Poll poll = option.getPoll();
        if (poll == null) {
            throw new IllegalStateException("Option is not associated with a poll");
        }

        if (soft) {
            option.setDeleted(true);
            log.debug("Option id {} soft deleted", option.getId());
        } else {
            poll.removeOption(option);
            log.debug("Option id {} deleted", option.getId());
        }

        pollRepository.save(poll);
    }

    public boolean saveOption(final Option option) {
        if (option == null) {
            throw new IllegalArgumentException("Option cannot be null when saving");
        }

        // Save through poll aggregate
        Poll poll = option.getPoll();
        if (poll != null) {
            pollRepository.save(poll);
            log.debug("Option {} successfully saved", option);
            return true;
        }

        log.warn("Cannot save option {} without associated poll", option.getId());
        return false;
    }

    private boolean pollCanDelete(final Poll poll) {
        String siteRef = externalLogic.getSiteRefFromId(poll.getSiteId());
        return isSiteOwner(poll.getSiteId())
            || externalLogic.isAllowedInLocation(PERMISSION_DELETE_ANY, siteRef)
            || (externalLogic.isAllowedInLocation(PERMISSION_DELETE_OWN, siteRef)
                && poll.getOwner().equals(externalLogic.getCurrentUserId()));
    }

    private boolean isSiteOwner(final String siteId) {
        return externalLogic.isAllowedInLocation("site.upd", externalLogic.getSiteRefFromId(siteId));
    }

    public String getLabel() {
        return "poll";
    }

    public boolean willArchiveMerge() {
        return true;
    }

    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments) {
        log.debug("archive: poll {}", siteId);
        // prepare the buffer for the results log
        StringBuilder results = new StringBuilder();

        // String assignRef = assignmentReference(siteId, SiteService.MAIN_CONTAINER);
        results.append("archiving " + getLabel() + " context " + Entity.SEPARATOR
                       + siteId + Entity.SEPARATOR + SiteService.MAIN_CONTAINER
                       + ".\n");

        // start with an element with our very own (service) name
        Element element = doc.createElement(PollsService.class.getName());
        stack.peek().appendChild(element);
        stack.push(element);

        List<Poll> pollsList = findAllPolls(siteId);
        log.debug("got list of {} polls", pollsList.size());
        for (Poll poll : pollsList) {
            try {
                log.debug("got poll {}", poll.getId());

                // archive this poll
                Element el = poll.toXml(doc, stack);

                // since we aren't archiving votes too, don't worry about archiving the
                // soft-deleted options -- only "visible".
                List<Option> options = poll.getOptions().stream().filter(Predicate.not(Option::getDeleted)).toList();

                for (Option option : options) {
                    Element el2 = PollUtil.optionToXml(option, doc, stack);
                    el.appendChild(el2);
                }

                element.appendChild(el);
            } catch (Exception e) {
                log.error("Failed to archive {} in site {}", poll.getId(), siteId, e);
            }
        }
        stack.pop();
        return results.toString();
    }

    public String merge(String siteId, Element root, String archivePath, String fromSiteId, MergeConfig mcx) {

        log.debug("merge archiveContext={} archiveServerUrl={}", mcx.archiveContext, mcx.archiveServerUrl);

        List<Poll> pollsList = findAllPolls(siteId);
        Set<String> pollTexts = pollsList.stream().map(Poll::getText).collect(Collectors.toCollection(LinkedHashSet::new));

        // Add polls not already in the site
        NodeList polls = root.getElementsByTagName("poll");
        for (int i=0; i<polls.getLength(); ++i) {
            Element pollElement = (Element) polls.item(i);
            Poll poll = Poll.fromXML(pollElement);

            String pollText = poll.getText();
            if ( pollText == null ) continue;
            if ( pollTexts.contains(pollText) ) continue;

            poll.setSiteId(siteId);
            poll.setOwner(mcx.creatorId);
            String description = poll.getDescription();
            description = ltiService.fixLtiLaunchUrls(description, siteId, mcx);
            description = linkMigrationHelper.migrateLinksInMergedRTE(siteId, mcx, description);
            poll.setDescription(description);

            NodeList options = pollElement.getElementsByTagName("option");
            for (int j=0; j<options.getLength(); ++j) {
                Element optionElement = (Element) options.item(j);
                Option option = PollUtil.xmlToOption(optionElement);
                option.setId(null);  // To force insert
                String text = option.getText();
                text = ltiService.fixLtiLaunchUrls(text, siteId, mcx);
                text = linkMigrationHelper.migrateLinksInMergedRTE(siteId, mcx, text);
                option.setText(text);
                // Use poll aggregate pattern - addOption sets bidirectional relationship
                poll.addOption(option);
            }
            // Save poll once with all options added
            savePoll(poll);
        }
        return null;
    }

    public boolean parseEntityReference(final String reference, final Reference ref) {
        if (reference == null || !reference.startsWith(REFERENCE_ROOT)) {
            return false;
        }

        String subType = "";
        String context = null;
        String id = null;
        String container = "";

        String remainder = Strings.CI.removeStart(reference, REFERENCE_ROOT);
        remainder = Strings.CI.removeStart(remainder, Entity.SEPARATOR);

        if (StringUtils.isNotEmpty(remainder)) {
            String[] segments = StringUtils.split(remainder, Entity.SEPARATOR);
            if (segments.length > 0) {
                context = segments[0];
                if (segments.length > 1) {
                    id = segments[1];
                }
            }
        }

        ref.set(APPLICATION_ID, subType, id, container, context);

        return true;
    }

    public Entity getEntity(final Reference ref) {
        if (APPLICATION_ID.equals(ref.getType())) {
            return getPollById(ref.getId())
                .map(p -> createPollEntity(p, false, false))
                .orElse(null);
        }
        return null;
    }

    public String[] myToolIds()
    {
        return new String[]{ "sakai.poll"};
    }


    @Override
    public Optional<List<String>> getTransferOptions() {
        return Optional.of(List.of(EntityTransferrer.COPY_PERMISSIONS_OPTION));
    }

    @Override
    public List<Map<String, String>> getEntityMap(String fromContext) {

        try {
            return findAllPolls(fromContext).stream()
                .map(p -> Map.of("id", p.getId(), "title", p.getText())).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to get the polls for site {}: {}", fromContext, e.toString());
        }

        return Collections.emptyList();
    }

    @Override
    public String getToolPermissionsPrefix() {
        return PERMISSION_PREFIX;
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> resourceIds, List<String> transferOptions, boolean cleanup) {

        if (cleanup) {
            try {
                for (Poll poll : findAllPolls(toContext)) {
                    deletePoll(poll);
                }
            } catch(Exception e) {
                log.warn("Could not remove existing polls in site [{}], {}", toContext, e.toString());
            }
        }

        return transferCopyEntities(fromContext, toContext, resourceIds, transferOptions);
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> resourceIds, List<String> transferOptions) {
        Map<String, String> transversalMap = new HashMap<>();
        try {
            for (Poll fromPoll : findAllPolls(fromContext)) {
                Poll toPoll = new Poll();
                toPoll.setOwner(fromPoll.getOwner());
                toPoll.setSiteId(toContext);
                toPoll.setCreationDate(fromPoll.getCreationDate());
                toPoll.setText(fromPoll.getText());
                toPoll.setMinOptions(fromPoll.getMinOptions());
                toPoll.setMaxOptions(fromPoll.getMaxOptions());
                toPoll.setVoteOpen(fromPoll.getVoteOpen());
                toPoll.setVoteClose(fromPoll.getVoteClose());
                toPoll.setDisplayResult(fromPoll.getDisplayResult());
                toPoll.setLimitVoting(fromPoll.isLimitVoting());
                String description = fromPoll.getDescription();
                description = ltiService.fixLtiLaunchUrls(description, fromContext, toContext, transversalMap);
                toPoll.setDescription(description);
 
                List<Option> options = fromPoll.getOptions();
                if (options != null && !options.isEmpty()) {
                    for (Option fromOption : options) {
                        Option toOption = new Option();
                        toOption.setStatus(fromOption.getStatus());
                        toOption.setDeleted(fromOption.getDeleted());
                        toOption.setOptionOrder(fromOption.getOptionOrder());

                        String text = fromOption.getText();
                        text = ltiService.fixLtiLaunchUrls(text, fromContext, toContext, transversalMap);
                        toOption.setText(text);
                        toPoll.addOption(toOption);
                    }
                }
                savePoll(toPoll);
            }
        } catch (Exception e) {
            log.error("Failed to save transfer polls: {}", e.toString());
        }

        return transversalMap;
    }

    public Optional<Poll> getPoll(String ref) {
        if (ref == null) {
            return Optional.empty();
        }

        String normalized = StringUtils.stripEnd(ref, Entity.SEPARATOR);
        if (StringUtils.isEmpty(normalized)) {
            normalized = ref;
        }
        String uuid = StringUtils.substringAfterLast(normalized, Entity.SEPARATOR);
        if (StringUtils.isEmpty(uuid)) {
            uuid = normalized;
        }
        if (StringUtils.isEmpty(uuid)) {
            uuid = ref;
        }

        return pollRepository.findById(uuid);
    }

    public boolean isAllowedViewResults(Poll poll, String userId) {
        if (externalLogic.isAllowedInLocation("site.upd", externalLogic.getCurrentLocationReference())) {
            return true;
        }

        if (poll.getDisplayResult().equals("open")) {
            return true;
        }

        if (poll.getDisplayResult().equals("afterVoting")) {

            boolean voted = voteRepository.existsByPollIdAndUserId(poll.getId(), userId);
            if (voted) {
                return true;
            }
        }

        if ((poll.getDisplayResult().equals("afterClosing") || poll.getDisplayResult().equals("afterVoting") )&& poll.getVoteClose().before(new Date())) {
            return true;
        }

        // the owner can view the results (null-safe comparison)
        return Objects.equals(poll.getOwner(), userId) && !externalLogic.userIsViewingAsRole();
    }

    public boolean isPollPublic(Poll poll) {

        // is this poll public?
        if (poll.isPublic()){
            return true;
        }

        //can the anonymous user vote?
        return externalLogic.isAllowedInLocation(PERMISSION_VOTE, externalLogic.getSiteRefFromId(poll.getSiteId()));
    }

    // Vote Management Methods (migrated from PollVoteManager)

    @Override
    @Transactional(readOnly = true)
    public Vote getVoteById(Long voteId) {
        if (voteId == null) {
            throw new IllegalArgumentException("voteId cannot be null when getting vote");
        }
        return voteRepository.findById(voteId).orElse(null);
    }

    @Override
    public boolean saveVote(Vote vote) {
        voteRepository.save(vote);
        log.debug("Vote {} successfully saved", vote.getId());
        return true;
    }

    @Override
    public void saveVoteList(List<Vote> votes) {
        String pollId = null;
        for (Vote vote : votes) {
            pollId = vote.getOption().getPoll().getId();
            saveVote(vote);
            getPollById(pollId).ifPresent(poll ->
                externalLogic.registerStatement(poll.getText(), vote)
            );
        }
    }

    @Override
    public Vote createVote(Poll poll, Option option, String submissionId) {
        if (poll == null) {
            throw new IllegalArgumentException("poll cannot be null when creating a vote");
        }
        if (option == null) {
            throw new IllegalArgumentException("option cannot be null when creating a vote");
        }
        if (submissionId == null) {
            throw new IllegalArgumentException("submissionId cannot be null when creating a vote");
        }

        String userId = externalLogic.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("Unable to determine current user id while creating vote");
        }

        String ip = DEFAULT_IP_ADDRESS;
        if (usageSessionService != null) {
            UsageSession usageSession = usageSessionService.getSession();
            if (usageSession != null && usageSession.getIpAddress() != null && !usageSession.getIpAddress().trim().isEmpty()) {
                ip = usageSession.getIpAddress();
            }
        }

        return new Vote(submissionId, Instant.now(), userId, ip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vote> getAllVotesForPoll(String pollId) {
        return voteRepository.findByPollId(pollId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vote> getAllVotesForOption(Option option) {
        Poll poll = option.getPoll();
        if (poll == null) {
            throw new IllegalArgumentException("option must have associated poll when retrieving votes");
        }
        return voteRepository.findByPollIdAndPollOption(poll.getId(), option.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Vote>> getVotesForUser(String userId, String[] pollIds) {
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

        Map<String, List<Vote>> map = new HashMap<>();
        for (Vote vote : votes) {
            String pollId = vote.getOption().getPoll().getId();
            map.computeIfAbsent(pollId, key -> new ArrayList<>()).add(vote);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public int getDisctinctVotersForPoll(Poll poll) {
        return voteRepository.countDistinctSubmissionIds(poll.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasVoted(String pollid, String userID) {
        return voteRepository.existsByPollIdAndUserId(pollid, userID);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasVoted(String pollId) {
        return userHasVoted(pollId, externalLogic.getCurrentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserAllowedVote(String userId, String pollId, boolean ignoreVoted) {
        boolean allowed = false;
        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll == null) {
            throw new IllegalArgumentException("Invalid poll id ("+pollId+") when checking user can vote");
        }
        if (externalLogic.isUserAdmin(userId)) {
            allowed = true;
        } else {
            String siteRef = "/site/" + poll.getSiteId();
            if (externalLogic.isAllowedInLocation(PERMISSION_VOTE, siteRef, "/user/" + userId)) {
                if (ignoreVoted) {
                    allowed = true;
                } else {
                    Map<String, List<Vote>> m = getVotesForUser(userId, new String[] {pollId});
                    if (m.isEmpty()) {
                        allowed = true;
                    }
                }
            }
        }
        return allowed;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean pollIsVotable(Poll poll) {
        // POLL-148 this could be null
        if (poll == null) {
            return false;
        }

        // poll must have options to be votable
        List<Option> votableOptions = getVisibleOptionsForPoll(poll.getId());
        if (votableOptions == null || votableOptions.isEmpty()) {
            log.debug("poll has no options");
            return false;
        }

        boolean pollAfterOpen = true;
        boolean pollBeforeClose = true;

        if (poll.getVoteClose() != null) {
            if (poll.getVoteClose().before(new Date())) {
                log.debug("Poll is closed for voting");
                pollBeforeClose = false;
            }
        }

        if (poll.getVoteOpen() != null) {
            if (new Date().before(poll.getVoteOpen())) {
                log.debug("Poll is not open yet");
                pollAfterOpen = false;
            }
        }

        if (pollAfterOpen && pollBeforeClose) {
            if (poll.isLimitVoting() && userHasVoted(poll.getId())) {
                return false;
            }
            // the user hasn't voted do they have permission to vote?
            log.debug("about to check if this user can vote in {}", poll.getSiteId());
            if (externalLogic.isAllowedInLocation("poll.vote", externalLogic.getSiteRefFromId(poll.getSiteId()))
                || externalLogic.isUserAdmin()) {
                log.debug("this poll is votable because the user has permissions, {}", poll.getText());
                return true;
            }

            // SAK-18855 individual public polls
            if (poll.isPublic()) {
                log.debug("this poll is votable because it is public, {}", poll.getText());
                return true;
            }
        }

        return false;
    }

    @Override
    public void deleteVote(Vote vote) {
        voteRepository.delete(vote);
    }

    @Override
    public void deleteAll(List<Vote> votes) {
        voteRepository.deleteAll(votes);
    }

    // EntityBroker Entity Adapter Methods Implementation

    @Override
    @Transactional(readOnly = true)
    public PollEntity createPollEntity(String pollId, boolean includeVotes, boolean includeOptions) throws SecurityException {
        Optional<Poll> pollOpt = getPollById(pollId);
        if (!pollOpt.isPresent()) {
            throw new IllegalArgumentException("Poll not found with id: " + pollId);
        }
        return createPollEntity(pollOpt.get(), includeVotes, includeOptions);
    }

    @Override
    @Transactional(readOnly = true)
    public PollEntity createPollEntity(Poll poll, boolean includeVotes, boolean includeOptions) {
        Objects.requireNonNull(poll, "Poll must not be null");

        // Construct Reference internally
        Reference reference = entityManager.newReference(REFERENCE_ROOT + "/" + poll.getId());

        // Get current user
        String currentUserId = externalLogic.getCurrentUserId();

        // Compute currentUserVoted
        boolean currentUserVoted = false;
        if (currentUserId != null) {
            currentUserVoted = userHasVoted(poll.getId(), currentUserId);
        }

        // Optionally load votes
        List<Vote> votes = null;
        if (includeVotes) {
            votes = getAllVotesForPoll(poll.getId());
        }

        // Note: includeOptions is handled by Poll.getOptions() which uses JPA lazy loading
        // If includeOptions is false and options are lazy, they won't be loaded
        // If they're already loaded or fetch is EAGER, they'll be included
        // The PollEntity delegates getOptions() to poll.getOptions()

        return new PollEntity(reference, poll, currentUserVoted, votes);
    }

    @Override
    public Poll updatePollFromEntity(String pollId, PollEntity pollEntity) throws SecurityException, IllegalArgumentException {
        Objects.requireNonNull(pollId, "Poll ID must not be null");
        Objects.requireNonNull(pollEntity, "PollEntity must not be null");

        // Load existing poll
        Optional<Poll> existingPollOpt = getPollById(pollId);
        if (!existingPollOpt.isPresent()) {
            throw new IllegalArgumentException("Poll not found with id: " + pollId);
        }

        Poll existingPoll = existingPollOpt.get();

        // Extract updates from PollEntity's wrapped Poll
        Poll updates = pollEntity.getPoll();

        // Apply updates to persistent entity
        // Only update fields that are part of the domain model
        if (updates.getText() != null) {
            existingPoll.setText(updates.getText());
        }
        if (updates.getDescription() != null) {
            existingPoll.setDescription(updates.getDescription());
        }
        if (updates.getVoteOpen() != null) {
            existingPoll.setVoteOpen(updates.getVoteOpen());
        }
        if (updates.getVoteClose() != null) {
            existingPoll.setVoteClose(updates.getVoteClose());
        }
        if (updates.getDisplayResult() != null) {
            existingPoll.setDisplayResult(updates.getDisplayResult());
        }

        existingPoll.setMinOptions(updates.getMinOptions());
        existingPoll.setMaxOptions(updates.getMaxOptions());
        existingPoll.setLimitVoting(updates.isLimitVoting());
        existingPoll.setPublic(updates.isPublic());

        // Save and return (savePoll handles security checks)
        return savePoll(existingPoll);
    }

    @Override
    public Poll createPollFromEntity(PollEntity pollEntity) throws SecurityException, IllegalArgumentException {
        Objects.requireNonNull(pollEntity, "PollEntity must not be null");

        // Extract Poll from PollEntity
        Poll poll = pollEntity.getPoll();

        // Set defaults for new poll
        if (poll.getOwner() == null) {
            poll.setOwner(externalLogic.getCurrentUserId());
        }
        if (poll.getSiteId() == null) {
            poll.setSiteId(externalLogic.getCurrentLocationId());
        }
        if (poll.getCreationDate() == null) {
            poll.setCreationDate(new Date());
        }

        // Save and return (savePoll handles security checks)
        return savePoll(poll);
    }
}
