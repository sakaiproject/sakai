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

import java.util.Optional;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.rubrics.api.model.ReturnedEvaluation;
import org.sakaiproject.rubrics.api.repository.ReturnedEvaluationRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class ReturnedEvaluationRepositoryImpl extends SpringCrudRepositoryImpl<ReturnedEvaluation, Long> implements ReturnedEvaluationRepository {

    public Optional<ReturnedEvaluation> findByOriginalEvaluationId(Long originalEvaluationId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ReturnedEvaluation> query = cb.createQuery(ReturnedEvaluation.class);
        Root<ReturnedEvaluation> eval = query.from(ReturnedEvaluation.class);
        query.where(cb.equal(eval.get("originalEvaluationId"), originalEvaluationId));

        return session.createQuery(query).uniqueResultOptional();
    }
}
