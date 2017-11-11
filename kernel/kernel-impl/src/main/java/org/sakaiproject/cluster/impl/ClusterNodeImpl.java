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
