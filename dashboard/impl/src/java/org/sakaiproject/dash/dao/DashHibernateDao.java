package org.sakaiproject.dash.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.sakaiproject.dash.model.JobRun;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DashHibernateDao extends HibernateDaoSupport {

    /* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#saveJobRun(org.sakaiproject.sitestats.api.JobRun)
	 */
	public boolean saveJobRun(final JobRun jobRun){
		if(jobRun == null) {
			return false;
		}
		Boolean r = getHibernateTemplate().execute(session -> {
            Transaction tx = null;
            try{
                tx = session.beginTransaction();
                session.saveOrUpdate(jobRun);
                tx.commit();
            }catch(Exception e){
                if(tx != null) tx.rollback();
                log.warn("Unable to commit transaction: ", e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        });
		return r.booleanValue();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#getLatestJobRun()
	 */
	public JobRun getLatestJobRun() throws Exception {
		JobRun r = getHibernateTemplate().execute(session -> {
            JobRun jobRun = null;
            Criteria c = session.createCriteria(JobRunImpl.class);
            c.setMaxResults(1);
            c.addOrder(Order.desc("id"));
            List jobs = c.list();
            if(jobs != null && jobs.size() > 0){
                jobRun = (JobRun) jobs.get(0);
            }
            return jobRun;
        });
		return r;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#getEventDateFromLatestJobRun()
	 */
	public Date getEventDateFromLatestJobRun() throws Exception {
		Date r = getHibernateTemplate().execute(session -> {
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
        });
		return r;
	}
}
