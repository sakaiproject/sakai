/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.kerberos.user;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.util.StringUtil;

import org.apache.commons.codec.binary.Base64;

/**
 * <p>
 * KerberosUserDirectoryProvider is a UserDirectoryProvider that authenticates usernames using Kerberos.
 * </p>
 * <p>
 * For more information on configuration, see the README.txt file
 * <p>
 */
public class KerberosUserDirectoryProvider implements UserDirectoryProvider
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(KerberosUserDirectoryProvider.class);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration options and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Configuration: Domain */
	protected String m_domain = "domain.tld";

	/**
	 * Configuration: Domain Name (for E-Mail Addresses)
	 * 
	 * @param domain
	 *        The domain in the form of "domain.tld"
	 */
	public void setDomain(String domain)
	{
		m_domain = domain;
	}

	/** Configuration: LoginContext */
	protected String m_logincontext = "KerberosAuthentication";

	/**
	 * Configuration: Authentication Name
	 * 
	 * @param logincontext
	 *        The context to be used from the login.config file - default "KerberosAuthentication"
	 */
	public void setLoginContext(String logincontext)
	{
		m_logincontext = logincontext;
	}

	/** Configuration: RequireLocalAccount */
	protected boolean m_requirelocalaccount = true;

	/**
	 * Configuration: Require Local Account
	 * 
	 * @param requirelocalaccount
	 *        Determine if a local account is required for user to authenticate - default "true"
	 */
	public void setRequireLocalAccount(Boolean requirelocalaccount)
	{
		m_requirelocalaccount = requirelocalaccount.booleanValue();
	}

	/** Configuration: KnownUserMsg */
	protected String m_knownusermsg = "Integrity check on decrypted field failed";

	/**
	 * Configuration: Kerberos Error Message
	 * 
	 * @param knownusermsg
	 *        Start of error returned for bad logins by known users - default is from RFC 1510
	 */
	public void setKnownUserMsg(String knownusermsg)
	{
		m_knownusermsg = knownusermsg;
	}

	/** Configuration: Cachettl */
	protected int m_cachettl = 5 * 60 * 1000;

	/**
	 * Configuration: Cache TTL
	 * 
	 * @param cachettl
	 *        Time (in milliseconds) to cache authenticated usernames - default is 300000 ms (5 minutes)
	 */
	public void setCachettl(int cachettl)
	{
		m_cachettl = cachettl;
	}

	/**
	 * Hash table for auth caching
	 */

	private Hashtable users = new Hashtable();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{

			// Full paths only from the file
			String kerberoskrb5conf = ServerConfigurationService.getString("provider.kerberos.krb5.conf", null);
			String kerberosauthloginconfig = ServerConfigurationService.getString("provider.kerberos.auth.login.config", null);
			boolean kerberosshowconfig = ServerConfigurationService.getBoolean("provider.kerberos.showconfig", false);
			String sakaihomepath = System.getProperty("sakai.home");

			// if locations are configured in sakai.properties, use them in place of the current system locations
			// if the location specified exists and is readable, use full absolute path
			// otherwise, try file path relative to sakai.home
			// if files are readable use the, otherwise print warning and use system defaults
			if (kerberoskrb5conf != null)
			{
				if (new File(kerberoskrb5conf).canRead())
				{
					System.setProperty("java.security.krb5.conf", kerberoskrb5conf);
				}
				else if (new File(sakaihomepath + kerberoskrb5conf).canRead())
				{
					System.setProperty("java.security.krb5.conf", sakaihomepath + kerberoskrb5conf);
				}
				else
				{
					M_log.warn(this + ".init(): Cannot set krb5conf location");
					kerberoskrb5conf = null;
				}
			}

			if (kerberosauthloginconfig != null)
			{

				if (new File(kerberosauthloginconfig).canRead())
				{
					System.setProperty("java.security.auth.login.config", kerberosauthloginconfig);
				}
				else if (new File(sakaihomepath + kerberosauthloginconfig).canRead())
				{
					System.setProperty("java.security.auth.login.config", sakaihomepath + kerberosauthloginconfig);
				}
				else
				{
					M_log.warn(this + ".init(): Cannot set kerberosauthloginconfig location");
					kerberosauthloginconfig = null;
				}
			}

			M_log.info(this + ".init()" + " Domain=" + m_domain + " LoginContext=" + m_logincontext + " RequireLocalAccount="
					+ m_requirelocalaccount + " KnownUserMsg=" + m_knownusermsg + " CacheTTL=" + m_cachettl);

			// show the whole config if set
			// system locations will read NULL if not set (system defaults will be used)
			if (kerberosshowconfig)
			{
				M_log.info(this + ".init()" + " SakaiHome=" + sakaihomepath + " SakaiPropertyKrb5Conf=" + kerberoskrb5conf
						+ " SakaiPropertyAuthLoginConfig=" + kerberosauthloginconfig + " SystemPropertyKrb5Conf="
						+ System.getProperty("java.security.krb5.conf") + " SystemPropertyAuthLoginConfig="
						+ System.getProperty("java.security.auth.login.config"));
			}

		}
		catch (Throwable t)
		{
			M_log.warn(this + ".init(): ", t);
		}

	} // init

	/**
	 * Returns to uninitialized state. You can use this method to release resources thet your Service allocated when Turbine shuts down.
	 */
	public void destroy()
	{
		M_log.info(this + ".destroy()");

	} // destroy

	/**********************************************************************************************************************************************************************************************************************************************************
	 * UserDirectoryProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Access a user object. Update the object with the information found.
	 * 
	 * @param edit
	 *        The user object (id is set) to fill in.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean getUser(UserEdit edit)
	{
		if (m_requirelocalaccount) return false;
		if (!userKnownToKerberos(edit.getEid())) return false;

		edit.setEmail(edit.getEid() + "@" + m_domain);
		edit.setType("kerberos");

		return true;
	} // getUser

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

	/**
	 * Find a user object who has this email address. Update the object with the information found.
	 * 
	 * @param email
	 *        The email address string.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean findUserByEmail(UserEdit edit, String email)
	{
		// lets not get messed up with spaces or cases
		String test = email.toLowerCase().trim();

		// if the email ends with "domain.tld" (even if it's from somebody@foo.bar.domain.tld)
		// use the local part as a user id.

		if (!test.endsWith(m_domain)) return false;

		// split the string once at the first "@"
		String parts[] = StringUtil.splitFirst(test, "@");
		edit.setEid(parts[0]);
		return getUser(edit);

	} // findUserByEmail

	/**
	 * Authenticate a user / password. Check for an "valid, previously authenticated" user in in-memory table.
	 * 
	 * @param id
	 *        The user id.
	 * @param edit
	 *        The UserEdit matching the id to be authenticated (and updated) if we have one.
	 * @param password
	 *        The password.
	 * @return true if authenticated, false if not.
	 */
	public boolean authenticateUser(String userId, UserEdit edit, String password)
	{
		// The in-memory caching mechanism is implemented here
		// try to get user from in-memory hashtable
		try
		{
			UserData existingUser = (UserData) users.get(userId);

			boolean authUser = false;
			String hpassword = encodeSHA(password);

			// Check for user in in-memory hashtable. To be a "valid, previously authenticated" user,
			// 3 conditions must be met:
			//
			// 1) an entry for the userId must exist in the cache
			// 2) the last usccessful authentication was < cachettl milliseconds ago
			// 3) the one-way hash of the entered password must be equivalent to what is stored in the cache
			//
			// If these conditions are not, the authentication is performed via JAAS and, if sucessful, a new entry is created

			if (existingUser == null || (System.currentTimeMillis() - existingUser.getTimeStamp()) > m_cachettl
					|| !(existingUser.getHpw().equals(hpassword)))
			{
				if (M_log.isDebugEnabled()) M_log.debug("authenticateUser(): user " + userId + " not in table, querying Kerberos");

				boolean authKerb = authenticateKerberos(userId, password);

				// if authentication succeeds, create entry for authenticated user in cache;
				// otherwise, remove any entries for this user from cache

				if (authKerb)
				{
					if (M_log.isDebugEnabled())
						M_log.debug("authenticateUser(): putting authenticated user (" + userId + ") in table for caching");

					UserData u = new UserData(); // create entry for authenticated user in cache
					u.setId(userId);
					u.setHpw(hpassword);
					u.setTimeStamp(System.currentTimeMillis());
					users.put(userId, u); // put entry for authenticated user into cache

				}
				else
				{
					users.remove(userId);
				}

				authUser = authKerb;

			}
			else
			{
				if (M_log.isDebugEnabled())
					M_log.debug("authenticateUser(): found authenticated user (" + existingUser.getId() + ") in table");
				authUser = true;
			}

			return authUser;
		}
		catch (Exception e)
		{
			if (M_log.isDebugEnabled()) M_log.debug("authenticateUser(): exception: " + e);
			return false;
		}
	} // authenticateUser

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Kerberos stuff
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Authenticate the user id and pw with Kerberos.
	 * 
	 * @param user
	 *        The user id.
	 * @param password
	 *        the user supplied password.
	 * @return true if successful, false if not.
	 */
	protected boolean authenticateKerberos(String user, String pw)
	{
		// assure some length to the password
		if ((pw == null) || (pw.length() == 0)) return false;

		// Obtain a LoginContext, needed for authentication. Tell it
		// to use the LoginModule implementation specified by the
		// appropriate entry in the JAAS login configuration
		// file and to also use the specified CallbackHandler.
		LoginContext lc = null;
		try
		{
			SakaiCallbackHandler t = new SakaiCallbackHandler();
			t.setId(user);
			t.setPw(pw);
			lc = new LoginContext(m_logincontext, t);
		}
		catch (LoginException le)
		{
			if (M_log.isDebugEnabled()) M_log.debug("authenticateKerberos(): " + le.toString());
			return false;
		}
		catch (SecurityException se)
		{
			if (M_log.isDebugEnabled()) M_log.debug("authenticateKerberos(): " + se.toString());
			return false;
		}

		try
		{
			// attempt authentication
			lc.login();
			lc.logout();

			if (M_log.isDebugEnabled()) M_log.debug("authenticateKerberos(" + user + ", pw): Kerberos auth success");

			return true;
		}
		catch (LoginException le)
		{
			if (M_log.isDebugEnabled())
				M_log.debug("authenticateKerberos(" + user + ", pw): Kerberos auth failed: " + le.toString());

			return false;
		}

	} // authenticateKerberos

	/**
	 * Check if the user id is known to kerberos.
	 * 
	 * @param user
	 *        The user id.
	 * @return true if successful, false if not.
	 */
	private boolean userKnownToKerberos(String user)
	{
		// use a dummy password
		String pw = "dummy";

		// Obtain a LoginContext, needed for authentication.
		// Tell it to use the LoginModule implementation specified
		// in the JAAS login configuration file and to use
		// use the specified CallbackHandler.
		LoginContext lc = null;
		try
		{
			SakaiCallbackHandler t = new SakaiCallbackHandler();
			t.setId(user);
			t.setPw(pw);
			lc = new LoginContext(m_logincontext, t);
		}
		catch (LoginException le)
		{
			if (M_log.isDebugEnabled()) M_log.debug("useKnownToKerberos(): " + le.toString());
			return false;
		}
		catch (SecurityException se)
		{
			if (M_log.isDebugEnabled()) M_log.debug("useKnownToKerberos(): " + se.toString());
			return false;
		}

		try
		{
			// attempt authentication
			lc.login();
			lc.logout();

			if (M_log.isDebugEnabled()) M_log.debug("useKnownToKerberos(" + user + "): Kerberos auth success");

			return true;
		}
		catch (LoginException le)
		{
			String msg = le.getMessage();

			// if this is the message, the user was good, the password was bad
			if (msg.startsWith(m_knownusermsg))
			{
				if (M_log.isDebugEnabled()) M_log.debug("userKnownToKerberos(" + user + "): Kerberos user known (bad pw)");

				return true;
			}

			// the other message is when the user is bad:
			if (M_log.isDebugEnabled()) M_log.debug("userKnownToKerberos(" + user + "): Kerberos user unknown or invalid");

			return false;
		}

	} // userKnownToKerberos

	/**
	 * Inner Class SakaiCallbackHandler Get the user id and password information for authentication purpose. This can be used by a JAAS application to instantiate a CallbackHandler.
	 * 
	 * @see javax.security.auth.callback
	 */
	protected class SakaiCallbackHandler implements CallbackHandler
	{
		private String m_id;

		private String m_pw;

		/** constructor */
		public SakaiCallbackHandler()
		{
			m_id = new String("");
			m_pw = new String("");

		} // SakaiCallbackHandler

		/**
		 * Handles the specified set of callbacks.
		 * 
		 * @param callbacks
		 *        the callbacks to handle
		 * @throws IOException
		 *         if an input or output error occurs.
		 * @throws UnsupportedCallbackException
		 *         if the callback is not an instance of NameCallback or PasswordCallback
		 */
		public void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException
		{
			for (int i = 0; i < callbacks.length; i++)
			{
				if (callbacks[i] instanceof TextOutputCallback)
				{
					if (M_log.isDebugEnabled()) M_log.debug("SakaiCallbackHandler: TextOutputCallback");
				}

				else if (callbacks[i] instanceof NameCallback)
				{
					NameCallback nc = (NameCallback) callbacks[i];

					String result = getId();
					if (result.equals(""))
					{
						result = nc.getDefaultName();
					}

					nc.setName(result);
				}

				else if (callbacks[i] instanceof PasswordCallback)
				{
					PasswordCallback pc = (PasswordCallback) callbacks[i];
					pc.setPassword(getPw());
				}

				else if (callbacks[i] instanceof ConfirmationCallback)
				{
					if (M_log.isDebugEnabled()) M_log.debug("SakaiCallbackHandler: ConfirmationCallback");
				}

				else
				{
					throw new UnsupportedCallbackException(callbacks[i], "SakaiCallbackHandler: Unrecognized Callback");
				}
			}

		} // handle

		void setId(String id)
		{
			m_id = id;

		} // setId

		private String getId()
		{
			return m_id;

		} // getid

		void setPw(String pw)
		{
			m_pw = pw;

		} // setPw

		private char[] getPw()
		{
			return m_pw.toCharArray();

		} // getPw

	} // SakaiCallbackHandler

	/**
	 * {@inheritDoc}
	 */
	public boolean authenticateWithProviderFirst(String id)
	{
		return false;
	}

	/**
	 * <p>
	 * Helper class for storing user data in an in-memory cache
	 * </p>
	 */
	class UserData
	{

		String id;

		String hpw;

		long timeStamp;

		/**
		 * @return Returns the id.
		 */
		public String getId()
		{
			return id;
		}

		/**
		 * @param id
		 *        The id to set.
		 */
		public void setId(String id)
		{
			this.id = id;
		}

		/**
		 * @param hpw
		 *        hashed pw to put in.
		 */
		public void setHpw(String hpw)
		{
			this.hpw = hpw;
		}

		/**
		 * @return Returns the hashed password.
		 */

		public String getHpw()
		{
			return hpw;
		}

		/**
		 * @return Returns the timeStamp.
		 */
		public long getTimeStamp()
		{
			return timeStamp;
		}

		/**
		 * @param timeStamp
		 *        The timeStamp to set.
		 */
		public void setTimeStamp(long timeStamp)
		{
			this.timeStamp = timeStamp;
		}

	} // UserData class

	/**
	 * <p>
	 * Hash string for storage in a cache using SHA
	 * </p>
	 * 
	 * @param UTF-8
	 *        string
	 * @return encoded hash of string
	 */

	private synchronized String encodeSHA(String plaintext)
	{

		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(plaintext.getBytes("UTF-8"));
			byte raw[] = md.digest();
			String hash = new String(Base64.encodeBase64(raw));
			return hash;
		}
		catch (Exception e)
		{
			M_log.warn("encodeSHA(): exception: " + e);
			return null;
		}
	} // encodeSHA

} // KerberosUserDirectoryProvider

