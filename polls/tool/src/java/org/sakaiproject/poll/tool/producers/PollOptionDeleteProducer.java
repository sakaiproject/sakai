package org.sakaiproject.poll.tool.producers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UICommand;

import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.VoteCollection;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.tool.params.VoteBean;

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


public class PollOptionDeleteProducer implements ViewComponentProducer,NavigationCaseReporter,ViewParamsReporter{
//,
	public static final String VIEW_ID = "pollOptionDelete";
	private static Log m_log = LogFactory.getLog(PollOptionDeleteProducer.class);
	private VoteBean voteBean;
	
	
	private MessageLocator messageLocator;
	private LocaleGetter localegetter;
	
	
	
	
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
	  
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker arg2) {
		m_log.info("rendering view");
		Poll poll = null;
		Option option = null;
		UIOutput.make(tofill,"confirm_delete",messageLocator.getMessage("delete_confirm"));
		UIOutput.make(tofill,"error",messageLocator.getMessage("delete_option_message"));
		EntityCentredViewParameters ecvp = (EntityCentredViewParameters) viewparams;
		if (poll== null || ecvp != null){
			//check the ecvp
			 
			 String strId = ecvp.getELPath().substring(ecvp.getELPath().indexOf(".") + 1);
			 String type = strId.substring(0,strId.indexOf('_'));
			 String id = strId.substring(strId.indexOf('_')+1);
				m_log.debug("got id of " + strId);
				if (type.equals("Poll")) {
					poll = pollListManager.getPollById(new Long(id));
				} else { 
					option = pollListManager.getOptionById(new Long(id));
					poll = pollListManager.getPollById(option.getPollId());
				}
		}
		UIOutput.make(tofill,"poll_text",option.getOptionText());
		UIForm form = UIForm.make(tofill,"opt-form");
		UIInput.make(form,"opt-text","#{option.optionText}",option.getOptionText());
		
		form.parameters.add(new UIELBinding("#{option.id}",
		           option.getId()));
		form.parameters.add(new UIELBinding("#{option.pollId}",
		           poll.getPollId()));
		 
		  UICommand saveAdd = UICommand.make(form, "submit-option-add", messageLocator.getMessage("delete_option_confirm"),
		  "#{pollToolBean.proccessActionDeleteOption}");
		  saveAdd.parameters.add(new UIELBinding("#{option.status}", "delete"));
		  
		  UICommand cancel = UICommand.make(form, "cancel",messageLocator.getMessage("new_poll_cancel"),"#{pollToolBean.cancel}");
		   cancel.parameters.add(new UIELBinding("#{option.status}", "cancel"));
		   
	}

	
	  public List reportNavigationCases() {
		    List togo = new ArrayList();
		    togo.add(new NavigationCase(null, new SimpleViewParameters(this.VIEW_ID)));
		    togo.add(new NavigationCase("success", new EntityCentredViewParameters(AddPollProducer.VIEW_ID, 
	    			new EntityID("Poll", "0"))));
		    togo.add(new NavigationCase("cancel", new EntityCentredViewParameters(AddPollProducer.VIEW_ID, 
	    			new EntityID("Poll", "0"))));
		    return togo;
		  }
	 
	  public ViewParameters getViewParameters() {
		    return new EntityCentredViewParameters(VIEW_ID, new EntityID("Option", null));

	  }
	 
}
