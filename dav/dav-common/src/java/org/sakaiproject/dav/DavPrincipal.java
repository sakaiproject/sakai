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

/**
 * Implementation of Principal for Dav support in Sakai - holds the user name and password
 */
public class DavPrincipal implements Principal
{
	/** The username of the user represented by this Principal. */
	protected String m_name = null;

	/** The authentication credentials for the user represented by this Principal. */
	protected String m_password = null;

	/**
	 * Construct with this name and password.
	 * 
	 * @param name
	 *        The username of the user represented by this Principal
	 * @param password
	 *        Credentials used to authenticate this user
	 */
	public DavPrincipal(String name, String password)
	{
		m_name = name;
		m_password = password;
	}

	public String getName()
	{
		return m_name;
	}

	public String getPassword()
	{
		return m_password;
	}

	/**
	 * Does the user represented by this Principal possess the specified role?
	 * 
	 * @param role
	 *        Role to be tested.
	 * @return true if the Principal has the role, false if not.
	 */
	public boolean hasRole(String role)
	{
		if (role == null) return (false);
		return (true);
	}

	public String toString()
	{
		return "DavPrincipal: " + m_name;
	}
}
