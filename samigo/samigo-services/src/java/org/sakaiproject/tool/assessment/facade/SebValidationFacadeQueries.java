/**
 * Copyright (c) ${license.git.copyrightYears} ${holder}
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
package org.sakaiproject.tool.assessment.facade;

import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.sakaiproject.tool.assessment.data.dao.assessment.SebValidationData;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.Setter;

@SuppressWarnings("unchecked")
@Transactional
public class SebValidationFacadeQueries implements SebValidationFacadeQueriesAPI {

  @Setter private SessionFactory sessionFactory;

  public Optional<SebValidationData> getLastSebValidation(final Long assessmentId, final String agentId) {

    Session session = sessionFactory.getCurrentSession();
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<SebValidationData> cq = cb.createQuery(SebValidationData.class);
    Root<SebValidationData> root = cq.from(SebValidationData.class);
    cq.where(
        cb.equal(root.get("publishedAssessmentId"), assessmentId),
        cb.equal(root.get("agentId"), agentId));
    cq.orderBy(cb.desc(root.get("id")));
    Query<SebValidationData> query = session.createQuery(cq);
    query.setMaxResults(1);

    return query.uniqueResultOptional();
  }

  public void saveSebValidation(Long assessmentId, String agentId, String url, String configKeyHash, String examKeyHash) {
    Session session = sessionFactory.getCurrentSession();
    session.persist(new SebValidationData(null, assessmentId, false, agentId, url, configKeyHash, examKeyHash));
  }

  public void expireSebValidations(Long assessmentId, String agentId) {
    Session session = sessionFactory.getCurrentSession();
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaUpdate<SebValidationData> cu = cb.createCriteriaUpdate(SebValidationData.class);
    Root<SebValidationData> root = cu.from(SebValidationData.class);
    cu.set(root.get("expired"), true);
    cu.where(
        cb.equal(root.get("publishedAssessmentId"), assessmentId),
        cb.equal(root.get("agentId"), agentId));

    session.createQuery(cu).executeUpdate();
  }

}
