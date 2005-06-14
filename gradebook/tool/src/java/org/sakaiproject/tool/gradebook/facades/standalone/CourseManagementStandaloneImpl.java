/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/java/org/sakaiproject/tool/gradebook/facades/standalone/CourseManagementStandaloneImpl.java,v 1.4 2005/06/11 17:40:00 ray.media.berkeley.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.gradebook.facades.standalone;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * A Standalone implementation of CourseManagement.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class CourseManagementStandaloneImpl extends HibernateDaoSupport implements CourseManagement {
    private static final Log log = LogFactory.getLog(CourseManagementStandaloneImpl.class);

    /**
     * Returns all enrollments in the specified gradebook
     */
	public Set getEnrollments(final String gradebookUid) {
		return (Set)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List list = session.find(
					"select enr from EnrollmentStandalone enr, Gradebook gb where gb.uid=? and enr.gradebook=gb",
					new Object[] {gradebookUid},
					new Type[] {Hibernate.STRING});
				return new HashSet(list);
			}
		});
	}

	public Set findEnrollmentsByUserUids(final String gradebookUid, final Collection userUids) {
		return (Set)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query q = session.createQuery("select enr from EnrollmentStandalone enr, Gradebook gb where gb.uid=:gbuid and enr.gradebook=gb and enr.user.userUid in (:userUids)");
				q.setString("gbuid", gradebookUid);
				q.setParameterList("userUids", userUids);
				return new HashSet(q.list());
			}
		});
	}

	public int getEnrollmentsSize(final String gradebookUid) {
		Integer size = (Integer)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List list = session.find(
					"select count(enr) from EnrollmentStandalone enr, Gradebook gb where gb.uid=? and enr.gradebook=gb",
					new Object[] {gradebookUid},
					new Type[] {Hibernate.STRING});
				return (Integer)list.get(0);
			}
		});
		return size.intValue();
	}

    /**
     */
    public Set findEnrollmentsByStudentDisplayUid(final String gradebookUid, final String studentDisplayUid) {
		return (Set)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				String hql = "select enr from EnrollmentStandalone enr, UserStandalone user, Gradebook gb where gb.uid=:gbuid and enr.gradebook=gb and enr.user=user and (user.displayName like :name or user.sortName like :name)";
                Query q = session.createQuery(hql);
                q.setString("gbuid", gradebookUid);
                q.setString("name", "%" + studentDisplayUid + "%"); // Use % for the like query
                return new HashSet(q.list());
			}
		});
	}

    /**
     */
    public Set findEnrollmentsByStudentNameOrDisplayUid(final String gradebookUid, final String studentNameQuery) {
		return (Set)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				StringBuffer hql = new StringBuffer("select enr from EnrollmentStandalone enr, UserStandalone user, Gradebook gb ");
                hql.append("where enr.gradebook=gb and gb.uid=:gbuid and ");
                hql.append("enr.user=user and ");
                hql.append("(lower(user.displayName) like :query or lower(user.sortName) like :query or lower(user.displayUid) like :query) ");
				Query q = session.createQuery(hql.toString());
				q.setString("gbuid", gradebookUid);
				q.setString("query", studentNameQuery.toLowerCase() + "%");
				return new HashSet(q.list());
			}
		});
	}

	public List findEnrollmentsPagedBySortName(final String gradebookUid, final int startRange, final int rangeMaximum, final boolean isAscending) {
		return (List)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				StringBuffer hql = new StringBuffer("select enr from EnrollmentStandalone enr, UserStandalone user, Gradebook gb ");
                hql.append("where enr.gradebook=gb and gb.uid=:gbuid and enr.user=user ");
                hql.append("order by lower(user.sortName) ");
                hql.append(isAscending ? "asc" : "desc");
				Query q = session.createQuery(hql.toString());
				q.setString("gbuid", gradebookUid);
				q.setFirstResult(startRange);
				q.setMaxResults(rangeMaximum);
				return q.list();
			}
		});
	}

	public List findEnrollmentsPagedByDisplayUid(final String gradebookUid, final int startRange, final int rangeMaximum, final boolean isAscending) {
		return (List)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				StringBuffer hql = new StringBuffer("select enr from EnrollmentStandalone enr, UserStandalone user, Gradebook gb ");
                hql.append("where enr.gradebook=gb and gb.uid=:gbuid and enr.user=user ");
                hql.append("order by lower(user.displayUid) ");
                hql.append(isAscending ? "asc" : "desc");
				Query q = session.createQuery(hql.toString());
				q.setString("gbuid", gradebookUid);
				q.setFirstResult(startRange);
				q.setMaxResults(rangeMaximum);
				return q.list();
			}
		});
	}

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/java/org/sakaiproject/tool/gradebook/facades/standalone/CourseManagementStandaloneImpl.java,v 1.4 2005/06/11 17:40:00 ray.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
