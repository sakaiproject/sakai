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