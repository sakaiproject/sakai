/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006,2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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

 
  public String processActionAdd() {
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
	  
		if (poll.getText() == null || poll.getText().length() == 0 ) {
			m_log.debug("Poll question is Empty!");
			messages.addMessage(new TargettedMessage("error_no_text","no text"));
			throw new  IllegalArgumentException("error_no_text");

		}
		
	  
	  poll.setDetails(FormattedText.processFormattedText(poll.getDetails(), new StringBuilder()));
	  m_log.debug("about to save poll " + poll);
      manager.savePoll(poll);
     
      m_log.info("Poll saved with id of " + poll.getPollId());
      
      voteBean.poll = poll;
    
      if (!isNew) {
    	  return "added";
      } else {
    	  m_log.info("returning option");
	  	  return "option";
      }
  }
  
  
  public void processActionDelete() {
    
    	for (int i = 0; i < deleteids.length; i ++) {
    		Poll todelete = (Poll) manager.getPollById(new Long(deleteids[i].longValue()));
    		try {
    			manager.deletePoll(todelete);
    		}
	        catch(PermissionException e){
	        	m_log.error(" Permission Error" + e);
	        }
    	}

  }
  
  public String processActionVote() {
	  //m_log.info("got a vote! with " + optionsSelected.length + "options");

		  
	  
	 
	  m_log.info("vote is on poll " + voteCollection.getPollId());
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
	  
	  for (int i = 0; i < options.size(); i++){
		  //create a new vote
		 m_log.info("this vote is for option " + options.get(i));
		 Option opt = new Option(new Long((String)options.get(i)));  
		 Vote vote = new Vote(poll,opt,votes.getId());
		 if (vote.getIp() == null) {
			 m_log.warn("IP is null");
			 vote.setIp("Nothing");
		 }
		 
		 pollVoteManager.saveVote(vote);
		 voteBean.voteCollection = votes;
		 
	  }
	  m_log.debug("Votes saved about to return");
	  return "Success";
  }
  
  public String proccessActionAddOption() {
	  
	  if (submissionStatus.equals("cancel"))
		  return "cancel";
    
	 m_log.debug("adding option with text " + option.getOptionText());
	 if (option.getOptionText() == null || option.getOptionText().trim().length()==0) {
		 //errors.reject("vote_closed","vote closed");
		// return null;
	 }
	 StringBuilder sb = new StringBuilder();
	 option.setOptionText(FormattedText.processFormattedText(option.getOptionText(), sb, true, true));
	 
	 String text = option.getOptionText();
	 String check = text.substring(3, text.length());

	 //if the only <p> tag is the one at the start we will trim off the start and end <p> tags
	if (!org.sakaiproject.util.StringUtil.containsIgnoreCase(check, "<p>")) {
		//this is a single para block
		text = text.substring(3, text.length());
		text = text.substring(0,text.length()- 4);
	}
	
	//are there trailing <p>&nbsp;</p>
	String empty = "<p>&nbsp;</p>";
	while (text.length() > empty.length() && text.substring(text.length() - empty.length()).trim().equals(empty)) {
		m_log.debug("found the empty string: " + empty);
		text = text.substring(0, text.length() - empty.length()).trim();
	}
	
	
 	option.setOptionText(text);
 	manager.saveOption(option);
 	m_log.debug("Succesuly save option with id" + option.getId());
	
	//voteBean.poll = manager.getPollById(option.getPollId());
	
	 
	if (submissionStatus.equals("option"))
	 return "option";
	else 
	 return "Saved";
	  
		 
  }
  
public String proccessActionDeleteOption() {
	m_log.info("about to delete option " + option.getId());
	Long pollId = option.getPollId();
	manager.deleteOption(option);
	
	//we now need to update the poll object in memory
	voteBean.setPoll(manager.getPollById(pollId));
	
	return "success";
	
}
  public String cancel() {
	  return "cancel";
  }
  
  
  
}
