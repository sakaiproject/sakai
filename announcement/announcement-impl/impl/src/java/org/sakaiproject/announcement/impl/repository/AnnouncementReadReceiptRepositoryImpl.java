/**
 * Copyright (c) 2003-2024 The Apereo Foundation
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
package org.sakaiproject.announcement.impl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import org.sakaiproject.announcement.api.model.AnnouncementReadReceipt;
import org.sakaiproject.announcement.api.repository.AnnouncementReadReceiptRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class AnnouncementReadReceiptRepositoryImpl extends SpringCrudRepositoryImpl<AnnouncementReadReceipt, Long> implements AnnouncementReadReceiptRepository {

    @Transactional(readOnly = true)
    public Optional<AnnouncementReadReceipt> findByMessageRefAndUserId(String messageRef, String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AnnouncementReadReceipt> query = cb.createQuery(AnnouncementReadReceipt.class);
        Root<AnnouncementReadReceipt> receipt = query.from(AnnouncementReadReceipt.class);
        query.where(cb.and(cb.equal(receipt.get("messageRef"), messageRef),
                            cb.equal(receipt.get("userId"), userId)));

        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<AnnouncementReadReceipt> findByMessageRef(String messageRef) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AnnouncementReadReceipt> query = cb.createQuery(AnnouncementReadReceipt.class);
        Root<AnnouncementReadReceipt> receipt = query.from(AnnouncementReadReceipt.class);
        query.where(cb.equal(receipt.get("messageRef"), messageRef));

        return session.createQuery(query).list();
    }
}
