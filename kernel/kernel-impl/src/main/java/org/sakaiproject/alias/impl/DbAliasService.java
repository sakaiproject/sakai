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

package org.sakaiproject.alias.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasEdit;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.BaseDbFlatStorage;
import org.sakaiproject.util.SingleStorageUser;
import org.sakaiproject.util.StorageUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * DbAliasService is an extension of the BaseAliasService with a database storage. Fields are fully relational. Full properties are not yet supported - core ones are.
 * </p>
 */
@Slf4j
public abstract class DbAliasService extends BaseAliasService
{
	/** Table name for aliases. */
	protected String m_tableName = "SAKAI_ALIAS";

	/** Table name for properties. */
	protected String m_propTableName = "SAKAI_ALIAS_PROPERTY";

	/** ID field. */
	protected String m_idFieldName = "ALIAS_ID";

	/** All fields. */
	protected String[] m_fieldNames = { "ALIAS_ID", "TARGET", "CREATEDBY", "MODIFIEDBY", "CREATEDON", "MODIFIEDON" };

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_useExternalLocks = true;

	/** Set if we are to run the from-old conversion. */
	protected boolean m_convertOld = false;

	/**
	 * Configuration: run the from-old conversion.
	 * 
	 * @param value
	 *        The conversion desired value.
	 */
	public void setConvertOld(String value)
	{
		m_convertOld = Boolean.valueOf(value).booleanValue();
	}

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
	 * Configuration: set the external locks value.
	 * 
	 * @param value
	 *        The external locks value.
	 */
	public void setExternalLocks(String value)
	{
		m_useExternalLocks = Boolean.valueOf(value).booleanValue();
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
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_alias");
			}

			super.init();

			log.info("init(): table: " + m_tableName + " external locks: " + m_useExternalLocks);
		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseAliasService extensions
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		return new DbStorage(this);

	} // newStorage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Covers for the BaseXmlFileStorage, providing User and AliasEdit parameters
	 */
	protected class DbStorage extends BaseDbFlatStorage implements Storage, SqlReader
	{
		/**
		 * Construct.
		 * 
		 * @param user
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbStorage(SingleStorageUser user)
		{
			super(m_tableName, m_idFieldName, m_fieldNames, m_propTableName, m_useExternalLocks, null, sqlService());
			m_reader = this;
			setCaseInsensitivity(true);
		}

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public AliasEdit get(String id)
		{
			return (AliasEdit) super.getResource(id);
		}

		public List getAll()
		{
			return super.getAllResources();
		}

		public List getAll(int first, int last)
		{
			return super.getAllResources(first, last);
		}

		public int count()
		{
			return super.countAllResources();
		}

		public List getAll(String target)
		{
			Object[] fields = new Object[1];
			fields[0] = target;

			return super.getSelectedResources("TARGET = ?", fields);
		}

		public List getAll(String target, int first, int last)
		{
			Object[] fields = new Object[1];
			fields[0] = target;

			return super.getSelectedResources("TARGET = ?", fields, first, last);
		}

		public AliasEdit put(String id)
		{
			if (check(id)) return null;

			BaseAliasEdit rv = (BaseAliasEdit) super.putResource(id, fields(id, null, false));
			if (rv != null) rv.activate();
			return rv;
		}

		public AliasEdit edit(String id)
		{
			BaseAliasEdit rv = (BaseAliasEdit) super.editResource(id);
			if (rv != null) rv.activate();
			return rv;
		}

		public void commit(AliasEdit edit)
		{
			super.commitResource(edit, fields(edit.getId(), edit, true), edit.getProperties());
		}

		public void cancel(AliasEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(AliasEdit edit)
		{
			super.removeResource(edit);
		}

		public List search(String criteria, int first, int last)
		{
			Object[] fields = new Object[2];
			fields[0] = "%" + StorageUtils.escapeSqlLike(criteria.toUpperCase()) + "%";
			fields[1] = fields[0];
			return super.getSelectedResources("UPPER(ALIAS_ID) LIKE ? OR UPPER(TARGET) LIKE ?", fields, first, last);
		}

		public int countSearch(String criteria)
		{
			Object[] fields = new Object[2];
			fields[0] = "%" + StorageUtils.escapeSqlLike(criteria.toUpperCase()) + "%";
			fields[1] = fields[0];
			return super.countSelectedResources("UPPER(ALIAS_ID) LIKE ? OR UPPER(TARGET) LIKE ?", fields);
		}

		/**
		 * Read properties from storage into the edit's properties.
		 * 
		 * @param edit
		 *        The user to read properties for.
		 */
		public void readProperties(AliasEdit edit, ResourcePropertiesEdit props)
		{
			super.readProperties(edit, props);
		}

		/**
		 * Get the fields for the database from the edit for this id, and the id again at the end if needed
		 * 
		 * @param id
		 *        The resource id
		 * @param edit
		 *        The edit (may be null in a new)
		 * @param idAgain
		 *        If true, include the id field again at the end, else don't.
		 * @return The fields for the database.
		 */
		protected Object[] fields(String id, AliasEdit edit, boolean idAgain)
		{
			Object[] rv = new Object[idAgain ? 7 : 6];
			rv[0] = caseId(id);
			if (idAgain)
			{
				rv[6] = rv[0];
			}

			if (edit == null)
			{
				String current = sessionManager().getCurrentSessionUserId();
				if (current == null) current = "";

				Time now = timeService().newTime();
				rv[1] = "";
				rv[2] = current;
				rv[3] = current;
				rv[4] = now;
				rv[5] = now;
			}

			else
			{
				rv[1] = edit.getTarget();
				ResourceProperties props = edit.getProperties();
				rv[2] = StringUtils.trimToEmpty(((BaseAliasEdit) edit).m_createdUserId);
				rv[3] = StringUtils.trimToEmpty(((BaseAliasEdit) edit).m_lastModifiedUserId);
				rv[4] = edit.getCreatedTime();
				rv[5] = edit.getModifiedTime();
			}

			return rv;
		}

		/**
		 * Read from the result one set of fields to create a Resource.
		 * 
		 * @param result
		 *        The Sql query result.
		 * @return The Resource object.
		 */
		public Object readSqlResultRecord(ResultSet result)
		{
			try
			{
				String id = result.getString(1);
				String target = result.getString(2);
				String createdBy = result.getString(3);
				String modifiedBy = result.getString(4);
				Time createdOn = timeService().newTime(result.getTimestamp(5).getTime());
				Time modifiedOn = timeService().newTime(result.getTimestamp(6).getTime());

				// create the Resource from these fields
				return new BaseAliasEdit(id, target, createdBy, createdOn, modifiedBy, modifiedOn);
			}
			catch (SQLException ignore)
			{
				return null;
			}
		}

	} // DbStorage
}
