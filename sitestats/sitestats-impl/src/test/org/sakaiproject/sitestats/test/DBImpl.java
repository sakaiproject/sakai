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

import org.sakaiproject.sitestats.impl.EventStatImpl;
import org.sakaiproject.sitestats.impl.ResourceStatImpl;
import org.sakaiproject.sitestats.impl.SiteActivityImpl;
import org.sakaiproject.sitestats.impl.SitePresenceImpl;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBImpl extends HibernateDaoSupport implements DB {

	public void insertObject(final Object obj) {
        try {
		    getHibernateTemplate().execute(session -> {
                session.saveOrUpdate(obj);
                return null;
            });
        } catch(DataAccessException dae) {
            log.error("Error while saving: {}", dae.getMessage(), dae);
        }
	}
	
	@SuppressWarnings("unchecked")
	public List getResultsForClass(final Class classz) {
        List results;
	    try {
            results = getHibernateTemplate().execute(session -> {
                return session.createCriteria(classz).list();
            });
        } catch(DataAccessException dae) {
            log.error("Error while retrieving results: {}", dae.getMessage(), dae);
            results = new ArrayList<>();
        }
        return results;
	}
	
	@SuppressWarnings("unchecked")
	public void deleteAllForClass(final Class classz) {
        try {
		    getHibernateTemplate().execute((HibernateCallback) session -> {
                List all = session.createCriteria(classz).list();
                for(Object o : all) {
                    session.delete(o);
                }
                return null;
            });
        } catch(DataAccessException dae) {
            log.error("Error while performing deletion: {}", dae.getMessage(), dae);
        }
	}
	
	@SuppressWarnings("unchecked")
	public void deleteAll() {
        try{
		    getHibernateTemplate().execute((HibernateCallback) session -> {
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
                return null;
            });
        } catch(DataAccessException dae){
            log.error("Error while performing deletion: {}", dae.getMessage(), dae);
        }
	}
}
