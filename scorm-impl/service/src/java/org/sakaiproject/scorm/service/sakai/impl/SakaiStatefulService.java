package org.sakaiproject.scorm.service.sakai.impl;

import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

public abstract class SakaiStatefulService implements LearningManagementSystem {

	protected abstract SessionManager sessionManager();
	protected abstract ToolManager toolManager();
	
	public String currentContext() {
		return toolManager().getCurrentPlacement().getContext();
	}
	
	public String currentLearnerId() {
		String learnerId = sessionManager().getCurrentSessionUserId();
		
		return learnerId;
	}
	
}
