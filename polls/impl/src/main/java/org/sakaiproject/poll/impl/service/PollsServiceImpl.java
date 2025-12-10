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
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.RenderedTemplate;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.poll.api.entity.PollEntity;
import org.sakaiproject.poll.api.model.VoteCollection;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.repository.PollRepository;
import org.sakaiproject.poll.api.repository.VoteRepository;
import org.sakaiproject.poll.api.util.PollUtil;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.MergeConfig;
import org.sakaiproject.util.ResourceLoader;
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
    public static final String EMAIL_TEMPLATE_NOTIFY_DELETED_OPTION = "polls.notifyDeletedOption";

    @Setter private AuthzGroupService authzGroupService;
    @Setter private EmailService emailService;
    @Setter private List<String> emailTemplates;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private EmailTemplateService emailTemplateService;
    @Setter private EntityManager entityManager;
    @Setter private FunctionManager functionManager;
    @Setter private PollRepository pollRepository;
    @Setter private VoteRepository voteRepository;
    @Setter private LearningResourceStoreService learningResourceStoreService;
    @Setter private LTIService ltiService;
    @Setter private LinkMigrationHelper linkMigrationHelper;
    @Setter private SecurityService securityService;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;
    @Setter private ToolManager toolManager;
    @Setter private UsageSessionService usageSessionService;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private ResourceLoader optionDeletedBundle;

    public void init() {
        entityManager.registerEntityProducer(this, REFERENCE_ROOT);
        functionManager.registerFunction(PERMISSION_VOTE, true);
        functionManager.registerFunction(PERMISSION_ADD, true);
        functionManager.registerFunction(PERMISSION_DELETE_OWN, true);
        functionManager.registerFunction(PERMISSION_DELETE_ANY, true);
        functionManager.registerFunction(PERMISSION_EDIT_ANY, true);
        functionManager.registerFunction(PERMISSION_EDIT_OWN, true);

        emailTemplateService.processEmailTemplates(emailTemplates);
    }

    public List<Poll> findAllPollsForUserAndSitesAndPermission(String userId, String[] siteIds, String permissionConstant) {
        if (userId == null || permissionConstant == null) {
            throw new IllegalArgumentException("userId and permissionConstant must be set");
        }
        List<Poll> polls;
        // get all allowed sites for this user
        List<String> allowedSites = getSitesForUser(userId, permissionConstant);
        if (allowedSites.isEmpty()) {
                // no sites to search so EXIT here
            return new ArrayList<>();
        } else {
            if (siteIds != null && siteIds.length > 0) {
                List<String> requestedSiteIds = Arrays.asList(siteIds);
                // filter down to just the requested ones
                allowedSites.retainAll(requestedSiteIds);
            }
            Instant now = Instant.now();
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
        String userId = sessionManager.getCurrentSessionUserId();
        String siteRef = siteService.siteReference(poll.getSiteId());
        if (!securityService.unlock(userId, PERMISSION_ADD, siteRef)) {
            throw new SecurityException("user:" + userId + " can't add poll to site: " + siteRef);
        }

        if (poll.getCreationDate() == null) poll.setCreationDate(Instant.now());
        if (StringUtils.isBlank(poll.getOwner())) poll.setOwner(userId);

        boolean isNew = poll.getId() == null;
        Poll savedPoll = pollRepository.save(poll);

        log.debug("Poll {} successfully saved", poll);

        LearningResourceStoreService.LRS_Actor student = learningResourceStoreService.getActor(userId);
        String url = serverConfigurationService.getPortalUrl();
        LearningResourceStoreService.LRS_Verb verb = new LearningResourceStoreService.LRS_Verb(LearningResourceStoreService.LRS_Verb.SAKAI_VERB.interacted);
        LearningResourceStoreService.LRS_Object lrsObject = new LearningResourceStoreService.LRS_Object(url + "/poll", isNew ? "new-poll" : "updated-poll");
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "User " + (isNew ? "created" : "updated") + " a poll");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<>();
        descMap.put("en-US", "User " + (isNew ? "created" : "updated") + " a poll with text:" + savedPoll.getText());
        lrsObject.setDescription(descMap);
        LearningResourceStoreService.LRS_Statement statement = new LearningResourceStoreService.LRS_Statement(student, verb, lrsObject);
        String eventType = isNew ? "poll.add" : "poll.update";
        // TODO update poll REFERENCE - "/poll/" + poll.getSiteId() + "/poll/" + poll.getId()
        Event event = eventTrackingService.newEvent(eventType, "/poll/" + poll.getSiteId() + "/poll/" + poll.getId(), null, true, NotificationService.NOTI_OPTIONAL, statement);
        eventTrackingService.post(event);

        return savedPoll;
    }

    @Override
    public void deletePoll(final String id) throws SecurityException, IllegalArgumentException {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("The id can't be null");
        }

        pollRepository.findById(id).ifPresent(poll -> {
            if (!canDeletePoll(poll)) {
                throw new SecurityException("user: " + sessionManager.getCurrentSessionUserId() + " can't delete poll: " + id);
            }

            // Delete poll - cascade will automatically delete options but not votes
            voteRepository.findByPollId(id).forEach(voteRepository::delete);
            pollRepository.deleteById(id);

            log.debug("Poll id {} deleted", id);
            eventTrackingService.post(eventTrackingService.newEvent("poll.delete", "/poll/" + poll.getSiteId() + "/" + id, true));
        });
    }

    @Override
    public boolean userCanDeletePoll(final Poll poll) {
        String siteRef = siteService.siteReference(poll.getSiteId());
        String userId = sessionManager.getCurrentSessionUserId();
        return securityService.unlock(userId, PERMISSION_DELETE_ANY, siteRef)
            || (securityService.unlock(userId, PERMISSION_DELETE_OWN, siteRef) && poll.getOwner().equals(userId));
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
            String userId = sessionManager.getCurrentSessionUserId();
            if (!securityService.unlock(userId, "site.visit", siteService.siteReference(poll.get().getSiteId()))) {
                throw new SecurityException("user:" + userId + " can't read poll " + pollId);
            }
        }
        return poll;
    }

    public List<Option> getVisibleOptionsForPoll(final String pollId) {
        return pollRepository.findById(pollId)
                .map(p -> p.getOptions().stream().filter(Predicate.not(Option::getDeleted)).toList())
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public Optional<Poll> getPollWithVotes(final String pollId) {
        return pollRepository.findById(pollId);
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

    public Poll saveNewOption(Poll poll, final Option option) {
        if (poll == null || option == null) {
            throw new IllegalArgumentException("Poll/Option cannot be null when saving");
        }

        // Save through poll aggregate
        if (poll.getId() != null && option.getId() == null) {
            poll.addOption(option);
            Poll saved = pollRepository.save(poll);
            log.debug("Option {} successfully saved", option);
            return saved;
        } else {
            log.warn("Cannot save existing option {}", option.getId());
        }

        return null;
    }

    private boolean canDeletePoll(final Poll poll) {
        String siteRef = siteService.siteReference(poll.getSiteId());
        String userId = sessionManager.getCurrentSessionUserId();
        return securityService.unlock(userId,"site.upd", siteRef)
            || securityService.unlock(userId, PERMISSION_DELETE_ANY, siteRef)
            || (securityService.unlock(userId, PERMISSION_DELETE_OWN, siteRef) && poll.getOwner().equals(userId));
    }

    private boolean isSiteOwner(final String siteId) {
        return securityService.unlock("site.upd", siteService.siteReference(siteId));
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
                    deletePoll(poll.getId());
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
                        // optionOrder is managed by @OrderColumn - position in list determines order

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
        if (securityService.unlock(userId, "site.upd", siteService.siteReference(poll.getSiteId()))) {
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

        if ((poll.getDisplayResult().equals("afterClosing") || poll.getDisplayResult().equals("afterVoting") )&& poll.getVoteClose().isBefore(Instant.now())) {
            return true;
        }

        // the owner can view the results (null-safe comparison)
        return Objects.equals(poll.getOwner(), userId) && StringUtils.isBlank(securityService.getUserEffectiveRole());
    }

    public boolean isPollPublic(Poll poll) {

        // is this poll public?
        if (poll.isPublic()){
            return true;
        }

        //can the anonymous user vote?
        return securityService.unlock(PERMISSION_VOTE, siteService.siteReference(poll.getSiteId()));
    }

    // Vote Management Methods (migrated from PollVoteManager)

    @Override
    @Transactional(readOnly = true)
    public Optional<Vote> getVoteById(Long voteId) {
        if (voteId == null) {
            throw new IllegalArgumentException("voteId cannot be null when getting vote");
        }
        return voteRepository.findById(voteId);
    }

    @Override
    public Vote saveVote(Vote vote) {
        Vote saved = voteRepository.save(vote);
        log.debug("Vote {} successfully saved", saved.getId());
        return saved;
    }

    @Override
    public void saveVoteList(List<Vote> votes) {
        String pollId;
        for (Vote vote : votes) {
            saveVote(vote);
            pollId = vote.getOption().getPoll().getId();

            getPollById(pollId).ifPresent(poll -> {
                LearningResourceStoreService.LRS_Actor student = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
                String url = serverConfigurationService.getPortalUrl();
                LearningResourceStoreService.LRS_Verb verb = new LearningResourceStoreService.LRS_Verb(LearningResourceStoreService.LRS_Verb.SAKAI_VERB.interacted);
                LearningResourceStoreService.LRS_Object lrsObject = new LearningResourceStoreService.LRS_Object(url + "/poll", "voted-in-poll");
                HashMap<String, String> nameMap = new HashMap<>();
                nameMap.put("en-US", "User voted in a poll");
                lrsObject.setActivityName(nameMap);
                HashMap<String, String> descMap = new HashMap<>();
                descMap.put("en-US", "User voted in a poll with text:" + poll.getText() + "; their vote was option: " + vote.getOption().getId());
                lrsObject.setDescription(descMap);
                LearningResourceStoreService.LRS_Statement statement = new LearningResourceStoreService.LRS_Statement(student, verb, lrsObject);

                String pollRef = "/poll/" + poll.getSiteId() + "/poll/" + poll.getId();
                Event event = eventTrackingService.newEvent("poll.vote", pollRef, null, true, NotificationService.NOTI_OPTIONAL, statement);
                eventTrackingService.post(event);
            });
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

        String userId = sessionManager.getCurrentSessionUserId();
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

        return new Vote(option, submissionId, Instant.now(), userId, ip);
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
        return voteRepository.findByOptionId(option.getId());
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
    public int getDistinctVotersForPoll(Poll poll) {
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
        return userHasVoted(pollId, sessionManager.getCurrentSessionUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserAllowedVote(String userId, String pollId, boolean ignoreVoted) {
        boolean allowed = false;
        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll == null) {
            throw new IllegalArgumentException("Invalid poll id ("+pollId+") when checking user can vote");
        }
        String siteRef = "/site/" + poll.getSiteId();
        if (securityService.unlock(userId, PERMISSION_VOTE, siteRef)) {
            if (ignoreVoted) {
                allowed = true;
            } else {
                Map<String, List<Vote>> m = getVotesForUser(userId, new String[] {pollId});
                if (m.isEmpty()) {
                    allowed = true;
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

        Instant now = Instant.now();
        if (poll.getVoteClose() != null) {
            if (poll.getVoteClose().isBefore(now)) {
                log.debug("Poll is closed for voting");
                pollBeforeClose = false;
            }
        }

        if (poll.getVoteOpen() != null) {
            if (now.isBefore(poll.getVoteOpen())) {
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
            if (securityService.unlock("poll.vote", siteService.siteReference(poll.getSiteId()))) {
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
        Reference reference = entityManager.newReference(REFERENCE_ROOT + "/" + poll.getSiteId() + "/poll/" + poll.getId());

        // Get current user
        String currentUserId = sessionManager.getCurrentSessionUserId();

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
            poll.setOwner(sessionManager.getCurrentSessionUserId());
        }
        if (poll.getSiteId() == null) {
            poll.setSiteId(toolManager.getCurrentPlacement().getContext());
        }
        if (poll.getCreationDate() == null) {
            poll.setCreationDate(Instant.now());
        }

        // Save and return (savePoll handles security checks)
        return savePoll(poll);
    }

    // Bulk Operations

    @Override
    public void deletePolls(java.util.Collection<String> pollIds) {
        for (String pollId : pollIds) {
            Optional<Poll> poll = getPollById(pollId);
            if (poll.isEmpty()) {
                log.warn("Poll {} not found during bulk delete", pollId);
                continue;
            }
            try {
                deletePoll(poll.get().getId());
            } catch (SecurityException e) {
                log.warn("User {} is not permitted to delete poll {}", sessionManager.getCurrentSessionUserId(), pollId);
            }
        }
    }

    @Override
    public void resetPollVotes(java.util.Collection<String> pollIds) {
        for (String pollId : pollIds) {
            Optional<Poll> poll = getPollById(pollId);
            if (poll.isEmpty()) {
                log.warn("Poll {} not found during bulk vote reset", pollId);
                continue;
            }
            if (userCanDeletePoll(poll.get())) {
                List<Vote> votes = getAllVotesForPoll(poll.get().getId());
                deleteAll(votes);
            }
        }
    }

    @Override
    public Poll deleteOptionWithVoteHandling(Long optionId, String orphanVoteHandling) {
        Optional<Option> option = getOptionById(optionId);
        if (option.isEmpty()) {
            throw new IllegalArgumentException("Option not found");
        }

        Poll poll = option.get().getPoll();
        if (poll == null) {
            throw new IllegalArgumentException("Option has no associated poll");
        }

        List<Vote> votes = getAllVotesForOption(option.get());

        if (votes != null && !votes.isEmpty()) {
            if ("return-votes".equals(orphanVoteHandling)) {
                Set<String> userIds = new java.util.TreeSet<>();
                deleteOption(option.get().getId());
                for (Vote vote : votes) {
                    if (vote.getUserId() != null) {
                        try {
                            String userEid = userDirectoryService.getUserEid(vote.getUserId());
                            if (StringUtils.isNotBlank(userEid)) {
                                userIds.add(userEid);
                            }
                        } catch (UserNotDefinedException e) {
                            log.warn("User {} not found during vote return", vote.getUserId());
                        }
                    }
                    deleteVote(vote);
                }
                String siteTitle = siteService.getOptionalSite(poll.getSiteId())
                        .map(Site::getTitle)
                        .orElse("Site Not Found: " + poll.getSiteId());
                notifyDeletedOption(new ArrayList<>(userIds), siteTitle, poll.getText());
            } else {
                // "do-nothing" - soft delete
                getOptionById(optionId).ifPresent(o -> deleteOption(o.getId(), true));
            }
        } else {
            deleteOption(option.get().getId());
        }

        return poll;
    }

    @Override
    public void saveOptionsBatch(String pollId, java.util.List<String> optionTexts) {
        if (optionTexts == null || optionTexts.isEmpty()) {
            throw new IllegalArgumentException("Option texts list cannot be empty");
        }

        Optional<Poll> poll = getPollById(pollId);
        if (poll.isEmpty()) {
            throw new IllegalArgumentException("Poll not found: " + pollId);
        }
        // optionOrder is managed by @OrderColumn - position in list determines order
        for (String optionText : optionTexts) {
            if (StringUtils.isNotBlank(optionText)) {
                Option option = new Option();
                option.setText(optionText);
                saveNewOption(poll.get(), option);
            }
        }
    }

    @Override
    public VoteCollection submitVote(String pollId, java.util.List<Long> selectedOptionIds) {
        Optional<Poll> poll = getPollById(pollId);
        if (poll.isEmpty()) {
            throw new IllegalArgumentException("Poll not found: " + pollId);
        }

        if (!pollIsVotable(poll.get())) {
            throw new IllegalArgumentException("User cannot vote on this poll");
        }

        if (poll.get().isLimitVoting() && userHasVoted(pollId)) {
            throw new IllegalArgumentException("User has already voted on this poll");
        }

        List<Long> votesToProcess = new ArrayList<>();
        if (selectedOptionIds != null) {
            votesToProcess.addAll(selectedOptionIds);
        }

        // Handle zero-min polls with no selection
        if (votesToProcess.isEmpty() && poll.get().getMinOptions() == 0) {
            votesToProcess.add(0L);
        }

        // Validate option membership and uniqueness (ignore sentinel 0L for zero-min polls)
        if (!(votesToProcess.size() == 1 && votesToProcess.get(0) == 0L)) {
            Set<Long> uniqueIds = new java.util.TreeSet<>(votesToProcess);
            if (uniqueIds.size() != votesToProcess.size()) {
                throw new IllegalArgumentException("Duplicate options selected");
            }

            List<Option> allowedOptions = getVisibleOptionsForPoll(pollId);
            Set<Long> allowedIds = allowedOptions.stream()
                    .map(Option::getId)
                    .collect(Collectors.toSet());

            if (!allowedIds.containsAll(votesToProcess)) {
                throw new IllegalArgumentException("Invalid option selected");
            }
        }

        // Validate vote selection counts
        int selectionCount = votesToProcess.size();
        if (poll.get().getMaxOptions() == poll.get().getMinOptions()
                && poll.get().getMaxOptions() == 1
                && selectionCount == 0) {
            throw new IllegalArgumentException("Must select exactly " + poll.get().getMinOptions() + " option(s)");
        }

        if (selectionCount > poll.get().getMaxOptions()) {
            throw new IllegalArgumentException("Too many options selected. Maximum: " + poll.get().getMaxOptions());
        }

        if (selectionCount < poll.get().getMinOptions()) {
            throw new IllegalArgumentException("Too few options selected. Minimum: " + poll.get().getMinOptions());
        }

        // Create and save votes
        VoteCollection voteCollection = new VoteCollection();
        voteCollection.setPollId(pollId);

        List<Vote> votesToSave = new ArrayList<>();
        for (Long optionId : votesToProcess) {
            Optional<Option> option = getOptionById(optionId);
            if (option.isPresent()) {
                Vote vote = createVote(poll.get(), option.get(), voteCollection.getId());
                votesToSave.add(vote);
            }
        }

        saveVoteList(votesToSave);
        voteCollection.setVotes(votesToSave);
        return voteCollection;
    }

    /**
     * Notify a list of users that an option they voted for in a poll has been deleted.
     *
     * @param userEids
     * 	A List of user EID's that identify the users to be notified
     * @param pollQuestion
     * 	The text of the poll whose option was deleted
     * @param siteTitle
     * 	The title of the site that owns the option's poll
     */
    private void notifyDeletedOption(List<String> userEids, String siteTitle, String pollQuestion) {
        Objects.requireNonNull(siteTitle, "Site title cannot be null");
        Objects.requireNonNull(pollQuestion, "Poll Question cannot be null");

        Map<String, Object> replacementValues = new HashMap<>();
        String from = serverConfigurationService.getSmtpFrom();

        for (String userEid : userEids) {
            User user;
            try {
                user = userDirectoryService.getUserByEid(userEid);
                replacementValues.put("localSakaiName", serverConfigurationService.getString("ui.service", "Sakai"));
                replacementValues.put("recipientFirstName",user.getFirstName());
                replacementValues.put("recipientDisplayName", user.getDisplayName());
                replacementValues.put("pollQuestion", pollQuestion);
                replacementValues.put("siteTitle", siteTitle);

                replacementValues.put("subject", optionDeletedBundle.getString("subject"));
                replacementValues.put("message1", optionDeletedBundle.getString("message1"));
                replacementValues.put("message2", optionDeletedBundle.getString("message2"));
                replacementValues.put("message3", optionDeletedBundle.getString("message3"));
                replacementValues.put("message4", optionDeletedBundle.getString("message4"));
                replacementValues.put("message5", optionDeletedBundle.getString("message5"));

                RenderedTemplate template = emailTemplateService.getRenderedTemplateForUser(
                        EMAIL_TEMPLATE_NOTIFY_DELETED_OPTION,
                        user.getReference(),
                        replacementValues);

                if (template == null) return;

                emailService.send(from, user.getEmail(), template.getRenderedSubject(), template.getRenderedMessage(), user.getEmail(), from, null);
            } catch (UserNotDefinedException e) {
                log.warn("Attempted to send email to unknown user (eid): [{}]", userEid, e);
            }
        }
    }

    private List<String> getSitesForUser(String userId, String permission) {
        log.debug("userId: {}, permission: {}", userId, permission);

        // Get authorized groups for user and permission
        Set<String> authzGroupIds = authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);

        // Filter and transform to site IDs
        List<String> siteIds = authzGroupIds.stream()
                .map(entityManager::newReference)
                .filter(r -> r.isKnownType()
                        && r.getType().equals(SiteService.APPLICATION_ID)
                        && SiteService.SITE_SUBTYPE.equals(r.getSubType()))
                .map(Reference::getId)
                .collect(Collectors.toList());

        if (siteIds.isEmpty()) {
            log.info("Empty list of siteIds for user:{}, permission: {}", userId, permission);
        }

        return siteIds;
    }

    @Override
    @Transactional(readOnly = true)
    public int getNumberUsersCanVote(String siteId) {
        if (siteId == null) {
            throw new IllegalArgumentException("siteId cannot be null");
        }
        List<String> siteGroupRefs = new ArrayList<>();
        siteGroupRefs.add(siteService.siteReference(siteId));
        return authzGroupService.getUsersIsAllowed(PERMISSION_VOTE, siteGroupRefs).size();
    }
}
