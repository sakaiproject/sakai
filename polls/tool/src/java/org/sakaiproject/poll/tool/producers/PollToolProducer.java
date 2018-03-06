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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Poll;
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
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.stringutil.StringList;

@Slf4j
public class PollToolProducer implements ViewComponentProducer,
DefaultView,NavigationCaseReporter {
	public static final String VIEW_ID = "votePolls";
	
	private PollListManager pollListManager;
	
	private MessageLocator messageLocator;
	private LocaleGetter localegetter;
	private PollVoteManager pollVoteManager;  

	private static final String NAVIGATE_ADD = "actions-add";
	private static final String NAVIGATE_PERMISSIONS = "actions-permissions";
	private static final String NAVIGATE_VOTE = "poll-vote";

	public String getViewID() {
		return VIEW_ID;
	}

    private ExternalLogic externalLogic;    
    public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
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

	private VoteBean voteBean;
	public void setVoteBean(VoteBean vb){
		this.voteBean = vb;
	}

	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		voteBean.setPoll(null);
		voteBean.voteCollection = null;

		String locale = localegetter.get().toString();
        Map<String, String> langMap = new HashMap<String, String>();
        langMap.put("lang", locale);
        langMap.put("xml:lang", locale);

		UIOutput.make(tofill, "polls-html", null).decorate(new UIFreeAttributeDecorator(langMap));
			
		UIOutput.make(tofill, "poll-list-title", messageLocator.getMessage("poll_list_title"));	
		
		boolean renderDelete = false;
		//populate the action links
		if (this.isAllowedPollAdd() || this.isSiteOwner() ) {
			UIBranchContainer actions = UIBranchContainer.make(tofill,"actions:",Integer.toString(0));
			log.debug("this user has some admin functions");
			if (this.isAllowedPollAdd()) {
				log.debug("User can add polls");
				//UIOutput.make(tofill, "poll-add", messageLocator
				//       .getMessage("action_add_poll"));
				UIInternalLink.make(actions,NAVIGATE_ADD,UIMessage.make("action_add_poll"),
						new PollViewParameters(AddPollProducer.VIEW_ID, "New 0"));
			} 
			if (this.isSiteOwner()) {
				UIInternalLink.make(actions, NAVIGATE_PERMISSIONS, UIMessage.make("action_set_permissions"),new SimpleViewParameters(PermissionsProducer.VIEW_ID));
			} 
		}


		List<Poll> polls = new ArrayList<Poll>();

		String siteId = externalLogic.getCurrentLocationId();
		if (siteId != null) {
			polls = pollListManager.findAllPolls(siteId);
		} else {
			log.warn("Unable to get siteid!");

		}

		if(polls.isEmpty()){
			UIOutput.make(tofill, "no-polls", messageLocator.getMessage("poll_list_empty"));
			UIOutput.make(tofill, "add-poll-icon");
			if (this.isAllowedPollAdd()) {
				UIInternalLink.make(tofill,"add-poll",UIMessage.make("new_poll_title"),
						new PollViewParameters(AddPollProducer.VIEW_ID, "New 0"));
			} 
		}
		else{
		// fix for broken en_ZA locale in JRE http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6488119
		Locale M_locale = null;
		String langLoc[] = localegetter.get().toString().split("_");
		if ( langLoc.length >= 2 ) {
			if ("en".equals(langLoc[0]) && "ZA".equals(langLoc[1]))
				M_locale = new Locale("en", "GB");
			else
				M_locale = new Locale(langLoc[0], langLoc[1]);
		} else{
			M_locale = new Locale(langLoc[0]);
		}

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.SHORT, M_locale);
		TimeZone tz = externalLogic.getLocalTimeZone();
		df.setTimeZone(tz);
		//m_log.debug("got timezone: " + tz.getDisplayName());

		UIOutput.make(tofill, "poll_list_remove_confirm", messageLocator.getMessage("poll_list_remove_confirm"));
		UIOutput.make(tofill, "poll_list_reset_confirm", messageLocator.getMessage("poll_list_reset_confirm"));

		UIForm deleteForm = UIForm.make(tofill, "delete-poll-form");
		// Create a multiple selection control for the tasks to be deleted.
		// We will fill in the options at the loop end once we have collected them.
		UISelect deleteselect = UISelect.makeMultiple(deleteForm, "delete-poll",
				null, "#{pollToolBean.deleteids}", new String[] {});

		//get the headers for the table
		/*UIMessage.make(deleteForm, "poll-question-title","poll_question_title");
		UIMessage.make(deleteForm, "poll-open-title", "poll_open_title");
		UIMessage.make(deleteForm, "poll-close-title", "poll_close_title");*/
		UIMessage.make(deleteForm, "poll-result-title", "poll_result_title");
		UIMessage.make(deleteForm, "poll-remove-title", "poll_remove_title");
		
		UILink question = UILink.make(tofill,"poll-question-title",messageLocator.getMessage("poll_question_title"), "#");
		question.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("poll_question_title_tooltip")));
		UILink open = UILink.make(tofill,"poll-open-title",messageLocator.getMessage("poll_open_title"), "#");
		open.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("poll_open_title_tooltip")));
		UILink close = UILink.make(tofill,"poll-close-title",messageLocator.getMessage("poll_close_title"), "#");
		close.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("poll_close_title_tooltip")));


		StringList deletable = new StringList();
		
		
		
		for (int i = 0 ; i < polls.size(); i++) {
			Poll poll = (Poll)polls.get(i);
			
			boolean canVote = pollVoteManager.pollIsVotable(poll);
			UIBranchContainer pollrow = UIBranchContainer.make(deleteForm,
					canVote ? "poll-row:votable"
							: "poll-row:nonvotable", poll.getPollId().toString());
			log.debug("adding poll row for " + poll.getText());

			if (canVote) {
				UIInternalLink voteLink = UIInternalLink.make(pollrow, NAVIGATE_VOTE, poll.getText(),
						new PollViewParameters(PollVoteProducer.VIEW_ID, poll.getPollId().toString()));
				//we need to add a decorator for the alt text
				voteLink.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("poll_vote_title") +":" + poll.getText()));

			} else {
				//the poll is lazily loaded so get the options
				poll.setOptions(pollListManager.getOptionsForPoll(poll.getPollId()));
				
				//is this not votable because of no options?
				if (poll.getPollOptions().size() == 0 )
					UIOutput.make(pollrow,"poll-text",poll.getText() + " (" + messageLocator.getMessage("poll_no_options") + ")");
				else
					UIOutput.make(pollrow,"poll-text",poll.getText());
			}



			if (pollListManager.isAllowedViewResults(poll, externalLogic.getCurrentUserId())) {
				UIInternalLink resultsLink =  UIInternalLink.make(pollrow, "poll-results", messageLocator.getMessage("action_view_results"),
						new PollViewParameters(ResultsProducer.VIEW_ID, poll.getPollId().toString()));
				resultsLink.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("action_view_results")+ ":" + poll.getText()));

			}

			if (poll.getVoteOpen()!=null)
				UIOutput.make(pollrow,"poll-open-date",df.format(poll.getVoteOpen()))
					.decorators = new DecoratorList(new UIFreeAttributeDecorator("name", "realDate:"+poll.getVoteOpen().toString()));
			else 
				UIVerbatim.make(pollrow,"poll-open-date","  ");

			if (poll.getVoteClose()!=null)
				UIOutput.make(pollrow,"poll-close-date",df.format(poll.getVoteClose()))
					.decorators = new DecoratorList(new UIFreeAttributeDecorator("name", "realDate:"+poll.getVoteClose().toString()));
			else 
				UIVerbatim.make(pollrow,"poll-close-date","  ");

			if (pollCanEdit(poll)) {
				UIInternalLink editLink = UIInternalLink.make(pollrow,"poll-revise",messageLocator.getMessage("action_revise_poll"),
						new PollViewParameters(AddPollProducer.VIEW_ID,poll.getPollId().toString()));
				editLink.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("action_revise_poll")+ ":" + poll.getText()));

			}
			if (pollCanDelete(poll)) {
				deletable.add(poll.getPollId().toString());
				UISelectChoice delete =  UISelectChoice.make(pollrow, "poll-select", deleteselect.getFullID(), (deletable.size()-1));
				delete.decorators = new DecoratorList(new UITooltipDecorator(UIMessage.make("delete_poll_tooltip", new String[] {poll.getText()})));
				UIMessage message = UIMessage.make(pollrow,"delete-label","delete_poll_tooltip", new String[] {poll.getText()});
				UILabelTargetDecorator.targetLabel(message,delete);
				log.debug("this poll can be deleted");
				renderDelete = true;

			}
		}



		deleteselect.optionlist.setValue(deletable.toStringArray());
		deleteForm.parameters.add(new UIELBinding("#{pollToolBean.siteID}", siteId));

		if (renderDelete) {
			UICommand.make(deleteForm, "delete-polls",  UIMessage.make("poll_list_delete"),
					"#{pollToolBean.processActionDelete}").decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("poll_list_delete_tooltip")));
			UICommand.make(deleteForm, "reset-polls-votes",  UIMessage.make("poll_list_reset"),
					"#{pollToolBean.processActionResetVotes}").decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("poll_list_reset_tooltip")));
		}
		}
	}


	private boolean isAllowedPollAdd() {
		if (externalLogic.isUserAdmin())
			return true;

		if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getCurrentLocationReference()))
			return true;

		return false;
	}

	private boolean isSiteOwner(){
		if (externalLogic.isUserAdmin())
			return true;
		else if (externalLogic.isAllowedInLocation("site.upd", externalLogic.getCurrentLocationReference()))
			return true;
		else
			return false;
	}

	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>(); // Always navigate back to this view.
		togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		return togo;
	}

	private boolean pollCanEdit(Poll poll) {
		if (externalLogic.isUserAdmin())
			return true;

		if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_EDIT_ANY, externalLogic.getCurrentLocationReference()))
			return true;

		if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_EDIT_OWN, externalLogic.getCurrentLocationReference())
				&& poll.getOwner().equals(externalLogic.getCurrentUserId()))
			return true;

		return false;
	}

	private boolean pollCanDelete(Poll poll) {
		return pollListManager.userCanDeletePoll(poll);
	}
}
