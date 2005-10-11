/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;

public class PagingUtilQueries
    extends HibernateDaoSupport implements PagingUtilQueriesAPI{
    private static Log log = LogFactory.getLog(PagingUtilQueries.class);

  public PagingUtilQueries () {
  }

  public List getAll(final int pageSize, final int pageNumber,
                                final String queryString) {

    HibernateCallback callback = new HibernateCallback(){
       public Object doInHibernate(Session session) throws HibernateException{
         ArrayList page = new ArrayList();
         Query q = session.createQuery(queryString);
         ScrollableResults assessmentList = q.scroll();
         if (assessmentList.first()){ // check that result set is not empty
           int first = pageSize * (pageNumber - 1);
           int i = 0;
           assessmentList.setRowNumber(first);
           assessmentList.beforeFirst();
           while ( (pageSize > i++) && assessmentList.next()){
             log.info("**** add "+i);
             page.add(assessmentList.get(0));
           }
         }
         return page;
       }
    };
    List pageList = (List) getHibernateTemplate().execute(callback);
    return pageList;
  }

}
