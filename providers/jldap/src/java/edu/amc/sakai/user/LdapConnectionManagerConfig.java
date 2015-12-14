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

import com.novell.ldap.LDAPSocketFactory;

/**
 * Bean interface for collecting {@link LdapConnectionManager} configuration.
 * 
 * <p>
 * This interface was introduced to retain forward compatibility of 
 * &lt;=2.3.0 config files, which configure a single {@link JLDAPDirectoryProvider}
 * bean. As of this writing, that class now implements this interface as a
 * mixin and makes itself available to the {@link LdapConnectionManager}
 * as a side-effect of {@link JLDAPDirectoryProvider#init()}.
 * </p>
 * 
 * @author Dan McCallum, Unicon Inc
 *
 */
public interface LdapConnectionManagerConfig {
	
	/**
	 * If <code>true</code>, connect to LDAP over a secure protocol.
	 */
	public boolean isSecureConnection();
	
	/**
	 * Set to <code>true</code> if LDAP connections
	 * should occur over a secure protocol.
	 */
	public void setSecureConnection(boolean secureConnection);
	
	/**
	 * @return location of a SSL keystore
	 */
	public String getKeystoreLocation();
	
	/**
	 * @param keystoreLocation the location of an SSL keystore
	 */
	public void setKeystoreLocation(String keystoreLocation);
	
	/**
	 * @return SSL keystore password
	 */
	public String getKeystorePassword();
	
	/**
	 * @param keystoreLocation SSL keystore password
	 */
	public void setKeystorePassword(String keystorePassword);
	
	/**
	 * Assign a {@link LDAPSocketFactory} appropriate for your
	 * environment, e.g. {@link com.novell.ldap.LDAPJSSESecureSocketFactory}
	 * or {@link com.novell.ldap.LDAPJSSEStartTLSFactory}. This
	 * factory will only be used if {@link #isSecureConnection()}
	 * returns <code>true</code>
	 */
	public LDAPSocketFactory getSecureSocketFactory();

	/**
	 * @param the current secureSocketFactory. Should not return <code>null</code>.
	 */
	public void setSecureSocketFactory(LDAPSocketFactory secureSocketFactory);

	/**
	 * The socket factory to be used when creating insecure connections.
	 * This factory will only be used if {@link #isSecureConnection()} returns
	 * <code>false</code>. Can return <code>null</code> if no socket factory
	 * needs to be used.
     */
	public LDAPSocketFactory getSocketFactory();

	/**
	 * @param socketFactory the socketFactory to use.
     */
	public void setSocketFactory(LDAPSocketFactory socketFactory);

	
	/**
	 * @return the directory operation timeout
	 */
	public int getOperationTimeout();

	/**
	 * @param operationTimeout
	 *        the directory operation timeout to set.
	 */
	public void setOperationTimeout(int operationTimeout);
	
	/**
	 * @return the LDAP host address or name.
	 */
	public String getLdapHost();

	/**
	 * @param ldapHost
	 *        The LDAP host address or name.
	 */
	public void setLdapHost(String ldapHost);

	/**
	 * @return the LDAP connection port.
	 */
	public int getLdapPort();

	/**
	 * @param ldapPort
	 *        The LDAP connection port to set.
	 */
	public void setLdapPort(int ldapPort);
	
	/**
	 * @return the LDAP user to bind as, typically a manager acct.
	 */
	public String getLdapUser();

	/**
	 * @param ldapUser The user to bind to LDAP as, typically a manager acct, 
	 *   leave blank for anonymous.
	 */
	public void setLdapUser(String ldapUser);

	/**
	 * @see #getLdapUser()
	 * @return Returns the LDAP password corresponding to the
	 *   current default bind-as user.
	 */
	public String getLdapPassword();

	/**
	 * @param ldapPassword the LDAP password corresponding to the
	 *   current default bind-as user.
	 */
	public void setLdapPassword(String ldapPassword);
	
	/**
	 * Access LDAP referral following configuration
	 * 
	 * @return if <code>true</code>, directory accesses will
	 *   follow referrals
	 */
	public boolean isFollowReferrals();

	/**
	 * Configures LDAP referral following
	 * 
	 * @param followReferrals if <code>true</code>, directory 
	 *   accesses will follow referrals
	 */
	public void setFollowReferrals(boolean followReferrals);
	
	/**
	 * Access the LDAP auto-bind configuration
	 * 
	 * @return if <code>true</code> connection allocation
	 *   ({@link LdapConnectionManager#getConnection()}) will include a 
	 *   bind attempt
	 */
	public boolean isAutoBind();

	/**
	 * Configure the LDAP auto-bind configuration
	 * 
	 * param autoBind if <code>true</code> connection allocation
	 *   ({@link LdapConnectionManager#getConnection()}) will include a 
	 *   bind attempt
	 */
	public void setAutoBind(boolean autoBind);
	
	/**
	 * Access the LDAP pooling configuration
	 * 
	 * @return if <code>true</code> connections will be maintained in a
	 * connection pool.
	 */
	public boolean isPooling();

	/**
	 * Configure the LDAP connection pooling configuration
	 * 
	 * param pooling if <code>true</code> connections will be maintained
	 * in a connection pool.  This automatically sets autoBind to true as
	 * well
	 */
	public void setPooling(boolean pooling);

	/**
	 * @return The maximum number of physical connections in the pool
	 */
	public int getPoolMaxConns();

	/**
	 * @param maxConns The maximum number of physical connections in the pool
	 */
	public void setPoolMaxConns(int maxConns);
	
	/**
	 * @return The maximum number of objects to lookup in one query.
	 * @deprecated {@link #getBatchSize()}
	 */
	public int getMaxObjectsToQueryFor();
	
	/**
	 * @param maxObjectsToQueryFor The maximum number objects to lookup in one query.
	 * @deprecated {@link #setBatchSize(int)}
	 */
	public void setMaxObjectsToQueryFor(int maxObjectsToQueryFor);
	
	/**
	 * @return The maximum number of objects to lookup in one query.
	 */
	public int getBatchSize();

	/**
	 * @param batchSize The maximum number objects to lookup in one query.
	 */
	public void setBatchSize(int batchSize);

	/**
	 * @return The maximum number of results to ever get back from LDAP.
	 */
	public int getMaxResultSize();

	/**
	 * @param maxResultSize The maximum number of results to ever get back from LDAP.
	 */
	public void setMaxResultSize(int maxResultSize);

	/**
	 * @param enable If <code>true</code> then perform searches for users by Authentication ID.
	 */
	public void setEnableAid(boolean enable);
}
