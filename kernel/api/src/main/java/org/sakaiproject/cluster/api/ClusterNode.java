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
