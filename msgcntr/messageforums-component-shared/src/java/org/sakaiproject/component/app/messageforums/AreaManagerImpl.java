package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;
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
  private List openForums = new UniqueArrayList();
  private List privateForums = new UniqueArrayList();
  private List discussionForums = new UniqueArrayList();
  private static final String QUERY_BY_CONTEXTID = "findAreaByContextId";
  private static final String CONTEXT_ID = "contextId";
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

//rshastri suggestion//
  public void addForum(BaseForum forum, String typeuuid)
  {
    // TODO Auto-generated method stub
  }


  public void removeForum(BaseForum forum, String typeuuid)
  {
    // TODO Auto-generated method stub
  }
  
  
////////////////////////////////////////////////////////////////////////
  // helper methods for collections
  // //////////////////////////////////////////////////////////////////////

  public void addOpenForum(OpenForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("addOpenForum(Forum " + forum + ")");
    }

    if (forum == null)
    {
      throw new IllegalArgumentException("forum == null");
    }

//    forum.setArea(this);
    openForums.add(forum);
  }

  public void removeOpenForum(OpenForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("removeOpenForum(Forum " + forum + ")");
    }

    if (forum == null)
    {
      throw new IllegalArgumentException("Illegal forum argument passed!");
    }

//    forum.setArea(null);
    openForums.remove(forum);
  }

  public void addPrivateForum(PrivateForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("addPrivateForum(Forum " + forum + ")");
    }

    if (forum == null)
    {
      throw new IllegalArgumentException("forum == null");
    }

//    forum.setArea(this);
    privateForums.add(forum);
  }

  public void removePrivateForum(PrivateForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("removePrivateForum(Forum " + forum + ")");
    }

    if (forum == null)
    {
      throw new IllegalArgumentException("Illegal forum argument passed!");
    }

//    forum.setArea(null);
    privateForums.remove(forum);
  }

  public void addDiscussionnForum(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("addDiscussionnForum(DiscussionnForum " + forum + ")");
    }

    if (forum == null)
    {
      throw new IllegalArgumentException("forum == null");
    }

//    forum.setArea(this);
    discussionForums.add(forum);

  }

  public void removeDiscussionForum(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("removeDiscussionForum(DiscussionForum " + forum + ")");
    }

    if (forum == null)
    {
      throw new IllegalArgumentException("Illegal forum argument passed!");
    }

//    forum.setArea(null);
    discussionForums.remove(forum);

  }
  


}
