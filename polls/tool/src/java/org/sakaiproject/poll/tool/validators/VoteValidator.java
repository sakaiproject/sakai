/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.poll.tool.validators;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.VoteCollection;

@Slf4j
public class VoteValidator implements Validator {

    private PollVoteManager pollVoteManager;
    private PollListManager manager;
    
    
    public void setPollListManager(PollListManager manager) {
      this.manager = manager;
    }
    
	public void setPollVoteManager(PollVoteManager pvm){
		this.pollVoteManager = pvm;
	}

	private ExternalLogic externalLogic;    
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}	
    
	public boolean supports(Class clazz) {
		// TODO Auto-generated method stub
		return clazz.equals(VoteCollection.class);
	}

	public void validate(Object obj, Errors errors) {

		VoteCollection votes = (VoteCollection)obj;
		log.debug("we are validating a vote collection of " + votes + " for poll " + votes.getPollId());
	
		if (votes.getSubmissionStatus().equals("cancel"))
			return;
		
		
	//get the poll
	Poll poll = manager.getPollById(votes.getPollId());
	log.debug("this is a vote for " + poll.getText());
	List<String> options = new ArrayList<String>();
	
	//is the poll open?
	if (!(poll.getVoteClose().after(new Date()) && new Date().after(poll.getVoteOpen()))) {
		log.warn("poll is closed!");
		if (!errors.hasFieldErrors("vote_closed")) {
			errors.reject("vote_closed","vote closed");
		} else {
			errors.reject("vote_closed.voteCollections","vote closed");
		}
		return;
	}
		
	//does the user have permission to vote
	if (!externalLogic.isUserAdmin()) {
		if (!pollVoteManager.pollIsVotable(poll))
		{
			log.error("attempt to vote in " + poll.getReference() + " by unauthorized user" );
			if (!errors.hasFieldErrors("vote_noperm")) {
				errors.reject("vote_noperm","no permissions");
			}
			else {
				errors.reject("vote_noperm.voteCollection","no permissions");
			}
			
			return;
		}
	}
	
	if (votes.getOptionsSelected() == null && votes.getOption() == null && poll.getMinOptions()>0) {
		log.debug("there seems to be no vote on this poll");  
		String errStr = Integer.valueOf(poll.getMinOptions()).toString();
		if (!errors.hasFieldErrors("error_novote")) {
			errors.reject("error_novote", new Object[] {errStr}, "no vote");
		}
		else {
			errors.reject("error_novote.voteCollection", new Object[] {errStr}, "no vote");
		}
		return;
	} else if (votes.getOptionsSelected() == null && votes.getOption() == null && poll.getMinOptions()==0) {
		//to do we need to map to somthing special
		options.add("0");
	}
	
	
	if (votes.getOptionsSelected() == null && votes.getOption() != null) {
		  options.add(votes.getOption());
	} else if (votes.getOptionsSelected() != null){
		for (int i = 0;i < votes.getOptionsSelected().length;i++){
			options.add(votes.getOptionsSelected()[i]);
		}
	}
	  
	  log.debug("options selected is " + options.size());
	  // the exact choise case
	  
	  if (pollVoteManager.userHasVoted(poll.getPollId()) && poll.getLimitVoting()) {
		  	if (!errors.hasFieldErrors("vote_hasvoted")) {
		  		errors.reject("vote_hasvoted","has voted");
		  	}
		  	else {
		  		errors.reject("vote_hasvoted.voteCollection","has voted");
		  	}
			return;
		}
	  
	  if (poll.getMaxOptions() == poll.getMinOptions() && options.size() != poll.getMaxOptions()){
		  log.debug("exact match failure!");
		  String errStr = Integer.valueOf(poll.getMinOptions()).toString();
		  errors.reject("error_exact_required", new Object[] {errStr}, "exact required");
	  }else if (options.size() > poll.getMaxOptions()) {
		  log.debug("votes are for more than allowed!");
		  String errStr = Integer.valueOf(poll.getMaxOptions()).toString();
		  errors.reject("error_tomany_votes", new Object[] {errStr}, "to many votes");
	  }else if (options.size() < poll.getMinOptions()) {
		  log.debug("votes are for fewer than required!");
		  String errStr = Integer.valueOf(poll.getMinOptions()).toString();
		  errors.reject("error_tofew_votes", new Object[] {errStr}, "to few");
	  }
	}

}
