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
import org.sakaiproject.thread_local.api.ThreadLocalManager;

public class JCRServiceImpl implements JCRService
{
	private static final Log log = LogFactory.getLog(JCRServiceImpl.class);

	public static final String DEFAULT_WORKSPACE = "sakai";

	/**
	 * The injected 170 repository
	 */
	private RepositoryBuilder repositoryBuilder = null;

	private ThreadLocal<SessionHolder> sessionHolder = new ThreadLocal<SessionHolder>();

	private Credentials repositoryCredentials;

	private boolean requestScope = true;

	private ThreadLocalManager threadLocalManager;

	private boolean enabled = false;

	private ServerConfigurationService serverConfigurationService;

	public void init()
	{
		enabled = serverConfigurationService.getBoolean("jcr.experimental",false);
		if (!enabled ) {
			log.info("JCR Service is not enabled");
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
			if (threadLocalManager == null)
			{
				log.error("threadLocalManager has not been set ");
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
			}

		}
		log.info("JCR Service passed init ");

	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		if ( !enabled ) return;
		repositoryBuilder.destroy();
		log.info("destroy()");
	}

	public Session getSession() throws LoginException, RepositoryException
	{
		if ( !enabled ) return null;
		return login();
	}

	public Session login() throws LoginException, RepositoryException
	{
		if ( !enabled ) return null;
		Session session = null;
		if (requestScope)
		{

			SessionHolder sh = (SessionHolder) threadLocalManager.get("jcrsession");
			if (sh == null)
			{
				long t1 = System.currentTimeMillis();
				sh = new SessionHolder(repositoryBuilder, repositoryCredentials, DEFAULT_WORKSPACE);
				threadLocalManager.set("jcrsession", sh);
                                if ( log.isDebugEnabled() ) 
				     log
						.debug("Session Start took " + (System.currentTimeMillis() - t1)
								+ "ms");
			}
			session = sh.getSession();
		}
		else
		{
			SessionHolder sh = sessionHolder.get();
			if (sh == null)
			{
				sh = new SessionHolder(repositoryBuilder, repositoryCredentials, DEFAULT_WORKSPACE);
				sessionHolder.set(sh);
			}
			session = sh.getSession();
		}
		return session;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.jcr.api.JCRService#logout()
	 */
	public void logout() throws LoginException, RepositoryException
	{
		if ( !enabled ) return;
		// this  will cause an unbind
		threadLocalManager.set("jcrsession",null);
	}

	public Credentials getRepositoryCredentials()
	{
		return repositoryCredentials;
	}

	public void setRepositoryCredentials(Credentials repositoryCredentials)
	{
		this.repositoryCredentials = repositoryCredentials;
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
	 * @return the threadLocalManager
	 */
	public ThreadLocalManager getThreadLocalManager()
	{
		return threadLocalManager;
	}

	/**
	 * @param threadLocalManager
	 *        the threadLocalManager to set
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager)
	{
		this.threadLocalManager = threadLocalManager;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.JCRService#getRepository()
	 */
	public Repository getRepository()
	{
		return repositoryBuilder.getInstance();
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.jcr.api.JCRService#setCurrentSession(javax.jcr.Session)
	 */
	public Session setSession(Session session) 
	{
		if ( !enabled ) return null;
		Session currentSession = null;
		if (requestScope)
		{
			SessionHolder sh = (SessionHolder) threadLocalManager.get("jcrsession");
			if (sh != null)
			{
				
				currentSession = sh.getSession();
				sh.keepLoggedIn();
			} 
			if ( session == null ) {
				threadLocalManager.set("jcrsession", null);
			} else {
				sh = new SessionHolder(session);
				threadLocalManager.set("jcrsession", sh);
			}
		}
		else
		{
			SessionHolder sh = sessionHolder.get();
			if (sh != null)
			{
				currentSession = sh.getSession();
			} 
			if ( session == null ) {
				sessionHolder.set( null);
			} else {
				sh = new SessionHolder(session);
				sessionHolder.set( sh);
			}
		}
		return currentSession;
	}


	public boolean needsMixin(Node node, String mixin) throws RepositoryException {
		return true;
		//! node.getSession().getWorkspace().getNodeTypeManager().getNodeType(node.getPrimaryNodeType().getName()).isNodeType(mixin);
	} 	
	
	public boolean hasActiveSession()
	{
		if ( !enabled ) return false;
		Session session = null;
		if (requestScope)
		{

			SessionHolder sh = (SessionHolder) threadLocalManager.get("jcrsession");
			if ( sh != null ) {
				session = sh.getSession();
			}
		}
		else
		{
			SessionHolder sh = sessionHolder.get();
			if ( sh != null ) {
				session = sh.getSession();
			}
		}
		return (session != null);
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
