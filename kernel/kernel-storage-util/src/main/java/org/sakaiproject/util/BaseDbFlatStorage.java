/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/BaseDbFlatStorage.java $
 * $Id: BaseDbFlatStorage.java 97116 2011-08-17 15:26:47Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.cover.MemoryServiceLocator;
import org.sakaiproject.time.cover.TimeService;

/**
 * <p>
 * BaseDbFlatStorage is a class that stores Resources (of some type) and associated properties
 * in a database, provides (optional) locked access, <br />
 * and generally implements a services "storage" class. <br />
 * The reason you would want to use this class is if you want to perform a DB query against the resource properties.
 * The service's storage class can extend this to provide covers to turn Resource and Edit into something more type specific to the service.
 * </p>
 * <p>
 * Note: the methods here are all "id" based, with the following assumptions:
 * <ul>
 * <li>just the Resource Id field is enough to distinguish one Resource from another</li>
 * <li> a resource's reference is based on no more than the resource id</li>
 * <li> a resource's id cannot change</li>
 * </ul>
 * <br />
 * In order to handle Unicode characters properly, the SQL statements executed by this class should not embed Unicode characters into the SQL
 * statement text; <br />
 * rather, Unicode values should be inserted as fields in a PreparedStatement. Databases handle Unicode better in fields.
 * </p>
 */
@Slf4j
public class BaseDbFlatStorage
{
	private static final String CACHE_NAME_PREFIX = "org.sakaiproject.db.BaseDbFlatStorage.";

	/** Table name for resource records. */
	protected String m_resourceTableName = null;

	/** Table name for the resource properties. */
	protected String m_resourcePropertyTableName = null;

	/** The field in the resource table that holds the resource id. */
	protected String m_resourceTableIdField = null;

	/** The field in the resource table that is used for sorting (first sort). */
	protected String m_resourceTableSortField1 = null;

	/** The field in the resource table that is used for sorting (second sort). */
	protected String m_resourceTableSortField2 = null;

	/** The full set of fields in the table to read. */
	protected String[] m_resourceTableReadFields = null;

	/** The full set of fields in the table for update - can be field name only, or field=xxx expression. */
	protected String[] m_resourceTableUpdateFields = null;

	/** The full set of fields in the table for insert - just field (not counting a dbid field). */
	protected String[] m_resourceTableInsertFields = null;

	/**
	 * The full set of value expressions for an insert - either null or ? or an expression - to match m_resourceTableInsertFields (not counting a dbid
	 * field).
	 */
	protected String[] m_resourceTableInsertValues = null;

	/** The extra db field for an integer 'db' id - auto-written on insert only. */
	protected String m_resourceTableDbidField = null;

	/** If false, we are not doing any locking, else we are. */
	protected boolean m_locking = true;

	/** If true, we do our locks in the remote database using a separate locking table, otherwise we do them in the class. */
	protected boolean m_locksAreInTable = true;

	/** Locks (if used), keyed by reference, holding Edits. */
	protected Hashtable m_locks = null;

	/** If set, we treat reasource ids as case insensitive. */
	protected boolean m_caseInsensitive = false;

	/** Injected (by constructor) SqlService. */
	protected SqlService m_sql = null;

	/** SqlReader to use when reading the record. */
	protected SqlReader m_reader = null;

	/** contains a map of the database dependent handlers. */
	protected static Map<String, FlatStorageSql> databaseBeans;

	/** The db handler we are using. */
	protected FlatStorageSql flatStorageSql;

