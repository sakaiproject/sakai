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
	
	protected PropertiesConfiguration statements;
	
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
			getJdbcTemplate().update(getStatement("insert.CalendarItem"),
				new Object[]{calendarItem.getCalendarTime(), calendarItem.getTitle(), 
						calendarItem.getEntityUrl(), calendarItem.getEntityReference(),
						calendarItem.getSourceType().getId(), calendarItem.getContext().getId()}
			);
			return true;
		} catch (DataAccessException ex) {
           log.error("addCalendarItem: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
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
						calendarLink.getContext().getId()}
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
           log.error("getContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.dao.DashboardDao#getCalendarItem(java.lang.String)
	 */
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
			return (Context) getJdbcTemplate().queryForObject(getStatement("select.Context.by.contextId"),
				new Object[]{contextId},
				new ContextMapper()
			);
		} catch (DataAccessException ex) {
           log.error("getContext: Error executing query: " + ex.getClass() + ":" + ex.getMessage());
           return null;
		}
	}
	
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
		//executeSqlStatement("create.table"));
		executeSqlStatement("create.Context.table");
		executeSqlStatement("create.Person.table");
		//executeSqlStatement("create.Realm.table");
		executeSqlStatement("create.SourceType.table");
		executeSqlStatement("create.NewsItem.table");
		executeSqlStatement("create.NewsLink.table");
		executeSqlStatement("create.CalendarItem.table");
		executeSqlStatement("create.CalendarLink.table");
		
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
	
	public boolean addThing(Thing t) {
		// TODO Auto-generated method stub
		return false;
	}

}
