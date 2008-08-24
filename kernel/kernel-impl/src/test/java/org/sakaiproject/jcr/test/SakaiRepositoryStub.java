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

package org.sakaiproject.jcr.test;

import java.util.Properties;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.WorkspaceImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.test.RepositoryStub;
import org.apache.jackrabbit.test.RepositoryStubException;
import org.sakaiproject.jcr.api.JCRService;
import org.sakaiproject.jcr.jackrabbit.RepositoryBuilder;
import org.sakaiproject.jcr.jackrabbit.sakai.SakaiJCRCredentials;
import org.sakaiproject.jcr.test.mock.MockComponentManager;

/**
 * @author ieb
 */
public class SakaiRepositoryStub extends RepositoryStub
{

	private static final Log log = LogFactory.getLog(SakaiRepositoryStub.class);

	private Repository repository;

	private String nodeTypeConfiguration = "/org/sakaiproject/jcr/test/TestNodeTypes.xml";

	/**
	 * @param arg0
	 */
	public SakaiRepositoryStub(Properties props)
	{
		super(props);
		MockComponentManager componentManager = new MockComponentManager();
		componentManager.init();
		RepositoryBuilder rb = (RepositoryBuilder) MockComponentManager
				.get(JCRService.class.getName() + ".repositoryBuilder");
		if (rb == null)
		{
			log.error("No repositroy Object found in the component manager "
					+ JCRService.class.getName() + ".repositoryBuilder");
			throw new RuntimeException("No repositroy Object found in the component manager "
					+ JCRService.class.getName() + ".repositoryBuilder");
		}
		repository = rb.getInstance();
		if ( repository == null ) {
			log.error("Repositroy is not available");
			throw new RuntimeException("Repositroy is available");
			
		}
		try
		{
			Session s = repository.login(new SakaiJCRCredentials());
			WorkspaceImpl w = (WorkspaceImpl) s.getWorkspace();
			try
			{
				NodeTypeManagerImpl ntm = (NodeTypeManagerImpl) w.getNodeTypeManager();
				ntm.registerNodeTypes(this.getClass().getResourceAsStream(
						nodeTypeConfiguration), "text/xml");
			}
			catch (Exception ex)
			{
				log.info("Loading Types, OK for second starts " + ex.getMessage());
			}
			try
			{
				w.createWorkspace("test");
			}
			catch (Exception ex)
			{
				log.info("Creating Workspace Failed " + ex.getMessage());
			}
			if (!s.getRootNode().hasNode("testdata"))
			{
				s.getRootNode().addNode("testdata", "nt:unstructured");
				log.info("Added Test Data Node Under Root");
			}
			else
			{
				log.info("Added Test Data Node Under Already present");

			}
			s.save();
			s.logout();
		}
		catch (Exception ex)
		{
			log.error("Failed to add root node", ex);
		}

		log.info("Repository Configured and Setup");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jackrabbit.test.RepositoryStub#getRepository()
	 */
	@Override
	public Repository getRepository() throws RepositoryStubException
	{
		return repository;
	}

}
