/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2006 2007, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.authz.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupFullException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.db.api.SqlServiceDeadlockException;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseDbFlatStorage;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * DbAuthzGroupService is an extension of the BaseAuthzGroupService with database storage.
 * </p>
 */
public abstract class DbAuthzGroupService extends BaseAuthzGroupService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(DbAuthzGroupService.class);

	/** All the event functions we know exist on the db. */
	protected Collection m_functionCache = new HashSet();

	/** All the event role names we know exist on the db. */
	protected Collection m_roleNameCache = new HashSet();

	/** Table name for realms. */
	protected String m_realmTableName = "SAKAI_REALM";

	/** Table name for realm properties. */
	protected String m_realmPropTableName = "SAKAI_REALM_PROPERTY";

	/** ID field for realm. */
	protected String m_realmIdFieldName = "REALM_ID";

	/** AuthzGroup dbid field. */
	protected String m_realmDbidField = "REALM_KEY";

	/** All "fields" for realm reading. */
	protected String[] m_realmReadFieldNames = {"REALM_ID", "PROVIDER_ID",
			"(select MAX(ROLE_NAME) from SAKAI_REALM_ROLE where ROLE_KEY = MAINTAIN_ROLE)", "CREATEDBY", "MODIFIEDBY", "CREATEDON", "MODIFIEDON",
			"REALM_KEY"};

	/** All "fields" for realm update. */
	protected String[] m_realmUpdateFieldNames = {"REALM_ID", "PROVIDER_ID",
			"MAINTAIN_ROLE = (select MAX(ROLE_KEY) from SAKAI_REALM_ROLE where ROLE_NAME = ?)", "CREATEDBY", "MODIFIEDBY", "CREATEDON", "MODIFIEDON"};

	/** All "fields" for realm insert. */
	protected String[] m_realmInsertFieldNames = {"REALM_ID", "PROVIDER_ID", "MAINTAIN_ROLE", "CREATEDBY", "MODIFIEDBY", "CREATEDON", "MODIFIEDON"};

	/** All "field values" for realm insert. */
	protected String[] m_realmInsertValueNames = {"?", "?", "(select MAX(ROLE_KEY) from SAKAI_REALM_ROLE where ROLE_NAME = ?)", "?", "?", "?", "?"};

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/

	/** map of database handlers. */
	protected Map<String, DbAuthzGroupSql> databaseBeans;

	/** The database handler we are using. */
	protected DbAuthzGroupSql dbAuthzGroupSql;

	public void setDatabaseBeans(Map databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	/**
	 * returns the bean which contains database dependent code.
	 */
	public DbAuthzGroupSql getDbAuthzGroupSql()
	{
		return dbAuthzGroupSql;
	}

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setDbAuthzGroupSql(String vendor) throws Exception
	{
		this.dbAuthzGroupSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract SqlService sqlService();

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/

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
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_realm");
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_realm_2_4_0_001");
			}

			super.init();
			setDbAuthzGroupSql(sqlService().getVendor());

			// pre-cache role and function names
			cacheRoleNames();
			cacheFunctionNames();

			M_log.info("init(): table: " + m_realmTableName + " external locks: " + m_useExternalLocks);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/*************************************************************************************************************************************************
	 * BaseAuthzGroupService extensions
	 ************************************************************************************************************************************************/

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		return new DbStorage();

	} // newStorage

	/**
	 * Check / assure this role name is defined.
	 * 
	 * @param name
	 *        the role name.
	 */
	protected void checkRoleName(String name)
	{
		if (name == null) return;
		name = name.intern();

		// check the cache to see if the role name already exists
		if (m_roleNameCache.contains(name)) return;

		// see if we have it in the db
		String statement = dbAuthzGroupSql.getCountRealmRoleSql();
		Object[] fields = new Object[1];
		fields[0] = name;

		List results = sqlService().dbRead(statement, fields, new SqlReader()
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

		boolean rv = false;
		if (!results.isEmpty())
		{
			rv = ((Integer) results.get(0)).intValue() > 0;
		}

		// write if we didn't find it
		if (!rv)
		{
			statement = dbAuthzGroupSql.getInsertRealmRoleSql();
			// write, but if it fails, we don't really care - it will fail if another app server has just written this role name
			sqlService().dbWriteFailQuiet(null, statement, fields);
		}

		synchronized (m_roleNameCache)
		{
			m_roleNameCache.add(name);
		}
	}

	/**
	 * Read all the role records, caching them
	 */
	protected void cacheRoleNames()
	{
		synchronized (m_roleNameCache)
		{
			String statement = dbAuthzGroupSql.getSelectRealmRoleSql();
			List results = sqlService().dbRead(statement, null, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String name = result.getString(1);
						m_roleNameCache.add(name);
					}
					catch (SQLException ignore)
					{
					}

					return null;
				}
			});
		}
	}

	/**
	 * Check / assure this function name is defined.
	 * 
	 * @param name
	 *        the role name.
	 */
	protected void checkFunctionName(String name)
	{
		if (name == null) return;
		name = name.intern();

		// check the cache to see if the function name already exists
		if (m_functionCache.contains(name)) return;

		// see if we have this on the db
		String statement = dbAuthzGroupSql.getCountRealmFunctionSql();
		Object[] fields = new Object[1];
		fields[0] = name;

		List results = sqlService().dbRead(statement, fields, new SqlReader()
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

		boolean rv = false;
		if (!results.isEmpty())
		{
			rv = ((Integer) results.get(0)).intValue() > 0;
		}

		// write if we didn't find it
		if (!rv)
		{
			statement = dbAuthzGroupSql.getInsertRealmFunctionSql();
			// write, but if it fails, we don't really care - it will fail if another app server has just written this function
			sqlService().dbWriteFailQuiet(null, statement, fields);
		}

		// cache the existance of the function name
		synchronized (m_functionCache)
		{
			m_functionCache.add(name);
		}
	}

	/**
	 * Read all the function records, caching them
	 */
	protected void cacheFunctionNames()
	{
		synchronized (m_functionCache)
		{
			String statement = dbAuthzGroupSql.getSelectRealmFunction1Sql();
			List results = sqlService().dbRead(statement, null, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String name = result.getString(1);
						m_functionCache.add(name);
					}
					catch (SQLException ignore)
					{
					}

					return null;
				}
			});
		}
	}

	/*************************************************************************************************************************************************
	 * Storage implementation
	 ************************************************************************************************************************************************/

	/**
	 * Covers for the BaseXmlFileStorage, providing AuthzGroup and RealmEdit parameters
	 */
	protected class DbStorage extends BaseDbFlatStorage implements Storage, SqlReader
	{
		/**
		 * Construct.
		 */
		public DbStorage()
		{
			super(m_realmTableName, m_realmIdFieldName, m_realmReadFieldNames, m_realmPropTableName, m_useExternalLocks, null, sqlService());
			m_reader = this;

			setDbidField(m_realmDbidField);
			setWriteFields(m_realmUpdateFieldNames, m_realmInsertFieldNames, m_realmInsertValueNames);

			setLocking(false);

			// setSortField(m_realmSortField, null);
		}

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public AuthzGroup get(String id)
		{
			return get(null, id);
		}

		protected AuthzGroup get(Connection conn, String id)
		{
			// read the base
			BaseAuthzGroup rv = (BaseAuthzGroup) super.getResource(conn, id);

			completeGet(conn, rv, false);

			return rv;
		}

		/**
		 * Complete the read process once the basic realm info has been read
		 * 
		 * @param realm
		 *        The real to complete
		 */
		public void completeGet(BaseAuthzGroup realm)
		{
			completeGet(null, realm, false);
		}

		/**
		 * Complete the read process once the basic realm info has been read
		 * 
		 * @param conn
		 *        optional SQL connection to use.
		 * @param realm
		 *        The real to complete.
		 * @param updateProvider
		 *        if true, update and store the provider info.
		 */
		protected void completeGet(Connection conn, final BaseAuthzGroup realm, boolean updateProvider)
		{
			if (realm == null) return;

			if (!realm.m_lazy) return;
			realm.m_lazy = false;

			// update the db and realm with latest provider
			if (updateProvider)
			{
				refreshAuthzGroup(realm);
			}

			// read the properties
			if (((BaseResourceProperties) realm.m_properties).isLazy())
			{
				((BaseResourcePropertiesEdit) realm.m_properties).setLazy(false);
				super.readProperties(conn, realm.getKey(), realm.m_properties);
			}

			// read the roles and role functions
			String sql = dbAuthzGroupSql.getSelectRealmRoleFunctionSql();
			Object fields[] = new Object[1];
			fields[0] = realm.getId();
			List all = m_sql.dbRead(conn, sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String roleName = result.getString(1);
						String functionName = result.getString(2);

						// make the role if needed
						BaseRole role = (BaseRole) realm.m_roles.get(roleName);
						if (role == null)
						{
							role = new BaseRole(roleName);
							realm.m_roles.put(role.getId(), role);
						}

						// add the function to the role
						role.allowFunction(functionName);

						return null;
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});

			// read the role descriptions
			sql = dbAuthzGroupSql.getSelectRealmRoleDescriptionSql();
			m_sql.dbRead(conn, sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String roleName = result.getString(1);
						String description = result.getString(2);
						boolean providerOnly = "1".equals(result.getString(3));

						// find the role - create it if needed
						// Note: if the role does not yet exist, it has no functions
						BaseRole role = (BaseRole) realm.m_roles.get(roleName);
						if (role == null)
						{
							role = new BaseRole(roleName);
							realm.m_roles.put(role.getId(), role);
						}

						// set the description
						role.setDescription(description);

						// set the provider only flag
						role.setProviderOnly(providerOnly);

						return null;
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});

			// read the role grants
			sql = dbAuthzGroupSql.getSelectRealmRoleGroup1Sql();
			all = m_sql.dbRead(conn, sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String roleName = result.getString(1);
						String userId = result.getString(2);
						String active = result.getString(3);
						String provided = result.getString(4);

						// give the user one and only one role grant - there should be no second...
						BaseMember grant = (BaseMember) realm.m_userGrants.get(userId);
						if (grant == null)
						{
							// find the role - if it does not exist, create it for this grant
							// NOTE: it would have no functions or description
							BaseRole role = (BaseRole) realm.m_roles.get(roleName);
							if (role == null)
							{
								role = new BaseRole(roleName);
								realm.m_roles.put(role.getId(), role);
							}

							grant = new BaseMember(role, "1".equals(active), "1".equals(provided), userId);

							realm.m_userGrants.put(userId, grant);
						}
						else
						{
							M_log.warn("completeGet: additional user - role grant: " + userId + " " + roleName);
						}

						return null;
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});
		}

		/**
		 * {@inheritDoc}
		 */
		public List getAuthzGroups(String criteria, PagingPosition page)
		{
			List rv = null;

			if (criteria != null)
			{
				criteria = "%" + criteria + "%";
				String where = "( UPPER(REALM_ID) like UPPER(?) or UPPER(PROVIDER_ID) like UPPER(?) )";
				Object[] fields = new Object[2];
				fields[0] = criteria;
				fields[1] = criteria;

				// paging
				if (page != null)
				{
					// adjust to the size of the set found
					// page.validate(rv.size());

					rv = getSelectedResources(where, fields, page.getFirst(), page.getLast());
				}
				else
				{
					rv = getSelectedResources(where, fields);
				}
			}

			else
			{
				// paging
				if (page != null)
				{
					// adjust to the size of the set found
					// page.validate(rv.size());

					rv = getAllResources(page.getFirst(), page.getLast());
				}
				else
				{
					rv = getAllResources();
				}
			}

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public List getAuthzUserGroupIds(ArrayList authzGroupIds, String userid)
		{
			if (authzGroupIds == null || userid == null || authzGroupIds.size() < 1)
				return new ArrayList(); // empty list

			String inClause = orInClause( authzGroupIds.size(), "SAKAI_REALM.REALM_ID" );
			String statement = dbAuthzGroupSql.getSelectRealmUserGroupSql( inClause );
			Object[] fields = new Object[authzGroupIds.size()+1];
			for ( int i=0; i<authzGroupIds.size(); i++ )
			{
				fields[i] = authzGroupIds.get(i);
			}
			fields[authzGroupIds.size()] = userid;
			
			return sqlService().dbRead(statement, fields, null );
		}

		/**
		 * {@inheritDoc}
		 */
		public int countAuthzGroups(String criteria)
		{
			int rv = 0;

			if (criteria != null)
			{
				criteria = "%" + criteria + "%";
				String where = "( UPPER(REALM_ID) like UPPER(?) or UPPER(PROVIDER_ID) like UPPER(?) )";
				Object[] fields = new Object[2];
				fields[0] = criteria;
				fields[1] = criteria;

				rv = countSelectedResources(where, fields);
			}

			else
			{
				rv = countAllResources();
			}

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public Set getProviderIds(String authzGroupId)
		{
			String statement = dbAuthzGroupSql.getSelectRealmProviderId1Sql();
			List results = sqlService().dbRead(statement, new Object[] {authzGroupId}, null);
			if (results == null)
			{
				return new HashSet();
			}
			return new HashSet(results);
		}

		/**
		 * {@inheritDoc}
		 */
		public Set getAuthzGroupIds(String providerId)
		{
			String statement = dbAuthzGroupSql.getSelectRealmIdSql();
			List results = sqlService().dbRead(statement, new Object[] {providerId}, null);
			if (results == null)
			{
				return new HashSet();
			}
			return new HashSet(results);
		}

		/**
		 * {@inheritDoc}
		 */
		public Set getAuthzGroupsIsAllowed(String userId, String lock, Collection azGroups)
		{
			// further limited to only those authz groups in the azGroups parameter if not null

			// if azGroups is not null, but empty, we can short-circut and return an empty set
			// or if the lock is null
			if (((azGroups != null) && azGroups.isEmpty()) || lock == null)
			{
				return new HashSet();
			}

			// Just like unlock, except we use all realms and get their ids
			// Note: consider over all realms just those realms where there's a grant of a role that satisfies the lock
			// Ignore realms where anon or auth satisfy the lock.

			boolean auth = (userId != null) && (!userDirectoryService().getAnonymousUser().getId().equals(userId));
			String sql = dbAuthzGroupSql.getSelectRealmIdSql(azGroups);
			int size = 2;
			String roleswap = null; // define the roleswap variable
			if (azGroups != null)
			{
				size += azGroups.size();
				for (Iterator i = azGroups.iterator(); i.hasNext();)
				{
					// FIXME - just use the azGroups directly rather than split them up
					String[] refs = StringUtil.split(i.next().toString(), Entity.SEPARATOR); // splits the azGroups values so we can look for swapped state
					for (int i2 = 0; i2 < refs.length; i2++)  // iterate through the groups to see if there is a swapped state in the variable
					{
						roleswap = SecurityService.getUserEffectiveRole("/site/" + refs[i2]);
						if (roleswap!=null) // break from this loop if a swapped state is found
							break;
					}
					if (roleswap!=null)
					{
						sql = dbAuthzGroupSql.getSelectRealmIdRoleSwapSql(azGroups);  // redefine the sql we use if there's a role swap
						size++; // increase the "size" by 1 for our new sql
						break; // break from the loop
					}
				}
			}
			Object[] fields = new Object[size];
			fields[0] = lock;
			fields[1] = userId;
			if (azGroups != null)
			{
				int pos = 2;
				for (Iterator i = azGroups.iterator(); i.hasNext();)
				{
					fields[pos++] = i.next();
				}
				if (roleswap!=null) // add in name of the role for the alternate query
				{
					fields[pos++] = roleswap;
				}
			}

			// Get resultset
			List results = m_sql.dbRead(sql, fields, null);
			Set rv = new HashSet();
			rv.addAll(results);

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public AuthzGroup put(String id)
		{
			BaseAuthzGroup rv = (BaseAuthzGroup) super.putResource(id, fields(id, null, false));
			if (rv != null)
			{
				rv.activate();
			}

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public AuthzGroup edit(String id)
		{
			BaseAuthzGroup edit = (BaseAuthzGroup) super.editResource(id);

			if (edit != null)
			{
				edit.activate();
				completeGet(null, edit, true);
			}

			return edit;

		}

		/**
		 * @inheritDoc
		 */
		public void addNewUser(final AuthzGroup azGroup, final String userId, final String role, final int maxSize) throws GroupFullException
		{

			// run our save code in a transaction that will restart on deadlock
			// if deadlock retry fails, or any other error occurs, a runtime error will be thrown

			m_sql.transact(new Runnable()
			{
				public void run()
				{
					addNewUserTx(azGroup, userId, role, maxSize);
				}
			}, "azg:" + azGroup.getId());

		}

		/**
		 * The transaction code to save the azg.
		 * 
		 * @param edit
		 *        The azg to save.
		 */
		protected void addNewUserTx(AuthzGroup edit, String userId, String role, int maxSize) throws GroupFullException
		{
			// Assume that users added in this way are always active and never provided
			boolean active = true;
			boolean provided = false;

			String sql;

			// Lock the table and count users if required
			if (maxSize > 0)
			{

				// Get the REALM_KEY and lock the realm for update
				sql = dbAuthzGroupSql.getSelectRealmUpdate();
				Object fields[] = new Object[1];
				fields[0] = edit.getId();

				List resultsKey = m_sql.dbRead(sql, fields, new SqlReader()
				{
					public Object readSqlResultRecord(ResultSet result)
					{
						try
						{
							int realm_key = result.getInt(1);
							return Integer.valueOf(realm_key);
						}
						catch (Throwable e)
						{
							M_log.warn("addNewUserTx: " + e.toString());
							return null;
						}
					}
				});

				int realm_key = -1;
				if (!resultsKey.isEmpty())
				{
					realm_key = ((Integer) resultsKey.get(0)).intValue();
				}
				else
				{
					// Can't find the REALM_KEY for this REALM (should never happen)
					M_log.error("addNewUserTx: can't find realm " + edit.getId());
				}

				// Count the number of users already in the realm
				sql = dbAuthzGroupSql.getSelectRealmSize();
				fields[0] = Integer.valueOf(realm_key);

				List resultsSize = m_sql.dbRead(sql, fields, new SqlReader()
				{
					public Object readSqlResultRecord(ResultSet result)
					{
						try
						{
							int count = result.getInt(1);
							return Integer.valueOf(count);
						}
						catch (Throwable e)
						{
							M_log.warn("addNewUserTx: " + e.toString());
							return null;
						}
					}
				});

				int currentSize = resultsSize.isEmpty() ? -1 : ((Integer) resultsSize.get(0)).intValue();

				if ((currentSize < 0) || (currentSize >= maxSize))
				{
					// We can't add the user - group already full, or we can't find the size
					throw new GroupFullException(edit.getId());
				}
			}

			// Add the user to SAKAI_REALM_RL_GR
			sql = dbAuthzGroupSql.getInsertRealmRoleGroup1Sql();

			Object fields[] = new Object[5];
			fields[0] = edit.getId();
			fields[1] = userId;
			fields[2] = role;
			fields[3] = active ? "1" : "0";
			fields[4] = provided ? "1" : "0";
			m_sql.dbWrite(sql, fields);

			// update the main realm table for new modified time and last-modified-by
			super.commitResource(edit, fields(edit.getId(), ((BaseAuthzGroup) edit), true), null);
		}

		/**
		 * @inheritDoc
		 */
		public void removeUser(final AuthzGroup azGroup, final String userId)
		{

			// run our save code in a transaction that will restart on deadlock
			// if deadlock retry fails, or any other error occurs, a runtime error will be thrown

			m_sql.transact(new Runnable()
			{
				public void run()
				{
					removeUserTx(azGroup, userId);
				}
			}, "azg:" + azGroup.getId());

		}

		/**
		 * The transaction code to save the azg.
		 * 
		 * @param edit
		 *        The azg to save.
		 */
		protected void removeUserTx(AuthzGroup edit, String userId)
		{
			// Remove the user from SAKAI_REALM_RL_GR
			String sql = dbAuthzGroupSql.getDeleteRealmRoleGroup4Sql();

			Object fields[] = new Object[2];
			fields[0] = edit.getId();
			fields[1] = userId;
			m_sql.dbWrite(sql, fields);

			// update the main realm table for new modified time and last-modified-by
			super.commitResource(edit, fields(edit.getId(), ((BaseAuthzGroup) edit), true), null);
		}

		/**
		 * @inheritDoc
		 */
		public void save(final AuthzGroup edit)
		{
			// pre-check the roles and functions to make sure they are all defined
			for (Iterator iRoles = ((BaseAuthzGroup) edit).m_roles.values().iterator(); iRoles.hasNext();)
			{
				Role role = (Role) iRoles.next();

				// make sure the role name is defined / define it
				checkRoleName(role.getId());

				for (Iterator iFunctions = role.getAllowedFunctions().iterator(); iFunctions.hasNext();)
				{
					String function = (String) iFunctions.next();

					// make sure the role name is defined / define it
					checkFunctionName(function);
				}
			}

			// run our save code in a transaction that will restart on deadlock
			// if deadlock retry fails, or any other error occurs, a runtime error will be thrown
			m_sql.transact(new Runnable()
			{
				public void run()
				{
					saveTx(edit);
				}
			}, "azg:" + edit.getId());

			// update with the provider
			refreshAuthzGroup((BaseAuthzGroup) edit);
		}

		/**
		 * The transaction code to save the azg.
		 * 
		 * @param edit
		 *        The azg to save.
		 */
		protected void saveTx(AuthzGroup edit)
		{
			// update SAKAI_REALM_RL_FN: read, diff with the edit, add and delete
			save_REALM_RL_FN(edit);

			// update SAKAI_REALM_RL_GR
			save_REALM_RL_GR(edit);

			// update SAKAI_REALM_PROVIDER
			save_REALM_PROVIDER(edit);

			// update SAKAI_REALM_ROLE_DESC
			save_REALM_ROLE_DESC(edit);

			// update the main realm table and properties
			super.commitResource(edit, fields(edit.getId(), ((BaseAuthzGroup) edit), true), edit.getProperties(), ((BaseAuthzGroup) edit).getKey());
		}

		protected void save_REALM_RL_FN(AuthzGroup azg)
		{
			// add what we have in the azg, unless we see it in the db
			final Set<RoleAndFunction> toAdd = new HashSet<RoleAndFunction>();
			for (Iterator iRoles = ((BaseAuthzGroup) azg).m_roles.values().iterator(); iRoles.hasNext();)
			{
				Role role = (Role) iRoles.next();
				for (Iterator iFunctions = role.getAllowedFunctions().iterator(); iFunctions.hasNext();)
				{
					String function = (String) iFunctions.next();
					toAdd.add(new RoleAndFunction(role.getId(), function));
				}
			}

			// delete anything we see in the db we don't have in the azg
			final Set<RoleAndFunction> toDelete = new HashSet<RoleAndFunction>();

			// read what we have there now
			String sql = dbAuthzGroupSql.getSelectRealmFunction2Sql();
			Object fields[] = new Object[1];
			fields[0] = caseId(azg.getId());
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String role = result.getString(1);
						String function = result.getString(2);
						RoleAndFunction raf = new RoleAndFunction(role, function);

						// if we have it in the set toAdd, we can remove it (it's alredy on the db)
						if (toAdd.contains(raf))
						{
							toAdd.remove(raf);
						}

						// if we don't have it in the azg, we need to delete it
						else
						{
							toDelete.add(raf);
						}
					}
					catch (Throwable e)
					{
						M_log.warn("save_REALM_RL_FN: " + e.toString());
					}

					return null;
				}
			});

			fields = new Object[3];
			fields[0] = caseId(azg.getId());

			// delete what we need to
			sql = dbAuthzGroupSql.getDeleteRealmRoleFunction1Sql();
			for (RoleAndFunction raf : toDelete)
			{
				fields[1] = raf.role;
				fields[2] = raf.function;
				m_sql.dbWrite(sql, fields);
			}

			// add what we need to
			sql = dbAuthzGroupSql.getInsertRealmRoleFunctionSql();

			fields[0] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleFunction1Sql(), fields[0]);
			for (RoleAndFunction raf : toAdd)
			{
				fields[1] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleFunction2Sql(), raf.role);
				fields[2] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleFunction3Sql(), raf.function);
				m_sql.dbWrite(sql, fields);
			}
		}

		protected void save_REALM_RL_GR(AuthzGroup azg)
		{
			// add what we have in the azg, unless we see it in the db
			final Set<UserAndRole> toAdd = new HashSet<UserAndRole>();
			for (Iterator i = ((BaseAuthzGroup) azg).m_userGrants.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry entry = (Map.Entry) i.next();
				Member grant = (Member) entry.getValue();
				toAdd.add(new UserAndRole(grant.getUserId(), grant.getRole().getId(), grant.isActive(), grant.isProvided()));
			}

			// delete anything we see in the db we don't have in the azg
			final Set<UserAndRole> toDelete = new HashSet<UserAndRole>();

			// read what we have there now
			String sql = dbAuthzGroupSql.getSelectRealmRoleGroup2Sql();
			Object fields[] = new Object[1];
			fields[0] = caseId(azg.getId());
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String userId = result.getString(1);
						String role = result.getString(2);
						boolean active = "1".equals(result.getString(3));
						boolean provided = "1".equals(result.getString(4));
						UserAndRole uar = new UserAndRole(userId, role, active, provided);

						// if we have it in the set toAdd, we can remove it (it's alredy on the db)
						if (toAdd.contains(uar))
						{
							toAdd.remove(uar);
						}

						// if we don't have it in the azg, we need to delete it
						else
						{
							toDelete.add(uar);
						}
					}
					catch (Throwable e)
					{
						M_log.warn("save_REALM_RL_GR: " + e.toString());
					}

					return null;
				}
			});

			fields = new Object[5];
			fields[0] = caseId(azg.getId());

			// delete what we need to
			sql = dbAuthzGroupSql.getDeleteRealmRoleGroup1Sql();
			for (UserAndRole uar : toDelete)
			{
				fields[1] = uar.role;
				fields[2] = uar.userId;
				fields[3] = uar.active ? "1" : "0";
				fields[4] = uar.provided ? "1" : "0";
				m_sql.dbWrite(sql, fields);
			}

			// add what we need to
			sql = dbAuthzGroupSql.getInsertRealmRoleGroup1Sql();
			fields[0] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleGroup1_1Sql(), fields[0]);
			for (UserAndRole uar : toAdd)
			{
				fields[1] = uar.userId;
				fields[2] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleGroup1_2Sql(), uar.role);
				fields[3] = uar.active ? "1" : "0";
				fields[4] = uar.provided ? "1" : "0";
				m_sql.dbWrite(sql, fields);
			}
		}

		protected void save_REALM_PROVIDER(AuthzGroup azg)
		{
			// we we are not provider, delete any for this realm
			if ((azg.getProviderGroupId() == null) || (m_provider == null))
			{
				String sql = dbAuthzGroupSql.getDeleteRealmProvider1Sql();
				Object[] fields = new Object[1];
				fields[0] = caseId(azg.getId());
				m_sql.dbWrite(sql, fields);
				return;
			}

			// add what we have in the azg, unless we see it in the db
			final Set<String> toAdd = new HashSet<String>();
			String[] ids = m_provider.unpackId(azg.getProviderGroupId());
			if (ids != null)
			{
				for (String id : ids)
				{
					toAdd.add(id);
				}
			}

			// delete anything we see in the db we don't have in the azg
			final Set<String> toDelete = new HashSet<String>();

			// read what we have there now
			String sql = dbAuthzGroupSql.getSelectRealmProviderId2Sql();
			Object fields[] = new Object[1];
			fields[0] = caseId(azg.getId());
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String provider = result.getString(1);

						// if we have it in the set toAdd, we can remove it (it's alredy on the db)
						if (toAdd.contains(provider))
						{
							toAdd.remove(provider);
						}

						// if we don't have it in the azg, we need to delete it
						else
						{
							toDelete.add(provider);
						}
					}
					catch (Throwable e)
					{
						M_log.warn("save_REALM_PROVIDER: " + e.toString());
					}

					return null;
				}
			});

			fields = new Object[2];
			fields[0] = caseId(azg.getId());

			// delete what we need to
			sql = dbAuthzGroupSql.getDeleteRealmProvider2Sql();
			for (String provider : toDelete)
			{
				fields[1] = provider;
				m_sql.dbWrite(sql, fields);
			}

			// add what we need to
			sql = dbAuthzGroupSql.getInsertRealmProviderSql();
			for (String provider : toAdd)
			{
				fields[1] = provider;
				m_sql.dbWrite(sql, fields);
			}
		}

		protected void save_REALM_ROLE_DESC(AuthzGroup azg)
		{
			// add what we have in the azg, unless we see it in the db
			final Set<RoleAndDescription> toAdd = new HashSet<RoleAndDescription>();
			for (Iterator iRoles = ((BaseAuthzGroup) azg).m_roles.values().iterator(); iRoles.hasNext();)
			{
				Role role = (Role) iRoles.next();
				toAdd.add(new RoleAndDescription(role.getId(), role.getDescription(), role.isProviderOnly()));
			}

			// delete anything we see in the db we don't have in the azg
			final Set<RoleAndDescription> toDelete = new HashSet<RoleAndDescription>();

			// read what we have there now
			String sql = dbAuthzGroupSql.getSelectRealmProvider2Sql();
			Object fields[] = new Object[1];
			fields[0] = caseId(azg.getId());
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String role = result.getString(1);
						String description = result.getString(2);
						boolean providerOnly = "1".equals(result.getString(3));
						RoleAndDescription rad = new RoleAndDescription(role, description, providerOnly);

						// if we have it in the set toAdd, we can remove it (it's alredy on the db)
						if (toAdd.contains(rad))
						{
							toAdd.remove(rad);
						}

						// if we don't have it in the azg, we need to delete it
						else
						{
							toDelete.add(rad);
						}
					}
					catch (Throwable e)
					{
						M_log.warn("save_REALM_ROLE_DESC: " + e.toString());
					}

					return null;
				}
			});

			fields = new Object[2];
			fields[0] = caseId(azg.getId());

			// delete what we need to
			sql = dbAuthzGroupSql.getDeleteRealmRoleDescription1Sql();
			for (RoleAndDescription rad : toDelete)
			{
				fields[1] = rad.role;
				m_sql.dbWrite(sql, fields);
			}

			fields = new Object[4];
			fields[0] = caseId(azg.getId());

			// add what we need to
			sql = dbAuthzGroupSql.getInsertRealmRoleDescriptionSql();
			fields[0] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleDescription1Sql(), fields[0]);
			for (RoleAndDescription rad : toAdd)
			{
				fields[1] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleDescription2Sql(), rad.role);
				fields[2] = rad.description;
				fields[3] = rad.providerOnly ? "1" : "0";
				m_sql.dbWrite(sql, fields);
			}
		}

		public void cancel(AuthzGroup edit)
		{
			super.cancelResource(edit);
		}

		public void remove(final AuthzGroup edit)
		{
			// in a transaction
			m_sql.transact(new Runnable()
			{
				public void run()
				{
					removeTx(edit);
				}
			}, "azgRemove:" + edit.getId());
		}

		/**
		 * Transaction code for removing the azg.
		 */
		protected void removeTx(AuthzGroup edit)
		{
			// delete all the role functions, auth grants, anon grants, role grants, fucntion grants
			// and then the realm and release the lock.

			// delete the role functions, role grants, provider entries
			Object fields[] = new Object[1];
			fields[0] = caseId(edit.getId());

			String statement = dbAuthzGroupSql.getDeleteRealmRoleFunction2Sql();
			m_sql.dbWrite(statement, fields);

			statement = dbAuthzGroupSql.getDeleteRealmRoleGroup2Sql();
			m_sql.dbWrite(statement, fields);

			statement = dbAuthzGroupSql.getDeleteRealmProvider1Sql();
			m_sql.dbWrite(statement, fields);

			statement = dbAuthzGroupSql.getDeleteRealmRoleDescription2Sql();
			m_sql.dbWrite(statement, fields);

			// delete the realm and properties
			super.removeResource(edit, ((BaseAuthzGroup) edit).getKey());
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
		protected Object[] fields(String id, BaseAuthzGroup edit, boolean idAgain)
		{
			Object[] rv = new Object[idAgain ? 8 : 7];
			rv[0] = caseId(id);
			if (idAgain)
			{
				rv[7] = rv[0];
			}

			if (edit == null)
			{
				String current = sessionManager().getCurrentSessionUserId();

				// if no current user, since we are working up a new user record, use the user id as creator...
				if (current == null) current = "";

				Time now = timeService().newTime();

				rv[1] = "";
				rv[2] = "";
				rv[3] = current;
				rv[4] = current;
				rv[5] = now;
				rv[6] = now;
			}

			else
			{
				rv[1] = StringUtil.trimToZero(edit.m_providerRealmId);
				rv[2] = StringUtil.trimToZero(edit.m_maintainRole);
				rv[3] = StringUtil.trimToZero(edit.m_createdUserId);
				rv[4] = StringUtil.trimToZero(edit.m_lastModifiedUserId);
				rv[5] = edit.getCreatedTime();
				rv[6] = edit.getModifiedTime();
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
				String providerId = result.getString(2);
				String maintainRole = result.getString(3);
				String createdBy = result.getString(4);
				String modifiedBy = result.getString(5);
				java.sql.Timestamp ts = result.getTimestamp(6, sqlService().getCal());
				Time createdOn = null;
				if (ts != null)
				{
					createdOn = timeService().newTime(ts.getTime());
				}
				ts = result.getTimestamp(7, sqlService().getCal());
				Time modifiedOn = null;
				if (ts != null)
				{
					modifiedOn = timeService().newTime(ts.getTime());
				}

				// the special local integer 'db' id field, read after the field list
				Integer dbid = Integer.valueOf(result.getInt(8));

				// create the Resource from these fields
				return new BaseAuthzGroup(DbAuthzGroupService.this,dbid, id, providerId, maintainRole, createdBy, createdOn, modifiedBy, modifiedOn);
			}
			catch (SQLException e)
			{
				M_log.warn("readSqlResultRecord: " + e);
				return null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isAllowed(String userId, String lock, String realmId)
		{
			if ((lock == null) || (realmId == null)) return false;

			// does the user have any roles granted that include this lock, based on grants or anon/auth?
			boolean auth = (userId != null) && (!userDirectoryService().getAnonymousUser().getId().equals(userId));

			String statement = dbAuthzGroupSql.getCountRealmRoleFunctionSql(ANON_ROLE, AUTH_ROLE, auth);
			Object[] fields = new Object[3];
			fields[0] = userId;
			fields[1] = lock;
			fields[2] = realmId;

			// checks to see if the user has the roleswap variable set in the session
			String roleswap = SecurityService.getUserEffectiveRole(realmId);
			
            if (roleswap != null)
            {
            	fields[0] = roleswap; // set the field to the student role for the alternate sql
            	statement = dbAuthzGroupSql.getCountRoleFunctionSql(); // set the function for our alternate sql
            }
            
			List resultsNew = m_sql.dbRead(statement, fields, new SqlReader()
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

			boolean rvNew = false;
			int countNew = -1;
			if (!resultsNew.isEmpty())
			{
				countNew = ((Integer) resultsNew.get(0)).intValue();
				rvNew = countNew > 0;
			}

			return rvNew;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isAllowed(String userId, String lock, Collection realms)
		{
			if (lock == null) return false;

			boolean auth = (userId != null) && (!userDirectoryService().getAnonymousUser().getId().equals(userId));

			if (realms == null || realms.size() < 1)
			{
				M_log.warn("isAllowed(): called with no realms: lock: " + lock + " user: " + userId);
				if (M_log.isDebugEnabled())
				{
					try
					{
						throw new Exception();
					}
					catch (Exception e)
					{
						M_log.debug("isAllowed():", e);
					}
				}
				return false;
			}

			String inClause = orInClause(realms.size(), "SAKAI_REALM.REALM_ID");

			// any of the grant or role realms
			String statement = dbAuthzGroupSql.getCountRealmRoleFunctionSql(ANON_ROLE, AUTH_ROLE, auth, inClause);
			Object[] fields = new Object[2 + (2 * realms.size())];
			Object[] fields2 = new Object[3]; // for roleswap
			int pos = 0;
			String siteId = "";
			for (Iterator i = realms.iterator(); i.hasNext();)
			{
				String role = (String) i.next();
				if (role.startsWith("/site/"))
				{
					fields2[2] = role;
					siteId = role; // set this variable for potential use later 
				}
				fields[pos++] = role;
			}
			fields[pos++] = lock;
			fields[pos++] = userId;
			for (Iterator i = realms.iterator(); i.hasNext();)
			{
				String role = (String) i.next();
				fields[pos++] = role;
			}

			// TODO: would be better to get this initially to make the code more efficient, but the realms collection does not have a common 
			// order for the site's id which is needed to determine if the session variable exists
			String roleswap = SecurityService.getUserEffectiveRole( (String) fields2[2]);
			
			List results = null;
			if (roleswap != null)
            {
				fields2[0] = roleswap;
				fields2[1] = lock;
				
				statement = dbAuthzGroupSql.getCountRoleFunctionSql();
				
				// check the main site id first for the permission since this  
				results = m_sql.dbRead(statement, fields2, new SqlReader()
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
				
				boolean rv = false;
				int count = -1;
				if (!results.isEmpty())
				{
					count = ((Integer) results.get(0)).intValue();
					rv = count > 0;
				}
				if (rv) // if true, go ahead and return
					return rv;
				
				for (Iterator i = realms.iterator(); i.hasNext();)
				{
					String site = (String) i.next();
					if (site == siteId) // we've already checked this so no need to do it again
						continue;
					
					fields2[2] = site;
				
					results = m_sql.dbRead(statement, fields2, new SqlReader()
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
					
					count = -1;
					if (!results.isEmpty())
					{
						count = ((Integer) results.get(0)).intValue();
						rv = count > 0;
					}
					if (rv) // if true, go ahead and return
						return rv;
					else if (!i.hasNext()) // if this is the last one and we still have not gotten a true result, go ahead and return the false
						return rv;
				}
            }
			else
			{
				results = m_sql.dbRead(statement, fields, new SqlReader()
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
			}

			boolean rv = false;
			int count = -1;
			if (!results.isEmpty())
			{
				count = ((Integer) results.get(0)).intValue();
				rv = count > 0;
			}

			return rv;

			// return rvNew;
		}

		/**
		 * {@inheritDoc}
		 */
		public Set getUsersIsAllowed(String lock, Collection realms)
		{
			if ((lock == null) || (realms == null) || (realms.isEmpty())) return new HashSet();

			String sql = dbAuthzGroupSql.getSelectRealmRoleGroupUserIdSql(orInClause(realms.size(), "SR.REALM_ID"), orInClause(realms.size(),
					"SR1.REALM_ID"));
			Object[] fields = new Object[1 + (2 * realms.size())];
			int pos = 0;
			for (Iterator i = realms.iterator(); i.hasNext();)
			{
				String roleRealm = (String) i.next();
				fields[pos++] = roleRealm;
			}
			fields[pos++] = lock;
			for (Iterator i = realms.iterator(); i.hasNext();)
			{
				String roleRealm = (String) i.next();
				fields[pos++] = roleRealm;
			}

			// read the strings
			List results = m_sql.dbRead(sql, fields, null);

			// prepare the return
			Set rv = new HashSet();
			rv.addAll(results);
			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<String[]> getUsersIsAllowedByGroup(String lock, Collection<String> realms)
		{
			final Set<String[]> usersByGroup = new HashSet<String[]>(); 
			
			if ((lock == null) || (realms != null && realms.isEmpty())) return usersByGroup;
			
			String sql;
			Object[] fields;
			
			if (realms != null) {
				sql = dbAuthzGroupSql.getSelectRealmRoleGroupUserIdSql(orInClause(realms.size(), "REALM_ID"));
				fields = new Object[realms.size() + 1];
				int pos = 0;
				fields[pos++] = lock;
				for (Iterator i = realms.iterator(); i.hasNext();)
				{
					String roleRealm = (String) i.next();
					fields[pos++] = roleRealm;
				}
			} else {
				sql = dbAuthzGroupSql.getSelectRealmRoleGroupUserIdSql("true");
				fields = new Object[1];
				fields[0] = lock;				
			}

			// read the strings
			m_sql.dbRead(sql, fields, new SqlReader()
					{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String[] useringroup = new String[2];
						useringroup[0] = result.getString(1);
						useringroup[1] = result.getString(2);
						
						usersByGroup.add( useringroup );
					}
					catch (SQLException ignore)
					{
					}

					return null;
				}
			});
						
			return usersByGroup;
		}

		/**
		 * {@inheritDoc}
		 */		
		public Map<String,Integer> getUserCountIsAllowed(String function, Collection<String> azGroups)
		{
			final Map<String, Integer> userCountByGroup = new HashMap<String, Integer>();
			
			if ((function == null) || (azGroups != null && azGroups.isEmpty())) return userCountByGroup;
			
			String sql;
			Object[] fields;
			
			if (azGroups != null) {
				sql = dbAuthzGroupSql.getSelectRealmRoleGroupUserCountSql(orInClause(azGroups.size(), "REALM_ID"));
				fields = new Object[azGroups.size() + 1];
				int pos = 0;
				fields[pos++] = function;

				for (Iterator i = azGroups.iterator(); i.hasNext();)
				{
					String roleRealm = (String) i.next();
					fields[pos++] = roleRealm;
				}				
			} else {
				sql = dbAuthzGroupSql.getSelectRealmRoleGroupUserCountSql("true");
				fields = new Object[1];
				fields[0] = function;
			}

			// read the realm size counts
			m_sql.dbRead(sql, fields, new SqlReader()
					{
						public Object readSqlResultRecord(ResultSet result)
						{
							try
							{
								String realm = result.getString(1);
								Integer size = result.getInt(2);
								userCountByGroup.put(realm, size);
							}
							catch (SQLException ignore)
							{
							}

							return null;
						}
					});
			
			return userCountByGroup;
		}

		
		/**
		 * {@inheritDoc}
		 */
		public Set getAllowedFunctions(String role, Collection realms)
		{
			if ((role == null) || (realms == null) || (realms.isEmpty())) return new HashSet();

			String sql = dbAuthzGroupSql.getSelectRealmFunctionFunctionNameSql(orInClause(realms.size(), "SR.REALM_ID"));
			Object[] fields = new Object[1 + realms.size()];
			fields[0] = role;
			int pos = 1;
			for (Iterator i = realms.iterator(); i.hasNext();)
			{
				String roleRealm = (String) i.next();
				fields[pos++] = roleRealm;
			}

			// read the strings
			List results = m_sql.dbRead(sql, fields, null);

			// prepare the return
			Set rv = new HashSet();
			rv.addAll(results);
			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public void refreshUser(String userId, Map providerGrants)
		{
			if (userId == null) return;

			String sql = dbAuthzGroupSql.getSelectRealmRoleGroup3Sql();
			String sqlParam = "";
			StringBuilder sqlBuf = null;
			StringBuilder sqlParamBuf = null;

			// read this user's grants from all realms
			Object[] fields = new Object[1];
			fields[0] = userId;

			List grants = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						int realmKey = result.getInt(1);
						String roleName = result.getString(2);
						String active = result.getString(3);
						String provided = result.getString(4);
						return new RealmAndRole(Integer.valueOf(realmKey), roleName, "1".equals(active), "1".equals(provided));
					}
					catch (Throwable ignore)
					{
						return null;
					}
				}
			});

			// make a map, realm id -> role granted, each for provider and non-provider (or inactive)
			Map<Integer, String> existing = new HashMap<Integer, String>();
			Map<Integer, String> nonProvider = new HashMap<Integer, String>();
			for (Iterator i = grants.iterator(); i.hasNext();)
			{
				RealmAndRole rar = (RealmAndRole) i.next();
				// active and provided are the currently stored provider grants
				if (rar.active && rar.provided)
				{
					if (existing.containsKey(rar.realmId))
					{
						M_log.warn("refreshUser: duplicate realm id found in provider grants: " + rar.realmId);
					}
					else
					{
						existing.put(rar.realmId, rar.role);
					}
				}

				// inactive or not provided are the currently stored internal grants - not to be overwritten by provider info
				else
				{
					if (nonProvider.containsKey(rar.realmId))
					{
						M_log.warn("refreshUser: duplicate realm id found in nonProvider grants: " + rar.realmId);
					}
					else
					{
						nonProvider.put(rar.realmId, rar.role);
					}
				}
			}

			// compute the user's realm roles based on the new provider information
			// same map form as existing, realm id -> role granted
			Map<Integer, String> target = new HashMap<Integer, String>();

			// for each realm that has a provider in the map, and does not have a grant for the user,
			// add the active provided grant with the map's role.

			if ((providerGrants != null) && (providerGrants.size() > 0))
			{
				// get all the realms that have providers in the map, with their full provider id

				// Assemble SQL. Note: distinct must be used because one cannot establish an equijoin between
				// SRP.PROVIDER_ID and SR.PROVIDER_ID as the values in SRP.PROVIDER_ID often include
				// additional concatenated course values. It may be worth reviewing this strategy.

				sql = dbAuthzGroupSql.getSelectRealmProviderSql(orInClause(providerGrants.size(), "SRP.PROVIDER_ID"));
				Object[] fieldsx = new Object[providerGrants.size()];
				int pos = 0;
				for (Iterator f = providerGrants.keySet().iterator(); f.hasNext();)
				{
					String providerId = (String) f.next();
					fieldsx[pos++] = providerId;
				}
				List realms = m_sql.dbRead(sql, fieldsx, new SqlReader()
				{
					public Object readSqlResultRecord(ResultSet result)
					{
						try
						{
							int id = result.getInt(1);
							String provider = result.getString(2);
							return new RealmAndProvider(Integer.valueOf(id), provider);
						}
						catch (Throwable ignore)
						{
							return null;
						}
					}
				});

				if ((realms != null) && (realms.size() > 0))
				{
					for (Iterator r = realms.iterator(); r.hasNext();)
					{
						RealmAndProvider rp = (RealmAndProvider) r.next();
						String role = (String) providerGrants.get(rp.providerId);
						if (role != null)
						{
							if (target.containsKey(rp.realmId))
							{
								M_log.warn("refreshUser: duplicate realm id computed for new grants: " + rp.realmId);
							}
							else
							{
								target.put(rp.realmId, role);
							}
						}
					}
				}
			}

			// compute the records we need to delete: every existing not in target or not matching target's role
			List<Integer> toDelete = new Vector<Integer>();
			for (Iterator i = existing.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry entry = (Map.Entry) i.next();
				Integer realmId = (Integer) entry.getKey();
				String role = (String) entry.getValue();

				String targetRole = (String) target.get(realmId);
				if ((targetRole == null) || (!targetRole.equals(role)))
				{
					toDelete.add(realmId);
				}
			}

			// compute the records we need to add: every target not in existing, or not matching's existing's role
			// we don't insert target grants that would override internal grants
			List<RealmAndRole> toInsert = new Vector<RealmAndRole>();
			for (Iterator i = target.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry entry = (Map.Entry) i.next();
				Integer realmId = (Integer) entry.getKey();
				String role = (String) entry.getValue();

				String existingRole = (String) existing.get(realmId);
				String nonProviderRole = (String) nonProvider.get(realmId);
				if ((nonProviderRole == null) && ((existingRole == null) || (!existingRole.equals(role))))
				{
					toInsert.add(new RealmAndRole(realmId, role, true, true));
				}
			}

			// if any, do it
			if ((toDelete.size() > 0) || (toInsert.size() > 0))
			{
				// do these each in their own transaction, to avoid possible deadlock
				// caused by transactions modifying more than one row at a time.

				// delete
				sql = dbAuthzGroupSql.getDeleteRealmRoleGroup3Sql();
				fields = new Object[2];
				fields[1] = userId;
				for (Iterator i = toDelete.iterator(); i.hasNext();)
				{
					Integer realmId = (Integer) i.next();
					fields[0] = realmId;
					m_sql.dbWrite(sql, fields);
				}

				// insert
				sql = dbAuthzGroupSql.getInsertRealmRoleGroup2Sql();
				fields = new Object[3];
				fields[1] = userId;
				for (Iterator i = toInsert.iterator(); i.hasNext();)
				{
					RealmAndRole rar = (RealmAndRole) i.next();
					fields[0] = rar.realmId;
					fields[2] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleGroup2_1Sql(), rar.role);

					m_sql.dbWrite(sql, fields);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void refreshAuthzGroup(BaseAuthzGroup realm)
		{
			if ((realm == null) || (m_provider == null)) return;

			String sql = "";
			StringBuilder sqlBuf = null;

			// Note: the realm is still lazy - we have the realm id but don't need to worry about changing grants

			// get the latest userEid -> role name map from the provider
			Map target = m_provider.getUserRolesForGroup(realm.getProviderGroupId());

			// read the realm's grants
			sql = dbAuthzGroupSql.getSelectRealmRoleGroup4Sql();
			Object[] fields = new Object[1];
			fields[0] = caseId(realm.getId());

			List grants = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String userId = result.getString(1);
						String roleName = result.getString(2);
						String active = result.getString(3);
						String provided = result.getString(4);
						return new UserAndRole(userId, roleName, "1".equals(active), "1".equals(provided));
					}
					catch (Throwable ignore)
					{
						return null;
					}
				}
			});

			// make a map, user id -> role granted, each for provider and non-provider (or inactive)
			Map<String, String> existing = new HashMap<String, String>();
			Map<String, String> nonProvider = new HashMap<String, String>();
			for (Iterator i = grants.iterator(); i.hasNext();)
			{
				UserAndRole uar = (UserAndRole) i.next();

				// active and provided are the currently stored provider grants
				if (uar.active && uar.provided)
				{
					if (existing.containsKey(uar.userId))
					{
						M_log.warn("refreshRealm: duplicate user id found in provider grants: " + uar.userId);
					}
					else
					{
						existing.put(uar.userId, uar.role);
					}
				}

				// inactive or not provided are the currently stored internal grants - not to be overwritten by provider info
				else
				{
					if (nonProvider.containsKey(uar.userId))
					{
						M_log.warn("refreshRealm: duplicate user id found in nonProvider grants: " + uar.userId);
					}
					else
					{
						nonProvider.put(uar.userId, uar.role);
					}
				}
			}

			// compute the records we need to delete: every existing not in target or not matching target's role
			List<String> toDelete = new Vector<String>();
			for (Iterator i = existing.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry entry = (Map.Entry) i.next();
				String userId = (String) entry.getKey();
				String role = (String) entry.getValue();

				try
				{
					String userEid = userDirectoryService().getUserEid(userId);
					String targetRole = (String) target.get(userEid);
					if ((targetRole == null) || (!targetRole.equals(role)))
					{
						toDelete.add(userId);
					}
				}
				catch (UserNotDefinedException e)
				{
					M_log.warn("refreshAuthzGroup: cannot find eid for user: " + userId);
				}
			}

			// compute the records we need to add: every target not in existing, or not matching's existing's role
			// we don't insert target grants that would override internal grants
			List<UserAndRole> toInsert = new Vector<UserAndRole>();
			for (Iterator i = target.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry entry = (Map.Entry) i.next();
				String userEid = (String) entry.getKey();
				try
				{
					String userId = userDirectoryService().getUserId(userEid);

					String role = (String) entry.getValue();

					String existingRole = (String) existing.get(userId);
					String nonProviderRole = (String) nonProvider.get(userId);
					if ((nonProviderRole == null) && ((existingRole == null) || (!existingRole.equals(role))))
					{
						toInsert.add(new UserAndRole(userId, role, true, true));
					}
				}
				catch (UserNotDefinedException e)
				{
					M_log.warn("refreshAuthzGroup: cannot find id for user eid: " + userEid);
				}
			}

			// if any, do it
			if ((toDelete.size() > 0) || (toInsert.size() > 0))
			{
				// do these each in their own transaction, to avoid possible deadlock
				// caused by transactions modifying more than one row at a time.

				// delete
				sql = dbAuthzGroupSql.getDeleteRealmRoleGroup4Sql();
				fields = new Object[2];
				fields[0] = caseId(realm.getId());
				for (Iterator i = toDelete.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(sql, fields);
				}

				// insert
				sql = dbAuthzGroupSql.getInsertRealmRoleGroup3Sql();
				fields = new Object[3];
				fields[0] = caseId(realm.getId());
				fields[0] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleGroup3_1Sql(), fields[0]);
				for (Iterator i = toInsert.iterator(); i.hasNext();)
				{
					UserAndRole uar = (UserAndRole) i.next();
					fields[1] = uar.userId;
					fields[2] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleGroup3_2Sql(), uar.role);

					m_sql.dbWrite(sql, fields);
				}
			}
		}

		public class RealmAndProvider
		{
			public Integer realmId;

			public String providerId;

			public RealmAndProvider(Integer id, String provider)
			{
				this.realmId = id;
				this.providerId = provider;
			}
		}

		public class RealmAndRole
		{
			public Integer realmId;

			public String role;

			boolean active;

			boolean provided;

			public RealmAndRole(Integer id, String role, boolean active, boolean provided)
			{
				this.realmId = id;
				this.role = role;
				this.active = active;
				this.provided = provided;
			}

			public boolean equals(Object obj)
			{
				if (!(obj instanceof RealmAndRole)) return false;
				if (this == obj) return true;
				RealmAndRole other = (RealmAndRole) obj;
				if (StringUtil.different(this.role, other.role)) return false;
				if (this.provided != other.provided) return false;
				if (this.active != other.active) return false;
				if (((this.realmId == null) && (other.realmId != null)) || ((this.realmId != null) && (other.realmId == null))
						|| ((this.realmId != null) && (other.realmId != null) && (!this.realmId.equals(other.realmId)))) return false;
				return true;
			}

			public int hashCode()
			{
				return (this.role + Boolean.valueOf(this.provided).toString() + Boolean.valueOf(this.active).toString() + this.realmId).hashCode();
			}
		}

		public class UserAndRole
		{
			public String userId;

			public String role;

			boolean active;

			boolean provided;

			public UserAndRole(String userId, String role, boolean active, boolean provided)
			{
				this.userId = userId;
				this.role = role;
				this.active = active;
				this.provided = provided;
			}

			public boolean equals(Object obj)
			{
				if (!(obj instanceof UserAndRole)) return false;
				if (this == obj) return true;
				UserAndRole other = (UserAndRole) obj;
				if (StringUtil.different(this.role, other.role)) return false;
				if (this.provided != other.provided) return false;
				if (this.active != other.active) return false;
				if (StringUtil.different(this.userId, other.userId)) return false;
				return true;
			}

			public int hashCode()
			{
				return (this.role + Boolean.valueOf(this.provided).toString() + Boolean.valueOf(this.active).toString() + this.userId).hashCode();
			}
		}

		public class RoleAndFunction
		{
			public String role;

			public String function;

			public RoleAndFunction(String role, String function)
			{
				this.role = role;
				this.function = function;
			}

			public boolean equals(Object obj)
			{
				if (!(obj instanceof RoleAndFunction)) return false;
				if (this == obj) return true;
				RoleAndFunction other = (RoleAndFunction) obj;
				if (StringUtil.different(this.role, other.role)) return false;
				if (StringUtil.different(this.function, other.function)) return false;
				return true;
			}

			public int hashCode()
			{
				return (this.role + this.function).hashCode();
			}
		}

		public class RoleAndDescription
		{
			public String role;

			public String description;

			public boolean providerOnly;

			public RoleAndDescription(String role, String description, boolean providerOnly)
			{
				this.role = role;
				this.description = description;
				this.providerOnly = providerOnly;
			}

			public boolean equals(Object obj)
			{
				if (!(obj instanceof RoleAndDescription)) return false;
				if (this == obj) return true;
				RoleAndDescription other = (RoleAndDescription) obj;
				if (StringUtil.different(this.role, other.role)) return false;
				if (StringUtil.different(this.description, other.description)) return false;
				if (this.providerOnly != other.providerOnly) return false;
				return true;
			}

			public int hashCode()
			{
				return (this.role + this.description + Boolean.valueOf(this.providerOnly).toString()).hashCode();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public String getUserRole(String userId, String azGroupId)
		{
			if ((userId == null) || (azGroupId == null)) return null;

			// checks to see if the user has the roleswap variable set in the session
			String rv = SecurityService.getUserEffectiveRole(azGroupId);

			// otherwise drop through to the usual check
			if (rv == null) {
				String sql = dbAuthzGroupSql.getSelectRealmRoleNameSql();
				Object[] fields = new Object[2];
				fields[0] = azGroupId;
				fields[1] = userId;
	
				// read the string
				List results = m_sql.dbRead(sql, fields, null);
	
				// prepare the return
				if ((results != null) && (!results.isEmpty()))
				{
					rv = (String) results.get(0);
					if (results.size() > 1)
					{
						M_log.warn("getUserRole: user: " + userId + " multiple roles");
					}
				}
			}
			
			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public Map getUsersRole(Collection userIds, String azGroupId)
		{
			if ((userIds == null) || (userIds.isEmpty()) || (azGroupId == null))
			{
				return new HashMap();
			}

			String inClause = orInClause(userIds.size(), "SRRG.USER_ID");
			String sql = dbAuthzGroupSql.getSelectRealmUserRoleSql(inClause);
			Object[] fields = new Object[1 + userIds.size()];
			fields[0] = azGroupId;
			int pos = 1;
			for (Iterator i = userIds.iterator(); i.hasNext();)
			{
				fields[pos++] = i.next();
			}

			// the return
			final Map rv = new HashMap();

			// read
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// read the results
						String userId = result.getString(1);
						String role = result.getString(2);

						if ((userId != null) && (role != null))
						{
							rv.put(userId, role);
						}
					}
					catch (Throwable t)
					{
					}

					return null;
				}
			});

			return rv;
		}

	} // DbStorage

	/** To avoide the dreaded ORA-01795 and the like, we need to limit to <100 the items in each in(?, ?, ...) clause, connecting them with ORs. */
	protected final static int MAX_IN_CLAUSE = 99;

	/**
	 * Form a SQL IN() clause, but break it up with ORs to keep the size of each IN below 100
	 * 
	 * @param size
	 *        The size
	 * @param field
	 *        The field name
	 * @return a SQL IN() with ORs clause this large.
	 */
	protected String orInClause(int size, String field)
	{
		// Note: to avoide the dreaded ORA-01795 and the like, we need to limit to <100 the items in each in(?, ?, ...) clause, connecting them with
		// ORs -ggolden
		int ors = size / MAX_IN_CLAUSE;
		int leftover = size - (ors * MAX_IN_CLAUSE);
		StringBuilder buf = new StringBuilder();

		// enclose them all in parens if we have > 1
		if (ors > 0)
		{
			buf.append(" (");
		}

		buf.append(" " + field + " IN ");

		// do all the full MAX_IN_CLAUSE '?' in/ors
		if (ors > 0)
		{
			for (int i = 0; i < ors; i++)
			{
				buf.append("(?");
				for (int j = 1; j < MAX_IN_CLAUSE; j++)
				{
					buf.append(",?");
				}
				buf.append(")");

				if (i < ors - 1)
				{
					buf.append(" OR " + field + " IN ");
				}
			}
		}

		// add one more for the extra
		if (leftover > 0)
		{
			if (ors > 0)
			{
				buf.append(" OR " + field + " IN ");
			}
			buf.append("(?");
			for (int i = 1; i < leftover; i++)
			{
				buf.append(",?");
			}
			buf.append(")");
		}

		// enclose them all in parens if we have > 1
		if (ors > 0)
		{
			buf.append(" )");
		}

		return buf.toString();
	}

	/**
	 * Get value for query & return that; needed for mssql which doesn't support select stmts in VALUES clauses
	 * 
	 * @param sqlQuery
	 * @param bindParameter
	 * @return value if mssql, bindparameter if not (basically a no-op for others)
	 */
	protected Object getValueForSubquery(String sqlQuery, Object bindParameter)
	{
		if (!"mssql".equals(sqlService().getVendor()))
		{
			return bindParameter;
		}
		else
		{
			List result = sqlService().dbRead(sqlQuery, new Object[] {bindParameter}, null);
			return (result.size() > 0 ? result.get(0) : null);
		}
	}
}