	public void setDatabaseBeans(Map databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setFlatStorageSql(String vendor)
	{
		this.flatStorageSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

	// since spring is not used and this class is instatiated directly, we need to "inject" these values ourselves
	static
	{
		databaseBeans = new Hashtable<String, FlatStorageSql>();
		databaseBeans.put("default", new FlatStorageSqlDefault());
		databaseBeans.put("hsql", new FlatStorageSqlHSql());
		databaseBeans.put("mysql", new FlatStorageSqlMySql());
		databaseBeans.put("oracle", new FlatStorageSqlOracle());
	}

	/**
	 * Construct.
	 * 
	 * @param resourceTableName
	 *        Table name for resources.
	 * @param resourceTableIdField
	 *        The field in the resource table that holds the id.
	 * @param resourceTableFields
	 *        The complete set of fields to read / write for the resource.
	 * @param propertyTableName
	 *        The table name for standard property support.
	 * @param locksInTable
	 *        If true, we do our locks in the remote database in a locks table, otherwise we do them here.
	 * @param reader
	 *        A SqlReader which will produce Edits given fields read from the table.
	 * @param sqlService
	 *        The SqlService.
	 */
	public BaseDbFlatStorage(String resourceTableName, String resourceTableIdField, String[] resourceTableFields, String propertyTableName,
			boolean locksInTable, SqlReader reader, SqlService sqlService)
	{
		m_resourceTableName = resourceTableName;
		m_resourceTableIdField = resourceTableIdField;
		m_resourceTableSortField1 = resourceTableIdField;
		m_resourceTableReadFields = resourceTableFields;
		m_resourcePropertyTableName = propertyTableName;
		m_locksAreInTable = locksInTable;
		m_sql = sqlService;
		m_reader = reader;

		m_resourceTableUpdateFields = resourceTableFields;
		m_resourceTableInsertFields = resourceTableFields;
		m_resourceTableInsertValues = resourceTableFields;

		setFlatStorageSql(m_sql.getVendor());
	}

	/**
	 * Get the cache manager for this table
	 *
	 * @param table
	 */
	protected Cache getCache(String table)
	{
		if ( table == null ) return null;
		String config =  ServerConfigurationService.getString("DbFlatPropertiesCache");

		// Default is :all:
		if ( config == null || config.trim().length() <= 0 ) config = ":all:";

		if ( config.indexOf(":none:") >= 0 ) return null;
		if ( config.indexOf(":all:") < 0 )
		{
			if ( config.indexOf(":"+table+":") < 0 ) return null;
		}

		String cacheName = CACHE_NAME_PREFIX+table;
		MemoryService memoryService = MemoryServiceLocator.getInstance();
		if ( memoryService == null ) return null;
		Cache myCache = memoryService.newCache(cacheName);
		return myCache;
	}

	/**
	 * Set the sort field to be something perhaps other than the default of the id field.
	 * 
	 * @param sortField1
	 *        The field name to use for sorting.
	 * @param sortField2
	 *        Optional second sort field.
	 */
	public void setSortField(String sortField1, String sortField2)
	{
		m_resourceTableSortField1 = sortField1;
		m_resourceTableSortField2 = sortField2;
	}

	/**
	 * Set a field that will be read after the field list, and
	 * 
	 * @param dbidField
	 */
	public void setDbidField(String dbidField)
	{
		m_resourceTableDbidField = dbidField;
	}

	/**
	 * Establish a different set of fields for inserts and updated.
	 * 
	 * @param resourceTableFields
	 *        The complete set of fields to write for the resource (not counting a dbid field if needed).
	 */
	public void setWriteFields(String[] updateFields, String[] insertFields, String[] insertValues)
	{
		m_resourceTableUpdateFields = updateFields;
		m_resourceTableInsertFields = insertFields;
		m_resourceTableInsertValues = insertValues;
	}

	/**
	 * Set if we are doing locking or not.
	 * 
	 * @param value
	 *        If true, we should do locking, else not.
	 */
	public void setLocking(boolean value)
	{
		m_locking = value;
	}

	/**
	 * Open and be ready to read / write.
	 */
	public void open()
	{
		// setup for locks
		m_locks = new Hashtable();
	}

	/**
	 * Close.
	 */
	public void close()
	{
		if (!m_locks.isEmpty())
		{
			log.warn("close(): locks remain!");
			// %%%
		}
		m_locks.clear();
		m_locks = null;
	}

	/**
	 * Check if a Resource by this id exists.
	 * 
	 * @param id
	 *        The id.
	 * @return true if a Resource by this id exists, false if not.
	 */
	public boolean checkResource(String id)
	{
		// just see if the record exists
		String sql = flatStorageSql.getSelectFieldSql(m_resourceTableName, m_resourceTableIdField);
		Object fields[] = new Object[1];
		fields[0] = caseId(id);
		List ids = m_sql.dbRead(sql, fields, null);

		return (!ids.isEmpty());
	}

	/**
	 * Get the Resource with this id, or null if not found.
	 * 
	 * @param id
	 *        The id.
	 * @return The Resource with this id, or null if not found.
	 */
	public Entity getResource(String id)
	{
		return getResource(null, id);
	}

	/**
	 * Get the Resource with this id, or null if not found.
	 * 
	 * @param optional
	 *        connection to use.
	 * @param id
	 *        The id.
	 * @return The Resource with this id, or null if not found.
	 */
	public Entity getResource(Connection conn, String id)
	{
		Entity entry = null;

		// get the user from the db
		String sql = flatStorageSql.getSelectFieldsSql(m_resourceTableName, fieldList(m_resourceTableReadFields, null), m_resourceTableIdField);
		Object fields[] = new Object[1];
		fields[0] = caseId(id);
		List rv = m_sql.dbRead(conn, sql, fields, m_reader);

		if ((rv != null) && (rv.size() > 0))
		{
			entry = (Entity) rv.get(0);
		}

		return entry;
	}

	public List getAllResources()
	{
		// read all resources from the db
		String sql = flatStorageSql.getSelectFieldsSql(m_resourceTableName, fieldList(m_resourceTableReadFields, null));
		List rv = m_sql.dbRead(sql, null, m_reader);

		return rv;
	}

	public int countAllResources()
	{

		// read all count
		String sql = flatStorageSql.getSelectCountSql(m_resourceTableName);
		List results = m_sql.dbRead(sql, null, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					int count = result.getInt(1);
					return Integer.valueOf(count);
				}
				catch (SQLException ignore)
				{
					return null;
				}
			}
		});

		if (results.isEmpty()) return 0;

