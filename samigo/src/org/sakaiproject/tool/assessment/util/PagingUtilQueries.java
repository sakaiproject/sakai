package org.sakaiproject.tool.assessment.util;

import java.util.ArrayList;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class PagingUtilQueries
    extends HibernateDaoSupport implements PagingUtilQueriesAPI{
  private static Logger LOG =
    Logger.getLogger(PagingUtilQueries.class.getName());

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
             System.out.println("**** add "+i);
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
