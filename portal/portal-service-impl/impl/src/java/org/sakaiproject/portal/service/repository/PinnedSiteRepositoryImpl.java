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
import java.util.Optional;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.sakaiproject.portal.api.model.PinnedSite;
import org.sakaiproject.portal.api.repository.PinnedSiteRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class PinnedSiteRepositoryImpl extends SpringCrudRepositoryImpl<PinnedSite, Long>  implements PinnedSiteRepository {

    @Transactional(readOnly = true)
    public List<PinnedSite> findByUserIdOrderByPosition(String userId) {
        return findByUserIdAndHasBeenUnpinnedOrderByPosition(userId, false);
    }

    @Transactional(readOnly = true)
    public List<PinnedSite> findByUserIdAndHasBeenUnpinnedOrderByPosition(String userId, boolean hasBeenUnpinned) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PinnedSite> query = cb.createQuery(PinnedSite.class);
        Root<PinnedSite> pinnedSite = query.from(PinnedSite.class);
        Predicate[] predicates = new Predicate[2];
        predicates[0] = cb.equal(pinnedSite.get("userId"), userId);
        predicates[1] = cb.equal(pinnedSite.get("hasBeenUnpinned"), hasBeenUnpinned);
        query.where(predicates).orderBy(cb.asc(pinnedSite.get("position")));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<PinnedSite> findByUserIdAndSiteId(String userId, String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PinnedSite> query = cb.createQuery(PinnedSite.class);
        Root<PinnedSite> pinnedSite = query.from(PinnedSite.class);
        Predicate[] predicates = new Predicate[2];
        predicates[0] = cb.equal(pinnedSite.get("userId"), userId);
        predicates[1] = cb.equal(pinnedSite.get("siteId"), siteId);
        query.where(predicates);

        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<PinnedSite> findBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PinnedSite> query = cb.createQuery(PinnedSite.class);
        Root<PinnedSite> pinnedSite = query.from(PinnedSite.class);
        query.where(cb.equal(pinnedSite.get("siteId"), siteId));

        return session.createQuery(query).list();
    }

    @Transactional
    public Integer deleteByUserId(String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<PinnedSite> delete = cb.createCriteriaDelete(PinnedSite.class);
        Root<PinnedSite> pinnedSite = delete.from(PinnedSite.class);
        Predicate[] predicates = new Predicate[2];
        predicates[0] = cb.equal(pinnedSite.get("userId"), userId);
        predicates[1] = cb.equal(pinnedSite.get("hasBeenUnpinned"), false);
        delete.where(predicates);

        return session.createQuery(delete).executeUpdate();
    }

    @Transactional
    public Integer deleteBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<PinnedSite> delete = cb.createCriteriaDelete(PinnedSite.class);
        Root<PinnedSite> pinnedSite = delete.from(PinnedSite.class);
        delete.where(cb.equal(pinnedSite.get("siteId"), siteId));

        return session.createQuery(delete).executeUpdate();
    }

    @Transactional
    public Integer deleteByUserIdAndSiteId(String userId, String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<PinnedSite> delete = cb.createCriteriaDelete(PinnedSite.class);
        Root<PinnedSite> pinnedSite = delete.from(PinnedSite.class);
        delete.where(cb.and(cb.equal(pinnedSite.get("userId"), userId), cb.equal(pinnedSite.get("siteId"), siteId)));

        return session.createQuery(delete).executeUpdate();
    }
}
