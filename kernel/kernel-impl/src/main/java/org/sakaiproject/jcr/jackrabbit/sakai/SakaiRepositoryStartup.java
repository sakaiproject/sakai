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

import java.util.Arrays;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.WorkspaceImpl;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.jcr.api.internal.StartupAction;
import org.sakaiproject.jcr.jackrabbit.JCRServiceImpl;

/**
 * Perfoms basic repository startup.
 * 
 * @author ieb
 */
public class SakaiRepositoryStartup implements StartupAction
{
	private static final Log log = LogFactory.getLog(SakaiRepositoryStartup.class);
	private ServerConfigurationService serverConfigurationService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.jcr.api.internal.StartupAction#startup(javax.jcr.Session)
	 */
	public void startup(Session s)
	{
		boolean enabled = serverConfigurationService.getBoolean("jcr.experimental",false);
		if ( !enabled  ) return;

		try
		{
			WorkspaceImpl workspace = (WorkspaceImpl) s.getWorkspace();
			List<String> existingWorkspaces = Arrays.asList(workspace
					.getAccessibleWorkspaceNames());
			if (!existingWorkspaces.contains(JCRServiceImpl.DEFAULT_WORKSPACE))
			{
				if (log.isDebugEnabled()) log.debug("Creating Workspace Sakai ");
				workspace.createWorkspace(JCRServiceImpl.DEFAULT_WORKSPACE);
				log.info("Created default Sakai Jackrabbit Workspace: "
						+ JCRServiceImpl.DEFAULT_WORKSPACE);
			}
			if (!s.getRootNode().hasNode("testdata"))
			{
				if (log.isDebugEnabled())
				{
					log.debug("Creating Test Data ");
				}
				s.getRootNode().addNode("testdata", "nt:unstructured");
				if (log.isDebugEnabled())
				{
					log.debug("Added Test Data Node Under Root");
				}
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("Added Test Data Node Under Already present");
				}
			}
		}
		catch (RepositoryException ex)
		{
			throw new IllegalStateException(
					"Failed to add Sakai Jackrabbit JCR root node, JCR workspace/repository failure",
					ex);
		}
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
