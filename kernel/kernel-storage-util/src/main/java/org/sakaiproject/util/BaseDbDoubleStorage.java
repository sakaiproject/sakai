/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/BaseDbDoubleStorage.java $
 * $Id: BaseDbDoubleStorage.java 66393 2009-09-10 08:19:24Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.javax.Order;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.javax.Search;
import org.sakaiproject.javax.SearchFilter;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;

/**
 * <p>
 * BaseDbDoubleStorage is a class that stores collections of Resources (of some type) in a database, <br />
 * provides locked access, and generally implements a services "storage" class. The <br />
 * service's storage class can extend this to provide covers to turn Resource and <br />
 * Edit into something more type specific to the service.
 * </p>
 * <p>
 * Note: the methods here are all "id" based, with the following assumptions:
 * <ul>
 * <li>just the Resource Id field is enough to distinguish one Resource from another (or, for resource, the container's id and the resource id).</li>
 * <li>a resource's reference is based on no more than the resource id, for containers </li>
 * <li>and no more than resource and container id for resources</li>
 * <li>a resource's id and container id cannot change</li>
 * </ul>
 * </p>
 * <br />
 * In order to handle Unicode characters properly, the SQL statements executed by this class should not embed Unicode characters into the SQL
 * statement text; <br />
 * rather, Unicode values should be inserted as fields in a PreparedStatement. Databases handle Unicode better in fields.
 * </p>
 */
@Slf4j
public class BaseDbDoubleStorage
{
	/** Table name for container records. */
	protected String m_containerTableName = null;

	/** The field in the table that holds the container id. */
	protected String m_containerTableIdField = null;

	/** Table name for resource records. */
	protected String m_resourceTableName = null;

	/** The field in the resource table that holds the resource id. */
	protected String m_resourceTableIdField = null;

	/** The field in the resource table that holds the container id. */
	protected String m_resourceTableContainerIdField = null;

	/** The additional field names in the resource table that go between the two ids and the xml. */
	protected String[] m_resourceTableOtherFields = null;

	/** The string searchable field names in the resource table. This must be either null
	  * (i.e. no fields) or this is assumed to be the only fields which participate in search.
	  */
	protected String[] m_resourceTableSearchFields = null;

	/** The field name in the resource table for ordering. */
	protected String m_resourceTableOrderField = null;

	/** The xml tag name for the element holding each actual resource entry. */
	protected String m_resourceEntryTagName = null;

	/** The xml tag name for the element holding each actual container entry. */
	protected String m_containerEntryTagName = null;

	/** The field in the record that has the user id of the resource owner. */
	protected String m_resourceTableOwnerField = null;

	/** The field in the record that has the draft indicator ('0' for no, '1' for yes). */
	protected String m_resourceTableDraftField = null;

	/** The field in the record that has the pubview indicator ('0' for no, '1' for yes). */
	protected String m_resourceTablePubViewField = null;

	/** If true, we do our locks in the remote database. */
	protected boolean m_locksAreInDb = true;

	/** If true, we do our locks in the remove database using a separate locking table. */
	protected boolean m_locksAreInTable = true;

	/** The StorageUser to callback for new Resource and Edit objects. */
	protected DoubleStorageUser m_user = null;

	/**
	 * Locks, keyed by reference, holding Connections (or, if locks are done locally, holding an Edit). Note: keying by reference allows botu
	 * container and resource locks to be stored, the reference distinguishes them.
	 */
	protected Hashtable m_locks = null;

	/** For container, the extra field is (no longer used) NEXT_ID */
	protected static final String[] M_containerExtraFields = {"NEXT_ID"};

	/** Injected (by constructor) SqlService. */
	protected SqlService m_sql = null;

	/** contains a map of the database dependent handlers. */
	protected static Map<String, DoubleStorageSql> databaseBeans;

	/** The db handler we are using. */
	protected DoubleStorageSql doubleStorageSql;
    
