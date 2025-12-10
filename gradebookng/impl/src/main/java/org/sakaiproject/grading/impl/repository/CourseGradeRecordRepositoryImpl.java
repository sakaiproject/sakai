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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.CourseGradeRecord;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.repository.CourseGradeRecordRepository;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class CourseGradeRecordRepositoryImpl extends SpringCrudRepositoryImpl<CourseGradeRecord, Long>  implements CourseGradeRecordRepository {

    @Transactional(readOnly = true)
    public List<CourseGradeRecord> findByGradableObject_Id(Long id) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGradeRecord> query = cb.createQuery(CourseGradeRecord.class);
        Join<CourseGradeRecord, GradableObject> go = query.from(CourseGradeRecord.class).join("gradableObject");
        query.where(cb.equal(go.get("id"), id));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<CourseGradeRecord> findByGradableObject_GradebookAndEnteredGradeNotNull(Gradebook gradebook) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGradeRecord> query = cb.createQuery(CourseGradeRecord.class);
        Root<CourseGradeRecord> cgr = query.from(CourseGradeRecord.class);
        Join<CourseGradeRecord, GradableObject> go = cgr.join("gradableObject");
        query.where(cb.and(cb.equal(go.get("gradebook"), gradebook), cb.isNotNull(cgr.get("enteredGrade"))));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<CourseGradeRecord> findByGradableObject_GradebookAndStudentId(Gradebook gradebook, String studentId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGradeRecord> query = cb.createQuery(CourseGradeRecord.class);
        Root<CourseGradeRecord> cgr = query.from(CourseGradeRecord.class);
        Join<CourseGradeRecord, GradableObject> go = cgr.join("gradableObject");
        query.where(cb.and(cb.equal(cgr.get("studentId"), studentId),
                            cb.equal(go.get("gradebook"), gradebook)));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public Long countByGradableObject_Gradebook_IdAndEnteredGradeNotNullAndStudentIdIn(Long gradebookId, Set<String> studentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CourseGradeRecord> cgr = query.from(CourseGradeRecord.class);
        Join<GradableObject, Gradebook> gb = cgr.join("gradableObject").join("gradebook");

        Predicate studentIdPredicate = HibernateCriterionUtils.PredicateInSplitter(cb, cgr.get("studentId"), studentIds);

        query.select(cb.count(cgr))
            .where(cb.and(cb.equal(gb.get("id"), gradebookId),
                            cb.isNotNull(cgr.get("enteredGrade")),
                            studentIdPredicate));
        return session.createQuery(query).uniqueResult();
    }

    @Transactional(readOnly = true)
    public List<CourseGradeRecord> findByGradableObject_Gradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGradeRecord> query = cb.createQuery(CourseGradeRecord.class);
        Root<CourseGradeRecord> cgr = query.from(CourseGradeRecord.class);
        Join<GradableObject, Gradebook> gb = cgr.join("gradableObject").join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }
}
