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

import org.sakaiproject.rubrics.api.model.Evaluation;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.EvaluationRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

public class EvaluationRepositoryImpl extends SpringCrudRepositoryImpl<Evaluation, Long> implements EvaluationRepository {

    public Optional<Evaluation> findByAssociationIdAndEvaluatedItemId(Long associationId, String evaluatedItemId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Evaluation> query = cb.createQuery(Evaluation.class);
        Root<Evaluation> eval = query.from(Evaluation.class);
        query.where(cb.and(cb.equal(eval.get("associationId"), associationId),
                            cb.equal(eval.get("evaluatedItemId"), evaluatedItemId)));

        return session.createQuery(query).uniqueResultOptional();
    }

    public Optional<Evaluation> findByAssociationIdAndUserId(Long associationId, String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Evaluation> query = cb.createQuery(Evaluation.class);
        Root<Evaluation> eval = query.from(Evaluation.class);
        //Join<Evaluation, ToolItemRubricAssociation> ass = eval.join("association");
        query.where(cb.and(cb.equal(eval.get("associationId"), associationId),
                            cb.equal(eval.get("evaluatedItemOwnerId"), userId)));

        return session.createQuery(query).uniqueResultOptional();
    }

    public Optional<Evaluation> findByAssociation_ItemIdAndUserId(String itemId, String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Evaluation> query = cb.createQuery(Evaluation.class);
        Root<Evaluation> eval = query.from(Evaluation.class);
        Join<Evaluation, ToolItemRubricAssociation> ass = eval.join("association");
        query.where(cb.and(cb.equal(ass.get("itemId"), itemId),
                            cb.equal(eval.get("evaluatedItemOwnerId"), userId)));

        return session.createQuery(query).uniqueResultOptional();
    }

    public int deleteByToolItemRubricAssociation_Id(Long associationId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<Evaluation> delete = cb.createCriteriaDelete(Evaluation.class);
        Root<Evaluation> eval = delete.from(Evaluation.class);
        delete.where(cb.equal(eval.get("associationId"), associationId));
        
        return session.createQuery(delete).executeUpdate();
    }

    @Override
    public int deleteByOwnerId(String ownerId) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Evaluation> query = cb.createQuery(Evaluation.class);
        Root<Evaluation> root = query.from(Evaluation.class);
        query.where(cb.equal(root.get("ownerId"), ownerId));

        List<Evaluation> evaluations = session.createQuery(query).list();
        evaluations.forEach(session::delete);
        return evaluations.size();
    }
}
