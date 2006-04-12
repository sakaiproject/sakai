/**********************************************************************************
*
* $Header$
*
***********************************************************************************
*
* Copyright (c) 2005 University of Cambridge
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

package uk.ac.cam.caret.sakai.rwiki.component.message.dao.impl;

import java.util.Date;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;

import org.sakaiproject.service.framework.log.Logger;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.message.model.TriggerImpl;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author ieb
 *
 */
public class TriggerDaoImpl extends HibernateDaoSupport implements
TriggerDao {
    private Logger log;

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#createTrigger(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public Trigger createTrigger(String pageName, String pageSpace, String triggerSpec, String user) {
        Trigger t = new TriggerImpl();
        t.setLastseen(new Date());
        t.setPagename(pageName);
        t.setPagespace(pageSpace);
        t.setTriggerspec(triggerSpec);
        t.setUser(user);
        return t;
    }
    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#findByUser(java.lang.String)
     */
    public List findByUser(final String user) {
        long start = System.currentTimeMillis();
        try {
            // there is no point in sorting by version, since there is only one version in 
            // this table.
            // also using like is much slower than eq
            HibernateCallback callback = new HibernateCallback() {
                public Object doInHibernate(Session session)
                throws HibernateException {
                    return session.createCriteria(Trigger.class).add(
                            Expression.eq("user", user)).list();
                }
            };
            return (List)getHibernateTemplate().execute(callback);
        } finally {
            long finish = System.currentTimeMillis();
            TimeLogger.printTimer("PagePresenceDaoImpl.findByUser: " + user,start,finish);
        }    
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#findBySpace(java.lang.String)
     */
    public List findBySpace(final String space) {
        long start = System.currentTimeMillis();
        try {
            // there is no point in sorting by version, since there is only one version in 
            // this table.
            // also using like is much slower than eq
            HibernateCallback callback = new HibernateCallback() {
                public Object doInHibernate(Session session)
                throws HibernateException {
                    return session.createCriteria(Trigger.class).add(
                            Expression.eq("pagespage", space)).list();
                }
            };
            return (List)getHibernateTemplate().execute(callback);
        } finally {
            long finish = System.currentTimeMillis();
            TimeLogger.printTimer("PagePresenceDaoImpl.findBySpace: " + space,start,finish);
        }    
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#findByPage(java.lang.String, java.lang.String)
     */
    public List findByPage(final String space, final String page) {
        long start = System.currentTimeMillis();
        try {
            // there is no point in sorting by version, since there is only one version in 
            // this table.
            // also using like is much slower than eq
            HibernateCallback callback = new HibernateCallback() {
                public Object doInHibernate(Session session)
                throws HibernateException {
                    return session.createCriteria(Trigger.class).add(
                            Expression.eq("pagespage", space)).add(
                                    Expression.eq("pagename",page)).list();
                }
            };
            return (List)getHibernateTemplate().execute(callback);
        } finally {
            long finish = System.currentTimeMillis();
            TimeLogger.printTimer("PagePresenceDaoImpl.findByPage: " + space + ":"+ page,start,finish);
        }    
    }
    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#update(java.lang.Object)
     */
    public void update(Object o) {
        getHibernateTemplate().saveOrUpdate(o);        
    }
    
    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }


}
