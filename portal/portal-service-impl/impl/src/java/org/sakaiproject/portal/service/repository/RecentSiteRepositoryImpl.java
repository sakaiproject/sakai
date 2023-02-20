/*
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.portal.service.repository;

import java.util.List;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.portal.api.model.RecentSite;
import org.sakaiproject.portal.api.repository.RecentSiteRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class RecentSiteRepositoryImpl extends SpringCrudRepositoryImpl<RecentSite, Long>  implements RecentSiteRepository {

    @Transactional(readOnly = true)
    public List<RecentSite> findByUserId(String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<RecentSite> query = cb.createQuery(RecentSite.class);
        Root<RecentSite> recentSite = query.from(RecentSite.class);
        query.where(cb.equal(recentSite.get("userId"), userId));
        query.orderBy(cb.desc(recentSite.get("created")));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<RecentSite> findBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<RecentSite> query = cb.createQuery(RecentSite.class);
        Root<RecentSite> recentSite = query.from(RecentSite.class);
        query.where(cb.equal(recentSite.get("siteId"), siteId));

        return session.createQuery(query).list();
    }

    @Transactional
    public Integer deleteByUserId(String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<RecentSite> delete = cb.createCriteriaDelete(RecentSite.class);
        Root<RecentSite> recentSite = delete.from(RecentSite.class);
        delete.where(cb.equal(recentSite.get("userId"), userId));

        return session.createQuery(delete).executeUpdate();
    }

    @Transactional
    public Integer deleteBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<RecentSite> delete = cb.createCriteriaDelete(RecentSite.class);
        Root<RecentSite> recentSite = delete.from(RecentSite.class);
        delete.where(cb.equal(recentSite.get("siteId"), siteId));

        return session.createQuery(delete).executeUpdate();
    }

    @Transactional
    public Integer deleteByUserIdAndSiteId(String userId, String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<RecentSite> delete = cb.createCriteriaDelete(RecentSite.class);
        Root<RecentSite> recentSite = delete.from(RecentSite.class);
        delete.where(cb.and(cb.equal(recentSite.get("userId"), userId), cb.equal(recentSite.get("siteId"), siteId)));

        return session.createQuery(delete).executeUpdate();
    }
}
