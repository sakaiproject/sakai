package org.sakaiproject.scorm.dao.sakai;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.scorm.dao.LearnerDao;
import org.sakaiproject.scorm.model.api.Learner;

public class MockLearnerDaoImpl implements LearnerDao {

	public List<Learner> find(String context) {
		ArrayList<Learner> rv = new ArrayList<Learner>();
		rv.add(load("learner1"));
		return rv;
	}

	public Learner load(String id) {
		Learner learner = new Learner(id);

		return learner;
	}

}