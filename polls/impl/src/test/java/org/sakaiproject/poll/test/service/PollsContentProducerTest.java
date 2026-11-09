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
package org.sakaiproject.poll.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.poll.api.PollConstants;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.repository.PollRepository;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Runs without a surrounding transaction, the way the indexer calls the producer, so a lazy load of the options outside
 * the service's own transaction would fail here.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PollsServiceTestConfiguration.class)
public class PollsContentProducerTest {

    private static final String STUDENT = "student";
    private static final String INSTRUCTOR = "instructor";

    @Autowired
    @Qualifier("org.sakaiproject.poll.impl.search.PollsContentProducer")
    private EntityContentProducer producer;

    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private FormattedText formattedText;
    @Autowired private PollRepository pollRepository;
    @Autowired private PollsService pollsService;
    @Autowired private SecurityService securityService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;

    private String siteId;
    private String siteRef;

    @Before
    public void setup() {
        siteId = UUID.randomUUID().toString();
        siteRef = "/site/" + siteId;
        when(siteService.siteReference(siteId)).thenReturn(siteRef);
        when(securityService.unlock(STUDENT, "site.visit", siteRef)).thenReturn(true);
        when(securityService.unlock(STUDENT, PollConstants.PERMISSION_VOTE, siteRef)).thenReturn(true);
        when(securityService.unlock(INSTRUCTOR, "site.visit", siteRef)).thenReturn(true);
        when(securityService.unlock(INSTRUCTOR, PollConstants.PERMISSION_ADD, siteRef)).thenReturn(true);
        when(formattedText.unEscapeHtml(anyString())).thenAnswer(i -> i.<String>getArgument(0).replace("&amp;", "&"));
        when(sessionManager.getCurrentSessionUserId()).thenReturn(STUDENT);
    }

    @Test
    public void listensToPollChangesAsThePollsTool() {
        assertEquals(Set.of("poll.add", "poll.update", "poll.delete"), ((EntityContentProducerEvents) producer).getTriggerFunctions());
        assertEquals("polls", producer.getTool());
    }

    @Test
    public void bothReferenceFormsIdentifyTheSameDocument() {
        String indexed = "/poll/site1/poll/abc";
        String deleted = "/poll/site1/abc";

        assertTrue(producer.matches(indexed));
        assertTrue(producer.matches(deleted));
        assertEquals("abc", producer.getId(indexed));
        assertEquals(producer.getId(indexed), producer.getId(deleted));
        assertEquals("site1", producer.getSiteId(indexed));
        assertEquals("site1", producer.getSiteId(deleted));

        assertFalse(producer.matches((String) null));
        assertFalse(producer.matches("/poll"));
        assertFalse(producer.matches("/poll/site1"));
        assertFalse(producer.matches("/poll/site1/option/abc"));
        assertFalse(producer.matches("/rubrics/site1/1"));
        assertEquals("", producer.getId("/poll/site1"));
        assertNull(producer.getSiteId("/poll/site1"));
    }

    @Test
    public void pollEventsMapToIndexActions() {
        assertEquals(SearchBuilderItem.ACTION_ADD, producer.getAction(event("poll.add", "/poll/site1/poll/abc")));
        assertEquals(SearchBuilderItem.ACTION_ADD, producer.getAction(event("poll.update", "/poll/site1/poll/abc")));
        assertEquals(SearchBuilderItem.ACTION_DELETE, producer.getAction(event("poll.delete", "/poll/site1/abc")));

        assertFalse(producer.matches(event("poll.vote", "/poll/site1/poll/abc")));
        assertEquals(SearchBuilderItem.ACTION_UNKNOWN, producer.getAction(event("poll.vote", "/poll/site1/poll/abc")));
        assertEquals(SearchBuilderItem.ACTION_UNKNOWN, producer.getAction(event("poll.update", "/other/abc")));
    }

    @Test
    public void contentHoldsQuestionInstructionsAndVisibleOptions() {
        Poll poll = poll(p -> {
            p.setText("Pizza &amp; pasta?");
            p.setDescription("<p>Pick photosynthesis</p>");
            p.addOption(option("Margherita", false));
            p.addOption(option("Removed topping", true));
        });
        String reference = reference(poll);

        assertEquals("Pizza & pasta?", producer.getTitle(reference));
        String content = producer.getContent(reference);
        assertTrue(content, content.contains("Pizza & pasta?"));
        assertTrue(content, content.contains("photosynthesis"));
        assertTrue(content, content.contains("Margherita"));
        assertFalse(content, content.contains("Removed topping"));
    }

    @Test
    public void missingPollHasNothingToIndexAndCannotBeRead() {
        String reference = "/poll/" + siteId + "/poll/" + UUID.randomUUID();

        assertEquals("", producer.getTitle(reference));
        assertEquals("", producer.getContent(reference));
        assertFalse(producer.canRead(reference));
    }

    @Test
    public void siteContentListsEveryPollOfTheSiteOnly() {
        Poll first = poll(p -> { });
        Poll second = poll(p -> { });
        String otherSite = UUID.randomUUID().toString();
        poll(p -> p.setSiteId(otherSite));

        List<String> references = new ArrayList<>();
        producer.getSiteContentIterator(siteId).forEachRemaining(references::add);

        assertEquals(2, references.size());
        assertTrue(references.contains(reference(first)));
        assertTrue(references.contains(reference(second)));
    }

