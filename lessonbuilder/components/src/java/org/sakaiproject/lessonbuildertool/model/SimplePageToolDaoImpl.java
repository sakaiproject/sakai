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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageCommentImpl;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageGroupImpl;
import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntryImpl;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.SimpleStudentPageImpl;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

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
	
	public boolean canEditPage(long pageId) {
		boolean canEdit = canEditPage();
		// forced comments have a pageid of -1, because they are associated with
		// more than one page. But the student can't edit them anyway, so fail it
		if(!canEdit && pageId != -1L) {
			SimplePage page = getPage(pageId);
			if(UserDirectoryService.getCurrentUser().getId()
					.equals(page.getOwner())) {
				canEdit = true;
			}
		}
		
		return canEdit;
	}
	
	public boolean canEditPage(String owner) {
		boolean canEdit = canEditPage();
		if(owner != null && !canEdit) {
			if(owner.equals(UserDirectoryService.getCurrentUser().getId())) {
				canEdit = true;
			}
		}
		
		return canEdit;
	}

	public void setSecurityService(SecurityService service) {
		securityService = service;
	}

	public void setToolManager(ToolManager service) {
		toolManager = service;
	}

	public List<SimplePageItem> findItemsOnPage(long pageId) {
	    DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId));
		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);
		
		Collections.sort(list, new Comparator<SimplePageItem>() {
			public int compare(SimplePageItem a, SimplePageItem b) {
				return Integer.valueOf(a.getSequence()).compareTo(b.getSequence());
			}
		});
		
		return list;
	}

	public List<SimplePageItem> findItemsInSite(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;
	    List<String> ids = SqlService.dbRead("select b.id from lesson_builder_pages a,lesson_builder_items b,SAKAI_SITE_PAGE c where a.siteId = ? and a.parent is null and a.pageId = b.sakaiId and b.type = 2 and b.pageId = 0 and a.toolId = c.PAGE_ID order by c.SITE_ORDER", fields, null);

	    List<SimplePageItem> result = new ArrayList<SimplePageItem>();
	    
	    if (result != null) {
		for (String id: ids) {
		    SimplePageItem i = findItem(new Long(id));
		    result.add(i);
		}
	    }
	    return result;
	}

	public List<SimplePageItem> findDummyItemsInSite(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;
	    List<String> ids = SqlService.dbRead("select b.id from lesson_builder_pages a,lesson_builder_items b where a.siteId = ? and a.pageId = b.pageId and b.sakaiId = '/dummy'", fields, null);

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
	
	public List<SimplePageComment> findComments(long commentWidgetId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("itemId", commentWidgetId));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public List<SimplePageComment> findCommentsOnItems(List<Long> commentItemIds) {
		if ( commentItemIds == null || commentItemIds.size() == 0)
		    return new ArrayList<SimplePageComment>();

		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.in("itemId", commentItemIds));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public List<SimplePageComment> findCommentsOnItemsByAuthor(List<Long> commentItemIds, String author) {
		if ( commentItemIds == null || commentItemIds.size() == 0)
		    return new ArrayList<SimplePageComment>();

		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.in("itemId", commentItemIds))
				.add(Restrictions.eq("author", author));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public List<SimplePageComment> findCommentsOnItemByAuthor(long commentWidgetId, String author) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class)
		    .add(Restrictions.eq("itemId", commentWidgetId))
		    .add(Restrictions.eq("author", author));

		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public List<SimplePageComment> findCommentsOnPageByAuthor(long pageId, String author) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class)
			.add(Restrictions.eq("pageId", pageId))
			.add(Restrictions.eq("author", author));
		
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public SimplePageComment findCommentById(long commentId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("id", commentId));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimplePageComment findCommentByUUID(String commentUUID) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("UUID", commentUUID));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimplePageItem findCommentsToolBySakaiId(String sakaiId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", sakaiId));
		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);
		
		// We loop through and check type here in-case something else has the same
		// sakaiId, and to prevent creating a new index for something that probably
		// doesn't really need it.  There shouldn't be more than a couple of matches
		// with different types.
		for(SimplePageItem item : list) {
			if(item.getType() == SimplePageItem.COMMENTS) {
				return item;
			}
		}
		
		return null;
	}
	
	public List<SimplePageItem> findItemsBySakaiId(String sakaiId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", sakaiId));
		return getHibernateTemplate().findByCriteria(d);
	}
	

	public SimpleStudentPage findStudentPage(long itemId, String owner) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("itemId", itemId))
			.add(Restrictions.eq("owner", owner)).add(Restrictions.eq("deleted", false));
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimpleStudentPage findStudentPage(long id) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("id", id));
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimpleStudentPage findStudentPageByPageId(long pageId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("pageId", pageId));
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public List<SimpleStudentPage> findStudentPages(long itemId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("itemId", itemId));
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public SimplePageItem findItemFromStudentPage(long pageId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("pageId", pageId));
	
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
	
		if(list.size() > 0) {
			return findItem(list.get(0).getItemId());
		}else {
			return null;
		}
	}

	public SimplePageItem findTopLevelPageItemBySakaiId(String id) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", id))
		    .add(Restrictions.eq("pageId", 0L))
		    .add(Restrictions.eq("type",SimplePageItem.PAGE));

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

	public List findControlledResourcesBySakaiId(String id, String siteId) {
	    Object [] fields = new Object[2];
	    fields[0] = id;
	    fields[1] = siteId;
	    List ids = SqlService.dbRead("select a.id from lesson_builder_items a, lesson_builder_pages b where a.sakaiId = ? and ( a.type=1 or a.type=7) and a.prerequisite = 1 and a.pageId = b.pageId and b.siteId = ?", fields, null);
	    return ids;

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

	public boolean saveItem(Object o, List<String>elist, String nowriteerr, boolean requiresEditPermission) {
		
		/*
		 * This checks a lot of conditions:
		 * 1) If o is SimplePageItem or SimplePage, it makes sure it gets the right page and checks the
		 *    permissions on it.
		 * 2) If it's a log entry, it lets it go.
		 * 3) If requiresEditPermission is set to false, it lets it go.
		 * 
		 * Essentially, if any of those say that the edit is fine, it won't throw the error.
		 */
		if(requiresEditPermission && !(o instanceof SimplePageItem && canEditPage(((SimplePageItem)o).getPageId()))
				&& !(o instanceof SimplePage && canEditPage(((SimplePage)o).getOwner()))
				&& !(o instanceof SimplePageLogEntry)
				&& !(o instanceof SimplePageGroup)) {
			elist.add(nowriteerr);
			return false;
		}

		try {
		    getHibernateTemplate().save(o);
		    
		    if (o instanceof SimplePageItem) {
			SimplePageItem i = (SimplePageItem)o;
			EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.create", "/lessonbuilder/item/" + i.getId(), true));
		    } else if (o instanceof SimplePage) {
			SimplePage i = (SimplePage)o;
			EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.create", "/lessonbuilder/page/" + i.getPageId(), true));
		    } 

		    if(o instanceof SimplePageItem || o instanceof SimplePage) {
		    	updateStudentPage(o);
		    }
		    
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
		/*
		 * If o is SimplePageItem or SimplePage, it makes sure it gets the right page and checks the
		 * permissions on it. If the item isn't SimplePageItem or SimplePage, it lets it go.
		 * 
		 * Essentially, if any of those say that the edit is fine, it won't throw the error.
		 */
		if(!(o instanceof SimplePageItem && canEditPage(((SimplePageItem)o).getPageId()))
				&& !(o instanceof SimplePage && canEditPage(((SimplePage)o).getOwner()))
				&& (o instanceof SimplePage || o instanceof SimplePageItem)) {
			return false;
		}

		if (o instanceof SimplePageItem) {
		    SimplePageItem i = (SimplePageItem)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.delete", "/lessonbuilder/item/" + i.getId(), true));
		} else if (o instanceof SimplePage) {
		    SimplePage i = (SimplePage)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.delete", "/lessonbuilder/page/" + i.getPageId(), true));
		} else if(o instanceof SimplePageComment) {
			SimplePageComment i = (SimplePageComment) o;
			EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.delete", "/lessonbuilder/comment/" + i.getId(), true));
		}

		try {
			getHibernateTemplate().delete(o);
			return true;
		} catch (DataAccessException e) {
			try {
				
				/* If we have multiple objects of the same item, you must merge them
				 * before deleting.  If the first delete fails, we merge and try again.
				 */
				getHibernateTemplate().delete(getHibernateTemplate().merge(o));
				
				return true;
			}catch(DataAccessException ex) {
				ex.printStackTrace();
				log.warn("Hibernate could not delete: " + e.toString());
				return false;
			}
		}
	}

	public boolean update(Object o, List<String>elist, String nowriteerr, boolean requiresEditPermission) {
		/*
		 * This checks a lot of conditions:
		 * 1) If o is SimplePageItem or SimplePage, it makes sure it gets the right page and checks the
		 *    permissions on it.
		 * 2) If it's a log entry, it lets it go.
		 * 3) If requiresEditPermission is set to false, it lets it go.
		 * 
		 * Essentially, if any of those say that the edit is fine, it won't throw the error.
		 */
		if(requiresEditPermission && !(o instanceof SimplePageItem && canEditPage(((SimplePageItem)o).getPageId()))
				&& !(o instanceof SimplePage && canEditPage(((SimplePage)o).getOwner()))
		   		&& !(o instanceof SimplePageLogEntry)
				&& !(o instanceof SimplePageGroup)) {
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
			if(!(o instanceof SimplePageLogEntry)) {
				getHibernateTemplate().merge(o);
			}else {
				// Updating seems to always update the timestamp on the log correctly,
				// while merging doesn't always get it right.  However, it's possible that
				// update will fail, so we do both, in order of preference.
				try {
					getHibernateTemplate().update(o);
				}catch(DataAccessException ex) {
					log.warn("Wasn't able to update log entry, timing might be a bit off.");
					getHibernateTemplate().merge(o);
				}
			}
		    
		    if(o instanceof SimplePageItem || o instanceof SimplePage) {
		    	updateStudentPage(o);
		    }
		    
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
			getHibernateTemplate().merge(o);
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

	public SimplePageLogEntry getLogEntry(String userId, long itemId, Long studentPageId) {
		if(studentPageId.equals(-1L)) studentPageId = null;
		
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageLogEntry.class).add(Restrictions.eq("userId", userId))
				.add(Restrictions.eq("itemId", itemId));
		
		if(studentPageId != null) {
			d.add(Restrictions.eq("studentPageId", studentPageId));
		}else {
			d.add(Restrictions.isNull("studentPageId"));
		}

		List l = getHibernateTemplate().findByCriteria(d);
		
		if (l != null && l.size() > 0) {
			return (SimplePageLogEntry) l.get(0);
		} else {
			return null;
		}
	}
	
        public boolean isPageVisited(long pageId, String userId, String owner) {
	    // if this is a student page, it's most likely the top level, so do that query first
	    if (owner != null) {
		Object [] fields = new Object[3];
		fields[0] = pageId;
		fields[1] = pageId;
		fields[2] = userId;
		List<String> ones = SqlService.dbRead("select 1 from lesson_builder_student_pages a, lesson_builder_log b where a.pageId=? and a.itemId = b.itemId and b.studentPageId=? and b.userId=?", fields, null);
		if (ones != null && ones.size() > 0)
		    return true;
	    }

	    Object [] fields = new Object[2];
	    fields[0] = Long.toString(pageId);
	    fields[1] = userId;
	    List<String> ones = SqlService.dbRead("select 1 from lesson_builder_items a, lesson_builder_log b where a.sakaiId=? and a.type=2 and a.id=b.itemId and b.userId=?", fields, null);
	    if (ones != null && ones.size() > 0)
		return true;
	    else
		return false;
	}

	public List<SimplePageLogEntry> getStudentPageLogEntries(long itemId, String userId) {		
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageLogEntry.class).add(Restrictions.eq("userId", userId))
				.add(Restrictions.eq("itemId", itemId))
				.add(Restrictions.isNotNull("studentPageId"));

		List<SimplePageLogEntry> entries = getHibernateTemplate().findByCriteria(d);
		
		return entries;
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


    public SimplePageGroup makeGroup(String itemId, String groupId, String groups) {
		return new SimplePageGroupImpl(itemId, groupId, groups);
    }


	public SimplePageLogEntry makeLogEntry(String userId, long itemId, Long studentPageId) {
		return new SimplePageLogEntryImpl(userId, itemId, studentPageId);
	}

	public SimplePageComment makeComment(long itemId, long pageId, String author, String comment, String UUID, boolean html) {
		return new SimplePageCommentImpl(itemId, pageId, author, comment, UUID, html);
	}
	
	public SimpleStudentPage makeStudentPage(long itemId, long pageId, String title, String author, boolean groupOwned) {
		return new SimpleStudentPageImpl(itemId, pageId, title, author, groupOwned);
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
	
	private void updateStudentPage(Object o) {
		SimplePage page;
		
		if(o instanceof SimplePageItem) {
			SimplePageItem item = (SimplePageItem) o;
			page = getPage(item.getPageId());
		}else if(o instanceof SimplePage) {
			page = (SimplePage) o;
		}else {
			return;
		}
		
		if(page != null && page.getTopParent() != null) {
			SimpleStudentPage studentPage = findStudentPage(page.getTopParent());
			if(studentPage != null) {
				studentPage.setLastUpdated(new Date());
				quickUpdate(studentPage);
			}
		}
	}
}