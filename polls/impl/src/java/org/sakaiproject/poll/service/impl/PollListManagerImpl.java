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

package org.sakaiproject.poll.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Setter;

import org.springframework.dao.DataAccessException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.repository.OptionRepository;
import org.sakaiproject.poll.repository.PollRepository;
import org.sakaiproject.poll.repository.VoteRepository;
import org.sakaiproject.poll.util.PollUtil;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.sakaiproject.util.MergeConfig;

@Slf4j
@Data
public class PollListManagerImpl implements PollListManager,EntityTransferrer {

    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "poll";

    private EntityManager entityManager;
    private IdManager idManager;
    private PollRepository pollRepository;
    private OptionRepository optionRepository;
    private VoteRepository voteRepository;
    private PollVoteManager pollVoteManager;    
    private ExternalLogic externalLogic;
    @Setter private LTIService ltiService;
    @Setter private LinkMigrationHelper linkMigrationHelper;
    public void init() {
        try {
            entityManager.registerEntityProducer(this, REFERENCE_ROOT);
        } catch (Exception t) {
            log.warn("init(): ", t);
        }

        externalLogic.registerFunction(PERMISSION_VOTE, true);
        externalLogic.registerFunction(PERMISSION_ADD, true);
        externalLogic.registerFunction(PERMISSION_DELETE_OWN, true);
        externalLogic.registerFunction(PERMISSION_DELETE_ANY, true);
        externalLogic.registerFunction(PERMISSION_EDIT_ANY, true);
        externalLogic.registerFunction(PERMISSION_EDIT_OWN, true);
        log.info(this + " init()");
    }

