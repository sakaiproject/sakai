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

import org.hibernate.Session;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.AssignmentGradeRecord;
import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.repository.AssignmentGradeRecordRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.sakaiproject.hibernate.HibernateCriterionUtils;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class AssignmentGradeRecordRepositoryImpl extends SpringCrudRepositoryImpl<AssignmentGradeRecord, Long>  implements AssignmentGradeRecordRepository {

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_Gradebook_IdAndGradableObject_RemovedOrderByPointsEarned(Long gradebookId, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");
        Join<GradableObject, Gradebook> gb = go.join("gradebook");
        query.where(cb.and(cb.equal(gb.get("id"), gradebookId), cb.equal(go.get("removed"), removed)))
            .orderBy(cb.asc(agr.get("pointsEarned")));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_IdAndGradableObject_RemovedOrderByPointsEarned(Long gradableObjectId, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");
        query.where(cb.and(cb.equal(go.get("id"), gradableObjectId), cb.equal(go.get("removed"), removed)))
            .orderBy(cb.asc(agr.get("pointsEarned")));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<AssignmentGradeRecord> findByGradableObject_IdAndStudentId(Long gradableObjectId, String studentId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");
        query.where(cb.and(cb.equal(go.get("id"), gradableObjectId), cb.equal(agr.get("studentId"), studentId)));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_Gradebook_Id(Long gradebookId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<GradableObject, Gradebook> gb = agr.join("gradableObject").join("gradebook");
        query.where(cb.equal(gb.get("id"), gradebookId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_Gradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<GradableObject, Gradebook> gb = agr.join("gradableObject").join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_RemovedAndGradableObject_IdInAndStudentIdIn(Boolean removed, List<Long> gradableObjectIds, List<String> studentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");

        Predicate gradableObjectIdPredicate = HibernateCriterionUtils.PredicateInSplitter(cb, go.get("id"), gradableObjectIds);
        Predicate studentIdPredicate = HibernateCriterionUtils.PredicateInSplitter(cb, agr.get("studentId"), studentIds);

        query.where(cb.and(cb.equal(go.get("removed"), removed), gradableObjectIdPredicate, studentIdPredicate));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_Gradebook_IdAndGradableObject_RemovedAndStudentIdIn(Long gradebookId, Boolean removed, Collection<String> studentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");
        Join<GradableObject, Gradebook> gb = go.join("gradebook");

        Predicate studentIdPredicate = HibernateCriterionUtils.PredicateInSplitter(cb, agr.get("studentId"), studentIds);

        query.where(cb.and(cb.equal(gb.get("id"), gradebookId), cb.equal(go.get("removed"), removed), studentIdPredicate));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObjectAndStudentIdIn(GradebookAssignment assignment, Collection<String> studentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);

        Predicate studentIdPredicate = HibernateCriterionUtils.PredicateInSplitter(cb, agr.get("studentId"), studentIds);

        query.where(cb.and(cb.equal(agr.get("gradableObject"), assignment), studentIdPredicate));
        return session.createQuery(query).list();
    }


    @Transactional
    public int deleteByGradableObject(GradebookAssignment assignment) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<AssignmentGradeRecord> delete = cb.createCriteriaDelete(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = delete.from(AssignmentGradeRecord.class);
        delete.where(cb.equal(agr.get("gradableObject"), assignment));
        return session.createQuery(delete).executeUpdate();
    }
}
