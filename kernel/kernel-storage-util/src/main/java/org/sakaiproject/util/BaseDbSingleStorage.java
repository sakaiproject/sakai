/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/BaseDbSingleStorage.java $
 * $Id: BaseDbSingleStorage.java 82134 2010-09-07 21:52:06Z aaronz@vt.edu $
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.time.cover.TimeService;

/**
 * Single Storage provides persisting of just resources(no properties, no container).
 * <p>
 * BaseDbSingleStorage is a class that stores Resources (of some type) in a database, <br />
 * provides locked access, and generally implements a services "storage" class. The <br />
 * service's storage class can extend this to provide covers to turn Resource and <br />
 * Edit into something more type specific to the service.
 * </p>
 * <p>
 * Note: the methods here are all "id" based, with the following assumptions: <br /> - just the Resource Id field is enough to distinguish one
 * Resource from another <br /> - a resource's reference is based on no more than the resource id <br /> - a resource's id cannot change.
 * </p>
 * <p>
 * In order to handle Unicode characters properly, the SQL statements executed by this class <br />
 * should not embed Unicode characters into the SQL statement text; rather, Unicode values <br />
 * should be inserted as fields in a PreparedStatement. Databases handle Unicode better in fields.
 * </p>
 */
@Slf4j
public class BaseDbSingleStorage implements DbSingleStorage 
{
	public static final String STORAGE_FIELDS = "XML";

	/** Table name for resource records. */
	protected String m_resourceTableName = null;

	/** The field in the resource table that holds the resource id. */
	protected String m_resourceTableIdField = null;

	/** The additional field names in the resource table that go between the two ids and the xml */
	protected String[] m_resourceTableOtherFields = null;

	/** The xml tag name for the element holding each actual resource entry. */
	protected String m_resourceEntryTagName = null;

	/** If true, we do our locks in the remote database. */
	protected boolean m_locksAreInDb = false;

	/** If true, we do our locks in the remove database using a separate locking table. */
	protected boolean m_locksAreInTable = true;

	/** The StorageUser to callback for new Resource and Edit objects. */
	protected SingleStorageUser m_user = null;

	/**
	 * Locks, keyed by reference, holding Connections (or, if locks are done locally, holding an Edit).
	 * Obviously this doesn't enable locking in a clustered environment.
	 */
	protected Hashtable m_locks = null;

	/** If set, we treat reasource ids as case insensitive. */
	protected boolean m_caseInsensitive = false;

	/** Injected (by constructor) SqlService. */
	protected SqlService m_sql = null;

	/** contains a map of the database dependent handlers. */
	protected static Map<String, SingleStorageSql> databaseBeans;

