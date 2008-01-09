package org.sakaiproject.scorm.dao.api;

import java.util.List;

import org.sakaiproject.scorm.model.api.Attempt;

public interface AttemptDao {

	public Attempt load(long id);
	
	public int count(long contentPackageId, String learnerId);
	
	public List<Attempt> find(long contentPackageId, String learnerId);
	
	public List<Attempt> find(String courseId, String learnerId);
	
	public Attempt lookup(long contentPackageId, String learnerId, long attemptNumber);
	
	public Attempt find(String courseId, String learnerId, long attemptNumber);
	
	public List<Attempt> find(long contentPackageId);
	
	public void save(Attempt attempt);
	
}
