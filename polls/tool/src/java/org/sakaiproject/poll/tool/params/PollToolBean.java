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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.tool.params;


import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.model.VoteCollection;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.tool.producers.AddPollProducer;
import org.sakaiproject.poll.tool.producers.PollOptionProducer;
import org.sakaiproject.poll.tool.producers.PollToolProducer;
import org.sakaiproject.poll.util.PollUtils;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.exception.PermissionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.stringutil.StringUtil;
import uk.org.ponder.util.UniversalRuntimeException;



public class PollToolBean {

	//public Poll newPoll = new Poll();
	public String siteID;


	public Option option;

	private VoteCollection voteCollection;

	//values to hold the parts of the date
	public String openDay;
	public String openMonth;
	public String openYear;
	public String openHour;
	public String openMinutes;
	public String openAmPm;

	public String closeDay;
	public String closeMonth;
	public String closeYear;
	public String closeHour;
	public String closeMinutes;
	public String closeAmPm;
	private VoteBean voteBean;
	public Long[] deleteids;
	public String submissionStatus;
	private PollVoteManager pollVoteManager;


	public Map perms = null;
	public void setRoleperms(Map perms)
	{
		this.perms = perms;
	} 

	private static Log m_log = LogFactory.getLog(PollToolBean.class);

	private PollListManager manager;
	public void setPollListManager(PollListManager manager) {
		this.manager = manager;
	}

	public void setPollVoteManager(PollVoteManager pvm){
		this.pollVoteManager = pvm;
	}

	public void setVoteBean(VoteBean vb){
		this.voteBean = vb;
	}

	public void setVoteCollection(VoteCollection vc) {
		this.voteCollection = vc;
	}

	public void setOption (Option o) {
		this.option = o;
	}

	private Poll poll;
	public void setPoll(Poll p) {
		poll = p;
	}

	private LocaleGetter localegetter;

	public void setLocaleGetter(LocaleGetter localegetter) {
		this.localegetter = localegetter;
	}
	private TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}


	public Poll processActionAdd() {
		boolean isNew = true;
		if (poll.getPollId()!=null) {
			m_log.debug("Actualy updating poll " + poll.getPollId());
			isNew = false;
			//check for possible unchanged values
			m_log.debug(" newPoll is " + poll.getText()+ " while poll text is " + poll.getText());
			
			
			if (poll.getText().equals("") && poll.getText()!=null)
				poll.setText(poll.getText());

			if (poll.getDetails().equals("") && poll.getDetails() != null)
				poll.setDetails(poll.getDetails());
		}

		
		m_log.debug("Poll opens: " + poll.getVoteOpen() + " and closes: " + poll.getVoteClose());
		if (poll.getVoteOpen().after(poll.getVoteClose())) {
			m_log.debug("Poll closes before it opens");

			messages.addMessage(new TargettedMessage("close_before_open"));
			throw new  IllegalArgumentException("close_before_open");
		}

		if (poll.getMinOptions() > poll.getMaxOptions()) {
			m_log.debug("Min options greater than max options");
			messages.addMessage(new TargettedMessage("min_greater_than_max"," min greater than max"));
			throw new  IllegalArgumentException("min_greater_than_max");
		}

		if (poll.getText().trim() == null || poll.getText().length() == 0 ) {
			m_log.debug("Poll question is Empty!");
			messages.addMessage(new TargettedMessage("error_no_text","no text"));
			throw new  IllegalArgumentException("error_no_text");

		}


		poll.setDetails(PollUtils.cleanupHtmlPtags(FormattedText.processFormattedText(poll.getDetails(), new StringBuilder())));
		m_log.debug("about to save poll " + poll);
		manager.savePoll(poll);

		m_log.info("Poll saved with id of " + poll.getPollId());
		//if this is not a new poll populate the options list
		if (!isNew)
			poll.setOptions(manager.getOptionsForPoll(poll));
		
		voteBean.poll = poll;
		
		return poll;
	}


	public void processActionDelete() {

		for (int i = 0; i < deleteids.length; i ++) {
			Poll todelete = (Poll) manager.getPollById(Long.valueOf(deleteids[i].longValue()));
			try {
				manager.deletePoll(todelete);
			}
			catch(PermissionException e){
				m_log.error(" Permission Error" + e);
			}
		}

	}

	public VoteCollection processActionVote() {
		//m_log.info("got a vote! with " + optionsSelected.length + "options");




		m_log.debug("vote is on poll " + voteCollection.getPollId());
		Poll poll = manager.getPollById(voteCollection.getPollId());

		//need to check if the user hasn't already voted on this poll
		//pollvoteManger.userHasVoted(poll.getPollId();



		VoteCollection votes = voteCollection;
		m_log.info("got vote collection with id " + votes.getId());

		List options = new ArrayList();

		if (votes.getOptionsSelected() == null && votes.getOption() != null) {
			options.add(votes.getOption());
		} else if (votes.getOptionsSelected() != null){
			for (int i = 0;i < votes.getOptionsSelected().length;i++){
				options.add(votes.getOptionsSelected()[i]);
			}
		}

		//if options list is empty this may be a spoiled vote
		if (options.size()==0 && poll.getMinOptions()==0) {
			//to do we need to map to somthing special
			m_log.warn("this is a spoiled vote");
			options.add("0");
		}
		
		List<Vote> votesToSave = new ArrayList<Vote>();
		for (int i = 0; i < options.size(); i++){
			//create a new vote
			m_log.debug("this vote is for option " + options.get(i));
			Option opt = new Option(Long.valueOf((String)options.get(i)));  
			Vote vote = new Vote(poll,opt,votes.getId());
			if (vote.getIp() == null) {
				m_log.warn("IP is null");
				vote.setIp("Nothing");
		}
			votesToSave.add(vote);
		}
		
		pollVoteManager.saveVoteList(votesToSave);

		voteBean.voteCollection = votes;
		m_log.debug("Votes saved about to return");
		return votes;
	}

	public String proccessActionAddOption() {
		
		if ("cancel".equals(submissionStatus))
			return "cancel";
		
		m_log.debug("adding option with text " + option.getOptionText());
		if (option.getOptionText() == null || option.getOptionText().trim().length()==0) {
			//errors.reject("vote_closed","vote closed");
			// return null;
		}
		StringBuilder sb = new StringBuilder();
		option.setOptionText(FormattedText.processFormattedText(option.getOptionText(), sb, true, true));

		String text = option.getOptionText();
		text = PollUtils.cleanupHtmlPtags(text);

		option.setOptionText(text);
		manager.saveOption(option);
		m_log.debug("Succesuly save option with id" + option.getId());

		//voteBean.poll = manager.getPollById(option.getPollId());

		
		if ("option".equals(submissionStatus))
			return "option";
		else 
			return "Saved";

		
	}

	public Poll proccessActionDeleteOption() {
		m_log.info("about to delete option " + option.getId());
		Long pollId = option.getPollId();
		manager.deleteOption(option);

		//we now need to update the poll object in memory
		voteBean.setPoll(manager.getPollById(pollId));

		return manager.getPollById(pollId);

	}
	public String cancel() {
		return "cancel";
	}



}