	/** The db handler we are using. */
	protected SingleStorageSql singleStorageSql;

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#setDatabaseBeans(java.util.Map)
	 */
	public void setDatabaseBeans(Map databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#setSingleStorageSql(java.lang.String)
	 */
	public void setSingleStorageSql(String vendor)
	{
		this.singleStorageSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

	// since spring is not used and this class is instatiated directly, we need to "inject" these values ourselves
	static
	{
		databaseBeans = new Hashtable<String, SingleStorageSql>();
		databaseBeans.put("default", new SingleStorageSqlDefault());
		databaseBeans.put("hsql", new SingleStorageSqlHSql());
		databaseBeans.put("mysql", new SingleStorageSqlMySql());
		databaseBeans.put("oracle", new SingleStorageSqlOracle());
	}

	/**
	 * Construct.
	 * 
	 * @param resourceTableName
	 *        Table name for resources.
	 * @param resourceTableIdField
	 *        The field in the resource table that holds the id.
	 * @param resourceTableOtherFields
	 *        The other fields in the resource table (between the two id and the xml fields).
	 * @param locksInDb
	 *        If true, we do our locks in the remote database, otherwise we do them here.
	 * @param resourceEntryName
	 *        The xml tag name for the element holding each actual resource entry.
	 * @param user
	 *        The StorageUser class to call back for creation of Resource and Edit objects.
	 * @param sqlService
	 *        The SqlService.
	 */
	public BaseDbSingleStorage(String resourceTableName, String resourceTableIdField, String[] resourceTableOtherFields, boolean locksInDb,
			String resourceEntryName, SingleStorageUser user, SqlService sqlService)
	{
	    this(resourceTableName, resourceTableIdField, resourceTableOtherFields, locksInDb, resourceEntryName, user, sqlService, null);
	}

	// support for SAK-12874
	protected DbSingleStorage m_storage = null;

    /**
     * Construct.
     * 
     * @param resourceTableName
     *        Table name for resources.
     * @param resourceTableIdField
     *        The field in the resource table that holds the id.
     * @param resourceTableOtherFields
     *        The other fields in the resource table (between the two id and the xml fields).
     * @param locksInDb
     *        If true, we do our locks in the remote database, otherwise we do them here.
     * @param resourceEntryName
     *        The xml tag name for the element holding each actual resource entry.
     * @param user
     *        The StorageUser class to call back for creation of Resource and Edit objects.
     * @param sqlService
     *        The SqlService.
     * @param storage
     *        The storage for the normal resource (only used by delete storage)
     */
	public BaseDbSingleStorage(String resourceTableName, String resourceTableIdField, String[] resourceTableOtherFields, boolean locksInDb,
	        String resourceEntryName, SingleStorageUser user, SqlService sqlService,
	        DbSingleStorage storage)
	{
	    m_resourceTableName = resourceTableName;
	    m_resourceTableIdField = resourceTableIdField;
	    m_resourceTableOtherFields = resourceTableOtherFields;
	    m_locksAreInDb = locksInDb;
	    m_resourceEntryTagName = resourceEntryName;
	    m_user = user;
	    m_sql = sqlService;

	    // support for SAK-12874
        m_storage = storage;
        if (m_storage == null && m_resourceTableName != null && m_resourceTableName.toUpperCase().contains("DELETE")) {
            // warn if the delete storage does not have the main storage set
            log.warn("resource storage is not set, delete table resource file paths will be invalid");
        }

	    setSingleStorageSql(m_sql.getVendor());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#open()
	 */
	public void open()
	{
		// setup for locks
		m_locks = new Hashtable();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#close()
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
	 * Read one Resource from xml
	 * 
	 * @param xml
	 *        An string containing the xml which describes the resource.
	 * @return The Resource object created from the xml.
	 */
	protected Entity readResource(String xml)
	{
		try
		{
			if (m_user instanceof SAXEntityReader)
			{
				SAXEntityReader sm_user = (SAXEntityReader) m_user;
				DefaultEntityHandler deh = sm_user.getDefaultHandler(sm_user
						.getServices());
				StorageUtils.processString(xml, deh);
				return deh.getEntity();
			}
			else
			{
				// read the xml
				Document doc = StorageUtils.readDocumentFromString(xml);

				// verify the root element
				Element root = doc.getDocumentElement();
				if (!root.getTagName().equals(m_resourceEntryTagName))
				{
					log.warn("readResource(): not = " + m_resourceEntryTagName + " : "
							+ root.getTagName());
					return null;
				}

				// re-create a resource
				Entity entry = m_user.newResource(null, root);

				return entry;
			}
		}
		catch (Exception e)
		{
			log.debug("readResource(): ", e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#checkResource(java.lang.String)
	 */
	public boolean checkResource(String id)
	{
		// just see if the record exists
		String sql = singleStorageSql.getResourceIdSql(m_resourceTableIdField, m_resourceTableName);

		Object fields[] = new Object[1];
		fields[0] = caseId(id);
		List ids = m_sql.dbRead(sql, fields, null);

		return (!ids.isEmpty());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#getResource(java.lang.String)
	 */
	public Entity getResource(String id)
	{
		Entity entry = null;

		// get the user from the db
		String sql = singleStorageSql.getXmlSql(m_resourceTableIdField, m_resourceTableName);

		Object fields[] = new Object[1];
		fields[0] = caseId(id);
		List xml = m_sql.dbRead(sql, fields, null);
		if (!xml.isEmpty())
		{
			// create the Resource from the db xml
			entry = readResource((String) xml.get(0));
		}

		return entry;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#isEmpty()
	 */
	public boolean isEmpty()
	{
		// count
		int count = countAllResources();
		return (count == 0);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#getAllResources()
	 */
	public List getAllResources()
	{
		List all = new Vector();

		// read all users from the db
		String sql = singleStorageSql.getXmlSql(m_resourceTableName);
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		List xml = m_sql.dbRead(sql);

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readResource((String) xml.get(i));
				if (entry != null) all.add(entry);
			}
		}

		return all;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#getAllResources(int, int)
	 */
	public List getAllResources(int first, int last)
	{
		String sql = singleStorageSql.getXmlSql(m_resourceTableIdField, m_resourceTableName, first, last);
		Object[] fields = singleStorageSql.getXmlFields(first, last);
		List xml = m_sql.dbRead(sql, fields, null);
		List rv = new Vector();

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readResource((String) xml.get(i));
				if (entry != null) rv.add(entry);
			}
		}

		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#countAllResources()
	 */
	public int countAllResources()
	{

		// read all count
		String sql = singleStorageSql.getNumRowsSql(m_resourceTableName);

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

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#countSelectedResourcesWhere(java.lang.String)
	 */
	public int countSelectedResourcesWhere(String sqlWhere)
	{
		// read all where count
		String sql = singleStorageSql.getNumRowsSql(m_resourceTableName, sqlWhere);
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#getAllResourcesWhere(java.lang.String, java.lang.String)
	 */
	public List getAllResourcesWhere(String field, String value)
	{
		// read all users from the db
		String sql = singleStorageSql.getXmlSql(field, m_resourceTableName);
		Object[] fields = new Object[1];
		fields[0] = value;
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		return loadResources(sql, fields);
	}

	protected List loadResources(String sql, Object[] fields)
	{
		List all = m_sql.dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					// create the Resource from the db xml
					String xml = result.getString(1);
					Entity entry = readResource(xml);
					return entry;
				}
				catch (SQLException ignore)
				{
					return null;
				}
			}
		});
		return all;
	}

	/**
	 * Get a limited number of Resources a given field matches a given value, returned in ascending order 
	 * by another field.  The limit on the number of rows is specified by values for the first item to be 
	 * retrieved (indexed from 0) and the maxCount.
	 * @param selectBy The name of a field to be used in selecting resources.
	 * @param selectByValue The value to select.
	 * @param orderBy The name of a field to be used in ordering the resources.
	 * @param tableName The table on which the query is to operate
	 * @param first A non-negative integer indicating the first record to return
	 * @param maxCount A positive integer indicating the maximum number of rows to return
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getAllResourcesWhere(String selectBy, String selectByValue, String orderBy, int first, int maxCount)
	{
		// read all users from the db
		String sql = singleStorageSql.getXmlWhereLimitSql(selectBy, orderBy, m_resourceTableName, first, maxCount);
		Object[] fields = new Object[1];
		fields[0] = selectByValue;
		// %%% + "order by " + m_resourceTableOrderField + " asc";
		return loadResources(sql, fields);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#getAllResourcesWhereLike(java.lang.String, java.lang.String)
	 */
	public List getAllResourcesWhereLike(String field, String value)
	{
		String sql = singleStorageSql.getXmlLikeSql(field, m_resourceTableName);
		Object[] fields = new Object[1];
		fields[0] = value;
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		return loadResources(sql, fields);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#getSelectedResources(org.sakaiproject.javax.Filter)
	 */
	public List getSelectedResources(final Filter filter)
	{
		List all = new Vector();

		// read all users from the db
		String sql = singleStorageSql.getXmlAndFieldSql(m_resourceTableIdField, m_resourceTableName);
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		List xml = m_sql.dbRead(sql, null, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					// read the id m_resourceTableIdField
					String id = result.getString(1);

					// read the xml
					String xml = result.getString(2);

					if (!filter.accept(caseId(id))) return null;

					return xml;
				}
				catch (SQLException ignore)
				{
					return null;
				}
			}
		});

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readResource((String) xml.get(i));
				if (entry != null) all.add(entry);
			}
		}

		return all;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#getSelectedResourcesWhere(java.lang.String)
	 */
	public List getSelectedResourcesWhere(String sqlWhere)
	{
		List all = new Vector();

		// read all users from the db
		String sql = singleStorageSql.getXmlWhereSql(m_resourceTableName, sqlWhere);
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		List xml = m_sql.dbRead(sql);

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readResource((String) xml.get(i));
				if (entry != null) all.add(entry);
			}
		}

		return all;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#putResource(java.lang.String, java.lang.Object[])
	 */
	public Edit putResource(String id, Object[] others)
	{
		// create one with just the id, and perhaps some other fields as well
		Entity entry = m_user.newResource(null, id, others);

		// form the XML and SQL for the insert
		Document doc = StorageUtils.createDocument();
		entry.toXml(doc, new Stack());
		String xml = StorageUtils.writeDocumentToString(doc);
		String statement = // singleStorageSql.
		"insert into " + m_resourceTableName + insertFields(m_resourceTableIdField, m_resourceTableOtherFields, "XML") + " values ( ?, "
				+ valuesParams(m_resourceTableOtherFields) + " ? )";

		Object[] flds = m_user.storageFields(entry);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 2];
		System.arraycopy(flds, 0, fields, 1, flds.length);
		fields[0] = caseId(entry.getId());
		fields[fields.length - 1] = xml;

		// process the insert
		boolean ok = m_sql.dbWrite(statement, fields);

		// if this failed, assume a key conflict (i.e. id in use)
		if (!ok) return null;

		// now get a lock on the record for edit
		Edit edit = editResource(id);
		if (edit == null)
		{
			log.warn("putResource(): didn't get a lock!");
			return null;
		}

		return edit;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#putDeleteResource(java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
	 */
	public Edit putDeleteResource(String id, String uuid, String userId, Object[] others)
	{
        // support for SAK-12874
        Entity entry = null;
        if (m_storage != null) {
            // use the object being deleted
            entry = m_storage.getResource(id);
        }
        if (entry == null) {
            // failsafe to the old method
            entry = m_user.newResource(null, id, others);
        }
		//Entity entry = m_user.newResource(null, id, others);

		// form the XML and SQL for the insert
		Document doc = StorageUtils.createDocument();
		entry.toXml(doc, new Stack());
		String xml = StorageUtils.writeDocumentToString(doc);
		String statement = "insert into " + m_resourceTableName
				+ insertDeleteFields(m_resourceTableIdField, m_resourceTableOtherFields, "RESOURCE_UUID", "DELETE_DATE", "DELETE_USERID", "XML")
				+ " values ( ?, " + valuesParams(m_resourceTableOtherFields) + " ? ,? ,? ,?)";

		Object[] flds = m_user.storageFields(entry);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 5];
		System.arraycopy(flds, 0, fields, 1, flds.length);
		fields[0] = caseId(entry.getId());
		// uuid added here
		fields[fields.length - 4] = uuid;
		// date added here
		fields[fields.length - 3] = TimeService.newTime();// .toStringLocalDate();

		// userId added here
		fields[fields.length - 2] = userId;
		fields[fields.length - 1] = xml;

		// process the insert
		boolean ok = m_sql.dbWrite(statement, fields);

		// if this failed, assume a key conflict (i.e. id in use)
		if (!ok) return null;

		// now get a lock on the record for edit
		Edit edit = editResource(id);
		if (edit == null)
		{
			log.warn("putResourceDelete(): didn't get a lock!");
			return null;
		}

		return edit;
	}

	/** Construct the SQL statement */
	protected String insertDeleteFields(String before, String[] fields, String uuid, String date, String userId, String after)
	{
		StringBuilder buf = new StringBuilder();
		buf.append(" (");
		buf.append(before);
		buf.append(",");
		if (fields != null)
		{
			for (int i = 0; i < fields.length; i++)
			{
				buf.append(fields[i] + ",");
			}
		}
		buf.append(uuid);
		buf.append(",");
		buf.append(date);
		buf.append(",");
		buf.append(userId);
		buf.append(",");
		buf.append(after);
		buf.append(")");

		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#commitDeleteResource(org.sakaiproject.entity.api.Edit, java.lang.String)
	 */
	public void commitDeleteResource(Edit edit, String uuid)
	{
		// form the SQL statement and the var w/ the XML
		Document doc = StorageUtils.createDocument();
		edit.toXml(doc, new Stack());
		String xml = StorageUtils.writeDocumentToString(doc);
		Object[] flds = m_user.storageFields(edit);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 2];
		System.arraycopy(flds, 0, fields, 0, flds.length);
		fields[fields.length - 2] = xml;
		fields[fields.length - 1] = uuid;// caseId(edit.getId());

		String statement = "update " + m_resourceTableName + " set " + updateSet(m_resourceTableOtherFields) + " XML = ? where ( RESOURCE_UUID = ? )";

		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				log.warn("commitResource(): edit not in locks");
				return;
			}
			// update, commit, release the lock's connection
			m_sql.dbUpdateCommit(statement, fields, null, lock);
			// remove the lock
			m_locks.remove(edit.getReference());
		}

