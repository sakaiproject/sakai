/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.user.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.util.BaseDbFlatStorage;

/**
 * <p>
 * DbCachedUserService is an extension of the BaseUserService with a database storage backed up by an in-memory cache.
 * </p>
 */
@Slf4j
public abstract class DbUserService extends BaseUserDirectoryService
{
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
	protected String[] m_fieldNames = {"USER_ID", "EMAIL", "EMAIL_LC", "FIRST_NAME", "LAST_NAME", "TYPE", "PW", "CREATEDBY", "MODIFIEDBY",
			"CREATEDON", "MODIFIEDON"};

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/
	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_useExternalLocks = true;

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/
	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;
	/** The map of database dependent handler. */
	protected Map<String, UserServiceSql> databaseBeans;
	/** The database handler we are using. */
	protected UserServiceSql userServiceSql;
	protected Cache cache = null;

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract SqlService sqlService();

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
	 * Configuration: set the external locks value.
	 *
	 * @param value
	 *        The external locks value.
	 */
	public void setExternalLocks(String value)
	{
		m_useExternalLocks = Boolean.valueOf(value).booleanValue();
	}

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

	public void setDatabaseBeans(Map databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	public UserServiceSql getUserServiceSql()
	{
		return userServiceSql;
	}

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setUserServiceSql(String vendor)
	{
		this.userServiceSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

	/*************************************************************************************************************************************************
	 * Init and Destroy
	 ************************************************************************************************************************************************/

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
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_user");
			}

			super.init();
			setUserServiceSql(sqlService().getVendor());

			log.info("init(): table: " + m_tableName + " external locks: " + m_useExternalLocks);
			cache = memoryService().getCache("org.sakaiproject.user.api.UserDirectoryService"); // user id/eid mapping cache
			log.info("User ID/EID mapping Cache [" + cache.getName() +"]");

		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}
	}

	/*************************************************************************************************************************************************
	 * BaseUserService extensions
	 ************************************************************************************************************************************************/

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		return new DbStorage();
	}

	/*************************************************************************************************************************************************
	 * Storage implementation
	 ************************************************************************************************************************************************/

	/**
	 * @return the cache
	 */
	public Cache getIdEidCache()
	{
		return cache;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setIdEidCache(Cache cache)
	{
		this.cache = cache;
	}

	/**
	 * Covers for the BaseXmlFileStorage, providing User and UserEdit parameters
	 */
	protected class DbStorage extends BaseDbFlatStorage implements Storage, SqlReader
	{

		/**
		 * Construct.
		 *
		 */
		public DbStorage()
		{
			super(m_tableName, m_idFieldName, m_fieldNames, m_propTableName, m_useExternalLocks, null, sqlService());
			setSortField(m_sortField1, m_sortField2);

			m_reader = this;
		}

		public boolean check(String id)
		{
			boolean rv = super.checkResource(id);

			return rv;
		}

		public UserEdit getById(String id)
		{
			UserEdit rv = (UserEdit) super.getResource(id);

			return rv;
		}

		public List getAll()
		{
			// let the db do range selection
			List all = super.getAllResources();
			return all;
		}

		public List getAll(int first, int last)
		{
			// let the db do range selection
			List all = super.getAllResources(first, last);
			return all;
		}

		public int count()
		{
			return super.countAllResources();
		}

		public UserEdit put(String id, String eid)
		{
			// check for already exists
			if (check(id)) return null;

			// assure mapping
			if (!putMap(id, eid)) return null;

			BaseUserEdit rv = (BaseUserEdit) super.putResource(id, fields(id, null, false));
			if (rv != null) rv.activate();
			return rv;
		}

		public UserEdit edit(String id)
		{
			BaseUserEdit rv = (BaseUserEdit) super.editResource(id);

			if (rv != null) rv.activate();
			return rv;
		}

		public boolean commit(UserEdit edit)
		{
			// update the mapping - fail if that does not succeed
			if (!updateMap(edit.getId(), edit.getEid())) return false;

			super.commitResource(edit, fields(edit.getId(), edit, true), edit.getProperties());
			return true;
		}

		public void cancel(UserEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(UserEdit edit)
		{
			unMap(edit.getId());
			super.removeResource(edit);
		}

		public List search(String criteria, int first, int last)
		{
			String search = "%" + criteria + "%";
			Object[] fields = new Object[5];
			fields[0] = criteria;
			fields[1] = search;
			fields[2] = search.toLowerCase();
			fields[3] = search;
			fields[4] = search;

			List rv = super.getSelectedResources(userServiceSql.getUserWhereSql(), "SAKAI_USER_ID_MAP.EID", fields, first, last, "SAKAI_USER_ID_MAP");

			return rv;
		}

		public int countSearch(String criteria)
		{
			String search = "%" + criteria + "%";
			Object[] fields = new Object[5];
			fields[0] = criteria;
			fields[1] = search;
			fields[2] = search.toLowerCase();
			fields[3] = search;
			fields[4] = search;
			int rv = super.countSelectedResources(userServiceSql.getUserWhereSql(), fields, "SAKAI_USER_ID_MAP");

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public Collection findUsersByEmail(String email)
		{
			Collection rv = new Vector();

			// search for it
			Object[] fields = new Object[1];
			fields[0] = email.toLowerCase();
			List users = super.getSelectedResources("EMAIL_LC = ?", fields);
			if (users != null)
			{
				rv.addAll(users);
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
				String attribUser = sessionManager().getCurrentSessionUserId();

				// if no current user, since we are working up a new user record, use the user id as creator...
				if ((attribUser == null) || (attribUser.length() == 0)) attribUser = (String) rv[0];

				Time now = timeService().newTime();
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
				rv[1] = StringUtils.trimToEmpty(edit.getEmail());
				rv[2] = StringUtils.trimToEmpty(edit.getEmail().toLowerCase());
				rv[3] = StringUtils.trimToEmpty(edit.getFirstName());
				rv[4] = StringUtils.trimToEmpty(edit.getLastName());
				rv[5] = StringUtils.trimToEmpty(edit.getType());
				rv[6] = StringUtils.trimToEmpty(((BaseUserEdit) edit).m_pw);

				// for creator and modified by, if null, make it the id
				rv[7] = StringUtils.trimToNull(((BaseUserEdit) edit).m_createdUserId);
				if (rv[7] == null)
				{
					rv[7] = rv[0];
				}
				rv[8] = StringUtils.trimToNull(((BaseUserEdit) edit).m_lastModifiedUserId);
				if (rv[8] == null)
				{
					rv[8] = rv[0];
				}

				rv[9] = edit.getCreatedDate();
				rv[10] = edit.getModifiedDate();
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
				Instant createdOn = Instant.ofEpochMilli(result.getTimestamp(10, sqlService().getCal()).getTime());
				Instant modifiedOn = Instant.ofEpochMilli(result.getTimestamp(11, sqlService().getCal()).getTime());

				// find the eid from the mapping
				String eid = checkMapForEid(id);
				if (eid == null)
				{
					log.warn("readSqlResultRecord: null eid for id: " + id);
				}

				// create the Resource from these fields
				return new BaseUserEdit(id, eid, email, firstName, lastName, type, pw, createdBy, createdOn, modifiedBy, modifiedOn);
			}
			catch (SQLException e)
			{
				log.warn("readSqlResultRecord: " + e);
				return null;
			}
		}

		/**
		 * Create a mapping between the id and eid.
		 *
		 * @param id
		 *        The user id.
		 * @param eid
		 *        The user eid.
		 * @return true if successful, false if not (id or eid might be in use).
		 */
		public boolean putMap(String id, String eid)
		{
			// if we are not doing separate id/eid, do nothing
			if (!m_separateIdEid) return true;

			String statement = userServiceSql.getInsertUserIdSql();

			Object fields[] = new Object[2];
			fields[0] = id;
			fields[1] = eid;

			if ( m_sql.dbWrite(statement, fields) ) {
				cache.put(IDCACHE+eid,id);
				cache.put(EIDCACHE+id,eid);
				return true;
			}
			return false;
		}

		/**
		 * Update the mapping
		 *
		 * @param id
		 *        The user id.
		 * @param eid
		 *        The user eid.
		 * @return true if successful, false if not (id or eid might be in use).
		 */
		protected boolean updateMap(String id, String eid)
		{
			// if we are not doing separate id/eid, do nothing
			if (!m_separateIdEid) return true;

			// do we have this id mapped?
			String eidAlready = checkMapForEid(id);

			// if not, add it
			if (eidAlready == null)
			{
				return putMap(id, eid);
			}

			// we have a mapping, is it what we want?
			if (eidAlready.equals(eid)) return true;

			// update the cache
			// we have a mapping that needs to be updated
			String statement = userServiceSql.getUpdateUserIdSql();



			Object fields[] = new Object[2];
			fields[0] = eid;
			fields[1] = id;

			if ( m_sql.dbWrite(statement, fields) ) {
				cache.put(IDCACHE+eid,id);
				cache.put(EIDCACHE+id,eid);
				return true;
			}
			return false;
		}

		/**
		 * Remove the mapping for this id
		 *
		 * @param id
		 *        The user id.
		 */
		protected void unMap(String id)
		{
			// if we are not doing separate id/eid, do nothing
			if (!m_separateIdEid) return;

			// clear both sides of the cache
			String eid = (String) cache.get(EIDCACHE+id);
			if ( eid != null ) {
				cache.remove(IDCACHE+eid);
			}
			cache.remove(EIDCACHE+id);

			String statement = userServiceSql.getDeleteUserIdSql();

			Object fields[] = new Object[1];
			fields[0] = id;

			m_sql.dbWrite(statement, fields);
		}

		/**
		 * Check the id -> eid mapping: lookup this id and return the eid if found
		 *
		 * @param id
		 *        The user id to lookup.
		 * @return The eid mapped to this id, or null if none.
		 */
		public String checkMapForEid(String id)
		{
			// if we are not doing separate id/eid, return the id
			if (!m_separateIdEid) return id;

			{
				String e = (String) cache.get(EIDCACHE+id);
				if ( e != null ) {
					return e;
				}
			}

			String statement = userServiceSql.getUserEidSql();
			Object fields[] = new Object[1];
			fields[0] = id;
			List rv = sqlService().dbRead(statement, fields, null);

			if (rv.size() > 0)
			{
				String eid = (String) rv.get(0);
				cache.put(IDCACHE+eid,id);
				cache.put(EIDCACHE+id,eid);
				return eid;
			}
			cache.put(EIDCACHE+id,null);

			return null;
		}


		/**
		 * Check the id -> eid mapping: lookup this eid and return the id if found
		 *
		 * @param eid
		 *        The user eid to lookup.
		 * @return The id mapped to this eid, or null if none.
		 */
		public String checkMapForId(String eid)
		{
			String id = getCachedIdByEid(eid);
			if (id != null)
			{
				return id;
			}

			String statement = userServiceSql.getUserIdSql();
			Object fields[] = new Object[1];
			fields[0] = eid;
			List rv = sqlService().dbRead(statement, fields, null);

			if (rv.size() > 0)
			{
				id = (String) rv.get(0);
				cache.put(EIDCACHE+id,eid);
				cache.put(IDCACHE+eid,id);
				return id;
			}

			cache.put(IDCACHE+eid,null);
			return null;
		}

		protected String getCachedIdByEid(String eid)
		{
			// if we are not doing separate id/eid, do nothing
			if (!m_separateIdEid) return eid;

			String e = (String) cache.get(IDCACHE+eid);
			if ( e != null )
			{
				return e;
			}
			else
			{
				return null;
			}
		}

		protected UserEdit getCachedUserByEid(String eid)
		{
			UserEdit user = null;
			String id = getCachedIdByEid(eid);
			if (id != null)
			{
				user = getCachedUser(userReference(id));
			}
			return user;
		}

		public List<User> getUsersByIds(Collection<String> ids)
		{
			List<User> foundUsers = new ArrayList<User>();

			// Put all the already cached user records to one side.
			Set<String> idsToSearch = new HashSet<String>();
			for (String id : ids)
			{
				UserEdit cachedUser = getCachedUser(userReference(id));
				if (cachedUser != null)
				{
					foundUsers.add(cachedUser);
				}
				else
				{
					idsToSearch.add(id);
				}
			}

			UserWithEidReader userWithEidReader = new UserWithEidReader(false);
			userWithEidReader.findMappedUsers(idsToSearch);

			// Add the Sakai-maintained user records.
			foundUsers.addAll(userWithEidReader.getUsersFromSakaiData());

			// Finally, fill in the provided user records.
			List<UserEdit> usersToQueryProvider = userWithEidReader.getUsersToQueryProvider();
			if ((m_provider != null) && !usersToQueryProvider.isEmpty())
			{
				m_provider.getUsers(usersToQueryProvider);

				// Make sure that returned users are mapped and cached correctly.
				for (UserEdit user : usersToQueryProvider)
				{
					putUserInCaches(user);
					foundUsers.add(user);
				}
			}

			return foundUsers;
		}

		public List<User> getUsersByEids(Collection<String> eids)
		{
			List<User> foundUsers = new ArrayList<User>();

			// Put all the already cached user records to one side.
			Set<String> eidsToSearch = new HashSet<String>();
			for (String eid : eids)
			{
				UserEdit cachedUser = getCachedUserByEid(eid);
				if (cachedUser != null)
				{
					foundUsers.add(cachedUser);
				}
				else
				{
					eidsToSearch.add(eid);
				}
			}

			UserWithEidReader userWithEidReader = new UserWithEidReader(true);
			userWithEidReader.findMappedUsers(eidsToSearch);

			// Add the Sakai-maintained user records.
			foundUsers.addAll(userWithEidReader.getUsersFromSakaiData());

			// We'll need to query the provider about any EIDs which did not appear
			// in the ID-EID mapping table, since this might be the first time
			// we've encountered them.
			List<UserEdit> usersToQueryProvider = new ArrayList<UserEdit>(userWithEidReader.getUsersToQueryProvider());
			for (UserEdit user : userWithEidReader.getUsersFromSakaiData())
			{
				eidsToSearch.remove(user.getEid());
			}
			for (UserEdit user : userWithEidReader.getUsersToQueryProvider())
			{
				eidsToSearch.remove(user.getEid());
			}
			for (String eid : eidsToSearch)
			{
				usersToQueryProvider.add(new BaseUserEdit(null, eid));
			}

			// Finally, fill in the provided user records.
			if ((m_provider != null) && !usersToQueryProvider.isEmpty())
			{
				m_provider.getUsers(usersToQueryProvider);

				// Make sure that returned users are mapped and cached correctly.
				for (UserEdit user : usersToQueryProvider)
				{
					ensureMappedIdForProvidedUser(user);
					putUserInCaches(user);
					foundUsers.add(user);
				}
			}

			return foundUsers;
		}

		protected void putUserInCaches(UserEdit user)
		{
			// Update ID-EID mapping cache.
			String id = user.getId();
			String eid = user.getEid();
			cache.put(EIDCACHE+id, eid);
			cache.put(IDCACHE+eid, id);

			// Update user record cache.
			putCachedUser(userReference(id), user);
		}

		/**
		 * Given just a BaseUserEdit object, there's no officially supported way to
		 * distinguish between a Sakai-stored user with all null metadata and a
		 * mapped user whose metadata must be obtained from a provider. Rather
		 * than hack a simulated flag out of a check for all-null fields, this
		 * reader splits the database results into two piles: one of fully read
		 * user records, and one of UserEdit ID-and-EID shells. The only way to
		 * get this data out of the legacy SqlService interface is to treat the
		 * list-gathering as a side-effect of the SqlReader interface.
		 */
		protected class UserWithEidReader implements SqlReader
		{
			private List<UserEdit> usersFromSakaiData = new ArrayList<UserEdit>();
			private List<UserEdit> usersToQueryProvider = new ArrayList<UserEdit>();
			private boolean isEidSearch;

			public UserWithEidReader(boolean isEidSearch)
			{
				this.isEidSearch = isEidSearch;
			}

			public void findMappedUsers(Collection<String> searchValues)
			{
				int maxEidsInQuery = userServiceSql.getMaxInputsForSelectWhereInQueries();
				Set<String> remainingSearchValues = new HashSet<String>(searchValues);

				while (!remainingSearchValues.isEmpty())
				{
					// Break the search up into safe chunks.
					Set<String> valuesForQuery = new HashSet<String>();
					if (remainingSearchValues.size() <= maxEidsInQuery)
					{
						valuesForQuery.addAll(remainingSearchValues);
						remainingSearchValues.clear();
					}
					else
					{
						Iterator<String> valueIter = remainingSearchValues.iterator();
						for (int i = 0; i < maxEidsInQuery; i++)
						{
							valuesForQuery.add(valueIter.next());
							valueIter.remove();
						}
					}

					// Use a single query to gather all obtainable fields from
					// the Sakai user data tables.
					Object[] valueArray = valuesForQuery.toArray();
					String sqlStatement = isEidSearch ?
							userServiceSql.getUsersWhereEidsInSql(valueArray.length) :
							userServiceSql.getUsersWhereIdsInSql(valueArray.length);
					m_sql.dbRead(sqlStatement, valueArray, this);
				}
			}

			/**
			 * The return object here is of less interest than the list-gathering
			 * properties.
			 */
			public Object readSqlResultRecord(ResultSet result) throws SqlReaderFinishedException
			{
				BaseUserEdit userEdit = null;
				try
				{
					String idFromMap = result.getString(1);
					String eidFromMap = cleanEid(result.getString(2));

					// If it's a provided user, then all these will be null.
					String idFromSakaiUser = result.getString(3);
					String email = result.getString(4);
					String firstName = result.getString(5);
					String lastName = result.getString(6);
					String type = result.getString(7);
					String pw = result.getString(8);
					String createdBy = result.getString(9);
					String modifiedBy = result.getString(10);
					Instant createdOn = (result.getObject(11) != null) ? Instant.ofEpochMilli(result.getTimestamp(11, sqlService().getCal()).getTime()) : null;
					Instant modifiedOn = (result.getObject(12) != null) ? Instant.ofEpochMilli(result.getTimestamp(12, sqlService().getCal()).getTime()) : null;

					// create the Resource from these fields
					userEdit = new BaseUserEdit(idFromMap, eidFromMap, email, firstName, lastName, type, pw, createdBy, createdOn, modifiedBy, modifiedOn);

					if (idFromSakaiUser != null)
					{
						usersFromSakaiData.add(userEdit);

						// Cache management is why this needs to be an inner class.
						putUserInCaches(userEdit);
					}
					else
					{
						usersToQueryProvider.add(userEdit);
					}
				}
				catch (SQLException e)
				{
					log.warn("readSqlResultRecord: " + e, e);
				}
				return userEdit;
			}

			public List<UserEdit> getUsersFromSakaiData() {
				return usersFromSakaiData;
			}
			public List<UserEdit> getUsersToQueryProvider() {
				return usersToQueryProvider;
			}

		}
	}
}
