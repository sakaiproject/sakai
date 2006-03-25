/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

// package
package org.sakaiproject.util.storage;

// import
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.sakaiproject.service.framework.log.cover.Logger;
import org.sakaiproject.service.framework.session.cover.UsageSessionService;
import org.sakaiproject.service.framework.sql.SqlReader;
import org.sakaiproject.service.framework.sql.SqlService;
import org.sakaiproject.service.legacy.entity.Edit;
import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.entity.ResourcePropertiesEdit;
import org.sakaiproject.service.legacy.time.cover.TimeService;

/**
 * <p>
 * BaseDbFlatStorage is a class that stores Resources (of some type) in a database, provides (optional) locked access, and generally implements a services "storage" class. The service's storage class can extend this to provide covers to turn Resource and Edit into
 * something more type specific to the service.
 * </p>
 * Note: the methods here are all "id" based, with the following assumptions: - just the Resource Id field is enough to distinguish one Resource from another - a resource's reference is based on no more than the resource id - a resource's id cannot
 * change. In order to handle Unicode characters properly, the SQL statements executed by this class should not embed Unicode characters into the SQL statement text; rather, Unicode values should be inserted as fields in a PreparedStatement. Databases
 * handle Unicode better in fields.
 * 
 * @author Sakai Software Development Team
 */
public class BaseDbFlatStorage
{
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

