/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.facade.util;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaQuery;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class PagingUtilQueries implements PagingUtilQueriesAPI {

  @Setter private SessionFactory sessionFactory;

  public PagingUtilQueries () {
  }

  public List getAll(final int pageSize, final int pageNumber,
                                final String queryString, final Integer value) {

    Session session = sessionFactory.getCurrentSession();
    HibernateCriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Object> criteria = cb.createQuery(queryString, Object.class);
    Query<Object> q = session.createQuery(criteria);
    List page = new ArrayList<>();
    if (value != null) {
      q.setParameter(0, value.intValue());
    }
    try (ScrollableResults<Object> assessmentList = q.scroll()) {
      if (assessmentList.first()){ // check that result set is not empty
        int first = pageSize * (pageNumber - 1);
        int i = 0;
        assessmentList.setRowNumber(first);
        assessmentList.beforeFirst();
        while ( (pageSize > i++) && assessmentList.next()){
          log.debug("**** add "+i);
          page.add(assessmentList.get());
        }
      }
    }
    return page;
  }
  
  public List getAll(final int pageSize, final int pageNumber,
          final String queryString) {
	  return getAll(pageSize, pageNumber, queryString, null);
  }
}
