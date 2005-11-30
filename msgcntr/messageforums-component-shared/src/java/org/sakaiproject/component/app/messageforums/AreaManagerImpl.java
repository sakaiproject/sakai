package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;
import java.util.Date;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.kernel.id.IdManager;
import org.sakaiproject.api.kernel.session.SessionManager;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * @author rshastri
 * 
 */
public class AreaManagerImpl extends HibernateDaoSupport implements AreaManager {
    private static final Log LOG = LogFactory.getLog(AreaManagerImpl.class);

    private static final String QUERY_BY_CONTEXTID = "findAreaByContextId";

    private static final String CONTEXT_ID = "contextId";

    private static final String QUERY_AREA_BY_CONTEXT_ID = "findAreaByContextId";

    private static final String CURRENT_USER = "id";

    private IdManager idManager;

    private SessionManager sessionManager;

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public IdManager getIdManager() {
        return idManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public void init() {
        ;
    }

    // TODO: do we need to restrict by type here?
    public Area getPrivateArea() {
        return getAreaByContextId();
    }
    
    // TODO: do we need to restrict by type here?
    public Area getDiscusionArea() {
        return getAreaByContextId();        
    }

    public boolean isPrivateAreaEnabled() {
        return getPrivateArea().getEnabled().booleanValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sakaiproject.api.app.messageforums.AreaManager#getArea()
     */
    public Area getArea() {
        return getArea(getContextId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sakaiproject.api.app.messageforums.AreaManager#getArea(java.lang.String)
     */
    public Area getArea(final String contextId) {
        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_CONTEXTID);
                q.setParameter(CONTEXT_ID, contextId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (AreaImpl) getHibernateTemplate().execute(hcb);
    }

    public Area createArea() {
        Area area = new AreaImpl();
        area.setUuid(getNextUuid());
        area.setCreated(new Date());
        area.setCreatedBy(getCurrentUser());
        area.setContextId(getContextId());
        LOG.debug("createArea executed with areaId: " + area.getId());
        return area;
    }

    public void saveArea(Area area) {
        area.setModified(new Date());
        area.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(area);
        LOG.debug("saveArea executed with areaId: " + area.getId());
    }

    public void deleteArea(Area area) {
        getHibernateTemplate().delete(area);
        LOG.debug("deleteArea executed with areaId: " + area.getId());
    }

    /**
     * ContextId is present site id for now.
     * 
     * @return
     */
    private String getContextId() {
        Placement placement = ToolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }

    public Area getAreaByContextId() {
        LOG.debug("getPrivateArea executing for current user: " + getCurrentUser());
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_AREA_BY_CONTEXT_ID);
                q.setParameter(CONTEXT_ID, getContextId(), Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (Area) getHibernateTemplate().execute(hcb);
    }

    
    // helpers

    private String getCurrentUser() {
        try {
            return sessionManager.getCurrentSessionUserId();
        } catch (Exception e) {
            // TODO: remove after done testing -- needed for unit testing
            // is there a better way to get this when there is no session?
            return "testuser";
        }
    }

    private String getNextUuid() {
        return idManager.createUuid();
    }

}