		return ((Integer) results.get(0)).intValue();
	}

	public List getAllResources(int first, int last)
	{
		String sql;
		Object[] fields = null;
		if ("oracle".equals(m_sql.getVendor()))
		{
			// use Oracle RANK function, adding the id to the sort fields to assure we have a unique ranking
			sql = flatStorageSql.getSelectFieldsSql1(m_resourceTableName, fieldList(m_resourceTableReadFields, null), m_resourceTableIdField,
					m_resourceTableSortField1, m_resourceTableSortField2,0,0);
			fields = flatStorageSql.getSelectFieldsFields(first, last);
		}
		else
		{
			sql = flatStorageSql.getSelectFieldsSql1(m_resourceTableName, fieldList(m_resourceTableReadFields, null), null,
					m_resourceTableSortField1, m_resourceTableSortField2, (first - 1), (last - first + 1));
		}
		List rv = m_sql.dbRead(sql, fields, m_reader);

		return rv;
	}

	/**
	 * Get all Resources matching a SQL where clause, with sorting
	 * 
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceeding "where ").
	 * @param values
	 *        The bind values
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getSelectedResources(String where, Object[] values)
	{
		return getSelectedResources(where, null, values, null);
	}

	/**
	 * Get all Resources matching a SQL where clause, with sorting and ordering
	 * 
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceeding "where ").
	 * @param order
	 *        the SQL order clause (not including the preceeding "order by ").
	 * @param values
	 *        The bind values
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getSelectedResources(String where, String order, Object[] values)
	{
		return getSelectedResources(where, order, values, null);
	}

	/**
	 * Get all Resources matching a SQL where clause, with sorting and ordering
	 * 
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceeding "where ").
	 * @param order
	 *        the SQL order clause (not including the preceeding "order by ").
	 * @param values
	 *        The bind values
	 * @param join
	 *        a single or comma separated set of other tables to join in the from clause
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getSelectedResources(String where, String order, Object[] values, String join)
	{
		return getSelectedResources(where, order, values, join, m_reader);
	}

	/**
	 * Get all Resources matching an SQL where clause, with sorting and ordering, using a specialized reader.
	 *
	 * This is provided for specialized cases, where a known operation should make some optimizations or
	 * apply transformations to given fields during row retrieval. For example, reading CLOBs may be the
	 * default, but in some cases like long descriptions in simple search results, they are not relevant
	 * and could hurt performance. Note that specialized readers should have the same return type and
	 * expect the same indices as the default reader.
	 *
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceding "where ").
	 * @param order
	 *        the SQL order clause (not including the preceding "order by ").
	 * @param values
	 *        The bind values
	 * @param join
	 *        a single or comma separated set of other tables to join in the from clause
	 * @param reader
	 *        a specialized SqlReader for this request supplied to read each row differently than the default
	 * @return The list of all Resources that meet the criteria.
	 */
	protected List getSelectedResources(String where, String order, Object[] values, String join, SqlReader reader)
	{
		// read all resources from the db with a where
		String sql = getResourceSql(where, order, values, join);
		List all = m_sql.dbRead(sql, values, reader);

		return all;
	}

	/**
	 * Get the SQL to retrieve all resources matching specified conditions.
	 *
	 * TODO: Push this down to FlatStorageSql
	 *
	 * See {@link #getSelectedResources(String where, String order, Object[] values, String join)} for parameter details
	 */
	protected String getResourceSql(String where, String order, Object[] values, String join)
	{
		String fieldNames = fieldList(m_resourceTableReadFields, null);
		return getResourceSql(fieldNames, where, order, values, join);
	}

	/**
	 * Get the SQL to retrieve a subset of fields for all resources matching specified conditions.
	 *
	 * TODO: Push this down to FlatStorageSql
	 *
	 * @param fieldNames the fully qualified field list to select as used in an SQL query
	 *
	 * See {@link #getSelectedResources(String where, String order, Object[] values, String join)} for other parameter details
	 */
	protected String getResourceSql(String fieldNames, String where, String order, Object[] values, String join)
	{
		if (order == null)
		{
			order = m_resourceTableName + "." + m_resourceTableSortField1
					+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2);
		}
		if (where == null) where = "";

		String sql = "select " + fieldNames + " from " + m_resourceTableName + ((join == null) ? "" : ("," + join))
				+ ((where.length() > 0) ? (" where " + where) : "") + " order by " + order;
		return sql;
	}

	/**
	 * Count all Resources matching a SQL where clause.
	 * 
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceeding "where ").
	 * @param values
	 *        The bind values
	 * @return The count of all Resources that meet the criteria.
	 */
	public int countSelectedResources(String where, Object[] values)
	{
		return countSelectedResources(where, values, null);
	}

	/**
	 * Count all Resources matching a SQL where clause.
	 * 
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceeding "where ").
	 * @param values
	 *        The bind values
	 * @param join
	 *        a single or comma separated set of other tables to join in the from clause
	 * @return The count of all Resources that meet the criteria.
	 */
	public int countSelectedResources(String where, Object[] values, String join)
	{
		// read all resources from the db with a where
		String sql = flatStorageSql.getSelectCount2Sql(m_resourceTableName, join, where);
		List results = m_sql.dbRead(sql, values, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					int count = result.getInt(1);
					return Integer.valueOf(count);
				}
				catch (SQLException ignore)
				{
					return null;
				}
			}
		});

		if (results.isEmpty()) return 0;

		return ((Integer) results.get(0)).intValue();
	}

	/**
	 * Get all Resources matching a SQL where clause.
	 * 
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceeding "where ".
	 * @param values
	 *        The bind values
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getSelectedResources(String where, Object[] values, int first, int last)
	{
		return getSelectedResources(where, null, values, first, last, null);
	}

	/**
	 * Get all Resources matching a SQL where clause.
	 * 
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceeding "where ".
	 * @param order
	 *        the SQL order clause (not including the preceeding "order by ").
	 * @param values
	 *        The bind values
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getSelectedResources(String where, String order, Object[] values, int first, int last)
	{
		return getSelectedResources(where, order, values, first, last, null);
	}

	/**
	 * Get all Resources matching an SQL where clause.
	 * 
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceeding "where ".
	 * @param order
	 *        the SQL order clause (not including the preceeding "order by ").
	 * @param values
	 *        The bind values
	 * @param first
	 *        the row number of the first record to include for pagination
	 * @param last
	 *        the row number of the last record to include for pagination
	 * @param join
	 *        a single or comma separated set of other tables to join in the from clause
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getSelectedResources(String where, String order, Object[] values, int first, int last, String join)
	{
		return getSelectedResources(where, order, values, first, last, join, m_reader);
	}

	/**
	 * Get all Resources matching an SQL where clause, with sorting and ordering, using a specialized reader.
	 *
	 * This is provided for specialized cases, where a known operation should make some optimizations or
	 * apply transformations to given fields during row retrieval. For example, reading CLOBs may be the
	 * default, but in some cases like long descriptions in simple search results, they are not relevant
	 * and could hurt performance. Note that specialized readers should have the same return type and
	 * expect the same indices as the default reader.
	 *
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceding "where ".
	 * @param order
	 *        the SQL order clause (not including the preceding "order by ").
	 * @param values
	 *        The bind values
	 * @param first
	 *        the number of the first sorted record to include for pagination
	 * @param last
	 *        the number of the last sorted record to include for pagination
	 * @param join
	 *        a single or comma separated set of other tables to join in the from clause
	 * @param reader
	 *        a specialized SqlReader for this request supplied to read each row differently than the default
	 * @return The list of all Resources that meet the criteria.
	 */
	protected List getSelectedResources(String where, String order, Object[] values, int first, int last, String join, SqlReader reader)
	{
		Object[] params = getPagedParameters(values, first, last);
		String sql = getResourceSql(where, order, values, first, last, join);
		List rv = m_sql.dbRead(sql, params, reader);

		return rv;
	}

	/**
	 * Get the SQL to retrieve all paged resources matching specified conditions.
	 *
	 * TODO: Push this down to FlatStorageSql
	 *
	 * See {@link #getSelectedResources(String where, String order, Object[] values, int first, int last, String join)} for parameter details.
	 */
	protected String getResourceSql(String where, String order, Object[] values, int first, int last, String join)
	{
		return getResourceSql(fieldList(m_resourceTableReadFields, null), where, order, values, first, last, join);
	}

	/**
	 * Get the SQL to retrieve all paged resources matching specified conditions.
	 *
	 * TODO: Push this down to FlatStorageSql
	 *
	 * See {@link #getSelectedResources(String where, String order, Object[] values, int first, int last, String join)} for parameter details.
	 */
	protected String getResourceSql(String fieldNames, String where, String order, Object[] values, int first, int last, String join)
	{
		String sql;

		if (order == null)
		{
			order = flatStorageSql.getOrder(m_resourceTableName, m_resourceTableSortField1, m_resourceTableSortField2);
		}

		if ("oracle".equals(m_sql.getVendor()))
		{
			sql = flatStorageSql.getSelectFieldsSql3(m_resourceTableName, fieldNames, m_resourceTableIdField,
					m_resourceTableSortField1, m_resourceTableSortField2, (first - 1), (last - first + 1), join, where, order);
		}
		else
		{
			sql = flatStorageSql.getSelectFieldsSql3(m_resourceTableName, fieldNames, null,
					m_resourceTableSortField1, m_resourceTableSortField2, (first - 1), (last - first + 1), join, where, order);
		}
		return sql;
	}

	/**
	 * Get the finalized parameter array for passing to a vendor-specific paged query (as from getResourceSql)
	 * to getSelectedResources.
	 *
	 * TODO: Push this down to FlatStorageSql
	 *
	 * @param values The parameter values to pass
	 * @param first The first record to include for pagination
	 * @param last The last record to include for pagination
	 * @return A new array with the original values copied in and pagination parameters in the correct places.
	 *
	 * @see #getResourceSql(String, String, String, Object[], int, int, String)
	 * @see #getSelectedResources(String, String, Object[], int, int, String)
	 */
	protected Object[] getPagedParameters(Object[] values, int first, int last)
	{
		Object[] fields;
		if ("oracle".equals(m_sql.getVendor()))
		{
			if (values != null)
			{
				fields = new Object[2 + values.length];
				System.arraycopy(values, 0, fields, 0, values.length);
			}
			else
			{
				fields = new Object[2];
			}
			fields[fields.length - 2] = Long.valueOf(last);
			fields[fields.length - 1] = Long.valueOf(first);
		}
		else
		{
			fields = values;
		}
		return fields;
	}

	/**
	 * Add a new Resource with this id.
	 * 
	 * @param id
	 *        The id.
	 * @param fields
	 *        The fields to write.
	 * @return The locked Resource object with this id, or null if the id is in use.
	 */
	public Edit putResource(String id, Object[] fields)
	{
		return putResource(null, id, fields);
	}

	/**
	 * putResource with optional connection to use.
	 * 
	 * @param conn
	 *        The optional database connection to use.
	 * @param id
	 *        The id.
	 * @param fields
	 *        The fields to write.
	 * @return The locked Resource object with this id, or null if the id is in use.
	 */
	public Edit putResource(Connection conn, String id, Object[] fields)
	{
		// process the insert
		boolean ok = insertResource(id, fields, conn);

		// if this failed, assume a key conflict (i.e. id in use)
		if (!ok) return null;

		// now get a lock on the record for edit
		Edit edit = editResource(conn, id);
		if (edit == null)
		{
			log.warn("putResource(): didn't get a lock!");
			return null;
		}

		return edit;
	}

	/**
	 * Add a new Resource with this id - no edit is returned, no lock is held.
	 * 
	 * @param id
	 *        The id.
	 * @param fields
	 *        The fields to write.
	 * @return True if successful, false if not.
	 */
	public boolean insertResource(String id, Object[] fields, Connection conn)
	{
		// for MSSQL, look at m_resourceTableInsertValues, and if any start with '(',
		// we need to process and store results since MSSQL doesn't support selects in the VALUES clause
		// bind values come from 'fields' array
		// store results in fieldOverrides
		// Note: MSSQL support removed in KNL-880

		// will be a copy of table's insert values, with overrides as necessary
		String[] overrideTableInsertValues = new String[m_resourceTableInsertValues.length];

		Object[] fieldOverrides = new Object[m_resourceTableInsertValues.length];

		for (int i = 0; i < m_resourceTableInsertValues.length; i++)
		{
			fieldOverrides[i] = fields[i];
			overrideTableInsertValues[i] = m_resourceTableInsertValues[i];
		}
		String statement = "insert into " + m_resourceTableName + "( " + fieldList(m_resourceTableInsertFields, m_resourceTableDbidField) + " )"
				+ " values ( " + valuesParams(overrideTableInsertValues, (m_resourceTableDbidField)) + " )";

		// process the insert
		boolean ok = m_sql.dbWrite(conn, statement, fields);
		return ok;
	} // putResource

	/**
	 * Get a lock on the Resource with this id, or null if a lock cannot be gotten.
	 * 
	 * @param id
	 *        The user id.
	 * @return The locked Resource with this id, or null if this records cannot be locked.
	 */
	public Edit editResource(String id)
	{
		return editResource(null, id);
	}

	/**
	 * Get a lock on the Resource with this id, or null if a lock cannot be gotten.
	 * 
	 * @param conn
	 *        The optional database connection to use.
	 * @param id
	 *        The user id.
	 * @return The locked Resource with this id, or null if this records cannot be locked.
	 */
	public Edit editResource(Connection conn, String id)
	{
		Edit edit = null;

		if (!m_locking)
		{
			return (Edit) getResource(conn, id);
		}

		// if the locks are in a separate table in the db
		if (m_locksAreInTable)
		{
			// read the record - fail if not there
			Entity entry = getResource(conn, id);
			if (entry == null) return null;

			// write a lock to the lock table - if we can do it, we get the lock
			String statement = flatStorageSql.getInsertLockSql();

			// we need session id and user id
			String sessionId = UsageSessionService.getSessionId();
			if (sessionId == null)
			{
				sessionId = "";
			}

			// collect the fields
			Object fields[] = new Object[4];
			fields[0] = m_resourceTableName;
			fields[1] = internalRecordId(caseId(id));
			fields[2] = TimeService.newTime();
			fields[3] = sessionId;

			// add the lock - if fails, someone else has the lock
			boolean ok = m_sql.dbWriteFailQuiet(null, statement, fields);
			if (!ok)
			{
				return null;
			}

			// we got the lock! - make the edit from the Resource
			edit = (Edit) entry;
		}

		// otherwise, get the lock locally
		else
		{
			// get the entry, and check for existence
			Entity entry = getResource(conn, id);
			if (entry == null) return null;

			// we only sync this getting - someone may release a lock out of sync
			synchronized (m_locks)
			{
				// if already locked
				if (m_locks.containsKey(entry.getReference())) return null;

				// make the edit from the Resource
				edit = (Edit) entry;

				// store the edit in the locks by reference
				m_locks.put(entry.getReference(), edit);
			}
		}

		return edit;
	}

	/**
	 * Commit the changes and release the lock.
	 * 
	 * @param edit
	 *        The Edit to commit.
	 * @param fields
	 *        The set of fields to write to the db, plus the id field as it is to be written again at the end.
	 */
	public void commitResource(Edit edit, Object fields[], ResourceProperties props)
	{
		commitResource(edit, fields, props, null);
	}

	/**
	 * Commit the changes and release the lock - optionally in a transaction.
	 * 
	 * @param edit
	 *        The Edit to commit.
	 * @param fields
	 *        The set of fields to write to the db, plus the id field as it is to be written again at the end.
	 * @param key
	 *        The object key used to relate to the properties - if null, we use the object id to relate.
	 */
	public void commitResource(Edit edit, Object fields[], ResourceProperties props, Object key)
	{
		// write out the properties
		writeProperties(edit, props, key);

		String statement = flatStorageSql.getUpdateSql(m_resourceTableName, updateSet(m_resourceTableUpdateFields), m_resourceTableIdField);
		// process the update
		m_sql.dbWrite(statement, updateFields(fields));

		if (m_locking)
		{
			if (m_locksAreInTable)
			{
				// remove the lock
				statement = flatStorageSql.getDeleteLockSql();

				// collect the fields
				Object lockFields[] = new Object[2];
				lockFields[0] = m_resourceTableName;
				lockFields[1] = internalRecordId(caseId(edit.getId()));
				boolean ok = m_sql.dbWrite(statement, lockFields);
				if (!ok)
				{
					log.warn("commit: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
				}
			}
			else
			{
				// remove the lock
				m_locks.remove(edit.getReference());
			}
		}
	}

	/**
	 * Cancel the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to cancel.
	 */
	public void cancelResource(Edit edit)
	{
		if (m_locking)
		{
			if (m_locksAreInTable)
			{
				// remove the lock
				String statement = flatStorageSql.getDeleteLockSql();

				// collect the fields
				Object lockFields[] = new Object[2];
				lockFields[0] = m_resourceTableName;
				lockFields[1] = internalRecordId(caseId(edit.getId()));
				boolean ok = m_sql.dbWrite(statement, lockFields);
				if (!ok)
				{
					log.warn("cancel: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
				}
			}
			else
			{
				// release the lock
				m_locks.remove(edit.getReference());
			}
		}
	}

	/**
	 * Remove this (locked) Resource.
	 * 
	 * @param user
	 *        The Edit to remove.
	 */
	public void removeResource(Edit edit)
	{
		removeResource(edit, null);
	}

	/**
	 * Remove this (locked) Resource.
	 * 
	 * @param edit
	 *        The Edit to remove.
	 * @param key
	 *        The key to relate resource to properties, of if null, id is assumed.
	 */
	public void removeResource(final Edit edit, final Object key)
	{
		// do this in a transaction
		m_sql.transact(new Runnable()
		{
			public void run()
			{
				removeResourceTx(edit, key);
			}
		}, "removeResource:" + edit.getId());
	}

	/**
	 * Transaction code to remove a resource.
	 */
	protected void removeResourceTx(Edit edit, Object key)
	{
		// remove the properties
		deleteProperties(edit, key);

		// form the SQL delete statement
		String statement = flatStorageSql.getDeleteSql(m_resourceTableName, m_resourceTableIdField);
		Object fields[] = new Object[1];
		fields[0] = caseId(edit.getId());

		// process the delete statement
		m_sql.dbWrite(statement, fields);

		if (m_locking)
		{
			if (m_locksAreInTable)
			{
				// remove the lock
				statement = flatStorageSql.getDeleteLockSql();

				// collect the fields
				Object lockFields[] = new Object[2];
				lockFields[0] = m_resourceTableName;
				lockFields[1] = internalRecordId(caseId(edit.getId()));
				boolean ok = m_sql.dbWrite(statement, lockFields);
				if (!ok)
				{
					log.warn("remove: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
				}
			}

			else
			{
				// release the lock
				m_locks.remove(edit.getReference());
			}
		}
	}

	/**
	 * Read in properties from the database - when the properties and the main table are related by the id
	 * 
	 * @param r
	 *        The resource for which properties are to be read.
	 * @param p
	 *        The properties object to fill.
	 */
	public void readProperties(Entity r, ResourcePropertiesEdit p)
	{
		readProperties(null, m_resourcePropertyTableName, m_resourceTableIdField, caseId(r.getId()), p);
	}

	/**
	 * Read in properties from the database - when the properties and the main table are related by the dbid
	 * 
	 * @param key
	 *        The resource key.
	 * @param p
	 *        The properties object to fill.
	 */
	public void readProperties(Integer dbid, ResourcePropertiesEdit p)
	{
		readProperties(null, m_resourcePropertyTableName, m_resourceTableDbidField, dbid, p);
	}

	/**
	 * Read in properties from the database - when the properties and the main table are related by the id
	 * 
	 * @param r
	 *        The resource for which properties are to be read.
	 * @param p
	 *        The properties object to fill.
	 */
	public void readProperties(Connection conn, Entity r, ResourcePropertiesEdit p)
	{
		readProperties(conn, m_resourcePropertyTableName, m_resourceTableIdField, caseId(r.getId()), p);
	}

	/**
	 * Read in properties from the database - when the properties and the main table are related by the dbid
	 * 
	 * @param key
	 *        The resource key.
	 * @param p
	 *        The properties object to fill.
	 */
	public void readProperties(Connection conn, Integer dbid, ResourcePropertiesEdit p)
	{
		readProperties(conn, m_resourcePropertyTableName, m_resourceTableDbidField, dbid, p);
	}

	/**
	 * Read in properties from the database.
	 * 
	 * @param r
	 *        The resource for which properties are to be read.
	 */
	public void readProperties(Connection conn, String table, String idField, Object id, ResourcePropertiesEdit p)
	{
		// if not properties table set, skip it
		if (table == null) return;

		// check we have an ID to lookup otherwise we get cross thread cache contamination.
		if (id == null) return;

		// the properties to fill in
		final ResourcePropertiesEdit props = p;

		Cache myCache = getCache(table);
		String cacheKey = table + ":" + idField + ":" + id;

		if ( myCache != null )
		{
			log.debug("CHECKING CACHE cacheKey={}", cacheKey);
			Object obj = myCache.get(cacheKey);
			if ( obj != null && obj instanceof ResourcePropertiesEdit ) 
			{
				// Clone the properties - do not return the real value
				ResourcePropertiesEdit re = (ResourcePropertiesEdit) obj;
				props.addAll(re);
				log.debug("CACHE HIT cacheKey={}", cacheKey);
				return;
			}
		}

		// get the properties from the db
		// ASSUME: NAME, VALUE for fields
		String sql = flatStorageSql.getSelectNameValueSql(table, idField);
		Object fields[] = new Object[1];
		fields[0] = id;
		m_sql.dbRead(conn, sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					// read the fields
					String name = result.getString(1);
					String value = result.getString(2);

					// add to props, if we got stuff from the fields
					if ((name != null) && (value != null))
					{
						props.addProperty(name, value);
					}

					// nothing to return
					return null;
				}
				catch (SQLException e)
				{
					log.warn("readProperties: " + e);
					return null;
				}
			}
		});

		if ( myCache != null )
		{
			// We don't want to put the returned value in the cache otherwise the
			// caller may changes it the copy in the cache is updated too, even if
			// the caller's changed copy is never persisted into the database.
			ResourcePropertiesEdit cacheCopy = new BaseResourcePropertiesEdit();
			cacheCopy.addAll(props);
			myCache.put(cacheKey,cacheCopy);
		}
	}

	/**
	 * Read in properties from the database.
	 * 
	 * @param r
	 *        The resource for which properties are to be read.
	 */
	public void readProperties(Connection conn, String table, String idField, Object id, Properties p)
	{
		// if not properties table set, skip it
		if (table == null) return;
		
		// if id is null then we won't be able to load/cache anything.
		if (id == null) return;

		// the properties to fill in
		final Properties props = p;

		// get the properties from the db
		// ASSUME: NAME, VALUE for fields
		String sql = flatStorageSql.getSelectNameValueSql(table, idField);
		Object fields[] = new Object[1];
		fields[0] = id;
		m_sql.dbRead(conn, sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					// read the fields
					String name = result.getString(1);
					String value = result.getString(2);

					// add to props, if we got stuff from the fields
					if ((name != null) && (value != null))
					{
						props.setProperty(name, value);
					}

					// nothing to return
					return null;
				}
				catch (SQLException e)
				{
					log.warn("readProperties: " + e);
					return null;
				}
			}
		});
	}

	/**
	 * Replace any properties for this resource with the resource's current set of properties.
	 * 
	 * @param conn
	 *        optional database connection to use.
	 * @param r
	 *        The resource for which properties are to be written.
	 * @param props
	 *        The properties to write.
	 */
	public void writeProperties(Entity r, ResourceProperties props)
	{
		writeProperties(m_resourcePropertyTableName, m_resourceTableIdField, caseId(r.getId()), null, null, props);
	}

	/**
	 * Replace any properties for this resource with the resource's current set of properties.
	 * 
	 * @param conn
	 *        optional database connection to use.
	 * @param r
	 *        The resource for which properties are to be written.
	 * @param props
	 *        The properties to write.
	 * @param key
	 *        The key used to relate the props to the resource.
	 */
	public void writeProperties(Entity r, ResourceProperties props, Object key)
	{
		if (key == null)
		{
			writeProperties(m_resourcePropertyTableName, m_resourceTableIdField, caseId(r.getId()), null, null, props);
		}
		else
		{
			writeProperties(m_resourcePropertyTableName, m_resourceTableDbidField, key, null, null, props);
		}
	}

	public void writeProperties(String table, String idField, Object id, String extraIdField, String extraId, ResourceProperties props)
	{
		boolean deleteFirst = true;
		writeProperties(table, idField, id, extraIdField, extraId, props, deleteFirst);
	}

	/**
	 * Replace any properties for this resource with the resource's current set of properties.
	 * 
	 * @param r
	 *        The resource for which properties are to be written.
	 */
	public void writeProperties(final String table, final String idField, final Object id, final String extraIdField, final String extraId,
			final ResourceProperties props, final boolean deleteFirst)
	{
		// if not properties table set, skip it
		if (table == null) return;
		if (props == null) return;

		Cache myCache = getCache(table);
		String cacheKey = table + ":" + idField + ":" + id;
		if ( myCache != null )
		{
			log.debug("CACHE REMOVE cacheKey={} cache={}", cacheKey, myCache);
			myCache.remove(cacheKey);
		}

		// do this in a transaction
		m_sql.transact(new Runnable()
		{
			public void run()
			{
				writePropertiesTx(table, idField, id, extraIdField, extraId, props, deleteFirst);
			}
		}, "writeProperties:" + id);
	}

	/**
	 * The transaction code that writes the properties.
	 * 
	 * @param r
	 *        The resource for which properties are to be written.
	 */
	protected void writePropertiesTx(String table, String idField, Object id, String extraIdField, String extraId, ResourceProperties props,
			boolean deleteFirst)
	{
		String statement;
		Object fields[];

		// if (true)
		if (deleteFirst)
		{
			// delete what's there
			statement = flatStorageSql.getDeleteSql(table, idField);
			fields = new Object[1];
			fields[0] = id;

			// process the delete statement
			m_sql.dbWrite(statement, fields);
		}

		// the SQL statement
		statement = flatStorageSql.getInsertSql(table, idField, extraIdField);
		fields = new Object[((extraIdField != null) ? 4 : 3)];
		fields[0] = id;

		// process each property
		for (Iterator i = props.getPropertyNames(); i.hasNext();)
		{
			String name = (String) i.next();
			String value = props.getProperty(name);

			fields[1] = name;
			fields[2] = value;

			if (extraIdField != null)
			{
				fields[3] = extraId;
			}

			// The value might be null if it's a list of values.
			// TODO support persisting to the database lists of values.
			// dont write it if there's only an empty string for value
			if (value != null && value.length() > 0)
			{
				m_sql.dbWrite(statement, fields);
			}
		}
	}

	/**
	 * Replace any properties for this resource with the resource's current set of properties.
	 * 
	 * @param r
	 *        The resource for which properties are to be written.
	 */
	public void writeProperties(String table, String idField, Object id, String extraIdField, String extraId, Properties props)
	{
		boolean deleteFirst = true;
		writeProperties(table, idField, id, extraIdField, extraId, props, deleteFirst);
	}

	/**
	 * Replace any properties for this resource with the resource's current set of properties.
	 * 
	 * @param r
	 *        The resource for which properties are to be written.
	 */
	public void writeProperties(final String table, final String idField, final Object id, final String extraIdField, final String extraId,
			final Properties props, final boolean deleteFirst)
	{
		// if not properties table set, skip it
		if (table == null) return;
		if (props == null) return;

		// do this in a transaction
		m_sql.transact(new Runnable()
		{
			public void run()
			{
				writePropertiesTx(table, idField, id, extraIdField, extraId, props, deleteFirst);
			}
		}, "writeProperties:" + id);

	}

	/**
	 * The transaction code for writing properties.
	 * 
	 * @param r
	 *        The resource for which properties are to be written.
	 */
	protected void writePropertiesTx(String table, String idField, Object id, String extraIdField, String extraId, Properties props,
			boolean deleteFirst)
	{
		String statement;
		Object[] fields;

		if (deleteFirst)
		{
			// delete what's there
			statement = flatStorageSql.getDeleteSql(table, idField);
			fields = new Object[1];
			fields[0] = id;

			// process the delete statement
			m_sql.dbWrite(statement, fields);
		}

		// the SQL statement
		statement = flatStorageSql.getInsertSql(table, idField, extraIdField);
		fields = new Object[((extraIdField != null) ? 4 : 3)];
		fields[0] = id;

		// process each property
		for (Enumeration i = props.propertyNames(); i.hasMoreElements();)
		{
			String name = (String) i.nextElement();
			String value = props.getProperty(name);

			fields[1] = name;
			fields[2] = value;

			if (extraIdField != null)
			{
				fields[3] = extraId;
			}

			// don't write it if there's only an empty string for value
			if (!StringUtils.isEmpty(value))
			{
				m_sql.dbWrite(statement, fields);
			}
		}
	}

	/**
	 * Remove all properties for this resource from the db.
	 * 
	 * @param r
	 *        The resource for which properties are to be deleted.
	 */
	protected void deleteProperties(Entity r, Object key)
	{
		String idField = m_resourceTableIdField;
		if (key != null)
		{
			idField = m_resourceTableDbidField;
		}

		// if not properties table set, skip it
		if (m_resourcePropertyTableName == null) return;

		// form the SQL delete statement
		String statement = flatStorageSql.getDeleteSql(m_resourcePropertyTableName, idField);
		Object fields[] = new Object[1];
		fields[0] = key == null ? caseId(r.getId()) : key;

		// process the delete statement
		m_sql.dbWrite(statement, fields);
	}

	/**
	 * Form a string of n question marks with commas, for sql value statements, one for each item in the values array, or an empty string if null. If
	 * the fields are "(...)" values, use these instead of ?.
	 * 
	 * @param values
	 *        The values to be inserted into the sql statement.
	 * @return A sql statement fragment for the values part of an insert, one for each value in the array.
	 */
	protected String valuesParams(String[] fields, String dbidField)
	{
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < fields.length - 1; i++)
		{
			if (fields[i].startsWith("("))
			{
				buf.append(fields[i]);
			}
			else
			{
				buf.append("?");
			}
			buf.append(",");
		}

		// for the last field
		if (fields[fields.length - 1].startsWith("("))
		{
			buf.append(fields[fields.length - 1]);
		}
		else
		{
			buf.append("?");
		}

		if (dbidField != null) buf.append(flatStorageSql.getIdField(m_resourceTableName));

		return buf.toString();
	}

	/**
	 * Form a string of n name=?, for sql update set statements, one for each item in the values array, or an empty string if null.
	 * 
	 * @param values
	 *        The values to be inserted into the sql statement.
	 * @return A sql statement fragment for the values part of an insert, one for each value in the array.
	 */
	protected String updateSet(String[] fields)
	{
		StringBuilder buf = new StringBuilder();

		// we assume the first field is the primary key, and we don't want to include that in the update, so start at 1
		for (int i = 1; i < fields.length; i++)
		{
			buf.append(fields[i]);

			// if the "field" contains an equals, assume it's a complete field=value statement, else add an =?
			if (fields[i].indexOf("=") == -1)
			{
				buf.append(" = ?");
			}
			buf.append(",");
		}

		// take off the last comma
		buf.setLength(buf.length() - 1);

		return buf.toString();
	}

	/**
	 * For update, we don't want to include the first, primary key, field, so strip it off
	 * 
	 * @param fields
	 *        The full set of fields
	 * @return The fields with the first removed
	 */
	protected Object[] updateFields(Object[] fields)
	{
		if (fields == null) return null;

		Object updateFields[] = new Object[fields.length - 1];

		System.arraycopy(fields, 1, updateFields, 0, updateFields.length);

		return updateFields;
	}

	/**
	 * Form a string of field, field, field - one for each item in the fields array.
	 * 
	 * @param fields
	 *        The field names.
	 * @return A string of field, field, field - one for each item in the fields array.
	 */
	protected String fieldList(String[] fields, String dbidField)
	{
		StringBuilder buf = new StringBuilder();

		for (int i = 0; i < fields.length - 1; i++)
		{
			buf.append(qualifyField(fields[i], m_resourceTableName) + ",");
		}

		buf.append(qualifyField(fields[fields.length - 1], m_resourceTableName));

		if (dbidField != null)
		{
			if (!"mysql".equals(m_sql.getVendor()))
			{
				// MySQL doesn't need this field, but oracle and HSQLDB do
				buf.append("," + qualifyField(dbidField, m_resourceTableName));
			}
		}
		return buf.toString();
	}

	/**
	 * Qualify the field with the table name, if it's a field.
	 * 
	 * @param field
	 *        The field.
	 * @param table
	 *        The table name.
	 * @return The field name qualified with the table name.
	 */
	protected String qualifyField(String field, String table)
	{
		// if it's not a field but a sub-select, don't qualify
		// if its hsqldb don't qualify, change from 1.8 to 2.x
		if (field.startsWith("(") || "hsqldb".equals(m_sql.getVendor()))
		{
			return field;
		}
		else
		{
			return table + "." + field;
		}
	}

	/**
	 * Fix the case of resource ids to support case insensitive ids if enabled
	 * 
	 * @param The
	 *        id to fix.
	 * @return The id, case modified as needed.
	 */
	protected String caseId(String id)
	{
		if (m_caseInsensitive)
		{
			return id.toLowerCase();
		}

		return id;
	}

	/**
	 * Enable / disable case insensitive ids.
	 * 
	 * @param setting
	 *        true to set case insensitivity, false to set case sensitivity.
	 */
	protected void setCaseInsensitivity(boolean setting)
	{
		m_caseInsensitive = setting;
	}

	/**
	 * Return a record ID to use internally in the database. This is needed for databases (MySQL) that have limits on key lengths. The hash code
	 * ensures that the record ID will be unique, even if the DB only considers a prefix of a very long record ID.
	 * 
	 * @param recordId
	 * @return The record ID to use internally in the database
	 */
	private String internalRecordId(String recordId)
	{
		return flatStorageSql.getRecordId(recordId);
	}
}
