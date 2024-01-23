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

import org.hibernate.query.Query;
import org.sakaiproject.tool.assessment.data.dao.assessment.SebValidationData;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

@SuppressWarnings("unchecked")
public class SebValidationFacadeQueries extends HibernateDaoSupport implements SebValidationFacadeQueriesAPI {

  public Optional<SebValidationData> getLastSebValidation(final Long assessmentId, final String agentId) {

    HibernateCallback<Optional<SebValidationData>> hibernateCallback = session -> {
      Query<SebValidationData> query = session.getNamedQuery(QUERY_GET_ENTRY_FOR_ASSESSMENT_AND_AGENT);
      query.setParameter("publishedId", assessmentId);
      query.setParameter("agentId", agentId);
      query.setMaxResults(1);
      return query.uniqueResultOptional();
    };

    return getHibernateTemplate().execute(hibernateCallback);
  }

  public void saveSebValidation(Long assessmentId, String agentId, String url, String configKeyHash, String examKeyHash) {
    getHibernateTemplate().save(new SebValidationData(null, assessmentId, false, agentId, url, configKeyHash, examKeyHash));
  }

  public void expireSebValidations(Long assessmentId, String agentId) {
    HibernateCallback<Boolean> hibernateCallback = session -> {
      Query<SebValidationData> query = session.getNamedQuery(QUERY_EXPIRE_CONFIGS_FOR_ASSESSMENT_AND_AGENT);
      query.setParameter("publishedId", assessmentId);
      query.setParameter("agentId", agentId);
      // Get query results and return the last
      query.executeUpdate();
      return true;
    };
    getHibernateTemplate().execute(hibernateCallback);
  }

}
