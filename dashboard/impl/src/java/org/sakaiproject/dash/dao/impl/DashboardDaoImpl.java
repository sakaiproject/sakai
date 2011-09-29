/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.dash.dao.impl;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.apache.log4j.Logger;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.PersonContext;
import org.sakaiproject.dash.model.PersonContextSourceType;
import org.sakaiproject.dash.model.PersonSourceType;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of ProjectDao
 * 
 * 
 *
 */
public class DashboardDaoImpl extends JdbcDaoSupport implements DashboardDao {

	private static final Logger log = Logger.getLogger(DashboardDaoImpl.class);
	
	protected ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	protected PropertiesConfiguration statements;
	
	public boolean addAvailabilityCheck(AvailabilityCheck availabilityCheck) {
		if(log.isDebugEnabled()) {
			log.debug("addAvailabilityCheck( " + availabilityCheck.toString() + ")");
		}
		
		// entity_ref, scheduled_time
		
		try {
			JdbcTemplate template = getJdbcTemplate();
			String sql = getStatement("insert.AvailabilityCheck");
			
			template.update(sql,
				new Object[]{availabilityCheck.getEntityReference(), availabilityCheck.getEntityTypeId(), 
						availabilityCheck.getScheduledTime()}
			);
			return true;
		} catch (DataAccessException ex) {
            log.error("addAvailabilityCheck: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
            return false;
		} catch (Exception e) {
	        log.error("addAvailabilityCheck: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addCalendarItem(org.sakaiproject.dash.model.CalendarItem)
	 */
	public boolean addCalendarItem(CalendarItem calendarItem) {
		if(log.isDebugEnabled()) {
			log.debug("addCalendarItem( " + calendarItem.toString() + ")");
		}
		
		// calendar_time, title , entity_url, entity_ref, source_type, context_id, realm_id
		
		try {
			JdbcTemplate template = getJdbcTemplate();
			String sql = getStatement("insert.CalendarItem");
			
			template.update(sql,
				new Object[]{calendarItem.getCalendarTime(), calendarItem.getCalendarTimeLabelKey(), calendarItem.getTitle(), 
						calendarItem.getEntityUrl(), calendarItem.getEntityReference(),
						calendarItem.getSourceType().getId(), calendarItem.getContext().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
            log.error("addCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
            return false;
		} catch (Exception e) {
	        log.error("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addCalendarLink(org.sakaiproject.dash.model.CalendarLink)
	 */
	public boolean addCalendarLink(CalendarLink calendarLink) {
		if(log.isDebugEnabled()) {
			log.debug("addCalendarLink( " + calendarLink.toString() + ")");
		}
		
		//  person_id, item_id, context_id, realm_id
		
		try {
			getJdbcTemplate().update(getStatement("insert.CalendarLink"),
				new Object[]{calendarLink.getPerson().getId(), calendarLink.getCalendarItem().getId(), 
						calendarLink.getContext().getId(), calendarLink.isHidden(), calendarLink.isSticky()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addContext(org.sakaiproject.dash.model.Context)
	 */
	public boolean addContext(Context context) {
		if(log.isDebugEnabled()) {
			log.debug("addContext( " + context.toString() + ")");
		}
		
		//  context_id, context_url, context_title
		
		String sql = getStatement("insert.Context");
		try {
			int rows = getJdbcTemplate().update(sql ,
				new Object[]{context.getContextId(), context.getContextUrl(), 
				context.getContextTitle()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		} catch (Exception e) {
	        log.error("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addNewsItem(org.sakaiproject.dash.model.NewsItem)
	 */
	public boolean addNewsItem(NewsItem newsItem) {
		if(log.isDebugEnabled()) {
			log.debug("addNewsItem( " + newsItem.toString() + ")");
		}
		
		// news_time, title , entity_url, entity_ref, source_type, context_id, realm_id
		
		try {
			getJdbcTemplate().update(getStatement("insert.NewsItem"),
				new Object[]{newsItem.getNewsTime(), newsItem.getTitle(), 
						newsItem.getEntityUrl(), newsItem.getEntityReference(),
						newsItem.getSourceType().getId(), newsItem.getContext().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addNewsLink(org.sakaiproject.dash.model.NewsLink)
	 */
	public boolean addNewsLink(NewsLink newsLink) {
		if(log.isDebugEnabled()) {
			log.debug("addNewsLink( " + newsLink.toString() + ")");
		}
		
		//  person_id, item_id, context_id, realm_id
		
		try {
			getJdbcTemplate().update(getStatement("insert.NewsLink"),
				new Object[]{newsLink.getPerson().getId(), newsLink.getNewsItem().getId(), 
						newsLink.getContext().getId(), newsLink.isHidden(), newsLink.isSticky()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addPerson(org.sakaiproject.dash.model.Person)
	 */
	public boolean addPerson(Person person) {
		if(log.isDebugEnabled()) {
			log.debug("addPerson( " + person.toString() + ")");
		}
		
		//  user_id,sakai_id
		
		try {
			getJdbcTemplate().update(getStatement("insert.Person"),
				new Object[]{person.getUserId(), person.getSakaiId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addPerson: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}
	}
	
	public boolean addPersonContext(PersonContext personContext) {
		if(log.isDebugEnabled()) {
			log.debug("addPersonContext( " + personContext.toString() + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("insert.PersonContext"),
				new Object[]{personContext.getItemType().getValue(), personContext.getPerson().getId(), personContext.getContext().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addPersonContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}
	}

	public boolean addPersonContextSourceType(PersonContextSourceType personContextSourceType) {
		if(log.isDebugEnabled()) {
			log.debug("addPersonContextSourceType( " + personContextSourceType.toString() + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("insert.PersonContextSourceType"),
				new Object[]{personContextSourceType.getItemType().getValue() ,personContextSourceType.getPerson().getId(), personContextSourceType.getContext().getId(), personContextSourceType.getSourceType().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addPersonContextSourceType: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}
	}

	public boolean addPersonSourceType(PersonSourceType personSourceType) {
		if(log.isDebugEnabled()) {
			log.debug("addPersonSourceType( " + personSourceType.toString() + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("insert.PersonSourceType"),
				new Object[]{personSourceType.getItemType().getValue(), personSourceType.getPerson().getId(), personSourceType.getSourceType().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addPersonSourceType: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addRealm(org.sakaiproject.dash.model.Realm)
	 */
	public boolean addRealm(Realm realm) {
		if(log.isDebugEnabled()) {
			log.debug("addRealm( " + realm.toString() + ")");
		}
		
		//  realm_id
		
		try {
			getJdbcTemplate().update(getStatement("insert.Realm"),
				new Object[]{realm.getRealmId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addRealm: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addSourceType(org.sakaiproject.dash.model.SourceType)
	 */
	public boolean addSourceType(SourceType sourceType) {
		if(log.isDebugEnabled()) {
			log.debug("addSourceType( " + sourceType.toString() + ")");
		}
		
		// name
		
		try {
			getJdbcTemplate().update(getStatement("insert.SourceType"),
				new Object[]{sourceType.getIdentifier(), sourceType.getAccessPermission()}
			
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addSourceType: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
	
	public boolean deleteAvailabilityChecks(String entityReference) {
		if(log.isDebugEnabled()) {
			log.debug("deleteAllAvailabilityChecks( " + entityReference + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.AvailabilityChecks.by.entityReference"),
				new Object[]{entityReference}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("deleteAllAvailabilityChecks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
		
	}

	public boolean deleteAvailabilityChecksBeforeTime(Date time) {
		if(log.isDebugEnabled()) {
			log.debug("deleteAvailabilityChecksBeforeTime(" + time + ")");
		}
		String sql = getStatement("delete.AvailabilityChecks.before.date");
		Object[] params = new Object[]{time};
		try {
			getJdbcTemplate().update(sql,params);
			return true;
			
		} catch (DataAccessException ex) {
           log.error("deleteAvailabilityChecksBeforeTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	public boolean deleteCalendarItem(Long id) {
		if(log.isDebugEnabled()) {
			log.debug("deleteCalendarItem( " + id + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.CalendarItem.by.id"),
				new Object[]{id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("deleteCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	public boolean deleteNewsItem(Long id) {
		if(log.isDebugEnabled()) {
			log.debug("deleteNewsItem( " + id + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.NewsItem.by.id"),
				new Object[]{id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("deleteNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	public boolean deleteCalendarLink(Long personId, Long calendarItemId) {
		if(log.isDebugEnabled()) {
			log.debug("deleteCalendarLink(" + personId + "," + calendarItemId + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.CalendarLink.by.personId.itemId"),
				new Object[]{personId, calendarItemId}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("deleteCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarLinks(java.lang.Long)
	 */
	public boolean deleteCalendarLinks(Long calendarItemId) {
		if(log.isDebugEnabled()) {
			log.debug("deleteCalendarLinks( " + calendarItemId + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.CalendarLinks.by.itemId"),
				new Object[]{calendarItemId}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("deleteCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	public boolean deleteCalendarLinks(Long personId, Long contextId) {
		if(log.isDebugEnabled()) {
			log.debug("deleteCalendarLinks( " + personId + "," + contextId + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.CalendarLinks.by.person.context"),
				new Object[]{personId, contextId}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("deleteCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	public boolean deleteNewsLink(Long personId, Long newsItemId) {
		if(log.isDebugEnabled()) {
			log.debug("deleteNewsLink(" + personId + "," + newsItemId + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.NewsLink.by.personId.itemId"),
				new Object[]{personId, newsItemId}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("deleteNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsLinks(java.lang.Long)
	 */
	public boolean deleteNewsLinks(Long newsItemId) {
		if(log.isDebugEnabled()) {
			log.debug("deleteNewsLinks( " + newsItemId + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.NewsLinks.by.itemId"),
				new Object[]{newsItemId}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("deleteNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}
	
	public boolean deleteNewsLinks(Long personId, Long contextId) {
		if(log.isDebugEnabled()) {
			log.debug("deleteNewsLinks( " + personId + "," + contextId + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.NewsLinks.by.person.context"),
				new Object[]{personId, contextId}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("deleteCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}
	
	public List<AvailabilityCheck> getAvailabilityChecksBeforeTime(Date time) {
		if(log.isDebugEnabled()) {
			log.debug("getAvailabilityChecksBeforeTime(" + time + ")");
		}
		String sql = getStatement("select.AvailabilityChecks.before.date");
		Object[] params = new Object[]{time};
		try {
			return (List<AvailabilityCheck>) getJdbcTemplate().query(sql,params,
				new AvailabilityCheckMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getAvailabilityChecksBeforeTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<AvailabilityCheck>();
		}
	}

	public SourceType getSourceType(long sourceTypeId) {
		if(log.isDebugEnabled()) {
			log.debug("getSourceType( " + sourceTypeId + ")");
		}
		
		try {
			return (SourceType) getJdbcTemplate().queryForObject(getStatement("select.SourceType.by.id"),
				new Object[]{sourceTypeId},
				new SourceTypeMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getSourceType: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}


	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getSourceType(java.lang.String)
	 */
	public SourceType getSourceType(String sourceTypeName) {
		if(log.isDebugEnabled()) {
			log.debug("getSourceType( " + sourceTypeName + ")");
		}
		
		try {
			return (SourceType) getJdbcTemplate().queryForObject(getStatement("select.SourceType.by.identifier"),
				new Object[]{sourceTypeName},
				new SourceTypeMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getSourceType: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItem(long)
	 */
	public CalendarItem getCalendarItem(long id) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItem(" + id + ")");
		}
		
		try {
			return (CalendarItem) getJdbcTemplate().queryForObject(getStatement("select.CalendarItem.by.id"),
				new Object[]{id},
				new CalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}

	}

	public CalendarItem getCalendarItem(String entityReference, String calendarTimeLabelKey) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItem(" + entityReference + "," + calendarTimeLabelKey + ")");
		}
		
		try {
			return (CalendarItem) getJdbcTemplate().queryForObject(getStatement("select.CalendarItem.by.entityReference.calendarTimeLabelKey"),
				new Object[]{entityReference, calendarTimeLabelKey},
				new CalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItem(java.lang.String)
	 */
	public List<CalendarItem> getCalendarItems(String entityReference) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItems(" + entityReference + ")");
		}
		
		try {
			return (List<CalendarItem>) getJdbcTemplate().query(getStatement("select.CalendarItems.by.entityReference"),
				new Object[]{entityReference},
				new CalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarItems: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItems(java.lang.String, java.lang.String)
	 */
	public List<CalendarItem> getCalendarItems(String sakaiUserId,
			String contextId, boolean saved, boolean hidden) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItems(" + sakaiUserId + "," + contextId + ")");
		}
		String sql = null;
		Object[] params = null;
		if(contextId == null) {
			if(hidden) {
				sql = getStatement("select.CalendarItems.by.sakaiId.hidden");
			} else if(saved) {
				sql = getStatement("select.CalendarItems.by.sakaiId.sticky");
			} else {
				sql = getStatement("select.CalendarItems.by.sakaiId");
			}
			params = new Object[]{sakaiUserId};
		} else {
			if(hidden) {
				sql = getStatement("select.CalendarItems.by.sakaiId.contextId.hidden");
			} else if(saved) {
				sql = getStatement("select.CalendarItems.by.sakaiId.contextId.sticky");
			} else {
				sql = getStatement("select.CalendarItems.by.sakaiId.contextId");
			}
			params = new Object[]{sakaiUserId, contextId};
		}
		log.info("getCalendarItems(" + sakaiUserId + "," + contextId + ") sql = " + sql);
		try {
			return (List<CalendarItem>) getJdbcTemplate().query(sql,params,
				new CalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<CalendarItem>();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItemsByContext(java.lang.String)
	 */
	public List<CalendarItem> getCalendarItemsByContext(String contextId) {
		
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItemsByContext(" + contextId + ")");
		}
		String sql = null;
		Object[] params = null;
		if(contextId != null) {
			sql = getStatement("select.CalendarItems.by.contextId");
			params = new Object[]{contextId};
		} 
		log.info("getCalendarItemsByContext(" + contextId + ") sql = " + sql);
		// TODO: what do do if sql and/or params null ??
		try {
			return (List<CalendarItem>) getJdbcTemplate().query(sql,params,
				new CalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<CalendarItem>();
		}
	}

	public CalendarLink getCalendarLink(long calendarItemId, long personId) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarLink(" + calendarItemId + ", " + personId + ")");
		}
		
		try {
			return (CalendarLink) getJdbcTemplate().queryForObject(getStatement("select.CalendarLink.by.calendarItemId.personId"),
				new Object[]{calendarItemId, personId},
				new CalendarLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getContext(long)
	 */
	public Context getContext(long id) {
		if(log.isDebugEnabled()) {
			log.debug("getContext(" + id + ")");
		}
		
		try {
			return (Context) getJdbcTemplate().queryForObject(getStatement("select.Context.by.id"),
				new Object[]{Long.valueOf(id)},
				new ContextMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getContext(java.lang.String)
	 */
	public Context getContext(String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getContext(" + contextId + ")");
		}
		
		try {
			String sql = getStatement("select.Context.by.contextId");
			
			return (Context) getJdbcTemplate().queryForObject(sql ,
				new Object[]{contextId},
				new ContextMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}  catch (Exception e) {
			log.error("getContext: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItem(long)
	 */
	public NewsItem getNewsItem(long id) {
		if(log.isDebugEnabled()) {
			log.debug("getNewsItem(" + id + ")");
		}
		
		try {
			return (NewsItem) getJdbcTemplate().queryForObject(getStatement("select.NewsItem.by.id"),
				new Object[]{id},
				new NewsItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItem(java.lang.String)
	 */
	public NewsItem getNewsItem(String entityReference) {
		if(log.isDebugEnabled()) {
			log.debug("getNewsItem(" + entityReference + ")");
		}
		
		try {
			return (NewsItem) getJdbcTemplate().queryForObject(getStatement("select.NewsItem.by.entityReference"),
				new Object[]{entityReference},
				new NewsItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItems(java.lang.String, java.lang.String)
	 */
	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getNewsItems(" + sakaiUserId + "," + contextId + ")");
		}
		String sql = null;
		Object[] params = null;
		if(contextId == null) {
			sql = getStatement("select.NewsItems.by.sakaiId");
			params = new Object[]{sakaiUserId};
		} else {
			sql = getStatement("select.NewsItems.by.sakaiId.contextId");
			params = new Object[]{sakaiUserId, contextId};
		}
		log.info("getNewsItems(" + sakaiUserId + "," + contextId + ") sql = " + sql);
		try {
			return (List<NewsItem>) getJdbcTemplate().query(sql,params,
				new NewsItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsItem>();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItemsByContext(java.lang.String)
	 */
	public List<NewsItem> getNewsItemsByContext(String contextId) {
		
		if(log.isDebugEnabled()) {
			log.debug("getNewsItemsByContext(" + contextId + ")");
		}
		String sql = null;
		Object[] params = null;
		if(contextId != null) {
			sql = getStatement("select.NewsItems.by.contextId");
			params = new Object[]{contextId};
		} 
		log.info("getNewsItemsByContext(" + contextId + ") sql = " + sql);
		// TODO: what do do if sql and/or params null ??
		try {
			return (List<NewsItem>) getJdbcTemplate().query(sql,params,
				new NewsItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsItem>();
		}
	}

	public NewsLink getNewsLink(long newsItemId, long personId) {
		if(log.isDebugEnabled()) {
			log.debug("getNewsLink(" + newsItemId + ", " + personId + ")");
		}
		
		try {
			return (NewsLink) getJdbcTemplate().queryForObject(getStatement("select.NewsLink.by.newsItemId.personId"),
				new Object[]{newsItemId, personId},
				new NewsLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getPersonBySakaiId(java.lang.String)
	 */
	public Person getPersonBySakaiId(String sakaiId) {
		if(log.isDebugEnabled()) {
			log.debug("getPersonBySakaiId(" + sakaiId + ")");
		}
		
		try {
			return (Person) getJdbcTemplate().queryForObject(getStatement("select.Person.by.sakaiId"),
				new Object[]{sakaiId},
				new PersonMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getPersonBySakaiId: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getRealm(java.lang.String)
	 */
	public Realm getRealm(String realmId) {
		if(log.isDebugEnabled()) {
			log.debug("getRealm(" + realmId + ")");
		}
		
		try {
			return (Realm) getJdbcTemplate().queryForObject(getStatement("select.Realm.by.realmId"),
				new Object[]{realmId},
				new RealmMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getRealm: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getRealm(long)
	 */
	public Realm getRealm(long id) {
		if(log.isDebugEnabled()) {
			log.debug("getRealm(" + id + ")");
		}
		
		try {
			return (Realm) getJdbcTemplate().queryForObject(getStatement("select.Realm.by.id"),
				new Object[]{Long.valueOf(id)},
				new RealmMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getRealm: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	public Set<String> getSakaIdsForUserWithCalendarLinks(String entityReference) {
		if(log.isDebugEnabled()) {
			log.debug("getSakaIdsForUserWithCalendarLinks(" + entityReference + ")");
		}
		String sql = getStatement("select.sakaiUserIds.in.calendarLinks.by.entityReference");
		Object[] params = new Object[]{entityReference};
		try {
			List<String> userIds = getJdbcTemplate().query(sql,params, new RowMapper(){

				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					
					return rs.getString(1);
				}
				
			});
			return new HashSet<String>(userIds);
		} catch (DataAccessException ex) {
           log.error("getSakaIdsForUserWithCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new HashSet<String>();
		}
	}

	public Set<String> getSakaiIdsForUserWithNewsLinks(String entityReference) {
		if(log.isDebugEnabled()) {
			log.debug("getSakaIdsForUserWithNewsLinks(" + entityReference + ")");
		}
		String sql = getStatement("select.sakaiUserIds.in.newsLinks.by.entityReference");
		Object[] params = new Object[]{entityReference};
		try {
			List<String> userIds = getJdbcTemplate().query(sql,params, new RowMapper(){

				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					
					return rs.getString(1);
				}
				
			});
			return new HashSet<String>(userIds);
		} catch (DataAccessException ex) {
           log.error("getSakaIdsForUserWithNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new HashSet<String>();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItem(java.lang.Long, java.lang.String, java.util.Date)
	 */
	public boolean updateCalendarItem(Long id, String newTitle, Date newTime) {
		if(log.isDebugEnabled()) {
			log.debug("updateCalendarItem( " + id + "," + newTitle + "," + newTitle + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.CalendarItem.title.calendarTime"),
				new Object[]{newTitle, newTime, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemTime(java.lang.Long, java.util.Date)
	 */
	public boolean updateCalendarItemTime(Long id, Date newTime) {
		if(log.isDebugEnabled()) {
			log.debug("updateCalendarItemTime( " + id + "," + newTime + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.CalendarItem.calendarTime"),
				new Object[]{newTime, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateCalendarItemTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemTitle(java.lang.Long, java.lang.String)
	 */
	public boolean updateCalendarItemTitle(Long id, String newTitle) {
		if(log.isDebugEnabled()) {
			log.debug("updateCalendarItemTitle( " + id + "," + newTitle + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.CalendarItem.title"),
				new Object[]{newTitle, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateCalendarItemTitle: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}
	
	public boolean updateCalendarLink(CalendarLink calendarLink) {
		if(log.isDebugEnabled()) {
			log.debug("addCalendarLink( " + calendarLink.toString() + ")");
		}
		
		//  person_id, item_id, context_id, realm_id
		
		try {
			getJdbcTemplate().update(getStatement("update.CalendarLink"),
				new Object[]{calendarLink.getPerson().getId(), calendarLink.getCalendarItem().getId(), 
						calendarLink.getContext().getId(), calendarLink.isHidden(), calendarLink.isSticky(), calendarLink.getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemTitle(java.lang.Long, java.lang.String)
	 */
	public boolean updateNewsItemTitle(Long id, String newTitle) {
		if(log.isDebugEnabled()) {
			log.debug("updateNewsItemTitle( " + id + "," + newTitle + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.NewsItem.title"),
				new Object[]{newTitle, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateNewsItemTitle: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
		
	}

	public boolean updateNewsLink(NewsLink newsLink) {
		if(log.isDebugEnabled()) {
			log.debug("addNewsLink( " + newsLink.toString() + ")");
		}
		
		//  person_id, item_id, context_id, realm_id
		
		try {
			getJdbcTemplate().update(getStatement("update.NewsLink"),
				new Object[]{newsLink.getPerson().getId(), newsLink.getNewsItem().getId(), 
						newsLink.getContext().getId(), newsLink.isHidden(), newsLink.isSticky(), newsLink.getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/**
	 * Get an SQL statement for the appropriate vendor from the bundle
	
	 * @param key
	 * @return statement or null if none found. 
	 */
	protected String getStatement(String key) {
		try {
			return statements.getString(key);
		} catch (NoSuchElementException e) {
			log.error("Statement: '" + key + "' could not be found in: " + statements.getFileName());
			return null;
		}
	}
	
	/**
	 * init
	 */
	public void init() {
		log.info("init()");
		
		//setup the vendor
		String vendor = serverConfigurationService.getString("vendor@org.sakaiproject.db.api.SqlService", null);
		
		//initialise the statements
		initStatements(vendor);
		
		//setup tables if we have auto.ddl enabled.
		boolean autoddl = serverConfigurationService.getBoolean("auto.ddl", true);
		
		if(autoddl) {
			initTables();
		}
	}
	
	/**
	 * Loads our SQL statements from the appropriate properties file
	 * @param vendor	DB vendor string. Must be one of mysql, oracle, hsqldb
	 */
	protected void initStatements(String vendor) {
		
		URL url = getClass().getClassLoader().getResource(vendor + ".properties"); 
		
		try {
			statements = new PropertiesConfiguration(); //must use blank constructor so it doesn't parse just yet (as it will split)
			statements.setReloadingStrategy(new InvariantReloadingStrategy());	//don't watch for reloads
			statements.setThrowExceptionOnMissing(true);	//throw exception if no prop
			statements.setDelimiterParsingDisabled(true); //don't split properties
			statements.load(url); //now load our file
		} catch (ConfigurationException e) {
			log.error(e.getClass() + ": " + e.getMessage());
			return;
		}
	}

	/**
	 * Sets up our tables
	 */
	protected void initTables() {
		executeSqlStatement("create.Context.table");
		executeSqlStatement("create.Person.table");
		executeSqlStatement("create.SourceType.table");
		executeSqlStatement("create.NewsItem.table");
		executeSqlStatement("create.NewsLink.table");
		executeSqlStatement("create.CalendarItem.table");
		executeSqlStatement("create.CalendarLink.table");
		executeSqlStatement("create.AvailabilityCheck.table");
		executeSqlStatement("create.PersonContext.table");
		executeSqlStatement("create.PersonContextSourceType.table");
		executeSqlStatement("create.PersonSourceType.table");
		
	}

	/**
	 * @param sqlStatement
	 */
	protected void executeSqlStatement(String key) {
		String sqlStatement = getStatement(key);
		if(sqlStatement == null || sqlStatement.trim().equals("")) {
			log.warn("Missing key in database properties file (" + statements.getFileName() + "): " + key);
		} else {
			String parts[] = sqlStatement.split(";");
			if( parts != null) {
				for(String sql : parts) {
					if(sql != null && ! sql.trim().equals("")) {
						try {
							getJdbcTemplate().execute(sql.trim());
							
						} catch (DataAccessException ex) {
							log.warn("Error executing SQL statement with key: " + key + " -- " + ex.getClass() + ": " + ex.getMessage());
						}
					}
				}
			}
		}
	}

}
