/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.cluster.api;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * ClusterService keeps track of running Sakai instances in a Sakai application server cluster.
 * </p>
 */
public interface ClusterService
{
	/**
	 * Get the status of the current node.
	 * @return A Status.
	 */
	Status getStatus();

	/**
	 * Status of this node in the cluster.
	 */
	enum Status
	{
		/**
		 * The node is starting up, but isn't yet ready to handle requests.
		 */
		STARTING,
		/**
		 * The node is running and can handle requests.
		 */
		RUNNING,
		/**
		 * The node is shutting down and shouldn't be sent new sessions.
		 */
		CLOSING,
		/**
		 * The node has been stopped and should have no requests sent to it.
		 * It will disappear from the list of nodes shortly afterwards.
		 */
		STOPPING,
		/**
		 * We don't know what we're doing or something is horribly wrong.
		 */
		UNKNOWN
	}

	/**
	 * Event for halting a node in the cluster.
	 */
	String EVENT_CLOSE = "cluster.close";

	/**
	 * Event for requesting a node run again.
	 */
	String EVENT_RUN = "cluster.run";

	/**
	 * Access a List of server ids active in the cluster.
	 * 
	 * @return The List (String) of server ids active in the cluster.
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerIdInstance()
	 */
	List<String> getServers();

	/**
	 * Get the statuses of the servers in the cluster.
	 * @return A Map of the servers with the value being the server status.
	 */
	Map<String, ClusterNode> getServerStatus();

	/**
	 * Marks a server as being closed. This prevents new sessions from being started.
	 *
	 * @param serverId The server ID.
	 * @param close If <code>true</code> mark the server as being in closed.
	 * @throws java.lang.IllegalArgumentException If the serverId doesn't exist.
	 */
	void markClosing(String serverId, boolean close);


}
