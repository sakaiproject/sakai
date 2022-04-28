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
package org.sakaiproject.conversations.impl.repository;

import java.util.Optional;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.conversations.api.model.ConvStatus;
import org.sakaiproject.conversations.api.repository.ConvStatusRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class ConvStatusRepositoryImpl extends SpringCrudRepositoryImpl<ConvStatus, Long>  implements ConvStatusRepository {

    @Transactional
    public Optional<ConvStatus> findBySiteIdAndUserId(String siteId, String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConvStatus> query = cb.createQuery(ConvStatus.class);
        Root<ConvStatus> status = query.from(ConvStatus.class);
        query.where(cb.and(cb.equal(status.get("siteId"), siteId),
                            cb.equal(status.get("userId"), userId)));

        return session.createQuery(query).uniqueResultOptional();
    }
}
