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
package org.sakaiproject.messaging.impl.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.messaging.api.repository.UserNotificationRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.lang3.StringUtils;

public class UserNotificationRepositoryImpl extends SpringCrudRepositoryImpl<UserNotification, Long> implements UserNotificationRepository {

    @Transactional(readOnly = true)
    public List<UserNotification> findByToUser(String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserNotification> query = cb.createQuery(UserNotification.class);
        Root<UserNotification> un = query.from(UserNotification.class);
        query.where(cb.and(cb.equal(un.get("deferred"), false), cb.equal(un.get("toUser"), userId))).orderBy(cb.desc(un.get("eventDate")));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public boolean existsByToUser(String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserNotification> un = query.from(UserNotification.class);
        query.select(cb.count(un))
             .where(cb.and(cb.equal(un.get("toUser"), userId), cb.equal(un.get("deferred"), false)));
        Long count = session.createQuery(query).uniqueResult();
        return count != null && count > 0L;
    }

    @Transactional
    public int deleteByToUserAndDeferred(String userId, boolean deferred) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<UserNotification> delete = cb.createCriteriaDelete(UserNotification.class);
        Root<UserNotification> un = delete.from(UserNotification.class);
        delete.where(cb.and(cb.equal(un.get("toUser"), userId), cb.equal(un.get("deferred"), deferred)));
        return session.createQuery(delete).executeUpdate();
    }

    @Transactional
    public int setAllNotificationsViewed(String userId, String siteId, String toolId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate cu = cb.createCriteriaUpdate(UserNotification.class);
        Root<UserNotification> un = cu.from(UserNotification.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(un.get("toUser"), userId));
        if (StringUtils.isNotBlank(siteId)) {
            predicates.add(cb.equal(un.get("siteId"), siteId));
        }
        if (StringUtils.isNotBlank(toolId)) {
            predicates.add(cb.equal(un.get("tool"), toolId));
        }
        cu.set("viewed", true).where(cb.and(predicates.toArray(new Predicate[0])));
        return session.createQuery(cu).executeUpdate();
    }

    @Transactional
    public int setDeferredBySiteId(String siteId, boolean deferred) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate cu = cb.createCriteriaUpdate(UserNotification.class);
        Root<UserNotification> un = cu.from(UserNotification.class);
        cu.set("deferred", deferred).where(cb.equal(un.get("siteId"), siteId));
        return session.createQuery(cu).executeUpdate();
    }
}
