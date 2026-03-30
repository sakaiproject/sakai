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
import java.util.HashMap;

import java.util.List;
import java.time.Instant;
import java.time.Instant;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

import javax.persistence.Tuple;
import javax.persistence.criteria.*;
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

@Slf4j
public class UserNotificationRepositoryImpl extends SpringCrudRepositoryImpl<UserNotification, Long> implements UserNotificationRepository {

    public List<UserNotification> findByToUser(String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserNotification> query = cb.createQuery(UserNotification.class);
        Root<UserNotification> un = query.from(UserNotification.class);
        query.where(cb.and(cb.equal(un.get("deferred"), false), cb.equal(un.get("toUser"), userId))).orderBy(cb.desc(un.get("eventDate")));
        return session.createQuery(query).list();
    }

    public List<UserNotification> findByBroadcast(boolean broadcast) {

        Session session = sessionFactory.getCurrentSession();

        Instant now = Instant.now();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserNotification> query = cb.createQuery(UserNotification.class);
        Root<UserNotification> un = query.from(UserNotification.class);
        query.where(cb.equal(un.get("broadcast"), broadcast), cb.greaterThan(un.get("endDate"), now)).orderBy(cb.desc(un.get("eventDate")));
        return session.createQuery(query).list();
    }

    public long countUnviewedByToUser(String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserNotification> un = query.from(UserNotification.class);
        query.select(cb.count(un)).where(cb.and(
                cb.equal(un.get("deferred"), false),
                cb.equal(un.get("toUser"), userId),
                cb.or(cb.isNull(un.get("viewed")), cb.isFalse(un.get("viewed")))
        ));
        return session.createQuery(query).getSingleResult();
    }

    public long countActiveUnviewedBroadcastNotifications() {

        Session session = sessionFactory.getCurrentSession();

        Instant now = Instant.now();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserNotification> un = query.from(UserNotification.class);
        query.select(cb.count(un)).where(cb.and(
                cb.equal(un.get("broadcast"), true),
                cb.greaterThan(un.get("endDate"), now),
                cb.or(cb.isNull(un.get("viewed")), cb.isFalse(un.get("viewed")))
        ));
        return session.createQuery(query).getSingleResult();
    }

    @Transactional
    public int deleteExpiredNotifications() {

        Session session = sessionFactory.getCurrentSession();

        Instant now = Instant.now();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<UserNotification> delete = cb.createCriteriaDelete(UserNotification.class);
        Root<UserNotification> un = delete.from(UserNotification.class);
        delete.where(cb.lessThan(un.get("endDate"), now));
        return session.createQuery(delete).executeUpdate();
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







    @Override
    @Transactional( readOnly = true)
    public List<Long> getIdsToDeleteByUserIdAndToolPrefix(String userId, int toDeleteCount, String toolPrefix) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> idQuery = cb.createQuery(Long.class);
        Root<UserNotification> un = idQuery.from(UserNotification.class);

        idQuery.select(un.get("id"));
        idQuery.where(
                cb.and(
                    cb.like(un.get("event"), toolPrefix + "%"),
                    cb.equal(un.get("toUser"), userId),
                    cb.equal(un.get("deferred"), false)
                )
        );
        idQuery.orderBy(cb.asc(un.get("eventDate")), cb.asc(un.get("id")));

        return session.createQuery(idQuery)
                .setMaxResults(toDeleteCount)
                .list();
    }



    @Override
    @Transactional(readOnly = true)
    public long countAllByToUserAndByToolAndNotDeferredOverThreshold(String toUser, String toolPrefix, int threshold) {
        Session session = sessionFactory.getCurrentSession();


        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<UserNotification> un = query.from(UserNotification.class);

        Expression<Long> count = cb.count(un);

        query.multiselect(
                un.get("toUser").alias("userId"),
                count.alias("count")
        );
        query.where(
                cb.and(
                    cb.equal(un.get("deferred"), false),
                    cb.like(un.get("event"), toolPrefix  + "%"),
                    cb.equal(un.get("toUser"), toUser)
                )
        );
        query.groupBy(un.get("toUser"));
        query.having(cb.gt(count, threshold));


        List<Tuple> rows = session.createQuery(query).getResultList();
        if (rows.isEmpty()) {
            return 0L;
        }
        return rows.get(0).get("count", Long.class);
    }


    @Override
    @Transactional
    public int deleteNotificationsInList(List<Long> idsToDelete) {
        if (idsToDelete == null || idsToDelete.isEmpty()) return 0;

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder db = session.getCriteriaBuilder();
        CriteriaDelete<UserNotification> delete = db.createCriteriaDelete(UserNotification.class);

        Root<UserNotification> un = delete.from(UserNotification.class);
        delete.where(un.get("id").in(idsToDelete));

        return session.createQuery(delete).executeUpdate();
    }


    @Override
    @Transactional(readOnly = true)
    public List<String> findAllDistinctToUser() {
        Session session  = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserNotification> un = query.from(UserNotification.class);
        query.select(un.get("toUser"));
        query.distinct(true);
        return session.createQuery(query).list();
    }





}