    @Test
    public void membersReadAnOpenPollButOutsidersDoNot() {
        String reference = reference(poll(p -> { }));

        assertTrue(producer.canRead(reference));

        when(sessionManager.getCurrentSessionUserId()).thenReturn("outsider");
        assertFalse(producer.canRead(reference));
    }

    @Test
    public void anOpenPollNeedsVotePermissionUnlessItIsPublic() {
        when(securityService.unlock("visitor", "site.visit", siteRef)).thenReturn(true);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("visitor");
        String privatePoll = reference(poll(p -> { }));
        String publicPoll = reference(poll(p -> p.setPublic(true)));

        assertFalse(producer.canRead(privatePoll));
        assertTrue(producer.canRead(publicPoll));
    }

    @Test
    public void groupPollsAreOnlyReadByMembersOfThoseGroups() throws IdUnusedException {
        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group1");
        Site site = mock(Site.class);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getGroupsWithMember(STUDENT)).thenReturn(List.of());
        when(site.getGroupsWithMember("member")).thenReturn(List.of(group));
        when(securityService.unlock("member", "site.visit", siteRef)).thenReturn(true);
        when(securityService.unlock("member", PollConstants.PERMISSION_VOTE, siteRef)).thenReturn(true);
        String reference = reference(poll(p -> {
            p.setTypeOfAccess(Poll.Access.GROUP);
            p.setGroupIds(Set.of("group1"));
        }));

        assertFalse(producer.canRead(reference));

        when(sessionManager.getCurrentSessionUserId()).thenReturn("member");
        assertTrue(producer.canRead(reference));
    }

    @Test
    public void onlyEditorsReadAPollBeforeItOpens() {
        String reference = reference(poll(p -> {
            p.setVoteOpen(Instant.now().plus(1, ChronoUnit.DAYS));
            p.setVoteClose(Instant.now().plus(2, ChronoUnit.DAYS));
        }));

        assertFalse(producer.canRead(reference));

        when(sessionManager.getCurrentSessionUserId()).thenReturn(INSTRUCTOR);
        assertTrue(producer.canRead(reference));
    }

    @Test
    public void afterClosingMembersNeedToSeeResultsToReadAPoll() {
        String closedWithResults = reference(poll(p -> closed(p, "open")));
        String closedWithoutResults = reference(poll(p -> closed(p, "never")));

        assertTrue(producer.canRead(closedWithResults));
        assertFalse(producer.canRead(closedWithoutResults));

        when(sessionManager.getCurrentSessionUserId()).thenReturn(INSTRUCTOR);
        assertTrue(producer.canRead(closedWithoutResults));
    }

    @Test
    public void urlLeadsToTheToolInTheSite() throws IdUnusedException {
        ToolConfiguration tool = mock(ToolConfiguration.class);
        when(tool.getId()).thenReturn("tool1");
        Site site = mock(Site.class);
        when(site.getToolForCommonId("sakai.poll")).thenReturn(tool);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(serverConfigurationService.getPortalUrl()).thenReturn("http://sakai/portal");

        assertEquals("http://sakai/portal/site/" + siteId + "/tool/tool1", producer.getUrl("/poll/" + siteId + "/poll/abc"));

        when(site.getToolForCommonId("sakai.poll")).thenReturn(null);
        assertEquals("", producer.getUrl("/poll/" + siteId + "/poll/abc"));
    }

    @Test
    public void changingOptionsAnnouncesThePollUpdate() {
        when(securityService.unlock(STUDENT, PollConstants.PERMISSION_ADD, siteRef)).thenReturn(false);
        Poll poll = poll(p -> p.addOption(option("Existing", false)));
        String reference = reference(poll);
        verify(eventTrackingService, never()).newEvent("poll.update", reference, true);

        pollsService.saveOptionsBatch(poll.getId(), List.of("Added"));
        verify(eventTrackingService).newEvent("poll.update", reference, true);

        Long optionId = pollsService.getVisibleOptionsForPoll(poll.getId()).get(0).getId();
        pollsService.deleteOption(optionId);
        verify(eventTrackingService, atLeastOnce()).newEvent("poll.update", reference, true);
        assertEquals(1, pollsService.getVisibleOptionsForPoll(poll.getId()).size());
    }

    private void closed(Poll poll, String displayResult) {
        poll.setVoteOpen(Instant.now().minus(2, ChronoUnit.DAYS));
        poll.setVoteClose(Instant.now().minus(1, ChronoUnit.DAYS));
        poll.setDisplayResult(displayResult);
    }

    private Poll poll(java.util.function.Consumer<Poll> customizer) {
        Poll poll = new Poll();
        poll.setSiteId(siteId);
        poll.setOwner(INSTRUCTOR);
        poll.setText("Question");
        poll.setCreationDate(Instant.now());
        poll.setVoteOpen(Instant.now().minus(1, ChronoUnit.HOURS));
        poll.setVoteClose(Instant.now().plus(1, ChronoUnit.HOURS));
        customizer.accept(poll);
        return pollRepository.save(poll);
    }

    private Option option(String text, boolean deleted) {
        Option option = new Option();
        option.setText(text);
        option.setDeleted(deleted);
        return option;
    }

    private String reference(Poll poll) {
        return "/poll/" + poll.getSiteId() + "/poll/" + poll.getId();
    }

    private Event event(String name, String resource) {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(name);
        when(event.getResource()).thenReturn(resource);
        return event;
    }
}