	/** The full set of value expressions for an insert - either null or ? or an expression - to match m_resourceTableInsertFields (not counting a dbid field). */
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
	public BaseDbFlatStorage(String resourceTableName, String resourceTableIdField, String[] resourceTableFields,
			String propertyTableName, boolean locksInTable, SqlReader reader, SqlService sqlService)
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

	} // BaseDbSingleStorage

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

	} // open

	/**
	 * Close.
	 */
	public void close()
	{
		if (!m_locks.isEmpty())
		{
			Logger.warn(this + ".close(): locks remain!");
			// %%%
		}
		m_locks.clear();
		m_locks = null;

	} // close

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
		String sql = "select " + m_resourceTableIdField + " from " + m_resourceTableName + " where ( " + m_resourceTableIdField
				+ " = ? )";

		Object fields[] = new Object[1];
		fields[0] = caseId(id);
		List ids = m_sql.dbRead(sql, fields, null);

		return (!ids.isEmpty());

	} // check

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
		String sql = "select " + fieldList(m_resourceTableReadFields, null) + " from " + m_resourceTableName + " where ( "
				+ m_resourceTableIdField + " = ? )";

		Object fields[] = new Object[1];
		fields[0] = caseId(id);
		List rv = m_sql.dbRead(conn, sql, fields, m_reader);

		if ((rv != null) && (rv.size() > 0))
		{
			entry = (Entity) rv.get(0);
		}

		return entry;

	} // getResource

	public List getAllResources()
	{
		// read all resources from the db
		String sql = "select " + fieldList(m_resourceTableReadFields, null) + " from " + m_resourceTableName;

		List rv = m_sql.dbRead(sql, null, m_reader);

		return rv;

	} // getAllResources

	public int countAllResources()
	{
		List all = new Vector();

		// read all count
		String sql = "select count(1) from " + m_resourceTableName;

		List results = m_sql.dbRead(sql, null, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					int count = result.getInt(1);
					return new Integer(count);
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
			sql = "select " + /* fieldList(m_resourceTableReadFields, null) */"*" + " from" + " (select "
					+ fieldList(m_resourceTableReadFields, null) + " ,RANK() OVER" + " (order by " + m_resourceTableName + "."
					+ m_resourceTableSortField1
					+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2) + ","
					+ m_resourceTableName + "." + m_resourceTableIdField + ") as rank" + " from " + m_resourceTableName
					+ " order by " + m_resourceTableName + "." + m_resourceTableSortField1
					+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2) + ","
					+ m_resourceTableName + "." + m_resourceTableIdField + " )" + " where rank between ? and ?";
			fields = new Object[2];
			fields[0] = new Long(first);
			fields[1] = new Long(last);
		}
		else if ("mysql".equals(m_sql.getVendor()))
		{
			// use MySQL LIMIT clause
			sql = "select " + fieldList(m_resourceTableReadFields, null) + " from " + m_resourceTableName + " order by "
					+ m_resourceTableName + "." + m_resourceTableSortField1
					+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2)
					+ " limit " + (last - first + 1) + " offset " + (first - 1);
		}
		else
		// if ("hsqldb".equals(m_sql.getVendor()))
		{
			// use SQL2000 clause
			sql = "select " + "limit " + (first - 1) + " " + (last - first + 1) + " " + fieldList(m_resourceTableReadFields, null)
					+ " from " + m_resourceTableName + " order by " + m_resourceTableName + "." + m_resourceTableSortField1
					+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2);
		}

		List rv = m_sql.dbRead(sql, fields, m_reader);

		return rv;

	} // getAllResources

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
		if (order == null)
		{
			order = m_resourceTableName + "." + m_resourceTableSortField1
					+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2);
		}
      if(where == null)
         where = "";

		// read all resources from the db with a where
		String sql = "select " + fieldList(m_resourceTableReadFields, null) + " from " + m_resourceTableName
				+ ((join == null) ? "" : ("," + join)) + ((where.length() > 0)? (" where " + where) : "") + " order by " + order;

		List all = m_sql.dbRead(sql, values, m_reader);

		return all;
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
		String sql = "select count(1) from " + m_resourceTableName + ((join == null) ? "" : ("," + join)) + (((where != null) && (where.length() > 0)) ? (" where " + where) : "");

		List results = m_sql.dbRead(sql, values, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					int count = result.getInt(1);
					return new Integer(count);
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
	 * Get all Resources matching a SQL where clause.
	 * 
	 * @param where
	 *        The SQL where clause with bind variables indicated (not including the preceeding "where ".
	 * @param order
	 *        the SQL order clause (not including the preceeding "order by ").
	 * @param values
	 *        The bind values
	 * @param join
	 *        a single or comma separated set of other tables to join in the from clause
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getSelectedResources(String where, String order, Object[] values, int first, int last, String join)
	{
		Object[] fields;
		String sql;

		if (order == null)
		{
			order = m_resourceTableName + "." + m_resourceTableSortField1
					+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2);
		}

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

			// use Oracle RANK function, adding the id field to the order to assure a unique ranking
			sql = "select " + /* fieldList(m_resourceTableReadFields, null) */"*" + " from" + " (select "
					+ fieldList(m_resourceTableReadFields, null) + " ,RANK() OVER" + " (order by " + order + ","
					+ m_resourceTableName + "." + m_resourceTableIdField + ") as rank" + " from " + m_resourceTableName
					+ ((join == null) ? "" : ("," + join)) + (((where != null) && (where.length() > 0)) ? (" where " + where) : "")
					+ " order by " + order + "," + m_resourceTableName
					+ "." + m_resourceTableIdField + " )" + " where rank between ? and ?";
			fields[fields.length - 2] = new Long(first);
			fields[fields.length - 1] = new Long(last);
		}
		else if ("mysql".equals(m_sql.getVendor()))
		{
			fields = values;
			// use MySQL LIMIT clause
			sql = "select " + fieldList(m_resourceTableReadFields, null) + " from " + m_resourceTableName
					+ ((join == null) ? "" : ("," + join)) + (((where != null) && (where.length() > 0)) ? (" where " + where) : "")
					+ " order by " + order + "," + m_resourceTableName
					+ "." + m_resourceTableSortField1
					+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2)
					+ " limit " + (last - first + 1) + " offset " + (first - 1);
		}
		else
		// if ("hsqldb".equals(m_sql.getVendor()))
		{
			// use SQL2000 LIMIT clause
			fields = values;
			sql = "select " + "limit " + (first - 1) + " " + (last - first + 1) + " " + fieldList(m_resourceTableReadFields, null)
					+ " from " + m_resourceTableName + ((join == null) ? "" : ("," + join)) + (((where != null) && (where.length() > 0)) ? (" where " + where) : "") + " order by "
					+ order + "," + m_resourceTableName + "." + m_resourceTableSortField1
					+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2);
		}

		List rv = m_sql.dbRead(sql, fields, m_reader);

		return rv;
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
			Logger.warn(this + ".putResource(): didn't get a lock!");
			return null;
		}

		return edit;

	} // putResource

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
		String statement = "insert into " + m_resourceTableName + "( "
				+ fieldList(m_resourceTableInsertFields, m_resourceTableDbidField) + " )" + " values ( "
				+ valuesParams(m_resourceTableInsertValues, (m_resourceTableDbidField)) + " )";

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
			String statement = "insert into SAKAI_LOCKS" + " (TABLE_NAME,RECORD_ID,LOCK_TIME,USAGE_SESSION_ID)"
					+ " values (?, ?, ?, ?)";

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

	} // editResource

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
		commitResource(null, edit, fields, props, null);
	}

	/**
	 * Commit the changes and release the lock - optionally in a transaction.
	 * 
	 * @param conn
	 *        The Db Connection of the transaction (optional).
	 * @param edit
	 *        The Edit to commit.
	 * @param fields
	 *        The set of fields to write to the db, plus the id field as it is to be written again at the end.
	 * @param key
	 *        The object key used to relate to the properties - if null, we use the object id to relate.
	 */
	public void commitResource(Connection conn, Edit edit, Object fields[], ResourceProperties props, Object key)
	{
		// write out the properties
		writeProperties(conn, edit, props, key);

		String statement = "update " + m_resourceTableName + " set " + updateSet(m_resourceTableUpdateFields) + " where ( "
				+ m_resourceTableIdField + " = ? )";

		// process the update
		m_sql.dbWrite(conn, statement, updateFields(fields));

		if (m_locking)
		{
			if (m_locksAreInTable)
			{
				// remove the lock
				statement = "delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";
	
				// collect the fields
				Object lockFields[] = new Object[2];
				lockFields[0] = m_resourceTableName;
				lockFields[1] = internalRecordId(caseId(edit.getId()));
				boolean ok = m_sql.dbWrite(conn, statement, lockFields);
				if (!ok)
				{
					Logger.warn(this + ".commit: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
				}
			}
	
			else
			{	
				// remove the lock
				m_locks.remove(edit.getReference());
			}
		}

	} // commitResource

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
				String statement = "delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";
	
				// collect the fields
				Object lockFields[] = new Object[2];
				lockFields[0] = m_resourceTableName;
				lockFields[1] = internalRecordId(caseId(edit.getId()));
				boolean ok = m_sql.dbWrite(statement, lockFields);
				if (!ok)
				{
					Logger.warn(this + ".cancel: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
				}
			}
	
			else
			{
				// release the lock
				m_locks.remove(edit.getReference());
			}
		}

	} // cancelResource

	/**
	 * Remove this (locked) Resource.
	 * 
	 * @param user
	 *        The Edit to remove.
	 */
	public void removeResource(Edit edit)
	{
		removeResource(null, edit, null);
	}

	/**
	 * Remove this (locked) Resource.
	 * 
	 * @param conn
	 *        Optional db connection to use.
	 * @param edit
	 *        The Edit to remove.
	 * @param key
	 *        The key to relate resource to properties, of if null, id is assumed.
	 */
	public void removeResource(Connection conn, Edit edit, Object key)
	{
		// remove the properties
		deleteProperties(conn, edit, key);

		// form the SQL delete statement
		String statement = "delete from " + m_resourceTableName + " where ( " + m_resourceTableIdField + " = ? )";

		Object fields[] = new Object[1];
		fields[0] = caseId(edit.getId());

		// process the delete statement
		m_sql.dbWrite(conn, statement, fields);

		if (m_locking)
		{
			if (m_locksAreInTable)
			{
				// remove the lock
				statement = "delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";
	
				// collect the fields
				Object lockFields[] = new Object[2];
				lockFields[0] = m_resourceTableName;
				lockFields[1] = internalRecordId(caseId(edit.getId()));
				boolean ok = m_sql.dbWrite(conn, statement, lockFields);
				if (!ok)
				{
					Logger.warn(this + ".remove: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
				}
			}
	
			else
			{
				// release the lock
				m_locks.remove(edit.getReference());
			}
		}

	} // removeResource

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

		// the properties to fill in
		final ResourcePropertiesEdit props = p;

		// get the properties from the db
		// ASSUME: NAME, VALUE for fields
		String sql = "select NAME, VALUE from " + table + " where ( " + idField + " = ? )";

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
					Logger.warn(this + ".readProperties: " + e);
					return null;
				}
			}
		});
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

		// the properties to fill in
		final Properties props = p;

		// get the properties from the db
		// ASSUME: NAME, VALUE for fields
		String sql = "select NAME, VALUE from " + table + " where ( " + idField + " = ? )";

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
					Logger.warn(this + ".readProperties: " + e);
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
	public void writeProperties(Connection conn, Entity r, ResourceProperties props)
	{
		writeProperties(conn, m_resourcePropertyTableName, m_resourceTableIdField, caseId(r.getId()), null, null, props);
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
	public void writeProperties(Connection conn, Entity r, ResourceProperties props, Object key)
	{
		if (key == null)
		{
			writeProperties(conn, m_resourcePropertyTableName, m_resourceTableIdField, caseId(r.getId()), null, null, props);
		}
		else
		{
			writeProperties(conn, m_resourcePropertyTableName, m_resourceTableDbidField, key, null, null, props);
		}
	}

	public void writeProperties(Connection conn, String table, String idField, Object id, String extraIdField, String extraId,
			ResourceProperties props)
	{
		boolean deleteFirst = true;
		writeProperties(conn, table, idField, id, extraIdField, extraId, props, deleteFirst);
	}

	/**
	 * Replace any properties for this resource with the resource's current set of properties.
	 * 
	 * @param r
	 *        The resource for which properties are to be written.
	 */
	public void writeProperties(Connection conn, String table, String idField, Object id, String extraIdField, String extraId,
			ResourceProperties props, boolean deleteFirst)
	{
		// if not properties table set, skip it
		if (table == null) return;
		if (props == null) return;

		// do this in a transaction
		Connection connection = conn;
		boolean wasCommit = true;
		try
		{
			if (conn == null)
			{
				connection = m_sql.borrowConnection();
				wasCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
			}

			String statement;
			Object fields[];

			// if (true)
			if (deleteFirst)
			{
				// delete what's there
				statement = "delete from " + table + " where ( " + idField + " = ? )";

				fields = new Object[1];
				fields[0] = id;

				// process the delete statement
				m_sql.dbWrite(connection, statement, fields);
			}

			// the SQL statement
			statement = "insert into " + table + "( " + idField + ", NAME, VALUE"
					+ ((extraIdField != null) ? (", " + extraIdField) : "") + " ) values (?,?,?"
					+ ((extraIdField != null) ? ",?" : "") + ")";

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

				// dont write it if there's only an empty string for value
				if (value.length() > 0)
				{
					m_sql.dbWrite(connection, statement, fields);
				}
			}

			// end the transaction
			if (conn == null)
			{
				connection.commit();
			}
		}
		catch (Exception e)
		{
			if (connection != null)
			{
				try
				{
					if (conn == null)
					{
						connection.rollback();
					}
				}
				catch (Exception ee)
				{
					Logger.warn(this + ".writeProperties, while rolling back: " + ee);
				}
			}
			Logger.warn(this + ".writeProperties: " + e);
		}
		finally
		{
			if (connection != null)
			{
				if (conn == null)
				{
					try
					{
						connection.setAutoCommit(wasCommit);
					}
					catch (Exception e)
					{
						Logger.warn(this + ".writeProperties, while setting auto commit: " + e);
					}
					m_sql.returnConnection(connection);
				}
			}
		}
	}

	/**
	 * Replace any properties for this resource with the resource's current set of properties.
	 * 
	 * @param r
	 *        The resource for which properties are to be written.
	 */
	public void writeProperties(Connection conn, String table, String idField, Object id, String extraIdField, String extraId,
			Properties props)
	{
		boolean deleteFirst = true;
		writeProperties(conn, table, idField, id, extraIdField, extraId, props, deleteFirst);
	}

	/**
	 * Replace any properties for this resource with the resource's current set of properties.
	 * 
	 * @param r
	 *        The resource for which properties are to be written.
	 */
	public void writeProperties(Connection conn, String table, String idField, Object id, String extraIdField, String extraId,
			Properties props, boolean deleteFirst)
	{
		// if not properties table set, skip it
		if (table == null) return;
		if (props == null) return;

		// do this in a transaction
		Connection connection = conn;
		boolean wasCommit = true;
		try
		{
			if (conn == null)
			{
				connection = m_sql.borrowConnection();
				wasCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
			}

			String statement;
			Object[] fields;

			if (deleteFirst)
			{
				// delete what's there
				statement = "delete from " + table + " where ( " + idField + " = ? )";

				fields = new Object[1];
				fields[0] = id;

				// process the delete statement
				m_sql.dbWrite(connection, statement, fields);
			}

			// the SQL statement
			statement = "insert into " + table + "( " + idField + ", NAME, VALUE"
					+ ((extraIdField != null) ? (", " + extraIdField) : "") + " ) values (?,?,?"
					+ ((extraIdField != null) ? ",?" : "") + ")";

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

				// dont write it if there's only an empty string for value
				if (value.length() > 0)
				{
					m_sql.dbWrite(connection, statement, fields);
				}
			}

			// end the transaction
			if (conn == null)
			{
				connection.commit();
			}
		}
		catch (Exception e)
		{
			if (connection != null)
			{
				try
				{
					if (conn == null)
					{
						connection.rollback();
					}
				}
				catch (Exception ee)
				{
					Logger.warn(this + ".writeProperties, while rolling back: " + ee);
				}
			}
			Logger.warn(this + ".writeProperties: " + e);
		}
		finally
		{
			if (connection != null)
			{
				if (conn == null)
				{
					try
					{
						connection.setAutoCommit(wasCommit);
					}
					catch (Exception e)
					{
						Logger.warn(this + ".writeProperties, while setting auto commit: " + e);
					}
					m_sql.returnConnection(connection);
				}
			}
		}
	}

	/**
	 * Remove all properties for this resource from the db.
	 * 
	 * @param conn
	 *        Optional db connection to use.
	 * @param r
	 *        The resource for which properties are to be deleted.
	 */
	protected void deleteProperties(Connection conn, Entity r, Object key)
	{
		String idField = m_resourceTableIdField;
		if (key != null)
		{
			idField = m_resourceTableDbidField;
		}

		// if not properties table set, skip it
		if (m_resourcePropertyTableName == null) return;

		// form the SQL delete statement
		String statement = "delete from " + m_resourcePropertyTableName + " where ( " + idField + " = ? )";

		Object fields[] = new Object[1];
		fields[0] = key == null ? caseId(r.getId()) : key;

		// process the delete statement
		m_sql.dbWrite(conn, statement, fields);
	}

	/**
	 * Form a string of n question marks with commas, for sql value statements, one for each item in the values array, or an empty string if null. If the fields are "(...)" values, use these instead of ?.
	 * 
	 * @param values
	 *        The values to be inserted into the sql statement.
	 * @return A sql statement fragment for the values part of an insert, one for each value in the array.
	 */
	protected String valuesParams(String[] fields, String dbidField)
	{
		StringBuffer buf = new StringBuffer();
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

		if (dbidField != null)
		{
			if ("oracle".equals(m_sql.getVendor()))
			{
				// insert the sequence next value based on the table name value for a dbid field
				buf.append(",");
				buf.append(m_resourceTableName);
				buf.append("_SEQ.NEXTVAL");
			}
			else if ("mysql".equals(m_sql.getVendor()))
			{
				// for mysql, the field will auto increment as part of the schema...
			}
			else
			// if ("hsqldb".equals(m_sql.getVendor()))
			{
				// insert the sequence next value based on the table name value for a dbid field
				buf.append(", NEXT VALUE FOR ");
				buf.append(m_resourceTableName);
				buf.append("_SEQ");
			}
		}

		return buf.toString();

	} // valuesParams

	/**
	 * Form a string of n name=?, for sql update set statements, one for each item in the values array, or an empty string if null.
	 * 
	 * @param values
	 *        The values to be inserted into the sql statement.
	 * @return A sql statement fragment for the values part of an insert, one for each value in the array.
	 */
	protected String updateSet(String[] fields)
	{
		StringBuffer buf = new StringBuffer();

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

	} // updateSet

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
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < fields.length - 1; i++)
		{
			buf.append(qualifyField(fields[i], m_resourceTableName) + ",");
		}

		buf.append(qualifyField(fields[fields.length - 1], m_resourceTableName));

		if (dbidField != null)
		{
			if (!"mysql".equals(m_sql.getVendor()))
			{
				// MySQL doesn't need this field, but Oracle and HSQLDB do
				buf.append("," + qualifyField(dbidField, m_resourceTableName));
			}
		}

		return buf.toString();

	} // fieldList

	/**
	 * Qualify the fiel with the table name, if it's a field.
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
		if (field.startsWith("("))
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

	} // caseId

	/**
	 * Enable / disable case insensitive ids.
	 * 
	 * @param setting
	 *        true to set case insensitivity, false to set case sensitivity.
	 */
	protected void setCaseInsensitivity(boolean setting)
	{
		m_caseInsensitive = setting;

	} // setCaseInsensitivity

	/**
	 * Return a record ID to use internally in the database. This is needed for databases (MySQL) that have limits on key lengths. The hash code ensures that the record ID will be unique, even if the DB only considers a prefix of a very long record ID.
	 * 
	 * @param recordId
	 * @return The record ID to use internally in the database
	 */
	private String internalRecordId(String recordId)
	{
		if ("mysql".equals(m_sql.getVendor()))
		{
			if (recordId == null) recordId = "null";
			return recordId.hashCode() + " - " + recordId;
		}
		else
		// oracle, hsqldb
		{
			return recordId;
		}
	}

} // BaseDbSingleStorage

