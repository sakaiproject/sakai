package org.sakaiproject.tool.gradebook.ui.helpers.producers;


import org.sakaiproject.tool.gradebook.ui.helpers.params.HelperAwareViewParams;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class HelperAwareProducer implements ActionResultInterceptor {

	public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {
		if (incoming instanceof HelperAwareViewParams) {
			HelperAwareViewParams params = (HelperAwareViewParams) incoming;
			//if the finishURL param is not null, and we are attempting to go to a new page then redirect to url provided
			//   the second portion of this is important to make sure that this navigation does not occur when
			//   feedback on current page is needed
			if (params.finishURL != null && !((ViewParameters)result.resultingView).viewID.equals(incoming.viewID)) {
				result.resultingView = new RawViewParameters(params.finishURL);
			}
		}
	}
}