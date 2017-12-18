/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade.authz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.authz.QualifierData;
import org.sakaiproject.tool.assessment.data.ifc.authz.AuthorizationIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIfc;
import org.sakaiproject.tool.assessment.facade.DataFacadeException;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

@Slf4j
public class AuthorizationFacadeQueries extends HibernateDaoSupport implements AuthorizationFacadeQueriesAPI{

  public AuthorizationFacadeQueries() {
  }

  public QualifierIteratorFacade getQualifierParents(final String qualifierId) {
    final HibernateCallback<List<QualifierData>> hcb = session -> session
            .createQuery("select p from QualifierData as p, QualifierData as c, QualifierHierarchyData as q " +
                    "where p.qualifierId = q.parentId and c.qualifierId = q.childId and q.childId = :id")
            .setString("id", qualifierId)
            .list();
    List<QualifierData> parents = getHibernateTemplate().execute(hcb);

    List<QualifierFacade> a = new ArrayList<>();
    for (QualifierData data : parents) {
      QualifierFacade qf = new QualifierFacade(data);
      a.add(qf);
    }
    return new QualifierIteratorFacade(a);
  }

  public QualifierIteratorFacade getQualifierChildren(final String qualifierId) {
    final HibernateCallback<List<QualifierData>> hcb = session -> session
            .createQuery("select p from QualifierData as p, QualifierData as c, QualifierHierarchyData as q " +
                    "where p.qualifierId = q.parentId and c.qualifierId = q.childId and q.parentId = :id")
            .setString("id", qualifierId)
            .list();
    List<QualifierData> children = getHibernateTemplate().execute(hcb);

    List<QualifierFacade> a = new ArrayList<>();
    for (QualifierData data : children) {
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

    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
       getHibernateTemplate().save(data);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem adding authorization: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
  }

  public void addQualifier(QualifierIfc q) {
    QualifierData data;
    if (q instanceof QualifierFacade)
      data = (QualifierData)((QualifierFacade) q).getData();
    else
      data = (QualifierData) q;

    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount().intValue();
    while (retryCount > 0){ 
      try {
        getHibernateTemplate().save(data);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem adding Qualifier: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
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
