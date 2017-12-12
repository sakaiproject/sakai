/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.component.kerberos.user;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * KerberosUserDirectoryProvider is a UserDirectoryProvider that authenticates usernames using Kerberos.
 * </p>
 * <p>
 * For more information on configuration, see the README.txt file
 * <p>
 */
@Slf4j
public class KerberosUserDirectoryProvider implements UserDirectoryProvider
{

	// Should we attempt ticket verification?
	private boolean m_verifyTicket;
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration options and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Configuration: Domain */
	protected String m_domain = null;

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
	
	/** Configuration: ServiceLoginContext */
	protected String m_servicelogincontext = "ServiceKerberosAuthentication";
	
	/**
	 * Configuration: Service Authentication Name
	 * 
	 * @param serviceLoginContext
	 *        The context for the service to be used from the login.config file - default "ServiceKerberosAuthentication" 
	 */
	public void setServiceLoginContext(String serviceLoginContext)
	{
		m_servicelogincontext = serviceLoginContext;
	}
	
	/** Configuration: ServicePrincipal */
	protected String m_serviceprincipal;

	/**
	 * Configuration: GSSAPI Service Principal
	 * 
	 * @param serviceprincipal
	 *        The name of the service principal for GSSAPI. Needs to be set.
	 */
	public void setServicePrincipal(String serviceprincipal) {
		this.m_serviceprincipal = serviceprincipal;
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

	/**
	 * Configuration: Cache TTL
	 * 
	 * @deprecated  No longer used. Use standard cache settings instead.
	 * @param cachettl
	 *        Time (in milliseconds) to cache authenticated usernames
	 */
	public void setCachettl(int cachettl)
	{
		log.warn(this + ".init(): Internal caching DEPRECATED -  Using standard cache settings instead.");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// Full paths only from the file
		String kerberoskrb5conf = ServerConfigurationService.getString("provider.kerberos.krb5.conf", null);
		String kerberosauthloginconfig = ServerConfigurationService.getString("provider.kerberos.auth.login.config", null);
		boolean kerberosshowconfig = ServerConfigurationService.getBoolean("provider.kerberos.showconfig", false);
		String sakaihomepath = System.getProperty("sakai.home");

		// if locations are configured in sakai.properties, use them in place of the current system locations
		// if the location specified exists and is readable, use full absolute path
		// otherwise, try file path relative to sakai.home
		// if files are readable use them, otherwise print warning and use system defaults
		if (kerberoskrb5conf != null)
		{
			if (new File(kerberoskrb5conf).canRead())
			{
				System.setProperty("java.security.krb5.conf", kerberoskrb5conf);
			}
			else if (new File(sakaihomepath, kerberoskrb5conf).canRead())
			{
				System.setProperty("java.security.krb5.conf", sakaihomepath + kerberoskrb5conf);
			}
			else
			{
				log.warn(this + ".init(): Cannot find krb5.conf at specified location - Using default rules for krb5.conf location.");
				kerberoskrb5conf = null;
			}
		}

		if (kerberosauthloginconfig != null)
		{

			if (new File(kerberosauthloginconfig).canRead())
			{
				System.setProperty("java.security.auth.login.config", kerberosauthloginconfig);
			}
			else if (new File(sakaihomepath, kerberosauthloginconfig).canRead())
			{
				System.setProperty("java.security.auth.login.config", sakaihomepath + kerberosauthloginconfig);
			}
			else
			{
				log.warn(this + ".init(): Cannot set kerberosauthloginconfig location");
				kerberosauthloginconfig = null;
			}
		}

		// Check if we're configured to verify service tickets.
		m_verifyTicket = (m_serviceprincipal != null && m_servicelogincontext != null);

		log.info(this + ".init()" + " Domain=" + m_domain + " LoginContext=" + m_logincontext + " RequireLocalAccount="
				+ m_requirelocalaccount + " KnownUserMsg=" + m_knownusermsg + " VerifyServiceTicket="+ m_verifyTicket );

		// show the whole config if set
		// system locations will read NULL if not set (system defaults will be used)
		if (kerberosshowconfig)
		{
			log.info(this + ".init()" + " SakaiHome=" + sakaihomepath + " SakaiPropertyKrb5Conf=" + kerberoskrb5conf
					+ " SakaiPropertyAuthLoginConfig=" + kerberosauthloginconfig + " SystemPropertyKrb5Conf="
					+ System.getProperty("java.security.krb5.conf") + " SystemPropertyAuthLoginConfig="
					+ System.getProperty("java.security.auth.login.config")
					+ " ServicePrincipal=" + m_serviceprincipal
					+ " ServiceLoginContext="+ m_servicelogincontext
			);
		}

		if (!m_requirelocalaccount && m_domain == null)
		{
			throw new IllegalStateException("If you don't require local accounts, you must set the domain for e-mail addresses. See docs/INSTALL.txt in the Kerberos provider source for more information.");
		}


	} // init

	/**
	 * Returns to uninitialized state. You can use this method to release resources that your Service allocated when Spring shuts down.
	 */
	public void destroy()
	{
		log.info(this + ".destroy()");

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
	public void getUsers(Collection<UserEdit> users)
	{
		for (Iterator<UserEdit> i = users.iterator(); i.hasNext();)
		{
			UserEdit user = i.next();
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
		if (m_requirelocalaccount) return false;
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
	 * Authenticate a user / password.
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
		try
		{
			JassAuthenticate jass;
			if (m_verifyTicket) {
				jass = new JassAuthenticate(m_serviceprincipal, m_servicelogincontext, m_logincontext);
			} else {
				jass = new JassAuthenticate(m_logincontext);
			}
			boolean authKerb = jass.attemptAuthentication(userId, password);
			return authKerb;
		}
		catch (Exception e)
		{
			log.warn("authenticateUser(): exception: ", e);
			return false;
		}
	} // authenticateUser

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Kerberos stuff
	 *********************************************************************************************************************************************************************************************************************************************************/

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
			CallbackHandler t = new UsernamePasswordCallback(user, pw);
			lc = new LoginContext(m_logincontext, t);
		}
		catch (LoginException le)
		{
			if (log.isDebugEnabled()) log.debug("useKnownToKerberos(): " + le.toString());
			return false;
		}
		catch (SecurityException se)
		{
			if (log.isDebugEnabled()) log.debug("useKnownToKerberos(): " + se.toString());
			return false;
		}

		try
		{
			// attempt authentication
			lc.login();
			lc.logout();

			if (log.isDebugEnabled()) log.debug("useKnownToKerberos(" + user + "): Kerberos auth success");

			return true;
		}
		catch (LoginException le)
		{
			String msg = le.getMessage();

			// if this is the message, the user was good, the password was bad
			if (msg.startsWith(m_knownusermsg))
			{
				if (log.isDebugEnabled()) log.debug("userKnownToKerberos(" + user + "): Kerberos user known (bad pw)");

				return true;
			}

			// the other message is when the user is bad:
			if (log.isDebugEnabled()) log.debug("userKnownToKerberos(" + user + "): Kerberos user unknown or invalid");

			return false;
		}

	} // userKnownToKerberos

	/**
	 * {@inheritDoc}
	 */
	public boolean authenticateWithProviderFirst(String id)
	{
		return false;
	}

} // KerberosUserDirectoryProvider

