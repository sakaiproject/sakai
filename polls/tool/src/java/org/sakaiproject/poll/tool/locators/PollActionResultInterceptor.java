/**********************************************************************************
 * $URL$
 * $Id$
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
		        outgoing.id = poll.getId();
		      }
		    }

	}

}
