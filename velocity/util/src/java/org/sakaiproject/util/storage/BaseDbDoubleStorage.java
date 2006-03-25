/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.sakaiproject.service.framework.log.cover.Log;
import org.sakaiproject.service.framework.log.cover.Logger;
import org.sakaiproject.service.framework.session.cover.UsageSessionService;
import org.sakaiproject.service.framework.sql.SqlReader;
import org.sakaiproject.service.framework.sql.SqlService;
import org.sakaiproject.service.legacy.entity.Edit;
import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.time.Time;
import org.sakaiproject.service.legacy.time.cover.TimeService;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.xml.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* <p>BaseDbDoubleStorage is a class that stores Resources (of some type) in a database, %%%
* provides locked access, and generally implements a services "storage" class.  The
* service's storage class can extend this to provide covers to turn Resource and
* Edit into something more type specific to the service.</p>
*
* Note: the methods here are all "id" based, with the following assumptions:
* - just the Resource Id field is enough to distinguish one Resource from another
*   (or, for resource, the container's id and the resource id).
* - a resource's reference is based on no more than the resource id, for containers, and 
*   no more than resource and container id for resources.
* - a resource's id and container id cannot change.
*
* In order to handle Unicode characters properly, the SQL statements executed by this class 
* should not embed Unicode characters into the SQL statement text; rather, Unicode values
* should be inserted as fields in a PreparedStatement.  Databases handle Unicode better in fields.
* 
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
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
	protected StorageUser m_user = null;

	/** Locks, keyed by reference, holding Connections (or, if locks are done locally, holding an Edit).
	Note: keying by reference allows botu container and resource locks to be stored, the reference distinguishes them.*/
	protected Hashtable m_locks = null;

	/** For container, the extra field is (no longer used) NEXT_ID */
	protected static final String[] M_containerExtraFields = { "NEXT_ID" };

	/** Injected (by constructor) SqlService. */
	protected SqlService m_sql = null;

	/**
	* Construct.
	* @param containerTableName Table name for containers.
	* @param containerTableIdField The field in the container table that holds the id.
	* @param resourceTableName Table name for resources.
	* @param resourceTableIdField The field in the resource table that holds the id.
	* @param resourceTableContainerIdField The field in the resource table that holds the container id.
	* @param resourceTableOrderField The field in the resource table that is used for ordering results.
	* @param resourceTableOtherFields The other fields in the resource table
	* (between the two id fields and the xml field).
	* @param locksInDb If true, we do our locks in the remote database, otherwise we do them here.
	* @param containerEntryName The xml tag name for the element holding each actual container entry.
	* @param resourceEntryName The xml tag name for the element holding each actual resource entry.
	* @param user The StorageUser class to call back for creation of Resource and Edit objects.
	* @param sqlService The SqlService.
	*/
	public BaseDbDoubleStorage(String containerTableName, String containerTableIdField,
						String resourceTableName, String resourceTableIdField,
						String resourceTableContainerIdField, String resourceTableOrderField,
						String resourceTableOwnerField, String resourceTableDraftField, String resourceTablePubViewField,
						String[] resourceTableOtherFields, boolean locksInDb,
						String containerEntryName, String resourceEntryName,
						StorageUser user, SqlService sqlService)
	{
		m_containerTableName = containerTableName;
		m_containerTableIdField = containerTableIdField;
		m_resourceTableName = resourceTableName;
		m_resourceTableIdField = resourceTableIdField;
		m_resourceTableContainerIdField = resourceTableContainerIdField;
		m_resourceTableOrderField = resourceTableOrderField;
		m_resourceTableOtherFields = resourceTableOtherFields;
		m_locksAreInDb = locksInDb;
		m_containerEntryTagName = containerEntryName;
		m_resourceEntryTagName = resourceEntryName;
		m_resourceTableOwnerField = resourceTableOwnerField;
		m_resourceTableDraftField = resourceTableDraftField;
		m_resourceTablePubViewField = resourceTablePubViewField;
		m_user = user;
		m_sql = sqlService;

	}	// BaseDbDoubleStorage
		
	/**
	* Open and be ready to read / write.
	*/
	public void open()
	{
		// setup for locks
		m_locks = new Hashtable();

	}   // open

	/**
	* Close.
	*/
	public void close()
	{
		if (!m_locks.isEmpty())
		{
			Log.warn("chef", this + ".close(): locks remain!");
			// %%%
		}
		m_locks.clear();
		m_locks = null;

	}   // close

	/**
	* Read one Container Resource from xml
	* @param xml An string containing the xml which describes the Container resource.
	* @return The Container Resource object created from the xml.
	*/
	protected Entity readContainer(String xml)
	{
		try
		{
			// read the xml
			Document doc =  Xml.readDocumentFromString(xml);

			// verify the root element
			Element root = doc.getDocumentElement();
			if (!root.getTagName().equals(m_containerEntryTagName))
			{
				Log.warn("chef", this + ".readContainer(): not = "
							+ m_containerEntryTagName + " : " + root.getTagName());
				return null;
			}

			// re-create a resource
			Entity entry = m_user.newContainer(root);

			return entry;
		}
		catch (Exception e)
		{
			Log.debug("chef", this + ".readContainer(): ", e);
			return null;
		}

	}   // readContainer

	/**
	* Check if a Container by this id exists.
	* @param ref The container reference.
	* @return true if a Container by this id exists, false if not.
	*/
	public boolean checkContainer(String ref)
	{
		// just see if the record exists
		String sql =
				"select " + m_containerTableIdField + "  from " + m_containerTableName
			+	" where ( " + m_containerTableIdField + " = ? )";
		Object[] fields = new Object[1];
		fields[0] = ref;
		List ids = m_sql.dbRead(sql, fields, null);
		return (!ids.isEmpty());

	}	// checkContainer

	/**
	* Get the Container with this id, or null if not found.
	* @param ref The container reference.
	* @return The Container with this id, or null if not found.
	*/
	public Entity getContainer(String ref)
	{
		Entity entry = null;

		// get the user from the db
		String sql =
				"select XML from " + m_containerTableName
			+   " where ( " + m_containerTableIdField + " = ? )";
		Object[] fields = new Object[1];
		fields[0] = ref;
		
		List xml = m_sql.dbRead(sql, fields, null);
		if (!xml.isEmpty())
		{
			// create the Resource from the db xml
			entry = readContainer((String) xml.get(0));
		}

		return entry;

	}   // getContainer

	/**
	* Get all Containers.
	* @return The list (Resource) of all Containers.
	*/
	public List getAllContainers()
	{
		List all = new Vector();

		// read all users from the db
		String sql = "select XML from " + m_containerTableName;
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

	}   // getAllContainers

	/**
	* Add a new Container with this id.
	* @param ref The container reference.
	* @return The locked Container object with this id, or null if the id is in use.
	*/
	public Edit putContainer(String ref)
	{
		// create one with just the id
		Entity entry = m_user.newContainer(ref);

		// form the XML and SQL for the insert
		Document doc = Xml.createDocument();
		entry.toXml(doc, new Stack());
		String xml = Xml.writeDocumentToString(doc);

		String statement =
				"insert into " + m_containerTableName
			+	insertFields(m_containerTableIdField, null, M_containerExtraFields, "XML")
			+   " values ( ? "
			+	",'0'"	// %%% was next id, no longer used (but still in db) -ggolden
			+   ", ? )";
		
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
			Log.warn("chef", this + ".putContainer(): didn't get a lock!");
			return null;
		}
		
		return edit;

	}   // putContainer

	/**
	* Get a lock on the Container with this id, or null if a lock cannot be gotten.
	* @param ref The container reference.
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
				String statement =
						"select XML from " + m_containerTableName
					+   " where ( " + m_containerTableIdField + " = '"
					+	Validator.escapeSql(ref) + "' )"
					+	" for update nowait";
				StringBuffer result = new StringBuffer();
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
			String statement =
					"insert into SAKAI_LOCKS"
				+	" (TABLE_NAME,RECORD_ID,LOCK_TIME,USAGE_SESSION_ID)"
				+	" values (?, ?, ?, ?)";

			// we need session id
			String sessionId = UsageSessionService.getSessionId();
			if (sessionId == null)
			{
				sessionId = "";
			}

			// collect the fields
			Object fields[] = new Object[4];
			fields[0] = m_containerTableName;
			fields[1] = internalRecordId(ref);
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

	}	// editContainer

	/**
	* Commit the changes and release the lock.
	* @param user The Edit to commit.
	*/
	public void commitContainer(Edit edit)
	{
		// form the SQL statement and the var w/ the XML
		Document doc = Xml.createDocument();
		edit.toXml(doc, new Stack());
		String xml = Xml.writeDocumentToString(doc);
		String statement =
				"update " + m_containerTableName
			+   " set XML = ?"
			+   " where " + m_containerTableIdField + " = ? ";

		Object[] fields = new Object[2];
		fields[0] = xml;
		fields[1] = edit.getReference();
		
		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				Log.warn("chef", this + ".commitContainer(): edit not in locks");
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
			statement =
					"delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_containerTableName;
			lockFields[1] = internalRecordId(edit.getReference());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				Logger.warn(this + ".commitContainer: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
			}
		}

		else
		{
			// just process the update
			m_sql.dbWrite(statement, fields);

			// remove the lock
			m_locks.remove(edit.getReference());
		}

	}	// commitContainer

	/**
	* Cancel the changes and release the lock.
	* @param user The Edit to cancel.
	*/
	public void cancelContainer(Edit edit)
	{
		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				Log.warn("chef", this + ".cancelContainer(): edit not in locks");
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
			String statement =
					"delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_containerTableName;
			lockFields[1] = internalRecordId(edit.getReference());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				Logger.warn(this + ".cancelContainer: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
			}
		}

		else
		{
			// release the lock
			m_locks.remove(edit.getReference());
		}

	}	// cancelContainer

	/**
	* Remove this (locked) Container.
	* @param user The Edit to remove.
	*/
	public void removeContainer(Edit edit)
	{
		// form the SQL delete statement
		String statement =
				"delete from " + m_containerTableName
			+   " where " + m_containerTableIdField + " = ? ";
		Object[] fields = new Object[1];
		fields[0] = edit.getReference();
		
		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				Log.warn("chef", this + ".removeContainer(): edit not in locks");
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
			statement =
					"delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_containerTableName;
			lockFields[1] = internalRecordId(edit.getReference());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				Logger.warn(this + ".remove: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
			}
		}

		else
		{
			// process the delete statement
			m_sql.dbWrite(statement, fields);

			// release the lock
			m_locks.remove(edit.getReference());
		}

	}   // removeContainer

	/**
	* Read one Resource from xml
	* @param container The container for this resource.
	* @param xml An string containing the xml which describes the resource.
	* @return The Resource object created from the xml.
	*/
	protected Entity readResource(Entity container, String xml)
	{
		try
		{
			// read the xml
			Document doc =  Xml.readDocumentFromString(xml);

			// verify the root element
			Element root = doc.getDocumentElement();
			if (!root.getTagName().equals(m_resourceEntryTagName))
			{
				Log.warn("chef", this + ".readResource(): not = "
							+ m_resourceEntryTagName + " : " + root.getTagName());
				return null;
			}

			// re-create a resource
			Entity entry = m_user.newResource(container, root);

			return entry;
		}
		catch (Exception e)
		{
			Log.debug("chef", this + ".readResource(): ", e);
			return null;
		}

	}   // readResource

	/**
	* Check if a Resource by this id exists.
	* @param container The container for this resource.
	* @param id The id.
	* @return true if a Resource by this id exists, false if not.
	*/
	public boolean checkResource(Entity container, String id)
	{
		// just see if the record exists
		String sql =
				"select " + m_resourceTableIdField + "  from " + m_resourceTableName
			+	" where (" + m_resourceTableContainerIdField + " = ? )"
			+   " and ( " + m_resourceTableIdField + " = ? )";
		Object[] fields = new Object[2];
		fields[0] = container.getReference();
		fields[1] = id;			
		List ids = m_sql.dbRead(sql, fields, null);
		return (!ids.isEmpty());

	}	// check

	/**
	* Get the Resource with this id, or null if not found.
	* @param container The container for this resource.
	* @param id The id.
	* @return The Resource with this id, or null if not found.
	*/
	public Entity getResource(Entity container, String id)
	{
		Entity entry = null;

		// get the user from the db
		String sql =
				"select XML from " + m_resourceTableName
			+	" where (" + m_resourceTableContainerIdField + " = ? )"
			+   " and ( " + m_resourceTableIdField + " = ? )";
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

	}   // getResource

	/**
	* Get all Resources.
	* @param container The container for this resource.
	* @return The list (Resource) of all Resources.
	*/
	public List getAllResources(Entity container)
	{
		List all = new Vector();
		
		// read all users from the db
		String sql = "select XML from " + m_resourceTableName
			+	" where (" + m_resourceTableContainerIdField + " = ? )"
			+	((m_resourceTableOrderField != null) ? (" order by " + m_resourceTableOrderField + " asc") : "");
		Object[] fields = new Object[1];
		fields[0] = container.getReference();
		List xml = m_sql.dbRead(sql, fields, null);

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readResource(container, (String) xml.get(i));
				if (entry != null) all.add(entry);
			}
		}

		return all;

	}   // getAllResources

	/**
	* Add a new Resource with this id.
	* @param container The container for this resource.
	* @param id The id.
	* @param others Other fields for the newResource call
	* @return The locked Resource object with this id, or null if the id is in use.
	*/
	public Edit putResource(Entity container, String id, Object[] others)
	{
		// create one with just the id, and perhaps some other fields, too
		Entity entry = m_user.newResource(container, id, others);

		// form the XML and SQL for the insert
		Document doc = Xml.createDocument();
		entry.toXml(doc, new Stack());
		String xml = Xml.writeDocumentToString(doc);
		
		String statement =
				"insert into " + m_resourceTableName
			+	insertFields(m_containerTableIdField, m_resourceTableIdField, m_resourceTableOtherFields, "XML")
			+   " values ( "
			+   "?, ?, "
			+	valuesParams(m_resourceTableOtherFields)
			+   " ? )";		
		
		Object[] flds = m_user.storageFields(entry);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length+3];
		System.arraycopy(flds, 0, fields, 2, flds.length);
		fields[0] = container.getReference();
		fields[1] = entry.getId();
		fields[fields.length-1] = xml;

		// process the insert
		boolean ok = m_sql.dbWrite(statement, fields);

		// if this failed, assume a key conflict (i.e. id in use)
		if (!ok) return null;

		// now get a lock on the record for edit
		Edit edit = editResource(container, id);
		if (edit == null)
		{
			Log.warn("chef", this + ".putResource(): didn't get a lock!");
			return null;
		}
		
		return edit;

	}   // putResource

	/**
	* Get a lock on the Resource with this id, or null if a lock cannot be gotten.
	* @param container The container for this resource.
	* @param id The user id.
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
				String statement =
					"select XML from " + m_resourceTableName
					+	" where (" + m_resourceTableContainerIdField + " ='"
					+	Validator.escapeSql(container.getReference()) + "' )"
					+   " and ( " + m_resourceTableIdField + " = '"
					+	Validator.escapeSql(id) + "' )"
					+	" for update nowait";
				StringBuffer result = new StringBuffer();
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
			String statement =
					"insert into SAKAI_LOCKS"
				+	" (TABLE_NAME,RECORD_ID,LOCK_TIME,USAGE_SESSION_ID)"
				+	" values (?, ?, ?, ?)";

			// we need session id and user id
			String sessionId = UsageSessionService.getSessionId();
			if (sessionId == null)
			{
				sessionId = "";
			}

			// collect the fields
			Object fields[] = new Object[4];
			fields[0] = m_resourceTableName;
			fields[1] = internalRecordId(container.getReference() + "/" + id);
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

	}	// editResource

	/**
	* Commit the changes and release the lock.
	* @param container The container for this resource.
	* @param user The Edit to commit.
	*/
	public void commitResource(Entity container, Edit edit)
	{
		// form the SQL statement and the var w/ the XML
		Document doc = Xml.createDocument();
		edit.toXml(doc, new Stack());
		String xml = Xml.writeDocumentToString(doc);
		String statement =
				"update " + m_resourceTableName
			// %%% others
			+	" set "
			+ 	updateSet(m_resourceTableOtherFields)
			+   " XML = ?"
			+	" where (" + m_resourceTableContainerIdField + " = ? )"
			+   " and ( " + m_resourceTableIdField + " = ? )";

		Object[] flds = m_user.storageFields(edit);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length+3];
		System.arraycopy(flds, 0, fields, 0, flds.length);
		fields[fields.length-3] = xml;
		fields[fields.length-2] = container.getReference();
		fields[fields.length-1] = edit.getId();
		
		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				Log.warn("chef", this + ".commitResource(): edit not in locks");
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
			statement =
					"delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_resourceTableName;
			lockFields[1] = internalRecordId(container.getReference() + "/" + edit.getId());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				Logger.warn(this + ".commitResource: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
			}
		}

		else
		{
			// just process the update
			m_sql.dbWrite(statement, fields);

			// remove the lock
			m_locks.remove(edit.getReference());
		}

	}	// commitResource

	/**
	* Cancel the changes and release the lock.
	* @param container The container for this resource.
	* @param user The Edit to cancel.
	*/
	public void cancelResource(Entity container, Edit edit)
	{
		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				Log.warn("chef", this + ".cancelResource(): edit not in locks");
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
			String statement =
					"delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_resourceTableName;
			lockFields[1] = internalRecordId(container.getReference() + "/" + edit.getId());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				Logger.warn(this + ".cancelResource: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
			}
		}

		else
		{
			// release the lock
			m_locks.remove(edit.getReference());
		}

	}	// cancelResource

	/**
	* Remove this (locked) Resource.
	* @param container The container for this resource.
	* @param user The Edit to remove.
	*/
	public void removeResource(Entity container, Edit edit)
	{
		// form the SQL delete statement
		String statement =
				"delete from " + m_resourceTableName
				+	" where (" + m_resourceTableContainerIdField + " = ? )"
				+   " and ( " + m_resourceTableIdField + " = ? )";
		Object[] fields = new Object[2];
		fields[0] = container.getReference();
		fields[1] = edit.getId();
		
		if (m_locksAreInDb)
		{
			// use this connection that is stored with the lock
			Connection lock = (Connection) m_locks.get(edit.getReference());
			if (lock == null)
			{
				Log.warn("chef", this + ".removeResource(): edit not in locks");
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
			statement =
					"delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";

			// collect the fields
			Object lockFields[] = new Object[2];
			lockFields[0] = m_resourceTableName;
			lockFields[1] = internalRecordId(container.getReference() + "/" + edit.getId());
			boolean ok = m_sql.dbWrite(statement, lockFields);
			if (!ok)
			{
				Logger.warn(this + ".removeResource: missing lock for table: " + lockFields[0] + " key: " + lockFields[1]);
			}
		}

		else
		{
			// process the delete statement
			m_sql.dbWrite(statement, fields);

			// release the lock
			m_locks.remove(edit.getReference());
		}

	}   // removeResource

	/**
	* Form a string of n question marks with commas, for sql value statements, one for each
	* item in the values array, or an empty string if null.
	* @param values The values to be inserted into the sql statement.
	* @return A sql statement fragment for the values part of an insert, one for each value in the array.
	*/
	protected String valuesParams(String[] fields)
	{
		if ((fields == null) || (fields.length == 0)) return "";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < fields.length; i++)
		{
			buf.append(" ?,");
		}
		return buf.toString();

	}	// valuesParams

	/**
	* Form a string of n name=?, for sql update set statements, one for each
	* item in the values array, or an empty string if null.
	* @param values The values to be inserted into the sql statement.
	* @return A sql statement fragment for the values part of an insert, one for each value in the array.
	*/
	protected String updateSet(String[] fields)
	{
		if ((fields == null) || (fields.length == 0)) return "";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < fields.length; i++)
		{
			buf.append(fields[i] + " = ?,");
		}
		return buf.toString();

	}	// updateSet

	/**
	* Form a string of (field, field, field), for sql insert statements, one for each
	* item in the fields array, plus one before, and one after.
	* @param before1 The first field name.
	* @param before2 (options) second field name.
	* @param values The extra field names, in the middle.
	* @param after The last field name.
	* @return A sql statement fragment for the insert fields.
	*/
	protected String insertFields(String before1, String before2, String[] fields, String after)
	{
		StringBuffer buf = new StringBuffer();
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

	}	// insertFields

	/**
	 * Get resources filtered by date and count and drafts, in descending (latest first) order
	 * @param afterDate if null, no date limit, else limited to only messages after this date.
	 * @param limitedToLatest if 0, no count limit, else limited to only the latest this number of messages.
	 * @param draftsForId how to handle drafts: null means no drafts, "*" means all, otherwise drafts only if
	 * created by this userId.
	 * @param pubViewOnly if true, include only messages marked pubview, else include any.
	 * @return A list of Message objects that meet the criteria; may be empty
	 */
	public List getResources(final Entity container, Time afterDate, int limitedToLatest, String draftsForId, boolean pubViewOnly)
	{
		// if we are limiting, and are filtering out drafts or doing pubview, and don't have draft/owner/pubview support, filter here after
		boolean canLimit = true;
		boolean filterAfter = false;
		if (	(limitedToLatest > 0)
			&&	(	(((m_resourceTableDraftField == null) || (m_resourceTableOwnerField == null)) && (!"*".equals(draftsForId)))
				||	((m_resourceTablePubViewField == null) && pubViewOnly)
				)
			)
		{
			canLimit = false;
			filterAfter = true;
		}

		StringBuffer buf = new StringBuffer();
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
			else //if ("hsqldb".equals(m_sql.getVendor()))
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
			else //if ("hsqldb".equals(m_sql.getVendor()))
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
			fields[pos++] = new Integer(limitedToLatest);
		}

		List all = m_sql.dbRead(buf.toString(), fields,
			new SqlReader()
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
					catch (SQLException ignore) { return null;}
				}
			} );

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
	 * @param context The id root to match.
	 * @return A List (String) of container id which match the root.
	 */
	public List getContainerIdsMatching(String root)
	{
		// the id of each container will be the part that follows the root reference
		final int pos = root.length();

		// read all users from the db
		String sql = "select " + m_containerTableIdField + " from " + m_containerTableName
				+ " where " + m_containerTableIdField + " like ?";
		Object fields[] = new Object[1];
		fields[0] = root + "%";

		List all = m_sql.dbRead(sql, fields,
			new SqlReader()
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
					catch (SQLException ignore) { return null;}
				}
			} );

		return all;
	}

	/**
	 * Return a record ID to use internally in the database.
	 * This is needed for databases (MySQL) that have limits on key lengths.  
	 * The hash code ensures that the record ID will be unique, 
	 * even if the DB only considers a prefix of a very long record ID.
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
	    else // oracle, hsqldb
		{
	        return recordId;
		}
	}	
}   // BaseDbDoubleStorage



