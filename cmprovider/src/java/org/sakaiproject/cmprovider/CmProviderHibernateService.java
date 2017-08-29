/**
 * Copyright (c) 2015-2017 The Apereo Foundation
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
package org.sakaiproject.cmprovider;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.coursemanagement.impl.EnrollmentSetCmImpl;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * Service for any custom hibernate calls that are needed for the course management providers.
 *
 * @author Christopher Schauer
 */
public class CmProviderHibernateService extends HibernateDaoSupport {
  /**
   * Needed to get course offering eid in the output for an enrollment set.
   */
  public EnrollmentSetCmImpl getEnrollmentSetByEid(final String eid) {
    HibernateCallback hc = session -> {
      StringBuilder hql = new StringBuilder();
      hql.append("from ").append(EnrollmentSetCmImpl.class.getName()).append(" as obj where obj.eid=:eid");
      Query q = session.createQuery(hql.toString());
      q.setParameter("eid", eid);
      EnrollmentSetCmImpl result = (EnrollmentSetCmImpl) q.uniqueResult();
      if (result == null) {
        throw new IdNotFoundException(eid, EnrollmentSetCmImpl.class.getName());
      }
      Hibernate.initialize(result.getCourseOffering());
      return result;
    };
    return (EnrollmentSetCmImpl) getHibernateTemplate().execute(hc);
  }
}
