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

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.sitestats.impl.DetailedEventImpl;
import org.sakaiproject.sitestats.impl.EventStatImpl;
import org.sakaiproject.sitestats.impl.LessonBuilderStatImpl;
import org.sakaiproject.sitestats.impl.ResourceStatImpl;
import org.sakaiproject.sitestats.impl.ServerStatImpl;
import org.sakaiproject.sitestats.impl.SiteActivityImpl;
import org.sakaiproject.sitestats.impl.SitePresenceImpl;
import org.sakaiproject.sitestats.impl.SitePresenceTotalImpl;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;
import org.sakaiproject.sitestats.impl.UserStatImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DB extends HibernateDaoSupport {

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
	
	public <T> List<T> getResultsForClass(final Class<T> classz) {
        List<T> results;
	    try {
            results = getHibernateTemplate().execute(session -> {
                CriteriaQuery criteriaQuery = session.getCriteriaBuilder().createQuery(classz);
                Root<T> root = criteriaQuery.from(classz);
                criteriaQuery.select(root);

                return session.createQuery(criteriaQuery).getResultList();
            });
        } catch(DataAccessException dae) {
            log.error("Error while retrieving results: {}", dae.getMessage(), dae);
            results = new ArrayList<T>();
        }
        return results;
	}

	private <T> void deleteAllOfClass(Session session, Class<T> classz) {
        CriteriaQuery<T> cq = session.getCriteriaBuilder().createQuery(classz);
        cq.from(classz);
        session.createQuery(cq).getResultList().forEach(session::delete);
    }
	
	@SuppressWarnings("unchecked")
	public <T> void deleteAllForClass(final Class<T> classz) {
        try {
             getHibernateTemplate().execute(session -> {
                deleteAllOfClass(session, classz);
                return null;
            });
        } catch(DataAccessException dae) {
            log.error("Error while performing deletion: {}", dae.getMessage(), dae);
        }
	}
	
	@SuppressWarnings("unchecked")
	public void deleteAll() {
        try{
		    getHibernateTemplate().execute(session -> {
                deleteAllOfClass(session, SiteVisitsImpl.class);
                deleteAllOfClass(session, SiteActivityImpl.class);
                deleteAllOfClass(session, EventStatImpl.class);
                deleteAllOfClass(session, ResourceStatImpl.class);
                deleteAllOfClass(session, SitePresenceImpl.class);
                deleteAllOfClass(session, SitePresenceTotalImpl.class);
                deleteAllOfClass(session, DetailedEventImpl.class);
                deleteAllOfClass(session, LessonBuilderStatImpl.class);
                deleteAllOfClass(session, UserStatImpl.class);
                deleteAllOfClass(session, ServerStatImpl.class);
                session.flush();
                return null;
            });
        } catch(DataAccessException dae){
            log.error("Error while performing deletion: {}", dae.getMessage(), dae);
        }
	}
}
