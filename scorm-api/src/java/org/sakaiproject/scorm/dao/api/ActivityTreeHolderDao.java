package org.sakaiproject.scorm.dao.api;

import org.sakaiproject.scorm.model.api.ActivityTreeHolder;

public interface ActivityTreeHolderDao {

	public ActivityTreeHolder find(long contentPackageId, String learnerId);
	
	public void save(ActivityTreeHolder holder);
	
}
