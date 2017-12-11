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

package org.sakaiproject.poll.tool.producers;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.VoteCollection;
import org.sakaiproject.poll.tool.params.PollViewParameters;
import org.sakaiproject.poll.tool.params.VoteCollectionViewParameters;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

@Slf4j
public class PollVoteProducer implements ViewComponentProducer,ViewParamsReporter, ActionResultInterceptor,NavigationCaseReporter {

	public static final String VIEW_ID = "voteQuestion";

	private PollListManager pollListManager;
	private MessageLocator messageLocator;
	private LocaleGetter localeGetter;
	private TargettedMessageList tml;
	private PollVoteManager pollVoteManager;

	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}

	public void setPollVoteManager(PollVoteManager pvm){
		this.pollVoteManager = pvm;
	}

	public void setMessageLocator(MessageLocator messageLocator) {

		this.messageLocator = messageLocator;
	}

	public void setLocaleGetter(LocaleGetter localeGetter) {
		this.localeGetter = localeGetter;
	}



	public void setPollListManager(PollListManager pollListManager) {
		this.pollListManager = pollListManager;
	}

	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}


	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIOutput.make(tofill, "poll-vote-title", messageLocator.getMessage("poll_vote_title"));

		PollViewParameters ecvp = (PollViewParameters) viewparams;


		String strId = ecvp.id;
		log.debug("got id of " + strId);
		Poll poll = pollListManager.getPollById(Long.valueOf(strId));

		if (!pollVoteManager.pollIsVotable(poll)){
			tml.addMessage(new TargettedMessage("vote_noperm.voteCollection"));
			return;
		} else {
			log.info("user: " + externalLogic.getCurrentUserId() + " can vote on poll: " + poll.getPollId());
		}

		log.debug("got poll " + poll.getText());


		//check if they can vote
		if (poll.getLimitVoting() && pollVoteManager.userHasVoted(poll.getPollId())) {
			log.warn("This user has already voted!");
			UIOutput.make(tofill, "hasErrors",messageLocator.getMessage("vote_hasvoted.voteCollection"));
			return;
		}

		String locale = localeGetter.get().toString();
        Map<String, String> langMap = new HashMap<String, String>();
        langMap.put("lang", locale);
        langMap.put("xml:lang", locale);

		UIOutput.make(tofill, "polls-html", null).decorate(new UIFreeAttributeDecorator(langMap));

		UIOutput.make(tofill,"poll-text",poll.getText());
		if (poll.getDetails() != null)
		{
			UIVerbatim.make(tofill,"poll-description",poll.getDetails());
		}

		log.debug("this poll has " + poll.getPollOptions().size()+ " options");

		UIForm voteForm = UIForm.make(tofill,"options-form",""); 

		List<Option> pollOptions = pollListManager.getVisibleOptionsForPoll(poll.getPollId());
		
		//build the options + label lists
		String[] values= new String[pollOptions.size()];
		for (int i = 0;i <pollOptions.size(); i++ ) {
			Option po = (Option)pollOptions.get(i);
			values[i]= po.getOptionId().toString();
		}


		String[] labels = new String[pollOptions.size()];
		for (int i = 0;i<  pollOptions.size(); i++ ) {
			Option po = (Option)pollOptions.get(i);
			if (po.getOptionText() != null ) {
				labels[i]= po.getOptionText();
			} else {
				log.warn("Option text is null!");
				labels[i]="null option!";
			}
		}

		//we need to deside is this a single or multiple?
		//poll.getMaxOptions()
		boolean isMultiple = false;
		if (poll.getMaxOptions()>1)
			isMultiple = true;


		UISelect radio;
		if (isMultiple)
			radio = UISelect.makeMultiple(voteForm,"optionform",values,"#{voteCollection.optionsSelected}",new String[]{});
		else
			radio = UISelect.make(voteForm,"optionform",values,"#{voteCollection.option}","");

		radio.optionnames = UIOutputMany.make(labels);
		String selectID = radio.getFullID();
		for (int i = 0;i < pollOptions.size(); i++ ) {
			Option po = (Option)pollOptions.get(i);
			log.debug("got option " + po.getOptionText() + " with id of  " + po.getId());
			UIBranchContainer radioRow = UIBranchContainer.make(voteForm,
					isMultiple ? "option:select"
							: "option:radio"						 
								,Integer.toString(i));
			UISelectChoice.make(radioRow,"option-radio",selectID,i);
			//UISelectLabel.make(radioRow,"option-label",selectID,i);
			UIVerbatim.make(radioRow,"option-label",labels[i]);
		}
		//bind some parameters
		voteForm.parameters.add(new UIELBinding("#{voteCollection.pollId}", poll.getPollId()));

		UICommand sub = UICommand.make(voteForm, "submit-new-vote",messageLocator.getMessage("vote_vote"),
		"#{pollToolBean.processActionVote}");
		sub.parameters.add(new UIELBinding("#{voteCollection.submissionStatus}", "sub"));
		UICommand cancel = UICommand.make(voteForm, "cancel",messageLocator.getMessage("vote_cancel"),"#{pollToolBean.cancel}");
		cancel.parameters.add(new UIELBinding("#{voteCollection.submissionStatus}", "cancel"));

		//o9nly show reset in !(min=max=1)
		if(!(poll.getMaxOptions()==1 && poll.getMinOptions()==1))
			UIOutput.make(voteForm, "reset", messageLocator.getMessage("vote_reset"));


	}

	public ViewParameters getViewParameters() {
		return new PollViewParameters();

	}


	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>(); // Always navigate back to this view.
		//togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		togo.add(new NavigationCase("Error", new SimpleViewParameters(VIEW_ID)));
		togo.add(new NavigationCase("Success", new VoteCollectionViewParameters(ConfirmProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		return togo;
	}

	public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {

		if (actionReturn instanceof VoteCollection) {
			VoteCollection votes = (VoteCollection) actionReturn;

			if (votes.getId() != null) {
				log.debug("got a voteCollection with id: " + votes.getId());
				result.resultingView = new VoteCollectionViewParameters(ConfirmProducer.VIEW_ID, votes.getId());
			} else {
				log.warn("no id in vote collection!");
			}
		}


	}
}
