package org.sakaiproject.scorm.dao;

import java.util.List;

import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.Learner;

public interface LearnerDao {

	public Learner load(String id) throws LearnerNotDefinedException;
	
	public List<Learner> find(String context);
	
	
}
