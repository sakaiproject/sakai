package org.sakaiproject.scorm.ui;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.scorm.model.api.SessionBean;

public interface UISynchronizer {

	public void synchronizeState(SessionBean sessionBean, AjaxRequestTarget target);
	
}
