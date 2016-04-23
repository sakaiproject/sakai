package org.sakaiproject.dash.dao;

import org.sakaiproject.dash.model.JobRun;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;



public class DashHibernateDao extends HibernateDaoSupport {

    private Logger                         LOG                                     = LoggerFactory.getLogger(DashHibernateDao.class);



    /* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#saveJobRun(org.sakaiproject.sitestats.api.JobRun)
	 */
	public boolean saveJobRun(final JobRun jobRun){
		if(jobRun == null) {
			return false;
		}
		Object r = getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					session.saveOrUpdate(jobRun);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			}			
		});
		return ((Boolean) r).booleanValue();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#getLatestJobRun()
	 */
	public JobRun getLatestJobRun() throws Exception {
		Object r = getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				JobRun jobRun = null;
				Criteria c = session.createCriteria(JobRunImpl.class);
				c.setMaxResults(1);
				c.addOrder(Order.desc("id"));
				List jobs = c.list();
				if(jobs != null && jobs.size() > 0){
					jobRun = (JobRun) jobs.get(0);
				}
				return jobRun;
			}			
		});
		return (JobRun) r;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#getEventDateFromLatestJobRun()
	 */
	public Date getEventDateFromLatestJobRun() throws Exception {
		Object r = getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria c = session.createCriteria(JobRunImpl.class);
				c.add(Expression.isNotNull("lastEventDate"));
				c.setMaxResults(1);
				c.addOrder(Order.desc("id"));
				List jobs = c.list();
				if(jobs != null && jobs.size() > 0){
					JobRun jobRun = (JobRun) jobs.get(0);
					return jobRun.getLastEventDate();
				}
				return null;
			}			
		});
		return (Date) r;
	}
}
