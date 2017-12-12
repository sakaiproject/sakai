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

package org.sakaiproject.poll.tool.producers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.tool.params.OptionViewParameters;
import org.sakaiproject.poll.tool.params.PollToolBean;
import org.sakaiproject.poll.tool.params.PollViewParameters;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

@Slf4j
public class PollOptionDeleteProducer implements ViewComponentProducer, ActionResultInterceptor,ViewParamsReporter {
	public static final String VIEW_ID = "pollOptionDelete";

	private MessageLocator messageLocator;
	private LocaleGetter localeGetter;
	
	public String getViewID() {
		
		return VIEW_ID;
	}
	
	  public void setMessageLocator(MessageLocator messageLocator) {
			  
		  this.messageLocator = messageLocator;
	  }

	public void setLocaleGetter(LocaleGetter localeGetter) {
		this.localeGetter = localeGetter;
	}

	  private PollListManager pollListManager;
	  public void setPollListManager(PollListManager p){
		  this.pollListManager = p;
	  }
	  
	  private PollVoteManager pollVoteManager;
	  public void setPollVoteManager(PollVoteManager pollVoteManager) {
		  this.pollVoteManager = pollVoteManager;
	  }
	  
	@SuppressWarnings("unchecked")
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker arg2) {
		log.debug("rendering view");
		
		UIOutput.make(tofill,"confirm_delete",messageLocator.getMessage("delete_confirm"));
		
		Option option = null;
		OptionViewParameters aivp = (OptionViewParameters) viewparams;
		if(aivp.id != null) {
			log.debug("got a paramater with id: " + Long.valueOf(aivp.id));
			// passed in an id so we should be modifying an item if we can find it
			option = pollListManager.getOptionById(Long.valueOf(aivp.id));
		} 
		
		if (option == null) {
			log.error("no such option found!");
			return;
		}

		String locale = localeGetter.get().toString();
        Map<String, String> langMap = new HashMap<String, String>();
        langMap.put("lang", locale);
        langMap.put("xml:lang", locale);

		UIOutput.make(tofill, "polls-html", null).decorate(new UIFreeAttributeDecorator(langMap));
		
		UIMessage.make(tofill, "error", "delete_option_message",
				new Object[] { option.getOptionText() }
			);
		
		UIForm form = UIForm.make(tofill,"opt-form");
		UIInput.make(form,"opt-text","#{option.optionText}",option.getOptionText());

		Poll poll = pollListManager.getPollById(option.getPollId());
		Boolean showVoteHandlingOptions = Boolean.FALSE;

		if (pollVoteManager.pollIsVotable(poll)) {
			//if the poll is votable, show the vote handling options since 
			//voting is not locked.
			
			showVoteHandlingOptions = Boolean.TRUE;
		} else {
			List votes = pollVoteManager.getAllVotesForOption(option);
		
			//if the poll is not votable, but contains votes, we need to 
			//ask the user how to handle them.
			if (votes != null && votes.size() > 0) {
				showVoteHandlingOptions = Boolean.TRUE;
			}
		}
		
		if (showVoteHandlingOptions) {		
			UIBranchContainer hasVotesContainer = UIBranchContainer.make(form, "has-votes:");
			UIMessage.make(hasVotesContainer, "has-votes-text", "delete_option_has_votes");
			
			UISelect handleVotesSelect = UISelect.make(
					hasVotesContainer,
					"handle-votes",
					new String[] {
							PollToolBean.HANDLE_DELETE_OPTION_DO_NOTHING,
							PollToolBean.HANDLE_DELETE_OPTION_RETURN_VOTES
						},
					"#{pollToolBean.handleOrphanVotes}",
					PollToolBean.HANDLE_DELETE_OPTION_RETURN_VOTES
				);
			
			UISelectChoice.make(hasVotesContainer, "do-nothing",
					handleVotesSelect.getFullID(), 0);
			
			UISelectChoice.make(hasVotesContainer, "return-votes",
					handleVotesSelect.getFullID(), 1);
			
			UIMessage.make(hasVotesContainer, "do-nothing-label", "handle_delete_option_do_nothing_label");
			UIMessage.make(hasVotesContainer, "return-votes-label", "handle_delete_option_return_votes_label");
		}
		
		//populate the relevant polls information in the form parameters
		form.parameters.add(new UIELBinding("#{option.optionId}",
		           option.getOptionId()));
		form.parameters.add(new UIELBinding("#{option.id}",
		           option.getId()));
		form.parameters.add(new UIELBinding("#{option.pollId}",
		           option.getPollId()));
		 
		  UICommand saveAdd = UICommand.make(form, "submit-option-add", messageLocator.getMessage("delete_option_confirm"),
		  "#{pollToolBean.proccessActionDeleteOption}");
		  saveAdd.parameters.add(new UIELBinding("#{option.status}", "delete"));
		  
		  UICommand cancel = UICommand.make(form, "cancel",messageLocator.getMessage("new_poll_cancel"),"#{pollToolBean.cancel}");
		   cancel.parameters.add(new UIELBinding("#{option.status}", "cancel"));
		   
	}


	  public ViewParameters getViewParameters() {
		  return new OptionViewParameters();

	  }
	  public void interceptActionResult(ARIResult result,
			  ViewParameters incoming, Object actionReturn) {
		  log.debug("intercepting action results!");
		  Poll poll = null;

		  if (actionReturn != null && actionReturn instanceof Poll) {
			  poll = (Poll) actionReturn;
			  log.debug("return is poll: " + poll.getPollId());
			  result.resultingView = new PollViewParameters(AddPollProducer.VIEW_ID,poll.getPollId().toString());
		  }
		  
		  if (result.resultingView instanceof OptionViewParameters) {
				OptionViewParameters optvp = (OptionViewParameters) result.resultingView;
				
				String retVal = (String) actionReturn;
				
				String viewId = AddPollProducer.VIEW_ID;
				
				if (optvp.pollId != null) {
					
					if (! "option".equals(retVal)) {
						result.resultingView = new PollViewParameters(viewId, optvp.pollId);
					} else {
						log.debug("New option for poll: " + optvp.pollId);
						result.resultingView = new OptionViewParameters(VIEW_ID, optvp.id , optvp.pollId);
					}

				} else {
					Option option = pollListManager.getOptionById(Long.valueOf(optvp.id));
					result.resultingView = new PollViewParameters(viewId, option.getPollId().toString());
				}
			}

	  }
}