		else if (m_locksAreInTable)
		{
			// process the update
			m_sql.dbWrite(statement, fields);

			// remove the lock
			statement = singleStorageSql.getDeleteLocksSql();

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
			// just process the update
			m_sql.dbWrite(statement, fields);

			// remove the lock
			m_locks.remove(edit.getReference());
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#editResource(java.lang.String)
	 */
	public Edit editResource(String id)
	{
		Edit edit = null;

		if (m_locksAreInDb)
		{
			if ("oracle".equals(m_sql.getVendor()))
			{
				// read the record and get a lock on it (non blocking)
				String statement = "select XML from " + m_resourceTableName + " where ( " + m_resourceTableIdField + " = '"
						+ StorageUtils.escapeSql(caseId(id)) + "' )" + " for update nowait";
				StringBuilder result = new StringBuilder();
				Connection lock = m_sql.dbReadLock(statement, result);

				// for missing or already locked...
				if ((lock == null) || (result.length() == 0)) return null;

				// make first a Resource, then an Edit
				Entity entry = readResource(result.toString());
				edit = m_user.newResourceEdit(null, entry);

				// store the lock for this object
				m_locks.put(entry.getReference(), lock);
			}
			else
			{
				throw new UnsupportedOperationException("Record locking only available when configured with Oracle database");
			}
		}

		// if the locks are in a separate table in the db
		else if (m_locksAreInTable)
		{
			// read the record - fail if not there
			Entity entry = getResource(id);
			if (entry == null) return null;

			// write a lock to the lock table - if we can do it, we get the lock
			String statement = singleStorageSql.getInsertLocks();

			// we need session id and user id
			String sessionId = UsageSessionService.getSessionId();
			if (sessionId == null)
			{
				sessionId = ""; // TODO - "" gets converted to a null and will never be able to be cleaned up -AZ (SAK-11841)
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
			edit = m_user.newResourceEdit(null, entry);
		}

		// otherwise, get the lock locally
		else
		{
			// get the entry, and check for existence
			Entity entry = getResource(id);
			if (entry == null) return null;

			// we only sync this getting - someone may release a lock out of sync
			synchronized (m_locks)
			{
				// if already locked
				if (m_locks.containsKey(entry.getReference())) return null;

				// make the edit from the Resource
				edit = m_user.newResourceEdit(null, entry);

				// store the edit in the locks by reference
				m_locks.put(entry.getReference(), edit);
			}
		}

		return edit;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#commitResource(org.sakaiproject.entity.api.Edit)
	 */
	public void commitResource(Edit edit)
	{
		// form the SQL statement and the var w/ the XML
		Document doc = StorageUtils.createDocument();
		edit.toXml(doc, new Stack());
		String xml = StorageUtils.writeDocumentToString(doc);
		Object[] flds = m_user.storageFields(edit);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 2];
		System.arraycopy(flds, 0, fields, 0, flds.length);
		fields[fields.length - 2] = xml;
		fields[fields.length - 1] = caseId(edit.getId());

		String statement = "update " + m_resourceTableName + " set " + updateSet(m_resourceTableOtherFields) + " XML = ? where ( "
				+ m_resourceTableIdField + " = ? )";
		// singleStorageSql.getUpdateXml(m_resourceTableIdField, m_resourceTableOtherFields, m_resourceTableName);

		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				log.warn("commitResource(): edit not in locks");
				return;
			}

			// update, commit, release the lock's connection
			m_sql.dbUpdateCommit(statement, fields, null, lock);

			// remove the lock
			m_locks.remove(edit.getReference());
		}

		else if (m_locksAreInTable)
		{
			// process the update
			m_sql.dbWrite(statement, fields);

			// remove the lock
			statement = singleStorageSql.getDeleteLocksSql();

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
			// just process the update
			m_sql.dbWrite(statement, fields);

			// remove the lock
			m_locks.remove(edit.getReference());
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#cancelResource(org.sakaiproject.entity.api.Edit)
	 */
	public void cancelResource(Edit edit)
	{
		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				log.warn("cancelResource(): edit not in locks");
				return;
			}

			// rollback and release the lock's connection
			m_sql.dbCancel(lock);

			// release the lock
			m_locks.remove(edit.getReference());
		}

		else if (m_locksAreInTable)
		{
			// remove the lock
			String statement = singleStorageSql.getDeleteLocksSql();

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

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.DbSingleStorage#removeResource(org.sakaiproject.entity.api.Edit)
	 */
	public void removeResource(Edit edit)
	{
		// form the SQL delete statement
		String statement = singleStorageSql.getDeleteSql(m_resourceTableIdField, m_resourceTableName);

		Object fields[] = new Object[1];
		fields[0] = caseId(edit.getId());

		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				log.warn("removeResource(): edit not in locks");
				return;
			}

			// process the delete statement, commit, and release the lock's connection
			m_sql.dbUpdateCommit(statement, fields, null, lock);

			// release the lock
			m_locks.remove(edit.getReference());
		}

		else if (m_locksAreInTable)
		{
			// process the delete statement
			m_sql.dbWrite(statement, fields);

			// remove the lock
			statement = singleStorageSql.getDeleteLocksSql();

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
			// process the delete statement
			m_sql.dbWrite(statement, fields);

			// release the lock
			m_locks.remove(edit.getReference());
		}
	}

