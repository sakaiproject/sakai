/**********************************************************************************
*
* $Header: /cvs/sakai2/providers/kerberos/src/java/org/sakaiproject/component/kerberos/user/KerberosUserDirectoryProvider.java,v 1.4 2005/06/02 14:50:09 csev.umich.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

// package
package org.sakaiproject.component.kerberos.user;

// imports
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.legacy.user.UserDirectoryProvider;
import org.sakaiproject.service.legacy.user.UserEdit;
import org.sakaiproject.util.java.StringUtil;

/**
* <p>KerberosUserDirectoryProvider is UserDirectoryProvider that provides a user for any
* person known to Kerberos.</p>
* <p>Note: Java Runtime must be setup properly:<ul>
* <li>{java_home}/jre/lib/security/java.security must have this line:<br/>
*   login.config.url.1=file:${java.home}/lib/security/jaas.conf</li>
* <li>the file "jaas.conf" must be placed into that same directory.</li>
* <li>the file "krb5.conf" must be placed into that same directory.</li></ul></p>
*
* <p>For more information on configuration, see the README.txt file<p>
*
* @author University of Michigan, Sakai Software Development Team
* @version $Revision: 1.4 $
*/
public class KerberosUserDirectoryProvider
	implements UserDirectoryProvider
{
	/*******************************************************************************
	* Dependencies and their setter methods
	*******************************************************************************/

	/** Dependency: logging service */
	protected Logger m_logger = null;

	/**
	 * Dependency: logging service.
	 * @param service The logging service.
	 */
	public void setLogger(Logger service)
	{
		m_logger = service;
	}

	/*******************************************************************************
	* Configuration options and their setter methods
	*******************************************************************************/

	/** Configuration: Domain */
	protected String m_domain = "umich.edu";

	/**
	 * Configuration: Domain Name (for E-Mail Addresses)
	 * @param domain The domain in the form of "umich.edu"
	 */
	public void setDomain(String domain)
	{
		m_domain = domain;
	}

	/** Configuration: LoginContext */
	protected String m_logincontext = "KerberosAuthentication";

	/**
	 * Configuration: Authentication Name
	 * @param logincontext The context to be used from the login.config file - default "KerberosAuthentication"
	 */
	public void setLoginContext(String logincontext)
	{
		m_logincontext = logincontext;
	}

	/** Configuration:  RequireLocalAccount */
	protected boolean m_requirelocalaccount = true;

	/**
	 * Configuration: Require Local Account
	 * @param requirelocalaccount Determine if a local account is required for user to authenticate - default "true"
	 */
	public void setRequireLocalAccount(Boolean requirelocalaccount)
	{
		m_requirelocalaccount = requirelocalaccount.booleanValue();
	}

	/** Configuration:  KnownUserMsg */
	protected String m_knownusermsg = "Integrity check on decrypted field failed";

	/**
	 * Configuration: Kerberos Error Message
	 * @param knownusermsg Start of error returned for bad logins by known users - default is from RFC 1510
	 */
	public void setKnownUserMsg(String knownusermsg)
	{
		m_knownusermsg = knownusermsg;
	}

	/*******************************************************************************
	* Init and Destroy
	*******************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			m_logger.info(this +".init() Domain=" + m_domain + " LoginContext=" + m_logincontext + " RequireLocalAccount=" + m_requirelocalaccount + " KnownUserMsg=" + m_knownusermsg);
		}
		catch (Throwable t)
		{
			m_logger.warn(this +".init(): ", t);
		}

	} // init

	/**
	* Returns to uninitialized state.
	*
	* You can use this method to release resources thet your Service
	* allocated when Turbine shuts down.
	*/
	public void destroy()
	{
		m_logger.info(this +".destroy()");

	} // destroy

	/*******************************************************************************
	* UserDirectoryProvider implementation
	*******************************************************************************/

	/**
	* See if a user by this id exists.
	* @param userId The user id string.
	* @return true if a user by this id exists, false if not.
	*/
	public boolean userExists(String userId)
	{
		if (m_requirelocalaccount) return false;

		boolean knownKerb = userKnownToKerberos(userId);
		m_logger.info(this + ".userExists: " + userId + " Kerberos: " + knownKerb);
		return knownKerb;
	}	// userExists

	/**
	* Access a user object.  Update the object with the information found.
	* @param edit The user object (id is set) to fill in.
	* @return true if the user object was found and information updated, false if not.
	*/
	public boolean getUser(UserEdit edit)
	{
		if (!userExists(edit.getId())) return false;

		edit.setEmail(edit.getId() + "@" + m_domain);
		edit.setType("kerberos");

		return true;
	}	// getUser

	/**
	* Find a user object who has this email address. Update the object with the information found.
	* @param email The email address string.
	* @return true if the user object was found and information updated, false if not.
	*/
	public boolean findUserByEmail(UserEdit edit, String email)
	{
		// lets not get messed up with spaces or cases
		String test = email.toLowerCase().trim();

		// if the email ends with "umich.edu" (even if it's from somebody@krusty.si.umich.edu) 
		// use the local part as a user id.

		if (!test.endsWith(m_domain)) return false;

		// split the string once at the first "@"
		String parts[] = StringUtil.splitFirst(test, "@");
		edit.setId(parts[0]);
		return getUser(edit);

	}	// findUserByEmail

	/**
	 * Authenticate a user / password.
	 * If the user edit exists it may be modified, and will be stored if...
	 * @param id The user id.
	 * @param edit The UserEdit matching the id to be authenticated (and updated) if we have one.
	 * @param password The password.
	 * @return true if authenticated, false if not.
	 */
	public boolean authenticateUser(String userId, UserEdit edit, String password)
	{
		boolean authKerb = authenticateKerberos(userId, password);
		return authKerb;
	}	// authenticateUser

	/**
	 * {@inheritDoc}
	 */
	public void destroyAuthentication()
	{

	}

	/**
	 * Will this provider update user records on successfull authentication?
	 * If so, the UserDirectoryService will cause these updates to be stored.
	 * @return true if the user record may be updated after successfull authentication, false if not.
	 */
	public boolean updateUserAfterAuthentication()
	{
		return false;
	}

	/*******************************************************************************
	* Kerberos stuff
	*******************************************************************************/

	/**
	* Authenticate the user id and pw with kerberos.
	* @param user The user id.
	* @param password the user supplied password.
	* @return true if successful, false if not.
	*/
	protected boolean authenticateKerberos(String user, String pw)
	{
		// assure some length to the password
		if ((pw == null) || (pw.length () == 0)) return false;

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
			if (m_logger.isDebugEnabled())
				m_logger.debug(this + ".authenticateKerberos(): " + le.toString());
			return false;
		}
		catch (SecurityException se)
		{
			if (m_logger.isDebugEnabled())
				m_logger.debug(this + ".authenticateKerberos(): " + se.toString());
			return false;
		}

		try
		{
			// attempt authentication
			lc.login();
			lc.logout();

			if (m_logger.isDebugEnabled())
				m_logger.debug(this + ".authenticateKerberos(" + user + ", pw): Kerberos auth success");

			return true;
		}
		catch (LoginException le)
		{
			if (m_logger.isDebugEnabled())
				m_logger.debug(this + ".authenticateKerberos(" + user + ", pw): Kerberos auth failed: " + le.toString());

			return false;
		}

	}   // authenticateKerberos

	/**
	* Check if the user id is known to kerberos.
	* @param user The user id.
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
			if (m_logger.isDebugEnabled())
				m_logger.debug(this + ".useKnownToKerberos(): " + le.toString());
			return false;
		}
		catch (SecurityException se)
		{
			if (m_logger.isDebugEnabled())
				m_logger.debug(this + ".useKnownToKerberos(): " + se.toString());
			return false;
		}

		try
		{
			// attempt authentication
			lc.login();
			lc.logout();

			if (m_logger.isDebugEnabled())
				m_logger.debug(this + ".useKnownToKerberos(" + user + "): Kerberos auth success");

			return true;
		}
		catch (LoginException le)
		{
			String msg = le.getMessage();
			
			// if this is the message, the user was good, the password was bad
			if (msg.startsWith(m_knownusermsg))
			{
				if (m_logger.isDebugEnabled())
					m_logger.debug(this + ".userKnownToKerberos(" + user + "): Kerberos user known (bad pw)");

				return true;
			}

			// the other message is when the user is bad:
			if (m_logger.isDebugEnabled())
				m_logger.debug(this + ".userKnownToKerberos(" + user + "): Kerberos user unknown or invalid");

			return false;
		}

	}   // userKnownToKerberos

	/**
	* Inner Class SakaiCallbackHandler
	*
	* Get the user id and password information for authentication purpose.
	* This can be used by a JAAS application to instantiate a CallbackHandler.
	* @see javax.security.auth.callback
	*/
	protected class SakaiCallbackHandler
		implements CallbackHandler
	{
		private String m_id;
		private String m_pw;
		
		/** constructor */
		public SakaiCallbackHandler ()
		{
			m_id = new String ("");
			m_pw = new String ("");
			
		}   // SakaiCallbackHandler
		
		/**
		 * Handles the specified set of callbacks.
		 *
		 * @param callbacks the callbacks to handle
		 * @throws IOException if an input or output error occurs.
		 * @throws UnsupportedCallbackException if the callback is not an
		 * instance of NameCallback or PasswordCallback
		 */
		public void handle (Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException
		{
			ConfirmationCallback confirmation = null;

			for (int i = 0; i < callbacks.length; i++)
			{
				if (callbacks[i] instanceof TextOutputCallback)
				{
					if (m_logger.isDebugEnabled())
						m_logger.debug("SakaiCallbackHandler: TextOutputCallback");
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
					if (m_logger.isDebugEnabled())
						m_logger.debug("SakaiCallbackHandler: ConfirmationCallback");
				}

				else
				{
					throw new UnsupportedCallbackException (callbacks[i], "SakaiCallbackHandler: Unrecognized Callback");
				}
			}

		}   // handle

		void setId(String id)
		{
			m_id = id;
			
		}   // setId
		
		private String getId()
		{
			return m_id;
			
		}   // getid
		
		void setPw(String pw)
		{
			m_pw = pw;
			
		}   // setPw
		
		private char[] getPw()
		{
			return m_pw.toCharArray();
			
		}   // getPw

	}   // SakaiCallbackHandler

}	// KerberosUserDirectoryProvider

/**********************************************************************************
*
* $Header: /cvs/sakai2/providers/kerberos/src/java/org/sakaiproject/component/kerberos/user/KerberosUserDirectoryProvider.java,v 1.4 2005/06/02 14:50:09 csev.umich.edu Exp $
*
**********************************************************************************/



