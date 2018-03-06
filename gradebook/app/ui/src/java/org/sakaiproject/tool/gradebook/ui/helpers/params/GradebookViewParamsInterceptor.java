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
package org.sakaiproject.tool.gradebook.ui.helpers.params;

import org.sakaiproject.tool.gradebook.ui.helpers.producers.AuthorizationFailedProducer;
import org.sakaiproject.tool.gradebook.ui.helpers.beans.locallogic.LocalPermissionLogic;

import uk.org.ponder.rsf.viewstate.AnyViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsInterceptor;

public class GradebookViewParamsInterceptor implements ViewParamsInterceptor{

	private LocalPermissionLogic localPermissionLogic;
	public void setLocalPermissionLogic(LocalPermissionLogic localPermissionLogic) {
		this.localPermissionLogic = localPermissionLogic;
	}
	
	public AnyViewParameters adjustViewParameters(ViewParameters incoming) {
		
		//Always allow the authorization failed page to pass through
		if (AuthorizationFailedProducer.VIEW_ID.equals(incoming.viewID)) {
			return incoming;
		}
		
		//Check permissions for each view
		if (localPermissionLogic.checkCurrentUserHasViewPermission(incoming)) {
			return incoming;
		}
		
		return new SimpleViewParameters(AuthorizationFailedProducer.VIEW_ID);
	}
	
}