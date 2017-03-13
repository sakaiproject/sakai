/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Transaction;
import org.sakaiproject.sitestats.impl.EventStatImpl;
import org.sakaiproject.sitestats.impl.ResourceStatImpl;
import org.sakaiproject.sitestats.impl.SiteActivityImpl;
import org.sakaiproject.sitestats.impl.SitePresenceImpl;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

public class DBImpl extends HibernateDaoSupport implements DB {

	public void insertObject(final Object obj) {
		getHibernateTemplate().execute(session -> {
            Transaction tx = null;
            try{
                tx = session.beginTransaction();
                session.saveOrUpdate(obj);
                tx.commit();
            }catch(Exception e){
                if(tx != null) tx.rollback();
            }
            return Boolean.TRUE;
        });
	}
	
	@SuppressWarnings("unchecked")
	public List getResultsForClass(final Class classz) {
		return (List) getHibernateTemplate().execute(session -> {
            Transaction tx = null;
            List result;
            try{
                tx = session.beginTransaction();
                result = session.createCriteria(classz).list();
                tx.commit();
            }catch(Exception e){
                if(tx != null) tx.rollback();
                return new ArrayList<>();
            }
            return result;
        });
	}
	
	@SuppressWarnings("unchecked")
	public void deleteAllForClass(final Class classz) {
		getHibernateTemplate().execute((HibernateCallback) session -> {
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
        });
	}
	
	@SuppressWarnings("unchecked")
	public void deleteAll() {
		getHibernateTemplate().execute((HibernateCallback) session -> {
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
                all = session.createCriteria(SitePresenceImpl.class).list();
                for(Object o : all) {
                    session.delete(o);
                }
                tx.commit();
            }catch(Exception e){
                if(tx != null) tx.rollback();
            }
            return null;
        });
	}
}
