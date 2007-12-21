package org.sakaiproject.scorm.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.sakaiproject.scorm.dao.api.AttemptDao;
import org.sakaiproject.scorm.model.api.Attempt;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class AttemptDaoImpl extends HibernateDaoSupport implements AttemptDao {

	public Attempt load(long id) {
		return (Attempt)getHibernateTemplate().load(Attempt.class, id);
	}
	
	public List<Attempt> find(String courseId, String learnerId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(Attempt.class.getName())
			.append(" where courseId=? and learnerId=? order by attemptNumber desc");
		
		List r = getHibernateTemplate().find(buffer.toString(), 
				new Object[]{ courseId, learnerId });
		
		return r;
	}
	
	public List<Attempt> find(long contentPackageId, String learnerId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(Attempt.class.getName())
			.append(" where contentPackageId=? and learnerId=? order by attemptNumber desc");
		
		List r = getHibernateTemplate().find(buffer.toString(), 
				new Object[]{ contentPackageId, learnerId });
		
		return r;
	}
	
	public Attempt find(String courseId, String learnerId, long attemptNumber) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(Attempt.class.getName())
			.append(" where courseId=? and learnerId=? and attemptNumber=? ");
		
		List r = getHibernateTemplate().find(buffer.toString(), 
				new Object[]{ courseId, learnerId, attemptNumber });
		
		if (r.size() == 0)
			return null;
			
		Attempt attempt = (Attempt)r.get(r.size() - 1);
		
		return attempt;
	}

	public List<Attempt> find(long contentPackageId) {
		List r = getHibernateTemplate().find(
				"from " + Attempt.class.getName()
						+ " where contentPackageId=? ", 
						new Object[]{ contentPackageId });
		
		return r;
	}
	
	public void save(Attempt attempt) {
		attempt.setLastModifiedDate(new Date());
		getHibernateTemplate().saveOrUpdate(attempt);
	}

}