	public void setDatabaseBeans(Map databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setDoubleStorageSql(String vendor)
	{
		this.doubleStorageSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

	// since spring is not used and this class is instatiated directly, we need to "inject" these values ourselves
	static
	{
		databaseBeans = new Hashtable<String, DoubleStorageSql>();
		databaseBeans.put("default", new DoubleStorageSqlDefault());
		databaseBeans.put("hsqldb", new DoubleStorageSqlHSql());
		databaseBeans.put("mysql", new DoubleStorageSqlMySql());
		databaseBeans.put("oracle", new DoubleStorageSqlOracle());
	}

	/**
	 * Construct.
	 * 
	 * @param containerTableName
	 *        Table name for containers.
	 * @param containerTableIdField
	 *        The field in the container table that holds the id.
	 * @param resourceTableName
	 *        Table name for resources.
	 * @param resourceTableIdField
	 *        The field in the resource table that holds the id.
	 * @param resourceTableContainerIdField
	 *        The field in the resource table that holds the container id.
	 * @param resourceTableOrderField
	 *        The field in the resource table that is used for ordering results.
	 * @param resourceTableOtherFields
	 *        The other fields in the resource table (between the two id fields and the xml field).
	 * @param locksInDb
	 *        If true, we do our locks in the remote database, otherwise we do them here.
	 * @param containerEntryName
	 *        The xml tag name for the element holding each actual container entry.
	 * @param resourceEntryName
	 *        The xml tag name for the element holding each actual resource entry.
	 * @param user
	 *        The StorageUser class to call back for creation of Resource and Edit objects.
	 * @param sqlService
	 *        The SqlService.
	 */
	public BaseDbDoubleStorage(String containerTableName, String containerTableIdField, 
			String resourceTableName, String resourceTableIdField,
			String resourceTableContainerIdField, String resourceTableOrderField, 
			String resourceTableOwnerField, String resourceTableDraftField,
			String resourceTablePubViewField, String[] resourceTableOtherFields, String[] resourceTableSearchFields, 
			boolean locksInDb, String containerEntryName,
			String resourceEntryName, DoubleStorageUser user, SqlService sqlService)
	{
		m_containerTableName = containerTableName;
		m_containerTableIdField = containerTableIdField;
		m_resourceTableName = resourceTableName;
		m_resourceTableIdField = resourceTableIdField;
		m_resourceTableContainerIdField = resourceTableContainerIdField;
		m_resourceTableOrderField = resourceTableOrderField;
		m_resourceTableOtherFields = resourceTableOtherFields;
		m_resourceTableSearchFields = resourceTableSearchFields;
		m_locksAreInDb = locksInDb;
		m_containerEntryTagName = containerEntryName;
		m_resourceEntryTagName = resourceEntryName;
		m_resourceTableOwnerField = resourceTableOwnerField;
		m_resourceTableDraftField = resourceTableDraftField;
		m_resourceTablePubViewField = resourceTablePubViewField;
		m_user = user;
		m_sql = sqlService;

		setDoubleStorageSql(m_sql.getVendor());
	}

	/** Backwards compatibility constructor for using DbDouble without search fields */

	public BaseDbDoubleStorage(String containerTableName, String containerTableIdField, 
			String resourceTableName, String resourceTableIdField,
			String resourceTableContainerIdField, String resourceTableOrderField, 
			String resourceTableOwnerField, String resourceTableDraftField,
			String resourceTablePubViewField, String[] resourceTableOtherFields, // String[] resourceTableSearchFields, 
			boolean locksInDb, String containerEntryName,
			String resourceEntryName, DoubleStorageUser user, SqlService sqlService)
	{
		m_containerTableName = containerTableName;
		m_containerTableIdField = containerTableIdField;
		m_resourceTableName = resourceTableName;
		m_resourceTableIdField = resourceTableIdField;
		m_resourceTableContainerIdField = resourceTableContainerIdField;
		m_resourceTableOrderField = resourceTableOrderField;
		m_resourceTableOtherFields = resourceTableOtherFields;
		m_resourceTableSearchFields = null; // resourceTableSearchFields;
		m_locksAreInDb = locksInDb;
		m_containerEntryTagName = containerEntryName;
		m_resourceEntryTagName = resourceEntryName;
		m_resourceTableOwnerField = resourceTableOwnerField;
		m_resourceTableDraftField = resourceTableDraftField;
		m_resourceTablePubViewField = resourceTablePubViewField;
		m_user = user;
		m_sql = sqlService;

		setDoubleStorageSql(m_sql.getVendor());
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
	 * Read one Container Resource from xml
	 * 
	 * @param xml
	 *        An string containing the xml which describes the Container resource.
	 * @return The Container Resource object created from the xml.
	 */
	protected Entity readContainer(String xml)
	{
		try
		{
			if ( m_user instanceof SAXEntityReader ) {
				SAXEntityReader sm_user = (SAXEntityReader) m_user;
				DefaultEntityHandler deh = sm_user.getDefaultHandler(sm_user.getServices());
				StorageUtils.processString(xml, deh);
				return deh.getEntity();
			} else {
				// read the xml
				Document doc = StorageUtils.readDocumentFromString(xml);
	
				// verify the root element
				Element root = doc.getDocumentElement();
				if (!root.getTagName().equals(m_containerEntryTagName))
				{
					log.warn("readContainer(): not = " + m_containerEntryTagName + " : " + root.getTagName());
					return null;
				}
	
				// re-create a resource
				Entity entry = m_user.newContainer(root);
				return entry;
				
			} 
			

		}
		catch (Exception e)
		{
			log.warn("readContainer(): "+e.getMessage());
			log.info("readContainer(): ", e);
			return null;
		}
	}

	/**
	 * Check if a Container by this id exists.
	 * 
	 * @param ref
	 *        The container reference.
	 * @return true if a Container by this id exists, false if not.
	 */
	public boolean checkContainer(String ref)
	{
		// just see if the record exists
		String sql = doubleStorageSql.getSelect1Sql(m_containerTableName, m_containerTableIdField);
		Object[] fields = new Object[1];
		fields[0] = ref;
		List ids = m_sql.dbRead(sql, fields, null);
		return (!ids.isEmpty());
	}

	/**
	 * Get the Container with this id, or null if not found.
	 * 
	 * @param ref
	 *        The container reference.
	 * @return The Container with this id, or null if not found.
	 */
	public Entity getContainer(String ref)
	{
		Entity entry = null;

		// get the user from the db
		String sql = doubleStorageSql.getSelectXml2Sql(m_containerTableName, m_containerTableIdField);
		Object[] fields = new Object[1];
		fields[0] = ref;

		List xml = m_sql.dbRead(sql, fields, null);
		if (!xml.isEmpty())
		{
			// create the Resource from the db xml
			entry = readContainer((String) xml.get(0));
		}

		return entry;
	}

	/**
	 * Get all Containers.
	 * 
	 * @return The list (Resource) of all Containers.
	 */
	public List getAllContainers()
	{
		List all = new Vector();

		// read all users from the db
		String sql = doubleStorageSql.getSelectXml1Sql(m_containerTableName);
		// %%% order by...
		List xml = m_sql.dbRead(sql);

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readContainer((String) xml.get(i));
				if (entry != null) all.add(entry);
			}
		}

		return all;
	}

	/**
	 * Add a new Container with this id.
	 * 
	 * @param ref
	 *        The container reference.
	 * @return The locked Container object with this id, or null if the id is in use.
	 */
	public Edit putContainer(String ref)
	{
		// create one with just the id
		Entity entry = m_user.newContainer(ref);

		// form the XML and SQL for the insert
		Document doc = StorageUtils.createDocument();
		entry.toXml(doc, new Stack());
		String xml = StorageUtils.writeDocumentToString(doc);

		String statement = doubleStorageSql.getInsertSql(m_containerTableName, insertFields(m_containerTableIdField, null, M_containerExtraFields,
				"XML"));
		Object[] fields = new Object[2];
		fields[0] = entry.getReference();
		fields[1] = xml;

		// process the insert
		boolean ok = m_sql.dbWrite(statement, fields);

		// if this failed, assume a key conflict (i.e. id in use)
		if (!ok) return null;

		// now get a lock on the record for edit
		Edit edit = editContainer(ref);
		if (edit == null)
		{
			log.warn("putContainer(): didn't get a lock!");
			return null;
		}

		return edit;
	}

	/**
	 * Get a lock on the Container with this id, or null if a lock cannot be gotten.
	 * 
	 * @param ref
	 *        The container reference.
	 * @return The locked Container with this id, or null if this cannot be locked.
	 */
	public Edit editContainer(String ref)
	{
		Edit edit = null;

		if (m_locksAreInDb)
		{
			if ("oracle".equals(m_sql.getVendor()))
			{
				// read the record and get a lock on it (non blocking)
				String statement = doubleStorageSql.getSelectXml3Sql(m_containerTableName, m_containerTableIdField, StorageUtils.escapeSql(ref));
				StringBuilder result = new StringBuilder();
				Connection lock = m_sql.dbReadLock(statement, result);

				// for missing or already locked...
				if ((lock == null) || (result.length() == 0)) return null;

				// make first a Resource, then an Edit
				Entity entry = readContainer(result.toString());
				edit = m_user.newContainerEdit(entry);

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
			// get, and return if not found
			Entity entry = getContainer(ref);
			if (entry == null) return null;

			// write a lock to the lock table - if we can do it, we get the lock
			String statement = doubleStorageSql.getInsertSql2();

			// we need session id
			String sessionId = UsageSessionService.getSessionId();
			if (sessionId == null)
			{
				sessionId = "";
			}

			// collect the fields
			Object fields[] = new Object[4];
			fields[0] = m_containerTableName;
			fields[1] = doubleStorageSql.getRecordId(ref);
			fields[2] = TimeService.newTime();
			fields[3] = sessionId;

			// add the lock - if fails, someone else has the lock
			boolean ok = m_sql.dbWriteFailQuiet(null, statement, fields);
			if (!ok)
			{
				return null;
			}

			// make the edit from the Resource
			edit = m_user.newContainerEdit(entry);
		}

		// otherwise, get the lock locally
		else
		{
			// get, and return if not found
			Entity entry = getContainer(ref);
			if (entry == null) return null;

			// we only sync this getting - someone may release a lock out of sync
			synchronized (m_locks)
			{
				// if already locked
				if (m_locks.containsKey(entry.getReference())) return null;

				// make the edit from the Resource
				edit = m_user.newContainerEdit(entry);

				// store the edit in the locks by reference
				m_locks.put(entry.getReference(), edit);
			}
		}
		return edit;
	}

	/**
	 * Commit the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to commit.
	 */
	public void commitContainer(Edit edit)
	{
		// form the SQL statement and the var w/ the XML
		Document doc = StorageUtils.createDocument();
		edit.toXml(doc, new Stack());
		String xml = StorageUtils.writeDocumentToString(doc);
		String statement = doubleStorageSql.getUpdateSql(m_containerTableName, m_containerTableIdField);
		Object[] fields = new Object[2];
		fields[0] = xml;
		fields[1] = edit.getReference();

		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				log.warn("commitContainer(): edit not in locks");
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
			statement = doubleStorageSql.getDeleteLocksSql();

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_containerTableName;
			lockFields[1] = doubleStorageSql.getRecordId(edit.getReference());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				log.warn("commitContainer: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
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

	/**
	 * Cancel the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to cancel.
	 */
	public void cancelContainer(Edit edit)
	{
		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				log.warn("cancelContainer(): edit not in locks");
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
			String statement = doubleStorageSql.getDeleteLocksSql();

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_containerTableName;
			lockFields[1] = doubleStorageSql.getRecordId(edit.getReference());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				log.warn("cancelContainer: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
			}
		}

		else
		{
			// release the lock
			m_locks.remove(edit.getReference());
		}
	}

	/**
	 * Remove this (locked) Container.
	 * 
	 * @param user
	 *        The Edit to remove.
	 */
	public void removeContainer(Edit edit)
	{
		// form the SQL delete statement
		String statement = doubleStorageSql.getDeleteSql(m_containerTableName, m_containerTableIdField);
		Object[] fields = new Object[1];
		fields[0] = edit.getReference();

		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				log.warn("removeContainer(): edit not in locks");
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
			statement = doubleStorageSql.getDeleteLocksSql();

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_containerTableName;
			lockFields[1] = doubleStorageSql.getRecordId(edit.getReference());
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
	 * Read one Resource from xml
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param xml
	 *        An string containing the xml which describes the resource.
	 * @return The Resource object created from the xml.
	 */
	protected Entity readResource(Entity container, String xml)
	{
		try
		{
			if ( m_user instanceof SAXEntityReader ) {
				SAXEntityReader sm_user = (SAXEntityReader) m_user;
				DefaultEntityHandler deh = sm_user.getDefaultHandler(sm_user.getServices());
				deh.setContainer(container);
				StorageUtils.processString(xml, deh);
				return deh.getEntity();
			} else {
			// read the xml
			Document doc = StorageUtils.readDocumentFromString(xml);
			
			//The resulting doc could be null
			if (doc == null) {
				log.warn("null xml document passed to readResource for container" + container.getId());
				return null;
			}
			// verify the root element
			Element root = doc.getDocumentElement();
			if (!root.getTagName().equals(m_resourceEntryTagName))
			{
				log.warn("readResource(): not = " + m_resourceEntryTagName + " : " + root.getTagName());
				return null;
			}

			// re-create a resource
			Entity entry = m_user.newResource(container, root);

			return entry;
			}
		}
		catch (Exception e)
		{
			log.warn("readResource(): "+e.getMessage());
			log.info("readResource(): ", e);
			return null;
		}
	}

	/**
	 * Check if a Resource by this id exists.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param id
	 *        The id.
	 * @return true if a Resource by this id exists, false if not.
	 */
	public boolean checkResource(Entity container, String id)
	{
		// just see if the record exists
		String sql = doubleStorageSql.getSelectIdSql(m_resourceTableName, m_resourceTableIdField, m_resourceTableContainerIdField);
		Object[] fields = new Object[2];
		fields[0] = container.getReference();
		fields[1] = id;
		List ids = m_sql.dbRead(sql, fields, null);
		return (!ids.isEmpty());
	}

	/**
	 * Get the Resource with this id, or null if not found.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param id
	 *        The id.
	 * @return The Resource with this id, or null if not found.
	 */
	public Entity getResource(Entity container, String id)
	{
		Entity entry = null;

		// get the user from the db
		String sql = doubleStorageSql.getSelectXml4Sql(m_resourceTableName, m_resourceTableIdField, m_resourceTableContainerIdField);
		Object[] fields = new Object[2];
		fields[0] = container.getReference();
		fields[1] = id;
		List xml = m_sql.dbRead(sql, fields, null);
		if (!xml.isEmpty())
		{
			// create the Resource from the db xml
			entry = readResource(container, (String) xml.get(0));
		}

		return entry;
	}
    
	/**
	 * Count all Resources
	 * @param container
	 *        The container for this resource.
	 */
	public int getCount(Entity container)
	{
		// read With or without a filter
		String sql = doubleStorageSql.getCountSql(m_resourceTableName, m_resourceTableContainerIdField);
		Object[] fields = new Object[1];
		fields[0] = container.getReference();
		List countList = m_sql.dbRead(sql, fields, null);
		  
		if ( countList.isEmpty() ) return 0;
		
		Object obj = countList.get(0);
		String str = (String) obj;
		return Integer.parseInt(str);
	}

	/**
	 * Count all Resources - This takes two approaches depending 
	 * on whether this table has search fields.  If searchfields 
	 * are available we can do a SELECT COUNT WHERE.  Otherwise
         * we retrieve the records and run the filter on each record.
	 * @param container
	 *        The container for this resource.
	 * @param filter
	 *        A filter object to accept / reject the searches
	 * @param search
	 *        Search string
	 */
	public int getCount(Entity container, Filter filter)
	{
		if ( filter == null ) return getCount(container);

		// If we have search fields - do a quick select count with a where clause
		if  ( m_resourceTableSearchFields != null && filter instanceof SearchFilter ) 
		{
			int searchFieldCount = 0;
			String searchString = ((SearchFilter) filter).getSearchString();
			if ( searchString != null && searchString.length() > 0 )
			{
				String searchWhere = doubleStorageSql.getSearchWhereClause(m_resourceTableSearchFields);
				if ( searchWhere != null && searchWhere.length() > 0 ) 
				{
					searchFieldCount = m_resourceTableSearchFields.length;
					String sql = doubleStorageSql.getCountSqlWhere(m_resourceTableName, 
						m_resourceTableContainerIdField, searchWhere);

					Object[] fields = new Object[1+searchFieldCount];
					fields[0] = container.getReference();
					for ( int i=0; i < searchFieldCount; i++) fields[i+1] = "%" + searchString + "%";

					List countList = m_sql.dbRead(sql, fields, null);
		  
					if ( countList.isEmpty() ) return 0;
		
					Object obj = countList.get(0);
					String str = (String) obj;
					return Integer.parseInt(str);
				}
			}
		}

		// No search fields - retrieve, filter and count
		String sql = doubleStorageSql.getSelectXml5Sql(m_resourceTableName, m_resourceTableContainerIdField, null, false);
		Object[] fields = new Object[1];
		fields[0] = container.getReference();
		List all = m_sql.dbRead(sql, fields, new SearchFilterReader(container, filter,  null, true));
		int count = all.size();
		return count;
	}

	/**
	 * Get all Resources.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @return The list (Resource) of all Resources.
	 */
	public List getAllResources(Entity container)
	{
		return getAllResources(container, null, null, true, null);
	}

	/**
	 * Get all Resources.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param filter
	 *        conditional for select statement
	 * @return The list (Resource) of all Resources.
	 */
	public List getAllResources(Entity container, Filter filter)
	{
		return getAllResources(container, filter,  null, true, null);
	}

	/**
	 * Get all Resources.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param sqlFilter
	 *        conditional for select statement
	 * @return The list (Resource) of all Resources.
	 */
	public List getAllResources(Entity container, String sqlFilter)
	{
		return getAllResources(container, null,  sqlFilter, true, null);
	}
	
	/**
	 * Deal with the fact that we can get a PagingPosition from a query filter
	 * or directly as a parameter.  In time remove the option to use
         * PagingPosition. 
         */
	// TODO: Remove all methods with PagingPostition and switch to Filter
	private PagingPosition fixPagingPosition(Filter filter, PagingPosition pos)
	{
		if ( filter == null ) return pos;
		if ( pos != null )
		{
			log.warn("The use of methods with PagingPosition should switch to using Search (SAK-13584) - Chuck");
			return pos;
		}
		if ( filter instanceof Search )
		{
			Search q = (Search) filter;
			if ( q.getLimit() > 0 && q.getLimit() >= q.getStart() ) 
			{
				return new PagingPosition((int) q.getStart(), (int) q.getLimit());
			}
		}
		return null;
	}


	/**
	 * Get all Resources.
	 *
	 * @param container
	 *        The container for this resource.
	 * @param softFilter
	 *        an optional software filter
	 * @param sqlFilter
	 *        an optional conditional for select statement
	 * @param asc
	 *        true means ascending
	 * @param pager
	 *        an optional range of elements to return inclusive
	 * @return The list (Resource) of all Resources.
	 */
	public List getAllResources(Entity container, Filter softFilter, String sqlFilter, boolean asc, PagingPosition pager) {
		return getAllResources(container, softFilter, sqlFilter, asc, pager, null);
	}

	/**
	 * Get all Resources.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param softFilter
	 *        an optional software filter
	 * @param sqlFilter
	 *        an optional conditional for select statement
	 * @param asc
	 *        true means ascending
	 * @param pager
	 *        an optional range of elements to return inclusive
	 * @param bindVariables
	 *        an optional list of bind variables
	 * @return The list (Resource) of all Resources.
	 */
	public List getAllResources(Entity container, Filter softFilter, String sqlFilter, boolean asc, PagingPosition pager, List <Object> bindVariables)
	{
		
		pager = fixPagingPosition(softFilter, pager);
        
		// Get the orders and get the ORDER BY clause
		Order[] orders = null;
		if ( softFilter instanceof Search ) 
		{
			orders = ((Search) softFilter).getOrders();
		}
		String orderString = doubleStorageSql.getOrderClause(orders,  m_resourceTableOrderField, asc);

		// Turn the search string into a WHERE clause if we can
		int searchFieldCount = 0;
		String searchString = null;
		if  ( m_resourceTableSearchFields != null && softFilter instanceof SearchFilter ) 
		{
			searchString = ((SearchFilter) softFilter).getSearchString();
			if ( searchString != null && searchString.length() > 0 )
			{
				String searchWhere = doubleStorageSql.getSearchWhereClause(m_resourceTableSearchFields);
				if ( searchWhere != null && searchWhere.length() > 0 ) 
				{
					if (sqlFilter == null ) 
					{
						sqlFilter = searchWhere;
					}
					else
					{
						sqlFilter = sqlFilter + " and " + searchWhere ;
					}
					searchFieldCount = m_resourceTableSearchFields.length;
				}
			}
		}

		String sql = doubleStorageSql.getSelectXml5filterSql(m_resourceTableName, 
				m_resourceTableContainerIdField, orderString, sqlFilter);
	
		// Add Paging to the Search if requested
		// TODO: Someday make this think Filter and emulate PagingPosition
		boolean pagedInSql = false;
		if ( pager != null )
		{
			String limitedSql = doubleStorageSql.addLimitToQuery(sql, pager.getFirst()-1, pager.getLast()-1);
 
			if ( limitedSql != null ) 
			{
				pagedInSql = true;
				sql = limitedSql;
			} else {
				// We don't subtract 1 because TOP is a count, not zero based like LIMIT
				String topSql = doubleStorageSql.addTopToQuery(sql, pager.getLast());
				if ( topSql != null )
				{
					sql = topSql;
				}
			}
		}

		Object[] fields = new Object[1+searchFieldCount];
		fields[0] = container.getReference();
		for ( int i=0; i < searchFieldCount; i++) fields[i+1] = "%" + searchString + "%";

		if (bindVariables != null && bindVariables.size() > 0) {
			// Add the bind variables to the fields to substitute in the prepared statement
			fields = ArrayUtils.addAll(fields, bindVariables.toArray(new Object[fields.length]));
		}

		// If we are paged in SQL - then do not pass in the pager
		List all = m_sql.dbRead(sql, fields, new SearchFilterReader(container, softFilter,  pagedInSql ? null : pager, false));
		
		return all;
	}
    
	/** matchXml - Perform an optional pre-de-serialize match if desired
	 *
	 * This is just a dummy implementation - this wil be overridden in the
	 * class that extends this class if a particular storage wants to take
	 * advantage of this feature.
	 *
	 * Return value:
	 * -1 indicates - definite "no"
	 * 0 indicates - maybe - continue and parse the Xml
	 * 1 indicates - "yes" - we know in this rouinte this is a match
	 */
	public int matchXml(String xml, String search)
	{
		return 0;
	}
    
	public class SearchFilterReader implements SqlReader
	{
		private Filter m_filter;
		private String m_search = null;
		private PagingPosition m_pager;
		private Entity m_container;
		private boolean m_doCount = false;
    	
		private int count = 0;
    	
		// If we are only counting - return a tiny thing - not a big thing
		private final Integer intValue = 1;
    	
		public SearchFilterReader(Entity container, Filter filter, PagingPosition pager, boolean doCount)
		{
			m_container = container;
			m_filter = filter;
			if ( filter instanceof SearchFilter ) m_search = ( (SearchFilter) filter).getSearchString();
			m_pager = pager;
			m_doCount = doCount;
		}
    	
		public Object readSqlResultRecord(ResultSet result)
			throws SqlReaderFinishedException
		{
			try
			{
				String theXml = result.getString(1);
				if ( m_pager != null && count > m_pager.getLast() ) 
				{
					throw new SqlReaderFinishedException();
				}
				
				int iTest = 0;  // Don't know if we have a match
				if ( m_search != null )
				{
					iTest = matchXml(theXml, m_search);
				}
                
				// If it is clearly rejected from pre-parse match
				if ( iTest == -1 ) return null;
                
				// If it is a match and we are just counting - no parsing
				// needed
				if ( iTest == 1 && m_doCount ) return intValue;
                
				// If it is known to be accepted (1) or unsure (0), 
				// parse the Xml and continue
				Entity entry = readResource(m_container, theXml);
				if ( entry == null ) return null;
                    
				// If there is no indication from matchXml
				if ( iTest == 0 && m_search != null)
				{
					if ( ! m_filter.accept(entry) ) return null;
				}
				count++;
				if ( m_pager != null && count < m_pager.getFirst() ) return null;
                
				if ( m_pager != null && count > m_pager.getLast() )
				{
					throw new SqlReaderFinishedException();
				}
                
				if ( m_doCount ) return intValue;
				return entry;
			}
			catch (SQLException ignore)
			{
				return null;
			}
		}
	}

	/**
	 * Add a new Resource with this id.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param id
	 *        The id.
	 * @param others
	 *        Other fields for the newResource call
	 * @return The locked Resource object with this id, or null if the id is in use.
	 */
	public Edit putResource(Entity container, String id, Object[] others)
	{
		// create one with just the id, and perhaps some other fields, too
		Entity entry = m_user.newResource(container, id, others);

		// form the XML and SQL for the insert
		Document doc = StorageUtils.createDocument();
		entry.toXml(doc, new Stack());
		String xml = StorageUtils.writeDocumentToString(doc);

		String statement = doubleStorageSql.getInsertSql3(m_resourceTableName, insertFields(m_containerTableIdField, m_resourceTableIdField,
				m_resourceTableOtherFields, "XML"), valuesParams(m_resourceTableOtherFields));
		Object[] flds = m_user.storageFields(entry);

		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 3];
		System.arraycopy(flds, 0, fields, 2, flds.length);
		fields[0] = container.getReference();
		fields[1] = entry.getId();
		fields[fields.length - 1] = xml;

		// process the insert
		boolean ok = m_sql.dbWrite(statement, fields);

		// if this failed, assume a key conflict (i.e. id in use)
		if (!ok) return null;

		// now get a lock on the record for edit
		Edit edit = editResource(container, id);
		if (edit == null)
		{
			log.warn("putResource(): didn't get a lock!");
			return null;
		}

		return edit;
	}

	/**
	 * Get a lock on the Resource with this id, or null if a lock cannot be gotten.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param id
	 *        The user id.
	 * @return The locked Resource with this id, or null if this records cannot be locked.
	 */
	public Edit editResource(Entity container, String id)
	{
		Edit edit = null;

		if (m_locksAreInDb)
		{
			if ("oracle".equals(m_sql.getVendor()))
			{
				// read the record and get a lock on it (non blocking)
				String statement = doubleStorageSql.getSelectXml6Sql(m_resourceTableName, m_resourceTableIdField, m_resourceTableContainerIdField,
				        StorageUtils.escapeSql(id), StorageUtils.escapeSql(container.getReference()));
				StringBuilder result = new StringBuilder();
				Connection lock = m_sql.dbReadLock(statement, result);

				// for missing or already locked...
				if ((lock == null) || (result.length() == 0)) return null;

				// make first a Resource, then an Edit
				Entity entry = readResource(container, result.toString());
				edit = m_user.newResourceEdit(container, entry);

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
			// get the entry, and check for existence
			Entity entry = getResource(container, id);
			if (entry == null) return null;

			// write a lock to the lock table - if we can do it, we get the lock
			String statement = doubleStorageSql.getInsertSql2();

			// we need session id and user id
			String sessionId = UsageSessionService.getSessionId();
			if (sessionId == null)
			{
				sessionId = "";
			}

			// collect the fields
			Object fields[] = new Object[4];
			fields[0] = m_resourceTableName;
			fields[1] = doubleStorageSql.getRecordId(container.getReference() + "/" + id);
			fields[2] = TimeService.newTime();
			fields[3] = sessionId;

			// add the lock - if fails, someone else has the lock
			boolean ok = m_sql.dbWriteFailQuiet(null, statement, fields);
			if (!ok)
			{
				return null;
			}

			// make the edit from the Resource
			edit = m_user.newResourceEdit(container, entry);
		}

		// otherwise, get the lock locally
		else
		{
			// get the entry, and check for existence
			Entity entry = getResource(container, id);
			if (entry == null) return null;

			// we only sync this getting - someone may release a lock out of sync
			synchronized (m_locks)
			{
				// if already locked
				if (m_locks.containsKey(entry.getReference())) return null;

				// make the edit from the Resource
				edit = m_user.newResourceEdit(container, entry);

				// store the edit in the locks by reference
				m_locks.put(entry.getReference(), edit);
			}
		}

		return edit;
	}

	/**
	 * Commit the changes and release the lock.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param user
	 *        The Edit to commit.
	 */
	public void commitResource(Entity container, Edit edit)
	{
		// form the SQL statement and the var w/ the XML
		Document doc = StorageUtils.createDocument();
		edit.toXml(doc, new Stack());
		String xml = StorageUtils.writeDocumentToString(doc);
		String statement = doubleStorageSql.getUpdate2Sql(m_resourceTableName, m_resourceTableIdField, m_resourceTableContainerIdField,
				updateSet(m_resourceTableOtherFields));

		Object[] flds = m_user.storageFields(edit);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 3];
		System.arraycopy(flds, 0, fields, 0, flds.length);
		fields[fields.length - 3] = xml;
		fields[fields.length - 2] = container.getReference();
		fields[fields.length - 1] = edit.getId();

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
			statement = doubleStorageSql.getDeleteLocksSql();

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_resourceTableName;
			lockFields[1] = doubleStorageSql.getRecordId(container.getReference() + "/" + edit.getId());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				log.warn("commitResource: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
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

	/**
	 * Cancel the changes and release the lock.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param user
	 *        The Edit to cancel.
	 */
	public void cancelResource(Entity container, Edit edit)
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
			String statement = doubleStorageSql.getDeleteLocksSql();

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_resourceTableName;
			lockFields[1] = doubleStorageSql.getRecordId(container.getReference() + "/" + edit.getId());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				log.warn("cancelResource: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
			}
		}

		else
		{
			// release the lock
			m_locks.remove(edit.getReference());
		}
	}

	/**
	 * Remove this (locked) Resource.
	 * 
	 * @param container
	 *        The container for this resource.
	 * @param user
	 *        The Edit to remove.
	 */
	public void removeResource(Entity container, Edit edit)
	{
		// form the SQL delete statement
		String statement = doubleStorageSql.getDelete2Sql(m_resourceTableName, m_resourceTableIdField, m_resourceTableContainerIdField);
		Object[] fields = new Object[2];
		fields[0] = container.getReference();
		fields[1] = edit.getId();

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
			statement = doubleStorageSql.getDeleteLocksSql();

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_resourceTableName;
			lockFields[1] = doubleStorageSql.getRecordId(container.getReference() + "/" + edit.getId());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				log.warn("removeResource: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
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
	 * @param before1
	 *        The first field name.
	 * @param before2
	 *        (options) second field name.
	 * @param values
	 *        The extra field names, in the middle.
	 * @param after
	 *        The last field name.
	 * @return A sql statement fragment for the insert fields.
	 */
	protected String insertFields(String before1, String before2, String[] fields, String after)
	{
		StringBuilder buf = new StringBuilder();
		buf.append(" (");

		buf.append(before1);
		buf.append(",");

		if (before2 != null)
		{
			buf.append(before2);
			buf.append(",");
		}

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
	 * Get resources filtered by date and count and drafts, in descending (latest first) order
	 * 
	 * @param afterDate
	 *        if null, no date limit, else limited to only messages after this date.
	 * @param limitedToLatest
	 *        if 0, no count limit, else limited to only the latest this number of messages.
	 * @param draftsForId
	 *        how to handle drafts: null means no drafts, "*" means all, otherwise drafts only if created by this userId.
	 * @param pubViewOnly
	 *        if true, include only messages marked pubview, else include any.
	 * @return A list of Message objects that meet the criteria; may be empty
	 */
	public List getResources(final Entity container, Time afterDate, int limitedToLatest, String draftsForId, boolean pubViewOnly)
	{
		// if we are limiting, and are filtering out drafts or doing pubview, and don't have draft/owner/pubview support, filter here after
		boolean canLimit = true;
		boolean filterAfter = false;
		if ((limitedToLatest > 0)
				&& ((((m_resourceTableDraftField == null) || (m_resourceTableOwnerField == null)) && (!"*".equals(draftsForId))) || ((m_resourceTablePubViewField == null) && pubViewOnly)))
		{
			canLimit = false;
			filterAfter = true;
		}

		StringBuilder buf = new StringBuilder();
		int numFields = 1;

		// start the outer statement, later finished with a limiting clause
		if ((limitedToLatest > 0) && canLimit)
		{
			if ("oracle".equals(m_sql.getVendor()))
			{
				buf.append("select XML from (");
				buf.append("select XML from " + m_resourceTableName);
			}
			else if ("mysql".equals(m_sql.getVendor()))
			{
				buf.append("select messages.XML from (");
				buf.append("select XML from " + m_resourceTableName);
			}
			else
			// if ("hsqldb".equals(m_sql.getVendor()))
			{
				// according to SQL2000 specification (used by HSQLDB) the limit clause appears first
				buf.append("select limit 0 " + limitedToLatest + " XML from " + m_resourceTableName);
			}
		}
		else
		{
			buf.append("select XML from " + m_resourceTableName);
		}

		buf.append(" where (" + m_resourceTableContainerIdField + " = ?");

		if ((m_resourceTableOrderField != null) && (afterDate != null))
		{
			buf.append(" and " + m_resourceTableOrderField + " > ?");
			numFields++;
		}

		// deal with drafts if we can
		if ((m_resourceTableDraftField != null) && (m_resourceTableOwnerField != null))
		{
			// if draftsForId is null, we don't want any drafts
			if (draftsForId == null)
			{
				buf.append(" and " + m_resourceTableDraftField + " = '0'");
			}
			// else a "*" means we take all drafts
			else if (!"*".equals(draftsForId))
			{
				// we want only drafts if the owner field matches
				buf.append(" and ( " + m_resourceTableDraftField + " = '0' or " + m_resourceTableOwnerField + " = ? )");
				numFields++;
			}
		}

		// pubview
		if ((m_resourceTablePubViewField != null) && pubViewOnly)
		{
			buf.append(" and " + m_resourceTablePubViewField + " = '1'");
		}

		// close the where
		buf.append(")");

		if (m_resourceTableOrderField != null)
		{
			buf.append(" order by " + m_resourceTableOrderField + " desc");
		}

		boolean useLimitField = false;
		if ((limitedToLatest > 0) && canLimit)
		{
			if ("oracle".equals(m_sql.getVendor()))
			{
				buf.append(" ) where rownum <= ?");
				numFields++;
				useLimitField = true;
			}
			else if ("mysql".equals(m_sql.getVendor()))
			{
				buf.append(" ) AS messages LIMIT " + limitedToLatest);
				useLimitField = false;
			}
         else
			// if ("hsqldb".equals(m_sql.getVendor()))
			{
				// the limit clause appears elsewhere in HSQLDB SQL statements, not here.
			}
		}

		// build up the fields
		Object fields[] = new Object[numFields];
		fields[0] = container.getReference();
		int pos = 1;
		if ((m_resourceTableOrderField != null) && (afterDate != null))
		{
			fields[pos++] = afterDate;
		}
		if ((m_resourceTableDraftField != null) && (m_resourceTableOwnerField != null) && (draftsForId != null) && (!"*".equals(draftsForId)))
		{
			fields[pos++] = draftsForId;
		}
		if (useLimitField)
		{
			fields[pos++] = Integer.valueOf(limitedToLatest);
		}

		List all = m_sql.dbRead(buf.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					// get the xml and parse into a Resource
					String xml = result.getString(1);
					Entity entry = readResource(container, xml);
					return entry;
				}
				catch (SQLException ignore)
				{
					return null;
				}
			}
		});

		// after filter for draft / pubview and limit
		if (filterAfter)
		{
			Vector v = new Vector();

			// deal with drafts / pubview
			for (Iterator i = all.iterator(); i.hasNext();)
			{
				Entity r = (Entity) i.next();
				Entity candidate = null;
				if (m_user.isDraft(r))
				{
					// if some drafts
					if ((draftsForId != null) && (m_user.getOwnerId(r).equals(draftsForId)))
					{
						candidate = r;
					}
				}
				else
				{
					candidate = r;
				}

				// if we have a candidate to add, and we need pub view only
				if ((candidate != null) && pubViewOnly)
				{
					// if this is not pub view, skip it
					if ((candidate.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null))
					{
						candidate = null;
					}
				}

				if (candidate != null)
				{
					v.add(candidate);
				}
			}

			// pick what we need
			if (limitedToLatest < v.size())
			{
				all = v.subList(0, limitedToLatest);
			}
			else
			{
				all = v;
			}
		}

		return all;
	}

	/**
	 * Access a list of container ids match (start with) the root.
	 * 
	 * @param context
	 *        The id root to match.
	 * @return A List (String) of container id which match the root.
	 */
	public List getContainerIdsMatching(String root)
	{
		// the id of each container will be the part that follows the root reference
		final int pos = root.length();

		// read all users from the db
		String sql = doubleStorageSql.getSelect9Sql(m_containerTableName, m_containerTableIdField);
		Object fields[] = new Object[1];
		fields[0] = root + "%";

		List all = m_sql.dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					// get the reference form and pull off the id (what follows after the root)
					String ref = result.getString(1);
					String id = ref.substring(pos);
					return id;
				}
				catch (SQLException ignore)
				{
					return null;
				}
			}
		});

		return all;
	}
}
