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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroup.RealmLockMode;
import org.sakaiproject.authz.api.GroupFullException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.MemberWithRoleId;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SimpleRole;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.scheduling.api.SchedulingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseDbFlatStorage;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.StringUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * DbAuthzGroupService is an extension of the BaseAuthzGroupService with database storage.
 * </p>
 */
@Slf4j
public abstract class DbAuthzGroupService extends BaseAuthzGroupService implements Observer
{
	/** To avoide the dreaded ORA-01795 and the like, we need to limit to <1000 the items in each in(?, ?, ...) clause, connecting them with ORs. */
	protected final static int MAX_IN_CLAUSE = 999;
	/** All the event functions we know exist on the db. */
	protected Collection<String> m_functionCache = new HashSet<>();
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

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/
	/** All "field values" for realm insert. */
	protected String[] m_realmInsertValueNames = {"?", "?", "(select MAX(ROLE_KEY) from SAKAI_REALM_ROLE where ROLE_NAME = ?)", "?", "?", "?", "?"};
	/** map of database handlers. */
	protected Map<String, DbAuthzGroupSql> databaseBeans;
	/** The database handler we are using. */
	protected DbAuthzGroupSql dbAuthzGroupSql;
	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_useExternalLocks = true;
	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;
	/**
	 * Configuration: Whether or not to automatically promote non-provided users with same status
	 * and role to provided
	 */
	protected boolean m_promoteUsersToProvided = true;
	protected boolean m_promoteUsersToProvidedRole = false;
	private MemoryService m_memoryService;

	protected abstract SchedulingService schedulingService();

	// KNL-600 CACHING for the realm role groups
	private Cache m_realmRoleGRCache;
	
	private Cache authzUserGroupIdsCache;

    private Cache maintainRolesCache;

    private Cache realmLocksCache;

	/** KNL-1325 provide a more efficent refreshAuthzGroup */
    public static final String REFRESH_MAX_TIME_PROPKEY = "authzgroup.refresh.max.time";
    public static final String REFRESH_INTERVAL_PROPKEY = "authzgroup.refresh.interval";

    /**
     * Number of seconds before running refreshAuthzGroupTask again to clear queue,
     * defaults to 60 (1 minute)
     */
    private long refreshTaskInterval = 60;

	/**
	 * Number of seconds an authz group refresh is allowed to take
	 * if threshold is reached delay processing the queue
	 */
	private long refreshMaxTime = 15;

	/** Queue of authzgroups to refresh used by refreshAuthzGroupTask */
	private Map<String, AuthzGroup> refreshQueue;

	public void setDatabaseBeans(Map databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/

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

	public void setMemoryService(MemoryService memoryService) {
		this.m_memoryService = memoryService;
	}

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract SqlService sqlService();

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
	
	/*************************************************************************************************************************************************
	 * Init and Destroy
	 ************************************************************************************************************************************************/

	/**
	 * Configuration: Whether or not to automatically promote non-provided users with same status
	 * and role to provided
	 *
	 * @param promoteUsersToProvided
	 * 	'true' to promote non-provided users, 'false' to maintain their non-provided status
	 */
	public void setPromoteUsersToProvided(boolean promoteUsersToProvided)
	{
		m_promoteUsersToProvided = promoteUsersToProvided;
	}

	/**
	 * Configuration: Whether or not to automatically promote non-provided users with same status
	 * to their provided role
	 *
	 * @param promoteUsersToProvidedRole
	 * 	'true' to promote non-provided users to their provided role, 'false' to maintain their non-provided status or to prevent a role change
	 */
	public void setPromoteUsersToProvidedRole(boolean promoteUsersToProvidedRole)
	{
		m_promoteUsersToProvidedRole = promoteUsersToProvidedRole;
	}
	
	public void setRefreshTaskInterval(long refreshTaskInterval) {
		log.info(REFRESH_INTERVAL_PROPKEY + " changed from " + this.refreshTaskInterval + " to " + refreshTaskInterval);
		this.refreshTaskInterval = refreshTaskInterval;
	}

	public void setRefreshMaxTime(long refreshMaxTime) {
		log.info(REFRESH_MAX_TIME_PROPKEY + " changed from " + this.refreshMaxTime + " to " + refreshMaxTime);
		this.refreshMaxTime = refreshMaxTime;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		log.info("table: {} external locks: {}", m_realmTableName, m_useExternalLocks);

		try
		{
			// The observer will be notified whenever there are new events. Priority observers get notified first, before normal observers.
			eventTrackingService().addPriorityObserver(this);

			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_realm");
			}

			super.init();
			setDbAuthzGroupSql(sqlService().getVendor());

			// pre-cache role and function names
			cacheRoleNames();
			cacheFunctionNames();

			m_realmRoleGRCache = m_memoryService.getCache("org.sakaiproject.authz.impl.DbAuthzGroupService.realmRoleGroupCache");
			authzUserGroupIdsCache = m_memoryService.getCache("org.sakaiproject.authz.impl.DbAuthzGroupService.authzUserGroupIdsCache");
			maintainRolesCache = m_memoryService.getCache("org.sakaiproject.authz.impl.DbAuthzGroupService.maintainRolesCache");
			realmLocksCache = m_memoryService.getCache("org.sakaiproject.authz.impl.DbAuthzGroupService.realmLocksCache");

            //get the set of maintain roles and cache them on startup
            getMaintainRoles();

            refreshTaskInterval = initConfig(REFRESH_INTERVAL_PROPKEY, serverConfigurationService().getString(REFRESH_INTERVAL_PROPKEY), refreshTaskInterval);
            refreshMaxTime = initConfig(REFRESH_MAX_TIME_PROPKEY, serverConfigurationService().getString(REFRESH_MAX_TIME_PROPKEY), refreshMaxTime);

            refreshQueue = Collections.synchronizedMap(new LinkedHashMap<>());

            schedulingService().scheduleWithFixedDelay(
                new RefreshAuthzGroupTask(),
                120, // minimally wait 2 mins for sakai to start
                refreshTaskInterval, // delay before running again
                TimeUnit.SECONDS
            );
		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}
	}

	private long initConfig(String propkey, String scsValue, long currentValue) {
		if (!"".equals(scsValue)) {
			try {
				long parsedVal = Long.parseLong(scsValue);
				log.info("initConfig() " + propkey + " changed from " + currentValue + " to " + parsedVal);
				return parsedVal;
			} catch (NumberFormatException e) {
				log.error("initConfig() " + propkey + " value cannot be parsed");
			}
		}
		return currentValue;
	}

	/*************************************************************************************************************************************************
	 * BaseAuthzGroupService extensions
	 ************************************************************************************************************************************************/

	/**
	* Returns to uninitialized state.
	*/
	public void destroy()
	{
		// done with event watching
		eventTrackingService().deleteObserver(this);

		authzUserGroupIdsCache.close();
		maintainRolesCache.close();
		realmLocksCache.close();

		log.info(this +".destroy()");
	}

