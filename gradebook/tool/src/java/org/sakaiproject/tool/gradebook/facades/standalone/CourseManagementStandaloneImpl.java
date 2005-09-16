/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

    /**
	 * As a historical note, the Gradebook project began by trying to allow for as
	 * much optimization as possible. When displaying paged sortable tables of enrollment data
	 * which were sorted by an enrollment field, we could theoretically get a big performance boost
	 * by relying on the enrollment database query to optimize the necessary searching, sorting,
	 * and filtering. Therefore, we specified two (optional) methods to allow for that optimization.
	 *
	 * In actual fact, however, the Sakai 2.0 framework was unable to perform such
	 * optimization, and it wasn't clear when it would be. We also began to wonder if it
	 * was such a good idea to have performance profiles so radically change depending on what
	 * sorting criterion the user selected.
	 *
	 * Since in effect, the methods only did us good while running standalone, we've dropped
	 * them for Sakai 2.1. I'll leave these comments here until the next release, just in
	 * case anyone wonders what happened.
	 *
	 * For more on how these would actually be used, see the Sakai 2.0 EnrollmentTableBean
	 * and TestStandaloneCourseManagement classes.
     */
    /*
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
	*/

}
