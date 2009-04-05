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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.tool.producers;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.tool.params.PollViewParameters;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ResultsProducer implements ViewComponentProducer,NavigationCaseReporter,ViewParamsReporter {

	public static final String VIEW_ID = "voteResults";

	
	private PollListManager pollListManager;
	private PollVoteManager pollVoteManager;
	private MessageLocator messageLocator;
	private LocaleGetter localegetter;
		  

	private static Log m_log  = LogFactory.getLog(ResultsProducer.class);




	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}





	public void setMessageLocator(MessageLocator messageLocator) {

		this.messageLocator = messageLocator;
	}

	
	public void setPollListManager(PollListManager pollListManager) {
		this.pollListManager = pollListManager;
	}

	
	public void setLocaleGetter(LocaleGetter localegetter) {
		this.localegetter = localegetter;
	}
	public void setPollVoteManager(PollVoteManager pvm){
		this.pollVoteManager = pvm;
	}


    private ExternalLogic externalLogic;    
    public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		PollViewParameters ecvp = (PollViewParameters) viewparams;

		String strId = ecvp.id;
		m_log.debug("got id of " + strId);
		Poll poll = pollListManager.getPollById(Long.valueOf(strId));


		//get the number of votes
		int voters = pollVoteManager.getDisctinctVotersForPoll(poll);
		//Object[] args = new Object[] { Integer.valueOf(voters).toString()};
		if (poll.getMaxOptions()>1)
			UIOutput.make(tofill,"poll-size",messageLocator.getMessage("results_poll_size",Integer.valueOf(voters).toString()));

		m_log.debug(voters + " have voted on this poll");

		UIOutput.make(tofill,"question",poll.getText());
		m_log.debug("got poll " + poll.getText());
		List<Option> pollOptions = poll.getPollOptions();

		m_log.debug("got a list of " + pollOptions.size() + " options");
		//appeng an option for no votes
		if (poll.getMinOptions()==0) {
			Option noVote = new Option(Long.valueOf(0));
			noVote.setOptionText(messageLocator.getMessage("result_novote"));
			noVote.setPollId(poll.getPollId());
			pollOptions.add(noVote);
		}

		List<Vote> votes = pollVoteManager.getAllVotesForPoll(poll);
		int totalVotes= votes.size();
		m_log.debug("got " + totalVotes + " votes");
		List<CollatedVote> collation = new ArrayList<CollatedVote>();

		for (int i=0; i <pollOptions.size(); i++ ) {
			CollatedVote collatedVote = new CollatedVote();
			Option option = (Option) pollOptions.get(i);
			m_log.debug("collating option " + option.getOptionId());
			collatedVote.setoptionId(option.getOptionId());
			collatedVote.setOptionText(option.getOptionText());
			for (int q=0; q <votes.size(); q++ ) {
				Vote vote = (Vote)votes.get(q);
				if (vote.getPollOption().equals(option.getOptionId())){
					m_log.debug("got a vote for option " + option.getOptionId());
					collatedVote.incrementVotes();

				}

			}
			collation.add(collatedVote);

		}

		UILink.make(tofill,"answers-title",messageLocator.getMessage("results_answers_title"), "#");
		UILink.make(tofill,"answers-count",messageLocator.getMessage("results_answers_numbering"), "#");

		//output the votes
		for (int i=0; i <collation.size(); i++ ) {
			CollatedVote cv = (CollatedVote)collation.get(i);
			UIBranchContainer resultRow = UIBranchContainer.make(tofill,"answer-row:",cv.getoptionId().toString());
			UIVerbatim.make(resultRow,"answer-option",cv.getOptionText());
			UIOutput.make(resultRow,"answer-count", Integer.valueOf(i+1).toString());
			UIOutput.make(resultRow,"answer-numVotes",Long.valueOf(cv.getVotes()).toString());
			m_log.debug("about to do the calc: (" + cv.getVotes()+"/"+ totalVotes +")*100");
			double percent = (double)0;
			if (totalVotes>0  && poll.getMaxOptions() == 1)
				percent = ((double)cv.getVotes()/(double)totalVotes); //*(double)100;
			else if (totalVotes>0  && poll.getMaxOptions() > 1)
				percent = ((double)cv.getVotes()/(double)voters); //*(double)100;
			else
				percent = (double) 0;


			m_log.debug("result is "+ percent);
			NumberFormat nf = NumberFormat.getPercentInstance(localegetter.get());
			UIOutput.make(resultRow,"answer-percVotes", nf.format(percent));

		}
		UIOutput.make(tofill,"votes-total",Integer.valueOf(totalVotes).toString());
		if (totalVotes > 0 && poll.getMaxOptions() == 1)
			UIOutput.make(tofill,"total-percent","100%");

		//the cancel button
		UIForm form = UIForm.make(tofill,"actform");
		UICommand.make(form,"cancel",messageLocator.getMessage("results_cancel"),"#{pollToolBean.cancel}"); 
		externalLogic.postEvent("poll.viewResult", "poll/site/" + externalLogic.getCurrentLocationId() +"/poll/" +  poll.getPollId(), false);


	}

	public List<NavigationCase> reportNavigationCases() {
		
		List<NavigationCase> togo = new ArrayList<NavigationCase>(); // Always navigate back to this view.
		togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		return togo;
	}	
	public ViewParameters getViewParameters() {
		return new PollViewParameters();

	}



	private static class CollatedVote {
		private Long optionId ;
		private String optionText;
		private int votes;

		public CollatedVote() {
			this.votes=0;
		}
		public void setoptionId(Long val){
			this.optionId = val;
		}

		public Long getoptionId(){
			return this.optionId;
		}

		public void setOptionText(String t){
			this.optionText = t;
		}
		public String getOptionText(){
			return this.optionText;
		}

		public void setVotes(int i){
			this.votes = i;
		}
		public int getVotes(){
			return this.votes;
		}
		public void incrementVotes(){
			this.votes++;
		}

	}
}
