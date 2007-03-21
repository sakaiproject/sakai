package org.sakaiproject.poll.tool.producers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UICommand;

import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

import org.sakaiproject.poll.tool.params.VoteBean;

import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;


import java.util.List;
import java.util.ArrayList;


public class ConfirmProducer implements ViewComponentProducer,NavigationCaseReporter{

	public static final String VIEW_ID = "voteThanks";
	private static Log m_log = LogFactory.getLog(PollVoteProducer.class);
	private VoteBean voteBean;
	
	
	private MessageLocator messageLocator;
	
	
	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}
	
	  public void setVoteBean(VoteBean vb){
		  this.voteBean = vb;
	  }

		
	  public void setMessageLocator(MessageLocator messageLocator) {
			  
		  this.messageLocator = messageLocator;
	  }


	  
	public void fillComponents(UIContainer tofill, ViewParameters arg1,
			ComponentChecker arg2) {
		// TODO Auto-generated method stub
		
		String voteId; 
		if (voteBean.voteCollection != null)
			voteId = voteBean.voteCollection.getId();
		else 
			voteId="VoteId is missing!";
		
		UIOutput.make(tofill,"confirm-msg",messageLocator.getMessage("thanks_msg"));
		UIOutput.make(tofill,"confirm-ref-msg",messageLocator.getMessage("thanks_ref"));
		UIOutput.make(tofill,"ref-number",voteId);
		UIForm form = UIForm.make(tofill,"back","");
		UICommand.make(form,"cancel",messageLocator.getMessage("thanks_done"),"#{pollToolBean.cancel}");
	}

	
	  public List reportNavigationCases() {
		    List togo = new ArrayList(); // Always navigate back to this view.
		    //togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		    //togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		    togo.add(new NavigationCase("cancel", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		    return togo;
		  }
	
}
