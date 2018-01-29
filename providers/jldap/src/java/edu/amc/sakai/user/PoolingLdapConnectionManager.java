/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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


package edu.amc.sakai.user;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

/**
 * Allocates connected, constrained, bound and optionally secure <code>LDAPConnection</code>s.
 * Uses commons-pool to provide a pool of connections instead of creating a new
 * connection for each request.  Originally tried implementing this with
 * <code>om.novell.ldap.connectionpool.PoolManager</code>, but it did not handle
 * recovering connections that had suffered a network error or connections that
 * were never returned but dropped out of scope.
 * @see LdapConnectionManagerConfig
 * @see PooledLDAPConnection
 * @see PooledLDAPConnectionFactory
 * @author John Lewis, Unicon Inc
 */
@Slf4j
public class PoolingLdapConnectionManager extends SimpleLdapConnectionManager {
	/** LDAP connection pool */
	private ObjectPool pool;

	private PooledLDAPConnectionFactory factory;

	/** How long to block waiting for an available connection before throwing an exception */
	private static final int POOL_MAX_WAIT = 60000;
	
	/**
	 * {@inheritDoc}
	 */
	public void init() {
		super.init();
		
		if ( pool != null ) {
			return;
		}
		
		if ( factory == null ) {
			factory = new PooledLDAPConnectionFactory();
		}
		factory.setConnectionManager(this);

		Config poolConfig = new Config();
		poolConfig.maxActive = getConfig().getPoolMaxConns();
		poolConfig.maxIdle = getConfig().getPoolMaxConns();
		poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
		poolConfig.maxWait = POOL_MAX_WAIT;
		poolConfig.testOnBorrow = true;
		poolConfig.testOnReturn = false;

		pool = new GenericObjectPool(factory, poolConfig);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public LDAPConnection getConnection() throws LDAPException {
		if (log.isDebugEnabled()) log.debug("getConnection(): attempting to borrow connection from pool");
		try {
			LDAPConnection conn = (LDAPConnection)pool.borrowObject();
			if (log.isDebugEnabled()) log.debug("getConnection(): successfully to borrowed connection from pool");
			return conn;
		} catch (Exception e) {
			if (e instanceof LDAPException)	throw (LDAPException) e;
			throw new RuntimeException("failed to get pooled connection", e);
		}
	}
	
	public LDAPConnection getBoundConnection(String dn, String pw) throws LDAPException {
		if (log.isDebugEnabled()) log.debug("getBoundConnection():dn=["+dn+"] attempting to borrow connection from pool and bind to dn");
        LDAPConnection conn = null;
		try {
			conn = (LDAPConnection)pool.borrowObject();
			if (log.isDebugEnabled()) log.debug("getBoundConnection():dn=["+dn+"] successfully borrowed connection from pool");
			conn.bind(LDAPConnection.LDAP_V3, dn, pw.getBytes("UTF8"));
			if (log.isDebugEnabled()) log.debug("getBoundConnection():dn=["+dn+"] successfully bound to dn");
			return conn;
		} catch (Exception e) {
            if ( conn != null ) {
                try {
                	if (log.isDebugEnabled()) log.debug("getBoundConnection():dn=["+dn+"]; error occurred, returning connection to pool");
                    returnConnection(conn);
                } catch ( Exception ee ) {
                	if (log.isDebugEnabled()) log.debug("getBoundConnection():dn=["+dn+"] failed to return connection to pool", ee);
                }
            }
			if (e instanceof LDAPException)	throw (LDAPException) e;
			throw new RuntimeException("failed to get pooled connection", e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void returnConnection(LDAPConnection conn) {
		if ( conn == null ) {
			if (log.isDebugEnabled()) log.debug("returnConnection() received null connection; nothing to do");
			return;
		} else {
			if (log.isDebugEnabled()) log.debug("returnConnection(): attempting to return connection to the pool");
		}
		
		try {
			pool.returnObject(conn);
			if (log.isDebugEnabled()) log.debug("returnConnection(): successfully returned connection to pool");
		} catch (Exception e) {
			throw new RuntimeException("failed to return pooled connection", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		try {
			if ( log.isDebugEnabled() ) log.debug("destroy(): closing connection pool");
			pool.close();
			if ( log.isDebugEnabled() ) log.debug("destroy(): successfully closed connection pool");
		} catch (Exception e) {
			throw new RuntimeException("failed to shutdown connection pool", e);
		}
		if ( log.isDebugEnabled() ) log.debug("destroy(): delegating to parent destroy() impl");
		super.destroy();
	}

	public PooledLDAPConnectionFactory getFactory() {
		return factory;
	}

	public void setFactory(PooledLDAPConnectionFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * Assign a pool implementation. If not specified, one will
	 * be constructed by {@link #init()}. If specified,
	 * {@link #setFactory(PooledLDAPConnectionFactory)} will have
	 * no effect.
	 * 
	 * <p>This method exists almost entirely for testing purposes.</p>
	 * 
	 * @param pool the pool to cache; accepts <code>null</code>
	 */
	protected void setPool(ObjectPool pool) {
		this.pool = pool;
	}

}
