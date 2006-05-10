package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.UserPermissionManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class AreaManagerImpl extends HibernateDaoSupport implements AreaManager {
    private static final Log LOG = LogFactory.getLog(AreaManagerImpl.class);

    private static final String QUERY_AREA_BY_CONTEXT_AND_TYPE_ID = "findAreaByContextIdAndTypeId";
    private static final String QUERY_AREA_BY_TYPE = "findAreaByType";
    
    private IdManager idManager;

    private MessageForumsForumManager forumManager;

    private SessionManager sessionManager;

    private MessageForumsTypeManager typeManager;

    private EventTrackingService eventTrackingService;

    private UserPermissionManager userPermissionManager;

    public void init() {
        ;
    }

    public UserPermissionManager getUserPermissionManager() {
        // userPermissionManager can not be injected by spring because of
        // circluar dependancies so it is loaded by the BeanFactory instead
//        if (userPermissionManager == null) {
//            try {
//                org.springframework.core.io.Resource resource = new InputStreamResource(urlResource.openStream(), classpathUrl);
//                BeanFactory beanFactory = new XmlBeanFactory(resource);
//                userPermissionManager = (UserPermissionManagerImpl) beanFactory.getBean(UserPermissionManager.BEAN_NAME);
//            } catch (Exception e) {
//                LOG.debug("Unable to load classpath resource: " + classpathUrl);
//            }
//        }
        return userPermissionManager;
    }

    public EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public MessageForumsTypeManager getTypeManager() {
        return typeManager;
    }

    public void setTypeManager(MessageForumsTypeManager typeManager) {
        this.typeManager = typeManager;
    }

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

    public void setForumManager(MessageForumsForumManager forumManager) {
        this.forumManager = forumManager;
    }

    public Area getPrivateArea() {
        Area area = getAreaByContextIdAndTypeId(typeManager.getPrivateMessageAreaType());
        if (area == null) {
            area = createArea(typeManager.getPrivateMessageAreaType());
            area.setContextId(getContextId());
            area.setName("Private Messages");
            area.setEnabled(Boolean.FALSE);
            area.setHidden(Boolean.TRUE);
            area.setLocked(Boolean.FALSE);
            saveArea(area);
        }

        return area;
    }

    public Area getDiscusionArea() {
        Area area = getAreaByContextIdAndTypeId(typeManager.getDiscussionForumType());
        if (area == null) {
            area = createArea(typeManager.getDiscussionForumType());
            area.setName("Disucssion Area");
            area.setEnabled(Boolean.TRUE);
            area.setHidden(Boolean.TRUE);
            area.setLocked(Boolean.FALSE);
            saveArea(area);
        }
        return area;
    }

    public boolean isPrivateAreaEnabled() {
        return getPrivateArea().getEnabled().booleanValue();
    }

    public Area createArea(String typeId) {
        Area area = new AreaImpl();
        area.setUuid(getNextUuid());
        area.setTypeUuid(typeId);
        area.setCreated(new Date());
        area.setCreatedBy(getCurrentUser());
        area.setContextId(getContextId());
        LOG.debug("createArea executed with areaId: " + area.getUuid());
        return area;
    }

    public void saveArea(Area area) {
        boolean isNew = area.getId() == null;

        area.setModified(new Date());
        area.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(area);

        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_ADD, getEventMessage(area), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_WRITE, getEventMessage(area), false));
        }

        LOG.debug("saveArea executed with areaId: " + area.getId());
    }

    public void deleteArea(Area area) {
        eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_REMOVE, getEventMessage(area), false));
        getHibernateTemplate().delete(area);
        LOG.debug("deleteArea executed with areaId: " + area.getId());
    }

    /**
     * ContextId is present site id for now.
     */
    private String getContextId() {
        if (TestUtil.isRunningTests()) {
            return "test-context";
        }
        Placement placement = ToolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }

    public Area getAreaByContextIdAndTypeId(final String typeId) {
        LOG.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_AREA_BY_CONTEXT_AND_TYPE_ID);
                q.setParameter("contextId", getContextId(), Hibernate.STRING);
                q.setParameter("typeId", typeId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (Area) getHibernateTemplate().execute(hcb);
    }

    public Area getAreaByType(final String typeId) {
      final String currentUser = getCurrentUser();
      LOG.debug("getAreaByContextAndTypeForUser executing for current user: " + currentUser);
      HibernateCallback hcb = new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
              Query q = session.getNamedQuery(QUERY_AREA_BY_TYPE);              
              q.setParameter("typeId", typeId, Hibernate.STRING);              
              return q.uniqueResult();
          }
      };        
      return (Area) getHibernateTemplate().execute(hcb);
    }
       
    // helpers

    private String getNextUuid() {
        return idManager.createUuid();
    }

    private String getCurrentUser() {
    	String user = sessionManager.getCurrentSessionUserId();
  		return (user == null) ? "test-user" : user;
    }

    private String getEventMessage(Object object) {
        return "MessageCenter::" + getCurrentUser() + "::" + object.toString();
    }

}
