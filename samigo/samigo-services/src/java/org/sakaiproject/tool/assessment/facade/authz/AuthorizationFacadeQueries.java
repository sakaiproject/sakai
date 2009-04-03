/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/authz/AuthorizationFacadeQueries.java $
 * $Id: AuthorizationFacadeQueries.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade.authz;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.authz.QualifierData;
import org.sakaiproject.tool.assessment.data.ifc.authz.AuthorizationIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIfc;
import org.sakaiproject.tool.assessment.facade.DataFacadeException;
import org.sakaiproject.tool.assessment.facade.authz.AuthorizationFacade;
import org.sakaiproject.tool.assessment.facade.authz.QualifierFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthorizationFacadeQueries
   extends HibernateDaoSupport implements AuthorizationFacadeQueriesAPI{

  private static Log log = LogFactory.getLog(AuthorizationFacadeQueries.class);

  public AuthorizationFacadeQueries() {
  }

  public QualifierIteratorFacade getQualifierParents(final String qualifierId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select p from QualifierData as p, QualifierData as c, QualifierHierarchyData as q where p.qualifierId=q.parentId and c.qualifierId=q.childId and q.childId=?");
	    		q.setString(0, qualifierId);
	    		return q.list();
	    	};
	    };
	    List parents = getHibernateTemplate().executeFind(hcb);

//    List parents = getHibernateTemplate().find(
//        "select p from QualifierData as p, QualifierData as c, QualifierHierarchyData as q where p.qualifierId=q.parentId and c.qualifierId=q.childId and q.childId=?",
//        new Object[] {qualifierId}
//        ,
//        new org.hibernate.type.Type[] {Hibernate.STRING});
    // turn them to Facade
    ArrayList a = new ArrayList();
    for (int i = 0; i < parents.size(); i++) {
      QualifierData data = (QualifierData) parents.get(i);
      QualifierFacade qf = new QualifierFacade(data);
      a.add(qf);
    }
    return new QualifierIteratorFacade(a);
  }

  public QualifierIteratorFacade getQualifierChildren(final String qualifierId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select p from QualifierData as p, QualifierData as c, QualifierHierarchyData as q where p.qualifierId=q.parentId and c.qualifierId=q.childId and q.parentId=?");
	    		q.setString(0, qualifierId);
	    		return q.list();
	    	};
	    };
	    List children = getHibernateTemplate().executeFind(hcb);

//    List children = getHibernateTemplate().find(
//        "select p from QualifierData as p, QualifierData as c, QualifierHierarchyData as q where p.qualifierId=q.parentId and c.qualifierId=q.childId and q.parentId=?",
//        new Object[] {qualifierId},
//        new org.hibernate.type.Type[] {Hibernate.STRING});
    // turn them to Facade
    ArrayList a = new ArrayList();
    for (int i = 0; i < children.size(); i++) {
      QualifierData data = (QualifierData) children.get(i);
      QualifierFacade qf = new QualifierFacade(data);
      a.add(qf);
    }
    return new QualifierIteratorFacade(a);
  }

  public void showQualifiers(QualifierIteratorFacade iter){
    while (iter.hasNextQualifier()){
      //QualifierFacade q = (QualifierFacade)iter.nextQualifier();
    }
  }

  public void addAuthz(AuthorizationIfc a) {
    AuthorizationData data;
    if (a instanceof AuthorizationFacade)
      data  = (AuthorizationData)((AuthorizationFacade) a).getData();
    else
      data = (AuthorizationData)a;

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
       getHibernateTemplate().save(data);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem adding authorization: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }

  public void addQualifier(QualifierIfc q) {
    QualifierData data;
    if (q instanceof QualifierFacade)
      data = (QualifierData)((QualifierFacade) q).getData();
    else
      data = (QualifierData) q;

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().save(data);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem adding Qualifier: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }

  public static void main(String[] args) throws DataFacadeException {
    AuthorizationFacadeQueriesAPI instance = new AuthorizationFacadeQueries();
    if (args[0].equals("listChild")) {
      QualifierIteratorFacade childrenIter = instance.getQualifierChildren(args[1]);
      instance.showQualifiers(childrenIter);
    }
    if (args[0].equals("addAuthz")) {
      AuthorizationFacade a = new AuthorizationFacade(args[1], args[2], args[3], new Date(),
        null,"2",new Date(),Boolean.TRUE);
      instance.addAuthz(a);
    }
    System.exit(0);
  }

}
