package org.sakaiproject.scorm.client.impl;

import org.sakaiproject.scorm.client.api.ScormClientService;
import org.sakaiproject.user.api.UserDirectoryService;

public class ScormClientServiceImpl implements ScormClientService {

	protected UserDirectoryService userDirectoryService() { return null; }
	
	public String getUserName() {
		return userDirectoryService().getCurrentUser().getDisplayName();
	}
	
}
