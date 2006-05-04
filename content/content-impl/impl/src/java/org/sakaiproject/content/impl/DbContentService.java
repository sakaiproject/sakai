/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.LockManager;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.BaseDbSingleStorage;
import org.sakaiproject.util.StorageUser;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * DbContentService is an extension of the BaseContentService with a database implementation.
 * </p>
 * <p>
 * The sql scripts in src/sql/chef_content.sql must be run on the database.
 * </p>
 */
public class DbContentService extends BaseContentService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(DbContentService.class);

	/** Table name for collections. */
	protected String m_collectionTableName = "CONTENT_COLLECTION";

	/** Table name for resources. */
	protected String m_resourceTableName = "CONTENT_RESOURCE";

	/** Table name for resources. */
	protected String m_resourceBodyTableName = "CONTENT_RESOURCE_BODY_BINARY";
	
	/** Table name for entity-group relationships. */
	protected String m_groupTableName = "CONTENT_ENTITY_GROUPS";

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_locksInDb = true;

	/** The extra field(s) to write to the database - collections. */
	protected static final String[] COLLECTION_FIELDS = { "IN_COLLECTION" };

	/** The extra field(s) to write to the database - resources - when we are doing bodys in files. */
	protected static final String[] RESOURCE_FIELDS_FILE = { "IN_COLLECTION", "FILE_PATH" };

	/** The extra field(s) to write to the database - resources - when we are doing bodys the db. */
	protected static final String[] RESOURCE_FIELDS = { "IN_COLLECTION" };
	
	/** Table name for resources delete. */
	protected String m_resourceDeleteTableName = "CONTENT_RESOURCE_DELETE";

	/** Table name for resources delete. */
	protected String m_resourceBodyDeleteTableName = "CONTENT_RESOURCE_BODY_BINARY_DELETE";

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: LockManager */
	LockManager m_lockManager = null;

	/**
	 * Dependency: LockManager
	 * 
	 * @param service
	 *        The LockManager
	 */
	public void setLockManager(LockManager lockManager)
	{
		m_lockManager = lockManager;
	}

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}

	/**
	 * Configuration: set the table name for collections.
	 * 
	 * @param path
	 *        The table name for collections.
	 */
	public void setCollectionTableName(String name)
	{
		m_collectionTableName = name;
	}

	/**
	 * Configuration: set the table name for resources.
	 * 
	 * @param path
	 *        The table name for resources.
	 */
	public void setResourceTableName(String name)
	{
		m_resourceTableName = name;
	}

	/**
	 * Configuration: set the table name for resource body.
	 * 
	 * @param path
	 *        The table name for resource body.
	 */
	public void setResourceBodyTableName(String name)
	{
		m_resourceBodyTableName = name;
	}

	/**
	 * Configuration: set the locks-in-db
	 * 
	 * @param value
	 *        The locks-in-db value.
	 */
	public void setLocksInDb(String value)
	{
		m_locksInDb = new Boolean(value).booleanValue();
	}

	/** Set if we are to run the to-file conversion. */
	protected boolean m_convertToFile = false;

	/**
	 * Configuration: run the to-file conversion.
	 * 
	 * @param value
	 *        The conversion desired value.
	 */
	public void setConvertToFile(String value)
	{
		m_convertToFile = new Boolean(value).booleanValue();
	}

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = new Boolean(value).booleanValue();
	}

	// htripath-start
	public void setResourceDeleteTableName(String name)
	{
		m_resourceDeleteTableName = name;
	}

	public void setResourceBodyDeleteTableName(String name)
	{
		m_resourceBodyDeleteTableName = name;
	}

	// htripath-end
	
	public void setEntityGroupTableName(String name)
	{
		m_groupTableName = name;
	}
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_content");

				// add the delete table
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_content_delete");

				// do the 2.1.0 conversions
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_content_2_1_0");
			}

			super.init();

			// convert?
			if (m_convertToFile)
			{
				m_convertToFile = false;
				convertToFile();
			}

			M_log.info("init(): tables: " + m_collectionTableName + " " + m_resourceTableName + " " + m_resourceBodyTableName + " " 
					+ m_groupTableName + " locks-in-db: " + m_locksInDb + " bodyPath: " + m_bodyPath);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * 
	 */
	private int countQuery(String sql, String param) throws IdUnusedException
	{

		Object[] fields = new Object[1];
		fields[0] = param;

		List list = m_sqlService.dbRead(sql, fields, null);

		if (list != null)
		{
			int rv = 0;
			Iterator iter = list.iterator();
			if (iter.hasNext())
			{
				try
				{
					Object val = iter.next();
					rv = Integer.parseInt((String) val);
				}
				catch (Exception ignore)
				{
				}
			}
			return rv;
		}
		throw new IdUnusedException(param);
	}

	public int getCollectionSize(String id) throws IdUnusedException, TypeException, PermissionException
	{

		String wildcard;

		if (id.endsWith("/"))
		{
			wildcard = id + "%";
		}
		else
		{
			wildcard = id + "/%";
		}

		int fileCount = countQuery("select count(IN_COLLECTION) from CONTENT_RESOURCE where IN_COLLECTION like ?", wildcard);
		int folderCount = countQuery("select count(IN_COLLECTION) from CONTENT_COLLECTION where IN_COLLECTION like ?", wildcard);;
		return fileCount + folderCount;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * UUID Support
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * For a given id, return its UUID (creating it if it does not already exist)
	 */

	public String getUuid(String id)
	{

		String uuid = null;

		uuid = findUuid(id);

		if (uuid != null) return uuid;

		// UUID not found, so create one and store it

		IdManager uuidManager = (IdManager) ComponentManager.get(IdManager.class);
		uuid = uuidManager.createUuid();

		setUuidInternal(id, uuid);

		return uuid;
	}

	/**
	 * @param id
	 *        id of the resource to set the UUID for
	 * @param uuid
	 *        the new UUID of the resource
	 * @throws IdInvalidException
	 *         if the given resource already has a UUID set
	 */
	public void setUuid(String id, String uuid) throws IdInvalidException
	{
		String existingUuid = findUuid(id);
		if (existingUuid != null)
		{
			throw new IdInvalidException(id);
		}

		setUuidInternal(id, uuid);
	}

	protected void setUuidInternal(String id, String uuid)
	{
		try
		{
			// get a connection for the updates
			final Connection connection = m_sqlService.borrowConnection();
			boolean wasCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			// set any existing one to null
			String sql = "update CONTENT_RESOURCE set RESOURCE_UUID = ? where RESOURCE_UUID = ?";
			Object[] fields = new Object[2];
			fields[0] = null;
			fields[1] = uuid;
			m_sqlService.dbWrite(connection, sql, fields);

			sql = "update CONTENT_RESOURCE set RESOURCE_UUID = ? where RESOURCE_ID = ?";
			fields = new Object[2];
			fields[0] = uuid;
			fields[1] = id;
			m_sqlService.dbWrite(connection, sql, fields);

			connection.commit();
			connection.setAutoCommit(wasCommit);
			m_sqlService.returnConnection(connection);
		}
		catch (Throwable t)
		{
			M_log.warn("getUuid: failed: " + t);
		}
	}

	/**
	 * private utility method to search for UUID for given id
	 */

	private String findUuid(String id)
	{
		String sql = "select RESOURCE_UUID from CONTENT_RESOURCE where RESOURCE_ID=?";
		Object[] fields = new Object[1];
		fields[0] = id;

		String uuid = null;
		List result = m_sqlService.dbRead(sql, fields, null);

		if (result != null)
		{
			Iterator iter = result.iterator();
			if (iter.hasNext())
			{
				uuid = (String) iter.next();
			}
		}

		return uuid;
	}

	/**
	 * For a given UUID, attempt to lookup and return the corresponding id (URI)
	 */

	public String resolveUuid(String uuid)
	{

		String id = null;

		try
		{
			String sql = "select RESOURCE_ID from CONTENT_RESOURCE where RESOURCE_UUID=?";
			Object[] fields = new Object[1];
			fields[0] = uuid;

			List result = m_sqlService.dbRead(sql, fields, null);

			if (result != null)
			{
				Iterator iter = result.iterator();
				if (iter.hasNext())
				{
					id = (String) iter.next();
				}
			}
		}
		catch (Throwable t)
		{
			M_log.warn("resolveUuid: failed: " + t);
		}
		return id;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseContentService extensions
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		return new DbStorage(new CollectionStorageUser(), new ResourceStorageUser(), (m_bodyPath != null));

	} // newStorage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class DbStorage implements Storage
	{
		/** A storage for collections. */
		protected BaseDbSingleStorage m_collectionStore = null;

		/** A storage for resources. */
		protected BaseDbSingleStorage m_resourceStore = null;

		/** htripath- Storage for resources delete */
		protected BaseDbSingleStorage m_resourceDeleteStore = null;
		
		/**
		 * Construct.
		 * 
		 * @param collectionUser
		 *        The StorageUser class to call back for creation of collection objects.
		 * @param resourceUser
		 *        The StorageUser class to call back for creation of resource objects.
		 */
		public DbStorage(StorageUser collectionUser, StorageUser resourceUser, boolean bodyInFile)
		{
			// build the collection store - a single level store
			m_collectionStore = new BaseDbSingleStorage(m_collectionTableName, "COLLECTION_ID", COLLECTION_FIELDS, m_locksInDb,
					"collection", collectionUser, m_sqlService);

			// build the resources store - a single level store
			m_resourceStore = new BaseDbSingleStorage(m_resourceTableName, "RESOURCE_ID", (bodyInFile ? RESOURCE_FIELDS_FILE
					: RESOURCE_FIELDS), m_locksInDb, "resource", resourceUser, m_sqlService);

			// htripath-build the resource for store of deleted record-single level store
			m_resourceDeleteStore = new BaseDbSingleStorage(m_resourceDeleteTableName, "RESOURCE_ID",
					(bodyInFile ? RESOURCE_FIELDS_FILE : RESOURCE_FIELDS), m_locksInDb, "resource", resourceUser, m_sqlService);
			
		} // DbStorage

		/**
		 * Open and be ready to read / write.
		 */
		public void open()
		{
			m_collectionStore.open();
			m_resourceStore.open();
			m_resourceDeleteStore.open();
		} // open

		/**
		 * Close.
		 */
		public void close()
		{
			m_collectionStore.close();
			m_resourceStore.close();
			m_resourceDeleteStore.close();
		} // close

		/** Collections * */

		public boolean checkCollection(String id)
		{
			return m_collectionStore.checkResource(id);
		}

		public ContentCollection getCollection(String id)
		{
			return (ContentCollection) m_collectionStore.getResource(id);
		}

		/**
		 * Get a list of all getCollections within a collection.
		 */
		public List getCollections(ContentCollection collection)
		{
			// limit to those whose reference path (based on id) matches the collection id
			final String target = collection.getId();

			/*
			 * // read all the records, then filter them to accept only those in this collection // Note: this is not desirable, as the read is linear to the database site -ggolden List rv = m_collectionStore.getSelectedResources( new Filter() { public
			 * boolean accept(Object o) { // o is a String, the collection id return StringUtil.referencePath((String) o).equals(target); } } );
			 */

			// read the records with a where clause to let the database select those in this collection
			List rv = m_collectionStore.getAllResourcesWhere("IN_COLLECTION", target);

			return rv;

		} // getCollections

		public ContentCollectionEdit putCollection(String id)
		{
			return (ContentCollectionEdit) m_collectionStore.putResource(id, null);
		}

		public ContentCollectionEdit editCollection(String id)
		{
			return (ContentCollectionEdit) m_collectionStore.editResource(id);
		}

		// protected String externalResourceDeleteFileName(ContentResource resource)
		// {
		// return m_bodyPath + "/delete/" + ((BaseResourceEdit) resource).m_filePath;
		// }
		
		// htripath -end
		
		public void cancelResource(ContentResourceEdit edit)
		{
			// clear the memory image of the body
			byte[] body = ((BaseResourceEdit) edit).m_body;
			((BaseResourceEdit) edit).m_body = null;
		
			m_resourceStore.cancelResource(edit);
		}

		public void commitCollection(ContentCollectionEdit edit)
		{
			m_collectionStore.commitResource(edit);
		}

		public void cancelCollection(ContentCollectionEdit edit)
		{
			m_collectionStore.cancelResource(edit);
		}

		public void removeCollection(ContentCollectionEdit edit)
		{
			m_collectionStore.removeResource(edit);
		}

		/** Resources * */

		public boolean checkResource(String id)
		{
			return m_resourceStore.checkResource(id);
		}

		public ContentResource getResource(String id)
		{
			return (ContentResource) m_resourceStore.getResource(id);
		}

		public List getResources(ContentCollection collection)
		{
			// limit to those whose reference path (based on id) matches the collection id
			final String target = collection.getId();

			/*
			 * // read all the records, then filter them to accept only those in this collection // Note: this is not desirable, as the read is linear to the database site -ggolden List rv = m_resourceStore.getSelectedResources( new Filter() { public
			 * boolean accept(Object o) { // o is a String, the resource id return StringUtil.referencePath((String) o).equals(target); } } );
			 */

			// read the records with a where clause to let the database select those in this collection
			List rv = m_resourceStore.getAllResourcesWhere("IN_COLLECTION", target);

			return rv;

		} // getResources

		public List getFlatResources(String collectionId)
		{
			return m_resourceStore.getAllResourcesWhereLike("IN_COLLECTION", collectionId + "%");
		}

		public ContentResourceEdit putResource(String id)
		{
			return (ContentResourceEdit) m_resourceStore.putResource(id, null);
		}

		public ContentResourceEdit editResource(String id)
		{
			return (ContentResourceEdit) m_resourceStore.editResource(id);
		}

		public void commitResource(ContentResourceEdit edit) throws ServerOverloadException
		{
			// keep the body out of the XML
			byte[] body = ((BaseResourceEdit) edit).m_body;
			((BaseResourceEdit) edit).m_body = null;
			m_resourceStore.commitResource(edit);

			// update the resource body
			if (body != null)
			{
				// if we have been configured to use an external file system
				if (m_bodyPath != null)
				{
					boolean ok = putResourceBodyFilesystem(edit, body);
					if (!ok)
					{
						cancelResource(edit);
						throw new ServerOverloadException("failed to write file");
					}
				}

				// otherwise use the database
				else
				{
					putResourceBodyDb(edit, body);
				}
			}
		}

		// htripath - start
		/** Add resource to content_resouce_delete table for user deleted resources */
		public ContentResourceEdit putDeleteResource(String id, String uuid, String userId)
		{
			return (ContentResourceEdit) m_resourceDeleteStore.putDeleteResource(id, uuid, userId, null);
		}

		/**
		 * update xml and store the body of file TODO storing of body content is not used now.
		 */
		public void commitDeleteResource(ContentResourceEdit edit, String uuid)
		{
			// keep the body out of the XML
			byte[] body = ((BaseResourceEdit) edit).m_body;
			((BaseResourceEdit) edit).m_body = null;

			// update properties in xml and delete locks
			m_resourceDeleteStore.commitDeleteResource(edit, uuid);
		}

		public void removeResource(ContentResourceEdit edit)
		{
			// delete the body

			// if we have been configured to use an external file system
			if (m_bodyPath != null)
			{
				delResourceBodyFilesystem(edit);
			}

			// otherwise use the database
			else
			{
				delResourceBodyDb(edit);
			}

			// clear the memory image of the body
			byte[] body = ((BaseResourceEdit) edit).m_body;
			((BaseResourceEdit) edit).m_body = null;

			m_resourceStore.removeResource(edit);
		}

		/**
		 * Read the resource's body.
		 * 
		 * @param resource
		 *        The resource whose body is desired.
		 * @return The resources's body content as a byte array.
		 * @exception ServerOverloadException
		 *            if the server is configured to save the resource body in the filesystem and an error occurs while accessing the server's filesystem.
		 */
		public byte[] getResourceBody(ContentResource resource) throws ServerOverloadException
		{
			if (((BaseResourceEdit) resource).m_contentLength <= 0)
			{
				M_log.warn("getResourceBody(): non-positive content length: " + ((BaseResourceEdit) resource).m_contentLength
						+ "  id: " + resource.getId());
				return null;
			}

			// if we have been configured to use an external file system
			if (m_bodyPath != null)
			{
				return getResourceBodyFilesystem(resource);
			}

			// otherwise use the database
			else
			{
				return getResourceBodyDb(resource);
			}
		}

		/**
		 * Read the resource's body from the database.
		 * 
		 * @param resource
		 *        The resource whose body is desired.
		 * @return The resources's body content as a byte array.
		 */
		protected byte[] getResourceBodyDb(ContentResource resource)
		{
			// get the resource from the db
			String sql = "select BODY from " + m_resourceBodyTableName + " where ( RESOURCE_ID = ? )";

			Object[] fields = new Object[1];
			fields[0] = resource.getId();

			// create the body to read into
			byte[] body = new byte[((BaseResourceEdit) resource).m_contentLength];
			m_sqlService.dbReadBinary(sql, fields, body);

			return body;
		}

		/**
		 * Read the resource's body from the external file system.
		 * 
		 * @param resource
		 *        The resource whose body is desired.
		 * @return The resources's body content as a byte array.
		 * @exception ServerOverloadException
		 *            if server is configured to store resource body in filesystem and error occurs trying to read from filesystem.
		 */
		protected byte[] getResourceBodyFilesystem(ContentResource resource) throws ServerOverloadException
		{
			// form the file name
			File file = new File(externalResourceFileName(resource));

			// read the new
			try
			{
				byte[] body = new byte[((BaseResourceEdit) resource).m_contentLength];
				FileInputStream in = new FileInputStream(file);

				in.read(body);
				in.close();

				return body;
			}
			catch (Throwable t)
			{
				// If there is not supposed to be data in the file - simply return zero length byte array
				if (((BaseResourceEdit) resource).m_contentLength == 0)
				{
					return new byte[0];
				}

				// If we have a non-zero body length and reading failed, it is an error worth of note
				M_log.warn(": failed to read resource: " + resource.getId() + " len: "
						+ ((BaseResourceEdit) resource).m_contentLength + " : " + t);
				throw new ServerOverloadException("failed to read resource");
				// return null;
			}
		}

		// the body is already in the resource for this version of storage
		public InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException
		{
			if (((BaseResourceEdit) resource).m_contentLength <= 0)
			{
				M_log.warn("getResourceBody(): non-positive content length: " + ((BaseResourceEdit) resource).m_contentLength
						+ "  id: " + resource.getId());
				return null;
			}

			// if we have been configured to use an external file system
			if (m_bodyPath != null)
			{
				return streamResourceBodyFilesystem(resource);
			}

			// otherwise use the database
			else
			{
				return streamResourceBodyDb(resource);
			}
		}

		/**
		 * Return an input stream.
		 * 
		 * @param resource -
		 *        the resource for the stream It is a non-fatal error for the file not to be readible as long as the resource's expected length is zero. A zero length body is indicated by returning null. We check for the body length *after* we try to read
		 *        the file. If the file is readible, we simply read it and return it as the body.
		 */

		protected InputStream streamResourceBodyFilesystem(ContentResource resource) throws ServerOverloadException
		{
			// form the file name
			File file = new File(externalResourceFileName(resource));

			// read the new
			try
			{
				FileInputStream in = new FileInputStream(file);
				return in;
			}
			catch (Throwable t)
			{
				// If there is not supposed to be data in the file - simply return null
				if (((BaseResourceEdit) resource).m_contentLength == 0)
				{
					return null;
				}

				// If we have a non-zero body length and reading failed, it is an error worth of note
				M_log.warn(": failed to read resource: " + resource.getId() + " len: "
						+ ((BaseResourceEdit) resource).m_contentLength + " : " + t);
				throw new ServerOverloadException("failed to read resource body");
				// return null;
			}
		}

		/**
		 * When resources are stored, zero length bodys are not placed in the table hence this routine will return a null when the particular resource body is not found
		 */
		protected InputStream streamResourceBodyDb(ContentResource resource) throws ServerOverloadException
		{
			// get the resource from the db
			String sql = "select BODY from " + m_resourceBodyTableName + " where ( RESOURCE_ID = ? )";

			Object[] fields = new Object[1];
			fields[0] = resource.getId();

			// get the stream, set expectations that this could be big
			InputStream in = m_sqlService.dbReadBinary(sql, fields, true);

			return in;
		}

		/**
		 * Write the resource body to the database table.
		 * 
		 * @param resource
		 *        The resource whose body is being written.
		 * @param body
		 *        The body bytes to write. If there is no body or the body is zero bytes, no entry is inserted into the table.
		 */
		protected void putResourceBodyDb(ContentResourceEdit resource, byte[] body)
		{
			if ((body == null) || (body.length == 0)) return;

			// delete the old
			String statement = "delete from " + m_resourceBodyTableName + " where resource_id = ? ";

			Object[] fields = new Object[1];
			fields[0] = resource.getId();

			m_sqlService.dbWrite(statement, fields);

			// add the new
			statement = "insert into " + m_resourceBodyTableName + " (RESOURCE_ID, BODY)" + " values (? , ? )";

			m_sqlService.dbWriteBinary(statement, fields, body, 0, body.length);

			/*
			 * %%% BLOB code // read the record's blob and update statement = "select body from " + m_resourceTableName + " where ( resource_id = '" + Validator.escapeSql(resource.getId()) + "' ) for update"; Sql.dbReadBlobAndUpdate(statement,
			 * ((BaseResource)resource).m_body);
			 */
		}

		/**
		 * Write the resource body to the external file system. The file name is the m_bodyPath with the resource id appended.
		 * 
		 * @param resource
		 *        The resource whose body is being written.
		 * @param body
		 *        The body bytes to write. If there is no body or the body is zero bytes, no entry is inserted into the filesystem.
		 */
		protected boolean putResourceBodyFilesystem(ContentResourceEdit resource, byte[] body)
		{

			// Do not create the files for resources with zero length bodies
			if ((body == null) || (body.length == 0)) return true;

			// form the file name
			File file = new File(externalResourceFileName(resource));

			// delete the old
			if (file.exists())
			{
				file.delete();
			}

			// add the new
			try
			{
				// make sure all directories are there
				File container = file.getParentFile();
				if (container != null)
				{
					container.mkdirs();
				}

				// write the file
				FileOutputStream out = new FileOutputStream(file);
				out.write(body);
				out.close();
			}
			catch (Throwable t)
			{
				M_log.warn(": failed to write resource: " + resource.getId() + " : " + t);
				return false;
			}

			return true;
		}

		/**
		 * Delete the resource body from the database table.
		 * 
		 * @param resource
		 *        The resource whose body is being deleted.
		 */
		protected void delResourceBodyDb(ContentResourceEdit resource)
		{
			// delete the record
			String statement = "delete from " + m_resourceBodyTableName + " where resource_id = ?";

			Object[] fields = new Object[1];
			fields[0] = resource.getId();

			m_sqlService.dbWrite(statement, fields);
		}

		/**
		 * Delete the resource body from the external file system. The file name is the m_bodyPath with the resource id appended.
		 * 
		 * @param resource
		 *        The resource whose body is being written.
		 */
		protected void delResourceBodyFilesystem(ContentResourceEdit resource)
		{
			// form the file name
			File file = new File(externalResourceFileName(resource));

			// delete
			if (file.exists())
			{
				file.delete();
			}
		}

	} // DbStorage

	/**
	 * Form the full file path+name used to store the resource body in an external file system.
	 * 
	 * @param resource
	 *        The resource.
	 * @return The resource external file name.
	 */
	protected String externalResourceFileName(ContentResource resource)
	{
		return m_bodyPath + ((BaseResourceEdit) resource).m_filePath;
	}

	/** We allow these characters to go un-escaped into the file name. */
	static protected final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.";

	/**
	 * Return file system safe escaped name, that's also unique if the initial id is unique. * Use only the name, not the path part of the id
	 * 
	 * @param value
	 *        The id to escape.
	 * @return value escaped.
	 */
	protected String escapeResourceName(String id)
	{
		if (id == null) return null;

		try
		{
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < id.length(); i++)
			{
				char c = id.charAt(i);

				// if not valid, escape
				if (VALID_CHARS.indexOf(c) == -1)
				{
					// the escape character
					buf.append('_');

					// the character value.
					buf.append(Integer.toHexString(c));
				}
				else
				{
					buf.append(c);
				}
			}

			String rv = buf.toString();
			return rv;
		}
		catch (Exception e)
		{
			M_log.warn("escapeResourceName: ", e);
			return id;
		}
	}

	/**
	 * Create a file system body binary for any content_resource record that has a null file_path.
	 */
	protected void convertToFile()
	{
		M_log.info("convertToFile");

		try
		{
			// get a connection for the updates
			final Connection connection = m_sqlService.borrowConnection();
			boolean wasCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			// get a connection for reading binary
			final Connection sourceConnection = m_sqlService.borrowConnection();

			final Counter count = new Counter();

			// read content_resource records that have null file path
			String sql = "select RESOURCE_ID, XML from CONTENT_RESOURCE where FILE_PATH IS NULL";
			m_sqlService.dbRead(sql, null, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					String id = null;
					try
					{
						// create the Resource from the db xml
						id = result.getString(1);
						String xml = result.getString(2);
						if (xml == null)
						{
							M_log.warn("convertToFile: null xml : " + id);
							return null;
						}

						// read the xml
						Document doc = Xml.readDocumentFromString(xml);
						if (doc == null)
						{
							M_log.warn("convertToFile: null xml doc : " + id);
							return null;
						}

						// verify the root element
						Element root = doc.getDocumentElement();
						if (!root.getTagName().equals("resource"))
						{
							M_log.warn("convertToFile: XML root element not resource: " + root.getTagName());
							return null;
						}
						BaseResourceEdit edit = new BaseResourceEdit(root);

						// zero length?
						if (edit.getContentLength() == 0)
						{
							M_log.warn("convertToFile: zero length body : " + id);
							return null;
						}

						// is it there?
						String sql = "select RESOURCE_ID from CONTENT_RESOURCE_BODY_BINARY where (RESOURCE_ID = ?)";
						Object[] fields = new Object[1];
						fields[0] = id;
						List found = m_sqlService.dbRead(sourceConnection, sql, fields, null);
						if ((found == null) || (found.size() == 0))
						{
							// not found
							M_log.warn("convertToFile: body not found in source : " + id);
							return null;
						}

						// get the creation date (or modified date, or now)
						Time created = null;
						try
						{
							created = edit.getProperties().getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
						}
						catch (Exception any)
						{
							try
							{
								created = edit.getProperties().getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
							}
							catch (Exception e)
							{
								created = TimeService.newTime();
							}
						}

						// form the file name
						edit.setFilePath(created);

						// read the body from the source
						sql = "select BODY from CONTENT_RESOURCE_BODY_BINARY where (RESOURCE_ID = ?)";
						byte[] body = new byte[edit.m_contentLength];
						m_sqlService.dbReadBinary(sourceConnection, sql, fields, body);

						// write the body to the file
						boolean ok = ((DbStorage) m_storage).putResourceBodyFilesystem(edit, body);
						if (!ok)
						{
							M_log.warn("convertToFile: body file failure : " + id + " file: " + edit.m_filePath);
							return null;
						}

						// regenerate the xml, now with file path set
						doc = Xml.createDocument();
						edit.toXml(doc, new Stack());
						xml = Xml.writeDocumentToString(doc);

						// update the record
						sql = "update CONTENT_RESOURCE set FILE_PATH = ?, XML = ? where RESOURCE_ID = ?";
						fields = new Object[3];
						fields[0] = edit.m_filePath;
						fields[1] = xml;
						fields[2] = id;
						m_sqlService.dbWrite(connection, sql, fields);

						// m_logger.info(" ** converted: " + id + " size: " + edit.m_contentLength);
						count.value++;
						if ((count.value % 1000) == 0)
						{
							connection.commit();
							M_log.info(" ** converted: " + count.value);
						}

						return null;
					}
					catch (Throwable e)
					{
						M_log.info(" ** exception converting : " + id + " : ", e);
						return null;
					}
				}
			});

			connection.commit();

			M_log.info("convertToFile: converted resources: " + count.value);

			m_sqlService.returnConnection(sourceConnection);

			connection.setAutoCommit(wasCommit);
			m_sqlService.returnConnection(connection);
		}
		catch (Throwable t)
		{
			M_log.warn("convertToFile: failed: " + t);
		}

		M_log.info("convertToFile: done");
	}

	/**
	 * <p>
	 * Counter is is a counter that can be marked final.
	 * </p>
	 */
	public class Counter
	{
		public int value = 0;
	}

	public Collection getLocks(String id)
	{
		return m_lockManager.getLocks(id);
	}

	public void lockObject(String id, String lockId, String subject, boolean system)
	{
		if (M_log.isDebugEnabled()) M_log.debug("lockObject has been called on: " + id);
		try
		{
			m_lockManager.lockObject(id, lockId, subject, system);
		}
		catch (Exception e)
		{
			M_log.warn("lockObject failed: " + e);
			e.printStackTrace();
			return;
		}
		if (M_log.isDebugEnabled()) M_log.debug("lockObject succeeded");
	}

	public void removeLock(String id, String lockId)
	{
		m_lockManager.removeLock(id, lockId);
	}

	public boolean isLocked(String id)
	{
		return m_lockManager.isLocked(id);
	}

	public boolean containsLockedNode(String id)
	{
		throw new RuntimeException("containsLockedNode has not been implemented");
	}

	public void removeAllLocks(String id)
	{
		m_lockManager.removeAllLocks(id);
	}

	protected List getFlatResources(String parentId)
	{
		return m_storage.getFlatResources(parentId);
	}
}
