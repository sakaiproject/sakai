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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPTLSSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * An object factory for managing <code>PooledLDAPConnection<code>s
 * within the commons-pool library.  Handles creating, configuring,
 * connecting, securing, and binding the connections as they pass
 * in and out of the pool. 
 * @see LdapConnectionManager
 * @see LdapConnectionManagerConfig
 * @author John Lewis, Unicon Inc
 */
@Slf4j
public class PooledLDAPConnectionFactory implements PoolableObjectFactory {

	/** the controlling connection manager */
	private LdapConnectionManager connectionManager;
	
	/** connection information */
	private String host;
	private int port;
	private String binddn;
	private byte[] bindpw;
	private boolean autoBind;

	/** flag to tell us if we are using TLS */
	private boolean useTLS;

	/** standard set of LDAP constraints */
	private LDAPConstraints standardConstraints;

	private LdapConnectionLivenessValidator livenessValidator;

	public PooledLDAPConnectionFactory() {
		this.livenessValidator = newDefaultConnectionLivenessValidator();
	}
	
	/**
	 * Constructs a new PooledLDAPConnection object, including:
	 * passing it the connection manager so it can return itself
	 * to the pool if it falls out of scope, setting a default
	 * set of constraints, connecting to the server, initiating
	 * TLS if needed.  Lastly it optionally binds as the "auto-bind 
	 * user" and clears the bindAttempted flag so we will know if 
	 * a user of the connection attempts to rebind it.
	 */
    public Object makeObject() throws LDAPException { 
		if (log.isDebugEnabled()) log.debug("makeObject()");
    	PooledLDAPConnection conn = newConnection();
    	if (log.isDebugEnabled()) log.debug("makeObject(): instantiated connection");
		conn.setConnectionManager(connectionManager);
		if (log.isDebugEnabled()) log.debug("makeObject(): assigned connection ConnectionManager");
		conn.setConstraints(standardConstraints);
		if (log.isDebugEnabled()) log.debug("makeObject(): assigned connection constraints");
		conn.connect(getHost(), port);
		if (log.isDebugEnabled()) log.debug("makeObject(): connected connection");
		if (useTLS) {
			if (log.isDebugEnabled()) log.debug("makeObject(): attempting to initiate TLS");
			conn.startTLS();
			if (log.isDebugEnabled()) log.debug("makeObject(): successfully initiated TLS");
		}
		if ( this.autoBind ) {
			if (log.isDebugEnabled()) log.debug("makeObject(): binding connection to default bind DN [" + binddn + "]");
			conn.bind(LDAPConnection.LDAP_V3, binddn, bindpw);
			if (log.isDebugEnabled()) log.debug("makeObject(): successfully bound connection to default bind DN [" + binddn + "]");
		}
		conn.setBindAttempted(false);
		if (log.isDebugEnabled()) log.debug("makeObject(): reset connection bindAttempted flag");
		return conn;
    }
    
    protected PooledLDAPConnection newConnection() {
    	return new PooledLDAPConnection();
    }

    /**
     * Activates a PooledLDAPConnection as it is being loaned out from the
     * pool, including: ensuring the default constraints are set and 
     * setting the active flag so that it can return itself to the pool 
     * if it falls out of scope.
     */
	public void activateObject(Object obj) throws LDAPException {
		if (log.isDebugEnabled()) log.debug("activateObject()");
    	if (obj instanceof PooledLDAPConnection) {
    		PooledLDAPConnection conn = (PooledLDAPConnection) obj;	
    		conn.setConstraints(standardConstraints);
    		if (log.isDebugEnabled()) log.debug("activateObject(): assigned connection constraints");
    		conn.setActive(true);
    		if (log.isDebugEnabled()) log.debug("activateObject(): set connection active flag");
    	} else {
    		if (log.isDebugEnabled()) {
    			log.debug("activateObject(): connection not of expected type [" + 
    					(obj == null ? "null" : obj.getClass().getName()) + "] nothing to do");
    		}		
    	}
	}

    /**
     * Passivates a PooledLDAPConnection as it is being returned to
     * the pool, including: clearing the active flag so that it won't
     * attempt to return itself to the pool.
     */
    public void passivateObject(Object obj) throws LDAPException {
		if (log.isDebugEnabled()) log.debug("passivateObject()");
    	if (obj instanceof PooledLDAPConnection) {
    		PooledLDAPConnection conn = (PooledLDAPConnection) obj;
    		conn.setActive(false);
    		if (log.isDebugEnabled()) log.debug("passivateObject(): unset connection active flag");
    	} else {
    		if (log.isDebugEnabled()) {
    			log.debug("passivateObject(): connection not of expected type [" + 
    				(obj == null ? "null" : obj.getClass().getName()) + "] nothing to do");
    		}
    	}
    } 

