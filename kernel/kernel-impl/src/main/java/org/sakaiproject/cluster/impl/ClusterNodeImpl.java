package org.sakaiproject.cluster.impl;

import org.sakaiproject.cluster.api.ClusterNode;
import org.sakaiproject.cluster.api.ClusterService;

import java.util.Date;

/**
 * Simple immutable implementation of ClusterNode.
 */
public class ClusterNodeImpl implements ClusterNode {

    private final String serverId;
    private final ClusterService.Status status;
    private final Date updated;


    public ClusterNodeImpl(String serverId, ClusterService.Status status, Date updated) {
        this.serverId = serverId;
        this.status = status;
        this.updated = updated;
    }

    @Override
    public ClusterService.Status getStatus() {
        return status;
    }

    @Override
    public String getServerId() {
        return serverId;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }
}
