package org.sakaiproject.scorm.navigation;

import org.sakaiproject.scorm.model.api.SessionBean;

public interface INavigable {

	public void displayResource(SessionBean sessionBean, Object target);
	
	public Object getApplication();
	
}