    /**
     * Validates a PooledLDAPConnection by checking if the connection
     * is alive and ensuring it is properly bound as the autoBind user.
     * If a borrower attempted to rebind the connection, then the
     * bindAttempted flag will be true -- in that case rebind it as
     * the autoBind user and clear the bindAttempted flag.  
     */
	public boolean validateObject(Object obj) {
		if (log.isDebugEnabled()) log.debug("validateObject()");
		if ( obj == null ) {
			if (log.isDebugEnabled()) log.debug("validateObject(): received null object reference, returning false");
			return false;
		}
    	if (obj instanceof PooledLDAPConnection) {
    		PooledLDAPConnection conn = (PooledLDAPConnection) obj;
    		if (log.isDebugEnabled()) log.debug("validateObject(): received PooledLDAPConnection object to validate");
    		
            // ensure we're always bound as the system user so the liveness
            // search can succeed (it actually uses the system user's account as
            // the base DN)
            if (conn.isBindAttempted()) {
            	
            	if (log.isDebugEnabled()) log.debug("validateObject(): connection bindAttempted flag is set");
            	
            	if ( !(autoBind) ) {
            		if (log.isDebugEnabled()) {
                        log.debug("validateObject(): last borrower attempted bind operation, but no default bind credentials available, invalidating connection");
            		}
            		conn.setActive(false);
            		if (log.isDebugEnabled()) log.debug("validateObject(): unset connection bindAttempted flag due to missing default bind credentials, returning false");
            		return false;
            	}
            	
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("validateObject(): last borrower attempted bind operation - rebinding with defaults [bind dn: " + binddn + "]");
                    }
                    conn.bind(LDAPConnection.LDAP_V3, binddn, bindpw);
                    if (log.isDebugEnabled()) log.debug("validateObject(): successfully bound connection [bind dn: " + binddn + "]");
                    conn.setBindAttempted(false);
                    if (log.isDebugEnabled()) log.debug("validateObject(): reset connection bindAttempted flag");
                } catch (Exception e) {
                    log.error("validateObject(): unable to rebind pooled connection", e);
                    conn.setActive(false);
                    if (log.isDebugEnabled()) log.debug("validateObject(): unset connection active flag due to bind failure, returning false");
                    return false;
                }
            }
            
            if (log.isDebugEnabled()) log.debug("validateObject(): beginning connection liveness testing");
            
