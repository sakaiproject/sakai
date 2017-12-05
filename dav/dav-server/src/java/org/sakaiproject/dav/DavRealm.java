/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.dav;

import java.security.Principal;

import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.realm.RealmBase;
import org.apache.catalina.Wrapper;

/**
 * Simple implementation of <b>Realm</b> that consults the Sakai user directory service to provide container security equivalent to then application security in CHEF.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong>: The user is assumed to have all "roles" because servlets and teamlets will enforce roles within Sakai - so in this realm, we simply indicate "true".
 */
@Slf4j
public final class DavRealm extends RealmBase
{
	/** Descriptive information about this Realm implementation. */
	protected static final String info = "org.sakaiproject.realm.DavRealm/1.0";

	/** Descriptive information about this Realm implementation. */
	protected static final String name = "DavRealm";

	/**
	 * Return descriptive information about this Realm implementation and the corresponding version number, in the format <code>&lt;description&gt;/&lt;version&gt;</code>.
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * Return the Principal associated with the specified username and credentials, if there is one; otherwise return <code>null</code>.
	 * 
	 * @param username
	 *        Username of the Principal to look up
	 * @param credentials
	 *        Password or other credentials to use in authenticating this username
	 */
	public Principal authenticate(String username, String credentials)
	{
		if (username == null || credentials == null) return null;
		if (username.length() <= 0 || credentials.length() <= 0) return null;

		return new DavPrincipal(username, credentials);
	}

	/**
	 * Return a short name for this Realm implementation.
	 */
	protected String getName()
	{
		return name;
	}

	protected Principal getPrincipal(String username)
	{
		log.debug("DavRealm.getPrincipal({}) -- why is this being called?", username);

		if (username == null) return null;

		return new DavPrincipal(username, " ");
	}

	/**
	 * Return the password associated with the given principal's user name.
	 */
	protected String getPassword(String username)
	{
		log.debug("DavRealm.getPassword({})", username);
		return null;
	}

	public boolean hasRole(Wrapper wrapper, Principal principal, String role)  {
	    return true;
	}

	public boolean hasRole(Principal principal, String role)
	{
		return true;
	}
}
