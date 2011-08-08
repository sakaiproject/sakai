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
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.apache.log4j.Logger;

import org.sakaiproject.component.cover.ServerConfigurationService;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.SourceType;

import org.sakaiproject.dash.dao.impl.CalendarItemMapper;
import org.sakaiproject.dash.dao.impl.ContextMapper;

import org.sakaiproject.dash.model.Thing;


/**
 * Implementation of ProjectDao
 * 
 * 
 *
 */
public class DashboardDaoImpl extends JdbcDaoSupport implements DashboardDao {

	private static final Logger log = Logger.getLogger(DashboardDaoImpl.class);
	
	private PropertiesConfiguration statements;
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Thing getThing(long id) {
		
		if(log.isDebugEnabled()) {
			log.debug("getThing(" + id + ")");
		}
		
		try {
			return (Thing) getJdbcTemplate().queryForObject(getStatement("select.thing"),
				new Object[]{Long.valueOf(id)},
				new ThingMapper()
			);
		} catch (DataAccessException ex) {
           log.error("Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Thing> getThings() {
		if(log.isDebugEnabled()) {
			log.debug("getThings()");
		}
		
		try {
			return getJdbcTemplate().query(getStatement("select.things"),
				new ThingMapper()
			);
		} catch (DataAccessException ex) {
           log.error("Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean addThing(Thing t) {
		
		if(log.isDebugEnabled()) {
			log.debug("addThing( " + t.toString() + ")");
		}
		
		try {
			getJdbcTemplate().update(getStatement("insert.thing"),
				new Object[]{t.getName()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
	
	/**
	 * init
	 */
	public void init() {
		log.info("init()");
		
		//setup the vendor
		String vendor = ServerConfigurationService.getInstance().getString("vendor@org.sakaiproject.db.api.SqlService", null);
		
		//initialise the statements
		initStatements(vendor);
		
		//setup tables if we have auto.ddl enabled.
		boolean autoddl = ServerConfigurationService.getInstance().getBoolean("auto.ddl", true);
		if(autoddl) {
			initTables();
		}
	}
	
	/**
	 * Sets up our tables
	 */
	private void initTables() {
		try {
			getJdbcTemplate().execute(getStatement("create.table"));
			getJdbcTemplate().execute(getStatement("create.Context.table"));
			getJdbcTemplate().execute(getStatement("create.Person.table"));
			getJdbcTemplate().execute(getStatement("create.Realm.table"));
			getJdbcTemplate().execute(getStatement("create.SourceType.table"));
			getJdbcTemplate().execute(getStatement("create.NewsItem.table"));
			getJdbcTemplate().execute(getStatement("create.NewsLink.table"));
			getJdbcTemplate().execute(getStatement("create.CalendarItem.table"));
			getJdbcTemplate().execute(getStatement("create.CalendarLink.table"));
			
			
		} catch (DataAccessException ex) {
			log.info("Error creating tables: " + ex.getClass() + ":" + ex.getMessage());
			return;
		}
	}
	
	/**
	 * Loads our SQL statements from the appropriate properties file
	 
	 * @param vendor	DB vendor string. Must be one of mysql, oracle, hsqldb
	 */
	private void initStatements(String vendor) {
		
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
	 * Get an SQL statement for the appropriate vendor from the bundle
	
	 * @param key
	 * @return statement or null if none found. 
	 */
	private String getStatement(String key) {
		try {
			return statements.getString(key);
		} catch (NoSuchElementException e) {
			log.error("Statement: '" + key + "' could not be found in: " + statements.getFileName());
			return null;
		}
	}

	/**
	 * 
	 */
	public boolean addCalendarItem(CalendarItem calendarItem) {
		if(log.isDebugEnabled()) {
			log.debug("addCalendarItem( " + calendarItem.toString() + ")");
}
		
		// calendar_time, title , entity_url, entity_ref, source_type, context, realm
		
		try {
			getJdbcTemplate().update(getStatement("insert.CalendarItem"),
				new Object[]{calendarItem.getCalendarTime(), calendarItem.getTitle(), 
						calendarItem.getEntityUrl(), calendarItem.getEntityReference(),
						calendarItem.getSourceType().getId(), calendarItem.getContext().getId(),
						calendarItem.getRealm().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/**
	 * 
	 */
	public boolean addCalendarLink(CalendarLink calendarLink) {
		if(log.isDebugEnabled()) {
			log.debug("addCalendarLink( " + calendarLink.toString() + ")");
		}
		
		//  person_id, item_id, context_id, realm_id
		
		try {
			getJdbcTemplate().update(getStatement("insert.CalendarLink"),
				new Object[]{calendarLink.getPerson().getId(), calendarLink.getCalendarItem().getId(), 
						calendarLink.getContext().getId(), calendarLink.getRealm().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addCalendarLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/**
	 * 
	 */
	public boolean addContext(Context context) {
		if(log.isDebugEnabled()) {
			log.debug("addContext( " + context.toString() + ")");
		}
		
		//  context_id, context_url, context_title
		
		try {
			getJdbcTemplate().update(getStatement("insert.Context"),
				new Object[]{context.getContextId(), context.getContextUrl(), 
				context.getContextTitle()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/**
	 * 
	 */
	public boolean addNewsItem(NewsItem newsItem) {
		if(log.isDebugEnabled()) {
			log.debug("addNewsItem( " + newsItem.toString() + ")");
		}
		
		// news_time, title , entity_url, entity_ref, source_type, context, realm
		
		try {
			getJdbcTemplate().update(getStatement("insert.NewsItem"),
				new Object[]{newsItem.getNewsTime(), newsItem.getTitle(), 
						newsItem.getEntityUrl(), newsItem.getEntityReference(),
						newsItem.getSourceType().getId(), newsItem.getContext().getId(),
						newsItem.getRealm().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addNewsItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/**
	 * 
	 */
	public boolean addNewsLink(NewsLink newsLink) {
		if(log.isDebugEnabled()) {
			log.debug("addNewsLink( " + newsLink.toString() + ")");
		}
		
		//  person_id, item_id, context_id, realm_id
		
		try {
			getJdbcTemplate().update(getStatement("insert.NewsLink"),
				new Object[]{newsLink.getPerson().getId(), newsLink.getNewsItem().getId(), 
						newsLink.getContext().getId(), newsLink.getRealm().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addNewsLink: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}

	/**
	 * 
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

	/**
	 * 
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

	/**
	 * 
	 */
	public boolean addSourceType(SourceType sourceType) {
		if(log.isDebugEnabled()) {
			log.debug("addSourceType( " + sourceType.toString() + ")");
		}
		
		// name
		
		try {
			getJdbcTemplate().update(getStatement("insert.SourceType"),
				new Object[]{sourceType.getSourceType()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addSourceType: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return false;
		}
	}
		
	/**
	 * 
	 */
	public SourceType getSourceType(String sourceTypeName) {
		if(log.isDebugEnabled()) {
			log.debug("getSourceType( " + sourceTypeName + ")");
		}
		
		try {
			return (SourceType) getJdbcTemplate().queryForObject(getStatement("select.SourceType.by.sourceType"),
				new Object[]{sourceTypeName},
				new SourceTypeMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}	


	public CalendarItem getCalendarItem(String entityReference) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItem(" + entityReference + ")");
		}
		
		try {
			return (CalendarItem) getJdbcTemplate().queryForObject(getStatement("select.CalendarItem.by.entityReference"),
				new Object[]{entityReference},
				new CalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}

	}
	public CalendarItem getCalendarItem(long id) {
		if(log.isDebugEnabled()) {
			log.debug("getCalendarItem(" + id + ")");
		}
		
		try {
			return (CalendarItem) getJdbcTemplate().queryForObject(getStatement("select.CalendarItem.by.id"),
				new Object[]{Long.valueOf(id)},
				new CalendarItemMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}

	}

	public Context getContext(String contextId) {
		if(log.isDebugEnabled()) {
			log.debug("getContext(" + contextId + ")");
		}
		
		try {
			return (Context) getJdbcTemplate().queryForObject(getStatement("select.Context.by.contextId"),
				new Object[]{contextId},
				new ContextMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
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
}
