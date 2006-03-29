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

package org.sakaiproject.user.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.util.BaseDbFlatStorage;
import org.sakaiproject.util.BaseDbSingleStorage;
import org.sakaiproject.util.StorageUser;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * DbCachedUserService is an extension of the BaseUserService with a database storage backed up by an in-memory cache.
 * </p>
 */
public class DbUserService extends BaseUserDirectoryService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(DbUserService.class);

	/** Table name for users. */
	protected String m_tableName = "SAKAI_USER";

	/** Table name for properties. */
	protected String m_propTableName = "SAKAI_USER_PROPERTY";

	/** ID field. */
	protected String m_idFieldName = "USER_ID";

	/** SORT field 1. */
	protected String m_sortField1 = "LAST_NAME";

	/** SORT field 2. */
	protected String m_sortField2 = "FIRST_NAME";

	/** All fields. */
	protected String[] m_fieldNames = { "USER_ID", "EMAIL", "EMAIL_LC", "FIRST_NAME", "LAST_NAME", "TYPE", "PW", "CREATEDBY",
			"MODIFIEDBY", "CREATEDON", "MODIFIEDON" };

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

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
	 * Configuration: set the table name
	 * 
	 * @param path
	 *        The table name.
	 */
	public void setTableName(String name)
	{
		m_tableName = name;
	}

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_useExternalLocks = true;

	/**
	 * Configuration: set the external locks value.
	 * 
	 * @param value
	 *        The external locks value.
	 */
	public void setExternalLocks(String value)
	{
		m_useExternalLocks = new Boolean(value).booleanValue();
	}

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
		m_convertOld = new Boolean(value).booleanValue();
	}

	/** Configuration: check the old table, too. */
	protected boolean m_checkOld = false;

	/**
	 * Configuration: set the locks-in-db
	 * 
	 * @param value
	 *        The locks-in-db value.
	 */
	public void setCheckOld(String value)
	{
		m_checkOld = new Boolean(value).booleanValue();
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
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_user");

				// load the 2.1.0.004 email_lc conversion
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_user_2_1_0_004");

				// load the 2.1.0 postmaster password conversion
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_user_2_1_0");
			}

			super.init();

			// convert?
			if (m_convertOld)
			{
				m_convertOld = false;
				convertOld();
			}

			// do a count which might find no old records so we can ignore old!
			if (m_checkOld)
			{
				m_storage.count();
			}

			M_log.info("init(): table: " + m_tableName + " external locks: " + m_useExternalLocks + " checkOld: " + m_checkOld);

		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseUserService extensions
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
	 * Covers for the BaseXmlFileStorage, providing User and UserEdit parameters
	 */
	protected class DbStorage extends BaseDbFlatStorage implements Storage, SqlReader
	{
		/** A prior version's storage model. */
		protected Storage m_oldStorage = null;

		/**
		 * Construct.
		 * 
		 * @param user
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbStorage(StorageUser user)
		{
			super(m_tableName, m_idFieldName, m_fieldNames, m_propTableName, m_useExternalLocks, null, m_sqlService);
			setSortField(m_sortField1, m_sortField2);

			m_reader = this;
			setCaseInsensitivity(!m_caseSensitiveId);

			// setup for old-new stradling
			if (m_checkOld)
			{
				m_oldStorage = new DbStorageOld(user);
			}
		}

		public boolean check(String id)
		{
			boolean rv = super.checkResource(id);

			// if not, check old
			if (m_checkOld && (!rv))
			{
				rv = m_oldStorage.check(id);
			}

			return rv;
		}

		public UserEdit get(String id)
		{
			UserEdit rv = (UserEdit) super.getResource(id);

			// if not, check old
			if (m_checkOld && (rv == null))
			{
				rv = m_oldStorage.get(id);
			}
			return rv;
		}

		public List getAll()
		{
			// if we have to be concerned with old stuff, we cannot let the db do the range selection
			if (m_checkOld)
			{
				List all = super.getAllResources();

				// add in any additional defined in old
				Set merge = new HashSet();
				merge.addAll(all);

				// add those in the old not already (id based equals) in all
				List more = m_oldStorage.getAll();
				merge.addAll(more);

				all.clear();
				all.addAll(merge);

				return all;
			}

			// let the db do range selection
			List all = super.getAllResources();
			return all;
		}

		public List getAll(int first, int last)
		{
			// if we have to be concerned with old stuff, we cannot let the db do the range selection
			if (m_checkOld)
			{
				List all = super.getAllResources();

				// add in any additional defined in old
				Set merge = new HashSet();
				merge.addAll(all);

				// add those in the old not already (id based equals) in all
				List more = m_oldStorage.getAll();
				merge.addAll(more);

				all.clear();
				all.addAll(merge);

				Collections.sort(all);

				// subset by position
				if (first < 1) first = 1;
				if (last >= all.size()) last = all.size();

				all = all.subList(first - 1, last);
				return all;
			}

			// let the db do range selection
			List all = super.getAllResources(first, last);
			return all;
		}

		public int count()
		{
			// if we have to be concerned with old stuff, we cannot let the db do all the counting
			if (m_checkOld)
			{
				int count = super.countAllResources();
				count += m_oldStorage.count();

				return count;
			}

			return super.countAllResources();
		}

		public UserEdit put(String id)
		{
			// check for already exists (new or old)
			if (check(id)) return null;

			BaseUserEdit rv = (BaseUserEdit) super.putResource(id, fields(id, null, false));
			if (rv != null) rv.activate();
			return rv;
		}

		public UserEdit edit(String id)
		{
			BaseUserEdit rv = (BaseUserEdit) super.editResource(id);

			// if not found, try from the old (convert to the new)
			if (m_checkOld && (rv == null))
			{
				// this locks the old table/record
				rv = (BaseUserEdit) m_oldStorage.edit(id);
				if (rv != null)
				{
					// create the record in new, also locking it into an edit
					rv = (BaseUserEdit) super.putResource(id, fields(id, rv, false));

					// delete the old record
					m_oldStorage.remove(rv);
				}
			}

			if (rv != null) rv.activate();
			return rv;
		}

		public void commit(UserEdit edit)
		{
			super.commitResource(edit, fields(edit.getId(), edit, true), edit.getProperties());
		}

		public void cancel(UserEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(UserEdit edit)
		{
			super.removeResource(edit);
		}

		public List search(String criteria, int first, int last)
		{
			// if we have to be concerned with old stuff, we cannot let the db do the search
			if (m_checkOld)
			{
				List all = getAll();
				List rv = new Vector();

				for (Iterator i = all.iterator(); i.hasNext();)
				{
					BaseUserEdit u = (BaseUserEdit) i.next();
					if (u.selectedBy(criteria))
					{
						rv.add(u);
					}
				}

				Collections.sort(rv);

				// subset by position
				if (first < 1) first = 1;
				if (last >= rv.size()) last = rv.size();

				rv = rv.subList(first - 1, last);

				return rv;
			}

			String search = "%" + criteria + "%";
			Object[] fields = new Object[4];
			fields[0] = search;
			fields[1] = search.toLowerCase();
			fields[2] = search;
			fields[3] = search;
			List rv = super
					.getSelectedResources(
							"UPPER(USER_ID) LIKE UPPER(?) OR EMAIL_LC LIKE ? OR UPPER(FIRST_NAME) LIKE UPPER(?) OR UPPER(LAST_NAME) LIKE UPPER(?)",
							fields);

			return rv;
		}

		public int countSearch(String criteria)
		{
			// if we have to be concerned with old stuff, we cannot let the db do the search and count
			if (m_checkOld)
			{
				List all = getAll();
				List rv = new Vector();

				for (Iterator i = all.iterator(); i.hasNext();)
				{
					BaseUserEdit u = (BaseUserEdit) i.next();
					if (u.selectedBy(criteria))
					{
						rv.add(u);
					}
				}

				return rv.size();
			}

			String search = "%" + criteria + "%";
			Object[] fields = new Object[4];
			fields[0] = search;
			fields[1] = search.toLowerCase();
			fields[2] = search;
			fields[3] = search;
			int rv = super
					.countSelectedResources(
							"UPPER(USER_ID) LIKE UPPER(?) OR EMAIL_LC LIKE ? OR UPPER(FIRST_NAME) LIKE UPPER(?) OR UPPER(LAST_NAME) LIKE UPPER(?)",
							fields);

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public Collection findUsersByEmail(String email)
		{
			Collection rv = null;

			// check old if needed
			if (m_checkOld)
			{
				rv = m_oldStorage.findUsersByEmail(email);
			}

			if (rv == null)
			{
				rv = new Vector();

				// search for it
				Object[] fields = new Object[1];
				fields[0] = email.toLowerCase();
				List users = super.getSelectedResources("EMAIL_LC = ?", fields);
				if (users != null)
				{
					rv.addAll(users);
				}
			}

			return rv;
		}

		/**
		 * Read properties from storage into the edit's properties.
		 * 
		 * @param edit
		 *        The user to read properties for.
		 */
		public void readProperties(UserEdit edit, ResourcePropertiesEdit props)
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
		protected Object[] fields(String id, UserEdit edit, boolean idAgain)
		{
			Object[] rv = new Object[idAgain ? 12 : 11];
			rv[0] = caseId(id);
			if (idAgain)
			{
				rv[11] = rv[0];
			}

			if (edit == null)
			{
				String attribUser = m_sessionManager.getCurrentSessionUserId();

				// if no current user, since we are working up a new user record, use the user id as creator...
				if ((attribUser == null) || (attribUser.length() == 0)) attribUser = (String) rv[0];

				Time now = m_timeService.newTime();
				rv[1] = "";
				rv[2] = "";
				rv[3] = "";
				rv[4] = "";
				rv[5] = "";
				rv[6] = "";
				rv[7] = attribUser;
				rv[8] = attribUser;
				rv[9] = now;
				rv[10] = now;
			}

			else
			{
				rv[1] = StringUtil.trimToZero(edit.getEmail());
				rv[2] = StringUtil.trimToZero(edit.getEmail().toLowerCase());
				rv[3] = StringUtil.trimToZero(edit.getFirstName());
				rv[4] = StringUtil.trimToZero(edit.getLastName());
				rv[5] = StringUtil.trimToZero(edit.getType());
				rv[6] = StringUtil.trimToZero(((BaseUserEdit) edit).m_pw);

				// for creator and modified by, if null, make it the id
				rv[7] = StringUtil.trimToNull(((BaseUserEdit) edit).m_createdUserId);
				if (rv[7] == null)
				{
					rv[7] = rv[0];
				}
				rv[8] = StringUtil.trimToNull(((BaseUserEdit) edit).m_lastModifiedUserId);
				if (rv[8] == null)
				{
					rv[8] = rv[0];
				}

				rv[9] = edit.getCreatedTime();
				rv[10] = edit.getModifiedTime();
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
				String email = result.getString(2);
				String email_lc = result.getString(3);
				String firstName = result.getString(4);
				String lastName = result.getString(5);
				String type = result.getString(6);
				String pw = result.getString(7);
				String createdBy = result.getString(8);
				String modifiedBy = result.getString(9);
				Time createdOn = m_timeService.newTime(result.getTimestamp(10, m_sqlService.getCal()).getTime());
				Time modifiedOn = m_timeService.newTime(result.getTimestamp(11, m_sqlService.getCal()).getTime());

				// create the Resource from these fields
				return new BaseUserEdit(id, email, firstName, lastName, type, pw, createdBy, createdOn, modifiedBy, modifiedOn);
			}
			catch (SQLException e)
			{
				M_log.warn("readSqlResultRecord: " + e);
				return null;
			}
		}
	}

	/**
	 * Covers for the BaseXmlFileStorage, providing User and UserEdit parameters
	 */
	protected class DbStorageOld extends BaseDbSingleStorage implements Storage
	{
		/**
		 * Construct.
		 * 
		 * @param user
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbStorageOld(StorageUser user)
		{
			super("CHEF_USER", "USER_ID", null, false, "user", user, m_sqlService);
		}

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public UserEdit get(String id)
		{
			return (UserEdit) super.getResource(id);
		}

		public List getAll()
		{
			return super.getAllResources();
		}

		public int count()
		{
			int rv = super.countAllResources();

			// if we find no more records in the old table, we can start ignoring it...
			// Note: this means once they go away they cannot come back (old versions cannot run in the cluster
			// and write to the old cluster table). -ggolden
			if (rv == 0)
			{
				m_checkOld = false;
				M_log.info(" ** starting to ignore old");
			}
			return rv;
		}

		public List getAll(int first, int last)
		{
			List all = super.getAllResources();

			// sort for position check
			Collections.sort(all);

			// subset by position
			if (first < 1) first = 1;
			if (last >= all.size()) last = all.size();

			all = all.subList(first - 1, last);

			return all;
		}

		public List search(String criteria, int first, int last)
		{
			List all = super.getAllResources();

			List rv = new Vector();
			for (Iterator i = all.iterator(); i.hasNext();)
			{
				BaseUserEdit u = (BaseUserEdit) i.next();
				if (u.selectedBy(criteria))
				{
					rv.add(u);
				}
			}

			Collections.sort(rv);

			// subset by position
			if (first < 1) first = 1;
			if (last >= rv.size()) last = rv.size();

			rv = rv.subList(first - 1, last);

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public int countSearch(String criteria)
		{
			List all = super.getAllResources();

			Vector rv = new Vector();
			for (Iterator i = all.iterator(); i.hasNext();)
			{
				BaseUserEdit u = (BaseUserEdit) i.next();
				if (u.selectedBy(criteria))
				{
					rv.add(u);
				}
			}

			return rv.size();
		}

		public UserEdit put(String id)
		{
			return (UserEdit) super.putResource(id, null);
		}

		public UserEdit edit(String id)
		{
			return (UserEdit) super.editResource(id);
		}

		public void commit(UserEdit edit)
		{
			super.commitResource(edit);
		}

		public void cancel(UserEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(UserEdit edit)
		{
			super.removeResource(edit);
		}

		/**
		 * {@inheritDoc}
		 */
		public Collection findUsersByEmail(String email)
		{
			Collection rv = new Vector();

			// check internal users
			List users = getUsers();
			for (Iterator iUsers = users.iterator(); iUsers.hasNext();)
			{
				UserEdit user = (UserEdit) iUsers.next();
				if (email.equalsIgnoreCase(user.getEmail()))
				{
					rv.add(user);
				}
			}

			return rv;
		}

		/**
		 * Read properties from storage into the edit's properties.
		 * 
		 * @param edit
		 *        The user to read properties for.
		 */
		public void readProperties(UserEdit edit, ResourcePropertiesEdit props)
		{
			M_log.warn("readProperties: should not be called.");
		}
	}

	/**
	 * Create a new table record for all old table records found, and delete the old.
	 */
	protected void convertOld()
	{
		M_log.info("convertOld");

		try
		{
			// get a connection
			final Connection connection = m_sqlService.borrowConnection();
			boolean wasCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			// read all user ids
			String sql = "select USER_ID, XML from CHEF_USER";
			m_sqlService.dbRead(connection, sql, null, new SqlReader()
			{
				private int count = 0;

				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// create the Resource from the db xml
						String id = result.getString(1);
						String xml = result.getString(2);

						// read the xml
						Document doc = Xml.readDocumentFromString(xml);

						// verify the root element
						Element root = doc.getDocumentElement();
						if (!root.getTagName().equals("user"))
						{
							M_log.warn("convertOld: XML root element not user: " + root.getTagName());
							return null;
						}
						UserEdit e = new BaseUserEdit(root);

						// pick up the fields
						Object[] fields = ((DbStorage) m_storage).fields(id, e, false);

						// insert the record
						boolean ok = ((DbStorage) m_storage).insertResource(id, fields, connection);
						if (!ok)
						{
							// warn, and don't delete the old!
							M_log.warn("convertOld: failed to insert: " + id);
							return null;
						}

						// delete the old record
						String statement = "delete from CHEF_USER where USER_ID = ?";
						fields = new Object[1];
						fields[0] = id;
						ok = m_sqlService.dbWrite(connection, statement, fields);
						if (!ok)
						{
							M_log.warn("convertOld: failed to delete: " + id);
						}

						// m_logger.info(" ** user converted: " + id);

						return null;
					}
					catch (Throwable ignore)
					{
						return null;
					}
				}
			});

			connection.commit();
			connection.setAutoCommit(wasCommit);
			m_sqlService.returnConnection(connection);
		}
		catch (Throwable t)
		{
			M_log.warn("convertOld: failed: " + t);
		}

		M_log.info("convertOld: done");
	}
}
