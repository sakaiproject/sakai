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

package es.udl.asic.user;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;

/**
 * <p>
 * An implementation of a Sakai UserDirectoryProvider that authenticates/retrieves users from an OpenLDAP directory.
 * </p>
 * 
 * @author ASIC - Udl
 */
@Slf4j
public class OpenLdapDirectoryProvider implements UserDirectoryProvider
{

	private String ldapHost = ""; // address of ldap server

	private int ldapPort = 389; // port to connect to ldap server on

	private String basePath = "";

	private Hashtable env = new Hashtable();

	public void init()
	{
		try
		{
			log.info("init()");
		}
		catch (Throwable t)
		{
			log.warn("init(): ", t);
		}

		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, getLdapHost() + ":" + getLdapPort());
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_CREDENTIALS, "secret");
	}

	public void destroy()
	{
		log.info("destroy()");
	}

	public boolean authenticateUser(String userLogin, UserEdit edit, String password)
	{
		Hashtable env = new Hashtable();
		InitialDirContext ctx;

		String INIT_CTX = "com.sun.jndi.ldap.LdapCtxFactory";
		String MY_HOST = getLdapHost() + ":" + getLdapPort();
		String cn;
		boolean returnVal = false;

		if (!password.equals(""))
		{

			env.put(Context.INITIAL_CONTEXT_FACTORY, INIT_CTX);
			env.put(Context.PROVIDER_URL, MY_HOST);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_CREDENTIALS, "secret");

			String[] returnAttribute = { "ou" };
			SearchControls srchControls = new SearchControls();
			srchControls.setReturningAttributes(returnAttribute);
			srchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			String searchFilter = "(&(objectclass=person)(uid=" + escapeSearchFilterTerm(userLogin) + "))";

			try
			{
				ctx = new InitialDirContext(env);
				NamingEnumeration answer = ctx.search(getBasePath(), searchFilter, srchControls);
				String trobat = "false";

				while (answer.hasMore() && trobat.equals("false"))
				{

					SearchResult sr = (SearchResult) answer.next();
					String dn = sr.getName().toString() + "," + getBasePath();

					// Second binding
					Hashtable authEnv = new Hashtable();
					try
					{
						authEnv.put(Context.INITIAL_CONTEXT_FACTORY, INIT_CTX);
						authEnv.put(Context.PROVIDER_URL, MY_HOST);
						authEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
						authEnv.put(Context.SECURITY_PRINCIPAL, sr.getName() + "," + getBasePath());
						authEnv.put(Context.SECURITY_CREDENTIALS, password);
						try
						{
							DirContext authContext = new InitialDirContext(authEnv);
							returnVal = true;
							trobat = "true";
							authContext.close();
						}
						catch (AuthenticationException ae)
						{
							log.info("Access forbidden");
						}

					}
					catch (NamingException namEx)
					{
						log.info("User doesn't exist");
						returnVal = false;
					}
				}
				if (trobat.equals("false")) returnVal = false;

			}
			catch (NamingException namEx)
			{
				log.error(namEx.getMessage(), namEx);
				returnVal = false;
			}
		}
		return returnVal;
	}

	public boolean findUserByEmail(UserEdit edit, String email)
	{

		env.put(Context.SECURITY_PRINCIPAL, "");
		env.put(Context.SECURITY_CREDENTIALS, "");
		String filter = "(&(objectclass=person)(mail=" + escapeSearchFilterTerm(email) + "))";
		return getUserInf(edit, filter);
	}

	public boolean getUser(UserEdit edit)
	{

		if (!userExists(edit.getEid())) return false;

		env.put(Context.SECURITY_PRINCIPAL, "");
		env.put(Context.SECURITY_CREDENTIALS, "");
		String filter = "(&(objectclass=person)(uid=" + escapeSearchFilterTerm(edit.getEid()) + "))";
		return getUserInf(edit, filter);
	}

	/**
	 * Access a collection of UserEdit objects; if the user is found, update the information, otherwise remove the UserEdit object from the collection.
	 * 
	 * @param users
	 *        The UserEdit objects (with id set) to fill in or remove.
	 */
	public void getUsers(Collection users)
	{
		for (Iterator i = users.iterator(); i.hasNext();)
		{
			UserEdit user = (UserEdit) i.next();
			if (!getUser(user))
			{
				i.remove();
			}
		}
	}

	protected boolean userExists(String id)
	{
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_CREDENTIALS, "secret");

		try
		{
			DirContext ctx = new InitialDirContext(env);

			/*
			 * Setup subtree scope to tell LDAP to recursively descend directory structure during searches.
			 */
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			/*
			 * Setup the directory entry attributes we want to search for. In this case it is the user's ID.
			 */

			String filter = "(&(objectclass=person)(uid=" + escapeSearchFilterTerm(id) + "))";

			/* Execute the search, starting at the directory level of Users */

			NamingEnumeration hits = ctx.search(getBasePath(), filter, searchControls);

			/* All we need to know is if there were any hits at all. */

			if (hits.hasMore())
			{
				hits.close();
				ctx.close();
				return true;
			}
			else
			{
				hits.close();
				ctx.close();
				return false;
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			return false;
		}
	}

	private boolean getUserInf(UserEdit edit, String filter)
	{

		String id = null;
		String firstName = null;
		String lastName = null;
		String employeenumber = null;
		String email = null;
		try
		{
			DirContext ctx = new InitialDirContext(env);

			// Setup subtree scope to tell LDAP to recursively descend directory structure
			// during searches.
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			// We want the user's id, first name and last name ...
			searchControls.setReturningAttributes(new String[] { "uid", "givenName", "sn" });

			// Execute the search, starting at the directory level of Users
			NamingEnumeration results = ctx.search(getBasePath(), filter, searchControls);

			while (results.hasMore())
			{
				SearchResult result = (SearchResult) results.next();
				String dn = result.getName().toString() + "," + getBasePath();
				Attributes attrs = ctx.getAttributes(dn);
				id = attrs.get("uid").get().toString();
				String cn = attrs.get("cn").get().toString();
				firstName = cn.substring(0, cn.indexOf(" "));
				lastName = cn.substring(cn.indexOf(" "));
				email = attrs.get("mail").get().toString();
			}

			results.close();
			ctx.close();
		}
		catch (Exception ex)
		{
			log.error(ex.getMessage(), ex);
			return false;
		}

		edit.setId(id);
		edit.setFirstName(firstName);
		edit.setLastName(lastName);
		edit.setEmail(email);
		return true;
	}

	/**
	 * @return Returns the ldapHost.
	 */

	public String getLdapHost()
	{
		return ldapHost;
	}

	/**
	 * @param ldapHost
	 *        The ldapHost to set.
	 */
	public void setLdapHost(String ldapHost)
	{
		this.ldapHost = ldapHost;
	}

	/**
	 * @return Returns the ldapPort.
	 */
	public int getLdapPort()
	{
		return ldapPort;
	}

	/**
	 * @param ldapPort
	 *        The ldapPort to set.
	 */
	public void setLdapPort(int ldapPort)
	{
		this.ldapPort = ldapPort;
	}

	/**
	 * @return Returns the basePath.
	 */
	public String getBasePath()
	{
		return basePath;
	}

	/**
	 * @param basePath
	 *        The basePath to set.
	 */
	public void setBasePath(String basePath)
	{
		this.basePath = basePath;
	}

	// helper class for storing user data in the hashtable cache

	/**
	 * {@inheritDoc}
	 */
	public boolean authenticateWithProviderFirst(String id)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean createUserRecord(String id)
	{
		return false;
	}
	
	/**
	 * Borrowed from the jldap provider
	 */
    public String escapeSearchFilterTerm(String term) {
        if (term == null) return null;
        //From RFC 2254
        String escapedStr = new String(term);
        escapedStr = escapedStr.replaceAll("\\\\","\\\\5c");
        escapedStr = escapedStr.replaceAll("\\*","\\\\2a");
        escapedStr = escapedStr.replaceAll("\\(","\\\\28");
        escapedStr = escapedStr.replaceAll("\\)","\\\\29");
        escapedStr = escapedStr.replaceAll("\\"+Character.toString('\u0000'), "\\\\00");
        return escapedStr;
    }
    
}
