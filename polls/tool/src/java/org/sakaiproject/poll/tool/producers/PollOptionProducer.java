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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.tool.constants.NavigationConstants;
import org.sakaiproject.poll.tool.params.OptionViewParameters;
import org.sakaiproject.poll.tool.params.PollViewParameters;
import org.sakaiproject.poll.tool.params.VoteBean;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
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
public class PollOptionProducer implements ViewComponentProducer,ViewParamsReporter,NavigationCaseReporter, ActionResultInterceptor {

	public static final String VIEW_ID = "pollOption";
	private VoteBean voteBean;


	private MessageLocator messageLocator;
	private LocaleGetter localeGetter;
	
	
	public String getViewID() {

		return VIEW_ID;
	}

	public void setVoteBean(VoteBean vb){
		this.voteBean = vb;
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

	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}


	private TextInputEvolver richTextEvolver;
	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
		this.richTextEvolver = richTextEvolver;
	}

	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker arg2) {

		String locale = localeGetter.get().toString();
        Map<String, String> langMap = new HashMap<String, String>();
        langMap.put("lang", locale);
        langMap.put("xml:lang", locale);

		UIOutput.make(tofill, "polls-html", null).decorate(new UIFreeAttributeDecorator(langMap));

		// Menu links
		UIBranchContainer actions = UIBranchContainer.make(tofill,"actions:",Integer.toString(0));
		UIInternalLink.make(actions, NavigationConstants.NAVIGATE_MAIN, UIMessage.make(NavigationConstants.NAVIGATE_MAIN_MESSAGE), new SimpleViewParameters(PollToolProducer.VIEW_ID));
		if (this.isAllowedPollAdd()) {
			UIInternalLink.make(actions, NavigationConstants.NAVIGATE_ADD, UIMessage.make(NavigationConstants.NAVIGATE_ADD_MESSAGE), new PollViewParameters(AddPollProducer.VIEW_ID, "New 0"));
		}
		if (this.isSiteOwner()) {
			UIInternalLink.make(actions, NavigationConstants.NAVIGATE_PERMISSIONS, UIMessage.make(NavigationConstants.NAVIGATE_PERMISSIONS_MESSAGE), new SimpleViewParameters(PermissionsProducer.VIEW_ID));
		}

		if (tml.size() > 0) {
			for (int i = 0; i < tml.size(); i ++ ) {
				UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:");
				String output;
				if (tml.messageAt(i).args != null ) {	    		
					output = messageLocator.getMessage(tml.messageAt(i).acquireMessageCode(),tml.messageAt(i).args[0]);
				} else {
					output = messageLocator.getMessage(tml.messageAt(i).acquireMessageCode());
				}
				UIOutput.make(errorRow,"error", output);
			}
		}



		
	

		Option option = null;
		Poll poll = null;
		OptionViewParameters aivp = (OptionViewParameters) viewparams;
		boolean newOption = false;
		if(aivp.id != null) {
			log.debug("got a paramater with id: " + Long.valueOf(aivp.id));
			// passed in an id so we should be modifying an item if we can find it
			option = pollListManager.getOptionById(Long.valueOf(aivp.id));
			// SAK-14702 : Bugfix
			poll = pollListManager.getPollById(option.getPollId());
			UIMessage.make(tofill,"new-option-title","edit_option_title");
		} else {
			option = new Option();
			if (aivp.pollId != null) {
				option.setPollId(Long.valueOf(aivp.pollId));
				poll = pollListManager.getPollById(Long.valueOf(aivp.pollId));
			} else { 
				option.setPollId(voteBean.getPoll().getPollId());
			}

			newOption = true;
			UIMessage.make(tofill,"new-option-title","new_option_title");
		}

		if (poll == null) {
			log.warn("no poll found");
			return;
		}
		

		UIOutput.make(tofill,"poll_text",poll.getText());
		UIOutput.make(tofill,"poll-question",messageLocator.getMessage("new_poll_question"));
		UIForm form = UIForm.make(tofill,"opt-form");

		//UIOutput.make(form,"option-label",messageLocator.getMessage("new_poll_option"));


		if (option.getText() == null)
			option.setText("");

		
		if (!externalLogic.isMobileBrowser())
		{
			// show WYSIWYG editor
		UIInput optText = UIInput.make(form,"optText:","#{option.text}",option.getText());
		richTextEvolver.evolveTextInput(optText);
		}
		else
		{
			// do not show WYSIWYG editor in the mobile view
			UIInput.make(form,"optText_mobile","#{option.text}",option.getText());
		}

		form.parameters.add(new UIELBinding("#{option.pollId}",
				poll.getPollId()));
		UICommand save =  UICommand.make(form, "submit-new-option", messageLocator.getMessage("new_poll_submit"),
		"#{pollToolBean.proccessActionAddOption}");
		save.parameters.add(new UIELBinding("#{pollToolBean.submissionStatus}", "save"));
		if (newOption) {
			UICommand saveAdd = UICommand.make(form, "submit-option-add", messageLocator.getMessage("new_poll_saveoption"),
			"#{pollToolBean.proccessActionAddOption}");
			saveAdd.parameters.add(new UIELBinding("#{pollToolBean.submissionStatus}", "option"));
		} else {
			form.parameters.add(new UIELBinding("#{option.optionId}",
					option.getOptionId()));
		}
		UICommand cancel = UICommand.make(form, "cancel",messageLocator.getMessage("new_poll_cancel"),"#{pollToolBean.cancel}");
		cancel.parameters.add(new UIELBinding("#{option.status}", "cancel"));

	}


	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase("save", new PollViewParameters(AddPollProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", null));
		return togo;
	}

	public ViewParameters getViewParameters() {
		return new OptionViewParameters();
	}


	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
		log.debug("checking IntercetpActionResult(");

		
		if (result.resultingView instanceof OptionViewParameters) {
			OptionViewParameters optvp = (OptionViewParameters) result.resultingView;
			log.debug("OptionViewParams: "  + optvp.id + " : " + optvp.pollId);
			String retVal = (String) actionReturn;
			log.debug("retval is " + retVal);
			if (retVal == null) {
				return;
			}
			
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

    private boolean isAllowedPollAdd() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getCurrentLocationReference());
    }

    private boolean isSiteOwner() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation("site.upd", externalLogic.getCurrentLocationReference());
    }

}

