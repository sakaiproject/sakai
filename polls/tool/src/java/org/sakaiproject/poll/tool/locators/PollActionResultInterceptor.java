package org.sakaiproject.poll.tool.locators;

import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.tool.params.PollViewParameters;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class PollActionResultInterceptor implements ActionResultInterceptor {

	
	  private PollBeanLocator pollBeanLocator;
	  public void setTemplateBeanLocator(PollBeanLocator templateBeanLocator) {
	    this.pollBeanLocator = templateBeanLocator;
	    }
	
	public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {
		
		if (result.resultingView instanceof PollViewParameters) {
			  PollViewParameters outgoing = (PollViewParameters) result.resultingView;
		      Poll poll = (Poll) pollBeanLocator.locateBean(PollBeanLocator.NEW_1);
		      if (poll != null && outgoing.id == null) {
		        outgoing.id = poll.getId().toString();
		      }
		    }

	}

}
