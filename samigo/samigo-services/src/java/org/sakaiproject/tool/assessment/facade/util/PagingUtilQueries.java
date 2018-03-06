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

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

@Slf4j
public class PagingUtilQueries
    extends HibernateDaoSupport implements PagingUtilQueriesAPI{

  public PagingUtilQueries () {
  }

  public List getAll(final int pageSize, final int pageNumber,
                                final String queryString, final Integer value) {

    HibernateCallback callback = new HibernateCallback(){
       public Object doInHibernate(Session session) throws HibernateException{
         ArrayList page = new ArrayList();
         Query q = session.createQuery(queryString);
         if (value != null) {
        	 q.setInteger(0, value.intValue());
         }
         ScrollableResults assessmentList = q.scroll();
         if (assessmentList.first()){ // check that result set is not empty
           int first = pageSize * (pageNumber - 1);
           int i = 0;
           assessmentList.setRowNumber(first);
           assessmentList.beforeFirst();
           while ( (pageSize > i++) && assessmentList.next()){
             log.debug("**** add "+i);
             page.add(assessmentList.get(0));
           }
         }
         return page;
       }
    };
    List pageList = (List) getHibernateTemplate().execute(callback);
    return pageList;
  }
  
  public List getAll(final int pageSize, final int pageNumber,
          final String queryString) {
	  return getAll(pageSize, pageNumber, queryString, null);
  }
}
