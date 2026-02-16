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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.CacheMode;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortalSubPageNavProvider;
import org.sakaiproject.time.api.UserTimeService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.ChecklistItemStatus;
import org.sakaiproject.lessonbuildertool.SimpleChecklistItem;
import org.sakaiproject.lessonbuildertool.SimpleChecklistItemImpl;
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
import org.sakaiproject.lessonbuildertool.SimplePagePeerEval;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResult;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResultImpl;
import org.sakaiproject.lessonbuildertool.SimplePageProperty;
import org.sakaiproject.lessonbuildertool.SimplePagePropertyImpl;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionAnswer;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionAnswerImpl;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponseImpl;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponseTotals;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponseTotalsImpl;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.SimpleStudentPageImpl;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.util.LessonsSubNavBuilder;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

@Setter @Slf4j
public class SimplePageToolDaoImpl extends HibernateDaoSupport implements SimplePageToolDao, PortalSubPageNavProvider {

	private AuthzGroupService authzGroupService;
	private EventTrackingService eventTrackingService;
	private PortalService portalService;
	private SecurityService securityService;
	private ServerConfigurationService serverConfigurationService;
	private SiteService siteService;
	private SqlService sqlService;
	private ToolManager toolManager;
	private UserDirectoryService userDirectoryService;
	private UserTimeService userTimeService;

	private Comparator<SimplePageItem> spiComparator;

        // part of HibernateDaoSupport; this is the only context in which it is OK
        // to modify the template configuration
	protected void initDao() throws Exception {
		super.initDao();
		getHibernateTemplate().setCacheQueries(true);
		log.info("initDao template {}", getHibernateTemplate());
		SimplePageItemImpl.setSimplePageToolDao(this);
		spiComparator = Comparator.comparingInt(SimplePageItem::getSequence);
	}

	public void init() {
		portalService.registerSubPageNavProvider(this);
	}

	// the permissions model here is preliminary. I'm not convinced that all the code in
	// upper layers checks where it should, so the Dao is supplying an extra layer of
	// protection. As far as I can tell, any database change should be done by
	// someone with update privs, except that add or update to the log is done on
	// behalf of normal people. I've checked all the code that does save or update for
	// log entries and it looks OK.

    public HibernateTemplate getDaoHibernateTemplate() {
	return getHibernateTemplate();
    }

    // make sure future reads come from the database. Currently used to minimize the possibiliy of race conditions
    // involving old data, for the sequence number. I'm not currently clearing the session cache, because this 
    // method is called before anyone has read any items, so there shouldn't be any old data there.
	public void setRefreshMode() {
		currentSession().setCacheMode(CacheMode.REFRESH);
	}

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
		//  also, top-level fake items have pageid of 0
		if(!canEdit && pageId != -1L && pageId != 0L) {
			SimplePage page = getPage(pageId);
			String owner = page.getOwner();
			String group = page.getGroup();
			if (group != null)
			    group = "/site/" + page.getSiteId() + "/group/" + group;
			String currentUser = userDirectoryService.getCurrentUser().getId();
			if (currentUser != null) {
			    if (group == null && currentUser.equals(owner))
				canEdit = true;
			    else if (group != null && authzGroupService.getUserRole(currentUser, group) != null)
				canEdit = true;
			}
		}
		
