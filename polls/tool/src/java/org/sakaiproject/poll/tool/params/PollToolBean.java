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

package org.sakaiproject.poll.tool.params;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.model.VoteCollection;
import org.sakaiproject.poll.tool.util.OptionsFileConverterUtil;
import org.sakaiproject.poll.util.PollUtils;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

@Slf4j
public class PollToolBean {

	public static final String
		HANDLE_DELETE_OPTION_DO_NOTHING = "do-nothing",
		HANDLE_DELETE_OPTION_RETURN_VOTES = "return-votes";
	
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
	
	//how to handle orphaned votes when deleting an option
	private String handleOrphanVotes;

	@Setter
	public Map<String, MultipartFile> multipartMap;

	public Map perms = null;
	public void setRoleperms(Map perms)
	{
		this.perms = perms;
	} 

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

	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
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

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	public Poll processActionAdd() {
		boolean isNew = true;
		if (poll.getPollId()!=null) {
			log.debug("Actualy updating poll " + poll.getPollId());
			Poll existingPoll = manager.getPollById(poll.getPollId(), false);
			isNew = false;
			//check for possible unchanged values
			log.debug("newPoll is {} while poll text is {}", poll.getText(), existingPoll.getText());
			

			if (poll.getCreationDate() == null) {
				poll.setCreationDate(existingPoll.getCreationDate());
			}
		} else {
			poll.setCreationDate(new Date());
		}

		
		log.debug("Poll opens: " + poll.getVoteOpen() + " and closes: " + poll.getVoteClose());
		if (poll.getVoteOpen().after(poll.getVoteClose())) {
			log.debug("Poll closes before it opens");

			messages.addMessage(new TargettedMessage("close_before_open"));
			throw new  IllegalArgumentException("close_before_open");
		}

		if (poll.getMinOptions() > poll.getMaxOptions()) {
			log.debug("Min options greater than max options");
			messages.addMessage(new TargettedMessage("min_greater_than_max"," min greater than max"));
			throw new  IllegalArgumentException("min_greater_than_max");
		}

		if (poll.getText().trim() == null || poll.getText().length() == 0 ) {
			log.debug("Poll question is Empty!");
			messages.addMessage(new TargettedMessage("error_no_text","no text"));
			throw new  IllegalArgumentException("error_no_text");

		}


		poll.setDetails(PollUtils.cleanupHtmlPtags(externalLogic.processFormattedText(poll.getDetails(), new StringBuilder())));
		log.debug("about to save poll " + poll);
		manager.savePoll(poll);

		log.info("Poll saved with id of " + poll.getPollId());
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
			catch(SecurityException e){
				log.error(" Permission Error" + e);
			}
		}

	}

	public void processActionResetVotes() {

		for (Long pollId : deleteids) {
			Poll poll = manager.getPollById(pollId);
			List<Vote> votes = pollVoteManager.getAllVotesForPoll(poll);
			if (manager.userCanDeletePoll(poll)){
				pollVoteManager.deleteAll(votes);
			}
		}
	}

	public VoteCollection processActionVote() {
		//m_log.info("got a vote! with " + optionsSelected.length + "options");




		log.debug("vote is on poll " + voteCollection.getPollId());
		Poll poll = manager.getPollById(voteCollection.getPollId());

		//need to check if the user hasn't already voted on this poll
		//pollvoteManger.userHasVoted(poll.getPollId();



		VoteCollection votes = voteCollection;
		log.info("got vote collection with id " + votes.getId());

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
			log.warn("this is a spoiled vote");
			options.add("0");
		}
		
		List<Vote> votesToSave = new ArrayList<Vote>();
		for (int i = 0; i < options.size(); i++){
			//create a new vote
			log.debug("this vote is for option " + options.get(i));
			Option opt = new Option(Long.valueOf((String)options.get(i)));  
			Vote vote = new Vote(poll,opt,votes.getId());
			if (vote.getIp() == null) {
				log.warn("IP is null");
				vote.setIp("Nothing");
		}
			votesToSave.add(vote);
		}
		
		pollVoteManager.saveVoteList(votesToSave);

		voteBean.voteCollection = votes;
		log.debug("Votes saved about to return");
		return votes;
	}

	public String proccessActionAddOption() {
		
		if ("cancel".equals(submissionStatus))
			return "cancel";
		
		log.debug("adding option with text " + option.getText());
		if (option.getText() == null || option.getText().trim().length()==0) {
			//errors.reject("vote_closed","vote closed");
			// return null;
		}
		StringBuilder sb = new StringBuilder();
		option.setText(externalLogic.processFormattedText(option.getText(), sb, true, true));

		String text = PollUtils.cleanupHtmlPtags(option.getText());
		option.setText(text);
		option.setOptionOrder(manager.getOptionsForPoll(option.getPollId()).size());
		manager.saveOption(option);
		log.debug("Succesuly save option with id" + option.getId());

		String action = "Saved";
		switch(submissionStatus) {
			case "option":
				action = "option";
				break;
			case "batch":
				action = "batch";
				break;
			default:
				break;
		}
		return action;
	}

	public String processActionAddOptionBatch() {
		log.debug("processActionAddOptionBatch");
		if ("cancel".equals(submissionStatus)){
			log.debug("processActionAddOptionBatch: cancel");
			return "cancel";
		}

		Long pollId = option.getPollId();
		if (pollId == null) {
			messages.addMessage(new TargettedMessage("error_batch_options","no file"));
			throw new IllegalArgumentException("error_batch_options");
		}

		MultipartFile file = null;

		if (multipartMap.size() > 0) {
			log.debug("The multipartMap is not empty so retrieving the file.");
			// 	user specified a file, create it
			file = multipartMap.values().iterator().next();
			//Clear the map to get new file items.
			multipartMap.clear();
		}

		boolean fileError = false;
		if (file != null) {
			log.debug("File uploaded successfully with contentType {}, size {} and name {}", file.getContentType(), file.getSize(), file.getOriginalFilename());
			try{
				switch(file.getContentType()){
					case "application/octet-stream":
					case "text/plain":
					case "text/csv":
					case "application/vnd.ms-excel":
					case "application/msexcel":
					case "application/x-msexcel":
					case "application/x-ms-excel":
					case "application/x-excel":
					case "application/x-dos_ms_excel":
					case "application/xls":
					case "application/x-xls":
					case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
						List<String> optionsList = OptionsFileConverterUtil.convertInputStreamToOptionList(file.getInputStream());
						for(String optionString : optionsList){
							Option newOption = new Option();
							newOption.setPollId(pollId);
							newOption.setText(PollUtils.cleanupHtmlPtags(optionString));
							newOption.setOptionOrder(manager.getOptionsForPoll(pollId).size());
							manager.saveOption(newOption);
							log.debug("Option with id {} successfully saved.", newOption.getId());
						}
						break;
					default:
						log.warn("File mimetype not accepted {}.", file.getContentType());
						throw new IOException();
				}
			} catch(Exception ex) {
				log.warn("Error converting the input file into options.", ex);
				fileError = true;
			}
		} else {
			log.warn("The uploaded file object is null.");
			fileError = true;
		}

		if (fileError) {
			log.debug("There was a problem processing the file.");
			messages.addMessage(new TargettedMessage("error_batch_options", "no file"));
			throw new IllegalArgumentException("error_batch_options");
		}

		log.debug("processActionAddOptionBatch: Saved");
		return "Saved";
	}

	public Poll proccessActionDeleteOption() {
		log.info("about to delete option " + option.getId());
		Long pollId = option.getPollId();
		
		List<Vote> votes = (List<Vote>) pollVoteManager.getAllVotesForOption(option);
		
		if (votes != null && votes.size() > 0) {
			//if the option had votes, we need some special handling
			
			if (HANDLE_DELETE_OPTION_RETURN_VOTES.equals(getHandleOrphanVotes())) {
				Set<String> userEids = new HashSet<String>();
				
				//hard-delete the option. It will no longer have any votes
				manager.deleteOption(option);
				
				for (Vote vote : votes) {
					String userId = vote.getUserId();
					
					if (userId != null) {
						String userEid = externalLogic.getUserEidFromId(userId);
						userEids.add(userEid);
					}
					
					pollVoteManager.deleteVote(vote);
				}
				
				//send the notification to affected users
				sendOptionDeletedNotification(userEids.toArray(new String[0]));
				
			} else {
				//soft delete the option. we still want it to show up in the results
				
				/*
				 * The option in this bean is mapped by parameters
				 * given to the form by the producer. This seems a bit
				 * shakey. 'persistentOption' will be a fresh and full
				 * copy of the option from the DB. This should be a safer
				 * "save" operation. -bv
				 */
				Option persistentOption = manager.getOptionById(option.getOptionId());
				manager.deleteOption(persistentOption, true);
			}
		} else {
			//if the option didn't have any votes, just blow it away
			manager.deleteOption(option);
		}
		
		//we now need to update the poll object in memory
		Poll poll = manager.getPollById(pollId);
		voteBean.setPoll(poll);
		return poll;
		
	}

	public String cancel() {
		return "cancel";
	}
	
	public void setHandleOrphanVotes(String handleOrphanVotes) {
		this.handleOrphanVotes = handleOrphanVotes;
	}
  
	public String getHandleOrphanVotes() {
		return this.handleOrphanVotes;
	}

	private void sendOptionDeletedNotification(String[] userEids) {
		Poll poll = manager.getPollById(option.getPollId());
		String siteTitle = externalLogic.getSiteTile(poll.getSiteId());
		externalLogic.notifyDeletedOption(Arrays.asList(userEids), siteTitle, poll.getText());
	}

}
