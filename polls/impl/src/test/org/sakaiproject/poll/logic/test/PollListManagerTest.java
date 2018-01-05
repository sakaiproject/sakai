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

package org.sakaiproject.poll.logic.test;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import org.sakaiproject.poll.dao.PollDao;
import org.sakaiproject.poll.logic.test.stubs.ExternalLogicStubb;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.service.impl.PollListManagerImpl;
import org.sakaiproject.poll.service.impl.PollVoteManagerImpl;

@ContextConfiguration(locations={
		"/hibernate-test.xml",
		"classpath:org/sakaiproject/poll/spring-hibernate.xml" })
@Slf4j
public class PollListManagerTest extends AbstractTransactionalJUnit4SpringContextTests {

	private TestDataPreload tdp = new TestDataPreload();

	@Autowired
	@Qualifier("org.sakaiproject.poll.dao.impl.PollDaoTarget")
	private PollDao dao;
	private PollListManagerImpl pollListManager;
	private PollVoteManagerImpl pollVoteManager;
	private ExternalLogicStubb externalLogicStubb;
	
	@Before
	public void onSetUp() {
		pollListManager = new PollListManagerImpl();
		pollListManager.setDao(dao);
		
		pollVoteManager = new PollVoteManagerImpl();
		pollVoteManager.setDao(dao);
		
		
		externalLogicStubb = new ExternalLogicStubb();
		pollListManager.setExternalLogic(externalLogicStubb);
		pollVoteManager.setExternalLogic(externalLogicStubb);
		pollListManager.setPollVoteManager(pollVoteManager);
		
		// preload testData
		tdp.preloadTestData(dao);
	}
	
	@Test
    public void testGetPollById() {
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
    	
    	//we shouldNot find this poll
    	Poll pollFail = pollListManager.getPollById(Long.valueOf(9999999));
    	Assert.assertNull(pollFail);
    	
    	//this one should exist -- the preload saves one poll and remembers its ID
    	externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
    	Poll poll1 = pollListManager.getPollById(tdp.getFirstPollId());
    	Assert.assertNotNull(poll1);
    	
    	//it should have options
    	Assert.assertNotNull(poll1.getPollOptions());
    	Assert.assertTrue(poll1.getPollOptions().size() > 0);
    	
    	//we expect this one to fails
		externalLogicStubb.currentUserId = TestDataPreload.USER_NO_ACCEESS;
		try {
			Poll poll2 = pollListManager.getPollById(tdp.getFirstPollId());
			Assert.fail("should not be allowed to read this poll");
		} 
		catch (SecurityException e) {
			log.error(e.getMessage(), e);
		}
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
		
		
		//If this has a value something is wrong without POJO
		Assert.assertNull(poll1.getPollId());
		
		pollListManager.savePoll(poll1);
		
		//if this is null we have a problem
		Assert.assertNotNull(poll1.getPollId());
		
		Poll poll2 = pollListManager.getPollById(poll1.getPollId());
		Assert.assertNotNull(poll2);
		Assert.assertEquals(poll1.getPollText(), poll2.getPollText());
		
		//TODO add failure cases - null parameters
		
		//we should not be able to save empty polls
		
		//a user needs privileges to save the poll
		try {
			pollListManager.savePoll(null);
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		}
		
		
		//a user needs privileges to save the poll
		try {
			Poll poll = new Poll();
			poll.setText("sdfgsdf");
			pollListManager.savePoll(poll);
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		}
		
		externalLogicStubb.currentUserId = TestDataPreload.USER_NO_ACCEESS;
		try {
			pollListManager.savePoll(poll1);
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		}
		catch (SecurityException se) {
			log.error(se.getMessage(), se);
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
			pollListManager.deletePoll(poll1);
			Assert.fail();
		} catch (SecurityException e) {
			log.error(e.getMessage(), e);
		} 
		catch (IllegalArgumentException e) {
			// Successful tests should be quiet. IllegalArgumentException is actually expected on a null ID.
			//log.error(e.getMessage(), e);
		}
		
		pollListManager.savePoll(poll1);
		
	    Option option1 = new Option();
	    option1.setPollId(poll1.getPollId());
	    option1.setOptionText("asdgasd");
	    
	    Option option2 = new Option();
	    option2.setPollId(poll1.getPollId());
	    option2.setOptionText("zsdbsdfb");
	    
	    pollListManager.saveOption(option2);
	    pollListManager.saveOption(option1);
	    
	    Vote vote = new Vote();
	    vote.setIp("Localhost");
	    vote.setPollId(poll1.getPollId());
	    vote.setPollOption(option1.getOptionId());
	    
	    
	    pollVoteManager.saveVote(vote);
	    
	    Long option1Id = option1.getOptionId();
	    Long option2Id = option2.getOptionId();
	    Long voteId = vote.getId();
	    
	    
		externalLogicStubb.currentUserId = TestDataPreload.USER_NO_ACCEESS;
		
    	try {
			pollListManager.deletePoll(poll1);
			Assert.fail();
		} catch (SecurityException e) {
			// Successful tests should be quiet. SecurityException is expected here.
			//log.error(e.getMessage(), e);
		}
		
		
		externalLogicStubb.currentUserId = TestDataPreload.USER_UPDATE;
    	try {
			pollListManager.deletePoll(poll1);
		} catch (SecurityException e) {
			log.error(e.getMessage(), e);
			Assert.fail();
		}
		
		
		//check that child options are deteled
		Vote v1 = pollVoteManager.getVoteById(voteId);
		Assert.assertNull(v1);
		
		Option o1 = pollListManager.getOptionById(option1Id);
		Option o2 = pollListManager.getOptionById(option2Id);
		Assert.assertNull(o1);
		Assert.assertNull(o2);
    }
}
