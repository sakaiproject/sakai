/**********************************************************************************
 * $URL: $
 * $Id:  $
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

package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class MessageForumsAreaManagerImpl extends HibernateDaoSupport implements MessageForumsAreaManager {
    
    private static final Log LOG = LogFactory.getLog(MessageForumsAreaManagerImpl.class);    

    private static final String QUERY_PRIVATE_AREA_BY_CURRENT_USER = "findPrivateAreaByCurrentUser";
    private static final String QUERY_DISCUSSION_AREA_BY_CURRENT_USER = "findDiscussionAreaByCurrentUser";
    private static final String CURRENT_USER = "id";

    public void init() {
        ;
    }
    
    public boolean isPrivateAreaEnabled() {
        return getPrivateArea().getEnabled().booleanValue();
    }
    
    public void saveArea(Area area) {
        getHibernateTemplate().saveOrUpdate(area);
        LOG.debug("saveArea executed with areaId: " + area.getId());                
    }
    
    public void deleteArea(Area area) {
        getHibernateTemplate().delete(area);
        LOG.debug("deleteArea executed with areaId: " + area.getId());                
    }
    
    // TODO: how do we tell hibernate to get the private area type and only for a 
    //       certain user (the current user)?
    public Area getPrivateArea() {
        LOG.debug("getPrivateArea executing for current user: " + getCurrentUser());
        return getArea(QUERY_PRIVATE_AREA_BY_CURRENT_USER);
    }
    
    // TODO: how do we tell hibernate to get the discussion area type and only for a 
    //       certain user (the current user)?
    public Area getDiscussionForumArea() {
        LOG.debug("getDiscussionForumArea executing for current user: " + getCurrentUser());
        return getArea(QUERY_DISCUSSION_AREA_BY_CURRENT_USER);
    }
    
    private Area getArea(final String query) {
        LOG.debug("getArea executing for current user: " + getCurrentUser());

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(query);
                q.setParameter(CURRENT_USER, getCurrentUser(), Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (Area) getHibernateTemplate().execute(hcb);
    }
    
    // helpers
    
    private String getCurrentUser() {
        // TODO: add the session manager back
        return "joe"; //SessionManager.getCurrentSession().getUserEid();
    }

}
