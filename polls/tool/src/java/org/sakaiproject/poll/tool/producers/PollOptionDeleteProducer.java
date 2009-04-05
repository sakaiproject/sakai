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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.tool.params.OptionViewParameters;
import org.sakaiproject.poll.tool.params.PollViewParameters;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;


public class PollOptionDeleteProducer implements ViewComponentProducer, ActionResultInterceptor,ViewParamsReporter {
//,
	public static final String VIEW_ID = "pollOptionDelete";
	private static Log m_log = LogFactory.getLog(PollOptionDeleteProducer.class);
	
	
	
	private MessageLocator messageLocator;
	
	
	
	
	
	public String getViewID() {
		
		return VIEW_ID;
	}
	

		
	  public void setMessageLocator(MessageLocator messageLocator) {
			  
		  this.messageLocator = messageLocator;
	  }

	  private PollListManager pollListManager;
	  public void setPollListManager(PollListManager p){
		  this.pollListManager = p;
	  }
	  
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker arg2) {
		m_log.debug("rendering view");
		
		UIOutput.make(tofill,"confirm_delete",messageLocator.getMessage("delete_confirm"));
		UIOutput.make(tofill,"error",messageLocator.getMessage("delete_option_message"));
		
		Option option = null;
		OptionViewParameters aivp = (OptionViewParameters) viewparams;
		if(aivp.id != null) {
			m_log.debug("got a paramater with id: " + Long.valueOf(aivp.id));
			// passed in an id so we should be modifying an item if we can find it
			option = pollListManager.getOptionById(Long.valueOf(aivp.id));
		} 
		
		if (option == null) {
			m_log.error("no such option found!");
			return;
		}
		
		UIVerbatim.make(tofill,"poll_text",option.getOptionText());
		UIForm form = UIForm.make(tofill,"opt-form");
		UIInput.make(form,"opt-text","#{option.optionText}",option.getOptionText());
		
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
		  m_log.debug("intercepting action results!");
		  Poll poll = null;

		  if (actionReturn != null && actionReturn instanceof Poll) {
			  poll = (Poll) actionReturn;
			  m_log.debug("return is poll: " + poll.getPollId());
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
						m_log.debug("New option for poll: " + optvp.pollId);
						result.resultingView = new OptionViewParameters(VIEW_ID, optvp.id , optvp.pollId);
					}

				} else {
					Option option = pollListManager.getOptionById(Long.valueOf(optvp.id));
					result.resultingView = new PollViewParameters(viewId, option.getPollId().toString());
				}
			}

	  }
}
