/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.timesheet.impl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.timesheet.api.TimeSheetEntry;
import org.sakaiproject.timesheet.api.repository.TimeSheetRepository;

public class TimeSheetRepositoryImpl extends SpringCrudRepositoryImpl<TimeSheetEntry, Long> implements TimeSheetRepository {

    public Optional<List<TimeSheetEntry>> findByReference(String reference) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TimeSheetEntry> cq = cb.createQuery(TimeSheetEntry.class);
        Root<TimeSheetEntry> root = cq.from(TimeSheetEntry.class);
        cq.select(root);
        cq.where(cb.equal(root.get("reference"), reference));

        return Optional.ofNullable(session.createQuery(cq).getResultList());
    }

    @Override
    public Optional<List<TimeSheetEntry>> findAllByUserIdAndReference(String userId, String reference) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TimeSheetEntry> cq = cb.createQuery(TimeSheetEntry.class);
        Root<TimeSheetEntry> root = cq.from(TimeSheetEntry.class);
        cq.select(root);
        Predicate pUser = cb.equal(root.get("userId"), userId);
        Predicate pReference = cb.equal(root.get("reference"), reference);
        cq.where(cb.and(pUser, pReference));

        return Optional.ofNullable(session.createQuery(cq).getResultList());
    }
}