    public List<Poll> findAllPollsForUserAndSitesAndPermission(String userId, String[] siteIds,
            String permissionConstant) {
        if (userId == null || permissionConstant == null) {
            throw new IllegalArgumentException("userId and permissionConstant must be set");
        }
        List<Poll> polls = null;
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
            if (PollListManager.PERMISSION_VOTE.equals(permissionConstant)) {
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

    public boolean savePoll(Poll t) throws SecurityException, IllegalArgumentException {
        boolean newPoll = false;
        
        if (t == null || t.getText() == null || t.getSiteId() == null || t.getVoteOpen() == null|| t.getVoteClose() == null) {
            throw new IllegalArgumentException("you must supply a question, siteId & open and close dates");
        }
        
        if (!externalLogic.isUserAdmin() && !externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getSiteRefFromId(t.getSiteId()),
                    externalLogic.getCurrentuserReference())) {
            throw new SecurityException();
        }
        
        if (t.getPollId() == null) {
            newPoll = true;
            String generatedUuid = null;
            if (idManager != null) {
                generatedUuid = idManager.createUuid();
            }
            if (generatedUuid == null || generatedUuid.trim().isEmpty()) {
                generatedUuid = UUID.randomUUID().toString();
            }
            t.setUuid(generatedUuid);
        }
        if(t.getCreationDate() == null) {
            t.setCreationDate(new Date());
        }

        try {
           if(!newPoll && t.getPollId() != null) {
                Poll poll = getPollById(t.getPollId());
                if(poll != null && !t.getOwner().equals(poll.getOwner())) {
                   t.setOwner(poll.getOwner());
                }
            }
            t = pollRepository.save(t);

        } catch (DataAccessException e) {
            log.error("Hibernate could not save: {}", t, e);
            return false;
        }
        log.debug("Poll {} successfully saved", t.toString());
        externalLogic.registerStatement(t.getText(), newPoll, t.getPollId().toString());

        return true;
    }

    public boolean deletePoll(Poll t) throws SecurityException, IllegalArgumentException {
        if (t == null) {
            throw new IllegalArgumentException("Poll can't be null");
        }

        if (t.getPollId() == null) {
            throw new IllegalArgumentException("Poll id can't be null");
        }

        if (!pollCanDelete(t)) {
            throw new SecurityException("user:" + externalLogic.getCurrentuserReference() + " can't delete poll: " + t.getUuid());
        }

        //Delete the Votes
        List<Vote> vote = t.getVotes();

        //We could have a partially populate item
        if (vote == null || vote.isEmpty()) {
            log.debug("getting votes as they where null");
            vote = pollVoteManager.getAllVotesForPoll(t);
            log.debug("got {} vote", vote.size());
        }

        voteRepository.deleteAll(vote);

        //Delete the Options
        List<Option> options = t.getOptions();
        //as above we could have a partialy populate item
        if (options ==  null || options.isEmpty()) {
            options = getOptionsForPoll(t);
        }
 
        optionRepository.deleteAll(options);

        pollRepository.delete(t);

        log.debug("Poll id {} deleted", t.getUuid());
        externalLogic.postEvent("poll.delete", "poll/site/"
                + t.getSiteId() + "/poll/" + t.getUuid(), true);
        return true;
    }

    public boolean userCanDeletePoll(Poll poll) {
        if (externalLogic.isUserAdmin())
            return true;
        if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_DELETE_ANY, externalLogic.getCurrentLocationReference()))
            return true;
        if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_DELETE_OWN, externalLogic.getCurrentLocationReference()) && poll.getOwner().equals(externalLogic.getCurrentUserId()))
            return true;

        return false;
    }

    public List<Poll> findAllPolls() {
        return new ArrayList<>(pollRepository.findAll());
    }

    public List<Poll> findAllPolls(String siteId) {
        return new ArrayList<>(pollRepository.findBySiteIdOrderByCreationDateDesc(siteId));
    }

    public Poll getPollById(Long pollId) throws SecurityException {
 
       return getPollById(pollId, true);
    }

    public Poll getPollById(Long pollId, boolean includeOptions) throws SecurityException {
        Poll poll = pollRepository.findById(pollId).orElse(null);

        if (poll == null) {
            return null;
        }

        if (includeOptions) {
            List<Option> optionList = getOptionsForPoll(poll);
            poll.setOptions(optionList);
        }

      //user needs at least site visit to read a poll
        if (!externalLogic.isAllowedInLocation("site.visit", externalLogic.getSiteRefFromId(poll.getSiteId()), externalLogic.getCurrentuserReference()) && !externalLogic.isUserAdmin()) {
            throw new SecurityException("user:" + externalLogic.getCurrentuserReference() + " can't read poll " + pollId);
        }

        return poll;
    }


    // OPTIONS

    public List<Option> getOptionsForPoll(Poll poll) {
        if (poll == null) {
            throw new IllegalArgumentException("Poll cannot be null when retrieving options");
        }
        return getOptionsForPoll(poll.getPollId());
    }

    public List<Option> getOptionsForPoll(Long pollId) {
        if (pollId == null) {
            throw new IllegalArgumentException("Poll id cannot be null when retrieving options");
        }
        return optionRepository.findByPollIdOrderByOptionOrder(pollId);
    }

    public List<Option> getVisibleOptionsForPoll(Long pollId) {
        List<Option> options = getOptionsForPoll(pollId);
 
        //iterate and remove deleted options
        options.removeIf(o -> o == null || o.getDeleted());
 
        return options;
    }

    public Poll getPollWithVotes(Long pollId) {
        return pollRepository.findById(pollId).orElse(null);
    }

    public Option getOptionById(Long optionId) {
        Option option = optionRepository.findById(optionId).orElse(null);
        if (option != null && option.getUuid() == null) {
            option.setUuid(UUID.randomUUID().toString());
            saveOption(option);
        }
        return option;
    }

    public void deleteOption(Option option) {
        optionRepository.delete(option);
        log.debug("Option id {} deleted", option.getOptionId());
    }

    public void deleteOption(Option option, boolean soft) {
        if (!soft) {
            deleteOption(option);
        } else {
            option.setDeleted(true);
            optionRepository.save(option);
            log.debug("Option id {} soft deleted.", option.getOptionId());
        }
    }

    public boolean saveOption(Option t) {
        if (t.getUuid() == null || t.getUuid().trim().isEmpty()) {
            t.setUuid( UUID.randomUUID().toString() );
        }

        optionRepository.save(t);
        log.debug("Option {} successfully saved", t.toString());
        return true;
    }

    
    // INTERNAL

    private boolean pollCanDelete(Poll poll) {
        if (externalLogic.isUserAdmin() || this.isSiteOwner(poll.getSiteId()))
            return true;
        if (externalLogic.isAllowedInLocation(PERMISSION_DELETE_ANY, externalLogic.getSiteRefFromId(poll.getSiteId())))
            return true;

        return externalLogic.isAllowedInLocation(PERMISSION_DELETE_OWN, externalLogic.getSiteRefFromId(poll.getSiteId()))
                && poll.getOwner().equals(externalLogic.getCurrentUserId());
    }

    private boolean isSiteOwner(String siteId) {
        if (externalLogic.isUserAdmin()) return true;
        return externalLogic.isAllowedInLocation("site.upd", externalLogic.getSiteRefFromId(siteId));
    }

    /*
     * EntityProducer Methods
     */
    public String getLabel() {
        return "poll";
    }

    public boolean willArchiveMerge() {
        return true;
    }

    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath,
            List<Reference> attachments) {
        log.debug("archive: poll {}", siteId);
        // prepare the buffer for the results log
        StringBuilder results = new StringBuilder();

        // String assignRef = assignmentReference(siteId, SiteService.MAIN_CONTAINER);
        results.append("archiving " + getLabel() + " context " + Entity.SEPARATOR
                       + siteId + Entity.SEPARATOR + SiteService.MAIN_CONTAINER
                       + ".\n");

        // start with an element with our very own (service) name
        Element element = doc.createElement(PollListManager.class.getName());
        stack.peek().appendChild(element);
        stack.push(element);

        List<Poll> pollsList = findAllPolls(siteId);
        log.debug("got list of {} polls", pollsList.size());
        for (Poll poll : pollsList) {
            try {
                log.debug("got poll {}", poll.getUuid());

                // archive this assignment
                Element el = poll.toXml(doc, stack);

                // since we aren't archiving votes too, don't worry about archiving the
                // soft-deleted options -- only "visible".
                List<Option> options = getVisibleOptionsForPoll(poll.getPollId());

                for (Option option : options) {
                    Element el2 = PollUtil.optionToXml(option, doc, stack);
                    el.appendChild(el2);
                }

                element.appendChild(el);
            } catch (Exception e) {
                log.error("Failed to archive {} in site {}", poll.getUuid(), siteId, e);
            }

        } // while

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
            String details = poll.getDetails();
            details = ltiService.fixLtiLaunchUrls(details, siteId, mcx);
            details = linkMigrationHelper.migrateLinksInMergedRTE(siteId, mcx, details);
            poll.setDetails(details);

            savePoll(poll);
            NodeList options = pollElement.getElementsByTagName("option");
            for (int j=0; j<options.getLength(); ++j) {
                Element optionElement = (Element) options.item(j);
                Option option = PollUtil.xmlToOption(optionElement);
                option.setOptionId(null);  // To force insert
                option.setUuid(UUID.randomUUID().toString());
                option.setPollId(poll.getPollId());
                String text = option.getText();
                text = ltiService.fixLtiLaunchUrls(text, siteId, mcx);
                text = linkMigrationHelper.migrateLinksInMergedRTE(siteId, mcx, text);
                option.setText(text);
                saveOption(option);
                poll.addOption(option);
            }
        }
        return null;
    }

    public boolean parseEntityReference(String reference, Reference ref) {
        if (reference == null || !reference.startsWith(REFERENCE_ROOT)) {
            return false;
        }

        String subType = "";
        String context = null;
        String id = null;
        String container = "";

        String remainder = StringUtils.removeStart(reference, REFERENCE_ROOT);
        remainder = StringUtils.removeStart(remainder, Entity.SEPARATOR);

        if (StringUtils.isNotEmpty(remainder)) {
            String[] segments = StringUtils.split(remainder, Entity.SEPARATOR);
            if (segments.length > 0) {
                context = segments[0];
                if (segments.length > 1) {
                    id = segments[1];
                }
            }
        }

        ref.set(PollListManager.class.getName(), subType, id, container, context);

        return true;
    }

    public Entity getEntity(Reference ref) {
        if (REF_POLL_TYPE.equals(ref.getSubType())) {
            Poll poll = getPoll(ref.getReference());
            if (poll != null) {
                return new PollEntityAdapter(poll);
            }
        }
        return null;
    }

    public String[] myToolIds()
    {
        return new String[]{ "sakai.poll"};
    }

    private static class PollEntityAdapter implements Entity {

        private final Poll delegate;

        PollEntityAdapter(Poll delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getId() {
            return delegate.getUuid();
        }

        @Override
        public String getReference() {
            return delegate.getReference();
        }

        @Override
        public String getReference(String rootProperty) {
            return delegate.getReference(rootProperty);
        }

        @Override
        public String getUrl() {
            return delegate.getUrl();
        }

        @Override
        public String getUrl(String rootProperty) {
            return delegate.getUrl(rootProperty);
        }

        @Override
        public ResourceProperties getProperties() {
            return delegate.getProperties();
        }
    }

    @Override
    public Optional<List<String>> getTransferOptions() {
        return Optional.of(Arrays.asList(new String[] { EntityTransferrer.COPY_PERMISSIONS_OPTION }));
    }

    @Override
    public List<Map<String, String>> getEntityMap(String fromContext) {

        try {
            return findAllPolls(fromContext).stream()
                .map(p -> Map.of("id", p.getUuid(), "title", p.getText())).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to get the polls for site {}: {}", fromContext, e.toString());
        }

        return Collections.emptyList();
    }

    @Override
    public String getToolPermissionsPrefix() {
        return PollListManager.PERMISSION_PREFIX;
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
                Poll fromPollV = getPollWithVotes(fromPoll.getPollId());
                Poll toPoll = new Poll();
                toPoll.setOwner(fromPollV.getOwner());
                toPoll.setSiteId(toContext);
                toPoll.setCreationDate(fromPollV.getCreationDate());
                toPoll.setText(fromPollV.getText());
                toPoll.setMinOptions(fromPollV.getMinOptions());
                toPoll.setMaxOptions(fromPollV.getMaxOptions());
                toPoll.setVoteOpen(fromPollV.getVoteOpen());
                toPoll.setVoteClose(fromPollV.getVoteClose());
                toPoll.setDisplayResult(fromPollV.getDisplayResult());
                toPoll.setLimitVoting(fromPollV.isLimitVoting());
                String details = fromPollV.getDetails();
                details = ltiService.fixLtiLaunchUrls(details, fromContext, toContext, transversalMap);
                toPoll.setDetails(details);
 
                //Guardamos toPoll para que se puedan ir añandiéndole las opciones y los votos
                savePoll(toPoll);
 
                //Añadimos las opciones
                List<Option> options = getOptionsForPoll(fromPoll);
                if (options != null) {
                    for (Option fromOption : options) {
                        Option toOption = new Option();
                        toOption.setStatus(fromOption.getStatus());
                        toOption.setPollId(toPoll.getPollId());
                        toOption.setDeleted(fromOption.getDeleted());
                        toOption.setOptionOrder(fromOption.getOptionOrder());

                        String text = fromOption.getText();
                        text = ltiService.fixLtiLaunchUrls(text, fromContext, toContext, transversalMap);
                        toOption.setText(text);

                        saveOption(toOption);
 
                        toPoll.addOption(toOption);
                    }
                }

                //Añadimos los votos
                List<Vote> votes = fromPollV.getVotes();
                if (votes != null) {
                    votes.forEach(toPoll::addVote);
                }

                //Actualizamos toPoll
                savePoll(toPoll);
            }
        } catch (Exception e) {
            log.error("Failed to save transfer polls: {}", e.toString());
        }

        return transversalMap;
    }

    public Poll getPoll(String ref) {
        if (ref == null) {
            return null;
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

        Poll poll = pollRepository.findByUuid(uuid).orElse(null);
        if (poll != null && poll.getUuid() == null) {
            poll.setUuid(idManager.createUuid());
            savePoll(poll);
        }
        return poll;
    }

    public boolean isAllowedViewResults(Poll poll, String userId) {
        if (externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation("site.upd", externalLogic.getCurrentLocationReference())) {
            return true;
        }

        if (poll.getDisplayResult().equals("open")) {
            return true;
        }

        if (poll.getDisplayResult().equals("afterVoting")) {

            boolean voted = voteRepository.existsByPollIdAndUserId(poll.getPollId(), userId);
            if (voted) {
                return true;
            }
        }

        if ((poll.getDisplayResult().equals("afterClosing") || poll.getDisplayResult().equals("afterVoting") )&& poll.getVoteClose().before(new Date())) {
            return true;
        }

        //the owner can view the results
        return poll.getOwner().equals(userId) && !externalLogic.userIsViewingAsRole();
    }

    public boolean isPollPublic(Poll poll) {
 
        // is this poll public?
        if (poll.isPublic()){
            return true;
        }
 
        //can the anonymous user vote?
        return externalLogic.isAllowedInLocation(PollListManager.PERMISSION_VOTE, externalLogic.getSiteRefFromId(poll.getSiteId()));
    }
}
