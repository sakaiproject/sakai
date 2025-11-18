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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.sakaiproject.poll.api.PollConstants.*;

import lombok.extern.slf4j.Slf4j;

@ContextConfiguration(classes = {PollsServiceTestConfiguration.class})
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
public class PollsServiceTests {

    public final static String USER_NO_ACCEESS = "user-nobody";
    public final static String USER_UPDATE = "user-12345678";
    public final static String LOCATION1_ID = "ref-1111111";
    public final static String LOCATION1_REF = "/site/" + LOCATION1_ID;

    @Autowired private PollsService pollsService;
    @Autowired private SiteService siteService;
    @Autowired private DeveloperHelperService developerHelperService;

    private String firstPollId;

    @Before
    public void onSetUp() {
        Poll poll1 = new Poll();
        poll1.setCreationDate(new Date());
        poll1.setVoteOpen(new Date());
        poll1.setVoteClose(new Date());
        poll1.setDescription("this is some text");
        poll1.setText("something");
        poll1.setOwner(USER_UPDATE);
        poll1.setSiteId(LOCATION1_ID);

        Option option1 = new Option();
        option1.setText("Option 1");
        option1.setOptionOrder(0);
        poll1.addOption(option1);

        Option option2 = new Option();
        option2.setText("Option 2");
        option2.setOptionOrder(1);
        poll1.addOption(option2);

        Mockito.when(developerHelperService.getCurrentUserReference()).thenReturn(USER_UPDATE);
        Mockito.when(siteService.siteReference(LOCATION1_ID)).thenReturn(LOCATION1_REF);
        Mockito.when(developerHelperService.isUserAllowedInEntityReference(USER_UPDATE, PERMISSION_ADD, LOCATION1_REF)).thenReturn(true);
        Mockito.when(developerHelperService.isUserAllowedInEntityReference(USER_UPDATE, PERMISSION_DELETE_ANY, LOCATION1_REF)).thenReturn(true);
        Mockito.when(developerHelperService.isUserAllowedInEntityReference(USER_UPDATE, "site.visit", LOCATION1_REF)).thenReturn(true);
        Mockito.when(developerHelperService.isUserAllowedInEntityReference(USER_NO_ACCEESS, "site.visit", LOCATION1_REF)).thenReturn(false);
        firstPollId = pollsService.savePoll(poll1).getId();
    }

    @Test
    public void testGetPollById() {
        // we shouldNot find this poll
        Optional<Poll> pollFail = pollsService.getPollById("non-existent-uuid");
        Assert.assertTrue(pollFail.isEmpty());

        // this one should exist -- the preload saves one poll and remembers its ID
        Optional<Poll> poll1 = pollsService.getPollById(firstPollId);
        Assert.assertTrue(poll1.isPresent());

        // it should have options
        Assert.assertNotNull(poll1.get().getOptions());
        Assert.assertFalse(poll1.get().getOptions().isEmpty());

        // we expect this one to fails
        Mockito.when(developerHelperService.getCurrentUserReference()).thenReturn(USER_NO_ACCEESS);
        Assert.assertThrows(SecurityException.class, () -> pollsService.getPollById(firstPollId));
    }

    @Test
    public void testSavePoll() {
        Poll poll1 = new Poll();
        poll1.setCreationDate(new Date());
        poll1.setVoteOpen(new Date());
        poll1.setVoteClose(new Date());
        poll1.setDescription("this is some text");
        poll1.setText("something");
        poll1.setOwner(USER_UPDATE);
        poll1.setSiteId(LOCATION1_ID);

        Poll savedPoll1 = pollsService.savePoll(poll1);
        Assert.assertNotNull(savedPoll1);
        Assert.assertNotNull(savedPoll1.getId());
        Assert.assertEquals(poll1.getText(), savedPoll1.getText());

        Assert.assertThrows(IllegalArgumentException.class, () -> pollsService.savePoll(null));

        Poll poll = new Poll();
        poll.setText("sdfgsdf");
        Assert.assertThrows(IllegalArgumentException.class, () -> pollsService.savePoll(poll));

        Mockito.when(developerHelperService.getCurrentUserReference()).thenReturn(USER_NO_ACCEESS);
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
        poll1.setOwner(USER_UPDATE);
        poll1.setSiteId(LOCATION1_ID);

        // we should not be able to delete a poll that hasn't been saved
        Assert.assertThrows(IllegalArgumentException.class, () -> pollsService.deletePoll(poll1));

        Option option1 = new Option();
        option1.setText("asdgasd");
        option1.setOptionOrder(0);
        poll1.addOption(option1);

        Option option2 = new Option();
        option2.setText("zsdbsdfb");
        option2.setOptionOrder(1);
        poll1.addOption(option2);

        Poll savedPoll = pollsService.savePoll(poll1);

        Vote vote = new Vote();
        vote.setIp("Localhost");
        vote.setUserId(USER_UPDATE);
        vote.setVoteDate(Instant.now());
        vote.setSubmissionId(USER_UPDATE + ":" + UUID.randomUUID());
        vote.setOption(option1);

        pollsService.saveVote(vote);

        List<Vote> votes = pollsService.getAllVotesForPoll(savedPoll.getId());

        Assert.assertEquals(2, savedPoll.getOptions().size());
        Assert.assertEquals(1, votes.size());
        savedPoll.getOptions().forEach(o -> Assert.assertNotNull(o.getId()));
        votes.forEach(v -> Assert.assertNotNull(v.getId()));

        Mockito.when(developerHelperService.getCurrentUserReference()).thenReturn(USER_NO_ACCEESS);
        Assert.assertThrows(SecurityException.class, () -> pollsService.deletePoll(savedPoll));

        Mockito.when(developerHelperService.getCurrentUserReference()).thenReturn(USER_UPDATE);
        try {
            pollsService.deletePoll(savedPoll);
        } catch (SecurityException e) {
            log.error(e.toString());
            Assert.fail();
        }

        Optional<Poll> deletedPoll = pollsService.getPollById(savedPoll.getId());
        Assert.assertTrue(deletedPoll.isEmpty());
    }

    @Test
    public void testGetPollForUserCorrectSites() {
        // User can see 3 sites.
        // List<String> userSites = new ArrayList<>(Arrays.asList("site1", "site2", "site3"));
        // Mockito.when(pollsService.getSitesForUser("userId", PERMISSION_VOTE)).thenReturn(userSites);
        // Find the polls in just one site.
        // pollsService.findAllPollsForUserAndSitesAndPermission("userId", new String[]{"site3"}, PERMISSION_VOTE);
        // ArgumentCaptor<List<String>> siteCaptor = ArgumentCaptor.forClass(List.class);
        // Mockito.verify(pollRepository).findOpenPollsBySiteIds(siteCaptor.capture(), Mockito.any());
        // Assert.assertEquals(List.of("site3"), siteCaptor.getValue());
    }

}
