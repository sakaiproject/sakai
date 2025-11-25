/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.poll.test.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.model.VoteCollection;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.impl.service.PollsServiceImpl;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import static org.sakaiproject.poll.api.PollConstants.*;

import lombok.extern.slf4j.Slf4j;

@ContextConfiguration(classes = {PollsServiceTestConfiguration.class})
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
public class PollsServiceTests {

    public final static String USER_NO_ACCEESS = "user-nobody";
    public final static String USER = "user-12345678";
    public final static String LOCATION1_ID = "ref-1111111";
    public final static String LOCATION1_REF = "/site/" + LOCATION1_ID;

    @Autowired private PollsService pollsService;
    @Autowired private SecurityService securityService;
    @Autowired private SiteService siteService;
    @Autowired private SessionManager sessionManager;

    @Before
    public void onSetUp() {
        ResourceLoader etsOptionDeleted = Mockito.mock(ResourceLoader.class);
        Mockito.when(etsOptionDeleted.getString("subject")).thenReturn("A poll option you voted for has been deleted");
        Mockito.when(etsOptionDeleted.getString("message1")).thenReturn("Dear");
        Mockito.when(etsOptionDeleted.getString("message2")).thenReturn("The poll option you voted for in the site");
        Mockito.when(etsOptionDeleted.getString("message3")).thenReturn("has been deleted by a poll maintainer. The poll question is:");
        Mockito.when(etsOptionDeleted.getString("message4")).thenReturn("Please log in to");
        Mockito.when(etsOptionDeleted.getString("message5")).thenReturn("and place a new vote for the poll.");
        ((PollsServiceImpl) AopTestUtils.getTargetObject(pollsService)).setOptionDeletedBundle(etsOptionDeleted);

        Mockito.when(siteService.siteReference(LOCATION1_ID)).thenReturn(LOCATION1_REF);

        Mockito.when(securityService.unlock(USER, "site.visit", LOCATION1_REF)).thenReturn(true);
        Mockito.when(securityService.unlock(USER, PERMISSION_ADD, LOCATION1_REF)).thenReturn(true);
        Mockito.when(securityService.unlock(USER, PERMISSION_DELETE_OWN, LOCATION1_REF)).thenReturn(true);
        Mockito.when(securityService.unlock(USER, PERMISSION_DELETE_ANY, LOCATION1_REF)).thenReturn(true);
        Mockito.when(securityService.unlock(USER_NO_ACCEESS, PERMISSION_ADD, LOCATION1_REF)).thenReturn(false);
    }

