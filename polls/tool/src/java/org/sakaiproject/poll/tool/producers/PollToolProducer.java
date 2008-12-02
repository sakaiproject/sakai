/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

import java.text.DateFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.tool.params.PollViewParameters;
import org.sakaiproject.poll.tool.params.VoteBean;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;


import org.sakaiproject.site.api.SiteService;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;

import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.stringutil.StringList;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
//import uk.org.ponder.rsf.viewstate.EntityCentredViewParameters;
import uk.org.ponder.beanutil.entity.EntityID;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;


public class PollToolProducer implements ViewComponentProducer,
DefaultView,NavigationCaseReporter {
	public static final String VIEW_ID = "votePolls";
	private UserDirectoryService userDirectoryService;
	private PollListManager pollListManager;
	private ToolManager toolManager;
	private MessageLocator messageLocator;
	private LocaleGetter localegetter;
	private PollVoteManager pollVoteManager;  

	private static final String NAVIGATE_ADD = "actions-add";
	private static final String NAVIGATE_PERMISSIONS = "actions-permissions";
	private static final String NAVIGATE_VOTE = "poll-vote";





	private static Log m_log = LogFactory.getLog(PollToolProducer.class);

	public String getViewID() {
		return VIEW_ID;
	}

	public void setMessageLocator(MessageLocator messageLocator) {

		this.messageLocator = messageLocator;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setPollListManager(PollListManager pollListManager) {
		this.pollListManager = pollListManager;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void setLocaleGetter(LocaleGetter localegetter) {
		this.localegetter = localegetter;
	}

	public void setPollVoteManager(PollVoteManager pvm){
		this.pollVoteManager = pvm;
	}

	private SiteService siteService;
	public void setSiteService(SiteService s){
		this.siteService = s;

	}
	private TimeService timeService;
	public void setTimeService(TimeService ts) {
		timeService = ts;
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

		
		  //process any messages
		if (tml.size() > 0) {
		    	for (int i = 0; i < tml.size(); i ++ ) {
		    		UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", Integer.valueOf(i).toString());
		    		if (tml.messageAt(i).args != null ) {	    		
		    			UIMessage.make(errorRow,"error",tml.messageAt(i).acquireMessageCode(),(String[])tml.messageAt(i).args[0]);
		    		} else {
		    			UIMessage.make(errorRow,"error",tml.messageAt(i).acquireMessageCode());
		    		}
		    		
		    	}
			}
		
		
		voteBean.setPoll(null);
		voteBean.voteCollection = null;
			
		UIOutput.make(tofill, "poll-list-title", messageLocator.getMessage("poll_list_title"));	
		
		boolean renderDelete = false;
		//populte the action links
		if (this.isAllowedPollAdd() || this.isSiteOwner() ) {
			UIBranchContainer actions = UIBranchContainer.make(tofill,"actions:",Integer.toString(0));
			m_log.debug("this user has some admin functions");
			if (this.isAllowedPollAdd()) {
				m_log.debug("User can add polls");
				//UIOutput.make(tofill, "poll-add", messageLocator
				//       .getMessage("action_add_poll"));
				UIInternalLink.make(actions,NAVIGATE_ADD,UIMessage.make("action_add_poll"),
						new PollViewParameters(AddPollProducer.VIEW_ID, "New 0"));
			} 
			if (this.isSiteOwner()) {
				UIInternalLink.make(actions, NAVIGATE_PERMISSIONS, UIMessage.make("action_set_permissions"),new SimpleViewParameters(PermissionsProducer.VIEW_ID));
			} 
		}

		User currentuser = userDirectoryService.getCurrentUser();
		String currentuserid = currentuser.getEid();





		List<Poll> polls = new ArrayList<Poll>();




		String siteId = toolManager.getCurrentPlacement().getContext();
		if (siteId != null) {
			polls = pollListManager.findAllPolls(siteId);
		} else {
			m_log.warn("Unable to get siteid!");

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
		TimeZone tz = timeService.getLocalTimeZone();
		df.setTimeZone(tz);
		//m_log.debug("got timezone: " + tz.getDisplayName());

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
		
		UILink.make(tofill,"poll-question-title",messageLocator.getMessage("poll_question_title"), "#");
		UILink.make(tofill,"poll-open-title",messageLocator.getMessage("poll_open_title"), "#");
		UILink.make(tofill,"poll-close-title",messageLocator.getMessage("poll_close_title"), "#");



		StringList deletable = new StringList();
		
		
		
		for (int i = 0 ; i < polls.size(); i++) {
			Poll poll = (Poll)polls.get(i);
			boolean canVote = pollIsVotable(poll);
			UIBranchContainer pollrow = UIBranchContainer.make(deleteForm,
					canVote ? "poll-row:votable"
							: "poll-row:nonvotable", poll.getPollId().toString());
			m_log.debug("adding poll row for " + poll.getText());

			if (canVote) {
				UIInternalLink voteLink = UIInternalLink.make(pollrow, NAVIGATE_VOTE, poll.getText(),
						new PollViewParameters(PollVoteProducer.VIEW_ID, poll.getPollId().toString()));
				//we need to add a decorator for the alt text
				voteLink.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("poll_vote_title") +":" + poll.getText()));

			} else {
				//is this not votable because of no options?
				if (poll.getPollOptions().size() == 0 )
					UIOutput.make(pollrow,"poll-text",poll.getText() + " (" + messageLocator.getMessage("poll_no_options") + ")");
				else
					UIOutput.make(pollrow,"poll-text",poll.getText());
			}



			if (isAllowedViewResults(poll)) {
				UIInternalLink resultsLink =  UIInternalLink.make(pollrow, "poll-results", messageLocator.getMessage("action_view_results"),
						new PollViewParameters(ResultsProducer.VIEW_ID, poll.getPollId().toString()));
				resultsLink.decorators = new DecoratorList(new UITooltipDecorator(messageLocator.getMessage("action_view_results")+ ":" + poll.getText()));

			}

			if (poll.getVoteOpen()!=null)
				UIOutput.make(pollrow,"poll-open-date",df.format(poll.getVoteOpen()));
			else 
				UIVerbatim.make(pollrow,"poll-open-date","  ");

			if (poll.getVoteClose()!=null)
				UIOutput.make(pollrow,"poll-close-date",df.format(poll.getVoteClose()));
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
				m_log.debug("this poll can be deleted");
				renderDelete = true;

			}
		}



		deleteselect.optionlist.setValue(deletable.toStringArray());
		deleteForm.parameters.add(new UIELBinding("#{pollToolBean.siteID}", siteId));

		if (renderDelete) 
			UICommand.make(deleteForm, "delete-polls",  UIMessage.make("poll_list_update"),
			"#{pollToolBean.processActionDelete}");
		}
	}


	private boolean isAllowedPollAdd() {
		if (SecurityService.isSuperUser())
			return true;

		if (SecurityService.unlock("poll.add", "/site/" + toolManager.getCurrentPlacement().getContext()))
			return true;

		return false;
	}

	private boolean isSiteOwner(){
		if (SecurityService.isSuperUser())
			return true;
		else if (SecurityService.unlock("site.upd", "/site/" + toolManager.getCurrentPlacement().getContext()))
			return true;
		else
			return false;
	}

	public List reportNavigationCases() {
		List togo = new ArrayList(); // Always navigate back to this view.
		togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		return togo;
	}

	private boolean pollIsVotable(Poll poll)
	{
		//poll must have options to be votable

		poll.setOptions(pollListManager.getOptionsForPoll(poll));
		if (poll.getPollOptions()== null || poll.getPollOptions().size() == 0) {
			m_log.debug("poll has no options");
			return false;
		}

		boolean pollAfterOpen = true;
		boolean pollBeforeClose = true;

		if (poll.getVoteClose()!=null) {
			if (poll.getVoteClose().before(new Date())) {
				m_log.debug("Poll is closed for voting");
				pollBeforeClose=false;
			}

		} 

		if (poll.getVoteOpen()!=null) {
			if(new Date().before(poll.getVoteOpen())) {
				m_log.debug("Poll is not open yet");
				pollAfterOpen=false;
			}
		} 

		if (pollAfterOpen && pollBeforeClose)
		{
			if (poll.getLimitVoting() && pollVoteManager.userHasVoted(poll.getPollId())) {
				return false;
			}
			//the user hasn't voted do they have permission to vote?'
			m_log.debug("about to check if this user can vote in " + toolManager.getCurrentPlacement().getContext());
			if (SecurityService.unlock("poll.vote","/site/" + toolManager.getCurrentPlacement().getContext()) || SecurityService.isSuperUser())
			{
				m_log.debug("this poll is votable  " + poll.getText());
				return true;
			}
		}

		return false;
	}

	private boolean isAllowedViewResults(Poll poll) {
		if (SecurityService.isSuperUser())
			return true;

		if (poll.getDisplayResult().equals("open"))
			return true;

		if (poll.getOwner().equals(userDirectoryService.getCurrentUser().getId()))
			return true;

		if (poll.getDisplayResult().equals("afterVoting") && pollVoteManager.userHasVoted(poll.getPollId()))
			return true;

		if ((poll.getDisplayResult().equals("afterClosing") || poll.getDisplayResult().equals("afterVoting") )&& poll.getVoteClose().before(new Date()))
			return true;

		//the owner can view the results
		if(poll.getOwner().equals(userDirectoryService.getCurrentUser().getId()))
			return true;

		return false;
	}

	private boolean pollCanEdit(Poll poll) {
		if (SecurityService.isSuperUser() )
			return true;

		if (SecurityService.unlock(pollListManager.PERMISSION_EDIT_ANY,"/site/" + toolManager.getCurrentPlacement().getContext()))
			return true;

		if (SecurityService.unlock(pollListManager.PERMISSION_EDIT_OWN,"/site/" + toolManager.getCurrentPlacement().getContext()) && poll.getOwner().equals(userDirectoryService.getCurrentUser().getId()))
			return true;

		return false;
	}

	private boolean pollCanDelete(Poll poll) {
		if (SecurityService.isSuperUser() )
			return true;
		if (SecurityService.unlock(pollListManager.PERMISSION_DELETE_ANY,"/site/" + toolManager.getCurrentPlacement().getContext()))
			return true;

		if (SecurityService.unlock(pollListManager.PERMISSION_DELETE_OWN,"/site/" + toolManager.getCurrentPlacement().getContext()) && poll.getOwner().equals(userDirectoryService.getCurrentUser().getId())) 
			return true;

		return false;
	}
}
