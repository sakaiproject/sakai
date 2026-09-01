/**
 * Copyright (c) 2020 The Apereo Foundation
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

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.SecureDeliveryData;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class SecureDeliveryFacadeQueries implements SecureDeliveryFacadeQueriesAPI {

  @Setter private SessionFactory sessionFactory;

  public List<SecureDeliveryData> getUrlsForAssessment(final Long assessmentId) {

    Session session = sessionFactory.getCurrentSession();
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<SecureDeliveryData> cq = cb.createQuery(SecureDeliveryData.class);
    Root<SecureDeliveryData> root = cq.from(SecureDeliveryData.class);
    cq.where(cb.equal(root.get("publishedAssessmentId"), assessmentId));

    return session.createQuery(cq).list();
  }

  public List<SecureDeliveryData> getUrlsForAssessmentAndUser(final Long assessmentId, final String agentId) {

    Session session = sessionFactory.getCurrentSession();
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<SecureDeliveryData> cq = cb.createQuery(SecureDeliveryData.class);
    Root<SecureDeliveryData> root = cq.from(SecureDeliveryData.class);
    cq.where(
        cb.equal(root.get("publishedAssessmentId"), assessmentId),
        cb.equal(root.get("agentId"), agentId));

    return session.createQuery(cq).list();
  }

  public void saveUrlsForAssessmentAndUser(Long assessmentId, String agentId, String instructorUrl, String studentUrl) {
    Session session = sessionFactory.getCurrentSession();
    SecureDeliveryData sd = new SecureDeliveryData();
    sd.setPublishedAssessmentId(assessmentId);
    sd.setAgentId(agentId);
    sd.setInstructorUrl(instructorUrl);
    sd.setStudentUrl(studentUrl);
    sd.setCreatedDate(new Date());
    session.merge(sd);
  }

}
