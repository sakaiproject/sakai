/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.hierarchy.dao.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.sakaiproject.springframework.data.PersistableEntity;

/**
 * This represents a single authorization entry in the hierarchy system,
 * this is DROP dead simplistic and needs to probably be improved VASTLY
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Entity
@Table(name = "HIERARCHY_PERMS", indexes = {
        @Index(name = "HIER_PERM_USER", columnList = "userId"),
        @Index(name = "HIER_PERM_NODE", columnList = "nodeId"),
        @Index(name = "HIER_PERM_PERM", columnList = "permission")
})
public class HierarchyNodePermission implements Serializable, PersistableEntity<Long> {
        private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "hierarchy_perm_sequence")
    @SequenceGenerator(name = "hierarchy_perm_sequence", sequenceName = "HIERARCHY_PERM_ID_SEQ")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdOn", nullable = false)
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastModified", nullable = false)
    private Date lastModified;

    @Column(name = "userId", length = 99, nullable = false)
    private String userId;

    @Column(name = "nodeId", length = 255, nullable = false)
    private String nodeId;

    @Column(name = "permission", length = 255, nullable = false)
    private String permission;

    public HierarchyNodePermission() {} // default constructor needed for reflection

    public HierarchyNodePermission(String userId, String nodeId, String permission) {
        if (userId == null || "".equals(userId) 
                || nodeId == null || "".equals(nodeId)
                || permission == null || "".equals(permission)) {
            throw new IllegalArgumentException("None of the inputs can be null or blank: type=" + userId + ":id=" + nodeId + ":eid=" + permission);
        }
        this.userId = userId;
        this.nodeId = nodeId;
        this.permission = permission;
        this.id = null; // default to null
        this.createdOn = new Date();
        this.lastModified = this.createdOn;
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	@Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        if (!(obj instanceof HierarchyNodePermission))
            return false;
        else {
            HierarchyNodePermission castObj = (HierarchyNodePermission) obj;
            boolean eq = (this.id == null ? castObj.id == null : this.id.equals(castObj.id))
            && (this.userId == null ? false : this.userId.equals(castObj.userId))
            && (this.nodeId == null ? false : this.nodeId.equals(castObj.nodeId))
            && (this.permission == null ? false : this.permission.equals(castObj.permission));
            return eq;
        }
    }

    @Override
    public int hashCode() {
        String hashStr = this.getClass().getName() + ":" + this.id + ":" + this.userId + ":" + this.permission + ":" + this.nodeId;
        return hashStr.hashCode();
    }

    @Override
    public String toString() {
        return "idEid("+this.id+"):user="+this.userId+":perm="+this.permission+":ref="+this.nodeId; //+": "+super.toString();
    }

}
