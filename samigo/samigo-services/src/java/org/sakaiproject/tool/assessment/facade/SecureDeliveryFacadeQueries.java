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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.tool.assessment.data.dao.grading.SecureDeliveryData;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

@Slf4j
public class SecureDeliveryFacadeQueries extends HibernateDaoSupport implements SecureDeliveryFacadeQueriesAPI {

  public List<SecureDeliveryData> getUrlsForAssessment(final Long assessmentId) {
    try {
      HibernateCallback hcb = (Session s) -> {
        Query q = s.getNamedQuery(QUERY_GET_URLS_FOR_ASSESSMENT);
        q.setParameter("publishedId", assessmentId);
        return q.list();
      };
    } catch (DataAccessException e) {
      log.error("Failed to retrieve URLs from SecureDelivery table for assessmentId={}", assessmentId);
    }
    return null;
  }

  public List<SecureDeliveryData> getUrlsForAssessmentAndUser(final Long assessmentId, final String agentId) {
    try {
      HibernateCallback hcb = (Session s) -> {
        Query q = s.getNamedQuery(QUERY_GET_URLS_FOR_ASSESSMENT_AND_USER);
        q.setParameter("publishedId", "assessmentId");
        q.setParameter("agentId", agentId);
        return q.list();
      };
    } catch (DataAccessException e) {
      log.error("Failed to retrieve URLs from SecureDelivery table for assessmentId={}", assessmentId);
    }
    return null;
  }

  public void saveUrlsForAssessmentAndUser(Long assessmentId, String agentId, String instructorUrl, String studentUrl) {
    SecureDeliveryData sd = new SecureDeliveryData();
    sd.setPublishedAssessmentId(assessmentId);
    sd.setAgentId(agentId);
    sd.setInstructorUrl(instructorUrl);
    sd.setStudentUrl(studentUrl);
    sd.setCreatedDate(new Date());
    getHibernateTemplate().save(sd);
  }

}