    private String createPoll(String ownerId, String siteId) {
        Poll poll1 = new Poll();
        poll1.setCreationDate(new Date());
        poll1.setVoteOpen(new Date());
        poll1.setVoteClose(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        poll1.setDescription("this is some text");
        poll1.setText("something");
        poll1.setOwner(ownerId);
        poll1.setSiteId(siteId);

        Option option1 = new Option();
        option1.setText("Option 1");
        option1.setOptionOrder(0);
        poll1.addOption(option1);

        Option option2 = new Option();
        option2.setText("Option 2");
        option2.setOptionOrder(1);
        poll1.addOption(option2);

        return pollsService.savePoll(poll1).getId();
    }

    @Test
    public void testGetPollById() {
        // we shouldNot find this poll
        Optional<Poll> pollFail = pollsService.getPollById("non-existent-uuid");
        Assert.assertTrue(pollFail.isEmpty());

        // this one should exist -- the preload saves one poll and remembers its ID
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);
        Assert.assertTrue(poll.isPresent());

        // it should have options
        Assert.assertNotNull(poll.get().getOptions());
        Assert.assertFalse(poll.get().getOptions().isEmpty());

        // we expect this one to fails
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_NO_ACCEESS);
        Assert.assertThrows(SecurityException.class, () -> pollsService.getPollById(pollId));
    }

    @Test
    public void testSavePoll() {
        Poll poll1 = new Poll();
        poll1.setCreationDate(new Date());
        poll1.setVoteOpen(new Date());
        poll1.setVoteClose(new Date());
        poll1.setDescription("this is some text");
        poll1.setText("something");
        poll1.setOwner(USER);
        poll1.setSiteId(LOCATION1_ID);

        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        Poll savedPoll1 = pollsService.savePoll(poll1);
        Assert.assertNotNull(savedPoll1);
        Assert.assertNotNull(savedPoll1.getId());
        Assert.assertEquals(poll1.getText(), savedPoll1.getText());

        Assert.assertThrows(IllegalArgumentException.class, () -> pollsService.savePoll(null));

        Poll poll = new Poll();
        poll.setText("sdfgsdf");
        Assert.assertThrows(IllegalArgumentException.class, () -> pollsService.savePoll(poll));

        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_NO_ACCEESS);
        Assert.assertThrows(SecurityException.class, () -> pollsService.savePoll(poll1));
    }

    @Test
    public void testDeletePoll() {

        Poll poll1 = new Poll();
        poll1.setCreationDate(new Date());
        poll1.setVoteOpen(new Date());
        poll1.setVoteClose(new Date());
        poll1.setDescription("this is some text");
        poll1.setText("something");
        poll1.setOwner(USER);
        poll1.setSiteId(LOCATION1_ID);

        // we should not be able to delete a poll that hasn't been saved
        Assert.assertThrows(IllegalArgumentException.class, () -> pollsService.deletePoll(poll1.getId()));

        Option option1 = new Option();
        option1.setText("asdgasd");
        option1.setOptionOrder(0);
        poll1.addOption(option1);

        Option option2 = new Option();
        option2.setText("zsdbsdfb");
        option2.setOptionOrder(1);
        poll1.addOption(option2);

        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        Poll savedPoll = pollsService.savePoll(poll1);

        Vote vote = new Vote();
        vote.setIp("Localhost");
        vote.setUserId(USER);
        vote.setVoteDate(Instant.now());
        vote.setSubmissionId(USER + ":" + UUID.randomUUID());
        vote.setOption(option1);

        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        pollsService.saveVote(vote);

        List<Vote> votes = pollsService.getAllVotesForPoll(savedPoll.getId());

        Assert.assertEquals(2, savedPoll.getOptions().size());
        Assert.assertEquals(1, votes.size());
        savedPoll.getOptions().forEach(o -> Assert.assertNotNull(o.getId()));
        votes.forEach(v -> Assert.assertNotNull(v.getId()));

        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_NO_ACCEESS);
        Assert.assertThrows(SecurityException.class, () -> pollsService.deletePoll(savedPoll.getId()));

        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        try {
            pollsService.deletePoll(savedPoll.getId());
        } catch (SecurityException e) {
            log.error(e.toString());
            Assert.fail();
        }

        Optional<Poll> deletedPoll = pollsService.getPollById(savedPoll.getId());
        Assert.assertTrue(deletedPoll.isEmpty());
    }

    @Test
    public void testFindAllPolls() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);

        List<Poll> pollsBefore = pollsService.findAllPolls();
        int initialCount = pollsBefore.size();

        createPoll(USER, LOCATION1_ID);
        createPoll(USER, LOCATION1_ID);

        List<Poll> pollsAfter = pollsService.findAllPolls();
        Assert.assertEquals(initialCount + 2, pollsAfter.size());
    }

    @Test
    public void testFindAllPollsBySite() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);

        String site1 = LOCATION1_ID;
        String site2 = "site-2222";
        String site2Ref = "/site/" + site2;

        Mockito.when(siteService.siteReference(site2)).thenReturn(site2Ref);
        Mockito.when(securityService.unlock(USER, PERMISSION_ADD, site2Ref)).thenReturn(true);

        createPoll(USER, site1);
        createPoll(USER, site1);
        createPoll(USER, site2);

        List<Poll> site1Polls = pollsService.findAllPolls(site1);
        Assert.assertTrue(site1Polls.size() >= 2);
        site1Polls.forEach(p -> Assert.assertEquals(site1, p.getSiteId()));
    }

    @Test
    public void testGetPoll() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);

        String ref = "/poll/" + LOCATION1_ID + "/" + pollId;
        Optional<Poll> poll = pollsService.getPoll(ref);
        Assert.assertTrue(poll.isPresent());
        Assert.assertEquals(pollId, poll.get().getId());

        Optional<Poll> notFound = pollsService.getPoll("/poll/nonexistent/ref");
        Assert.assertTrue(notFound.isEmpty());
    }

    @Test
    public void testGetPollWithVotes() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);

        Optional<Poll> pollOpt = pollsService.getPollWithVotes(pollId);
        Assert.assertTrue(pollOpt.isPresent());
        Assert.assertEquals(pollId, pollOpt.get().getId());

        Optional<Poll> notFound = pollsService.getPollWithVotes("nonexistent-id");
        Assert.assertTrue(notFound.isEmpty());
    }

    // ========== Option Tests ==========

    @Test
    public void testGetOptionById() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);

        Optional<Poll> poll = pollsService.getPollById(pollId);
        Long optionId = poll.get().getOptions().get(0).getId();

        Optional<Option> option = pollsService.getOptionById(optionId);
        Assert.assertTrue(option.isPresent());
        Assert.assertEquals(optionId, option.get().getId());

        Optional<Option> notFound = pollsService.getOptionById(999999L);
        Assert.assertTrue(notFound.isEmpty());
    }

    @Test
    public void testGetVisibleOptionsForPoll() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);

        List<Option> options = pollsService.getVisibleOptionsForPoll(pollId);
        Assert.assertEquals(2, options.size());

        // Soft delete one option
        Long optionId = options.get(0).getId();
        pollsService.deleteOption(optionId, true);

        List<Option> visibleOptions = pollsService.getVisibleOptionsForPoll(pollId);
        Assert.assertEquals(1, visibleOptions.size());
    }

    @Test
    public void testSaveNewOption() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        Option newOption = new Option();
        String random = UUID.randomUUID().toString();
        newOption.setText(random);
        newOption.setOptionOrder(2);

        Poll saved = pollsService.saveNewOption(poll.get(), newOption);
        Assert.assertNotNull(saved);
        Assert.assertNotNull(saved.getOptions().stream().filter(o -> o.getText().equals(random)).findFirst().orElse(null));
    }

    @Test
    public void testDeleteOption() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);
        Long optionId = poll.get().getOptions().get(0).getId();

        pollsService.deleteOption(optionId);

        Optional<Option> deleted = pollsService.getOptionById(optionId);
        Assert.assertTrue(deleted.isEmpty());
    }

    @Test
    public void testDeleteOptionSoft() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);
        Long optionId = poll.get().getOptions().get(0).getId();

        pollsService.deleteOption(optionId, true);

        Optional<Option> softDeleted = pollsService.getOptionById(optionId);
        Assert.assertTrue(softDeleted.isPresent());
        Assert.assertTrue(softDeleted.get().getDeleted());
    }

    @Test
    public void testSaveNewOptionsBatch() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);

        List<String> optionTexts = List.of("Batch Option 1", "Batch Option 2", "Batch Option 3");
        pollsService.saveOptionsBatch(pollId, optionTexts);

        // Reload poll to get updated options
        Optional<Poll> reloaded = pollsService.getPollById(pollId);
        List<Option> options = pollsService.getVisibleOptionsForPoll(pollId);
        Assert.assertTrue(options.size() >= 5); // 2 original + 3 new
    }

    @Test
    public void testDeleteOptionWithVoteHandling() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);
        Option option = poll.get().getOptions().get(0);
        Long optionId = option.getId();

        Vote vote = new Vote();
        vote.setIp("Localhost");
        vote.setUserId(USER);
        vote.setVoteDate(Instant.now());
        vote.setSubmissionId(USER + ":" + UUID.randomUUID());
        vote.setOption(option);
        pollsService.saveVote(vote);

        Poll result = pollsService.deleteOptionWithVoteHandling(optionId, "do-nothing");
        Assert.assertNotNull(result);

        // Reload the option from database
        Optional<Poll> reloadedPoll = pollsService.getPollById(pollId);
        Optional<Option> softDeleted = reloadedPoll.get().getOptions().stream()
            .filter(o -> o.getId().equals(optionId))
            .findFirst();
        Assert.assertTrue(softDeleted.isPresent());
        Assert.assertTrue(softDeleted.get().getDeleted());
    }

    // ========== Vote Tests ==========

    @Test
    public void testGetVoteById() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        Vote vote = new Vote();
        vote.setIp("127.0.0.1");
        vote.setUserId(USER);
        vote.setVoteDate(Instant.now());
        vote.setSubmissionId(USER + ":" + UUID.randomUUID());
        vote.setOption(poll.get().getOptions().get(0));
        pollsService.saveVote(vote);

        Optional<Vote> retrieved = pollsService.getVoteById(vote.getId());
        Assert.assertTrue(retrieved.isPresent());
        Assert.assertEquals(vote.getId(), retrieved.get().getId());

        Optional<Vote> notFound = pollsService.getVoteById(999999L);
        Assert.assertTrue(notFound.isEmpty());
    }

    @Test
    public void testSaveVoteList() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        List<Vote> votes = List.of(
            createVoteForOption(poll.get().getOptions().get(0)),
            createVoteForOption(poll.get().getOptions().get(1))
        );

        pollsService.saveVoteList(votes);

        votes.forEach(v -> Assert.assertNotNull(v.getId()));
    }

    @Test
    public void testCreateVote() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);
        Option option = poll.get().getOptions().get(0);

        String submissionId = UUID.randomUUID().toString();
        Vote vote = pollsService.createVote(poll.get(), option, submissionId);

        Assert.assertNotNull(vote);
        Assert.assertEquals(submissionId, vote.getSubmissionId());
        Assert.assertEquals(USER, vote.getUserId());
        Assert.assertNotNull(vote.getVoteDate());
    }

    @Test
    public void testGetAllVotesForOption() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);
        Option option = poll.get().getOptions().get(0);
        Long optionId = option.getId();

        Vote vote1 = createVoteForOption(option);
        Vote vote2 = createVoteForOption(option);
        pollsService.saveVote(vote1);
        pollsService.saveVote(vote2);

        // Reload option to ensure proper relationship
        Optional<Poll> reloaded = pollsService.getPollById(pollId);
        Option reloadedOption = reloaded.get().getOptions().stream()
            .filter(o -> o.getId().equals(optionId))
            .findFirst().get();

        List<Vote> votes = pollsService.getAllVotesForOption(reloadedOption);
        Assert.assertTrue(votes.size() >= 2);
    }

    @Test
    public void testGetVotesForUser() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        Vote vote = createVoteForOption(poll.get().getOptions().get(0));
        pollsService.saveVote(vote);

        java.util.Map<String, List<Vote>> votesMap = pollsService.getVotesForUser(USER, null);
        Assert.assertFalse(votesMap.isEmpty());
    }

    @Test
    public void testGetDistinctVotersForPoll() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        String submissionId = UUID.randomUUID().toString();
        Vote vote1 = createVoteForOption(poll.get().getOptions().get(0));
        vote1.setSubmissionId(submissionId);
        Vote vote2 = createVoteForOption(poll.get().getOptions().get(1));
        vote2.setSubmissionId(submissionId); // Same submission

        pollsService.saveVote(vote1);
        pollsService.saveVote(vote2);

        int distinctVoters = pollsService.getDistinctVotersForPoll(poll.get());
        Assert.assertTrue(distinctVoters >= 1);
    }

    @Test
    public void testDeleteVote() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        Vote vote = createVoteForOption(poll.get().getOptions().get(0));
        pollsService.saveVote(vote);
        Long voteId = vote.getId();

        pollsService.deleteVote(vote);

        Optional<Vote> deleted = pollsService.getVoteById(voteId);
        Assert.assertTrue(deleted.isEmpty());
    }

    @Test
    public void testDeleteAllVotes() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        Vote vote1 = createVoteForOption(poll.get().getOptions().get(0));
        Vote vote2 = createVoteForOption(poll.get().getOptions().get(1));
        pollsService.saveVote(vote1);
        pollsService.saveVote(vote2);

        List<Vote> votes = List.of(vote1, vote2);
        pollsService.deleteAll(votes);

        votes.forEach(v -> {
            Optional<Vote> deleted = pollsService.getVoteById(v.getId());
            Assert.assertTrue(deleted.isEmpty());
        });
    }

    @Test
    public void testSubmitVote() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        Mockito.when(securityService.unlock(USER, PERMISSION_VOTE, LOCATION1_REF)).thenReturn(true);
        Mockito.when(securityService.unlock("poll.vote", LOCATION1_REF)).thenReturn(true);

        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);
        Long optionId = poll.get().getOptions().get(0).getId();

        VoteCollection voteCollection = pollsService.submitVote(pollId, List.of(optionId));

        Assert.assertNotNull(voteCollection);
        Assert.assertFalse(voteCollection.getVotes().isEmpty());
    }

    // ========== Permission/Authorization Tests ==========

    @Test
    public void testUserCanDeletePoll() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        boolean canDelete = pollsService.userCanDeletePoll(poll.get());
        Assert.assertTrue(canDelete);
    }

    @Test
    public void testIsPollPublic() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        poll.get().setPublic(true);
        pollsService.savePoll(poll.get());

        boolean isPublic = pollsService.isPollPublic(poll.get());
        Assert.assertTrue(isPublic);
    }

    @Test
    public void testUserHasVoted() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        Assert.assertFalse(pollsService.userHasVoted(pollId, USER));

        Vote vote = createVoteForOption(poll.get().getOptions().get(0));
        pollsService.saveVote(vote);

        Assert.assertTrue(pollsService.userHasVoted(pollId, USER));
        Assert.assertTrue(pollsService.userHasVoted(pollId)); // current user version
    }

    @Test
    public void testIsUserAllowedVote() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        Mockito.when(securityService.unlock(USER, PERMISSION_VOTE, LOCATION1_REF)).thenReturn(true);

        String pollId = createPoll(USER, LOCATION1_ID);

        boolean allowed = pollsService.isUserAllowedVote(USER, pollId, true);
        Assert.assertTrue(allowed);
    }

    @Test
    public void testPollIsVotable() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        Mockito.when(securityService.unlock("poll.vote", LOCATION1_REF)).thenReturn(true);
        Mockito.when(siteService.siteReference(LOCATION1_ID)).thenReturn(LOCATION1_REF);

        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        boolean votable = pollsService.pollIsVotable(poll.get());
        Assert.assertTrue(votable);
    }

    @Test
    public void testIsAllowedViewResults() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        Mockito.when(securityService.unlock(USER, "site.upd", LOCATION1_REF)).thenReturn(true);

        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        boolean allowed = pollsService.isAllowedViewResults(poll.get(), USER);
        Assert.assertTrue(allowed);
    }

    // ========== Bulk Operations Tests ==========

    @Test
    public void testDeletePolls() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId1 = createPoll(USER, LOCATION1_ID);
        String pollId2 = createPoll(USER, LOCATION1_ID);

        pollsService.deletePolls(List.of(pollId1, pollId2));

        Assert.assertTrue(pollsService.getPollById(pollId1).isEmpty());
        Assert.assertTrue(pollsService.getPollById(pollId2).isEmpty());
    }

    @Test
    public void testResetPollVotes() {
        Mockito.when(sessionManager.getCurrentSessionUserId()).thenReturn(USER);
        String pollId = createPoll(USER, LOCATION1_ID);
        Optional<Poll> poll = pollsService.getPollById(pollId);

        Vote vote = createVoteForOption(poll.get().getOptions().get(0));
        pollsService.saveVote(vote);

        pollsService.resetPollVotes(List.of(pollId));

        List<Vote> votes = pollsService.getAllVotesForPoll(pollId);
        Assert.assertTrue(votes.isEmpty());
    }

    // ========== Helper Methods ==========

    private Vote createVoteForOption(Option option) {
        Vote vote = new Vote();
        vote.setIp("127.0.0.1");
        vote.setUserId(USER);
        vote.setVoteDate(Instant.now());
        vote.setSubmissionId(USER + ":" + UUID.randomUUID());
        vote.setOption(option);
        return vote;
    }

}
