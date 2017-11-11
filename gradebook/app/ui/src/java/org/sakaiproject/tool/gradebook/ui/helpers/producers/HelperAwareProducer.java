/**
 * Copyright (c) 2003-2008 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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