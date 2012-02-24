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

/**
 * Implementations manage <code>LDAPConnection</code> allocation. 
 * 
 * @see LdapConnectionManager
 * @author Dan McCallum, Unicon Inc
 * @author John Lewis, Unicon Inc
 *
 */
public interface LdapConnectionManager {

	/**
	 * Initializes an instance for use, typically
	 * by digesting the contents of the assigned
	 * {@link LdapConnectionManagerConfig}. Usually
	 * called by Spring.
	 */
	public void init();
	
	/**
	 * Retrieve an <code>LDAPConnection</code> -- the connection may
	 * already be bound depending on the configuration
	 * @return a connected <code>LDAPConnection</code>
	 * @throws LDAPException if the <code>LDAPConnection</code> allocation fails
	 */
	public LDAPConnection getConnection() throws LDAPException;
	
	/**
	 * Retrieve a bound <code>LDAPConnection</code> using the indicated credentials
	 * @param dn the distringuished name for binding
	 * @param pw the password for binding
	 * @return a connected <code>LDAPConnection</code>
	 * @throws LDAPException if the <code>LDAPConnection</code> allocation fails
	 */
	public LDAPConnection getBoundConnection(String dn, String pw) throws LDAPException;
	
	/**
	 * Return an <code>LDAPConnection</code>.  This can allow for
	 * connections to be pooled instead of just destroyed.
	 * @param conn an <code>LDAPConnection</code> that you no longer need
	 */
	public void returnConnection(LDAPConnection conn);
	
	/**
	 * Assign the LDAPConnection management configuration.
	 * Should typically be invoked once and followed by a
	 * call to init().
	 * @param config a reference to a {@link LdapConnectionManagerConfig}. Should be cacheable without defensive copying.
	 */
	public void setConfig(LdapConnectionManagerConfig config);
	
	/**
	 * Retrieve the currently assigned {@link LdapConnectionManagerConfig}.
	 * @return the currently assigned {@link LdapConnectionManagerConfig}, if any
	 */
	public LdapConnectionManagerConfig getConfig();

	/**
	 * Shuts down an instance. Usually called by Spring.
	 */
	public void destroy();

}
