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

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.repository.GradebookAssignmentRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradebookAssignmentRepositoryImpl extends SpringCrudRepositoryImpl<GradebookAssignment, Long>  implements GradebookAssignmentRepository {

    @Transactional(readOnly = true)
    public Optional<GradebookAssignment> findByNameAndGradebook_UidAndRemoved(String name, String gradebookUid, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = ga.join("gradebook");
        query.where(cb.and(cb.equal(ga.get("name"), name),
                            cb.equal(gb.get("uid"), gradebookUid),
                            cb.equal(ga.get("removed"), removed)));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<GradebookAssignment> findByCategory_IdAndRemoved(Long categoryId, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Category> cat = ga.join("category");
        query.where(cb.and(cb.equal(cat.get("id"), categoryId),
                            cb.equal(ga.get("removed"), removed)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<GradebookAssignment> findByIdAndGradebook_UidAndRemoved(Long id, String gradebookUid, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = ga.join("gradebook");
        query.where(cb.and(cb.equal(ga.get("id"), id),
                            cb.equal(gb.get("uid"), gradebookUid),
                            cb.equal(ga.get("removed"), removed)));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<GradebookAssignment> findByGradebook_IdAndRemoved(Long gradebookId, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = ga.join("gradebook");
        query.where(cb.and(cb.equal(gb.get("id"), gradebookId),
                            cb.equal(ga.get("removed"), removed)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<GradebookAssignment> findByGradebook_IdAndCategory_IdAndRemoved(Long gradebookId, Long categoryId, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);

        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = ga.join("gradebook");
        Join<GradebookAssignment, Category> cat = ga.join("category");

        Predicate byGradebook = cb.equal(gb.get("id"), gradebookId);
        Predicate byCategoryId = cb.equal(cat.get("id"), categoryId);
        Predicate byRemoved = cb.equal(ga.get("removed"), removed);

        query.where(cb.and(byGradebook, byCategoryId, byRemoved));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<GradebookAssignment> findByGradebook_IdAndRemovedAndNotCounted(Long gradebookId, Boolean removed, Boolean notCounted) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = ga.join("gradebook");
        query.where(cb.and(cb.equal(gb.get("id"), gradebookId),
                            cb.equal(ga.get("removed"), removed),
                            cb.equal(ga.get("notCounted"), notCounted)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<GradebookAssignment> findByGradebook_IdAndRemovedAndNotCountedAndUngraded(Long gradebookId, Boolean removed, Boolean notCounted, Boolean ungraded) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = ga.join("gradebook");
        query.where(cb.and(cb.equal(gb.get("id"), gradebookId),
                            cb.equal(ga.get("removed"), removed),
                            cb.equal(ga.get("notCounted"), notCounted),
                            cb.equal(ga.get("ungraded"), ungraded)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<GradebookAssignment> findByGradebook_UidAndExternalId(String gradebookUid, String externalId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = ga.join("gradebook");
        query.where(cb.and(cb.equal(gb.get("uid"), gradebookUid),
                            cb.equal(ga.get("externalId"), externalId)));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<GradebookAssignment> findByExternalId(String externalId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        query.where(cb.equal(ga.get("externalId"), externalId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Long countByGradebook_UidAndExternalId(String gradebookUid, String externalId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = ga.join("gradebook");
        query.select(cb.count(ga))
            .where(cb.and(cb.equal(gb.get("uid"), gradebookUid),
                            cb.equal(ga.get("externalId"), externalId)));
        return session.createQuery(query).uniqueResult();
    }

    @Transactional(readOnly = true)
    public Long countByNameAndGradebook_UidAndRemoved(String name, String gradebookUid, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = ga.join("gradebook");
        query.select(cb.count(ga))
            .where(cb.and(cb.equal(gb.get("uid"), gradebookUid),
                            cb.equal(ga.get("name"), name),
                            cb.equal(ga.get("removed"), removed)));
        return session.createQuery(query).uniqueResult();
    }


    @Transactional(readOnly = true)
    public Long countByNameAndGradebookAndNotIdAndRemoved(String name, Gradebook gradebook, Long id, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        query.select(cb.count(ga))
            .where(cb.and(cb.equal(ga.get("name"), name),
                            cb.equal(ga.get("gradebook"), gradebook),
                            cb.notEqual(ga.get("id"), id),
                            cb.equal(ga.get("removed"), removed)));
        return session.createQuery(query).uniqueResult();
    }

    @Transactional(readOnly = true)
    public boolean existsByIdAndRemoved(Long id, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<GradebookAssignment> ga = query.from(GradebookAssignment.class);
        query.select(cb.count(ga))
            .where(cb.and(cb.equal(ga.get("id"), id), cb.equal(ga.get("removed"), removed)));
        return session.createQuery(query).uniqueResult() == 1L;
    }

    @Transactional(readOnly = true)
    public List<GradebookAssignment> findByGradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookAssignment> query = cb.createQuery(GradebookAssignment.class);
        Join<GradebookAssignment, Gradebook> gb = query.from(GradebookAssignment.class).join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }
}
