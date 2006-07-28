package org.sakaiproject.tool.podcasts;

import java.security.Principal;

public class PodPrincipal implements Principal {
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
	public PodPrincipal(String name, String password)
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
		return "PodPrincipal: " + m_name;
	}

}