	/**
	 * Construct a Storage object.
	 *
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		DbStorage storage = new DbStorage(entityManager(), siteService);
		storage.setPromoteUsersToProvided(m_promoteUsersToProvided);
		storage.setPromoteUsersToProvidedRole(m_promoteUsersToProvidedRole);
		return storage;

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
		if (getRealmRoleKey(name) != null) return;

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
			//Get realm role Key
			statement = dbAuthzGroupSql.getSelectRealmRoleKeySql();
			results = sqlService().dbRead(statement, fields, new SqlReader() {
				public Object readSqlResultRecord(ResultSet result) {
					try {
						String name = result.getString(1);
						Integer key = result.getInt(2);
						RealmRole realmRole = new RealmRole(name, key);
						m_roleNameCache.add(realmRole);
					}
					catch (SQLException ignore) {
					}
					return null;
				}
			});
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
						Integer key = result.getInt(2);
						RealmRole realmRole = new RealmRole(name, key);
						m_roleNameCache.add(realmRole);
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

	/*************************************************************************************************************************************************
	 * Storage implementation
	 ************************************************************************************************************************************************/

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
		// Note: to avoide the dreaded ORA-01795 and the like, we need to limit to <1000 the items in each in(?, ?, ...) clause, connecting them with
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
	 * Note that MSSQL support was removed in KNL-880, so this is a no-op.
	 *
	 * @param sqlQuery
	 * @param bindParameter
	 * @return value if mssql, bindparameter if not (basically a no-op for others)
	 */
	protected Object getValueForSubquery(String sqlQuery, Object bindParameter)
	{
		return bindParameter;
	}

	private Integer getRealmRoleKey(String roleName) {
		Iterator<RealmRole> itr = m_roleNameCache.iterator();
		while (itr.hasNext()) {
			RealmRole realmRole = (RealmRole) itr.next();
			if (realmRole != null && realmRole.getName().equals(roleName)) {
				return realmRole.getKey();
			}
		}
		return null;
	}
	
	public void update(Observable arg0, Object arg) {
        if (arg == null || !(arg instanceof Event))
			return;
		Event event = (Event) arg;

		// check the event function against the functions we have notifications watching for
		String function = event.getEvent();
		if (SECURE_UPDATE_AUTHZ_GROUP.equals(function)
				|| SECURE_UPDATE_OWN_AUTHZ_GROUP.equals(function)
				|| SECURE_REMOVE_AUTHZ_GROUP.equals(function)
				|| SECURE_JOIN_AUTHZ_GROUP.equals(function)
				|| SECURE_UNJOIN_AUTHZ_GROUP.equals(function)
				|| SECURE_ADD_AUTHZ_GROUP.equals(function)) {
			// Get the resource ID
			String realmId = extractEntityId(event.getResource());

			if (realmId != null) {
				if (log.isDebugEnabled()) {
					log.debug("clear authzUserGroupIdsCache/realmRoleGRCache/realmLocksCache for {}", realmId);
				}

				for (String user : getAuthzUsersInGroups(new HashSet<String>(Arrays.asList(realmId)))) {
					authzUserGroupIdsCache.remove(user);
				}

				m_realmRoleGRCache.remove(realmId);
				realmLocksCache.remove(realmId);
			} else {
				// This should never happen as the events we generate should always have
				// a /realm/ prefix on the resource.
				log.warn("DBAuthzGroupService update(): failed to extract realm ID from "+ event.getResource());
			}
		}
	}
	
	/**
	 * based on value from RealmRoleGroupCache
	 * transform a Map<String, MemberWithRoleId> object into a Map<String, Member> object
	 * KNL-1037
	 */
	private Map<String, Member> getMemberMap(Map<String, MemberWithRoleId> mMap, Map<?,?> roleMap)
	{
	    Map<String, Member> rv = new HashMap<String, Member>();
	    for (Map.Entry<String, MemberWithRoleId> entry : mMap.entrySet())
	    {
	        String userId = entry.getKey();
	        MemberWithRoleId m = entry.getValue();
	        String roleId = m.getRoleId();
	        if (roleId != null && roleMap != null && roleMap.containsKey(roleId))
	        {
	            Role role = (Role) roleMap.get(roleId);
				rv.put(userId, new BaseMember(role, m.isActive(), m.isProvided(), userId, userDirectoryService()));
	        }
	    }
	    return rv;
	}
	
	/**
	 * transform a Map<String, Member> object into a Map<String, MemberWithRoleId> object
	 * to be used in RealmRoleGroupCache
	 * KNL-1037
	 */
	private Map<String, MemberWithRoleId> getMemberWithRoleIdMap(Map<String, Member> userGrants)
	{
	    Map<String, MemberWithRoleId> rv = new HashMap<String, MemberWithRoleId>();
	    for (Map.Entry<String, Member> entry : userGrants.entrySet())
	    {
	        String userId = entry.getKey();
	        Member member = entry.getValue();
	        rv.put(userId, new MemberWithRoleId(member));
	    }
	    return rv;
	}

	/**
	 * Step through queue and call refreshAuthzGroup on all groups queued up for
	 * a refresh
	 */
	protected class RefreshAuthzGroupTask implements Runnable {
		@Override
		public void run() {
			if (log.isDebugEnabled()) log.debug("RefreshAuthzGroupTask.run() refreshing " + refreshQueue.size() + " realms");
			if (refreshQueue.size() > 0) {
				long numberRefreshed = 0;
				long timeRefreshed = 0;
				long longestRefreshed = 0;
				String longestName = null;
				
				while (true) {
					AuthzGroup azGroup;
					String azGroupId;
					synchronized (refreshQueue) {
						if (refreshQueue.isEmpty()) {
							break;
						}
						azGroup = refreshQueue.values().iterator().next();
						azGroupId = azGroup.getId();
						refreshQueue.remove(azGroupId);
					}
					log.debug("RefreshAuthzGroupTask.run() start refresh of azgroup: {}", azGroupId);

					numberRefreshed++;
					long time = 0;
					long start = System.currentTimeMillis();
					try {
						((DbStorage) m_storage).refreshAuthzGroupInternal((BaseAuthzGroup) azGroup);
					} catch (Throwable e) {
						log.error("RefreshAuthzGroupTask.run() Problem refreshing azgroup: {}", azGroupId, e);
					} finally {
						time = (System.currentTimeMillis() - start);
						log.debug("RefreshAuthzGroupTask.run() refresh of azgroup: {} took {} seconds", azGroupId, time/1e3);
					}
					timeRefreshed += time;
					if (time > longestRefreshed) {
						longestRefreshed = time;
						longestName = azGroupId;
					}
					
					if (time > (refreshMaxTime * 1000L)) {
						log.warn("RefreshAuthzGroupTask.run() " + azGroupId + " took " + time/1e3 + 
								" seconds which is longer than the maximum allowed of " + refreshMaxTime + 
								" seconds, delay processing the rest of the queue");
						break;
					}
				}
				log.info("RefreshAuthzGroupTask.run() refreshed " + numberRefreshed + " realms in " + timeRefreshed/1e3 + 
						" seconds, longest realm was " + longestName + " at " + longestRefreshed/1e3 + " seconds");
			}
		}
	}

	/**
	 * Covers for the BaseXmlFileStorage, providing AuthzGroup and RealmEdit parameters
	 */
	protected class DbStorage extends BaseDbFlatStorage implements BaseAuthzGroupService.Storage, SqlReader
	{

		private static final String REALM_USER_GRANTS_CACHE = "REALM_USER_GRANTS_CACHE";
		private static final String REALM_ROLES_CACHE = "REALM_ROLES_CACHE";
		private boolean promoteUsersToProvided = true;
		private boolean promoteUsersToProvidedRole = false;
		private EntityManager entityManager;
		private SiteService siteService;

