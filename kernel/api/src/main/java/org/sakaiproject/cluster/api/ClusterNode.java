/**
 * Copyright (c) 2003-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.cluster.api;

import java.util.Date;

/**
 * This holds information about a node in the cluster.
 */
public interface ClusterNode {

    /**
     * Gets the status of a node.
     * @return The current node status, or {@link org.sakaiproject.cluster.api.ClusterService.Status#UNKNOWN}
     * if the status isn't known.
     */
    ClusterService.Status getStatus();

    /**
     * Gets the server ID of a node.
     * @return The server ID.
     */
    String getServerId();

    /**
     * Gets when the status of the node was last updated.
     * @return The date when the status of the node was last updated.
     */
    Date getUpdated();
}
