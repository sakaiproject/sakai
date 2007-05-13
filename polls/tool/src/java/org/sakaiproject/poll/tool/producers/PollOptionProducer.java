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
import org.sakaiproject.poll.tool.params.VoteBean;
//import org.sakaiproject.tool.poll.impl.PollListManagerDaoImpl;

import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.EntityCentredViewParameters;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIInput;
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
		EntityCentredViewParameters ecvp = (EntityCentredViewParameters) viewparams;
		if (ecvp.mode.equals(EntityCentredViewParameters.MODE_NEW) ) {
			UIOutput.make(tofill,"new-option-title",messageLocator.getMessage("new_option_title"));
		} else {
			UIOutput.make(tofill,"new-option-title",messageLocator.getMessage("edit_option_title"));
		}
	
		
		if (ecvp != null && ecvp.mode!=null && !ecvp.mode.equals(EntityCentredViewParameters.MODE_NEW)){
			//check the ecvp
			 
			 String strId = ecvp.getELPath().substring(ecvp.getELPath().indexOf(".") + 1);
			 String type = strId.substring(0,strId.indexOf('_'));
			 String id = strId.substring(strId.indexOf('_')+1);
				m_log.debug("got id of " + strId);
				if (type.equals("Poll")) {
					if (id.equals("0"))
						poll = voteBean.getPoll();
					else
						poll = pollListManager.getPollById(new Long(id));
				} else {
					if (!id.equals("0"))
						option = pollListManager.getOptionById(new Long(id));
						poll = pollListManager.getPollById(option.getPollId());
				}
				voteBean.setPoll(poll);
		}
		
		
		//if the option bean is null set it
		if (optionBean == null) {
			m_log.debug("setting the option bean");
			optionBean = new Option();
			//make sure the option is null
			
		}
		
		UIOutput.make(tofill,"poll_text",poll.getText());
		UIOutput.make(tofill,"poll-question",messageLocator.getMessage("new_poll_question"));
		UIForm form = UIForm.make(tofill,"opt-form");
		
		UIOutput.make(form,"option-label",messageLocator.getMessage("new_poll_option"));
		if (ecvp.mode.equals(EntityCentredViewParameters.MODE_NEW) ) {
			UIInput.make(form,"opt-text","#{option.optionText}","");
			m_log.debug("here we are! " + ecvp.mode);
		} else {
			m_log.debug("mode is " + ecvp.mode);
			UIInput.make(form,"opt-text","#{option.optionText}",option.getOptionText());
			form.parameters.add(new UIELBinding("#{option.id}",
			           option.getId()));
		}
		
		
		form.parameters.add(new UIELBinding("#{option.pollId}",
		           poll.getPollId()));
		 UICommand save =  UICommand.make(form, "submit-new-option", messageLocator.getMessage("new_poll_submit"),
		  "#{pollToolBean.proccessActionAddOption}");
		 save.parameters.add(new UIELBinding("#{pollToolBean.submissionStatus}", "save"));
		 if (ecvp.mode.equals(EntityCentredViewParameters.MODE_NEW)) {
			 UICommand saveAdd = UICommand.make(form, "submit-option-add", messageLocator.getMessage("new_poll_saveoption"),
			 "#{pollToolBean.proccessActionAddOption}");
			 saveAdd.parameters.add(new UIELBinding("#{pollToolBean.submissionStatus}", "option"));
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
		    return new EntityCentredViewParameters(VIEW_ID, new EntityID("Poll", "Poll_0"));
		  	
	  }
}
