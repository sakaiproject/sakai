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

package org.sakaiproject.poll.tool.producers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIOutput;
//import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UICommand;

import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

//import org.sakaiproject.tool.poll.api.VoteCollection;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.tool.params.OptionViewParameters;
import org.sakaiproject.poll.tool.params.VoteBean;
//import org.sakaiproject.tool.poll.impl.PollListManagerDaoImpl;

import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.EntityCentredViewParameters;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UITextDimensionsDecorator;
import uk.org.ponder.beanutil.entity.EntityID;

import java.util.List;
import java.util.ArrayList;


public class PollOptionProducer implements ViewComponentProducer,ViewParamsReporter,NavigationCaseReporter {

	public static final String VIEW_ID = "pollOption";
	private static Log m_log = LogFactory.getLog(PollOptionProducer.class);
	private VoteBean voteBean;
	
	
	private MessageLocator messageLocator;
	private LocaleGetter localegetter;
	
	private Option optionBean;
	  public void setOption (Option o) {
		  this.optionBean = o;
	  }
	
	public String getViewID() {
		
		return VIEW_ID;
	}
	
	  public void setVoteBean(VoteBean vb){
		   this.voteBean = vb;
	  }

		
	  public void setMessageLocator(MessageLocator messageLocator) {
			  
		  this.messageLocator = messageLocator;
	  }

	  public void setLocaleGetter(LocaleGetter localegetter) {
		this.localegetter = localegetter;
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
	  
	  
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker arg2) {
	
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
    
		
		
		Poll poll = voteBean.getPoll();
		if (poll == null)
			m_log.warn("Bean poll is null!");
		else
			m_log.debug("bean poll is " + poll.getPollId());
		

		Option option = null;
		OptionViewParameters aivp = (OptionViewParameters) viewparams;
		boolean newOption = false;
		if(aivp.id != null) {
			m_log.debug("got a paramater with id: " + new Long(aivp.id));
			// passed in an id so we should be modifying an item if we can find it
			option = pollListManager.getOptionById(new Long(aivp.id));
		} else {
			option = new Option();
			if (aivp.pollId != null)
				option.setPollId(new Long(aivp.pollId));
			else 
				option.setPollId(voteBean.getPoll().getPollId());
				
			newOption = true;
		}
		

		
		
		UIOutput.make(tofill,"poll_text",poll.getText());
		UIOutput.make(tofill,"poll-question",messageLocator.getMessage("new_poll_question"));
		UIForm form = UIForm.make(tofill,"opt-form");
		
		UIOutput.make(form,"option-label",messageLocator.getMessage("new_poll_option"));
		
		/*
		if (ecvp.mode.equals(EntityCentredViewParameters.MODE_NEW) ) {
			UIInput.make(form,"opt-text","#{option.optionText}","");
			m_log.debug("here we are! " + ecvp.mode);
		} else {
			m_log.debug("mode is " + ecvp.mode);
			UIInput.make(form,"opt-text","#{option.optionText}",option.getOptionText());
			form.parameters.add(new UIELBinding("#{option.optionId}",
			           option.getOptionId()));
		}
		*/
		if (option.getOptionText() == null)
			option.setOptionText("");
		
		UIInput optText = UIInput.make(form,"optText:","#{option.optionText}",option.getOptionText());
		//optText.decorators = new DecoratorList(new UITextDimensionsDecorator(4, 4));
		  richTextEvolver.evolveTextInput(optText);
		
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

	
	  public List reportNavigationCases() {
		    List togo = new ArrayList();
		    togo.add(new NavigationCase("save", new EntityCentredViewParameters(AddPollProducer.VIEW_ID, 
		    			new EntityID("Poll", "0"))));
		    togo.add(new NavigationCase("cancel", new EntityCentredViewParameters(AddPollProducer.VIEW_ID, 
	    			new EntityID("Poll", "0"))));
		    return togo;
		  }
	  
	  public ViewParameters getViewParameters() {
		    return new OptionViewParameters();
		  	
	  }
}