		return canEdit;
	}
	
	public boolean canEditPage(SimplePage page) {
		boolean canEdit = canEditPage();
		// forced comments have a pageid of -1, because they are associated with
		// more than one page. But the student can't edit them anyway, so fail it
		if(!canEdit && page != null) {
			String owner = page.getOwner();
			String group = page.getGroup();
			if (group != null)
			    group = "/site/" + page.getSiteId() + "/group/" + group;
			String currentUser = userDirectoryService.getCurrentUser().getId();
			if (currentUser != null) {
			    if (group == null && currentUser.equals(owner))
				canEdit = true;
			    else if (group != null && authzGroupService.getUserRole(currentUser, group) != null)
				canEdit = true;
			}
		}
		return canEdit;
	}

	public List<SimplePageItem> findItemsOnPage(long pageId) {
	    DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId));
		List<SimplePageItem> list = (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);
		
		Collections.sort(list, new Comparator<SimplePageItem>() {
			public int compare(SimplePageItem a, SimplePageItem b) {
				return Integer.valueOf(a.getSequence()).compareTo(b.getSequence());
			}
		});
		
		return list;
	}

	public void flush() {
	    getHibernateTemplate().flush();
	}

	public void clear() {
	    getHibernateTemplate().clear();
	}

    // find pseudo-items for top-level pages in site
    public List<SimplePageItem> findItemsInSite(String siteId) {

        List<SimplePage> topLevelPages = getTopLevelPages(siteId);

        List<String> lessonsPageIds = new ArrayList<>();
        if (topLevelPages != null && !topLevelPages.isEmpty()) {
            for (SimplePage lessonsPage : topLevelPages) {
                String pageId = String.valueOf(lessonsPage.getPageId());
                lessonsPageIds.add(pageId);
            }
            List<SimplePageItem> pageItems = findTopLevelPageItemsBySakaiIds(lessonsPageIds);
            return pageItems;
        }
        return null;
    }

	public List<SimplePageItem> findDummyItemsInSite(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;
	    List<String> ids = sqlService.dbRead("select b.id from lesson_builder_pages a,lesson_builder_items b where a.siteId = ? and a.pageId = b.pageId and b.sakaiId = '/dummy'", fields, null);

	    List<SimplePageItem> result = new ArrayList<SimplePageItem>();
	    
	    if (result != null) {
		for (String id: ids) {
		    SimplePageItem i = findItem(new Long(id));
		    result.add(i);
		}
	    }
	    return result;
	}


    // warning: only the id and html fields will be filled out
    // we had serious performance issues with an earlier version, so I'm doing it without hibernate
	public List<SimplePageItem> findTextItemsInSite(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;
	    List<SimplePageItem> items = sqlService.dbRead("select b.id,b.html from lesson_builder_pages a,lesson_builder_items b where a.siteId = ? and a.pageId = b.pageId and b.type = 5", fields, new SqlReader() {
		    public Object readSqlResultRecord(ResultSet result) {
    			try {
			    SimplePageItem item = new SimplePageItemImpl();
			    item.setId(result.getLong(1));
			    item.setHtml(result.getString(2));
			    return item;
    			} catch (SQLException e) {
                            log.warn("findTextItemsInSite: {}", e);
			    return null;
    			}
		    }
		});

	    return items;
	}

    // because they don't come from hibernate, you can't use update on them.
    // we need to be able to update the HTML field


    public PageData findMostRecentlyVisitedPage(final String userId, final String toolId) {
    	Object [] fields = new Object[4];
    	fields[0] = userId;
    	fields[1] = toolId;
    	fields[2] = userId;
    	fields[3] = toolId;
    	
    	List<PageData> rv = sqlService.dbRead("select a.itemId, a.id, b.sakaiId, b.name from lesson_builder_log a, lesson_builder_items b where a.userId=? and a.toolId=? and a.lastViewed = (select max(lastViewed) from lesson_builder_log where userId=? and toolId = ?) and a.itemId = b.id", fields, new SqlReader() {
    		public Object readSqlResultRecord(ResultSet result) {
    			try {
    				PageData ret = new PageData();
    				ret.itemId = result.getLong(1);
    				ret.pageId = result.getLong(3);
    				ret.name = result.getString(4);
    				
    				return ret;
    			} catch (SQLException e) {
    				log.warn("findMostRecentlyVisitedPage: {}: {}", toolId, e);
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
		List<SimplePageItem> list = (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);

		if (list != null && list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	public SimplePageProperty findProperty(String attribute) {
	    
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageProperty.class).add(Restrictions.eq("attribute", attribute));
		
		List<SimplePageProperty> list = null;
		try {
		    list = (List<SimplePageProperty>) getHibernateTemplate().findByCriteria(d);
		} catch (org.hibernate.ObjectNotFoundException e) {
		    return null;
		}

		if (list != null && list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

        public SimplePageProperty makeProperty(String attribute, String value) {
	    return new SimplePagePropertyImpl(attribute, value);
	}

	public List<SimplePageComment> findComments(long commentWidgetId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("itemId", commentWidgetId));
		return (List<SimplePageComment>) getHibernateTemplate().findByCriteria(d);
	}
	
	public List<SimplePageComment> findCommentsOnItems(List<Long> commentItemIds) {
		if ( commentItemIds == null || commentItemIds.size() == 0)
		    return new ArrayList<SimplePageComment>();

		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.in("itemId", commentItemIds));
		return (List<SimplePageComment>) getHibernateTemplate().findByCriteria(d);
	}
	
	public List<SimplePageComment> findCommentsOnItemsByAuthor(List<Long> commentItemIds, String author) {
		if ( commentItemIds == null || commentItemIds.size() == 0)
		    return new ArrayList<SimplePageComment>();

		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.in("itemId", commentItemIds))
				.add(Restrictions.eq("author", author));
		return (List<SimplePageComment>) getHibernateTemplate().findByCriteria(d);
	}
	
	public List<SimplePageComment> findCommentsOnItemByAuthor(long commentWidgetId, String author) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class)
		    .add(Restrictions.eq("itemId", commentWidgetId))
		    .add(Restrictions.eq("author", author));

		return (List<SimplePageComment>) getHibernateTemplate().findByCriteria(d);
	}
	
	public List<SimplePageComment> findCommentsOnPageByAuthor(long pageId, String author) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class)
			.add(Restrictions.eq("pageId", pageId))
			.add(Restrictions.eq("author", author));
		
		return (List<SimplePageComment>) getHibernateTemplate().findByCriteria(d);
	}
	
	public SimplePageComment findCommentById(long commentId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("id", commentId));
		List<SimplePageComment> list = (List<SimplePageComment>) getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimplePageComment findCommentByUUID(String commentUUID) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("UUID", commentUUID));
		List<SimplePageComment> list = (List<SimplePageComment>) getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimplePageItem findCommentsToolBySakaiId(String sakaiId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", sakaiId));
		List<SimplePageItem> list = (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);
		
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
		return (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);
	}

	public List<SimplePageItem> findPageItemsByPageId(long pageId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId));
		return (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);
	}

	private List<SimplePageItem> findSubPageItemsByPageId(long pageId) {
		return getHibernateTemplate().execute(session -> {
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<SimplePageItemImpl> query = cb.createQuery(SimplePageItemImpl.class);
			Root<SimplePageItemImpl> root = query.from(SimplePageItemImpl.class);
			// sakaiId is a varchar, so it's important to use String.valueOf(pageId) here
			query.select(root).where(cb.and(cb.equal(root.get("pageId"), String.valueOf(pageId)), cb.equal(root.get("type"), SimplePageItem.PAGE)));
			List<SimplePageItem> simplePageItems = new ArrayList<>(session.createQuery(query).getResultList());
			simplePageItems.sort(spiComparator);
			return simplePageItems;
		});
	}

	private Optional<SimplePageItem> findTopLevelPageItem(long pageId) {
		return getHibernateTemplate().execute(session -> {
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<SimplePageItemImpl> query = cb.createQuery(SimplePageItemImpl.class);
			Root<SimplePageItemImpl> root = query.from(SimplePageItemImpl.class);
			query.select(root).where(cb.and(cb.equal(root.get("sakaiId"), String.valueOf(pageId)), cb.equal(root.get("type"), SimplePageItem.PAGE)));
			List<SimplePageItemImpl> result = session.createQuery(query).getResultList();
			if (result.isEmpty()) return Optional.empty();
			else {
				if (result.size() > 1) log.warn("query found more than one SimplePageItem where sakaiId={} and type=2", pageId);
				return Optional.of(result.get(0));
			}
		});
    }

	// find the student's page. In theory we keep them from doing a second page. With
    // group pages that means students in more than one group can only do one. So return the first
    // Different versions if item is controlled by group or not. That lets us use simple
    // hibernate queries and maximum caching
	public SimpleStudentPage findStudentPage(long itemId, String owner) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("itemId", itemId))
			.add(Restrictions.eq("owner", owner)).add(Restrictions.eq("deleted", false));
		List<SimpleStudentPage> list = (List<SimpleStudentPage>) getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
    // groups is set of groups to search. 
    // null groups means there are no permitted groups, so the answer is obviously null
	public SimpleStudentPage findStudentPage(long itemId, Collection<String> groups) {
		if (groups == null || groups.size() == 0) // no possible groups, so no result
		    return null;

		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("itemId", itemId))
			.add(Restrictions.in("group", groups)).add(Restrictions.eq("deleted", false));
		List<SimpleStudentPage> list = (List<SimpleStudentPage>) getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}

	
	public SimpleStudentPage findStudentPage(long id) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("id", id));
		List<SimpleStudentPage> list = (List<SimpleStudentPage>) getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimpleStudentPage findStudentPageByPageId(long pageId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("pageId", pageId));
		List<SimpleStudentPage> list = (List<SimpleStudentPage>) getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public List<SimpleStudentPage> findStudentPages(long itemId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("itemId", itemId));
		return (List<SimpleStudentPage>) getHibernateTemplate().findByCriteria(d);
	}
	
	public SimplePageItem findItemFromStudentPage(long pageId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("pageId", pageId));
	
		List<SimpleStudentPage> list = (List<SimpleStudentPage>) getHibernateTemplate().findByCriteria(d);
	
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

		List<SimplePageItem> list = (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1)
		    return null;
			
		return list.get(0);
	}

	public List<SimplePageItem> findTopLevelPageItemsBySakaiIds(List<String> ids) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class)
			.add(Restrictions.in("sakaiId", ids))
			.add(Restrictions.eq("pageId", 0L))
			.add(Restrictions.eq("type",SimplePageItem.PAGE));

		List<SimplePageItem> list = (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1) {
			return null;
		}

		return list;
	}

	public List<SimplePageItem> findPageItemsBySakaiId(String id) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", id)).
		    add(Restrictions.eq("type",SimplePageItem.PAGE));

		return (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);
	}

	public List findControlledResourcesBySakaiId(String id, String siteId) {
	    Object [] fields = new Object[2];
	    fields[0] = id;
	    fields[1] = siteId;
	    List ids = sqlService.dbRead("select a.id from lesson_builder_items a, lesson_builder_pages b where a.sakaiId = ? and ( a.type=1 or a.type=7) and a.prerequisite = 1 and a.pageId = b.pageId and b.siteId = ?", fields, null);
	    return ids;

	}


	public SimplePageItem findNextPageItemOnPage(long pageId, int sequence) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId)).
		    add(Restrictions.eq("sequence", sequence+1)).
		    add(Restrictions.eq("type",SimplePageItem.PAGE));

		List<SimplePageItem> list = (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1)
		    return null;
			
		return list.get(0);
	}

	public SimplePageItem findNextItemOnPage(long pageId, int sequence) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId)).
		    add(Restrictions.eq("sequence", sequence+1));

		List<SimplePageItem> list = (List<SimplePageItem>) getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1)
		    return null;
			
		return list.get(0);
	}
	
	public List<SimplePageQuestionAnswer> findAnswerChoices(SimplePageItem question) {
		List<SimplePageQuestionAnswer> ret = new ArrayList<SimplePageQuestionAnswer>();

		// find new id number, max + 1
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null)
		    return ret;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    SimplePageQuestionAnswer newAnswer = new SimplePageQuestionAnswerImpl((Long)answer.get("id"),
			       (String)answer.get("text"), (Boolean) answer.get("correct"));
		    ret.add(newAnswer);
		}
		return ret;
	}
	
	public boolean hasCorrectAnswer(SimplePageItem question) {
		// find new id number, max + 1
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null)
		    return false;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    if ((Boolean) answer.get("correct"))
			return true;
		}
		return false;
	}

	public SimplePageQuestionAnswer findAnswerChoice(SimplePageItem question, long answerId) {
		// find new id number, max + 1
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null)
		    return null;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    if (answerId == (Long)answer.get("id")) {
			SimplePageQuestionAnswer newAnswer = new SimplePageQuestionAnswerImpl(answerId,(String)answer.get("text"), (Boolean) answer.get("correct"));
			return newAnswer;
		    }
		}
		return null;
	}
	
	public SimplePageQuestionResponse findQuestionResponse(long questionId, String userId) {
        DetachedCriteria d = DetachedCriteria.forClass(SimplePageQuestionResponse.class).add(Restrictions.eq("questionId", questionId))
        		.add(Restrictions.eq("userId", userId));

        List<SimplePageQuestionResponse> list = (List<SimplePageQuestionResponse>) getHibernateTemplate().findByCriteria(d);
        if(list != null && list.size() > 0) {
        	return list.get(0);
        }else {
        	return null;
        }
	}
	
	public SimplePageQuestionResponse findQuestionResponse(long responseId) {
        DetachedCriteria d = DetachedCriteria.forClass(SimplePageQuestionResponse.class).add(Restrictions.eq("id", responseId));

        List<SimplePageQuestionResponse> list = (List<SimplePageQuestionResponse>) getHibernateTemplate().findByCriteria(d);
        if(list != null && list.size() > 0) {
        	return list.get(0);
        }else {
        	return null;
        }
	}
	
	public List<SimplePageQuestionResponse> findQuestionResponses(long questionId) {
        DetachedCriteria d = DetachedCriteria.forClass(SimplePageQuestionResponse.class).add(Restrictions.eq("questionId", questionId));

        List<SimplePageQuestionResponse> list = (List<SimplePageQuestionResponse>) getHibernateTemplate().findByCriteria(d);
        return list;
	}

	public void getCause(Throwable t, List<String>elist) {
		while (t.getCause() != null) {
			t = t.getCause();
		}
		log.warn("error saving or updating: {}", t.toString());
		elist.add(t.getLocalizedMessage());
	}

	public boolean saveItem(final Object o, List<String>elist, String nowriteerr, boolean requiresEditPermission) {
		
		/*
		 * This checks a lot of conditions:
		 * 1) If o is SimplePageItem or SimplePage, it makes sure it gets the right page and checks the
		 *    permissions on it.
		 * 2) If it's a log entry or question response, it lets it go.
		 * 3) If requiresEditPermission is set to false, it lets it go.
		 * 
		 * Essentially, if any of those say that the edit is fine, it won't throw the error.
		 */
		if(requiresEditPermission && !(o instanceof SimplePageItem && canEditPage(((SimplePageItem)o).getPageId()))
				&& !(o instanceof SimplePage && canEditPage((SimplePage)o))
				&& !(o instanceof SimplePageLogEntry || o instanceof SimplePageQuestionResponse)
				&& !(o instanceof SimplePageGroup)) {
			elist.add(nowriteerr);
			return false;
		}

		try {

			boolean isSimplePageLongEntryObject = o instanceof SimplePageLogEntry;
			boolean isLoggedIn = false;
			if (isSimplePageLongEntryObject) {
				SimplePageLogEntry simplePageLogEntry = (SimplePageLogEntry) o;
				isLoggedIn = StringUtils.isNotBlank(simplePageLogEntry.getUserId());
			}

			if (!isSimplePageLongEntryObject || isLoggedIn) {
				getHibernateTemplate().save(o);
			}

			if (o instanceof SimplePageItem || o instanceof SimplePage) {
				updateStudentPage(o);
			}

			if (o instanceof SimplePageItem) {
				SimplePageItem item = (SimplePageItem)o;
				eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.ITEM_CREATE, "/lessonbuilder/item/" + item.getId(), true));
			} else if (o instanceof SimplePage) {
				SimplePage page = (SimplePage)o;
				eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.PAGE_CREATE, "/lessonbuilder/page/" + page.getPageId(), true));
			} else if (o instanceof SimplePageComment) {
				SimplePageComment comment = (SimplePageComment)o;
				eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.COMMENT_CREATE, "/lessonbuilder/comment/" + comment.getId(), true));
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

	public boolean saveOrUpdate(Object o, List<String>elist, String nowriteerr, boolean requiresEditPermission) {
		
		/*
		 * 1) If o is SimplePageItem or SimplePage, it makes sure it gets the right page and checks the
		 *    permissions on it.
		 * 2) If it's a log entry or question response, it lets it go.
		 * 3) If requiresEditPermission is set to false, it lets it go.
		 * 
		 * Essentially, if any of those say that the edit is fine, it won't throw the error.
		 */
	    if(requiresEditPermission && !(o instanceof SimplePageItem && canEditPage(((SimplePageItem)o).getPageId()))
	    			&& !(o instanceof SimplePage && canEditPage((SimplePage)o))
				&& !(o instanceof SimplePageLogEntry || o instanceof SimplePageQuestionResponse)
				&& !(o instanceof SimplePageGroup)) {
			elist.add(nowriteerr);
			return false;
		}

		try {
		    getHibernateTemplate().saveOrUpdate(o);
		    
		    if (o instanceof SimplePageItem) {
				SimplePageItem i = (SimplePageItem)o;
				if (i.getId() == 0) {
					eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.ITEM_CREATE, "/lessonbuilder/item/" + i.getId(), true));
				} else  {
					eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.ITEM_UPDATE, "/lessonbuilder/item/" + i.getId(), true));
				}
		    } else if (o instanceof SimplePage) {
				SimplePage i = (SimplePage)o;
				if (i.getPageId() == 0) {
					eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.PAGE_CREATE, "/lessonbuilder/page/" + i.getPageId(), true));
				} else {
					eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.PAGE_UPDATE, "/lessonbuilder/page/" + i.getPageId(), true));
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

    // for use within copytransfer. We don't need to do permissions, and it probably
    // doesn't make sense to log every item created
	public boolean quickSaveItem(Object o) {
		try {
			Object id = getHibernateTemplate().save(o);
			return true;
		} catch (DataAccessException e) {
			log.warn("Hibernate could not save: {}", e.toString());
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
				&& !(o instanceof SimplePage && canEditPage((SimplePage)o))
				&& (o instanceof SimplePage || o instanceof SimplePageItem)) {
			return false;
		}

		if (o instanceof SimplePageItem) {
		    SimplePageItem i = (SimplePageItem)o;
		    eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.ITEM_DELETE, "/lessonbuilder/item/" + i.getId(), true));
		} else if (o instanceof SimplePage) {
		    SimplePage i = (SimplePage)o;
		    eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.PAGE_DELETE, "/lessonbuilder/page/" + i.getPageId(), true));
		} else if(o instanceof SimplePageComment) {
			SimplePageComment i = (SimplePageComment) o;
			eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.COMMENT_DELETE, "/lessonbuilder/comment/" + i.getId(), true));
		}

		try {
			Object p = getDaoHibernateTemplate().merge(o);
			getHibernateTemplate().delete(p);
			getHibernateTemplate().flush();
			return true;
		} catch (DataAccessException | IllegalArgumentException e) {
			log.warn("Hibernate could not delete: {}", e.toString());
			return false;
		}
	}

	public boolean quickDelete(Object o) {
		try {
			Object p = getHibernateTemplate().merge(o);
			getHibernateTemplate().delete(p);
			getHibernateTemplate().flush();
			return true;
		} catch (DataAccessException | IllegalArgumentException e) {
			log.warn("Hibernate could not delete: {}", e.toString());
			return false;
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
				&& !(o instanceof SimplePage && canEditPage((SimplePage)o))
				&& !(o instanceof SimplePageLogEntry || o instanceof SimplePageQuestionResponse)
				&& !(o instanceof SimplePageGroup)) {
			elist.add(nowriteerr);
			return false;
		}
		
		if (o instanceof SimplePageItem) {
			SimplePageItem item = (SimplePageItem)o;
			eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.ITEM_UPDATE, "/lessonbuilder/item/" + item.getId(), true));
		} else if (o instanceof SimplePage) {
			SimplePage page = (SimplePage)o;
			eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.PAGE_UPDATE, "/lessonbuilder/page/" + page.getPageId(), true));
		} else if (o instanceof SimplePageComment) {
			SimplePageComment comment = (SimplePageComment)o;
			eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.COMMENT_UPDATE, "/lessonbuilder/comment/" + comment.getId(), true));
		}

		try {
			if(!(o instanceof SimplePageLogEntry)) {
				getHibernateTemplate().merge(o);
			}else {
				SimplePageLogEntry entry = (SimplePageLogEntry) o;
				boolean isLoggedIn = StringUtils.isNotBlank(entry.getUserId());
				if (isLoggedIn) {
					// Updating seems to always update the timestamp on the log correctly,
					// while merging doesn't always get it right.  However, it's possible that
					// update will fail, so we do both, in order of preference.
					try {
						getHibernateTemplate().update(o);
					} catch (DataAccessException ex) {
						log.warn("Wasn't able to update log entry, timing might be a bit off.");
						getHibernateTemplate().merge(o);
					}
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
			log.error(e.getMessage(), e);
			return false;
		}
	}

	public Long getTopLevelPageId(String toolId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePage.class).add(Restrictions.eq("toolId", toolId))
			.add(Restrictions.isNull("parent")).addOrder(Order.desc("pageId"));

		List list = getHibernateTemplate().findByCriteria(d);

		if ( list == null || list.size() < 1 ) return null;

		if ( list.size() == 1)  return ((SimplePage) list.get(0)).getPageId();

		// When there is more than one page associated with a placement, the data model is
		// broken due to an errant import, timing problem, system crash, NPE or whatever.
		// when it happens // we ignore empty pages and return the non-empty page with
		// the largest primary key (a weak proxy for "latest" but it is all we have)
		// with a warning message.
		log.debug("Scanning {} top pages for placment toolId=", list.size(), toolId);
		SimplePage page = null;
		for (int i = 0; i < list.size(); i++) {
			page = (SimplePage) list.get(i);
			List<SimplePageItem> items = this.findItemsOnPage(page.getPageId());
			if ( items.size() > 0 ) {
				log.warn("Multiple top level pages found, choosing page {} {} with {} items", i, page.getPageId(), items.size());
				return page.getPageId();
			}
			log.debug("Page {} {} is empty", i, page.getPageId());
		}
		log.warn("Multiple top level pages found, Returning {} page", (page != null ? page.getPageId() : null));
		return page != null ? page.getPageId() : null;
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

	/**
	 * Find the main SimplePage for a LessonBuilder tool by its site page ID (toolId)
	 * This method is used by the portal to check if a LessonBuilder page should be hidden from navigation
	 * @param sitePageId the site page ID (which corresponds to toolId in LessonBuilder)
	 * @return the SimplePage or null if not found
	 */
	@Override
	public SimplePage findSimplePageBySitePageId(String sitePageId) {

		return findPageWithToolId(sitePageId);
	}

	public List<ToolConfiguration> getSiteTools(String siteId) {

		try {
			return new ArrayList(siteService.getSite(siteId).getTools(LessonBuilderConstants.TOOL_ID));
		} catch (IdUnusedException iue) {
			log.warn("{} is not a valid site id", siteId);
		}
		return Collections.<ToolConfiguration>emptyList();
	}

	public String getPageUrl(long pageId) {

		List<SimplePageItem> pageItems = findPageItemsBySakaiId(Long.toString(pageId));
		if (pageItems.size() == 0) {
			pageItems = findPageItemsByPageId(pageId);
			if (pageItems.size() == 0) {
				log.error("No page items found for lessons page with id: {}", pageId);
				return null;
			}
		}
		long pageItemId = pageItems.get(0).getId();
		SimplePage page = getPage(pageId);
		SitePage sitePage = siteService.findPage(page.getToolId());
		if (sitePage == null) {
			log.error("Failed to find Sakai SitePage for lessons page with id: {}. Returning null ...", pageId);
			return null;
		}
		String siteId = sitePage.getSiteId();
		Collection<ToolConfiguration> tools = sitePage.getTools(new String[] {LessonBuilderConstants.TOOL_ID});
		if (tools.size() == 0) {
			log.error("Failed to find a lessonbuilder tool for for lessons page with id: {}. Returning null ...", pageId);
			return null;
		}
		String toolId = tools.iterator().next().getId();
		StringBuilder sb = new StringBuilder(serverConfigurationService.getPortalUrl());
		sb.append("/site/").append(siteId).append("/tool/").append(toolId)
			.append("/ShowPage?returnView=&studentItemId=0&sendingPage=").append(pageId)
			.append("&newTopLevel=false&postedComment=false&path=0&itemId=").append(pageItemId)
			.append("&addTool=-1");
		return sb.toString();
	}

	public List<SimplePage> getSitePages(String siteId) {
	    DetachedCriteria d = DetachedCriteria.forClass(SimplePage.class).add(Restrictions.eq("siteId", siteId))
		    .add(Restrictions.disjunction()
				    .add(Restrictions.isNull("owner"))
				    .add(Restrictions.eq("owned", true))
		    );

		List<SimplePage> l = (List<SimplePage>) getHibernateTemplate().findByCriteria(d);

		if (l != null && l.size() > 0) {
		    return l;
		} else {
		    return null;
		}
	}

	public SimplePage findPage(long pageId) {
		return getHibernateTemplate().execute(session -> {
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<SimplePageImpl> query = cb.createQuery(SimplePageImpl.class);
			Root<SimplePageImpl> root = query.from(SimplePageImpl.class);
			query.select(root).where(cb.equal(root.get("pageId"), pageId));
			List<SimplePageImpl> result = session.createQuery(query).getResultList();
			return (result != null && !result.isEmpty()) ? result.get(0) : null;
		});
	}

	public SimplePage findPageWithToolId(String toolId) {
		return getHibernateTemplate().execute(session -> {
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<SimplePageImpl> query = cb.createQuery(SimplePageImpl.class);
			Root<SimplePageImpl> root = query.from(SimplePageImpl.class);
			query.select(root).where(cb.and(cb.equal(root.get("toolId"), toolId),cb.isNull(root.get("parent"))));
			List<SimplePageImpl> result = session.createQuery(query).getResultList();
			return (result != null && !result.isEmpty()) ? result.get(0) : null;
		});
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
	
	// owner not currently used. would need group as well
        public boolean isPageVisited(long pageId, String userId, String owner) {
	    // if this is a student page, it's most likely the top level, so do that query first
	    if (owner != null) {
		Object [] fields = new Object[3];
		fields[0] = pageId;
		fields[1] = pageId;
		fields[2] = userId;
		List<String> ones = sqlService.dbRead("select 1 from lesson_builder_student_pages a, lesson_builder_log b where a.pageId=? and a.itemId = b.itemId and b.studentPageId=? and b.userId=?", fields, null);
		if (ones != null && ones.size() > 0)
		    return true;
	    }

	    Object [] fields = new Object[2];
	    fields[0] = Long.toString(pageId);
	    fields[1] = userId;
	    List<String> ones = sqlService.dbRead("select 1 from lesson_builder_items a, lesson_builder_log b where a.sakaiId=? and a.type=2 and a.id=b.itemId and b.userId=?", fields, null);
	    if (ones != null && ones.size() > 0)
		return true;
	    else
		return false;
	}

	public List<SimplePageLogEntry> getStudentPageLogEntries(long itemId, String userId) {		
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageLogEntry.class).add(Restrictions.eq("userId", userId))
				.add(Restrictions.eq("itemId", itemId))
				.add(Restrictions.isNotNull("studentPageId"));

		return (List<SimplePageLogEntry>) getHibernateTemplate().findByCriteria(d);
	}

	public List<String> findUserWithCompletePages(Long itemId){
		Object [] fields = new Object[1];
		fields[0] = itemId;

		List<String> users = sqlService.dbRead("select a.userId from lesson_builder_log a where a.itemId = ? and a.complete = true", fields, null);

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

	public SimplePageGroup makeGroup(String itemId, String groupId, String groups, String siteId) {
		return new SimplePageGroupImpl(itemId, groupId, groups, siteId);
	}

	public SimplePageQuestionResponse makeQuestionResponse(String userId, long questionId) {
		return new SimplePageQuestionResponseImpl(userId, questionId);
	}

	public SimplePageLogEntry makeLogEntry(String userId, long itemId, Long studentPageId) {
		return new SimplePageLogEntryImpl(userId, itemId, studentPageId);
	}

	public SimplePageComment makeComment(long itemId, long pageId, String author, String comment, String UUID, boolean html) {
		return new SimplePageCommentImpl(itemId, pageId, author, comment, UUID, html);
	}
	
	public SimpleStudentPage makeStudentPage(long itemId, long pageId, String title, String author, String group) {
		return new SimpleStudentPageImpl(itemId, pageId, title, author, group);
	}
	
    // answer is stored as id [int], text, correct [boolean]

	public SimplePageQuestionAnswer makeQuestionAnswer(String text, boolean correct) {
		return new SimplePageQuestionAnswerImpl(0, text, correct);
	}
	
	
    // only implemented for existing questions, i.e. questions with id numbers
	public boolean deleteQuestionAnswer(SimplePageQuestionAnswer questionAnswer, SimplePageItem question) {
		if(!canEditPage(question.getPageId())) {
			log.warn("User tried to edit question on page without edit permission. PageId: {}", question.getPageId());
		    return false;
		}

		// getId returns long, so this can never be null
		Long delid = questionAnswer.getId();

		// find new id number, max + 1
		// JSON uses Long for integer values
		List answers = (List)question.getJsonAttribute("answers");
		Long max = -1L;
		if (answers == null)
		    return false;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    if (delid.equals(answer.get("id"))) {
			answers.remove(a);
			return true;
		    }
		}

		return false;
	}
	
       // methods above are historical. I leave them for completeness. THere are the ones actually used:

	public void clearQuestionAnswers(SimplePageItem question) {
		question.setJsonAttribute("answers", null);
	}

	public Long maxQuestionAnswer(SimplePageItem question)  {
		Long max = 0L;
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null)
		    return max;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    Long i = (Long)answer.get("id");
		    if (i > max)
			max = i;
		}
		return max;
	}

	public Long addQuestionAnswer(SimplePageItem question, Long id, String text, Boolean isCorrect) {
		// no need to check security. that happens when item is saved
		
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null) {
		    answers = new JSONArray();
		    question.setJsonAttribute("answers", answers);
		    if (id <= 0L)
			id = 1L;
		} else if (id <= 0L) {
		    Long max = 0L;
		    for (Object a: answers) {
			Map answer = (Map) a;
			Long i = (Long)answer.get("id");
			if (i > max)
			    max = i;
		    }
		    id = max + 1;
		}
		
		// create and add the json form of the answer
		Map newAnswer = new JSONObject();
		newAnswer.put("id", id);
		newAnswer.put("text", text);
		newAnswer.put("correct", isCorrect);
		answers.add(newAnswer);

		return id;
	}
	
  
	public void clearPeerEvalRows(SimplePageItem question) {
		question.setJsonAttribute("rows", null);
	}

	public Long maxPeerEvalRow(SimplePageItem question)  {
		Long max = 0L;
		List rows = (List)question.getJsonAttribute("rows");
		if (rows == null)
		    return max;
		for (Object a: rows) {
		    Map row = (Map) a;
		    Long i = (Long)row.get("id");
		    if (i > max)
			max = i;
		}
		return max;
	}

    public void addPeerEvalRow(SimplePageItem question, Long id, String text) {

        // no need to check security. that happens when item is saved

        List rows = (List) question.getJsonAttribute("rows");
        if (rows == null) {
            rows = new JSONArray();
            question.setJsonAttribute("rows", rows);
            if (id <= 0L) {
                id = 1L;
            }
        } else if (id <= 0L) {
            Long max = 0L;
            for (Object r: rows) {
                Map row =(Map) r;
                Long i = (Long)row.get("id");
                if (i > max) {
                    max = i;
                }
            }
            id = max +1;
        }
        Map newRow = new JSONObject();
        newRow.put("id", id);
        newRow.put("rowText",text);
        rows.add(newRow);
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
		item.setSameWindow(old.isSameWindow());
		item.setAnonymous(old.isAnonymous());
		item.setShowComments(old.getShowComments());
		item.setForcedCommentsAnonymous(old.getForcedCommentsAnonymous());
		item.setAttributeString(old.getAttributeString()); // copy via json
		item.setShowPeerEval(old.getShowPeerEval());

		//		Map<String, SimplePageItemAttributeImpl> attrs = ((SimplePageItemImpl)old).getAttributes();
		//		if (attrs != null) {
		//		    Collection<SimplePageItemAttributeImpl> attributes = attrs.values();
		//		    if (attributes.size() > 0) {
		//			for (SimplePageItemAttributeImpl attr: attributes) {
		//			    item.setAttribute(attr.getAttr(), attr.getValue());
		//}
		//		    }
		//		}

		return item;
	}

    // phase 2 of copy after save, we need item number here
	public SimplePageItem copyItem2(SimplePageItem old, SimplePageItem item) {
	       
		syncQRTotals(item);

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

	public Map JSONParse(String s) {
	    return (JSONObject)JSONValue.parse(s);
	}

	public Map newJSONObject() {
	    return new JSONObject();
	}

	public List newJSONArray() {
	    return new JSONArray();
	}

	public SimplePageQuestionResponseTotals makeQRTotals(long qid, long rid) {
	    return new SimplePageQuestionResponseTotalsImpl(qid, rid);
	}

	public List<SimplePageQuestionResponseTotals> findQRTotals(long questionId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageQuestionResponseTotals.class).add(Restrictions.eq("questionId", questionId));
		return (List<SimplePageQuestionResponseTotals>) getHibernateTemplate().findByCriteria(d);
	}

	public void incrementQRCount(long questionId, long responseId) {
		getHibernateTemplate().execute(session -> {
			Query query = session.createQuery("update SimplePageQuestionResponseTotalsImpl s set s.count = s.count + 1 where s.questionId= :questionId and s.responseId = :responseId");
			query.setParameter("questionId", questionId, LongType.INSTANCE);
			query.setParameter("responseId", responseId, LongType.INSTANCE);
			return query.executeUpdate();
		});
	}

	public void syncQRTotals(SimplePageItem item) {
	    if (item.getType() != SimplePageItem.QUESTION || ! "multipleChoice".equals(item.getAttribute("questionType")))
		return;
	    
	    Map<Long, SimplePageQuestionResponseTotals> oldTotals = new HashMap<Long, SimplePageQuestionResponseTotals>();
	    List<SimplePageQuestionResponseTotals> oldQrTotals = findQRTotals(item.getId());
	    for (SimplePageQuestionResponseTotals total: oldQrTotals)
		oldTotals.put(total.getResponseId(), total);

	    for (SimplePageQuestionAnswer answer: findAnswerChoices(item)) {
		Long id = answer.getId();
		if (oldTotals.get(id) != null)
		    oldTotals.remove(id);  // in both old and new, done with it
		else {
		    // in new but not old, add it
		    SimplePageQuestionResponseTotals total = makeQRTotals(item.getId(), id);
		    quickSaveItem(total);
		}
	    }

	    // entries that were in old list but not new one, remove them
	    for (Long rid: oldTotals.keySet()) {
		deleteItem(oldTotals.get(rid));
	    }
	}

	public SimplePagePeerEval findPeerEval(long itemId) {
		SimplePageItem item = findItem(itemId);
	
		List rows = (List)item.getJsonAttribute("rows");
		if (rows == null)
		    return null;
		for (Object a: rows) {
		    Map row = (Map) a;
		}
		return null;
	}

	public List<SimplePagePeerEvalResult> findPeerEvalResult(long pageId,String grader,String gradee,String gradeeGroup) 
	{
				
		String hql = "select result from org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResult result where result.pageId = :pageId and result.grader = :grader and result.selected = 1 and ";
		if (gradee != null && gradeeGroup != null)
		    hql += "(result.gradee = :gradee or result.gradeeGroup = :gradeeGroup)";
		else if (gradee != null)
		    hql += "result.gradee = :gradee";
		else
		    hql += "result.gradeeGroup = :gradeeGroup";

		int n = 3;
		if (gradee != null && gradeeGroup != null)
		    n = 4;
		String[] names = new String[n];
		Object[] values = new Object[n];
		names[0] = "pageId"; values[0] = pageId;
		names[1] = "grader"; values[1] = grader;
		if (gradee != null && gradeeGroup != null) {
		    names[2] = "gradee"; values[2] = gradee;
		    names[3] = "gradeeGroup"; values[3] = gradeeGroup;
		} else if (gradee != null) {
		    names[2] = "gradee"; values[2] = gradee;
		} else {
		    names[2] = "gradeeGroup"; values[2] = gradeeGroup;
		}

		return (List<SimplePagePeerEvalResult>)getHibernateTemplate().findByNamedParam(hql, names, values);

	}

	public SimplePagePeerEvalResult makePeerEvalResult(long pageId, String gradee,String gradeeGroup, String grader, String rowText, long rowId, int columnValue){
		return new SimplePagePeerEvalResultImpl(pageId, gradee, gradeeGroup, grader, rowText, rowId, columnValue);
	}
	
	public List<SimplePagePeerEvalResult> findPeerEvalResultByOwner(long pageId,String gradee, String gradeeGroup){
		String hql = "select result from org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResult result where result.pageId = :pageId and result.selected = 1 and ";
		if (gradee != null && gradeeGroup != null)
		    hql += "(result.gradee = :gradee or result.gradeeGroup = :gradeeGroup)";
		else if (gradee != null)
		    hql += "result.gradee = :gradee";
		else
		    hql += "result.gradeeGroup = :gradeeGroup";

		int n = 2;
		if (gradee != null && gradeeGroup != null)
		    n = 3;
		String[] names = new String[n];
		Object[] values = new Object[n];
		names[0] = "pageId"; values[0] = pageId;
		if (gradee != null && gradeeGroup != null) {
		    names[1] = "gradee"; values[1] = gradee;
		    names[2] = "gradeeGroup"; values[2] = gradeeGroup;
		} else if (gradee != null) {
		    names[1] = "gradee"; values[1] = gradee;
		} else {
		    names[1] = "gradeeGroup"; values[1] = gradeeGroup;
		}

		return (List<SimplePagePeerEvalResult>)getHibernateTemplate().findByNamedParam(hql, names, values);

	}

	public List<SimplePageItem>findGradebookItems(final String gradebookUid) {

	    String hql = "select item from org.sakaiproject.lessonbuildertool.SimplePageItem item, org.sakaiproject.lessonbuildertool.SimplePage page where item.pageId = page.pageId and page.siteId = :site and (item.gradebookId is not null or item.altGradebook is not null)";
	    return (List<SimplePageItem>) getHibernateTemplate().findByNamedParam(hql, "site", gradebookUid);
	}
	    
	public List<SimplePage>findGradebookPages(final String gradebookUid) {

	    String hql = "select page from org.sakaiproject.lessonbuildertool.SimplePage page where page.siteId = :site and (page.gradebookPoints is not null)";
	    return (List<SimplePage>) getHibernateTemplate().findByNamedParam(hql, "site", gradebookUid);
	}

	// items in lesson_builder_groups for specified site, map of itemId to groups
	public Map<String,String> getExternalAssigns(String siteId) {

	    DetachedCriteria d = DetachedCriteria.forClass(SimplePageGroup.class)
		.add(Restrictions.eq("siteId", siteId));

	    List<SimplePageGroup> list = (List<SimplePageGroup>) getHibernateTemplate().findByCriteria(d);

	    Map<String,String>ret = new HashMap<String,String>();	
	    for (SimplePageGroup group: list)
		ret.put(group.getItemId(), group.getGroups());

	    return ret;
	    
	}
    
    // return 1 if we found something, 0 if not
    // this is only going to find something once per site, typically.
    // so it's best to optimize for the normal case that nothing is there
    // initialy this called
    // dbWriteCount("delete from SAKAI_SITE_PROPERTY where SITE_ID=? and NAME='lessonbuilder-needsfixup'", fields, null, null, false);
    // but that doesn't exist in 2.8.

	public int clearNeedsFixup(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;

	    List<String> needsList = sqlService.dbRead("select VALUE from SAKAI_SITE_PROPERTY where SITE_ID=? and NAME='lessonbuilder-needsfixup'", fields, null);
	    
	    // normal case -- no flag
	    if (needsList == null || needsList.size() == 0) {
	      return 0;
	    }

	    // there is a flag, do something more carefully avoiding race conditions
	    //   There is a possible timing issue if someone copies data into the site after the
	    // last test. If so, we'll get it next time someone uses the site.
	    // we need to be provably sure that if the flag is set, this code returns 1 exactly once.
	    // I believe that is the case.

	    int retval = 0;
	    Connection conn = null;
	    boolean wasCommit = true;

	    try {
		conn = sqlService.borrowConnection();
		needsList = sqlService.dbRead(conn, "select VALUE from SAKAI_SITE_PROPERTY where SITE_ID=? and NAME='lessonbuilder-needsfixup' for update", fields, null);
		wasCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		if (needsList != null && needsList.size() > 0) {
		    retval = 1;
		    try {
			retval = Integer.parseInt(needsList.get(0));
		    } catch (Exception ignore) {}
		    sqlService.dbWrite(conn, "delete from SAKAI_SITE_PROPERTY where SITE_ID=? and NAME='lessonbuilder-needsfixup'", fields);
		}
		
		conn.commit();

	// I don't think we need to handle errrors explicitly. They will result in
	// returning 0, which is about the best we can do
	    } catch (Exception e) {
	    } finally {

		if (conn != null) {

		    try {
			conn.setAutoCommit(wasCommit);
		    } catch (Exception e) {
				log.warn("transact: (setAutoCommit): {}", e.toString());
		    }
  
		    sqlService.returnConnection(conn);
		}

	    }

	    return retval;

	}

	public void setNeedsGroupFixup(String siteId, final int value) {
	    // I'm doing this in hibernate because the table is cached and I don't want
	    // hibernate's caceh to be out of date
	    final String property = "groupfixup " + siteId;
	    getHibernateTemplate().flush();

	    Session session = getSessionFactory().openSession();
	    Transaction tx = session.getTransaction();
	    try {
		tx = session.beginTransaction();

		Query query = session.createQuery("from SimplePagePropertyImpl as prop where prop.attribute = :attr");
		query.setParameter("attr", property, StringType.INSTANCE);

		SimplePageProperty prop = (SimplePageProperty)query.uniqueResult();

		if (prop != null) {
		    int oldValue = 0;
		    try {
			oldValue = Integer.parseInt(prop.getValue());
		    } catch (Exception e) {};
		    if (value > oldValue)
			prop.setValue(Integer.toString(value));
		} else {
		    prop = makeProperty(property, Integer.toString(value));
		}
	    
		session.saveOrUpdate(prop);

		tx.commit();

	    } catch (Exception e) {
		if (tx != null)
		    tx.rollback();
	    } finally {
		session.close();
	    }

	}

    // for efficiency, we first check a cached reference. On multiple systems
    // this may be out of date, but the most common case is that the instructor copies the site and
    // then tries it on the same server. If not, the fixup can be delayed 10 min. I thikn that's OK
    // really don't want to add one db query per page display for this stupid thing

	public int clearNeedsGroupFixup(String siteId) {
	    String property = "groupfixup " + siteId;

	    DetachedCriteria d = DetachedCriteria.forClass(SimplePageProperty.class).add(Restrictions.eq("attribute", property));
	    List<SimplePageProperty> list = (List<SimplePageProperty>) getHibernateTemplate().findByCriteria(d);

	    if (list == null || list.size() == 0)
		return 0;

	    // there is a flag, do something more carefully avoiding race conditions
	    //   There is a possible timing issue if someone copies data into the site after the
	    // last test. If so, we'll get it next time someone uses the site.
	    // we need to be provably sure that if the flag is set, this code returns 1 exactly once.
	    // I believe that is the case.

	    getHibernateTemplate().flush();

	    int retval = 0;

	    Session session = getSessionFactory().openSession();
	    Transaction tx = session.getTransaction();
	    try {
		tx = session.beginTransaction();

		Query query = session.createQuery("from SimplePagePropertyImpl as prop where prop.attribute = :attr");
		query.setParameter("attr", property, StringType.INSTANCE);

		SimplePageProperty prop = (SimplePageProperty)query.uniqueResult();

		// if it's there, remember current value and delete
		if (prop != null) {
		    try {
			retval = Integer.parseInt(prop.getValue());
		    } catch (Exception e) {};
		    session.delete(prop);
		}
	    
		tx.commit();

	    } catch (Exception e) {
		if (tx != null)
		    tx.rollback();
	    } finally {
		session.close();
	    }

	    return retval;

	}

	public Map<String,String> getObjectMap(String siteId) {
    
	    Object [] fields = new Object[1];
	    fields[0] = siteId;
	    final Map<String,String> objectMap = new HashMap<String, String>();
	    sqlService.dbRead("select a.sakaiId,a.alt from lesson_builder_items a, lesson_builder_pages b where a.pageId=b.pageId and b.siteId=? and a.type in (3,4,8,21)", fields, new SqlReader() {
		    public Object readSqlResultRecord(ResultSet result) {
    			try {
			    String newObject = result.getString(1);
			    String oldObject = result.getString(2);
			    if (oldObject != null && oldObject.length()> 0 && !oldObject.startsWith("sam_core")) {
				int i = oldObject.indexOf("/");
				if (i >= 0)
				    i = oldObject.indexOf("/", i+1);
				if (i >= 0)
				    oldObject = oldObject.substring(0, i);
				oldObject = "/" + oldObject;
				objectMap.put(oldObject, newObject);
			    }
			    // also put new object in map
			    if (!newObject.startsWith("/sam_core"))
				objectMap.put(newObject, "");
			    return null;
    			} catch (SQLException e) {
					log.warn("findTextItemsInSite: {}", e.toString());
					return null;
    			}
		    }
		});

	    return objectMap;
	}

	public boolean doesPageFolderExist(final String siteId, final String folder) {

	    Object [] fields = new Object[2];
	    fields[0] = siteId;
	    fields[1] = folder;

	    List<String> counts = sqlService.dbRead("select count(*) from lesson_builder_pages where siteId = ? and folder = ?", fields, null);

	    Long count = new Long(counts.get(0));

	    return (count > 0);

	}

	public List<SimpleChecklistItem> findChecklistItems(SimplePageItem checklist) {
		List<SimpleChecklistItem> ret = new ArrayList<SimpleChecklistItem>();

		// find new id number, max + 1
		List checklistItems = (List)checklist.getJsonAttribute("checklistItems");
		if (checklistItems == null)
			return ret;
		for (Object i: checklistItems) {
			Map checklistItem = (Map) i;
			SimpleChecklistItem newChecklistItem = new SimpleChecklistItemImpl((Long)checklistItem.get("id"), (String)checklistItem.get("name"), (Long)checklistItem.get("link"));
			ret.add(newChecklistItem);
		}
		return ret;
	}

	public SimpleChecklistItem findChecklistItem(SimplePageItem checklist, long checklistItemId) {
		// find new id number, max + 1
		List checklistItems = (List)checklist.getJsonAttribute("checklistItems");
		if (checklistItems == null)
			return null;
		for (Object i: checklistItems) {
			Map checklistItem = (Map) i;
			if (checklistItemId == (Long)checklistItem.get("id")) {
				SimpleChecklistItem newChecklistItem = new SimpleChecklistItemImpl(checklistItemId, (String)checklistItem.get("name"), (Long)checklistItem.get("link"));
				return newChecklistItem;
			}
		}
		return null;
	}

	public boolean deleteAllSavedStatusesForChecklist(SimplePageItem checklist) {
		try {
			List<ChecklistItemStatus> checklistItemStatuses = findChecklistItemStatusesForChecklist(checklist.getId());
			if(checklistItemStatuses != null) {
				getHibernateTemplate().deleteAll(checklistItemStatuses);
			}
			return true;
		} catch (DataAccessException dae) {
			log.warn("Unable to delete all saved status for checklist: {}", dae.toString());
			return false;
		}
	}

	public boolean deleteAllSavedStatusesForChecklistItem(long checklistId, long checklistItemId) {
		try {
			List<ChecklistItemStatus> checklistItemStatuses = findChecklistItemStatusesForChecklistItem(checklistId, checklistItemId);
			if(checklistItemStatuses != null) {
				getHibernateTemplate().deleteAll(checklistItemStatuses);
			}
			return true;
		} catch (DataAccessException dae) {
			log.warn("Unable to delete all checklist item statuses for checklist item {}", dae.toString());
			return false;
		}
	}

	public void clearChecklistItems(SimplePageItem checklist) {
		checklist.setJsonAttribute("checklistItems", null);
	}

	public Long maxChecklistItem(SimplePageItem checklist)  {
		Long max = 0L;
		List checklistItems = (List)checklist.getJsonAttribute("checklistItems");
		if (checklistItems == null)
			return max;
		for (Object i: checklistItems) {
			Map item = (Map) i;
			Long id = (Long)item.get("id");
			if (id > max)
				max = id;
		}
		return max;
	}

	public Long addChecklistItem(SimplePageItem checklist, Long id, String name, Long linkedId) {
		// no need to check security. that happens when item is saved

		List checklistItems = (List)checklist.getJsonAttribute("checklistItems");
		if (checklistItems == null) {
			checklistItems = new JSONArray();
			checklist.setJsonAttribute("checklistItems", checklistItems);
			if (id <= 0L) {
				id = 1L;
			}
		} else if (id <= 0L) {
			Long max = 0L;
			for (Object i: checklistItems) {
				Map item = (Map) i;
				Long newId = (Long)item.get("id");
				if (newId > max)
					max = newId;
			}
			id = max + 1;
		}

		// create and add the json form of the answer
		Map newChecklistItem = new JSONObject();
		newChecklistItem.put("id", id);
		newChecklistItem.put("name", name);
		newChecklistItem.put("link", linkedId);
		checklistItems.add(newChecklistItem);

		return id;
	}

	@SuppressWarnings("unchecked")
	public boolean isChecklistItemChecked(long checklistId, long checklistItemId, String userId) {
		DetachedCriteria d = DetachedCriteria.forClass(ChecklistItemStatus.class)
				.add(Restrictions.eq("id.checklistId", checklistId))
				.add(Restrictions.eq("id.checklistItemId", checklistItemId))
				.add(Restrictions.eq("id.owner", userId));

		List<ChecklistItemStatus> list = (List<ChecklistItemStatus>) getHibernateTemplate().findByCriteria(d);

		if(list.size() > 0) {
			return list.get(0).isDone();
		}else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<ChecklistItemStatus> findChecklistItemStatusesForChecklist(long checklistId) {
		DetachedCriteria d = DetachedCriteria.forClass(ChecklistItemStatus.class)
				.add(Restrictions.eq("id.checklistId", checklistId));
		List<ChecklistItemStatus> list = (List<ChecklistItemStatus>) getHibernateTemplate().findByCriteria(d);

		if(list.size() > 0) {
			return list;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<ChecklistItemStatus> findChecklistItemStatusesForChecklistItem(long checklistId, long checklistItemId) {
		DetachedCriteria d = DetachedCriteria.forClass(ChecklistItemStatus.class)
				.add(Restrictions.eq("id.checklistId", checklistId))
				.add(Restrictions.eq("id.checklistItemId", checklistItemId));
		List<ChecklistItemStatus> list = (List<ChecklistItemStatus>) getHibernateTemplate().findByCriteria(d);

		if(list.size() > 0) {
			return list;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public ChecklistItemStatus findChecklistItemStatus(long checklistId, long checklistItemId, String userId) {
		DetachedCriteria d = DetachedCriteria.forClass(ChecklistItemStatus.class)
				.add(Restrictions.eq("id.checklistId", checklistId))
				.add(Restrictions.eq("id.checklistItemId", checklistItemId))
				.add(Restrictions.eq("id.owner", userId));
		List<ChecklistItemStatus> list = (List<ChecklistItemStatus>) getHibernateTemplate().findByCriteria(d);

		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}

	public boolean saveChecklistItemStatus(ChecklistItemStatus checklistItemStatus) {
		try {
			getHibernateTemplate().saveOrUpdate(checklistItemStatus);
			return true;
		} catch (DataAccessException e) {
			log.warn("Failed to save checklist item status {}", checklistItemStatus.toString());
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<SimplePageItem> findAllChecklistsInSite(String siteId) {
		String hql = "select item from org.sakaiproject.lessonbuildertool.SimplePageItem item, org.sakaiproject.lessonbuildertool.SimplePage page where item.pageId = page.pageId and page.siteId = :site and item.type = 15";
		return (List<SimplePageItem>) getHibernateTemplate().findByNamedParam(hql, "site", siteId);
	}


	@Override
	public String getLessonSubPageJSON(final String userId, final String siteId, final Collection<String> pageIds) {
		if (!pageIds.isEmpty()) {
			String ref = siteService.siteReference(siteId);
			boolean canSeeAll = securityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref) || securityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL, ref);

			try {
				final List<String> groupIds = siteService.getSite(siteId).getGroupsWithMember(userId).stream().map(Group::getId).collect(Collectors.toList());
				final LessonsSubNavBuilder lessonsSubNavBuilder = new LessonsSubNavBuilder(userTimeService, siteId, canSeeAll, groupIds);

				List<SimplePage> lp = new ArrayList<>();
				Map<SimplePage, String> m = new HashMap<>();
				for (String pageId : pageIds) {
					SimplePage p = findPageWithToolId(pageId);
					if (p == null) {
						log.debug("Page ID [{}] in site {} not found. It is most likely blank.", pageId, siteId);
						continue;
					}
					lp.add(p);

					try {
						String sakaiToolId = siteService.getSite(siteId).getPage(p.getToolId()).getTools().get(0).getId();
						m.put(p, sakaiToolId);
						findSubPageItemsByPageId(p.getPageId()).forEach(spi -> lessonsSubNavBuilder.processResult(
								sakaiToolId,
								p,
								spi,
								findPage(Long.parseLong(spi.getSakaiId())),
								getLogEntry(userId, spi.getId(), -1L))
						);
					} catch (Exception e) {
						// This condition commonly occurs when executing Site Info > Import from Site, such that
						// the top-level pages for a target site do not yet exist or have just been created. The
						// problem resolves upon the next browser refresh.
						log.warn("page id [{}] in site [{}] not found, {}", pageId, siteId, e);
					}
				}

				for (SimplePage p : lp) {
					findTopLevelPageItem(p.getPageId()).ifPresent(spi -> {
						SimplePageLogEntry le = getLogEntry(userId, spi.getId(), -1L);
						lessonsSubNavBuilder.processTopLevelPageProperties(m.get(p), p, spi, le);
					});
				}

				return lessonsSubNavBuilder.toJSON();
			} catch (Exception impossible) {
				log.warn("Could not retrieve groups for site [{}]", impossible, impossible);
			}
		}
		return null;
	}

    // returns top level pages; null if none
	public List<SimplePage> getTopLevelPages(final String siteId) {
	    // set of all top level pages, actually the items pointing to them                                                                       
		try {
			List<SitePage> sitePages = siteService.getSite(siteId).getOrderedPages();
			if (sitePages.isEmpty()) {
				return null;
			}

			final List<String> sitePageIds = sitePages.stream().map(sp -> sp.getId()).collect(Collectors.toList());

			DetachedCriteria d = DetachedCriteria.forClass(SimplePage.class);
			d.add(Restrictions.in("toolId", sitePageIds));
			d.add(Restrictions.isNull("parent"));

			List<SimplePage> lessonsPages = (List<SimplePage>) getHibernateTemplate().findByCriteria(d);

			return lessonsPages;

		} catch (IdUnusedException e) {
			log.warn("Could not find site {}: {}", siteId, e);
		}
		return null;
	}

    /**
     * Gets the top level page items, ordered to correspond with the top level lessons tools
     */
    public List<SimplePageItem> getOrderedTopLevelPageItems(String siteId) {

        // The unordered top level items
        final List<SimplePageItem> tmpSiteItems = findItemsInSite(siteId);

        final List<ToolConfiguration> siteTools = getSiteTools(siteId);

        if (siteTools.size() < 1) {
            return tmpSiteItems;
        }

        // build map of all pages, so we can see if any are left over
        final Map<Long, SimplePage> pageMap = getSitePages(siteId)
                .stream().collect(Collectors.toMap(SimplePage::getPageId, Function.identity()));

        return siteTools.stream().map(t -> {

            return tmpSiteItems
                .stream()
                .filter(spi -> pageMap.get(Long.valueOf(spi.getSakaiId())).getToolId().equals(t.getPageId()))
                .findAny().orElse(null);

        }).filter(spi -> spi != null).collect(Collectors.toList());
    }

	public void deleteLogForLessonsItem(SimplePageItem item) {
		try {
			DetachedCriteria d2 = DetachedCriteria.forClass(SimplePageLogEntry.class).add(Restrictions.eq("itemId",item.getId()));
			List<SimplePageLogEntry> logEntries = (List<SimplePageLogEntry>) getHibernateTemplate().findByCriteria(d2);
			getHibernateTemplate().deleteAll(logEntries);
		} catch (DataAccessException e) {
			log.warn("Failed to delete lessons log for item {}", item.getId());
		}
	}

	public void deleteQuestionResponsesForItem(SimplePageItem item) {
		try {
			DetachedCriteria d = DetachedCriteria.forClass(SimplePageQuestionResponse.class).add(Restrictions.eq("questionId",item.getId()));
			DetachedCriteria d2 = DetachedCriteria.forClass(SimplePageQuestionResponseTotals.class).add(Restrictions.eq("questionId",item.getId()));
			getHibernateTemplate().deleteAll(getHibernateTemplate().findByCriteria(d));
			getHibernateTemplate().deleteAll(getHibernateTemplate().findByCriteria(d2));
		} catch (DataAccessException e) {
			log.error("Failed to delete SimplePageQuestion responses for item {}: {}", item.getId(), e.toString());
		}
	}

	public void deleteCommentsForLessonsItem(SimplePageItem item) {
		try {
			DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("itemId",item.getId()));
			getHibernateTemplate().deleteAll(getHibernateTemplate().findByCriteria(d));
		} catch (DataAccessException e) {
			log.error("Failed to delete SimplePageComments for item {}: {}", item.getId(), e.toString());
		}
	}

	@Override
	public String getName() {
		return "sakai.lessonbuildertool";
	}

	@Override
	public String getData(String siteId, String userId, Collection<String> pageIds) {
		return getLessonSubPageJSON(userId, siteId, pageIds);
	}
}
