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

package org.sakaiproject.event.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.util.BaseDbSingleStorage;
import org.sakaiproject.util.SingleStorageUser;

/**
 * <p>
 * DbNotificationService is ... %%%.
 * </p>
 */
@Slf4j
public abstract class DbNotificationService extends BaseNotificationService
{
	/** Table name for users. */
	protected String m_tableName = "SAKAI_NOTIFICATION";

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_locksInDb = true;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract SqlService sqlService();

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
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_notification");
			}

			super.init();

			log.info(".init(): table: " + m_tableName + " locks-in-db: " + m_locksInDb);
		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseNotificationService extensions
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

	protected class DbStorage extends BaseDbSingleStorage implements Storage
	{
		/**
		 * Construct.
		 * 
		 * @param user
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbStorage(SingleStorageUser user)
		{
			super(m_tableName, "NOTIFICATION_ID", null, m_locksInDb, "notification", user, sqlService());
		}

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public Notification get(String id)
		{
			return (Notification) super.getResource(id);
		}

		public List getAll()
		{
			return super.getAllResources();
		}

		/**
		 * Get a Set of all the notifications that are interested in this Event function. Note: instead of this looking, we could have an additional "key" in storage of the event... -ggolen
		 * 
		 * @param function
		 *        The Event function
		 * @return The Set (Notification) of all the notifications that are interested in this Event function.
		 */
		public List getAll(String function)
		{
			List rv = new Vector();
			if (function == null) return rv;

			List all = super.getAllResources();
			for (Iterator it = all.iterator(); it.hasNext();)
			{
				Notification notification = (Notification) it.next();
				if (notification.containsFunction(function))
				{
					rv.add(notification);
				}
			}

			return rv;
		}

		public NotificationEdit put(String id)
		{
			return (NotificationEdit) super.putResource(id, null);
		}

		public NotificationEdit edit(String id)
		{
			return (NotificationEdit) super.editResource(id);
		}

		public void commit(NotificationEdit edit)
		{
			super.commitResource(edit);
		}

		public void cancel(NotificationEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(NotificationEdit edit)
		{
			super.removeResource(edit);
		}
	}
}
