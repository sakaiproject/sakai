/**********************************************************************************
*
* $Id$
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
package org.sakaiproject.component.sections;

import java.util.Date;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.sections.SampleManager;
import org.sakaiproject.tool.sections.CourseSectionImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class SampleComponent extends HibernateDaoSupport implements SampleManager {
    private static final Log log = LogFactory.getLog(SampleComponent.class);
    
    public String createSection(final String title) {
        // TODO Get the uuid from a uuid manager
        final String uuid = Long.toString((new Date()).getTime());
        log.info("Creating UUID = " + uuid);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
                CourseSectionImpl section = new CourseSectionImpl();
                section.setTitle(title);
                section.setUuid(uuid);
                session.save(section);
                return null;
            }
        };
            
        getHibernateTemplate().execute(hc);
        return uuid;
    }

    public void disbandSection(final String uuid) {
        log.info("Disbanding UUID = " + uuid);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from CourseSectionImpl as section where section.uuid=:uuid");
                q.setParameter("uuid", uuid);
                CourseSectionImpl section = (CourseSectionImpl)q.list().get(0);
                session.delete(section);
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    public List getSections() {
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from CourseSectionImpl as section");
                return q.list();
            }
        };
        return (List)getHibernateTemplate().execute(hc);
    }
}