		/**
		 * Construct.
		 */
		public DbStorage(EntityManager entityManager, SiteService siteService)
		{
			super(m_realmTableName, m_realmIdFieldName, m_realmReadFieldNames, m_realmPropTableName, m_useExternalLocks, null, sqlService());
			m_reader = this;

			setDbidField(m_realmDbidField);
			setWriteFields(m_realmUpdateFieldNames, m_realmInsertFieldNames, m_realmInsertValueNames);

			setLocking(false);
			this.entityManager = entityManager;
			this.siteService = siteService;

			// setSortField(m_realmSortField, null);
		}

		/**
		 * Configure whether or not users with same status and role will be "promoted" to
		 * being provided.
		 *
		 * @param promoteUsersToProvided Whether or not to promote non-provided users
		 */
		public void setPromoteUsersToProvided(boolean promoteUsersToProvided) {
			this.promoteUsersToProvided = promoteUsersToProvided;
		}

		/**
		 * Configure whether or not users with same status will be "promoted" to their provided role.
		 *
		 * @param promoteUsersToProvidedRole Whether or not to promote non-provided users to their provided role
		 * with no consideration of whether the role is identical.
		 */
		public void setPromoteUsersToProvidedRole(boolean promoteUsersToProvidedRole) {
			this.promoteUsersToProvidedRole = promoteUsersToProvidedRole;
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

			Map <String, Map> realmRoleGRCache = (Map<String, Map>)m_realmRoleGRCache.get(realm.getId());

			if (log.isDebugEnabled()) {
				log.debug("realmRoleGRCache: found {} in cache? {}", realm.getId(), (realmRoleGRCache != null));
			}

			if (realmRoleGRCache != null) {
				// KNL-1037 read the cached role and membership information
				Map<String, Role> roles = new HashMap<String, Role>();
				
				// dehydrate to SimpleRoles, which can be stored in a distributed Terracotta cache
				Map<String, SimpleRole> roleProperties = realmRoleGRCache.get(REALM_ROLES_CACHE);
				for (java.util.Map.Entry<String, SimpleRole> mapEntry : roleProperties.entrySet()) {
					roles.put(mapEntry.getKey(), new BaseRole(mapEntry.getValue()));
				}
				Map<String, Member> userGrants = new HashMap<String, Member>();
				
				Map<String, MemberWithRoleId> userGrantsWithRoleIdMap = (Map<String, MemberWithRoleId>) realmRoleGRCache.get(REALM_USER_GRANTS_CACHE);
				userGrants.putAll(getMemberMap(userGrantsWithRoleIdMap, roles));
				
				realm.m_roles = roles;
				realm.m_userGrants = userGrants;
			} else {
			    // KNL-1183
			    refreshAuthzGroup(realm);

			    // read the roles and role functions
			    String sql = dbAuthzGroupSql.getSelectRealmRoleFunctionSql();
			    Object fields[] = new Object[1];
			    fields[0] = realm.getId();

			    m_sql.dbRead(conn, sql, fields, new SqlReader()
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
			    m_sql.dbRead(conn, sql, fields, new SqlReader()
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

								grant = new BaseMember(role, "1".equals(active), "1".equals(provided), userId, userDirectoryService());

			                    realm.m_userGrants.put(userId, grant);
			                }
			                else
			                {
			                    log.warn("completeGet: additional user - role grant: " + userId + " " + roleName);
			                }

			                return null;
			            }
			            catch (SQLException ignore)
			            {
			                return null;
			            }
			        }
			    });

