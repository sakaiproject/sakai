/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.apache.commons.dbcp;

import java.sql.Connection;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;

/**
 * <p>
 * SakaiBasicDataSource extends apache common's BasicDataSource ...
 * </p>
 * <p>
 * Based on apache commons dbcp version 1.2.1, apache commons pool version 1.2
 * </p>
 */
@Slf4j
public class SakaiPoolableConnectionFactory extends PoolableConnectionFactory
{
	/** Configuration: to rollback each connection when borrowed from the pool. */
	protected boolean m_rollbackOnReturn = false;

	/**
	 * Create a new <tt>PoolableConnectionFactory</tt>.
	 * 
	 * @param connFactory
	 *        the {@link ConnectionFactory} from which to obtain base {@link Connection}s
	 * @param pool
	 *        the {@link ObjectPool} in which to pool those {@link Connection}s
	 * @param stmtPoolFactory
	 *        the {@link KeyedObjectPoolFactory} to use to create {@link KeyedObjectPool}s for pooling {@link java.sql.PreparedStatement}s, or <tt>null</tt> to disable {@link java.sql.PreparedStatement} pooling
	 * @param validationQuery
	 *        a query to use to {@link #validateObject validate} {@link Connection}s. Should return at least one row. Using <tt>null</tt> turns off validation.
	 * @param defaultReadOnly
	 *        the default "read only" setting for borrowed {@link Connection}s
	 * @param defaultAutoCommit
	 *        the default "auto commit" setting for returned {@link Connection}s
	 * @param rollbackOnReturn
	 *        The rollback on borrow setting
	 */
	public SakaiPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory,
			String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit, boolean rollbackOnReturn)
	{
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit);
		m_rollbackOnReturn = rollbackOnReturn;
	}

	/**
	 * Create a new <tt>PoolableConnectionFactory</tt>.
	 * 
	 * @param connFactory
	 *        the {@link ConnectionFactory} from which to obtain base {@link Connection}s
	 * @param pool
	 *        the {@link ObjectPool} in which to pool those {@link Connection}s
	 * @param stmtPoolFactory
	 *        the {@link KeyedObjectPoolFactory} to use to create {@link KeyedObjectPool}s for pooling {@link java.sql.PreparedStatement}s, or <tt>null</tt> to disable {@link java.sql.PreparedStatement} pooling
	 * @param validationQuery
	 *        a query to use to {@link #validateObject validate} {@link Connection}s. Should return at least one row. Using <tt>null</tt> turns off validation.
	 * @param defaultReadOnly
	 *        the default "read only" setting for borrowed {@link Connection}s
	 * @param defaultAutoCommit
	 *        the default "auto commit" setting for returned {@link Connection}s
	 * @param defaultTransactionIsolation
	 *        the default "Transaction Isolation" setting for returned {@link Connection}s
	 * @param rollbackOnReturn
	 *        The rollback on borrow setting
	 */
	public SakaiPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory,
			String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation,
			boolean rollbackOnReturn)
	{
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation);
		m_rollbackOnReturn = rollbackOnReturn;
	}

	/**
	 * Create a new <tt>PoolableConnectionFactory</tt>.
	 * 
	 * @param connFactory
	 *        the {@link ConnectionFactory} from which to obtain base {@link Connection}s
	 * @param pool
	 *        the {@link ObjectPool} in which to pool those {@link Connection}s
	 * @param stmtPoolFactory
	 *        the {@link KeyedObjectPoolFactory} to use to create {@link KeyedObjectPool}s for pooling {@link java.sql.PreparedStatement}s, or <tt>null</tt> to disable {@link java.sql.PreparedStatement} pooling
	 * @param validationQuery
	 *        a query to use to {@link #validateObject validate} {@link Connection}s. Should return at least one row. Using <tt>null</tt> turns off validation.
	 * @param defaultReadOnly
	 *        the default "read only" setting for borrowed {@link Connection}s
	 * @param defaultAutoCommit
	 *        the default "auto commit" setting for returned {@link Connection}s
	 * @param config
	 *        the AbandonedConfig if tracing SQL objects
	 * @param rollbackOnReturn
	 *        The rollback on borrow setting
	 * @deprecated AbandonedConfig is now deprecated.
	 */
	public SakaiPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory,
			String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit, AbandonedConfig config,
			boolean rollbackOnReturn)
	{
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, config);
		m_rollbackOnReturn = rollbackOnReturn;
	}

	/**
	 * Create a new <tt>PoolableConnectionFactory</tt>.
	 * 
	 * @param connFactory
	 *        the {@link ConnectionFactory} from which to obtain base {@link Connection}s
	 * @param pool
	 *        the {@link ObjectPool} in which to pool those {@link Connection}s
	 * @param stmtPoolFactory
	 *        the {@link KeyedObjectPoolFactory} to use to create {@link KeyedObjectPool}s for pooling {@link java.sql.PreparedStatement}s, or <tt>null</tt> to disable {@link java.sql.PreparedStatement} pooling
	 * @param validationQuery
	 *        a query to use to {@link #validateObject validate} {@link Connection}s. Should return at least one row. Using <tt>null</tt> turns off validation.
	 * @param defaultReadOnly
	 *        the default "read only" setting for borrowed {@link Connection}s
	 * @param defaultAutoCommit
	 *        the default "auto commit" setting for returned {@link Connection}s
	 * @param defaultTransactionIsolation
	 *        the default "Transaction Isolation" setting for returned {@link Connection}s
	 * @param config
	 *        the AbandonedConfig if tracing SQL objects
	 * @param rollbackOnReturn
	 *        The rollback on borrow setting
	 * @deprecated AbandonedConfig is now deprecated.
	 */
	public SakaiPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory,
			String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation,
			AbandonedConfig config, boolean rollbackOnReturn)
	{
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation,
				config);
		m_rollbackOnReturn = rollbackOnReturn;
	}

	/**
	 * Create a new <tt>PoolableConnectionFactory</tt>.
	 * 
	 * @param connFactory
	 *        the {@link ConnectionFactory} from which to obtain base {@link Connection}s
	 * @param pool
	 *        the {@link ObjectPool} in which to pool those {@link Connection}s
	 * @param stmtPoolFactory
	 *        the {@link KeyedObjectPoolFactory} to use to create {@link KeyedObjectPool}s for pooling {@link java.sql.PreparedStatement}s, or <tt>null</tt> to disable {@link java.sql.PreparedStatement} pooling
	 * @param validationQuery
	 *        a query to use to {@link #validateObject validate} {@link Connection}s. Should return at least one row. Using <tt>null</tt> turns off validation.
	 * @param defaultReadOnly
	 *        the default "read only" setting for borrowed {@link Connection}s
	 * @param defaultAutoCommit
	 *        the default "auto commit" setting for returned {@link Connection}s
	 * @param defaultTransactionIsolation
	 *        the default "Transaction Isolation" setting for returned {@link Connection}s
	 * @param defaultCatalog
	 *        the default "catalog" setting for returned {@link Connection}s
	 * @param config
	 *        the AbandonedConfig if tracing SQL objects
	 * @param rollbackOnReturn
	 *        The rollback on borrow setting
	 * @deprecated AbandonedConfig is now deprecated.
	 */
	public SakaiPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory,
			String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation,
			String defaultCatalog, AbandonedConfig config, boolean rollbackOnReturn)
	{
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation,
				defaultCatalog, config);
		m_rollbackOnReturn = rollbackOnReturn;
	}

	/**
	 * Create a new <tt>PoolableConnectionFactory</tt>.
	 * 
	 * @param connFactory
	 *        the {@link ConnectionFactory} from which to obtain base {@link Connection}s
	 * @param pool
	 *        the {@link ObjectPool} in which to pool those {@link Connection}s
	 * @param stmtPoolFactory
	 *        the {@link KeyedObjectPoolFactory} to use to create {@link KeyedObjectPool}s for pooling {@link java.sql.PreparedStatement}s, or <tt>null</tt> to disable {@link java.sql.PreparedStatement} pooling
	 * @param validationQuery
	 *        a query to use to {@link #validateObject validate} {@link Connection}s. Should return at least one row. Using <tt>null</tt> turns off validation.
	 * @param defaultReadOnly
	 *        the default "read only" setting for borrowed {@link Connection}s
	 * @param defaultAutoCommit
	 *        the default "auto commit" setting for returned {@link Connection}s
	 * @param defaultTransactionIsolation
	 *        the default "Transaction Isolation" setting for returned {@link Connection}s
	 * @param defaultCatalog
	 *        the default "catalog" setting for returned {@link Connection}s
	 * @param config
	 *        the AbandonedConfig if tracing SQL objects
	 * @param rollbackOnReturn
	 *        The rollback on borrow setting
	 */
	public SakaiPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory,
			String validationQuery, Boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation,
			String defaultCatalog, AbandonedConfig config, boolean rollbackOnReturn)
	{
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation,
				defaultCatalog, config);
		m_rollbackOnReturn = rollbackOnReturn;
	}

	/**
	 * Sakai modification: don't set autocommit, don't rollback if so configured!
	 */
	public void passivateObject(Object obj) throws Exception
	{
		if (obj instanceof Connection)
		{
			Connection conn = (Connection) obj;
			if (m_rollbackOnReturn)
			{
				if (!conn.getAutoCommit() && !conn.isReadOnly())
				{
					Exception e = new RuntimeException("Automatic Transaction Rollback");
					log.error("Transaction RolledBack!", e);
					conn.rollback();
				}
			}

			conn.clearWarnings();
			// conn.setAutoCommit(true);
		}
		if (obj instanceof DelegatingConnection)
		{
			((DelegatingConnection) obj).passivate();
		}
	}

	/**
	 * Sakai modifications: set auto-commit only if it does not match the default setting
	 */
	public void activateObject(Object obj) throws Exception
	{
		if (obj instanceof DelegatingConnection)
		{
			((DelegatingConnection) obj).activate();
		}
		if (obj instanceof Connection)
		{
			Connection conn = (Connection) obj;
			if (conn.getAutoCommit() != _defaultAutoCommit)
			{
				conn.setAutoCommit(_defaultAutoCommit);
			}
			if ((_defaultTransactionIsolation != UNKNOWN_TRANSACTIONISOLATION)
					&& (conn.getTransactionIsolation() != _defaultTransactionIsolation))
			{
				conn.setTransactionIsolation(_defaultTransactionIsolation);
			}
			if ((_defaultReadOnly != null) && (conn.isReadOnly() != _defaultReadOnly.booleanValue()))
			{
				conn.setReadOnly(_defaultReadOnly.booleanValue());
			}
			if ((_defaultCatalog != null) && (!_defaultCatalog.equals(conn.getCatalog())))
			{
				conn.setCatalog(_defaultCatalog);
			}
		}
	}
}