	/**
	 * Form a string of n question marks with commas, for sql value statements, one for each item in the values array, or an empty string if null.
	 * 
	 * @param values
	 *        The values to be inserted into the sql statement.
	 * @return A sql statement fragment for the values part of an insert, one for each value in the array.
	 */
	protected String valuesParams(String[] fields)
	{
		if ((fields == null) || (fields.length == 0)) return "";
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < fields.length; i++)
		{
			buf.append(" ?,");
		}
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
		if ((fields == null) || (fields.length == 0)) return "";
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < fields.length; i++)
		{
			buf.append(fields[i] + " = ?,");
		}
		return buf.toString();
	}

	/**
	 * Form a string of (field, field, field), for sql insert statements, one for each item in the fields array, plus one before, and one after.
	 * 
	 * @param before
	 *        The first field name.
	 * @param values
	 *        The extra field names, in the middle.
	 * @param after
	 *        The last field name.
	 * @return A sql statement fragment for the insert fields.
	 */
	protected String insertFields(String before, String[] fields, String after)
	{
		StringBuilder buf = new StringBuilder();
		buf.append(" (");

		buf.append(before);
		buf.append(",");

		if (fields != null)
		{
			for (int i = 0; i < fields.length; i++)
			{
				buf.append(fields[i] + ",");
			}
		}

		buf.append(after);

		buf.append(")");

		return buf.toString();
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
}
