/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.jackrabbit.sakai;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.security.CredentialsCallback;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.jcr.jackrabbit.JCRAnonymousPrincipal;
import org.sakaiproject.jcr.jackrabbit.JCRSystemPrincipal;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.IdPwEvidence;

public class SakaiLoginModule implements LoginModule
{
	private static final String SAKAI_SYSTEM_USER = "sakaisystem";

	private static final String SAKAI_ANON_USER = ".anon";

	private static final Log log = LogFactory.getLog(SakaiLoginModule.class);

	private Subject subject;

	private CallbackHandler callbackHandler;

	private final Set principals = new HashSet();

	private UserDirectoryService userDirectoryService;
	
	private AuthenticationManager authenticationManager;
   public void setAuthenticationManager(AuthenticationManager authenticationManager) {
      this.authenticationManager = authenticationManager;
   }

	/**
	 * Constructor
	 */
	public SakaiLoginModule()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options)
	{
		this.subject = subject;
		this.callbackHandler = callbackHandler;

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean login() throws LoginException
	{
		// prompt for a user name and password
		if (callbackHandler == null)
		{
			throw new LoginException("no CallbackHandler available");
		}
		if (userDirectoryService == null)
		{
			userDirectoryService = getUserDirectoryService();
		}
      if (authenticationManager == null)
      {
         authenticationManager = getAuthenticationManager();
      }

		boolean authenticated = false;
		principals.clear();
		try
		{

			// Get credentials using a JAAS callback
			CredentialsCallback ccb = new CredentialsCallback();
			callbackHandler.handle(new Callback[] { ccb });
			Credentials creds = ccb.getCredentials();
			// Use the credentials to set up principals
			if (creds != null)
			{
				if (creds instanceof SimpleCredentials)
				{
					SimpleCredentials sc = (SimpleCredentials) creds;
					// authenticate

					User u = null;
					try {
                  Authentication auth = authenticationManager.authenticate( 
                        new IdPwEvidence( sc.getUserID(), new String(sc.getPassword()) ) );
                  u = userDirectoryService.getUser(auth.getUid());
               } catch (NullPointerException e) {
                  u = null;
               } catch (AuthenticationException e) {
                  u = null;
               } catch (UserNotDefinedException e) {
                  u = null;
               }
               // old way used UDS directly, no caching, new way above gets cached -AZ
//					User u = userDirectoryService.authenticate(sc.getUserID(),
//							new String(sc.getPassword()));
					if (u == null)
					{
						principals.add(new JCRAnonymousPrincipal(SAKAI_ANON_USER));
					}
					else
					{
						principals.add(new SakaiUserPrincipalImpl(u));
					}

					authenticated = true;
				}
				else if (creds instanceof SakaiJCRCredentials)
				{
					principals.add(new JCRSystemPrincipal(SAKAI_SYSTEM_USER));
					authenticated = true;
				}
			}
			else
			{
				// authenticated via Session or Sakai Wrapper
				User u = userDirectoryService.getCurrentUser();
				if (u == null)
				{
					principals.add(new JCRAnonymousPrincipal(SAKAI_ANON_USER));
				}
				else
				{
					principals.add(new SakaiUserPrincipalImpl(u));
				}
				authenticated = true;
			}
		}
		catch (java.io.IOException ioe)
		{
			throw new LoginException(ioe.toString());
		}
		catch (UnsupportedCallbackException uce)
		{
			throw new LoginException(uce.getCallback().toString() + " not available");
		}

		if (authenticated)
		{
			return !principals.isEmpty();
		}
		else
		{
			principals.clear();
			throw new FailedLoginException();
		}
	}

	/**
	 * @return
	 */
	protected UserDirectoryService getUserDirectoryService()
	{
		return (UserDirectoryService) ComponentManager.get(UserDirectoryService.class
				.getName());
	}

	protected AuthenticationManager getAuthenticationManager() {
	   return (AuthenticationManager) ComponentManager.get(AuthenticationManager.class.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean commit() throws LoginException
	{
		if (principals.isEmpty())
		{
			return false;
		}
		else
		{
			// add a principals (authenticated identities) to the Subject
			subject.getPrincipals().addAll(principals);
			return true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean abort() throws LoginException
	{
		if (principals.isEmpty())
		{
			return false;
		}
		else
		{
			logout();
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean logout() throws LoginException
	{
		subject.getPrincipals().removeAll(principals);
		principals.clear();
		return true;
	}
}
