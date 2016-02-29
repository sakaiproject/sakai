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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.apache.log4j.Logger;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.dao.mapper.AvailabilityCheckMapper;
import org.sakaiproject.dash.dao.mapper.CalendarItemMapper;
import org.sakaiproject.dash.dao.mapper.CalendarLinkMapper;
import org.sakaiproject.dash.dao.mapper.ContextMapper;
import org.sakaiproject.dash.dao.mapper.NewsItemMapper;
import org.sakaiproject.dash.dao.mapper.NewsLinkMapper;
import org.sakaiproject.dash.dao.mapper.PersonMapper;
import org.sakaiproject.dash.dao.mapper.RepeatingCalendarItemMapper;
import org.sakaiproject.dash.dao.mapper.SourceTypeMapper;
import org.sakaiproject.dash.dao.mapper.TaskLockMapper;
import org.sakaiproject.dash.dao.mapper.ContextUserMapper;
import org.sakaiproject.dash.logic.TaskLock;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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

	private static final int MAX_LENGTH_SUBTYPE_FIELD = 255;

	private static final int ALWAYS_ACCESS_PERMISSION_SIZE = 1024;
	
	protected ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	protected PropertiesConfiguration statements;

	protected String databaseVendor;
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addAvailabilityCheck(org.sakaiproject.dash.model.AvailabilityCheck)
	 */
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
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addAvailabilityCheck() " + e);
			return false;
		} catch (DataAccessException ex) {
            log.warn("addAvailabilityCheck: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
            return false;
		} catch (Exception e) {
	        log.warn("addAvailabilityCheck: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        return false;
		}
	}
	
	@Override
	public boolean updateAvailabilityCheck(AvailabilityCheck availabilityCheck) {
		if(log.isDebugEnabled()) {
			log.debug("updateAvailabilityCheck( " + availabilityCheck.toString() + ")");
		}
		try {
			getJdbcTemplate().update(getStatement("update.AvailabilityCheck"),
					new Object[]{availabilityCheck.getScheduledTime(),availabilityCheck.getEntityReference()}
					);
			return true;
		} catch (DataAccessException ex) {
			log.warn("updateAvailabilityCheck: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}	
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addCalendarItem(org.sakaiproject.dash.model.CalendarItem)
	 */
	public boolean addCalendarItem(CalendarItem calendarItem) {
		if(log.isDebugEnabled()) {
			log.debug("addCalendarItem( " + calendarItem.toString() + ")");
		}
		
		// calendar_time, title , entity_url, entity_ref, source_type, context_id, realm_id

		String subtype = calendarItem.getSubtype();
		// DASH-191
		if(subtype != null && subtype.length() > MAX_LENGTH_SUBTYPE_FIELD) {
			StringBuilder buf = new StringBuilder();
			buf.append("addCalendarItem().  Truncating subtype ");
			buf.append(subtype);
			buf.append(" for entity ");
			buf.append(calendarItem.getEntityReference());
			log.warn(buf);
			subtype = subtype.substring(0, MAX_LENGTH_SUBTYPE_FIELD - 1);
		}
		try {
			JdbcTemplate template = getJdbcTemplate();
			Object[] params = null;
			String sql = null;
			if(calendarItem.getRepeatingCalendarItem() == null) {
				sql = getStatement("insert.CalendarItem");
				params = new Object[]{calendarItem.getCalendarTime(), calendarItem.getCalendarTimeLabelKey(), calendarItem.getTitle(), 
						calendarItem.getEntityReference(), subtype,
						calendarItem.getSourceType().getId(), calendarItem.getContext().getId()};
			} else {
				sql = getStatement("insert.CalendarItem.repeats");
				params = new Object[]{calendarItem.getCalendarTime(), calendarItem.getCalendarTimeLabelKey(), calendarItem.getTitle(), 
						calendarItem.getEntityReference(), subtype,
						calendarItem.getSourceType().getId(), calendarItem.getContext().getId(), 
						calendarItem.getRepeatingCalendarItem().getId(), calendarItem.getSequenceNumber()};
			}
			int result = template.update(sql,params);
			
			return result > 0;
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.warn("addCalendarItem() " + e);
		} catch (DataAccessException ex) {
            log.warn("addCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
	        // System.out.println("addCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		} 
		return false;
	}

	/* (non-Javadoc)
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
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addCalendarLink() " + e);
			return false;
		} catch (DataAccessException ex) {
           log.warn("addCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addCalendarLink(org.sakaiproject.dash.model.CalendarLink)
	 */
	public int addCalendarLinks(final List<CalendarLink> calendarLinks) {
		if(log.isDebugEnabled()) {
			log.debug("addCalendarLinks( " + calendarLinks.size() + ")");
		}
		
		//  person_id, item_id, context_id, realm_id
		int count = 0;
		try {
			String sql = getStatement("insert.CalendarLink");
			int[] updates = getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter(){

				@Override
				public void setValues(PreparedStatement ps, int i)
						throws SQLException {
					CalendarLink calendarLink = calendarLinks.get(i);
					ps.setLong(1, calendarLink.getPerson().getId());
					ps.setLong(2, calendarLink.getCalendarItem().getId());
					ps.setLong(3, calendarLink.getContext().getId());
					ps.setBoolean(4, calendarLink.isHidden());
					ps.setBoolean(5, calendarLink.isSticky());
				}

				@Override
				public int getBatchSize() {
					return calendarLinks.size();
				}
				
			});
			if(updates != null && updates.length > 0) {
				for(int u : updates) {
					count += u;
				}
			}
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addCalendarLinks() " + e);
		} catch (DataAccessException ex) {
           log.warn("addCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		}
		return count;
	}

	/* (non-Javadoc)
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
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addContext() " + e);
			return false;
		} catch (DataAccessException ex) {
           log.warn("addContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		} catch (Exception e) {
	        log.warn("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addNewsItem(org.sakaiproject.dash.model.NewsItem)
	 */
	public boolean addNewsItem(NewsItem newsItem) {
		if(log.isDebugEnabled()) {
			log.debug("addNewsItem( " + newsItem.toString() + ")");
		}
		
		// news_time, title , entity_url, entity_ref, source_type, context_id, realm_id
		String subtype = newsItem.getSubtype();
		// DASH-191
		if(subtype != null && subtype.length() > MAX_LENGTH_SUBTYPE_FIELD) {
			StringBuilder buf = new StringBuilder();
			buf.append("addNewsItem().  Truncating subtype ");
			buf.append(subtype);
			buf.append(" for entity ");
			buf.append(newsItem.getEntityReference());
			log.warn(buf);
			subtype = subtype.substring(0, MAX_LENGTH_SUBTYPE_FIELD - 1);
		}
		
		try {
			JdbcTemplate template = getJdbcTemplate();
			template.update(getStatement("insert.NewsItem"),
				new Object[]{newsItem.getNewsTime(), newsItem.getTitle(), newsItem.getNewsTimeLabelKey(), newsItem.getEntityReference(),
						subtype, newsItem.getSourceType().getId(), newsItem.getContext().getId(), newsItem.getGroupingIdentifier()}
			);

			return true;
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addNewsItem() " + e);
		} catch (DataAccessException ex) {
           log.warn("addNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		}
        return false;
	}
	
	/* (non-Javadoc)
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
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addNewsLink() " + e);
			return false;
		} catch (DataAccessException ex) {
           log.warn("addNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addNewsLinks(java.util.List)
	 */
	public int addNewsLinks(final List<NewsLink> newsLinks) {
		if(log.isDebugEnabled()) {
			log.debug("addNewsLinks( " + newsLinks.size() + ")");
		}
		
		//  person_id, item_id, context_id, realm_id
		int count = 0;
		try {
			String sql = getStatement("insert.NewsLink");
			int[] updates = getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter(){

				@Override
				public void setValues(PreparedStatement ps, int i)
						throws SQLException {
					NewsLink newsLink = newsLinks.get(i);
					ps.setLong(1, newsLink.getPerson().getId());
					ps.setLong(2, newsLink.getNewsItem().getId());
					ps.setLong(3, newsLink.getContext().getId());
					ps.setBoolean(4, newsLink.isHidden());
					ps.setBoolean(5, newsLink.isSticky());
				}

				@Override
				public int getBatchSize() {
					return newsLinks.size();
				}
				
			});
			if(updates != null && updates.length > 0) {
				for(int u : updates) {
					count += u;
				}
			}
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addNewsLink() " + e);
		} catch (DataAccessException ex) {
           log.warn("addNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		}
		return count;
	}

	/* (non-Javadoc)
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
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addPerson() " + e);
			return false;
		} catch (DataAccessException ex) {
           log.warn("addPerson: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addRepeatingCalendarItem(org.sakaiproject.dash.model.RepeatingCalendarItem)
	 */
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
		String subtype = repeatingCalendarItem.getSubtype();
		// DASH-191
		if(subtype != null && subtype.length() > MAX_LENGTH_SUBTYPE_FIELD) {
			StringBuilder buf = new StringBuilder();
			buf.append("addRepeatingCalendarItem().  Truncating subtype ");
			buf.append(subtype);
			buf.append(" for entity ");
			buf.append(repeatingCalendarItem.getEntityReference());
			log.warn(buf);
			subtype = subtype.substring(0, MAX_LENGTH_SUBTYPE_FIELD - 1);
		}
		Object[] params = new Object[]{
				repeatingCalendarItem.getFirstTime(), repeatingCalendarItem.getLastTime(), 
				repeatingCalendarItem.getFrequency(), repeatingCalendarItem.getMaxCount(), 
				repeatingCalendarItem.getCalendarTimeLabelKey(), repeatingCalendarItem.getTitle(),
				repeatingCalendarItem.getEntityReference(), subtype, sourceTypeId, contextId
			};
		
		try {
			getJdbcTemplate().update(sql, params);
			return true;
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addRepeatingCalendarItem() " + e);
			return false;
		} catch (DataAccessException ex) {
			log.warn("addRepeatingCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			//System.out.println("addRepeatingCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addSourceType(org.sakaiproject.dash.model.SourceType)
	 */
	public boolean addSourceType(SourceType sourceType) {
		if(log.isDebugEnabled()) {
			log.debug("addSourceType( " + sourceType.toString() + ")");
		}
		
		// identifier
		
		try {
			getJdbcTemplate().update(getStatement("insert.SourceType"),
				new Object[]{ sourceType.getIdentifier() }
			
			);
			return true;
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addSourceType() " + e);
			return false;
		} catch (DataAccessException ex) {
           log.warn("addSourceType: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addTaskLock(org.sakaiproject.dash.logic.TaskLock)
	 */
	public boolean addTaskLock(TaskLock taskLock) {
		if(log.isDebugEnabled()) {
			log.debug("addTaskLock( " + taskLock.toString() + ")");
		}
		
		//  task, server_id, claim_time, last_update
		
		try {
			getJdbcTemplate().update(getStatement("insert.TaskLock"),
				new Object[]{ taskLock.getTask(), taskLock.getServerId(), 
						taskLock.getClaimTime(), taskLock.getLastUpdate(), taskLock.isHasLock() 
					});
			return true;
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addTaskLock() " + e);
			return false;
		} catch (DataAccessException ex) {
           log.warn("addTaskLock: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteAvailabilityChecks(java.lang.String)
	 */
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
           log.warn("deleteAllAvailabilityChecks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteAvailabilityChecksBeforeTime(java.util.Date)
	 */
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
           log.warn("deleteAvailabilityChecksBeforeTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarItem(java.lang.Long)
	 */
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
           log.warn("deleteCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarItemsWithoutLinks()
	 */
	public boolean deleteCalendarItemsWithoutLinks() {
		log.info("deleteCalendarItemsWithoutLinks()");
		
		try {
			getJdbcTemplate().update(getStatement("delete.CalendarItems.no.links"));
			return true;
		} catch (DataAccessException ex) {
           log.warn("deleteCalendarItemsWithoutLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsItem(java.lang.Long)
	 */
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
           log.warn("deleteNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarLink(java.lang.Long, java.lang.Long)
	 */
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
           log.warn("deleteCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/* (non-Javadoc)
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
           log.warn("deleteCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarLinks(java.lang.Long, java.lang.Long)
	 */
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
           log.warn("deleteCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteCalendarLinksBefore(java.util.Date, boolean, boolean)
	 */
	public boolean deleteCalendarLinksBefore(Date expireBefore, boolean starred,
			boolean hidden) {
		log.info("deleteCalendarLinksBefore( " + expireBefore + "," + starred + "," + hidden + ")");
		
		try {
			getJdbcTemplate().update(getStatement("delete.CalendarLinks.by.item_calendarTime.starred.hidden"),
				new Object[]{expireBefore, new Boolean(starred), new Boolean(hidden)}
			);
			return true;
		} catch (DataAccessException ex) {
           log.warn("deleteCalendarLinksBefore: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteLinksByContext(java.lang.String, java.lang.String)
	 */
	public boolean deleteLinksByContext(String context, String type) {
		log.info("deleteLinksByContext(" + context + ", " + type + ")");
		if (type == null)
		{
			log.error(this + " deleteLinksByContext: null type string");
			return false;
		}
		try 
		{
			if (DashboardLogic.TYPE_CALENDAR.equals(type))
			{
				// remove calendar links
				getJdbcTemplate().update(getStatement("delete.CalendarLinks.by.context"), new Object[]{context});
			}
			else if (DashboardLogic.TYPE_NEWS.equals(type))
			{
				// remove news links
				getJdbcTemplate().update(getStatement("delete.NewsLinks.by.context"), new Object[]{context});
			}
			else
			{
				// wrong value for the type string
				log.error(this + " deleteLinksByContext: wrong type string " + type);
				return false;
			}
			return true;
		} catch (DataAccessException ex) {
			log.warn("deleteLinksByContext: "+ context + ", " + type + " Error executing query: " + ex.getClass() + ":" + ex.getMessage() );
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsItemsWithoutLinks()
	 */
	public boolean deleteNewsItemsWithoutLinks() {
		log.info("deleteNewsItemsWithoutLinks()");
		
		try {
			getJdbcTemplate().update(getStatement("delete.NewsItems.no.links"));
			return true;
		} catch (DataAccessException ex) {
           log.warn("deleteNewsItemsWithoutLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsLink(java.lang.Long, java.lang.Long)
	 */
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
           log.warn("deleteNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/* (non-Javadoc)
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
           log.warn("deleteNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsLinks(java.lang.Long, java.lang.Long)
	 */
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
           log.warn("deleteCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteNewsLinksBefore(java.util.Date, boolean, boolean)
	 */
	public boolean deleteNewsLinksBefore(Date expireBefore, boolean starred,
			boolean hidden) {
		log.info("deleteNewsLinksBefore( " + expireBefore + "," + starred + "," + hidden + ")");
		
		try {
			getJdbcTemplate().update(getStatement("delete.NewsLinks.by.item_newsTime.starred.hidden"),
				new Object[]{expireBefore, new Boolean(starred), new Boolean(hidden)}
			);
			return true;
		} catch (DataAccessException ex) {
           log.warn("deleteNewsLinksBefore: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteTaskLocks(java.lang.String)
	 */
	public boolean deleteTaskLocks(String task) {
		if(log.isDebugEnabled()) {
			log.debug("deleteTaskLocks( " + task + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.TaskLock.by.task"),
				new Object[]{ task }
			);
			return true;
		} catch (DataAccessException ex) {
           log.warn("deleteTaskLocks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}


	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getAvailabilityChecksBeforeTime(java.util.Date)
	 */
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
           log.warn("getAvailabilityChecksBeforeTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<AvailabilityCheck>();
		}
	}
	public boolean isScheduleAvailabilityCheckMade(String entityReference){
		if (log.isDebugEnabled()) {
			log.debug("isScheduleAvailabilityCheckMade");
		}
		String sql = getStatement("select.AvailabilityChecks.entry");
		Object[] params = new Object[] { entityReference };
		try {
			List<AvailabilityCheck> ac = (List<AvailabilityCheck>) getJdbcTemplate().query(sql, params,
					new AvailabilityCheckMapper());
			return !ac.isEmpty();
		} catch (DataAccessException ex) {
			log.warn("isScheduleAvailabilityCheckMade: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getRepeatingCalendarItem(java.lang.String, java.lang.String)
	 */
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
           log.warn("getRepeatingCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}


	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getRepeatingCalendarItems()
	 */
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
           log.warn("getRepeatingCalendarItems: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<RepeatingCalendarItem>();
		}
	}
	
	public List<CalendarItem> getCalendarItems(RepeatingCalendarItem repeatingEvent) {
		if(log.isDebugEnabled()) {
			log.debug("getInstancesOfRepeatingEvents(" + repeatingEvent + ")");
		}
		String sql  = getStatement("select.CalendarItems.by.repeatingEvent");
		Object[] params = new Object[]{repeatingEvent.getId()};
		
		try {
			return (List<CalendarItem>) getJdbcTemplate().query(sql,params,
				new CalendarItemMapper()
			);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getInstancesOfRepeatingEvents: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getInstancesOfRepeatingEvents: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<CalendarItem>();
		}
	}


	/* (non-Javadoc)
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getSourceType: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getSourceType: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/* (non-Javadoc)
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getCalendarItem: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItem(java.lang.String, java.lang.String, java.lang.Integer)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getCalendarItem: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        // System.out.println("addCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException e) {
           log.warn("getCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
	       // System.out.println("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
           return null;
		} catch (Exception e) {
	        log.warn("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        // System.out.println("addCalendarItem: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItems(java.lang.String)
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getCalendarItems: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getCalendarItems: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItems(java.lang.String, java.lang.String, boolean, boolean)
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
		
		try {
			return (List<CalendarItem>) getJdbcTemplate().query(sql,params,
				new CalendarItemMapper()
			);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getCalendarItems: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getCalendarItems: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<CalendarItem>();
		}
	}
	
	/* (non-Javadoc)
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
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItemsByContext(" + contextId + ") sql = " + sql);
		}
		// TODO: what do do if sql and/or params null ??
		try {
			return (List<CalendarItem>) getJdbcTemplate().query(sql,params,
				new CalendarItemMapper()
			);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getCalendarItemsByContext: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<CalendarItem>();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarLink(long, long)
	 */
	public CalendarLink getCalendarLink(long calendarItemId, long personId) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarLink(" + calendarItemId + ", " + personId + ")");
		}
		
		try {
			return (CalendarLink) getJdbcTemplate().queryForObject(getStatement("select.CalendarLink.by.calendarItemId.personId"),
				new Object[]{calendarItemId, personId},
				new CalendarLinkMapper()
			);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getCalendarLink: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarLink(long)
	 */
	public CalendarLink getCalendarLink(long id) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarLink(" + id + ")");
		}
		
		try {
			return (CalendarLink) getJdbcTemplate().queryForObject(getStatement("select.CalendarLink.by.id"),
				new Object[]{id},
				new CalendarLinkMapper()
			);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getCalendarLink: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getFutureCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		if(log.isDebugEnabled()) {
			log.debug("getFutureCalendarLinks(" + sakaiUserId + "," + contextId + "," + hidden + ")");
		}
		
		if(sakaiUserId == null) {
			log.warn("getFutureCalendarLinks() called with null sakaiUserId");
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getFutureCalendarLinks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getFutureCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getPastCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		if(log.isDebugEnabled()) {
			log.debug("getPastCalendarLinks(" + sakaiUserId + "," + contextId + "," + hidden + ")");
		}
		
		if(sakaiUserId == null) {
			log.warn("getPastCalendarLinks() called with null sakaiUserId");
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getPastCalendarLinks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getPastCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getStarredCalendarLinks(java.lang.String, java.lang.String)
	 */
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId, String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getStarredCalendarLinks(" + sakaiUserId + "," + contextId + ")");
		}
		
		if(sakaiUserId == null) {
			log.warn("getStarredCalendarLinks() called with null sakaiUserId");
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getStarredCalendarLinks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getStarredCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/* (non-Javadoc)
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getContext: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}  catch (Exception e) {
			log.warn("getContext: Error executing query: " + e.getClass() + ":" + e.getMessage());
	        return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getMOTD(java.lang.String)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getMOTD: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getMOTD: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsItem>();
		}
		
	}

	/* (non-Javadoc)
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getNewsItem: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}

	/* (non-Javadoc)
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getNewsItem: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/* (non-Javadoc)
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
		if(log.isDebugEnabled()) {
			log.debug("getNewsItemsByContext(" + contextId + ") sql = " + sql);
		}
		// TODO: what do do if sql and/or params null ??
		try {
			return (List<NewsItem>) getJdbcTemplate().query(sql,params,
				new NewsItemMapper()
			);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getNewsItemsByContext: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return new ArrayList<NewsItem>();
		} catch (DataAccessException ex) {
           log.warn("getNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsItem>();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsLink(long, long)
	 */
	public NewsLink getNewsLink(long newsItemId, long personId) {
		if(log.isDebugEnabled()) {
			log.debug("getNewsLink(" + newsItemId + ", " + personId + ")");
		}
		
		try {
			return (NewsLink) getJdbcTemplate().queryForObject(getStatement("select.NewsLink.by.newsItemId.personId"),
				new Object[]{newsItemId, personId},
				new NewsLinkMapper()
			);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getNewsLink: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
			return null;
		} catch (DataAccessException ex) {
           log.warn("getNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCurrentNewsLinks(java.lang.String, java.lang.String)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getCurrentNewsLinks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new ArrayList<NewsLink>();
		} catch (DataAccessException ex) {
           log.warn("getCurrentNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsLink>();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getStarredNewsLinks(java.lang.String, java.lang.String)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getStarredNewsLinks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new ArrayList<NewsLink>();
		} catch (DataAccessException ex) {
           log.warn("getStarredNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsLink>();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getHiddenNewsLinks(java.lang.String, java.lang.String)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getHiddenNewsLinks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new ArrayList<NewsLink>();
		} catch (DataAccessException ex) {
           log.warn("getHiddenNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsLink>();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#countNewsLinksByGroupId(java.lang.String, java.lang.String)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("countNewsLinksByGroupId: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return 0;
		} catch (DataAccessException ex) {
           log.warn("countNewsLinksByGroupId: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return 0;
		}
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getNewsLinksByGroupId(java.lang.String, java.lang.String, int, int)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getNewsLinksByGroupId: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new ArrayList<NewsLink>();
		} catch (DataAccessException ex) {
           log.warn("getNewsLinksByGroupId: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<NewsLink>();
		}
	}

	/* (non-Javadoc)
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getPersonBySakaiId: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return null;
		} catch (DataAccessException ex) {
           log.warn("getPersonBySakaiId: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getTaskLocks(java.lang.String)
	 */
	public List<TaskLock> getTaskLocks(String task) {
		// select.TaskLocks.by.task
		List<NewsItem> items = null;
		if(log.isDebugEnabled()) {
			log.debug("getTaskLocks(" + task + ")");
		}
		if(task == null) {
			return new ArrayList<TaskLock>();
		}
		String sql = getStatement("select.TaskLocks.by.task");
		
		Object[] params = new Object[]{task};
		try {
			return (List<TaskLock>) getJdbcTemplate().query(sql,params,
				new TaskLockMapper()
			);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getTaskLocks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new ArrayList<TaskLock>();
		} catch (DataAccessException ex) {
           log.warn("getTaskLocks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<TaskLock>();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getAssignedTaskLocks()
	 */
	public List<TaskLock> getAssignedTaskLocks() {
		// select.TaskLocks.by.hasLock
		List<NewsItem> items = null;
		if(log.isDebugEnabled()) {
			log.debug("getAssignedTaskLocks()");
		}
		String sql = getStatement("select.TaskLocks.by.hasLock");
		
		try {
			return (List<TaskLock>) getJdbcTemplate().query(sql,
				new TaskLockMapper()
			);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getAssignedTaskLocks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new ArrayList<TaskLock>();
		} catch (DataAccessException ex) {
           log.warn("getAssignedTaskLocks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new ArrayList<TaskLock>();
		}
	}
		
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getFutureSequenceNumbers(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public SortedSet<Integer> getFutureSequenceNumbers(String entityReference,
			String calendarTimeLabelKey, Integer firstSequenceNumber) {
		if(log.isDebugEnabled()) {
			log.debug("getLastIndexInSequence(" + entityReference + "," + calendarTimeLabelKey + "," + firstSequenceNumber + ")");
		}
		
		try {
			
			String sql = getStatement("select.SequenceNumbers.entityReference.calendarTimeLabelKey.sequenceNumber");
				Object[] args = new Object[]{ entityReference, calendarTimeLabelKey, firstSequenceNumber };
			List items = getJdbcTemplate().queryForList(sql, args, Integer.class);
			return new TreeSet(items);
			
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getFutureSequenceNumbers: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new TreeSet<Integer>();
		} catch (DataAccessException ex) {
           log.warn("getFutureSequenceNumbers: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new TreeSet<Integer>();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getSakaIdsForUserWithCalendarLinks(java.lang.String)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getSakaIdsForUserWithCalendarLinks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new HashSet<String>();
		} catch (DataAccessException ex) {
           log.warn("getSakaIdsForUserWithCalendarLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new HashSet<String>();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getSakaiIdsForUserWithNewsLinks(java.lang.String)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getSakaIdsForUserWithNewsLinks: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new HashSet<String>();
		} catch (DataAccessException ex) {
           log.warn("getSakaIdsForUserWithNewsLinks: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new HashSet<String>();
		}
	}
	
	public Set<String> listUsersWithLinks(CalendarItem calendarItem) {
		if(log.isDebugEnabled()) {
			log.debug("listUsersWithAccess(" + calendarItem + ")");
		}
		
		try {
			
			String sql = getStatement("select.Person.sakaiId.by.calendarLink");
				Object[] args = new Object[]{ calendarItem.getId() };
			List items = getJdbcTemplate().queryForList(sql, args, String.class);
			if(items == null || items.isEmpty()) {
				return new TreeSet<String>();
			}
			return new TreeSet<String>(items);
			
		} catch (EmptyResultDataAccessException ex) {
			log.debug("listUsersWithAccess: Empty result executing query: " + ex.getClass() + ":" + ex.getMessage());
	        return new TreeSet<String>();
		} catch (DataAccessException ex) {
           log.warn("listUsersWithAccess: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return new TreeSet<String>();
		}

	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#addEvent(java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
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
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("addEvent() " + e);
			return false;
		} catch (DataAccessException ex) {
			log.warn("saveEvent: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			//System.out.println("addRepeatingCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			return false;
		}
		
	}
	
	public boolean updateCalendarItem(CalendarItem calendarItem) {
		if(log.isDebugEnabled()) {
			log.debug("updateCalendarItem( " + calendarItem + ")");
		}
		
		String subtype = calendarItem.getSubtype();
		// DASH-191
		if(subtype != null && subtype.length() > MAX_LENGTH_SUBTYPE_FIELD) {
			StringBuilder buf = new StringBuilder();
			buf.append("addCalendarItem().  Truncating subtype ");
			buf.append(subtype);
			buf.append(" for entity ");
			buf.append(calendarItem.getEntityReference());
			log.warn(buf);
			subtype = subtype.substring(0, MAX_LENGTH_SUBTYPE_FIELD - 1);
		}
		
		JdbcTemplate template = getJdbcTemplate();
		Object[] params = null;
		String sql = null;
		if(calendarItem.getRepeatingCalendarItem() == null) {
			sql = getStatement("update.CalendarItem");
			params = new Object[]{calendarItem.getCalendarTime(), calendarItem.getCalendarTimeLabelKey(), calendarItem.getTitle(), 
					calendarItem.getEntityReference(), subtype,
					calendarItem.getSourceType().getId(), calendarItem.getContext().getId(), calendarItem.getId()};
		} else {
			sql = getStatement("update.CalendarItem.repeats");
			params = new Object[]{calendarItem.getCalendarTime(), calendarItem.getCalendarTimeLabelKey(), calendarItem.getTitle(), 
					calendarItem.getEntityReference(), subtype,
					calendarItem.getSourceType().getId(), calendarItem.getContext().getId(), 
					calendarItem.getRepeatingCalendarItem().getId(), calendarItem.getSequenceNumber(), calendarItem.getId()};
		}

		template.update(sql,params);
		return true;
	}

	/* (non-Javadoc)
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
           log.warn("updateCalendarItemTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemTime(java.lang.String, java.lang.String, java.lang.Integer, java.util.Date)
	 */
	public boolean updateCalendarItemTime(String entityReference, String labelKey,
			Integer sequenceNumber, Date newDate) {
		if(log.isDebugEnabled()) {
			log.debug("updateCalendarItemTime( " + entityReference + "," + labelKey + "," + sequenceNumber + "," + newDate + ")");
		}
		
		try {
			if(sequenceNumber == null) {
				getJdbcTemplate().update(getStatement("update.CalendarItem.calendarTime.entityReference.labelKey"),
						new Object[]{newDate, entityReference, labelKey });
			} else {
				getJdbcTemplate().update(getStatement("update.CalendarItem.calendarTime.entityReference.labelKey.sequenceNumber"),
						new Object[]{newDate, entityReference, labelKey, sequenceNumber });
			}
			return true;
		} catch (DataAccessException ex) {
           log.warn("updateCalendarItemTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarItemsLabelKey(java.lang.String, java.lang.String, java.lang.String)
	 */
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
           log.warn("updateCalendarsItemLabelKey: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}

	/* (non-Javadoc)
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
           log.warn("updateCalendarItemTitle: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateCalendarLink(org.sakaiproject.dash.model.CalendarLink)
	 */
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
           log.warn("updateCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateContextTitle(java.lang.String, java.lang.String)
	 */
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
           log.warn("updateContextTitle: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemTime(java.lang.Long, java.util.Date, java.lang.String)
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
           log.warn("updateNewsItemTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsItemTitle(java.lang.Long, java.lang.String, java.util.Date, java.lang.String, java.lang.String)
	 */
	public boolean updateNewsItemTitle(Long id, String newTitle, Date newNewsTime, String newLabelKey, String newGroupingIdentifier) {
		if(log.isDebugEnabled()) {
			log.debug("updateNewsItemTitle( " + id + "," + newTitle + ")");
		}
		
		if(id == null || newTitle == null || newNewsTime == null || newLabelKey == null) {
			log.warn("updateNewsItemTitle() called with null values");
			return false;
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.NewsItem.title"),
				new Object[]{newTitle, newNewsTime, newLabelKey, newGroupingIdentifier, id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.warn("updateNewsItemTitle: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateNewsLink(org.sakaiproject.dash.model.NewsLink)
	 */
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
           log.warn("updateNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemFrequency(java.lang.String, java.lang.String)
	 */
	public boolean updateRepeatingCalendarItemFrequency(String entityReference,
			String frequency) {
		if(log.isDebugEnabled()) {
			log.debug("updateRepeatingCalendarItemFrequency( " + entityReference + "," + frequency + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.RepeatingEventsFrequency.entityReference"),
				new Object[]{frequency, entityReference}
			);
			return true;
		} catch (DataAccessException ex) {
           log.warn("updateRepeatingCalendarItemFrequency: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}

	/* (non-Javadoc)
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
           log.warn("updateRepeatingCalendarItemsLabelKey: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemTime(java.lang.String, java.util.Date, java.util.Date)
	 */
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
           log.warn("updateRepeatingCalendarItemTime: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateRepeatingCalendarItemTitle(java.lang.String, java.lang.String)
	 */
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
           log.warn("updateRepeatingCalendarItemTitle: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateTaskLock(org.sakaiproject.dash.logic.TaskLock)
	 */
	public boolean updateTaskLock(long id, boolean hasLock, Date lastUpdate) {
		if(log.isDebugEnabled()) {
			log.debug("updateTaskLock( " + id + "," + hasLock + "," + lastUpdate + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.TaskLock.hasLock"),
				new Object[]{ hasLock, lastUpdate, id }
			);
			return true;
		} catch (DataAccessException ex) {
           log.warn("updateTaskLock: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}				
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#updateTaskLock(java.lang.String, java.lang.String, java.util.Date)
	 */
	public boolean updateTaskLock(String task, String serverId, Date lastUpdate) {
		if(log.isDebugEnabled()) {
			log.debug("updateTaskLock( " + task + "," + serverId + "," + lastUpdate + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("update.TaskLock.lastUpdate"),
				new Object[]{ lastUpdate, task, serverId }
			);
			return true;
		} catch (DataAccessException ex) {
           log.warn("updateTaskLock: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
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
			log.warn("Statement: '" + key + "' could not be found in: " + statements.getFileName());
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
			log.warn(e.getClass() + ": " + e.getMessage());
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
			executeSqlStatement("create.TaskLock.table");
		} catch(Exception e) {
	        //System.out.println("\ninitTables: Error executing query: " + e.getClass() + ":\n" + e.getMessage() + "\n");
			log.warn("initTables() " + e);
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
							
						} catch(DataIntegrityViolationException e) {
							// this means we're trying to insert a duplicate
							log.debug("executeSqlStatement() " + e);
						} catch (DataAccessException ex) {
							log.warn("Error executing SQL statement with key: " + key + " -- " + ex.getClass() + ": " + ex.getMessage());
					        //System.out.println("\nError executing SQL statement with key: " + key + " -- " + ex.getClass() + ": \n" + ex.getMessage() + "\n");
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getConfigProperty(java.lang.String)
	 */
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
		} catch (EmptyResultDataAccessException ex) {
			// do nothing.  This means no value is set for this property, an expected condition in some cases.
			// log.warn("getConfigProperty: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		} catch (DataAccessException ex) {
            log.warn("getConfigProperty: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		} catch (Exception ex) {
	        log.warn("getConfigProperty: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		}
		
		return value ;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#setConfigProperty(java.lang.String, java.lang.Integer)
	 */
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
		} catch (DataIntegrityViolationException e) {
			// this means we're trying to insert a duplicate
			log.debug("setConfigProperty() " + e);
		} catch (Exception e) {
			// insert failed -- try update instead of insert
	        try {
	        	jdbcTemplate.update(sql_update, params_update);
	        } catch (DataAccessException ex) {
	            log.warn("setConfigProperty: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			} catch (Exception ex) {
		        log.warn("setConfigProperty: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
			}
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

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#deleteRepeatingEvent(java.lang.Long)
	 */
	public boolean deleteRepeatingEvent(Long id) {
		if(log.isDebugEnabled()) {
			log.debug("deleteRepeatingEvent( " + id + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("delete.RepeatingEvent.by.id"),
				new Object[]{id}
			);
			return true;
		} catch (DataAccessException ex) {
           log.warn("deleteRepeatingEvent: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getDashboardCalendarContextUserMap()
	 */
	public HashMap<String, Set<String>> getDashboardCalendarContextUserMap()
	{
		return getDashboardContextUserMap("select.context.user.from.calendar.link");
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getDashboardNewsContextUserMap()
	 */
	public HashMap<String, Set<String>> getDashboardNewsContextUserMap()
	{
		return getDashboardContextUserMap("select.context.user.from.news.link");
	}
	
	/**
	 * construct HashMap, 
	 * keyed with context id, 
	 * value is set of user ids that has links(calendarlink, newslink) in dashboard
	 * @param the sql name
	 * @return HashMap object
	 */
	private HashMap<String, Set<String>> getDashboardContextUserMap(String sqlName)
	{
		HashMap<String, Set<String>> dashboardUserMap = new HashMap<String, Set<String>>();
		String sql = getStatement(sqlName);
		try {
			List<String> contextUsersList = (List<String>) getJdbcTemplate().query(sql,
				new ContextUserMapper()
			);
			
			if (contextUsersList != null)
			{
				for(String contextUser : contextUsersList)
				{
					// the string returned from db query is of format context id + " " + user id
					// need to parse it out and form HashMap
					String[] parts = contextUser.split(" ");
					if (parts.length == 2)
					{
						// parts: context id (site id) and user id
						String context_id = parts[0];
						String user_id = parts[1];
						if (dashboardUserMap.containsKey(context_id))
						{
							// get the current set and add user id into it
							Set<String> current = dashboardUserMap.get(context_id);
							current.add(user_id);
							dashboardUserMap.put(context_id, current);
						}
						else
						{
							// add the new key
							Set<String> current = new HashSet<String>();
							current.add(user_id);
							dashboardUserMap.put(context_id, current);
						}
							
					}
				}
			}
		} catch (EmptyResultDataAccessException ex) {
			log.debug("getDashboardContextUserMap: Empty result executing query: " + sqlName + " " + ex.getClass() + ":" + ex.getMessage());
		} catch (DataAccessException ex) {
           log.warn("getDashboardContextUserMap: Error executing query: " + sqlName + " " + ex.getClass() + ":" + ex.getMessage());
		}
		
		return dashboardUserMap;
	}

	
}
