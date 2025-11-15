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
package org.sakaiproject.grading.impl.repository;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.repository.GradingEventRepository;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradingEventRepositoryImpl extends SpringCrudRepositoryImpl<GradingEvent, Long>  implements GradingEventRepository {

    @Transactional(readOnly = true)
    public List<GradingEvent> findByGradableObject_Gradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingEvent> query = cb.createQuery(GradingEvent.class);
        Root<GradingEvent> ge = query.from(GradingEvent.class);
        Join<GradingEvent, Gradebook> gb = ge.join("gradableObject").join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<GradingEvent> findByGradableObject_IdAndStudentIdOrderByDateGraded(Long assignmentId, String studentId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingEvent> query = cb.createQuery(GradingEvent.class);
        Root<GradingEvent> ge = query.from(GradingEvent.class);
        Join<GradingEvent, GradableObject> go = ge.join("gradableObject");
        query.where(cb.and(cb.equal(go.get("id"), assignmentId), cb.equal(ge.get("studentId"), studentId)))
            .orderBy(cb.desc(ge.get("dateGraded")));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<GradingEvent> findByDateGreaterThanEqualAndGradableObject_IdIn(Date since, List<Long> assignmentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingEvent> query = cb.createQuery(GradingEvent.class);
        Root<GradingEvent> ge = query.from(GradingEvent.class);
        Join<GradingEvent, GradableObject> go = ge.join("gradableObject");

        Predicate assignmentIdPredicate = HibernateCriterionUtils.PredicateInSplitter(cb, go.get("id"), assignmentIds);

        query.where(cb.and(cb.greaterThanOrEqualTo(ge.get("dateGraded"), since), assignmentIdPredicate));
        return session.createQuery(query).list();
    }

    @Transactional
    public int deleteByGradableObject(GradebookAssignment assignment) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<GradingEvent> delete = cb.createCriteriaDelete(GradingEvent.class);
        Root<GradingEvent> ge = delete.from(GradingEvent.class);
        delete.where(cb.equal(ge.get("gradableObject"), assignment));
        return session.createQuery(delete).executeUpdate();
    }
}
