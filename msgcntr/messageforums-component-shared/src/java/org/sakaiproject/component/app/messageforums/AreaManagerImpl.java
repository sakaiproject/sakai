package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;
 
/**
 * @author rshastri
 *
 */
public class AreaManagerImpl extends HibernateDaoSupport implements AreaManager
{
  private static final Log LOG = LogFactory.getLog(AreaManagerImpl.class);
  private static final String QUERY_BY_CONTEXTID = "findAreaByContextId";
  private static final String CONTEXT_ID = "contextId";
  private static final String QUERY_PRIVATE_AREA_BY_CURRENT_USER = "findPrivateAreaByCurrentUser";
  private static final String QUERY_DISCUSSION_AREA_BY_CURRENT_USER = "findDiscussionAreaByCurrentUser";
  private static final String CURRENT_USER = "id";

  public void init() {
      ;
  }
  
  public boolean isPrivateAreaEnabled() {
      return getPrivateArea().getEnabled().booleanValue();
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.AreaManager#getArea()
   */
  public Area getArea()
  {    
    return getArea(getContextId());
  }


  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.AreaManager#getArea(java.lang.String)
   */
  public Area getArea(final String contextId)
  {
    if (contextId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
          
    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_BY_CONTEXTID);                        
        q.setParameter(CONTEXT_ID, contextId, Hibernate.STRING);                   
        return q.uniqueResult();
      }
    };
        
    return (AreaImpl) getHibernateTemplate().execute(hcb);
  }

  public void saveArea(Area area) {
      getHibernateTemplate().saveOrUpdate(area);
      LOG.debug("saveArea executed with areaId: " + area.getId());                
  }
  
  public void deleteArea(Area area) {
      getHibernateTemplate().delete(area);
      LOG.debug("deleteArea executed with areaId: " + area.getId());                
  }
  
  /**
   * ContextId is present site id for now.
   * @return
   */
  private String getContextId()
  {
    Placement placement = ToolManager.getCurrentPlacement();
    String presentSiteId = placement.getContext();
    return presentSiteId;
  }
  
  // TODO: how do we tell hibernate to get the private area type and only for a 
  //       certain user (the current user)?
  public Area getPrivateArea() 
  {
    LOG.debug("getPrivateArea executing for current user: " + getCurrentUser());
    return getAreaByQuery(QUERY_PRIVATE_AREA_BY_CURRENT_USER);
  }
  
  // TODO: how do we tell hibernate to get the discussion area type and only for a 
  //       certain user (the current user)?
  public Area getDiscussionForumArea() 
  {
    LOG.debug("getDiscussionForumArea executing for current user: " + getCurrentUser());
    return getAreaByQuery(QUERY_DISCUSSION_AREA_BY_CURRENT_USER);
  }
  
  private Area getAreaByQuery(final String query) 
  {
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
  
  private String getCurrentUser() 
  {
    // TODO: add the session manager back
    return "joe"; //SessionManager.getCurrentSession().getUserEid();
  }
     
}
