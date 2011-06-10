/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.model;

import java.util.List;
import java.util.ArrayList;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;

import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageGroupImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntryImpl;

import org.sakaiproject.lessonbuildertool.service.SimplePageToolService;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.user.cover.UserDirectoryService;

public class SimplePageToolDaoImpl extends HibernateDaoSupport implements SimplePageToolDao {
	private static Log log = LogFactory.getLog(SimplePageToolDaoImpl.class);

	private ToolManager toolManager;
	private SecurityService securityService;
	private static String SITE_UPD = "site.upd";

        // part of HibernateDaoSupport; this is the only context in which it is OK
        // to modify the template configuration
        protected void initDao() throws Exception {
	    super.initDao();
	    getHibernateTemplate().setCacheQueries(true);
	    log.info("initDao template " + getHibernateTemplate());
	}

	// the permissions model here is preliminary. I'm not convinced that all the code in
	// upper layers checks where it should, so the Dao is supplying an extra layer of
	// protection. As far as I can tell, any database change should be done by
	// someone with update privs, except that add or update to the log is done on
	// behalf of normal people. I've checked all the code that does save or update for
	// log entries and it looks OK.

	public boolean canEditPage() {
	        String ref = null;
		// no placement, startup testing, should be an advisor in place
		try {
		    ref = "/site/" + toolManager.getCurrentPlacement().getContext();
		} catch (java.lang.NullPointerException ignore) {
		    ref = "";
		}
		return securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref);
	}

	public void setSecurityService(SecurityService service) {
		securityService = service;
	}

	public void setToolManager(ToolManager service) {
		toolManager = service;
	}

	public List<SimplePageItem> findItemsOnPage(long pageId) {
	    DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId)).addOrder(Order.asc("sequence"));
		return getHibernateTemplate().findByCriteria(d);
	}

        

	public List<SimplePageItem> findItemsInSite(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;
	    List<String> ids = SqlService.dbRead("select b.id from lesson_builder_pages a,lesson_builder_items b,SAKAI_SITE_PAGE c where a.siteId = ? and a.parent is null and a.pageId = b.sakaiId and b.type = 2 and a.toolId = c.PAGE_ID order by c.SITE_ORDER", fields, null);

	    List<SimplePageItem> result = new ArrayList<SimplePageItem>();
	    
	    if (result != null) {
		for (String id: ids) {
		    SimplePageItem i = findItem(new Long(id));
		    result.add(i);
		}
	    }
	    return result;
	}

    public PageData findMostRecentlyVisitedPage(final String userId, final String toolId) {
	    Object [] fields = new Object[4];
	    fields[0] = userId;
	    fields[1] = toolId;
	    fields[2] = userId;
	    fields[3] = toolId;
	    
	    List<PageData> rv = SqlService.dbRead("select a.itemId, a.id, b.sakaiId, b.name from lesson_builder_log a, lesson_builder_items b where a.userId=? and a.toolId=? and a.lastViewed = (select max(lastViewed) from lesson_builder_log where userId=? and toolId = ?) and a.itemId = b.id", fields, new SqlReader() {
		    public Object readSqlResultRecord(ResultSet result) {
			try {
			    PageData ret = new PageData();
			    ret.itemId = result.getLong(1);
			    ret.pageId = result.getLong(3);
			    ret.name = result.getString(4);
			    
			    return ret;
			} catch (SQLException e) {
			    log.warn("findMostRecentlyVisitedPage: " + toolId + " : " + e);
			    return null;
			}
		    }
		});


	    if (rv != null && rv.size() > 0)
		return rv.get(0);
	    else
		return null;
	}

	public SimplePageItem findItem(long id) {
	    
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("id", id));
		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		if (list != null && list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public SimplePageItem findTopLevelPageItemBySakaiId(String id) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", id)).
		    add(Restrictions.eq("pageId", 0L)).
		    add(Restrictions.eq("type",SimplePageItem.PAGE));

		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1)
		    return null;
			
		return list.get(0);
	}

	public List<SimplePageItem> findPageItemsBySakaiId(String id) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", id)).
		    add(Restrictions.eq("type",SimplePageItem.PAGE));

		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		return list;
	}


	public SimplePageItem findNextPageItemOnPage(long pageId, int sequence) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId)).
		    add(Restrictions.eq("sequence", sequence+1)).
		    add(Restrictions.eq("type",SimplePageItem.PAGE));

		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1)
		    return null;
			
		return list.get(0);
	}

	public SimplePageItem findNextItemOnPage(long pageId, int sequence) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId)).
		    add(Restrictions.eq("sequence", sequence+1));

		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1)
		    return null;
			
		return list.get(0);
	}

	public void getCause(Throwable t, List<String>elist) {
	    while (t.getCause() != null) {
		t = t.getCause();
	    }
	    log.warn("error saving or updating: " + t.toString());
	    elist.add(t.getLocalizedMessage());
	}

	public boolean saveItem(Object o, List<String>elist, String nowriteerr) {
		if (!(o instanceof SimplePageLogEntry || canEditPage())) {
		    elist.add(nowriteerr);
		    return false;
		}

		if (o instanceof SimplePageItem) {
		    SimplePageItem i = (SimplePageItem)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.create", "/lessonbuilder/item/" + i.getId(), true));
		} else if (o instanceof SimplePage) {
		    SimplePage i = (SimplePage)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.create", "/lessonbuilder/page/" + i.getPageId(), true));
		} 

		try {
		    getHibernateTemplate().save(o);
		    return true;
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
		    getCause(e, elist);
		    return false;
		} catch (org.hibernate.exception.DataException e) {
		    getCause(e, elist);
		    return false;
		} catch (DataAccessException e) {
		    getCause(e, elist);
		    return false;
		}
	}

    // for use within copytransfer. We don't need to do permissions, and it probably
    // doesn't make sense to log every item created
	public boolean quickSaveItem(Object o) {
		try {
			Object id = getHibernateTemplate().save(o);
			return true;
		} catch (DataAccessException e) {
			e.printStackTrace();
			log.warn("Hibernate could not save: " + e.toString());
			return false;
		}
	}

	public boolean deleteItem(Object o) {
		if (!canEditPage()) {
			return false;
		}

		if (o instanceof SimplePageItem) {
		    SimplePageItem i = (SimplePageItem)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.delete", "/lessonbuilder/item/" + i.getId(), true));
		} else if (o instanceof SimplePage) {
		    SimplePage i = (SimplePage)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.delete", "/lessonbuilder/page/" + i.getPageId(), true));
		} 

		try {
			getHibernateTemplate().delete(o);
			return true;
		} catch (DataAccessException e) {
			e.printStackTrace();
			log.warn("Hibernate could not delete: " + e.toString());
			return false;
		}
	}

	public boolean update(Object o, List<String>elist, String nowriteerr) {
		if (!(o instanceof SimplePageLogEntry || canEditPage())) {
		    elist.add(nowriteerr);
		    return false;
		}
		if (o instanceof SimplePageItem) {
		    SimplePageItem i = (SimplePageItem)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.update", "/lessonbuilder/item/" + i.getId(), true));
		} else if (o instanceof SimplePage) {
		    SimplePage i = (SimplePage)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.update", "/lessonbuilder/page/" + i.getPageId(), true));
		} 

		try {
		    getHibernateTemplate().merge(o);
		    return true;
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
		    getCause(e, elist);
		    return false;
		} catch (org.hibernate.exception.DataException e) {
		    getCause(e, elist);
		    return false;
		} catch (DataAccessException e) {
		    getCause(e, elist);
		    return false;
		}
	}

    // ditto for update
	public boolean quickUpdate(Object o) {
		try {
			getHibernateTemplate().update(o);
			return true;
		} catch (DataAccessException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Long getTopLevelPageId(String toolId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePage.class).add(Restrictions.eq("toolId", toolId)).add(Restrictions.isNull("parent"));

		List list = getHibernateTemplate().findByCriteria(d);

		if (list.size() > 1) {
			log.warn("Problem finding which page we should be on.  Doing the best we can.");
		}

		if (list != null && list.size() > 0) {
			return ((SimplePage) list.get(0)).getPageId();
		} else {
			return null;
		}
	}

	public SimplePage getPage(long pageId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePage.class).add(Restrictions.eq("pageId", pageId));

		List l = getHibernateTemplate().findByCriteria(d);

		if (l != null && l.size() > 0) {
			return (SimplePage) l.get(0);
		} else {
			return null;
		}
	}

	public List<SimplePage> getSitePages(String siteId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePage.class).add(Restrictions.eq("siteId", siteId));

		List<SimplePage> l = getHibernateTemplate().findByCriteria(d);

		if (l != null && l.size() > 0) {
		    return l;
		} else {
		    return null;
		}
	}

	public SimplePageLogEntry getLogEntry(String userId, long itemId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageLogEntry.class).add(Restrictions.eq("userId", userId)).add(Restrictions.eq("itemId", itemId));

		List l = getHibernateTemplate().findByCriteria(d);

		if (l != null && l.size() > 0) {
			return (SimplePageLogEntry) l.get(0);
		} else {
			return null;
		}
	}

        public List<String> findUserWithCompletePages(Long itemId){
	    Object [] fields = new Object[1];
            fields[0] = itemId;

            List<String> users = SqlService.dbRead("select a.userId from lesson_builder_log a where a.itemId = ? and a.complete = true", fields, null);

	    return users;
        }

	public SimplePageGroup findGroup(String itemId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageGroup.class).add(Restrictions.eq("itemId", itemId));

		List l = getHibernateTemplate().findByCriteria(d);

		if (l != null && l.size() > 0) {
			return (SimplePageGroup) l.get(0);
		} else {
			return null;
		}
	}

    public SimplePage makePage(String toolId, String siteId, String title, Long parent, Long topParent) {
	return new SimplePageImpl(toolId, siteId, title, parent, topParent);
    }

    public SimplePageItem makeItem(long id, long pageId, int sequence, int type, String sakaiId, String name) {
	return new SimplePageItemImpl(id, pageId, sequence, type, sakaiId, name);
    }

    public SimplePageItem makeItem(long pageId, int sequence, int type, String sakaiId, String name) {
	return new SimplePageItemImpl(pageId, sequence, type, sakaiId, name);
    }

    public SimplePageGroup makeGroup(String itemId, String groupId) {
	return new SimplePageGroupImpl(itemId, groupId);
    }

    public SimplePageLogEntry makeLogEntry(String userId, long itemId) {
	return new SimplePageLogEntryImpl(userId, itemId);
    }

    public SimplePageItem copyItem(SimplePageItem old) {
	SimplePageItem item =  new SimplePageItemImpl();
	item.setPageId(old.getPageId());
	item.setSequence(old.getSequence());
	item.setType(old.getType());
	item.setSakaiId(old.getSakaiId());
	item.setName(old.getName());
	item.setHtml(old.getHtml());
	item.setDescription(old.getDescription());
	item.setHeight(old.getHeight());
	item.setWidth(old.getWidth());
	item.setAlt(old.getAlt());
	item.setNextPage(old.getNextPage());
	item.setFormat(old.getFormat());
	item.setRequired(old.isRequired());
	item.setAlternate(old.isAlternate());
	item.setPrerequisite(old.isPrerequisite());
	item.setSubrequirement(old.getSubrequirement());
	item.setRequirementText(old.getRequirementText());
	return item;
   }
}