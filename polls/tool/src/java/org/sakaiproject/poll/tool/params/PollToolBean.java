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
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.exception.PermissionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import uk.org.ponder.localeutil.LocaleGetter;



public class PollToolBean {
  /** A holder for the single new task that may be in creation **/
  public Poll newPoll = new Poll();
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
 
  private LocaleGetter localegetter;

  public void setLocaleGetter(LocaleGetter localegetter) {
    this.localegetter = localegetter;
  }
  public String processActionAdd() {
	  
	  
	  

	  Poll poll = null;
	  boolean isNew = true;
	  if (newPoll.getPollId()!=null) {
		  m_log.debug("Actualy updating poll " + newPoll.getPollId());
		  poll = manager.getPollById(newPoll.getPollId());
		  isNew = false;
		  //check for possible unchanged values
		  m_log.debug(" newPoll test is " + newPoll.getText()+ " while poll text is " + poll.getText());
		  if (newPoll.getText().equals("") && poll.getText()!=null)
			  newPoll.setText(poll.getText());
		  
		  if (newPoll.getDetails().equals("") && poll.getDetails() != null)
			  newPoll.setDetails(poll.getDetails());
	  }
	  
	  SimpleDateFormat yearf = new SimpleDateFormat("yyyy");
	  if (openYear == null && poll!=null) {
		  openYear = yearf.format(poll.getVoteOpen());
	  } else if (openYear == null) {
		  openYear = yearf.format(new Date());
	  }
	  if (closeYear == null && poll!=null) {
		  closeYear = yearf.format(poll.getVoteClose());
	  } else if (closeYear == null) {
		  closeYear = yearf.format(new Date());
	  }
	  
	  SimpleDateFormat monthf = new SimpleDateFormat("M",localegetter.get());
	  if (openMonth == null && poll!=null) {
		  openMonth = monthf.format(poll.getVoteOpen());
	  } else if (openMonth == null) {
		  openMonth = monthf.format(new Date());
	  }
	  if (closeMonth == null && poll!=null) {
		  closeMonth = monthf.format(poll.getVoteClose());
	  } else if (closeMonth == null) {
		  closeMonth = monthf.format(new Date());
	  }
	  
	  SimpleDateFormat dayf = new SimpleDateFormat("d");
	  if (openDay == null && poll!=null) {
		  openDay = dayf.format(poll.getVoteOpen());
	  } else if (openDay == null) {
		  openDay = dayf.format(new Date());
	  }
	  if (closeDay == null && poll!=null) {
		  closeDay = dayf.format(poll.getVoteClose());
	  } else if (closeDay == null) {
		  closeDay = dayf.format(new Date());
	  }
	  
	  
	  SimpleDateFormat hoursf = new SimpleDateFormat("h");
	  if (openHour == null && poll!=null) {
		  openHour = hoursf.format(poll.getVoteOpen());
	  } else if (openHour == null) {
		  openHour = hoursf.format(new Date());
	  }
	  if (closeHour == null && poll!=null) {
		  closeHour = hoursf.format(poll.getVoteClose());
	  } else if (closeHour == null) {
		  closeHour = hoursf.format(new Date());
	  }
	  
	  SimpleDateFormat minf = new SimpleDateFormat("m");
	  if (openMinutes == null && poll!=null) {
		  openMinutes = minf.format(poll.getVoteOpen());
	  } else if (openMinutes == null) {
		  openMinutes = minf.format(new Date());
	  }
	  if (closeMinutes == null && poll!=null) {
		  closeMinutes = minf.format(poll.getVoteClose());
	  } else if (closeMinutes == null) {
		  closeMinutes = minf.format(new Date());
	  }
	  
	  SimpleDateFormat amf = new SimpleDateFormat("a");
	  if (openAmPm == null && poll!=null) {
		  openAmPm = amf.format(poll.getVoteOpen());
	  } else if (openAmPm == null) {
		  openAmPm = amf.format(new Date());
	  }
	  if (closeAmPm == null && poll!=null) {
		  closeAmPm = amf.format(poll.getVoteClose());
	  } else if (closeAmPm == null) {
		  closeAmPm = amf.format(new Date());
	  }
	  
	  String openString =   openYear+ "/" + openMonth + "/" + openDay  + " "	  
	  	+ openHour  + ":" + openMinutes  + " " + openAmPm;
	  
	  String closeString =   closeYear  + "/"  + closeMonth + "/" + closeDay + " "	  
	  	+ closeHour  + ":" + closeMinutes  + " " + closeAmPm;	  
	  
	  
	  
	  //conver to dates
	  String strFormat = "yyyy/M/d h:mm a";
	                      //2006/12/26 10:00 PM
	  DateFormat myDateFormat = new SimpleDateFormat(strFormat);
	  Date openDate = null;
	  Date closeDate = null;
	  try {
		  openDate = myDateFormat.parse(openString);
		  closeDate = myDateFormat.parse(closeString);
	  }
	  catch (Exception e)
	  {
		 m_log.error("error converting date" + e);
	  }
	  
	  
	  	  
	  
	  newPoll.setLimitVoting(true);
 
	  if (poll == null)
		  newPoll.setCreationDate(new Date());
	  else 
		  newPoll.setCreationDate(poll.getCreationDate());
      
	  newPoll.setVoteOpen(openDate);
      newPoll.setVoteClose(closeDate);
      newPoll.setSiteId(siteID);
      
      m_log.debug("about to save poll " + newPoll);
      manager.savePoll(newPoll);
     
      m_log.info("Poll saved with id of " + newPoll.getPollId());
      
      voteBean.poll = newPoll;
    
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
	  m_log.info("got vote collexction with id " + votes.getId());
	  
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
	 if (option.getOptionText() == null || option.getOptionText().length()==0) {
		 m_log.error("OptionText is empty");
		 //errors.reject("vote_closed","vote closed");
		// return null;
	 }
		 
	 
	 manager.saveOption(option);
	 m_log.info("Succesuly save option with id" + option.getId());
	
	 voteBean.poll = manager.getPollById(option.getPollId());
	
	 
	 if (submissionStatus.equals("option"))
		 return "option";
	 else 
		 return "save";
	  
		 
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
