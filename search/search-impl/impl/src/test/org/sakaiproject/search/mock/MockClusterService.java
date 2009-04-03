/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.search.mock;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * @author ieb
 *
 */
public class MockClusterService implements ClusterService
{

	List<String> servers = new ArrayList<String>();
	List<ServerConfigurationService> configs = new ArrayList<ServerConfigurationService>();
	public void init() {
		for ( ServerConfigurationService sc : configs ) {
			servers.add(sc.getServerId());
		}
	}
	
	public void addServerConfigurationService(ServerConfigurationService sc ) {
		configs.add(sc);
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.cluster.api.ClusterService#getServers()
	 */
	public List getServers()
	{
		return servers;
	}

}
