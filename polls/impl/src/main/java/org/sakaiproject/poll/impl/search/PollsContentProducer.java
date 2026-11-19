/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.poll.impl.search;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.poll.api.PollConstants;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.api.FormattedText;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Indexes polls (question, instructions and answer options) for Sakai search. References have the form
 * {@code /poll/{siteId}/poll/{pollId}}. The poll.delete event uses {@code /poll/{siteId}/{pollId}} instead, so both
 * are understood; the index identifies a document by the poll id alone.
 */
@Slf4j
@Setter
public class PollsContentProducer implements EntityContentProducer, EntityContentProducerEvents {

    private static final String REFERENCE_PREFIX = PollConstants.REFERENCE_ROOT + Entity.SEPARATOR;
    private static final String TOOL_ID = "sakai.poll";

    private static final Map<String, Integer> EVENT_ACTIONS = Map.of(
            PollConstants.EVENT_POLL_ADD, SearchBuilderItem.ACTION_ADD,
            PollConstants.EVENT_POLL_UPDATE, SearchBuilderItem.ACTION_ADD,
            PollConstants.EVENT_POLL_DELETE, SearchBuilderItem.ACTION_DELETE);

    private FormattedText formattedText;
    private PollsService pollsService;
    private SearchIndexBuilder searchIndexBuilder;
    private ServerConfigurationService serverConfigurationService;
    private SessionManager sessionManager;
    private SiteService siteService;

    public void init() {
        if (serverConfigurationService.getBoolean("search.enable", false)) {
            searchIndexBuilder.registerEntityContentProducer(this);
        }
    }

    @Override
    public Set<String> getTriggerFunctions() {
        return EVENT_ACTIONS.keySet();
    }

    @Override
    public boolean matches(String reference) {
        return parse(reference).isPresent();
    }

    @Override
    public boolean matches(Event event) {
        return EVENT_ACTIONS.containsKey(event.getEvent()) && matches(event.getResource());
    }

    @Override
    public Integer getAction(Event event) {
        return matches(event) ? EVENT_ACTIONS.get(event.getEvent()) : SearchBuilderItem.ACTION_UNKNOWN;
    }

    /**
     * Results show the instructions and options, so besides the poll's own site and group checks a member only finds
     * a poll while those are visible in the tool: while voting is open if they may vote or the poll is public, and
     * after it closes only if results are visible.
     * This runs per search, so a scheduled poll becomes findable when it opens without being indexed again.
     */
    @Override
    public boolean canRead(String reference) {
        return parse(reference).map(ref -> {
            try {
                Optional<Poll> found = pollsService.getPollById(ref.pollId());
                if (found.isEmpty()) {
                    return false;
                }
                Poll poll = found.get();
                if (pollsService.isAllowedPollAdd(poll.getSiteId())) {
                    return true;
                }
                String userId = sessionManager.getCurrentSessionUserId();
                Instant now = Instant.now();
                if (now.isBefore(poll.getVoteOpen())) {
                    return false;
                }
                if (now.isAfter(poll.getVoteClose())) {
                    return pollsService.isAllowedViewResults(poll, userId);
                }
                return pollsService.isPollPublic(poll) || pollsService.isUserAllowedVote(userId, poll.getId(), true);
            } catch (SecurityException e) {
                return false;
            }
        }).orElse(false);
    }

    @Override
    public String getTitle(String reference) {
        return poll(reference).map(this::title).orElse("");
    }

    @Override
    public String getContent(String reference) {
        return poll(reference).map(poll -> Stream.concat(
                        Stream.of(title(poll), poll.getDescription()),
                        pollsService.getVisibleOptionsForPoll(poll.getId()).stream().map(Option::getText))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "))).orElse("");
    }

    /**
     * The url is stored when the poll is indexed, so it can't depend on who searches. It leads to the poll list rather
     * than the voting page, which turns away anyone who can't vote at that moment, such as those who already voted.
     */
    @Override
    public String getUrl(String reference) {
        return parse(reference).map(ref -> {
            try {
                ToolConfiguration tool = siteService.getSite(ref.siteId()).getToolForCommonId(TOOL_ID);
                if (tool != null) {
                    return serverConfigurationService.getPortalUrl() + "/site/" + ref.siteId() + "/tool/" + tool.getId();
                }
            } catch (Exception e) {
                log.debug("Could not build url for poll {}: {}", reference, e.toString());
            }
            return "";
        }).orElse("");
    }

    @Override
    public String getTool() {
        return "polls";
    }

    @Override
    public String getId(String reference) {
        return parse(reference).map(PollRef::pollId).orElse("");
    }

    @Override
    public String getSiteId(String reference) {
        return parse(reference).map(PollRef::siteId).orElse(null);
    }

    @Override
    public Iterator<String> getSiteContentIterator(String siteId) {
        return pollsService.findAllPolls(siteId).stream()
                .map(poll -> REFERENCE_PREFIX + siteId + Entity.SEPARATOR + PollConstants.REF_POLL_TYPE + Entity.SEPARATOR + poll.getId())
                .iterator();
    }

    // The indexer has no user to check, so this is the unchecked lookup; canRead guards what searchers see
    private Optional<Poll> poll(String reference) {
        return parse(reference).flatMap(ref -> pollsService.getPoll(ref.pollId()));
    }

    // The question is stored escaped, and the tool unescapes it for display too
    private String title(Poll poll) {
        return formattedText.unEscapeHtml(poll.getText());
    }

    private static Optional<PollRef> parse(String reference) {
        if (reference == null || !reference.startsWith(REFERENCE_PREFIX)) {
            return Optional.empty();
        }
        String[] parts = reference.substring(REFERENCE_PREFIX.length()).split(Entity.SEPARATOR);
        boolean typed = parts.length == 3 && PollConstants.REF_POLL_TYPE.equals(parts[1]);
        if (!(typed || parts.length == 2) || parts[0].isEmpty() || parts[parts.length - 1].isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new PollRef(parts[0], parts[parts.length - 1]));
    }

    private record PollRef(String siteId, String pollId) { }
}
