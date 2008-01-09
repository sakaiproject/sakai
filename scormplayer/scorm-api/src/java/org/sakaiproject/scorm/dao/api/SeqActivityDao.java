package org.sakaiproject.scorm.dao.api;

import org.sakaiproject.scorm.model.api.SeqActivitySnapshot;

public interface SeqActivityDao {

	public SeqActivitySnapshot findSnapshot(String activityId);
	
}
