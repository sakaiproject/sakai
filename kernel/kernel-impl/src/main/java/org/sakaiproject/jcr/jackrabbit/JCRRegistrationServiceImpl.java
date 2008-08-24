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

import java.io.InputStream;

import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.jcr.api.JCRRegistrationService;
import org.sakaiproject.jcr.jackrabbit.sakai.SakaiJCRCredentials;

/**
 * @author Steve Githens
 */
public class JCRRegistrationServiceImpl implements JCRRegistrationService
{

	private static final Log log = LogFactory.getLog(JCRRegistrationServiceImpl.class);

	private RepositoryBuilder repositoryBuilder;
	
	private boolean enabled = false;

	private ServerConfigurationService serverConfigurationService;
	
	public void init() {
		enabled = serverConfigurationService.getBoolean("jcr.experimental",false);
	}

	public void registerNamespace(String prefix, String uri)
	{
		if ( !enabled ) return;
		Session s = null;
		SakaiJCRCredentials system = new SakaiJCRCredentials();
		Repository repository = repositoryBuilder.getInstance();
		try
		{
			s = repository.login(system);
		}
		catch (LoginException e)
		{
			throw new SecurityException("Failed to login to Sakai repository", e);
		}
		catch (RepositoryException e)
		{
			throw new RuntimeException("Failed to login to Sakai repository", e);
		}

		Workspace w = s.getWorkspace();
		NamespaceRegistry reg;
		try
		{
			reg = w.getNamespaceRegistry();
		}
		catch (RepositoryException e)
		{
			throw new RuntimeException(
					"Failed to get workspace namespace registry for workspace: "
							+ w.getName(), e);
		}

		try
		{
			reg.getPrefix(uri);
			// if we get to this point the namespace already exists
		}
		catch (NamespaceException e)
		{
			try
			{
				reg.registerNamespace(prefix, uri);
			}
			catch (RepositoryException ex)
			{
				throw new RuntimeException(
						"Failed to register additional namespace prefix (" + prefix
								+ ") with uri (" + uri + ") in workspace: " + w.getName(),
						ex);
			}
		}
		catch (RepositoryException ex)
		{
			throw new RuntimeException("Failed to lookup namespace prefix ("
					+ prefix + ") with uri (" + uri + ") in workspace: " + w.getName(),
					ex);
		}
		finally
		{
			try
			{
				s.logout();
			}
			finally
			{
			}
		}

	}

	public void registerNodetypes(InputStream xml)
	{
		if ( !enabled ) return;
		Session s = null;
		try
		{
			SakaiJCRCredentials system = new SakaiJCRCredentials();
			Repository repository = repositoryBuilder.getInstance();
			s = repository.login(system);
			Workspace w = s.getWorkspace();
			NodeTypeManagerImpl ntm = (NodeTypeManagerImpl) w.getNodeTypeManager();
			ntm.registerNodeTypes(xml, "text/xml");
		}
		catch (Exception e)
		{
			log.info("Error Registering Additional JCR NameSpaces/Nodetypes "
					+ e.getMessage());
		}
		finally
		{
			try
			{
				s.logout();
			}
			finally
			{
			}
		}

	}

	/**
	 * @return the repositoryBuilder
	 */
	public RepositoryBuilder getRepositoryBuilder()
	{
		return repositoryBuilder;
	}

	/**
	 * @param repositoryBuilder
	 *        the repositoryBuilder to set
	 */
	public void setRepositoryBuilder(RepositoryBuilder repositoryBuilder)
	{
		this.repositoryBuilder = repositoryBuilder;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
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
