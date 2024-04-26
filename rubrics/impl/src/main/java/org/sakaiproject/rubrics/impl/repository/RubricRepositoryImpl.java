/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.impl.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.repository.EvaluationRepository;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class RubricRepositoryImpl extends SpringCrudRepositoryImpl<Rubric, Long> implements RubricRepository {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Override
    public Optional<Rubric> findById(Long aLong) {
        Optional<Rubric> rubric = super.findById(aLong);
        rubric.ifPresent(this::determineRubricLock);
        return rubric;
    }

    @Override
    public Rubric getById(Long aLong) {
        Rubric rubric = super.getById(aLong);
        determineRubricLock(rubric);
        return rubric;
    }

    public List<Rubric> findByShared(Boolean shared) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Rubric> query = cb.createQuery(Rubric.class);
        Root<Rubric> rubric = query.from(Rubric.class);
        query.where(cb.equal(rubric.get("shared"), shared));

        List<Rubric> rubrics = session.createQuery(query).list();
        rubrics.forEach(this::determineRubricLock);
        return rubrics;
    }

    @Transactional(readOnly = true)
    public List<Rubric> findByOwnerId(String ownerId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Rubric> query = cb.createQuery(Rubric.class);
        Root<Rubric> rubric = query.from(Rubric.class);
        query.where(cb.equal(rubric.get("ownerId"), ownerId));

        List<Rubric> rubrics = session.createQuery(query).list();
        rubrics.forEach(this::determineRubricLock);
        return rubrics;
    }

    @Transactional(readOnly = true)
    public List<Rubric> findAdhocByTitle(String itemId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Rubric> query = cb.createQuery(Rubric.class);
        Root<Rubric> rubric = query.from(Rubric.class);
        query.where(cb.equal(rubric.get("adhoc"), true));
        query.where(cb.equal(rubric.get("title"), itemId));

        return session.createQuery(query).list();
    }

    @Transactional
    public int deleteByOwnerId(String ownerId) {

        Session session = sessionFactory.getCurrentSession();

        List<Rubric> rubrics = findByOwnerId(ownerId);
        rubrics.forEach(session::delete);
        return rubrics.size();
    }

    private void determineRubricLock(Rubric rubric) {
        if (rubric != null) {
            rubric.setLocked(rubric.getAssociations().stream().anyMatch(Predicate.not(assoc -> evaluationRepository.findByAssociationId(assoc.getId()).isEmpty())));
        }
    }
}
