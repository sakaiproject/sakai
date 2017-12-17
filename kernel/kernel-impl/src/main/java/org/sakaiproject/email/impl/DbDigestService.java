/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.email.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.email.api.Digest;
import org.sakaiproject.email.api.DigestEdit;
import org.sakaiproject.util.BaseDbSingleStorage;
import org.sakaiproject.util.SingleStorageUser;

/**
 * <p>
 * DbDigestService is an extension of the BaseDigestService with database storage.
 * </p>
 */
@Slf4j
public class DbDigestService extends BaseDigestService
{
	/** Table name for realms. */
	protected String m_tableName = "SAKAI_DIGEST";

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_locksInDb = true;

	protected SqlService sqlService;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the MemoryService collaborator.
	 */
	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Configuration: set the table name
	 * 
	 * @param path
	 *        The table name.
	 */
	public void setTableName(String name)
	{
		m_tableName = name;
	}

	/**
	 * Configuration: set the locks-in-db
	 * 
	 * @param value
	 *        The locks-in-db value.
	 */
	public void setLocksInDb(String value)
	{
		m_locksInDb = Boolean.valueOf(value).booleanValue();
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
		m_autoDdl = Boolean.valueOf(value).booleanValue();
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
				sqlService.ddl(this.getClass().getClassLoader(), "sakai_digest");
			}

			super.init();

			log.info("init(): table: " + m_tableName + " locks-in-db: " + m_locksInDb);
		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseDigestService extensions
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		return new DbStorage(this);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Covers for the BaseXmlFileStorage, providing Digest and DigestEdit parameters
	 */
	protected class DbStorage extends BaseDbSingleStorage implements Storage
	{
		/**
		 * Construct.
		 * 
		 * @param realm
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbStorage(SingleStorageUser user)
		{
			super(m_tableName, "DIGEST_ID", null, m_locksInDb, "digest", user, sqlService);
		}

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public Digest get(String id)
		{
			return (Digest) super.getResource(id);
		}

		public List getAll()
		{
			return super.getAllResources();
		}

		public DigestEdit put(String id)
		{
			return (DigestEdit) super.putResource(id, null);
		}

		public DigestEdit edit(String id)
		{
			return (DigestEdit) super.editResource(id);
		}

		public void commit(DigestEdit edit)
		{
			super.commitResource(edit);
		}

		public void cancel(DigestEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(DigestEdit edit)
		{
			super.removeResource(edit);
		}
	}
}
