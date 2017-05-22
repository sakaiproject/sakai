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
