/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.user.impl;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.util.IPAddrUtil;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.AuthenticationMissingException;
import org.sakaiproject.user.api.AuthenticationUnknownException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.ExternalTrustedEvidence;
import org.sakaiproject.user.api.IdPwEvidence;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <p>
 * An Authentication component working with the UserDirectoryService.
 * </p>
 */
@Slf4j
public abstract class UserAuthnComponent implements AuthenticationManager
{
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();
	
	protected abstract AuthenticationCache authenticationCache();


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		log.info("init()");
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: AuthenticationManager
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Authentication authenticate(Evidence e) throws AuthenticationException
	{
		if (e instanceof IdPwEvidence)
		{
			IdPwEvidence evidence = (IdPwEvidence) e;

			// reject null or blank
			if ((evidence.getPassword() == null) || (evidence.getPassword().trim().length() == 0)
					|| (evidence.getIdentifier() == null) || (evidence.getIdentifier().trim().length() == 0))
			{
				throw new AuthenticationException("Invalid Login: Either identifier or password empty.");
			}
			
			// Check the cache. If repeat authentication failures are being throttled,
			// an immediate AuthenticationException might be thrown here.
			Authentication rv = authenticationCache().getAuthentication(evidence.getIdentifier(), evidence.getPassword());
			if (rv != null) {
				return rv;
			}

			// the evidence id must match a defined User
			User user = userDirectoryService().authenticate(evidence.getIdentifier(), evidence.getPassword());
			if (user == null)
			{
				authenticationCache().putAuthenticationFailure(evidence.getIdentifier(), evidence.getPassword());
				throw new AuthenticationException("Invalid Login: Either user not found or password incorrect.");
			}

			// Check to see if the user account is disabled
			String disabled = user.getProperties().getProperty("disabled");
			if (disabled != null && "true".equals(disabled))
			{
				throw new AuthenticationException("Account Disabled: The user's authentication has been disabled");
			}

			// Check optional whitelist for this account
			String whitelist = user.getProperties().getProperty("ip-whitelist");
			if (whitelist != null && !whitelist.isEmpty() && !IPAddrUtil.matchIPList(whitelist, evidence.getRemoteAddr())) {
				throw new AuthenticationException("Authentication refused: The user may only authenticate from whitelisted addresses");
			}

			rv = new org.sakaiproject.util.Authentication(user.getId(), user.getEid());
			
			// Cache the authentication.
			authenticationCache().putAuthentication(evidence.getIdentifier(), evidence.getPassword(), rv);
			
			return rv;
		}

		else if (e instanceof ExternalTrustedEvidence)
		{
			ExternalTrustedEvidence evidence = (ExternalTrustedEvidence) e;

			// reject null or blank
			if ((evidence.getIdentifier() == null) || (evidence.getIdentifier().trim().length() == 0))
			{
				throw new AuthenticationException("Invalid Login: Identifier empty.");
			}

			// accept, so now lookup the user in our database.
			try
			{
				User user = userDirectoryService().getUserByAid(evidence.getIdentifier());
				String disabled = user.getProperties().getProperty("disabled");
				if (disabled != null && "true".equals(disabled))
				{
					throw new AuthenticationException("Account Disabled: The users authentication has been disabled");
				}
				Authentication rv = new org.sakaiproject.util.Authentication(user.getId(), user.getEid());
				return rv;
			}
			catch (UserNotDefinedException ex)
			{
				// reject if the user is not defined
				// TODO: create the user record here?
				throw new AuthenticationMissingException("User '" + evidence.getIdentifier() + "' not defined", e);
			}
		}

		else
		{
			throw new AuthenticationUnknownException(e.toString());
		}
	}
}