            try {
            	if ( !(livenessValidator.isConnectionAlive(conn)) )  {
            		if (log.isInfoEnabled())
            			log.info("validateObject(): connection failed liveness test, "+ conn.getHost());
            		conn.setActive(false);
            		if (log.isDebugEnabled()) log.debug("validateObject(): unset connection active flag on stale connection, returning false");
            		return false;
            	}
            } catch ( Exception e ) {
            	log.error("validateObject(): unable to test connection liveness", e);
                conn.setActive(false);
                if (log.isDebugEnabled()) log.debug("validateObject(): unset connection active flag due to liveness test error, returning false");
                return false;
            }
    		
    	} else {
    		// we know the ref is not null
    		if (log.isDebugEnabled()) { 
    			log.debug("validateObject(): connection not of expected type [" +
    				obj.getClass().getName() + "] nothing to do");
    		}
    	}
    	
    	if (log.isDebugEnabled()) { 
			log.debug("validateObject(): connection appears to be valid, returning true");
		}
    	return true;
	}

	/**
	 * Cleans up a PooledLDAPConnection that is about to be destroyed by
	 * invoking {@link PooledLDAPConnection#disconnect()}. To ensure that the
	 * object is not inadvertently returned to the pool again by a 
	 * <code>finalize()</code> call, the connection's active flag is lowered 
	 * prior to the <code>disconnect()</code> call. Does nothing but log a pair of
	 * debug messages if the received object is not a {@link PooledLDAPConnection}.
	 */
	public void destroyObject(Object obj) throws Exception {
		if (log.isDebugEnabled()) log.debug("destroyObject()");
		if ( obj instanceof PooledLDAPConnection ) {
			((PooledLDAPConnection)obj).setActive(false);
			((PooledLDAPConnection)obj).disconnect();
		} else {
			if (log.isDebugEnabled()) {
    			log.debug("destroyObject(): connection not of expected type [" + 
    				(obj == null ? "null" : obj.getClass().getName()) + "] nothing to do");
    		}
		}
	}


	/**
	 * Gives the LdapConnectionMananger that the Factory is using
	 * to configure its PooledLDAPConnections.
	 * @return
	 */
	public LdapConnectionManager getConnectionManager() {
		return connectionManager;
	}

	/** Sets the LdapConnectionManager that the Factory will use
	 * to configure and manage its PooledLDAPConnections.  This includes
	 * gathering all the connection information (host, port, user, passord),
	 * setting the SocketFactory, determining if we are using TLS, and
	 * creating the default constraints.
	 * @param connectionManager the LdapConnectionManager to use
	 */
	public void setConnectionManager(LdapConnectionManager connectionManager) {

		this.connectionManager = connectionManager;

		// collect connection information
		this.host = connectionManager.getConfig().getLdapHost();
		this.port = connectionManager.getConfig().getLdapPort();
		this.autoBind = connectionManager.getConfig().isAutoBind();
		if ( this.autoBind ) {
			this.binddn = connectionManager.getConfig().getLdapUser();
			try {
				this.bindpw = connectionManager.getConfig().getLdapPassword().getBytes("UTF8");
			} catch (Exception e) {
				throw new RuntimeException("unable to encode bind password", e);
			}
		}
		
		// determine if we are using TLS
		useTLS = (connectionManager.getConfig().isSecureConnection() &&
			(connectionManager.getConfig().getSecureSocketFactory() instanceof LDAPTLSSocketFactory));

		// set up the standard constraints
		standardConstraints = new LDAPConstraints();
		standardConstraints.setTimeLimit(connectionManager.getConfig().getOperationTimeout());
		standardConstraints.setReferralFollowing(connectionManager.getConfig().isFollowReferrals());
	
	}

	/**
	 * Assign a strategy for verifying {@link LDAPConnection}
	 * liveness. Defaults to an instance of 
	 * {@link NativeLdapConnectionLivenessValidator}.
	 * 
	 * @param livenessValidator an object implementing a 
	 *   liveness validation strategy. Pass <code>null</code>
	 *   to force default behavior.
	 */
	public void setConnectionLivenessValidator(
			LdapConnectionLivenessValidator livenessValidator) {
		if ( livenessValidator == null ) {
			livenessValidator = newDefaultConnectionLivenessValidator();
		}
		this.livenessValidator = livenessValidator;
	}

	/**
	 * As implemented, will return a new {@link NativeLdapConnectionLivenessValidator}.
	 * 
	 * @return the default {@link LdapConnectionLivenessValidator} strategy.
	 */
	protected LdapConnectionLivenessValidator newDefaultConnectionLivenessValidator() {
		return new NativeLdapConnectionLivenessValidator();
	}

	/**
	 * Access the strategy for verifying {@link LDAPConnection}
	 * liveness. Will not return <code>null</code>.
	 * 
	 * @return the current connection liveness validator.
	 */
	public LdapConnectionLivenessValidator getConnectionLivenessValidator() {
		return this.livenessValidator;
	}
	
	/**
	 * Get the host to connect to. Attempt to resolve all addresses for a hostname when round robin DNS
	 * is used.
	 *
	 * We do this resolution low down in the stack as we don't want the results cached at all.
	 * Unless configured otherwise the JVM will cache DNS lookups. Even if this is just for 30 seconds
	 * (default in 1.6/1.7) it means when the pool is getting prebuilt at startup the connections will
	 * all point to the same server.
	 *
	 * @return A hostname or a space separated string of IP addresses to connect to. 
	 */
	protected String getHost() {
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		// The host may already be space separated.
		StringTokenizer hosts = new StringTokenizer(host, " ");
		while (hosts.hasMoreTokens()) {
			try {
				addresses.addAll(Arrays.asList(InetAddress.getAllByName(hosts.nextToken())));
			} catch (UnknownHostException e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to resolve " + host + " not handling now, will deal with later.");
				}
			}
		}
		if (addresses.size() > 1) {
			StringBuilder resolvedHosts = new StringBuilder();
			// So that we don't always connect to the same host.
			// Is need on platforms that don't do round robin DNS well.
			Collections.shuffle(addresses);
			for (InetAddress address : addresses) {
				resolvedHosts.append(address.getHostAddress() + " ");
			}
			return resolvedHosts.toString();
		} else {
			// Just return the configured hostname and let it be resolved when making the connection.
			return host;
		}
	}

}
