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
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.tool.params.OptionViewParameters;
import org.sakaiproject.poll.tool.params.PollViewParameters;
import org.sakaiproject.poll.tool.params.VoteBean;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
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
public class AddPollProducer implements ViewComponentProducer,NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {
	public static final String VIEW_ID = "voteAdd";

	private PollListManager pollListManager;
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

	public void setPollListManager(PollListManager pollListManager) {
		this.pollListManager = pollListManager;
	}



	private VoteBean voteBean;
	public void setVoteBean(VoteBean vb){
		this.voteBean = vb;
	}

	private TextInputEvolver richTextEvolver;
	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
		this.richTextEvolver = richTextEvolver;
	}

	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}


	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private PollVoteManager pollVoteManager;



	public void setPollVoteManager(PollVoteManager pvm){
		this.pollVoteManager = pvm;
	}




	/*
	 * You can change the date input to accept time as well by uncommenting the lines like this:
	 * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
	 * and commenting out lines like this:
	 * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
	 * -AZ
	 */
	private FormatAwareDateInputEvolver dateevolver;
	public void setDateEvolver(FormatAwareDateInputEvolver dateevolver) {
		this.dateevolver = dateevolver;
	}



	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {



		String currentuserid = externalLogic.getCurrentUserId();

		PollViewParameters ecvp = (PollViewParameters) viewparams;
		Poll poll = null;
		boolean isNew = true;

		String locale = localeGetter.get().toString();
        Map<String, String> langMap = new HashMap<String, String>();
        langMap.put("lang", locale);
        langMap.put("xml:lang", locale);

		UIOutput.make(tofill, "polls-html", null).decorate(new UIFreeAttributeDecorator(langMap));


		

		
		UIForm newPoll = UIForm.make(tofill, "add-poll-form");
		log.debug("Poll of id: " + ecvp.id);
		if (ecvp.id == null || "New 0".equals(ecvp.id)) {
			UIMessage.make(tofill,"new_poll_title","new_poll_title");
			//build an empty poll
			log.debug("this is a new poll");
			poll = new Poll();
		} else {
			UIMessage.make(tofill,"new_poll_title","new_poll_title_edit");

			String strId = ecvp.id;
			log.debug("got id of " + strId);
			poll = pollListManager.getPollById(Long.valueOf(strId));
			voteBean.setPoll(poll);
			newPoll.parameters.add(new UIELBinding("#{poll.pollId}",
					poll.getPollId()));

			isNew = false;
		}
		
		
		if (!externalLogic.isUserAdmin() && !externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getCurrentLocationReference(),
				externalLogic.getCurrentuserReference())) {
			tml.addMessage(new TargettedMessage("new_poll_noperms"));
			return;
		}		

		//only display for exisiting polls
		if (!isNew) {
			//fill the options list
			UIBranchContainer actionBlock = UIBranchContainer.make(newPoll, "option-headers:");
			UIMessage.make(actionBlock,"options-title","new_poll_option_title");
			UIInternalLink.make(actionBlock,"option-add",UIMessage.make("new_poll_option_add"),
					new OptionViewParameters(PollOptionProducer.VIEW_ID, null, poll.getPollId().toString()));

			List<Vote> votes = pollVoteManager.getAllVotesForPoll(poll);
			if (votes != null && votes.size() > 0 ) {
				log.debug("Poll has " + votes.size() + " votes");
				UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", "0");
				UIMessage.make(errorRow,"error", "warn_poll_has_votes");

			}


			List<Option> options = pollListManager.getVisibleOptionsForPoll(poll.getPollId());
			for (int i = 0; i < options.size(); i++){
				Option o = (Option)options.get(i);
				UIBranchContainer oRow = UIBranchContainer.make(actionBlock,"options-row:",o.getOptionId().toString());
				UIVerbatim.make(oRow,"options-name",o.getOptionText());


				UIInternalLink editOption = UIInternalLink.make(oRow,"option-edit",UIMessage.make("new_poll_option_edit"),
						new OptionViewParameters(PollOptionProducer.VIEW_ID, o.getOptionId().toString()));

				editOption.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("new_poll_option_edit") +":" + FormattedText.convertFormattedTextToPlaintext(o.getOptionText())));

				UIInternalLink deleteOption = UIInternalLink.make(oRow,"option-delete",UIMessage.make("new_poll_option_delete"),
						new OptionViewParameters(PollOptionDeleteProducer.VIEW_ID,o.getOptionId().toString()));

				deleteOption.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("new_poll_option_delete") +":" + FormattedText.convertFormattedTextToPlaintext(o.getOptionText())));

			}
		}

		UIMessage.make(tofill, "new-poll-descr", "new_poll_title");
		UIMessage.make(tofill, "new-poll-question-label", "new_poll_question_label");
		UIMessage pollDescr = UIMessage.make(tofill, "new-poll-descr-label", "new_poll_descr_label");
		UIMessage.make(tofill, "new-poll-descr-label2", "new_poll_descr_label2");

		//UIMessage.make(tofill, "new-poll-open-label", "new_poll_open_label");
		//UIMessage.make(tofill, "new-poll-close-label", "new_poll_close_label");

		UIMessage.make(tofill, "new-poll-limits", "new_poll_limits");
		//UIMessage pollMin = UIMessage.make(tofill, "new-poll-min-limits", "new_poll_min_limits");
		//UIMessage pollMax =  UIMessage.make(tofill, "new-poll-max-limits", "new_poll_max_limits");


		//the form fields
		UIInput.make(newPoll, "new-poll-text", "#{poll.text}",poll.getText());
		
		
		if (!externalLogic.isMobileBrowser())
		{
			// show WYSIWYG editor
		UIInput itemDescr = UIInput.make(newPoll, "newpolldescr:", "#{poll.details}", poll.getDetails()); //$NON-NLS-1$ //$NON-NLS-2$
		richTextEvolver.evolveTextInput(itemDescr);
		UILabelTargetDecorator.targetLabel(pollDescr, itemDescr);
		}
		else
		{
			// do not show WYSIWYG editor in the mobile view
			UIInput itemDescr = UIInput.make(newPoll, "newpolldescr_mobile", "#{poll.details}", poll.getDetails()); //$NON-NLS-1$ //$NON-NLS-2$
			UILabelTargetDecorator.targetLabel(pollDescr, itemDescr);
		}

		UIInput voteOpen = UIInput.make(newPoll, "openDate-iso8601", "poll.voteOpenStr", poll.getVoteOpenStr());
		UIInput voteClose = UIInput.make(newPoll, "closeDate-iso8601", "poll.voteCloseStr", poll.getVoteCloseStr());
		//UILabelTargetDecorator.targetLabel(pollOpen, voteOpen);
		//UILabelTargetDecorator.targetLabel(pollClose, voteClose);

		/*
		 * access options
		 */
		UIMessage pollAccessLabel = UIMessage.make(newPoll,"poll_access_label","new_poll_access_label");
		UIBoundBoolean accessPublic = UIBoundBoolean.make(newPoll, "access-public", "poll.isPublic", poll.getIsPublic());
		UIMessage newPollAccessPublicLabel = UIMessage.make(newPoll, "new_poll_access_public_label", "new_poll_access_public");
		
		// SAK-25399: Do not display the public access by default
		if(!externalLogic.isShowPublicAccess()) {
			newPoll.remove(pollAccessLabel);		
			newPoll.remove(accessPublic);
			newPoll.remove(newPollAccessPublicLabel);
		}
		
		String[] minVotes = new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};
		String[] maxVotes = new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};
		UISelect min = UISelect.make(newPoll,"min-votes",minVotes,"#{poll.minOptions}",Integer.toString(poll.getMinOptions()));
		UISelect max = UISelect.make(newPoll,"max-votes",maxVotes,"#{poll.maxOptions}",Integer.toString(poll.getMaxOptions()));
	

		/*
		 * 	open - can be viewd at any time
		 * 	never - not diplayed
		 * 	afterVoting - after user has voted
		 * 	afterClosing
		 *
		 */





		String[] values = new String[] { "open", "afterVoting", "afterClosing","never"};
		String[] labels = new String[] {
				messageLocator.getMessage("new_poll_open"),
				messageLocator.getMessage("new_poll_aftervoting"),
				messageLocator.getMessage("new_poll_afterClosing"),
				messageLocator.getMessage("new_poll_never")
		};



		UISelect radioselect = UISelect.make(newPoll, "release-select", values,
				"#{poll.displayResult}", poll.getDisplayResult());

		radioselect.optionnames = UIOutputMany.make(labels);


		String selectID = radioselect.getFullID();
		//StringList optList = new StringList();
		UIMessage.make(newPoll,"add_results_label","new_poll_results_label");
		for (int i = 0; i < values.length; ++i) {

			UIBranchContainer radiobranch = UIBranchContainer.make(newPoll,
					"releaserow:", Integer.toString(i));
			UISelectChoice choice = UISelectChoice.make(radiobranch, "release", selectID, i);
			UISelectLabel lb = UISelectLabel.make(radiobranch, "releaseLabel", selectID, i);
			UILabelTargetDecorator.targetLabel(lb, choice);
		}



		log.debug("About to close the form");
		newPoll.parameters.add(new UIELBinding("#{poll.owner}",
				currentuserid));
		String siteId = externalLogic.getCurrentLocationId();
		newPoll.parameters.add(new UIELBinding("#{poll.siteId}",siteId));

		if (isNew || poll.getPollOptions() == null || poll.getPollOptions().size() == 0)	 {
			UICommand.make(newPoll, "submit-new-poll", UIMessage.make("new_poll_saveoption"),
			"#{pollToolBean.processActionAdd}");
		} else {
			UICommand.make(newPoll, "submit-new-poll", UIMessage.make("new_poll_submit"),
			"#{pollToolBean.processActionAdd}");
		}

		UICommand cancel = UICommand.make(newPoll, "cancel",UIMessage.make("new_poll_cancel"),"#{pollToolBean.cancel}");
		cancel.parameters.add(new UIELBinding("#{voteCollection.submissionStatus}", "cancel"));
		log.debug("Finished generating view");
	}


	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>(); // Always navigate back to this view.
		togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		togo.add(new NavigationCase("added", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		togo.add(new NavigationCase("option", new OptionViewParameters(PollOptionProducer.VIEW_ID, null, null)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		return togo;
	}

	public ViewParameters getViewParameters() {
		return new PollViewParameters();

	}

	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
		// OptionViewParameters outgoing = (OptionViewParameters) result.resultingView;
		// SAK-14726 : Start BugFix
		if (log.isDebugEnabled() && actionReturn != null) {
			log.debug("actionReturn is of type " + actionReturn.getClass());
		}

		if (actionReturn == null) {
			return;
		}
		
		Poll poll = null;
		
		if(actionReturn instanceof org.sakaiproject.poll.model.Poll) {
			poll = (Poll) actionReturn;
		}
		else {

			PollViewParameters ecvp = (PollViewParameters) incoming;

			if(null == ecvp || null == ecvp.id || "New 0".equals(ecvp.id)) {
				return;

			} else {

				poll = pollListManager.getPollById(Long.valueOf(ecvp.id));
			}
		}
		// SAK-14726 : End BugFix

		if (poll == null) {
			return;
		}

		log.debug("Action result got poll: " + poll.getPollId());
		log.debug("resulting view is: " + result.resultingView);

		if (poll.getPollOptions() == null || poll.getPollOptions().size() == 0) {
			result.resultingView = new OptionViewParameters(PollOptionProducer.VIEW_ID, null, poll.getPollId().toString());
		} else {
			result.resultingView = new SimpleViewParameters(PollToolProducer.VIEW_ID);
		}

		//if (poll != null && outgoing.id == null) {
		//  outgoing.id = poll.getId().toString();
		//}
	}


}


