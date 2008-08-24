/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.jackrabbit;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.jcr.api.JCRService;

/* This is mostly the same as the JCRServiceImpl, except you are never bound to a 
 * particular thread.  You will need to keep track of your session, and use the logout
 * methods available on JSR-170. 
 */
public class UnboundJCRServiceImpl implements JCRService
{

	private static final Log log = LogFactory.getLog(UnboundJCRServiceImpl.class);

	public static final String DEFAULT_WORKSPACE = "sakai";

	/**
	 * The injected 170 repository
	 */
	private RepositoryBuilder repositoryBuilder = null;

	private Credentials repositoryCredentials;

	private boolean requestScope = true;

	private boolean enabled = false;

	private ServerConfigurationService serverConfigurationService;

	public void init()
	{
		enabled   = serverConfigurationService.getBoolean("jcr.experimental",false);
		if (!enabled ) {
			return;
		}

		boolean error = false;
		try
		{
			if (repositoryBuilder == null)
			{
				log.error("Repository has not been set ");
				error = true;
			}
			if (repositoryCredentials == null)
			{
				log.error("Credentials has not been set ");
				error = true;
			}
		}
		catch (Throwable t)
		{
			log.error("Failed init(): ", t);
			error = true;
		}
		finally
		{
			if (error)
			{
				throw new RuntimeException(
						"Fatal error initialising JCRService... (see previous logged ERROR for details)");
				// System.exit is not a good idea to use, it causes everything
				// to die instead of shutting down -AZ
				// System.exit(-1);
			}
		}
		log.info("JCR Service initialised...");
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		// repositoryBuilder.destroy();
		log.info("destroy()");
	}

	public Session getSession() throws LoginException, RepositoryException
	{
		return login();
	}

	public Session login() throws LoginException, RepositoryException
	{
		Repository repository = repositoryBuilder.getInstance();
		return repository.login(repositoryCredentials);
	}

	/*
	 * See JSR-170 for how to use session.logout()
	 */
	public void logout() throws LoginException, RepositoryException
	{
	}

	public Credentials getRepositoryCredentials()
	{
		return repositoryCredentials;
	}

	public void setRepositoryCredentials(Credentials repositoryCredentials)
	{
		this.repositoryCredentials = repositoryCredentials;
	}

	public void dump(String id)
	{

	}

	/**
	 * @return the requestScope
	 */
	public boolean isRequestScope()
	{
		return requestScope;
	}

	/**
	 * @param requestScope
	 *        the requestScope to set
	 */
	public void setRequestScope(boolean requestScope)
	{
		this.requestScope = requestScope;
	}

	/**
	 * @return the repositoryBuilder
	 */
	public RepositoryBuilder getRepositoryBuilder()
	{
		return repositoryBuilder;
	}

	public void setRepositoryBuilder(RepositoryBuilder repositoryBuilder)
	{
		this.repositoryBuilder = repositoryBuilder;
	}

	public Repository getRepository()
	{
		return repositoryBuilder.getInstance();
	}

	public Session setSession(Session session)
	{
		return session;
	}

	public boolean needsMixin(Node node, String mixin) throws RepositoryException
	{
		return true;
	}

	public boolean hasActiveSession()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.jcr.api.JCRService#isEnabled()
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}
}
