package org.sakaiproject.scorm.tool;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface ISyncCallback {

	public void synchronizeState(RunState runState, AjaxRequestTarget target);
	
}
