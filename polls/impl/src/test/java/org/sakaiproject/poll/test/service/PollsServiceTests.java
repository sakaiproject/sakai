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

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.sakaiproject.poll.api.PollConstants.PERMISSION_VOTE;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sakaiproject.poll.api.logic.ExternalLogic;
import org.sakaiproject.poll.api.repository.PollRepository;
import org.sakaiproject.poll.api.repository.VoteRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import org.sakaiproject.poll.test.service.stubs.ExternalLogicStubb;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.impl.service.PollsServiceImpl;
import org.sakaiproject.poll.impl.repository.PollRepositoryImpl;
import org.sakaiproject.poll.impl.repository.VoteRepositoryImpl;
import org.hibernate.SessionFactory;

@ContextConfiguration(locations={
		"/hibernate-test.xml" })
@Slf4j
public class PollsServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

	private TestDataPreload tdp = new TestDataPreload();
	private PollsServiceImpl pollsService;
	private ExternalLogicStubb externalLogicStubb;

	@Before
	public void onSetUp() {
		SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory");
		PollRepositoryImpl pollRepository = new PollRepositoryImpl();
		pollRepository.setSessionFactory(sessionFactory);
		VoteRepositoryImpl voteRepository = new VoteRepositoryImpl();
		voteRepository.setSessionFactory(sessionFactory);

		pollsService = new PollsServiceImpl();
		pollsService.setPollRepository(pollRepository);
		pollsService.setVoteRepository(voteRepository);

		externalLogicStubb = new ExternalLogicStubb();
		pollsService.setExternalLogic(externalLogicStubb);

		// preload testData
		tdp.preloadTestData(pollRepository);
	}
	
	@Test
    public void testGetPollById() {
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;

    	//we shouldNot find this poll
    	Optional<Poll> pollFail = pollsService.getPollById("non-existent-uuid");
    	Assert.assertTrue(pollFail.isEmpty());

    	//this one should exist -- the preload saves one poll and remembers its ID
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
    	Optional<Poll> poll1 = pollsService.getPollById(tdp.getFirstPollId());
    	Assert.assertTrue(poll1.isPresent());

    	//it should have options
    	Assert.assertNotNull(poll1.get().getOptions());
        Assert.assertFalse(poll1.get().getOptions().isEmpty());

    	//we expect this one to fails
		externalLogicStubb.currentUserId = TestDataPreload.USER_NO_ACCEESS;
        Assert.assertThrows(SecurityException.class, () -> pollsService.getPollById(tdp.getFirstPollId()));
    }

	@Test
    public void testSavePoll() {
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
		
    	Poll poll1 = new Poll();
		poll1.setCreationDate(new Date());
		poll1.setVoteOpen(new Date());
		poll1.setVoteClose(new Date());
		poll1.setDescription("this is some text");
		poll1.setText("something");
		poll1.setOwner(TestDataPreload.USER_UPDATE);
		poll1.setSiteId(TestDataPreload.LOCATION1_ID);
		
        Poll savedPoll1 = pollsService.savePoll(poll1);
        Assert.assertNotNull(savedPoll1);
		Assert.assertNotNull(savedPoll1.getId());
		Assert.assertEquals(poll1.getText(), savedPoll1.getText());
		
		Assert.assertThrows(IllegalArgumentException.class, () -> pollsService.savePoll(null));
		
        Poll poll = new Poll();
        poll.setText("sdfgsdf");
        Assert.assertThrows(IllegalArgumentException.class, () -> pollsService.savePoll(poll));

		externalLogicStubb.currentUserId = TestDataPreload.USER_NO_ACCEESS;
		try {
			pollsService.savePoll(poll1);
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			log.debug("Expected illegal argument when unauthorized user saves poll", e);
		}
		catch (SecurityException se) {
			log.debug("Expected security exception when unauthorized user saves poll", se);
		}

    }

	@Test
    public void testDeletePoll() {
    	
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
    	Poll poll1 = new Poll();
		poll1.setCreationDate(new Date());
		poll1.setVoteOpen(new Date());
		poll1.setVoteClose(new Date());
		poll1.setDescription("this is some text");
		poll1.setText("something");
		poll1.setOwner(TestDataPreload.USER_UPDATE);
		poll1.setSiteId(TestDataPreload.LOCATION1_ID);

		//we should not be able to delete a poll that hasn't been saved
		try {
			pollsService.deletePoll(poll1);
			Assert.fail();
		} catch (SecurityException e) {
			log.debug("Expected security exception when deleting unsaved poll");
		} catch (IllegalArgumentException e) {
            log.debug("Expected illegal argument when deleting poll with no id");
		}

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
	    vote.setUserId(TestDataPreload.USER_UPDATE);
	    vote.setVoteDate(Instant.now());
	    vote.setSubmissionId(TestDataPreload.USER_UPDATE + ":" + UUID.randomUUID());
	    vote.setOption(option1);

        pollsService.saveVote(vote);

        List<Vote> votes = pollsService.getAllVotesForPoll(savedPoll.getId());

        Assert.assertEquals(2, savedPoll.getOptions().size());
        Assert.assertEquals(1, votes.size());
        savedPoll.getOptions().forEach(o -> Assert.assertNotNull(o.getId()));
        votes.forEach(v -> Assert.assertNotNull(v.getId()));

		externalLogicStubb.currentUserId = TestDataPreload.USER_NO_ACCEESS;

        Assert.assertThrows(SecurityException.class, () -> pollsService.deletePoll(savedPoll));

		
		externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
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
        ExternalLogic externalLogic = Mockito.mock(ExternalLogic.class);
        PollRepository pollRepository = Mockito.mock(PollRepository.class);
        VoteRepository voteRepository = Mockito.mock(VoteRepository.class);

        PollsServiceImpl impl = new PollsServiceImpl();
        impl.setExternalLogic(externalLogic);
        impl.setPollRepository(pollRepository);
        impl.setVoteRepository(voteRepository);

        // User can see 3 sites.
        List<String> userSites = new ArrayList<>(Arrays.asList("site1", "site2", "site3"));
        Mockito.when(externalLogic.getSitesForUser("userId", PERMISSION_VOTE)).thenReturn(userSites);
        // Find the polls in just one site.
        impl.findAllPollsForUserAndSitesAndPermission("userId", new String[]{"site3"}, PERMISSION_VOTE);
        ArgumentCaptor<List<String>> siteCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(pollRepository).findOpenPollsBySiteIds(siteCaptor.capture(), Mockito.any());
        Assert.assertEquals(List.of("site3"), siteCaptor.getValue());
    }

}