				Map<String, Map> payLoad = new HashMap<String, Map>();
				// rehydrate from SimpleRole, which can be stored in a Terracotta cache
				Map<String, SimpleRole> roleProperties = new HashMap<String, SimpleRole>();
				for (java.util.Map.Entry<String, BaseRole> entry : ((Map<String, BaseRole>) realm.m_roles).entrySet()) {
					roleProperties.put(entry.getKey(), entry.getValue().exportToSimpleRole());
				}
				Map<String, MemberWithRoleId> membersWithRoleIds = getMemberWithRoleIdMap(realm.m_userGrants);
				payLoad.put(REALM_ROLES_CACHE, roleProperties);
				payLoad.put(REALM_USER_GRANTS_CACHE, membersWithRoleIds);
				m_realmRoleGRCache.put(realm.getId(), payLoad);
			}

			// RealmLock handling
			Set<RealmLock> cachedRealmLock = (Set<RealmLock>) realmLocksCache.get(realm.getId());

			if (log.isDebugEnabled()) {
				log.debug("cachedRealmLock: found {} in cache? {}", realm.getId(), (cachedRealmLock != null));
			}

			if (cachedRealmLock != null) {
				realm.m_realmLocks = cachedRealmLock;
			} else {
				String realmLocksSql = dbAuthzGroupSql.getSelectRealmLocksSql();
				m_sql.dbRead(conn, realmLocksSql, new String[] {realm.getId()}, (SqlReader) result -> {
					try {
						Integer key = result.getInt(1);
						String reference = result.getString(2);
						Integer lockType = result.getInt(3);

						RealmLock realmLock = new RealmLock(key, reference, RealmLockMode.values()[lockType]);
						realm.m_realmLocks.add(realmLock);
					} catch (SQLException se) {
						log.warn("Could not read locks for realm {}, Exception: {}", realm.getId(), se.getMessage());
					}
					return null;
				});
				realmLocksCache.put(realm.getId(), realm.m_realmLocks);
			}
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
				String where = "( UPPER(REALM_ID) like ? or UPPER(PROVIDER_ID) like ? )";
				Object[] fields = new Object[2];
				fields[0] = criteria.toUpperCase();
				fields[1] = criteria.toUpperCase();

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

			// first consult the cache
			UserAndGroups uag = (UserAndGroups) authzUserGroupIdsCache.get(userid);
			if (uag != null) {
				List<String> result = uag.getRealmQuery(new HashSet<String>(authzGroupIds));
				log.debug(uag.toString());
				if (result != null) {
					// hit
					return result;
				}
				// miss
			}

			// not in the cache
			String inClause = orInClause( authzGroupIds.size(), "SAKAI_REALM.REALM_ID" );
			String statement = dbAuthzGroupSql.getSelectRealmUserGroupSql( inClause );
			Object[] fields = new Object[authzGroupIds.size()+1];
			for ( int i=0; i<authzGroupIds.size(); i++ )
			{
				fields[i] = authzGroupIds.get(i);
			}
			fields[authzGroupIds.size()] = userid;

			List dbResult = sqlService().dbRead(statement, fields, null );

			// no cache for user so create
			if (uag == null) {
				uag =  new UserAndGroups(userid);
			}
			// add to the users cache
			uag.addRealmQuery(new HashSet<String>(authzGroupIds), dbResult);
			authzUserGroupIdsCache.put(userid, uag);

			return dbResult;
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
				String where = "( UPPER(REALM_ID) like ? or UPPER(PROVIDER_ID) like ? )";
				Object[] fields = new Object[2];
				fields[0] = criteria.toUpperCase();
				fields[1] = criteria.toUpperCase();

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
        public Collection<String> getAuthzUsersInGroups(Set<String> groupIds)
        {
            if (groupIds == null || groupIds.isEmpty()) {
                return new ArrayList<String>(); // empty list
            }

            // make a big where condition for groupIds with ORs
            String inClause = orInClause( groupIds.size(), "SR.REALM_ID" );
            String statement = dbAuthzGroupSql.getSelectRealmUsersInGroupsSql(inClause);
            Object[] fields = groupIds.toArray();
            @SuppressWarnings("unchecked")
            List<String> results = sqlService().dbRead(statement, fields, null);
            return results;
        }

		/**
		 * {@inheritDoc}
		 */
		public Set<String> getProviderIds(String authzGroupId)
		{
			String statement = dbAuthzGroupSql.getSelectRealmProviderId1Sql();
			List results = sqlService().dbRead(statement, new Object[] {authzGroupId}, null);
			if (results == null)
			{
				return new HashSet<>();
			}
			return new HashSet<>(results);
		}

		/**
		 * {@inheritDoc}
		 */
		public Map<String, List<String>> getProviderIDsForRealms(List<String> realmIDs)
		{
			Map<String, List<String>> realmProviderMap = new HashMap<>();
			if (realmIDs != null && realmIDs.size() > 0)
			{
				// Custom reader to get only realm_id and provider_id
				SqlReader reader = (result)-> {
					try
					{
						String realmID = result.getString(1);
						String providerIDs = result.getString(2);
						List<String> retVal = new ArrayList<>();
						retVal.add(realmID);
						retVal.add(providerIDs);
						return retVal;
					}
					catch (SQLException ex)
					{
						// Avoid nulls by returning an empty Colleciton<String>
						log.warn("getProviderIDsForRealms.readSqlResultRecord: " + ex);
						return Collections.<String>emptyList();
					}
				};

				// Execute the SQL statement
				String sql = dbAuthzGroupSql.getSelectRealmsProviderIDsSql(orInClause(realmIDs.size(), "r.realm_id"));
				Object[] fields = realmIDs.toArray();
				List<List<String>> results = (List<List<String>>) m_sql.dbRead(sql, fields, reader);

				// Build the realm-provider map
				for (List<String> list : results)
				{
					String realmID = list.get(0);
					String providerIDs = list.get(1);

					if (StringUtils.isNotBlank(realmID) && StringUtils.isNotBlank(providerIDs))
					{
						realmProviderMap.put(realmID, Arrays.asList(providerIDs.split("\\+")));
					}
				}
			}

			return realmProviderMap;
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

            if ("".equals(lock) || "*".equals(lock)) {
                // SPECIAL CASE - return all authzGroup IDs this user is active in (much faster)
                String statement = dbAuthzGroupSql.getSelectRealmUserGroupSql("SAKAI_REALM_RL_GR.ACTIVE = '1'");
                Object[] fields = new Object[1];
                fields[0] = userId;
                List dbResult = sqlService().dbRead(statement, fields, null );
                return new HashSet(dbResult);
            }

			// Just like unlock, except we use all realms and get their ids
			// Note: consider over all realms just those realms where there's a grant of a role that satisfies the lock
			// Ignore realms where anon or auth satisfy the lock.

			boolean auth = (userId != null) && (!userDirectoryService().getAnonymousUser().getId().equals(userId));
			String sql = dbAuthzGroupSql.getSelectRealmIdSql(azGroups);
			int size = 2;
			if (azGroups != null)
			{
				size += azGroups.size();
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
						catch (Exception e)
						{
							log.warn("addNewUserTx: " + e.toString());
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
					log.error("addNewUserTx: can't find realm " + edit.getId());
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
						catch (Exception e)
						{
							log.warn("addNewUserTx: " + e.toString());
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

			// update SAKAI_REALM_LOCKS
			save_REALM_LOCKS(edit);

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
					catch (Exception e)
					{
						log.warn("save_REALM_RL_FN: " + e.toString());
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

            // KNL-1230 need to be able to tell when changes occur in the AZG
            HashSet<RoleAndFunction> lastChanged = new HashSet<RoleAndFunction>();
            if (!toAdd.isEmpty()) {
                lastChanged.addAll(toAdd);
            }
            if (!toDelete.isEmpty()) {
                lastChanged.addAll(toDelete);
            }
            ((BaseAuthzGroup) azg).m_lastChangedRlFn = lastChanged;
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
					catch (Exception e)
					{
						log.warn("save_REALM_RL_GR: " + e.toString());
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
					catch (Exception e)
					{
						log.warn("save_REALM_PROVIDER: " + e.toString());
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
					catch (Exception e)
					{
						log.warn("save_REALM_ROLE_DESC: " + e.toString());
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

		protected void save_REALM_LOCKS(AuthzGroup azg)
		{
			// add what we have in the azg, unless we see it in the db
			final Set<RealmLock> toAdd = new HashSet<>();
			((BaseAuthzGroup)azg).m_realmLocks.forEach(l -> toAdd.add(new RealmLock(l)));

			// delete anything we see in the db we don't have in the azg
			final Set<RealmLock> toDelete = new HashSet<>();

			// read what we have there now
			final String selectSql = dbAuthzGroupSql.getSelectRealmLocksSql();
			final String azgId = caseId(azg.getId());
			Object[] selectFields = new Object[1];
			selectFields[0] = azgId;
			m_sql.dbRead(selectSql, selectFields, result -> {
				try {
					Integer key = result.getInt(1);
					String reference = result.getString(2);
					Integer lockType = result.getInt(3);

					RealmLock realmLock = new RealmLock(key, reference, RealmLockMode.values()[lockType]);
					// if it exists in the database
					if (toAdd.contains(realmLock)) {
						// remove it from toAdd
						toAdd.remove(realmLock);
					} else {
						// add it to toDelete
						toDelete.add(realmLock);
					}
				} catch (SQLException se) {
					log.warn("Could not read locks for realm {}, Exception: {}", azgId, se.getMessage());
				}
				return null;
			});


			// delete what we need to
			final String deleteSql = dbAuthzGroupSql.getDeleteRealmLocksForRealmWithReferenceSql();
			toDelete.forEach(l -> {
				Object[] deleteFields = new Object[2];
				deleteFields[0] = azgId;
				deleteFields[1] = l.getReference();
				m_sql.dbWrite(deleteSql, deleteFields);
			});

			// add what we need to
			final String insertSql = dbAuthzGroupSql.getInsertRealmLocksSql();
			toAdd.forEach(l -> {
				Object[] insertFields = new Object[3];
				insertFields[0] = azgId;
				insertFields[1] = l.getReference();
				insertFields[2] = l.getLockMode().ordinal();
				m_sql.dbWrite(insertSql, insertFields);
			});
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

			statement = dbAuthzGroupSql.getDeleteRealmLocksForRealmSql();
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

				Instant now = Instant.now();

				rv[1] = "";
				rv[2] = "";
				rv[3] = current;
				rv[4] = current;
				rv[5] = now;
				rv[6] = now;
			}

			else
			{
				rv[1] = StringUtils.trimToEmpty(edit.m_providerRealmId);
				rv[2] = StringUtils.trimToEmpty(edit.m_maintainRole);
				rv[3] = StringUtils.trimToEmpty(edit.m_createdUserId);
				rv[4] = StringUtils.trimToEmpty(edit.m_lastModifiedUserId);
				rv[5] = edit.getCreatedDate();
				rv[6] = edit.getModifiedDate();
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
				java.sql.Timestamp ts = result.getTimestamp(6);
				Instant createdOn = null;
				if (ts != null)
				{
					createdOn = ts.toInstant();
				}
				ts = result.getTimestamp(7);
				Instant modifiedOn = null;
				if (ts != null)
				{
					modifiedOn = ts.toInstant();
				}

				// the special local integer 'db' id field, read after the field list
				Integer dbid = Integer.valueOf(result.getInt(8));

				// create the Resource from these fields
				return new BaseAuthzGroup(DbAuthzGroupService.this,dbid, id, providerId, maintainRole, createdBy, createdOn, modifiedBy, modifiedOn);
			}
			catch (SQLException e)
			{
				log.warn("readSqlResultRecord: " + e);
				return null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isAllowed(String userId, String lock, String realmId)
		{
			if ((lock == null) || (realmId == null)) return false;

			return isAllowed(userId,lock,Arrays.asList(new String[]{realmId}));
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isAllowed(String userId, String lock, Collection<String> realms)
		{
			if (lock == null) return false;


			if (realms == null || realms.size() < 1)
			{
				log.warn("isAllowed(): called with no realms: lock: " + lock + " user: " + userId);
				if (log.isDebugEnabled())
					log.debug("isAllowed():", new Exception());
				return false;
			}
			
			Set<String> roles = getEmptyRoles(userId);
			
			if (log.isDebugEnabled())
				log.debug("isAllowed: userId=" + userId + " lock=" + lock + " realms=" + realms
						+ " roles="+ StringUtils.join(roles, ','));

			String inClause = orInClause(realms.size(), "SAKAI_REALM.REALM_ID");
			Set<Integer> roleIds = getRealmRoleKeys(roles);

			// any of the grant or role realms
			String statement = dbAuthzGroupSql.getCountRealmRoleFunctionSql(roleIds, inClause);
			Object[] fields = new Object[2 + (2 * realms.size()) + roleIds.size()];
			int pos = 0;

			String userSiteRef = null;
			String siteRef = null;

			// oracle query has different order of parameters
			String dbAuthzGroupSqlClassName=dbAuthzGroupSql.getClass().getName();

			if(dbAuthzGroupSqlClassName.equals("org.sakaiproject.authz.impl.DbAuthzGroupSqlOracle")) {
					fields[pos++] = userId;
			}

			// populate values for fields
			for (String realmId : realms)
			{
				// These checks for roleswap assume there is at most one of each type of site in the realms collection,
				// i.e. one ordinary site and one user site

				if (realmId.startsWith(SiteService.REFERENCE_ROOT + Entity.SEPARATOR))		// Starts with /site/
				{
					if (userId != null && userId.equals(siteService.getSiteUserId(realmId))) {
						userSiteRef = realmId;
					} else {
						siteRef = realmId; // set this variable for potential use later
					}
				}
				fields[pos++] = realmId;
			}
			fields[pos++] = lock;
			if(!dbAuthzGroupSqlClassName.equals("org.sakaiproject.authz.impl.DbAuthzGroupSqlOracle")) {
				fields[pos++] = userId;
			}
			for (String realmId : realms)
			{
				fields[pos++] = realmId;
			}

			for (Integer roleId : roleIds)
			{
				fields[pos++] = roleId;
			}

			
			/* Delegated access essentially behaves like roleswap except instead of just specifying which role, you can also specify
			 * the realm as well.  The access map is populated by an Event Listener that listens for dac.checkaccess and is stored in the session
			 * attribute: delegatedaccess.accessmap.  This is a map of: SiteRef -> String[]{realmId, roleId}.  Delegated access
			 * will defer to roleswap if it's set.
			 */
			String[] delegatedAccessGroupAndRole = getDelegatedAccessRealmRole(siteRef);
			boolean delegatedAccess = delegatedAccessGroupAndRole != null && delegatedAccessGroupAndRole.length == 2;

			// Would be better to get this initially to make the code more efficient, but the realms collection
			// does not have a common order for the site's id which is needed to determine if the session variable exists
			String roleswap = securityService().getUserEffectiveRole();
			Reference ref = entityManager().newReference(siteRef);

			List results = null;

			if (delegatedAccess && userId != null && userId.equals(sessionManager().getCurrentSessionUserId())) {

				// First check in the user's own Home site realm if it's in the list
				// We don't want to change the user's role in their own site, so call the regular function.
				// This catches permission checks for entity references such as user dropboxes.

				if (userSiteRef != null && isAllowed(userId, lock, userSiteRef))
					return true;

				// Then check the site where there's a roleswap effective
				if (log.isDebugEnabled()) log.debug("userId="+userId+", siteRef="+siteRef+", roleswap="+roleswap+", delegatedAccess="+delegatedAccess);
				// In roleswap check all realms, not for delegated access
				int fieldCount = 3 + (roleswap!=null?realms.size():1); 
				Object[] fields2 = new Object[fieldCount-(delegatedAccess?1:0)];
				if (roleswap != null) {
				    fields2[0] = roleswap;
				} else if (delegatedAccess
				        && delegatedAccessGroupAndRole != null ) {
				    // set the role for delegated access
				    fields2[0] = delegatedAccessGroupAndRole[1];
				}
				fields2[1] = lock;
				pos = 2;
				if (roleswap == null
				        && delegatedAccess
				        && delegatedAccessGroupAndRole != null
				        ) {
				    // set the realm for delegated access
				    fields2[2] = delegatedAccessGroupAndRole[0];
				    pos++;
				} else {
					// Check all realms in roleswap
					for (String realmId : realms) {
						fields2[pos++] = realmId;
					}
				}
				if (!delegatedAccess) fields2[pos] = userId;
				if (log.isDebugEnabled()) log.debug("roleswap/dac fields: "+Arrays.toString(fields2));
				// In delegated access use a single in clause
				if (roleswap==null) {
					inClause = orInClause(1, "SAKAI_REALM.REALM_ID");
				}
				statement = dbAuthzGroupSql.getCountRoleFunctionSql(inClause,delegatedAccess);

				results = m_sql.dbRead(statement, fields2, new SqlReader()
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
				int count = -1;
				if (!results.isEmpty())
				{
					count = ((Integer) results.get(0)).intValue();
					rv = count > 0;
				}
				return rv;
            }

			// Regular lookup (not roleswap)

			results = m_sql.dbRead(statement, fields, new SqlReader()
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
			int count = -1;
			if (!results.isEmpty())
			{
				count = ((Integer) results.get(0)).intValue();
				rv = count > 0;
			}

			return rv;
		}

		/**
		 * Delegated access essentially behaves like roleswap except instead of just specifying which role, you can also specify
		 * the realm as well.  The access map is populated by an Event Listener that listens for dac.checkaccess and is stored in the session
		 * attribute: delegatedaccess.accessmap.  This is a map of: SiteRef -> String[]{realmId, roleId}.
		 * Delegated access will defer to roleswap if it is set.
		 *
		 * @param siteRef the site realm id
		 * @return String[]{realmId, roleId} or null if delegated access is disabled
		 */
		private String[] getDelegatedAccessRealmRole(String siteRef){
            if (log.isDebugEnabled()) log.debug("getDelegatedAccessRealmRole(siteRef="+siteRef+")");
		    String[] delegatedAccessGroupAndRole = null;
		    // first we get the map out of the session (if it exists and is safe)
		    Map<?,?> delegatedAccessMap = null;
		    if (sessionManager().getCurrentSession().getAttribute("delegatedaccess.accessmapflag") != null) {
		        // only check for the map if the accessmapflag is set
		        Object delegatedAccessMapObj = sessionManager().getCurrentSession().getAttribute("delegatedaccess.accessmap");
		        if (delegatedAccessMapObj != null && delegatedAccessMapObj instanceof Map) {
		            // only read the map value out if it is set and is an actual map
		            delegatedAccessMap = (Map<?,?>) delegatedAccessMapObj;
		        }
		        //if the siteRef doesn't exist in the map, then that means that we haven't checked delegatedaccess for this user and site.
		        //if the user doesn't have access, the map will have a null value for that siteRef.
		        if (siteRef != null
		                && (delegatedAccessMap == null || !delegatedAccessMap.containsKey(siteRef))){
		            /* the delegatedaccess.accessmapflag is set during login and is only set for user's who have some kind of delegated access
		             * if the user has access somewhere but either the map is null or there isn't any record for this site, then that means
		             * this site hasn't been checked yet.  By posting an event, a DelegatedAccess observer will check this site's access for this user
		             * and store it in the user's session
		             */
		            eventTrackingService().post(eventTrackingService().newEvent("dac.checkaccess", siteRef, false, NotificationService.NOTI_REQUIRED));
		            //grab the session after the checkaccess event since the checkaccess event could have modified it
		            delegatedAccessMapObj = sessionManager().getCurrentSession().getAttribute("delegatedaccess.accessmap");
		            if (delegatedAccessMapObj != null && delegatedAccessMapObj instanceof Map) {
		                // only read the map value out if it is set and is an actual map
		                delegatedAccessMap = (Map<?,?>) delegatedAccessMapObj;
		            }
		        }

		        if (siteRef != null
		                && delegatedAccessMap != null
		                && delegatedAccessMap.containsKey(siteRef)
		                && delegatedAccessMap.get(siteRef) instanceof String[]) {
		            if (log.isDebugEnabled()) log.debug("siteRef="+siteRef+", delegatedAccessMap="+delegatedAccessMap);

		            delegatedAccessGroupAndRole = (String[]) delegatedAccessMap.get(siteRef);

		            if (log.isInfoEnabled()) {
		                String dacgarStr = "";
		                if (delegatedAccessGroupAndRole != null && delegatedAccessGroupAndRole.length > 1) {
		                    dacgarStr = ", GroupAndRole["+delegatedAccessGroupAndRole[0]+", "+delegatedAccessGroupAndRole[1]+"]";
		                }
		                log.info("delegatedAccessCheck: userId="+sessionManager().getCurrentSessionUserId()+", siteRef="+siteRef+", delegatedAccess="+dacgarStr);
		            }
		        }
		    }
            if (log.isDebugEnabled()) log.debug("getDelegatedAccessRealmRole(siteRef="+siteRef+"): "+Arrays.toString(delegatedAccessGroupAndRole));
		    return delegatedAccessGroupAndRole;
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<String> getUsersIsAllowed(String lock, Collection<String> realms)
		{
			if ((lock == null) || (realms == null) || (realms.isEmpty())) return new HashSet<String>();

			String sql = dbAuthzGroupSql.getSelectRealmRoleUserIdSql(orInClause(realms.size(), "SR.REALM_ID"));
			Object[] fields = new Object[1 + realms.size()];
			int pos = 0;
			fields[pos++] = lock;
			for (String roleRealm : realms)
			{
				fields[pos++] = roleRealm;
			}

			// read the strings
			List<String> results = m_sql.dbRead(sql, fields, null);

			// prepare the return
			Set<String> rv = new HashSet<String>();
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
		public void refreshUser(String userId, Map<String, String> providerGrants)
		{
			if (userId == null) return;

			String sql = dbAuthzGroupSql.getSelectRealmRoleGroup3Sql();

			// read this user's grants from all realms
			Object[] fields = new Object[1];
			fields[0] = userId;

			List<RealmAndRole> grants = m_sql.dbRead(sql, fields, new SqlReader()
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
					catch (Exception ignore)
					{
						return null;
					}
				}
			});

			// make a map, realm id -> role granted, each for provider and non-provider (or inactive)
			Map<Integer, String> existing = new HashMap<Integer, String>();
			Map<Integer, String> providedInactive = new HashMap<Integer, String>();
			Map<Integer, String> nonProvider = new HashMap<Integer, String>();
			for (RealmAndRole rar : grants)
			{
				// active and provided are the currently stored provider grants
				if (rar.provided)
				{
					if (existing.containsKey(rar.realmId))
					{
						log.warn("refreshUser: duplicate realm id found in provider grants: " + rar.realmId);
					}
					else
					{
						existing.put(rar.realmId, rar.role);

						// Record inactive status
						if (!rar.active) {
							providedInactive.put(rar.realmId, rar.role);
						}
					}
				}

				// inactive or not provided are the currently stored internal grants - not to be overwritten by provider info
				else
				{
					if (nonProvider.containsKey(rar.realmId))
					{
						log.warn("refreshUser: duplicate realm id found in nonProvider grants: " + rar.realmId);
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
				for (String providerId : providerGrants.keySet())
				{
					fieldsx[pos++] = providerId;
				}
				List<RealmAndProvider> realms = m_sql.dbRead(sql, fieldsx, new SqlReader()
				{
					public Object readSqlResultRecord(ResultSet result)
					{
						try
						{
							int id = result.getInt(1);
							String provider = result.getString(2);
							return new RealmAndProvider(Integer.valueOf(id), provider);
						}
						catch (Exception ignore)
						{
							return null;
						}
					}
				});

				if ((realms != null) && (realms.size() > 0))
				{
					for (RealmAndProvider rp : realms)
					{
						String role = providerGrants.get(rp.providerId);
						if (role != null)
						{
							if (target.containsKey(rp.realmId))
							{
								log.warn("refreshUser: duplicate realm id computed for new grants: " + rp.realmId);
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
			for (Map.Entry<Integer, String> entry : existing.entrySet())
			{
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
			for (Map.Entry<Integer, String> entry : target.entrySet())
			{
				Integer realmId = entry.getKey();
				String role = entry.getValue();

				String existingRole = (String) existing.get(realmId);
				String nonProviderRole = (String) nonProvider.get(realmId);
				if ((nonProviderRole == null) && ((existingRole == null) || (!existingRole.equals(role))))
				{
					boolean active = true;
					if (providedInactive.get(realmId) != null) {
						active = false;
					}

					toInsert.add(new RealmAndRole(realmId, role, active, true));
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
				for (Integer realmId : toDelete)
				{
					fields[0] = realmId;
					m_sql.dbWrite(sql, fields);
				}

				// insert
				sql = dbAuthzGroupSql.getInsertRealmRoleGroup2Sql();
				fields = new Object[3];
				fields[1] = userId;
				for (RealmAndRole rar : toInsert)
				{
					fields[0] = rar.realmId;
					fields[2] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleGroup2_1Sql(), rar.role);

					m_sql.dbWrite(sql, fields);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void refreshAuthzGroup(BaseAuthzGroup azGroup) {
			if (azGroup == null) return;

			if (azGroup.m_isNew) {
				// refresh new authz groups immediately
				log.debug("Refresh new authz group: {}", azGroup.getId());
				refreshAuthzGroupInternal(azGroup);

				// refresh parent
				Reference reference = entityManager.newReference(azGroup.getId());
				if (SiteService.APPLICATION_ID.equals(reference.getType()) && SiteService.GROUP_SUBTYPE.equals(reference.getSubType())) {
					try {
						refreshAuthzGroupInternal((BaseAuthzGroup) getAuthzGroup(siteService.siteReference(reference.getContainer())));
					} catch (Exception e) {
						log.warn("Cannot refresh parent authz group for authz group: {}", azGroup.getId(), e);
					}
				}
			} else {
				// Add the AuthzGroup to the queue, keyed on id to eliminate duplicate refreshes
				log.debug("Queue authz group for refresh " + azGroup.getId());
				refreshQueue.put(azGroup.getId(), azGroup);
			}
		}

		/**
		 * Update the realm with info from the provider
		 * 
		 * @param realm the realm to be refreshed
		 */
		protected void refreshAuthzGroupInternal(BaseAuthzGroup realm)
		{
			if ((realm == null) || (m_provider == null)) return;
			log.debug("Refreshing authz group: {}", realm);

			boolean synchWithContainingRealm = serverConfigurationService().getBoolean("authz.synchWithContainingRealm", true);

			// check to see whether this is of group realm or not
			// if of Group Realm, get the containing Site Realm
			String containingRealmId = null;
			AuthzGroup containingRealm = null;
			Reference ref = entityManager.newReference(realm.getReference());
			if (SiteService.APPLICATION_ID.equals(ref.getType())
				&& SiteService.GROUP_SUBTYPE.equals(ref.getSubType()))
			{
				containingRealmId = ref.getContainer();
			}
			if (containingRealmId != null)
			{
				String containingRealmRef = siteService.siteReference(containingRealmId);
				try
				{
					containingRealm = getAuthzGroup(containingRealmRef);
				}
				catch (GroupNotDefinedException e)
				{
					log.warn("refreshAuthzGroupInternal() cannot find containing realm for id: " + containingRealmRef);
				}
			}

			String sql = "";

			// Note: the realm is still lazy - we have the realm id but don't need to worry about changing grants

			// get the latest userEid -> role name map from the provider
			Map<String,String> target = m_provider.getUserRolesForGroup(realm.getProviderGroupId());

			// read the realm's grants
			List<UserAndRole> grants = getGrants(realm);

			// make a map, user id -> role granted, each for provider and non-provider (or inactive)
			Map<String, String> existing = new HashMap<String, String>();
			Map<String, String> providedInactive = new HashMap<String, String>();
			Map<String, String> nonProvider = new HashMap<String, String>();
			for (UserAndRole uar : grants)
			{
				// active and provided are the currently stored provider grants
				if (uar.provided)
				{
					if (existing.containsKey(uar.userId))
					{
						log.warn("refreshAuthzGroupInternal() duplicate user id found in provider grants: " + uar.userId);
					}
					else
					{
						existing.put(uar.userId, uar.role);

						// Record inactive status
						if (!uar.active) {
							providedInactive.put(uar.userId, uar.role);
						}
					}
				}

				// inactive or not provided are the currently stored internal grants - not to be overwritten by provider info
				else
				{
					if (nonProvider.containsKey(uar.userId))
					{
						log.warn("refreshAuthzGroupInternal() duplicate user id found in nonProvider grants: " + uar.userId);
					}
					else
					{
						nonProvider.put(uar.userId, uar.role);
					}
				}
			}

			// compute the records we need to delete: every existing not in target or not matching target's role
			List<String> toDelete = new Vector<String>();
			for (Map.Entry<String,String> entry : existing.entrySet())
			{
				String userId = entry.getKey();
				String role = entry.getValue();

				try
				{
					String userEid = userDirectoryService().getUserEid(userId);
					String targetRole = (String) target.get(userEid);
					
					Member cMember = null;
					if (containingRealm != null)
					{
						cMember = containingRealm.getMember(userId);
					}
					
					// KNL-1273 - special case - sync role and active status with containing realm grants for this provided user
					if (synchWithContainingRealm && cMember != null && targetRole != null)
					{
						// the sync code in the next loop performs the delete if necessary,
						// so we do nothing here except prevent the delete code in this loop
						// from running
					}
					else
					{
						if ((targetRole == null) || (!targetRole.equals(role)))
						{
							toDelete.add(userId);
						}
					}
				}
				catch (UserNotDefinedException e)
				{
					log.warn("refreshAuthzGroupInternal() cannot find eid for user: " + userId);
				}
			}

			// compute the records we need to add: every target not in existing, or not matching's existing's role
			// we don't insert target grants that would override internal grants
			List<UserAndRole> toInsert = new Vector<UserAndRole>();
			for (Map.Entry<String,String> entry : target.entrySet())
			{
				String userEid = entry.getKey();
				try
				{
					String userId = userDirectoryService().getUserId(userEid);

					String role = entry.getValue();
					boolean active = true;
					String existingRole = (String) existing.get(userId);
					String nonProviderRole = (String) nonProvider.get(userId);

					Member cMember = null;
					if (containingRealm != null)
					{
						cMember = containingRealm.getMember(userId);
					}
					
					// KNL-1273 - special case - sync role and active status with containing realm grants for this provided user
					if (synchWithContainingRealm && cMember != null && nonProviderRole == null)
					{
						// determines if realm update is required because role or active status differs from containing realm grants
						boolean insertRequired = true;
						
						String cMemberRoleId = cMember.getRole() != null ? cMember.getRole().getId() : null;
						boolean cMemberActive = cMember.isActive();
						
						// the user has a provided realm entry already, so delete it before inserting if needed
						if (existingRole != null)
						{
							boolean roleEqual = existingRole.equals(cMemberRoleId);
							
							// user is currently active if not in the providedInactive map
							boolean currentlyActive = providedInactive.get(userId) == null;
							boolean activeEqual = currentlyActive == cMemberActive;
							
							insertRequired = !roleEqual || !activeEqual;
							
							if (insertRequired)
							{
								toDelete.add(userId);
							}
						}
						
						if (insertRequired)
						{
							// Add or update user's role and active status to match containg realm grants
							toInsert.add(new UserAndRole(userId, cMemberRoleId, cMemberActive, true));
							
							if ((existingRole != null && !existingRole.equals(cMemberRoleId)) // overriding existing authz group role
									||!role.equals(cMemberRoleId))	// overriding provided role
							{
								log.info("refreshAuthzGroupInternal() realm id=" + realm.getId() + ", overrides group role of user eid=" + userEid + ": provided role=" + role + ", with site-level role=" + cMemberRoleId + " and site-level active status=" + cMemberActive);
							}
						}
					}
					else
					{
						if ((nonProviderRole == null) && ((existingRole == null) || (!existingRole.equals(role))))
						{
							// Check whether this user was inactive in the site previously, if so preserve status
							if (providedInactive.get(userId) != null)
							{
								active = false;
							}

							// this is either at site level or at the group level but no need to synchronize
							toInsert.add(new UserAndRole(userId, role, active, true));
						}
					}
				}
				catch (UserNotDefinedException e)
				{
					log.warn("refreshAuthzGroupInternal() cannot find id for user eid: " + userEid);
				}
			}

			if (promoteUsersToProvided)
			{
				// compute the records we want to promote from non-provided to provider:
				// every non-provided user with an equivalent provided entry with the same role
				for (Map.Entry<String,String> entry : nonProvider.entrySet())
				{
					String userId = entry.getKey();
					String role = entry.getValue();
					try
					{
						String userEid = userDirectoryService().getUserEid(userId);
						String targetRole = (String) target.get(userEid);

						if (role.equals(targetRole) || (StringUtils.isNotBlank(targetRole) && promoteUsersToProvidedRole))
						{
							log.debug("promoting user={} from role={} to targetRole={}", userEid, role, targetRole);

							// remove from non-provided and add as provided
							toDelete.add(userId);

							// Check whether this user was inactive in the site previously, if so preserve status
							boolean active = true;
							if (providedInactive.get(userId) != null) {
								active = false;
							}

							toInsert.add(new UserAndRole(userId, targetRole, active, true));
						}
					}
					catch (UserNotDefinedException e)
					{
						log.warn("refreshAuthzGroupInternal() cannot find eid for user: " + userId);
					}

				}
			}

			// if any, do it
			if ((toDelete.size() > 0) || (toInsert.size() > 0))
			{
				// do these each in their own transaction, to avoid possible deadlock
				// caused by transactions modifying more than one row at a time.

				// delete
				sql = dbAuthzGroupSql.getDeleteRealmRoleGroup4Sql();
				Object[] fields = new Object[2];
				fields[0] = caseId(realm.getId());
				for (String userId : toDelete)
				{
					fields[1] = userId;
					m_sql.dbWrite(sql, fields);
				}

				// insert
				sql = dbAuthzGroupSql.getInsertRealmRoleGroup3Sql();
				fields = new Object[5];
				fields[0] = caseId(realm.getId());
				fields[0] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleGroup3_1Sql(), fields[0]);
				for (UserAndRole uar : toInsert)
				{
					fields[1] = uar.userId;
					fields[2] = getValueForSubquery(dbAuthzGroupSql.getInsertRealmRoleGroup3_2Sql(), uar.role);
					fields[3] = uar.active ? "1" : "0"; // KNL-1099
					fields[4] = uar.provided ? "1" : "0"; // KNL-1099

					m_sql.dbWrite(sql, fields);
				}
				eventTrackingService().post(eventTrackingService().newEvent(SECURE_UPDATE_AUTHZ_GROUP, realm.getReference(), true));
			}
			if (log.isDebugEnabled()) {
				log.debug("refreshAuthzGroupInternal() deleted: "+ toDelete.size()+ " inserted: "+ toInsert.size()+ " provided: "+ existing.size()+ " nonProvider: "+ nonProvider.size());
			}
		}

		private List<UserAndRole> getGrants(AuthzGroup realm) {
			// read the realm's grants
			String sql = dbAuthzGroupSql.getSelectRealmRoleGroup2Sql();
			Object[] fields = new Object[1];
			fields[0] = caseId(realm.getId());

			List<UserAndRole> grants = m_sql.dbRead(sql, fields, new SqlReader()
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
					catch (Exception ignore)
					{
						return null;
					}
				}
			});
			return grants;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getUserRole(String userId, String azGroupId)
		{
			if ((userId == null) || (azGroupId == null)) return null;

			// checks to see if the user is the current user and has the roleswap variable set in the session
			String rv = null;

			if (userId.equals(sessionManager().getCurrentSessionUserId())) {
				rv = securityService().getUserEffectiveRole();
			}

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
						log.warn("getUserRole: user: " + userId + " multiple roles");
					}
				}
			}

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public Map<String, String> getUserRoles(String userId, Collection<String> azGroupIds)
		{
			final HashMap<String, String> rv = new HashMap<String, String>();
			if (userId == null || "".equals(userId))
				return rv;

			String inClause;
			int azgCount = azGroupIds == null ? 0 : azGroupIds.size();
			if (azgCount == 0) {
				inClause = " 1=1 ";
			}
			else {
				inClause = orInClause(azgCount, "REALM_ID");
			}

			String sql = dbAuthzGroupSql.getSelectRealmRolesSql(inClause);
			Object[] fields = new Object[1 + azgCount];
			fields[0] = userId;
			if (azgCount > 0) {
				int pos = 1;
				for (String s : azGroupIds) {
					fields[pos++] = s;
				}
			}

			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String realmId = result.getString(1);
						String roleName = result.getString(2);

						// ignore if we get an unexpected null -- it's useless to us
						if ((realmId != null) && (roleName != null))
						{
							rv.put(realmId, roleName);
						}
					}
					catch (Exception t)
					{
						log.warn("Serious database error occurred reading result set", t);
					}

					return null;
				}
			});

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
					catch (Exception t)
					{
					}

					return null;
				}
			});

			return rv;
		}

        public Set<String> getMaintainRoles(){

            Set<String> maintainRoles = null;

            if (maintainRolesCache != null && maintainRolesCache.containsKey("maintainRoles")) {
                maintainRoles = (Set<String>) maintainRolesCache.get("maintainRoles");
            }
            if(maintainRoles == null) {
                String sql = dbAuthzGroupSql.getMaintainRolesSql();
                maintainRoles = new HashSet<String>(m_sql.dbRead(sql));
                maintainRolesCache.put("maintainRoles", maintainRoles);
            }

            return maintainRoles;
        }

        public RealmLock newRealmLock(Integer key, String reference, RealmLockMode lockMode) {
            return new RealmLock(key, reference, lockMode);
        }

		private class UserAndGroups
		{
			String user;
			long total;
			long hit;
			Map<Long, List<String>> realmsQuery;

			public UserAndGroups(String userid) {
				this.user = userid;
				this.total = 0;
				this.hit = 0;
				this.realmsQuery = new HashMap<Long, List<String>>();
			}

			void addRealmQuery(Set<String> query, List<String> result) {
				if (query == null || query.size() < 1) return;
				total++;
				Long queryHash = computeRealmQueryHash(query);

				if (queryHash != null) {
					if (result == null) result = Collections.emptyList();
					realmsQuery.put(queryHash, result);
				}
			}

			List<String> getRealmQuery(Set<String> query) {
				if (query == null || query.size() < 1) return null;
				List<String> result = null;

				total++;
				Long queryHash = computeRealmQueryHash(query);

				if (queryHash != null) {
					if (realmsQuery.containsKey(queryHash)) {
						result = realmsQuery.get(queryHash);
						hit++;
					}
				}
				return result;
			}

			Long computeRealmQueryHash(Set<String> query) {

				if (query == null || query.size() == 0) return null;

				long hash = 0;
				for (String q : query) {
					hash += q.hashCode();
				}

				return Long.valueOf(hash);
			}

			@Override
			public int hashCode() {
				return user.hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == null) return false;
				if (this == obj) return true;
				if (getClass() != obj.getClass())
					return false;
				UserAndGroups other = (UserAndGroups) obj;
				if (user == null) {
					if (other.user != null)
						return false;
				} else if (!user.equals(other.user))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "UserAndGroups [" + (user != null ? "user=" + user : "") + "]" +
					" size=" + realmsQuery.size() + ", total=" + total + ", hits=" + hit + ", hit ratio=" + (hit * 100) / (float) total;
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

		@Data
		@AllArgsConstructor
		@EqualsAndHashCode
		class RealmLock {

			private Integer key;
			private String reference;
			@EqualsAndHashCode.Exclude private RealmLockMode lockMode;

			public RealmLock(RealmLock realmLock) {
				this.key = realmLock.getKey();
				this.reference = realmLock.getReference();
				this.lockMode = realmLock.getLockMode();
			}
		}
	} // DbStorage
	
	private Set<Integer> getRealmRoleKeys(Set<String> roles) {
		Set<Integer> roleIds = new HashSet<Integer>();
		for(String role: roles) {
			Integer realmRoleKey = getRealmRoleKey(role);
			// If the role hasn't yet been used then it won't exist and so we can't lookup it's ID.
			if (realmRoleKey != null) {
				roleIds.add(realmRoleKey);
			}
		}
		return roleIds;
	}

	class RealmRole implements Comparable<RealmRole>{
		private String name;
		private Integer key;

		RealmRole(String name) {
			this.name = name;
		}

		RealmRole(String name, Integer key) {
			this.name = name;
			this.key = key;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getKey() {
			return key;
		}

		public void setKey(Integer key) {
			this.key = key;
		}

		public int compareTo(RealmRole realmRole) {
			return this.name.compareToIgnoreCase(realmRole.name);
		}
	}
}
