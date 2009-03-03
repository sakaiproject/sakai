package org.sakaiproject.sitestats.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sakaiproject.sitestats.impl.EventStatImpl;
import org.sakaiproject.sitestats.impl.ResourceStatImpl;
import org.sakaiproject.sitestats.impl.SiteActivityImpl;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class DBImpl extends HibernateDaoSupport implements DB {

	@SuppressWarnings("unchecked")
	public List getResultsForClass(final Class classz) {
		return (List) getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				List result;
				try{
					tx = session.beginTransaction();
					result = session.createCriteria(classz).list();
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					return new ArrayList();
				}
				return result;
			}
		});		
	}
	
	@SuppressWarnings("unchecked")
	public void deleteAllForClass(final Class classz) {
		getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				int count = 0;
				try{
					tx = session.beginTransaction();
					List all = session.createCriteria(classz).list();
					for(Object o : all) {
						session.delete(o);
					}
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
				}
				return Integer.valueOf(count);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void deleteAll() {
		getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					List all = session.createCriteria(SiteVisitsImpl.class).list();
					for(Object o : all) {
						session.delete(o);
					}
					all = session.createCriteria(SiteActivityImpl.class).list();
					for(Object o : all) {
						session.delete(o);
					}
					all = session.createCriteria(EventStatImpl.class).list();
					for(Object o : all) {
						session.delete(o);
					}
					all = session.createCriteria(ResourceStatImpl.class).list();
					for(Object o : all) {
						session.delete(o);
					}			
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
				}
				return null;
			}
		});
	}
}
