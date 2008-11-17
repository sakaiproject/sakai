package org.sakaiproject.scorm.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.scorm.dao.api.AttemptDao;
import org.sakaiproject.scorm.model.api.Attempt;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class AttemptDaoImpl extends HibernateDaoSupport implements AttemptDao {

	public int count(final long contentPackageId, final String learnerId) {
		HibernateCallback hcb = new HibernateCallback()
	      {                
	        public Object doInHibernate(Session session) throws HibernateException, SQLException
	        {            
	          Criteria criteria = session.createCriteria(Attempt.class)
	              .add(Restrictions.eq("contentPackageId", contentPackageId))
	              .add(Restrictions.eq("learnerId", learnerId))
	              .setProjection(Projections.count("id"));
	          return criteria.uniqueResult();
	        }
	      };       
	      
	      Object result = getHibernateTemplate().execute(hcb);
	      
	      int r = 0;
	      if (result != null) {
	    	  if (result instanceof Number)
	    		  r = ((Number)result).intValue();
	      }
	      return r;
	}
	
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
	
	public Attempt lookup(final long contentPackageId, final String learnerId, final long attemptNumber) {
		HibernateCallback hcb = new HibernateCallback() {                
	        public Object doInHibernate(Session session) throws HibernateException, SQLException {   
	        	StringBuilder buffer = new StringBuilder();
	    		buffer.append("from ").append(Attempt.class.getName())
	    			.append(" where contentPackageId=? and learnerId=? and attemptNumber=?");
	        	
	    		Query query = session.createQuery(buffer.toString());
	        	query.setLong(0, contentPackageId);
	        	query.setString(1, learnerId);
	        	query.setLong(2, attemptNumber);
	          
	        	return query.uniqueResult();
	        }
		};       
	      
		Attempt attempt = (Attempt)getHibernateTemplate().execute(hcb);
		
		return attempt;
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
