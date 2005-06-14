package org.sakaiproject.tool.assessment.facade.authz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.hibernate.Hibernate;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.authz.QualifierData;
import org.sakaiproject.tool.assessment.facade.DataFacadeException;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


public class AuthorizationFacadeQueries
   extends HibernateDaoSupport implements AuthorizationFacadeQueriesAPI{

  public AuthorizationFacadeQueries() {
  }

  public QualifierIteratorFacade getQualifierParents(String qualifierId) {
    List parents = getHibernateTemplate().find(
        "select p from QualifierData as p, QualifierData as c, QualifierHierarchyData as q where p.qualifierId=q.parentId and c.qualifierId=q.childId and q.childId=?",
        new Object[] {qualifierId}
        ,
        new net.sf.hibernate.type.Type[] {Hibernate.STRING});
    // turn them to Facade
    ArrayList a = new ArrayList();
    for (int i = 0; i < parents.size(); i++) {
      QualifierData data = (QualifierData) parents.get(i);
      QualifierFacade qf = new QualifierFacade(data);
      a.add(qf);
    }
    //System.out.println("parent size = "+a.size());
    return new QualifierIteratorFacade(a);
  }

  public QualifierIteratorFacade getQualifierChildren(String qualifierId) {
    List children = getHibernateTemplate().find(
        "select p from QualifierData as p, QualifierData as c, QualifierHierarchyData as q where p.qualifierId=q.parentId and c.qualifierId=q.childId and q.parentId=?",
        new Object[] {qualifierId},
        new net.sf.hibernate.type.Type[] {Hibernate.STRING});
    // turn them to Facade
    ArrayList a = new ArrayList();
    for (int i = 0; i < children.size(); i++) {
      QualifierData data = (QualifierData) children.get(i);
      QualifierFacade qf = new QualifierFacade(data);
      a.add(qf);
    }
    //System.out.println("children size = "+a.size());
    return new QualifierIteratorFacade(a);
  }

  public void showQualifiers(QualifierIteratorFacade iter){
    while (iter.hasNextQualifier()){
      QualifierFacade q = (QualifierFacade)iter.nextQualifier();
      //System.out.println(q.getDisplayName());
    }
  }

  public void addAuthz(AuthorizationFacade a) {
    AuthorizationData data = (AuthorizationData) a.getData();
    getHibernateTemplate().save(data);
  }

  public void addQualifier(QualifierFacade q) {
    QualifierData data = (QualifierData) q.getData();
    getHibernateTemplate().save(data);
  }

  public static void main(String[] args) throws DataFacadeException {
    AuthorizationFacadeQueriesAPI instance = new AuthorizationFacadeQueries();
    if (args[0].equals("listChild")) {
      QualifierIteratorFacade childrenIter = instance.getQualifierChildren(args[1]);
      instance.showQualifiers(childrenIter);
    }
    if (args[0].equals("addAuthz")) {
      AuthorizationFacade a = new AuthorizationFacade(args[1], args[2], args[3], new Date(),
        null,"2",new Date(),new Boolean("true"));
      instance.addAuthz(a);
    }
    System.exit(0);
  }

}
