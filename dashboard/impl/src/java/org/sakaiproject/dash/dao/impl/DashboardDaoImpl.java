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
import java.util.Calendar;
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
import org.sakaiproject.dash.model.RepeatingCalendarItem;
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

	protected String databaseVendor;
	
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
			Object[] params = null;
			String sql = null;
			if(calendarItem.getRepeatingCalendarItem() == null) {
				sql = getStatement("insert.CalendarItem");
				params = new Object[]{calendarItem.getCalendarTime(), calendarItem.getCalendarTimeLabelKey(), calendarItem.getTitle(), 
						calendarItem.getEntityReference(), calendarItem.getSubtype(),
						calendarItem.getSourceType().getId(), calendarItem.getContext().getId()};
			} else {
				sql = getStatement("insert.CalendarItem.repeats");
				params = new Object[]{calendarItem.getCalendarTime(), calendarItem.getCalendarTimeLabelKey(), calendarItem.getTitle(), 
						calendarItem.getEntityReference(), calendarItem.getSubtype(),
						calendarItem.getSourceType().getId(), calendarItem.getContext().getId(), 
						calendarItem.getRepeatingCalendarItem().getId(), calendarItem.getSequenceNumber()};
			}

			template.update(sql,params);
			return true;
		} catch (DataAccessException ex) {
            log.error("addCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
	        //System.out.println("addCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
            return false;
		} catch (Exception e) {
	        log.error("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        //System.out.println("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
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
				new Object[]{newsItem.getNewsTime(), newsItem.getTitle(), newsItem.getNewsTimeLabelKey(), newsItem.getEntityReference(),
						newsItem.getSubtype(), newsItem.getSourceType().getId(), newsItem.getContext().getId(), newsItem.getGroupingIdentifier()}
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
	
	public boolean addRepeatingCalendarItem(RepeatingCalendarItem repeatingCalendarItem) {
		if(log.isDebugEnabled()) {
			log.debug("addRepeatingCalendarItem( " + repeatingCalendarItem.toString() + ")");
		}
		//System.out.println("addRepeatingCalendarItem( " + repeatingCalendarItem.toString() + ")");
		
		//  first_time, last_time, frequency, count, calendar_time_label_key, title, entity_ref, entity_type, context_id
		String sql = getStatement("insert.RepeatingEvent");
		//System.out.println("addRepeatingCalendarItem() sql == " + sql);
		long sourceTypeId = repeatingCalendarItem.getSourceType().getId();
		long contextId = repeatingCalendarItem.getContext().getId();
		Object[] params = new Object[]{
				repeatingCalendarItem.getFirstTime(), repeatingCalendarItem.getLastTime(), 
				repeatingCalendarItem.getFrequency(), repeatingCalendarItem.getMaxCount(), 
				repeatingCalendarItem.getCalendarTimeLabelKey(), repeatingCalendarItem.getTitle(),
				repeatingCalendarItem.getEntityReference(), repeatingCalendarItem.getSubtype(), sourceTypeId, contextId
			};
		
		try {
			getJdbcTemplate().update(sql, params);
			return true;
		} catch (DataAccessException ex) {
			log.error("addRepeatingCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			//System.out.println("addRepeatingCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
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
		
		// identifier, accessPermission
		
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
	
	public RepeatingCalendarItem getRepeatingCalendarItem(String entityReference, String calendarTimeLabelKey) {
		if(log.isDebugEnabled()) {
			log.debug("getRepeatingCalendarItem(" + entityReference + "," + calendarTimeLabelKey + ")");
		}
		
		try {
			return (RepeatingCalendarItem) getJdbcTemplate().queryForObject(getStatement("select.RepeatingCalendarItem.by.entityReference.calendarTimeLabelKey"),
				new Object[]{entityReference, calendarTimeLabelKey},
				new RepeatingCalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getRepeatingCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}


	
	public List<RepeatingCalendarItem> getRepeatingCalendarItems() {
		if(log.isDebugEnabled()) {
			log.debug("getRepeatingCalendarItems()");
		}
		String sql = getStatement("select.RepeatingEvents");
		try {
			return (List<RepeatingCalendarItem>) getJdbcTemplate().query(sql,
				new RepeatingCalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getRepeatingCalendarItems: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<RepeatingCalendarItem>();
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

	public CalendarItem getCalendarItem(String entityReference, String calendarTimeLabelKey, Integer sequenceNumber) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItem(" + entityReference + "," + calendarTimeLabelKey + "," + sequenceNumber + ")");
		}
		
		String sql = null;
		Object[] params = null;	
		if(sequenceNumber == null) {
			sql = getStatement("select.CalendarItem.by.entityReference.calendarTimeLabelKey");
			params = new Object[]{entityReference, calendarTimeLabelKey};	
		} else if(calendarTimeLabelKey == null) {
			sql = getStatement("select.CalendarItem.by.entityReference.sequenceNumber");
			params = new Object[]{entityReference, sequenceNumber};
		} else {
			sql = getStatement("select.CalendarItem.by.entityReference.calendarTimeLabelKey.sequenceNumber");
			params = new Object[]{entityReference, calendarTimeLabelKey, sequenceNumber};
		}
		try {
			return (CalendarItem) getJdbcTemplate().queryForObject(sql,
				params,
				new CalendarItemMapper()
			);
		} catch (DataAccessException e) {
           log.error("getCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        //System.out.println("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
           return null;
		} catch (Exception e) {
	        log.error("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        //System.out.println("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
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
	public List<CalendarItem> getCalendarItems(String sakaiUserId, String contextId, boolean saved, boolean hidden) {
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
		//log.info("getCalendarItems(" + sakaiUserId + "," + contextId + ") sql = " + sql);
		try {
			return (List<CalendarItem>) getJdbcTemplate().query(sql,params,
				new CalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<CalendarItem>();
		}
	}
	
	public List<CalendarItem> getFutureCalendarItems(String sakaiUserId, String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getFutureCalendarItems(" + sakaiUserId + "," + contextId + ")");
		}
		String sql = null;
		Object[] params = null;
		if(contextId == null) {
			sql = getStatement("select.CalendarItems.by.sakaiId.future");
			params = new Object[]{sakaiUserId};
		} else {
			sql = getStatement("select.CalendarItems.by.sakaiId.contextId.future");
			params = new Object[]{sakaiUserId, contextId};
		}
		//log.info("getCalendarItems(" + sakaiUserId + "," + contextId + ") sql = " + sql);
		try {
			return (List<CalendarItem>) getJdbcTemplate().query(sql,params,
				new CalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<CalendarItem>();
		}
	}

	public List<CalendarItem> getPastCalendarItems(String sakaiUserId, String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItems(" + sakaiUserId + "," + contextId + ")");
		}
		String sql = null;
		Object[] params = null;
		if(contextId == null) {
			sql = getStatement("select.CalendarItems.by.sakaiId.past");
			params = new Object[]{sakaiUserId};
		} else {
			sql = getStatement("select.CalendarItems.by.sakaiId.contextId.past");
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

	public CalendarLink getCalendarLink(long id) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarLink(" + id + ")");
		}
		
		try {
			return (CalendarLink) getJdbcTemplate().queryForObject(getStatement("select.CalendarLink.by.id"),
				new Object[]{id},
				new CalendarLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		if(log.isDebugEnabled()) {
			log.debug("getFutureCalendarLinks(" + sakaiUserId + "," + contextId + "," + hidden + ")");
		}
		
		if(sakaiUserId == null) {
			logger.warn("getFutureCalendarLinks() called with null sakaiUserId");
			return null;
		}
		String sql = null;
		Object[] params = null;
		
		if( contextId == null) {
			sql = getStatement("select.future.CalendarLinks.by.sakaiId.hidden");
			params = new Object[]{sakaiUserId, Boolean.valueOf(hidden), getPreviousMidnight()};
		} else {
			sql = getStatement("select.future.CalendarLinks.by.sakaiId.contextId.hidden");
			params = new Object[]{sakaiUserId, contextId, Boolean.valueOf(hidden), getPreviousMidnight()};
		} 
		
		try {
			return (List<CalendarLink>) getJdbcTemplate().query(sql,
				params,
				new CalendarLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getFutureCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		if(log.isDebugEnabled()) {
			log.debug("getPastCalendarLinks(" + sakaiUserId + "," + contextId + "," + hidden + ")");
		}
		
		if(sakaiUserId == null) {
			logger.warn("getPastCalendarLinks() called with null sakaiUserId");
			return null;
		}
		String sql = null;
		Object[] params = null;
		
		if( contextId == null) {
			sql = getStatement("select.past.CalendarLinks.by.sakaiId.hidden");
			params = new Object[]{sakaiUserId, Boolean.valueOf(hidden), new Date()};
		} else {
			sql = getStatement("select.past.CalendarLinks.by.sakaiId.contextId.hidden");
			params = new Object[]{sakaiUserId, contextId, Boolean.valueOf(hidden), new Date()};
		} 
		
		try {
			return (List<CalendarLink>) getJdbcTemplate().query(sql,
				params,
				new CalendarLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getPastCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId, String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getStarredCalendarLinks(" + sakaiUserId + "," + contextId + ")");
		}
		
		if(sakaiUserId == null) {
			logger.warn("getStarredCalendarLinks() called with null sakaiUserId");
			return null;
		}
		String sql = null;
		Object[] params = null;
		
		if( contextId == null) {
			sql = getStatement("select.starred.CalendarLinks.by.sakaiId");
			params = new Object[]{sakaiUserId};
		} else {
			sql = getStatement("select.starred.CalendarLinks.by.sakaiId.contextId");
			params = new Object[]{sakaiUserId, contextId};
		} 
		
		try {
			return (List<CalendarLink>) getJdbcTemplate().query(sql,
				params,
				new CalendarLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getStarredCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/**
	 * @return
	 */
	protected Date getPreviousMidnight() {
		Date thisAM = null;
		Calendar midnight = Calendar.getInstance();
		midnight.set(Calendar.MILLISECOND, 0);
		midnight.set(Calendar.SECOND, 0);
		midnight.set(Calendar.MINUTE, 0);
		midnight.set(Calendar.HOUR, 0);
		thisAM = midnight.getTime();
		return thisAM;
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
	
	public List<NewsItem> getMOTD(String motdContextId) {
		if(log.isDebugEnabled()) {
			log.debug("getMOTD()");
		}
		
		String sql = getStatement("select.motd.recent");
		Object[] params = new Object[]{
				motdContextId
		};
		
		try {
			return (List<NewsItem>) getJdbcTemplate().query(sql,params,
				new NewsItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getMOTD: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsItem>();
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
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItems(java.lang.String, java.lang.String, int)
	 */
	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId, int collapseCount) {
		if(log.isDebugEnabled()) {
			log.debug("getNewsItems(" + sakaiUserId + "," + contextId + "," + collapseCount + ")");
		}
		System.out.println("getNewsItems(" + sakaiUserId + "," + contextId + "," + collapseCount + ")");
		String sql = null; 
		Object[] params = null;
		if(contextId == null) {
			sql = getStatement("select.NewsItems.sakaiId.collapseCount");
			//params = new Object[]{sakaiUserId, collapseCount, sakaiUserId, collapseCount};
			params = new Object[]{sakaiUserId, collapseCount, sakaiUserId, sakaiUserId, collapseCount};
		} else {
			sql = getStatement("select.NewsItems.sakaiId.contextId.collapseCount");
			params = new Object[]{sakaiUserId, contextId, collapseCount, sakaiUserId, sakaiUserId, contextId, collapseCount};
		}
		//System.out.println("getNewsItems() " + sql + " " + params);
		
		try {
			return (List<NewsItem>) getJdbcTemplate().query(sql,params,
				new NewsItemMapper()
			);
		} catch (DataAccessException ex) {
			System.out.println("\n\ngetNewsItems() " + ex + "\n\n");
		
        	log.error("getNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
        	return new ArrayList<NewsItem>();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsItems(java.lang.String, java.lang.String)
	 */
	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId, boolean saved, boolean hidden) {
		if(log.isDebugEnabled()) {
			log.debug("getNewsItems(" + sakaiUserId + "," + contextId + ")");
		}
		String sql = null;
		Object[] params = null;
		if(contextId == null) {
			if(saved) {
				sql = getStatement("select.NewsItems.by.sakaiId.sticky");
			} else if(hidden) {
				sql = getStatement("select.NewsItems.by.sakaiId.hidden");
			} else {
				sql = getStatement("select.NewsItems.by.sakaiId");
			}
			params = new Object[]{sakaiUserId};
		} else {
			if(saved) {
				sql = getStatement("select.NewsItems.by.sakaiId.contextId.sticky");
			} else if(hidden) {
				sql = getStatement("select.NewsItems.by.sakaiId.contextId.hidden");
			} else {
				sql = getStatement("select.NewsItems.by.sakaiId.contextId");
			}
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
	
	public NewsLink getNewsLink(long id) {
		if(log.isDebugEnabled()) {
			log.debug("getNewsLink(" + id + ")");
		}
		
		try {
			return (NewsLink) getJdbcTemplate().queryForObject(getStatement("select.NewsLink.by.id"),
				new Object[]{id},
				new NewsLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	public List<NewsLink> getCurrentNewsLinks(String sakaiUserId, String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getCurrentNewsLinks(" + sakaiUserId + "," + contextId + ")");
		}
		
		String sql = null;
		Object[] params = null;
		if(contextId == null) {
			sql = getStatement("select.current.NewsLinks.by.sakaiUserId");
			params = new Object[]{sakaiUserId, 2, sakaiUserId, 2, sakaiUserId};
		} else {
			sql = getStatement("select.current.NewsLinks.by.sakaiUserId.contextId");
			// sakai-id context-id gr-count sakai-id gr-count sakai-id context-id 
			params = new Object[]{sakaiUserId, contextId, 2, sakaiUserId, 2, sakaiUserId, contextId};
			
		}
		
		try {
			return (List<NewsLink>) getJdbcTemplate().query(sql,params,
				new NewsLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCurrentNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsLink>();
		}
	}

	public List<NewsLink> getStarredNewsLinks(String sakaiUserId, String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getStarredNewsLinks(" + sakaiUserId + "," + contextId + ")");
		}
		
		String sql = null;
		Object[] params = null;
		if(contextId == null) {
			sql = getStatement("select.starred.NewsLinks.by.sakaiUserId");
			params = new Object[]{sakaiUserId};
		} else {
			sql = getStatement("select.starred.NewsLinks.by.sakaiUserId.contextId");
			params = new Object[]{sakaiUserId, contextId};
			
		}
		
		try {
			return (List<NewsLink>) getJdbcTemplate().query(sql,params,
				new NewsLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getStarredNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsLink>();
		}
	}

	public List<NewsLink> getHiddenNewsLinks(String sakaiUserId, String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getHiddenNewsLinks(" + sakaiUserId + "," + contextId + ")");
		}
		
		String sql = null;
		Object[] params = null;
		if(contextId == null) {
			sql = getStatement("select.hidden.NewsLinks.by.sakaiUserId");
			params = new Object[]{sakaiUserId};
		} else {
			sql = getStatement("select.hidden.NewsLinks.by.sakaiUserId.contextId");
			params = new Object[]{sakaiUserId, contextId};
			
		}
		
		try {
			return (List<NewsLink>) getJdbcTemplate().query(sql,params,
				new NewsLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getHiddenNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsLink>();
		}
	}

	public int countNewsLinksByGroupId(String sakaiUserId, String groupId) {
		if(log.isDebugEnabled()) {
			log.debug("getNewsItemsByGroupId(" + sakaiUserId + "," + groupId + ")");
		}
		if(sakaiUserId == null || groupId == null) {
			return 0;
		}
		String sql = getStatement("count.NewsLinks.by.sakaiId.groupId");
		Object[] params = new Object[]{sakaiUserId, groupId};
		try {
			return getJdbcTemplate().queryForInt(sql,params);
		} catch (DataAccessException ex) {
           log.error("getNewsItemsByGroupId: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return 0;
		}
	}

	
	public List<NewsLink> getNewsLinksByGroupId(String sakaiUserId,
			String groupId, int limit, int offset) {
		List<NewsItem> items = null;
		if(log.isDebugEnabled()) {
			log.debug("getNewsItemsByGroupId(" + sakaiUserId + "," + groupId + "," + limit + "," + offset + ")");
		}
		if(sakaiUserId == null || groupId == null) {
			return new ArrayList<NewsLink>();
		}
		String sql = getStatement("select.NewsLinks.by.sakaiId.groupId.paged");
		
		Object[] params = null;
		if("oracle".equalsIgnoreCase(this.databaseVendor)) {
			params = new Object[]{sakaiUserId, groupId, offset + limit, offset};
		} else {
			params = new Object[]{sakaiUserId, groupId, limit, offset};
		}
		try {
			return (List<NewsLink>) getJdbcTemplate().query(sql,params,
				new NewsLinkMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getNewsItemsByGroupId: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsLink>();
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
	
	public boolean addEvent(Date eventDate, String event, String itemRef,
			String contextId, String sessionId, String eventCode) {
		if(log.isDebugEnabled()) {
			log.debug("saveEvent( " + eventDate + "," + event + "," + itemRef + "," + contextId + "," + sessionId + "," + eventCode + ")");
		}
		
		//  insert.EventLog = insert into dash_event (event_date, event, ref, context, session_id, event_code) values (?, ?, ?, ?, ?, ?)
		String sql = getStatement("insert.EventLog");
		Object[] params = new Object[]{
				eventDate, event, itemRef, contextId, sessionId, eventCode
			};
		
		try {
			getJdbcTemplate().update(sql, params);
			return true;
		} catch (DataAccessException ex) {
			log.error("saveEvent: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			//System.out.println("addRepeatingCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
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
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemSubtype(java.lang.Long, java.lang.String)
	 */
	public boolean updateCalendarItemSubtype(Long id, String newSubtype) {
		if(log.isDebugEnabled()) {
			log.debug("updateCalendarItemSubtype( " + id + "," + newSubtype + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.CalendarItem.subtype"),
				new Object[]{newSubtype, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateCalendarItemSubtype: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}
	
	public boolean updateCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey) {
		if(log.isDebugEnabled()) {
			log.debug("updateCalendarItemTime( " + entityReference + "," + oldLabelKey + "," + newLabelKey + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.CalendarItemsLabelKey.entityReference.oldLabelKey"),
				new Object[]{newLabelKey, entityReference, oldLabelKey}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateCalendarsItemLabelKey: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
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
			log.debug("updateCalendarLink( " + calendarLink.toString() + ")");
		}
		
		//  update dash_calendar_link set person_id=?, item_id=?, context_id=?, hidden=?, sticky=? where id=? 
		
		try {
			getJdbcTemplate().update(getStatement("update.CalendarLink"),
				new Object[]{calendarLink.getPerson().getId(), calendarLink.getCalendarItem().getId(), 
						calendarLink.getContext().getId(), calendarLink.isHidden(), calendarLink.isSticky(), calendarLink.getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
	
	public boolean updateContextTitle(String contextId, String newContextTitle) {
		if(log.isDebugEnabled()) {
			log.debug("updateContextTitle( " + contextId + "," + newContextTitle + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.Context.title"),
				new Object[]{newContextTitle, contextId}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateContextTitle: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemSubtype(java.lang.Long, java.lang.String)
	 */
	public boolean updateNewsItemSubtype(Long id, String newSubtype, Date newNewsTime, String newLabelKey, String newGroupingIdentifier) {
		if(log.isDebugEnabled()) {
			log.debug("updateNewsItemSubtype( " + id + "," + newSubtype + ")");
		}
		
		if(id == null || newSubtype == null || newNewsTime == null || newLabelKey == null) {
			log.error("updateNewsItemSubtype() called with null values");
			return false;
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.NewsItem.subtype"),
				new Object[]{newSubtype, newNewsTime, newLabelKey, newGroupingIdentifier, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateNewsItemSubtype: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemTime(java.lang.Long, java.util.Date)
	 */
	public boolean updateNewsItemTime(Long id, Date newTime, String newGroupingIdentifier) {
		if(log.isDebugEnabled()) {
			log.debug("updateNewsItemTime( " + id + "," + newTime + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.NewsItem.newsTime"),
				new Object[]{newTime, newGroupingIdentifier, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateNewsItemTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemTitle(java.lang.Long, java.lang.String)
	 */
	public boolean updateNewsItemTitle(Long id, String newTitle, Date newNewsTime, String newLabelKey, String newGroupingIdentifier) {
		if(log.isDebugEnabled()) {
			log.debug("updateNewsItemTitle( " + id + "," + newTitle + ")");
		}
		
		if(id == null || newTitle == null || newNewsTime == null || newLabelKey == null) {
			log.error("updateNewsItemTitle() called with null values");
			return false;
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.NewsItem.title"),
				new Object[]{newTitle, newNewsTime, newLabelKey, newGroupingIdentifier, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateNewsItemTitle: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemLabelKey(java.lang.Long, java.lang.String)
	 */
	public boolean updateNewsItemLabelKey(Long id, String labelKey, String newGroupingIdentifier) {
		if(log.isDebugEnabled()) {
			log.debug("updateNewsItemLabelKey( " + id + "," + labelKey + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.NewsItem.newsTimeLabelKey"),
				new Object[]{labelKey, newGroupingIdentifier, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateNewsItemLabelKey: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
		
	}

	public boolean updateNewsLink(NewsLink newsLink) {
		if(log.isDebugEnabled()) {
			log.debug("updateNewsLink( " + newsLink.toString() + ")");
		}
		
		//  update dash_news_link set person_id=?, item_id=?, context_id=?, hidden=?, sticky=?  where id=?
		
		try {
			getJdbcTemplate().update(getStatement("update.NewsLink"),
				new Object[]{newsLink.getPerson().getId(), newsLink.getNewsItem().getId(), 
						newsLink.getContext().getId(), newsLink.isHidden(), newsLink.isSticky(), newsLink.getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemsLabelKey(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean updateRepeatingCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey) {
		if(log.isDebugEnabled()) {
			log.debug("updateRepeatingCalendarItemsLabelKey( " + entityReference + "," + oldLabelKey + "," + newLabelKey + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.RepeatingEventsLabelKey.entityReference.oldLabelKey"),
				new Object[]{newLabelKey, entityReference, oldLabelKey}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateRepeatingCalendarItemsLabelKey: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemsSubtype(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean updateRepeatingCalendarItemsSubtype(String entityReference, String labelKey, String newSubtype) {
		if(log.isDebugEnabled()) {
			log.debug("updateRepeatingCalendarItemsSubtype( " + entityReference + "," + labelKey + "," + newSubtype + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.RepeatingEventsSubtype.entityReference.labelKey"),
				new Object[]{newSubtype, entityReference, labelKey}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateRepeatingCalendarItemsSubtype: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}

	public boolean updateRepeatingCalendarItemTime(String entityReference,
			Date newFirstTime, Date newLastTime) {
		if(log.isDebugEnabled()) {
			log.debug("updateRepeatingCalendarItemTime( " + entityReference + "," + newFirstTime + "," + newLastTime + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.RepeatingEventsTime.entityReference"),
				new Object[]{newFirstTime, newLastTime, entityReference}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateRepeatingCalendarItemTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
	
	public boolean updateRepeatingCalendarItemTitle(String entityReference, String newTitle) {
		if(log.isDebugEnabled()) {
			log.debug("updateRepeatingCalendarItemTitle( " + entityReference + "," + newTitle + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.RepeatingEventsTitle.entityReference"),
				new Object[]{newTitle, entityReference}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("updateRepeatingCalendarItemTitle: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
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
		this.databaseVendor = serverConfigurationService.getString("vendor@org.sakaiproject.db.api.SqlService", null);
		
		//initialise the statements
		initStatements(databaseVendor);
		
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
		try {
			executeSqlStatement("create.Context.table");
			executeSqlStatement("create.Person.table");
			executeSqlStatement("create.SourceType.table");
			executeSqlStatement("create.NewsItem.table");
			executeSqlStatement("create.NewsLink.table");
			executeSqlStatement("create.CalendarItem.table");
			executeSqlStatement("create.CalendarLink.table");
			executeSqlStatement("create.AvailabilityCheck.table");
			executeSqlStatement("create.RepeatingEvent.table");
			executeSqlStatement("create.Config.table");
			executeSqlStatement("create.EventLog.table");

			// TODO: eliminate all references to these three tables in the sql files and the dao, including these three lines
			executeSqlStatement("create.PersonContext.table");
			executeSqlStatement("create.PersonContextSourceType.table");
			executeSqlStatement("create.PersonSourceType.table");

		} catch(Exception e) {
	        //System.out.println("\ninitTables: Error executing query: " + e.getClass() + ":\n" + e.getMessage() + "\n");
			logger.warn("initTables() " + e);
		}
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
					        //System.out.println("\nError executing SQL statement with key: " + key + " -- " + ex.getClass() + ": \n" + ex.getMessage() + "\n");
						}
					}
				}
			}
		}
	}

	public Integer getConfigProperty(String propertyName) {
		
		if(log.isDebugEnabled()) {
			log.debug("getConfigProperty( " + propertyName + ")");
		}
		
		Integer value = null;
		String sql = getStatement("select.Config.by.propertyName");
		Object[] params = new Object[]{
				propertyName	
		};
		
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		try {
			value = jdbcTemplate.queryForInt(sql, params);
		} catch (DataAccessException ex) {
            log.error("getConfigProperty: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		} catch (Exception ex) {
	        log.error("getConfigProperty: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		}
		
		return value ;
	}

	public void setConfigProperty(String propertyName, Integer propertyValue) {
		
		if(log.isDebugEnabled()) {
			log.debug("setConfigProperty( " + propertyName + "," + propertyValue + ")");
		}
		
		// insert into dash_config (property_name, property_value) values (?, ?)
		String sql_insert = getStatement("insert.Config");
		Object[] params_insert = new Object[]{
			propertyName, propertyValue	
		};
		
		// update dash_config set property_value=? where property_name=?
		String sql_update = getStatement("update.Config.propertyName");
		Object[] params_update = new Object[]{
				propertyValue, propertyName
		};
		
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		try {
			jdbcTemplate.update(sql_insert, params_insert);
		} catch (Exception e) {
			// insert failed -- try update instead of insert
	        try {
	        	jdbcTemplate.update(sql_update, params_update);
	        } catch (DataAccessException ex) {
	            log.error("setConfigProperty: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			} catch (Exception ex) {
		        log.error("setConfigProperty: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			}
		}
		
	}

}
