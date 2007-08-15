package org.sakaiproject.scorm.dao.api;

import org.adl.datamodels.IDataManager;

public interface DataManagerDao {

	public IDataManager find(String courseId, String userId);
	
	public void save(IDataManager dataManager);
	
}
