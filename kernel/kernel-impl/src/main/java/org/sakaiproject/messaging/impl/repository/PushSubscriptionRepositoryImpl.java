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

import java.util.List;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.sakaiproject.messaging.api.model.PushSubscription;
import org.sakaiproject.messaging.api.repository.PushSubscriptionRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class PushSubscriptionRepositoryImpl extends SpringCrudRepositoryImpl<PushSubscription, Long> implements PushSubscriptionRepository {

    @Transactional(readOnly = true)
    public List<PushSubscription> findByUser(String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PushSubscription> query = cb.createQuery(PushSubscription.class);
        Root<PushSubscription> ps = query.from(PushSubscription.class);
        query.where(cb.equal(ps.get("userId"), userId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public int deleteByFingerprint(String browserFingerprint) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<PushSubscription> delete = cb.createCriteriaDelete(PushSubscription.class);
        Root<PushSubscription> ps = delete.from(PushSubscription.class);
        delete.where(cb.equal(ps.get("fingerprint"), browserFingerprint));

        return session.createQuery(delete).executeUpdate();
    }
}
